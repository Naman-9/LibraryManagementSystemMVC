
package library.controller;

import java.util.List;

import library.exception.LibraryException;
import library.model.Book;
import library.model.BorrowingRecord;
import library.security.UserRole;
import library.service.AuthenticationService;
import library.service.LibraryService;
import library.view.LibraryView;

public class StudentController {

    private final LibraryService libraryService;
    private final AuthenticationService authenticationService;
    private final LibraryView view;

    public StudentController(
            LibraryService libraryService,
            AuthenticationService authenticationService,
            LibraryView view) {

        this.libraryService = libraryService;
        this.authenticationService = authenticationService;
        this.view = view;
    }

    public void run() {
        boolean loggedIn = true;

        while (loggedIn) {
            view.displayStudentMenu();

            int choice = view.getUserChoice();

            switch (choice) {
                case 1 -> handleShowBooks();
                case 2 -> handleSearchBooks();
                case 3 -> handleIssueBook();
                case 4 -> handleReturnBook();
                case 5 -> handleMyBorrowingHistory();
                case 0 -> {
                    authenticationService.logout();
                    loggedIn = false;
                    view.displayMessage("Student logged out.");
                }
                default -> view.displayMessage("Invalid choice. Try again.");
            }
        }
    }

    private void handleShowBooks() {
        view.displayBookList(libraryService.getAllBooks());
    }

    private void handleSearchBooks() {
        view.displayMessage("Search by:");
        view.displayMessage("1. Book ID");
        view.displayMessage("2. Title");
        view.displayMessage("3. Author");
        view.displayMessage("4. Tag");

        int choice = view.getUserChoice();
        String keyword;

        switch (choice) {
            case 1 -> {
                keyword = view.getInput("Enter book ID: ");
                showResults(libraryService.searchById(keyword));
            }
            case 2 -> {
                keyword = view.getInput("Enter title: ");
                showResults(libraryService.searchByTitle(keyword));
            }
            case 3 -> {
                keyword = view.getInput("Enter author: ");
                showResults(libraryService.searchByAuthor(keyword));
            }
            case 4 -> {
                keyword = view.getInput("Enter tag: ");
                showResults(libraryService.searchByTag(keyword));
            }
            default -> view.displayMessage("Invalid search option.");
        }
    }

    private void showResults(List<Book> results) {
        if (results.isEmpty()) {
            view.displayMessage("No Book Found.");
        } else {
            view.displayBookList(results);
        }
    }

    private void handleIssueBook() {
        String bookId = view.getInput("Enter Book ID: ");

        try {
            libraryService.issueBook(bookId);
            view.displayMessage("Book issued successfully.");
        } catch (LibraryException | SecurityException e) {
            view.displayMessage(e.getMessage());
        }
    }

    private void handleReturnBook() {
        String bookId = view.getInput("Id to return: ");

        try {
            libraryService.returnBook(bookId);
            view.displayMessage("Book returned.");
        } catch (LibraryException | SecurityException e) {
            view.displayMessage(e.getMessage());
        }
    }

    private void handleMyBorrowingHistory() {
        List<BorrowingRecord> records =
                libraryService.getMyBorrowingRecords();

        view.displayBorrowingRecords(records);
    }
}
