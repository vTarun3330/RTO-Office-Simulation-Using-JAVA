package com.rto.service;

import com.rto.model.License;
import com.rto.patterns.NotificationSubject;
import com.rto.patterns.EmailNotifier;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * License Service - Handles license application and management
 * Demonstrates Observer Pattern for notifications
 */
public class LicenseService implements IService {
  private DatabaseService db;
  private NotificationSubject notificationSubject;

  public LicenseService() {
    this.db = DatabaseService.getInstance();
    this.notificationSubject = new NotificationSubject();
  }

  @Override
  public void initialize() {
    System.out.println("LicenseService initialized");
  }

  /**
   * Apply for a new license
   */
  public boolean applyForLicense(License license) {
    String sql = """
        INSERT INTO licenses
        (license_id, user_id, license_type, status, applicant_name, applicant_email, applicant_address, blood_group)
        VALUES (?, ?, ?, ?, ?, ?, ?, ?)
        """;

    boolean success = db.executeUpdate(sql,
        license.getLicenseId(),
        license.getUserId(),
        license.getLicenseType(),
        license.getStatus(),
        license.getApplicantName(),
        license.getApplicantEmail(),
        license.getApplicantAddress(),
        license.getBloodGroup());

    if (success && license.getApplicantEmail() != null) {
      notificationSubject.attach(new EmailNotifier(license.getApplicantEmail()));
      notificationSubject.notifyObservers(
          "Your license application " + license.getLicenseId() + " has been submitted successfully!");
    }

    return success;
  }

  /**
   * Approve a license application
   */
  public boolean approveLicense(String licenseId) {
    License license = getLicenseById(licenseId);
    if (license == null)
      return false;

    license.approve();

    String sql = "UPDATE licenses SET status = ?, issue_date = ?, expiry_date = ? WHERE license_id = ?";
    boolean success = db.executeUpdate(sql,
        license.getStatus(),
        Date.valueOf(license.getIssueDate()),
        Date.valueOf(license.getExpiryDate()),
        licenseId);

    if (success && license.getApplicantEmail() != null) {
      notificationSubject.attach(new EmailNotifier(license.getApplicantEmail()));
      notificationSubject.notifyObservers(
          "Congratulations! Your license " + licenseId + " has been APPROVED!");
    }

    return success;
  }

  /**
   * Reject a license application
   */
  public boolean rejectLicense(String licenseId) {
    String sql = "UPDATE licenses SET status = ? WHERE license_id = ?";
    boolean success = db.executeUpdate(sql, "REJECTED", licenseId);

    if (success) {
      License license = getLicenseById(licenseId);
      if (license != null && license.getApplicantEmail() != null) {
        notificationSubject.attach(new EmailNotifier(license.getApplicantEmail()));
        notificationSubject.notifyObservers(
            "Your license application " + licenseId + " has been rejected.");
      }
    }

    return success;
  }

  /**
   * Renew a license
   */
  public boolean renewLicense(String licenseId) {
    License license = getLicenseById(licenseId);
    if (license == null)
      return false;

    license.approve();

    String sql = "UPDATE licenses SET expiry_date = ? WHERE license_id = ?";
    return db.executeUpdate(sql, Date.valueOf(license.getExpiryDate()), licenseId);
  }

  /**
   * Get license by ID
   */
  public License getLicenseById(String licenseId) {
    String sql = "SELECT * FROM licenses WHERE license_id = ?";
    try (ResultSet rs = db.executeQuery(sql, licenseId)) {
      if (rs != null && rs.next()) {
        return mapResultSetToLicense(rs);
      }
    } catch (SQLException e) {
      System.err.println("Error getting license: " + e.getMessage());
    }
    return null;
  }

  /**
   * Get all licenses for a user
   */
  public List<License> getLicensesByUserId(String userId) {
    List<License> licenses = new ArrayList<>();
    String sql = "SELECT * FROM licenses WHERE user_id = ?";
    try (ResultSet rs = db.executeQuery(sql, userId)) {
      while (rs != null && rs.next()) {
        licenses.add(mapResultSetToLicense(rs));
      }
    } catch (SQLException e) {
      System.err.println("Error getting licenses for user: " + e.getMessage());
    }
    return licenses;
  }

  /**
   * Get all pending license applications (for admin)
   */
  public List<License> getPendingApplications() {
    List<License> licenses = new ArrayList<>();
    String sql = "SELECT * FROM licenses WHERE status = 'PENDING'";
    try (ResultSet rs = db.executeQuery(sql)) {
      while (rs != null && rs.next()) {
        licenses.add(mapResultSetToLicense(rs));
      }
    } catch (SQLException e) {
      System.err.println("Error getting pending applications: " + e.getMessage());
    }
    return licenses;
  }

  /**
   * Get all licenses
   */
  public List<License> getAllLicenses() {
    List<License> licenses = new ArrayList<>();
    String sql = "SELECT * FROM licenses";
    try (ResultSet rs = db.executeQuery(sql)) {
      while (rs != null && rs.next()) {
        licenses.add(mapResultSetToLicense(rs));
      }
    } catch (SQLException e) {
      System.err.println("Error getting all licenses: " + e.getMessage());
    }
    return licenses;
  }

  private License mapResultSetToLicense(ResultSet rs) throws SQLException {
    License license = new License();
    license.setLicenseId(rs.getString("license_id"));
    license.setUserId(rs.getString("user_id"));
    license.setLicenseType(rs.getString("license_type"));
    license.setStatus(rs.getString("status"));

    Date issueDate = rs.getDate("issue_date");
    if (issueDate != null)
      license.setIssueDate(issueDate.toLocalDate());

    Date expiryDate = rs.getDate("expiry_date");
    if (expiryDate != null)
      license.setExpiryDate(expiryDate.toLocalDate());

    license.setApplicantName(rs.getString("applicant_name"));
    license.setApplicantEmail(rs.getString("applicant_email"));
    license.setApplicantAddress(rs.getString("applicant_address"));
    license.setBloodGroup(rs.getString("blood_group"));

    return license;
  }
}
