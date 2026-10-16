/*
 * Copyright (C) 2000 - 2026 Silverpeas
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

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.silverpeas.core.annotation.WebService;
import org.silverpeas.core.contribution.attachment.AttachmentServiceProvider;
import org.silverpeas.core.contribution.attachment.model.SimpleDocument;
import org.silverpeas.core.contribution.attachment.model.SimpleDocumentPK;
import org.silverpeas.core.sharing.model.Ticket;
import org.silverpeas.core.webapi.sharing.SharingCodeVerifier;
import org.silverpeas.core.sharing.security.AccessControlContext;
import org.silverpeas.core.sharing.services.SharingServiceProvider;

import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;
import java.io.*;
import java.util.StringTokenizer;

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

  @Operation(summary = "Downloads the content of an attachment shared through the given link.")
  @ApiResponse(responseCode = "200", description = "The content of the shared attachment.")
  @GET
  @Path("{id}/{name}")
  @Produces(MediaType.APPLICATION_OCTET_STREAM)
  public Response getFileContent(final @PathParam("id") String attachmentId) {
    return getAttachmentContent(attachmentId);
  }

  @Operation(summary = "Gathers into a ZIP archive the attachments shared through the given link.")
  @ApiResponse(responseCode = "200", description = "The archive gathering the shared attachments.",
    content = @Content(mediaType = "application/zip"))
  @GET
  @Path("{ids}/zip")
  @Produces("application/zip")
  public Response zipFiles(@PathParam("ids") String attachmentIds) {
    java.util.List<SimpleDocument> attachments = new java.util.ArrayList<>();
    StringTokenizer tokenizer = new StringTokenizer(attachmentIds, ",");
    if (!tokenizer.hasMoreTokens()) {
      throw new BadRequestException("No attachments selected");
    }
    while (tokenizer.hasMoreTokens()) {
      SimpleDocument attachment = AttachmentServiceProvider.getAttachmentService()
          .searchDocumentById(new SimpleDocumentPK(tokenizer.nextToken()), null)
          .getLastPublicVersion();
      if (attachment == null || !isFileReadable(attachment)) {
        throw new WebApplicationException(Status.UNAUTHORIZED);
      }
      attachments.add(attachment);
    }
    jakarta.ws.rs.core.StreamingOutput stream = output -> {
      java.util.Set<String> names = new java.util.HashSet<>();
      try (java.util.zip.ZipOutputStream zip = new java.util.zip.ZipOutputStream(output)) {
        for (SimpleDocument attachment : attachments) {
          String name = new File(attachment.getFilename()).getName();
          if (!names.add(name)) {
            name = attachment.getPk().getId() + "-" + name;
          }
          zip.putNextEntry(new java.util.zip.ZipEntry(name));
          AttachmentServiceProvider.getAttachmentService()
              .getBinaryContent(zip, attachment.getPk(), null);
          zip.closeEntry();
        }
      }
    };
    return Response.ok(stream)
        .header("Content-Disposition", "attachment; filename=\"shared-files.zip\"")
        .build();
  }

  @Operation(summary = "Downloads the ZIP archive of the attachments shared through the given " +
      "link.")
  @ApiResponse(responseCode = "200", description = "The content of the archive.")
  @GET
  @Path("zipcontent/{name}")
  public Response getZipContent(@PathParam("name") String zipFileName) {
    throw new WebApplicationException(Status.NOT_FOUND);
  }

  @Override
  protected boolean isFileReadable(SimpleDocument attachment) {
    Ticket ticket = SharingServiceProvider.getSharingTicketService().getTicket(getToken());
    var ctx = AccessControlContext.about(attachment);
    return ticket != null && ticket.isValid() &&
        SharingCodeVerifier.verify(ticket, getHttpRequest().getHeader("X-Verification-Code")) &&
        ticket.getAccessControl().isReadable(ctx);
  }

  protected String getToken() {
    return token;
  }

}
