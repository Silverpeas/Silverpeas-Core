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

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import org.silverpeas.core.util.CollectionUtil;
import org.silverpeas.core.util.logging.SilverLogger;

import java.util.Collection;
import java.util.EnumSet;
import java.util.Set;

import static org.silverpeas.core.SilverpeasExceptionMessages.unknown;

/**
 * @author Yohann Chastagnier
 * Date: 30/12/13
 */
public enum AccessControlOperation {
  UNKNOWN, CREATION, MODIFICATION, DELETION, DOWNLOAD, SHARING, SEARCH;

  static final Set<AccessControlOperation> PERSIST_ACTIONS =
      EnumSet.of(CREATION, MODIFICATION, DELETION);

  @JsonValue
  public String getName() {
    return name().toLowerCase();
  }

  @JsonCreator
  public static AccessControlOperation from(String name) {
    try {
      return valueOf(name.toUpperCase());
    } catch (Exception e) {
      SilverLogger.getLogger(AccessControlOperation.class)
          .warn(unknown("access control operation", name), e);
      return UNKNOWN;
    }
  }

  /**
   * Indicates if it exists a persist action from the given collection.
   * @param accessControlOperations actions.
   * @return true if it exists at least one of persist action, false otherwise.
   */
  public static boolean isPersistActionFrom(
      Collection<AccessControlOperation> accessControlOperations) {
    return CollectionUtil.isNotEmpty(accessControlOperations) && !CollectionUtil
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
        accessControlOperations.contains(SHARING);
  }

  /**
   * Indicates if it exists a download action from the given collection.
   * @param accessControlOperations actions.
   * @return true if it exists a download action, false otherwise.
   */
  public static boolean isDownloadActionFrom(
      Collection<AccessControlOperation> accessControlOperations) {
    return CollectionUtil.isNotEmpty(accessControlOperations) &&
        accessControlOperations.contains(DOWNLOAD);
  }

  /**
   * Indicates if it exists a search action from the given collection.
   * <p>
   * A search action means that all the specific details of resources have been already processed
   * (e.g. visibility of publication) before using the {@link AccessController}.
   * </p>
   * @param accessControlOperations actions.
   * @return true if it exists a search action, false otherwise.
   */
  public static boolean isSearchActionFrom(
      Collection<AccessControlOperation> accessControlOperations) {
    return CollectionUtil.isNotEmpty(accessControlOperations) &&
        accessControlOperations.contains(SEARCH);
  }
}
