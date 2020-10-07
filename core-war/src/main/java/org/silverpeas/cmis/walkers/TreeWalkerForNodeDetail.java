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

import org.apache.chemistry.opencmis.commons.data.ObjectInFolderContainer;
import org.apache.chemistry.opencmis.commons.data.ObjectInFolderList;
import org.apache.chemistry.opencmis.commons.data.ObjectParentData;
import org.silverpeas.cmis.Filtering;
import org.silverpeas.cmis.Paging;
import org.silverpeas.core.Identifiable;
import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.annotation.Service;
import org.silverpeas.core.cmis.CmisContributionsProvider;
import org.silverpeas.core.cmis.model.CmisFolder;
import org.silverpeas.core.cmis.model.ContributionFolder;
import org.silverpeas.core.contribution.model.Contribution;
import org.silverpeas.core.contribution.model.ContributionIdentifier;
import org.silverpeas.core.i18n.AbstractI18NBean;
import org.silverpeas.core.node.model.NodeDetail;
import org.silverpeas.core.node.model.NodePK;
import org.silverpeas.core.node.service.NodeService;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Stream;

/**
 * A {@link CmisObjectsTreeWalker} object that knows how to walk down the subtree rooted to a
 * node in a given Silverpeas application. The node is a technical representation in Silverpeas of
 * a folding container that can accept as elements one or more nodes and/or one or more other types
 * of user contribution. It is used to represent an album in a media gallery, a category in a blog
 * or a topic (folder) in an EDM.
 * @author mmoquillon
 */
@Service
@Singleton
public class TreeWalkerForNodeDetail extends AbstractCmisObjectsTreeWalker {

  @Inject
  private NodeService nodeService;

  @Override
  protected NodeDetail getSilverpeasObjectById(final String objectId) {
    return nodeService.getDetail(asNodePk(objectId));
  }

  @Override
  protected <T extends AbstractI18NBean & Identifiable> Stream<T> getAllowedChildrenOfSilverpeasObject(
      final String parentId, final User user) {
    ContributionIdentifier nodeId = ContributionIdentifier.decode(parentId);
    return getAllowedChildrenOfNode(nodeId, user).map(c -> {
      AbstractCmisObjectsTreeWalker walker = AbstractCmisObjectsTreeWalker.selectInstance(c);
      return walker.getSilverpeasObjectById(c);
    });
  }

  @Override
  protected ContributionFolder createCmisObject(final Object silverpeasObject, final String language) {
    return getObjectFactory().createContributionFolder((NodeDetail) silverpeasObject, language);
  }

  @Override
  protected boolean isSupported(final String objectId) {
    try {
      return ContributionIdentifier.decode(objectId).getType().equals(NodeDetail.TYPE);
    } catch (IllegalArgumentException e) {
      return false;
    }
  }

  @Override
  protected List<ObjectInFolderContainer> browseObjectsInFolderTree(final Identifiable parent,
      final Filtering filtering, final long depth) {
    User user = filtering.getCurrentUser();
    Contribution node = (Contribution) parent;
    String[] childrenIds =
        getAllowedChildrenOfNode(node.getContributionId(), user).toArray(String[]::new);
    return browseObjectsInFolderSubTrees(childrenIds, filtering, depth);
  }

  @Override
  protected ObjectInFolderList browseObjectsInFolder(final Identifiable object,
      final Filtering filtering, final Paging paging) {
    User user = filtering.getCurrentUser();
    Contribution node = (Contribution) object;
    String[] childrenIds =
        getAllowedChildrenOfNode(node.getContributionId(), user).toArray(String[]::new);
    return buildObjectInFolderList(childrenIds, filtering, paging);
  }

  @Override
  protected List<ObjectParentData> browseParentsOfObject(final Identifiable object,
      final Filtering filtering) {
    NodeDetail node = (NodeDetail) object;
    String language = filtering.getLanguage();
    // root folder is the node representation of the application
    String fatherId = node.getFatherPK().isRoot() ?
        node.getContributionId().getComponentInstanceId() : asFolderId(node.getFatherPK());
    AbstractCmisObjectsTreeWalker walker = AbstractCmisObjectsTreeWalker.selectInstance(fatherId);
    Object parent = walker.getSilverpeasObjectById(fatherId);
    final CmisFolder cmisParent = walker.createCmisObject(parent, language);
    final CmisFolder cmisObject = getObjectFactory().createContributionFolder(node, language);
    final ObjectParentData parentData = buildObjectParentData(cmisParent, cmisObject, filtering);
    return Collections.singletonList(parentData);
  }

  private Stream<String> getAllowedChildrenOfNode(final ContributionIdentifier nodeId,
      final User user) {
    CmisContributionsProvider provider = getContributionsProvider(nodeId.getComponentInstanceId());
    return provider.getAllowedContributionsInFolder(nodeId, user)
        .stream()
        .filter(isNotBinNeitherUnclassified)
        .map(ContributionIdentifier::asString);
  }

  private NodePK asNodePk(final String nodeId) {
    ContributionIdentifier identifier = ContributionIdentifier.decode(nodeId);
    return new NodePK(identifier.getLocalId(), identifier.getComponentInstanceId());
  }

  private String asFolderId(final NodePK nodePK) {
    if (nodePK.isUndefined()) {
      throw new IllegalArgumentException(
          "Contribution identifier conversion error: a folder cannot be undefined");
    }
    ContributionIdentifier identifier = ContributionIdentifier.from(nodePK, NodeDetail.TYPE);
    return identifier.asString();
  }

  private Predicate<ContributionIdentifier> isNotBinNeitherUnclassified =
      c -> !c.getType().equals(NodeDetail.TYPE) ||
          (!c.getLocalId().equals(NodePK.UNCLASSED_NODE_ID) &&
          !c.getLocalId().equals(NodePK.BIN_NODE_ID));

}
  