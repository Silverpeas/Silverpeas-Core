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
import org.silverpeas.core.Identifiable;
import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.annotation.Service;
import org.silverpeas.core.cmis.model.CmisObjectFactory;
import org.silverpeas.core.cmis.model.Publication;
import org.silverpeas.core.contribution.model.ContributionIdentifier;
import org.silverpeas.core.contribution.publication.model.Location;
import org.silverpeas.core.contribution.publication.model.PublicationDetail;
import org.silverpeas.core.contribution.publication.model.PublicationPK;
import org.silverpeas.core.contribution.publication.service.PublicationService;
import org.silverpeas.core.i18n.AbstractI18NBean;
import org.silverpeas.core.node.model.NodeDetail;
import org.silverpeas.core.node.model.NodePK;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.math.BigInteger;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

/**
 * A {@link CmisObjectsTreeWalker} object that knows how to walk down the subtree rooted to a
 * publication in a given Silverpeas application. The publication is a localized contribution that
 * can have a content (form, rich text (WYSIWYG), ...) and that can have one or more documents
 * attached to it.
 * @author mmoquillon
 */
@Service
@Singleton
public class TreeWalkerForPublicationDetail extends AbstractCmisObjectsTreeWalker {

  @Inject
  private PublicationService publicationService;

  @Override
  protected PublicationDetail getSilverpeasObjectById(final String objectId) {
    return publicationService.getDetail(asPublicationPK(objectId));
  }

  @Override
  protected <T extends AbstractI18NBean & Identifiable> Stream<T> getAllowedChildrenOfSilverpeasObject(
      final String parentId, final User user) {
    return Stream.empty();
  }

  @Override
  protected Publication createCmisObject(final Object silverpeasObject, final String language) {
    PublicationDetail pub = (PublicationDetail) silverpeasObject;
    Location location = publicationService.getMainLocation(pub.getPK())
        .orElse(new Location(NodePK.ROOT_NODE_ID, pub.getInstanceId()));
    ContributionIdentifier folder = ContributionIdentifier.from(location, NodeDetail.TYPE);
    return CmisObjectFactory.getInstance().createPublication(pub, folder, language);
  }

  @Override
  protected boolean isSupported(final String objectId) {
    try {
      return ContributionIdentifier.decode(objectId)
          .getType()
          .equals(PublicationDetail.getResourceType());
    } catch (IllegalArgumentException e) {
      return false;
    }
  }

  @Override
  protected List<ObjectInFolderContainer> browseObjectsInFolderTree(final Identifiable object,
      final Filtering filtering, final long depth) {
    return Collections.emptyList();
  }

  @Override
  protected ObjectInFolderList browseObjectsInFolder(final Identifiable object,
      final Filtering filtering, final Paging paging) {
    ObjectInFolderListImpl folderList = new ObjectInFolderListImpl();
    folderList.setObjects(Collections.emptyList());
    folderList.setNumItems(BigInteger.ZERO);
    folderList.setHasMoreItems(false);
    return folderList;
  }

  @Override
  protected List<ObjectParentData> browseParentsOfObject(final Identifiable object,
      final Filtering filtering) {
    return Collections.emptyList();
  }

  private PublicationPK asPublicationPK(final String pubId) {
    ContributionIdentifier identifier = ContributionIdentifier.decode(pubId);
    return new PublicationPK(identifier.getLocalId(), identifier.getComponentInstanceId());
  }
}
  