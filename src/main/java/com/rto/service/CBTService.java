package com.rto.service;

import java.sql.*;
import java.util.*;

/**
 * CBT (Computer-Based Test) Service - Handles Learner's License Test
 * Implements automated quiz evaluation for LL applications
 */
public class CBTService implements IService {
    private DatabaseService db;
    private static final int PASS_PERCENTAGE = 60;
    private static final int QUESTIONS_PER_TEST = 10;

    public CBTService() {
        this.db = DatabaseService.getInstance();
    }

    @Override
    public void initialize() {
        System.out.println("CBTService initialized");
        seedQuestions(); // Populate question bank on first run
    }

    /**
     * Get random questions for CBT
     */
    public List<CBTQuestion> getRandomQuestions(int count) {
        List<CBTQuestion> questions = new ArrayList<>();
        String sql = "SELECT * FROM cbt_questions ORDER BY RAND() LIMIT ?";

        try (ResultSet rs = db.executeQuery(sql, count)) {
            while (rs != null && rs.next()) {
                questions.add(mapResultSetToQuestion(rs));
            }
        } catch (SQLException e) {
            System.err.println("❌ ERROR: Failed to get questions: " + e.getMessage());
        }

        return questions;
    }

    /**
     * Evaluate CBT answers and return result
     * 
     * @param userId         The user taking the test
     * @param answers        Map of questionId to selected answer
     * @param totalQuestions Total number of questions in the test (default 10)
     */
    public CBTResult evaluateTest(String userId, Map<String, String> answers, int totalQuestions) {
        if (userId == null || answers == null) {
            System.err.println("❌ ERROR: Invalid test submission");
            return null;
        }

        int correctCount = 0;

        // Get correct answers from database
        for (String questionId : answers.keySet()) {
            String userAnswer = answers.get(questionId);
            String correctAnswer = getCorrectAnswer(questionId);

            if (userAnswer != null && userAnswer.equals(correctAnswer)) {
                correctCount++;
            }
        }

        // Use provided totalQuestions (handles unanswered questions)
        if (totalQuestions <= 0) {
            totalQuestions = 10; // Default
        }

        // Calculate score percentage
        int scorePercentage = (correctCount * 100) / totalQuestions;
        boolean passed = scorePercentage >= PASS_PERCENTAGE;

        // Save result
        CBTResult result = new CBTResult();
        result.setResultId("CBT-" + System.currentTimeMillis());
        result.setUserId(userId);
        result.setScore(correctCount);
        result.setTotalQuestions(totalQuestions);
        result.setPassed(passed);

        saveResult(result);

        System.out.println(
                passed ? "✅ Test PASSED: " + correctCount + "/" + totalQuestions + " (" + scorePercentage + "%)"
                        : "❌ Test FAILED: " + correctCount + "/" + totalQuestions + " (" + scorePercentage + "%)");

        return result;
    }

    /**
     * Issue Learner's License if test passed
     */
    public boolean issueLearnerLicense(String userId, CBTResult result) {
        if (!result.isPassed()) {
            System.err.println("❌ Cannot issue LL: Test not passed");
            return false;
        }

        try {
            LicenseService licenseService = new LicenseService();
            // Auto-create LL license
            String licenseId = "LL-" + System.currentTimeMillis();

            String sql = """
                    INSERT INTO licenses
                    (license_id, user_id, license_type, status, license_stage, ll_issue_date, test_attempts)
                    VALUES (?, ?, 'LEARNER', 'APPROVED', 'LL', CURRENT_DATE, 1)
                    """;

            boolean success = db.executeUpdate(sql, licenseId, userId, "LEARNER");

            if (success) {
                System.out.println("✅ Learner's License issued: " + licenseId);
            }

            return success;
        } catch (Exception e) {
            System.err.println("❌ ERROR: Failed to issue LL: " + e.getMessage());
            return false;
        }
    }

    // Helper Methods

    private String getCorrectAnswer(String questionId) {
        String sql = "SELECT correct_answer FROM cbt_questions WHERE question_id = ?";
        try (ResultSet rs = db.executeQuery(sql, questionId)) {
            if (rs != null && rs.next()) {
                return rs.getString("correct_answer");
            }
        } catch (SQLException e) {
            System.err.println("❌ ERROR: Failed to get correct answer");
        }
        return null;
    }

