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
import org.silverpeas.cmis.Filtering;
import org.silverpeas.cmis.Paging;
import org.silverpeas.core.ResourceIdentifier;
import org.silverpeas.core.admin.component.model.ComponentInstLight;
import org.silverpeas.core.admin.space.SpaceInstLight;
import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.annotation.Service;
import org.silverpeas.core.cmis.CmisContributionsProvider;
import org.silverpeas.core.cmis.model.Application;
import org.silverpeas.core.cmis.model.CmisFolder;
import org.silverpeas.core.i18n.LocalizedResource;

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
  public ContentStream getContentStream(final String objectId, final String language,
      final long offset, final long length) {
    throw new CmisNotSupportedException("The content stream isn't supported by applications");
  }

  @Override
  @SuppressWarnings("unchecked")
  protected ComponentInstLight getSilverpeasObjectById(final String objectId) {
    return getController().getComponentInstLight(objectId);
  }

  @Override
  @SuppressWarnings("unchecked")
  protected Application createCmisObject(final LocalizedResource resource, final String language) {
    return getObjectFactory().createApplication((ComponentInstLight) resource, language);
  }

  @Override
  protected boolean isSupported(final String objectId) {
    try {
      CmisContributionsProvider provider = CmisContributionsProvider.getById(objectId);
      return provider != null;
    } catch (IllegalStateException e) {
      return false;
    }
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
        CmisContributionsProvider.getById(appId.asString());
    List<? extends LocalizedResource> contributions =
        contributionsProvider.getAllowedRootContributions(appId, user);
    return (List<LocalizedResource>) contributions;
  }
}
  