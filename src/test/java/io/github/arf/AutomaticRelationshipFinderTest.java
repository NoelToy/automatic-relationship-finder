package io.github.arf;

import io.github.arf.lib.models.Relationship;
import io.github.arf.lib.models.Table;
import org.junit.jupiter.api.Test;

import java.util.List;

public class AutomaticRelationshipFinderTest {
    @Test
    public void automaticRelationshipFinderTest(){
        String tableAName = "Customer";
        List<String> columnNamesA = List.of("PK_Customer_ID","First", "Last", "Age");
        List<List<String>> dataTableA = List.of(
                List.of("1","Anaïs", "Nin","45"),
                List.of("2","Gertrude", "Stein","52"),
                List.of("3","Paul", "Campbell","20"),
                List.of("4","Pablo", "Picasso","28"),
                List.of("5","Theodore", "Camp","34"),
                List.of("6","Henri", "Matisse","28"),
                List.of("7","Georges", "Braque","54"),
                List.of("8","Ernest", "Hemingway","55"),
                List.of("9","Alice", "Toklas","45"),
                List.of("10","Eleanor", "Roosevelt","28"),
                List.of("11","Edgar", "Degas","47"),
                List.of("12","Pierre-Auguste", "Wren","59"),
                List.of("13","Claude", "Monet","28"),
                List.of("14","Édouard", "Sorenson","25"),
                List.of("15","Mary", "Dunning","88"),
                List.of("16","Alfred", "Jones","77"),
                List.of("17","Joseph", "Smith","41"),
                List.of("18","Camille", "Pissarro","47"),
                List.of("19","Franklin", "Roosevelt","57"),
                List.of("20","Winston", "Churchill","78")
        );
        Table<String> tableA = new Table<>(tableAName,columnNamesA,dataTableA);
        String tableBName = "Orders";
        List<String> columnNamesB = List.of("ORDER_ID","Quantity", "Product_ID", "FK_Customer_ID");
        List<List<String>> dataTableB = List.of(
                List.of("OD001","2","P002","1"),
                List.of("OD003","20","P012","2"),
                List.of("OD003","28","P008","8"),
                List.of("OD004","8","P009","3"),
                List.of("OD005","14","P022","4"),
                List.of("OD006","17","P109","5"),
                List.of("OD007","1","P700","13"),
                List.of("OD008","3","P802","7"),
                List.of("OD009","7","P052","11"),
                List.of("OD010","1","P092","12"),
                List.of("OD011","1","P042","18"),
                List.of("OD012","3","P072","19")
        );
        Table<String> tableB = new Table<>(tableBName,columnNamesB,dataTableB);
        List<Table<String>> tables = List.of(tableA, tableB);

        AutomaticRelationshipFinder<String> relationshipFinder = new AutomaticRelationshipFinder.AutomaticRelationshipFinderBuilder<>(tables)
                .setColumnNameConfidence(0.4)
                .setColumnNameConfidence(0.5)
                .builder();
        List<Relationship> relationships = relationshipFinder.findRelationShip();
        relationships.forEach(relationship -> System.out.println("From Table: "+relationship.fromTable()+" | To Table: "+relationship.toTable()+" | From Column: "+relationship.fromColumnName()+" | To Column: "+relationship.toColumnName()));
    }
}
