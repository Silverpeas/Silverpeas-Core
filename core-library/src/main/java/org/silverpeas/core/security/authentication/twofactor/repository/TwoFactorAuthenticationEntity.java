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
package org.silverpeas.core.security.authentication.twofactor.repository;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Id;
import jakarta.persistence.Column;
import java.time.Instant;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/**
 * Persistent representation of ST_User_2FA.
 */
@Entity
@Table(name = "ST_User_2FA")
public class TwoFactorAuthenticationEntity {

  @Id
  @Column(name = "userId", nullable = false)
  int userId;
  @Column(name = "secret", length = 1024, nullable = false)
  String secret;
  @Column(name = "status", length = 20, nullable = false)
  String status;
  @JdbcTypeCode(SqlTypes.TIMESTAMP)
  @Column(name = "createdAt", nullable = false)
  Instant createdAt;
  @JdbcTypeCode(SqlTypes.TIMESTAMP)
  @Column(name = "updatedAt", nullable = false)
  Instant updatedAt;
  @JdbcTypeCode(SqlTypes.TIMESTAMP)
  @Column(name = "lastUsedAt")
  Instant lastUsedAt;
  @Column(name = "failedAttempts", nullable = false)
  int failedAttempts;
  @JdbcTypeCode(SqlTypes.TIMESTAMP)
  @Column(name = "lockedUntil")
  Instant lockedUntil;

  protected TwoFactorAuthenticationEntity() {
  }
}
