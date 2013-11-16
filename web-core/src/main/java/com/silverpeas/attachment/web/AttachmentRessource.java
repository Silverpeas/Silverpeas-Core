/**
 * Copyright (C) 2000 - 2013 Silverpeas
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
package com.silverpeas.attachment.web;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.util.StringTokenizer;
import java.util.UUID;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.StreamingOutput;
import javax.ws.rs.core.UriBuilderException;

import org.silverpeas.attachment.AttachmentServiceFactory;
import org.silverpeas.attachment.model.SimpleDocument;
import org.silverpeas.attachment.model.SimpleDocumentPK;

import com.silverpeas.annotation.RequestScoped;
import com.silverpeas.annotation.Service;
import com.silverpeas.sharing.model.Ticket;
import com.silverpeas.sharing.security.ShareableAttachment;
import com.silverpeas.sharing.services.SharingServiceFactory;
import com.silverpeas.util.MimeTypes;
import com.silverpeas.util.ZipManager;
import com.silverpeas.web.RESTWebService;

import com.stratelia.webactiv.util.FileRepositoryManager;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.ByteArrayOutputStream;

@Service
@RequestScoped
@Path("attachments/{componentId}/{token}")
public class AttachmentRessource extends RESTWebService {

  @PathParam("componentId")
  private String componentId;
  @PathParam("token")
  private String token;

  @Override
  public String getComponentId() {
    return componentId;
  }

  @GET
  @Path("{id}/{name}")
  @Produces(MediaType.APPLICATION_OCTET_STREAM)
  public Response getFileContent(final @PathParam("id") String attachmentId) {
    final SimpleDocument attachment = AttachmentServiceFactory.getAttachmentService().
        searchDocumentById(new SimpleDocumentPK(attachmentId), null).getLastPublicVersion();
    if (!isFileReadable(attachment)) {
      throw new WebApplicationException(Status.UNAUTHORIZED);
    }
    StreamingOutput data = new StreamingOutput() {
      @Override
      public void write(OutputStream output) throws WebApplicationException {
        try {
          AttachmentServiceFactory.getAttachmentService().getBinaryContent(output, attachment.
              getPk(), attachment.getLanguage());
        } catch (Exception e) {
          throw new WebApplicationException(e, Status.INTERNAL_SERVER_ERROR);
        }
      }
    };
    return Response.ok().entity(data).type(attachment.getContentType()).build();
  }

  @GET
  @Path("{ids}/zip")
  @Produces(MediaType.APPLICATION_JSON)
  public ZipEntity zipFiles(@PathParam("ids") String attachmentIds) {
    StringTokenizer tokenizer = new StringTokenizer(attachmentIds, ",");
    File folderToZip = FileUtils.getFile(FileRepositoryManager.getTemporaryPath(), UUID.randomUUID()
        .toString());
    while (tokenizer.hasMoreTokens()) {
      SimpleDocumentPK pk = new SimpleDocumentPK(tokenizer.nextToken());
      SimpleDocument attachment = AttachmentServiceFactory.getAttachmentService().
          searchDocumentById(pk, null);
      if (!isFileReadable(attachment)) {
        throw new WebApplicationException(Status.UNAUTHORIZED);
      }
      OutputStream out = null;
      try {
        out = FileUtils.openOutputStream(FileUtils.getFile(folderToZip, attachment.getFilename()));
        AttachmentServiceFactory.getAttachmentService().getBinaryContent(out, pk, token);
      } catch (IOException e) {
        throw new WebApplicationException(Status.INTERNAL_SERVER_ERROR);
      } finally {
        IOUtils.closeQuietly(out);
      }
    }
    try {
      File zipFile = FileUtils.getFile(folderToZip.getPath() + ".zip");
      URI downloadUri = getUriInfo().getBaseUriBuilder().path("attachments").path(componentId).path(
          token).path("zipcontent").path(zipFile.getName()).build();
      long size = ZipManager.compressPathToZip(folderToZip, zipFile);
      return new ZipEntity(getUriInfo().getRequestUri(), downloadUri.toString(), size);
    } catch (IllegalArgumentException e) {
      throw new WebApplicationException(Status.INTERNAL_SERVER_ERROR);
    } catch (UriBuilderException e) {
      throw new WebApplicationException(Status.INTERNAL_SERVER_ERROR);
    } catch (IOException e) {
      throw new WebApplicationException(Status.INTERNAL_SERVER_ERROR);
    } finally {
      FileUtils.deleteQuietly(folderToZip);
    }
  }

  @GET
  @Path("zipcontent/{name}")
  @Produces(MediaType.APPLICATION_OCTET_STREAM)
  public Response getZipContent(@PathParam("name") String zipFileName) {
    String zipFilePath = FileRepositoryManager.getTemporaryPath() + zipFileName;
    File realFile = new File(zipFilePath);
    if (!realFile.exists() && !realFile.isFile()) {
      return Response.ok().build();
    } else {
      ByteArrayOutputStream output = new ByteArrayOutputStream();
      InputStream in = null;
      try {
        in = new FileInputStream(realFile);
        output.write(in);
        return Response.ok().entity(output.toByteArray()).type(MimeTypes.SHORT_ARCHIVE_MIME_TYPE).
            build();
      } catch (IOException e) {
        throw new WebApplicationException(Status.INTERNAL_SERVER_ERROR);
      } finally {
        IOUtils.closeQuietly(in);
        IOUtils.closeQuietly(output);
      }

    }
  }

  @SuppressWarnings("unchecked")
  private boolean isFileReadable(SimpleDocument attachment) {
    ShareableAttachment attachmentResource = new ShareableAttachment(token, attachment);
    Ticket ticket = SharingServiceFactory.getSharingTicketService().getTicket(token);
    return ticket != null && ticket.getAccessControl().isReadable(attachmentResource);
  }
}
