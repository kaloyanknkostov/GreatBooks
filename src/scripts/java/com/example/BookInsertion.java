package com.example;

import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

public class BookInsertion {

    /*
TODO authors create new table authors and book authors

for date
1 │import java.time.LocalDate;
    2 │import java.time.format.DateTimeFormatter;
    3 │import java.time.format.DateTimeParseException;
    4 │
    5 │private static final DateTimeFormatter PUBLISH_DATE =
    6 │    DateTimeFormatter.ofPattern("MM/dd/yy");
    7 │
    8 │private static LocalDate parsePublishDateOrNull(String raw) {
    9 │    if (raw == null || raw.isBlank()) {
   10 │        return null;
   11 │    }
   12 │    try {
   13 │        return LocalDate.parse(raw.trim(), PUBLISH_DATE);
   14 │    } catch (DateTimeParseException e) {
   15 │        return null; // or throw with book_id for debugging
   16 │    }
   17 │}
*/
    private final NamedParameterJdbcTemplate namedjdbc;
    private static final String sql = """
              INSERT INTO books(
                                 average_rating           ,   -- float
                                 best_book_id             ,
                                 book_id                  ,
                                 books_count              ,
                                 description              ,   --String
                                 genres                   ,   --String
                                 goodreads_book_id        ,
                                 image_url                ,   --String
                                 isbn13                   ,   --float
                                 language_code            ,   --String
                                 pages                    ,   --float
                                 -- publishDate              ,   --DATE
                                 ratings_1                ,
                                 ratings_2                ,
                                 ratings_3                ,
                                 ratings_4                ,
                                 ratings_5                ,
                                 ratings_count            ,
                                 title                    ,   --String
                                 work_id                  ,
                                 work_ratings_count       ,
                                 work_text_reviews_count
          )
          Values(
                                 :average_rating           ,   -- float
                                 :best_book_id             ,
                                 :book_id                  ,
                                 :books_count              ,
                                 :description              ,   --String
                                 :genres                   ,   --String
                                 :goodreads_book_id        ,
                                 :image_url                ,   --String
                                 :isbn13                   ,   --float
                                 :language_code            ,   --String
                                 :pages                    ,   --float
                                 --:publishDate              ,   --DATE
                                 :ratings_1                ,
                                 :ratings_2                ,
                                 :ratings_3                ,
                                 :ratings_4                ,
                                 :ratings_5                ,
                                 :ratings_count            ,
                                 :title                    ,   --String
                                 :work_id                  ,
                                 :work_ratings_count       ,
                                 :work_text_reviews_count
                    )
        """;

    public BookInsertion(
        NamedParameterJdbcTemplate namedParameterJdbcTemplate
    ) {
        this.namedjdbc = namedParameterJdbcTemplate;
    }

    public void runner() {
        var booksCSV = new File(
            "data/goodbooks-10k-extended/books_enriched.csv"
        );
        // sample
        //booksCSV = new File("data/goodbooks-10k-extended/sample.csv");
        try (CSVReader reader = new CSVReader(new FileReader(booksCSV))) {
            String[] nextRecord;
            String[] header = reader.readNext();
            if (header == null) {
                System.out.println("Empty file");
                return;
            }
            var counter = 0;
            while ((nextRecord = reader.readNext()) != null) {
                if (header.length != nextRecord.length) {
                    System.out.println("Wrong lenght at item " + counter);
                    return;
                }
                var params = mapToSqlParams(
                    currentLineToMap(header, nextRecord)
                );
                namedjdbc.update(sql, params);
                counter++;
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
    }

    private MapSqlParameterSource mapToSqlParams(Map<String, String> row) {
        return new MapSqlParameterSource()
            .addValue(
                "average_rating",
                parseFloatsSafe(row.get("average_rating"))
            )
            .addValue("best_book_id", Integer.parseInt(row.get("best_book_id")))
            .addValue("book_id", Integer.parseInt(row.get("book_id")))
            .addValue("books_count", Integer.parseInt(row.get("books_count")))
            .addValue("description", row.get("description"))
            .addValue("genres", row.get("genres"))
            .addValue(
                "goodreads_book_id",
                Integer.parseInt(row.get("goodreads_book_id"))
            )
            .addValue("image_url", row.get("image_url"))
            .addValue("isbn13", parseFloatsSafe(row.get("isbn13")))
            .addValue("language_code", row.get("language_code"))
            .addValue("pages", parseFloatsSafe(row.get("pages")))
            //.addValue("publishDate", row.get("publishDate"))
            .addValue("ratings_1", Integer.parseInt(row.get("ratings_1")))
            .addValue("ratings_2", Integer.parseInt(row.get("ratings_2")))
            .addValue("ratings_3", Integer.parseInt(row.get("ratings_3")))
            .addValue("ratings_4", Integer.parseInt(row.get("ratings_4")))
            .addValue("ratings_5", Integer.parseInt(row.get("ratings_5")))
            .addValue(
                "ratings_count",
                Integer.parseInt(row.get("ratings_count"))
            )
            .addValue("title", row.get("title"))
            .addValue("work_id", Integer.parseInt(row.get("work_id")))
            .addValue(
                "work_ratings_count",
                Integer.parseInt(row.get("work_ratings_count"))
            )
            .addValue(
                "work_text_reviews_count",
                Integer.parseInt(row.get("work_text_reviews_count"))
            );
    }

    private Float parseFloatsSafe(String input) {
        if (input == null || input.isBlank()) return null;
        return Float.parseFloat(input);
    }

    private Map<String, String> currentLineToMap(
        String[] header,
        String[] line
    ) {
        var map = new HashMap<String, String>();
        for (int i = 1; i < line.length; i++) {
            map.put(header[i], line[i]);
        }
        return map;
    }
}
