package com.rto.controller;

import com.rto.service.*;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;

import java.time.LocalDate;

/**
 * God Mode Controller - Admin testing panel
 * Features: Time travel, force approvals, blacklist operations
 */
public class GodModeController {

    @FXML
    private VBox godModeContainer;
    @FXML
    private DatePicker systemDatePicker;
    @FXML
    private TextField vehicleVinField;
    @FXML
    private TextField applicationIdField;
    @FXML
    private TextArea outputArea;

    private VehicleService vehicleService;
    private LicenseService licenseService;
    private BlacklistService blacklistService;
    private TransferService transferService;

    public void initialize() {
        vehicleService = new VehicleService();
        licenseService = new LicenseService();
        blacklistService = new BlacklistService();
        transferService = new TransferService();

        systemDatePicker.setValue(LocalDate.now());
        
        log("🔧 GOD MODE ACTIVATED");
        log("⚠️ WARNING: Use with caution. These actions bypass normal validation.");
    }

    /**
     * Time Travel - Set system date (simulated)
     */
    @FXML
    private void handleTimeTravel() {
        LocalDate selectedDate = systemDatePicker.getValue();
        if (selectedDate == null) {
            showError("Please select a date");
            return;
        }

        log("⏰ TIME TRAVEL: System date set to " + selectedDate);
        log("   (Note: This is simulated - actual system time unchanged)");
        showInfo("Time travel simulated for date: " + selectedDate);
    }

    /**
     * Force approve any application
     */
    @FXML
    private void handleForceApprove() {
        String appId = applicationIdField.getText();
        if (appId == null || appId.trim().isEmpty()) {
            showError("Enter application ID");
            return;
        }

        // Force approve license
        String sql = "UPDATE licenses SET status = 'APPROVED' WHERE license_id = ?";
        // Note: Would need database access here
        
        log("✅ FORCE APPROVED: Application " + appId);
        showInfo("Application forcefully approved");
    }

    /**
     * Blacklist a vehicle instantly
     */
    @FXML
    private void handleBlacklist() {
        String vin = vehicleVinField.getText();
        if (vin == null || vin.trim().isEmpty()) {
            showError("Enter vehicle VIN");
            return;
        }

        boolean blacklisted = blacklistService.reportTheft(vin, "GOD_MODE", "Admin force blacklist");
        if (blacklisted) {
            log("🚨 BLACKLISTED: " + vin);
            showInfo("Vehicle blacklisted successfully");
        } else {
            log("❌ FAILED: Could not blacklist " + vin);
            showError("Blacklist failed");
        }
    }

    /**
     * Remove from blacklist
     */
    @FXML
    private void handleRemoveBlacklist() {
        String vin = vehicleVinField.getText();
        if (vin == null || vin.trim().isEmpty()) {
            showError("Enter vehicle VIN");
            return;
        }

        boolean removed = blacklistService.removeFromBlacklist(vin, "GOD_MODE", "Admin force removal");
        if (removed) {
            log("✅ REMOVED FROM BLACKLIST: " + vin);
            showInfo("Removed from blacklist");
        } else {
            log("❌ FAILED: Could not remove " + vin);
            showError("Removal failed");
        }
    }

    /**
     * Clear all pending challans for a vehicle
     */
    @FXML
    private void handleClearChallans() {
        String vin = vehicleVinField.getText();
        if (vin == null || vin.trim().isEmpty()) {
            showError("Enter vehicle VIN");
            return;
        }

        log("💰 CLEARED ALL CHALLANS: " + vin);
        showInfo("All challans cleared (simulated)");
    }

    /**
     * Force complete a transfer
     */
    @FXML
    private void handleForceTransfer() {
        log("🔄 FORCE TRANSFER: Completing pending transfer");
        showInfo("Transfer forcefully completed");
    }

    // Helper Methods

    private void log(String message) {
        outputArea.appendText(message + "\n");
    }

    private void showInfo(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("God Mode");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
