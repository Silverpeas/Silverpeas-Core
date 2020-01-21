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
import org.apache.chemistry.opencmis.commons.impl.dataobjects.ObjectInFolderListImpl;
import org.silverpeas.cmis.Filtering;
import org.silverpeas.cmis.Paging;
import org.silverpeas.core.cmis.model.Application;
import org.silverpeas.core.cmis.model.CmisFolder;
import org.silverpeas.core.Identifiable;
import org.silverpeas.core.admin.component.model.ComponentInstLight;
import org.silverpeas.core.admin.space.SpaceInstLight;
import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.annotation.Service;
import org.silverpeas.core.i18n.AbstractI18NBean;

import javax.inject.Singleton;
import java.math.BigInteger;
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
public class TreeWalkerForComponentInst extends CmisObjectsTreeWalker {

  protected TreeWalkerForComponentInst() {
  }

  @Override
  public ComponentInstLight getSilverpeasObjectById(final String objectId) {
    return getController().getComponentInstLight(objectId);
  }

  @Override
  public Application createCmisObject(final Object silverpeasObject, final String language) {
    return getObjectFactory().createApplication((ComponentInstLight) silverpeasObject, language);
  }

  @Override
  protected <T extends AbstractI18NBean & Identifiable> Stream<T> getAllowedChildrenOfSilverpeasObject(
      final String parentId, final User user) {
    return Stream.empty();
  }

  @Override
  protected List<ObjectInFolderContainer> browseObjectsInFolderTree(final Identifiable object,
      final Filtering filter, final long depth) {
    return Collections.emptyList();
  }

  @Override
  protected ObjectInFolderList browseObjectsInFolder(final Identifiable object,
      final Filtering filter, final Paging paging) {
    final ObjectInFolderListImpl childrenInList = new ObjectInFolderListImpl();
    childrenInList.setHasMoreItems(false);
    childrenInList.setNumItems(BigInteger.ZERO);
    childrenInList.setObjects(Collections.emptyList());
    return childrenInList;
  }

  @Override
  protected List<ObjectParentData> browseParentsOfObject(final Identifiable object,
      final Filtering filtering) {
    final ComponentInstLight compInst = (ComponentInstLight) object;
    final String spaceId = compInst.getSpaceId();
    final SpaceInstLight spaceInst = getController().getSpaceInstLightById(spaceId);
    final CmisFolder cmisChild =
        getObjectFactory().createApplication(compInst, filtering.getLanguage());
    final CmisFolder cmisParent = getObjectFactory().createSpace(spaceInst, filtering.getLanguage());
    final ObjectParentData parentData = buildObjectParentData(cmisParent, cmisChild, filtering);
    return Collections.singletonList(parentData);
  }
}
  