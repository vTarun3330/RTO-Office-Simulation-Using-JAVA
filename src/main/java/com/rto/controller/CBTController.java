package com.rto.controller;

import com.rto.service.*;
import com.rto.service.CBTService.*;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

import java.util.*;

/**
 * CBT Controller - Computer Based Test for Learner's License
 * Features: 10-question quiz, timer, auto-grading
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
    
    public void initialize() {
        cbtService = new CBTService();
        cbtService.initialize(); // Seed questions
        session = SessionManager.getInstance();
        selectedAnswers = new HashMap<>();
        
        resultContainer.setVisible(false);
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
        }
    }
    
    @FXML
    private void handleSubmit() {
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
        timer.stop();
        
        // Prepare answers map
        Map<String, String> answers = new HashMap<>();
        for (Map.Entry<String, RadioButton> entry : selectedAnswers.entrySet()) {
            String questionId = entry.getKey();
            String answer = (String) entry.getValue().getUserData();
            answers.put(questionId, answer);
        }
        
        // Evaluate
        String userId = session.getCurrentUser().getId();
        CBTResult result = cbtService.evaluateTest(userId, answers);
        
        if (result != null) {
            displayResult(result);
            
            // Auto-issue LL if passed
            if (result.isPassed()) {
                cbtService.issueLearnerLicense(userId, result);
            }
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
        // Close the quiz window or return to dashboard
        quizContainer.getScene().getWindow().hide();
    }
}
