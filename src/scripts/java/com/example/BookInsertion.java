package com.example;

import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

public class BookInsertion {

    /*

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
    final NamedParameterJdbcTemplate namedjdbc;
    final File booksCSV = new File(
        "data/goodbooks-10k-extended/books_enriched.csv"
    );
    final File sampleBooksCSV = new File(
        "data/goodbooks-10k-extended/sample.csv"
    );
    private static final String sql = """
              INSERT INTO books(
                                 id                       ,
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
                                 :id                       ,
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
    final String createAuthorSQL = """
        INSERT INTO authors (name) VALUES (:name)
        ON CONFLICT (name) DO UPDATE SET name = EXCLUDED.name
        RETURNING id
        """;

    final String insertAuthorBook = """
        INSERT INTO book_authors (book_id, author_id)
        VALUES (:bookId, :authorId)
        ON CONFLICT DO NOTHING;
        """;

    public BookInsertion(
        NamedParameterJdbcTemplate namedParameterJdbcTemplate
    ) {
        this.namedjdbc = namedParameterJdbcTemplate;
    }

    public void runner() {
        try (CSVReader reader = new CSVReader(new FileReader(booksCSV))) {
            String[] nextRecord;
            var header = reader.readNext();
            if (header == null) {
                System.out.println("Empty file");
                return;
            }
            var counter = 1;
            while ((nextRecord = reader.readNext()) != null) {
                if (header.length != nextRecord.length) {
                    System.out.println("Wrong lenght at item " + counter);
                    return;
                }
                var line = currentLineToMap(header, nextRecord);
                var params = mapToSqlParams(line);
                params.addValue("id", counter);
                namedjdbc.update(sql, params);
                handleAuthors(counter, line.get("authors"));
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

    @Transactional
    private void handleAuthors(int book_id, String authors) {
        var authorName = getAuthor(authors);
        if (authorName.equals("")) {
            return;
        }
        var authorParams = new MapSqlParameterSource().addValue(
            "name",
            authorName
        );
        var authorId = namedjdbc.queryForObject(
            createAuthorSQL,
            authorParams,
            Integer.class
        );
        if (authorId == null) {
            throw new IllegalStateException("Failed to obtain author ID.");
        }
        var joinParams = new MapSqlParameterSource()
            .addValue("bookId", book_id)
            .addValue("authorId", authorId);

        namedjdbc.update(insertAuthorBook, joinParams);
    }

    private String getAuthor(String authors) {
        var sb = new StringBuilder();
        for (char c : authors.substring(2).toCharArray()) {
            if (sb.isEmpty() && c == '\'') {
                return "";
            }
            if (c == '\'') {
                break;
            }
            sb.append(c);
        }
        return sb.toString();
    }

    private MapSqlParameterSource mapToSqlParams(Map<String, String> row) {
        var params = new MapSqlParameterSource();
        for (String header : row.keySet()) {
            switch (header) {
                //ignore
                case
                    "",
                    "authors_2",
                    "isbn",
                    "authors",
                    "original_publication_year",
                    "original_title",
                    "small_image_url" -> {
                }
                // float
                case "average_rating", "isbn13", "pages" -> params.addValue(
                    header,
                    parseFloatsSafe(row.get(header))
                );
                // raw String
                case
                    "description",
                    "image_url",
                    "language_code",
                    "publishDate",
                    "title",
                    "genres" -> params.addValue(header, row.get(header));
                // int
                default -> params.addValue(
                    header,
                    Integer.parseInt(row.get(header))
                );
            }
        }
        return params;
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
