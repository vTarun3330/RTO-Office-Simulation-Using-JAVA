package com.rto.patterns;

import com.rto.model.Vehicle;

/**
 * Strategy Pattern - Tax Calculation Strategy Interface
 */
public interface TaxCalculationStrategy {
  double calculateTax(Vehicle vehicle);

  String getStrategyName();
}
