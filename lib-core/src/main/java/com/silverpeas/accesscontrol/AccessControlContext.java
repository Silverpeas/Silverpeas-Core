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
package com.silverpeas.accesscontrol;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

/**
 * This class permits to define the context of access to a resource.
 * User: Yohann Chastagnier
 * Date: 30/12/13
 */
public class AccessControlContext {

  private Set<AccessControlOperation> operations = EnumSet.noneOf(AccessControlOperation.class);

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
}
