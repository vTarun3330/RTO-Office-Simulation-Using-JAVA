package com.rto.patterns;

import com.rto.model.Vehicle;

/**
 * Concrete Strategy - Premium Tax Calculation
 * Applies higher tax rate for premium/luxury vehicles
 */
public class PremiumTaxStrategy implements TaxCalculationStrategy {
  @Override
  public double calculateTax(Vehicle vehicle) {
    // Premium vehicles pay 50% more tax
    return vehicle.calculateTax() * 1.5;
  }

  @Override
  public String getStrategyName() {
    return "PREMIUM";
  }
}
