package com.rto.service;

import com.rto.model.*;

import java.sql.*;

/**
 * Database Service - Singleton Pattern
 * Manages H2 database connection and operations
 */
public class DatabaseService {
  private static DatabaseService instance;
  private static final String DB_URL = "jdbc:h2:./rto_db;AUTO_SERVER=TRUE";
  private static final String DB_USER = "sa";
  private static final String DB_PASSWORD = "";

  private Connection connection;

  private DatabaseService() {
    initDatabase();
  }

  public static synchronized DatabaseService getInstance() {
    if (instance == null) {
      instance = new DatabaseService();
    }
    return instance;
  }

  private void initDatabase() {
    try {
      connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
      createTables();
      createDefaultAdmin();
      System.out.println("Database initialized successfully at " + DB_URL);
    } catch (SQLException e) {
      System.err.println("Failed to initialize database: " + e.getMessage());
    }
  }

  private void createTables() throws SQLException {
    String[] createTableStatements = {
        // Users table
        """
            CREATE TABLE IF NOT EXISTS users (
                id VARCHAR(50) PRIMARY KEY,
                username VARCHAR(100) NOT NULL UNIQUE,
                password VARCHAR(100) NOT NULL,
                role VARCHAR(20) NOT NULL,
                email VARCHAR(100),
                full_name VARCHAR(150),
                dob DATE,
                phone VARCHAR(20)
            )
            """,
        // Vehicles table
        """
            CREATE TABLE IF NOT EXISTS vehicles (
                registration_number VARCHAR(50) PRIMARY KEY,
                owner_id VARCHAR(50) NOT NULL,
                model VARCHAR(100) NOT NULL,
                type VARCHAR(20) NOT NULL,
                manufacturing_year INT,
                color VARCHAR(50),
                engine_number VARCHAR(50),
                make VARCHAR(100),
                vin VARCHAR(17),
                tax_status VARCHAR(20) DEFAULT 'PENDING',
                FOREIGN KEY (owner_id) REFERENCES users(id)
            )
            """,
        // Licenses table
        """
            CREATE TABLE IF NOT EXISTS licenses (
                license_id VARCHAR(50) PRIMARY KEY,
                user_id VARCHAR(50) NOT NULL,
                license_type VARCHAR(30) NOT NULL,
                status VARCHAR(20) NOT NULL,
                issue_date DATE,
                expiry_date DATE,
                applicant_name VARCHAR(100),
                applicant_email VARCHAR(100),
                applicant_address VARCHAR(255),
                blood_group VARCHAR(10),
                FOREIGN KEY (user_id) REFERENCES users(id)
            )
            """,
        // Transactions table
        """
            CREATE TABLE IF NOT EXISTS transactions (
                transaction_id VARCHAR(50) PRIMARY KEY,
                user_id VARCHAR(50) NOT NULL,
                amount DECIMAL(10,2) NOT NULL,
                timestamp TIMESTAMP NOT NULL,
                payment_method VARCHAR(30),
                transaction_type VARCHAR(50),
                reference_id VARCHAR(50),
                status VARCHAR(20),
                FOREIGN KEY (user_id) REFERENCES users(id)
            )
            """,
        // Applications table (NEW)
        """
            CREATE TABLE IF NOT EXISTS applications (
                app_id VARCHAR(50) PRIMARY KEY,
                applicant_id VARCHAR(50) NOT NULL,
                app_type VARCHAR(20) NOT NULL,
                status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
                remarks VARCHAR(500),
                submission_date TIMESTAMP NOT NULL,
                reference_id VARCHAR(50),
                FOREIGN KEY (applicant_id) REFERENCES users(id)
            )
            """,
        // Challans table (NEW)
        """
            CREATE TABLE IF NOT EXISTS challans (
                challan_id VARCHAR(50) PRIMARY KEY,
                vehicle_vin VARCHAR(50) NOT NULL,
                offense_type VARCHAR(100) NOT NULL,
                amount DECIMAL(10,2) NOT NULL,
                issue_date DATE NOT NULL,
                is_paid BOOLEAN DEFAULT FALSE,
                issued_by VARCHAR(50),
                payment_transaction_id VARCHAR(50)
            )
            """
    };

    try (Statement stmt = connection.createStatement()) {
      for (String sql : createTableStatements) {
        stmt.execute(sql);
      }
      System.out.println("Database tables created successfully");
    }
  }

