package com.rto.model;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Model class for Vehicle Registration Request
 * Used for citizen vehicle registration approval workflow
 */
public class VehicleRequest implements Serializable {
  private static final long serialVersionUID = 1L;

  private String requestId;
  private String applicantId;
  private String applicantName;
  private String vehicleType;
  private String vehicleModel;
  private String vehicleSpec;
  private String status; // PENDING, APPROVED, REJECTED
  private LocalDateTime submissionDate;
  private String approvedBy;
  private LocalDateTime approvalDate;

  public VehicleRequest() {
    this.requestId = "VREQ" + System.currentTimeMillis();
    this.status = "PENDING";
    this.submissionDate = LocalDateTime.now();
  }

  public VehicleRequest(String applicantId, String applicantName, String vehicleType,
      String vehicleModel, String vehicleSpec) {
    this();
    this.applicantId = applicantId;
    this.applicantName = applicantName;
    this.vehicleType = vehicleType;
    this.vehicleModel = vehicleModel;
    this.vehicleSpec = vehicleSpec;
  }

  // Getters and Setters
  public String getRequestId() {
    return requestId;
  }

  public void setRequestId(String requestId) {
    this.requestId = requestId;
  }

  public String getApplicantId() {
    return applicantId;
  }

  public void setApplicantId(String applicantId) {
    this.applicantId = applicantId;
  }

  public String getApplicantName() {
    return applicantName;
  }

  public void setApplicantName(String applicantName) {
    this.applicantName = applicantName;
  }

  public String getVehicleType() {
    return vehicleType;
  }

  public void setVehicleType(String vehicleType) {
    this.vehicleType = vehicleType;
  }

  public String getVehicleModel() {
    return vehicleModel;
  }

  public void setVehicleModel(String vehicleModel) {
    this.vehicleModel = vehicleModel;
  }

  public String getVehicleSpec() {
    return vehicleSpec;
  }

  public void setVehicleSpec(String vehicleSpec) {
    this.vehicleSpec = vehicleSpec;
  }

  public String getStatus() {
    return status;
  }

  public void setStatus(String status) {
    this.status = status;
  }

  public LocalDateTime getSubmissionDate() {
    return submissionDate;
  }

  public void setSubmissionDate(LocalDateTime submissionDate) {
    this.submissionDate = submissionDate;
  }

  public String getApprovedBy() {
    return approvedBy;
  }

  public void setApprovedBy(String approvedBy) {
    this.approvedBy = approvedBy;
  }

  public LocalDateTime getApprovalDate() {
    return approvalDate;
  }

  public void setApprovalDate(LocalDateTime approvalDate) {
    this.approvalDate = approvalDate;
  }

  @Override
  public String toString() {
    return "VehicleRequest{" +
        "requestId='" + requestId + '\'' +
        ", applicantName='" + applicantName + '\'' +
        ", vehicleType='" + vehicleType + '\'' +
        ", vehicleModel='" + vehicleModel + '\'' +
        ", status='" + status + '\'' +
        '}';
  }
}
