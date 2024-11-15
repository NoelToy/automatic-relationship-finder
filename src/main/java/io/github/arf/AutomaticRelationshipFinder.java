package io.github.arf;

import com.cobber.fta.*;
import com.cobber.fta.core.FTAPluginException;
import com.cobber.fta.core.FTAUnsupportedLocaleException;
import com.cobber.fta.dates.DateTimeParser;
import io.github.arf.lib.exceptions.ConfidenceValueRageException;
import io.github.arf.lib.models.IntermediateRelationship;
import io.github.arf.lib.models.Relationship;
import io.github.arf.lib.models.Table;
import io.github.arf.lib.models.constants.DataTypes;
import io.github.arf.lib.util.JaccardIndex;
import io.github.arf.lib.util.ListToArray;
import io.github.fwm.WordMatcher;
import io.github.fwm.lib.enums.MatchType;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Pattern;
import java.util.stream.IntStream;

public class AutomaticRelationshipFinder<T> {

    private final List<Table<T>> tables;
    private final double columnNameConfidence;
    private final double dataConfidence;
    private final List<DataTypes> ignoreDatatypes;
    private final List<String> ignoreColumnNamePatterns;
    private final int threadCount;

    private AutomaticRelationshipFinder(List<Table<T>> tables, double columnNameConfidence, double dataConfidence, List<DataTypes> ignoreDatatypes,List<String> ignoreColumnNamePatterns,int threadCount){
        this.tables = tables;
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
            int threadCount = Runtime.getRuntime().availableProcessors()-1;
            this.threadCount = Math.min(this.tables.size(), threadCount);
        }

        public AutomaticRelationshipFinderBuilder<T> setColumnNameConfidence(double value){
            if (this.isValidRange(value)) {
                throw new ConfidenceValueRageException("Column name confidence value range should be between 0.0 and 1.0");
            }
            this.columnNameConfidence = value;
            return this;
        }

        public AutomaticRelationshipFinderBuilder<T> setDataConfidence(double value){
            if (this.isValidRange(value)) {
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

        private boolean isValidRange(double value) {
            return !(value <= 1.0) || !(value >= 0.0);
        }

    }

    public List<Relationship> findRelationShip(){
        Map<String,RecordAnalysisResult> tableAnalyzesResult = new HashMap<>();
//        Analyzing each table using FTA
        ExecutorService executor = Executors.newFixedThreadPool(this.threadCount);
        List<CompletableFuture<Void>> futures = tables.stream()
                .map(table -> CompletableFuture.runAsync(() -> {
                    AnalyzerContext context = new AnalyzerContext(null, DateTimeParser.DateResolutionMode.Auto, table.name(), ListToArray.convertToArray(table.columns()));
                    TextAnalyzer template = new TextAnalyzer(context);
                    RecordAnalyzer analysis = new RecordAnalyzer(template);
                    for (String[] data : ListToArray.convertTo2DArray(table.data())) {
                        try {
                            analysis.train(data);
                        } catch (FTAPluginException | FTAUnsupportedLocaleException e) {
                            throw new RuntimeException(e);
                        }
                    }

                    try {
                        synchronized (tableAnalyzesResult) {
                            tableAnalyzesResult.put(table.name(), analysis.getResult());
                        }
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
        for (int i = 0; i < tables.size(); i++) {
            String tableAName = tables.get(i).name();
            List<String> columnDataTypesA = getColumnDataTypes(tableAnalyzesResult.get(tableAName).getStreamResults());
            WordMatcher wordMatcher = new WordMatcher.WordMatcherBuilder(tables.get(i).columns().stream().map(String::toLowerCase).toList(), MatchType.COSINE_SIMILARITY)
                    .setTolerance(columnNameConfidence)
                    .setThreshold(columnNameConfidence)
                    .setDefaultValue(null).build();
            for (int j = i+1; j < tables.size(); j++) {
                String tableBName = tables.get(j).name();
                List<String> columnDataTypesB = getColumnDataTypes(tableAnalyzesResult.get(tableBName).getStreamResults());
                int finalI = i;
                int finalJ = j;
                List<IntermediateRelationship> intermediateRelationships = IntStream.range(0, columnDataTypesA.size())
                        .boxed()
                        .flatMap(k -> IntStream.range(0, columnDataTypesB.size())
                                .filter(l-> notInIgnoreColumnNames(tables.get(finalI).columns().get(k),tables.get(finalJ).columns().get(l)))
                                .filter(l -> columnDataTypesA.get(k).equals(columnDataTypesB.get(l)) && !ignoreDatatypeList.contains(columnDataTypesA.get(k)))
                                .mapToObj(l -> {
                                    boolean columnNameMatch = isColumnNameMatch(wordMatcher, tables.get(finalI).columns().get(k).toLowerCase(), tables.get(finalJ).columns().get(l).toLowerCase());
                                    double dataSimilarity = getJaccardIndex(tables.get(finalI).data(), tables.get(finalJ).data(), k, l);
                                    return new IntermediateRelationship(finalI, finalJ, k, l,columnNameMatch,dataSimilarity);
                                } ))
                        .toList();
                intermediateTableRelationships.add(intermediateRelationships);
            }
        }
//        Filtering out relationships
        return intermediateTableRelationships.stream()
                .flatMap(r -> r.stream()
                        .filter(c -> c.isColumnNameMatch() && c.dataSimilarity() >= dataConfidence))
                .map(ir -> new Relationship(
                        tables.get(ir.fromTableIndex()).name(),
                        tables.get(ir.toTableIndex()).name(),
                        tables.get(ir.fromTableIndex()).columns().get(ir.fromColumnIndex()),
                        tables.get(ir.toTableIndex()).columns().get(ir.toColumnIndex())))
                .toList();

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

    private <R> double getJaccardIndex(List<List<R>> dataA, List<List<R>> dataB, int colIndexA, int colIndexB){
        Set<R> a = new HashSet<>(extractColumnData(dataA,colIndexA));
        Set<R> b = new HashSet<>(extractColumnData(dataB,colIndexB));
        return JaccardIndex.getSimilarity(a,b);
    }

    private <R>List<R> extractColumnData(List<List<R>> data, int index){
        List<R> columnData = new ArrayList<>();
        for (List<R> row : data) {
            columnData.add(row.get(index));
        }
        return columnData;
    }
    private boolean isTypeBoolean(TextAnalysisResult result){
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
}
