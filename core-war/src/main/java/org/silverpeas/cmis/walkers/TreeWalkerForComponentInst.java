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
import org.silverpeas.cmis.Filtering;
import org.silverpeas.cmis.Paging;
import org.silverpeas.cmis.util.CmisProperties;
import org.silverpeas.core.ResourceIdentifier;
import org.silverpeas.core.admin.component.model.ComponentInstLight;
import org.silverpeas.core.admin.space.SpaceInstLight;
import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.annotation.Service;
import org.silverpeas.core.cmis.CmisContributionsProvider;
import org.silverpeas.core.cmis.model.Application;
import org.silverpeas.core.cmis.model.CmisFolder;
import org.silverpeas.core.cmis.model.TypeId;
import org.silverpeas.core.contribution.model.ContributionIdentifier;
import org.silverpeas.core.i18n.LocalizedResource;
import org.silverpeas.core.node.model.NodeDetail;
import org.silverpeas.core.node.model.NodePK;

import javax.inject.Singleton;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

/**
 * A {@link CmisObjectsTreeWalker} object that knows how to walk the subtree rooted to an
 * application instance in Silverpeas.
 *
 * @author mmoquillon
 */
@Service
@Singleton
public class TreeWalkerForComponentInst extends AbstractCmisObjectsTreeWalker {

  protected TreeWalkerForComponentInst() {
  }

  @Override
  protected void prepareChildDataCreation(final LocalizedResource app,
      final CmisProperties properties, final ContentStream contentStream, final String language) {
    String parentId = ContributionIdentifier.from(app.getIdentifier().asString(),
        NodePK.ROOT_NODE_ID, NodeDetail.TYPE).asString();
    AbstractCmisObjectsTreeWalker walker = getTreeWalkerSelector().selectByObjectIdOrFail(parentId);
    walker.prepareChildDataCreation(walker.getSilverpeasObjectById(parentId), properties,
        contentStream, language);
  }

  @Override
  @SuppressWarnings("unchecked")
  protected ComponentInstLight getSilverpeasObjectById(final String objectId) {
    return getController().getComponentInstLight(objectId);
  }

  @Override
  @SuppressWarnings("unchecked")
  protected Application encodeToCmisObject(final LocalizedResource resource,
      final String language) {
    return getObjectFactory().createApplication((ComponentInstLight) resource, language);
  }

  @Override
  protected boolean isObjectSupported(final String objectId) {
    try {
      CmisContributionsProvider provider = CmisContributionsProvider.getByAppId(objectId);
      return provider != null;
    } catch (IllegalStateException e) {
      return false;
    }
  }

  @Override
  protected boolean isTypeSupported(final TypeId typeId) {
    return typeId == TypeId.SILVERPEAS_APPLICATION;
  }

  @Override
  protected Stream<LocalizedResource> getAllowedChildrenOfSilverpeasObject(
      final ResourceIdentifier parentId, final User user) {
    return getAllowedRootContributions(parentId, user).stream();
  }

  @Override
  protected List<ObjectInFolderContainer> browseObjectsInFolderTree(final LocalizedResource object,
      final Filtering filtering, final long depth) {
    List<LocalizedResource> contributions =
        getAllowedRootContributions(object.getIdentifier(), filtering.getCurrentUser());
    return browseObjectsInFolderSubTrees(contributions, filtering, depth);
  }

  @Override
  protected ObjectInFolderList browseObjectsInFolder(final LocalizedResource object,
      final Filtering filtering, final Paging paging) {
    List<LocalizedResource> contributions =
        getAllowedRootContributions(object.getIdentifier(), filtering.getCurrentUser());
    return buildObjectInFolderList(contributions, filtering, paging);
  }

  @Override
  protected List<ObjectParentData> browseParentsOfObject(final LocalizedResource object,
      final Filtering filtering) {
    final ComponentInstLight compInst = (ComponentInstLight) object;
    final String spaceId = compInst.getSpaceId();
    final SpaceInstLight spaceInst = getController().getSpaceInstLightById(spaceId);
    final CmisFolder cmisChild =
        getObjectFactory().createApplication(compInst, filtering.getLanguage());
    final CmisFolder cmisParent =
        getObjectFactory().createSpace(spaceInst, filtering.getLanguage());
    final ObjectParentData parentData = buildObjectParentData(cmisParent, cmisChild, filtering);
    return Collections.singletonList(parentData);
  }

  @SuppressWarnings("unchecked")
  private List<LocalizedResource> getAllowedRootContributions(final ResourceIdentifier appId,
      final User user) {
    CmisContributionsProvider contributionsProvider =
        CmisContributionsProvider.getByAppId(appId.asString());
    List<? extends LocalizedResource> contributions =
        contributionsProvider.getAllowedRootContributions(appId, user);
    return (List<LocalizedResource>) contributions;
  }
}
  