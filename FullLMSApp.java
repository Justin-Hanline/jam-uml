import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Main application controller for the JAME Enterprise Learning Management System.
 */
public class FullLMSApp extends Application implements CourseTile.TileNavigationHandler {

    private Stage primaryStage;
    private static List<Course> courseList = new ArrayList<>();
    private FlowPane studentTileGrid;
    private VBox teacherCourseContainer;
    private VBox adminCourseContainer;
    private String loggedInUser = "";
    private String loggedInRole = "";

    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;
        primaryStage.setTitle("JAME Enterprise Learning Management System");

        seedInitialData();
        showLoginView();
        primaryStage.show();
    }

    private void seedInitialData() {
        if (!courseList.isEmpty()) return;

        Course cs3141 = new Course("CS3141", "R01", "Software Engineering", "Dr. Smith", "smith@univ.edu");
        cs3141.getAssignments().add(new Assignment("Project Proposal", "10/30/2026 23:59", "Submit project scope document."));
        cs3141.getAssignments().add(new Assignment("Sprint 1 Project", "11/05/2026 17:00", "Build JavaFX front end."));
        
        Quiz sampleQuiz = new Quiz("Midterm Exam 1", 30, "11/15/2026 23:59");
        List<String> opts1 = new ArrayList<>();
        opts1.add("A) Waterfall"); opts1.add("B) Agile / Scrum"); opts1.add("C) Monolith");
        sampleQuiz.addQuestion(new QuizQuestion("Which methodology emphasizes iterative sprints?", opts1, 1));
        
        List<String> opts2 = new ArrayList<>();
        opts2.add("A) Git"); opts2.add("B) JavaFX"); opts2.add("C) JUnit");
        sampleQuiz.addQuestion(new QuizQuestion("What tool is used for distributed version control?", opts2, 0));
        
        cs3141.addQuiz(sampleQuiz);
        cs3141.addStudent("student");

        Course cs3411 = new Course("CS3411", "R01", "Systems Programming", "Prof. Johnson", "johnson@univ.edu");
        cs3411.getAssignments().add(new Assignment("Program 3: Process Utilities", "11/10/2026 23:59", "Implement fork/exec C functions."));
        cs3411.addStudent("student");

        courseList.add(cs3141);
        courseList.add(cs3411);
        
        AuditLog.log("System", "Initialized LMS with seed courses.");
    }

    // ==========================================
    // 1. LOGIN VIEW
    // ==========================================
    private void showLoginView() {
        AuthService authService = new AuthService();

        VBox root = new VBox(20);
        root.setAlignment(Pos.CENTER);
        root.setStyle("-fx-background-color: #fffbf5; -fx-padding: 40px;");

        StackPane logoPane = new StackPane();
        logoPane.setStyle("-fx-background-color: #f59e0b; -fx-padding: 20px 40px; -fx-background-radius: 8px;");
        Label logoText = new Label("JAME LMS");
        logoText.setStyle("-fx-font-size: 26px; -fx-font-weight: bold; -fx-text-fill: white;");
        logoPane.getChildren().add(logoText);

        VBox formCard = new VBox(15);
        formCard.setMaxWidth(380);
        formCard.setStyle("-fx-background-color: white; -fx-padding: 30px; -fx-background-radius: 12px; -fx-border-color: #e5e7eb; -fx-border-radius: 12px; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.08), 8, 0, 0, 3);");

        Label prompt = new Label("Secure Portal Login\n(student/password123 | teacher/teacher123 | admin/admin2026)");
        prompt.setWrapText(true);
        prompt.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: #374151;");

        TextField userField = new TextField();
        userField.setPromptText("Username");
        userField.setStyle("-fx-padding: 10px; -fx-background-radius: 6px;");

        PasswordField passField = new PasswordField();
        passField.setPromptText("Password");
        passField.setStyle("-fx-padding: 10px; -fx-background-radius: 6px;");

        Button loginBtn = new Button("Sign In");
        loginBtn.setStyle("-fx-background-color: #f59e0b; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 14px; -fx-padding: 10px; -fx-max-width: Infinity; -fx-background-radius: 6px; -fx-cursor: hand;");
        
        loginBtn.setOnAction(e -> {
            String enteredUser = userField.getText();
            String enteredPass = passField.getText();

            if (authService.authenticate(enteredUser, enteredPass)) {
                loggedInUser = enteredUser.trim();
                loggedInRole = authService.getRole(loggedInUser);
                
                AuditLog.log(loggedInUser, "Successfully authenticated as role: " + loggedInRole);

                if (loggedInRole.equals("admin")) {
                    showAdminDashboardView();
                } else if (loggedInRole.equals("instructor")) {
                    showTeacherDashboardView();
                } else {
                    showStudentDashboardView();
                }
            } else {
                AuditLog.log(enteredUser.isEmpty() ? "Unknown" : enteredUser, "Failed authentication attempt.");
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Authentication Failed");
                alert.setHeaderText("Invalid Credentials");
                alert.setContentText("Please check your username and password.");
                alert.showAndWait();
            }
        });

        formCard.getChildren().addAll(prompt, userField, passField, loginBtn);
        root.getChildren().addAll(logoPane, formCard);

        primaryStage.setScene(new Scene(root, 1000, 650));
    }

    // ==========================================
    // 2. STUDENT DASHBOARD
    // ==========================================
    private void showStudentDashboardView() {
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: #ffffff;");

        root.setTop(createTopNav());
        root.setLeft(createSidebar("Courses"));

        VBox content = new VBox(20);
        content.setPadding(new Insets(25, 30, 30, 30));
        content.setStyle("-fx-background-color: #ffffff;");

        Label portalLabel = new Label("FALL 2026 / STUDENT PORTAL");
        portalLabel.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: #b45309;");

        HBox sectionHeader = new HBox(15);
        sectionHeader.setAlignment(Pos.CENTER_LEFT);
        Label myCoursesLabel = new Label("■ My Courses");
        myCoursesLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #111827;");
        
        Region headerSpacer = new Region();
        HBox.setHgrow(headerSpacer, Priority.ALWAYS);
        
        TextField searchField = new TextField();
        searchField.setPromptText("🔍 Search courses");
        searchField.setStyle("-fx-background-radius: 6px; -fx-padding: 8px 12px; -fx-border-color: #d1d5db; -fx-border-radius: 6px;");
        searchField.textProperty().addListener((observable, oldValue, newValue) -> refreshStudentTiles(newValue));

        sectionHeader.getChildren().addAll(myCoursesLabel, headerSpacer, searchField);

        studentTileGrid = new FlowPane();
        studentTileGrid.setHgap(20);
        studentTileGrid.setVgap(20);
        refreshStudentTiles("");

        ScrollPane scrollPane = new ScrollPane(studentTileGrid);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: transparent; -fx-background: transparent;");

        content.getChildren().addAll(portalLabel, sectionHeader, scrollPane);
        root.setCenter(content);

        primaryStage.setScene(new Scene(root, 1150, 750));
    }

    private void refreshStudentTiles(String query) {
        studentTileGrid.getChildren().clear();
        String lowerQuery = query == null ? "" : query.toLowerCase().trim();

        for (Course c : courseList) {
            if (c.getEnrolledStudents().contains(loggedInUser)) {
                if (lowerQuery.isEmpty() || 
                    c.getCode().toLowerCase().contains(lowerQuery) || 
                    c.getTitle().toLowerCase().contains(lowerQuery) || 
                    c.getInstructorName().toLowerCase().contains(lowerQuery)) {
                    studentTileGrid.getChildren().add(new CourseTile(c, loggedInUser, this));
                }
            }
        }
    }

    // ==========================================
    // 3. INSTRUCTOR DASHBOARD
    // ==========================================
    private void showTeacherDashboardView() {
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: #ffffff;");

        root.setTop(createTopNav());
        root.setLeft(createSidebar("Courses"));

        VBox content = new VBox(20);
        content.setPadding(new Insets(25, 30, 30, 30));
        content.setStyle("-fx-background-color: #ffffff;");

        Label portalLabel = new Label("FALL 2026 / FACULTY PORTAL");
        portalLabel.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: #b45309;");

        HBox controlBar = new HBox(15);
        controlBar.setAlignment(Pos.CENTER_LEFT);
        Label titleLabel = new Label("Assigned Classes & Grading");
        titleLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #111827;");
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        TextField searchField = new TextField();
        searchField.setPromptText("🔍 Search classes");
        searchField.setStyle("-fx-background-radius: 6px; -fx-padding: 8px 12px; -fx-border-color: #d1d5db; -fx-border-radius: 6px;");
        searchField.textProperty().addListener((observable, oldValue, newValue) -> refreshTeacherCourseList(newValue));

        Button addAssignmentBtn = new Button("+ Publish Assignment");
        addAssignmentBtn.setStyle("-fx-background-color: #10b981; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8px 16px; -fx-background-radius: 6px; -fx-cursor: hand;");
        addAssignmentBtn.setOnAction(e -> openAddAssignmentDialog());

        Button addQuizBtn = new Button("+ Publish Quiz");
        addQuizBtn.setStyle("-fx-background-color: #0284c7; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8px 16px; -fx-background-radius: 6px; -fx-cursor: hand;");
        addQuizBtn.setOnAction(e -> openAddQuizDialog());

        // Audit log button removed for teachers per requirement

        controlBar.getChildren().addAll(titleLabel, spacer, searchField, addAssignmentBtn, addQuizBtn);

        teacherCourseContainer = new VBox(12);
        refreshTeacherCourseList("");

        ScrollPane scrollPane = new ScrollPane(teacherCourseContainer);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: transparent; -fx-background: transparent;");

        content.getChildren().addAll(portalLabel, controlBar, new Separator(), scrollPane);
        root.setCenter(content);

        primaryStage.setScene(new Scene(root, 1150, 750));
    }

    private void refreshTeacherCourseList(String query) {
        teacherCourseContainer.getChildren().clear();
        String lowerQuery = query == null ? "" : query.toLowerCase().trim();

        boolean hasMatches = false;
        for (Course c : courseList) {
            // Instructors ONLY see courses they instruct
            if (c.getInstructorName().equalsIgnoreCase(loggedInUser)) {
                if (lowerQuery.isEmpty() || c.getCode().toLowerCase().contains(lowerQuery) || c.getTitle().toLowerCase().contains(lowerQuery)) {
                    hasMatches = true;
                    HBox row = new HBox(15);
                    row.setAlignment(Pos.CENTER_LEFT);
                    row.setStyle("-fx-background-color: #f8fafc; -fx-border-color: #cbd5e1; -fx-border-radius: 8px; -fx-background-radius: 8px; -fx-padding: 15px 20px;");

                    VBox info = new VBox(4);
                    Label codeTitle = new Label(c.getCode() + " - " + c.getTitle() + " (" + c.getSection() + ")");
                    codeTitle.setStyle("-fx-font-weight: bold; -fx-font-size: 16px; -fx-text-fill: #0f172a;");
                    
                    Label details = new Label("Enrolled Students: " + c.getEnrolledStudents().size() + " | Assignments: " + c.getAssignments().size() + " | Quizzes: " + c.getQuizzes().size());
                    details.setStyle("-fx-text-fill: #64748b; -fx-font-size: 13px;");
                    details.setStyle("-fx-text-fill: #64748b; -fx-font-size: 13px;");
                    info.getChildren().addAll(codeTitle, details);

                    Region sp = new Region();
                    HBox.setHgrow(sp, Priority.ALWAYS);

                    Button gradeWorkBtn = new Button("Grade Work");
                    gradeWorkBtn.setStyle("-fx-background-color: #6366f1; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand;");
                    gradeWorkBtn.setOnAction(e -> openGradingDialog(c));

                    Button rosterBtn = new Button("👥 Roster");
                    rosterBtn.setStyle("-fx-background-color: #0284c7; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand;");
                    rosterBtn.setOnAction(e -> openRosterManagementDialog(c));

                    Button atRiskBtn = new Button("📊 At-Risk Hub");
                    atRiskBtn.setStyle("-fx-background-color: #ef4444; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand;");
                    atRiskBtn.setOnAction(e -> openAtRiskAnalyticsModal(c));

                    Button announcementsBtn = new Button("📢 Announcements (" + c.getAnnouncements().size() + ")");
                    announcementsBtn.setStyle("-fx-background-color: #f59e0b; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand;");
                    announcementsBtn.setOnAction(e -> showAnnouncementsView(c));

                    row.getChildren().addAll(info, sp, gradeWorkBtn, rosterBtn, atRiskBtn, announcementsBtn);
                    teacherCourseContainer.getChildren().add(row);
                }
            }
        }

        if (!hasMatches) {
            teacherCourseContainer.getChildren().add(new Label("No assigned courses match your search."));
        }
    }

    // ==========================================
    // 4. ADMIN DASHBOARD
    // ==========================================
    private void showAdminDashboardView() {
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: #ffffff;");

        root.setTop(createTopNav());
        root.setLeft(createSidebar("Courses"));

        VBox content = new VBox(20);
        content.setPadding(new Insets(25, 30, 30, 30));
        content.setStyle("-fx-background-color: #ffffff;");

        Label portalLabel = new Label("FALL 2026 / SYSTEM ADMINISTRATION PORTAL");
        portalLabel.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: #b45309;");

        HBox controlBar = new HBox(15);
        controlBar.setAlignment(Pos.CENTER_LEFT);
        Label titleLabel = new Label("System Course & User Oversight");
        titleLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #111827;");
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        TextField searchField = new TextField();
        searchField.setPromptText("🔍 Search courses");
        searchField.setStyle("-fx-background-radius: 6px; -fx-padding: 8px 12px; -fx-border-color: #d1d5db; -fx-border-radius: 6px;");
        searchField.textProperty().addListener((observable, oldValue, newValue) -> refreshAdminCourseList(newValue));

        Button createCourseBtn = new Button("+ Create Course");
        createCourseBtn.setStyle("-fx-background-color: #f59e0b; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8px 16px; -fx-background-radius: 6px; -fx-cursor: hand;");
        createCourseBtn.setOnAction(e -> openAddCourseDialog());

        Button createUserBtn = new Button("+ Register User");
        createUserBtn.setStyle("-fx-background-color: #0ea5e9; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8px 16px; -fx-background-radius: 6px; -fx-cursor: hand;");
        createUserBtn.setOnAction(e -> openRegisterUserDialog());

        Button auditLogBtn = new Button("📋 Audit Log");
        auditLogBtn.setStyle("-fx-background-color: #475569; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8px 16px; -fx-background-radius: 6px; -fx-cursor: hand;");
        auditLogBtn.setOnAction(e -> openAuditLogViewer());

        controlBar.getChildren().addAll(titleLabel, spacer, searchField, createCourseBtn, createUserBtn, auditLogBtn);

        adminCourseContainer = new VBox(12);
        refreshAdminCourseList("");

        ScrollPane scrollPane = new ScrollPane(adminCourseContainer);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: transparent; -fx-background: transparent;");

        content.getChildren().addAll(portalLabel, controlBar, new Separator(), scrollPane);
        root.setCenter(content);

        primaryStage.setScene(new Scene(root, 1150, 750));
    }

    private void refreshAdminCourseList(String query) {
        adminCourseContainer.getChildren().clear();
        String lowerQuery = query == null ? "" : query.toLowerCase().trim();

        boolean hasMatches = false;
        for (Course c : courseList) {
            if (lowerQuery.isEmpty() || 
                c.getCode().toLowerCase().contains(lowerQuery) || 
                c.getTitle().toLowerCase().contains(lowerQuery) || 
                c.getInstructorName().toLowerCase().contains(lowerQuery)) {
                
                hasMatches = true;
                HBox row = new HBox(15);
                row.setAlignment(Pos.CENTER_LEFT);
                row.setStyle("-fx-background-color: #f8fafc; -fx-border-color: #cbd5e1; -fx-border-radius: 8px; -fx-background-radius: 8px; -fx-padding: 15px 20px;");

                VBox info = new VBox(4);
                Label codeTitle = new Label(c.getCode() + " - " + c.getTitle() + " (" + c.getSection() + ")");
                codeTitle.setStyle("-fx-font-weight: bold; -fx-font-size: 16px; -fx-text-fill: #0f172a;");
                
                Label details = new Label("Instructor: " + c.getInstructorName() + " | Enrolled Students: " + c.getEnrolledStudents().size() + " | Assignments: " + c.getAssignments().size());
                details.setStyle("-fx-text-fill: #64748b; -fx-font-size: 13px;");
                info.getChildren().addAll(codeTitle, details);

                Region sp = new Region();
                HBox.setHgrow(sp, Priority.ALWAYS);

                // --- ADDED: Roster button so admins can open the roster modal ---
                Button rosterBtn = new Button("👥 Roster");
                rosterBtn.setStyle("-fx-background-color: #0284c7; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand;");
                rosterBtn.setOnAction(e -> openRosterManagementDialog(c));

                Button editCourseBtn = new Button("⚙️ Edit / Roll Over");
                editCourseBtn.setStyle("-fx-background-color: #f59e0b; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand;");
                editCourseBtn.setOnAction(e -> openEditCourseDialog(c));

                Button dropCourseBtn = new Button("Drop Course");
                dropCourseBtn.setStyle("-fx-background-color: #ef4444; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand;");
                dropCourseBtn.setOnAction(e -> {
                    Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
                    confirm.setTitle("Confirm Course Removal");
                    confirm.setHeaderText("Drop Course: " + c.getCode());
                    confirm.setContentText("Are you sure you want to delete this course system-wide?");
                    confirm.showAndWait().ifPresent(response -> {
                        if (response == ButtonType.OK) {
                            courseList.remove(c);
                            AuditLog.log(loggedInUser, "Dropped course: " + c.getCode());
                            refreshAdminCourseList(lowerQuery);
                        }
                    });
                });

                // Include rosterBtn in the row
                row.getChildren().addAll(info, sp, rosterBtn, editCourseBtn, dropCourseBtn);
                adminCourseContainer.getChildren().add(row);
            }
        }

        if (!hasMatches) {
            adminCourseContainer.getChildren().add(new Label("No system courses match your search query."));
        }
    }

    // ==========================================
    // MASTER DEADLINES FEED & HELPER
    // ==========================================
    private class DeadlineItem {
        String courseCode, title, desc, type, dueDate;
        java.util.Date parsedDate;

        public DeadlineItem(String courseCode, String title, String desc, String type, String dueDate) {
            this.courseCode = courseCode;
            this.title = title;
            this.desc = desc;
            this.type = type;
            this.dueDate = dueDate;
            try {
                java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("MM/dd/yyyy HH:mm");
                this.parsedDate = sdf.parse(dueDate);
            } catch (Exception e) {
                this.parsedDate = new java.util.Date(Long.MAX_VALUE); // Fallback for sorting if format varies
            }
        }
    }

    private void showDeadlinesFeedView() {
        if (loggedInRole.equals("admin")) {
            showAdminDashboardView();
            return;
        }

        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: #ffffff;");

        root.setTop(createTopNav());
        root.setLeft(createSidebar("Deadlines"));

        VBox content = new VBox(20);
        content.setPadding(new Insets(25, 30, 30, 30));
        content.setStyle("-fx-background-color: #ffffff;");

        Label portalLabel = new Label("ENTERPRISE SCHEDULE / MASTER DEADLINES FEED");
        portalLabel.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: #b45309;");

        Label headerTitle = new Label("📅 Chronological Deadlines & Quizzes");
        headerTitle.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: #111827;");

        List<DeadlineItem> deadlinesList = new ArrayList<>();

        for (Course c : courseList) {
            boolean isRelevant = (loggedInRole.equals("instructor") && c.getInstructorName().equalsIgnoreCase(loggedInUser)) || 
                                 (loggedInRole.equals("student") && c.getEnrolledStudents().contains(loggedInUser));

            if (isRelevant) {
                // Collect Assignments
                for (Assignment a : c.getAssignments()) {
                    Submission sub = a.getSubmissionForStudent(loggedInUser);
                    if (!(sub.isSubmitted() && loggedInRole.equals("student"))) {
                        deadlinesList.add(new DeadlineItem(c.getCode(), a.getName(), a.getDescription(), "Assignment", a.getDueDate()));
                    }
                }

                // Collect Quizzes & Exams
                for (Quiz q : c.getQuizzes()) {
                    boolean completed = q.hasStudentCompleted(loggedInUser);
                    if (!(completed && loggedInRole.equals("student"))) {
                        deadlinesList.add(new DeadlineItem(c.getCode(), q.getTitle(), "Timed Quiz (" + q.getTimeLimitMinutes() + " mins)", "Quiz / Exam", q.getDueDate()));
                    }
                }
            }
        }

        // Sort chronologically by due date
        deadlinesList.sort((d1, d2) -> d1.parsedDate.compareTo(d2.parsedDate));

        VBox feedContainer = new VBox(12);
        if (deadlinesList.isEmpty()) {
            feedContainer.getChildren().add(new Label("🎉 All caught up! No pending homework or quiz deadlines found."));
        } else {
            for (DeadlineItem item : deadlinesList) {
                HBox row = createDeadlineRow(item.courseCode, item.title, item.desc, item.type, item.dueDate);
                feedContainer.getChildren().add(row);
            }
        }

        ScrollPane scrollPane = new ScrollPane(feedContainer);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: transparent; -fx-background: transparent;");

        content.getChildren().addAll(portalLabel, headerTitle, new Separator(), scrollPane);
        root.setCenter(content);

        primaryStage.setScene(new Scene(root, 1150, 750));
    }

    private HBox createDeadlineRow(String courseCode, String title, String desc, String type, String dueDate) {
        HBox row = new HBox(15);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setStyle("-fx-background-color: #fffbeb; -fx-border-color: #fcd34d; -fx-border-radius: 8px; -fx-background-radius: 8px; -fx-padding: 15px 20px;");

        VBox info = new VBox(4);
        Label courseAndAssign = new Label("[" + courseCode + "] " + title);
        courseAndAssign.setStyle("-fx-font-weight: bold; -fx-font-size: 15px; -fx-text-fill: #0f172a;");
        
        Label descLbl = new Label(desc);
        descLbl.setStyle("-fx-text-fill: #4b5563; -fx-font-size: 12px;");
        
        Label statusBadge = new Label("⏳ Pending " + type);
        statusBadge.setStyle("-fx-text-fill: #b45309; -fx-font-weight: bold; -fx-font-size: 11px;");

        info.getChildren().addAll(courseAndAssign, descLbl, statusBadge);

        Region sp = new Region();
        HBox.setHgrow(sp, Priority.ALWAYS);

        VBox dueDateBox = new VBox(2);
        dueDateBox.setAlignment(Pos.CENTER_RIGHT);
        Label dueHeader = new Label("DUE DATE / TIME");
        dueHeader.setStyle("-fx-font-size: 9px; -fx-font-weight: bold; -fx-text-fill: #6b7280;");
        Label dueDateLbl = new Label(dueDate);
        dueDateLbl.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #1f2937;");
        dueDateBox.getChildren().addAll(dueHeader, dueDateLbl);

        row.getChildren().addAll(info, sp, dueDateBox);
        return row;
    }

    // ==========================================
    // ANNOUNCEMENTS VIEW
    // ==========================================
    private void showAnnouncementsView(Course course) {
        VBox root = createSubPageLayout("Course Announcements - " + course.getTitle());
        VBox feed = new VBox(12);

        if (course.getAnnouncements().isEmpty()) {
            feed.getChildren().add(new Label("No announcements posted for this course yet."));
        } else {
            for (Announcement ann : course.getAnnouncements()) {
                VBox card = new VBox(6);
                card.setStyle("-fx-background-color: #f8fafc; -fx-padding: 15px; -fx-border-color: #cbd5e1; -fx-border-radius: 8px; -fx-background-radius: 8px;");
                
                Label title = new Label("📢 " + ann.getTitle());
                title.setStyle("-fx-font-weight: bold; -fx-font-size: 15px; -fx-text-fill: #0f172a;");
                
                Label content = new Label(ann.getContent());
                content.setStyle("-fx-text-fill: #334155; -fx-font-size: 13px;");
                content.setWrapText(true);
                
                Label meta = new Label("Posted by " + ann.getAuthor() + " on " + ann.getTimestamp());
                meta.setStyle("-fx-text-fill: #64748b; -fx-font-size: 10px; -fx-font-style: italic;");
                
                card.getChildren().addAll(title, content, meta);
                feed.getChildren().add(card);
            }
        }

        if (loggedInRole.equals("instructor")) {
            Button postBtn = new Button("+ Post New Announcement");
            postBtn.setStyle("-fx-background-color: #f59e0b; -fx-text-fill: white; -fx-font-weight: bold;");
            postBtn.setOnAction(e -> {
                Dialog<ButtonType> dialog = new Dialog<>();
                dialog.setTitle("Post Announcement");
                dialog.setHeaderText("Broadcast Bulletin to " + course.getCode());
                dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

                GridPane grid = new GridPane();
                grid.setHgap(10); grid.setVgap(10); grid.setPadding(new Insets(20));

                TextField titleField = new TextField(); titleField.setPromptText("Headline");
                TextArea contentArea = new TextArea(); contentArea.setPromptText("Announcement details...");
                contentArea.setPrefRowCount(4);

                grid.add(new Label("Title:"), 0, 0); grid.add(titleField, 1, 0);
                grid.add(new Label("Message:"), 0, 1); grid.add(contentArea, 1, 1);

                dialog.getDialogPane().setContent(grid);
                Optional<ButtonType> res = dialog.showAndWait();

                if (res.isPresent() && res.get() == ButtonType.OK && !titleField.getText().isEmpty() && !contentArea.getText().isEmpty()) {
                    Announcement newAnn = new Announcement(titleField.getText(), contentArea.getText(), course.getInstructorName());
                    course.addAnnouncement(newAnn);
                    AuditLog.log(loggedInUser, "Posted announcement in " + course.getCode() + ": " + titleField.getText());
                    showAnnouncementsView(course);
                }
            });
            root.getChildren().add(postBtn);
        }

        ScrollPane sp = new ScrollPane(feed);
        sp.setFitToWidth(true);
        root.getChildren().add(sp);

        primaryStage.setScene(new Scene(root, 1000, 700));
    }

    // ==========================================
    // COURSE FILES & RESOURCES REPOSITORY
    // ==========================================
    private void showCourseFilesView(Course course) {
        VBox root = createSubPageLayout("Course Files & Resources - " + course.getCode());
        VBox fileListContainer = new VBox(12);

        Label descLbl = new Label("Access lecture notes, project starter templates, and syllabus documents provided by " + course.getInstructorName() + ".");
        descLbl.setStyle("-fx-text-fill: #64748b; -fx-font-size: 13px;");
        fileListContainer.getChildren().add(descLbl);

        VBox filesBox = new VBox(10);

        if (course.getCourseFiles().isEmpty()) {
            filesBox.getChildren().add(new Label("No files uploaded for this course yet."));
        } else {
            for (CourseFile file : course.getCourseFiles()) {
                HBox fileRow = new HBox(15);
                fileRow.setAlignment(Pos.CENTER_LEFT);
                fileRow.setStyle("-fx-background-color: #f8fafc; -fx-border-color: #cbd5e1; -fx-padding: 14px; -fx-background-radius: 8px; -fx-border-radius: 8px;");

                VBox info = new VBox(3);
                Label nameLbl = new Label("📄 " + file.getFileName());
                nameLbl.setStyle("-fx-font-weight: bold; -fx-font-size: 15px; -fx-text-fill: #0f172a;");
                Label metaLbl = new Label("Uploaded by " + file.getUploader() + " on " + file.getTimestamp());
                metaLbl.setStyle("-fx-text-fill: #64748b; -fx-font-size: 11px;");
                info.getChildren().addAll(nameLbl, metaLbl);

                Region sp = new Region();
                HBox.setHgrow(sp, Priority.ALWAYS);

                Button openFileBtn = new Button("Download / Open");
                openFileBtn.setStyle("-fx-background-color: #0284c7; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand;");
                openFileBtn.setDisable(file.getFilePath() == null || file.getFilePath().isEmpty());
                
                openFileBtn.setOnAction(e -> {
                    try {
                        File f = new File(file.getFilePath());
                        if (f.exists() && java.awt.Desktop.isDesktopSupported()) {
                            java.awt.Desktop.getDesktop().open(f);
                        } else {
                            Alert err = new Alert(Alert.AlertType.ERROR, "File path not found on disk: " + file.getFilePath());
                            err.showAndWait();
                        }
                    } catch (Exception ex) {
                        Alert err = new Alert(Alert.AlertType.ERROR, "Could not open file: " + ex.getMessage());
                        err.showAndWait();
                    }
                });

                fileRow.getChildren().addAll(info, sp, openFileBtn);

                if (loggedInRole.equals("instructor")) {
                    Button deleteBtn = new Button("Delete");
                    deleteBtn.setStyle("-fx-background-color: #ef4444; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand;");
                    deleteBtn.setOnAction(e -> {
                        course.removeCourseFile(file);
                        AuditLog.log(loggedInUser, "Deleted course file " + file.getFileName() + " from " + course.getCode());
                        showCourseFilesView(course);
                    });
                    fileRow.getChildren().add(deleteBtn);
                }

                filesBox.getChildren().add(fileRow);
            }
        }

        fileListContainer.getChildren().add(filesBox);

        if (loggedInRole.equals("instructor")) {
            Button uploadBtn = new Button("📤 Upload New Course File");
            uploadBtn.setStyle("-fx-background-color: #10b981; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 10px 16px; -fx-cursor: hand;");
            
            uploadBtn.setOnAction(e -> {
                FileChooser chooser = new FileChooser();
                chooser.setTitle("Select Course Resource (PDF, ZIP, Doc)");
                File selectedFile = chooser.showOpenDialog(primaryStage);
                
                if (selectedFile != null) {
                    CourseFile newCf = new CourseFile(selectedFile.getName(), selectedFile.getAbsolutePath(), course.getInstructorName());
                    course.addCourseFile(newCf);
                    AuditLog.log(loggedInUser, "Uploaded course file " + selectedFile.getName() + " to " + course.getCode());
                    Alert alert = new Alert(Alert.AlertType.INFORMATION, "Successfully uploaded " + selectedFile.getName());
                    alert.showAndWait();
                    showCourseFilesView(course);
                }
            });

            fileListContainer.getChildren().add(uploadBtn);
        }

        ScrollPane scroll = new ScrollPane(fileListContainer);
        scroll.setFitToWidth(true);
        root.getChildren().add(scroll);

        primaryStage.setScene(new Scene(root, 1000, 700));
    }
    
    private void showThingsToBeGradedView() {
        if (!loggedInRole.equals("instructor")) {
            showStudentDashboardView();
            return;
        }

        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: #ffffff;");

        root.setTop(createTopNav());
        root.setLeft(createSidebar("GradingQueue"));

        VBox content = new VBox(20);
        content.setPadding(new Insets(25, 30, 30, 30));
        content.setStyle("-fx-background-color: #ffffff;");

        Label portalLabel = new Label("FACULTY WORKSPACE / ACTION REQUIRED");
        portalLabel.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: #b45309;");

        Label headerTitle = new Label("📝 Things to be Graded");
        headerTitle.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: #111827;");

        VBox queueContainer = new VBox(12);
        boolean hasPendingSubmissions = false;

        for (Course c : courseList) {
            // Check if course belongs to logged-in instructor
            if (c.getInstructorName().equalsIgnoreCase(loggedInUser)) {
                for (Assignment a : c.getAssignments()) {
                    for (String student : c.getEnrolledStudents()) {
                        Submission sub = a.getSubmissionForStudent(student);

                        // Filter for submissions that are submitted and still pending grade
                        boolean isPending = sub.isSubmitted() && (sub.getGrade() == null || sub.getGrade().equalsIgnoreCase("Pending") || sub.getGrade().isEmpty());

                        if (isPending) {
                            hasPendingSubmissions = true;

                            HBox row = new HBox(15);
                            row.setAlignment(Pos.CENTER_LEFT);
                            row.setStyle("-fx-background-color: #fffbeb; -fx-border-color: #fcd34d; -fx-border-radius: 8px; -fx-background-radius: 8px; -fx-padding: 15px 20px;");

                            VBox info = new VBox(4);
                            Label courseAndAssign = new Label("[" + c.getCode() + "] " + a.getName());
                            courseAndAssign.setStyle("-fx-font-weight: bold; -fx-font-size: 15px; -fx-text-fill: #0f172a;");
                            
                            Label studentLbl = new Label("👤 Student: " + student + (sub.isLate() ? " [LATE]" : ""));
                            studentLbl.setStyle("-fx-text-fill: #b45309; -fx-font-weight: bold; -fx-font-size: 12px;");
                            
                            Label timeLbl = new Label("Submitted on: " + sub.getSubmissionTimestamp());
                            timeLbl.setStyle("-fx-text-fill: #64748b; -fx-font-size: 11px;");

                            info.getChildren().addAll(courseAndAssign, studentLbl, timeLbl);

                            Region sp = new Region();
                            HBox.setHgrow(sp, Priority.ALWAYS);

                            Button gradeNowBtn = new Button("Grade Submission →");
                            gradeNowBtn.setStyle("-fx-background-color: #6366f1; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand;");
                            gradeNowBtn.setOnAction(e -> openGradingDialog(c));

                            row.getChildren().addAll(info, sp, gradeNowBtn);
                            queueContainer.getChildren().add(row);
                        }
                    }
                }
            }
        }

        if (!hasPendingSubmissions) {
            queueContainer.getChildren().add(new Label("🎉 All caught up! There are no ungraded student submissions pending."));
        }

        ScrollPane scrollPane = new ScrollPane(queueContainer);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: transparent; -fx-background: transparent;");

        content.getChildren().addAll(portalLabel, headerTitle, new Separator(), scrollPane);
        root.setCenter(content);

        primaryStage.setScene(new Scene(root, 1150, 750));
    }
    
    // ==========================================
    // AT-RISK STUDENT ANALYTICS DASHBOARD
    // ==========================================
    private void openAtRiskAnalyticsModal(Course course) {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("At-Risk Student Analytics Hub");
        dialog.setHeaderText("Performance Oversight for " + course.getCode());
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);

        VBox container = new VBox(12);
        container.setPadding(new Insets(20));

        Label infoLbl = new Label("Students with a weighted course average below 70.0% are automatically flagged as At-Risk.");
        infoLbl.setStyle("-fx-text-fill: #64748b; -fx-font-size: 12px;");
        container.getChildren().add(infoLbl);

        boolean flaggedAny = false;
        for (String student : course.getEnrolledStudents()) {
            double avg = course.getGradePercentage(student);
            if (avg >= 0 && avg < 70.0) {
                flaggedAny = true;
                HBox studentRow = new HBox(15);
                studentRow.setAlignment(Pos.CENTER_LEFT);
                studentRow.setStyle("-fx-background-color: #fef2f2; -fx-border-color: #fca5a5; -fx-padding: 12px; -fx-background-radius: 6px; -fx-border-radius: 6px;");

                VBox details = new VBox(3);
                Label name = new Label("⚠️ Student: " + student);
                name.setStyle("-fx-font-weight: bold; -fx-text-fill: #991b1b; -fx-font-size: 14px;");
                Label score = new Label(String.format("Weighted Average: %.1f%% (%s)", avg, course.getLetterGrade(student)));
                score.setStyle("-fx-text-fill: #b91c1c; -fx-font-size: 12px;");
                details.getChildren().addAll(name, score);

                Region sp = new Region();
                HBox.setHgrow(sp, Priority.ALWAYS);

                Button emailBtn = new Button("View At-Risk Notice");
                emailBtn.setStyle("-fx-background-color: #dc2626; -fx-text-fill: white; -fx-font-weight: bold;");
                emailBtn.setOnAction(e -> {
                    AuditLog.log(loggedInUser, "Reviewed at-risk student record for " + student + " in " + course.getCode());
                    Alert alert = new Alert(Alert.AlertType.INFORMATION, "Student " + student + " has a weighted average of " + String.format("%.1f%%", avg) + ". Recommended action: Schedule office hours.");
                    alert.showAndWait();
                });

                studentRow.getChildren().addAll(details, sp, emailBtn);
                container.getChildren().add(studentRow);
            }
        }

        if (!flaggedAny) {
            container.getChildren().add(new Label("🎉 Excellent! No students are currently flagged as At-Risk in this course."));
        }

        ScrollPane sp = new ScrollPane(container);
        sp.setFitToWidth(true);
        sp.setPrefSize(750, 400);

        dialog.getDialogPane().setContent(sp);
        dialog.showAndWait();
    }
    
    private void openEditCourseDialog(Course course) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Edit & Roll Over Course");
        dialog.setHeaderText("Update Course Settings for " + course.getCode());
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10); grid.setVgap(10); grid.setPadding(new Insets(20));

        TextField sectionField = new TextField(course.getSection());
        TextField instructorField = new TextField(course.getInstructorName());
        TextField emailField = new TextField(course.getInstructorEmail());

        Button clearRosterBtn = new Button("🗑️ Clear Enrolled Roster (New Semester)");
        clearRosterBtn.setStyle("-fx-background-color: #ef4444; -fx-text-fill: white; -fx-font-weight: bold;");
        
        Label rosterStatusLbl = new Label("Currently enrolled students: " + course.getEnrolledStudents().size());
        clearRosterBtn.setOnAction(e -> {
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Are you sure you want to clear all student enrollments for a fresh semester?");
            confirm.showAndWait().ifPresent(res -> {
                if (res == ButtonType.OK) {
                    course.clearRoster();
                    rosterStatusLbl.setText("Currently enrolled students: 0");
                    AuditLog.log(loggedInUser, "Cleared student roster for course " + course.getCode());
                }
            });
        });

        grid.add(new Label("Section (e.g. R02 / Spring 2027):"), 0, 0); grid.add(sectionField, 1, 0);
        grid.add(new Label("Assigned Instructor Username:"), 0, 1); grid.add(instructorField, 1, 1);
        grid.add(new Label("Instructor Email:"), 0, 2); grid.add(emailField, 1, 2);
        grid.add(new Label("Semester Roll Over:"), 0, 3); grid.add(clearRosterBtn, 1, 3);
        grid.add(rosterStatusLbl, 1, 4);

        dialog.getDialogPane().setContent(grid);
        Optional<ButtonType> res = dialog.showAndWait();

        if (res.isPresent() && res.get() == ButtonType.OK) {
            String newInst = instructorField.getText().trim();
            if (!AuthService.isRegisteredInstructor(newInst)) {
                Alert err = new Alert(Alert.AlertType.ERROR, "Invalid Instructor Username. Must be a registered instructor account.");
                err.showAndWait();
                return;
            }

            course.setSection(sectionField.getText());
            course.setInstructor(newInst, emailField.getText());
            AuditLog.log(loggedInUser, "Updated course metadata and rollover settings for " + course.getCode());
            refreshAdminCourseList("");
            
            Alert alert = new Alert(Alert.AlertType.INFORMATION, "Course successfully updated for the new semester!");
            alert.showAndWait();
        }
    }
    
    // ==========================================
    // TIMED QUIZ TAKING VIEW
    // ==========================================
    private void showQuizTakerView(Course course, Quiz quiz) {
        if (quiz.hasStudentCompleted(loggedInUser)) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Quiz Already Completed");
            alert.setHeaderText("Single Attempt Limit Reached");
            alert.setContentText("You have already submitted this quiz. Your score is: " + quiz.getScoreForStudent(loggedInUser));
            alert.showAndWait();
            return;
        }

        quiz.startAttempt(loggedInUser);

        VBox root = createSubPageLayout("Quiz: " + quiz.getTitle());
        VBox form = new VBox(15);

        Label timerLbl = new Label();
        timerLbl.setStyle("-fx-font-weight: bold; -fx-text-fill: #b45309; -fx-font-size: 14px; -fx-background-color: #fef3c7; -fx-padding: 8px 12px; -fx-background-radius: 6px;");
        form.getChildren().add(timerLbl);

        List<ToggleGroup> groups = new ArrayList<>();

        for (int i = 0; i < quiz.getQuestions().size(); i++) {
            QuizQuestion q = quiz.getQuestions().get(i);
            VBox qBox = new VBox(8);
            qBox.setStyle("-fx-background-color: #f8fafc; -fx-padding: 12px; -fx-background-radius: 6px; -fx-border-color: #cbd5e1; -fx-border-radius: 6px;");

            Label qText = new Label("Q" + (i + 1) + ": " + q.getQuestionText());
            qText.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
            qBox.getChildren().add(qText);

            ToggleGroup group = new ToggleGroup();
            groups.add(group);

            List<String> options = q.getOptions();
            for (int optIdx = 0; optIdx < options.size(); optIdx++) {
                RadioButton rb = new RadioButton(options.get(optIdx));
                rb.setToggleGroup(group);
                rb.setUserData(optIdx);
                
                final int qIndex = i;
                final int choiceIdx = optIdx;
                rb.setOnAction(ev -> quiz.saveDraftAnswer(loggedInUser, qIndex, choiceIdx));

                qBox.getChildren().add(rb);
            }

            form.getChildren().add(qBox);
        }

        Button submitQuizBtn = new Button("Submit Quiz & Grade");
        submitQuizBtn.setStyle("-fx-background-color: #059669; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 14px;");
        
        Runnable submitAction = () -> {
            List<Integer> answers = new ArrayList<>();
            for (int i = 0; i < groups.size(); i++) {
                ToggleGroup g = groups.get(i);
                RadioButton selectedRb = (RadioButton) g.getSelectedToggle();
                if (selectedRb != null && selectedRb.getUserData() != null) {
                    answers.add((Integer) selectedRb.getUserData());
                } else {
                    answers.add(-1);
                }
            }

            String quizGrade = quiz.gradeQuiz(loggedInUser, answers);
            AuditLog.log(loggedInUser, "Completed quiz " + quiz.getTitle() + " in " + course.getCode() + " with score " + quizGrade);
            
            Alert alert = new Alert(Alert.AlertType.INFORMATION, "Quiz Submitted Successfully!\nYour Score: " + quizGrade);
            alert.showAndWait();
            onAssignmentsClicked(course);
        };

        submitQuizBtn.setOnAction(e -> submitAction.run());
        form.getChildren().add(submitQuizBtn);

        // Live Countdown Timeline
        javafx.animation.Timeline timeline = new javafx.animation.Timeline(
            new javafx.animation.KeyFrame(javafx.util.Duration.seconds(1), event -> {
                long remainingSecs = quiz.getRemainingSeconds(loggedInUser);
                long mins = remainingSecs / 60;
                long secs = remainingSecs % 60;
                timerLbl.setText(String.format("⏱️ Time Remaining: %02d:%02d", mins, secs));

                if (remainingSecs <= 0) {
                    Alert timeoutAlert = new Alert(Alert.AlertType.WARNING, "Time's up! Your quiz has been auto-submitted with saved answers.");
                    timeoutAlert.showAndWait();
                    submitAction.run();
                }
            })
        );
        timeline.setCycleCount(javafx.animation.Animation.INDEFINITE);
        timeline.play();

        ScrollPane sp = new ScrollPane(form);
        sp.setFitToWidth(true);
        root.getChildren().add(sp);

        primaryStage.setScene(new Scene(root, 900, 700));
    }

    // ==========================================
    // DYNAMIC MULTI-QUESTION QUIZ PUBLISHER
    // ==========================================
    private void openAddQuizDialog() {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Create Timed Quiz");
        dialog.setHeaderText("Publish New 4-Option Quiz");
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        VBox container = new VBox(12);
        container.setPadding(new Insets(20));

        ComboBox<Course> courseCombo = new ComboBox<>();
        // Instructors can only select their own courses
        for (Course c : courseList) {
            if (loggedInRole.equals("admin") || c.getInstructorName().equalsIgnoreCase(loggedInUser)) {
                courseCombo.getItems().add(c);
            }
        }
        
        courseCombo.setCellFactory(p -> new ListCell<Course>() {
            @Override protected void updateItem(Course item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "" : item.getCode() + " - " + item.getTitle());
            }
        });
        courseCombo.setButtonCell(courseCombo.getCellFactory().call(null));

        TextField titleField = new TextField(); titleField.setPromptText("Quiz Title (e.g. Midterm 1)");
        TextField timeField = new TextField(); timeField.setPromptText("Time limit in minutes (e.g., 30)");
        TextField dateField = new TextField(); dateField.setPromptText("Due Date: MM/dd/yyyy HH:mm");

        VBox questionsListContainer = new VBox(10);
        List<VBox> questionForms = new ArrayList<>();

        Runnable addQuestionForm = () -> {
            VBox qForm = new VBox(6);
            qForm.setStyle("-fx-background-color: #f1f5f9; -fx-padding: 10px; -fx-background-radius: 6px;");
            
            TextField qText = new TextField(); qText.setPromptText("Question Text");
            TextField opt0 = new TextField(); opt0.setPromptText("A) Option 1");
            TextField opt1 = new TextField(); opt1.setPromptText("B) Option 2");
            TextField opt2 = new TextField(); opt2.setPromptText("C) Option 3");
            TextField opt3 = new TextField(); opt3.setPromptText("D) Option 4"); // 4th Option
            
            ComboBox<Integer> correctIdx = new ComboBox<>();
            correctIdx.getItems().addAll(0, 1, 2, 3);
            correctIdx.setValue(0);
            HBox correctRow = new HBox(8, new Label("Correct Answer Index (0=A, 1=B, 2=C, 3=D):"), correctIdx);

            qForm.getChildren().addAll(new Label("Question:"), qText, opt0, opt1, opt2, opt3, correctRow);
            questionForms.add(qForm);
            questionsListContainer.getChildren().add(qForm);
        };

        addQuestionForm.run();

        Button addAnotherQBtn = new Button("+ Add Another Question");
        addAnotherQBtn.setOnAction(e -> addQuestionForm.run());

        ScrollPane qScroll = new ScrollPane(questionsListContainer);
        qScroll.setFitToWidth(true);
        qScroll.setPrefHeight(250);

        container.getChildren().addAll(
            new Label("Select Course:"), courseCombo,
            new Label("Quiz Title:"), titleField,
            new Label("Time Limit (Minutes):"), timeField,
            new Label("Due Date (MM/dd/yyyy HH:mm):"), dateField,
            new Separator(),
            new Label("Quiz Questions (4 Options Each):"), qScroll, addAnotherQBtn
        );

        dialog.getDialogPane().setContent(container);
        Optional<ButtonType> res = dialog.showAndWait();

        Course selected = courseCombo.getValue();
        if (res.isPresent() && res.get() == ButtonType.OK && selected != null && !titleField.getText().isEmpty() && !questionForms.isEmpty()) {
            int mins = 30;
            try { mins = Integer.parseInt(timeField.getText()); } catch(Exception ignored) {}

            Quiz newQuiz = new Quiz(titleField.getText(), mins, dateField.getText());

            for (VBox qf : questionForms) {
                TextField qTxt = (TextField) qf.getChildren().get(1);
                TextField o0 = (TextField) qf.getChildren().get(2);
                TextField o1 = (TextField) qf.getChildren().get(3);
                TextField o2 = (TextField) qf.getChildren().get(4);
                TextField o3 = (TextField) qf.getChildren().get(5);
                HBox row = (HBox) qf.getChildren().get(6);
                ComboBox<Integer> cIdx = (ComboBox<Integer>) row.getChildren().get(1);

                if (!qTxt.getText().isEmpty()) {
                    List<String> options = new ArrayList<>();
                    if (!o0.getText().isEmpty()) options.add(o0.getText());
                    if (!o1.getText().isEmpty()) options.add(o1.getText());
                    if (!o2.getText().isEmpty()) options.add(o2.getText());
                    if (!o3.getText().isEmpty()) options.add(o3.getText());

                    if (options.size() >= 2) {
                        newQuiz.addQuestion(new QuizQuestion(qTxt.getText(), options, cIdx.getValue()));
                    }
                }
            }

            if (!newQuiz.getQuestions().isEmpty()) {
                selected.addQuiz(newQuiz);
                AuditLog.log(loggedInUser, "Published 4-option quiz " + titleField.getText() + " to " + selected.getCode());
                refreshTeacherCourseList("");
                Alert alert = new Alert(Alert.AlertType.INFORMATION, "4-Option Quiz successfully published!");
                alert.showAndWait();
            }
        }
    }

    // ==========================================
    // SYSTEM AUDIT TRAIL VIEWER MODAL
    // ==========================================
    private void openAuditLogViewer() {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("System Audit Trail & Activity Log");
        dialog.setHeaderText("Immutable Security & Compliance Log");
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);

        VBox container = new VBox(10);
        container.setPadding(new Insets(20));

        if (AuditLog.getEntries().isEmpty()) {
            container.getChildren().add(new Label("No audit events recorded yet."));
        } else {
            for (AuditLog.AuditEntry entry : AuditLog.getEntries()) {
                HBox row = new HBox(15);
                row.setAlignment(Pos.CENTER_LEFT);
                row.setStyle("-fx-background-color: #f8fafc; -fx-padding: 10px; -fx-background-radius: 6px; -fx-border-color: #cbd5e1; -fx-border-radius: 6px;");

                Label timeLbl = new Label("[" + entry.getTimestamp() + "]");
                timeLbl.setStyle("-fx-font-family: monospace; -fx-text-fill: #64748b; -fx-font-size: 11px;");

                Label actorLbl = new Label("👤 " + entry.getActor());
                actorLbl.setStyle("-fx-font-weight: bold; -fx-text-fill: #0f172a; -fx-min-width: 90px;");

                Label actionLbl = new Label(entry.getAction());
                actionLbl.setStyle("-fx-text-fill: #334155; -fx-font-size: 13px;");
                HBox.setHgrow(actionLbl, Priority.ALWAYS);

                row.getChildren().addAll(timeLbl, actorLbl, actionLbl);
                container.getChildren().add(row);
            }
        }

        ScrollPane sp = new ScrollPane(container);
        sp.setFitToWidth(true);
        sp.setPrefSize(850, 500);

        dialog.getDialogPane().setContent(sp);
        dialog.showAndWait();
    }

    // ==========================================
    // NAVIGATION SUB-PAGES
    // ==========================================
    @Override
    public void onSyllabusClicked(Course course) {
        VBox root = createSubPageLayout("Syllabus & Information - " + course.getTitle());
        
        Label content = new Label(course.getSyllabusPath() == null || course.getSyllabusPath().isEmpty() 
            ? "No Syllabus file uploaded for this course yet." 
            : "Loaded Managed Syllabus File: " + course.getSyllabusPath());
        content.setStyle("-fx-font-size: 15px;");

        Button viewAnnouncementsBtn = new Button("📢 View Course Announcements (" + course.getAnnouncements().size() + ")");
        viewAnnouncementsBtn.setStyle("-fx-background-color: #f59e0b; -fx-text-fill: white; -fx-font-weight: bold;");
        viewAnnouncementsBtn.setOnAction(e -> showAnnouncementsView(course));

        Button viewFilesRepoBtn = new Button("📁 View Course Files & Lecture Resources (" + course.getCourseFiles().size() + ")");
        viewFilesRepoBtn.setStyle("-fx-background-color: #0284c7; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 10px 16px;");
        viewFilesRepoBtn.setOnAction(e -> showCourseFilesView(course));

        root.getChildren().addAll(content, viewAnnouncementsBtn, viewFilesRepoBtn);
        primaryStage.setScene(new Scene(root, 1000, 700));
    }

    @Override
    public void onAssignmentsClicked(Course course) {
        VBox root = createSubPageLayout("Assignments & Quizzes - " + course.getTitle());
        VBox contentList = new VBox(15);

        Label assignHeader = new Label("■ Homework Assignments");
        assignHeader.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
        contentList.getChildren().add(assignHeader);

        if (course.getAssignments().isEmpty()) {
            contentList.getChildren().add(new Label("No assignments listed for this course."));
        } else {
            for (Assignment a : course.getAssignments()) {
                Submission sub = a.getSubmissionForStudent(loggedInUser);

                HBox row = new HBox(15);
                row.setAlignment(Pos.CENTER_LEFT);
                row.setStyle("-fx-background-color: #e0f2fe; -fx-border-color: #0284c7; -fx-padding: 15px; -fx-background-radius: 6px; -fx-border-radius: 6px;");
                
                VBox info = new VBox(4);
                Label name = new Label(a.getName() + (sub.isLate() ? " (LATE SUBMISSION)" : ""));
                name.setStyle("-fx-font-weight: bold; -fx-font-size: 15px;");
                Label desc = new Label(a.getDescription());
                desc.setStyle("-fx-text-fill: #334155; -fx-font-size: 12px;");
                
                Label statusLbl = new Label(sub.isSubmitted() ? "Status: Submitted (" + sub.getSubmissionTimestamp() + ")" : "Status: Not Submitted");
                statusLbl.setStyle(sub.isSubmitted() ? "-fx-text-fill: #047857; -fx-font-weight: bold; -fx-font-size: 11px;" : "-fx-text-fill: #b45309; -fx-font-weight: bold; -fx-font-size: 11px;");
                
                info.getChildren().addAll(name, desc, statusLbl);

                Region sp = new Region();
                HBox.setHgrow(sp, Priority.ALWAYS);

                VBox dueBox = new VBox(2);
                dueBox.setAlignment(Pos.CENTER_RIGHT);
                Label dueTitle = new Label("DUE:");
                dueTitle.setStyle("-fx-font-size: 9px; -fx-font-weight: bold; -fx-text-fill: #64748b;");
                Label due = new Label(a.getDueDate());
                due.setStyle("-fx-font-weight: bold; -fx-font-size: 13px;");
                dueBox.getChildren().addAll(dueTitle, due);

                Button submitBtn = new Button(sub.isSubmitted() ? "Resubmit Work" : "Submit Homework");
                submitBtn.setStyle(sub.isSubmitted() ? "-fx-background-color: #10b981; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand;" : "-fx-background-color: #f59e0b; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand;");
                submitBtn.setOnAction(e -> openStudentSubmissionDialog(a, course));

                row.getChildren().addAll(info, sp, dueBox, submitBtn);
                contentList.getChildren().add(row);
            }
        }

        Label quizHeader = new Label("■ Timed Quizzes & Exams");
        quizHeader.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
        contentList.getChildren().add(quizHeader);

        if (course.getQuizzes().isEmpty()) {
            contentList.getChildren().add(new Label("No quizzes published for this course."));
        } else {
            for (Quiz q : course.getQuizzes()) {
                boolean completed = q.hasStudentCompleted(loggedInUser);

                HBox qRow = new HBox(15);
                qRow.setAlignment(Pos.CENTER_LEFT);
                qRow.setStyle(completed 
                    ? "-fx-background-color: #f0fdf4; -fx-border-color: #86efac; -fx-padding: 15px; -fx-background-radius: 6px; -fx-border-radius: 6px;"
                    : "-fx-background-color: #fef3c7; -fx-border-color: #f59e0b; -fx-padding: 15px; -fx-background-radius: 6px; -fx-border-radius: 6px;");

                VBox qInfo = new VBox(4);
                Label qName = new Label(q.getTitle());
                qName.setStyle("-fx-font-weight: bold; -fx-font-size: 15px;");
                
                Label qMeta = new Label(completed 
                    ? "✓ Completed | Your Score: " + q.getScoreForStudent(loggedInUser)
                    : "Time Limit: " + q.getTimeLimitMinutes() + " mins | Questions: " + q.getQuestions().size());
                qMeta.setStyle(completed ? "-fx-text-fill: #166534; -fx-font-weight: bold; -fx-font-size: 12px;" : "-fx-text-fill: #92400e; -fx-font-size: 12px;");
                
                qInfo.getChildren().addAll(qName, qMeta);

                Region sp2 = new Region();
                HBox.setHgrow(sp2, Priority.ALWAYS);

                // Due date box for quizzes and exams
                VBox qDueBox = new VBox(2);
                qDueBox.setAlignment(Pos.CENTER_RIGHT);
                Label qDueTitle = new Label("DUE:");
                qDueTitle.setStyle("-fx-font-size: 9px; -fx-font-weight: bold; -fx-text-fill: #64748b;");
                Label qDue = new Label(q.getDueDate());
                qDue.setStyle("-fx-font-weight: bold; -fx-font-size: 13px;");
                qDueBox.getChildren().addAll(qDueTitle, qDue);

                Button takeQuizBtn = new Button(completed ? "Review Score" : "Take Quiz");
                takeQuizBtn.setStyle(completed 
                    ? "-fx-background-color: #10b981; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand;"
                    : "-fx-background-color: #d97706; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand;");
                takeQuizBtn.setOnAction(e -> showQuizTakerView(course, q));

                qRow.getChildren().addAll(qInfo, sp2, qDueBox, takeQuizBtn);
                contentList.getChildren().add(qRow);
            }
        }

        ScrollPane sp = new ScrollPane(contentList);
        sp.setFitToWidth(true);
        root.getChildren().add(sp);

        primaryStage.setScene(new Scene(root, 1000, 700));
    }

    @Override
    public void onGradesClicked(Course course) {
        VBox root = createSubPageLayout("Grades & Feedback - " + course.getTitle());
        
        double pct = course.getGradePercentage(loggedInUser);
        String pctStr = (pct < 0) ? "N/A" : String.format("%.1f%%", pct);
        Label summaryLabel = new Label("Weighted Course Grade: " + course.getLetterGrade(loggedInUser) + " (" + pctStr + ")");
        summaryLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #111827;");

        VBox assignList = new VBox(10);
        Label sectionHeader = new Label("Past Assignments & Feedback:");
        sectionHeader.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #4b5563;");

        if (course.getAssignments().isEmpty()) {
            assignList.getChildren().add(new Label("No past assignments recorded yet."));
        } else {
            for (Assignment a : course.getAssignments()) {
                Submission sub = a.getSubmissionForStudent(loggedInUser);

                HBox row = new HBox(15);
                row.setAlignment(Pos.CENTER_LEFT);
                row.setStyle("-fx-background-color: #f8fafc; -fx-border-color: #cbd5e1; -fx-border-radius: 6px; -fx-background-radius: 6px; -fx-padding: 12px;");

                VBox info = new VBox(3);
                Label nameLbl = new Label(a.getName());
                nameLbl.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
                
                Label feedbackLbl = new Label("Feedback: " + sub.getFeedback());
                feedbackLbl.setStyle("-fx-text-fill: #64748b; -fx-font-size: 12px;");
                info.getChildren().addAll(nameLbl, feedbackLbl);

                Region sp = new Region();
                HBox.setHgrow(sp, Priority.ALWAYS);

                Label gradeLbl = new Label(sub.getGrade());
                gradeLbl.setStyle("-fx-font-weight: bold; -fx-font-size: 16px; -fx-text-fill: #047857;");

                row.getChildren().addAll(info, sp, gradeLbl);
                assignList.getChildren().add(row);
            }
        }

        root.getChildren().addAll(summaryLabel, new Separator(), sectionHeader, assignList);
        primaryStage.setScene(new Scene(root, 1000, 700));
    }

    @Override
    public void onInstructorClicked(Course course) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Instructor Contact");
        alert.setHeaderText(course.getInstructorName());
        alert.setContentText("Email: " + course.getInstructorEmail());
        alert.showAndWait();
    }

    // ==========================================
    // INTERACTIVE DIALOGS & MODALS
    // ==========================================
    private void openStudentSubmissionDialog(Assignment assignment, Course course) {
        Submission sub = assignment.getSubmissionForStudent(loggedInUser);

        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Submit Homework");
        dialog.setHeaderText("Submit Work for: " + assignment.getName());
        
        ButtonType submitBtnType = new ButtonType("Submit Work", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(submitBtnType, ButtonType.CANCEL);

        VBox box = new VBox(15);
        box.setPadding(new Insets(20));

        final String[] selectedPath = { sub.getSubmittedFilePath() };
        Label fileLbl = new Label(selectedPath[0] == null ? "No file attached" : "Attached File: " + selectedPath[0]);
        Button chooseBtn = new Button("Browse File...");
        
        chooseBtn.setOnAction(e -> {
            FileChooser chooser = new FileChooser();
            File file = chooser.showOpenDialog(primaryStage);
            if (file != null) {
                selectedPath[0] = file.getAbsolutePath();
                fileLbl.setText("Ready to submit: " + file.getName());
            }
        });

        box.getChildren().addAll(new Label("Select your solution file (PDF, ZIP, Java):"), chooseBtn, fileLbl);
        dialog.getDialogPane().setContent(box);

        Optional<ButtonType> result = dialog.showAndWait();
        if (result.isPresent() && result.get() == submitBtnType && selectedPath[0] != null) {
            assignment.submitWork(loggedInUser, selectedPath[0]);
            AuditLog.log(loggedInUser, "Submitted file for " + assignment.getName() + " in " + course.getCode());
            Alert alert = new Alert(Alert.AlertType.INFORMATION, "Work successfully submitted!");
            alert.showAndWait();
            onAssignmentsClicked(course);
        }
    }

    private void openGradingDialog(Course course) {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Individual Grading Portal");
        dialog.setHeaderText("Grade Submissions for " + course.getCode());
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);

        VBox list = new VBox(15);
        list.setPadding(new Insets(20));

        if (course.getAssignments().isEmpty()) {
            list.getChildren().add(new Label("No assignments created yet for this course."));
        }

        for (Assignment a : course.getAssignments()) {
            VBox assignBox = new VBox(10);
            assignBox.setStyle("-fx-background-color: #f8fafc; -fx-padding: 15px; -fx-background-radius: 8px; -fx-border-color: #cbd5e1; -fx-border-radius: 8px;");

            Label assignTitle = new Label("Assignment: " + a.getName() + " (Due: " + a.getDueDate() + ")");
            assignTitle.setStyle("-fx-font-weight: bold; -fx-font-size: 15px; -fx-text-fill: #0f172a;");
            assignBox.getChildren().add(assignTitle);

            if (course.getEnrolledStudents().isEmpty()) {
                assignBox.getChildren().add(new Label("No students currently enrolled."));
            }

            for (String studentUser : course.getEnrolledStudents()) {
                Submission sub = a.getSubmissionForStudent(studentUser);

                HBox studentRow = new HBox(10);
                studentRow.setAlignment(Pos.CENTER_LEFT);
                studentRow.setStyle("-fx-background-color: #ffffff; -fx-padding: 10px; -fx-background-radius: 6px; -fx-border-color: #e2e8f0; -fx-border-radius: 6px;");

                Label studentLbl = new Label("👤 " + studentUser + (sub.isLate() ? " [LATE]" : ""));
                studentLbl.setStyle("-fx-font-weight: bold; -fx-min-width: 110px;");

                Button viewFileBtn = new Button("View File");
                viewFileBtn.setStyle("-fx-background-color: #0284c7; -fx-text-fill: white; -fx-font-weight: bold;");
                viewFileBtn.setDisable(sub.getSubmittedFilePath() == null || sub.getSubmittedFilePath().isEmpty());
                
                viewFileBtn.setOnAction(e -> {
                    try {
                        File file = new File(sub.getSubmittedFilePath());
                        if (file.exists() && java.awt.Desktop.isDesktopSupported()) {
                            java.awt.Desktop.getDesktop().open(file);
                        } else {
                            Alert err = new Alert(Alert.AlertType.ERROR, "File not found at: " + sub.getSubmittedFilePath());
                            err.showAndWait();
                        }
                    } catch (Exception ex) {
                        Alert err = new Alert(Alert.AlertType.ERROR, "Could not open file: " + ex.getMessage());
                        err.showAndWait();
                    }
                });

                Button autoTestBtn = new Button("🤖 Auto-Test");
                autoTestBtn.setStyle("-fx-background-color: #059669; -fx-text-fill: white; -fx-font-weight: bold;");
                autoTestBtn.setDisable(sub.getSubmittedFilePath() == null || sub.getSubmittedFilePath().isEmpty());
                autoTestBtn.setOnAction(e -> {
                    // Simple built-in syntax/file validation check without needing AutoTester.java
                    File submittedFile = new File(sub.getSubmittedFilePath());
                    String autoScore = submittedFile.exists() ? "100% (File Verified)" : "0% (File Missing)";
                    
                    sub.setGrade(autoScore);
                    sub.setFeedback("Internal file verification executed. Result: " + autoScore);
                    AuditLog.log(loggedInUser, "Ran file verification for " + studentUser + " on " + a.getName() + " (Score: " + autoScore + ")");
                    Alert alert = new Alert(Alert.AlertType.INFORMATION, "Verification executed!\nAssigned Status: " + autoScore);
                    alert.showAndWait();
                });

                TextField gradeField = new TextField(sub.getGrade());
                gradeField.setPromptText("Grade (%)");
                gradeField.setPrefWidth(85);

                TextField feedbackField = new TextField(sub.getFeedback());
                feedbackField.setPromptText("Feedback for " + studentUser);
                HBox.setHgrow(feedbackField, Priority.ALWAYS);

                Button saveBtn = new Button("Save");
                saveBtn.setStyle("-fx-background-color: #6366f1; -fx-text-fill: white; -fx-font-weight: bold;");
                saveBtn.setOnAction(e -> {
                    sub.setGrade(gradeField.getText());
                    sub.setFeedback(feedbackField.getText());
                    AuditLog.log(loggedInUser, "Graded " + studentUser + " for " + a.getName() + " with " + sub.getGrade());
                    Alert alert = new Alert(Alert.AlertType.INFORMATION, "Saved grade (" + sub.getGrade() + ") for " + studentUser);
                    alert.showAndWait();
                });

                studentRow.getChildren().addAll(studentLbl, viewFileBtn, autoTestBtn, gradeField, feedbackField, saveBtn);
                assignBox.getChildren().add(studentRow);
            }

            list.getChildren().add(assignBox);
        }

        ScrollPane sp = new ScrollPane(list);
        sp.setFitToWidth(true);
        sp.setPrefSize(850, 520);

        dialog.getDialogPane().setContent(sp);
        dialog.showAndWait();
        refreshTeacherCourseList("");
    }

    private void openAddAssignmentDialog() {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Create Assignment");
        dialog.setHeaderText("Publish New Assignment with Validated Date & Time");
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10); grid.setVgap(10); grid.setPadding(new Insets(20));

        ComboBox<Course> courseCombo = new ComboBox<>();
        // Instructors can only select and publish assignments for their own courses
        for (Course c : courseList) {
            if (loggedInRole.equals("admin") || c.getInstructorName().equalsIgnoreCase(loggedInUser)) {
                courseCombo.getItems().add(c);
            }
        }
        
        courseCombo.setCellFactory(p -> new ListCell<Course>() {
            @Override protected void updateItem(Course item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "" : item.getCode() + " - " + item.getTitle());
            }
        });
        courseCombo.setButtonCell(courseCombo.getCellFactory().call(null));

        TextField nameField = new TextField(); nameField.setPromptText("Assignment Name");
        TextField dateField = new TextField(); dateField.setPromptText("MM/dd/yyyy HH:mm (e.g. 10/30/2026 23:59)");
        TextField pointsField = new TextField("100.0"); pointsField.setPromptText("Points Possible");
        TextField descField = new TextField(); descField.setPromptText("Detailed instructions...");

        grid.add(new Label("Select Course:"), 0, 0); grid.add(courseCombo, 1, 0);
        grid.add(new Label("Assignment Title:"), 0, 1); grid.add(nameField, 1, 1);
        grid.add(new Label("Due Date (MM/dd/yyyy HH:mm):"), 0, 2); grid.add(dateField, 1, 2);
        grid.add(new Label("Points Possible:"), 0, 3); grid.add(pointsField, 1, 3);
        grid.add(new Label("Description:"), 0, 4); grid.add(descField, 1, 4);

        dialog.getDialogPane().setContent(grid);
        Optional<ButtonType> res = dialog.showAndWait();

        Course selected = courseCombo.getValue();
        if (res.isPresent() && res.get() == ButtonType.OK && selected != null && !nameField.getText().isEmpty()) {
            double pts = 100.0;
            try { pts = Double.parseDouble(pointsField.getText()); } catch(Exception ignored) {}

            selected.getAssignments().add(new Assignment(nameField.getText(), dateField.getText(), descField.getText(), pts));
            AuditLog.log(loggedInUser, "Published assignment " + nameField.getText() + " to " + selected.getCode());
            refreshTeacherCourseList("");
            Alert alert = new Alert(Alert.AlertType.INFORMATION, "Assignment successfully published!");
            alert.showAndWait();
        }
    }
    
    private void openAddCourseDialog() {
        Dialog<Course> dialog = new Dialog<>();
        dialog.setTitle("Create Course");
        dialog.setHeaderText("Enter Course Details (Assigned Instructor Required)");

        ButtonType saveButtonType = new ButtonType("Save Course", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10); grid.setVgap(10); grid.setPadding(new Insets(20));

        TextField codeField = new TextField(); codeField.setPromptText("CS4321");
        TextField sectionField = new TextField(); sectionField.setPromptText("R02");
        TextField titleField = new TextField(); titleField.setPromptText("Algorithms");
        TextField instructorField = new TextField(); instructorField.setPromptText("Valid instructor username");
        TextField emailField = new TextField(); emailField.setPromptText("smith@univ.edu");
        
        Label syllabusPathLbl = new Label("No syllabus selected");
        Button uploadSyllabusBtn = new Button("Upload Syllabus");
        final String[] selectedPath = {""};
        
        uploadSyllabusBtn.setOnAction(e -> {
            FileChooser chooser = new FileChooser();
            File file = chooser.showOpenDialog(primaryStage);
            if (file != null) {
                selectedPath[0] = file.getAbsolutePath();
                syllabusPathLbl.setText(file.getName());
            }
        });

        grid.add(new Label("Course Code:"), 0, 0); grid.add(codeField, 1, 0);
        grid.add(new Label("Section:"), 0, 1); grid.add(sectionField, 1, 1);
        grid.add(new Label("Course Title:"), 0, 2); grid.add(titleField, 1, 2);
        grid.add(new Label("Instructor Username:"), 0, 3); grid.add(instructorField, 1, 3);
        grid.add(new Label("Instructor Email:"), 0, 4); grid.add(emailField, 1, 4);
        grid.add(uploadSyllabusBtn, 0, 5); grid.add(syllabusPathLbl, 1, 5);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                String instUser = instructorField.getText().trim();
                if (!AuthService.isRegisteredInstructor(instUser)) {
                    Alert err = new Alert(Alert.AlertType.ERROR, "Invalid Instructor Username. Must be a registered instructor account.");
                    err.showAndWait();
                    return null;
                }
                Course newCourse = new Course(codeField.getText(), sectionField.getText(), titleField.getText(), instUser, emailField.getText());
                newCourse.setSyllabusPath(selectedPath[0]);
                return newCourse;
            }
            return null;
        });

        dialog.showAndWait().ifPresent(course -> {
            courseList.add(course);
            AuditLog.log(loggedInUser, "Created system course: " + course.getCode());
            refreshAdminCourseList("");
        });
    }
    
    private void openRegisterUserDialog() {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Register New User");
        dialog.setHeaderText("Create System Account");
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10); grid.setVgap(10); grid.setPadding(new Insets(20));

        TextField userField = new TextField(); userField.setPromptText("username");
        PasswordField passField = new PasswordField(); passField.setPromptText("password");
        
        ComboBox<String> roleCombo = new ComboBox<>();
        roleCombo.getItems().addAll("student", "instructor", "admin");
        roleCombo.setValue("student");

        grid.add(new Label("Username:"), 0, 0); grid.add(userField, 1, 0);
        grid.add(new Label("Password:"), 0, 1); grid.add(passField, 1, 1);
        grid.add(new Label("System Role:"), 0, 2); grid.add(roleCombo, 1, 2);

        dialog.getDialogPane().setContent(grid);
        Optional<ButtonType> res = dialog.showAndWait();

        String newUser = userField.getText();
        String newPass = passField.getText();
        String newRole = roleCombo.getValue();

        if (res.isPresent() && res.get() == ButtonType.OK && newUser != null && !newUser.trim().isEmpty() && newPass != null && !newPass.trim().isEmpty()) {
            AuthService.registerUser(newUser, newPass, newRole);
            AuditLog.log(loggedInUser, "Registered new account: " + newUser + " as " + newRole);
            Alert alert = new Alert(Alert.AlertType.INFORMATION, "Successfully registered user: " + newUser + " as " + newRole);
            alert.showAndWait();
        }
    }
    
    private void openChangePasswordDialog() {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Change Password");
        dialog.setHeaderText("Update Password for: " + loggedInUser);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        VBox content = new VBox(10);
        content.setPadding(new Insets(20));

        PasswordField currentField = new PasswordField();
        currentField.setPromptText("Enter current password");

        PasswordField newField = new PasswordField();
        newField.setPromptText("Enter new password");

        PasswordField confirmField = new PasswordField();
        confirmField.setPromptText("Confirm new password");

        content.getChildren().addAll(
            new Label("Current Password:"), currentField,
            new Label("New Password:"), newField,
            new Label("Confirm New Password:"), confirmField
        );

        dialog.getDialogPane().setContent(content);

        // Intercept OK button to perform validation before closing
        Button okButton = (Button) dialog.getDialogPane().lookupButton(ButtonType.OK);
        okButton.addEventFilter(javafx.event.ActionEvent.ACTION, event -> {
            String oldPass = currentField.getText();
            String newPass = newField.getText();
            String confirmPass = confirmField.getText();

            AuthService auth = new AuthService();

            // 1. Verify current password
            if (!auth.authenticate(loggedInUser, oldPass)) {
                showAlert(Alert.AlertType.ERROR, "Password Error", "The current password you entered is incorrect.");
                event.consume(); // Keeps dialog open
                return;
            }

            // 2. Validate new password match
            if (newPass.isEmpty() || !newPass.equals(confirmPass)) {
                showAlert(Alert.AlertType.ERROR, "Password Error", "New passwords do not match or cannot be empty.");
                event.consume(); // Keeps dialog open
                return;
            }

            // 3. Update password
            auth.updatePassword(loggedInUser, newPass);
            AuditLog.log(loggedInUser, "Changed account password");
            showAlert(Alert.AlertType.INFORMATION, "Success", "Your password has been successfully updated.");
        });

        dialog.showAndWait();
    }

    // Helper for quick alerts if you don't already have one
    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
    
    private void openRosterManagementDialog(Course course) {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Course Roster Management");
        dialog.setHeaderText("Manage Enrolled Students for " + course.getCode());
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);

        VBox container = new VBox(15);
        container.setPadding(new Insets(20));

        VBox studentListContainer = new VBox(8);
        
        // Runnable to dynamically refresh the student roster list view
        Runnable refreshList = new Runnable() {
            @Override
            public void run() {
                studentListContainer.getChildren().clear();
                if (course.getEnrolledStudents().isEmpty()) {
                    studentListContainer.getChildren().add(new Label("No students enrolled currently."));
                } else {
                    for (String student : course.getEnrolledStudents()) {
                        HBox row = new HBox(15);
                        row.setAlignment(Pos.CENTER_LEFT);
                        row.setStyle("-fx-background-color: #f8fafc; -fx-padding: 10px; -fx-background-radius: 6px; -fx-border-color: #cbd5e1; -fx-border-radius: 6px;");

                        Label nameLbl = new Label("👤 " + student);
                        nameLbl.setStyle("-fx-font-weight: bold;");
                        HBox.setHgrow(nameLbl, Priority.ALWAYS);

                        row.getChildren().add(nameLbl);

                        // ADMIN ONLY: Allow dropping individual students from the course
                        if (loggedInRole.equals("admin")) {
                            Button dropBtn = new Button("Drop Student");
                            dropBtn.setStyle("-fx-background-color: #ef4444; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand;");
                            dropBtn.setOnAction(e -> {
                                course.removeStudent(student);
                                AuditLog.log(loggedInUser, "Dropped student " + student + " from " + course.getCode());
                                this.run(); // Re-render list immediately upon removal
                            });
                            row.getChildren().add(dropBtn);
                        }

                        studentListContainer.getChildren().add(row);
                    }
                }
            }
        };

        refreshList.run();

        container.getChildren().addAll(new Label("Current Enrolled Roster:"), studentListContainer);

        // ADMIN ONLY: Enroll new student section
        if (loggedInRole.equals("admin")) {
            HBox enrollBox = new HBox(10);
            TextField newStudentField = new TextField();
            newStudentField.setPromptText("Enter student username");
            Button enrollBtn = new Button("Enroll Student");
            enrollBtn.setStyle("-fx-background-color: #10b981; -fx-text-fill: white; -fx-font-weight: bold;");
            
            enrollBtn.setOnAction(e -> {
                String uname = newStudentField.getText().trim();
                if (uname.isEmpty()) return;

                // 1. MUST use a lowercase object variable name (authService)
                AuthService authService = new AuthService();
                
                // 2. MUST call it on the instance, NOT the class name (AuthService.getRole)
                String role = authService.getRole(uname);

                // Check if user exists
                if (role == null || role.trim().isEmpty()) {
                    Alert err = new Alert(Alert.AlertType.ERROR);
                    err.setTitle("Enrollment Failed");
                    err.setHeaderText("User Not Found");
                    err.setContentText("The username '" + uname + "' does not exist in the system.");
                    err.showAndWait();
                    return;
                }

                // Check if student role
                if (!"student".equalsIgnoreCase(role)) {
                    Alert err = new Alert(Alert.AlertType.ERROR);
                    err.setTitle("Enrollment Failed");
                    err.setHeaderText("Invalid User Role");
                    err.setContentText("Only student accounts can be enrolled.");
                    err.showAndWait();
                    return;
                }

                if (course.getEnrolledStudents().contains(uname)) {
                    Alert warn = new Alert(Alert.AlertType.WARNING);
                    warn.setTitle("Already Enrolled");
                    warn.setHeaderText("Duplicate Enrollment");
                    warn.setContentText("Student '" + uname + "' is already enrolled.");
                    warn.showAndWait();
                    return;
                }

                course.addStudent(uname);
                AuditLog.log(loggedInUser, "Enrolled student " + uname + " into " + course.getCode());
                newStudentField.clear();
                refreshList.run();
            });
            enrollBox.getChildren().addAll(newStudentField, enrollBtn);
            container.getChildren().addAll(new Separator(), new Label("Enroll New Student:"), enrollBox);
        }

        ScrollPane sp = new ScrollPane(container);
        sp.setFitToWidth(true);
        sp.setPrefSize(500, 400);

        dialog.getDialogPane().setContent(sp);
        dialog.showAndWait();
    }

    private HBox createTopNav() {
        HBox topNav = new HBox(15);
        topNav.setPadding(new Insets(15, 25, 15, 25));
        topNav.setAlignment(Pos.CENTER_LEFT);
        topNav.setStyle("-fx-border-color: #e5e7eb; -fx-border-width: 0 0 1 0; -fx-background-color: white;");

        HBox logoBox = new HBox(8);
        logoBox.setAlignment(Pos.CENTER);
        Label iconLabel = new Label("J");
        iconLabel.setStyle("-fx-background-color: #f59e0b; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 4px 10px; -fx-background-radius: 4px;");
        Label brandLabel = new Label("JAME LMS");
        brandLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 16px; -fx-text-fill: #111827;");
        logoBox.getChildren().addAll(iconLabel, brandLabel);

        Region navSpacer = new Region();
        HBox.setHgrow(navSpacer, Priority.ALWAYS);

        Button roleBadgeBtn = new Button(loggedInRole.toUpperCase() + " PORTAL");
        roleBadgeBtn.setStyle("-fx-background-color: #fef3c7; -fx-text-fill: #b45309; -fx-font-weight: bold; -fx-background-radius: 20px; -fx-padding: 6px 14px;");
        
        String singleInitial = loggedInUser.isEmpty() ? "U" : loggedInUser.substring(0, 1).toUpperCase();
        Button avatarBtn = new Button(singleInitial);
        avatarBtn.setStyle("-fx-background-color: #0f172a; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8px 14px; -fx-background-radius: 20px; -fx-cursor: hand;");
        
        ContextMenu profileMenu = new ContextMenu();
        
        // Change Password Menu Item
        MenuItem changePassItem = new MenuItem("🔑 Change Password");
        changePassItem.setStyle("-fx-font-weight: bold; -fx-text-fill: #0284c7;");
        changePassItem.setOnAction(e -> openChangePasswordDialog());

        // Logout Menu Item
        MenuItem logoutItem = new MenuItem("🚪 Log out");
        logoutItem.setStyle("-fx-font-weight: bold; -fx-text-fill: #dc2626;");
        logoutItem.setOnAction(e -> {
            AuditLog.log(loggedInUser, "User logged out.");
            showLoginView();
        });

        // Add both options with a separator
        profileMenu.getItems().addAll(changePassItem, new SeparatorMenuItem(), logoutItem);
        avatarBtn.setOnAction(e -> profileMenu.show(avatarBtn, javafx.geometry.Side.BOTTOM, 0, 5));

        topNav.getChildren().addAll(logoBox, navSpacer, roleBadgeBtn, avatarBtn);
        return topNav;
    }

    private VBox createSidebar(String activeTab) {
        VBox sidebar = new VBox(10);
        sidebar.setPadding(new Insets(20));
        sidebar.setPrefWidth(220);
        sidebar.setStyle("-fx-border-color: #e5e7eb; -fx-border-width: 0 1 0 0; -fx-background-color: white;");

        Label workspaceTitle = new Label("WORKSPACE");
        workspaceTitle.setStyle("-fx-font-size: 10px; -fx-font-weight: bold; -fx-text-fill: #9ca3af;");

        Button coursesNav = new Button("📁 Courses");
        coursesNav.setStyle(activeTab.equals("Courses") 
            ? "-fx-background-color: #fef3c7; -fx-text-fill: #b45309; -fx-font-weight: bold; -fx-alignment: CENTER_LEFT; -fx-max-width: Infinity; -fx-padding: 10px; -fx-background-radius: 6px;"
            : "-fx-background-color: transparent; -fx-text-fill: #374151; -fx-font-weight: bold; -fx-alignment: CENTER_LEFT; -fx-max-width: Infinity; -fx-padding: 10px; -fx-cursor: hand;");
        coursesNav.setOnAction(e -> {
            if (loggedInRole.equals("admin")) {
                showAdminDashboardView();
            } else if (loggedInRole.equals("instructor")) {
                showTeacherDashboardView();
            } else {
                showStudentDashboardView();
            }
        });

        sidebar.getChildren().addAll(workspaceTitle, coursesNav);

        // Add "Things to be Graded" workspace tab specifically for Instructors
        if (loggedInRole.equals("instructor")) {
            Button gradingQueueNav = new Button("📝 To Be Graded");
            gradingQueueNav.setStyle(activeTab.equals("GradingQueue") 
                ? "-fx-background-color: #fef3c7; -fx-text-fill: #b45309; -fx-font-weight: bold; -fx-alignment: CENTER_LEFT; -fx-max-width: Infinity; -fx-padding: 10px; -fx-background-radius: 6px;"
                : "-fx-background-color: transparent; -fx-text-fill: #374151; -fx-font-weight: bold; -fx-alignment: CENTER_LEFT; -fx-max-width: Infinity; -fx-padding: 10px; -fx-cursor: hand;");
            gradingQueueNav.setOnAction(e -> showThingsToBeGradedView());
            
            sidebar.getChildren().add(gradingQueueNav);
        }

        // Add Deadlines Feed for students/instructors
        if (!loggedInRole.equals("admin")) {
            Button deadlinesNav = new Button("📅 Deadlines Feed");
            deadlinesNav.setStyle(activeTab.equals("Deadlines") 
                ? "-fx-background-color: #fef3c7; -fx-text-fill: #b45309; -fx-font-weight: bold; -fx-alignment: CENTER_LEFT; -fx-max-width: Infinity; -fx-padding: 10px; -fx-background-radius: 6px;"
                : "-fx-background-color: transparent; -fx-text-fill: #374151; -fx-font-weight: bold; -fx-alignment: CENTER_LEFT; -fx-max-width: Infinity; -fx-padding: 10px; -fx-cursor: hand;");
            deadlinesNav.setOnAction(e -> showDeadlinesFeedView());
            
            sidebar.getChildren().add(deadlinesNav);
        }

        return sidebar;
    }

    private VBox createSubPageLayout(String title) {
        VBox root = new VBox(20);
        root.setPadding(new Insets(30));
        Button back = new Button("← Back to Dashboard");
        back.setOnAction(e -> {
            if (loggedInRole.equals("admin")) {
                showAdminDashboardView();
            } else if (loggedInRole.equals("instructor")) {
                showTeacherDashboardView();
            } else {
                showStudentDashboardView();
            }
        });
        Label h = new Label(title);
        h.setStyle("-fx-font-size: 22px; -fx-font-weight: bold;");
        root.getChildren().addAll(back, h);
        return root;
    }

    public static void main(String[] args) {
        launch(args);
    }
}