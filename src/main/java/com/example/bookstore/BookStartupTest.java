package com.example.bookstore;
import com.example.bookstore.book.model.Book;
import com.example.bookstore.book.service.BookService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;


@Component
public class BookStartupTest  implements CommandLineRunner {
    private final BookService bookService;

    public BookStartupTest (BookService bookService) {
        this.bookService = bookService;
    }

    @Override
    public void run(String... args) {
    }
}