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
 * It is a process implied within the creation of a contribution. Usually such process is
 * about the creation of some transverses resources that are used by the contribution being
 * created such as attachments or PdC classification for example. The process should be invoked only
 * when one or more contributions are created, not when a component instance is created from a
 * template with some predefined contributions.
 * <p>
 * Each implementation of this interface is invoked by the generic contribution creation
 * process to perform their specific task. The process listens for events about contribution
 * creation and so each more concrete business service working on a given type of contribution has
 * to send such events once they created a contribution. Usually, this interface should be
 * implemented by each transverse services in Silverpeas Core.
 * </p>
 * <p>
 * For each contribution, whatever it is, different kind of transverses resources can be
 * created and managed. When such a contribution is then created in Silverpeas, those additional
 * resources require to be then also created. Nevertheless, both the generic contribution creation
 * process and the contribution itself aren't usually aware of them. It is then the responsibility
 * of the services behind the different kinds of resources to take in charge the creation of the
 * resources related to the contribution that has been created. For doing, they have to implement
 * this interface.
 * </p>
 * @author mmoquillon
 */
public interface ContributionCreation {

  /**
   * Creates the resources belonging to the specified contribution. This method is invoked
   * by the generic contribution creation service when an event about the creation of a contribution
   * is received.
   * @param contribution the contribution that was created.
   */
  void create(final Contribution contribution);
}
