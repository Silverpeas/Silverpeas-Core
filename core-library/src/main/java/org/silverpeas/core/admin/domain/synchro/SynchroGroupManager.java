/*
 * Copyright (C) 2000 - 2024 Silverpeas
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
package org.silverpeas.core.admin.domain.synchro;

import org.silverpeas.core.admin.service.Administration;
import org.silverpeas.core.admin.user.model.Group;
import org.silverpeas.core.util.ServiceProvider;

import javax.annotation.PostConstruct;

/**
 * This manager maintains a context of synchronized groups in the final aim to perform manual or
 * scheduled synchronizations.
 * <p>
 *   A secondary goal of this manager is to avoid to request the database each time a
 *   synchronization is requested. That is why it maintains a context of synchronized group that
 *   MUST be updated mainly by administration services.
 * </p>
 * @apiNote the synchronization group manager MUST be application scoped in order to be handled
 * properly.
 * @implNote no @{@link PostConstruct} annotation is used to call {@link #resetContext()} to
 * initialize the context. The reset method is called by {@link Administration} implementation.
 */
public interface SynchroGroupManager {

  static SynchroGroupManager get() {
    return ServiceProvider.getService(SynchroGroupManager.class);
  }

  /**
   * Performs the synchronization of synchronized groups.
   * @apiNote the synchronized groups are those from the contexts, no database requests are
   * performed.
   * @implSpec implementation takes into account that several call can be performed at a same time.
   */
  void synchronize();

  /**
   * Updates the context of the manager with the given group data.
   * <p>
   *   If the group is a synchronized one, then it will be added to the list of group to perform
   *   update on. If it is not synchronized, then it is removed from this list.
   * </p>
   * @param group data representing a group.
   * @throws IllegalArgumentException if group identifier does not exist into data.
   */
  void updateContextWith(final Group group);

  /**
   * Removes the given group from the context of the manager.
   * @param group data representing a group.
   * @throws IllegalArgumentException if group identifier does not exist into data.
   */
  void removeFromContext(final Group group);

  /**
   * Resets the context of the manager.
   * @apiNote it performs mainly the load of synchronized groups from the repository (database).
   */
  void resetContext();
}
