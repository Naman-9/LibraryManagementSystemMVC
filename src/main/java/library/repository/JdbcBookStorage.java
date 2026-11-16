
package library.repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import library.config.DatabaseConnection;
import library.exception.StorageException;
import library.model.Book;

public class JdbcBookStorage implements BookStorage {

    @Override
    public void save(Book book) {
        String bookSql = """
                INSERT INTO books (book_id, title, author, issued_to)
                VALUES (?, ?, ?, ?)
                ON DUPLICATE KEY UPDATE
                    title = VALUES(title),
                    author = VALUES(author),
                    issued_to = VALUES(issued_to)
                """;

        String deleteTagsSql =
                "DELETE FROM book_tags WHERE book_id = ?";

        String insertTagSql =
                "INSERT INTO book_tags (book_id, tag) VALUES (?, ?)";

        try (Connection connection = DatabaseConnection.getConnection()) {

            connection.setAutoCommit(false);

            try {
                try (PreparedStatement statement =
                             connection.prepareStatement(bookSql)) {

                    statement.setString(1, book.getId());
                    statement.setString(2, book.getTitle());
                    statement.setString(3, book.getAuthor());
                    statement.setString(4, book.getIssuedTo());

                    statement.executeUpdate();
                }

                try (PreparedStatement statement =
                             connection.prepareStatement(deleteTagsSql)) {

                    statement.setString(1, book.getId());
                    statement.executeUpdate();
                }

                try (PreparedStatement statement =
                             connection.prepareStatement(insertTagSql)) {

                    for (String tag : book.getTags()) {
                        statement.setString(1, book.getId());
                        statement.setString(2, tag);
                        statement.addBatch();
                    }

                    statement.executeBatch();
                }

                connection.commit();

            } catch (SQLException e) {
                connection.rollback();
                throw e;
            }

        } catch (SQLException e) {
            throw new StorageException("Unable to save book.", e);
        }
    }

    @Override
    public Optional<Book> findById(String id) {
        String sql = """
                SELECT book_id, title, author, issued_to
                FROM books
                WHERE book_id = ?
                """;

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, id);

            try (ResultSet resultSet = statement.executeQuery()) {

                if (resultSet.next()) {
                    return Optional.of(mapBook(resultSet, connection));
                }

                return Optional.empty();
            }

        } catch (SQLException e) {
            throw new StorageException("Unable to find book: " + id, e);
        }
    }

    @Override
    public List<Book> findAll() {
        String sql = """
                SELECT book_id, title, author, issued_to
                FROM books
                ORDER BY book_id
                """;

        List<Book> books = new ArrayList<>();

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                books.add(mapBook(resultSet, connection));
            }

            return books;

        } catch (SQLException e) {
            throw new StorageException("Unable to retrieve books.", e);
        }
    }

    @Override
    public void delete(String id) {
        String sql = "DELETE FROM books WHERE book_id = ?";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, id);
            statement.executeUpdate();

        } catch (SQLException e) {
            throw new StorageException("Unable to delete book: " + id, e);
        }
    }

    private Book mapBook(ResultSet resultSet, Connection connection)
            throws SQLException {

        String id = resultSet.getString("book_id");
        String title = resultSet.getString("title");
        String author = resultSet.getString("author");
        String issuedTo = resultSet.getString("issued_to");

        Set<String> tags = new HashSet<>();

        String tagSql = "SELECT tag FROM book_tags WHERE book_id = ?";

        try (PreparedStatement statement =
                     connection.prepareStatement(tagSql)) {

            statement.setString(1, id);

            try (ResultSet tagResultSet = statement.executeQuery()) {
                while (tagResultSet.next()) {
                    tags.add(tagResultSet.getString("tag"));
                }
            }
        }

        return new Book(id, title, author, tags, issuedTo);
    }
}
