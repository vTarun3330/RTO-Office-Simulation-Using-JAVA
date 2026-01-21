package com.rto.patterns;

// Simulated 3rd Party Payment API (Adaptee)
public class SimulatedThirdPartyPaymentAPI {
  public boolean makeTransaction(String cardNumber, String cvv, double amt) {
    System.out.println("Connecting to Bank...");
    System.out.println("Charging " + amt + " to Card " + cardNumber);
    return true; // Always succeeds for demo
  }
}
