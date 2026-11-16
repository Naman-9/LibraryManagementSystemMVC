
package library.service;

import library.config.AdminConfig;
import library.model.Student;
import library.repository.InMemoryStudentStorage;
import library.security.PasswordHasher;
import library.security.UserRole;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AuthenticationServiceTest {

    private static final String STUDENT_PASSWORD = "student-pass";

    private InMemoryStudentStorage studentStorage;
    private PasswordHasher passwordHasher;
    private AdminConfig adminConfig;
    private AuthenticationService authenticationService;

    @BeforeEach
    void setUp() {
        studentStorage = new InMemoryStudentStorage();
        passwordHasher = new PasswordHasher();
        adminConfig = new AdminConfig();

        authenticationService = new AuthenticationService(
                studentStorage,
                passwordHasher,
                adminConfig
        );
    }

    private Student createStudent(String name) {
        String passwordHash = passwordHasher.hashPassword(STUDENT_PASSWORD);

        Student student = new Student(name, passwordHash);
        studentStorage.save(student);

        return student;
    }

    // 1. Admin authentication

    @Test
    void adminShouldLoginWithCorrectCredentials() {
        assertTrue(authenticationService.authenticateAdmin(
                "ADMIN001",
                "LMS123"
        ));

        assertEquals(
                UserRole.ADMIN,
                authenticationService.getCurrentSession()
                        .orElseThrow()
                        .getRole()
        );
    }

    // 2. Invalid admin credentials

    @Test
    void adminShouldNotLoginWithWrongPassword() {
        assertFalse(authenticationService.authenticateAdmin(
                "ADMIN001",
                "wrong"
        ));

        assertFalse(authenticationService.isLoggedIn());
    }

    // 3. Student authentication

    @Test
    void studentShouldLoginWithCorrectCredentials() {
        Student student = createStudent("Student One");

        assertTrue(authenticationService.authenticateStudent(
                student.getStudentId(),
                STUDENT_PASSWORD
        ));

        assertEquals(
                student.getStudentId(),
                authenticationService.getCurrentSession()
                        .orElseThrow()
                        .getUserId()
        );

        assertEquals(
                UserRole.STUDENT,
                authenticationService.getCurrentSession()
                        .orElseThrow()
                        .getRole()
        );
    }

    // 4. Invalid student credentials

    @Test
    void studentShouldNotLoginWithWrongPassword() {
        Student student = createStudent("Student One");

        assertFalse(authenticationService.authenticateStudent(
                student.getStudentId(),
                "wrong-password"
        ));

        assertFalse(authenticationService.isLoggedIn());
    }
}
