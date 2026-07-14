package com.example.bookstore.book.controller;

import com.example.bookstore.book.model.Book;
import com.example.bookstore.book.service.BookService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;

@Controller
public class BookPageController {
    private final BookService bookService;

    public BookPageController(BookService bookService) {
        this.bookService = bookService;
    }

    @GetMapping("/book/{id}")
    public String getBookHtml(@PathVariable int id, Model model) {
        if (id <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }

        Book book = bookService.findBook(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        model.addAttribute("book", book);

        return "book";
    }
    @GetMapping("/search")
    public String searchBooks(@RequestParam(required = false, name = "q" )  String search, Model model) {
        var list = bookService.findBooks(search);
        model.addAttribute("books", list);
        return "search";
    }
}
