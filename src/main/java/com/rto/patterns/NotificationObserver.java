package com.rto.patterns;

/**
 * Observer Pattern - Observer interface
 * Used for notification system in RTO
 */
public interface NotificationObserver {
  void update(String message);

  String getObserverType();
}
