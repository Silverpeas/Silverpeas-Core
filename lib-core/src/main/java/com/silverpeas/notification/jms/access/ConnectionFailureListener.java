package com.silverpeas.notification.jms.access;

/**
 * A listener of JMS connection failures.
 *
 * When a connection is broken or unexpected closed, the JMS implementation then will open the
 * lost connection and will inform a such listener about the event. The listener can then perform
 * specific tasks with the JMS service like reallocating some resources that could be lost with
 * the connection failure.
 */
public interface ConnectionFailureListener {

  void onConnectionFailure();
}
