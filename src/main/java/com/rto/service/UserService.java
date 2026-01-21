package com.rto.service;

import com.rto.model.*;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * User Service - Handles user management operations
 * Factory Pattern used via ServiceFactory
 */
public class UserService implements IService {
  private DatabaseService db;

  public UserService() {
    this.db = DatabaseService.getInstance();
  }

  @Override
  public void initialize() {
    System.out.println("UserService initialized");
  }

  /**
   * Register a new citizen user
   */
  public boolean registerUser(String username, String password, String email) {
    try {
      String userId = "USER-" + System.currentTimeMillis();
      String sql = "INSERT INTO users (id, username, password, role, email) VALUES (?, ?, ?, ?, ?)";
      return db.executeUpdate(sql, userId, username, password, "CITIZEN", email);
    } catch (Exception e) {
      System.err.println("Error registering user: " + e.getMessage());
      return false;
    }
  }

  /**
   * Authenticate user
   */
  public User authenticate(String username, String password) {
    String sql = "SELECT * FROM users WHERE username = ? AND password = ?";
    try (ResultSet rs = db.executeQuery(sql, username, password)) {
      if (rs != null && rs.next()) {
        String role = rs.getString("role");
        String id = rs.getString("id");
        String email = rs.getString("email");

        return switch (role) {
          case "ADMIN" -> new Admin(id, username, password);
          case "RTO_OFFICER" -> new RTOOfficer(id, username, password);
          default -> new Citizen(id, username, password, email);
        };
      }
    } catch (SQLException e) {
      System.err.println("Error authenticating user: " + e.getMessage());
    }
    return null;
  }

  /**
   * Get user by ID
   */
  public User getUserById(String userId) {
    String sql = "SELECT * FROM users WHERE id = ?";
    try (ResultSet rs = db.executeQuery(sql, userId)) {
      if (rs != null && rs.next()) {
        String username = rs.getString("username");
        String password = rs.getString("password");
        String role = rs.getString("role");
        String email = rs.getString("email");

        return switch (role) {
          case "ADMIN" -> new Admin(userId, username, password);
          case "RTO_OFFICER" -> new RTOOfficer(userId, username, password);
          default -> new Citizen(userId, username, password, email);
        };
      }
    } catch (SQLException e) {
      System.err.println("Error getting user by ID: " + e.getMessage());
    }
    return null;
  }

  /**
   * Get all citizens
   */
  public List<User> getAllCitizens() {
    List<User> citizens = new ArrayList<>();
    String sql = "SELECT * FROM users WHERE role = 'CITIZEN'";
    try (ResultSet rs = db.executeQuery(sql)) {
      while (rs != null && rs.next()) {
        citizens.add(new Citizen(
            rs.getString("id"),
            rs.getString("username"),
            rs.getString("password"),
            rs.getString("email")));
      }
    } catch (SQLException e) {
      System.err.println("Error getting all citizens: " + e.getMessage());
    }
    return citizens;
  }

  /**
   * Update user email
   */
  public boolean updateEmail(String userId, String newEmail) {
    String sql = "UPDATE users SET email = ? WHERE id = ?";
    return db.executeUpdate(sql, newEmail, userId);
  }
}
