
package library;

import library.config.AdminConfig;
import library.config.DatabaseConnection;
import library.repository.*;
import library.security.PasswordHasher;
import library.service.AuthenticationService;
import library.service.LibraryService;
import library.service.StudentService;
import library.controller.LoginController;
import library.controller.AdminController;
import library.controller.StudentController;
import library.view.LibraryView;

public class Main {

        public static void main(String[] args) {

                // Storage setup

                // Earlier file-based implementations
                // BookStorage storage = new FileBookStorage();
                // StudentStorage studentStorage = new FileStudentStorage();
                // BorrowingRecordStorage borrowRecordsStorage =
                // new FileBorrowingRecordStorage();

                // New MySQL-based implementations
                BookStorage storage = new JdbcBookStorage();
                StudentStorage studentStorage = new JdbcStudentStorage();
                BorrowingRecordStorage borrowRecordsStorage = new JdbcBorrowingRecordStorage();

                // Shared dependencies
                PasswordHasher passwordHasher = new PasswordHasher();
                AdminConfig adminConfig = new AdminConfig();

                // Service setup
                AuthenticationService authenticationService = new AuthenticationService(
                                studentStorage,
                                passwordHasher,
                                adminConfig);

                StudentService studentService = new StudentService(
                                studentStorage,
                                passwordHasher,
                                authenticationService);

                LibraryService libraryService = new LibraryService(
                                storage,
                                studentStorage,
                                authenticationService,
                                borrowRecordsStorage);

                // View setup
                LibraryView view = new LibraryView();

                // Controller setup
                AdminController adminController = new AdminController(
                                libraryService,
                                studentService,
                                authenticationService,
                                view);

                StudentController studentController = new StudentController(
                                libraryService,
                                authenticationService,
                                view);

                LoginController loginController = new LoginController(
                                authenticationService,
                                view,
                                adminController,
                                studentController);

                // Start application
                try {
                        loginController.run();
                } finally {
                        DatabaseConnection.shutdown();
                }
        }
}
