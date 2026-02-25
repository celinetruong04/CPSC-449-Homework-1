package com.example.webbackend.controller;

import com.example.webbackend.entity.Book;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
public class BookController {

    private List<Book> books = new ArrayList<>();

    private Long nextId = 1L;

    public BookController() {
        // Add 15 books with varied data for testing
        books.add(new Book(nextId++, "Spring Boot in Action", "Craig Walls", 39.99));
        books.add(new Book(nextId++, "Effective Java", "Joshua Bloch", 45.00));
        books.add(new Book(nextId++, "Clean Code", "Robert Martin", 42.50));
        books.add(new Book(nextId++, "Java Concurrency in Practice", "Brian Goetz", 49.99));
        books.add(new Book(nextId++, "Design Patterns", "Gang of Four", 54.99));
        books.add(new Book(nextId++, "Head First Java", "Kathy Sierra", 35.00));
        books.add(new Book(nextId++, "Spring in Action", "Craig Walls", 44.99));
        books.add(new Book(nextId++, "Clean Architecture", "Robert Martin", 39.99));
        books.add(new Book(nextId++, "Refactoring", "Martin Fowler", 47.50));
        books.add(new Book(nextId++, "The Pragmatic Programmer", "Andrew Hunt", 41.99));
        books.add(new Book(nextId++, "You Don't Know JS", "Kyle Simpson", 29.99));
        books.add(new Book(nextId++, "JavaScript: The Good Parts", "Douglas Crockford", 32.50));
        books.add(new Book(nextId++, "Eloquent JavaScript", "Marijn Haverbeke", 27.99));
        books.add(new Book(nextId++, "Python Crash Course", "Eric Matthes", 38.00));
        books.add(new Book(nextId++, "Automate the Boring Stuff", "Al Sweigart", 33.50));
    }

    // get all books - /api/books
    @GetMapping("/books")
    public List<Book> getBooks() {
        return books;
    }

    // get book by id
    @GetMapping("/books/{id}")
    public Book getBook(@PathVariable Long id) {
        return books.stream().filter(book -> book.getId().equals(id))
                .findFirst().orElse(null);
    }

    // create a new book
    @PostMapping("/books")
    public List<Book> createBook(@RequestBody Book book) {
        books.add(book);
        return books;
    }

    // search by title
    @GetMapping("/books/search")
    public List<Book> searchByTitle(
            @RequestParam(required = false, defaultValue = "") String title
    ) {
        if(title.isEmpty()) {
            return books;
        }

        return books.stream()
                .filter(book -> book.getTitle().toLowerCase().contains(title.toLowerCase()))
                .collect(Collectors.toList());

    }

    // price range
    @GetMapping("/books/price-range")
    public List<Book> getBooksByPrice(
            @RequestParam(required = false) Double minPrice,
            @RequestParam(required = false) Double maxPrice
    ) {
        return books.stream()
                .filter(book -> {
                    boolean min = minPrice == null || book.getPrice() >= minPrice;
                    boolean max = maxPrice == null || book.getPrice() <= maxPrice;

                    return min && max;
                }).collect(Collectors.toList());
    }

    // sort
    @GetMapping("/books/sorted")
    public List<Book> getSortedBooks(
            @RequestParam(required = false, defaultValue = "title") String sortBy,
            @RequestParam(required = false, defaultValue = "asc") String order
    ){
        Comparator<Book> comparator;

        switch(sortBy.toLowerCase()) {
            case "author":
                comparator = Comparator.comparing(Book::getAuthor);
                break;
                case "title":
                comparator = Comparator.comparing(Book::getTitle);
            default:
                comparator = Comparator.comparing(Book::getTitle);
                break;
        }

        if("desc".equalsIgnoreCase(order)) {
            comparator = comparator.reversed();
        }

        return books.stream().sorted(comparator)
                .collect(Collectors.toList());

    }

    // PUT - Update entire book
    @PutMapping("/books/{id}")
    public Book updateBook(@PathVariable Long id,
                           @RequestBody Book updatedBook) {
        for (Book book : books) {
            if (book.getId().equals(id)) {
                book.setTitle(updatedBook.getTitle());
                book.setAuthor(updatedBook.getAuthor());
                book.setPrice(updatedBook.getPrice());

                return book;
            }
        }

        return null;
    }

    // PATCH - partial update
    @PatchMapping("/books/{id}")
    public Book patchBook(@PathVariable Long id,
                          @RequestBody Map<String, Object> updates) {
        for (Book book : books) {
            if (book.getId().equals(id)) {

                if (updates.containsKey("title")) {
                    book.setTitle((String) updates.get("title"));
                }

                if (updates.containsKey("author")) {
                    book.setAuthor((String) updates.get("author"));
                }

                if (updates.containsKey("price")) {
                    book.setPrice(Double.valueOf(updates.get("price").toString()));
                }

                return book;
            }
        }

        return null;
    }

    // DELETE - remove book
    @DeleteMapping("/books/{id}")
    public String deleteBook(@PathVariable Long id) {

        Iterator<Book> iterator = books.iterator();

        while (iterator.hasNext()) {
            Book book = iterator.next();
            if (book.getId().equals(id)) {
                iterator.remove();
                return "Book deleted successfully.";
            }
        }

        return "Book not found.";
    }

    // pagination
    @GetMapping("/books/paginated")
    public List<Book> getPaginatedBooks(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size
    ) {
        int start = page * size;
        int end = Math.min(start + size, books.size());

        if(start > books.size()) {
            return new ArrayList<>();
        }

        return books.subList(start, end);
    }

    // advanced - filter, sort, then paginate
    @GetMapping("/books/advanced")
    public List<Book> getAdvancedBooks(
            @RequestParam(required = false) String title,
            @RequestParam(required = false) Double minPrice,
            @RequestParam(required = false) Double maxPrice,
            @RequestParam(defaultValue = "title") String sortBy,
            @RequestParam(defaultValue = "asc") String order,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size
    ) {
        // filter
        List<Book> filtered = books.stream()
                .filter(book -> {
                    boolean matchesTitle =
                            title == null || book.getTitle().toLowerCase().contains(title.toLowerCase());

                    boolean matchesMin =
                            minPrice == null || book.getPrice() >= minPrice;

                    boolean matchesMax =
                            maxPrice == null || book.getPrice() <= maxPrice;

                    return matchesTitle && matchesMin && matchesMax;
                })
                .collect(Collectors.toList());

        // sort
        Comparator<Book> comparator;

        switch (sortBy.toLowerCase()) {
            case "author":
                comparator = Comparator.comparing(Book::getAuthor);
                break;
            case "price":
                comparator = Comparator.comparing(Book::getPrice);
                break;
            case "title":
            default:
                comparator = Comparator.comparing(Book::getTitle);
                break;
        }

        if ("desc".equalsIgnoreCase(order)) {
            comparator = comparator.reversed();
        }

        List<Book> sorted = filtered.stream()
                .sorted(comparator)
                .collect(Collectors.toList());

        // paginate
        int start = page * size;
        int end = Math.min(start + size, sorted.size());

        if(start > sorted.size()) {
            return new ArrayList<>();
        }

        return sorted.subList(start, end);
    }


}
