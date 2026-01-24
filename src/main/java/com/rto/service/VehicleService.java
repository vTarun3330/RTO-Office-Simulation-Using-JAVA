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
    if (vehicle == null) {
      System.err.println("❌ ERROR: Cannot register null vehicle");
      return false;
    }

    if (!vehicle.isValid()) {
      System.err.println("❌ ERROR: Invalid vehicle data:");
      System.err.println("   - Owner ID: " + (vehicle.getOwnerId() == null ? "MISSING" : "OK"));
      System.err.println("   - Model: " + (vehicle.getModel() == null || vehicle.getModel().isEmpty() ? "MISSING" : "OK"));
      System.err.println("   - Year: " + (vehicle.getManufacturingYear() <= 1900 ? "INVALID" : "OK"));
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

    boolean success = db.executeUpdate(sql,
        vehicle.getRegistrationNumber(),
        vehicle.getOwnerId(),
        vehicle.getModel(),
        vehicle.getType(),
        vehicle.getManufacturingYear(),
        vehicle.getColor(),
        vehicle.getEngineNumber());
        
    if (success) {
      System.out.println("✅ Vehicle registered successfully: " + regNumber);
    } else {
      System.err.println("❌ ERROR: Failed to register vehicle in database");
    }
    return success;
  }

  /**
   * Get vehicle by registrationNumber
   */
  public Vehicle getVehicleByRegistration(String registrationNumber) {
    if (registrationNumber == null || registrationNumber.trim().isEmpty()) {
      System.err.println("❌ ERROR: Registration number cannot be null or empty");
      return null;
    }
    
    String sql = "SELECT * FROM vehicles WHERE registration_number = ?";
    try (ResultSet rs = db.executeQuery(sql, registrationNumber.trim())) {
      if (rs != null && rs.next()) {
        Vehicle v = mapResultSetToVehicle(rs);
        if (v != null) {
          System.out.println("✅ Found vehicle: " + registrationNumber);
        }
        return v;
      } else {
        System.out.println("⚠️  No vehicle found with registration: " + registrationNumber);
      }
    } catch (SQLException e) {
      System.err.println("❌ ERROR: Database error while fetching vehicle: " + e.getMessage());
      e.printStackTrace();
    } catch (Exception e) {
      System.err.println("❌ ERROR: Unexpected error: " + e.getMessage());
      e.printStackTrace();
    }
    return null;
  }

  /**
   * Get all vehicles for a user
   */
  public List<Vehicle> getVehiclesByOwnerId(String ownerId) {
    List<Vehicle> vehicles = new ArrayList<>();
    
    if (ownerId == null || ownerId.trim().isEmpty()) {
      System.err.println("❌ ERROR: Owner ID cannot be null or empty");
      return vehicles; // Return empty list instead of null
    }
    
    String sql = "SELECT * FROM vehicles WHERE owner_id = ?";
    try (ResultSet rs = db.executeQuery(sql, ownerId.trim())) {
      while (rs != null && rs.next()) {
        Vehicle v = mapResultSetToVehicle(rs);
        if (v != null) {
          vehicles.add(v);
        }
      }
      System.out.println("✅ Found " + vehicles.size() + " vehicle(s) for owner: " + ownerId);
    } catch (SQLException e) {
      System.err.println("❌ ERROR: Failed to get vehicles for owner: " + e.getMessage());
      e.printStackTrace();
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
