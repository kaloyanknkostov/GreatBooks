package com.example;

import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;

public class SimpleTableInsertion {

    final NamedParameterJdbcTemplate namedjdbc;
    final String book_tagsCSV = "data/goodbooks-10k-extended/book_tags.csv";
    final String tagsCSV = "data/goodbooks-10k-extended/tags.csv";

    final String insertBookTagsSQL = """
        INSERT INTO book_tags (goodreads_book_id , tag_id, count)
        VALUES (:goodreads_book_id , :tag_id, :count)
        ON CONFLICT DO NOTHING;
        """;
    // chagned the tags.csv header from tag_id to id only
    final String insertTagsSQL = """
        INSERT INTO tags (id, name)
        VALUES (:id, :name)
        """;

    ArrayList<String> insertQuerySqlList = new ArrayList<String>();
    ArrayList<String> csvPathsList = new ArrayList<String>();

    public SimpleTableInsertion(NamedParameterJdbcTemplate namedjdbc) {
        this.namedjdbc = namedjdbc;
        insertQuerySqlList.add(insertTagsSQL);
        insertQuerySqlList.add(insertBookTagsSQL);
        csvPathsList.add(tagsCSV);
        csvPathsList.add(book_tagsCSV);
    }

    public void runner() {
        var sqlcounter = 0;
        for (String csvTable : csvPathsList) {
            System.out.println("Started with " + csvPathsList);
            try (CSVReader reader = new CSVReader(new FileReader(csvTable))) {
                String[] nextRecord;
                var headers = reader.readNext();
                if (headers == null) {
                    System.out.println("Empty file");
                    return;
                }
                var counter = 0;

                var batchSize = 2000;
                var batch = new ArrayList<SqlParameterSource>();
                while ((nextRecord = reader.readNext()) != null) {
                    if (nextRecord.length != headers.length) {
                        System.out.println("Wrong lenght at item " + counter);
                        return;
                    }
                    var params = new MapSqlParameterSource();
                    var headerCounter = 0;
                    for (String header : headers) {
                        if (header.equals("tag_name")) params.addValue(
                            "name",
                            nextRecord[headerCounter]
                        );
                        /*
                        else if (header.equals("tag_id")) params.addValue(
                            "id",
                            Integer.parseInt(nextRecord[headerCounter])
                        );
                        */
                        else {
                            params.addValue(
                                header,
                                Integer.parseInt(nextRecord[headerCounter])
                            );
                        }
                        headerCounter++;
                    }
                    batch.add(params);
                    if (batch.size() == batchSize) {
                        namedjdbc.batchUpdate(
                            insertQuerySqlList.get(sqlcounter),
                            batch.toArray(new SqlParameterSource[0])
                        );
                        batch.clear();
                    }
                }
                if (!batch.isEmpty()) {
                    namedjdbc.batchUpdate(
                        insertQuerySqlList.get(sqlcounter),
                        batch.toArray(new SqlParameterSource[0])
                    );
                    batch.clear();
                }

                reader.close();
            } catch (FileNotFoundException e) {
                System.out.println("File doesn't exist");
                System.exit(1);
            } catch (IOException e) {
                System.out.println("Error");
                System.exit(1);
            } catch (CsvValidationException e) {
                System.out.println("Error");
                System.exit(1);
            }

            System.out.println("Done with " + csvPathsList);
            sqlcounter++;
        }
    }
}
