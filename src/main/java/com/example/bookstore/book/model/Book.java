package com.example.bookstore.book.model;

public record Book(
        int id,
        String title,
        String author,
        float average_rating,
        String description,
        String genres,
        int goodreads_book_id,
        String image_url,
        float isbn13,
        String language_code,
        float pages,
        String publishDate,
        int ratings_count
) {
    public String formattedGenres() {
        if (genres == null || genres.isBlank()) {
            return "—";
        }

        return genres.replaceAll("[\\[\\]']", "");
    }

    public String bestCoverUrl() {
        if (image_url == null || image_url.isBlank()) {
            return "/images/placeholder.jpg";
        }

        if (!image_url.startsWith("http")) {
            return "/images/placeholder.jpg";
        }

        // Goodreads only serves s/m/l sizes; l is the largest available.
        if (image_url.contains("images.gr-assets.com/books/")) {
            return image_url.replaceFirst(
                    "(https://images\\.gr-assets\\.com/books/\\d+)[sml](/\\d+\\.jpg)",
                    "$1l$2"
            );
        }

        return image_url;
    }
}
