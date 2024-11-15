package io.github.arf;

import io.github.arf.lib.models.Relationship;
import io.github.arf.lib.models.Table;
import org.junit.jupiter.api.Test;

import java.util.List;

public class AutomaticRelationshipFinderTest {
    @Test
    public void automaticRelationshipFinderTest(){
        String tableAName = "Customer";
        List<String> columnNamesA = List.of("PK_Customer_ID","First", "Last", "Age","IsActive");
        List<List<String>> dataTableA = List.of(
                List.of("1","Anaïs", "Nin","45","n"),
                List.of("2","Gertrude", "Stein","52","n"),
                List.of("3","Paul", "Campbell","20","y"),
                List.of("4","Pablo", "Picasso","28","n"),
                List.of("5","Theodore", "Camp","34","y"),
                List.of("6","Henri", "Matisse","28","n"),
                List.of("7","Georges", "Braque","54","y"),
                List.of("8","Ernest", "Hemingway","55","n"),
                List.of("9","Alice", "Toklas","45","y"),
                List.of("10","Eleanor", "Roosevelt","28","n"),
                List.of("11","Edgar", "Degas","47","y"),
                List.of("12","Pierre-Auguste", "Wren","59","n"),
                List.of("13","Claude", "Monet","28","y"),
                List.of("14","Édouard", "Sorenson","25","y"),
                List.of("15","Mary", "Dunning","88","n"),
                List.of("16","Alfred", "Jones","77","n"),
                List.of("17","Joseph", "Smith","41","y"),
                List.of("18","Camille", "Pissarro","47","n"),
                List.of("19","Franklin", "Roosevelt","57","y"),
                List.of("20","Winston", "Churchill","78","n")
        );
        Table<String> tableA = new Table<>(tableAName,columnNamesA,dataTableA);
        String tableBName = "Orders";
        List<String> columnNamesB = List.of("ORDER_ID","Quantity", "Product_ID", "FK_Customer_ID","Is_Active");
        List<List<String>> dataTableB = List.of(
                List.of("OD001","2","P002","1","n"),
                List.of("OD003","20","P012","2","y"),
                List.of("OD003","28","P008","8","n"),
                List.of("OD004","8","P009","3","y"),
                List.of("OD005","14","P022","4","n"),
                List.of("OD006","17","P109","5","y"),
                List.of("OD007","1","P700","13","y"),
                List.of("OD008","3","P802","7","y"),
                List.of("OD009","7","P052","11","y"),
                List.of("OD010","1","P092","12","n"),
                List.of("OD011","1","P042","18","n"),
                List.of("OD012","3","P072","19","y")
        );
        Table<String> tableB = new Table<>(tableBName,columnNamesB,dataTableB);
        List<Table<String>> tables = List.of(tableA, tableB);
        List<String> ignoreColumPatterns = List.of("is.*");
        AutomaticRelationshipFinder<String> relationshipFinder = new AutomaticRelationshipFinder.AutomaticRelationshipFinderBuilder<>(tables)
                .setColumnNameConfidence(0.4)
                .setDataConfidence(0.5)
                .setIgnoreColumnNamePatterns(ignoreColumPatterns)
                .builder();
        List<Relationship> relationships = relationshipFinder.findRelationShip();
        relationships.forEach(relationship -> System.out.println("From Table: "+relationship.fromTable()+" | To Table: "+relationship.toTable()+" | From Column: "+relationship.fromColumnName()+" | To Column: "+relationship.toColumnName()));
    }
}
