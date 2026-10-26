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

    public List<Book> searchById(String id) {
        // List<Book> result = new ArrayList<>();
        // for (Book book : books) {
        // if (book.getId().equalsIgnoreCase(id.trim())) {
        // result.add(book);
        // }
        // }
        // return result;
        return books.stream()
                .filter(book -> book.getId().equalsIgnoreCase(id.trim()))
                .toList();
    }

    public List<Book> searchByTitle(String query) {
        String q = query.trim().toLowerCase();
        return books.stream()
                .filter(b -> b.getTitle().toLowerCase().contains(q))
                .toList();
    }

    public List<Book> searchByAuthor(String query) {
        String q = query.trim().toLowerCase();
        return books.stream()
                .filter(b -> b.getAuthor().toLowerCase().contains(q))
                .toList();
    }

    public List<Book> searchByTag(String query) {
        return books.stream()
                .filter(b -> b.hasTag(query))
                .toList();
    }

    public void loadDemoData() {
        addBook("Clean Code", "Robert C. Martin", Set.of("Programming", "Java", "Software"));
        addBook("Effective Java", "Joshua Bloch", Set.of("Java", "Programming", "OOP"));
        addBook("Head First Java", "Kathy Sierra", Set.of("Java", "Beginner", "Programming"));
        addBook("The Pragmatic Programmer", "Andrew Hunt", Set.of("Programming", "Software", "Career"));
        addBook("Design Patterns", "Erich Gamma", Set.of("OOP", "Design", "Programming"));
        addBook("Introduction to Algorithms", "Thomas H. Cormen", Set.of("Algorithms", "DSA", "Computer Science"));
        addBook("Java: The Complete Reference", "Herbert Schildt", Set.of("Java", "Programming", "Reference"));
        addBook("Don't Make Me Think", "Steve Krug", Set.of("Design", "UX", "Web"));
        addBook("The Clean Coder", "Robert C. Martin", Set.of("Programming", "Software", "Career"));
        addBook("Refactoring", "Martin Fowler", Set.of("Programming", "Java", "Software"));
    }
}