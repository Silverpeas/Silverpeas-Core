/*
 * Copyright (C) 2000 - 2021 Silverpeas
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
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.silverpeas.core.wbe;

import java.time.OffsetDateTime;
import java.util.StringJoiner;

import static java.time.OffsetDateTime.now;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.silverpeas.core.util.StringUtil.isDefined;

/**
 * This class permits to handle Web Browser Edition file locking from point of view of the HOST.
 * <p>
 * When a lock is set, it is alive for 30 minutes.
 * </p>
 * @author silveryocha
 */
public class WbeFileLock {

  private String id;
  private OffsetDateTime lastLockDate;

  protected WbeFileLock() {
    clear();
  }

  /**
   * Gets the identifier of the lock if any.
   * @return a string if any, empty string if none or if the lock timer is over.
   */
  public String id() {
    checkTimer();
    return id;
  }

  /**
   * Sets a new lock identifier.
   * <p>
   * The lock timer is set with {@link OffsetDateTime#now()}.
   * </p>
   * @param id a lock identifier.
   */
  public void setId(final String id) {
    this.id = id;
    lastLockDate = now();
  }

  /**
   * Clears the lock identifier and the associated timer.
   */
  public void clear() {
    id = EMPTY;
    lastLockDate = null;
  }

  /**
   * Indicates if a lock exists.
   * <p>
   * Even if {@link #setId(String)} or {@link #clear()} have not been called, if lock timer is
   * over, no lock is concidered.
   * </p>
   * @return true if exists, false otherwise.
   */
  public boolean exists() {
    checkTimer();
    return isDefined(id) && lastLockDate != null;
  }

  private void checkTimer() {
    if (lastLockDate != null && lastLockDate.plusMinutes(30).compareTo(now()) < 0) {
      clear();
    }
  }

  @Override
  public String toString() {
    return new StringJoiner(", ", WbeFileLock.class.getSimpleName() + "[", "]")
        .add("id='" + id + "'").add("lastLockDate=" + lastLockDate).toString();
  }
}
