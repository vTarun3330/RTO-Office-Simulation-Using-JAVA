package com.rto.service;

import com.rto.service.HypothecationService.Hypothecation;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Transfer Service - Manages vehicle ownership transfer
 * Implements Chain of Responsibility for validation
 * Implements State Pattern for transfer workflow
 */
public class TransferService implements IService {
    private DatabaseService db;
    private HypothecationService hypothecationService;
    private ChallanService challanService;
    private VehicleService vehicleService;

    public TransferService() {
        this.db = DatabaseService.getInstance();
        this.hypothecationService = new HypothecationService();
        this.challanService = new ChallanService();
        this.vehicleService = new VehicleService();
    }

    @Override
    public void initialize() {
        System.out.println("TransferService initialized");
    }

    /**
     * Initiate transfer from seller side
     * Returns transfer token if successful
     */
    public String initiateTransfer(String vehicleVin, String sellerId, String buyerMobile) {
        try {
            // Input validation
            if (vehicleVin == null || sellerId == null || buyerMobile == null) {
                System.err.println("❌ ERROR: Missing required transfer information");
                return null;
            }

            // Run validation chain
            ValidationResult result = validateTransferEligibility(vehicleVin, sellerId);
            if (!result.isValid()) {
                System.err.println("❌ Transfer Blocked: " + result.getReason());
                return null;
            }

            // Generate unique transfer token
            String token = generateTransferToken();
            String transferId = "TXF-" + System.currentTimeMillis();

            String sql = """
                INSERT INTO transfer_requests 
                (transfer_id, vehicle_vin, seller_id, buyer_mobile, transfer_token, status, created_date)
                VALUES (?, ?, ?, ?, ?, 'BUYER_PENDING', CURRENT_TIMESTAMP)
                """;

            boolean success = db.executeUpdate(sql, transferId, vehicleVin, sellerId, buyerMobile, token);

            if (success) {
                System.out.println("✅ Transfer initiated. Token: " + token);
                // In real system: Send SMS to buyer with token
                System.out.println("📱 SMS sent to " + buyerMobile + ": Your transfer token is " + token);
                return token;
            }
        } catch (Exception e) {
            System.err.println("❌ ERROR: Transfer initiation failed: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Complete transfer from buyer side using token
     */
    public boolean completeTransferByBuyer(String token, String buyerId) {
        if (token == null || buyerId == null) {
            System.err.println("❌ ERROR: Invalid token or buyer ID");
            return false;
        }

        try {
            // Find transfer request by token
            String findSql = "SELECT * FROM transfer_requests WHERE transfer_token = ? AND status = 'BUYER_PENDING'";
            TransferRequest request = null;

            try (ResultSet rs = db.executeQuery(findSql, token)) {
                if (rs != null && rs.next()) {
                    request = mapResultSetToTransfer(rs);
                } else {
                    System.err.println("❌ ERROR: Invalid or expired token");
                    return false;
                }
            }

            if (request == null) {
                return false;
            }

            // Update with buyer ID and change status
            String updateSql = "UPDATE transfer_requests SET buyer_id = ?, status = 'PAYMENT_DONE' WHERE transfer_token = ?";
            boolean success = db.executeUpdate(updateSql, buyerId, token);

            if (success) {
                System.out.println("✅ Buyer confirmed. Status: PAYMENT_DONE. Awaiting RTO approval.");
                // Simulate payment (in real system, integrate payment gateway here)
                recordTransferPayment(request.getTransferId(), buyerId, request.getTransferFee());
            }

            return success;
        } catch (Exception e) {
            System.err.println("❌ ERROR: Buyer transfer completion failed: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * RTO Officer approves transfer (Final step)
     */
    public boolean approveTransfer(String transferId, String officerId) {
        if (transferId == null) {
            return false;
        }

        try {
            // Get transfer request
            String findSql = "SELECT * FROM transfer_requests WHERE transfer_id = ?";
            TransferRequest request = null;

            try (ResultSet rs = db.executeQuery(findSql, transferId)) {
                if (rs != null && rs.next()) {
                    request = mapResultSetToTransfer(rs);
                }
            }

            if (request == null || !request.getStatus().equals("PAYMENT_DONE")) {
                System.err.println("❌ ERROR: Transfer not ready for approval");
                return false;
            }

            // Update vehicle ownership
            String updateVehicleSql = "UPDATE vehicles SET owner_id = ? WHERE registration_number = ?";
            db.executeUpdate(updateVehicleSql, request.getBuyerId(), request.getVehicleVin());

            // Mark transfer as completed
            String updateTransferSql = "UPDATE transfer_requests SET status = 'COMPLETED', approved_by = ?, completed_date = CURRENT_TIMESTAMP WHERE transfer_id = ?";
            boolean success = db.executeUpdate(updateTransferSql, officerId, transferId);

            if (success) {
                System.out.println("✅ Transfer APPROVED by RTO Officer. Ownership transferred.");
            }

            return success;
        } catch (Exception e) {
            System.err.println("❌ ERROR: Transfer approval failed: " + e.getMessage());
            return false;
        }
    }

    /**
     * Reject transfer request
     */
    public boolean rejectTransfer(String transferId, String officerId, String reason) {
        String sql = "UPDATE transfer_requests SET status = 'REJECTED', approved_by = ?, rejection_reason = ? WHERE transfer_id = ?";
        return db.executeUpdate(sql, officerId, reason, transferId);
    }

    /**
     * Validation Chain: Check if vehicle is eligible for transfer
     * Chain: Ownership → Hypothecation → Blacklist → Unpaid Challans
     */
    public ValidationResult validateTransferEligibility(String vehicleVin, String sellerId) {
        // 1. Check if seller owns the vehicle
        String ownerSql = "SELECT owner_id FROM vehicles WHERE registration_number = ?";
        try (ResultSet rs = db.executeQuery(ownerSql, vehicleVin)) {
            if (rs == null || !rs.next()) {
                return new ValidationResult(false, "Vehicle not found");
            }
            String ownerId = rs.getString("owner_id");
            if (!ownerId.equals(sellerId)) {
                return new ValidationResult(false, "You are not the owner of this vehicle");
            }
        } catch (SQLException e) {
            return new ValidationResult(false, "Database error");
        }

        // 2. Check Hypothecation (Bank Loan)
        if (hypothecationService.hasActiveHypothecation(vehicleVin)) {
            Hypothecation hyp = hypothecationService.getHypothecation(vehicleVin);
            return new ValidationResult(false, 
                "Vehicle has active loan with " + hyp.getBankName() + ". Bank NOC required.");
        }

        // 3. Check Blacklist
        String blacklistSql = "SELECT is_blacklisted FROM vehicles WHERE registration_number = ?";
        try (ResultSet rs = db.executeQuery(blacklistSql, vehicleVin)) {
            if (rs != null && rs.next() && rs.getBoolean("is_blacklisted")) {
                return new ValidationResult(false, "CRITICAL: Vehicle is blacklisted/detained");
            }
        } catch (SQLException e) {
            return new ValidationResult(false, "Database error");
        }

        // 4. Check Unpaid Challans
        double unpaidAmount = challanService.getTotalUnpaidAmount(vehicleVin);
        if (unpaidAmount > 0) {
            return new ValidationResult(false, 
                "Unpaid challans of ₹" + unpaidAmount + " must be cleared first");
        }

        // All checks passed
        return new ValidationResult(true, "Vehicle eligible for transfer");
    }

    /**
     * Get all transfer requests (for Admin dashboard)
     */
    public List<TransferRequest> getAllPendingTransfers() {
        List<TransferRequest> transfers = new ArrayList<>();
        String sql = "SELECT * FROM transfer_requests WHERE status IN ('PAYMENT_DONE', 'RTO_PENDING') ORDER BY created_date DESC";

        try (ResultSet rs = db.executeQuery(sql)) {
            while (rs != null && rs.next()) {
                transfers.add(mapResultSetToTransfer(rs));
            }
        } catch (SQLException e) {
            System.err.println("❌ ERROR: Failed to get transfers: " + e.getMessage());
        }
        return transfers;
    }

    /**
     * Get transfer requests by seller
     */
    public List<TransferRequest> getTransfersBySeller(String sellerId) {
        List<TransferRequest> transfers = new ArrayList<>();
        String sql = "SELECT * FROM transfer_requests WHERE seller_id = ? ORDER BY created_date DESC";

        try (ResultSet rs = db.executeQuery(sql, sellerId)) {
            while (rs != null && rs.next()) {
                transfers.add(mapResultSetToTransfer(rs));
            }
        } catch (SQLException e) {
            System.err.println("❌ ERROR: Failed to get seller transfers");
        }
        return transfers;
    }

    // Helper Methods

    private String generateTransferToken() {
        // Generate 6-digit token
        Random random = new Random();
        return String.format("%06d", random.nextInt(1000000));
    }

    private void recordTransferPayment(String transferId, String buyerId, double amount) {
        TransactionService txnService = new TransactionService();
        txnService.recordTransaction(buyerId, amount, "UPI", "TRANSFER_FEE", transferId);
    }

    private TransferRequest mapResultSetToTransfer(ResultSet rs) throws SQLException {
        TransferRequest tr = new TransferRequest();
        tr.setTransferId(rs.getString("transfer_id"));
        tr.setVehicleVin(rs.getString("vehicle_vin"));
        tr.setSellerId(rs.getString("seller_id"));
        tr.setBuyerId(rs.getString("buyer_id"));
        tr.setBuyerMobile(rs.getString("buyer_mobile"));
        tr.setTransferToken(rs.getString("transfer_token"));
        tr.setTransferFee(rs.getDouble("transfer_fee"));
        tr.setStatus(rs.getString("status"));
        
        Timestamp created = rs.getTimestamp("created_date");
        if (created != null) {
            tr.setCreatedDate(created.toLocalDateTime());
        }
        
        return tr;
    }

    // Inner Classes

    public static class TransferRequest {
        private String transferId;
        private String vehicleVin;
        private String sellerId;
        private String buyerId;
        private String buyerMobile;
        private String transferToken;
        private double transferFee;
        private String status;
        private LocalDateTime createdDate;

        // Getters and Setters
        public String getTransferId() { return transferId; }
        public void setTransferId(String transferId) { this.transferId = transferId; }
        
        public String getVehicleVin() { return vehicleVin; }
        public void setVehicleVin(String vehicleVin) { this.vehicleVin = vehicleVin; }
        
        public String getSellerId() { return sellerId; }
        public void setSellerId(String sellerId) { this.sellerId = sellerId; }
        
        public String getBuyerId() { return buyerId; }
        public void setBuyerId(String buyerId) { this.buyerId = buyerId; }
        
        public String getBuyerMobile() { return buyerMobile; }
        public void setBuyerMobile(String buyerMobile) { this.buyerMobile = buyerMobile; }
        
        public String getTransferToken() { return transferToken; }
        public void setTransferToken(String transferToken) { this.transferToken = transferToken; }
        
        public double getTransferFee() { return transferFee; }
        public void setTransferFee(double transferFee) { this.transferFee = transferFee; }
        
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        
        public LocalDateTime getCreatedDate() { return createdDate; }
        public void setCreatedDate(LocalDateTime createdDate) { this.createdDate = createdDate; }
    }

    public static class ValidationResult {
        private boolean valid;
        private String reason;

        public ValidationResult(boolean valid, String reason) {
            this.valid = valid;
            this.reason = reason;
        }

        public boolean isValid() { return valid; }
        public String getReason() { return reason; }
    }
}
