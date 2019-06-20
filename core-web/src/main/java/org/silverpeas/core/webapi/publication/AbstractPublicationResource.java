/*
 * Copyright (C) 2000 - 2019 Silverpeas
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
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.silverpeas.core.webapi.publication;

import org.silverpeas.core.contribution.attachment.AttachmentService;
import org.silverpeas.core.contribution.attachment.AttachmentServiceProvider;
import org.silverpeas.core.contribution.attachment.model.DocumentType;
import org.silverpeas.core.contribution.attachment.model.SimpleDocument;
import org.silverpeas.core.contribution.publication.model.PublicationDetail;
import org.silverpeas.core.contribution.publication.service.PublicationService;
import org.silverpeas.core.node.model.NodePK;
import org.silverpeas.core.node.service.NodeService;
import org.silverpeas.core.util.ServiceProvider;
import org.silverpeas.core.webapi.base.RESTWebService;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response.Status;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * A REST Web resource providing access to publications.
 */
public abstract class AbstractPublicationResource extends RESTWebService {

  /**
   * Gets the nodes that are children of a parent node.
   *
   * @param nodeId The ID of the parent node.
   * @param withAttachments Indicated whether attachments related to publications are required.
   * @return An array of the nodes whose parent is the node matching the specified ID.
   */
  protected List<PublicationEntity> getPublications(String nodeId, boolean withAttachments) {

    NodePK nodePK = new NodePK(nodeId, getComponentId());
    if (!isNodeReadable(nodePK)) {
      throw new WebApplicationException(Status.UNAUTHORIZED);
    }

    Collection<PublicationDetail> publications = getPublicationService().getDetailsByFatherPK(
        nodePK, null, true);

    List<PublicationEntity> entities = new ArrayList<>();
    for (PublicationDetail publication : publications) {
      PublicationEntity entity = getPublicationEntity(publication, withAttachments);
      if (entity != null) {
        entities.add(entity);
      }
    }
    return entities;
  }

  protected PublicationEntity getPublicationEntity(PublicationDetail publication, boolean withAttachments) {
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
        entity.withAttachments(attachments);
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
      return ServiceProvider.getService(PublicationService.class);
    } catch (Exception e) {
      throw new WebApplicationException(Status.INTERNAL_SERVER_ERROR);
    }
  }

}