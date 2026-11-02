import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Represents a broadcast announcement or news bulletin published by an instructor for a course.
 */
public class Announcement {
    private String title;
    private String content;
    private String author;
    private String timestamp;

    public Announcement(String title, String content, String author) {
        this.title = (title != null) ? title.trim() : "Course Update";
        this.content = (content != null) ? content.trim() : "";
        this.author = (author != null) ? author.trim() : "Instructor";
        this.timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("MM/dd/yyyy HH:mm"));
    }

    public String getTitle() { return title; }
    public String getContent() { return content; }
    public String getAuthor() { return author; }
    public String getTimestamp() { return timestamp; }
}