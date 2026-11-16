
package library.repository;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.AtomicMoveNotSupportedException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import library.exception.InvalidStudentException;
import library.exception.StorageException;
import library.model.Student;

public class FileStudentStorage implements StudentStorage {

    private final Map<String, Student> students;
    private final Path filePath;

    public FileStudentStorage() {
        this(Path.of("data", "students.csv"));
    }

    public FileStudentStorage(Path filePath) {
        this.students = new LinkedHashMap<>();
        this.filePath = filePath;

        try {
            Files.createDirectories(filePath.getParent());

            if (!Files.exists(filePath)) {
                Files.createFile(filePath);
            }

            loadStudents();

        } catch (IOException e) {
            throw new StorageException(
                    "Unable to initialize student storage.", e);
        }
    }

    private void loadStudents() throws IOException {

        List<String> lines = Files.readAllLines(
                filePath, StandardCharsets.UTF_8);

        for (String line : lines) {

            if (line.isBlank()) {
                continue;
            }

            String[] data = line.split(",", -1);

            if (data.length != 3) {
                throw new StorageException(
                        "Invalid student record in file: " + line);
            }

            String studentId = data[0];
            String name;

            try {
                name = new String(
                        Base64.getDecoder().decode(data[1]),
                        StandardCharsets.UTF_8);
            } catch (IllegalArgumentException e) {
                throw new StorageException(
                        "Invalid encoded student name in storage: " + line, e);
            }

            String passwordHash = data[2];

            try {
                Student student = new Student(
                        studentId, name, passwordHash);

                if (students.putIfAbsent(studentId, student) != null) {
                    throw new StorageException(
                            "Duplicate student ID: " + studentId);
                }

            } catch (InvalidStudentException e) {
                throw new StorageException(
                        "Invalid student data in storage: " + line, e);
            }
        }
    }

    @Override
    public void save(Student student) {

        Student previousStudent =
                students.put(student.getStudentId(), student);

        try {
            writeStudents();
        } catch (StorageException e) {
            if (previousStudent == null) {
                students.remove(student.getStudentId());
            } else {
                students.put(student.getStudentId(), previousStudent);
            }

            throw e;
        }
    }

    private void writeStudents() {

        List<String> lines = new ArrayList<>();

        for (Student student : students.values()) {

            String encodedName = Base64.getEncoder().encodeToString(
                    student.getName().getBytes(StandardCharsets.UTF_8));

            lines.add(
                    student.getStudentId() + ","
                            + encodedName + ","
                            + student.getPasswordHash());
        }

        Path tempPath = filePath.resolveSibling("students.tmp");

        try {
            Files.write(
                    tempPath, lines, StandardCharsets.UTF_8);

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
                    "Could not save student records.", e);
        }
    }

    @Override
    public Optional<Student> findById(String studentId) {
        return Optional.ofNullable(students.get(studentId));
    }

    @Override
    public List<Student> findAll() {
        return students.values()
                .stream()
                .sorted(Comparator.comparing(Student::getStudentId))
                .toList();
    }

    @Override
    public void delete(String studentId) {
        if (students.remove(studentId) != null) {
            writeStudents();
        }
    }
}
