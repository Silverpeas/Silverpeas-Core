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
 * FLOSS exception. You should have received a copy of the text describing
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

package org.silverpeas.core.cmis.model;

/**
 * A CMIS object with the capabilities of being file-able into the CMIS objects tree. Those include
 * CMIS folders, CMIS documents, CMIS policies and CMIS items.
 * @author mmoquillon
 */
public interface Fileable {

  /**
   * Gets the unique identifier of the folder into which this object is filed.
   * @return the unique identifier of the parent of this fileable instance or null if it is a root
   * folder. Cannot be null or empty for any non-folder objects.
   */
  String getParentId();

  /**
   * Gets the path of this object from the root folder in the CMIS objects tree.
   * @return a slash-separated path whose the first slash is the root folder in the CMIS objects
   * tree.
   */
  String getPath();

  /**
   * Is this file-able object orphaned? A file-able object is orphaned if and only if it isn't
   * in the CMIS objects tree and, as such, it has no parent. In this case, the
   * {@link Fileable#getParentId()} returns null.
   * @return
   */
  default boolean isOrphaned() {
    return getParentId() == null;
  }
}
