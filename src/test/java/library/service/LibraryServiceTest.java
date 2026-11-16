
package library.service;

import library.config.AdminConfig;
import library.exception.BookUnavailableException;
import library.model.Book;
import library.model.BorrowingRecord;
import library.model.Student;
import library.repository.InMemoryBookStorage;
import library.repository.InMemoryBorrowingRecordStorage;
import library.repository.InMemoryStudentStorage;
import library.security.PasswordHasher;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class LibraryServiceTest {

    private static final String STUDENT_PASSWORD = "student-pass";

    private static final String STUDENT_PASSWORD_HASH =
            new PasswordHasher().hashPassword(STUDENT_PASSWORD);

    private InMemoryBookStorage bookStorage;
    private InMemoryStudentStorage studentStorage;
    private InMemoryBorrowingRecordStorage recordStorage;

    private PasswordHasher passwordHasher;
    private AdminConfig adminConfig;

    private AuthenticationService authenticationService;
    private LibraryService service;

    @BeforeEach
    void setUp() {
        bookStorage = new InMemoryBookStorage();
        studentStorage = new InMemoryStudentStorage();
        recordStorage = new InMemoryBorrowingRecordStorage();

        passwordHasher = new PasswordHasher();
        adminConfig = new AdminConfig();

        authenticationService = new AuthenticationService(
                studentStorage,
                passwordHasher,
                adminConfig
        );

        service = new LibraryService(
                bookStorage,
                studentStorage,
                authenticationService,
                recordStorage
        );
    }

    private Student createStudent(String name) {
        Student student = new Student(name, STUDENT_PASSWORD_HASH);
        studentStorage.save(student);
        return student;
    }

    private Book createBook(String title) {
        Book book = new Book(
                title,
                "Test Author",
                Set.of("testing")
        );

        bookStorage.save(book);
        return book;
    }

    private void loginAsAdmin() {
        assertTrue(authenticationService.authenticateAdmin(
                "ADMIN001",
                "LMS123"
        ));
    }

    private void loginAsStudent(Student student) {
        assertTrue(authenticationService.authenticateStudent(
                student.getStudentId(),
                STUDENT_PASSWORD
        ));
    }

    // 1. Admin can add books

    @Test
    void adminShouldBeAbleToAddBook() {
        loginAsAdmin();

        service.addBook(
                "Clean Code",
                "Robert C. Martin",
                Set.of("Java", "Programming")
        );

        assertEquals(1, service.getAllBooks().size());
    }

    // 2. Unauthenticated user cannot add books

    @Test
    void unauthenticatedUserShouldNotAddBook() {
        assertThrows(
                SecurityException.class,
                () -> service.addBook(
                        "Clean Code",
                        "Robert Martin",
                        Set.of("Java")
                )
        );
    }

    // 3. Student can issue a book

    @Test
    void studentShouldBeAbleToIssueBook() {
        Student student = createStudent("Student One");
        Book book = createBook("Clean Code");

        loginAsStudent(student);

        service.issueBook(book.getId());

        Book issuedBook = bookStorage.findById(book.getId())
                .orElseThrow();

        assertEquals(student.getStudentId(), issuedBook.getIssuedTo());

        BorrowingRecord record = recordStorage
                .findActiveByBookId(book.getId())
                .orElseThrow();

        assertTrue(record.isActive());
        assertEquals(student.getStudentId(), record.getStudentId());
    }

    // 4. Cannot issue an already issued book

    @Test
    void alreadyIssuedBookShouldNotBeIssuedAgain() {
        Student student = createStudent("Student One");
        Book book = createBook("Clean Code");

        loginAsStudent(student);

        service.issueBook(book.getId());

        assertThrows(
                BookUnavailableException.class,
                () -> service.issueBook(book.getId())
        );
    }

    // 5. Student cannot issue a book for another student

    @Test
    void studentShouldNotIssueBookForAnotherStudent() {
        Student studentOne = createStudent("Student One");
        Student studentTwo = createStudent("Student Two");
        Book book = createBook("Clean Code");

        loginAsStudent(studentOne);

        assertThrows(
                SecurityException.class,
                () -> service.issueBook(
                        book.getId(),
                        studentTwo.getStudentId()
                )
        );
    }

    // 6. Admin can issue a book to a student

    @Test
    void adminShouldBeAbleToIssueBookForStudent() {
        Student student = createStudent("Student One");
        Book book = createBook("Clean Code");

        loginAsAdmin();

        service.issueBook(book.getId(), student.getStudentId());

        assertEquals(
                student.getStudentId(),
                bookStorage.findById(book.getId())
                        .orElseThrow()
                        .getIssuedTo()
        );
    }

    // 7. Student can return their own book

    @Test
    void studentShouldBeAbleToReturnOwnBook() {
        Student student = createStudent("Student One");
        Book book = createBook("Clean Code");

        loginAsStudent(student);

        service.issueBook(book.getId());
        service.returnBook(book.getId());

        assertTrue(
                bookStorage.findById(book.getId())
                        .orElseThrow()
                        .isAvailable()
        );

        BorrowingRecord record = recordStorage
                .findAll()
                .get(0);

        assertFalse(record.isActive());
        assertNotNull(record.getReturnDate());
    }

    // 8. Student cannot return another student's book

    @Test
    void studentShouldNotReturnAnotherStudentsBook() {
        Student studentOne = createStudent("Student One");
        Student studentTwo = createStudent("Student Two");
        Book book = createBook("Clean Code");

        loginAsAdmin();
        service.issueBook(book.getId(), studentOne.getStudentId());

        authenticationService.logout();
        loginAsStudent(studentTwo);

        assertThrows(
                SecurityException.class,
                () -> service.returnBook(book.getId())
        );
    }

    // 9. Student can see only their own borrowing history

    @Test
    void studentShouldSeeOnlyOwnBorrowingRecords() {
        Student studentOne = createStudent("Student One");
        Student studentTwo = createStudent("Student Two");

        Book bookOne = createBook("Clean Code");
        Book bookTwo = createBook("Effective Java");

        loginAsAdmin();

        service.issueBook(bookOne.getId(), studentOne.getStudentId());
        service.issueBook(bookTwo.getId(), studentTwo.getStudentId());

        authenticationService.logout();
        loginAsStudent(studentOne);

        List<BorrowingRecord> records = service.getMyBorrowingRecords();

        assertEquals(1, records.size());
        assertEquals(
                studentOne.getStudentId(),
                records.get(0).getStudentId()
        );
    }

    // 10. Admin can see all borrowing records

    @Test
    void adminShouldSeeAllBorrowingRecords() {
        Student studentOne = createStudent("Student One");
        Student studentTwo = createStudent("Student Two");

        Book bookOne = createBook("Clean Code");
        Book bookTwo = createBook("Effective Java");

        loginAsAdmin();

        service.issueBook(bookOne.getId(), studentOne.getStudentId());
        service.issueBook(bookTwo.getId(), studentTwo.getStudentId());

        List<BorrowingRecord> records = service.getAllBorrowingRecords();

        assertEquals(2, records.size());
    }

    // 11. Student cannot see all borrowing records

    @Test
    void studentShouldNotSeeAllBorrowingRecords() {
        Student student = createStudent("Student One");

        loginAsStudent(student);

        assertThrows(
                SecurityException.class,
                () -> service.getAllBorrowingRecords()
        );
    }
}
