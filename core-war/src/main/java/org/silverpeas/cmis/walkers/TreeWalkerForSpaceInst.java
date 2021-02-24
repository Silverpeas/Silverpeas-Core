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

import org.apache.chemistry.opencmis.commons.data.ContentStream;
import org.apache.chemistry.opencmis.commons.data.ObjectInFolderContainer;
import org.apache.chemistry.opencmis.commons.data.ObjectInFolderList;
import org.apache.chemistry.opencmis.commons.data.ObjectParentData;
import org.apache.chemistry.opencmis.commons.exceptions.CmisNotSupportedException;
import org.jetbrains.annotations.NotNull;
import org.silverpeas.cmis.Filtering;
import org.silverpeas.cmis.Paging;
import org.silverpeas.core.BasicIdentifier;
import org.silverpeas.core.ResourceIdentifier;
import org.silverpeas.core.admin.space.SpaceInstLight;
import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.annotation.Service;
import org.silverpeas.core.cmis.model.CmisFile;
import org.silverpeas.core.cmis.model.CmisFolder;
import org.silverpeas.core.cmis.model.CmisObject;
import org.silverpeas.core.cmis.model.Space;
import org.silverpeas.core.i18n.LocalizedResource;
import org.silverpeas.core.security.authorization.ComponentAccessControl;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * A {@link CmisObjectsTreeWalker} object that knows how to walk the subtree rooted to a
 * collaborative space in Silverpeas. It takes care of the virtual root node in the CMIS objects
 * tree that represents the virtual container of all of the root spaces in Silverpeas.
 *
 * @author mmoquillon
 */
@Service
@Singleton
public class TreeWalkerForSpaceInst extends AbstractCmisObjectsTreeWalker {

  @Inject
  private ComponentAccessControl componentAccessControl;

  protected TreeWalkerForSpaceInst() {
  }

  @Override
  @SuppressWarnings("unchecked")
  protected SpaceInstLight getSilverpeasObjectById(final String objectId) {
    return getController().getSpaceInstLightById(objectId);
  }

  @Override
  @SuppressWarnings("unchecked")
  protected Space createCmisObject(final LocalizedResource space, final String language) {
    return getObjectFactory().createSpace((SpaceInstLight) space, language);
  }

  @Override
  protected boolean isSupported(final String objectId) {
    return Space.isSpace(objectId);
  }

  @Override
  protected Stream<LocalizedResource> getAllowedChildrenOfSilverpeasObject(
      final ResourceIdentifier parentId, final User user) {
    Stream<LocalizedResource> children;
    if (Space.ROOT_ID == parentId) {
      children = Arrays.stream(getController().getAllRootSpaceIds(user.getId()))
          .map(this::getSilverpeasObjectById);
    } else {
      children = getAllowedChildrenOfSpace(parentId, user);
    }
    return children;
  }

  @Override
  public CmisObject getObjectData(final String objectId, final Filtering filtering) {
    if (Space.ROOT_ID.asString().equals(objectId)) {
      final CmisObject rootSpace = getObjectFactory().createRootSpace();
      setObjectDataFields(rootSpace, filtering);
      return rootSpace;
    } else {
      return super.getObjectData(objectId, filtering);
    }
  }

  @Override
  public CmisFile getObjectDataByPath(final String path, final Filtering filtering) {
    if (CmisFile.PATH_SEPARATOR.equals(path)) {
      final CmisFile rootSpace = getObjectFactory().createRootSpace();
      setObjectDataFields(rootSpace, filtering);
      return rootSpace;
    } else {
      return super.getObjectDataByPath(path, filtering);
    }
  }

  @Override
  public List<ObjectParentData> getParentsData(final String objectId, final Filtering filtering) {
    if (Space.ROOT_ID.asString().equals(objectId)) {
      return Collections.emptyList();
    } else {
      return super.getParentsData(objectId, filtering);
    }
  }

