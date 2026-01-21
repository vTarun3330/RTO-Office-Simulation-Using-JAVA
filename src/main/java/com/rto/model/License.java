package com.rto.model;

import java.io.Serializable;
import java.time.LocalDate;

/**
 * License model representing a driving license
 * Part of RTO Management System domain model
 */
public class License implements Serializable {
  private String licenseId;
  private String userId;
  private String licenseType; // "TWO_WHEELER", "FOUR_WHEELER", "COMMERCIAL"
  private LocalDate issueDate;
  private LocalDate expiryDate;
  private String status; // "PENDING", "APPROVED", "REJECTED", "EXPIRED", "ACTIVE"
  private String applicantName;
  private String applicantEmail;
  private String applicantAddress;
  private String bloodGroup;

  public License() {
    this.status = "PENDING";
  }

  public License(String userId, String licenseType, String applicantName,
      String applicantEmail, String applicantAddress, String bloodGroup) {
    this.licenseId = generateLicenseId();
    this.userId = userId;
    this.licenseType = licenseType;
    this.applicantName = applicantName;
    this.applicantEmail = applicantEmail;
    this.applicantAddress = applicantAddress;
    this.bloodGroup = bloodGroup;
    this.status = "PENDING";
    this.issueDate = null;
    this.expiryDate = null;
  }

  private String generateLicenseId() {
    return "LIC" + System.currentTimeMillis();
  }

  public void approve() {
    this.status = "ACTIVE";
    this.issueDate = LocalDate.now();
    this.expiryDate = LocalDate.now().plusYears(10);
  }

  public void reject() {
    this.status = "REJECTED";
  }

  public boolean isExpired() {
    if (expiryDate == null)
      return false;
    return LocalDate.now().isAfter(expiryDate);
  }

  public boolean needsRenewal() {
    if (expiryDate == null)
      return false;
    return LocalDate.now().plusMonths(3).isAfter(expiryDate);
  }

  // Getters and Setters
  public String getLicenseId() {
    return licenseId;
  }

  public void setLicenseId(String licenseId) {
    this.licenseId = licenseId;
  }

  public String getUserId() {
    return userId;
  }

  public void setUserId(String userId) {
    this.userId = userId;
  }

  public String getLicenseType() {
    return licenseType;
  }

  public void setLicenseType(String licenseType) {
    this.licenseType = licenseType;
  }

  public LocalDate getIssueDate() {
    return issueDate;
  }

  public void setIssueDate(LocalDate issueDate) {
    this.issueDate = issueDate;
  }

  public LocalDate getExpiryDate() {
    return expiryDate;
  }

  public void setExpiryDate(LocalDate expiryDate) {
    this.expiryDate = expiryDate;
  }

  public String getStatus() {
    return status;
  }

  public void setStatus(String status) {
    this.status = status;
  }

  public String getApplicantName() {
    return applicantName;
  }

  public void setApplicantName(String applicantName) {
    this.applicantName = applicantName;
  }

  public String getApplicantEmail() {
    return applicantEmail;
  }

  public void setApplicantEmail(String applicantEmail) {
    this.applicantEmail = applicantEmail;
  }

  public String getApplicantAddress() {
    return applicantAddress;
  }

  public void setApplicantAddress(String applicantAddress) {
    this.applicantAddress = applicantAddress;
  }

  public String getBloodGroup() {
    return bloodGroup;
  }

  public void setBloodGroup(String bloodGroup) {
    this.bloodGroup = bloodGroup;
  }

  @Override
  public String toString() {
    return "License{" +
        "licenseId='" + licenseId + '\'' +
        ", licenseType='" + licenseType + '\'' +
        ", status='" + status + '\'' +
        ", expiryDate=" + expiryDate +
        '}';
  }
}
