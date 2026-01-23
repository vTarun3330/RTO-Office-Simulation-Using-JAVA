package com.rto.service;

import com.rto.model.Transaction;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Transaction Service - Handles payment transaction operations
 */
public class TransactionService implements IService {
  private DatabaseService db;

  public TransactionService() {
    this.db = DatabaseService.getInstance();
  }

  @Override
  public void initialize() {
    System.out.println("TransactionService initialized");
  }

  /**
   * Record a new transaction
   */
  public boolean recordTransaction(Transaction transaction) {
    String sql = """
        INSERT INTO transactions
        (transaction_id, user_id, amount, timestamp, payment_method, transaction_type, reference_id, status)
        VALUES (?, ?, ?, ?, ?, ?, ?, ?)
        """;

    try {
      return db.executeUpdate(sql,
          transaction.getTransactionId(),
          transaction.getUserId(),
          transaction.getAmount(),
          Timestamp.valueOf(transaction.getTimestamp()),
          transaction.getPaymentMethod(),
          transaction.getTransactionType(),
          transaction.getReferenceId(),
          transaction.getStatus());
    } catch (Exception e) {
      System.err.println("Error recording transaction: " + e.getMessage());
      return false;
    }
  }

  /**
   * Convenience method to record a transaction with individual parameters.
   */
  public boolean recordTransaction(String userId, double amount, String paymentMethod,
      String transactionType, String referenceId) {
    Transaction txn = new Transaction();
    txn.setUserId(userId);
    txn.setAmount(amount);
    txn.setPaymentMethod(paymentMethod);
    txn.setTransactionType(transactionType);
    txn.setReferenceId(referenceId);
    txn.setStatus("SUCCESS");
    return recordTransaction(txn);
  }

  /**
   * Get all transactions for a user
   */
  public List<Transaction> getTransactionsByUserId(String userId) {
    List<Transaction> transactions = new ArrayList<>();
    String sql = "SELECT * FROM transactions WHERE user_id = ? ORDER BY timestamp DESC";

    try (ResultSet rs = db.executeQuery(sql, userId)) {
      while (rs != null && rs.next()) {
        Transaction txn = mapResultSetToTransaction(rs);
        transactions.add(txn);
      }
    } catch (SQLException e) {
      System.err.println("Error getting transactions: " + e.getMessage());
    }
    return transactions;
  }

  /**
   * Get all transactions
   */
  public List<Transaction> getAllTransactions() {
    List<Transaction> transactions = new ArrayList<>();
    String sql = "SELECT * FROM transactions ORDER BY timestamp DESC";

    try (ResultSet rs = db.executeQuery(sql)) {
      while (rs != null && rs.next()) {
        transactions.add(mapResultSetToTransaction(rs));
      }
    } catch (SQLException e) {
      System.err.println("Error getting all transactions: " + e.getMessage());
    }
    return transactions;
  }

  /**
   * Get total revenue
   */
  public double getTotalRevenue() {
    String sql = "SELECT SUM(amount) FROM transactions WHERE status = 'SUCCESS'";
    try (ResultSet rs = db.executeQuery(sql)) {
      if (rs != null && rs.next()) {
        return rs.getDouble(1);
      }
    } catch (SQLException e) {
      System.err.println("Error calculating total revenue: " + e.getMessage());
    }
    return 0.0;
  }

  /**
   * Update transaction status
   */
  public boolean updateTransactionStatus(String transactionId, String status) {
    String sql = "UPDATE transactions SET status = ? WHERE transaction_id = ?";
    return db.executeUpdate(sql, status, transactionId);
  }

  private Transaction mapResultSetToTransaction(ResultSet rs) throws SQLException {
    Transaction txn = new Transaction();
    txn.setTransactionId(rs.getString("transaction_id"));
    txn.setUserId(rs.getString("user_id"));
    txn.setAmount(rs.getDouble("amount"));
    txn.setTimestamp(rs.getTimestamp("timestamp").toLocalDateTime());
    txn.setPaymentMethod(rs.getString("payment_method"));
    txn.setTransactionType(rs.getString("transaction_type"));
    txn.setReferenceId(rs.getString("reference_id"));
    txn.setStatus(rs.getString("status"));
    return txn;
  }
}