    private void saveResult(CBTResult result) {
        String sql = """
                INSERT INTO cbt_results
                (result_id, user_id, score, total_questions, passed, test_date)
                VALUES (?, ?, ?, ?, ?, CURRENT_TIMESTAMP)
                """;

        db.executeUpdate(sql,
                result.getResultId(),
                result.getUserId(),
                result.getScore(),
                result.getTotalQuestions(),
                result.isPassed());
    }

    private CBTQuestion mapResultSetToQuestion(ResultSet rs) throws SQLException {
        CBTQuestion q = new CBTQuestion();
        q.setQuestionId(rs.getString("question_id"));
        q.setQuestionText(rs.getString("question_text"));
        q.setOptionA(rs.getString("option_a"));
        q.setOptionB(rs.getString("option_b"));
        q.setOptionC(rs.getString("option_c"));
        q.setOptionD(rs.getString("option_d"));
        q.setCorrectAnswer(rs.getString("correct_answer"));
        q.setCategory(rs.getString("category"));
        return q;
    }

    /**
     * Get test history for a user
     */
    public List<CBTResult> getTestHistory(String userId) {
        List<CBTResult> history = new ArrayList<>();
        String sql = "SELECT * FROM cbt_results WHERE user_id = ? ORDER BY test_date DESC";

        try (ResultSet rs = db.executeQuery(sql, userId)) {
            while (rs != null && rs.next()) {
                CBTResult result = new CBTResult();
                result.setResultId(rs.getString("result_id"));
                result.setUserId(rs.getString("user_id"));
                result.setScore(rs.getInt("score"));
                result.setTotalQuestions(rs.getInt("total_questions"));
                result.setPassed(rs.getBoolean("passed"));
                result.setTestDate(rs.getTimestamp("test_date"));
                history.add(result);
            }
        } catch (SQLException e) {
            System.err.println("❌ ERROR: Failed to get test history: " + e.getMessage());
        }

        return history;
    }

    /**
     * Seed question bank with sample questions
     */
    private void seedQuestions() {
        // Check if questions already exist
        String checkSql = "SELECT COUNT(*) FROM cbt_questions";
        try (ResultSet rs = db.executeQuery(checkSql)) {
            if (rs != null && rs.next() && rs.getInt(1) > 0) {
                return; // Questions already seeded
            }
        } catch (SQLException e) {
            // Table might not exist yet, continue
        }

        System.out.println("📚 Seeding CBT question bank...");

        String[][] questions = {
                { "Q001", "What does a red traffic light mean?", "Go", "Stop", "Slow down", "Wait", "B",
                        "TRAFFIC_SIGNS" },
                { "Q002", "Maximum speed limit in school zone?", "40 km/h", "50 km/h", "25 km/h", "60 km/h", "C",
                        "SPEED_LIMITS" },
                { "Q003", "When must you use headlights?", "Daytime", "Night time", "Always", "Never", "B",
                        "ROAD_SAFETY" },
                { "Q004", "What does a yellow traffic light indicate?", "Stop", "Go fast", "Prepare to stop",
                        "Turn left", "C", "TRAFFIC_SIGNS" },
                { "Q005", "Minimum age for obtaining a Learner's License?", "16 years", "18 years", "21 years",
                        "25 years", "A", "RULES" },
                { "Q006", "What should you do at a STOP sign?", "Slow down", "Come to complete stop", "Honk",
                        "Speed up", "B", "TRAFFIC_SIGNS" },
                { "Q007", "Wearing helmet is mandatory for?", "Driver only", "Pillion only", "Both", "None", "C",
                        "ROAD_SAFETY" },
                { "Q008", "What does a triangular sign indicate?", "Stop", "Give way", "No entry", "Speed limit", "B",
                        "TRAFFIC_SIGNS" },
                { "Q009", "Safe following distance on highway?", "1 car length", "2 car lengths", "3-4 car lengths",
                        "No distance needed", "C", "ROAD_SAFETY" },
                { "Q010", "What is the legal blood alcohol limit?", "0.03%", "0.05%", "0.00%", "0.08%", "C", "RULES" },
                { "Q011", "Overtaking from left side is?", "Allowed", "Not allowed", "Allowed on highway",
                        "Depends  on road", "B", "ROAD_RULES" },
                { "Q012", "Use of mobile phone while driving is?", "Allowed", "Prohibited", "Allowed handsfree",
                        "Depends", "B", "ROAD_SAFETY" },
                { "Q013", "Pedestrians have right of way at?", "Anywhere", "Zebra crossing", "Roads", "Highways", "B",
                        "ROAD_RULES" },
                { "Q014", "What does a circular sign with red border mean?", "Mandatory", "Prohibitory", "Informatory",
                        "Warning", "B", "TRAFFIC_SIGNS" },
                { "Q015", "Seatbelt is mandatory for?", "Front seat only", "All passengers", "Driver only", "Optional",
                        "B", "ROAD_SAFETY" }
        };

        String insertSql = """
                INSERT INTO cbt_questions
                (question_id, question_text, option_a, option_b, option_c, option_d, correct_answer, category)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                """;

        for (String[] q : questions) {
            db.executeUpdate(insertSql, q[0], q[1], q[2], q[3], q[4], q[5], q[6], q[7]);
        }

        System.out.println("✅ " + questions.length + " questions added to CBT bank");
    }

