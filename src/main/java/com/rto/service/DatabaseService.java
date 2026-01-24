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
      if (connection == null || connection.isClosed()) {
        throw new SQLException("Failed to establish database connection");
      }
      createTables();
      createDefaultAdmin();
      System.out.println("✅ Database initialized successfully at " + DB_URL);
    } catch (SQLException e) {
      System.err.println("❌ CRITICAL: Failed to initialize database: " + e.getMessage());
      System.err.println("   Please ensure H2 database is accessible and no other instance is running.");
      e.printStackTrace();
      throw new RuntimeException("Database initialization failed", e);
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
            """,
        // Phase 1: Hypothecation tracking
        """
            CREATE TABLE IF NOT EXISTS hypothecations (
                id VARCHAR(50) PRIMARY KEY,
                vehicle_vin VARCHAR(50) NOT NULL,
                bank_name VARCHAR(100) NOT NULL,
                loan_account VARCHAR(50),
                loan_amount DECIMAL(10,2),
                start_date DATE,
                is_active BOOLEAN DEFAULT TRUE,
                noc_issued BOOLEAN DEFAULT FALSE,
                noc_date DATE
            )
            """,
        // Phase 1: Transfer requests
        """
            CREATE TABLE IF NOT EXISTS transfer_requests (
                transfer_id VARCHAR(50) PRIMARY KEY,
                vehicle_vin VARCHAR(50) NOT NULL,
                seller_id VARCHAR(50) NOT NULL,
                buyer_id VARCHAR(50),
                buyer_mobile VARCHAR(20),
                transfer_token VARCHAR(20) UNIQUE,
                transfer_fee DECIMAL(10,2) DEFAULT 500.00,
                status VARCHAR(20) DEFAULT 'INITIATED',
                created_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                completed_date TIMESTAMP,
                approved_by VARCHAR(50),
                rejection_reason VARCHAR(500)
            )
            """,
        // Phase 2: CBT Questions
        """
            CREATE TABLE IF NOT EXISTS cbt_questions (
                question_id VARCHAR(50) PRIMARY KEY,
                question_text VARCHAR(500) NOT NULL,
                option_a VARCHAR(200) NOT NULL,
                option_b VARCHAR(200) NOT NULL,
                option_c VARCHAR(200) NOT NULL,
                option_d VARCHAR(200) NOT NULL,
                correct_answer CHAR(1) NOT NULL,
                category VARCHAR(50)
            )
            """,
        // Phase 2: CBT Results  
        """
            CREATE TABLE IF NOT EXISTS cbt_results (
                result_id VARCHAR(50) PRIMARY KEY,
                user_id VARCHAR(50) NOT NULL,
                score INT NOT NULL,
                total_questions INT NOT NULL,
                passed BOOLEAN NOT NULL,
                test_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP
            )
            """,
        // Phase 3: Test Slots
        """
            CREATE TABLE IF NOT EXISTS test_slots (
                slot_id VARCHAR(50) PRIMARY KEY,
                slot_date DATE NOT NULL,
                slot_time TIME NOT NULL,
                test_type VARCHAR(20) NOT NULL,
                mvi_officer_id VARCHAR(50),
                capacity INT DEFAULT 5,
                booked_count INT DEFAULT 0
            )
            """,
        // Phase 3: Slot Bookings
        """
            CREATE TABLE IF NOT EXISTS slot_bookings (
                booking_id VARCHAR(50) PRIMARY KEY,
                slot_id VARCHAR(50) NOT NULL,
                user_id VARCHAR(50) NOT NULL,
                license_id VARCHAR(50),
                status VARCHAR(20) DEFAULT 'BOOKED',
                test_result VARCHAR(20),
                cooloff_until DATE
            )
            """,
        // Phase 4: Documents
        """
            CREATE TABLE IF NOT EXISTS documents (
                document_id VARCHAR(50) PRIMARY KEY,
                application_id VARCHAR(50) NOT NULL,
                application_type VARCHAR(20) NOT NULL,
                document_type VARCHAR(50) NOT NULL,
                file_path VARCHAR(500),
                upload_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                status VARCHAR(20) DEFAULT 'PENDING',
                verified_by VARCHAR(50),
                verification_date TIMESTAMP,
                rejection_reason VARCHAR(500)
            )
            """,
        // Phase 5: Blacklist Log
        """
            CREATE TABLE IF NOT EXISTS blacklist_log (
                log_id VARCHAR(50) PRIMARY KEY,
                vehicle_vin VARCHAR(50) NOT NULL,
                action VARCHAR(20) NOT NULL,
                reason VARCHAR(500),
                action_by VARCHAR(50),
                action_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP
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
    try {
      // Validate connection and reconnect if needed
      if (connection == null || connection.isClosed()) {
        System.out.println("⚠️  Database connection lost. Attempting to reconnect...");
        connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
        System.out.println("✅ Database reconnected successfully");
      }
    } catch (SQLException e) {
      System.err.println("❌ ERROR: Failed to reconnect to database: " + e.getMessage());
      throw new RuntimeException("Database connection unavailable", e);
    }
    return connection;
  }

  // Generic CRUD operations with auto-retry
  public boolean executeUpdate(String sql, Object... params) {
    int retries = 2;
    while (retries >= 0) {
      try (PreparedStatement pstmt = getConnection().prepareStatement(sql)) {
        if (params != null) {
          for (int i = 0; i < params.length; i++) {
            pstmt.setObject(i + 1, params[i]);
          }
        }
        pstmt.executeUpdate();
        return true;
      } catch (SQLException e) {
        retries--;
        if (retries < 0) {
          System.err.println("❌ ERROR: executeUpdate failed after retries: " + e.getMessage());
          System.err.println("   SQL: " + sql);
          e.printStackTrace();
          return false;
        }
        System.out.println("⚠️  Query failed, retrying... (" + retries + " attempts left)");
        try { Thread.sleep(100); } catch (InterruptedException ie) { }
      }
    }
    return false;
  }

  public ResultSet executeQuery(String sql, Object... params) {
    try {
      PreparedStatement pstmt = getConnection().prepareStatement(sql);
      if (params != null) {
        for (int i = 0; i < params.length; i++) {
          if (params[i] == null) {
            System.out.println("⚠️  Warning: Null parameter at position " + (i+1) + " in query");
          }
         pstmt.setObject(i + 1, params[i]);
        }
      }
      return pstmt.executeQuery();
    } catch (SQLException e) {
      System.err.println("❌ ERROR: executeQuery failed: " + e.getMessage());
      System.err.println("   SQL: " + sql);
      e.printStackTrace();
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
