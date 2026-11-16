
package library.service;

import java.util.Optional;

import library.config.AdminConfig;
import library.exception.AuthenticationException;
import library.exception.StudentNotFoundException;
import library.model.Student;
import library.repository.StudentStorage;
import library.security.PasswordHasher;
import library.security.UserRole;
import library.security.UserSession;

public class AuthenticationService {

    private final StudentStorage studentStorage;
    private final PasswordHasher passwordHasher;
    private final AdminConfig adminConfig;

    private UserSession currentSession;

    public AuthenticationService(
            StudentStorage studentStorage,
            PasswordHasher passwordHasher,
            AdminConfig adminConfig) {

        this.studentStorage = studentStorage;
        this.passwordHasher = passwordHasher;
        this.adminConfig = adminConfig;
    }

    public boolean authenticateStudent(String studentId, String password) {

        if (studentId == null || studentId.isBlank()) {
            throw new AuthenticationException("Please provide student ID.");
        }

        if (password == null || password.isBlank()) {
            throw new AuthenticationException("Please provide password.");
        }

        Optional<Student> result = studentStorage.findById(studentId);

        if (result.isEmpty()) {
            return false;
        }

        Student student = result.get();

        if (!passwordHasher.verifyPassword(
                password, student.getPasswordHash())) {
            return false;
        }

        currentSession = new UserSession(studentId, UserRole.STUDENT);
        return true;
    }

    public boolean authenticateAdmin(String adminId, String password) {

        if (adminId == null || adminId.isBlank()) {
            throw new AuthenticationException("Please provide admin ID.");
        }

        if (password == null || password.isBlank()) {
            throw new AuthenticationException("Please provide password.");
        }

        if (!adminId.equals(adminConfig.getAdminId())) {
            return false;
        }

        if (!passwordHasher.verifyPassword(
                password, adminConfig.getAdminPasswordHash())) {
            return false;
        }

        currentSession = new UserSession(adminId, UserRole.ADMIN);
        return true;
    }

    public boolean changeStudentPassword(
            String studentId,
            String currentPassword,
            String newPassword) {

        if (studentId == null || studentId.isBlank()) {
            throw new AuthenticationException("Please provide student ID.");
        }

        if (currentSession == null
                || currentSession.getRole() != UserRole.STUDENT
                || !currentSession.getUserId().equals(studentId)) {
            throw new SecurityException("Access denied.");
        }

        if (currentPassword == null || currentPassword.isBlank()) {
            throw new AuthenticationException("Please provide current password.");
        }

        if (newPassword == null || newPassword.isBlank()) {
            throw new AuthenticationException("Please provide new password.");
        }

        Optional<Student> result = studentStorage.findById(studentId);

        if (result.isEmpty()) {
            throw new StudentNotFoundException("Student not found.");
        }

        Student student = result.get();

        if (!passwordHasher.verifyPassword(
                currentPassword, student.getPasswordHash())) {
            throw new AuthenticationException("Invalid credentials");
        }

        boolean samePassword = passwordHasher.verifyPassword(
                newPassword, student.getPasswordHash());

        if (samePassword) {
            throw new AuthenticationException(
                    "New password must be different from the current password.");
        }

        String newPasswordHash = passwordHasher.hashPassword(newPassword);

        student.updatePasswordHash(newPasswordHash);
        studentStorage.save(student);

        return true;
    }

    public boolean resetStudentPassword(
            String studentId,
            String newPassword) {

        requireRole(UserRole.ADMIN);

        if (studentId == null || studentId.isBlank()) {
            throw new AuthenticationException("Please provide student ID.");
        }

        if (newPassword == null || newPassword.isBlank()) {
            throw new AuthenticationException("Please provide new password.");
        }

        Optional<Student> result = studentStorage.findById(studentId);

        if (result.isEmpty()) {
            return false;
        }

        Student student = result.get();

        student.updatePasswordHash(passwordHasher.hashPassword(newPassword));
        studentStorage.save(student);

        return true;
    }

    public Optional<UserSession> getCurrentSession() {
        return Optional.ofNullable(currentSession);
    }

    public boolean isLoggedIn() {
        return currentSession != null;
    }

    public void logout() {
        currentSession = null;
    }

    void requireRole(UserRole requiredRole) {
        if (currentSession == null
                || currentSession.getRole() != requiredRole) {
            throw new SecurityException("Access denied.");
        }
    }

    void requireLoggedIn() {
        if (currentSession == null) {
            throw new SecurityException("Please log in first.");
        }
    }
}
