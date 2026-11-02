import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

public class Assignment implements Serializable {
    private String name;
    private String dueDate; // Formatted "MM/dd/yyyy HH:mm"
    private String description;
    private double pointsPossible;
    private Map<String, Submission> submissions;

    private static final String STORAGE_DIR = ".jame_storage" + File.separator + "submissions";

    public Assignment(String name, String dueDate, String description, double pointsPossible) {
        this.name = (name != null) ? name.trim() : "Untitled Assignment";
        this.dueDate = validateAndFormatDate(dueDate);
        this.description = (description != null) ? description.trim() : "";
        this.pointsPossible = (pointsPossible > 0) ? pointsPossible : 100.0;
        this.submissions = new HashMap<>();
    }

    public Assignment(String name, String dueDate, String description) {
        this(name, dueDate, description, 100.0);
    }

    private String validateAndFormatDate(String input) {
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy HH:mm");
            LocalDateTime dt = LocalDateTime.parse(input, formatter);
            return dt.format(formatter);
        } catch (Exception e) {
            return LocalDateTime.now().plusDays(7).format(DateTimeFormatter.ofPattern("MM/dd/yyyy 23:59"));
        }
    }

    public Submission getSubmissionForStudent(String username) {
        if (!submissions.containsKey(username)) {
            submissions.put(username, new Submission());
        }
        return submissions.get(username);
    }

    public void submitWork(String username, String originalFilePath) {
        Submission sub = getSubmissionForStudent(username);
        
        if (originalFilePath != null && !originalFilePath.isEmpty()) {
            File source = new File(originalFilePath);
            if (source.exists()) {
                try {
                    File storageDir = new File(STORAGE_DIR);
                    if (!storageDir.exists()) storageDir.mkdirs();

                    String managedName = username + "_" + name.replaceAll("\\s+", "_") + "_" + source.getName();
                    File dest = new File(storageDir, managedName);
                    Files.copy(source.toPath(), dest.toPath(), StandardCopyOption.REPLACE_EXISTING);

                    boolean isLate = false;
                    try {
                        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy HH:mm");
                        LocalDateTime due = LocalDateTime.parse(dueDate, formatter);
                        isLate = LocalDateTime.now().isAfter(due);
                    } catch (Exception ignored) {}

                    sub.recordSubmission(dest.getAbsolutePath(), isLate);
                } catch (IOException e) {
                    sub.recordSubmission(originalFilePath, false);
                }
            }
        }
    }

    public String getName() { return name; }
    public String getDueDate() { return dueDate; }
    public String getDescription() { return description; }
    public double getPointsPossible() { return pointsPossible; }
}