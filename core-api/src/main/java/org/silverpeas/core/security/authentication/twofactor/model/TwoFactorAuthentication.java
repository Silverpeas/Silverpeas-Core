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
 * FLOSS exception.
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

    public enum Status {
        DISABLED,
        PENDING,
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

    public Status getStatus() {
        return status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public Instant getLastUsedAt() {
        return lastUsedAt;
    }

    public int getFailedAttempts() {
        return failedAttempts;
    }

    public Instant getLockedUntil() {
        return lockedUntil;
    }

    public boolean isLocked() {
        return isLocked(Instant.now());
    }

    public boolean isLocked(final Instant now) {
        return lockedUntil != null && lockedUntil.isAfter(now);
    }

    public boolean isEnabled() {
        return status == Status.ENABLED;
    }

    public boolean isPending() {
        return status == Status.PENDING;
    }

    public boolean isDisabled() {
        return status == Status.DISABLED;
    }

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

        public Builder secret(final String secret) {
            this.secret = secret;
            return this;
        }

        public Builder status(final Status status) {
            this.status = status;
            return this;
        }

        public Builder createdAt(final Instant createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public Builder updatedAt(final Instant updatedAt) {
            this.updatedAt = updatedAt;
            return this;
        }

        public Builder lastUsedAt(final Instant lastUsedAt) {
            this.lastUsedAt = lastUsedAt;
            return this;
        }

        public Builder failedAttempts(final int failedAttempts) {
            this.failedAttempts = failedAttempts;
            return this;
        }

        public Builder lockedUntil(final Instant lockedUntil) {
            this.lockedUntil = lockedUntil;
            return this;
        }

        public TwoFactorAuthentication build() {
            return new TwoFactorAuthentication(this);
        }
    }
}