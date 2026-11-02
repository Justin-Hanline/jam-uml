import java.io.File;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

/**
 * Manages user accounts, credential authentication, and role-based access control.
 */
public class AuthService implements Serializable {
    private static Map<String, UserCredentials> userDatabase = new HashMap<>();

    static {
        // Seed initial default accounts
        registerUser("student", "password123", "student");
        registerUser("teacher", "teacher123", "instructor");
        registerUser("admin", "admin2026", "admin");
    }

    public static class UserCredentials implements Serializable {
        private String password;
        private String role; // "student", "instructor", "admin"

        public UserCredentials(String password, String role) {
            this.password = password;
            this.role = role;
        }

        public String getPassword() { return password; }
        public String getRole() { return role; }
    }

    public static void registerUser(String username, String password, String role) {
        if (username != null && !username.trim().isEmpty() && password != null && role != null) {
            userDatabase.put(username.trim(), new UserCredentials(password, role.trim().toLowerCase()));
        }
    }

    public boolean authenticate(String username, String password) {
        if (username == null || password == null) return false;
        String key = username.trim();
        if (userDatabase.containsKey(key)) {
            return userDatabase.get(key).getPassword().equals(password);
        }
        return false;
    }

    public String getRole(String username) {
        if (username == null || username.trim().isEmpty()) return null;
        String key = username.trim();
        
        // Look up the user directly from the shared userDatabase map (case-insensitive search)
        for (Map.Entry<String, UserCredentials> entry : userDatabase.entrySet()) {
            if (entry.getKey().equalsIgnoreCase(key)) {
                return entry.getValue().getRole();
            }
        }
        
        return null; // Returns null if the user isn't found in the database
    }

    public static boolean isRegisteredInstructor(String username) {
        if (username == null || username.trim().isEmpty()) {
            return false;
        }
        String role = new AuthService().getRole(username.trim());
        return "instructor".equalsIgnoreCase(role) || "admin".equalsIgnoreCase(role);
    }
    
    public boolean updatePassword(String username, String newPassword) {
        if (username == null || newPassword == null || newPassword.trim().isEmpty()) {
            return false;
        }
        String key = username.trim();
        if (userDatabase.containsKey(key)) {
            UserCredentials creds = userDatabase.get(key);
            // Update credentials while preserving the user's role
            userDatabase.put(key, new UserCredentials(newPassword, creds.getRole()));
            return true;
        }
        return false;
    }
    
    public static int batchImportUsers(File csvFile) {
        if (csvFile == null || !csvFile.exists()) return 0;
        int count = 0;
        try (Scanner scanner = new Scanner(csvFile)) {
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine().trim();
                if (!line.isEmpty() && !line.startsWith("#")) {
                    String[] parts = line.split(",");
                    if (parts.length >= 3) {
                        registerUser(parts[0].trim(), parts[1].trim(), parts[2].trim());
                        count++;
                    }
                }
            }
        } catch (Exception ignored) {}
        return count;
    }
}