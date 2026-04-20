package com.rto.service.evaluation;

public class EvaluationStrategyFactory {
    // This could be enhanced to read from a database or configuration file
    public static IEvaluationStrategy getStrategy(String type) {
        if (type == null) {
            return new StandardEvaluationStrategy(60); // Default 60%
        }
        
        switch (type.toUpperCase()) {
            case "STRICT":
                return new StandardEvaluationStrategy(80); // 80% to pass
            case "ATTEMPT_BASED":
                // e.g. 60% based on attempts, minimum 8 attempts required
                return new AttemptBasedEvaluationStrategy(60, 8); 
            case "STANDARD":
            default:
                return new StandardEvaluationStrategy(60); // 60% standard
        }
    }
}
