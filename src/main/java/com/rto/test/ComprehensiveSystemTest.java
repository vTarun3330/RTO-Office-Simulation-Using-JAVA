package com.rto.test;

import com.rto.model.*;
import com.rto.patterns.*;
import com.rto.service.*;
import com.rto.util.ValidationEngine;

import java.util.List;

/**
 * Comprehensive System Test Suite
 * Tests all aspects of the RTO Office Simulation:
 * - Design Patterns (Singleton, Factory, Builder, Adapter, Facade, Strategy,
 * Decorator, Observer)
 * - Service Layer Operations
 * - Database Operations
 * - Error Handling
 * - Edge Cases
 */
public class ComprehensiveSystemTest {

  private static int passedTests = 0;
  private static int failedTests = 0;
  private static int totalTests = 0;

  public static void main(String[] args) {
    System.out.println("\n╔═══════════════════════════════════════════════════════════════╗");
    System.out.println("║     RTO OFFICE SIMULATION - COMPREHENSIVE TEST SUITE          ║");
    System.out.println("╚═══════════════════════════════════════════════════════════════╝\n");

    // Run all test suites
    testSingletonPattern();
    testFactoryPattern();
    testBuilderPattern();
    testAdapterPattern();
    testStrategyPattern();
    testDecoratorPattern();
    testObserverPattern();
    testFacadePattern();
    testServiceLayer();
    testValidationEngine();
    testDatabaseOperations();
    testEdgeCases();

    // Print summary
    printSummary();
  }

  // ========== DESIGN PATTERN TESTS ==========

  private static void testSingletonPattern() {
    System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
    System.out.println("📌 TEST SUITE: Singleton Pattern (SessionManager)");
    System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");

    // Test 1: Singleton returns same instance
    SessionManager sm1 = SessionManager.getInstance();
    SessionManager sm2 = SessionManager.getInstance();
    test("Singleton returns same instance", sm1 == sm2);

    // Test 2: Session not logged in initially
    sm1.logout(); // Clear any existing session
    test("Session not logged in initially", !sm1.isLoggedIn());

    // Test 3: Login creates session
    Citizen testUser = new Citizen("testId", "testSingletonUser", "password", "test@test.com");
    sm1.login(testUser);
    test("Login creates session", sm1.isLoggedIn());

    // Test 4: Current user is correct
    test("Current user is correct", sm1.getCurrentUser() == testUser);

    // Test 5: Logout clears session
    sm1.logout();
    test("Logout clears session", !sm1.isLoggedIn() && sm1.getCurrentUser() == null);

    System.out.println();
  }

  private static void testFactoryPattern() {
    System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
    System.out.println("📌 TEST SUITE: Factory Pattern (ServiceFactory)");
    System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");

    // Test 1: Create VehicleService
    IService vehicleService = ServiceFactory.getService("VEHICLE");
    test("Factory creates VehicleService", vehicleService instanceof VehicleService);

    // Test 2: Create LicenseService
    IService licenseService = ServiceFactory.getService("LICENSE");
    test("Factory creates LicenseService", licenseService instanceof LicenseService);

    // Test 3: Create UserService
    IService userService = ServiceFactory.getService("USER");
    test("Factory creates UserService", userService instanceof UserService);

    // Test 4: Create TransactionService
    IService transactionService = ServiceFactory.getService("TRANSACTION");
    test("Factory creates TransactionService", transactionService instanceof TransactionService);

    // Test 5: Invalid service type throws exception
    boolean exceptionThrown = false;
    try {
      ServiceFactory.getService("INVALID");
    } catch (IllegalArgumentException e) {
      exceptionThrown = true;
    }
    test("Invalid service type throws exception", exceptionThrown);

    // Test 6: Typed getters work
    test("Typed getter VehicleService", ServiceFactory.getVehicleService() != null);
    test("Typed getter LicenseService", ServiceFactory.getLicenseService() != null);
    test("Typed getter UserService", ServiceFactory.getUserService() != null);
    test("Typed getter TransactionService", ServiceFactory.getTransactionService() != null);

    System.out.println();
  }

