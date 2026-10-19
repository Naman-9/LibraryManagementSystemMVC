package library.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import library.model.Book;

public class LibraryService {

    private final List<Book> books;

    public LibraryService() {
        this.books = new ArrayList<>();
    }

    public void addBook(String title, String author, Set<String> tags) {
        Book book = new Book(title, author, tags);
        books.add(book);
    }

    public void updateBook(String id, String title, String author) {
        Optional<Book> result = findById(id);

        if (result.isEmpty()) {
            throw new IllegalArgumentException("Book not found: " + id);
        }

        Book book = result.get();
        book.updateTitle(title);
        book.updateAuthor(author);
    }

    public void deleteBook(String id) {
        boolean removed = books.removeIf(book -> book.getId().equals(id));

        if (!removed) {
            throw new IllegalArgumentException("Book not found: " + id);
        }
    }

    public void issueBook(String id, String borrowerId) {

        Optional<Book> result = findById(id);

        if (result.isEmpty()) {
            throw new IllegalArgumentException("Book not found: " + id);
        }

        Book book = result.get();
        book.issueTo(borrowerId);

    }

    public void returnBook(String id) {
        Optional<Book> result = findById(id);

        if (result.isEmpty()) {
            throw new IllegalArgumentException("Book not found: " + id);
        }

        Book book = result.get();
        book.returnBook();
    }

    public Optional<Book> findById(String id) {
        for (Book book : books) {
            if (book.getId().equals(id)) {
                return Optional.of(book);
            }
        }
        return Optional.empty();
    }

    public List<Book> getAllBooks() {
        return new ArrayList<>(books);
    }
}