package com.rto.service;

/**
 * Configuration that evaluates users based on an absolute number of 
 * correct answers (attempted questions) rather than a percentage.
 */
public class AttemptBasedCBTConfig implements ICBTConfig {
    private static final int REQUIRED_CORRECT_ANSWERS = 7;
    private static final int QUESTIONS_PER_TEST = 10;

    @Override
    public int getQuestionsPerTest() {
        return QUESTIONS_PER_TEST;
    }

    @Override
    public boolean isPassed(int correctCount, int totalQuestions) {
        // Pass if they got a specific absolute number of questions right,
        // ignoring the overall percentage.
        return correctCount >= REQUIRED_CORRECT_ANSWERS;
    }
}
