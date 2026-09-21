/*
 * Copyright (C) 2000 - 2026 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
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
    private final String encryptedSecret;
    private final Status status;
    private final Instant createdAt;
    private final Instant updatedAt;
    private final Instant lastUsedAt;

    private TwoFactorAuthentication(Builder builder) {
        this.userId = builder.userId;
        this.encryptedSecret = builder.encryptedSecret;
        this.status = builder.status;
        this.createdAt = builder.createdAt;
        this.updatedAt = builder.updatedAt;
        this.lastUsedAt = builder.lastUsedAt;
    }

    public int getUserId() {
        return userId;
    }

    public String getEncryptedSecret() {
        return encryptedSecret;
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
                .secret(encryptedSecret)
                .status(status)
                .createdAt(createdAt)
                .updatedAt(updatedAt)
                .lastUsedAt(lastUsedAt);
    }

    public static Builder builder(int userId) {
        return new Builder(userId);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof TwoFactorAuthentication)) {
            return false;
        }
        TwoFactorAuthentication that = (TwoFactorAuthentication) o;
        return userId == that.userId
                && Objects.equals(encryptedSecret, that.encryptedSecret)
                && status == that.status
                && Objects.equals(createdAt, that.createdAt)
                && Objects.equals(updatedAt, that.updatedAt)
                && Objects.equals(lastUsedAt, that.lastUsedAt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId, encryptedSecret, status,
                createdAt, updatedAt, lastUsedAt);
    }

    @Override
    public String toString() {
        return "TwoFactorAuthentication{" +
                "userId=" + userId +
                ", status=" + status +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                ", lastUsedAt=" + lastUsedAt +
                '}';
    }

    public static final class Builder {

        private final int userId;
        private String encryptedSecret;
        private Status status = Status.DISABLED;
        private Instant createdAt;
        private Instant updatedAt;
        private Instant lastUsedAt;

        private Builder(int userId) {
            this.userId = userId;
        }

        public Builder secret(String encryptedSecret) {
            this.encryptedSecret = encryptedSecret;
            return this;
        }

        public Builder status(Status status) {
            this.status = status;
            return this;
        }

        public Builder createdAt(Instant createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public Builder updatedAt(Instant updatedAt) {
            this.updatedAt = updatedAt;
            return this;
        }

        public Builder lastUsedAt(Instant lastUsedAt) {
            this.lastUsedAt = lastUsedAt;
            return this;
        }

        public TwoFactorAuthentication build() {
            return new TwoFactorAuthentication(this);
        }
    }
}