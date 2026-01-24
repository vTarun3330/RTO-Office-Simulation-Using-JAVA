package com.rto.controller;

import com.rto.Main;
import com.rto.model.*;
import com.rto.service.*;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import java.io.IOException;
import java.util.List;

/**
 * Dashboard Controller - Main application screen after login.
 * Shows different content based on user role (Admin vs Citizen).
 */
public class DashboardController {

  @FXML
  private Label welcomeLabel;
  @FXML
  private TabPane mainTabPane;
  @FXML
  private Tab vehicleTab;
  @FXML
  private Tab licenseTab;
  @FXML
  private Tab transferTab;
  @FXML
  private Tab cbtTab;

  // Admin-specific tabs (created dynamically)
  private Tab applicationsTab;
  private Tab usersTab;
  private Tab challansTab;

  private RTOSystemFacade rtoFacade;
  private boolean isAdmin;

  public void initialize() {
    rtoFacade = new RTOSystemFacade();

    User currentUser = SessionManager.getInstance().getCurrentUser();
    if (currentUser != null) {
      welcomeLabel.setText("Welcome, " + currentUser.getUsername() + " [" + currentUser.getRole() + "]");
      isAdmin = "ADMIN".equalsIgnoreCase(currentUser.getRole());
    } else {
      welcomeLabel.setText("Welcome, Guest");
      isAdmin = false;
    }

    // Load Vehicle Registration Form
    loadTabContent(vehicleTab, "/com/rto/view/RegistrationForm.fxml");

    // Load License Application Form
    loadTabContent(licenseTab, "/com/rto/view/LicenseApplication.fxml");
    
    // Load Transfer Form (Phase 1 Feature)
    loadTabContent(transferTab, "/com/rto/view/TransferView.fxml");
    
    // Load CBT Test (Phase 2 Feature)
    loadTabContent(cbtTab, "/com/rto/view/CBTView.fxml");

    // Add Admin-specific tabs if user is admin
    if (isAdmin) {
      setupAdminTabs();
    }
  }

  private void loadTabContent(Tab tab, String fxmlPath) {
    try {
      FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
      tab.setContent(loader.load());
    } catch (Exception e) {
      System.err.println("Error loading " + fxmlPath + ": " + e.getMessage());
      tab.setContent(new Label("Failed to load content"));
    }
  }

  private void setupAdminTabs() {
    // Applications Review Tab
    applicationsTab = new Tab("Review Applications");
    applicationsTab.setClosable(false);
    applicationsTab.setContent(createApplicationsReviewPane());

    // Users Management Tab
    usersTab = new Tab("Manage Users");
    usersTab.setClosable(false);
    usersTab.setContent(createUsersManagementPane());

    // Challans Tab
    challansTab = new Tab("Issue Challan");
    challansTab.setClosable(false);
    challansTab.setContent(createChallansPane());

    mainTabPane.getTabs().addAll(applicationsTab, usersTab, challansTab);
  }

  @SuppressWarnings("unchecked")
  private VBox createApplicationsReviewPane() {
    VBox container = new VBox(15);
    container.setStyle("-fx-padding: 20;");

    Label header = new Label("Pending License Applications");
    header.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

    // Table for pending applications
    TableView<License> table = new TableView<>();

    TableColumn<License, String> idCol = new TableColumn<>("License ID");
    idCol.setCellValueFactory(new PropertyValueFactory<>("licenseId"));
    idCol.setPrefWidth(150);

    TableColumn<License, String> nameCol = new TableColumn<>("Applicant");
    nameCol.setCellValueFactory(new PropertyValueFactory<>("applicantName"));
    nameCol.setPrefWidth(150);

    TableColumn<License, String> typeCol = new TableColumn<>("Type");
    typeCol.setCellValueFactory(new PropertyValueFactory<>("licenseType"));
    typeCol.setPrefWidth(100);

    TableColumn<License, String> statusCol = new TableColumn<>("Status");
    statusCol.setCellValueFactory(new PropertyValueFactory<>("status"));
    statusCol.setPrefWidth(100);

    TableColumn<License, Void> actionCol = new TableColumn<>("Actions");
    actionCol.setPrefWidth(200);
    actionCol.setCellFactory(col -> new TableCell<>() {
      private final Button approveBtn = new Button("Approve");
      private final Button rejectBtn = new Button("Reject");
      {
        approveBtn.setStyle("-fx-background-color: #27AE60; -fx-text-fill: white;");
        rejectBtn.setStyle("-fx-background-color: #E74C3C; -fx-text-fill: white;");

        approveBtn.setOnAction(e -> {
          License license = getTableView().getItems().get(getIndex());
          if (rtoFacade.approveLicense(license.getLicenseId())) {
            refreshApplicationsTable(table);
          }
        });

        rejectBtn.setOnAction(e -> {
          License license = getTableView().getItems().get(getIndex());
          if (rtoFacade.rejectLicense(license.getLicenseId())) {
            refreshApplicationsTable(table);
          }
        });
      }

      @Override
      protected void updateItem(Void item, boolean empty) {
        super.updateItem(item, empty);
        if (empty) {
          setGraphic(null);
        } else {
          javafx.scene.layout.HBox hbox = new javafx.scene.layout.HBox(5, approveBtn, rejectBtn);
          setGraphic(hbox);
        }
      }
    });

    table.getColumns().addAll(idCol, nameCol, typeCol, statusCol, actionCol);
    refreshApplicationsTable(table);

    Button refreshBtn = new Button("Refresh");
    refreshBtn.setOnAction(e -> refreshApplicationsTable(table));

    container.getChildren().addAll(header, table, refreshBtn);
    return container;
  }

