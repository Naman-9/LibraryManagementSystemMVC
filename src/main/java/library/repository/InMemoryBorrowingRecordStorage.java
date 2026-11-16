package library.repository;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import library.model.Book;
import library.model.BorrowingRecord;

public class InMemoryBorrowingRecordStorage implements BorrowingRecordStorage {
    
    private final Map<String, BorrowingRecord> borrowingRecords;

    public InMemoryBorrowingRecordStorage(){
        this.borrowingRecords = new HashMap<>();
    }

    @Override
    public void save(BorrowingRecord borrowingRecord){
        borrowingRecords.put(borrowingRecord.getRecordId(), borrowingRecord);
    }

    @Override
    public Optional<BorrowingRecord> findById(String id){
        return Optional.ofNullable(borrowingRecords.get(id));
    }

    @Override
    public List<BorrowingRecord> findAll(){
        return borrowingRecords.values().stream().sorted(Comparator.comparing(BorrowingRecord::getRecordId)).toList();
    }

    @Override
    public List<BorrowingRecord> findByStudentId(String studentId){
        return borrowingRecords.values().stream()
        .filter(record -> record.getStudentId().equals(studentId))
        .toList();
    }
    @Override
    public Optional<BorrowingRecord> findActiveByBookId(String bookId){
        return borrowingRecords.values().stream()
        .filter(record -> record.getBookId().equals(bookId))
        .filter(BorrowingRecord::isActive)
        .findFirst();
    }
}


