
package library.service;

import library.config.AdminConfig;
import library.model.Student;
import library.repository.InMemoryStudentStorage;
import library.security.PasswordHasher;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class StudentServiceTest {

    private InMemoryStudentStorage studentStorage;
    private PasswordHasher passwordHasher;
    private AuthenticationService authenticationService;
    private StudentService studentService;

    @BeforeEach
    void setUp() {
        studentStorage = new InMemoryStudentStorage();
        passwordHasher = new PasswordHasher();

        AdminConfig adminConfig = new AdminConfig();

        authenticationService = new AuthenticationService(
                studentStorage,
                passwordHasher,
                adminConfig
        );

        studentService = new StudentService(
                studentStorage,
                passwordHasher,
                authenticationService
        );
    }

    private void loginAsAdmin() {
        assertTrue(authenticationService.authenticateAdmin(
                "ADMIN001",
                "LMS123"
        ));
    }

    private Student createStudent(String name) {
        String passwordHash = passwordHasher.hashPassword("student-pass");

        Student student = new Student(name, passwordHash);
        studentStorage.save(student);

        return student;
    }

    // 1. Admin can register a student

    @Test
    void adminShouldBeAbleToRegisterStudent() {
        loginAsAdmin();

        studentService.registerStudent("Student One", "student-pass");

        List<Student> students = studentStorage.findAll();

        assertEquals(1, students.size());

        assertTrue(passwordHasher.verifyPassword(
                "student-pass",
                students.get(0).getPasswordHash()
        ));
    }

    // 2. Unauthenticated user cannot register a student

    @Test
    void unauthenticatedUserShouldNotRegisterStudent() {
        assertThrows(
                SecurityException.class,
                () -> studentService.registerStudent(
                        "Student One",
                        "student-pass"
                )
        );

        assertTrue(studentStorage.findAll().isEmpty());
    }

    // 3. Student cannot register another student

    @Test
    void studentShouldNotRegisterAnotherStudent() {
        Student student = createStudent("Existing Student");

        assertTrue(authenticationService.authenticateStudent(
                student.getStudentId(),
                "student-pass"
        ));

        assertThrows(
                SecurityException.class,
                () -> studentService.registerStudent(
                        "New Student",
                        "new-password"
                )
        );

        assertEquals(1, studentStorage.findAll().size());
    }

    // 4. Admin can view all students

    @Test
    void adminShouldBeAbleToViewAllStudents() {
        createStudent("Student One");
        createStudent("Student Two");

        loginAsAdmin();

        List<Student> students = studentService.getAllStudents();

        assertEquals(2, students.size());
    }

    // 5. Student cannot view all students

    @Test
    void studentShouldNotBeAbleToViewAllStudents() {
        Student student = createStudent("Student One");

        assertTrue(authenticationService.authenticateStudent(
                student.getStudentId(),
                "student-pass"
        ));

        assertThrows(
                SecurityException.class,
                () -> studentService.getAllStudents()
        );
    }
}