  private void createDefaultAdmin() {
    try {
      String checkSql = "SELECT COUNT(*) FROM users WHERE username = 'admin'";
      try (Statement stmt = connection.createStatement();
          ResultSet rs = stmt.executeQuery(checkSql)) {
        rs.next();
        if (rs.getInt(1) == 0) {
          String insertSql = "INSERT INTO users (id, username, password, role, email) VALUES (?, ?, ?, ?, ?)";
          try (PreparedStatement pstmt = connection.prepareStatement(insertSql)) {
            pstmt.setString(1, "admin-1");
            pstmt.setString(2, "admin");
            pstmt.setString(3, "admin123");
            pstmt.setString(4, "ADMIN");
            pstmt.setString(5, "admin@rto.gov.in");
            pstmt.executeUpdate();
            System.out.println("Default admin created: admin/admin123");
          }
          String insertSqlUser = "INSERT INTO users (id, username, password, role, email) VALUES (?, ?, ?, ?, ?)";
          try (PreparedStatement pstmt = connection.prepareStatement(insertSqlUser)) {
            pstmt.setString(1, "user-1");
            pstmt.setString(2, "user");
            pstmt.setString(3, "user123");
            pstmt.setString(4, "CIITZEN");
            pstmt.setString(5, "user@test.com");
            pstmt.executeUpdate();
            System.out.println("Default user created: user/user123");
          }
        }
      }
    } catch (SQLException e) {
      System.err.println("Error creating default admin: " + e.getMessage());
    }
  }

  public User authenticate(String username, String password) {
    String sql = "SELECT * FROM users WHERE username = ? AND password = ?";
    try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
      pstmt.setString(1, username);
      pstmt.setString(2, password);
      try (ResultSet rs = pstmt.executeQuery()) {
        if (rs.next()) {
          String role = rs.getString("role");
          String id = rs.getString("id");
          String email = rs.getString("email");

          if ("ADMIN".equalsIgnoreCase(role)) {
            return new Admin(id, username, password);
          } else {
            return new Citizen(id, username, password, email);
          }
        }
      }
    } catch (SQLException e) {
      System.err.println("Authentication error: " + e.getMessage());
    }
    return null;
  }

  public Connection getConnection() {
    return connection;
  }

  // Generic CRUD operations
  public boolean executeUpdate(String sql, Object... params) {
    try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
      for (int i = 0; i < params.length; i++) {
        pstmt.setObject(i + 1, params[i]);
      }
      pstmt.executeUpdate();
      return true;
    } catch (SQLException e) {
      System.err.println("Error executing update: " + e.getMessage());
      return false;
    }
  }

  public ResultSet executeQuery(String sql, Object... params) {
    try {
      PreparedStatement pstmt = connection.prepareStatement(sql);
      for (int i = 0; i < params.length; i++) {
        pstmt.setObject(i + 1, params[i]);
      }
      return pstmt.executeQuery();
    } catch (SQLException e) {
      System.err.println("Error executing query: " + e.getMessage());
      return null;
    }
  }

  public boolean registerVehicle(Vehicle vehicle) {
    String sql = "INSERT INTO vehicles (registration_number, owner_id, model, type, manufacturing_year, color, engine_number) VALUES (?, ?, ?, ?, ?, ?, ?)";
    try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
      pstmt.setString(1, vehicle.getRegistrationNumber());
      pstmt.setString(2, vehicle.getOwnerId());
      pstmt.setString(3, vehicle.getModel());
      pstmt.setString(4, vehicle.getType());
      pstmt.setInt(5, vehicle.getManufacturingYear());
      pstmt.setString(6, vehicle.getColor());
      pstmt.setString(7, vehicle.getEngineNumber());
      pstmt.executeUpdate();
      return true;
    } catch (SQLException e) {
      System.err.println("Error registering vehicle: " + e.getMessage());
      return false;
    }
  }

  public void close() {
    try {
      if (connection != null && !connection.isClosed()) {
        connection.close();
        System.out.println("Database connection closed");
      }
    } catch (SQLException e) {
      System.err.println("Error closing database connection: " + e.getMessage());
    }
  }
}
