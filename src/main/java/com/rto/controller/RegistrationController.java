package com.rto.controller;

import com.rto.service.RTOSystemFacade;
import javafx.fxml.FXML;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

public class RegistrationController {

  @FXML
  private TextField modelField;
  @FXML
  private ChoiceBox<String> typeBox;
  @FXML
  private TextField specField; // Fuel or CC
  @FXML
  private TextField cardField;
  @FXML
  private TextField cvvField;
  @FXML
  private Label statusLabel;

  private RTOSystemFacade rtoFacade;

  public void initialize() {
    rtoFacade = new RTOSystemFacade();
    // Initialize type choices if not set in FXML
    if (typeBox.getItems().isEmpty()) {
      typeBox.getItems().addAll("CAR", "BIKE", "TRUCK");
      typeBox.setValue("CAR");
    }
  }

  @FXML
  private void handleSubmit() {
    try {
        String model = modelField.getText();
        String type = typeBox.getValue();
        String spec = specField.getText();
    
        // Validate inputs
        if (model == null || model.isBlank()) {
          statusLabel.setText("Please enter vehicle model");
          statusLabel.setStyle("-fx-text-fill: red;");
          return;
        }
    
        if (type == null) {
          statusLabel.setText("Please select vehicle type");
          statusLabel.setStyle("-fx-text-fill: red;");
          return;
        }
    
        // Use Adapter Pattern for payment via Facade
        String cardNumber = cardField.getText();
        String cvv = cvvField.getText();
        double registrationFee = 500.0; // Base registration fee
    
        boolean paymentSuccess = rtoFacade.processPayment(cardNumber, cvv, registrationFee);
    
        if (paymentSuccess) {
          var vehicle = rtoFacade.registerVehicle(type, model, spec);
          if (vehicle != null) {
            double tax = vehicle.calculateTax();
            statusLabel.setText("Success! Vehicle Registered. Reg#: " + vehicle.getRegistrationNumber() + ", Tax: ₹" + tax);
            statusLabel.setStyle("-fx-text-fill: green;");
            clearFields();
          } else {
            statusLabel.setText("Registration Failed. Please check if you're logged in.");
            statusLabel.setStyle("-fx-text-fill: red;");
          }
        } else {
          statusLabel.setText("Payment Failed! Please check card details.");
          statusLabel.setStyle("-fx-text-fill: red;");
        }
    } catch (Exception e) {
        statusLabel.setText("Error: " + e.getMessage());
        statusLabel.setStyle("-fx-text-fill: red;");
        e.printStackTrace();
    }
  }

  private void clearFields() {
    modelField.clear();
    specField.clear();
    cardField.clear();
    cvvField.clear();
  }
}
