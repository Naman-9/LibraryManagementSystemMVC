
package library.repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import library.config.DatabaseConnection;
import library.exception.StorageException;
import library.model.BorrowingRecord;

public class JdbcBorrowingRecordStorage implements BorrowingRecordStorage {

    @Override
    public void save(BorrowingRecord record) {
        String sql = """
                INSERT INTO borrowing_records
                    (record_id, book_id, student_id, issue_date, return_date)
                VALUES (?, ?, ?, ?, ?)
                ON DUPLICATE KEY UPDATE
                    book_id = VALUES(book_id),
                    student_id = VALUES(student_id),
                    issue_date = VALUES(issue_date),
                    return_date = VALUES(return_date)
                """;

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, record.getRecordId());
            statement.setString(2, record.getBookId());
            statement.setString(3, record.getStudentId());
            statement.setDate(4, java.sql.Date.valueOf(record.getIssueDate()));

            LocalDateTime returnDate = record.getReturnDate();

            if (returnDate == null) {
                statement.setNull(5, Types.TIMESTAMP);
            } else {
                statement.setTimestamp(5, Timestamp.valueOf(returnDate));
            }

            statement.executeUpdate();

        } catch (SQLException e) {
            throw new StorageException("Unable to save borrowing record.", e);
        }
    }

    @Override
    public Optional<BorrowingRecord> findById(String recordId) {
        String sql = """
                SELECT record_id, book_id, student_id, issue_date, return_date
                FROM borrowing_records
                WHERE record_id = ?
                """;

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, recordId);

            try (ResultSet resultSet = statement.executeQuery()) {

                if (resultSet.next()) {
                    return Optional.of(mapRecord(resultSet));
                }

                return Optional.empty();
            }

        } catch (SQLException e) {
            throw new StorageException(
                    "Unable to find borrowing record: " + recordId, e);
        }
    }

    @Override
    public List<BorrowingRecord> findAll() {
        String sql = """
                SELECT record_id, book_id, student_id, issue_date, return_date
                FROM borrowing_records
                ORDER BY record_id
                """;

        List<BorrowingRecord> records = new ArrayList<>();

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                records.add(mapRecord(resultSet));
            }

            return records;

        } catch (SQLException e) {
            throw new StorageException(
                    "Unable to retrieve borrowing records.", e);
        }
    }

    @Override
    public List<BorrowingRecord> findByStudentId(String studentId) {
        String sql = """
                SELECT record_id, book_id, student_id, issue_date, return_date
                FROM borrowing_records
                WHERE student_id = ?
                """;

        List<BorrowingRecord> records = new ArrayList<>();

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, studentId);

            try (ResultSet resultSet = statement.executeQuery()) {

                while (resultSet.next()) {
                    records.add(mapRecord(resultSet));
                }

            }

            return records;

        } catch (SQLException e) {
            throw new StorageException(
                    "Unable to retrieve borrowing records for student: "
                            + studentId, e);
        }
    }

    @Override
    public Optional<BorrowingRecord> findActiveByBookId(String bookId) {
        String sql = """
                SELECT record_id, book_id, student_id, issue_date, return_date
                FROM borrowing_records
                WHERE book_id = ?
                  AND return_date IS NULL
                """;

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, bookId);

            try (ResultSet resultSet = statement.executeQuery()) {

                if (resultSet.next()) {
                    BorrowingRecord record = mapRecord(resultSet);

                    if (record.isActive()) {
                        return Optional.of(record);
                    }
                }

                return Optional.empty();
            }

        } catch (SQLException e) {
            throw new StorageException(
                    "Unable to find active borrowing record for book: "
                            + bookId, e);
        }
    }

    private BorrowingRecord mapRecord(ResultSet resultSet)
            throws SQLException {

        String recordId = resultSet.getString("record_id");
        String bookId = resultSet.getString("book_id");
        String studentId = resultSet.getString("student_id");

        LocalDate issueDate =
                resultSet.getDate("issue_date").toLocalDate();

        Timestamp returnTimestamp =
                resultSet.getTimestamp("return_date");

        LocalDateTime returnDate = returnTimestamp == null
                ? null
                : returnTimestamp.toLocalDateTime();

        return new BorrowingRecord(
                recordId,
                bookId,
                studentId,
                issueDate,
                returnDate
        );
    }
}
