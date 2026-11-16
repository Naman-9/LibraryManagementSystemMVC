
package library.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import library.exception.BookNotFoundException;
import library.exception.BookNotIssuedException;
import library.exception.BookUnavailableException;
import library.exception.StudentNotFoundException;
import library.model.Book;
import library.model.BorrowingRecord;
import library.repository.BookStorage;
import library.repository.BorrowingRecordStorage;
import library.repository.StudentStorage;
import library.security.UserRole;
import library.security.UserSession;

public class LibraryService {

    // book lists available in library
    // private final List<Book> books;

    private final BookStorage storage;

    private final StudentStorage studentStorage;
    private final AuthenticationService authenticationService;
    private final BorrowingRecordStorage borrowRecordStorage;

    public LibraryService(
            BookStorage storage,
            StudentStorage studentStorage,
            AuthenticationService authenticationService,
            BorrowingRecordStorage borrowRecordStorage) {

        this.storage = storage;
        this.studentStorage = studentStorage;
        this.authenticationService = authenticationService;
        this.borrowRecordStorage = borrowRecordStorage;
    }

    public void addBook(String title, String author, Set<String> tags) {
        requireRole(UserRole.ADMIN);
        Book book = new Book(title, author, tags);
        storage.save(book);
    }

    public void updateBook(String id, String title, String author) {
        requireRole(UserRole.ADMIN);
        Optional<Book> result = findById(id);

        if (result.isEmpty()) {
            throw new BookNotFoundException("Book not found: " + id);
        }

        Book existingBook = result.get();

        Book updatedBook = new Book(
                existingBook.getId(),
                title,
                author,
                existingBook.getTags(),
                existingBook.getIssuedTo());

        storage.save(updatedBook); // csv files - update
    }

    public void deleteBook(String id) {
        requireRole(UserRole.ADMIN);
        Optional<Book> book = findById(id);

        if (book.isEmpty()) {
            throw new BookNotFoundException("Book not found: " + id);
        }

        storage.delete(id);
    }

    public void issueBook(String bookId, String borrowerId) {
        requireLoggedIn();

        UserSession currentSession =
                authenticationService.getCurrentSession().orElseThrow();

        if (currentSession.getRole() == UserRole.STUDENT
                && !currentSession.getUserId().equals(borrowerId)) {
            throw new SecurityException("You can only issue books for yourself.");
        }

        Optional<Book> result = findById(bookId);

        if (result.isEmpty()) {
            throw new BookNotFoundException("Book not found: " + bookId);
        }

        Book book = result.get();

        if (!book.isAvailable()) {
            throw new BookUnavailableException("Book is already issued.");
        }

        if (studentStorage.findById(borrowerId).isEmpty()) {
            throw new StudentNotFoundException(
                    "Student not found: " + borrowerId);
        }

        BorrowingRecord record = new BorrowingRecord(
                bookId,
                borrowerId,
                LocalDate.now());

        borrowRecordStorage.save(record);
        book.issueTo(borrowerId);
        storage.save(book);
    }

    public void returnBook(String id) {
        requireLoggedIn();

        Optional<Book> result = findById(id);

        if (result.isEmpty()) {
            throw new BookNotFoundException("Book not found: " + id);
        }

        Book book = result.get();

        Optional<BorrowingRecord> recordResult =
                borrowRecordStorage.findActiveByBookId(id);

        if (recordResult.isEmpty()) {
            throw new BookNotIssuedException(
                    "No active borrowing record found for this book.");
        }

        BorrowingRecord record = recordResult.get();

        UserSession currentSession =
                authenticationService.getCurrentSession().orElseThrow();

        if (currentSession.getRole() == UserRole.STUDENT
                && !currentSession.getUserId().equals(record.getStudentId())) {
            throw new SecurityException(
                    "You can only return books issued to yourself.");
        }

        record.markAsReturned();
        borrowRecordStorage.save(record);

        book.returnBook();
        storage.save(book);
    }

    public Optional<Book> findById(String id) {
        // for (Book book : books) {
        // if (book.getId().equals(id)) {
        // return Optional.of(book);
        // }
        // }
        // return Optional.empty();

        return storage.findById(id);
    }

    public List<Book> getAllBooks() {
        // return new ArrayList<>(books);
        return storage.findAll();
    }

    public List<Book> searchById(String id) {
        // List<Book> result = new ArrayList<>();
        // for (Book book : books) {
        // if (book.getId().equalsIgnoreCase(id.trim())) {
        // result.add(book);
        // }
        // }
        // return result;

        // return books.stream()
        // .filter(book -> book.getId().equalsIgnoreCase(id.trim()))
        // .toList();

        return getAllBooks().stream()
                .filter(book -> book.getId().equalsIgnoreCase(id.trim()))
                .toList();
    }

    public List<Book> searchByTitle(String query) {
        String q = query.trim().toLowerCase();
        return getAllBooks().stream()
                .filter(b -> b.getTitle().toLowerCase().contains(q))
                .toList();
    }

    public List<Book> searchByAuthor(String query) {
        String q = query.trim().toLowerCase();
        return getAllBooks().stream()
                .filter(b -> b.getAuthor().toLowerCase().contains(q))
                .toList();
    }

    public List<Book> searchByTag(String query) {
        return getAllBooks().stream()
                .filter(b -> b.hasTag(query))
                .toList();
    }

    public void loadDemoData() {
        if (!getAllBooks().isEmpty()) {
            return;
        }

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

    private void requireRole(UserRole requiredRole) {
        authenticationService.requireRole(requiredRole);
    }

    private void requireLoggedIn() {
        authenticationService.requireLoggedIn();
    }

    public List<BorrowingRecord> getMyBorrowingRecords() {
        requireRole(UserRole.STUDENT);

        return borrowRecordStorage.findByStudentId(
                authenticationService.getCurrentSession()
                        .orElseThrow()
                        .getUserId());
    }

    public List<BorrowingRecord> getAllBorrowingRecords() {
        requireRole(UserRole.ADMIN);

        return borrowRecordStorage.findAll();
    }

    public void issueBook(String bookId) {
        requireRole(UserRole.STUDENT);

        issueBook(
                bookId,
                authenticationService.getCurrentSession()
                        .orElseThrow()
                        .getUserId());
    }
}