  @Override
  public ObjectInFolderList getChildrenData(final String folderId, final Filtering filtering,
      final Paging paging) {
    if (Space.ROOT_ID.asString().equals(folderId)) {
      final User user = filtering.getCurrentUser();
      final List<LocalizedResource> rootSpaces = getAllowedRootSpaces(user);
      return buildObjectInFolderList(rootSpaces, filtering, paging);
    } else {
      return super.getChildrenData(folderId, filtering, paging);
    }
  }

  @Override
  public List<ObjectInFolderContainer> getSubTreeData(final String folderId,
      final Filtering filtering, final long depth) {
    final List<ObjectInFolderContainer> tree;
    if (Space.ROOT_ID.asString().equals(folderId)) {
      final User user = filtering.getCurrentUser();
      List<LocalizedResource> rootSpaces = getAllowedRootSpaces(user);
      tree = browseObjectsInFolderSubTrees(rootSpaces, filtering, depth);
    } else {
      tree = super.getSubTreeData(folderId, filtering, depth);
    }
    return tree;
  }

  @Override
  public ContentStream getContentStream(final String objectId, final String language,
      final long offset, final long length) {
    throw new CmisNotSupportedException(
        "The content stream isn't supported by collaborative spaces");
  }

  @Override
  protected List<ObjectInFolderContainer> browseObjectsInFolderTree(final LocalizedResource object,
      final Filtering filtering, final long depth) {
    User user = filtering.getCurrentUser();
    List<LocalizedResource> children =
        getAllowedChildrenOfSpace(object.getIdentifier(), user).collect(Collectors.toList());
    return browseObjectsInFolderSubTrees(children, filtering, depth);
  }

  @NotNull
  private List<LocalizedResource> getAllowedRootSpaces(final User user) {
    return Stream.of(getController().getAllRootSpaceIds(user.getId()))
        .map(this::getSilverpeasObjectById)
        .collect(Collectors.toList());
  }

  @NotNull
  private Stream<LocalizedResource> getAllowedChildrenOfSpace(final ResourceIdentifier spaceId,
      final User user) {
    String[] allowedSubSpaceIds =
        getController().getAllowedSubSpaceIds(user.getId(), spaceId.asString());
    String[] compInstIds = getController().getAllComponentIds(spaceId.asString());
    Stream<String> allowedCompInstIds =
        componentAccessControl.filterAuthorizedByUser(List.of(compInstIds), user.getId());

    // we browse first the subspaces and then the component instances that are supported by our CMIS
    // implementation
    return Stream.concat(Stream.of(allowedSubSpaceIds), allowedCompInstIds)
        .map(BasicIdentifier::new)
        .map(id -> {
          AbstractCmisObjectsTreeWalker walker =
              AbstractCmisObjectsTreeWalker.selectInstance(id.asString());
          return walker.getSilverpeasObjectById(id.asString());
        });
  }

  @Override
  protected ObjectInFolderList browseObjectsInFolder(final LocalizedResource object,
      final Filtering filtering, final Paging paging) {
    User user = filtering.getCurrentUser();
    List<LocalizedResource> ids =
        getAllowedChildrenOfSpace(object.getIdentifier(), user).collect(Collectors.toList());
    return buildObjectInFolderList(ids, filtering, paging);
  }

  @Override
  protected List<ObjectParentData> browseParentsOfObject(final LocalizedResource object,
      final Filtering filtering) {
    final SpaceInstLight space = (SpaceInstLight) object;
    final String rootSpaceId = String.valueOf(Space.ROOT_ID.asLocalId());
    final String fatherId = space.getFatherId();
    final CmisFolder cmisChild = getObjectFactory().createSpace(space, filtering.getLanguage());
    final CmisFolder cmisParent;
    if (rootSpaceId.equals(fatherId)) {
      cmisParent = getObjectFactory().createRootSpace();
    } else {
      final SpaceInstLight father = getController().getSpaceInstLightById(fatherId);
      cmisParent = createCmisObject(father, filtering.getLanguage());
    }
    final ObjectParentData parentData = buildObjectParentData(cmisParent, cmisChild, filtering);
    return Collections.singletonList(parentData);
  }
}
  