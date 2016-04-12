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
package org.silverpeas.core.persistence.datasource.repository;

import org.silverpeas.core.cache.service.CacheServiceProvider;
import org.silverpeas.core.util.ArgumentAssertion;
import org.silverpeas.core.admin.user.model.UserDetail;
import org.silverpeas.core.persistence.datasource.model.Entity;

/**
 * This class permits to give details about operation actions (save action for now), especially the
 * user who is the author of the operation.
 * It is usually used from service method implementation. It is used by JPA Silverpeas API to set
 * the technical entity informations on:
 * <ul>
 *   <li>entity create</li>
 *   <li>entity update</li>
 * </ul>
 * @author Yohann Chastagnier
 */
public class OperationContext {

  private static final String CACHE_KEY = OperationContext.class.getName();
  private boolean updatingInCaseOfCreation = false;

  // The user
  private UserDetail user = null;

  /**
   * Creates an empty instance.
   * @return
   */
  public static OperationContext createInstance() {
    return new OperationContext();
  }

  /**
   * Creates an instance from the given identifier which aims a user.
   * @param userId
   * @return
   */
  public static OperationContext fromUser(String userId) {
    return fromUser(UserDetail.getById(userId));
  }

  /**
   * Creates an instance from the given user.
   * @param user
   * @return
   */
  public static OperationContext fromUser(UserDetail user) {
    return new OperationContext().withUser(user);
  }

  /**
   * Get the current instance of the transaction from a cache (thread cache (request cache
   * exactly)).
   * The call of this method is automatically done by tachnical JPA tools.
   * @return
   */
  public static OperationContext getFromCache() {
    return CacheServiceProvider.getRequestCacheService().get(CACHE_KEY, OperationContext.class);
  }

  /**
   * Default hidden constructor.
   */
  private OperationContext() {
    // Nothing.
  }

  /**
   * Sets the user associated to the save operation.
   * @param user
   * @return
   */
  private OperationContext withUser(UserDetail user) {
    this.user = user;
    return this;
  }

  /**
   * Calling this method to indicates that the current data update is performed in a case of a
   * creation. Indeed, in some cases, the creation of a resource into database is done by a
   * chaining of inserts and updates.
   */
  public OperationContext setUpdatingInCaseOfCreation() {
    return setUpdatingInCaseOfCreation(true);
  }

  /**
   * Calling this method to indicates that the current data update is performed in a case of a
   * creation or not. Indeed, in some cases, the creation of a resource into database is done by a
   * chaining of inserts and updates.
   * @param updatingInCaseOfCreation true to specify that the update is performed in a case of data
   * creation.
   */
  public OperationContext setUpdatingInCaseOfCreation(boolean updatingInCaseOfCreation) {
    this.updatingInCaseOfCreation = updatingInCaseOfCreation;
    return this;
  }

  /**
   * Indicates if the date update is performed in a case of creation.
   * @return
   */
  public boolean isUpdatingInCaseOfCreation() {
    return updatingInCaseOfCreation;
  }

  /**
   * Gets the user behind the operation.
   * @return
   */
  public UserDetail getUser() {
    return user;
  }

  /**
   * Put the current instance into a cache (thread cache (request cache exactly)) to be retrieved
   * from @PrePersist or @PreUpdate operations.
   * The call of this method is automatically done by technical JPA tools.
   */
  public void putIntoCache() {
    CacheServiceProvider.getRequestCacheService().put(CACHE_KEY, this);
  }

  /**
   * Applying informations of the context to the given entity on a persist operation.
   * @param entity
   */
  public void applyToPersistOperation(Entity entity) {
    String errorMessage = "the user identifier must exist when performing persist operation";
    ArgumentAssertion.assertNotNull(user, errorMessage);
    ArgumentAssertion.assertDefined(user.getId(), errorMessage);
    entity.setCreator(user);
    entity.setLastUpdater(user);
  }

  /**
   * Applying informations of the context to the given entity on a update opearation.
   * @param entity
   */
  public void applyToUpdateOperation(Entity entity) {
    String errorMessage = "the user identifier must exist when performing update operation";
    ArgumentAssertion.assertNotNull(user, errorMessage);
    ArgumentAssertion.assertDefined(user.getId(), errorMessage);
    entity.setLastUpdater(user);
  }
}
