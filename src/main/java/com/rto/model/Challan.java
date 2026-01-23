package com.rto.model;

import java.io.Serializable;
import java.time.LocalDate;

/**
 * Challan model for traffic violations/offenses.
 * Used by Admin to issue challans and by Citizens to pay fines.
 */
public class Challan implements Serializable {
  private static final long serialVersionUID = 1L;

  private String challanId;
  private String vehicleVin; // Vehicle identification (registration number)
  private String offenseType;
  private double amount;
  private LocalDate issueDate;
  private boolean isPaid;
  private String issuedBy; // Officer ID who issued
  private String paymentTransactionId;

  public Challan() {
    this.challanId = "CHN-" + System.currentTimeMillis();
    this.issueDate = LocalDate.now();
    this.isPaid = false;
  }

  public Challan(String vehicleVin, String offenseType, double amount, String issuedBy) {
    this();
    this.vehicleVin = vehicleVin;
    this.offenseType = offenseType;
    this.amount = amount;
    this.issuedBy = issuedBy;
  }

  // Business Logic
  public void markAsPaid(String transactionId) {
    this.isPaid = true;
    this.paymentTransactionId = transactionId;
  }

  public boolean isOverdue() {
    return !isPaid && LocalDate.now().isAfter(issueDate.plusDays(30));
  }

  public double calculatePenalty() {
    if (isOverdue()) {
      return amount * 0.10; // 10% penalty for late payment
    }
    return 0;
  }

  public double getTotalDue() {
    return amount + calculatePenalty();
  }

  // Getters and Setters
  public String getChallanId() {
    return challanId;
  }

  public void setChallanId(String challanId) {
    this.challanId = challanId;
  }

  public String getVehicleVin() {
    return vehicleVin;
  }

  public void setVehicleVin(String vehicleVin) {
    this.vehicleVin = vehicleVin;
  }

  public String getOffenseType() {
    return offenseType;
  }

  public void setOffenseType(String offenseType) {
    this.offenseType = offenseType;
  }

  public double getAmount() {
    return amount;
  }

  public void setAmount(double amount) {
    this.amount = amount;
  }

  public LocalDate getIssueDate() {
    return issueDate;
  }

  public void setIssueDate(LocalDate issueDate) {
    this.issueDate = issueDate;
  }

  public boolean isPaid() {
    return isPaid;
  }

  public void setPaid(boolean paid) {
    isPaid = paid;
  }

  public String getIssuedBy() {
    return issuedBy;
  }

  public void setIssuedBy(String issuedBy) {
    this.issuedBy = issuedBy;
  }

  public String getPaymentTransactionId() {
    return paymentTransactionId;
  }

  public void setPaymentTransactionId(String paymentTransactionId) {
    this.paymentTransactionId = paymentTransactionId;
  }

  @Override
  public String toString() {
    return "Challan{" +
        "id='" + challanId + '\'' +
        ", offense='" + offenseType + '\'' +
        ", amount=" + amount +
        ", paid=" + isPaid +
        '}';
  }
}
