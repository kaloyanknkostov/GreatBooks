package com.example;

import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import net.datafaker.Faker;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;

public class UserInsertion {

    final NamedParameterJdbcTemplate namedjdbc;
    final String ratingsCSV = "data/goodbooks-10k-extended/ratings.csv";
    final String toReadCSV = "data/goodbooks-10k-extended/to_read.csv";

    final String insertRatingSql = """
        INSERT INTO ratings (user_id, book_id, rating)
        VALUES (:user_id , :book_id, :rating)
        ON CONFLICT DO NOTHING;
        """;

    final String insertUserSql = """
        INSERT INTO users (id,username,email,password)
        VALUES (:id, :username, :email, :password)
        ON CONFLICT DO NOTHING;
        """;

    final String insertToReadSql = """
        INSERT INTO to_read(user_id, book_id)
        VALUES (:user_id , :book_id)
        ON CONFLICT DO NOTHING;
        """;
    private static final Faker faker = new Faker();

    public UserInsertion(NamedParameterJdbcTemplate namedjdbc) {
        this.namedjdbc = namedjdbc;
    }

    public void runner() {
        try (CSVReader reader = new CSVReader(new FileReader(ratingsCSV))) {
            String[] nextRecord;
            var headers = reader.readNext();
            if (headers == null) {
                System.out.println("Empty file");
                return;
            }
            var batchSize = 50000;
            var batch = new ArrayList<SqlParameterSource>();
            var batchUsers = new ArrayList<SqlParameterSource>();
            var params = new MapSqlParameterSource();
            var usersIds = new HashSet<Integer>();
            int userId;
            while ((nextRecord = reader.readNext()) != null) {
                if (Integer.parseInt(nextRecord[1]) > 9999) {
                    continue;
                }
                if (
                    Integer.parseInt(nextRecord[2]) > 5 ||
                    Integer.parseInt(nextRecord[2]) < 1
                ) {
                    System.out.println("ERROR");
                }
                userId = Integer.parseInt(nextRecord[0]);
                if (!usersIds.contains(userId)) {
                    batchUsers.add(createUser(userId));
                    usersIds.add(userId);
                }
                params = new MapSqlParameterSource();
                var headerCounter = 0;
                for (String header : headers) {
                    params.addValue(
                        header,
                        Integer.parseInt(nextRecord[headerCounter])
                    );
                    headerCounter++;
                }
                batch.add(params);
                if (batch.size() == batchSize) {
                    namedjdbc.batchUpdate(
                        insertUserSql,
                        batchUsers.toArray(new SqlParameterSource[0])
                    );
                    namedjdbc.batchUpdate(
                        insertRatingSql,
                        batch.toArray(new SqlParameterSource[0])
                    );
                    batch.clear();
                    batchUsers.clear();
                }
            }
            if (!batch.isEmpty()) {
                namedjdbc.batchUpdate(
                    insertUserSql,
                    batchUsers.toArray(new SqlParameterSource[0])
                );
                namedjdbc.batchUpdate(
                    insertRatingSql,
                    batch.toArray(new SqlParameterSource[0])
                );

                batch.clear();
                batchUsers.clear();
            }

            insertToRead();
        } catch (FileNotFoundException e) {
            System.out.println("File doesn't exist");
            System.exit(1);
        } catch (IOException | CsvValidationException e) {
            System.out.println("Error");
            System.exit(1);
        }
    }

    private MapSqlParameterSource createUser(int id) {
        var username = faker.credentials().username() + id;
        return new MapSqlParameterSource()
            .addValue("id", id)
            .addValue("username", username)
            .addValue("email", faker.internet().emailAddress(username))
            .addValue(
                "password",
                faker.credentials().password(10, 50, true, true, true)
            );
    }

    private void insertToRead()
        throws IOException, CsvValidationException {
        System.out.println("Started wiht to read");
        try (CSVReader reader = new CSVReader(new FileReader(toReadCSV))) {
            String[] nextRecord;
            var headers = reader.readNext();
            if (headers == null) {
                System.out.println("Empty file");
                return;
            }
            var batchSize = 30000;
            var batch = new ArrayList<SqlParameterSource>();
            var params = new MapSqlParameterSource();
            while ((nextRecord = reader.readNext()) != null) {
                if (Integer.parseInt(nextRecord[1]) > 9999) {
                    continue;
                }
                params = new MapSqlParameterSource();
                var headerCounter = 0;
                for (String header : headers) {
                    params.addValue(
                        header,
                        Integer.parseInt(nextRecord[headerCounter])
                    );
                    headerCounter++;
                }
                batch.add(params);
                if (batch.size() == batchSize) {
                    namedjdbc.batchUpdate(
                        insertToReadSql,
                        batch.toArray(new SqlParameterSource[0])
                    );
                    batch.clear();
                }
            }
            if (!batch.isEmpty()) {
                namedjdbc.batchUpdate(
                    insertToReadSql,
                    batch.toArray(new SqlParameterSource[0])
                );
                batch.clear();
            }
        }
    }
}
