/*
 * Copyright (C) 2000 - 2013 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception. You should have recieved a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.silverpeas.persistence.model;

import com.stratelia.webactiv.beans.admin.UserDetail;

import java.util.Date;

/**
 * This interface must be implemented by all Silverpeas entity definitions that have to be
 * persisted.
 * It provides signatures for the following technical data :
 * - entity identifier
 * - entity "created by" (a free string)
 * - entity user creator (if "created by" is a known user id)
 * - entity create date
 * - entity "last updated by" (a free string)
 * - entity last user updater (if "last updated by" is a known user id)
 * - entity last update date
 * User: Yohann Chastagnier
 * Date: 20/11/13
 */
public interface Entity<ENTITY extends Entity<ENTITY, IDENTIFIER_TYPE>, IDENTIFIER_TYPE> {

  /**
   * Gets the id of the entity.
   * @return
   */
  String getId();

  /**
   * Gets the date of the entity creation (in the persistence environment).
   * @return
   */
  Date getCreateDate();

  /**
   * Gets the free "created by" data.
   * @return
   */
  String getCreatedBy();

  /**
   * Gets the user which has created the entity (in the persistence environment).
   * (if "created by" is a known user id)
   * @return
   */
  UserDetail getCreator();

  /**
   * Sets the user which has created the entity (in the persistence environment).
   * @param creator
   * @return
   */
  ENTITY setCreator(UserDetail creator);

  /**
   * Gets the last date and time of the entity update (in the persistence environment).
   * @return
   */
  Date getLastUpdateDate();

  /**
   * Gets the free "last updated by" data.
   * @return
   */
  String getLastUpdatedBy();

  /**
   * Gets the last user which has updated the entity (in the persistence environment).
   * (if "last updated by" is a known user id)
   * @return
   */
  UserDetail getLastUpdater();

  /**
   * Sets the last user which has updated the entity (in the persistence environment).
   * @param updater
   * @return
   */
  ENTITY setLastUpdater(UserDetail updater);

  /**
   * Gets the version of the entity (in the persistence environment).
   * @return
   */
  Long getVersion();

  /**
   * Indicates if the entity is persisted (commonly if the entity has an id)
   * @return
   */
  boolean isPersisted();

  /**
   * Indicates if the entity has been modified at least one time.
   * @return
   */
  boolean hasBeenModified();
}
