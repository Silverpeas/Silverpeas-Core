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

import java.util.Collections;
import java.util.List;

/**
 * The abstract representation of a Silverpeas resource as a CMIS folder. In CMIS, a Folder is a
 * file-able CMIS object that can contain others file-ables CMIS objects. In Silverpeas, a space,
 * a component instance (aka application), a topic in an EDM, a gallery in a media library, ... are
 * all a container of other objects and hence they can be represented by a CMIS folder.
 * @author mmoquillon
 */
public abstract class CmisFolder extends CmisObject implements Folding {

  public static final String PATH_SEPARATOR = "/";

  private String parentId;

  /**
   * Gets the types of the objects this type of folder accept as children. This method returns
   * an empty list by default and requires to be overridden by the children classes in the case
   * the {@link CmisFolder} is a CMIS Folder. Otherwise, nothing is returned.
   * @return a list with all the {@link TypeId} that can be a children of such a folder type. If it
   * isn't a folder, then returns an empty list. By default, this implementation returns an empty
   * list.
   */
  public static List<TypeId> getAllowedChildrenType() {
    return Collections.emptyList();
  }

  CmisFolder(final String id, final String name, final String language) {
    super(id, name, language);
  }

  @Override
  public String getParentId() {
    return parentId;
  }

  @Override
  public abstract String getPath();

  /**
   * Sets the unique identifier of the parent to this folder is any. If null, then this folder is a
   * root one in the CMIS objects tree. Otherwise, the identifier must be the one of another CMIS
   * folder.
   * @param parentId the unique identifier of the parent folder.
   * @return either the unique identifier of a folder, parent of it, or null if this folder is a
   * root one in the CMIS objects tree.
   */
  public CmisFolder setParentId(final String parentId) {
    this.parentId = parentId;
    return this;
  }

  /**
   * Gets the segment of the path of this folder in the CMIS objects tree relative to its parent.
   * The path of an object in the tree identifies it uniquely in the tree. Each segment of a path is
   * the name of the objects that made up the path down to this object. The value depends on the
   * context within which the folder is built:
   * <ul>
   *   <li>in the case the folder is get as child: the segment is its name.</li>
   *   <li>in the case the folder is get as parent of a given object: the segment is the name
   *   of the given object; it is the segment of the path of the object relative to it.</li>
   * </ul>
   * @return the name of this object as referred in its path.
   */
  public String getPathSegment() {
    final String path = getPath();
    int idx = path.lastIndexOf('/');
    return path.substring(idx + 1);
  }
}
  