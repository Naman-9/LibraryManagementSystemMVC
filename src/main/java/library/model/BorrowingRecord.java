package library.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

import library.exception.BookAlreadyReturnedException;
import library.exception.InvalidBorrowingRecordException;

public class BorrowingRecord {
    private final String recordId;
    private final String bookId;
    private final String studentId;
    private final LocalDate issueDate;
    private LocalDateTime returnDate;

    private static int idCounter = 1;

    public static String generateId() {
        return String.format("R%04d", idCounter++);
    }

    private static void updateIdCounter(String existingId) {
        if (existingId == null || !existingId.matches("R\\d{4,}")) {
            throw new InvalidBorrowingRecordException("Invalid borrowing ID: " + existingId);
        }
        int number = Integer.parseInt(existingId.substring(1));
        idCounter = Math.max(idCounter, number + 1);
    }

    private static void validateRecordDetails(
            String bookId,
            String studentId,
            LocalDate issueDate) {

        if (bookId == null || bookId.isBlank()) {
            throw new InvalidBorrowingRecordException("Book ID cannot be blank.");
        }

        if (studentId == null || studentId.isBlank()) {
            throw new InvalidBorrowingRecordException("Student ID cannot be blank.");
        }

        if (issueDate == null) {
            throw new InvalidBorrowingRecordException("Issue date cannot be null.");
        }
    }

    public BorrowingRecord(String bookId, String studentId, LocalDate issueDate) {

        validateRecordDetails(bookId, studentId, issueDate);
        this.recordId = generateId();
        this.bookId = bookId;
        this.studentId = studentId;
        this.issueDate = issueDate;
        this.returnDate = null;
    }

    public BorrowingRecord(
            String recordId,
            String bookId,
            String studentId,
            LocalDate issueDate,
            LocalDateTime returnDate) {

        validateRecordDetails(bookId, studentId, issueDate);

        if (returnDate != null &&
                returnDate.toLocalDate().isBefore(issueDate)) {
            throw new InvalidBorrowingRecordException(
                    "Return date cannot be before issue date.");
        }

        this.recordId = recordId;
        updateIdCounter(recordId);
        this.bookId = bookId;
        this.studentId = studentId;
        this.issueDate = issueDate;
        this.returnDate = returnDate;
    }

    public boolean isActive() {
        return returnDate == null;
    }

    public void markAsReturned() {
        if (!isActive()) {
            throw new BookAlreadyReturnedException("Book has already been returned.");
        }

        returnDate = LocalDateTime.now();
    }

    public String getRecordId() {
        return recordId;
    }

    public String getBookId() {
        return bookId;
    }

    public String getStudentId() {
        return studentId;
    }

    public LocalDate getIssueDate() {
        return issueDate;
    }

    public LocalDateTime getReturnDate() {
        return returnDate;
    }
}
