import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Represents a downloadable course resource (lecture slide, assignment template, reading).
 */
public class CourseFile implements Serializable {
    private String fileName;
    private String filePath;
    private String uploader;
    private String timestamp;

    public CourseFile(String fileName, String filePath, String uploader) {
        this.fileName = (fileName != null) ? fileName.trim() : "Resource Document";
        this.filePath = (filePath != null) ? filePath : "";
        this.uploader = (uploader != null) ? uploader.trim() : "Instructor";
        this.timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("MM/dd/yyyy HH:mm"));
    }

    public String getFileName() { return fileName; }
    public String getFilePath() { return filePath; }
    public String getUploader() { return uploader; }
    public String getTimestamp() { return timestamp; }
}