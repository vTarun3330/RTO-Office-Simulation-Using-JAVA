package com.rto.patterns;

/**
 * Concrete Observer - Email Notification
 */
public class EmailNotifier implements NotificationObserver {
  private String emailAddress;

  public EmailNotifier(String emailAddress) {
    this.emailAddress = emailAddress;
  }

  @Override
  public void update(String message) {
    System.out.println("✉️  EMAIL to " + emailAddress + ": " + message);
  }

  @Override
  public String getObserverType() {
    return "EMAIL";
  }
}
