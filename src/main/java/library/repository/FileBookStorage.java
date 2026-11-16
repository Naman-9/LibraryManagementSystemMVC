package library.repository;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import library.exception.InvalidBookException;
import library.exception.StorageException;
import library.model.Book;

public class FileBookStorage implements BookStorage {

    private final Map<String, Book> books;
    private final Path filePath;

    public FileBookStorage() {
        this(Path.of("data", "books.csv"));
    }

    public FileBookStorage(Path filePath) {

        if (filePath == null) {
            throw new StorageException("Book storage file path cannot be null.");
        }
        this.books = new HashMap<>();
        this.filePath = filePath;

        try {
            if (filePath.getParent() != null) {
                Files.createDirectories(filePath.getParent());
            }
            if (!Files.exists(filePath)) {
                Files.createFile(filePath);
            }

            loadBooks();

        } catch (IOException e) {
            throw new StorageException(
                    "Unable to initialize book storage.", e);
        }
    }

    private void loadBooks() {
        try {
            List<String> lines = Files.readAllLines(filePath);

            for (String line : lines) {
                if (line.isBlank()) {
                    continue;
                }

                String[] fields = line.split(",", -1);
                if (fields.length != 5) {
                    throw new StorageException("Invalid book record: " + line);
                }

                String id = fields[0];
                String title = fields[1];
                String author = fields[2];
                String tagData = fields[3];
                String issuedTo = fields[4];

                if (issuedTo.isEmpty()) {
                    issuedTo = null;
                }

                Set<String> tags;

                if (tagData.isBlank()) {
                    tags = new HashSet<>();
                } else {
                    tags = new HashSet<>(Arrays.asList(tagData.split("\\|")));
                }

                try {
                    Book book = new Book(id, title, author, tags, issuedTo);

                    if (books.containsKey(id)) {
                        throw new StorageException("Duplicate book ID: " + id);
                    }

                    books.put(id, book);

                } catch (InvalidBookException e) {
                    throw new StorageException(
                            "Invalid book data in storage: " + line, e);
                }
            }
        } catch (IOException e) {
            throw new StorageException(
                    "Unable to load books from file.", e);
        }

    }

    // write hashmap data in books.csv
    private void writeBooks() {

        List<String> lines = new ArrayList<>();
        for (Book book : books.values()) {

            String tags = String.join("|", book.getTags());
            String issuedTo = !book.isAvailable() ? book.getIssuedTo() : "";

            String record = book.getId() + "," + book.getTitle() + "," + book.getAuthor() + "," + tags + "," + issuedTo;

            lines.add(record);

        }

        Path tempPath = filePath.resolveSibling("books.tmp");

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
                    "Unable to update books file.", e);
        }

    }

    @Override
    public void save(Book book) {
        Book previousBook = books.put(book.getId(), book);

        try {
            writeBooks();
        } catch (StorageException e) {
            if (previousBook == null) {
                books.remove(book.getId());
            } else {
                books.put(book.getId(), previousBook);
            }
            throw e;
        }
    }

    @Override
    public Optional<Book> findById(String id) {
        return Optional.ofNullable(books.get(id));
    }

    @Override
    public List<Book> findAll() {
        return books.values().stream().sorted(Comparator.comparing(Book::getId)).toList();
    }

    @Override
    public void delete(String id) {
        Book removedBook = books.remove(id);

        try {
            writeBooks();
        } catch (StorageException e) {
            if (removedBook != null) {
                books.put(id, removedBook);
            }
            throw e;
        }
    }

}