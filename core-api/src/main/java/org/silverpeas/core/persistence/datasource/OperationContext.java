/*
 * Copyright (C) 2000 - 2019 Silverpeas
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
package org.silverpeas.core.persistence.datasource;

import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.cache.model.SimpleCache;
import org.silverpeas.core.cache.service.CacheServiceProvider;
import org.silverpeas.core.util.ServiceProvider;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * <p>
 * This class permits to give details about operation actions (save action for now), especially the
 * user who is the trigger of the operation.
 * It is usually used from service method implementation. It is used by JPA Silverpeas API to set
 * the technical entity information on:
 * </p>
 * <ul>
 *   <li>entity create</li>
 *   <li>entity update</li>
 * </ul>
 * <p>
 * As each operation in Silverpeas is relative to a thread, such an operation context is then
 * attached to the thread that processes the operation. So, the operation context can be get by
 * any processing actors within the same thread without being worried on the context
 * transmission between them.
 * </p>
 * @author Yohann Chastagnier
 */
public class OperationContext {

  private static final String CACHE_KEY = OperationContext.class.getName();

  private List<State> states = new ArrayList<>();
  private boolean updatingInCaseOfCreation = false;

  // The user
  private User user = null;
  private List<PersistenceOperation> persistenceOperations = new ArrayList<>();

  /**
   * Creates an empty instance.
   * @return a new {@link OperationContext} instance.
   */
  private OperationContext() {
    // Nothing.
  }

  /**
   * Creates an instance from the given identifier which aims a user. If an operation context
   * already exists for the current thread, then sets it with the specified user and returns it.
   * @param userId the unique identifier of the user at the source of the operation.
   * @return a new {@link OperationContext} for the specified user identifier.
   */
  public static OperationContext fromUser(String userId) {
    return fromUser(User.getById(userId));
  }

  /**
   * Creates an instance from the given user. If an operation context already exists for the current
   * thread, then sets it with the specified user and returns it.
   * @param user the user at the source of the operation.
   * @return a new {@link OperationContext} for the specified user.
   */
  public static OperationContext fromUser(User user) {
    return getFromCache().withUser(user);
  }

  /**
   * Creates an instance from the current requester ({@link User#getCurrentRequester()}). If an
   * operation context exists for the current thread, then returns it. Otherwise, constructs a new
   * one from the current requester of the operation (if any). If there is no user set, then set
   * it with the current requester before returning it.
   * @return a new {@link OperationContext} for the current user.
   */
  public static OperationContext fromCurrentRequester() {
    OperationContext context = getFromCache();
    return context.getUser() == null ? context.withUser(User.getCurrentRequester()) : context;
  }

  /**
   * Get the current {@link OperationContext} instance from a cache (thread cache (request cache
   * exactly)).
   * The call of this method is automatically done by technical JPA tools.
   * @return the current {@link OperationContext} instance from the cache.
   */
  public static OperationContext getFromCache() {
    final SimpleCache cache = CacheServiceProvider.getRequestCacheService().getCache();
    OperationContext context = cache.get(CACHE_KEY, OperationContext.class);
    if (context == null) {
      context = new OperationContext();
      cache.put(CACHE_KEY, context);
    }
    return context;
  }

  /**
   * Indicates if the given states are well set into context.
   * @param states the states to verify.
   * @return true if the specified states exists into the context, false otherwise.
   */
  public static boolean statesOf(final State... states) {
    final OperationContext context = getFromCache();
    if (context.states.isEmpty()) {
      return false;
    }
    for (State state : states) {
      if (!context.states.contains(state)) {
        return false;
      }
    }
    return true;
  }

  /**
   * Adds states into context.
   * @param states the states to add.
   * @return itself.
   */
  public static OperationContext addStates(final State... states) {
    final OperationContext context = getFromCache();
    context.states.addAll(Arrays.asList(states));
    return context;
  }

  /**
   * Removes states from context.
   * @param states the states to remove.
   * @return itself.
   */
  public static OperationContext removeStates(final State... states) {
    final OperationContext context = getFromCache();
    context.states.removeAll(Arrays.asList(states));
    return context;
  }

  /**
   * Sets the user associated to the save operation.
   * @param user the user to set.
   * @return itself.
   */
  public OperationContext withUser(User user) {
    this.user = user;
    return this;
  }

  /**
   * Calling this method to indicates that the current data update is performed in a case of a
   * creation. Indeed, in some cases, the creation of a resource into database is done by a
   * chaining of inserts and updates.
   * @return itselft.
   */
  public OperationContext setUpdatingInCaseOfCreation() {
    return setUpdatingInCaseOfCreation(true);
  }

  /**
   * Indicates if the date update is performed in a case of creation.
   * @return true if the date is updated, false otherwise.
   */
  public boolean isUpdatingInCaseOfCreation() {
    return updatingInCaseOfCreation;
  }

  /**
   * Calling this method to indicates that the current data update is performed in a case of a
   * creation or not. Indeed, in some cases, the creation of a resource into database is done by a
   * chaining of inserts and updates.
   * @param updatingInCaseOfCreation true to specify that the update is performed in a case of data
   * creation.
   * @return itself
   */
  public OperationContext setUpdatingInCaseOfCreation(boolean updatingInCaseOfCreation) {
    this.updatingInCaseOfCreation = updatingInCaseOfCreation;
    return this;
  }

  /**
   * Gets the user behind the operation.
   * @return the user concerned by the operation.
   */
  public User getUser() {
    return user;
  }

  /**
   * Gets the current active persistence operation from this context.
   * @param operationType the type of the expected persistence operation.
   * @param <T> the concrete type of the implementation of {@link PersistenceOperation} class.
   * @return a persistence operation matching the expected type. Null if no such type exists.
   */
  @SuppressWarnings("unchecked")
  public <T extends PersistenceOperation> T getPersistenceOperation(
      final Class<T> operationType) {
    Annotation annotation = operationType.getAnnotation(UpdateOperation.class);
    if (annotation == null) {
      annotation = operationType.getAnnotation(PersistOperation.class);
      if (annotation == null) {
        return null;
      }
    }

    final Annotation qualifier = annotation;
    return (T) persistenceOperations.stream()
        .filter(c -> c.getClass().getAnnotation(qualifier.annotationType()) != null &&
            c.getClass().equals(operationType))
        .findFirst()
        .orElseGet(() -> {
          PersistenceOperation c = ServiceProvider.getService(operationType, qualifier);
          persistenceOperations.add(c);
          return c;
        });
  }


  public enum State {
    EXPORT, IMPORT
  }
}
