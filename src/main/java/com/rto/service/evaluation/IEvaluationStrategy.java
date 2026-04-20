package com.rto.service.evaluation;

/**
 * Strategy interface for evaluating CBT (Computer Based Test) results.
 * Implementing this interface allows applying the Open-Closed Principle (OCP),
 * where new evaluation rules can be added without modifying the existing service.
 */
public interface IEvaluationStrategy {
    
    /**
     * Determines whether the user has passed the test based on the strategy's rules.
     */
    boolean isPassed(int correctCount, int totalQuestions, int attemptedCount);
    
    /**
     * Calculates the score percentage.
     */
    int calculateScorePercentage(int correctCount, int totalQuestions, int attemptedCount);
}
