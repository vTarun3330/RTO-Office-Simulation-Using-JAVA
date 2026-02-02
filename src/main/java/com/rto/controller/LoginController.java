package com.rto.controller;

import com.rto.Main;
import com.rto.model.User;
import com.rto.service.RTOSystemFacade;
import com.rto.service.SessionManager;
import com.rto.util.ValidationEngine;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import java.io.IOException;

/**
 * Login Controller - Handles user authentication.
 * Supports login and registration (toggled via link).
 */
public class LoginController {

  @FXML
  private TextField usernameField;
  @FXML
  private PasswordField passwordField;
  @FXML
  private Label errorLabel;

  private RTOSystemFacade rtoSystem;

  public void initialize() {
    rtoSystem = new RTOSystemFacade();
    errorLabel.setVisible(false);

    // Allow login on Enter key press in password field
    passwordField.setOnAction(event -> {
      try {
        handleLogin();
      } catch (IOException e) {
        e.printStackTrace();
      }
    });
  }

  @FXML
  private void handleLogin() throws IOException {
    try {
      String username = usernameField.getText().trim();
      String password = passwordField.getText();

      // Validate input
      if (username.isEmpty() || password.isEmpty()) {
        showError("Please enter username and password");
        return;
      }

      // Attempt login
      User loggedUser = rtoSystem.login(username, password);

      if (loggedUser != null) {
        SessionManager.getInstance().login(loggedUser);
        System.out.println("Login successful: " + loggedUser.getUsername() + " [" + loggedUser.getRole() + "]");
        Main.setRoot("Dashboard");
      } else {
        showError("Invalid username or password");
      }
    } catch (Exception e) {
      System.err.println("Login Error: " + e.getMessage());
      e.printStackTrace();
      showError("System Error: " + e.getMessage());
    }
  }

  @FXML
  private void handleRegister() {
    try {
      String username = usernameField.getText().trim();
      String password = passwordField.getText();

      // Validate input
      if (!ValidationEngine.isValidUsername(username)) {
        showError("Username must be 3-20 alphanumeric characters");
        return;
      }

      if (!ValidationEngine.isValidPassword(password)) {
        showError("Password must be at least 6 characters");
        return;
      }

      // Register user with a placeholder email (can be updated later)
      String email = username + "@rto.temp";
      boolean success = rtoSystem.registerUser(username, password, email);

      if (success) {
        showSuccess("Registration successful! You can now login.");
        passwordField.clear();
      } else {
        showError("Registration failed. Username may already exist.");
      }
    } catch (Exception e) {
      System.err.println("Registration Error: " + e.getMessage());
      e.printStackTrace();
      showError("System Error: " + e.getMessage());
    }
  }

  private void showError(String message) {
    errorLabel.setText(message);
    errorLabel.setStyle("-fx-text-fill: #E74C3C;");
    errorLabel.setVisible(true);
  }

  private void showSuccess(String message) {
    errorLabel.setText(message);
    errorLabel.setStyle("-fx-text-fill: #27AE60;");
    errorLabel.setVisible(true);
  }
}
