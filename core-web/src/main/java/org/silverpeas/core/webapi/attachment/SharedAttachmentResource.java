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
package org.silverpeas.core.webapi.attachment;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.silverpeas.core.annotation.WebService;
import org.silverpeas.core.contribution.attachment.AttachmentServiceProvider;
import org.silverpeas.core.contribution.attachment.model.SimpleDocument;
import org.silverpeas.core.contribution.attachment.model.SimpleDocumentPK;
import org.silverpeas.core.sharing.model.Ticket;
import org.silverpeas.core.sharing.security.AccessControlContext;
import org.silverpeas.core.sharing.services.SharingServiceProvider;
import org.silverpeas.core.util.MimeTypes;
import org.silverpeas.core.util.ZipUtil;
import org.silverpeas.core.util.file.FileRepositoryManager;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriBuilderException;
import java.io.*;
import java.net.URI;
import java.util.StringTokenizer;
import java.util.UUID;

/**
 * A REST Web resource providing access to attachments through sharing mode.
 */
@WebService
@Path(SharedAttachmentResource.PATH + "/{componentId}/{token}")
public class SharedAttachmentResource extends AbstractAttachmentResource {

  static final String PATH = "sharing/attachments";

  @PathParam("token")
  private String token;

  @Override
  protected String getResourceBasePath() {
    return PATH;
  }

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
      try(OutputStream out =
              FileUtils.openOutputStream(FileUtils.getFile(folderToZip, attachment.getFilename()))) {
        AttachmentServiceProvider.getAttachmentService()
            .getBinaryContent(out, attachment.getPk(), null);
      } catch (IOException e) {
        throw new WebApplicationException(Status.INTERNAL_SERVER_ERROR);
      }
    }
    try {
      File zipFile = FileUtils.getFile(folderToZip.getPath() + ".zip");
      URI downloadUri = getUri().getWebResourcePathBuilder()
          .path(getToken())
          .path("zipcontent")
          .path(zipFile.getName())
          .build();
      long size = ZipUtil.compressPathToZip(folderToZip, zipFile);
      return new ZipEntity(getUri().getRequestUri(), downloadUri.toString(), size);
    } catch (IllegalArgumentException | UriBuilderException | IOException e) {
      throw new WebApplicationException(Status.INTERNAL_SERVER_ERROR);
    }   finally {
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
      try(ByteArrayOutputStream output = new ByteArrayOutputStream();
          InputStream in = new FileInputStream(realFile)) {
        output.write(in);
        return Response.ok().entity(output.toByteArray()).type(MimeTypes.SHORT_ARCHIVE_MIME_TYPE).
            build();
      } catch (IOException e) {
        throw new WebApplicationException(Status.INTERNAL_SERVER_ERROR);
      }
    }
  }


  @Override
  protected boolean isFileReadable(SimpleDocument attachment) {
    Ticket ticket = SharingServiceProvider.getSharingTicketService().getTicket(getToken());
    var ctx = AccessControlContext.about(attachment);
    return ticket != null && ticket.getAccessControl().isReadable(ctx);
  }

  protected String getToken() {
    return token;
  }

}
