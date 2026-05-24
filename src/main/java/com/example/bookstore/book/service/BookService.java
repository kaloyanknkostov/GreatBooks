package com.example.bookstore.book.service;

import com.example.bookstore.book.model.Book;
import com.example.bookstore.book.proxy.BookImageFetch;
import com.example.bookstore.book.repository.BookRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class BookService {
    private final BookRepository repo;
    private final BookImageFetch fetcher;

    public BookService(BookRepository repo,BookImageFetch fetcher){
        this.repo=repo;
        this.fetcher=fetcher;
    }

    public void createBook(int id,String title, String author, double price){
       Book temp= new Book(id,title,author,price);
       fetcher.testCalled();
       repo.addBook(temp);
    }

    public Book findBook(int id ){
        Optional<Book> book= repo.getBook(id);
        return book.orElse(null);
    }


}
