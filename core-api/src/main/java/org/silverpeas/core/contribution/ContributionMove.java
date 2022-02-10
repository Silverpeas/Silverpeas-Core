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
 * FLOSS exception. You should have received a copy of the text describing
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

package org.silverpeas.core.contribution;

import org.silverpeas.core.contribution.model.Contribution;

/**
 * It is a process implied within the move of a contribution between two different locations, each
 * of them being either a node in a component instance or a component instance. Usually such process
 * is about the move of some transverses resources that are used by the contribution being
 * moved such as attachments or comments for example. The process should be invoked only
 * when one or more contributions are moved, not when a component instance is moved from one
 * collaborative space to another one.
 * <p>
 * Each implementation of this interface is invoked by the generic contribution move
 * process to perform their specific task. The process listens for events about contribution
 * move and so each more concrete business service has to send such events once the contribution of
 * which they are in charge is moved. Usually, this interface should be implemented by each
 * transverse services in Silverpeas Core.
 * </p>
 * <p>
 * For each contribution, whatever it is, different kind of transverses resources can be
 * moved and managed. When such a contribution is then moved in Silverpeas, those additional
 * resources require to be then also moved. Nevertheless, both the generic contribution move
 * process and the contribution itself aren't usually aware of them. It is then the responsibility
 * of the services behind the different kinds of resources to take in charge the move of the
 * resources related to the contribution that has been created. For doing, they have to implement
 * this interface.
 * </p>
 * @author mmoquillon
 */
public interface ContributionMove {

  /**
   * Updates the resources belonging to the specified contribution. This method is invoked
   * by the generic contribution modification service when an event about the modification of a
   * contribution is received.
   * @param before the contribution before the move and from which the source location can be
   * figuring out.
   * @param after the contribution after the move and from which the destination location can be
   * figuring out.
   */
  void move(final Contribution before, final Contribution after);
}
