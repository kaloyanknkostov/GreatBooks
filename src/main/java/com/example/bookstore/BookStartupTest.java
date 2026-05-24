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
        System.out.println("=== TESTING SPRING CONTEXT ===");
        bookService.createBook(1,"Dune", "Frank Herbert", 15.99);
        Book found = bookService.findBook(0);  // list index 0 for now
        System.out.println("Found book: " + found.getTitle());
    }
}