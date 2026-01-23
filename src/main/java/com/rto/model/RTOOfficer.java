package com.rto.model;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * RTOOfficer class representing RTO office employees.
 * Officers can verify applications, conduct tests, and issue challans.
 */
public class RTOOfficer extends User {
    private static final long serialVersionUID = 1L;

    private String employeeId;
    private String designation;
    private String officeLocation;
    private LocalDate joiningDate;
    private List<String> certifications;
    private int testsCondutedToday;
    private boolean isOnDuty;

    public RTOOfficer(String id, String username, String password) {
        super(id, username, password, "RTO_OFFICER");
        this.certifications = new ArrayList<>();
        this.joiningDate = LocalDate.now();
        this.isOnDuty = true;
    }

    public RTOOfficer(String id, String username, String password, String designation, String officeLocation) {
        this(id, username, password);
        this.designation = designation;
        this.officeLocation = officeLocation;
    }

    // Getters and Setters
    public String getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(String employeeId) {
        this.employeeId = employeeId;
    }

    public String getDesignation() {
        return designation;
    }

    public void setDesignation(String designation) {
        this.designation = designation;
    }

    public String getOfficeLocation() {
        return officeLocation;
    }

    public void setOfficeLocation(String officeLocation) {
        this.officeLocation = officeLocation;
    }

    public LocalDate getJoiningDate() {
        return joiningDate;
    }

    public void setJoiningDate(LocalDate joiningDate) {
        this.joiningDate = joiningDate;
    }

    public List<String> getCertifications() {
        return certifications;
    }

    public int getTestsConductedToday() {
        return testsCondutedToday;
    }

    public void incrementTestsConducted() {
        this.testsCondutedToday++;
    }

    public void resetDailyCount() {
        this.testsCondutedToday = 0;
    }

    public boolean isOnDuty() {
        return isOnDuty;
    }

    public void setOnDuty(boolean onDuty) {
        this.isOnDuty = onDuty;
    }

    /**
     * Add a certification to officer's profile.
     */
    public void addCertification(String certification) {
        if (!certifications.contains(certification)) {
            certifications.add(certification);
        }
    }

    /**
     * Check if officer can conduct driving tests.
     */
    public boolean canConductDrivingTest() {
        return certifications.contains("DRIVING_TEST_EXAMINER") && isOnDuty;
    }

    /**
     * Check if officer can verify documents.
     */
    public boolean canVerifyDocuments() {
        return isOnDuty;
    }

    /**
     * Check if officer can issue challans.
     */
    public boolean canIssueChallan() {
        return certifications.contains("ENFORCEMENT") && isOnDuty;
    }

    /**
     * Get years of service.
     */
    public int getYearsOfService() {
        if (joiningDate == null)
            return 0;
        return java.time.Period.between(joiningDate, LocalDate.now()).getYears();
    }

    @Override
    public String toString() {
        return "RTOOfficer{" +
                "id='" + getId() + '\'' +
                ", username='" + getUsername() + '\'' +
                ", designation='" + designation + '\'' +
                ", officeLocation='" + officeLocation + '\'' +
                ", onDuty=" + isOnDuty +
                '}';
    }
}
