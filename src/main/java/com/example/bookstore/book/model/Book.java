package com.example.bookstore.book.model;

import lombok.Setter;
import lombok.Getter;

@Getter
@Setter
public class Book {
    private int id;
    private String title;
    private String author;
    private double price;

    public Book(int id, String title, String author, double price){
        this.id=0;
        this.title=title;
        this.author=author;
        this.price=price;
    }
}
