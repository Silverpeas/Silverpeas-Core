/**
 * Copyright (C) 2000 - 2011 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU Affero General Public License as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of the GPL, you may
 * redistribute this Program in connection with Free/Libre Open Source Software ("FLOSS")
 * applications as described in Silverpeas's FLOSS exception. You should have received a copy of the
 * text describing the FLOSS exception, and it is also available here:
 * "http://repository.silverpeas.com/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License along with this program.
 * If not, see <http://www.gnu.org/licenses/>.
 */
package org.silverpeas.attachment.web;

import java.io.IOException;
import java.io.OutputStream;

import com.silverpeas.annotation.Authorized;
import com.silverpeas.annotation.RequestScoped;
import com.silverpeas.annotation.Service;
import com.silverpeas.web.RESTWebService;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;
import org.silverpeas.attachment.AttachmentServiceFactory;
import org.silverpeas.attachment.model.SimpleDocument;
import org.silverpeas.attachment.model.SimpleDocumentPK;

@Service
@RequestScoped
@Path("documents/{componentId}")
@Authorized
public class DocumentRessource extends RESTWebService {

  @PathParam("componentId")
  private String componentId;

  @Override
  public String getComponentId() {
    return componentId;
  }

  /*@GET
  @Path("{id}/{lang}")
  @Produces(MediaType.APPLICATION_JSON)
  public Response getFileContent(final @PathParam("id") String attachmentId) {
    SimpleDocument attachment = AttachmentServiceFactory.getAttachmentService().
        searchAttachmentById(new SimpleDocumentPK(attachmentId), null);
    if (!isFileReadable(attachment)) {
      throw new WebApplicationException(Status.UNAUTHORIZED);
    }
    StreamingOutput data = new StreamingOutput() {
      @Override
      public void write(OutputStream output) throws IOException, WebApplicationException {
        try {
          AttachmentServiceFactory.getAttachmentService().getBinaryContent(output,
              new SimpleDocumentPK(attachmentId), null);
        } catch (Exception e) {
          throw new WebApplicationException(Status.INTERNAL_SERVER_ERROR);
        }
      }
    };
    return Response.ok().entity(data).type(attachment.getContentType()).build();
  }*/

  @GET
  @Path("file/{id}/{lang}")
  @Produces(MediaType.APPLICATION_OCTET_STREAM)
  public Response documentFile(@PathParam("id") final String documentId,
      @PathParam("lang") final String language) {
    SimpleDocument document = AttachmentServiceFactory.getAttachmentService().
        searchAttachmentById(new SimpleDocumentPK(documentId), language);
    StreamingOutput stream = new StreamingOutput() {
      @Override
      public void write(OutputStream output) throws IOException, WebApplicationException {
        try {
          AttachmentServiceFactory.getAttachmentService().getBinaryContent(output,
              new SimpleDocumentPK(documentId), language);
        } catch (Exception e) {
          throw new WebApplicationException(e);
        }
      }
    };

    return Response.ok(stream).type(document.getContentType()).header(HttpHeaders.CONTENT_LENGTH, document.
        getSize()).header("content-disposition", "attachment;filename=" + document.getFilename()).
        build();
  }
}
