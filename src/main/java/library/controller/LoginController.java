
package library.controller;

import library.exception.LibraryException;
import library.security.UserSession;
import library.service.AuthenticationService;
import library.view.LibraryView;

public class LoginController {

    private final AuthenticationService authenticationService;
    private final LibraryView view;
    private final AdminController adminController;
    private final StudentController studentController;

    public LoginController(
            AuthenticationService authenticationService,
            LibraryView view,
            AdminController adminController,
            StudentController studentController) {

        this.authenticationService = authenticationService;
        this.view = view;
        this.adminController = adminController;
        this.studentController = studentController;
    }

    public void run() {
        boolean running = true;

        while (running) {
            view.displayLoginMenu();

            int choice = view.getUserChoice();

            switch (choice) {
                case 1 -> handleAdminLogin();
                case 2 -> handleStudentLogin();
                case 0 -> {
                    running = false;
                    view.displayMessage(
                            "Thank you for using the Library Management System.");
                }
                default -> view.displayMessage("Invalid choice. Try again.");
            }
        }
    }

    private void handleAdminLogin() {
        String adminId = view.getInput("Admin ID: ");
        String password = view.getInput("Password: ");

        try {
            if (authenticationService.authenticateAdmin(adminId, password)) {
                view.displayMessage("Admin login successful.");
                adminController.run();
            } else {
                view.displayMessage("Invalid admin credentials.");
            }
        } catch (LibraryException e) {
            view.displayMessage(e.getMessage());
        }
    }

    private void handleStudentLogin() {
        String studentId = view.getInput("Student ID: ");
        String password = view.getInput("Password: ");

        try {
            if (authenticationService.authenticateStudent(studentId, password)) {
                view.displayMessage("Student login successful.");
                studentController.run();
            } else {
                view.displayMessage("Invalid student credentials.");
            }
        } catch (LibraryException e) {
            view.displayMessage(e.getMessage());
        }
    }
}
