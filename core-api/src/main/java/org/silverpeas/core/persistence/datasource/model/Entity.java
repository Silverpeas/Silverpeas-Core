/*
 * Copyright (C) 2000 - 2020 Silverpeas
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
package org.silverpeas.core.persistence.datasource.model;

import org.silverpeas.core.admin.user.model.User;

import java.util.Date;

/**
 * An entity is an object managed or handled by Silverpeas and that can be persisted into a data
 * source. This interface must be implemented by all Silverpeas entity definitions that have to be
 * persisted.
 *
 * It provides signatures for the following technical data :
 * <ul>
 * <li>entity identifier</li>
 * <li>entity "created by" (a free string)</li>
 * <li>entity user creator (if "created by" is a known user id)</li>
 * <li>entity create date</li>
 * <li>entity "last updated by" (a free string)</li>
 * <li>entity last user updater (if "last updated by" is a known user id)</li>
 * <li>entity last update date</li>
 * </ul>
 * @param <ENTITY> specify the class name of the entity itself which is handled by a repository
 * manager.
 * @param <IDENTIFIER_TYPE> the identifier class name used by {@link ENTITY} for its primary key
 * definition.
 * @author Yohann Chastagnier
 */
public interface Entity<ENTITY extends Entity<ENTITY, IDENTIFIER_TYPE>,
    IDENTIFIER_TYPE extends EntityIdentifier> extends IdentifiableEntity {

  /**
   * Gets the date of the entity creation (in the persistence environment).
   * @return the date at which this entity was created.
   */
  Date getCreationDate();

  /**
   * Gets unique identifier of the user that has created this entity.
   * @return the unique identifier of the creator.
   */
  String getCreatorId();

  /**
   * Gets the user which has created the entity (in the persistence environment).
   * (if "created by" is a known user id)
   * @return the creator.
   */
  User getCreator();

  /**
   * Sets explicitly the user who has created the entity (in the persistence context).
   * The creation date will be set at now.
   * <p>
   * The Silverpeas persistence system sets automatically the creator from the operation context
   * in the current thread and it sets automatically the creation date at the date the entity is
   * persisted. By using this method, you overrides this behaviour.
   * </p>
   * @param creator the creator of this entity.
   * @return itself.
   */
  ENTITY createdBy(User creator);

  /**
   * sets the user who has created the entity (in the persistence environment) with the date of the
   * creation.
   *  <p>
   * The Silverpeas persistence system sets automatically the creator from the operation context
   * in the current thread and it sets automatically the creation date at the date the entity is
   * persisted. By using this method, you overrides this behaviour.
   * </p>
   * @param creator the creator of this entity.
   * @param creationDate the date at which the entity is created.
   * @return itself.
   */
  ENTITY createdBy(User creator, Date creationDate);

  /**
   * Gets the last date and time of the entity update (in the persistence context). If the entity
   * wasn't yet updated since its inception, then the creation date is returned.
   * @return the last date at which this entity was updated.
   */
  Date getLastUpdateDate();

  /**
   * Gets the unique identifier of the last updater of this entity.
   * @return the unique identifier of the last updater.
   */
  String getLastUpdaterId();

  /**
   * Gets the last user which has updated the entity (in the persistence context).
   * If the entity hasn't yet been updated since its inception, then the creator is considered the
   * last user that touched the entity and hence it is returned.
   * @return the last updater.
   */
  User getLastUpdater();

  /**
   * Sets explicitly the last user who has updated the entity (in the persistence context). The
   * last update date will be set at now.
   *  <p>
   * The Silverpeas persistence system sets automatically the updater from the operation context
   * in the current thread and it sets automatically the update date at the date the entity is
   * updated in the persistence context. By using this method, you overrides this behaviour.
   * </p>
   * @param updater the last updater to set.
   * @return itself.
   */
  ENTITY updatedBy(User updater);

  /**
   * sets the user who has updated the entity (in the persistence context) with the date of the
   * update.
   *  <p>
   * The Silverpeas persistence system sets automatically the updater from the operation context
   * in the current thread and it sets automatically the update date at the date the entity is
   * updated in the persistence cotnext. By using this method, you overrides this behaviour.
   * </p>
   * @param updater the user who updates this entity.
   * @param updateDate the date at which the entity is updated.
   * @return itself.
   */
  ENTITY updatedBy(User updater, Date updateDate);

  /**
   * Gets the version of the entity (in the persistence context).
   * @return the version of this entity.
   */
  Long getVersion();

  /**
   * Indicates if the entity has been modified at least one time.
   * @return true if this entity was modified since its first fetching from the database. False
   * otherwise.
   */
  boolean hasBeenModified();

  /**
   * By default, if no functional data is changed, last update date, last updated by and version
   * technical data are not automatically updated on entity save operation.
   * But in some cases, it could be useful that this three above technical data are updated, even if
   * functional data are not changed.
   * So, calling this method ensures that last update date, last updated by and version will be
   * updated.
   */
  void markAsModified();
}
