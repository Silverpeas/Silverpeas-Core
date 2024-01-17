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
package org.silverpeas.core.security.authorization;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static java.util.EnumSet.noneOf;

/**
 * This class permits to define the context of access to a resource.
 */
public class AccessControlContext {

  private final Set<AccessControlOperation> operations = noneOf(AccessControlOperation.class);
  private final Map<String, Object> cache = new HashMap<>();

  /**
   * Gets an initialized instance of access control context.
   * @return an empty {@link AccessControlContext} instance.
   */
  public static AccessControlContext init() {
    return new AccessControlContext();
  }

  /**
   * Default hidden constructor.
   */
  private AccessControlContext() {
  }

  /**
   * Defines the operations performed into the context.
   * @param operations one or several {@link AccessControlOperation} instances.
   * @return the completed context itself.
   */
  public AccessControlContext onOperationsOf(AccessControlOperation... operations) {
    Collections.addAll(this.operations, operations);
    return this;
  }

  /**
   * Removes the operations from the context. This is an explicit method that allows
   * {@link AccessController} implementations to adjust the context by adding (forcing) an
   * operation before a treatment and removing it when done.
   * @param operations one or several {@link AccessControlOperation} instances.
   * @return the context itself.
   */
  public AccessControlContext removeOperationsOf(AccessControlOperation... operations) {
    this.operations.removeAll(Set.of(operations));
    return this;
  }

  /**
   * Gets the operations performed into the context.
   * @return a set of {@link AccessControlOperation} instances.
   */
  public Set<AccessControlOperation> getOperations() {
    if (operations.isEmpty()) {
      return Collections.singleton(AccessControlOperation.UNKNOWN);
    }
    return Collections.unmodifiableSet(operations);
  }

  /**
   * Puts into context a value linked to a key.
   * @param key the key.
   * @param value the value.
   * @param <T> the type of resource the access control is performed.
   * @return the current instance.
   */
  public <T> AccessControlContext put(String key, T value) {
    cache.put(key, value);
    return this;
  }

  /**
   * Gets from context a value from a key that has been stored into the context instance.
   * @param key the key associated to the searched value.
   * @param classType the type of expected value.
   * @param <T> the type of resource the access control is performed.
   * @return the value if any, null if the expected type does not match with the one of the existing
   * value.
   */
  @SuppressWarnings("unchecked")
  public <T> T get(String key, Class<T> classType) {
    Object value = cache.get(key);
    if (value == null || !classType.isAssignableFrom(value.getClass())) {
      return null;
    }
    return (T) value;
  }
}
