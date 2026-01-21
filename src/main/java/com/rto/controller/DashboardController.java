package com.rto.controller;

import com.rto.Main;
import com.rto.service.SessionManager;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import java.io.IOException;

public class DashboardController {

  @FXML
  private Label welcomeLabel;
  @FXML
  private TabPane mainTabPane;
  @FXML
  private Tab vehicleTab;
  @FXML
  private Tab licenseTab;

  public void initialize() {
    if (SessionManager.getInstance().isLoggedIn()) {
      welcomeLabel.setText("Welcome, " + SessionManager.getInstance().getCurrentUser().getUsername() +
          " [" + SessionManager.getInstance().getCurrentUser().getRole() + "]");
    }

    // Load Vehicle Registration Form dynamically
    try {
      FXMLLoader loader = new FXMLLoader(
          getClass().getResource("/com/rto/view/RegistrationForm.fxml"));
      vehicleTab.setContent(loader.load());
    } catch (Exception e) {
      System.err.println("Error loading RegistrationForm: " + e.getMessage());
      e.printStackTrace();
    }

    // Load License Application Form dynamically
    try {
      FXMLLoader loader = new FXMLLoader(
          getClass().getResource("/com/rto/view/LicenseApplication.fxml"));
      licenseTab.setContent(loader.load());
    } catch (Exception e) {
      System.err.println("Error loading LicenseApplication: " + e.getMessage());
      e.printStackTrace();
    }
  }

  @FXML
  private void handleLogout() throws IOException {
    SessionManager.getInstance().logout();
    Main.setRoot("LoginView");
  }
}
