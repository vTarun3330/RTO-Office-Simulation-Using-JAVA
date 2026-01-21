package com.rto.model;

import java.io.Serializable;
import java.time.Year;

public abstract class Vehicle implements Serializable {
    private String registrationNumber;
    private String ownerId;
    private String model;
    private String type;
    private int manufacturingYear;
    private String color;
    private String engineNumber;

    public Vehicle(String ownerId, String model, String type) {
        this.ownerId = ownerId;
        this.model = model;
        this.type = type;
        this.registrationNumber = "PENDING";
        this.manufacturingYear = Year.now().getValue();
        this.color = "Not Specified";
        this.engineNumber = generateEngineNumber();
    }

    private String generateEngineNumber() {
        return "ENG" + System.currentTimeMillis();
    }

    public boolean isValid() {
        return ownerId != null && !ownerId.isEmpty() &&
                model != null && !model.isEmpty() &&
                manufacturingYear > 1900 && manufacturingYear <= Year.now().getValue();
    }

    public void setRegistrationNumber(String regNo) {
        this.registrationNumber = regNo;
    }

    public String getRegistrationNumber() {
        return registrationNumber;
    }

    public String getOwnerId() {
        return ownerId;
    }

    public String getModel() {
        return model;
    }

    public String getType() {
        return type;
    }

    public int getManufacturingYear() {
        return manufacturingYear;
    }

    public void setManufacturingYear(int year) {
        this.manufacturingYear = year;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public String getEngineNumber() {
        return engineNumber;
    }

    public void setEngineNumber(String engineNumber) {
        this.engineNumber = engineNumber;
    }

    public abstract double calculateTax();
}
