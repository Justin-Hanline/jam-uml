import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Submission implements Serializable {
    private String submittedFilePath;
    private String submissionTimestamp;
    private boolean submitted;
    private boolean isLate;
    private String grade;
    private String feedback;
    private List<String> submissionHistory;

    public Submission() {
        this.submitted = false;
        this.isLate = false;
        this.grade = "Pending";
        this.feedback = "No feedback provided yet.";
        this.submissionHistory = new ArrayList<>();
    }

    public void recordSubmission(String filePath, boolean late) {
        this.submittedFilePath = filePath;
        this.submissionTimestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("MM/dd/yyyy HH:mm:ss"));
        this.submitted = true;
        this.isLate = late;
        
        String record = "[" + submissionTimestamp + "] File: " + filePath + (late ? " (LATE)" : "");
        this.submissionHistory.add(record);
    }

    public String getSubmittedFilePath() { return submittedFilePath; }
    public String getSubmissionTimestamp() { return submissionTimestamp; }
    public boolean isSubmitted() { return submitted; }
    public boolean isLate() { return isLate; }
    public String getGrade() { return grade; }
    public void setGrade(String grade) { this.grade = grade; }
    public String getFeedback() { return feedback; }
    public void setFeedback(String feedback) { this.feedback = feedback; }
    public List<String> getSubmissionHistory() { return Collections.unmodifiableList(submissionHistory); }
}