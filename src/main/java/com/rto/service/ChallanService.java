package com.rto.service;

import com.rto.model.Challan;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Challan Service - Handles traffic violation/offense management.
 * Used by Admin to issue challans and by Citizens to view/pay fines.
 */
public class ChallanService implements IService {
  private DatabaseService db;

  public ChallanService() {
    this.db = DatabaseService.getInstance();
  }

  @Override
  public void initialize() {
    System.out.println("ChallanService initialized");
  }

  /**
   * Issue a new challan for a vehicle.
   */
  public boolean issueChallan(Challan challan) {
    if (challan == null) {
        System.err.println("❌ ERROR: Cannot issue null challan");
        return false;
    }
    
    // Validation
    if (challan.getVehicleVin() == null || challan.getVehicleVin().isEmpty()) {
        System.err.println("❌ ERROR: Vehicle VIN required for challan");
        return false;
    }
    if (challan.getAmount() <= 0) {
        System.err.println("❌ ERROR: Challan amount must be positive");
        return false;
    }

    try {
        String sql = """
            INSERT INTO challans
            (challan_id, vehicle_vin, offense_type, amount, issue_date, is_paid, issued_by)
            VALUES (?, ?, ?, ?, ?, ?, ?)
            """;

        boolean success = db.executeUpdate(sql,
            challan.getChallanId(),
            challan.getVehicleVin(),
            challan.getOffenseType(),
            challan.getAmount(),
            Date.valueOf(challan.getIssueDate()),
            challan.isPaid(),
            challan.getIssuedBy());
            
        if (success) {
            System.out.println("✅ Challan issued successfully: " + challan.getChallanId());
        } else {
            System.err.println("❌ ERROR: Failed to save challan to database");
        }
        return success;
    } catch (Exception e) {
        System.err.println("❌ ERROR: Unexpected error issuing challan: " + e.getMessage());
        e.printStackTrace();
        return false;
    }
  }

  /**
   * Get all challans for a vehicle.
   */
  public List<Challan> getChallansByVehicle(String vehicleVin) {
    List<Challan> challans = new ArrayList<>();
    String sql = "SELECT * FROM challans WHERE vehicle_vin = ? ORDER BY issue_date DESC";

    try (ResultSet rs = db.executeQuery(sql, vehicleVin)) {
      while (rs != null && rs.next()) {
        challans.add(mapResultSetToChallan(rs));
      }
    } catch (SQLException e) {
      System.err.println("Error getting challans: " + e.getMessage());
    }
    return challans;
  }

  /**
   * Get all unpaid challans for a vehicle.
   */
  public List<Challan> getUnpaidChallans(String vehicleVin) {
    List<Challan> challans = new ArrayList<>();
    String sql = "SELECT * FROM challans WHERE vehicle_vin = ? AND is_paid = FALSE";

    try (ResultSet rs = db.executeQuery(sql, vehicleVin)) {
      while (rs != null && rs.next()) {
        challans.add(mapResultSetToChallan(rs));
      }
    } catch (SQLException e) {
      System.err.println("Error getting unpaid challans: " + e.getMessage());
    }
    return challans;
  }

  /**
   * Get all pending (unpaid) challans (Admin view).
   */
  public List<Challan> getAllPendingChallans() {
    List<Challan> challans = new ArrayList<>();
    String sql = "SELECT * FROM challans WHERE is_paid = FALSE ORDER BY issue_date DESC";

    try (ResultSet rs = db.executeQuery(sql)) {
      while (rs != null && rs.next()) {
        challans.add(mapResultSetToChallan(rs));
      }
    } catch (SQLException e) {
      System.err.println("Error getting pending challans: " + e.getMessage());
    }
    return challans;
  }

  /**
   * Mark a challan as paid.
   */
  public boolean payChallan(String challanId, String transactionId) {
    if (challanId == null || transactionId == null) {
        System.err.println("❌ ERROR: Invalid parameters for paying challan");
        return false;
    }

    try {
        String sql = "UPDATE challans SET is_paid = TRUE, payment_transaction_id = ? WHERE challan_id = ?";
        boolean success = db.executeUpdate(sql, transactionId, challanId);
        
        if (success) {
            System.out.println("✅ Challan " + challanId + " marked as PAID");
        } else {
            System.err.println("❌ ERROR: Failed to update challan payment status. ID may be incorrect.");
        }
        return success;
    } catch (Exception e) {
        System.err.println("❌ ERROR: Unexpected error paying challan: " + e.getMessage());
        return false;
    }
  }

  /**
   * Get challan by ID.
   */
  public Challan getChallanById(String challanId) {
    String sql = "SELECT * FROM challans WHERE challan_id = ?";
    try (ResultSet rs = db.executeQuery(sql, challanId)) {
      if (rs != null && rs.next()) {
        return mapResultSetToChallan(rs);
      }
    } catch (SQLException e) {
      System.err.println("Error getting challan: " + e.getMessage());
    }
    return null;
  }

  /**
   * Get total unpaid challan amount for a vehicle.
   */
  public double getTotalUnpaidAmount(String vehicleVin) {
    String sql = "SELECT SUM(amount) FROM challans WHERE vehicle_vin = ? AND is_paid = FALSE";
    try (ResultSet rs = db.executeQuery(sql, vehicleVin)) {
      if (rs != null && rs.next()) {
        return rs.getDouble(1);
      }
    } catch (SQLException e) {
      System.err.println("Error calculating unpaid amount: " + e.getMessage());
    }
    return 0.0;
  }

  private Challan mapResultSetToChallan(ResultSet rs) throws SQLException {
    Challan challan = new Challan();
    challan.setChallanId(rs.getString("challan_id"));
    challan.setVehicleVin(rs.getString("vehicle_vin"));
    challan.setOffenseType(rs.getString("offense_type"));
    challan.setAmount(rs.getDouble("amount"));

    Date issueDate = rs.getDate("issue_date");
    if (issueDate != null) {
      challan.setIssueDate(issueDate.toLocalDate());
    }

    challan.setPaid(rs.getBoolean("is_paid"));
    challan.setIssuedBy(rs.getString("issued_by"));
    challan.setPaymentTransactionId(rs.getString("payment_transaction_id"));

    return challan;
  }
}
