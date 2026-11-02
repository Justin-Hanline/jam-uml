import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

/**
 * Reusable visual course card component for student and instructor dashboards.
 */
public class CourseTile extends VBox {

    public interface TileNavigationHandler {
        void onSyllabusClicked(Course course);
        void onAssignmentsClicked(Course course);
        void onGradesClicked(Course course);
        void onInstructorClicked(Course course);
    }

    private Course course;
    private String username;
    private TileNavigationHandler handler;

    public CourseTile(Course course, String username, TileNavigationHandler handler) {
        this.course = course;
        this.username = username;
        this.handler = handler;

        setPrefWidth(360);
        setMaxWidth(360);
        setSpacing(12);
        setPadding(new Insets(16));
        setStyle("-fx-background-color: white; -fx-background-radius: 12px; -fx-border-color: #e5e7eb; -fx-border-radius: 12px; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.06), 6, 0, 0, 2);");

        // 1. Course Header / Title linking directly to Syllabus
        VBox titleBox = new VBox(4);
        titleBox.setStyle("-fx-cursor: hand;");
        titleBox.setOnMouseClicked(e -> handler.onSyllabusClicked(course));

        Label codeLabel = new Label(course.getCode() + " — " + course.getSection());
        codeLabel.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: #b45309;");

        Label titleLabel = new Label(course.getTitle() + " 📄");
        titleLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #0f172a;");
        titleLabel.setWrapText(true);

        titleBox.getChildren().addAll(codeLabel, titleLabel);

        // 2. Two Large Square Cards Side-by-Side underneath (Next Assignment & Grade Standing)
        HBox topCardsRow = new HBox(10);
        topCardsRow.setAlignment(Pos.CENTER);

        // Square Card 1: Next Assignment Due (Orange Theme)
        VBox assignmentCard = new VBox(6);
        assignmentCard.setPrefSize(160, 95);
        assignmentCard.setAlignment(Pos.CENTER_LEFT);
        assignmentCard.setPadding(new Insets(12));
        assignmentCard.setStyle("-fx-background-color: #fef3c7; -fx-border-color: #fcd34d; -fx-background-radius: 8px; -fx-border-radius: 8px; -fx-cursor: hand;");
        
        Assignment nextAssign = course.getNextAssignment(username);
        String assignTitleText = (nextAssign != null) ? nextAssign.getName() : "No Pending Work";
        String assignDueText = (nextAssign != null) ? "Due: " + nextAssign.getDueDate() : "All caught up!";

        Label assignHeaderLbl = new Label("📝 NEXT ASSIGNMENT");
        assignHeaderLbl.setStyle("-fx-font-size: 9px; -fx-font-weight: bold; -fx-text-fill: #92400e;");
        
        Label assignNameLbl = new Label(assignTitleText);
        assignNameLbl.setStyle("-fx-font-weight: bold; -fx-font-size: 13px; -fx-text-fill: #78350f;");
        assignNameLbl.setWrapText(true);

        Label assignDueLbl = new Label(assignDueText);
        assignDueLbl.setStyle("-fx-font-size: 11px; -fx-text-fill: #b45309;");

        assignmentCard.getChildren().addAll(assignHeaderLbl, assignNameLbl, assignDueLbl);
        assignmentCard.setOnMouseClicked(e -> handler.onAssignmentsClicked(course));

        // Square Card 2: Current Grade Standing (Green Theme - Letter + Number below)
        VBox gradeCard = new VBox(6);
        gradeCard.setPrefSize(160, 95);
        gradeCard.setAlignment(Pos.CENTER_LEFT);
        gradeCard.setPadding(new Insets(12));
        gradeCard.setStyle("-fx-background-color: #f0fdf4; -fx-border-color: #86efac; -fx-background-radius: 8px; -fx-border-radius: 8px; -fx-cursor: hand;");

        double gradePct = course.getGradePercentage(username);
        String letterGrade = course.getLetterGrade(username);

        Label gradeHeaderLbl = new Label("📊 CURRENT STANDING");
        gradeHeaderLbl.setStyle("-fx-font-size: 9px; -fx-font-weight: bold; -fx-text-fill: #166534;");

        Label letterGradeLbl = new Label("Grade: " + letterGrade);
        letterGradeLbl.setStyle("-fx-font-weight: bold; -fx-font-size: 15px; -fx-text-fill: #047857;");

        Label percentGradeLbl = new Label(String.format("Percentage: %.1f%%", gradePct));
        percentGradeLbl.setStyle("-fx-font-size: 11px; -fx-text-fill: #15803d;");

        gradeCard.getChildren().addAll(gradeHeaderLbl, letterGradeLbl, percentGradeLbl);
        gradeCard.setOnMouseClicked(e -> handler.onGradesClicked(course));

        topCardsRow.getChildren().addAll(assignmentCard, gradeCard);

        // 3. Long Rectangle at the Bottom for Professor Contact Information (Blue Theme)
        Button contactRectBtn = new Button("✉️ Professor Contact: " + course.getInstructorName());
        contactRectBtn.setMaxWidth(Double.MAX_VALUE);
        contactRectBtn.setAlignment(Pos.CENTER_LEFT);
        contactRectBtn.setStyle("-fx-background-color: #e0f2fe; -fx-text-fill: #0369a1; -fx-font-weight: bold; -fx-font-size: 12px; -fx-padding: 12px 16px; -fx-background-radius: 8px; -fx-cursor: hand;");
        contactRectBtn.setOnAction(e -> handler.onInstructorClicked(course));

        getChildren().addAll(titleBox, topCardsRow, contactRectBtn);
    }
}