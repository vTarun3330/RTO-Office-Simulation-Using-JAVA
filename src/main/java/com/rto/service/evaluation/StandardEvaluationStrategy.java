package com.rto.service.evaluation;

public class StandardEvaluationStrategy implements IEvaluationStrategy {
    private final int passPercentage;

    public StandardEvaluationStrategy(int passPercentage) {
        this.passPercentage = passPercentage;
    }

    @Override
    public boolean isPassed(int correctCount, int totalQuestions, int attemptedCount) {
        return calculateScorePercentage(correctCount, totalQuestions, attemptedCount) >= passPercentage;
    }

    @Override
    public int calculateScorePercentage(int correctCount, int totalQuestions, int attemptedCount) {
        if (totalQuestions <= 0) return 0;
        return (correctCount * 100) / totalQuestions;
    }
}
