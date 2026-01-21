package com.rto.controller;

import com.rto.patterns.PaymentGatewayAdapter;
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
  }

  @FXML
  private void handleSubmit() {
    String model = modelField.getText();
    String type = typeBox.getValue();
    String spec = specField.getText();

    // Use Adapter Pattern for payment
    PaymentGatewayAdapter payment = new PaymentGatewayAdapter(cardField.getText(), cvvField.getText());
    boolean success = rtoFacade.payTax(payment, 500.0); // Dummy fee

    if (success) {
      var vehicle = rtoFacade.registerVehicle(type, model, spec);
      if (vehicle != null) {
        statusLabel.setText("Success! Vehicle Registered. Tax: " + vehicle.calculateTax());
        statusLabel.setStyle("-fx-text-fill: green;");
      } else {
        statusLabel.setText("Registration Failed.");
        statusLabel.setStyle("-fx-text-fill: red;");
      }
    } else {
      statusLabel.setText("Payment Failed!");
      statusLabel.setStyle("-fx-text-fill: red;");
    }
  }
}
