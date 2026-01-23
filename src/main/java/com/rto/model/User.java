package com.rto.model;

import java.io.Serializable;
import java.time.LocalDate;

/**
 * Base User class for the RTO Management System.
 * Supports multiple roles: ADMIN, CITIZEN, OFFICER.
 */
public abstract class User implements Serializable {
    private static final long serialVersionUID = 1L;

    protected String id;
    protected String username;
    protected String password;
    protected String role;
    protected String email;
    protected String fullName;
    protected LocalDate dateOfBirth;
    protected String phone;
    protected LocalDate createdAt;
    protected boolean isActive;

    public User(String id, String username, String password, String role) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.role = role;
        this.createdAt = LocalDate.now();
        this.isActive = true;
    }

    // Getters
    public String getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getRole() {
        return role;
    }

    public String getEmail() {
        return email;
    }

    public String getFullName() {
        return fullName;
    }

    public LocalDate getDateOfBirth() {
        return dateOfBirth;
    }

    public String getPhone() {
        return phone;
    }

    public LocalDate getCreatedAt() {
        return createdAt;
    }

    public boolean isActive() {
        return isActive;
    }

    // Setters
    public void setEmail(String email) {
        this.email = email;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public void setDateOfBirth(LocalDate dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public void setActive(boolean active) {
        this.isActive = active;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * Calculate age based on date of birth.
     */
    public int getAge() {
        if (dateOfBirth == null)
            return 0;
        return java.time.Period.between(dateOfBirth, LocalDate.now()).getYears();
    }

    /**
     * Check if user is eligible for license (age >= 18).
     */
    public boolean isEligibleForLicense() {
        return getAge() >= 18;
    }

    /**
     * Check if user is an admin.
     */
    public boolean isAdmin() {
        return "ADMIN".equalsIgnoreCase(role);
    }

    /**
     * Check if user is an officer.
     */
    public boolean isOfficer() {
        return "OFFICER".equalsIgnoreCase(role) || "RTO_OFFICER".equalsIgnoreCase(role);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "{" +
                "id='" + id + '\'' +
                ", username='" + username + '\'' +
                ", role='" + role + '\'' +
                ", fullName='" + fullName + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null || getClass() != obj.getClass())
            return false;
        User user = (User) obj;
        return id != null && id.equals(user.id);
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }
}
