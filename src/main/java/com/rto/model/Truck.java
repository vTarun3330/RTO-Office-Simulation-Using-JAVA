package com.rto.model;

/**
 * Truck class representing commercial heavy vehicles
 * Demonstrates polymorphism with different tax calculation
 */
public class Truck extends Vehicle {
  private double loadCapacity; // in tons
  private boolean isCommercial;

  public Truck(String ownerId, String model, double loadCapacity) {
    super(ownerId, model, "TRUCK");
    this.loadCapacity = loadCapacity;
    this.isCommercial = true;
  }

  @Override
  public double calculateTax() {
    // Commercial vehicles have higher tax rates
    // Base: 5000, Plus 500 per ton of load capacity
    double baseTax = 5000.0;
    double capacityTax = loadCapacity * 500.0;
    return baseTax + capacityTax;
  }

  public double getLoadCapacity() {
    return loadCapacity;
  }

  public void setLoadCapacity(double loadCapacity) {
    this.loadCapacity = loadCapacity;
  }

  public boolean isCommercial() {
    return isCommercial;
  }

  @Override
  public String toString() {
    return "Truck{" +
        "registrationNumber='" + getRegistrationNumber() + '\'' +
        ", model='" + getModel() + '\'' +
        ", loadCapacity=" + loadCapacity + " tons" +
        '}';
  }
}
