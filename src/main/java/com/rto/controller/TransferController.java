package com.rto.controller;

import com.rto.service.*;
import com.rto.service.TransferService.*;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import java.util.List;

/**
 * Transfer Controller - Handles vehicle ownership transfer
 * Seller flow: Select vehicle → Enter buyer mobile → Generate token
 * Buyer flow: Enter token → Confirm transfer
 */
public class TransferController {
    
    @FXML
    private TabPane transferTabPane;
    @FXML
    private Tab sellerTab;
    @FXML
    private Tab buyerTab;
    
    // Seller Flow UI Elements
    @FXML
    private ChoiceBox<String> vehicleChoiceBox;
    @FXML
    private TextField buyerMobileField;
    @FXML
    private Label validationResultLabel;
    @FXML
    private Label tokenLabel;
    @FXML
    private Button initiateTransferBtn;
    
    // Buyer Flow UI Elements
    @FXML
    private TextField tokenInputField;
    @FXML
    private Label buyerStatusLabel;
    @FXML
    private Button confirmTransferBtn;
    
    private TransferService transferService;
    private VehicleService vehicleService;
    private SessionManager session;
    private String currentUserId;
    
    public void initialize() {
        transferService = new TransferService();
        vehicleService = new VehicleService();
        session = SessionManager.getInstance();
        
        if (session.isLoggedIn()) {
            currentUserId = session.getCurrentUser().getId();
            loadUserVehicles();
        }
        
        validationResultLabel.setVisible(false);
        tokenLabel.setVisible(false);
        buyerStatusLabel.setVisible(false);
    }
    
    /**
     * Load vehicles owned by current user into dropdown
     */
    private void loadUserVehicles() {
        try {
            var vehicles = vehicleService.getVehiclesByOwnerId(currentUserId);
            vehicleChoiceBox.setItems(FXCollections.observableArrayList(
                vehicles.stream()
                    .map(v -> v.getRegistrationNumber() + " - " + v.getModel())
                    .toList()
            ));
            
            if (!vehicles.isEmpty()) {
                vehicleChoiceBox.setValue(vehicleChoiceBox.getItems().get(0));
            }
        } catch (Exception e) {
            showError("Failed to load vehicles: " + e.getMessage());
        }
    }
    
    /**
     * Seller: Check eligibility before initiating transfer
     */
    @FXML
    private void handleCheckEligibility() {
        try {
            String selection = vehicleChoiceBox.getValue();
            if (selection == null) {
                showValidation("Please select a vehicle", false);
                return;
            }
            
            String vehicleVin = selection.split(" - ")[0];
            
            ValidationResult result = transferService.validateTransferEligibility(vehicleVin, currentUserId);
            
            showValidation(result.getReason(), result.isValid());
            initiateTransferBtn.setDisable(!result.isValid());
            
        } catch (Exception e) {
            showValidation("Error: " + e.getMessage(), false);
        }
    }
    
    /**
     * Seller: Initiate transfer and generate token
     */
    @FXML
    private void handleInitiateTransfer() {
        try {
            String selection = vehicleChoiceBox.getValue();
            String buyerMobile = buyerMobileField.getText().trim();
            
            if (selection == null || buyerMobile.isEmpty()) {
                showError("Please select vehicle and enter buyer mobile");
                return;
            }
            
            if (!buyerMobile.matches("\\d{10}")) {
                showError("Please enter a valid 10-digit mobile number");
                return;
            }
            
            String vehicleVin = selection.split(" - ")[0];
            String token = transferService.initiateTransfer(vehicleVin, currentUserId, buyerMobile);
            
            if (token != null) {
                tokenLabel.setText("✅ Transfer Token: " + token);
                tokenLabel.setStyle("-fx-text-fill: green; -fx-font-size: 16px; -fx-font-weight: bold;");
                tokenLabel.setVisible(true);
                
                showSuccess("Transfer initiated! Share this token with buyer: " + token);
                
                buyerMobileField.clear();
                initiateTransferBtn.setDisable(true);
            } else {
                showError("Transfer initiation failed. Check console for details.");
            }
            
        } catch (Exception e) {
            showError("Error: " + e.getMessage());
        }
    }
    
    /**
     * Buyer: Complete transfer using token
     */
    @FXML
    private void handleConfirmTransfer() {
        try {
            String token = tokenInputField.getText().trim();
            
            if (token.isEmpty()) {
                showBuyerError("Please enter the transfer token");
                return;
            }
            
            boolean success = transferService.completeTransferByBuyer(token, currentUserId);
            
            if (success) {
                showBuyerSuccess("Transfer confirmed! Payment recorded. Awaiting RTO approval.");
                tokenInputField.clear();
            } else {
                showBuyerError("Invalid or expired token. Please check and try again.");
            }
            
        } catch (Exception e) {
            showBuyerError("Error: " + e.getMessage());
        }
    }
    
    // Helper Methods
    
    private void showValidation(String message, boolean valid) {
        validationResultLabel.setText(message);
        validationResultLabel.setStyle(valid ? 
            "-fx-text-fill: green;" : 
            "-fx-text-fill: red;");
        validationResultLabel.setVisible(true);
    }
    
    private void showSuccess(String message) {
        validationResultLabel.setText(message);
        validationResultLabel.setStyle("-fx-text-fill: green;");
        validationResultLabel.setVisible(true);
    }
    
    private void showError(String message) {
        validationResultLabel.setText(message);
        validationResultLabel.setStyle("-fx-text-fill: red;");
        validationResultLabel.setVisible(true);
    }
    
    private void showBuyerSuccess(String message) {
        buyerStatusLabel.setText(message);
        buyerStatusLabel.setStyle("-fx-text-fill: green;");
        buyerStatusLabel.setVisible(true);
    }
    
    private void showBuyerError(String message) {
        buyerStatusLabel.setText(message);
        buyerStatusLabel.setStyle("-fx-text-fill: red;");
        buyerStatusLabel.setVisible(true);
    }
}
