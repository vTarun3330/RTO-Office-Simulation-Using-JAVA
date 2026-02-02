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
    } else {
      // Add Citizen-specific tabs (My Requests to track status)
      setupCitizenTabs();
    }
  }

  private void setupCitizenTabs() {
    Tab myRequestsTab = new Tab("My Requests");
    myRequestsTab.setClosable(false);
    myRequestsTab.setContent(createMyRequestsPane());
    mainTabPane.getTabs().add(myRequestsTab);
  }

  @SuppressWarnings("unchecked")
  private VBox createMyRequestsPane() {
    VBox container = new VBox(15);
    container.setStyle("-fx-padding: 20;");

    Label header = new Label("My Vehicle Registration Requests");
    header.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

    Label infoLabel = new Label("Here you can track the status of your submitted vehicle registration requests.");
    infoLabel.setStyle("-fx-text-fill: #7F8C8D;");

    // Table for user's requests
    TableView<com.rto.model.VehicleRequest> table = new TableView<>();
    table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

    TableColumn<com.rto.model.VehicleRequest, String> idCol = new TableColumn<>("Request ID");
    idCol.setCellValueFactory(new PropertyValueFactory<>("requestId"));
    idCol.setStyle("-fx-alignment: CENTER;");

    TableColumn<com.rto.model.VehicleRequest, String> typeCol = new TableColumn<>("Type");
    typeCol.setCellValueFactory(new PropertyValueFactory<>("vehicleType"));
    typeCol.setStyle("-fx-alignment: CENTER;");

    TableColumn<com.rto.model.VehicleRequest, String> modelCol = new TableColumn<>("Model");
    modelCol.setCellValueFactory(new PropertyValueFactory<>("vehicleModel"));
    modelCol.setStyle("-fx-alignment: CENTER;");

    TableColumn<com.rto.model.VehicleRequest, String> specCol = new TableColumn<>("Spec");
    specCol.setCellValueFactory(new PropertyValueFactory<>("vehicleSpec"));
    specCol.setStyle("-fx-alignment: CENTER;");

    TableColumn<com.rto.model.VehicleRequest, String> statusCol = new TableColumn<>("Status");
    statusCol.setCellValueFactory(new PropertyValueFactory<>("status"));
    statusCol.setStyle("-fx-alignment: CENTER;");
    // Color-code the status
    statusCol.setCellFactory(col -> new TableCell<>() {
      @Override
      protected void updateItem(String status, boolean empty) {
        super.updateItem(status, empty);
        if (empty || status == null) {
          setText(null);
          setStyle("");
        } else {
          setText(status);
          switch (status) {
            case "PENDING" -> setStyle("-fx-text-fill: #F39C12; -fx-font-weight: bold;");
            case "APPROVED" -> setStyle("-fx-text-fill: #27AE60; -fx-font-weight: bold;");
            case "REJECTED" -> setStyle("-fx-text-fill: #E74C3C; -fx-font-weight: bold;");
            default -> setStyle("");
          }
        }
      }
    });

    table.getColumns().addAll(idCol, typeCol, modelCol, specCol, statusCol);
    refreshMyRequestsTable(table);

    Button refreshBtn = new Button("Refresh Status");
    refreshBtn.setOnAction(e -> refreshMyRequestsTable(table));

    container.getChildren().addAll(header, infoLabel, table, refreshBtn);
    return container;
  }

  private void refreshMyRequestsTable(TableView<com.rto.model.VehicleRequest> table) {
    java.util.List<com.rto.model.VehicleRequest> requests = rtoFacade.getMyVehicleRequests();
    table.setItems(FXCollections.observableArrayList(requests));
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
    // Applications Review Tab (License)
    applicationsTab = new Tab("Review Applications");
    applicationsTab.setClosable(false);
    applicationsTab.setContent(createApplicationsReviewPane());

    // Vehicle Requests Tab (NEW)
    Tab vehicleRequestsTab = new Tab("Vehicle Requests");
    vehicleRequestsTab.setClosable(false);
    vehicleRequestsTab.setContent(createVehicleRequestsPane());

    // Users Management Tab
    usersTab = new Tab("Manage Users");
    usersTab.setClosable(false);
    usersTab.setContent(createUsersManagementPane());

    // Challans Tab
    challansTab = new Tab("Issue Challan");
    challansTab.setClosable(false);
    challansTab.setContent(createChallansPane());

    mainTabPane.getTabs().addAll(applicationsTab, vehicleRequestsTab, usersTab, challansTab);
  }

  @SuppressWarnings("unchecked")
  private VBox createVehicleRequestsPane() {
    VBox container = new VBox(15);
    container.setStyle("-fx-padding: 20;");

    Label header = new Label("Pending Vehicle Registration Requests");
    header.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

    // Table for pending vehicle requests
    TableView<com.rto.model.VehicleRequest> table = new TableView<>();
    table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

    TableColumn<com.rto.model.VehicleRequest, String> idCol = new TableColumn<>("Request ID");
    idCol.setCellValueFactory(new PropertyValueFactory<>("requestId"));
    idCol.setStyle("-fx-alignment: CENTER;");

    TableColumn<com.rto.model.VehicleRequest, String> nameCol = new TableColumn<>("Applicant");
    nameCol.setCellValueFactory(new PropertyValueFactory<>("applicantName"));
    nameCol.setStyle("-fx-alignment: CENTER;");

    TableColumn<com.rto.model.VehicleRequest, String> typeCol = new TableColumn<>("Type");
    typeCol.setCellValueFactory(new PropertyValueFactory<>("vehicleType"));
    typeCol.setStyle("-fx-alignment: CENTER;");

    TableColumn<com.rto.model.VehicleRequest, String> modelCol = new TableColumn<>("Model");
    modelCol.setCellValueFactory(new PropertyValueFactory<>("vehicleModel"));
    modelCol.setStyle("-fx-alignment: CENTER;");

    TableColumn<com.rto.model.VehicleRequest, String> statusCol = new TableColumn<>("Status");
    statusCol.setCellValueFactory(new PropertyValueFactory<>("status"));
    statusCol.setStyle("-fx-alignment: CENTER;");

    TableColumn<com.rto.model.VehicleRequest, Void> actionCol = new TableColumn<>("Actions");
    actionCol.setPrefWidth(250);
    actionCol.setMinWidth(250);
    actionCol.setCellFactory(col -> new TableCell<>() {
      private final Button approveBtn = new Button("Approve");
      private final Button rejectBtn = new Button("Reject");
      {
        approveBtn.setMinWidth(90);
        approveBtn.setPrefWidth(90);
        rejectBtn.setMinWidth(80);
        rejectBtn.setPrefWidth(80);
        approveBtn.setStyle(
            "-fx-background-color: #27AE60; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 6 12; -fx-background-radius: 5;");
        rejectBtn.setStyle(
            "-fx-background-color: #E74C3C; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 6 12; -fx-background-radius: 5;");

        approveBtn.setOnAction(e -> {
          com.rto.model.VehicleRequest request = getTableView().getItems().get(getIndex());
          com.rto.model.Vehicle vehicle = rtoFacade.approveVehicleRequest(request.getRequestId());
          if (vehicle != null) {
            refreshVehicleRequestsTable(table);
            showAlert("Success", "Vehicle approved and registered: " + vehicle.getRegistrationNumber());
          }
        });

        rejectBtn.setOnAction(e -> {
          com.rto.model.VehicleRequest request = getTableView().getItems().get(getIndex());
          if (rtoFacade.rejectVehicleRequest(request.getRequestId())) {
            refreshVehicleRequestsTable(table);
            showAlert("Rejected", "Vehicle request has been rejected");
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

    table.getColumns().addAll(idCol, nameCol, typeCol, modelCol, statusCol, actionCol);
    refreshVehicleRequestsTable(table);

    Button refreshBtn = new Button("Refresh");
    refreshBtn.setOnAction(e -> refreshVehicleRequestsTable(table));

    container.getChildren().addAll(header, table, refreshBtn);
    return container;
  }

  private void refreshVehicleRequestsTable(TableView<com.rto.model.VehicleRequest> table) {
    java.util.List<com.rto.model.VehicleRequest> pending = rtoFacade.getPendingVehicleRequests();
    table.setItems(FXCollections.observableArrayList(pending));
  }

  private void showAlert(String title, String message) {
    javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.INFORMATION);
    alert.setTitle(title);
    alert.setHeaderText(null);
    alert.setContentText(message);
    alert.showAndWait();
  }

  @SuppressWarnings("unchecked")
  private VBox createApplicationsReviewPane() {
    VBox container = new VBox(15);
    container.setStyle("-fx-padding: 20;");

    Label header = new Label("Pending License Applications");
    header.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

    // Table for pending applications
    TableView<License> table = new TableView<>();
    table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

    TableColumn<License, String> idCol = new TableColumn<>("License ID");
    idCol.setCellValueFactory(new PropertyValueFactory<>("licenseId"));
    idCol.setStyle("-fx-alignment: CENTER;");

    TableColumn<License, String> nameCol = new TableColumn<>("Applicant");
    nameCol.setCellValueFactory(new PropertyValueFactory<>("applicantName"));
    nameCol.setStyle("-fx-alignment: CENTER;");

    TableColumn<License, String> typeCol = new TableColumn<>("Type");
    typeCol.setCellValueFactory(new PropertyValueFactory<>("licenseType"));
    typeCol.setStyle("-fx-alignment: CENTER;");

    TableColumn<License, String> statusCol = new TableColumn<>("Status");
    statusCol.setCellValueFactory(new PropertyValueFactory<>("status"));
    statusCol.setStyle("-fx-alignment: CENTER;");

    TableColumn<License, Void> actionCol = new TableColumn<>("Actions");
    actionCol.setPrefWidth(250);
    actionCol.setMinWidth(250);
    actionCol.setCellFactory(col -> new TableCell<>() {
      private final Button approveBtn = new Button("Approve");
      private final Button rejectBtn = new Button("Reject");
      {
        approveBtn.setMinWidth(90);
        approveBtn.setPrefWidth(90);
        rejectBtn.setMinWidth(80);
        rejectBtn.setPrefWidth(80);
        approveBtn.setStyle(
            "-fx-background-color: #27AE60; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 6 12; -fx-background-radius: 5;");
        rejectBtn.setStyle(
            "-fx-background-color: #E74C3C; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 6 12; -fx-background-radius: 5;");

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

    Label infoLabel = new Label("Double-click on a user to view their details.");
    infoLabel.setStyle("-fx-text-fill: #7F8C8D;");

    TableView<User> table = new TableView<>();
    table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

    TableColumn<User, String> idCol = new TableColumn<>("ID");
    idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
    idCol.setStyle("-fx-alignment: CENTER;");

    TableColumn<User, String> usernameCol = new TableColumn<>("Username");
    usernameCol.setCellValueFactory(new PropertyValueFactory<>("username"));
    usernameCol.setStyle("-fx-alignment: CENTER;");

    TableColumn<User, String> roleCol = new TableColumn<>("Role");
    roleCol.setCellValueFactory(new PropertyValueFactory<>("role"));
    roleCol.setStyle("-fx-alignment: CENTER;");

    table.getColumns().addAll(idCol, usernameCol, roleCol);

    // Double-click handler to show user details
    table.setRowFactory(tv -> {
      javafx.scene.control.TableRow<User> row = new javafx.scene.control.TableRow<>();
      row.setOnMouseClicked(event -> {
        if (event.getClickCount() == 2 && !row.isEmpty()) {
          User selectedUser = row.getItem();
          showUserDetailsDialog(selectedUser, table);
        }
      });
      return row;
    });

    List<User> citizens = rtoFacade.getAllCitizens();
    table.setItems(FXCollections.observableArrayList(citizens));

    Button refreshBtn = new Button("Refresh");
    refreshBtn.setOnAction(e -> {
      List<User> updated = rtoFacade.getAllCitizens();
      table.setItems(FXCollections.observableArrayList(updated));
    });

    container.getChildren().addAll(header, infoLabel, table, refreshBtn);
    return container;
  }

  @SuppressWarnings("unchecked")
  private void showUserDetailsDialog(User user, TableView<User> parentTable) {
    javafx.scene.control.Dialog<Void> dialog = new javafx.scene.control.Dialog<>();
    dialog.setTitle("User Details - " + user.getUsername());
    dialog.setHeaderText("Complete information for: " + user.getUsername());

    // Dialog content
    javafx.scene.control.TabPane tabPane = new javafx.scene.control.TabPane();
    tabPane.setPrefSize(700, 400);

    // === User Info Tab ===
    Tab infoTab = new Tab("User Info");
    infoTab.setClosable(false);
    VBox infoBox = new VBox(10);
    infoBox.setStyle("-fx-padding: 15;");

    javafx.scene.control.TextField emailField = new javafx.scene.control.TextField(
        user.getEmail() != null ? user.getEmail() : "");
    emailField.setPromptText("Email");
    javafx.scene.control.TextField phoneField = new javafx.scene.control.TextField(
        user.getPhone() != null ? user.getPhone() : "");
    phoneField.setPromptText("Phone");
    javafx.scene.control.TextField fullNameField = new javafx.scene.control.TextField(
        user.getFullName() != null ? user.getFullName() : "");
    fullNameField.setPromptText("Full Name");

    infoBox.getChildren().addAll(
        new Label("User ID: " + user.getId()),
        new Label("Username: " + user.getUsername()),
        new Label("Role: " + user.getRole()),
        new javafx.scene.layout.HBox(10, new Label("Email:"), emailField),
        new javafx.scene.layout.HBox(10, new Label("Phone:"), phoneField),
        new javafx.scene.layout.HBox(10, new Label("Full Name:"), fullNameField));
    infoTab.setContent(infoBox);

    // === Vehicles Tab ===
    Tab vehiclesTab = new Tab("Vehicles");
    vehiclesTab.setClosable(false);
    TableView<com.rto.model.Vehicle> vehiclesTable = new TableView<>();
    vehiclesTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

    TableColumn<com.rto.model.Vehicle, String> vRegCol = new TableColumn<>("Reg. No.");
    vRegCol.setCellValueFactory(new PropertyValueFactory<>("registrationNumber"));
    TableColumn<com.rto.model.Vehicle, String> vModelCol = new TableColumn<>("Model");
    vModelCol.setCellValueFactory(new PropertyValueFactory<>("model"));
    TableColumn<com.rto.model.Vehicle, String> vTypeCol = new TableColumn<>("Type");
    vTypeCol.setCellValueFactory(new PropertyValueFactory<>("type"));

    vehiclesTable.getColumns().addAll(vRegCol, vModelCol, vTypeCol);
    java.util.List<com.rto.model.Vehicle> vehicles = rtoFacade.getVehiclesByUserId(user.getId());
    vehiclesTable.setItems(FXCollections.observableArrayList(vehicles));
    vehiclesTab.setContent(vehiclesTable);

    // === Licenses Tab ===
    Tab licensesTab = new Tab("Licenses");
    licensesTab.setClosable(false);
    TableView<com.rto.model.License> licensesTable = new TableView<>();
    licensesTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

    TableColumn<com.rto.model.License, String> lIdCol = new TableColumn<>("License ID");
    lIdCol.setCellValueFactory(new PropertyValueFactory<>("licenseId"));
    TableColumn<com.rto.model.License, String> lTypeCol = new TableColumn<>("Type");
    lTypeCol.setCellValueFactory(new PropertyValueFactory<>("licenseType"));
    TableColumn<com.rto.model.License, String> lStatusCol = new TableColumn<>("Status");
    lStatusCol.setCellValueFactory(new PropertyValueFactory<>("status"));

    licensesTable.getColumns().addAll(lIdCol, lTypeCol, lStatusCol);
    java.util.List<com.rto.model.License> licenses = rtoFacade.getLicensesByUserId(user.getId());
    licensesTable.setItems(FXCollections.observableArrayList(licenses));
    licensesTab.setContent(licensesTable);

    // === Challans Tab ===
    Tab challansTab = new Tab("Challans");
    challansTab.setClosable(false);
    TableView<com.rto.model.Challan> challansTable = new TableView<>();
    challansTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

    TableColumn<com.rto.model.Challan, String> cIdCol = new TableColumn<>("Challan ID");
    cIdCol.setCellValueFactory(new PropertyValueFactory<>("challanId"));
    TableColumn<com.rto.model.Challan, String> cOffenseCol = new TableColumn<>("Offense");
    cOffenseCol.setCellValueFactory(new PropertyValueFactory<>("offenseType"));
    TableColumn<com.rto.model.Challan, Double> cAmountCol = new TableColumn<>("Amount");
    cAmountCol.setCellValueFactory(new PropertyValueFactory<>("amount"));
    TableColumn<com.rto.model.Challan, Boolean> cPaidCol = new TableColumn<>("Paid");
    cPaidCol.setCellValueFactory(new PropertyValueFactory<>("paid"));

    challansTable.getColumns().addAll(cIdCol, cOffenseCol, cAmountCol, cPaidCol);
    java.util.List<com.rto.model.Challan> challans = rtoFacade.getChallansByUserId(user.getId());
    challansTable.setItems(FXCollections.observableArrayList(challans));
    challansTab.setContent(challansTable);

    tabPane.getTabs().addAll(infoTab, vehiclesTab, licensesTab, challansTab);

    // Dialog buttons
    javafx.scene.control.ButtonType saveButtonType = new javafx.scene.control.ButtonType("Save",
        javafx.scene.control.ButtonBar.ButtonData.OK_DONE);
    dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, javafx.scene.control.ButtonType.CLOSE);

    dialog.getDialogPane().setContent(tabPane);

    // Handle save button
    dialog.setResultConverter(buttonType -> {
      if (buttonType == saveButtonType) {
        boolean updated = rtoFacade.updateUserInfo(
            user.getId(),
            emailField.getText().trim(),
            phoneField.getText().trim(),
            fullNameField.getText().trim());
        if (updated) {
          showAlert("Success", "User information updated successfully!");
          // Refresh parent table
          List<User> refreshed = rtoFacade.getAllCitizens();
          parentTable.setItems(FXCollections.observableArrayList(refreshed));
        } else {
          showAlert("Error", "Failed to update user information.");
        }
      }
      return null;
    });

    dialog.showAndWait();
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
