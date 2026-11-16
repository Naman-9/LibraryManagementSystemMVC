package library.repository;

import java.util.List;
import java.util.Optional;

import library.model.BorrowingRecord;

public interface BorrowingRecordStorage {

    void save(BorrowingRecord record);
    Optional<BorrowingRecord> findById(String recordId);
    List<BorrowingRecord> findAll();
    List<BorrowingRecord> findByStudentId(String studentId);
    Optional<BorrowingRecord> findActiveByBookId(String bookId);

}
