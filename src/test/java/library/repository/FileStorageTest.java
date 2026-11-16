package library.repository;

import library.model.Book;
import library.model.BorrowingRecord;
import library.model.Student;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.time.LocalDate;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class FileStorageTest {

    @TempDir
    Path tempDir;

    @Test
    void bookStorageShouldSaveUpdateReloadAndDeleteBooks() {
        Path file = tempDir.resolve("books.csv");

        FileBookStorage storage = new FileBookStorage(file);

        Book book = new Book("Clean Code", "Robert Martin", Set.of("Java", "Programming"));
        String bookId = book.getId();

        storage.save(book);

        assertTrue(storage.findById(bookId).isPresent());

        book.updateTitle("Clean Code Revised");
        storage.save(book);

        FileBookStorage reloadedStorage = new FileBookStorage(file);

        assertEquals(
                "Clean Code Revised",
                reloadedStorage.findById(bookId).orElseThrow().getTitle()
        );

        reloadedStorage.delete(bookId);

        FileBookStorage finalStorage = new FileBookStorage(file);

        assertTrue(finalStorage.findById(bookId).isEmpty());
    }

    @Test
    void studentStorageShouldSaveReloadAndDeleteStudents() {
        Path file = tempDir.resolve("students.csv");

        FileStudentStorage storage = new FileStudentStorage(file);

        Student student = new Student("John, Smith", "hashed-password");
        String studentId = student.getStudentId();

        storage.save(student);

        FileStudentStorage reloadedStorage = new FileStudentStorage(file);

        Student loadedStudent =
                reloadedStorage.findById(studentId).orElseThrow();

        assertEquals("John, Smith", loadedStudent.getName());
        assertEquals("hashed-password", loadedStudent.getPasswordHash());

        reloadedStorage.delete(studentId);

        FileStudentStorage finalStorage = new FileStudentStorage(file);

        assertTrue(finalStorage.findById(studentId).isEmpty());
    }

    @Test
    void borrowingRecordStorageShouldSaveAndReloadActiveAndReturnedRecords() {
        Path file = tempDir.resolve("borrowRecords.csv");

        FileBorrowingRecordStorage storage =
                new FileBorrowingRecordStorage(file);

        BorrowingRecord activeRecord = new BorrowingRecord(
                "B0001", "S0001", LocalDate.now()
        );

        BorrowingRecord returnedRecord = new BorrowingRecord(
                "B0002", "S0001", LocalDate.now()
        );

        returnedRecord.markAsReturned();

        storage.save(activeRecord);
        storage.save(returnedRecord);

        FileBorrowingRecordStorage reloadedStorage =
                new FileBorrowingRecordStorage(file);

        assertEquals(2, reloadedStorage.findAll().size());

        assertTrue(
                reloadedStorage.findById(activeRecord.getRecordId())
                        .orElseThrow().isActive()
        );

        assertFalse(
                reloadedStorage.findById(returnedRecord.getRecordId())
                        .orElseThrow().isActive()
        );

        assertEquals(
                2,
                reloadedStorage.findByStudentId("S0001").size()
        );

        assertEquals(
                activeRecord.getRecordId(),
                reloadedStorage.findActiveByBookId("B0001")
                        .orElseThrow().getRecordId()
        );

        assertTrue(
                reloadedStorage.findActiveByBookId("B0002").isEmpty()
        );
    }
}