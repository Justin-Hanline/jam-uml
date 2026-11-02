import java.io.Serializable;
import java.util.Collections;
import java.util.List;

public class QuizQuestion implements Serializable {
    private String questionText;
    private List<String> options;
    private int correctOptionIndex;

    public QuizQuestion(String questionText, List<String> options, int correctOptionIndex) {
        if (options == null || options.size() < 2) {
            throw new IllegalArgumentException("Quiz question must contain at least 2 options.");
        }
        if (correctOptionIndex < 0 || correctOptionIndex >= options.size()) {
            throw new IllegalArgumentException("Correct option index is out of bounds.");
        }
        this.questionText = (questionText != null) ? questionText.trim() : "Question";
        this.options = options;
        this.correctOptionIndex = correctOptionIndex;
    }

    public String getQuestionText() { return questionText; }
    public List<String> getOptions() { return Collections.unmodifiableList(options); }
    public boolean isCorrect(int selectedIndex) { return selectedIndex == correctOptionIndex; }
}