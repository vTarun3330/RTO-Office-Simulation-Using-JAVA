package com.rto.model;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Application model for tracking license and vehicle registration applications.
 * Used by the Admin module to approve/reject citizen requests.
 */
public class Application implements Serializable {
  private static final long serialVersionUID = 1L;

  private String applicationId;
  private String applicantId;
  private String applicationType; // "LICENSE" or "VEHICLE"
  private String status; // "PENDING", "APPROVED", "REJECTED"
  private String remarks;
  private LocalDateTime submissionDate;
  private String referenceId; // License ID or Vehicle Registration Number

  public Application() {
    this.applicationId = "APP-" + System.currentTimeMillis();
    this.status = "PENDING";
    this.submissionDate = LocalDateTime.now();
  }

  public Application(String applicantId, String applicationType, String referenceId) {
    this();
    this.applicantId = applicantId;
    this.applicationType = applicationType;
    this.referenceId = referenceId;
  }

  // Business Logic
  public void approve(String remarks) {
    this.status = "APPROVED";
    this.remarks = remarks;
  }

  public void reject(String remarks) {
    this.status = "REJECTED";
    this.remarks = remarks;
  }

  public boolean isPending() {
    return "PENDING".equals(status);
  }

  // Getters and Setters
  public String getApplicationId() {
    return applicationId;
  }

  public void setApplicationId(String applicationId) {
    this.applicationId = applicationId;
  }

  public String getApplicantId() {
    return applicantId;
  }

  public void setApplicantId(String applicantId) {
    this.applicantId = applicantId;
  }

  public String getApplicationType() {
    return applicationType;
  }

  public void setApplicationType(String applicationType) {
    this.applicationType = applicationType;
  }

  public String getStatus() {
    return status;
  }

  public void setStatus(String status) {
    this.status = status;
  }

  public String getRemarks() {
    return remarks;
  }

  public void setRemarks(String remarks) {
    this.remarks = remarks;
  }

  public LocalDateTime getSubmissionDate() {
    return submissionDate;
  }

  public void setSubmissionDate(LocalDateTime submissionDate) {
    this.submissionDate = submissionDate;
  }

  public String getReferenceId() {
    return referenceId;
  }

  public void setReferenceId(String referenceId) {
    this.referenceId = referenceId;
  }

  @Override
  public String toString() {
    return "Application{" +
        "id='" + applicationId + '\'' +
        ", type='" + applicationType + '\'' +
        ", status='" + status + '\'' +
        '}';
  }
}
