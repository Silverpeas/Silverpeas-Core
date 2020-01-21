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

package org.silverpeas.cmis;

import org.apache.chemistry.opencmis.commons.data.ObjectData;
import org.apache.chemistry.opencmis.commons.data.ObjectInFolderContainer;
import org.apache.chemistry.opencmis.commons.data.ObjectInFolderList;
import org.apache.chemistry.opencmis.commons.data.ObjectParentData;
import org.apache.chemistry.opencmis.commons.exceptions.CmisInvalidArgumentException;
import org.silverpeas.cmis.walkers.CmisObjectsTreeWalker;
import org.silverpeas.core.annotation.Service;

import java.util.List;

/**
 * A manager of CMIS objects that are stored into Silverpeas. It is responsible to map the
 * organizational resources (spaces, component instances, etc.) and contributions in Silverpeas to
 * the organizational tree of CMIS objects (folders, documents, relationships, and so on). It is
 * the one that has the knowledge of how the Silverpeas objects are mapped to the CMIS objects
 * while satisfying the CMIS specification requirements. It is dedicated to be used by a CMIS
 * repository implementation.
 * <p>
 * In order to facilitate the mapping between the organizational tree of a CMIS objects and the
 * organization of the Silverpeas resources, the manager defines a one-to-one correspondence of its
 * methods with those of the CMIS services. Hence, the client of the manager has just to delegate
 * the CMIS service's operation by invoking one of the manager's method with the correct parameters
 * correspondence.
 * </p>
 * <p>
 * The walking down/up and the modification operations on the CMIS objects tree (and therefore their
 * corresponding Silverpeas resources and contributions) is performed by a
 * {@link CmisObjectsTreeWalker} instance that knows both how to walk down/up a subtree and how to
 * perform CRUD operations on the object of that subtree by the type of the object that is passed
 * through. For example, the walking down a subtree isn't done identically when rooted to a space
 * than when rooted to an application (as they don't accept the same children type).
 * </p>
 * @author mmoquillon
 */
@Service
public class SilverpeasCmisObjectManager {

  /**
   * Gets the specified object in Silverpeas.
   * @param objectId the unique identifier of the object.
   * @param filtering filtering parameters to apply on the characteristics to include with the
   * object.
   * @return the data about the asked object.
   */
  public ObjectData getObject(final String objectId, final Filtering filtering) {
    checkArgumentNotNull("object", objectId);
    return CmisObjectsTreeWalker.getInstance().getObjectData(objectId, filtering);
  }

  /**
   * Gets the object that is defined at the specified path in the organizational CMIS tree.
   * @param path the path of the object from the root. The path is made up of segments, each of
   * them separated by the '/' character. A segment can be either the name of a CMIS fileable
   * object or its unique identifier. For better performance we recommend strongly to use
   * object's identifiers for segments.
   * @param filtering parameters to apply on the characteristics to include with the
   * object.
   * @return the data about the asked object.
   */
  public ObjectData getObjectByPath(final String path, final Filtering filtering) {
    checkArgumentNotNull("path", path);
    if (path.trim().isEmpty() || path.charAt(0) != '/') {
      throw new CmisInvalidArgumentException("The path isn't valid!");
    }
    return CmisObjectsTreeWalker.getInstance().getObjectDataByPath(path, filtering);
  }

  /**
   * Gets the objects that are children of the specified folder.
   * @param folderId the unique identifier of a folder, whatever its concrete type in Silverpeas.
   * @param filtering the filtering parameters to apply on the characteristics to include with each
   * object.
   * @param paging paging parameters to apply on the list of objects to return.
   * @return a list of objects that are children of a folder.
   */
  public ObjectInFolderList getChildren(final String folderId, final Filtering filtering,
      final Paging paging) {
    checkArgumentNotNull("folder", folderId);
    return CmisObjectsTreeWalker.getInstance().getChildrenData(folderId, filtering, paging);
  }

  /**
   * Gets the objects that are the descendents of the specified folder down to the given
   * depth of its offspring tree.
   * @param folderId the unique identifier of a folder, whatever its concrete type in Silverpeas.
   * @param filtering the filtering parameters to apply on the characteristics to include with each
   * object.
   * @param depth the depth of the offspring tree to walk across. If 1, only
   * the direct children of the specified folder are returned, if negative all descendant objects at
   * all depth levels in the CMIS hierarchy are returned, any positive value means only objects that
   * are children of the folder and descendants down to the given levels deep.
   * @return a list of the children of the specified folder with for each their own children, and
   * so one, down to the given depth.
   */
  public List<ObjectInFolderContainer> getDescendants(final String folderId,
      final Filtering filtering, final long depth) {
    checkArgumentNotNull("folder", folderId);
    return CmisObjectsTreeWalker.getInstance().getSubTreeData(folderId, filtering, depth);
  }

  /**
   * Gets the parents of the specified object.
   * @param objectId the unique identifier of an object in Silverpeas, whatever its concrete type
   * in Silverpeas.
   * @param filtering the filtering parameters to apply on the list of objects to return.
   * @return a list of folders that are parents of other objects.
   */
  public List<ObjectParentData> getParents(final String objectId, final Filtering filtering) {
    checkArgumentNotNull("object", objectId);
    return CmisObjectsTreeWalker.getInstance().getParentsData(objectId, filtering);
  }

  private static void checkArgumentNotNull(final String argName, final Object arg) {
    if (arg == null) {
      final String name = argName.substring(0, 1).toUpperCase() + argName.substring(1);
      throw new CmisInvalidArgumentException(name + " is not valid!");
    }
  }
}
  