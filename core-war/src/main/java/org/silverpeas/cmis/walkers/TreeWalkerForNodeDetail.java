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
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package org.silverpeas.cmis.walkers;

import org.apache.chemistry.opencmis.commons.data.ContentStream;
import org.apache.chemistry.opencmis.commons.data.ObjectInFolderContainer;
import org.apache.chemistry.opencmis.commons.data.ObjectInFolderList;
import org.apache.chemistry.opencmis.commons.data.ObjectParentData;
import org.apache.chemistry.opencmis.commons.exceptions.CmisPermissionDeniedException;
import org.silverpeas.cmis.Filtering;
import org.silverpeas.cmis.Paging;
import org.silverpeas.cmis.util.CmisProperties;
import org.silverpeas.core.ResourceIdentifier;
import org.silverpeas.core.SilverpeasRuntimeException;
import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.annotation.Service;
import org.silverpeas.core.cmis.CmisContributionsProvider;
import org.silverpeas.core.cmis.model.CmisFolder;
import org.silverpeas.core.cmis.model.ContributionFolder;
import org.silverpeas.core.cmis.model.TypeId;
import org.silverpeas.core.contribution.model.ContributionIdentifier;
import org.silverpeas.core.contribution.model.CoreContributionType;
import org.silverpeas.core.contribution.model.I18nContribution;
import org.silverpeas.core.i18n.LocalizedResource;
import org.silverpeas.core.node.model.NodeDetail;
import org.silverpeas.core.node.model.NodePK;

import javax.inject.Singleton;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * A {@link CmisObjectsTreeWalker} object that knows how to walk down the subtree rooted to a node
 * in a given Silverpeas application. The node is a technical representation in Silverpeas of a
 * folding container that can accept as elements one or more nodes and/or one or more other types of
 * user contribution. It is used to represent an album in a media gallery, a category in a blog or a
 * topic (folder) in an EDM.
 *
 * @author mmoquillon
 */
@Service
@Singleton
public class TreeWalkerForNodeDetail extends AbstractCmisObjectsTreeWalker {

  @Override
  public void prepareChildDataCreation(final LocalizedResource folder,
      final CmisProperties properties, final ContentStream contentStream, final String language) {
    properties.setParentObjectId(folder.getIdentifier().asString());
    TypeId typeId = properties.getObjectTypeId();
    if (typeId == TypeId.SILVERPEAS_FOLDER) {
      typeId = TypeId.SILVERPEAS_PUBLICATION;
      properties.setObjectTypeId(typeId);
    }
  }

  @Override
  @SuppressWarnings("unchecked")
  protected I18nContribution getSilverpeasObjectById(final String objectId) {
    ContributionIdentifier id = ContributionIdentifier.decode(objectId);
    User user = User.getCurrentRequester();
    return Optional.of(CmisContributionsProvider.getByAppId(id.getComponentInstanceId())
        .getContribution(id, user))
        .filter(isNotBinNeitherUnclassified)
        .orElseThrow(() -> new CmisPermissionDeniedException("Forbidden access!"));
  }

  @Override
  protected Stream<LocalizedResource> getAllowedChildrenOfSilverpeasObject(
      final ResourceIdentifier parentId, final User user) {
    ContributionIdentifier nodeId = ContributionIdentifier.from(parentId);
    return getAllowedChildrenOfNode(nodeId, user);
  }

  @Override
  @SuppressWarnings("unchecked")
  protected ContributionFolder encodeToCmisObject(final LocalizedResource resource,
      final String language) {
    return getObjectFactory().createContributionFolder((NodeDetail) resource, language);
  }

  @Override
  protected boolean isObjectSupported(final String objectId) {
    try {
      String type =  ContributionIdentifier.decode(objectId).getType();
      if (type.equals(NodeDetail.TYPE)) {
        return true;
      } else if (type.equals(CoreContributionType.UNKNOWN.name())) {
        return getSilverpeasObjectById(objectId) != null;
      } else {
        return false;
      }
    } catch (IllegalArgumentException|SilverpeasRuntimeException e) {
      return false;
    }
  }

  @Override
  protected boolean isTypeSupported(final TypeId typeId) {
    return typeId == TypeId.SILVERPEAS_FOLDER;
  }

  @Override
  protected List<ObjectInFolderContainer> browseObjectsInFolderTree(final LocalizedResource parent,
      final Filtering filtering, final long depth) {
    User user = filtering.getCurrentUser();
    List<LocalizedResource> children =
        getAllowedChildrenOfNode(parent.getIdentifier(), user).collect(Collectors.toList());
    return browseObjectsInFolderSubTrees(children, filtering, depth);
  }

  @Override
  protected ObjectInFolderList browseObjectsInFolder(final LocalizedResource object,
      final Filtering filtering, final Paging paging) {
    User user = filtering.getCurrentUser();
    List<LocalizedResource> children =
        getAllowedChildrenOfNode(object.getIdentifier(), user).collect(Collectors.toList());
    return buildObjectInFolderList(children, filtering, paging);
  }

  @Override
  protected List<ObjectParentData> browseParentsOfObject(final LocalizedResource object,
      final Filtering filtering) {
    NodeDetail node = (NodeDetail) object;
    String language = filtering.getLanguage();
    // root folder is the node representation of the application
    String fatherId = node.getFatherPK().isRoot() ? node.getIdentifier().getComponentInstanceId() :
        asFolderId(node.getFatherPK());
    AbstractCmisObjectsTreeWalker walker = getTreeWalkerSelector().selectByObjectIdOrFail(fatherId);
    LocalizedResource parent = walker.getSilverpeasObjectById(fatherId);
    final CmisFolder cmisParent = walker.encodeToCmisObject(parent, language);
    final CmisFolder cmisObject = getObjectFactory().createContributionFolder(node, language);
    final ObjectParentData parentData = buildObjectParentData(cmisParent, cmisObject, filtering);
    return Collections.singletonList(parentData);
  }

  private Stream<LocalizedResource> getAllowedChildrenOfNode(final ContributionIdentifier nodeId,
      final User user) {
    CmisContributionsProvider provider =
        CmisContributionsProvider.getByAppId(nodeId.getComponentInstanceId());
    return provider.getAllowedContributionsInFolder(nodeId, user)
        .stream()
        .filter(isNotBinNeitherUnclassified)
        .map(LocalizedResource.class::cast);
  }

  private String asFolderId(final NodePK nodePK) {
    if (nodePK.isUndefined()) {
      throw new IllegalArgumentException(
          "Contribution identifier conversion error: a folder cannot be undefined");
    }
    ContributionIdentifier identifier = ContributionIdentifier.from(nodePK, NodeDetail.TYPE);
    return identifier.asString();
  }

  private final Predicate<LocalizedResource> isNotBinNeitherUnclassified = r -> {
    ContributionIdentifier id = r.getIdentifier();
    return !id.getType().equals(NodeDetail.TYPE) ||
        (!id.getLocalId().equals(NodePK.UNCLASSED_NODE_ID) &&
            !id.getLocalId().equals(NodePK.BIN_NODE_ID));
  };

}
  