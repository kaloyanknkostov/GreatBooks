package com.example.bookstore.book.repository;
import com.example.bookstore.book.model.Book;

import java.util.Optional;

public interface BookRepository {
    void addBook(Book book);
    Optional<Book> getBook(int id);
}
