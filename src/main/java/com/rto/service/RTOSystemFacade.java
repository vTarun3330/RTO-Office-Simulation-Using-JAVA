package com.rto.service;

import com.rto.model.*;
import com.rto.patterns.*;
import com.rto.util.ValidationEngine;

import java.util.List;

/**
 * Facade Pattern - The Central Controller.
 * UI controllers ONLY talk to this Facade.
 * The Facade orchestrates the Services and Database.
 */
public class RTOSystemFacade {
  private SessionManager session;
  private UserService userService;
  private VehicleService vehicleService;
  private LicenseService licenseService;
  private TransactionService transactionService;

  public RTOSystemFacade() {
    this.session = SessionManager.getInstance();
    this.userService = ServiceFactory.getUserService();
    this.vehicleService = ServiceFactory.getVehicleService();
    this.licenseService = ServiceFactory.getLicenseService();
    this.transactionService = ServiceFactory.getTransactionService();
  }

  // ==================== AUTHENTICATION ====================

  /**
   * Authenticate user and log them in.
   */
  public User login(String username, String password) {
    User user = userService.authenticate(username, password);
    if (user != null) {
      session.login(user);
    }
    return user;
  }

  /**
   * Logout current user.
   */
  public void logout() {
    session.logout();
  }

  /**
   * Register a new citizen user.
   */
  /**
   * Register a new citizen user.
   */
  public boolean registerUser(String username, String password, String email) {
    try {
      if (!ValidationEngine.isValidUsername(username)) {
        System.err.println("❌ Validation Error: Invalid username format");
        return false;
      }
      if (!ValidationEngine.isValidPassword(password)) {
        System.err.println("❌ Validation Error: Password must be at least 6 characters");
        return false;
      }
      if (!ValidationEngine.isValidEmail(email)) {
        System.err.println("❌ Validation Error: Invalid email format");
        return false;
      }
      return userService.registerUser(username, password, email);
    } catch (Exception e) {
      System.err.println("❌ CRITICAL ERROR in Facade (Register): " + e.getMessage());
      e.printStackTrace();
      return false;
    }
  }

  /**
   * Get current logged-in user.
   */
  public User getCurrentUser() {
    return session.getCurrentUser();
  }

  /**
   * Check if user is logged in.
   */
  public boolean isLoggedIn() {
    return session.isLoggedIn();
  }

  /**
   * Check if current user is admin.
   */
  public boolean isAdmin() {
    return isLoggedIn() && "ADMIN".equalsIgnoreCase(getCurrentUser().getRole());
  }

  // ==================== VEHICLE OPERATIONS ====================

  /**
   * Register a new vehicle using Builder pattern.
   */
  public Vehicle registerVehicle(String type, String model, String spec) {
    try {
      if (!isLoggedIn()) {
        System.out.println("⚠️  User must be logged in to register a vehicle");
        return null;
      }

      try {
        Vehicle vehicle = new VehicleBuilder()
            .setOwnerId(getCurrentUser().getId())
            .setType(type)
            .setModel(model)
            .setExtraData(spec)
            .build();

        if (vehicleService.registerVehicle(vehicle)) {
          return vehicle;
        }
      } catch (IllegalArgumentException e) {
        System.err.println("❌ Vehicle Builder Error: " + e.getMessage());
      }
    } catch (Exception e) {
      System.err.println("❌ CRITICAL ERROR in Facade (RegisterVehicle): " + e.getMessage());
      e.printStackTrace();
    }
    return null;
  }

  /**
   * Get all vehicles for the current user.
   */
  public List<Vehicle> getMyVehicles() {
    if (!isLoggedIn())
      return List.of();
    return vehicleService.getVehiclesByOwnerId(getCurrentUser().getId());
  }

  /**
   * Get all vehicles (Admin only).
   */
  public List<Vehicle> getAllVehicles() {
    if (!isAdmin())
      return List.of();
    return vehicleService.getAllVehicles();
  }

  /**
   * Search vehicles by model.
   */
  public List<Vehicle> searchVehicles(String model) {
    return vehicleService.searchVehiclesByModel(model);
  }

  /**
   * Get vehicle by registration number.
   */
  public Vehicle getVehicle(String registrationNumber) {
    return vehicleService.getVehicleByRegistration(registrationNumber);
  }

  // ==================== LICENSE OPERATIONS ====================

