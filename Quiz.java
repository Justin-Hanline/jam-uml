import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.Duration;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class Quiz implements Serializable {
    private String title;
    private int timeLimitMinutes;
    private String dueDate; // Added due date for deadlines feed
    private List<QuizQuestion> questions;
    private Map<String, String> studentScores;
    private Map<String, Boolean> completedStudents;
    private Map<String, LocalDateTime> startTimes;
    private Map<String, List<Integer>> savedDraftAnswers;

    public Quiz(String title, int timeLimitMinutes, String dueDate) {
        this.title = (title != null) ? title.trim() : "Timed Quiz";
        this.timeLimitMinutes = (timeLimitMinutes > 0) ? timeLimitMinutes : 15;
        this.dueDate = validateAndFormatDate(dueDate);
        this.questions = new ArrayList<>();
        this.studentScores = new HashMap<>();
        this.completedStudents = new HashMap<>();
        this.startTimes = new HashMap<>();
        this.savedDraftAnswers = new HashMap<>();
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

    public void startAttempt(String username) {
        if (!startTimes.containsKey(username)) {
            startTimes.put(username, LocalDateTime.now());
        }
    }

    public long getRemainingSeconds(String username) {
        if (!startTimes.containsKey(username)) return timeLimitMinutes * 60;
        LocalDateTime start = startTimes.get(username);
        long elapsed = Duration.between(start, LocalDateTime.now()).getSeconds();
        long remaining = (timeLimitMinutes * 60) - elapsed;
        return Math.max(0, remaining);
    }

    public void saveDraftAnswer(String username, int questionIndex, int answerIndex) {
        if (!savedDraftAnswers.containsKey(username)) {
            savedDraftAnswers.put(username, new ArrayList<>(Collections.nCopies(questions.size(), -1)));
        }
        List<Integer> drafts = savedDraftAnswers.get(username);
        if (questionIndex >= 0 && questionIndex < drafts.size()) {
            drafts.set(questionIndex, answerIndex);
        }
    }

    public String gradeQuiz(String username, List<Integer> studentAnswers) {
        if (hasStudentCompleted(username)) return getScoreForStudent(username);
        if (questions.isEmpty()) return "0%";

        List<Integer> finalAnswers = (studentAnswers != null) ? studentAnswers : savedDraftAnswers.getOrDefault(username, new ArrayList<>());
        int correctCount = 0;

        for (int i = 0; i < questions.size(); i++) {
            if (i < finalAnswers.size()) {
                int choice = finalAnswers.get(i);
                if (choice >= 0 && choice < questions.get(i).getOptions().size()) {
                    if (questions.get(i).isCorrect(choice)) correctCount++;
                }
            }
        }

        double percentage = ((double) correctCount / questions.size()) * 100.0;
        String scoreResult = String.format("%.0f%% (%d/%d correct)", percentage, correctCount, questions.size());

        studentScores.put(username, scoreResult);
        completedStudents.put(username, true);
        return scoreResult;
    }

    public String getTitle() { return title; }
    public int getTimeLimitMinutes() { return timeLimitMinutes; }
    public String getDueDate() { return dueDate; }
    public List<QuizQuestion> getQuestions() { return Collections.unmodifiableList(questions); }
    public void addQuestion(QuizQuestion q) { if (q != null) questions.add(q); }
    public boolean hasStudentCompleted(String username) { return completedStudents.getOrDefault(username, false); }
    public String getScoreForStudent(String username) { return studentScores.getOrDefault(username, "Not Taken"); }
}