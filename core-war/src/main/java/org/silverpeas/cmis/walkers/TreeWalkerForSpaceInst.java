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
import org.jetbrains.annotations.NotNull;
import org.silverpeas.cmis.Filtering;
import org.silverpeas.cmis.Paging;
import org.silverpeas.core.Identifiable;
import org.silverpeas.core.admin.space.SpaceInstLight;
import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.annotation.Service;
import org.silverpeas.core.cmis.model.CmisFolder;
import org.silverpeas.core.cmis.model.CmisObject;
import org.silverpeas.core.cmis.model.Space;
import org.silverpeas.core.i18n.AbstractI18NBean;
import org.silverpeas.core.util.StringUtil;

import javax.inject.Singleton;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

/**
 * A {@link CmisObjectsTreeWalker} object that knows how to walk the subtree rooted to a
 * collaborative space in Silverpeas. It takes care of the virtual root node in the CMIS objects
 * tree that represents the virtual container of all of the root spaces in Silverpeas.
 * @author mmoquillon
 */
@Service
@Singleton
public class TreeWalkerForSpaceInst extends AbstractCmisObjectsTreeWalker {

  protected TreeWalkerForSpaceInst() {
  }

  @Override
  protected SpaceInstLight getSilverpeasObjectById(final String objectId) {
    return getController().getSpaceInstLightById(objectId);
  }

  @Override
  protected Space createCmisObject(final Object space, final String language) {
    return getObjectFactory().createSpace((SpaceInstLight) space, language);
  }

  @Override
  protected boolean isSupported(final String objectId) {
    return Space.isSpace(objectId);
  }

  @Override
  protected <T extends AbstractI18NBean & Identifiable> Stream<T> getAllowedChildrenOfSilverpeasObject(
      final String parentId, final User user) {
    String[] childrenIds;
    if (StringUtil.isNotDefined(parentId) || parentId.equals(Space.ROOT_ID)) {
      childrenIds = getController().getAllRootSpaceIds(user.getId());
    } else {
      childrenIds = getAllowedChildrenOfSpace(parentId, user);
    }
    return Stream.of(childrenIds)
        .map(id -> {
          AbstractCmisObjectsTreeWalker walker = AbstractCmisObjectsTreeWalker.selectInstance(id);
          return walker.getSilverpeasObjectById(id);
        });
  }

  @Override
  public ObjectData getObjectData(final String objectId, final Filtering filtering) {
    if (Space.ROOT_ID.equals(objectId)) {
      final CmisObject rootSpace = getObjectFactory().createRootSpace();
      return buildObjectData(rootSpace, filtering);
    } else {
      return super.getObjectData(objectId, filtering);
    }
  }

  @Override
  public ObjectData getObjectDataByPath(final String path, final Filtering filtering) {
    if (CmisFolder.PATH_SEPARATOR.equals(path)) {
      final CmisObject rootSpace = getObjectFactory().createRootSpace();
      return buildObjectData(rootSpace, filtering);
    } else {
      return super.getObjectDataByPath(path, filtering);
    }
  }

  @Override
  public List<ObjectParentData> getParentsData(final String objectId, final Filtering filtering) {
    if (Space.ROOT_ID.equals(objectId)) {
      return Collections.emptyList();
    } else {
      return super.getParentsData(objectId, filtering);
    }
  }

  @Override
  public ObjectInFolderList getChildrenData(final String folderId, final Filtering filtering,
      final Paging paging) {
    if (Space.ROOT_ID.equals(folderId)) {
      final User user = filtering.getCurrentUser();
      final String[] subSpaceIds = getController().getAllRootSpaceIds(user.getId());
      return buildObjectInFolderList(subSpaceIds, filtering, paging);
    } else {
      return super.getChildrenData(folderId, filtering, paging);
    }
  }

  @Override
  public List<ObjectInFolderContainer> getSubTreeData(final String folderId,
      final Filtering filtering, final long depth) {
    final List<ObjectInFolderContainer> tree;
    if (Space.ROOT_ID.equals(folderId)) {
      final User user = filtering.getCurrentUser();
      final String[] subSpaceIds = getController().getAllRootSpaceIds(user.getId());
      tree = browseObjectsInFolderSubTrees(subSpaceIds, filtering, depth);
    } else {
      tree = super.getSubTreeData(folderId, filtering, depth);
    }
    return tree;
  }

  @Override
  protected List<ObjectInFolderContainer> browseObjectsInFolderTree(final Identifiable object,
      final Filtering filtering, final long depth) {
    User user = filtering.getCurrentUser();
    String[] ids = getAllowedChildrenOfSpace(object.getId(), user);
    return browseObjectsInFolderSubTrees(ids, filtering, depth);
  }

  @NotNull
  private String[] getAllowedChildrenOfSpace(final String spaceId, final User user) {
    String[] subSpaceIds = getController().getAllowedSubSpaceIds(user.getId(), spaceId);
    String[] compInstIds = getController().getAvailCompoIds(spaceId, user.getId());
    // we browse first the subspaces and then the component instances that are supported by our CMIS
    // implementation
    return Stream.concat(Stream.of(subSpaceIds),
        Stream.of(compInstIds).filter(AbstractCmisObjectsTreeWalker::supports))
        .toArray(String[]::new);
  }

  @Override
  protected ObjectInFolderList browseObjectsInFolder(final Identifiable object,
      final Filtering filtering, final Paging paging) {
    User user = filtering.getCurrentUser();
    SpaceInstLight space = (SpaceInstLight) object;
    String[] ids = getAllowedChildrenOfSpace(space.getId(), user);
    return buildObjectInFolderList(ids, filtering, paging);
  }

  @Override
  protected List<ObjectParentData> browseParentsOfObject(final Identifiable object,
      final Filtering filtering) {
    final SpaceInstLight space = (SpaceInstLight) object;
    final String fatherId = space.getFatherId();
    final CmisFolder cmisChild = getObjectFactory().createSpace(space, filtering.getLanguage());
    final CmisFolder cmisParent;
    if (StringUtil.isDefined(fatherId)) {
      cmisParent = getObjectFactory().createRootSpace();
    } else {
      final SpaceInstLight father = getController().getSpaceInstLightById(fatherId);
      cmisParent = getObjectFactory().createSpace(father, filtering.getLanguage());
    }
    final ObjectParentData parentData = buildObjectParentData(cmisParent, cmisChild, filtering);
    return Collections.singletonList(parentData);
  }
}
  