package com.rto.patterns;

import com.rto.model.Vehicle;

/**
 * Concrete Decorator - Insurance Feature
 */
public class InsuranceDecorator extends VehicleFeatureDecorator {
  private static final double INSURANCE_COST = 15000.0;

  public InsuranceDecorator(Vehicle vehicle) {
    super(vehicle);
  }

  @Override
  public double getAdditionalCost() {
    return INSURANCE_COST;
  }

  @Override
  public String getFeatureDescription() {
    return "Comprehensive Insurance (" + INSURANCE_COST + ")";
  }

  @Override
  public String toString() {
    return decoratedVehicle.toString() + " + " + getFeatureDescription();
  }
}
