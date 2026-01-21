package com.rto.model;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Transaction model representing payment transactions
 * Tracks all financial transactions in the RTO system
 */
public class Transaction implements Serializable {
  private String transactionId;
  private String userId;
  private double amount;
  private LocalDateTime timestamp;
  private String paymentMethod; // "CREDIT_CARD", "DEBIT_CARD", "UPI", "NET_BANKING"
  private String transactionType; // "VEHICLE_REGISTRATION", "LICENSE_FEE", "TAX_PAYMENT", "RENEWAL"
  private String referenceId; // Vehicle ID or License ID
  private String status; // "SUCCESS", "FAILED", "PENDING"

  public Transaction() {
    this.timestamp = LocalDateTime.now();
    this.transactionId = generateTransactionId();
  }

  public Transaction(String userId, double amount, String paymentMethod,
      String transactionType, String referenceId) {
    this();
    this.userId = userId;
    this.amount = amount;
    this.paymentMethod = paymentMethod;
    this.transactionType = transactionType;
    this.referenceId = referenceId;
    this.status = "PENDING";
  }

  private String generateTransactionId() {
    return "TXN" + System.currentTimeMillis();
  }

  public void markSuccess() {
    this.status = "SUCCESS";
  }

  public void markFailed() {
    this.status = "FAILED";
  }

  // Getters and Setters
  public String getTransactionId() {
    return transactionId;
  }

  public void setTransactionId(String transactionId) {
    this.transactionId = transactionId;
  }

  public String getUserId() {
    return userId;
  }

  public void setUserId(String userId) {
    this.userId = userId;
  }

  public double getAmount() {
    return amount;
  }

  public void setAmount(double amount) {
    this.amount = amount;
  }

  public LocalDateTime getTimestamp() {
    return timestamp;
  }

  public void setTimestamp(LocalDateTime timestamp) {
    this.timestamp = timestamp;
  }

  public String getPaymentMethod() {
    return paymentMethod;
  }

  public void setPaymentMethod(String paymentMethod) {
    this.paymentMethod = paymentMethod;
  }

  public String getTransactionType() {
    return transactionType;
  }

  public void setTransactionType(String transactionType) {
    this.transactionType = transactionType;
  }

  public String getReferenceId() {
    return referenceId;
  }

  public void setReferenceId(String referenceId) {
    this.referenceId = referenceId;
  }

  public String getStatus() {
    return status;
  }

  public void setStatus(String status) {
    this.status = status;
  }

  @Override
  public String toString() {
    return "Transaction{" +
        "transactionId='" + transactionId + '\'' +
        ", amount=" + amount +
        ", timestamp=" + timestamp +
        ", status='" + status + '\'' +
        '}';
  }
}