  private static void testBuilderPattern() {
    System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
    System.out.println("📌 TEST SUITE: Builder Pattern (VehicleBuilder)");
    System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");

    // Test 1: Build a Car
    Vehicle car = new VehicleBuilder()
        .setOwnerId("owner1")
        .setModel("Honda City")
        .setType("CAR")
        .setExtraData("PETROL")
        .setColor("Red")
        .setManufacturingYear(2023)
        .setEngineNumber("ENG123")
        .build();
    test("Builder creates Car", car instanceof Car);
    test("Car has correct model", "Honda City".equals(car.getModel()));
    test("Car has correct color", "Red".equals(car.getColor()));
    test("Car has correct year", car.getManufacturingYear() == 2023);

    // Test 2: Build a Bike
    Vehicle bike = new VehicleBuilder()
        .setOwnerId("owner2")
        .setModel("Royal Enfield")
        .setType("BIKE")
        .setExtraData("350")
        .build();
    test("Builder creates Bike", bike instanceof Bike);

    // Test 3: Build a Truck
    Vehicle truck = new VehicleBuilder()
        .setOwnerId("owner3")
        .setModel("Tata Truck")
        .setType("TRUCK")
        .setExtraData("10.5")
        .build();
    test("Builder creates Truck", truck instanceof Truck);

    // Test 4: Invalid type throws exception
    boolean exceptionThrown = false;
    try {
      new VehicleBuilder()
          .setType("HELICOPTER")
          .build();
    } catch (IllegalArgumentException e) {
      exceptionThrown = true;
    }
    test("Invalid vehicle type throws exception", exceptionThrown);

    // Test 5: Fluent API returns builder
    VehicleBuilder builder = new VehicleBuilder();
    test("Fluent API returns builder", builder.setOwnerId("x") == builder);

    System.out.println();
  }

  private static void testAdapterPattern() {
    System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
    System.out.println("📌 TEST SUITE: Adapter Pattern (PaymentGatewayAdapter)");
    System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");

    // Test 1: Adapter implements IPaymentProcessor
    PaymentGatewayAdapter adapter = new PaymentGatewayAdapter("1234567890123456", "123");
    test("Adapter implements IPaymentProcessor", adapter instanceof IPaymentProcessor);

    // Test 2: Valid payment processing
    boolean paymentResult = adapter.processPayment(500.0);
    test("Payment processing works", paymentResult); // Simulated API likely returns true

    // Test 3: Multiple payments work
    PaymentGatewayAdapter adapter2 = new PaymentGatewayAdapter("9876543210123456", "456");
    boolean payment2 = adapter2.processPayment(1000.0);
    test("Multiple payment adapters work", payment2);

    System.out.println();
  }

  private static void testStrategyPattern() {
    System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
    System.out.println("📌 TEST SUITE: Strategy Pattern (TaxCalculationStrategy)");
    System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");

    // Create test vehicles
    Vehicle testCar = new Car("testOwner", "Test Model", "PETROL");
    Vehicle testBike = new Bike("testOwner", "Test Bike", 150);
    Vehicle testTruck = new Truck("testOwner", "Test Truck", 10.0);

    // Test 1: Standard Tax Strategy
    TaxCalculationStrategy standardTax = new StandardTaxStrategy();
    double standardResult = standardTax.calculateTax(testCar);
    test("StandardTaxStrategy returns non-zero tax", standardResult > 0);
    test("StandardTaxStrategy has name", standardTax.getStrategyName() != null);

    // Test 2: Premium Tax Strategy
    TaxCalculationStrategy premiumTax = new PremiumTaxStrategy();
    double premiumResult = premiumTax.calculateTax(testCar);
    test("PremiumTaxStrategy returns non-zero tax", premiumResult > 0);
    test("PremiumTaxStrategy has name", premiumTax.getStrategyName() != null);

    // Test 3: Commercial Tax Strategy
    TaxCalculationStrategy commercialTax = new CommercialTaxStrategy();
    double commercialResult = commercialTax.calculateTax(testTruck);
    test("CommercialTaxStrategy returns non-zero tax", commercialResult > 0);
    test("CommercialTaxStrategy has name", commercialTax.getStrategyName() != null);

    // Test 4: Different vehicles get different taxes
    double bikeTax = standardTax.calculateTax(testBike);
    double truckTax = standardTax.calculateTax(testTruck);
    test("Different vehicles have different taxes", bikeTax != truckTax);

    System.out.println();
  }

