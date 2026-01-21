package com.rto.controller;

import com.rto.service.RTOSystemFacade;
import com.rto.service.SessionManager;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.TextArea;

public class LicenseController {

  @FXML
  private ChoiceBox<String> licenseTypeBox;
  @FXML
  private TextField nameField;
  @FXML
  private TextField emailField;
  @FXML
  private TextArea addressField;
  @FXML
  private ChoiceBox<String> bloodGroupBox;
  @FXML
  private Label statusLabel;

  private RTOSystemFacade rtoFacade;

  public void initialize() {
    rtoFacade = new RTOSystemFacade();

    licenseTypeBox.setItems(FXCollections.observableArrayList(
        "TWO_WHEELER", "FOUR_WHEELER", "COMMERCIAL"));
    licenseTypeBox.setValue("TWO_WHEELER");

    bloodGroupBox.setItems(FXCollections.observableArrayList(
        "A+", "A-", "B+", "B-", "O+", "O-", "AB+", "AB-"));
    bloodGroupBox.setValue("O+");

    // Pre-fill if known (optional, skipping for now to keep it simple)
    if (SessionManager.getInstance().isLoggedIn()) {
      nameField.setText(SessionManager.getInstance().getCurrentUser().getUsername());
    }
  }

  @FXML
  private void handleSubmit() {
    String type = licenseTypeBox.getValue();
    String name = nameField.getText();
    String email = emailField.getText();
    String address = addressField.getText();
    String bg = bloodGroupBox.getValue();

    if (address.isEmpty() || email.isEmpty()) {
      statusLabel.setText("Please fill all fields.");
      statusLabel.setStyle("-fx-text-fill: red;");
      return;
    }

    boolean success = rtoFacade.applyForLicense(type, name, email, address, bg);

    if (success) {
      statusLabel.setText("Application Submitted Successfully!");
      statusLabel.setStyle("-fx-text-fill: green;");
      clearFields();
    } else {
      statusLabel.setText("Application Failed.");
      statusLabel.setStyle("-fx-text-fill: red;");
    }
  }

  private void clearFields() {
    addressField.clear();
    emailField.clear();
  }
}
