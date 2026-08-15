package io.github.arf;

import com.cobber.fta.*;
import com.cobber.fta.core.FTAPluginException;
import com.cobber.fta.core.FTAType;
import com.cobber.fta.core.FTAUnsupportedLocaleException;
import com.cobber.fta.dates.DateTimeParser;
import io.github.arf.lib.converters.TableConverter;
import io.github.arf.lib.exceptions.ConfidenceValueRageException;
import io.github.arf.lib.models.IntermediateRelationship;
import io.github.arf.lib.models.Relationship;
import io.github.arf.lib.models.RelationshipColumn;
import io.github.arf.lib.models.Table;
import io.github.arf.lib.models.constants.ColumnRole;
import io.github.arf.lib.models.constants.DataTypes;
import io.github.arf.lib.models.internal.ColumnSet;
import io.github.arf.lib.models.internal.InternalTable;
import io.github.arf.lib.models.internal.Row;
import io.github.arf.lib.util.ColumnRoleResolver;
import io.github.arf.lib.util.JaccardIndex;
import io.github.arf.lib.util.ListToArray;
import io.github.fwm.WordMatcher;
import io.github.fwm.lib.enums.MatchType;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Pattern;
import java.util.stream.IntStream;

public class AutomaticRelationshipFinder<T> {

    private final List<InternalTable> xTables;
    private final double columnNameConfidence;
    private final double dataConfidence;
    private final List<DataTypes> ignoreDatatypes;
    private final List<String> ignoreColumnNamePatterns;
    private final int threadCount;

    private AutomaticRelationshipFinder(List<Table<T>> tables, double columnNameConfidence, double dataConfidence, List<DataTypes> ignoreDatatypes,List<String> ignoreColumnNamePatterns,int threadCount){
        this.xTables = TableConverter.convertAll(tables);
        this.columnNameConfidence = columnNameConfidence;
        this.dataConfidence = dataConfidence;
        this.ignoreDatatypes = ignoreDatatypes;
        this.ignoreColumnNamePatterns = ignoreColumnNamePatterns;
        this.threadCount = threadCount;
    }

    public static class AutomaticRelationshipFinderBuilder<T>{
        private final List<Table<T>> tables;
        private double columnNameConfidence = 0.4;
        private double dataConfidence = 0.6;
        private final List<DataTypes> ignoreDatatypes = new ArrayList<>();
        private final List<String> ignoreColumnNamePatterns = new ArrayList<>();
        private int threadCount;

        public AutomaticRelationshipFinderBuilder(List<Table<T>> tables){
            this.tables = tables;
            int availableCores = Runtime.getRuntime().availableProcessors()-1;
            int safeThreadCount = Math.max(1, availableCores);
            this.threadCount = Math.max(1, Math.min(this.tables.size(), safeThreadCount));
        }

        public AutomaticRelationshipFinderBuilder<T> setColumnNameConfidence(double value){
            if (this.isOutOfRange(value)) {
                throw new ConfidenceValueRageException("Column name confidence value range should be between 0.0 and 1.0");
            }
            this.columnNameConfidence = value;
            return this;
        }

        public AutomaticRelationshipFinderBuilder<T> setDataConfidence(double value){
            if (this.isOutOfRange(value)) {
                throw new ConfidenceValueRageException("Data confidence value range should be between 0.0 and 1.0");
            }
            this.dataConfidence = value;
            return this;
        }

        public AutomaticRelationshipFinderBuilder<T> setIgnoreDatatypes(List<DataTypes> ignoreDatatypes){
            this.ignoreDatatypes.addAll(ignoreDatatypes);
            return this;
        }

        public AutomaticRelationshipFinderBuilder<T> setIgnoreColumnNamePatterns(List<String> ignoreColumnNamePatterns){
            this.ignoreColumnNamePatterns.addAll(ignoreColumnNamePatterns);
            return this;
        }

