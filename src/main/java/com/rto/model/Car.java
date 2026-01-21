package com.rto.model;

/**
 * Car class representing four-wheeler vehicles
 * Demonstrates polymorphism with tax calculation based on fuel type
 */
public class Car extends Vehicle {
    private String fuelType; // "PETROL", "DIESEL", "ELECTRIC", "HYBRID"

    public Car(String ownerId, String model, String fuelType) {
        super(ownerId, model, "CAR");
        this.fuelType = fuelType;
    }

    public String getFuelType() {
        return fuelType;
    }

    public void setFuelType(String fuelType) {
        this.fuelType = fuelType;
    }

    @Override
    public double calculateTax() {
        // Tax calculation based on fuel type
        // Electric vehicles get lower tax, Diesel higher
        double baseTax = 5000.0;
        switch (fuelType.toUpperCase()) {
            case "ELECTRIC":
                return baseTax * 0.5; // 50% discount for electric
            case "HYBRID":
                return baseTax * 0.7; // 30% discount for hybrid
            case "DIESEL":
                return baseTax * 1.3; // 30% surcharge for diesel
            case "PETROL":
            default:
                return baseTax;
        }
    }

    @Override
    public String toString() {
        return "Car{" +
                "registrationNumber='" + getRegistrationNumber() + '\'' +
                ", model='" + getModel() + '\'' +
                ", fuelType='" + fuelType + '\'' +
                ", tax=" + calculateTax() +
                '}';
    }
}
