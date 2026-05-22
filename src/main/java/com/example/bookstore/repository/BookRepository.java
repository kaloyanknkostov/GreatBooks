package com.example.bookstore.repository;
import com.example.bookstore.model.Book;
public interface BookRepository {
    void addBook(Book book);
    Book getBook(int id);
}
