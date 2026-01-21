package com.rto.service;

import com.rto.model.User;
import com.rto.model.Vehicle;
import com.rto.patterns.VehicleBuilder;
import com.rto.patterns.IPaymentProcessor;

// Facade Pattern
public class RTOSystemFacade {
  private SessionManager session;

  public RTOSystemFacade() {
    this.session = SessionManager.getInstance();
  }

  public User login(String name, String password) {
    return DatabaseService.getInstance().authenticate(name, password);
  }

  public Vehicle registerVehicle(String type, String model, String spec) {
    if (!session.isLoggedIn())
      return null;

    Vehicle vehicle = new VehicleBuilder()
        .setOwnerId(session.getCurrentUser().getId())
        .setType(type)
        .setModel(model)
        .setExtraData(spec)
        .build();

    if (DatabaseService.getInstance().registerVehicle(vehicle)) {
      return vehicle;
    }
    return null;
  }

  public boolean payTax(IPaymentProcessor processor, double amount) {
    return processor.processPayment(amount);
  }

  public boolean applyForLicense(String type, String name, String email, String address, String bloodGroup) {
    if (!session.isLoggedIn())
      return false;

    com.rto.model.License license = new com.rto.model.License(
        session.getCurrentUser().getId(),
        type,
        name,
        email,
        address,
        bloodGroup);

    return new LicenseService().applyForLicense(license);
  }
}
