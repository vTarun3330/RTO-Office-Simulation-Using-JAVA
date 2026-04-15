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
    // Input validation
    if (username == null || username.trim().isEmpty()) {
      System.err.println("❌ ERROR: Username cannot be empty");
      return false;
    }
    if (password == null || password.length() < 6) {
      System.err.println("❌ ERROR: Password must be at least 6 characters");
      return false;
    }
    if (email == null || !email.contains("@")) {
      System.err.println("❌ ERROR: Invalid email format");
      return false;
    }

    // Check for duplicate username
    String checkSql = "SELECT COUNT(*) FROM users WHERE username = ?";
    try (ResultSet rs = db.executeQuery(checkSql, username.trim())) {
      if (rs != null && rs.next() && rs.getInt(1) > 0) {
        System.err.println("❌ ERROR: Username '" + username + "' already exists");
        return false;
      }
    } catch (SQLException e) {
      System.err.println("❌ ERROR: Failed to check username: " + e.getMessage());
      return false;
    }

    try {
      String userId = "USER-" + System.currentTimeMillis();
      String sql = "INSERT INTO users (id, username, password, role, email) VALUES (?, ?, ?, ?, ?)";
      boolean success = db.executeUpdate(sql, userId, username.trim(), password, "CITIZEN", email.trim());
      if (success) {
        System.out.println("✅ User registered successfully: " + username);
      }
      return success;
    } catch (Exception e) {
      System.err.println("❌ ERROR: Failed to register user: " + e.getMessage());
      e.printStackTrace();
      return false;
    }
  }

  /**
   * Authenticate user
   */
  public User authenticate(String username, String password) {
    if (username == null || username.trim().isEmpty()) {
      System.err.println("❌ ERROR: Username cannot be empty");
      return null;
    }
    if (password == null || password.isEmpty()) {
      System.err.println("❌ ERROR: Password cannot be empty");
      return null;
    }

    String sql = "SELECT * FROM users WHERE username = ? AND password = ?";
    try (ResultSet rs = db.executeQuery(sql, username.trim(), password)) {
      if (rs != null && rs.next()) {
        String role = rs.getString("role");
        String id = rs.getString("id");
        String email = rs.getString("email");

        User user = switch (role) {
          case "ADMIN" -> new Admin(id, username, password);
          case "RTO_OFFICER" -> new RTOOfficer(id, username, password);
          default -> new Citizen(id, username, password, email);
        };
        System.out.println("✅ Authentication successful for: " + username);
        return user;
      } else {
        System.err.println("⚠️  Authentication failed: Invalid credentials for " + username);
      }
    } catch (SQLException e) {
      System.err.println("❌ ERROR: Database error during authentication: " + e.getMessage());
      e.printStackTrace();
    } catch (Exception e) {
      System.err.println("❌ ERROR: Unexpected error during authentication: " + e.getMessage());
      e.printStackTrace();
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
   * Get user by username
   */
  public User getUserByUsername(String username) {
    String sql = "SELECT * FROM users WHERE username = ?";
    try (ResultSet rs = db.executeQuery(sql, username)) {
      if (rs != null && rs.next()) {
        String id = rs.getString("id");
        String pwd = rs.getString("password");
        String role = rs.getString("role");
        String email = rs.getString("email");

        return switch (role) {
          case "ADMIN" -> new Admin(id, username, pwd);
          case "RTO_OFFICER" -> new RTOOfficer(id, username, pwd);
          default -> new Citizen(id, username, pwd, email);
        };
      }
    } catch (SQLException e) {
      System.err.println("Error getting user by username: " + e.getMessage());
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

  /**
   * Update user info (email, phone, full_name)
   */
  public boolean updateUserInfo(String userId, String email, String phone, String fullName) {
    String sql = "UPDATE users SET email = ?, phone = ?, full_name = ? WHERE id = ?";
    boolean success = db.executeUpdate(sql, email, phone, fullName, userId);
    if (success) {
      System.out.println("✅ User info updated for: " + userId);
    }
    return success;
  }

  /**
   * Delete user and all their associated data (Hard Delete)
   */
  public boolean deleteUser(String userId) {
    if (userId == null || userId.trim().isEmpty()) {
      return false;
    }
    return db.deleteUserCascade(userId);
  }
}
