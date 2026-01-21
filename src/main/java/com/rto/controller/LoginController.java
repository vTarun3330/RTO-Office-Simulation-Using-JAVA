package com.rto.controller;

import com.rto.Main;
import com.rto.service.RTOSystemFacade;
import com.rto.service.SessionManager;
import com.rto.model.User;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import java.io.IOException;

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
  }

  @FXML
  private void handleLogin() throws IOException {
    String user = usernameField.getText();
    String pass = passwordField.getText();

    User loggedUser = rtoSystem.login(user, pass);

    if (loggedUser != null) {
      SessionManager.getInstance().login(loggedUser);
      Main.setRoot("Dashboard");
    } else {
      errorLabel.setText("Invalid Credentials");
    }
  }
}
