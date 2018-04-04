/*
 * Copyright (C) 2000 - 2018 Silverpeas
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
package org.silverpeas.core.webapi.attachment;

import org.silverpeas.core.ResourceReference;
import org.silverpeas.core.annotation.RequestScoped;
import org.silverpeas.core.annotation.Service;
import org.silverpeas.core.contribution.attachment.AttachmentServiceProvider;
import org.silverpeas.core.contribution.attachment.model.DocumentType;
import org.silverpeas.core.contribution.attachment.model.SimpleDocument;
import org.silverpeas.core.webapi.base.annotation.Authorized;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
@RequestScoped
@Path(AbstractSimpleDocumentResource.PATH + "/{componentId}/resource/{id}")
@Authorized
public class SimpleDocumentListResource extends AbstractSimpleDocumentResource {

  @PathParam("id")
  private String resourceId;

  public String getResourceId() {
    return resourceId;
  }

  /**
   * Returns documents of specified resource in the specified language
   *
   * @param lang the wanted language.
   * @return documents of specified resource
   */
  @GET
  @Path("{lang}")
  @Produces(MediaType.APPLICATION_JSON)
  public List<SimpleDocumentEntity> getDocuments(@PathParam("lang") final String lang) {
    List<SimpleDocument> docs =
        AttachmentServiceProvider.getAttachmentService().listDocumentsByForeignKeyAndType(
            getResourceReference(), DocumentType.attachment, lang);
    return asWebEntities(docs);
  }

  /**
   * Returns documents of specified resource in the specified language
   *
   * @param lang the wanted language.
   * @return documents of specified resource
   */
  @GET
  @Path("types/{type}/{lang}")
  @Produces(MediaType.APPLICATION_JSON)
  public List<SimpleDocumentEntity> getDocumentsByType(@PathParam("type") final String type,
      @PathParam("lang") final String lang) {
    DocumentType documentType = DocumentType.decode(type);
    if (documentType == null) {
      return Collections.emptyList();
    }
    List<SimpleDocument> docs =
        AttachmentServiceProvider.getAttachmentService().listDocumentsByForeignKeyAndType(
            getResourceReference(), documentType, lang);
    return asWebEntities(docs);
  }

  private ResourceReference getResourceReference() {
    return new ResourceReference(getResourceId(), getComponentId());
  }

  private List<SimpleDocumentEntity> asWebEntities(List<SimpleDocument> docs) {
    List<SimpleDocumentEntity> entities = new ArrayList<>();
    for (SimpleDocument doc : docs) {
      entities.add(SimpleDocumentEntity.fromAttachment(doc));
    }
    return entities;
  }

}