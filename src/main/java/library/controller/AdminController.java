
package library.controller;

import java.util.List;
import java.util.Set;
import java.util.HashSet;

import library.exception.LibraryException;
import library.model.Book;
import library.model.BorrowingRecord;
import library.model.Student;
import library.service.AuthenticationService;
import library.service.LibraryService;
import library.service.StudentService;
import library.view.LibraryView;

public class AdminController {

    private final LibraryService libraryService;
    private final StudentService studentService;
    private final AuthenticationService authenticationService;
    private final LibraryView view;

    public AdminController(
            LibraryService libraryService,
            StudentService studentService,
            AuthenticationService authenticationService,
            LibraryView view) {

        this.libraryService = libraryService;
        this.studentService = studentService;
        this.authenticationService = authenticationService;
        this.view = view;
    }

    public void run() {
        boolean loggedIn = true;

        while (loggedIn) {
            view.displayAdminMenu();

            int choice = view.getUserChoice();

            switch (choice) {
                case 1 -> handleShowBooks();
                case 2 -> handleAddBook();
                case 3 -> handleUpdateBook();
                case 4 -> handleDeleteBook();
                case 5 -> handleRegisterStudent();
                case 6 -> handleShowStudents();
                case 7 -> handleAllBorrowingRecords();
                case 0 -> {
                    authenticationService.logout();
                    loggedIn = false;
                    view.displayMessage("Admin logged out.");
                }
                default -> view.displayMessage("Invalid choice. Try again.");
            }
        }
    }

    private void handleShowBooks() {
        view.displayBookList(libraryService.getAllBooks());
    }

    private void handleAddBook() {
        String title = view.getInput("Title: ");
        String author = view.getInput("Author: ");
        String tagsInput = view.getInput("Tags (comma separated): ");

        Set<String> tags = new HashSet<>();

        for (String tag : tagsInput.split(",")) {
            if (!tag.isBlank()) {
                tags.add(tag.trim());
            }
        }

        try {
            libraryService.addBook(title, author, tags);
            view.displayMessage("Book added.");
        } catch (LibraryException e) {
            view.displayMessage(e.getMessage());
        }
    }

    private void handleUpdateBook() {
        String id = view.getInput("Id: ");
        String title = view.getInput("Title: ");
        String author = view.getInput("Author: ");

        try {
            libraryService.updateBook(id, title, author);
            view.displayMessage("Book updated.");
        } catch (LibraryException e) {
            view.displayMessage(e.getMessage());
        }
    }

    private void handleDeleteBook() {
        String id = view.getInput("Id to delete: ");

        try {
            libraryService.deleteBook(id);
            view.displayMessage("Book deleted.");
        } catch (LibraryException e) {
            view.displayMessage(e.getMessage());
        }
    }

    private void handleRegisterStudent() {
        String name = view.getInput("Student name: ");
        String password = view.getInput("Student password: ");

        try {
            studentService.registerStudent(name, password);
            view.displayMessage("Student registered successfully.");
        } catch (LibraryException e) {
            view.displayMessage(e.getMessage());
        }
    }

    private void handleShowStudents() {
        List<Student> students = studentService.getAllStudents();
        view.displayStudentList(students);
    }

    private void handleAllBorrowingRecords() {
        List<BorrowingRecord> records =
                libraryService.getAllBorrowingRecords();

        view.displayBorrowingRecords(records);
    }
}