    // Inner Classes

    public static class CBTQuestion {
        private String questionId;
        private String questionText;
        private String optionA;
        private String optionB;
        private String optionC;
        private String optionD;
        private String correctAnswer;
        private String category;

        // Getters and Setters
        public String getQuestionId() {
            return questionId;
        }

        public void setQuestionId(String questionId) {
            this.questionId = questionId;
        }

        public String getQuestionText() {
            return questionText;
        }

        public void setQuestionText(String questionText) {
            this.questionText = questionText;
        }

        public String getOptionA() {
            return optionA;
        }

        public void setOptionA(String optionA) {
            this.optionA = optionA;
        }

        public String getOptionB() {
            return optionB;
        }

        public void setOptionB(String optionB) {
            this.optionB = optionB;
        }

        public String getOptionC() {
            return optionC;
        }

        public void setOptionC(String optionC) {
            this.optionC = optionC;
        }

        public String getOptionD() {
            return optionD;
        }

        public void setOptionD(String optionD) {
            this.optionD = optionD;
        }

        public String getCorrectAnswer() {
            return correctAnswer;
        }

        public void setCorrectAnswer(String correctAnswer) {
            this.correctAnswer = correctAnswer;
        }

        public String getCategory() {
            return category;
        }

        public void setCategory(String category) {
            this.category = category;
        }
    }

    public static class CBTResult {
        private String resultId;
        private String userId;
        private int score;
        private int totalQuestions;
        private boolean passed;
        private java.sql.Timestamp testDate;

        // Getters and Setters
        public String getResultId() {
            return resultId;
        }

        public void setResultId(String resultId) {
            this.resultId = resultId;
        }

        public String getUserId() {
            return userId;
        }

        public void setUserId(String userId) {
            this.userId = userId;
        }

        public int getScore() {
            return score;
        }

        public void setScore(int score) {
            this.score = score;
        }

        public int getTotalQuestions() {
            return totalQuestions;
        }

        public void setTotalQuestions(int totalQuestions) {
            this.totalQuestions = totalQuestions;
        }

        public boolean isPassed() {
            return passed;
        }

        public void setPassed(boolean passed) {
            this.passed = passed;
        }

        public java.sql.Timestamp getTestDate() {
            return testDate;
        }

        public void setTestDate(java.sql.Timestamp testDate) {
            this.testDate = testDate;
        }

        // Helper method for display
        public String getEligibilityStatus() {
            return passed ? "ELIGIBLE" : "NOT ELIGIBLE";
        }

        // Get score display format (e.g., "7/10 (70%)")
        public String getScoreDisplay() {
            int percentage = totalQuestions > 0 ? (score * 100) / totalQuestions : 0;
            return score + "/" + totalQuestions + " (" + percentage + "%)";
        }

        public String getFormattedDate() {
            if (testDate == null) return "";
            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd hh:mm a");
            return sdf.format(testDate);
        }
    }
}
