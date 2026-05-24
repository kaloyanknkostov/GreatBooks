package com.example.bookstore.book.proxy;
import org.springframework.stereotype.Component;

@Component
public class BookImageFetch{
    // later fetches an image and returns it
    public String testCalled(){
        return "Works!";
    }
}
