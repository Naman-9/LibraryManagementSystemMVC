package library.model;

import library.exception.InvalidStudentException;

public class Student {

    private final String studentId;
    private String name;
    private String passwordHash;
    private static int idCounter = 1;

    private static String generateId() {
        return String.format("S%04d", idCounter++);
    }

    public Student(String name, String passwordHash) {

        if (name == null || name.isBlank()) {
            throw new InvalidStudentException(
                    "Please provide name.");
        }
        if (passwordHash == null || passwordHash.isBlank()) {
            throw new InvalidStudentException(
                    "Please provide password.");
        }

        this.studentId = generateId();
        this.name = name.trim();
        this.passwordHash = passwordHash;
    }

    public Student(String id, String name, String passwordHash) {

        if (id == null || id.isBlank()) {
            throw new InvalidStudentException(
                    "Please provide id.");
        }
        if (name == null || name.isBlank()) {
            throw new InvalidStudentException(
                    "Please provide name.");
        }
        if (passwordHash == null || passwordHash.isBlank()) {
            throw new InvalidStudentException(
                    "Please provide password.");
        }

        this.studentId = id;
        updateIdCounter(id);
        this.name = name.trim();
        this.passwordHash = passwordHash;
    }

    private static void updateIdCounter(String existingId) {
        if (existingId == null || !existingId.matches("S\\d{4,}")) {
            throw new InvalidStudentException("Invalid student ID: " + existingId);
        }
        int number = Integer.parseInt(existingId.substring(1));
        idCounter = Math.max(idCounter, number + 1);

    }

    public String getStudentId() {
        return studentId;
    }

    public String getName() {
        return name;
    }

    public void updateName(String name) {
        if (name == null || name.isBlank()) {
            throw new InvalidStudentException("Student name cannot be blank.");
        }
        this.name = name.trim();
    }

    public void updatePasswordHash(String newPasswordHash) {
        if (newPasswordHash == null || newPasswordHash.isBlank()) {
            throw new InvalidStudentException("Invalid password hash.");
        }

        this.passwordHash = newPasswordHash;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

}
