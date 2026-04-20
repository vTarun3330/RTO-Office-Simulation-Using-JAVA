package com.rto.service;

/**
 * Relaxed configuration for CBT test.
 * Intended for users who have multiple failed attempts, reducing the required pass mark.
 * Requires 50% score to pass.
 */
public class RelaxedCBTConfig implements ICBTConfig {
    private static final int PASS_PERCENTAGE = 50;
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