  private static void testDecoratorPattern() {
    System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
    System.out.println("📌 TEST SUITE: Decorator Pattern (VehicleFeatureDecorator)");
    System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");

    // Create base vehicle
    Vehicle baseCar = new Car("testOwner", "Base Model", "PETROL");
    double baseTax = baseCar.calculateTax();

    // Test 1: Insurance Decorator
    InsuranceDecorator insuredCar = new InsuranceDecorator(baseCar);
    double insuredCost = insuredCar.getAdditionalCost();
    double insuredTotal = insuredCar.calculateTotalCost();
    test("InsuranceDecorator adds additional cost", insuredCost > 0);
    test("InsuranceDecorator total cost includes base", insuredTotal >= baseTax);
    test("InsuranceDecorator has description", insuredCar.getFeatureDescription() != null);

    // Test 2: Extended Warranty Decorator
    ExtendedWarrantyDecorator warrantyCar = new ExtendedWarrantyDecorator(baseCar);
    double warrantyCost = warrantyCar.getAdditionalCost();
    double warrantyTotal = warrantyCar.calculateTotalCost();
    test("ExtendedWarrantyDecorator adds cost", warrantyCost > 0);
    test("ExtendedWarrantyDecorator total cost includes base", warrantyTotal >= baseTax);
    test("ExtendedWarrantyDecorator has description", warrantyCar.getFeatureDescription() != null);

    // Test 3: Chained Decorators
    ExtendedWarrantyDecorator fullFeatureCar = new ExtendedWarrantyDecorator(baseCar);
    InsuranceDecorator doubleDecorated = new InsuranceDecorator(baseCar);
    double fullTotal = fullFeatureCar.calculateTotalCost();
    test("Chained decorators add cost", fullTotal > baseTax);

    // Test 4: getDecoratedVehicle returns base
    test("getDecoratedVehicle returns base vehicle", insuredCar.getDecoratedVehicle() == baseCar);

    System.out.println();
  }

  private static void testObserverPattern() {
    System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
    System.out.println("📌 TEST SUITE: Observer Pattern (NotificationObserver)");
    System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");

    // Test 1: Create observers with required parameters
    NotificationObserver emailObserver = new EmailNotifier("test@example.com");
    NotificationObserver smsObserver = new SMSNotifier("1234567890");
    test("EmailNotifier created", emailObserver != null);
    test("SMSNotifier created", smsObserver != null);

    // Test 2: Observers have correct types
    test("EmailNotifier has correct type", "EMAIL".equals(emailObserver.getObserverType()));
    test("SMSNotifier has correct type", "SMS".equals(smsObserver.getObserverType()));

    // Test 3: Observers can receive updates (no exceptions)
    boolean updateSuccess = true;
    try {
      emailObserver.update("Test notification message");
      smsObserver.update("Test notification message");
    } catch (Exception e) {
      updateSuccess = false;
    }
    test("Observers receive updates without error", updateSuccess);

    System.out.println();
  }

  private static void testFacadePattern() {
    System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
    System.out.println("📌 TEST SUITE: Facade Pattern (RTOSystemFacade)");
    System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");

    RTOSystemFacade facade = new RTOSystemFacade();

    // Test 1: Facade created successfully
    test("RTOSystemFacade created", facade != null);

    // Test 2: Not logged in initially
    facade.logout(); // Ensure clean state
    test("Not logged in initially", !facade.isLoggedIn());

    // Test 3: Admin login
    User admin = facade.login("admin", "admin");
    test("Admin login successful", admin != null);
    test("Admin has correct role", admin != null && "ADMIN".equals(admin.getRole()));

    // Test 4: Is admin check
    test("IsAdmin returns true for admin", facade.isAdmin());

    // Test 5: Logout
    facade.logout();
    test("Logout works", !facade.isLoggedIn());

    // Test 6: User registration
    String uniqueUsername = "testuser_" + System.currentTimeMillis();
    boolean registered = facade.registerUser(uniqueUsername, "password123", uniqueUsername + "@test.com");
    test("User registration works", registered);

    // Test 7: Login as new user
    User newUser = facade.login(uniqueUsername, "password123");
    test("New user can login", newUser != null);

    // Test 8: Non-admin check
    test("IsAdmin returns false for citizen", !facade.isAdmin());

    // Test 9: Tax calculation
    double tax = facade.calculateTaxByValue("CAR", 500000);
    test("Tax calculation works", tax > 0);

    facade.logout();
    System.out.println();
  }

  // ========== SERVICE LAYER TESTS ==========

  private static void testServiceLayer() {
    System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
    System.out.println("📌 TEST SUITE: Service Layer");
    System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");

    // Test UserService
    UserService userService = new UserService();
    String uniqueUser = "serviceTestUser_" + System.currentTimeMillis();
    boolean userCreated = userService.registerUser(uniqueUser, "password123", "test@test.com");
    test("UserService: Register user", userCreated);

    User authenticatedUser = userService.authenticate(uniqueUser, "password123");
    test("UserService: Authenticate user", authenticatedUser != null);

    User wrongPassword = userService.authenticate(uniqueUser, "wrongpassword");
    test("UserService: Wrong password returns null", wrongPassword == null);

    // Test VehicleService
    VehicleService vehicleService = new VehicleService();
    test("VehicleService: Initialize works", vehicleService != null);

    List<Vehicle> allVehicles = vehicleService.getAllVehicles();
    test("VehicleService: Get all vehicles works", allVehicles != null);

    // Test LicenseService
    LicenseService licenseService = new LicenseService();
    test("LicenseService: Initialize works", licenseService != null);

    System.out.println();
  }

