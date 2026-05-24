package com.example.bookstore.book.model;

import lombok.Setter;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class Book {
    private int id;
    private String title;
    private String author;
    private double price;
    private ArrayList<Tag> bookTags;

    public Book(int id, String title, String author, double price, ArrayList<Tag>tags){
        this.id=0;
        this.title=title;
        this.author=author;
        this.price=price;
        this.bookTags=tags;
    }
}
