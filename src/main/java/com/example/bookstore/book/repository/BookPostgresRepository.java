package com.example.bookstore.book.repository;

import com.example.bookstore.book.model.Book;
import org.flywaydb.core.internal.jdbc.RowMapper;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.DataClassRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;



@Repository
public class BookPostgresRepository  implements BookRepository {
    final JdbcTemplate jdbcTemplate;

    public BookPostgresRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }


    @Override
    public void addBook(Book book) {
    }
    @Override
    public Optional<Book> getBook(int id) {

        var sql = """
        SELECT books.*, authors.name as author
        FROM books
        JOIN book_authors ON books.id = book_authors.book_id
        JOIN authors ON book_authors.author_id = authors.id
        WHERE books.id = ?;
        """;
        try {
            Book book = jdbcTemplate.queryForObject(sql, new DataClassRowMapper<>(Book.class), id);
            return Optional.ofNullable(book);
        }
        catch (EmptyResultDataAccessException e){
            return Optional.empty();
        }
    }
}
