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
import org.silverpeas.core.admin.component.model.ComponentInstLight;
import org.silverpeas.core.admin.space.SpaceInstLight;
import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.annotation.Service;
import org.silverpeas.core.cmis.CmisContributionsProvider;
import org.silverpeas.core.cmis.model.Application;
import org.silverpeas.core.cmis.model.CmisFolder;
import org.silverpeas.core.contribution.model.ContributionIdentifier;
import org.silverpeas.core.i18n.AbstractI18NBean;

import javax.inject.Singleton;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

/**
 * A {@link CmisObjectsTreeWalker} object that knows how to walk the subtree rooted to an
 * application instance in Silverpeas.
 * @author mmoquillon
 */
@Service
@Singleton
public class TreeWalkerForComponentInst extends AbstractCmisObjectsTreeWalker {

  protected TreeWalkerForComponentInst() {
  }

  @Override
  protected ComponentInstLight getSilverpeasObjectById(final String objectId) {
    return getController().getComponentInstLight(objectId);
  }

  @Override
  protected Application createCmisObject(final Object silverpeasObject, final String language) {
    return getObjectFactory().createApplication((ComponentInstLight) silverpeasObject, language);
  }

  @Override
  protected boolean isSupported(final String objectId) {
    try {
      CmisContributionsProvider provider = getContributionsProvider(objectId);
      return provider != null;
    } catch (IllegalStateException e) {
      return false;
    }
  }

  @Override
  protected <T extends AbstractI18NBean & Identifiable> Stream<T> getAllowedChildrenOfSilverpeasObject(
      final String parentId, final User user) {
    return getAllowedRootContributionsIds(parentId, user)
        .map(id -> {
          AbstractCmisObjectsTreeWalker walker = AbstractCmisObjectsTreeWalker.selectInstance(id);
          return walker.getSilverpeasObjectById(id);
        });
  }

  @Override
  protected List<ObjectInFolderContainer> browseObjectsInFolderTree(final Identifiable object,
      final Filtering filtering, final long depth) {
    String[] ids = getAllowedRootContributionsIds(object.getId(), filtering.getCurrentUser())
        .toArray(String[]::new);
    return browseObjectsInFolderSubTrees(ids, filtering, depth);
  }

  @Override
  protected ObjectInFolderList browseObjectsInFolder(final Identifiable object,
      final Filtering filtering, final Paging paging) {
    String[] ids = getAllowedRootContributionsIds(object.getId(), filtering.getCurrentUser())
        .toArray(String[]::new);
    return buildObjectInFolderList(ids, filtering, paging);
  }

  @Override
  protected List<ObjectParentData> browseParentsOfObject(final Identifiable object,
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

  private Stream<String> getAllowedRootContributionsIds(final String appId, final User user) {
    CmisContributionsProvider contributionsProvider = getContributionsProvider(appId);
    return contributionsProvider.getAllowedRootContributions(appId, user)
        .stream()
        .map(ContributionIdentifier::asString);
  }
}
  