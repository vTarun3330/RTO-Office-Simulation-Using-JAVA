package com.rto.service;

/**
 * Factory class to instantiate the correct ICBTConfig based on user history or state.
 * Implements the Factory Pattern to encapsulate creation logic.
 */
public class CBTConfigFactory {

    /**
     * Determines which CBT configuration to apply based on the number of attempts.
     * 
     * @param previousAttempts Number of previous test attempts by the user
     * @return ICBTConfig instance specific to the user's situation
     */
    public static ICBTConfig getConfig(int previousAttempts) {
        // If the user has tried many times, switch to the absolute attempt-based configuration
        if (previousAttempts >= 4) {
            return new AttemptBasedCBTConfig();
        }
        // Logic: if user has attempted more than 2 times, relax the passing criteria
        if (previousAttempts >= 2) {
            return new RelaxedCBTConfig();
        }
        return new StandardCBTConfig();
    }
}
