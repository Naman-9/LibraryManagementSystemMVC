
package library.repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import library.config.DatabaseConnection;
import library.exception.StorageException;
import library.model.Student;

public class JdbcStudentStorage implements StudentStorage {

    @Override
    public void save(Student student) {
        String sql = """
                INSERT INTO students (student_id, name, password_hash)
                VALUES (?, ?, ?)
                ON DUPLICATE KEY UPDATE
                    name = VALUES(name),
                    password_hash = VALUES(password_hash)
                """;

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, student.getStudentId());
            statement.setString(2, student.getName());
            statement.setString(3, student.getPasswordHash());

            statement.executeUpdate();

        } catch (SQLException e) {
            throw new StorageException("Unable to save student.", e);
        }
    }

    @Override
    public Optional<Student> findById(String id) {
        String sql = """
                SELECT student_id, name, password_hash
                FROM students
                WHERE student_id = ?
                """;

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, id);

            try (ResultSet resultSet = statement.executeQuery()) {

                if (resultSet.next()) {
                    return Optional.of(mapStudent(resultSet));
                }

                return Optional.empty();
            }

        } catch (SQLException e) {
            throw new StorageException("Unable to find student: " + id, e);
        }
    }

    @Override
    public List<Student> findAll() {
        String sql = """
                SELECT student_id, name, password_hash
                FROM students
                ORDER BY student_id
                """;

        List<Student> students = new ArrayList<>();

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                students.add(mapStudent(resultSet));
            }

            return students;

        } catch (SQLException e) {
            throw new StorageException("Unable to retrieve students.", e);
        }
    }

    @Override
    public void delete(String id) {
        String sql = "DELETE FROM students WHERE student_id = ?";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, id);
            statement.executeUpdate();

        } catch (SQLException e) {
            throw new StorageException("Unable to delete student: " + id, e);
        }
    }

    private Student mapStudent(ResultSet resultSet) throws SQLException {
        return new Student(
                resultSet.getString("student_id"),
                resultSet.getString("name"),
                resultSet.getString("password_hash")
        );
    }
}