        public AutomaticRelationshipFinderBuilder<T> setThreadCount(int threadCount){
            if (threadCount < 1) {
                throw new IllegalArgumentException("threadCount must be >= 1");
            }
            this.threadCount = threadCount;
            return this;
        }

        public AutomaticRelationshipFinder<T> builder(){
            return new AutomaticRelationshipFinder<>(this.tables,
                    this.columnNameConfidence,
                    this.dataConfidence,
                    this.ignoreDatatypes,
                    this.ignoreColumnNamePatterns,
                    this.threadCount);
        }

        private boolean isOutOfRange(double value) {
            return value < 0.0 || value > 1.0;
        }
    }

    public List<Relationship> findRelationShip(){
        ConcurrentHashMap<String,RecordAnalysisResult> tableAnalyzesResult = new ConcurrentHashMap<>();
//        Analyzing each table using FTA
        ExecutorService executor = Executors.newFixedThreadPool(this.threadCount);
        List<CompletableFuture<Void>> futures = xTables.stream()
                .map(table -> CompletableFuture.runAsync(() -> {
                    AnalyzerContext context = new AnalyzerContext(null, DateTimeParser.DateResolutionMode.Auto, table.tableName(), table.columnNames());
                    TextAnalyzer template = new TextAnalyzer(context);
                    RecordAnalyzer analysis = new RecordAnalyzer(template);
                    for (Row row : table.rows()) {
                        try {
                            analysis.train(row.values());
                        } catch (FTAPluginException | FTAUnsupportedLocaleException e) {
                            throw new RuntimeException(e);
                        }
                    }
                    try {
                        tableAnalyzesResult.put(table.tableName(), analysis.getResult());
                    } catch (FTAPluginException | FTAUnsupportedLocaleException e) {
                        throw new RuntimeException(e);
                    }
                }, executor)).toList();

        for (CompletableFuture<Void> future : futures) {
            future.join();
        }

        executor.shutdown();
//        Checking datatypes and forming intermediate relationships
        List<List<IntermediateRelationship>> intermediateTableRelationships = new ArrayList<>();
        List<String> ignoreDatatypeList = ignoreDatatypes.stream().map(Enum::name).toList();
        for (int i = 0; i < xTables.size(); i++) {
            String tableAName = xTables.get(i).tableName();
            List<String> columnDataTypesA = getColumnDataTypes(tableAnalyzesResult.get(tableAName).getStreamResults());
            WordMatcher wordMatcher = new WordMatcher.WordMatcherBuilder(Arrays.stream(xTables.get(i).columnNames()).map(String::toLowerCase).toList(), MatchType.COSINE_SIMILARITY)
                    .setTolerance(columnNameConfidence)
                    .setThreshold(columnNameConfidence)
                    .setDefaultValue(null).build();
            for (int j = i+1; j < xTables.size(); j++) {
                String tableBName = xTables.get(j).tableName();
                List<String> columnDataTypesB = getColumnDataTypes(tableAnalyzesResult.get(tableBName).getStreamResults());
                int finalI = i;
                int finalJ = j;
                List<IntermediateRelationship> intermediateRelationships = IntStream.range(0, columnDataTypesA.size())
                        .boxed()
                        .flatMap(k -> IntStream.range(0, columnDataTypesB.size())
                                .filter(l-> notInIgnoreColumnNames(xTables.get(finalI).columnNames()[k],xTables.get(finalJ).columnNames()[l]))
                                .filter(l -> columnDataTypesA.get(k).equals(columnDataTypesB.get(l)) && !ignoreDatatypeList.contains(columnDataTypesA.get(k)))
                                .mapToObj(l -> {
                                    boolean columnNameMatch = isColumnNameMatch(wordMatcher, xTables.get(finalI).columnNames()[k].toLowerCase(), xTables.get(finalJ).columnNames()[l].toLowerCase());
                                    double dataSimilarity = getJaccardIndex(xTables.get(finalI).rows(), xTables.get(finalJ).rows(), k, l);
                                    return new IntermediateRelationship(finalI, finalJ, ColumnSet.single(k), ColumnSet.single(l),columnNameMatch,dataSimilarity);
                                } ))
                        .filter(ir -> ir.isColumnNameMatch() && ir.dataSimilarity() >= dataConfidence)
                        .toList();
                intermediateTableRelationships.add(intermediateRelationships);
            }
        }
//        Generating out relationships
        return intermediateTableRelationships.stream()
                .flatMap(Collection::stream)
                .map(this::toRelationship).toList();

    }

