package com.rto.patterns;

import java.time.LocalDateTime;
import java.util.Random;

/**
 * Simulated Third-Party Payment API (Adaptee in Adapter Pattern).
 * This simulates an external banking/payment gateway API.
 * In production, this would be replaced with actual payment gateway
 * integration.
 */
public class SimulatedThirdPartyPaymentAPI {

  private static final Random random = new Random();
  private String apiKey;
  private String merchantId;

  public SimulatedThirdPartyPaymentAPI() {
    this.apiKey = "RTO_DEMO_API_KEY_2024";
    this.merchantId = "RTO_OFFICE_001";
  }

  public SimulatedThirdPartyPaymentAPI(String apiKey, String merchantId) {
    this.apiKey = apiKey;
    this.merchantId = merchantId;
  }

  /**
   * Main transaction method - simulates external payment processing.
   * 
   * @param cardNumber Credit/Debit card number
   * @param cvv        Card verification value
   * @param amount     Transaction amount
   * @return true if transaction succeeded, false otherwise
   */
  public boolean makeTransaction(String cardNumber, String cvv, double amount) {
    System.out.println("═══════════════════════════════════════════");
    System.out.println("  PAYMENT GATEWAY - Processing Transaction  ");
    System.out.println("  Merchant: " + merchantId + " | API: " + apiKey.substring(0, 8) + "...");
    System.out.println("═══════════════════════════════════════════");
    System.out.println("🔗 Connecting to Bank Server...");

    // Simulate network delay
    simulateDelay(500);

    // Validate card details
    if (!validateCard(cardNumber, cvv)) {
      System.out.println("❌ Transaction FAILED: Invalid card details");
      return false;
    }

    // Validate amount
    if (amount <= 0) {
      System.out.println("❌ Transaction FAILED: Invalid amount");
      return false;
    }

    System.out.println("✓ Card validated");
    System.out.println("💳 Card: **** **** **** " + getLastFourDigits(cardNumber));
    System.out.println("💰 Amount: ₹" + String.format("%.2f", amount));

    // Simulate processing
    simulateDelay(300);
    System.out.println("⏳ Processing payment...");
    simulateDelay(200);

    // 95% success rate for demo
    boolean success = random.nextDouble() < 0.95;

    if (success) {
      String txnRef = generateTransactionReference();
      System.out.println("✅ Transaction SUCCESSFUL");
      System.out.println("📝 Reference: " + txnRef);
      System.out.println("📅 Time: " + LocalDateTime.now());
    } else {
      System.out.println("❌ Transaction FAILED: Bank declined");
    }

    System.out.println("═══════════════════════════════════════════");
    return success;
  }

  /**
   * Check card balance (simulated).
   */
  public double checkBalance(String cardNumber, String cvv) {
    if (!validateCard(cardNumber, cvv)) {
      return -1;
    }
    // Return a random balance between 10000 and 100000
    return 10000 + random.nextDouble() * 90000;
  }

  /**
   * Refund a transaction (simulated).
   */
  public boolean refundTransaction(String transactionRef, double amount) {
    System.out.println("🔄 Processing refund for " + transactionRef);
    simulateDelay(300);
    System.out.println("✅ Refund of ₹" + String.format("%.2f", amount) + " processed");
    return true;
  }

  /**
   * Verify card validity without charging.
   */
  public boolean verifyCard(String cardNumber, String cvv) {
    System.out.println("🔍 Verifying card...");
    return validateCard(cardNumber, cvv);
  }

  // Private helper methods

  private boolean validateCard(String cardNumber, String cvv) {
    if (cardNumber == null || cardNumber.isEmpty())
      return false;
    if (cvv == null || cvv.isEmpty())
      return false;

    // Remove spaces and dashes
    String cleanedCard = cardNumber.replaceAll("[\\s-]", "");

    // Check card number length (13-19 digits)
    if (cleanedCard.length() < 13 || cleanedCard.length() > 19)
      return false;

    // Check if all digits
    if (!cleanedCard.matches("\\d+"))
      return false;

    // Check CVV (3-4 digits)
    if (!cvv.matches("\\d{3,4}"))
      return false;

    // Luhn algorithm validation (simplified)
    return luhnCheck(cleanedCard);
  }

  private boolean luhnCheck(String cardNumber) {
    int sum = 0;
    boolean alternate = false;

    for (int i = cardNumber.length() - 1; i >= 0; i--) {
      int digit = Character.getNumericValue(cardNumber.charAt(i));

      if (alternate) {
        digit *= 2;
        if (digit > 9)
          digit -= 9;
      }

      sum += digit;
      alternate = !alternate;
    }

    // Luhn validation: sum should be divisible by 10
    // For demo purposes, we accept cards with proper format
    return sum >= 0; // In production: return sum % 10 == 0;
  }

  private String getLastFourDigits(String cardNumber) {
    String cleaned = cardNumber.replaceAll("[\\s-]", "");
    if (cleaned.length() >= 4) {
      return cleaned.substring(cleaned.length() - 4);
    }
    return "****";
  }

  private String generateTransactionReference() {
    return "TXN" + System.currentTimeMillis() + String.format("%04d", random.nextInt(10000));
  }

  private void simulateDelay(int millis) {
    try {
      Thread.sleep(millis);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
    }
  }
}
