/*
 * Copyright (C) 2000 - 2014 Silverpeas
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
package org.silverpeas.core.security.authorization;

import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * This class permits to define the context of access to a resource.
 */
public class AccessControlContext {

  private Set<AccessControlOperation> operations = EnumSet.noneOf(AccessControlOperation.class);
  private Map<String, Object> cache = new HashMap<String, Object>();

  /**
   * Gets an initialized instance of access control context.
   * @return
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
   * @param operations
   * @return
   */
  public AccessControlContext onOperationsOf(AccessControlOperation... operations) {
    Collections.addAll(this.operations, operations);
    return this;
  }

  /**
   * Gets the operations performed into the context.
   * @return
   */
  public Set<AccessControlOperation> getOperations() {
    if (operations.isEmpty()) {
      return Collections.unmodifiableSet(Collections.singleton(AccessControlOperation.unknown));
    }
    return Collections.unmodifiableSet(operations);
  }

  /**
   * Puts into context a value linked to a key.
   * @param key the key.
   * @param value the value.
   * @param <T>
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
   * @param <T>
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
