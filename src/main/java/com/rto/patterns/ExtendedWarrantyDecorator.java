package com.rto.patterns;

import com.rto.model.Vehicle;

/**
 * Concrete Decorator - Extended Warranty Feature
 */
public class ExtendedWarrantyDecorator extends VehicleFeatureDecorator {
  private static final double WARRANTY_COST = 8000.0;

  public ExtendedWarrantyDecorator(Vehicle vehicle) {
    super(vehicle);
  }

  @Override
  public double getAdditionalCost() {
    return WARRANTY_COST;
  }

  @Override
  public String getFeatureDescription() {
    return "Extended Warranty (" + WARRANTY_COST + ")";
  }

  @Override
  public String toString() {
    return decoratedVehicle.toString() + " + " + getFeatureDescription();
  }
}
