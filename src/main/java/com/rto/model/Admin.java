package com.rto.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Admin class representing system administrators.
 * Admins can approve/reject applications, manage users, and issue challans.
 */
public class Admin extends User {
    private static final long serialVersionUID = 1L;

    private String department;
    private String employeeId;
    private List<String> permissions;
    private int applicationsProcessedToday;

    public Admin(String id, String username, String password) {
        super(id, username, password, "ADMIN");
        this.department = "RTO Administration";
        this.permissions = new ArrayList<>();
        initializeDefaultPermissions();
    }

    public Admin(String id, String username, String password, String department) {
        this(id, username, password);
        this.department = department;
    }

    private void initializeDefaultPermissions() {
        permissions.add("VIEW_ALL_USERS");
        permissions.add("APPROVE_LICENSE");
        permissions.add("REJECT_LICENSE");
        permissions.add("APPROVE_VEHICLE");
        permissions.add("REJECT_VEHICLE");
        permissions.add("ISSUE_CHALLAN");
        permissions.add("VIEW_ALL_TRANSACTIONS");
        permissions.add("MANAGE_OFFICERS");
        permissions.add("TRANSFER_OWNERSHIP");
        permissions.add("GENERATE_REPORTS");
    }

    // Getters and Setters
    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public String getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(String employeeId) {
        this.employeeId = employeeId;
    }

    public List<String> getPermissions() {
        return permissions;
    }

    public int getApplicationsProcessedToday() {
        return applicationsProcessedToday;
    }

    public void incrementApplicationsProcessed() {
        this.applicationsProcessedToday++;
    }

    public void resetDailyCount() {
        this.applicationsProcessedToday = 0;
    }

    /**
     * Check if admin has a specific permission.
     */
    public boolean hasPermission(String permission) {
        return permissions.contains(permission);
    }

    /**
     * Add a permission to this admin.
     */
    public void addPermission(String permission) {
        if (!permissions.contains(permission)) {
            permissions.add(permission);
        }
    }

    /**
     * Remove a permission from this admin.
     */
    public void removePermission(String permission) {
        permissions.remove(permission);
    }

    /**
     * Check if admin can approve applications.
     */
    public boolean canApproveApplications() {
        return hasPermission("APPROVE_LICENSE") || hasPermission("APPROVE_VEHICLE");
    }

    /**
     * Check if admin can issue challans.
     */
    public boolean canIssueChallan() {
        return hasPermission("ISSUE_CHALLAN");
    }

    @Override
    public String toString() {
        return "Admin{" +
                "id='" + getId() + '\'' +
                ", username='" + getUsername() + '\'' +
                ", department='" + department + '\'' +
                ", employeeId='" + employeeId + '\'' +
                ", permissions=" + permissions.size() +
                '}';
    }
}
