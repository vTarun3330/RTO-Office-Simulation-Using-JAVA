package com.rto.patterns;

import java.util.ArrayList;
import java.util.List;

/**
 * Observable Subject for notification system
 * Manages observers and sends notifications
 */
public class NotificationSubject {
  private List<NotificationObserver> observers;

  public NotificationSubject() {
    this.observers = new ArrayList<>();
  }

  public void attach(NotificationObserver observer) {
    observers.add(observer);
  }

  public void detach(NotificationObserver observer) {
    observers.remove(observer);
  }

  public void notifyObservers(String message) {
    for (NotificationObserver observer : observers) {
      observer.update(message);
    }
  }
}
