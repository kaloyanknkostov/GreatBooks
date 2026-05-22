package com.example.bookstore.repository;
import com.example.bookstore.model.Book;

import org.springframework.stereotype.Repository;
import java.util.ArrayList;

@Repository
public class MockRepository implements BookRepository{
    private final ArrayList<Book> list= new ArrayList<Book>() ;


    @Override
    public void addBook(Book book) {
        list.add(book);
    }

    @Override
    public Book getBook(int id) {
        return list.get(id);
    }
}
