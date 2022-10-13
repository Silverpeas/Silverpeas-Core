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
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package org.silverpeas.core;

import org.silverpeas.core.admin.user.model.User;

import java.util.Date;

/**
 * A resource managed in Silverpeas that is uniquely identifiable. This interface is the more
 * generic representation of a resource of any type managed in Silverpeas. All conceptual
 * resources in use in Silverpeas should implement this interface. It encapsulates the more
 * generic methods a unique identifiable resource in Silverpeas should satisfy.
 * @author mmoquillon
 */
public interface SilverpeasResource extends Nameable {

  /**
   * Gets the unique identifier of this resource.
   * @return the {@link ResourceIdentifier} object representing the unique identifier of the
   * resource.
   */
  <T extends ResourceIdentifier> T getIdentifier();

  /**
   * Gets the date at which the resource has been created.
   * @return the date of creation of the resource.
   */
  Date getCreationDate();

  /**
   * Gets the date at which the resource has been lastly updated. If the resource doesn't have such
   * an information, then this method should return the date of the resource creation.
   * @return the date of the last update of the resource.
   */
  Date getLastUpdateDate();

  /**
   * Gets the user that has created the resource.
   * @return a {@link User} in Silverpeas.
   */
  User getCreator();

  /**
   * Gets the user that has lastly updated the resource. If the resource doesn't have such an
   * information, then this method should return the user that has created the resource.
   * @return a {@link User} in Silverpeas.
   */
  User getLastUpdater();
}
