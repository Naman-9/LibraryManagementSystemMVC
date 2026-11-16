
package library.repository;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import library.exception.InvalidBorrowingRecordException;
import library.exception.StorageException;
import library.model.Book;
import library.model.BorrowingRecord;

public class FileBorrowingRecordStorage implements BorrowingRecordStorage {

    private final Map<String, BorrowingRecord> borrowRecords;
    private final Path filePath;

    public FileBorrowingRecordStorage() {
        this(Path.of("data", "borrowRecords.csv"));
    }

    public FileBorrowingRecordStorage(Path filePath) {
        this.borrowRecords = new HashMap<>();
        this.filePath = filePath;

        try {
            Files.createDirectories(filePath.getParent());

            if (!Files.exists(filePath)) {
                Files.createFile(filePath);
            }

            loadRecords();

        } catch (IOException e) {
            throw new StorageException(
                    "Unable to initialize borrowing record storage.", e);
        }
    }

    private void loadRecords() {
        try {
            List<String> lines = Files.readAllLines(filePath, StandardCharsets.UTF_8);

            for (String line : lines) {
                if (line.isBlank()) {
                    continue;
                }

                String[] fields = line.split(",", -1);
                if (fields.length != 5) {
                    throw new InvalidBorrowingRecordException("Invalid record: " + line);
                }

                String recordId = fields[0];
                String bookId = fields[1];
                String studentId = fields[2];
                String issueDate = fields[3];
                String returnDate = fields[4];

                BorrowingRecord record = new BorrowingRecord(
                        recordId,
                        bookId,
                        studentId,
                        LocalDate.parse(issueDate),
                        returnDate.isBlank() ? null : LocalDateTime.parse(returnDate));

                if (borrowRecords.containsKey(recordId)) {
                    throw new InvalidBorrowingRecordException("Duplicate record ID: " + recordId);
                }

                borrowRecords.put(recordId, record);
            }

        } catch (IOException e) {
            throw new StorageException(
                    "Unable to load records from file.", e);
        }

    }

    // write hashmap data in borrowRecords.csv
    private void writeRecords() {

        List<String> lines = new ArrayList<>();
        for (BorrowingRecord record : borrowRecords.values()) {

            String line = record.getRecordId() + ","
                    + record.getBookId() + ","
                    + record.getStudentId() + ","
                    + record.getIssueDate() + ","
                    + (record.getReturnDate() == null ? "" : record.getReturnDate());

            lines.add(line);

        }

        Path tempPath = filePath.resolveSibling("borrowRecords.tmp");

        try {
            Files.write(tempPath, lines, StandardCharsets.UTF_8);

            try {
                Files.move(
                        tempPath,
                        filePath,
                        StandardCopyOption.REPLACE_EXISTING,
                        StandardCopyOption.ATOMIC_MOVE);
            } catch (AtomicMoveNotSupportedException e) {
                Files.move(
                        tempPath,
                        filePath,
                        StandardCopyOption.REPLACE_EXISTING);
            }

        } catch (IOException e) {
            throw new StorageException(
                    "Unable to update borrow records file.", e);
        }

    }

    @Override
    public void save(BorrowingRecord record) {
        borrowRecords.put(record.getRecordId(), record);
        writeRecords();
    };

    @Override
    public Optional<BorrowingRecord> findById(String recordId) {
        return Optional.ofNullable(borrowRecords.get(recordId));
    };

    @Override
    public List<BorrowingRecord> findAll() {
        return borrowRecords.values().stream().sorted(Comparator.comparing(BorrowingRecord::getRecordId)).toList();
    };

    @Override
    public List<BorrowingRecord> findByStudentId(String studentId) {
        return borrowRecords.values().stream()
                .filter(record -> record.getStudentId().equals(studentId)).toList();
    };

    @Override
    public Optional<BorrowingRecord> findActiveByBookId(String bookId) {
        return borrowRecords.values().stream()
                .filter(record -> record.getBookId().equals(bookId))
                .filter(BorrowingRecord::isActive)
                .findFirst();
    }
}
