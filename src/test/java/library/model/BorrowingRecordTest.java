package library.model;


import library.exception.BookAlreadyReturnedException;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class BorrowingRecordTest {

    @Test
    void newRecordShouldBeActive() {
        BorrowingRecord record = new BorrowingRecord(
                "B0001", "S0001", LocalDate.now()
        );

        assertTrue(record.isActive());
        assertNull(record.getReturnDate());
    }

    @Test
    void newRecordShouldGenerateId() {
        BorrowingRecord record = new BorrowingRecord(
                "B0001", "S0001", LocalDate.now()
        );

        assertNotNull(record.getRecordId());
        assertTrue(record.getRecordId().matches("R\\d{4,}"));
    }

    @Test
    void recordShouldBeMarkedAsReturned() {
        BorrowingRecord record = new BorrowingRecord(
                "B0001", "S0001", LocalDate.now()
        );

        record.markAsReturned();

        assertFalse(record.isActive());
        assertNotNull(record.getReturnDate());
    }

    @Test
    void returnedRecordShouldHaveReturnDate() {
        BorrowingRecord record = new BorrowingRecord(
                "B0001", "S0001", LocalDate.now()
        );

        record.markAsReturned();

        assertTrue(
                record.getReturnDate().isAfter(
                        LocalDateTime.now().minusMinutes(1)
                )
        );
    }

    @Test
    void returningAlreadyReturnedRecordShouldThrowException() {
        BorrowingRecord record = new BorrowingRecord(
                "B0001", "S0001", LocalDate.now()
        );

        record.markAsReturned();

        assertThrows(
                BookAlreadyReturnedException.class,
                record::markAsReturned
        );
    }

    @Test
    void reconstructedActiveRecordShouldRemainActive() {
        BorrowingRecord record = new BorrowingRecord(
                "R9001",
                "B0001",
                "S0001",
                LocalDate.now(),
                null
        );

        assertTrue(record.isActive());
    }

    @Test
    void reconstructedReturnedRecordShouldNotBeActive() {
        LocalDate issueDate = LocalDate.now();

        BorrowingRecord record = new BorrowingRecord(
                "R9002",
                "B0001",
                "S0001",
                issueDate,
                issueDate.atTime(12, 30)
        );

        assertFalse(record.isActive());
        assertNotNull(record.getReturnDate());
    }
}
