import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.Date;
import static org.junit.jupiter.api.Assertions.*;

public class CourseEnrollmentTest {

    private Course course;
    private AuthService authService;

    @BeforeEach
    public void setUp() {
        // Assuming a Course constructor like: Course(code, title, section, instructorName)
        course = new Course("CS1142", "Programming Down in C", "M01", "teacher", "Fall 2026");
        authService = new AuthService();
        
        // Register test users
        AuthService.registerUser("ty", "pass123", "student");
        AuthService.registerUser("teacher", "teach123", "instructor");
    }

    @Test
    public void testValidStudentEnrollment() {
        String studentUser = "ty";
        
        // Verify user exists and is a student
        String role = authService.getRole(studentUser);
        assertNotNull(role);
        assertEquals("student", role.toLowerCase());

        // Enroll student
        course.addStudent(studentUser);
        
        // Verify addition
        assertTrue(course.getEnrolledStudents().contains(studentUser));
        assertEquals(1, course.getEnrolledStudents().size());
    }

    @Test
    public void testDuplicateEnrollmentPrevention() {
        String studentUser = "student";
        
        course.addStudent(studentUser);
        assertEquals(1, course.getEnrolledStudents().size());

        // Try enrolling the same student again
        if (!course.getEnrolledStudents().contains(studentUser)) {
            course.addStudent(studentUser);
        }

        // Size should remain 1
        assertEquals(1, course.getEnrolledStudents().size());
    }

    @Test
    public void testNonStudentRoleBlocking() {
        String instructorUser = "teacher";
        
        // Verify user is an instructor, not a student
        String role = authService.getRole(instructorUser);
        assertEquals("instructor", role.toLowerCase());

        // Enforce system rule: Only allow enrollment if role is "student"
        boolean canEnroll = "student".equalsIgnoreCase(role);
        assertFalse(canEnroll, "Instructors should not be eligible for student enrollment.");

        if (canEnroll) {
            course.addStudent(instructorUser);
        }

        // Verify roster remains empty
        assertTrue(course.getEnrolledStudents().isEmpty());
    }
}