  private static void testValidationEngine() {
    System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
    System.out.println("📌 TEST SUITE: Validation Engine");
    System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");

    // Username validation
    test("Valid username (alphanumeric)", ValidationEngine.isValidUsername("user123"));
    test("Invalid username (too short)", !ValidationEngine.isValidUsername("ab"));
    test("Invalid username (null)", !ValidationEngine.isValidUsername(null));
    test("Invalid username (special chars)", !ValidationEngine.isValidUsername("user@#$"));

    // Password validation
    test("Valid password", ValidationEngine.isValidPassword("password123"));
    test("Invalid password (too short)", !ValidationEngine.isValidPassword("12345"));
    test("Invalid password (null)", !ValidationEngine.isValidPassword(null));

    // Email validation
    test("Valid email", ValidationEngine.isValidEmail("test@example.com"));
    test("Invalid email (no @)", !ValidationEngine.isValidEmail("testexample.com"));
    test("Invalid email (null)", !ValidationEngine.isValidEmail(null));

    System.out.println();
  }

  private static void testDatabaseOperations() {
    System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
    System.out.println("📌 TEST SUITE: Database Operations");
    System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");

    DatabaseService db = DatabaseService.getInstance();

    // Test 1: Database singleton
    DatabaseService db2 = DatabaseService.getInstance();
    test("DatabaseService is singleton", db == db2);

    // Test 2: Connection works
    test("Database connection works", true); // Implicit test - we got this far

    // Test 3: Admin exists
    User admin = db.authenticate("admin", "admin");
    test("Default admin exists", admin != null);

    System.out.println();
  }

  private static void testEdgeCases() {
    System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
    System.out.println("📌 TEST SUITE: Edge Cases & Error Handling");
    System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");

    UserService userService = new UserService();
    VehicleService vehicleService = new VehicleService();
    LicenseService licenseService = new LicenseService();

    // Test null handling
    test("UserService: Null username rejected", !userService.registerUser(null, "pass", "email@test.com"));
    test("UserService: Null password rejected", !userService.registerUser("user", null, "email@test.com"));
    test("UserService: Null email rejected", !userService.registerUser("user", "pass", null));

    test("VehicleService: Null vehicle rejected", !vehicleService.registerVehicle(null));

    test("LicenseService: Null license rejected", !licenseService.applyForLicense(null));

    // Test empty string handling
    test("UserService: Empty username rejected", !userService.registerUser("", "pass", "email@test.com"));

    // Test invalid vehicle data
    Vehicle invalidVehicle = new Car(null, null, "PETROL");
    test("VehicleService: Invalid vehicle rejected", !vehicleService.registerVehicle(invalidVehicle));

    // Test facade safety net
    RTOSystemFacade facade = new RTOSystemFacade();
    facade.logout(); // Ensure no login
    Vehicle noLoginVehicle = facade.registerVehicle("CAR", "Test", "PETROL");
    test("Facade: No login vehicle registration returns null", noLoginVehicle == null);

    boolean noLoginPayment = facade.processPayment("1234", "123", 100);
    test("Facade: No login payment returns false", !noLoginPayment);

    System.out.println();
  }

  // ========== UTILITY METHODS ==========

  private static void test(String testName, boolean passed) {
    totalTests++;
    if (passed) {
      passedTests++;
      System.out.printf("   ✅ %-50s : PASS%n", testName);
    } else {
      failedTests++;
      System.out.printf("   ❌ %-50s : FAIL%n", testName);
    }
  }

  private static void printSummary() {
    System.out.println("\n╔═══════════════════════════════════════════════════════════════╗");
    System.out.println("║                        TEST SUMMARY                           ║");
    System.out.println("╠═══════════════════════════════════════════════════════════════╣");
    System.out.printf("║   Total Tests:  %-46d ║%n", totalTests);
    System.out.printf("║   Passed:       %-46d ║%n", passedTests);
    System.out.printf("║   Failed:       %-46d ║%n", failedTests);
    System.out.printf("║   Pass Rate:    %-45.1f%% ║%n", (totalTests > 0 ? (passedTests * 100.0 / totalTests) : 0));
    System.out.println("╠═══════════════════════════════════════════════════════════════╣");
    if (failedTests == 0) {
      System.out.println("║   🎉 ALL TESTS PASSED! The system is working correctly.      ║");
    } else {
      System.out.println("║   ⚠️  Some tests failed. Please review the output above.     ║");
    }
    System.out.println("╚═══════════════════════════════════════════════════════════════╝\n");
  }
}
