
package library.repository;

import static org.junit.jupiter.api.Assertions.*;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.Set;

import org.junit.jupiter.api.Test;

import library.config.DatabaseConnection;
import library.model.Book;
import library.model.BorrowingRecord;
import library.model.Student;

class JdbcStorageTest {

    private final JdbcBookStorage bookStorage = new JdbcBookStorage();
    private final JdbcStudentStorage studentStorage = new JdbcStudentStorage();
    private final JdbcBorrowingRecordStorage recordStorage =
            new JdbcBorrowingRecordStorage();

    @Test
    void testJdbcRepositories() {

        // Use valid IDs reserved for JDBC testing.
        String studentId = "S9991";
        String bookId = "B9991";
        String recordId = "R9991";

        Student student = new Student(
                studentId,
                "JDBC Test Student",
                "test-password-hash"
        );

        Book book = new Book(
                bookId,
                "JDBC Test Book",
                "Test Author",
                Set.of("testing", "jdbc"),
                null
        );

        BorrowingRecord record = null;

        try {
            // 1. Test Student Storage
            studentStorage.save(student);

            assertTrue(
                    studentStorage.findById(studentId).isPresent()
            );

            // 2. Test Book Storage
            bookStorage.save(book);

            Book savedBook = bookStorage.findById(bookId)
                    .orElseThrow();

            assertEquals(book.getTitle(), savedBook.getTitle());
            assertEquals(book.getAuthor(), savedBook.getAuthor());
            assertEquals(book.getTags(), savedBook.getTags());

            // 3. Test Book Update
            book.updateTitle("Updated JDBC Test Book");
            bookStorage.save(book);

            assertEquals(
                    "Updated JDBC Test Book",
                    bookStorage.findById(bookId)
                            .orElseThrow()
                            .getTitle()
            );

            // 4. Test Borrowing Record Storage
            record = new BorrowingRecord(
                    recordId,
                    bookId,
                    studentId,
                    LocalDate.now(),
                    null
            );

            recordStorage.save(record);

            assertTrue(
                    recordStorage.findById(recordId).isPresent()
            );

            assertTrue(
                    recordStorage.findByStudentId(studentId)
                            .stream()
                            .anyMatch(r -> r.getRecordId().equals(recordId))
            );

            assertTrue(
                    recordStorage.findActiveByBookId(bookId).isPresent()
            );

            // 5. Test Returning a Book
            record.markAsReturned();
            recordStorage.save(record);

            assertTrue(
                    recordStorage.findActiveByBookId(bookId).isEmpty()
            );

            assertNotNull(
                    recordStorage.findById(recordId)
                            .orElseThrow()
                            .getReturnDate()
            );

            // 6. Test Find All
            assertTrue(
                    bookStorage.findAll().stream()
                            .anyMatch(b -> b.getId().equals(bookId))
            );

            assertTrue(
                    studentStorage.findAll().stream()
                            .anyMatch(s -> s.getStudentId().equals(studentId))
            );

            assertTrue(
                    recordStorage.findAll().stream()
                            .anyMatch(r -> r.getRecordId().equals(recordId))
            );

        } finally {
            // 7. Cleanup Test Data
            // Delete borrowing record first because it references the book.

            try (Connection connection = DatabaseConnection.getConnection();
                 PreparedStatement statement = connection.prepareStatement(
                         "DELETE FROM borrowing_records WHERE record_id = ?")) {

                statement.setString(1, recordId);
                statement.executeUpdate();

            } catch (SQLException e) {
                throw new RuntimeException(
                        "Unable to clean up test borrowing record.", e
                );
            }

            bookStorage.delete(bookId);
            studentStorage.delete(studentId);
        }
    }
}
