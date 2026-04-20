package com.rto.service;

/**
 * Standard configuration for CBT test.
 * Requires 60% score to pass.
 */
public class StandardCBTConfig implements ICBTConfig {
    private static final int PASS_PERCENTAGE = 60;
    private static final int QUESTIONS_PER_TEST = 10;

    @Override
    public int getQuestionsPerTest() {
        return QUESTIONS_PER_TEST;
    }

    @Override
    public boolean isPassed(int correctCount, int totalQuestions) {
        if (totalQuestions <= 0) {
            totalQuestions = getQuestionsPerTest();
        }
        int scorePercentage = (correctCount * 100) / totalQuestions;
        return scorePercentage >= PASS_PERCENTAGE;
    }
}
