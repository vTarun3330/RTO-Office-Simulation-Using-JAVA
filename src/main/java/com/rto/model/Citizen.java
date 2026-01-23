package com.rto.model;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Citizen class representing regular users of the RTO system.
 * Citizens can register vehicles, apply for licenses, and pay challans.
 */
public class Citizen extends User {
    private static final long serialVersionUID = 1L;

    private String address;
    private String aadharNumber;
    private List<String> vehicleRegistrations;
    private List<String> licenseIds;
    private boolean isVerified;

    public Citizen(String id, String username, String password, String email) {
        super(id, username, password, "CITIZEN");
        this.setEmail(email);
        this.vehicleRegistrations = new ArrayList<>();
        this.licenseIds = new ArrayList<>();
        this.isVerified = false;
    }

    public Citizen(String id, String username, String password, String email,
            String fullName, LocalDate dob, String phone) {
        this(id, username, password, email);
        this.setFullName(fullName);
        this.setDateOfBirth(dob);
        this.setPhone(phone);
    }

    // Getters and Setters
    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getAadharNumber() {
        return aadharNumber;
    }

    public void setAadharNumber(String aadharNumber) {
        this.aadharNumber = aadharNumber;
    }

    public List<String> getVehicleRegistrations() {
        return vehicleRegistrations;
    }

    public List<String> getLicenseIds() {
        return licenseIds;
    }

    public boolean isVerified() {
        return isVerified;
    }

    public void setVerified(boolean verified) {
        isVerified = verified;
    }

    /**
     * Add a vehicle registration to this citizen's account.
     */
    public void addVehicle(String registrationNumber) {
        if (!vehicleRegistrations.contains(registrationNumber)) {
            vehicleRegistrations.add(registrationNumber);
        }
    }

    /**
     * Add a license ID to this citizen's account.
     */
    public void addLicense(String licenseId) {
        if (!licenseIds.contains(licenseId)) {
            licenseIds.add(licenseId);
        }
    }

    /**
     * Get total number of vehicles owned.
     */
    public int getVehicleCount() {
        return vehicleRegistrations.size();
    }

    /**
     * Check if citizen has at least one active license.
     */
    public boolean hasLicense() {
        return !licenseIds.isEmpty();
    }

    @Override
    public String toString() {
        return "Citizen{" +
                "id='" + getId() + '\'' +
                ", username='" + getUsername() + '\'' +
                ", email='" + getEmail() + '\'' +
                ", fullName='" + getFullName() + '\'' +
                ", vehicles=" + vehicleRegistrations.size() +
                ", verified=" + isVerified +
                '}';
    }
}
