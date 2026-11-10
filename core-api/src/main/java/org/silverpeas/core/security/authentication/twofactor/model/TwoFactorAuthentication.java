/*
 * Copyright (C) 2000 - 2026 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have received a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "https://www.silverpeas.org/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package org.silverpeas.core.security.authentication.twofactor.model;

import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;

/**
 * Two-factor authentication configuration associated with one Silverpeas user.
 */
public final class TwoFactorAuthentication implements Serializable {

  private static final long serialVersionUID = 1L;

  /**
   * Lifecycle state of an authenticator enrollment.
   */
  public enum Status {
    /**
     * No second factor is active.
     */
    DISABLED,
    /**
     * A secret exists but ownership of the authenticator is not yet confirmed.
     */
    PENDING,
    /**
     * The confirmed authenticator is available for authentication.
     */
    ENABLED
  }

  private final int userId;
  private final String secret;
  private final Status status;
  private final Instant createdAt;
  private final Instant updatedAt;
  private final Instant lastUsedAt;
  private final int failedAttempts;
  private final Instant lockedUntil;

  private TwoFactorAuthentication(final Builder builder) {
    this.userId = builder.userId;
    this.secret = builder.secret;
    this.status = builder.status;
    this.createdAt = builder.createdAt;
    this.updatedAt = builder.updatedAt;
    this.lastUsedAt = builder.lastUsedAt;
    this.failedAttempts = builder.failedAttempts;
    this.lockedUntil = builder.lockedUntil;
  }

  /**
   * Gets the Silverpeas identifier of the owner.
   *
   * @return the Silverpeas identifier of the owner.
   */
  public int getUserId() {
    return userId;
  }

  /**
   * Gets the TOTP secret.
   *
   * <p>The repository is responsible for encrypting this value before
   * persisting it.</p>
   *
   * @return the TOTP secret.
   */
  public String getSecret() {
    return secret;
  }

  /**
   * Gets the enrollment status.
   *
   * @return the enrollment status.
   */
  public Status getStatus() {
    return status;
  }

  /**
   * Gets the creation instant.
   *
   * @return the creation instant.
   */
  public Instant getCreatedAt() {
    return createdAt;
  }

  /**
   * Gets the last configuration update instant.
   *
   * @return the last configuration update instant.
   */
  public Instant getUpdatedAt() {
    return updatedAt;
  }

  /**
   * Gets the last successful second-factor authentication instant, or {@code null}.
   *
   * @return the last successful second-factor authentication instant, or {@code null}.
   */
  public Instant getLastUsedAt() {
    return lastUsedAt;
  }

  /**
   * Gets the number of consecutive failed validations.
   *
   * @return the number of consecutive failed validations.
   */
  public int getFailedAttempts() {
    return failedAttempts;
  }

  /**
   * Gets the lock expiration instant, or {@code null} when unlocked.
   *
   * @return the lock expiration instant, or {@code null} when unlocked.
   */
  public Instant getLockedUntil() {
    return lockedUntil;
  }

  /**
   * Indicates whether validation is temporarily locked at the current instant.
   *
   * @return {@code true} while the lock has not expired.
   */
  public boolean isLocked() {
    return isLocked(Instant.now());
  }

  /**
   * Indicates whether validation is temporarily locked at the specified instant.
   *
   * @param now the instant at which to evaluate the lock.
   * @return {@code true} while the lock has not expired.
   */
  public boolean isLocked(final Instant now) {
    return lockedUntil != null && lockedUntil.isAfter(now);
  }

  /**
   * Indicates whether the authenticator is enrolled and can authenticate the user.
   *
   * @return {@code true} if the authenticator is enrolled and can authenticate the user.
   */
  public boolean isEnabled() {
    return status == Status.ENABLED;
  }

  /**
   * Indicates whether enrollment still requires confirmation with a valid code.
   *
   * @return {@code true} if enrollment still requires confirmation with a valid code.
   */
  public boolean isPending() {
    return status == Status.PENDING;
  }

  /**
   * Indicates whether two-factor authentication is disabled.
   *
   * @return {@code true} if two-factor authentication is disabled.
   */
  public boolean isDisabled() {
    return status == Status.DISABLED;
  }

