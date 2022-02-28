/*
 * Copyright (C) 2000 - 2022 Silverpeas
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

package org.silverpeas.core.persistence.jcr;

import java.time.ZonedDateTime;
import java.time.temporal.Temporal;
import java.util.List;
import java.util.Optional;

/**
 * This interface defines a task monitor dedicated to the JCR DataStore management.
 */
public interface JcrDatastoreTaskMonitor {

  /**
   * Gets the types of task monitor.
   * <p>
   *   "DATASTORE_PURGE" for example.
   * </p>
   * @return a string.
   */
  String getType();

  /**
   * Gets the running status list of the task.
   * @return a {@link Status} instance.
   */
  List<Status> getStatuses();

  /**
   * Gets the error if any.
   * @return an optional {@link Exception} instance.
   */
  Optional<Exception> getError();

  /**
   * Gets the number of processed nodes.
   * @return a long.
   */
  long getNbNodeProcessed();

  /**
   * Status of a {@link JcrDatastoreTaskMonitor} instance at a given instant.
   */
  class Status {
    private final ZonedDateTime at = ZonedDateTime.now();
    private final StatusType type;

    public Status(final StatusType type) {
      this.type = type;
    }

    public Temporal getAt() {
      return at;
    }

    public StatusType getType() {
      return type;
    }
  }

  /**
   * Different task status types
   */
  enum StatusType {
    WAITING, STARTED, MARKING, DELETING, TERMINATED
  }
}
