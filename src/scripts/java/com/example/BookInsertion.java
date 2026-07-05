package com.example;

import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.transaction.annotation.Transactional;

public class BookInsertion {

    /*
09/14/08
for date
import java.time.LocalDate;
     import java.time.format.DateTimeFormatter;
     import java.time.format.DateTimeParseException;

     private static final DateTimeFormatter PUBLISH_DATE =
         DateTimeFormatter.ofPattern("MM/dd/yy");

     private static LocalDate parsePublishDateOrNull(String raw) {
         if (raw == null || raw.isBlank()) {
            return null;
        }
        try {
           return LocalDate.parse(raw.trim(), PUBLISH_DATE);
        } catch (DateTimeParseException e) {
            return null; // or throw with book_id for debugging
        }
    }
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
                                 publishDate              ,   --DATE
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
                                 :publishDate              ,   --DATE
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
            var counter = 0;
            var batchSize = 2000;
            var batch = new ArrayList<SqlParameterSource>();
            var batchAuthor = new ArrayList<SqlParameterSource>();
            while ((nextRecord = reader.readNext()) != null) {
                if (header.length != nextRecord.length) {
                    System.out.println("Wrong lenght at item " + counter);
                    return;
                }
                var line = currentLineToMap(header, nextRecord);
                var params = mapToSqlParams(line);
                params.addValue("id", counter);
                batch.add(params);
                var author = handleAuthors(counter, line.get("authors"));
                if (author != null) {
                    batchAuthor.add(author);
                }
                if (batch.size() == batchSize) {
                    namedjdbc.batchUpdate(
                        sql,
                        batch.toArray(new SqlParameterSource[0])
                    );
                    namedjdbc.batchUpdate(
                        insertAuthorBook,
                        batchAuthor.toArray(new SqlParameterSource[0])
                    );
                    batch.clear();
                    batchAuthor.clear();
                }
                counter++;
            }
            if (!batch.isEmpty()) {
                namedjdbc.batchUpdate(
                    sql,
                    batch.toArray(new SqlParameterSource[0])
                );
                namedjdbc.batchUpdate(
                    insertAuthorBook,
                    batchAuthor.toArray(new SqlParameterSource[0])
                );
                batch.clear();
                batchAuthor.clear();
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
                    "title",
                    "genres" -> params.addValue(header, row.get(header));
                //date
                case "publishDate" -> {
                    var parsedDate = parseDate(row.get(header));
                    params.addValue(
                        header,
                        parsedDate == null
                            ? null
                            : java.sql.Date.valueOf(parsedDate)
                    );
                }
                // int
                default -> params.addValue(
                    header,
                    Integer.parseInt(row.get(header))
                );
            }
        }
        return params;
    }

    private LocalDate parseDate(String dateString) {
        var formatter = DateTimeFormatter.ofPattern("MM/dd/yy");
        try {
            var date = LocalDate.parse(dateString, formatter);
            if (date.getYear() > LocalDate.now().getYear()) {
                date = date.minusYears(100);
            }
            return date;
        } catch (Exception e) {
            return null;
        }
    }

    private Float parseFloatsSafe(String input) {
        if (input == null || input.isBlank()) return null;
        return Float.parseFloat(input);
    }

    @Transactional
    private MapSqlParameterSource handleAuthors(int book_id, String authors) {
        var authorName = getAuthor(authors);
        if (authorName.equals("")) {
            return null;
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
        return new MapSqlParameterSource()
            .addValue("bookId", book_id)
            .addValue("authorId", authorId);
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
}
