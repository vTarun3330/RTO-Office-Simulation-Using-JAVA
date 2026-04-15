package com.rto.controller;

import com.rto.service.*;
import com.rto.service.CBTService.*;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.util.Duration;

import java.util.*;

/**
 * CBT Controller - Computer Based Test for Learner's License
 * Features: 10-question quiz, timer, auto-grading, test history
 */
public class CBTController {

    @FXML
    private VBox quizContainer;
    @FXML
    private Label timerLabel;
    @FXML
    private Label questionCountLabel;
    @FXML
    private Button submitButton;
    @FXML
    private VBox resultContainer;
    @FXML
    private Label resultLabel;
    @FXML
    private Label scoreLabel;

    private CBTService cbtService;
    private SessionManager session;
    private List<CBTQuestion> questions;
    private Map<String, RadioButton> selectedAnswers;
    private Timeline timer;
    private int timeRemaining = 600; // 10 minutes in seconds
    private boolean testStarted = false;

    public void initialize() {
        cbtService = new CBTService();
        cbtService.initialize(); // Seed questions
        session = SessionManager.getInstance();
        selectedAnswers = new HashMap<>();

        resultContainer.setVisible(false);
        resultContainer.managedProperty().bind(resultContainer.visibleProperty());
        
        submitButton.setVisible(false);
        submitButton.managedProperty().bind(submitButton.visibleProperty());
        timerLabel.setText("Time: --:--");
        questionCountLabel.setText("");

        // Show welcome screen with Start button and history
        showWelcomeScreen();
    }

