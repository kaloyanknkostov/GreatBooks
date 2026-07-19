package com.example.bookstore.book.service;

import com.example.bookstore.book.model.Book;
import com.example.bookstore.book.model.Tag;
import com.example.bookstore.book.proxy.BookImageFetch;
import com.example.bookstore.book.repository.BookRepository;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Service
public class BookService {
    private final BookRepository repo;
    private final BookImageFetch fetcher;

    public BookService(@Qualifier("bookPostgresRepository") BookRepository repo, BookImageFetch fetcher){
        this.repo=repo;
        this.fetcher=fetcher;
    }

    public void createBook(int id,String title, String author, double price){
    }

    public Optional<Book> findBook(int id ){
        return repo.getBook(id);
    }
    public List<Book> findBooks( String query){
        return repo.getBooks(query);
    }


}
