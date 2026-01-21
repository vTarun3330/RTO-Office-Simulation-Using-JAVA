package com.rto.patterns;

/**
 * Concrete Observer - SMS Notification
 */
public class SMSNotifier implements NotificationObserver {
  private String phoneNumber;

  public SMSNotifier(String phoneNumber) {
    this.phoneNumber = phoneNumber;
  }

  @Override
  public void update(String message) {
    System.out.println("📱 SMS to " + phoneNumber + ": " + message);
  }

  @Override
  public String getObserverType() {
    return "SMS";
  }
}
