package com.rto.service;

import com.rto.model.Vehicle;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Hypothecation Service - Manages vehicle loans and bank NOC
 * Simulates Bank API for loan tracking
 */
public class HypothecationService implements IService {
    private DatabaseService db;

    public HypothecationService() {
        this.db = DatabaseService.getInstance();
    }

    @Override
    public void initialize() {
        System.out.println("HypothecationService initialized");
    }

    /**
     * Check if a vehicle has active hypothecation (loan)
     */
    public boolean hasActiveHypothecation(String vehicleVin) {
        if (vehicleVin == null || vehicleVin.trim().isEmpty()) {
            return false;
        }

        String sql = "SELECT COUNT(*) FROM hypothecations WHERE vehicle_vin = ? AND is_active = TRUE AND noc_issued = FALSE";
        try (ResultSet rs = db.executeQuery(sql, vehicleVin.trim())) {
            if (rs != null && rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            System.err.println("❌ ERROR: Failed to check hypothecation: " + e.getMessage());
        }
        return false;
    }

    /**
     * Get hypothecation details for a vehicle
     */
    public Hypothecation getHypothecation(String vehicleVin) {
        String sql = "SELECT * FROM hypothecations WHERE vehicle_vin = ? AND is_active = TRUE";
        try (ResultSet rs = db.executeQuery(sql, vehicleVin)) {
            if (rs != null && rs.next()) {
                Hypothecation hyp = new Hypothecation();
                hyp.setId(rs.getString("id"));
                hyp.setVehicleVin(rs.getString("vehicle_vin"));
                hyp.setBankName(rs.getString("bank_name"));
                hyp.setLoanAccount(rs.getString("loan_account"));
                hyp.setLoanAmount(rs.getDouble("loan_amount"));
                
                Date startDate = rs.getDate("start_date");
                if (startDate != null) {
                    hyp.setStartDate(startDate.toLocalDate());
                }
                
                hyp.setActive(rs.getBoolean("is_active"));
                hyp.setNocIssued(rs.getBoolean("noc_issued"));
                
                return hyp;
            }
        } catch (SQLException e) {
            System.err.println("❌ ERROR: Failed to get hypothecation: " + e.getMessage());
        }
        return null;
    }

    /**
     * Request NOC from bank (Simulated)
     * In real system, this would call a Bank API
     */
    public boolean requestNOC(String hypothecationId, String userId) {
        if (hypothecationId == null) {
            System.err.println("❌ ERROR: Invalid hypothecation ID");
            return false;
        }

        try {
            // Simulate: In real system, this would trigger bank approval workflow
            System.out.println("📨 NOC request sent to bank for hypothecation: " + hypothecationId);
            
            // For simulation: Auto-approve NOC after request (instant approval)
            // In production: This would be a separate admin workflow
            String sql = "UPDATE hypothecations SET noc_issued = TRUE, noc_date = CURRENT_DATE WHERE id = ?";
            boolean success = db.executeUpdate(sql, hypothecationId);
            
            if (success) {
                System.out.println("✅ NOC issued by bank (simulated)");
                // Update vehicle hypothecation flag
                updateVehicleHypothecationFlag(hypothecationId, false);
            }
            return success;
        } catch (Exception e) {
            System.err.println("❌ ERROR: NOC request failed: " + e.getMessage());
            return false;
        }
    }

    /**
     * Add hypothecation (when vehicle is purchased with loan)
     */
    public boolean addHypothecation(String vehicleVin, String bankName, String loanAccount, double loanAmount) {
        if (vehicleVin == null || bankName == null) {
            System.err.println("❌ ERROR: Invalid hypothecation data");
            return false;
        }

        try {
            String hypId = "HYP-" + System.currentTimeMillis();
            String sql = """
                INSERT INTO hypothecations 
                (id, vehicle_vin, bank_name, loan_account, loan_amount, start_date, is_active, noc_issued)
                VALUES (?, ?, ?, ?, ?, CURRENT_DATE, TRUE, FALSE)
                """;
            
            boolean success = db.executeUpdate(sql, hypId, vehicleVin, bankName, loanAccount, loanAmount);
            
            if (success) {
                // Update vehicle flag
                updateVehicleHypothecationFlag(vehicleVin, true);
                System.out.println("✅ Hypothecation added: " + hypId);
            }
            return success;
        } catch (Exception e) {
            System.err.println("❌ ERROR: Failed to add hypothecation: " + e.getMessage());
            return false;
        }
    }

    /**
     * Remove hypothecation (when loan is cleared)
     */
    public boolean removeHypothecation(String hypothecationId) {
        String sql = "UPDATE hypothecations SET is_active = FALSE WHERE id = ?";
        boolean success = db.executeUpdate(sql, hypothecationId);
        
        if (success) {
            updateVehicleHypothecationFlag(hypothecationId, false);
            System.out.println("✅ Hypothecation removed");
        }
        return success;
    }

    /**
     * Helper: Update vehicle's hypothecation flag
     */
    private void updateVehicleHypothecationFlag(String identifier, boolean flag) {
        // Check if identifier is hypothecation ID or VIN
        String vehicleVin = identifier;
        
        if (identifier.startsWith("HYP-")) {
            // Get VIN from hypothecation ID
            String sql = "SELECT vehicle_vin FROM hypothecations WHERE id = ?";
            try (ResultSet rs = db.executeQuery(sql, identifier)) {
                if (rs != null && rs.next()) {
                    vehicleVin = rs.getString("vehicle_vin");
                }
            } catch (SQLException e) {
                System.err.println("❌ ERROR: Failed to get vehicle VIN");
                return;
            }
        }
        
        // Update vehicle table
        String updateSql = "UPDATE vehicles SET hypothecation_flag = ? WHERE registration_number = ?";
        db.executeUpdate(updateSql, flag, vehicleVin);
    }

    /**
     * Inner class: Hypothecation model
     */
    public static class Hypothecation implements java.io.Serializable {
        private String id;
        private String vehicleVin;
        private String bankName;
        private String loanAccount;
        private double loanAmount;
        private LocalDate startDate;
        private boolean isActive;
        private boolean nocIssued;

        // Getters and Setters
        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        
        public String getVehicleVin() { return vehicleVin; }
        public void setVehicleVin(String vehicleVin) { this.vehicleVin = vehicleVin; }
        
        public String getBankName() { return bankName; }
        public void setBankName(String bankName) { this.bankName = bankName; }
        
        public String getLoanAccount() { return loanAccount; }
        public void setLoanAccount(String loanAccount) { this.loanAccount = loanAccount; }
        
        public double getLoanAmount() { return loanAmount; }
        public void setLoanAmount(double loanAmount) { this.loanAmount = loanAmount; }
        
        public LocalDate getStartDate() { return startDate; }
        public void setStartDate(LocalDate startDate) { this.startDate = startDate; }
        
        public boolean isActive() { return isActive; }
        public void setActive(boolean active) { isActive = active; }
        
        public boolean isNocIssued() { return nocIssued; }
        public void setNocIssued(boolean nocIssued) { this.nocIssued = nocIssued; }

        @Override
        public String toString() {
            return "Hypothecation{" +
                    "bank='" + bankName + '\'' +
                    ", amount=" + loanAmount +
                    ", active=" + isActive +
                    ", nocIssued=" + nocIssued +
                    '}';
        }
    }
}
