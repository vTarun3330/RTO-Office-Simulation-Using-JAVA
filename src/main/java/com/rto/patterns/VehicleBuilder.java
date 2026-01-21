package com.rto.patterns;

import com.rto.model.Vehicle;
import com.rto.model.Car;
import com.rto.model.Bike;

// Builder Pattern to construct complex Vehicle objects
public class VehicleBuilder {
  private String ownerId;
  private String model;
  private String type;
  private String extraData; // FuelType or CC

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

  public Vehicle build() {
    if ("CAR".equalsIgnoreCase(type)) {
      return new Car(ownerId, model, extraData);
    } else if ("BIKE".equalsIgnoreCase(type)) {
      return new Bike(ownerId, model, Integer.parseInt(extraData));
    }
    return null;
  }
}
