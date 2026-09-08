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

import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;

import org.silverpeas.core.admin.user.model.UserDetail;
import org.silverpeas.core.annotation.WebService;
import org.silverpeas.core.contribution.attachment.model.SimpleDocument;
import org.silverpeas.core.web.rs.annotation.Authorized;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

/**
 * A REST Web resource providing access to attachments through private mode.
 */
@Tag(name = "Attachments",
    description = "The files attached to the contributions and visible to the authenticated user.")
@WebService
@Path(AttachmentResource.PATH + "/{componentId}")
@Authorized
public class AttachmentResource extends AbstractAttachmentResource {

  static final String PATH = "private/attachments";

  @Override
  protected String getResourceBasePath() {
    return PATH;
  }

  @Operation(summary = "Downloads the content of the given attachment.")
  @ApiResponse(responseCode = "200", description = "The content of the attachment.")
  @GET
  @Path("{id}/{name}")
  @Produces(MediaType.APPLICATION_OCTET_STREAM)
  public Response getFileContent(final @PathParam("id") String attachmentId) {
    return getAttachmentContent(attachmentId);
  }

  @Override
  protected boolean isFileReadable(SimpleDocument attachment) {
    return attachment.canBeAccessedBy(UserDetail.from(getUser()));
  }

}