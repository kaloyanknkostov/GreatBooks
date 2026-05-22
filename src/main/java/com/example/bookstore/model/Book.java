package com.example.bookstore.model;

import lombok.Setter;
import lombok.Getter;

@Getter
@Setter
public class Book {
    private int id;
    private String title;
    private String author;
    private double price;
    private int quantityInStock;

    public Book(String title, String author, double price){
        this.id=0;
        this.title=title;
        this.author=author;
        this.price=price;
        this.quantityInStock=0;
    }
}
