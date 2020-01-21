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

package org.silverpeas.cmis.walkers;

import org.apache.chemistry.opencmis.commons.data.ObjectData;
import org.apache.chemistry.opencmis.commons.data.ObjectInFolderContainer;
import org.apache.chemistry.opencmis.commons.data.ObjectInFolderList;
import org.apache.chemistry.opencmis.commons.data.ObjectParentData;
import org.apache.chemistry.opencmis.commons.exceptions.CmisObjectNotFoundException;
import org.silverpeas.cmis.Filtering;
import org.silverpeas.cmis.Paging;
import org.silverpeas.core.cmis.model.CmisObject;
import org.silverpeas.core.cmis.model.Space;
import org.silverpeas.core.Identifiable;
import org.silverpeas.core.NotSupportedException;
import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.annotation.Service;
import org.silverpeas.core.i18n.AbstractI18NBean;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.List;
import java.util.stream.Stream;

/**
 * A selector of the correct {@link CmisObjectsTreeWalker} object to use for walking the subtree
 * rooted to a node of a given type in the CMIS objects tree. The selector plays then the role of
 * the delegator to the {@link CmisObjectsTreeWalker} object that knows how to handle the CMIS
 * object (and hence the Silverpeas object mapped with it) that is implied in the method invocation.
 * <p>
 * The CMIS objects tree is made up of branches whose nodes can be of several types. Each type
 * provides to the node some properties like, for example, the types of nodes it can have as
 * children. So, because walking the subtree rooted to a given node depends of the type of that
 * node, it is required for each type of node to have a specific {@link CmisObjectsTreeWalker}
 * object. The selector responsibility is to find this walker and then to delegate the method call
 * to it.
 * </p>
 * @author mmoquillon
 */
@Service
@Singleton
public class TreeWalkerSelector extends CmisObjectsTreeWalker {

  private static final String INVALID_DIRECT_CALL_TO = "Invalid direct call to ";
  @Inject
  private TreeWalkerForSpaceInst walkerForSpaceInst;

  @Inject
  private TreeWalkerForComponentInst walkerForComponentInst;

  protected CmisObjectsTreeWalker selectByObjectId(final String objectId) {
    if (Space.isSpace(objectId)) {
      return walkerForSpaceInst;
    } else if (objectId.startsWith("kmelia")) {
      return walkerForComponentInst;
    } else {
      throw new CmisObjectNotFoundException(
          String.format("Object %s not exposed in the CMIS objects tree", objectId));
    }
  }

  protected CmisObjectsTreeWalker selectByPath(final String path) {
    throw new NotSupportedException(INVALID_DIRECT_CALL_TO + getClass().getSimpleName());
  }

  @Override
  public ObjectData getObjectData(final String objectId, final Filtering filtering) {
    return selectByObjectId(objectId).getObjectData(objectId, filtering);
  }

  @Override
  public ObjectData getObjectDataByPath(final String path, final Filtering filtering) {
    return walkerForSpaceInst.getObjectDataByPath(path, filtering);
  }

  @Override
  public List<ObjectParentData> getParentsData(final String objectId, final Filtering filtering) {
    return selectByObjectId(objectId).getParentsData(objectId, filtering);
  }

  @Override
  public ObjectInFolderList getChildrenData(final String folderId, final Filtering filtering,
      final Paging paging) {
    return selectByObjectId(folderId).getChildrenData(folderId, filtering, paging);
  }

  @Override
  public List<ObjectInFolderContainer> getSubTreeData(final String folderId,
      final Filtering filtering, final long depth) {
    return selectByObjectId(folderId).getSubTreeData(folderId, filtering, depth);
  }

  @Override
  protected List<ObjectInFolderContainer> browseObjectsInFolderTree(final Identifiable object,
      final Filtering filter, final long depth) {
    throw new NotSupportedException(INVALID_DIRECT_CALL_TO + getClass().getSimpleName());
  }

  @Override
  protected <T extends Identifiable> T getSilverpeasObjectById(final String objectId) {
    throw new NotSupportedException(INVALID_DIRECT_CALL_TO + getClass().getSimpleName());
  }

  @Override
  protected <T extends AbstractI18NBean & Identifiable> Stream<T> getAllowedChildrenOfSilverpeasObject(
      final String parentId, final User user) {
    throw new NotSupportedException(INVALID_DIRECT_CALL_TO + getClass().getSimpleName());
  }

  @Override
  protected CmisObject createCmisObject(final Object silverpeasObject, final String language) {
    throw new NotSupportedException(INVALID_DIRECT_CALL_TO + getClass().getSimpleName());
  }

  @Override
  protected ObjectInFolderList browseObjectsInFolder(final Identifiable object, final Filtering filter,
      final Paging paging) {
    throw new NotSupportedException(INVALID_DIRECT_CALL_TO + getClass().getSimpleName());
  }

  @Override
  protected List<ObjectParentData> browseParentsOfObject(final Identifiable object,
      final Filtering filtering) {
    throw new NotSupportedException(INVALID_DIRECT_CALL_TO + getClass().getSimpleName());
  }
}
  