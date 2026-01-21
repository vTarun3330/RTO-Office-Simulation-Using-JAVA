package com.rto.patterns;

import com.rto.model.Vehicle;

/**
 * Concrete Strategy - Standard Tax Calculation
 */
public class StandardTaxStrategy implements TaxCalculationStrategy {
  @Override
  public double calculateTax(Vehicle vehicle) {
    // Just use the vehicle's own tax calculation
    return vehicle.calculateTax();
  }

  @Override
  public String getStrategyName() {
    return "STANDARD";
  }
}