  /**
   * Creates a builder initialized with every value of this configuration.
   *
   * @return a builder for a modified copy.
   */
  public Builder toBuilder() {
    return builder(userId)
        .secret(secret)
        .status(status)
        .createdAt(createdAt)
        .updatedAt(updatedAt)
        .lastUsedAt(lastUsedAt)
        .failedAttempts(failedAttempts)
        .lockedUntil(lockedUntil);
  }

  /**
   * Creates a builder for the specified user.
   *
   * @param userId the Silverpeas identifier of the owner.
   * @return a new builder.
   */
  public static Builder builder(final int userId) {
    return new Builder(userId);
  }

  @Override
  public boolean equals(final Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof TwoFactorAuthentication)) {
      return false;
    }
    final TwoFactorAuthentication that = (TwoFactorAuthentication) o;
    return userId == that.userId
        && failedAttempts == that.failedAttempts
        && Objects.equals(secret, that.secret)
        && status == that.status
        && Objects.equals(createdAt, that.createdAt)
        && Objects.equals(updatedAt, that.updatedAt)
        && Objects.equals(lastUsedAt, that.lastUsedAt)
        && Objects.equals(lockedUntil, that.lockedUntil);
  }

  @Override
  public int hashCode() {
    return Objects.hash(userId, secret, status, createdAt, updatedAt, lastUsedAt,
        failedAttempts, lockedUntil);
  }

  @Override
  public String toString() {
    return "TwoFactorAuthentication{" +
        "userId=" + userId +
        ", status=" + status +
        ", createdAt=" + createdAt +
        ", updatedAt=" + updatedAt +
        ", lastUsedAt=" + lastUsedAt +
        ", failedAttempts=" + failedAttempts +
        ", lockedUntil=" + lockedUntil +
        '}';
  }

  /**
   * Builds an immutable two-factor authentication configuration.
   */
  public static final class Builder {

    private final int userId;
    private String secret;
    private Status status = Status.DISABLED;
    private Instant createdAt;
    private Instant updatedAt;
    private Instant lastUsedAt;
    private int failedAttempts;
    private Instant lockedUntil;

    private Builder(final int userId) {
      this.userId = userId;
    }

    /**
     * Sets the Base32-encoded TOTP secret in clear text.
     *
     * @param secret the Base32-encoded TOTP secret in clear text.
     * @return this builder.
     */
    public Builder secret(final String secret) {
      this.secret = secret;
      return this;
    }

    /**
     * Sets the enrollment status.
     *
     * @param status the enrollment status.
     * @return this builder.
     */
    public Builder status(final Status status) {
      this.status = status;
      return this;
    }

    /**
     * Sets the creation instant.
     *
     * @param createdAt the creation instant.
     * @return this builder.
     */
    public Builder createdAt(final Instant createdAt) {
      this.createdAt = createdAt;
      return this;
    }

    /**
     * Sets the last configuration update instant.
     *
     * @param updatedAt the last configuration update instant.
     * @return this builder.
     */
    public Builder updatedAt(final Instant updatedAt) {
      this.updatedAt = updatedAt;
      return this;
    }

    /**
     * Sets the last successful second-factor authentication instant, or {@code null}.
     *
     * @param lastUsedAt the last successful second-factor authentication instant, or {@code null}.
     * @return this builder.
     */
    public Builder lastUsedAt(final Instant lastUsedAt) {
      this.lastUsedAt = lastUsedAt;
      return this;
    }

    /**
     * Sets the number of consecutive failed validations.
     *
     * @param failedAttempts the number of consecutive failed validations.
     * @return this builder.
     */
    public Builder failedAttempts(final int failedAttempts) {
      this.failedAttempts = failedAttempts;
      return this;
    }

    /**
     * Sets the lock expiration instant, or {@code null} when unlocked.
     *
     * @param lockedUntil the lock expiration instant, or {@code null} when unlocked.
     * @return this builder.
     */
    public Builder lockedUntil(final Instant lockedUntil) {
      this.lockedUntil = lockedUntil;
      return this;
    }

    /**
     * Creates the immutable configuration from the builder values.
     *
     * @return the resulting configuration.
     */
    public TwoFactorAuthentication build() {
      return new TwoFactorAuthentication(this);
    }
  }
}
