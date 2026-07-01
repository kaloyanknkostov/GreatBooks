package com.example.bookstore.book.repository;

import com.example.bookstore.book.model.Book;
import java.util.ArrayList;
import java.util.Optional;
import org.springframework.stereotype.Repository;

@Repository
public class BookMockRepository implements BookRepository {

    private final ArrayList<Book> list = new ArrayList<Book>();

    @Override
    public void addBook(Book book) {
        list.add(book);
    }

    @Override
    public Optional<Book> getBook(int id) {
        return list
            .stream()
            .filter(book -> book.getId() == id)
            .findFirst();
    }
}