    private void showWelcomeScreen() {
        quizContainer.getChildren().clear();
        quizContainer.setAlignment(Pos.TOP_CENTER);

        // Welcome header
        Label welcomeLabel = new Label("📋 Learner's License Test");
        welcomeLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-padding: 20;");

        // Instructions
        VBox instructionsBox = new VBox(10);
        instructionsBox.setStyle(
                "-fx-padding: 20; -fx-background-color: #e8f4fd; -fx-border-color: #3498db; -fx-border-radius: 5;");
        instructionsBox.setMaxWidth(600);

        Label instructionsTitle = new Label("📌 Test Instructions:");
        instructionsTitle.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        Label instructions = new Label(
                "• The test consists of 10 multiple choice questions\n" +
                        "• You have 10 minutes to complete the test\n" +
                        "• You need at least 60% to pass (6/10 correct)\n" +
                        "• If you pass, your Learner's License will be automatically issued\n" +
                        "• You can retake the test if you fail");
        instructions.setStyle("-fx-font-size: 14px;");
        instructions.setWrapText(true);

        instructionsBox.getChildren().addAll(instructionsTitle, instructions);

        // Start button
        Button startButton = new Button("🚀 Start Test");
        startButton.setStyle(
                "-fx-background-color: #27ae60; -fx-text-fill: white; -fx-font-size: 18px; -fx-padding: 15 40; -fx-cursor: hand;");
        startButton.setOnAction(e -> startTest());

        // Test History section
        VBox historyBox = new VBox(10);
        historyBox.setStyle("-fx-padding: 20;");
        historyBox.setMaxWidth(700);

        Label historyTitle = new Label("📊 Your Previous Test Results:");
        historyTitle.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        TableView<CBTResult> historyTable = new TableView<>();
        historyTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        historyTable.setPrefHeight(150); // Preferred height, will grow if needed

        TableColumn<CBTResult, String> dateCol = new TableColumn<>("Test Date");
        dateCol.setCellValueFactory(new PropertyValueFactory<>("formattedDate"));
        dateCol.setStyle("-fx-alignment: CENTER;");

        TableColumn<CBTResult, Integer> scoreCol = new TableColumn<>("Score");
        scoreCol.setCellValueFactory(new PropertyValueFactory<>("score"));
        scoreCol.setStyle("-fx-alignment: CENTER;");

        TableColumn<CBTResult, Integer> totalCol = new TableColumn<>("Total");
        totalCol.setCellValueFactory(new PropertyValueFactory<>("totalQuestions"));
        totalCol.setStyle("-fx-alignment: CENTER;");

        TableColumn<CBTResult, String> eligibilityCol = new TableColumn<>("LL Eligibility (Score/Total)");
        eligibilityCol.setCellValueFactory(new PropertyValueFactory<>("scoreDisplay"));
        eligibilityCol.setStyle("-fx-alignment: CENTER;");
        eligibilityCol.setCellFactory(col -> new TableCell<CBTResult, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    // Check if passed (score >= 60%)
                    CBTResult result = getTableRow().getItem();
                    if (result != null && result.isPassed()) {
                        setStyle("-fx-text-fill: green; -fx-font-weight: bold; -fx-alignment: CENTER;");
                    } else {
                        setStyle("-fx-text-fill: red; -fx-font-weight: bold; -fx-alignment: CENTER;");
                    }
                }
            }
        });

        historyTable.getColumns().addAll(dateCol, eligibilityCol);

        // Load test history
        String userId = session.getCurrentUser().getId();
        List<CBTResult> history = cbtService.getTestHistory(userId);

        if (history.isEmpty()) {
            Label noHistory = new Label("No previous test attempts found.");
            noHistory.setStyle("-fx-text-fill: #7F8C8D; -fx-font-style: italic;");
            historyBox.getChildren().addAll(historyTitle, noHistory);
        } else {
            historyTable.setItems(FXCollections.observableArrayList(history));
            historyBox.getChildren().addAll(historyTitle, historyTable);
        }

        quizContainer.getChildren().addAll(welcomeLabel, instructionsBox, historyBox, startButton);
    }

    private void startTest() {
        testStarted = true;
        selectedAnswers.clear(); // Important: clear old answers before new test
        submitButton.setVisible(true);
        loadQuiz();
        startTimer();
    }

    private void loadQuiz() {
        questions = cbtService.getRandomQuestions(10);

        if (questions.isEmpty()) {
            showError("Failed to load quiz. Please contact administrator.");
            return;
        }

        questionCountLabel.setText("Total Questions: " + questions.size());

        quizContainer.getChildren().clear();
        quizContainer.setAlignment(Pos.TOP_LEFT);

        for (int i = 0; i < questions.size(); i++) {
            CBTQuestion q = questions.get(i);
            VBox questionBox = createQuestionUI(q, i + 1);
            quizContainer.getChildren().add(questionBox);
        }
    }

    private VBox createQuestionUI(CBTQuestion question, int questionNumber) {
        VBox box = new VBox(10);
        box.setStyle("-fx-padding: 15; -fx-background-color: #f8f9fa; -fx-border-color: #dee2e6; -fx-border-width: 1;");

        Label questionLabel = new Label(questionNumber + ". " + question.getQuestionText());
        questionLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");
        questionLabel.setWrapText(true);

        ToggleGroup group = new ToggleGroup();

        RadioButton optA = new RadioButton("A. " + question.getOptionA());
        optA.setToggleGroup(group);
        optA.setUserData("A");

        RadioButton optB = new RadioButton("B. " + question.getOptionB());
        optB.setToggleGroup(group);
        optB.setUserData("B");

        RadioButton optC = new RadioButton("C. " + question.getOptionC());
        optC.setToggleGroup(group);
        optC.setUserData("C");

        RadioButton optD = new RadioButton("D. " + question.getOptionD());
        optD.setToggleGroup(group);
        optD.setUserData("D");

        // Track selection
        group.selectedToggleProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                selectedAnswers.put(question.getQuestionId(), (RadioButton) newVal);
            }
        });

        box.getChildren().addAll(questionLabel, optA, optB, optC, optD);
        return box;
    }

    private void startTimer() {
        timeRemaining = 600; // Reset timer
        updateTimerDisplay();

        timer = new Timeline(new KeyFrame(Duration.seconds(1), event -> {
            timeRemaining--;
            updateTimerDisplay();

            if (timeRemaining <= 0) {
                timer.stop();
                handleAutoSubmit();
            }
        }));
        timer.setCycleCount(Timeline.INDEFINITE);
        timer.play();
    }

    private void updateTimerDisplay() {
        int minutes = timeRemaining / 60;
        int seconds = timeRemaining % 60;
        timerLabel.setText(String.format("Time Remaining: %02d:%02d", minutes, seconds));

        if (timeRemaining < 60) {
            timerLabel.setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
        } else {
            timerLabel.setStyle("-fx-text-fill: white; -fx-font-size: 16px;");
        }
    }

    @FXML
    private void handleSubmit() {
        if (!testStarted) {
            return; // Ignore if test hasn't started
        }

        if (selectedAnswers.size() < questions.size()) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Incomplete Test");
            alert.setHeaderText("You have not answered all questions!");
            alert.setContentText("Do you want to submit anyway?");

            Optional<ButtonType> result = alert.showAndWait();
            if (result.isEmpty() || result.get() != ButtonType.OK) {
                return;
            }
        }

        submitTest();
    }

    private void handleAutoSubmit() {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Time's Up!");
        alert.setHeaderText("The test time has expired.");
        alert.setContentText("Your test will be submitted automatically.");
        alert.showAndWait();

        submitTest();
    }

    private void submitTest() {
        if (timer != null) {
            timer.stop();
        }

        // Prepare answers map
        Map<String, String> answers = new HashMap<>();
        for (Map.Entry<String, RadioButton> entry : selectedAnswers.entrySet()) {
            String questionId = entry.getKey();
            String answer = (String) entry.getValue().getUserData();
            answers.put(questionId, answer);
        }

        // Evaluate - pass the actual total questions (10)
        // (If answers were empty, evaluateTest will handle calculating a zero score properly and save it)
        String userId = session.getCurrentUser().getId();
        CBTResult result = cbtService.evaluateTest(userId, answers, questions.size());

        if (result != null) {
            displayResult(result);

            // Auto-issue LL if passed
            if (result.isPassed()) {
                cbtService.issueLearnerLicense(userId, result);
            }
        } else {
            // Fallback - show error result
            showError("Failed to evaluate test. Please try again.");
        }
    }

    private void displayResult(CBTResult result) {
        quizContainer.setVisible(false);
        submitButton.setVisible(false);
        timerLabel.setVisible(false);
        resultContainer.setVisible(true);

        if (result.isPassed()) {
            resultLabel.setText("✅ CONGRATULATIONS! You PASSED!");
            resultLabel.setStyle("-fx-text-fill: green; -fx-font-size: 24px; -fx-font-weight: bold;");

            scoreLabel.setText(String.format("Your Score: %d/%d (%d%%)\n\n" +
                    "Your Learner's License has been issued!\n" +
                    "You can apply for a Driving License after 30 days.",
                    result.getScore(), result.getTotalQuestions(),
                    (result.getScore() * 100) / result.getTotalQuestions()));
        } else {
            resultLabel.setText("❌ Test Failed");
            resultLabel.setStyle("-fx-text-fill: red; -fx-font-size: 24px; -fx-font-weight: bold;");

            scoreLabel.setText(String.format("Your Score: %d/%d (%d%%)\n\n" +
                    "You need at least 60%% to pass.\n" +
                    "Please try again after reviewing the traffic rules.",
                    result.getScore(), result.getTotalQuestions(),
                    (result.getScore() * 100) / result.getTotalQuestions()));
        }

        scoreLabel.setAlignment(Pos.CENTER);
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @FXML
    private void handleClose() {
        // Return to the CBT dashboard/history view instead of closing the app
        resultContainer.setVisible(false);
        quizContainer.setVisible(true);
        timerLabel.setVisible(true);
        testStarted = false;
        timerLabel.setText("Time: --:--");
        questionCountLabel.setText("");
        showWelcomeScreen();
    }
}
