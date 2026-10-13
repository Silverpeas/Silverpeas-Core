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
 * One recovery code associated with a user.
 *
 * The clear text code is never persisted.
 */
public final class RecoveryCode implements Serializable {

    private static final long serialVersionUID = 1L;

    private final long id;
    private final int userId;
    private final String hash;
    private final boolean used;
    private final Instant createdAt;
    private final Instant usedAt;

    private RecoveryCode(Builder builder) {
        this.id = builder.id;
        this.userId = builder.userId;
        this.hash = builder.hash;
        this.used = builder.used;
        this.createdAt = builder.createdAt;
        this.usedAt = builder.usedAt;
    }

    public long getId() {
        return id;
    }

    public int getUserId() {
        return userId;
    }

    public String getHash() {
        return hash;
    }

    public boolean isUsed() {
        return used;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUsedAt() {
        return usedAt;
    }

    public Builder toBuilder() {
        return builder(userId)
                .id(id)
                .hash(hash)
                .used(used)
                .createdAt(createdAt)
                .usedAt(usedAt);
    }

    public static Builder builder(int userId) {
        return new Builder(userId);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof RecoveryCode)) {
            return false;
        }
        RecoveryCode that = (RecoveryCode) o;
        return id == that.id
                && userId == that.userId
                && used == that.used
                && Objects.equals(hash, that.hash)
                && Objects.equals(createdAt, that.createdAt)
                && Objects.equals(usedAt, that.usedAt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, userId, hash, used, createdAt, usedAt);
    }

    @Override
    public String toString() {
        return "RecoveryCode{" +
                "id=" + id +
                ", userId=" + userId +
                ", used=" + used +
                '}';
    }

    public static final class Builder {

        private final int userId;
        private long id;
        private String hash;
        private boolean used;
        private Instant createdAt;
        private Instant usedAt;

        private Builder(int userId) {
            this.userId = userId;
        }

        public Builder id(long id) {
            this.id = id;
            return this;
        }

        public Builder hash(String hash) {
            this.hash = hash;
            return this;
        }

        public Builder used(boolean used) {
            this.used = used;
            return this;
        }

        public Builder createdAt(Instant createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public Builder usedAt(Instant usedAt) {
            this.usedAt = usedAt;
            return this;
        }

        public RecoveryCode build() {
            return new RecoveryCode(this);
        }
    }
}