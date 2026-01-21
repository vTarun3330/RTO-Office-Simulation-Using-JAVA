package com.rto.model;

/**
 * Bike class representing two-wheeler vehicles
 * Demonstrates polymorphism with tax calculation based on engine CC
 */
public class Bike extends Vehicle {
    private int cc; // Engine capacity in cubic centimeters

    public Bike(String ownerId, String model, int cc) {
        super(ownerId, model, "BIKE");
        this.cc = cc;
    }

    public int getCc() {
        return cc;
    }

    public void setCc(int cc) {
        this.cc = cc;
    }

    @Override
    public double calculateTax() {
        // Tax calculation based on engine capacity
        // Higher CC = Higher tax
        double baseTax = 1000.0;
        if (cc <= 100) {
            return baseTax * 0.8; // Small bikes get discount
        } else if (cc <= 150) {
            return baseTax;
        } else if (cc <= 300) {
            return baseTax * 1.5;
        } else {
            return baseTax * 2.0; // Premium bikes pay double
        }
    }

    @Override
    public String toString() {
        return "Bike{" +
                "registrationNumber='" + getRegistrationNumber() + '\'' +
                ", model='" + getModel() + '\'' +
                ", cc=" + cc +
                ", tax=" + calculateTax() +
                '}';
    }
}
