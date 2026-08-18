[![License](https://img.shields.io/badge/License-Apache_2.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)
[![Maven Central](https://img.shields.io/maven-central/v/io.github.noeltoy/automatic-relationship-finder.svg?label=Maven%20Central)](https://search.maven.org/artifact/io.github.noeltoy/automatic-relationship-finder)
[![GitHub release (latest by date)](https://img.shields.io/github/v/release/noeltoy/automatic-relationship-finder?logo=GitHub)](https://github.com/noeltoy/automatic-relationship-finder/releases)
[![javadoc](https://javadoc.io/badge2/io.github.noeltoy/automatic-relationship-finder/javadoc.svg)](https://javadoc.io/doc/io.github.noeltoy/automatic-relationship-finder)
# Automatic Relationship Finder (ARF)

**Automatic Relationship Finder (ARF)** is a Java library that automatically detects implicit relationships between database tables by analyzing column names and data patterns. Designed for OLTP environments where physical relationships may not be defined at the RDBMS level, ARF allows users to configure confidence thresholds for column name and data matching, fine-tuning relationship detection precision. Additionally, ARF provides control over which data types should be considered in relationship checks, ensuring context-specific and targeted analysis.
## Key Features
+ **Automatic Relationship Detection**: ARF identifies relationships between tables in relational databases by analyzing column names and data values, making it useful for databases where physical relationships (like foreign keys) may not be explicitly defined.
+ **Configurable Column Name Matching**: Allows users to specify a confidence level for column name matching, helping the library to recognize and match similarly named columns (e.g., Dist_Code and District Code) based on customizable thresholds.
+ **Data-Based Relationship Matching**: In addition to column names, ARF uses actual data values to detect relationships, allowing for more accurate and context-based results.
+ **Customizable Data Match Confidence**: Users can define a confidence level for data matching, adjusting the sensitivity for relationship detection based on how closely values should match.
+ **Data Type Selection for Matching**: Provides the flexibility to specify which data types (e.g., integers, strings, dates) should be considered when checking for relationships, enabling tailored analysis for different types of data.
+ **Adaptability for OLTP Systems**: Designed with Online Transaction Processing (OLTP) systems in mind, ARF can detect implied relationships in transactional data where explicit keys are not always present.
+ **Java-Based and Easily Integrable**: ARF is built in Java, making it compatible with Java-based applications and libraries, and straightforward to integrate into existing data processing or cataloging systems.
+ **Primary Key / Foreign Key Role Detection (new in 1.2)**: For every relationship it detects, ARF also infers which side looks like the primary key and which looks like the foreign key, based on uniqueness and null analysis of the two columns involved. See [Column Roles](#column-roles) below.

## Use Cases
+ **Data Cataloging and Discovery in Legacy Databases**: Many legacy databases lack explicit primary key-foreign key relationships, which can make data cataloging challenging. ARF helps data cataloging tools infer these implicit relationships, enabling users to understand data linkages without requiring schema modifications.
+ **ETL (Extract, Transform, Load) Optimization**: In ETL workflows, it’s often crucial to identify relationships between tables to accurately join data from different sources. ARF automates the detection of these relationships, making it easier to configure ETL pipelines for databases that lack physical constraints.
+ **Data Migration Between Systems**: During data migration, especially between OLTP and OLAP systems, ARF can identify hidden relationships within the source data, helping preserve referential integrity and data structure during transformation and loading into the target system.
+ **Database Reverse Engineering**: ARF assists in reverse-engineering undocumented databases by discovering implied relationships between tables, making it easier for developers and data analysts to comprehend the structure and meaning of the data.
+ **Data Quality and Integrity Audits**: By detecting unlinked but related columns, ARF can help data quality tools flag potential data integrity issues, such as missing foreign key constraints or inconsistent data relationships across tables.
+ **Intelligent Data Integration**: When integrating data from multiple sources, ARF can identify potential joins across databases or tables that lack explicit relationships. This capability supports building unified data views and data marts from diverse systems.
+ **Metadata Enrichment for Data Lakes**: For data lakes containing relational data, ARF can help enrich metadata by detecting and documenting relationships. This metadata enrichment supports improved data discovery and governance.
+ **Machine Learning Data Preparation**: In machine learning pipelines, discovering relationships between datasets is essential for feature engineering. ARF helps data scientists automatically detect related tables and columns, making it easier to create joinable datasets and improve model inputs.
+ **Data Lineage Tracking**: Understanding data lineage involves tracing relationships between datasets over time. ARF can aid in capturing implicit relationships as part of a data lineage tracking system, adding context to lineage data that lacks defined foreign keys.

## Dependencies
+ **Java 17 or higher**: The minimum required Java version to run the library.

## Migrating from 1.1
Version 1.2 changes the shape of `Relationship`:
+ `Relationship.fromColumnName()` and `toColumnName()` still work but are **deprecated as of 1.2, scheduled for removal in 1.3**. Replace them with `relationship.fromColumn().columnName()` and `relationship.toColumn().columnName()`.
+ `Relationship` now also carries `fromColumn().role()` and `toColumn().role()` — see [Column Roles](#column-roles).
+ `Relationship` gained a `dataSimilarity` field (a `double` similarity score for the matched data), returned alongside every relationship.

## Usage/Examples
### Add Maven Dependency
```xml
<dependency>
    <groupId>io.github.noeltoy</groupId>
    <artifactId>automatic-relationship-finder</artifactId>
    <version>1.2</version>
</dependency>
```

### Example
```java
String tableAName = "Customer";
List<String> columnNamesA = List.of("PK_Customer_ID", "First", "Last", "Age", "IsActive");
List<List<String>> dataTableA = List.of(
        List.of("1", "Anaïs", "Nin", "45", "0"),
        List.of("2", "Gertrude", "Stein", "52", "1")
        // ...
);
Table<String> tableA = new Table<>(tableAName, columnNamesA, dataTableA);

String tableBName = "Orders";
List<String> columnNamesB = List.of("ORDER_ID", "Quantity", "Product_ID", "FK_Customer_ID", "Is_Active");
List<List<String>> dataTableB = List.of(
        List.of("OD001", "2", "P002", "1", "0"),
        List.of("OD003", "20", "P012", "2", "1")
        // ...
);
Table<String> tableB = new Table<>(tableBName, columnNamesB, dataTableB);

List<Table<String>> tables = List.of(tableA, tableB);
List<String> ignoreColumPatterns = List.of("is.*");

AutomaticRelationshipFinder<String> relationshipFinder =
        new AutomaticRelationshipFinder.AutomaticRelationshipFinderBuilder<>(tables)
                .setColumnNameConfidence(0.4)
                .setDataConfidence(0.5)
                .setIgnoreColumnNamePatterns(ignoreColumPatterns)
                .builder();

List<Relationship> relationships = relationshipFinder.findRelationShip();

relationships.forEach(relationship -> System.out.println(
        "From Table: " + relationship.fromTable() +
        " | To Table: " + relationship.toTable() +
        " | From Column: " + relationship.fromColumn().columnName() +
        " (" + relationship.fromColumn().role() + ")" +
        " | To Column: " + relationship.toColumn().columnName() +
        " (" + relationship.toColumn().role() + ")" +
        " | Data Similarity Score: " + relationship.dataSimilarity()));
```

For the full runnable example, see the test package (`AutomaticRelationshipFinderTest`).

## Data Similarity

Every `Relationship` includes a `dataSimilarity` score (`double`, range `0.0`–`1.0`) describing how similar the *values* in the two matched columns are — independent of how similar their *names* are (name matching is a separate, earlier filtering step controlled by `setColumnNameConfidence`).

`dataSimilarity` is computed using the **Jaccard index**: each column's values are collected into a set of distinct raw strings, and the score is `|A ∩ B| / |A ∪ B|` — the size of the intersection divided by the size of the union. A score of `1.0` means the two columns contain exactly the same set of distinct values; `0.0` means no overlap at all.

A few practical notes on how this is computed:
+ Comparisons are on **raw string values**, with no case-folding, trimming, or type coercion. `"1"` and `"1 "` are treated as distinct values, as are `"Y"` and `"y"`.
+ The comparison uses **distinct values**, not row counts — a column with `["1","1","2"]` is treated as the set `{"1","2"}`. Frequency/duplication within a column does not affect the score.
+ This check only runs on column pairs whose names already passed `columnNameConfidence`, so it's not run exhaustively across every possible column pair in your dataset.
+ Two entirely empty columns return a similarity of `0.0` rather than an undefined value.

Use `setDataConfidence(...)` on the builder to set the minimum `dataSimilarity` a column pair must reach to be reported as a relationship.

## Column Roles

Starting in version 1.2, every detected `Relationship` includes a `fromColumn` and `toColumn`, each of type `RelationshipColumn(columnName, role)`. The `role` is a `ColumnRole` describing how that column looks *relative to the other column in this specific relationship* — it is not a claim that a column is globally "the" primary key of its table.

| `ColumnRole` | Meaning |
|---|---|
| `PRIMARY_KEY_CANDIDATE` | This column is 100% distinct and non-null; the other side is not. Looks like the "one" side of the relationship. |
| `FOREIGN_KEY_CANDIDATE` | The other column is 100% distinct and non-null; this side is not. Looks like the "many" side of the relationship. |
| `POSSIBLE_ONE_TO_ONE_KEY` | Both columns are 100% distinct and non-null. May indicate a genuine 1:1 relationship, or two independent surrogate keys that happen to be unique in the sample provided. |
| `UNKNOWN` | Neither column is 100% distinct and non-null. A relationship was detected, but no clear key-like structure was found on either side. |

**How it's determined:** a column is treated as a key candidate only if *every* value across all rows is non-null, non-blank, and distinct from every other value in that column. This check runs only on columns that are already part of a detected relationship (i.e., after name/data confidence filtering), so it adds negligible overhead and never affects which relationships are found — only how the two sides of an already-found relationship are labeled.

**Note on sample data:** because this is computed from the rows you provide, a column can resolve to `POSSIBLE_ONE_TO_ONE_KEY` or `PRIMARY_KEY_CANDIDATE` based on a small or non-representative sample (e.g., a foreign key column where, by chance, no value repeats in the given dataset). Treat `ColumnRole` as a heuristic signal to guide review, not a guaranteed schema constraint.

## License
[Apache License 2.0](https://choosealicense.com/licenses/apache-2.0/)

## Authors
- [@noeltoy](https://github.com/NoelToy)