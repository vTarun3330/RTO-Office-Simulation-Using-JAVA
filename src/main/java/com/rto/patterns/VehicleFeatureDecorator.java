package com.rto.patterns;

import com.rto.model.Vehicle;

/**
 * Decorator Pattern - Simple wrapper approach
 * Adds features like insurance, warranty without extending Vehicle
 * This approach uses composition instead of inheritance for the decorator
 */
public abstract class VehicleFeatureDecorator {
  protected Vehicle decoratedVehicle;

  public VehicleFeatureDecorator(Vehicle vehicle) {
    this.decoratedVehicle = vehicle;
  }

  public Vehicle getDecoratedVehicle() {
    return decoratedVehicle;
  }

  public double calculateTotalCost() {
    return decoratedVehicle.calculateTax() + getAdditionalCost();
  }

  public abstract double getAdditionalCost();

  public abstract String getFeatureDescription();
}
