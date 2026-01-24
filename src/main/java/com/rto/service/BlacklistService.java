package com.rto.service;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.*;

/**
 * Blacklist Service - Handles vehicle theft reporting and blacklisting
 * Blocks all operations on blacklisted vehicles
 */
public class BlacklistService implements IService {
    private DatabaseService db;

    public BlacklistService() {
        this.db = DatabaseService.getInstance();
    }

    @Override
    public void initialize() {
        System.out.println("BlacklistService initialized");
    }

    /**
     * Report a vehicle as stolen (blacklist it)
     */
    public boolean reportTheft(String vehicleVin, String reportedBy, String reason) {
        if (vehicleVin == null || vehicleVin.trim().isEmpty()) {
            System.err.println("❌ ERROR: Vehicle VIN required");
            return false;
        }

        // Check if already blacklisted
        if (isBlacklisted(vehicleVin)) {
            System.err.println("⚠️ WARNING: Vehicle already blacklisted");
            return false;
        }

        // Update vehicle blacklist status
        String updateSql = "UPDATE vehicles SET is_blacklisted = TRUE WHERE registration_number = ?";
        boolean updated = db.executeUpdate(updateSql, vehicleVin);

        if (updated) {
            // Log the blacklist event
            String logSql = """
                INSERT INTO blacklist_log 
                (log_id, vehicle_vin, action, reason, action_by, action_date)
                VALUES (?, ?, 'BLACKLISTED', ?, ?, CURRENT_TIMESTAMP)
                """;
            
            String logId = "BL-" + System.currentTimeMillis();
            db.executeUpdate(logSql, logId, vehicleVin, reason, reportedBy);
            
            System.out.println("🚨 Vehicle BLACKLISTED: " + vehicleVin);
            System.out.println("   Reason: " + reason);
            return true;
        }

        return false;
    }

    /**
     * Remove vehicle from blacklist (recovered)
     */
    public boolean removeFromBlacklist(String vehicleVin, String removedBy, String reason) {
        if (!isBlacklisted(vehicleVin)) {
            System.err.println("⚠️ WARNING: Vehicle not blacklisted");
            return false;
        }

        String updateSql = "UPDATE vehicles SET is_blacklisted = FALSE WHERE registration_number = ?";
        boolean updated = db.executeUpdate(updateSql, vehicleVin);

        if (updated) {
            // Log the removal
            String logSql = """
                INSERT INTO blacklist_log 
                (log_id, vehicle_vin, action, reason, action_by, action_date)
                VALUES (?, ?, 'REMOVED', ?, ?, CURRENT_TIMESTAMP)
                """;
            
            String logId = "BL-" + System.currentTimeMillis();
            db.executeUpdate(logSql, logId, vehicleVin, reason, removedBy);
            
            System.out.println("✅ Vehicle removed from blacklist: " + vehicleVin);
            return true;
        }

        return false;
    }

    /**
     * Check if a vehicle is blacklisted
     */
    public boolean isBlacklisted(String vehicleVin) {
        String sql = "SELECT is_blacklisted FROM vehicles WHERE registration_number = ?";
        try (ResultSet rs = db.executeQuery(sql, vehicleVin)) {
            if (rs != null && rs.next()) {
                return rs.getBoolean("is_blacklisted");
            }
        } catch (SQLException e) {
            System.err.println("❌ ERROR: Failed to check blacklist status");
        }
        return false;
    }

    /**
     * Get all blacklisted vehicles
     */
    public List<String> getAllBlacklistedVehicles() {
        List<String> vehicles = new ArrayList<>();
        String sql = "SELECT registration_number FROM vehicles WHERE is_blacklisted = TRUE";
        
        try (ResultSet rs = db.executeQuery(sql)) {
            while (rs != null && rs.next()) {
                vehicles.add(rs.getString("registration_number"));
            }
        } catch (SQLException e) {
            System.err.println("❌ ERROR: Failed to get blacklisted vehicles");
        }
        
        return vehicles;
    }

    /**
     * Get blacklist history for a vehicle
     */
    public List<BlacklistLog> getBlacklistHistory(String vehicleVin) {
        List<BlacklistLog> history = new ArrayList<>();
        String sql = "SELECT * FROM blacklist_log WHERE vehicle_vin = ? ORDER BY action_date DESC";
        
        try (ResultSet rs = db.executeQuery(sql, vehicleVin)) {
            while (rs != null && rs.next()) {
                BlacklistLog log = new BlacklistLog();
                log.setLogId(rs.getString("log_id"));
                log.setVehicleVin(rs.getString("vehicle_vin"));
                log.setAction(rs.getString("action"));
                log.setReason(rs.getString("reason"));
                log.setActionBy(rs.getString("action_by"));
                
                Timestamp actionDate = rs.getTimestamp("action_date");
                if (actionDate != null) {
                    log.setActionDate(actionDate.toLocalDateTime());
                }
                
                history.add(log);
            }
        } catch (SQLException e) {
            System.err.println("❌ ERROR: Failed to get blacklist history");
        }
        
        return history;
    }

    /**
     * Validate operation on vehicle (block if blacklisted)
     */
    public boolean validateOperation(String vehicleVin, String operationType) {
        if (isBlacklisted(vehicleVin)) {
            System.err.println("🚨 OPERATION BLOCKED: Vehicle is blacklisted");
            System.err.println("   Vehicle: " + vehicleVin);
            System.err.println("   Operation: " + operationType);
            return false;
        }
        return true;
    }

    // Inner Class
    public static class BlacklistLog {
        private String logId;
        private String vehicleVin;
        private String action;
        private String reason;
        private String actionBy;
        private LocalDateTime actionDate;

        // Getters and Setters
        public String getLogId() { return logId; }
        public void setLogId(String logId) { this.logId = logId; }

        public String getVehicleVin() { return vehicleVin; }
        public void setVehicleVin(String vehicleVin) { this.vehicleVin = vehicleVin; }

        public String getAction() { return action; }
        public void setAction(String action) { this.action = action; }

        public String getReason() { return reason; }
        public void setReason(String reason) { this.reason = reason; }

        public String getActionBy() { return actionBy; }
        public void setActionBy(String actionBy) { this.actionBy = actionBy; }

        public LocalDateTime getActionDate() { return actionDate; }
        public void setActionDate(LocalDateTime actionDate) { this.actionDate = actionDate; }
    }
}
