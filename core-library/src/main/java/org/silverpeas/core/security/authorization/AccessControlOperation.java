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

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import org.apache.commons.collections.CollectionUtils;
import org.silverpeas.core.util.CollectionUtil;

import java.util.Collection;
import java.util.EnumSet;

/**
 * User: Yohann Chastagnier
 * Date: 30/12/13
 */
public enum AccessControlOperation {
  unknown, creation, modification, deletion, download, sharing;

  public static EnumSet<AccessControlOperation> PERSIST_ACTIONS =
      EnumSet.of(creation, modification, deletion);

  @JsonValue
  public String getName() {
    return name();
  }

  @JsonCreator
  public static AccessControlOperation from(String name) {
    try {
      return valueOf(name);
    } catch (Exception e) {
      return unknown;
    }
  }

  /**
   * Indicates if it exists a persist action from the given collection.
   * @param accessControlOperations actions.
   * @return true if it exists at least one of persist action, false otherwise.
   */
  public static boolean isPersistActionFrom(
      Collection<AccessControlOperation> accessControlOperations) {
    return CollectionUtil.isNotEmpty(accessControlOperations) && !CollectionUtils
        .intersection(AccessControlOperation.PERSIST_ACTIONS, accessControlOperations).isEmpty();
  }

  /**
   * Indicates if it exists a sharing action from the given collection.
   * @param accessControlOperations actions.
   * @return true if it exists a sharing action, false otherwise.
   */
  public static boolean isSharingActionFrom(
      Collection<AccessControlOperation> accessControlOperations) {
    return CollectionUtil.isNotEmpty(accessControlOperations) &&
        accessControlOperations.contains(sharing);
  }

  /**
   * Indicates if it exists a download action from the given collection.
   * @param accessControlOperations actions.
   * @return true if it exists a download action, false otherwise.
   */
  public static boolean isDownloadActionFrom(
      Collection<AccessControlOperation> accessControlOperations) {
    return CollectionUtil.isNotEmpty(accessControlOperations) &&
        accessControlOperations.contains(download);
  }
}
