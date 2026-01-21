package com.rto.service;

public class ServiceFactory {
    public static IService getService(String type) {
        if (type.equalsIgnoreCase("VEHICLE"))
            return new VehicleService();
        if (type.equalsIgnoreCase("LICENSE"))
            return new LicenseService();
        return null;
    }
}
