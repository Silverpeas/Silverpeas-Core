package org.silverpeas.core.security.authentication;

/**
 * A connection with a server of a remote authentication service.
 * It wraps the actual object of type T used to communicate with the server.
 * @param <T> the type of the connector to use when communicating with the remote server. The
 * connector is specific to the authentication service.
 */
public class AuthenticationConnection<T> {

  private final T connector;

  /**
   * Constructs a new connection with an authentication server by using the specified  specific server
   * connector.
   * @param connector a connector specific to the remote authentication server.
   */
  public AuthenticationConnection(final T connector) {
    this.connector = connector;
  }

  /**
   * Gets the connector specific to the remote authentication server used by this connection.
   * @return a connector of the authentication server.
   */
  public T getConnector() {
    return connector;
  }
}
