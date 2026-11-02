import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class AuthServiceTest {

    private AuthService authService;

    @BeforeEach
    public void setUp() {
        authService = new AuthService();
    }

    @Test
    public void testDefaultSeedAccounts() {
        // Verify default accounts exist and have correct roles
        assertEquals("admin", authService.getRole("admin"));
        assertEquals("instructor", authService.getRole("teacher"));
        assertEquals("student", authService.getRole("student"));
    }

    @Test
    public void testSuccessfulAuthentication() {
        // Verify default credentials authenticate correctly
        assertTrue(authService.authenticate("admin", "admin2026"));
        assertTrue(authService.authenticate("student", "password123"));
    }

    @Test
    public void testFailedAuthentication() {
        // Wrong password or non-existent user should fail
        assertFalse(authService.authenticate("admin", "wrongpassword"));
        assertFalse(authService.authenticate("nonexistent", "password123"));
        assertFalse(authService.authenticate(null, null));
    }

    @Test
    public void testUserRegistrationAndRoleLookup() {
        // Register a new user (like "ty") and test lookups
        AuthService.registerUser("ty", "pass123", "student");
        
        assertEquals("student", authService.getRole("ty"));
        assertTrue(authService.authenticate("ty", "pass123"));
    }

    @Test
    public void testInstructorRoleValidation() {
        // Test helper check for instructor/admin privileges
        assertTrue(AuthService.isRegisteredInstructor("admin"));
        assertTrue(AuthService.isRegisteredInstructor("teacher"));
        assertFalse(AuthService.isRegisteredInstructor("student"));
        assertFalse(AuthService.isRegisteredInstructor("unknownUser"));
    }
}