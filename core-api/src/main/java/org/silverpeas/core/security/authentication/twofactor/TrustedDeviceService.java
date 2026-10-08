/*
 * Copyright (C) 2000 - 2026 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 */
package org.silverpeas.core.security.authentication.twofactor;

/**
 * Service managing trusted devices used to skip the second authentication factor
 * after a successful two-factor authentication.
 */
public interface TrustedDeviceService {

  /**
   * Creates a new trusted-device token for the user.
   *
   * @param userId the Silverpeas user identifier.
   * @param userAgent the user-agent of the trusted browser.
   * @return the clear-text token. It must only be sent to the browser.
   */
  String create(int userId, String userAgent);

  /**
   * Validates the supplied token and rotates it when it is valid.
   *
   * @param userId the Silverpeas user identifier.
   * @param token the clear-text token received from the browser.
   * @param userAgent the current user-agent.
   * @return a new clear-text token, or {@code null} when the token is invalid or expired.
   */
  String validateAndRotate(int userId, String token, String userAgent);

  /**
   * Returns the remaining lifetime, in seconds, of a trusted-device token.
   * Returns zero when the token is unknown or expired.
   */
  long getRemainingLifetime(int userId, String token);

  /**
   * Revokes every trusted device of the user.
   *
   * @param userId the Silverpeas user identifier.
   */
  void revokeAll(int userId);
}
