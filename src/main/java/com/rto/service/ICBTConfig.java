package com.rto.service;

/**
 * Interface for CBT configuration.
 * Adheres to the Open-Closed Principle (OCP) by allowing new 
 * evaluation logic to be added without modifying the CBTService.
 */
public interface ICBTConfig {
    int getQuestionsPerTest();
    boolean isPassed(int correctCount, int totalQuestions);
}