  private void refreshApplicationsTable(TableView<License> table) {
    List<License> pending = rtoFacade.getPendingApplications();
    table.setItems(FXCollections.observableArrayList(pending));
  }

  @SuppressWarnings("unchecked")
  private VBox createUsersManagementPane() {
    VBox container = new VBox(15);
    container.setStyle("-fx-padding: 20;");

    Label header = new Label("Registered Citizens");
    header.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

    TableView<User> table = new TableView<>();

    TableColumn<User, String> idCol = new TableColumn<>("ID");
    idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
    idCol.setPrefWidth(150);

    TableColumn<User, String> usernameCol = new TableColumn<>("Username");
    usernameCol.setCellValueFactory(new PropertyValueFactory<>("username"));
    usernameCol.setPrefWidth(150);

    TableColumn<User, String> roleCol = new TableColumn<>("Role");
    roleCol.setCellValueFactory(new PropertyValueFactory<>("role"));
    roleCol.setPrefWidth(100);

    table.getColumns().addAll(idCol, usernameCol, roleCol);

    List<User> citizens = rtoFacade.getAllCitizens();
    table.setItems(FXCollections.observableArrayList(citizens));

    Button refreshBtn = new Button("Refresh");
    refreshBtn.setOnAction(e -> {
      List<User> updated = rtoFacade.getAllCitizens();
      table.setItems(FXCollections.observableArrayList(updated));
    });

    container.getChildren().addAll(header, table, refreshBtn);
    return container;
  }

  private VBox createChallansPane() {
    VBox container = new VBox(15);
    container.setStyle("-fx-padding: 20;");

    Label header = new Label("Issue Traffic Challan");
    header.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

    TextField vinField = new TextField();
    vinField.setPromptText("Vehicle Registration Number");

    ChoiceBox<String> offenseBox = new ChoiceBox<>(FXCollections.observableArrayList(
        "Speeding", "Signal Violation", "No Helmet", "No Seatbelt",
        "Wrong Parking", "Drunk Driving", "No Insurance", "No License"));
    offenseBox.setValue("Speeding");

    TextField amountField = new TextField();
    amountField.setPromptText("Fine Amount (₹)");

    Label statusLabel = new Label();

    Button issueBtn = new Button("Issue Challan");
    issueBtn.setStyle("-fx-background-color: #E74C3C; -fx-text-fill: white;");
    issueBtn.setOnAction(e -> {
      try {
          String vin = vinField.getText();
          String offense = offenseBox.getValue();
          String amountStr = amountField.getText();
    
          if (vin.isEmpty() || amountStr.isEmpty()) {
            statusLabel.setText("Please fill all fields");
            statusLabel.setStyle("-fx-text-fill: red;");
            return;
          }
    
          try {
            double amount = Double.parseDouble(amountStr);
            Challan challan = new Challan(vin, offense, amount,
                SessionManager.getInstance().getCurrentUser().getId());
    
            ChallanService challanService = new ChallanService();
            if (challanService.issueChallan(challan)) {
              statusLabel.setText("Challan issued: " + challan.getChallanId());
              statusLabel.setStyle("-fx-text-fill: green;");
              vinField.clear();
              amountField.clear();
            } else {
              statusLabel.setText("Failed to issue challan");
              statusLabel.setStyle("-fx-text-fill: red;");
            }
          } catch (NumberFormatException ex) {
            statusLabel.setText("Invalid amount");
            statusLabel.setStyle("-fx-text-fill: red;");
          }
      } catch (Exception ex) {
          statusLabel.setText("Error: " + ex.getMessage());
          statusLabel.setStyle("-fx-text-fill: red;");
          ex.printStackTrace();
      }
    });

    container.getChildren().addAll(header,
        new Label("Vehicle Registration:"), vinField,
        new Label("Offense Type:"), offenseBox,
        new Label("Fine Amount:"), amountField,
        issueBtn, statusLabel);

    return container;
  }

  @FXML
  private void handleLogout() throws IOException {
    try {
        SessionManager.getInstance().logout();
        Main.setRoot("LoginView");
    } catch (Exception e) {
        System.err.println("Logout failed: " + e.getMessage());
        e.printStackTrace();
    }
  }
}