  /**
   * Apply for a new license with validation.
   */
  /**
   * Apply for a new license with validation.
   */
  public boolean applyForLicense(String type, String name, String email, String address, String bloodGroup) {
    try {
      if (!isLoggedIn()) {
        System.out.println("⚠️  User must be logged in to apply for a license");
        return false;
      }

      if (!ValidationEngine.isValidEmail(email)) {
        System.err.println("❌ Validation Error: Invalid email format");
        return false;
      }

      License license = new License(
          getCurrentUser().getId(),
          type,
          name,
          email,
          address,
          bloodGroup);

      return licenseService.applyForLicense(license);
    } catch (Exception e) {
      System.err.println("❌ CRITICAL ERROR in Facade (ApplyLicense): " + e.getMessage());
      e.printStackTrace();
      return false;
    }
  }

  /**
   * Get all licenses for the current user.
   */
  public List<License> getMyLicenses() {
    if (!isLoggedIn())
      return List.of();
    return licenseService.getLicensesByUserId(getCurrentUser().getId());
  }

  /**
   * Get pending applications (Admin only).
   */
  public List<License> getPendingApplications() {
    if (!isAdmin())
      return List.of();
    return licenseService.getPendingApplications();
  }

  /**
   * Approve a license application (Admin only).
   */
  public boolean approveLicense(String licenseId) {
    if (!isAdmin()) {
      System.out.println("Only admin can approve licenses");
      return false;
    }
    return licenseService.approveLicense(licenseId);
  }

  /**
   * Reject a license application (Admin only).
   */
  public boolean rejectLicense(String licenseId) {
    if (!isAdmin()) {
      System.out.println("Only admin can reject licenses");
      return false;
    }
    return licenseService.rejectLicense(licenseId);
  }

  // ==================== PAYMENT OPERATIONS ====================

  /**
   * Process a payment using the Adapter pattern.
   */
  public boolean processPayment(String cardNumber, String cvv, double amount) {
    if (!isLoggedIn())
      return false;

    IPaymentProcessor processor = new PaymentGatewayAdapter(cardNumber, cvv);
    boolean success = processor.processPayment(amount);

    if (success) {
      transactionService.recordTransaction(
          getCurrentUser().getId(),
          amount,
          "CARD",
          "TAX_PAYMENT",
          null);
    }

    return success;
  }

  /**
   * Calculate tax for a vehicle using Strategy pattern.
   */
  public double calculateTax(Vehicle vehicle) {
    if (vehicle == null)
      return 0.0;

    TaxCalculationStrategy strategy = switch (vehicle.getType().toUpperCase()) {
      case "CAR" -> new StandardTaxStrategy();
      case "BIKE" -> new PremiumTaxStrategy();
      case "TRUCK" -> new CommercialTaxStrategy();
      default -> new StandardTaxStrategy();
    };
    return strategy.calculateTax(vehicle);
  }

  /**
   * Calculate tax by vehicle type and value (simplified version without
   * Strategy).
   */
  public double calculateTaxByValue(String vehicleType, double vehicleValue) {
    return switch (vehicleType.toUpperCase()) {
      case "CAR" -> vehicleValue * 0.10; // 10% for cars
      case "BIKE" -> vehicleValue * 0.05; // 5% for bikes
      case "TRUCK" -> vehicleValue * 0.12; // 12% for trucks
      default -> vehicleValue * 0.10;
    };
  }

  // ==================== ADMIN OPERATIONS ====================

  /**
   * Get all citizens (Admin only).
   */
  public List<User> getAllCitizens() {
    if (!isAdmin())
      return List.of();
    return userService.getAllCitizens();
  }

  /**
   * Get all licenses (Admin only).
   */
  public List<License> getAllLicenses() {
    if (!isAdmin())
      return List.of();
    return licenseService.getAllLicenses();
  }

  // ==================== TRANSFER APPROVAL OPERATIONS (Admin) ====================

  /**
   * Get all pending transfer requests awaiting admin approval.
   */
  public java.util.List<TransferService.TransferRequest> getPendingTransfers() {
    if (!isAdmin()) return java.util.List.of();
    TransferService transferService = new TransferService();
    return transferService.getAllPendingTransfers();
  }

  /**
   * Approve a transfer request (Admin only).
   * This updates the vehicle's owner_id in the vehicles table.
   */
  public boolean approveTransfer(String transferId) {
    if (!isAdmin()) {
      System.out.println("Only admin can approve transfers");
      return false;
    }
    TransferService transferService = new TransferService();
    return transferService.approveTransfer(transferId, getCurrentUser().getId());
  }

  /**
   * Reject a transfer request (Admin only).
   */
  public boolean rejectTransfer(String transferId, String reason) {
    if (!isAdmin()) {
      System.out.println("Only admin can reject transfers");
      return false;
    }
    TransferService transferService = new TransferService();
    return transferService.rejectTransfer(transferId, getCurrentUser().getId(), reason);
  }

