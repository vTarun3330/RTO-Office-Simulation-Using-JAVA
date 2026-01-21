package com.rto.service;

import com.rto.model.*;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Vehicle Service - Handles vehicle registration and management
 * Demonstrates Builder Pattern via VehicleBuilder
 */
public class VehicleService implements IService {
  private DatabaseService db;

  public VehicleService() {
    this.db = DatabaseService.getInstance();
  }

  @Override
  public void initialize() {
    System.out.println("VehicleService initialized");
  }

  /**
   * Register a new vehicle
   */
  public boolean registerVehicle(Vehicle vehicle) {
    if (!vehicle.isValid()) {
      System.out.println("Attempted to register invalid vehicle");
      return false;
    }

    // Generate registration number
    String regNumber = generateRegistrationNumber(vehicle.getType());
    vehicle.setRegistrationNumber(regNumber);

    String sql = """
        INSERT INTO vehicles
        (registration_number, owner_id, model, type, manufacturing_year, color, engine_number)
        VALUES (?, ?, ?, ?, ?, ?, ?)
        """;

    return db.executeUpdate(sql,
        vehicle.getRegistrationNumber(),
        vehicle.getOwnerId(),
        vehicle.getModel(),
        vehicle.getType(),
        vehicle.getManufacturingYear(),
        vehicle.getColor(),
        vehicle.getEngineNumber());
  }

  /**
   * Get vehicle by registrationNumber
   */
  public Vehicle getVehicleByRegistration(String registrationNumber) {
    String sql = "SELECT * FROM vehicles WHERE registration_number = ?";
    try (ResultSet rs = db.executeQuery(sql, registrationNumber)) {
      if (rs != null && rs.next()) {
        return mapResultSetToVehicle(rs);
      }
    } catch (SQLException e) {
      System.err.println("Error getting vehicle: " + e.getMessage());
    }
    return null;
  }

  /**
   * Get all vehicles for a user
   */
  public List<Vehicle> getVehiclesByOwnerId(String ownerId) {
    List<Vehicle> vehicles = new ArrayList<>();
    String sql = "SELECT * FROM vehicles WHERE owner_id = ?";
    try (ResultSet rs = db.executeQuery(sql, ownerId)) {
      while (rs != null && rs.next()) {
        vehicles.add(mapResultSetToVehicle(rs));
      }
    } catch (SQLException e) {
      System.err.println("Error getting vehicles for owner: " + e.getMessage());
    }
    return vehicles;
  }

  /**
   * Get all vehicles
   */
  public List<Vehicle> getAllVehicles() {
    List<Vehicle> vehicles = new ArrayList<>();
    String sql = "SELECT * FROM vehicles";
    try (ResultSet rs = db.executeQuery(sql)) {
      while (rs != null && rs.next()) {
        vehicles.add(mapResultSetToVehicle(rs));
      }
    } catch (SQLException e) {
      System.err.println("Error getting all vehicles: " + e.getMessage());
    }
    return vehicles;
  }

  /**
   * Search vehicles by model
   */
  public List<Vehicle> searchVehiclesByModel(String model) {
    List<Vehicle> vehicles = new ArrayList<>();
    String sql = "SELECT * FROM vehicles WHERE LOWER(model) LIKE ?";
    try (ResultSet rs = db.executeQuery(sql, "%" + model.toLowerCase() + "%")) {
      while (rs != null && rs.next()) {
        vehicles.add(mapResultSetToVehicle(rs));
      }
    } catch (SQLException e) {
      System.err.println("Error searching vehicles: " + e.getMessage());
    }
    return vehicles;
  }

  /**
   * Delete a vehicle
   */
  public boolean deleteVehicle(String registrationNumber) {
    String sql = "DELETE FROM vehicles WHERE registration_number = ?";
    return db.executeUpdate(sql, registrationNumber);
  }

  /**
   * Update vehicle color
   */
  public boolean updateVehicleColor(String registrationNumber, String newColor) {
    String sql = "UPDATE vehicles SET color = ? WHERE registration_number = ?";
    return db.executeUpdate(sql, newColor, registrationNumber);
  }

  private String generateRegistrationNumber(String type) {
    String prefix = switch (type.toUpperCase()) {
      case "CAR" -> "KA-01-C";
      case "BIKE" -> "KA-01-B";
      case "TRUCK" -> "KA-01-T";
      default -> "KA-01-X";
    };
    return prefix + "-" + (1000 + (int) (Math.random() * 9000));
  }

  private Vehicle mapResultSetToVehicle(ResultSet rs) throws SQLException {
    String type = rs.getString("type");
    String ownerId = rs.getString("owner_id");
    String model = rs.getString("model");

    Vehicle vehicle = switch (type.toUpperCase()) {
      case "CAR" -> new Car(ownerId, model, "PETROL");
      case "BIKE" -> new Bike(ownerId, model, 150);
      case "TRUCK" -> new Truck(ownerId, model, 5.0);
      default -> null;
    };

    if (vehicle != null) {
      vehicle.setRegistrationNumber(rs.getString("registration_number"));
      vehicle.setManufacturingYear(rs.getInt("manufacturing_year"));
      vehicle.setColor(rs.getString("color"));
      vehicle.setEngineNumber(rs.getString("engine_number"));
    }

    return vehicle;
  }
}
