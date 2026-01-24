package com.rto.test;

import com.rto.model.User;
import com.rto.model.Vehicle;
import com.rto.service.*;
import com.rto.util.ValidationEngine;

public class ErrorHandlingTest {
    public static void main(String[] args) {
        System.out.println("==========================================");
        System.out.println("   RTO System - Error Handling Verification");
        System.out.println("==========================================\n");

        testUserValidation();
        testVehicleSafety();
        testLicenseValidation();
        testGlobalFacadeSafety();
        
        System.out.println("\n==========================================");
        System.out.println("   ✅ Verification Complete");
        System.out.println("==========================================");
    }

    private static void testUserValidation() {
        System.out.println("1. Testing User Input Validation...");
        UserService userService = new UserService();
        
        // Test 1: Null Username
        boolean r1 = userService.registerUser(null, "pass123", "email@test.com");
        printResult("Register Null Username", !r1); // Should fail

        // Test 2: Invalid Email
        boolean r2 = userService.registerUser("valid_user", "pass123", "invalid-email");
        printResult("Register Invalid Email", !r2); // Should fail

        // Test 3: Short Password
        boolean r3 = userService.registerUser("valid_user", "123", "email@test.com");
        printResult("Register Short Password", !r3); // Should fail
        
        // Test 4: Valid Registration
        String uniqueUser = "testuser_" + System.currentTimeMillis();
        boolean r4 = userService.registerUser(uniqueUser, "password123", "test@example.com");
        printResult("Valid Registration", r4); // Should pass
    }

    private static void testVehicleSafety() {
        System.out.println("\n2. Testing Vehicle Service Null Safety...");
        VehicleService vehicleService = new VehicleService();

        // Test 1: Register Null Vehicle
        boolean r1 = vehicleService.registerVehicle(null);
        printResult("Register Null Vehicle", !r1);

        // Test 2: Incomplete Vehicle
        Vehicle v = new Vehicle(null, null, "CAR") {
            public double calculateTax() { return 0; }
        };
        boolean r2 = vehicleService.registerVehicle(v);
        printResult("Register Incomplete Vehicle", !r2);
        
        // Test 3: Null Owner Search
        try {
            vehicleService.getVehiclesByOwnerId(null);
            printResult("Null Owner Search (No Crash)", true);
        } catch (Exception e) {
            printResult("Null Owner Search (No Crash)", false);
            e.printStackTrace();
        }
    }

    private static void testLicenseValidation() {
        System.out.println("\n3. Testing License Service Validation...");
        LicenseService licenseService = new LicenseService();

        // Test 1: Apply with Null License
        boolean r1 = licenseService.applyForLicense(null);
        printResult("Apply Null License", !r1);
    }

    private static void testGlobalFacadeSafety() {
        System.out.println("\n4. Testing Global Facade Safety Net...");
        RTOSystemFacade facade = new RTOSystemFacade();

        // Facade should handle exceptions gracefully and return safe default/false
        try {
            // Attempt to register vehicle without login
            Vehicle v = facade.registerVehicle("CAR", "Model X", "Electric");
            printResult("Register Vehicle No Login (Returns Null)", v == null);
            
            // Attempt payment without login
            boolean p = facade.processPayment("1234", "123", 500.0);
            printResult("Process Payment No Login (Returns False)", !p);
            
        } catch (Exception e) {
            printResult("Facade Safety Net", false);
            System.err.println("Facade let an exception escape!");
            e.printStackTrace();
        }
    }

    private static void printResult(String testName, boolean passed) {
        System.out.printf("   %-40s : %s%n", testName, passed ? "✅ PASS" : "❌ FAIL");
    }
}
