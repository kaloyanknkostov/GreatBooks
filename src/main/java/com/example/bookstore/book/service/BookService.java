package com.example.bookstore.book.service;

import com.example.bookstore.book.model.Book;
import com.example.bookstore.book.model.Tag;
import com.example.bookstore.book.proxy.BookImageFetch;
import com.example.bookstore.book.repository.BookRepository;
import java.util.ArrayList;
import java.util.Optional;
import org.springframework.stereotype.Service;

@Service
public class BookService {
    private final BookRepository repo;
    private final BookImageFetch fetcher;

    public BookService(BookRepository repo,BookImageFetch fetcher){
        this.repo=repo;
        this.fetcher=fetcher;
    }

    public void createBook(int id,String title, String author, double price){
       //TODO create tags repo import here and find the correct tags and fill the list
       var tags = new ArrayList<Tag>();
       Book temp= new Book(id,title,author,price,tags);
       fetcher.testCalled();
       repo.addBook(temp);

    }

    public Book findBook(int id ){
        Optional<Book> book= repo.getBook(id);
        return book.orElse(null);
    }


}
