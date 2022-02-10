/*
 * Copyright (C) 2000 - 2022 Silverpeas
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

import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.contribution.attachment.AttachmentServiceProvider;
import org.silverpeas.core.contribution.attachment.model.SimpleDocument;
import org.silverpeas.core.contribution.attachment.model.SimpleDocumentPK;
import org.silverpeas.core.util.logging.SilverLogger;
import org.silverpeas.core.web.rs.RESTWebService;

import javax.ws.rs.PathParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.StreamingOutput;

import static java.util.Optional.ofNullable;

/**
 * A REST Web resource providing access to attachments.
 */
public abstract class AbstractAttachmentResource extends RESTWebService {

  @PathParam("componentId")
  protected String componentId;

  @Override
  public String getComponentId() {
    return componentId;
  }

  protected Response getFileContent(String attachmentId) {
    final SimpleDocument attachment = AttachmentServiceProvider.getAttachmentService()
        .searchDocumentById(new SimpleDocumentPK(attachmentId), null);
    if (attachment == null) {
      SilverLogger.getLogger(this).warn("Resource not found from request {0} with user {1}",
          getHttpRequest().getRequestURI(), ofNullable(getUser()).map(User::getId).orElse("N/A"));
      return Response.noContent().status(Status.NOT_FOUND).build();
    }
    final SimpleDocument lastPublicVersion = attachment.getLastPublicVersion();
    if (!isFileReadable(lastPublicVersion)) {
      SilverLogger.getLogger(this).warn("Resource not authorized from request {0} with user {1}",
          getHttpRequest().getRequestURI(), ofNullable(getUser()).map(User::getId).orElse("N/A"));
      return Response.noContent().status(Status.UNAUTHORIZED).build();
    }
    StreamingOutput data = output -> {
      try {
        AttachmentServiceProvider.getAttachmentService().getBinaryContent(
            output, lastPublicVersion.getPk(), lastPublicVersion.getLanguage());
      } catch (Exception e) {
        throw new WebApplicationException(e, Status.INTERNAL_SERVER_ERROR);
      }
    };
    return Response.ok().entity(data).type(lastPublicVersion.getContentType()).build();
  }

  protected abstract boolean isFileReadable(SimpleDocument attachment);

}