    private List<String> getColumnDataTypes(TextAnalysisResult[] analysisResult){
        List<String> columnDataTypes = new ArrayList<>();
        for (TextAnalysisResult result : analysisResult) {
            if (isTypeBoolean(result)) {
                columnDataTypes.add(DataTypes.BOOLEAN.name());
            }
            else {
                columnDataTypes.add(result.getType().name());
            }
        }
        return columnDataTypes;
    }

    private boolean isColumnNameMatch(WordMatcher wordMatcher,String columnNameA,String columnNameB){
        String match = wordMatcher.findBestMatch(columnNameB);
        return match != null && match.equalsIgnoreCase(columnNameA);
    }

    private <R>List<R> extractColumnData(List<List<R>> data, int index){
        List<R> columnData = new ArrayList<>();
        for (List<R> row : data) {
            columnData.add(row.get(index));
        }
        return columnData;
    }

    private double getJaccardIndex(Row[] rowsOfTableA, Row[] rowsOfTableB, int colIndexA, int colIndexB){
        Set<String> a = new HashSet<>(extractColumnData(rowsOfTableA,colIndexA));
        Set<String> b = new HashSet<>(extractColumnData(rowsOfTableB,colIndexB));
        return JaccardIndex.getSimilarity(a,b);
    }

    private List<String> extractColumnData(Row[] rows,int index){
        List<String> columnData = new ArrayList<>();
        for (Row row : rows) {
            columnData.add(row.values()[index]);
        }
        return columnData;
    }

    private boolean isTypeBoolean(TextAnalysisResult result){
        if (FTAType.BOOLEAN.equals(result.getType())) {
            return true;
        }
        if(result.getDistinctCount()==2){
            if(result.getMinValue().equalsIgnoreCase("f") && result.getMaxValue().equalsIgnoreCase("t")){
                return true;
            }
            else if(result.getMinValue().equalsIgnoreCase("n") && result.getMaxValue().equalsIgnoreCase("y")){
                return true;
            }
            else return result.getMinValue().equalsIgnoreCase("no") && result.getMaxValue().equalsIgnoreCase("yes");

        }
        else {
            return false;
        }
    }

    private boolean notInIgnoreColumnNames(String columnA, String columnB){
        return ignoreColumnNamePatterns.stream()
                .noneMatch(regex-> Pattern.compile(regex,Pattern.CASE_INSENSITIVE)
                        .matcher(columnA).matches()) && ignoreColumnNamePatterns.stream()
                .noneMatch(regex-> Pattern.compile(regex,Pattern.CASE_INSENSITIVE)
                        .matcher(columnB).matches());
    }

    private Relationship toRelationship(IntermediateRelationship ir){
        InternalTable fromTable = xTables.get(ir.fromTableIndex());
        InternalTable toTable = xTables.get(ir.toTableIndex());

        String fromColumnName = fromTable.getColumnName(ir.fromColumns());
        String toColumnName = toTable.getColumnName(ir.toColumns());

        int fromColIndex = fromTable.getColumnIndex(fromColumnName);
        int toColIndex = toTable.getColumnIndex(toColumnName);

        ColumnRole[] roles = ColumnRoleResolver.resolve(
                fromTable.rows(), toTable.rows(), fromColIndex, toColIndex);
        return new Relationship(
                fromTable.tableName(),
                toTable.tableName(),
                new RelationshipColumn(fromColumnName, roles[0]),
                new RelationshipColumn(toColumnName, roles[1]),
                ir.dataSimilarity());
    }
}
