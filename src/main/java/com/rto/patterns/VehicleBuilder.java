package com.rto.patterns;

import com.rto.model.Vehicle;
import com.rto.model.Car;
import com.rto.model.Bike;
import com.rto.model.Truck;

/**
 * Builder Pattern to construct complex Vehicle objects.
 * Supports Car, Bike, and Truck types with fluent API.
 */
public class VehicleBuilder {
  private String ownerId;
  private String model;
  private String type;
  private String extraData; // FuelType, CC, or Load Capacity
  private String color;
  private int manufacturingYear;
  private String engineNumber;

  public VehicleBuilder setOwnerId(String ownerId) {
    this.ownerId = ownerId;
    return this;
  }

  public VehicleBuilder setModel(String model) {
    this.model = model;
    return this;
  }

  public VehicleBuilder setType(String type) {
    this.type = type;
    return this;
  }

  public VehicleBuilder setExtraData(String data) {
    this.extraData = data;
    return this;
  }

  public VehicleBuilder setColor(String color) {
    this.color = color;
    return this;
  }

  public VehicleBuilder setManufacturingYear(int year) {
    this.manufacturingYear = year;
    return this;
  }

  public VehicleBuilder setEngineNumber(String engineNumber) {
    this.engineNumber = engineNumber;
    return this;
  }

  public Vehicle build() {
    Vehicle vehicle = switch (type.toUpperCase()) {
      case "CAR" -> new Car(ownerId, model, extraData != null ? extraData : "PETROL");
      case "BIKE" -> new Bike(ownerId, model, parseIntSafe(extraData, 150));
      case "TRUCK" -> new Truck(ownerId, model, parseDoubleSafe(extraData, 5.0));
      default -> throw new IllegalArgumentException("Unknown vehicle type: " + type);
    };

    if (color != null)
      vehicle.setColor(color);
    if (manufacturingYear > 0)
      vehicle.setManufacturingYear(manufacturingYear);
    if (engineNumber != null)
      vehicle.setEngineNumber(engineNumber);

    return vehicle;
  }

  private int parseIntSafe(String value, int defaultValue) {
    try {
      return value != null ? Integer.parseInt(value) : defaultValue;
    } catch (NumberFormatException e) {
      return defaultValue;
    }
  }

  private double parseDoubleSafe(String value, double defaultValue) {
    try {
      return value != null ? Double.parseDouble(value) : defaultValue;
    } catch (NumberFormatException e) {
      return defaultValue;
    }
  }
}
