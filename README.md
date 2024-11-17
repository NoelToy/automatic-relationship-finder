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

## Usage/Examples
### Add Maven Dependency
```xml
<dependency>
    <groupId>io.github.noeltoy</groupId>
    <artifactId>automatic-relationship-finder</artifactId>
    <version>1.1</version>
</dependency>
```
### Example
For usage please refer test package.
## License
[Apache License 2.0](https://choosealicense.com/licenses/apache-2.0/)

## Authors
- [@noeltoy](https://github.com/NoelToy)