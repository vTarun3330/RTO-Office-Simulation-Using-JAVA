package com.rto.service;

/**
 * Factory Pattern - Returns the correct service implementation.
 * Centralizes service creation and management.
 */
public class ServiceFactory {

    public static IService getService(String type) {
        return switch (type.toUpperCase()) {
            case "VEHICLE" -> new VehicleService();
            case "LICENSE" -> new LicenseService();
            case "USER" -> new UserService();
            case "TRANSACTION" -> new TransactionService();
            default -> throw new IllegalArgumentException("Unknown service type: " + type);
        };
    }

    // Typed getters for convenience
    public static VehicleService getVehicleService() {
        return new VehicleService();
    }

    public static LicenseService getLicenseService() {
        return new LicenseService();
    }

    public static UserService getUserService() {
        return new UserService();
    }

    public static TransactionService getTransactionService() {
        return new TransactionService();
    }
}
