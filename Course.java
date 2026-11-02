import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class Course implements Serializable {
    private String code;
    private String section;
    private String title;
    private String instructorName;
    private String instructorEmail;
    private String syllabusPath;
    private List<Assignment> assignments;
    private List<Quiz> quizzes;
    private List<CourseFile> courseFiles;
    private List<String> enrolledStudents;
    private List<Announcement> announcements;
    private Map<String, Double> categoryWeights;

    private static final String STORAGE_DIR = ".jame_storage" + File.separator + "syllabi";

    public Course(String code, String section, String title, String instructorName, String instructorEmail) {
        this.code = (code != null) ? code.trim().toUpperCase() : "COURSE-00";
        this.section = (section != null) ? section.trim().toUpperCase() : "R01";
        this.title = (title != null) ? title.trim() : "Untitled Course";
        this.instructorName = (instructorName != null) ? instructorName.trim() : "Staff";
        this.instructorEmail = (instructorEmail != null) ? instructorEmail.trim() : "staff@institution.edu";
        
        this.assignments = new ArrayList<>();
        this.quizzes = new ArrayList<>();
        this.courseFiles = new ArrayList<>();
        this.enrolledStudents = new ArrayList<>();
        this.announcements = new ArrayList<>();

        this.categoryWeights = new HashMap<>();
        categoryWeights.put("Homework", 0.40);
        categoryWeights.put("Quiz", 0.30);
        categoryWeights.put("Project", 0.30);
        
        this.enrolledStudents.add("student");
    }

    public double getGradePercentage(String username) {
        double totalWeightedScore = 0.0;
        double totalWeightAccounted = 0.0;

        // 1. Evaluate Assignments Category
        if (!assignments.isEmpty()) {
            double earnedPoints = 0.0;
            double possiblePoints = 0.0;

            for (Assignment a : assignments) {
                Submission sub = a.getSubmissionForStudent(username);
                boolean pastDue = isPastDue(a.getDueDate());

                if (sub.isSubmitted()) {
                    try {
                        double score = Double.parseDouble(sub.getGrade().replace("%", "").trim());
                        earnedPoints += (score / 100.0) * a.getPointsPossible();
                        possiblePoints += a.getPointsPossible();
                    } catch (Exception ignored) {}
                } else if (pastDue) {
                    // Unsubmitted past-due items score 0
                    possiblePoints += a.getPointsPossible();
                }
            }

            if (possiblePoints > 0) {
                double catAvg = (earnedPoints / possiblePoints) * 100.0;
                double weight = categoryWeights.getOrDefault("Homework", 0.40);
                totalWeightedScore += catAvg * weight;
                totalWeightAccounted += weight;
            }
        }

        // 2. Evaluate Quizzes Category
        if (!quizzes.isEmpty()) {
            double quizEarned = 0.0;
            int quizCount = 0;

            for (Quiz q : quizzes) {
                if (q.hasStudentCompleted(username)) {
                    try {
                        String raw = q.getScoreForStudent(username);
                        double score = Double.parseDouble(raw.split("%")[0].trim());
                        quizEarned += score;
                        quizCount++;
                    } catch (Exception ignored) {}
                }
            }

            if (quizCount > 0) {
                double quizAvg = quizEarned / quizCount;
                double weight = categoryWeights.getOrDefault("Quiz", 0.30);
                totalWeightedScore += quizAvg * weight;
                totalWeightAccounted += weight;
            }
        }

        if (totalWeightAccounted == 0.0) return -1.0; // Return -1 to represent N/A for empty course
        return totalWeightedScore / totalWeightAccounted;
    }

    public String getLetterGrade(String username) {
        double percentage = getGradePercentage(username);
        if (percentage < 0) return "N/A";
        if (percentage >= 90.0) return "A";
        if (percentage >= 80.0) return "B";
        if (percentage >= 70.0) return "C";
        if (percentage >= 60.0) return "D";
        return "F";
    }

    private boolean isPastDue(String dueDateStr) {
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy HH:mm");
            LocalDateTime dueDate = LocalDateTime.parse(dueDateStr, formatter);
            return LocalDateTime.now().isAfter(dueDate);
        } catch (Exception e) {
            return false;
        }
    }
    
    public void removeStudent(String username) {
        if (username != null) {
            enrolledStudents.remove(username.trim());
        }
    }
    
    public void setSyllabusPath(String originalPath) {
        if (originalPath == null || originalPath.trim().isEmpty()) return;
        try {
            File source = new File(originalPath);
            if (!source.exists()) return;

            File storageDir = new File(STORAGE_DIR);
            if (!storageDir.exists()) storageDir.mkdirs();

            File dest = new File(storageDir, code + "_Syllabus_" + source.getName());
            Files.copy(source.toPath(), dest.toPath(), StandardCopyOption.REPLACE_EXISTING);
            this.syllabusPath = dest.getAbsolutePath();
        } catch (IOException e) {
            this.syllabusPath = originalPath;
        }
    }

    public Assignment getNextAssignment(String username) {
        if (assignments == null || assignments.isEmpty()) return null;
        for (Assignment assignment : assignments) {
            Submission sub = assignment.getSubmissionForStudent(username);
            if (sub == null || !sub.isSubmitted()) return assignment;
        }
        return assignments.get(assignments.size() - 1);
    }
    
    public void setInstructor(String instructorName, String instructorEmail) {
        if (instructorName != null && !instructorName.trim().isEmpty()) {
            this.instructorName = instructorName.trim();
        }
        if (instructorEmail != null && !instructorEmail.trim().isEmpty()) {
            this.instructorEmail = instructorEmail.trim();
        }
    }

    public void setSection(String section) {
        if (section != null && !section.trim().isEmpty()) {
            this.section = section.trim().toUpperCase();
        }
    }

    public void clearRoster() {
        this.enrolledStudents.clear();
    }
    
    public String getCode() { return code; }
    public String getSection() { return section; }
    public String getTitle() { return title; }
    public String getInstructorName() { return instructorName; }
    public String getInstructorEmail() { return instructorEmail; }
    public String getSyllabusPath() { return syllabusPath; }

    // Read-only snapshots for collection safety
    public List<Assignment> getAssignments() { return assignments; }
    public List<Quiz> getQuizzes() { return quizzes; }
    public List<CourseFile> getCourseFiles() { return Collections.unmodifiableList(courseFiles); }
    public List<String> getEnrolledStudents() { return Collections.unmodifiableList(enrolledStudents); }
    public List<Announcement> getAnnouncements() { return Collections.unmodifiableList(announcements); }

    public void addStudent(String username) {
        if (username != null && !username.trim().isEmpty()) {
            String sanitized = username.trim();
            if (!enrolledStudents.contains(sanitized)) enrolledStudents.add(sanitized);
        }
    }

    public void addStudents(List<String> usernames) {
        if (usernames != null) {
            for (String u : usernames) addStudent(u);
        }
    }

    public void addQuiz(Quiz quiz) { if (quiz != null) quizzes.add(quiz); }
    public void addCourseFile(CourseFile file) { if (file != null) courseFiles.add(file); }
    public void removeCourseFile(CourseFile file) { if (file != null) courseFiles.remove(file); }
    public void addAnnouncement(Announcement announcement) { if (announcement != null) announcements.add(0, announcement); }
}