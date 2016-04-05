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

import org.silverpeas.core.annotation.RequestScoped;
import org.silverpeas.core.annotation.Service;
import org.silverpeas.core.sharing.model.Ticket;
import org.silverpeas.core.sharing.security.ShareableAttachment;
import org.silverpeas.core.sharing.services.SharingServiceProvider;
import org.silverpeas.core.util.MimeTypes;
import org.silverpeas.core.util.ZipUtil;
import org.silverpeas.core.util.file.FileRepositoryManager;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.silverpeas.core.contribution.attachment.AttachmentServiceProvider;
import org.silverpeas.core.contribution.attachment.model.SimpleDocument;
import org.silverpeas.core.contribution.attachment.model.SimpleDocumentPK;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriBuilderException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.util.StringTokenizer;
import java.util.UUID;

/**
 * A REST Web resource providing access to attachments through sharing mode.
 */
@Service
@RequestScoped
@Path("sharing/attachments/{componentId}/{token}")
public class SharedAttachmentResource extends AbstractAttachmentResource {

  @PathParam("token")
  private String token;

  @GET
  @Path("{id}/{name}")
  @Produces(MediaType.APPLICATION_OCTET_STREAM)
  public Response getFileContent(final @PathParam("id") String attachmentId) {
    return super.getFileContent(attachmentId);
  }

  @GET
  @Path("{ids}/zip")
  @Produces(MediaType.APPLICATION_JSON)
  public ZipEntity zipFiles(@PathParam("ids") String attachmentIds) {
    StringTokenizer tokenizer = new StringTokenizer(attachmentIds, ",");
    File folderToZip = FileUtils.getFile(
        FileRepositoryManager.getTemporaryPath(), UUID.randomUUID().toString());
    while (tokenizer.hasMoreTokens()) {
      SimpleDocument attachment = AttachmentServiceProvider.getAttachmentService().
          searchDocumentById(new SimpleDocumentPK(tokenizer.nextToken()), null)
          .getLastPublicVersion();
      if (!isFileReadable(attachment)) {
        throw new WebApplicationException(Status.UNAUTHORIZED);
      }
      OutputStream out = null;
      try {
        out = FileUtils.openOutputStream(FileUtils.getFile(folderToZip, attachment.getFilename()));
        AttachmentServiceProvider.getAttachmentService()
            .getBinaryContent(out, attachment.getPk(), null);
      } catch (IOException e) {
        throw new WebApplicationException(Status.INTERNAL_SERVER_ERROR);
      } finally {
        IOUtils.closeQuietly(out);
      }
    }
    try {
      File zipFile = FileUtils.getFile(folderToZip.getPath() + ".zip");
      URI downloadUri =
          getUriInfo().getBaseUriBuilder().path("sharing").path("attachments").path(componentId)
              .path(getToken()).path("zipcontent").path(zipFile.getName()).build();
      long size = ZipUtil.compressPathToZip(folderToZip, zipFile);
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


  @Override
  @SuppressWarnings("unchecked")
  protected boolean isFileReadable(SimpleDocument attachment) {
    ShareableAttachment attachmentResource = new ShareableAttachment(getToken(), attachment);
    Ticket ticket = SharingServiceProvider.getSharingTicketService().getTicket(getToken());
    return ticket != null && ticket.getAccessControl().isReadable(attachmentResource);
  }

  protected String getToken() {
    return token;
  }

}
