/*
 * Copyright (C) 2000 - 2016 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU Affero General Public License as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of the GPL, you may
 * redistribute this Program in connection with Free/Libre Open Source Software ("FLOSS")
 * applications as described in Silverpeas's FLOSS exception. You should have received a copy of the
 * text describing the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License along with this program.
 * If not, see <http://www.gnu.org/licenses/>.
 */
package org.silverpeas.core.webapi.attachment;

import org.silverpeas.core.ForeignPK;
import org.silverpeas.core.annotation.RequestScoped;
import org.silverpeas.core.annotation.Service;
import org.silverpeas.core.contribution.attachment.AttachmentServiceProvider;
import org.silverpeas.core.contribution.attachment.model.DocumentType;
import org.silverpeas.core.contribution.attachment.model.SimpleDocument;
import org.silverpeas.core.webapi.base.RESTWebService;
import org.silverpeas.core.webapi.base.annotation.Authorized;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.ArrayList;
import java.util.List;

@Service
@RequestScoped
@Path("documents/{componentId}/resource/{id}")
@Authorized
public class SimpleDocumentListResource extends RESTWebService {

  @PathParam("id")
  private String resourceId;

  @PathParam("componentId")
  private String componentId;

  public String getResourceId() {
    return resourceId;
  }

  @Override
  public String getComponentId() {
    return componentId;
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
            new ForeignPK(getResourceId(), getComponentId()), DocumentType.attachment, lang);
    List<SimpleDocumentEntity> entities = new ArrayList<>();
    for (SimpleDocument doc : docs) {
      entities.add(SimpleDocumentEntity.fromAttachment(doc));
    }
    return entities;
  }
}