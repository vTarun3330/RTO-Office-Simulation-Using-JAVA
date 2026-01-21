package com.rto.patterns;

import com.rto.model.Vehicle;

/**
 * Concrete Strategy - Commercial Tax Calculation
 * Special tax rates for commercial vehicles
 */
public class CommercialTaxStrategy implements TaxCalculationStrategy {
  @Override
  public double calculateTax(Vehicle vehicle) {
    // Commercial vehicles pay double tax
    return vehicle.calculateTax() * 2.0;
  }

  @Override
  public String getStrategyName() {
    return "COMMERCIAL";
  }
}
