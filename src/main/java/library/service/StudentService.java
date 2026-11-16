
package library.service;

import java.util.List;

import library.exception.InvalidStudentException;
import library.model.Student;
import library.repository.StudentStorage;
import library.security.PasswordHasher;
import library.security.UserRole;

public class StudentService {

    private final StudentStorage studentStorage;
    private final PasswordHasher passwordHasher;
    private final AuthenticationService authenticationService;

    public StudentService(
            StudentStorage studentStorage,
            PasswordHasher passwordHasher,
            AuthenticationService authenticationService) {

        this.studentStorage = studentStorage;
        this.passwordHasher = passwordHasher;
        this.authenticationService = authenticationService;
    }

    public void registerStudent(String name, String password) {

        authenticationService.requireRole(UserRole.ADMIN);

        if (name == null || name.isBlank()) {
            throw new InvalidStudentException("Please provide name.");
        }

        if (password == null || password.isBlank()) {
            throw new InvalidStudentException("Please provide password.");
        }

        String passwordHash = passwordHasher.hashPassword(password);
        Student student = new Student(name, passwordHash);

        studentStorage.save(student);
    }

    public List<Student> getAllStudents() {

        authenticationService.requireRole(UserRole.ADMIN);

        return studentStorage.findAll();
    }
}
