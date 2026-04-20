package com.rto.service.evaluation;

public class AttemptBasedEvaluationStrategy implements IEvaluationStrategy {
    private final int passPercentage;
    private final int minAttempts;

    public AttemptBasedEvaluationStrategy(int passPercentage, int minAttempts) {
        this.passPercentage = passPercentage;
        this.minAttempts = minAttempts;
    }

    @Override
    public boolean isPassed(int correctCount, int totalQuestions, int attemptedCount) {
        if (attemptedCount < minAttempts) {
            return false;
        }
        return calculateScorePercentage(correctCount, totalQuestions, attemptedCount) >= passPercentage;
    }

    @Override
    public int calculateScorePercentage(int correctCount, int totalQuestions, int attemptedCount) {
        if (attemptedCount <= 0) return 0;
        // Score based on attempted questions rather than total
        return (correctCount * 100) / attemptedCount;
    }
}
