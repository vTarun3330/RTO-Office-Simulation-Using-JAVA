package com.rto.patterns;

// Adapter Pattern Implementation
public class PaymentGatewayAdapter implements IPaymentProcessor {
  private SimulatedThirdPartyPaymentAPI api;
  private String cardNumber;
  private String cvv;

  public PaymentGatewayAdapter(String cardNumber, String cvv) {
    this.api = new SimulatedThirdPartyPaymentAPI();
    this.cardNumber = cardNumber;
    this.cvv = cvv;
  }

  @Override
  public boolean processPayment(double amount) {
    return api.makeTransaction(cardNumber, cvv, amount);
  }
}
