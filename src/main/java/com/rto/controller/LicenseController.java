package com.rto.controller;

import com.rto.service.RTOSystemFacade;
import com.rto.util.ValidationEngine;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.Node;

public class LicenseController {

  @FXML
  private ChoiceBox<String> licenseTypeBox;
  @FXML
  private TextField nameField;
  @FXML
  private TextField emailField;
  @FXML
  private TextField dNoField;
  @FXML
  private TextField addressLaneField;
  @FXML
  private TextField cityField;
  @FXML
  private TextField stateField;
  @FXML
  private TextField pincodeField;
  @FXML
  private ChoiceBox<String> bloodGroupBox;
  @FXML
  private Label statusLabel;

  // Per-field error labels
  @FXML
  private Label nameError;
  @FXML
  private Label emailError;
  @FXML
  private Label dNoError;
  @FXML
  private Label laneError;
  @FXML
  private Label cityError;
  @FXML
  private Label stateError;
  @FXML
  private Label pincodeError;

  private RTOSystemFacade rtoFacade;

  public void initialize() {
    rtoFacade = new RTOSystemFacade();

    licenseTypeBox.setItems(FXCollections.observableArrayList(
        "TWO_WHEELER", "FOUR_WHEELER", "COMMERCIAL"));
    licenseTypeBox.setValue("TWO_WHEELER");

    bloodGroupBox.setItems(FXCollections.observableArrayList(
        "A+", "A-", "B+", "B-", "O+", "O-", "AB+", "AB-"));
    bloodGroupBox.setValue("O+");

    // Remove error highlighting and hide per-field error when user types
    setupFieldListener(nameField, nameError);
    setupFieldListener(emailField, emailError);
    setupFieldListener(dNoField, dNoError);
    setupFieldListener(addressLaneField, laneError);
    setupFieldListener(cityField, cityError);
    setupFieldListener(stateField, stateError);
    setupFieldListener(pincodeField, pincodeError);
  }

  private void setupFieldListener(TextField field, Label errorLabel) {
      if (field != null) {
          field.textProperty().addListener((observable, oldValue, newValue) -> {
              field.getStyleClass().remove("field-error");
              if (errorLabel != null) {
                  errorLabel.setVisible(false);
                  errorLabel.setManaged(false);
              }
          });
      }
  }

  @FXML
  private void handleSubmit() {
    clearAllErrors();
    try {
        String type = licenseTypeBox.getValue();
        String name = nameField.getText().trim();
        String email = emailField.getText().trim();
        String dNo = dNoField.getText().trim();
        String lane = addressLaneField.getText().trim();
        String city = cityField.getText().trim();
        String state = stateField.getText().trim();
        String pincode = pincodeField.getText().trim();
        String bg = bloodGroupBox.getValue();

        boolean hasErrors = false;

        // 1. Name validation
        if (name.isEmpty()) {
            showFieldError(nameField, nameError, "Name is required.");
            hasErrors = true;
        }

        // 2. Email validation
        if (email.isEmpty()) {
            showFieldError(emailField, emailError, "Email is required.");
            hasErrors = true;
        } else if (!ValidationEngine.isValidEmail(email)) {
            showFieldError(emailField, emailError, "Invalid Email (must be a @gmail.com address).");
            hasErrors = true;
        }

        // 3. Door No validation
        if (dNo.isEmpty()) {
            showFieldError(dNoField, dNoError, "Door Number is required.");
            hasErrors = true;
        } else if (!ValidationEngine.isValidDoorNo(dNo)) {
            showFieldError(dNoField, dNoError, "Invalid Door Number (use 0-9, -, / only).");
            hasErrors = true;
        }

        // 4. Address Lane validation
        if (lane.isEmpty()) {
            showFieldError(addressLaneField, laneError, "Address Lane is required.");
            hasErrors = true;
        }

        // 5. City validation
        if (city.isEmpty()) {
            showFieldError(cityField, cityError, "City is required.");
            hasErrors = true;
        }

        // 6. State validation
        if (state.isEmpty()) {
            showFieldError(stateField, stateError, "State is required.");
            hasErrors = true;
        }

        // 7. Pincode validation
        if (pincode.isEmpty()) {
            showFieldError(pincodeField, pincodeError, "Pincode is required.");
            hasErrors = true;
        } else if (!pincode.matches("^[0-9]+$")) {
            showFieldError(pincodeField, pincodeError, "Pincode can only contain numbers.");
            hasErrors = true;
        } else if (pincode.length() != 6) {
            showFieldError(pincodeField, pincodeError, "Pincode must be exactly 6 digits (entered " + pincode.length() + ").");
            hasErrors = true;
        }

        // If any errors, show summary and stop
        if (hasErrors) {
            statusLabel.setText("❌ Please fix the highlighted errors above.");
            statusLabel.setStyle("-fx-text-fill: #DC3545;");
            return;
        }

        // Construct concatenated address
        String address = String.format("DNo: %s, Lane: %s, %s, %s - %s", dNo, lane, city, state, pincode);
    
        boolean success = rtoFacade.applyForLicense(type, name, email, address, bg);
    
        if (success) {
          statusLabel.setText("✅ Application Submitted Successfully!");
          statusLabel.setStyle("-fx-text-fill: #28A745;");
          clearFields();
        } else {
          statusLabel.setText("❌ Application Failed. Database error.");
          statusLabel.setStyle("-fx-text-fill: #DC3545;");
        }
    } catch (Exception e) {
        statusLabel.setText("❌ Error: " + e.getMessage());
        statusLabel.setStyle("-fx-text-fill: #DC3545;");
        e.printStackTrace();
    }
  }

  private void showFieldError(TextField field, Label errorLabel, String message) {
      field.getStyleClass().add("field-error");
      if (errorLabel != null) {
          errorLabel.setText("⚠ " + message);
          errorLabel.setVisible(true);
          errorLabel.setManaged(true);
      }
  }

  private void clearAllErrors() {
      TextField[] fields = {nameField, emailField, dNoField, addressLaneField, cityField, stateField, pincodeField};
      Label[] errors = {nameError, emailError, dNoError, laneError, cityError, stateError, pincodeError};
      for (TextField f : fields) {
          f.getStyleClass().remove("field-error");
      }
      for (Label lbl : errors) {
          if (lbl != null) {
              lbl.setVisible(false);
              lbl.setManaged(false);
          }
      }
      statusLabel.setText("");
  }

  private void clearFields() {
    nameField.clear();
    emailField.clear();
    dNoField.clear();
    addressLaneField.clear();
    cityField.clear();
    stateField.clear();
    pincodeField.clear();
  }
}
