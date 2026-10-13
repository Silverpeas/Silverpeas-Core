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

import org.silverpeas.core.security.authentication.twofactor.model.RecoveryCode;

import java.time.Instant;
import java.util.List;

/**
 * Repository of recovery codes used by the native two-factor authentication.
 */
public interface RecoveryCodeRepository {

    /**
     * Gets all unused recovery codes of a user.
     *
     * @param userId the Silverpeas user identifier.
     * @return the unused recovery codes.
     */
    List<RecoveryCode> getUnused(int userId);

    /**
     * Creates a recovery code.
     *
     * @param recoveryCode the code to persist.
     */
    void save(RecoveryCode recoveryCode);

    /**
     * Atomically consumes an unused recovery code belonging to the specified user.
     *
     * @param userId the Silverpeas user identifier.
     * @param hash the hash of the recovery code.
     * @param usedAt the date at which the code was consumed.
     * @return true if an unused code belonging to the user was consumed.
     */
    boolean consume(int userId, String hash, Instant usedAt);

    /**
     * Deletes all recovery codes of a user.
     *
     * @param userId the Silverpeas user identifier.
     */
    void deleteAll(int userId);
}
