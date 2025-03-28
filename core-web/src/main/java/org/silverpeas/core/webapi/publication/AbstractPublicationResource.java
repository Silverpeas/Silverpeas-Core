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
 * FLOSS exception.  You should have received a copy of the text describing
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
package org.silverpeas.core.webapi.publication;

import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.contribution.attachment.AttachmentService;
import org.silverpeas.core.contribution.attachment.AttachmentServiceProvider;
import org.silverpeas.core.contribution.attachment.model.DocumentType;
import org.silverpeas.core.contribution.attachment.model.SimpleDocument;
import org.silverpeas.core.contribution.publication.model.PublicationDetail;
import org.silverpeas.core.contribution.publication.service.PublicationService;
import org.silverpeas.core.node.model.NodePK;
import org.silverpeas.core.node.service.NodeService;
import org.silverpeas.core.security.authorization.AccessControlContext;
import org.silverpeas.core.security.authorization.AccessControlOperation;
import org.silverpeas.core.security.authorization.SimpleDocumentAccessControl;
import org.silverpeas.core.web.rs.RESTWebService;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response.Status;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * A REST Web resource providing access to publications.
 */
public abstract class AbstractPublicationResource extends RESTWebService {

  /**
   * Gets all the publications in the specified topic (a folder or a category) with or not their
   * attachments.
   *
   * @param nodeId The unique identifier of the node representing the topic.
   * @param includingAliases  are the publication aliases in the topic have to be also taken into
   * account.
   * @param withAttachments are the attachments of the publications have to be also got with them.
   * @return a list of the publications in the given topic. If no publications are located in the
   * specified topic, then an empty list is returned.
   */
  protected List<PublicationEntity> getPublications(String nodeId,
      boolean includingAliases, boolean withAttachments) {

    NodePK nodePK = new NodePK(nodeId, getComponentId());
    if (!isNodeReadable(nodePK)) {
      throw new WebApplicationException(Status.UNAUTHORIZED);
    }

    var publications = getPublicationService().getVisiblePublicationsIn(nodePK);
    List<PublicationEntity> entities = new ArrayList<>();
    for (PublicationDetail publication : publications) {
      if (includingAliases || !publication.isAlias()) {
        PublicationEntity entity = getPublicationEntity(publication, withAttachments);
        if (entity != null) {
          entities.add(entity);
        }
      }
    }
    return entities;
  }

  protected PublicationEntity getPublicationEntity(PublicationDetail publication,
      boolean withAttachments) {
    if (publication.isValid()) {
      URI uri = getPublicationUri(publication);
      PublicationEntity entity = PublicationEntity.fromPublicationDetail(publication, uri);
      if (withAttachments) {
        AttachmentService attachmentService = AttachmentServiceProvider.getAttachmentService();
        // expose regular files
        Collection<SimpleDocument> attachments =
            attachmentService.listDocumentsByForeignKey(publication.getPK().toResourceReference(),
                null);
        // and files attached to form too...
        attachments.addAll(attachmentService.listDocumentsByForeignKeyAndType(
            publication.getPK().toResourceReference(),
            DocumentType.form, null));
        // only the attachments that can be accessible by any readers (meaning that can be
        // downloaded in order to access the file content)
        List<SimpleDocument> accessibleAttachments = attachments.stream()
            .filter(this::isAttachmentAuthorized)
            .collect(Collectors.toList());
        entity.withAttachments(accessibleAttachments);
      }
      return entity;
    }
    return null;
  }

  protected abstract boolean isNodeReadable(NodePK nodePK);

  private URI getPublicationUri(PublicationDetail publication) {
    return getUri().getAbsolutePathBuilder()
        .path("publication")
        .path(publication.getPK().getId())
        .build();
  }

  protected NodeService getNodeService() {
    try {
      return NodeService.get();
    } catch (Exception e) {
      throw new WebApplicationException(Status.INTERNAL_SERVER_ERROR);
    }
  }

  protected PublicationService getPublicationService() {
    try {
      return PublicationService.get();
    } catch (Exception e) {
      throw new WebApplicationException(Status.INTERNAL_SERVER_ERROR);
    }
  }

  private boolean isAttachmentAuthorized(final SimpleDocument attachment) {
    final boolean authorized;
    if (isUserDefined()) {
      final User user = getUser();
      authorized = SimpleDocumentAccessControl.get().isUserAuthorized(user.getId(), attachment,
          AccessControlContext.init().onOperationsOf(AccessControlOperation.DOWNLOAD));
    } else {
      authorized = attachment.isDownloadAllowedForReaders();
    }
    return authorized;
  }
}