  // ==================== VEHICLE REQUEST OPERATIONS (Approval Workflow)
  // ====================

  /**
   * Submit a vehicle registration request (for citizens).
   * Admins should use registerVehicle() directly.
   */
  public boolean submitVehicleRequest(String type, String model, String spec) {
    if (!isLoggedIn()) {
      System.out.println("⚠️  User must be logged in to submit a vehicle request");
      return false;
    }

    User currentUser = getCurrentUser();
    VehicleRequest request = new VehicleRequest(
        currentUser.getId(),
        currentUser.getUsername(),
        type,
        model,
        spec);

    return vehicleService.submitVehicleRequest(request);
  }

  /**
   * Get all pending vehicle registration requests (Admin only).
   */
  public List<VehicleRequest> getPendingVehicleRequests() {
    if (!isAdmin())
      return List.of();
    return vehicleService.getPendingVehicleRequests();
  }

  /**
   * Approve a vehicle registration request (Admin only).
   */
  public Vehicle approveVehicleRequest(String requestId) {
    if (!isAdmin()) {
      System.out.println("Only admin can approve vehicle requests");
      return null;
    }
    return vehicleService.approveVehicleRequest(requestId, getCurrentUser().getId());
  }

  /**
   * Reject a vehicle registration request (Admin only).
   */
  public boolean rejectVehicleRequest(String requestId) {
    if (!isAdmin()) {
      System.out.println("Only admin can reject vehicle requests");
      return false;
    }
    return vehicleService.rejectVehicleRequest(requestId, getCurrentUser().getId());
  }

  /**
   * Get vehicle requests for the current logged-in citizen.
   */
  public List<VehicleRequest> getMyVehicleRequests() {
    if (!isLoggedIn())
      return List.of();
    return vehicleService.getVehicleRequestsByApplicant(getCurrentUser().getId());
  }

  /**
   * Get all challans for the current logged-in citizen's vehicles.
   */
  public List<Challan> getMyChallans() {
    if (!isLoggedIn())
      return List.of();
    List<Vehicle> vehicles = vehicleService.getVehiclesByOwnerId(getCurrentUser().getId());
    List<Challan> allChallans = new java.util.ArrayList<>();
    ChallanService challanService = new ChallanService();
    for (Vehicle v : vehicles) {
      allChallans.addAll(challanService.getChallansByVehicle(v.getRegistrationNumber()));
    }
    return allChallans;
  }

  /**
   * Pay a challan fine for the current citizen.
   */
  public boolean payMyChallan(String challanId) {
    if (!isLoggedIn())
      return false;
    ChallanService challanService = new ChallanService();
    Challan challan = challanService.getChallanById(challanId);
    if (challan == null || challan.isPaid()) {
      return false;
    }
    // Record transaction
    String txnId = "TXN-" + System.currentTimeMillis();
    transactionService.recordTransaction(getCurrentUser().getId(), challan.getAmount(), "UPI", "CHALLAN_PAYMENT", challanId);
    return challanService.payChallan(challanId, txnId);
  }

  // ==================== USER DETAILS (Admin) ====================

  /**
   * Get vehicles for a specific user (Admin only).
   */
  public List<Vehicle> getVehiclesByUserId(String userId) {
    if (!isAdmin())
      return List.of();
    return vehicleService.getVehiclesByOwnerId(userId);
  }

  /**
   * Get licenses for a specific user (Admin only).
   */
  public List<License> getLicensesByUserId(String userId) {
    if (!isAdmin())
      return List.of();
    return licenseService.getLicensesByUserId(userId);
  }

  /**
   * Get challans for a specific user (Admin only) - via their vehicles.
   */
  public List<Challan> getChallansByUserId(String userId) {
    if (!isAdmin())
      return List.of();
    // Get all vehicles owned by user, then get challans for each
    List<Vehicle> vehicles = vehicleService.getVehiclesByOwnerId(userId);
    List<Challan> allChallans = new java.util.ArrayList<>();
    ChallanService challanService = new ChallanService();
    for (Vehicle v : vehicles) {
      allChallans.addAll(challanService.getChallansByVehicle(v.getRegistrationNumber()));
    }
    return allChallans;
  }

  /**
   * Update user info (Admin only).
   */
  public boolean updateUserInfo(String userId, String email, String phone, String fullName) {
    if (!isAdmin()) {
      System.out.println("Only admin can update user info");
      return false;
    }
    return userService.updateUserInfo(userId, email, phone, fullName);
  }

  /**
   * Delete a user (Admin only).
   */
  public boolean deleteUser(String userId) {
    if (!isAdmin()) {
      System.out.println("Only admin can delete users");
      return false;
    }
    return userService.deleteUser(userId);
  }
}
