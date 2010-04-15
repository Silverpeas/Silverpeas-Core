/**
 * Copyright (C) 2000 - 2009 Silverpeas
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
 * "http://repository.silverpeas.com/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.silverpeas.external.filesharing.servlets;

import static com.silverpeas.external.filesharing.servlets.FileSharingConstants.ATT_ATTACHMENT;
import static com.silverpeas.external.filesharing.servlets.FileSharingConstants.ATT_DOCUMENT;
import static com.silverpeas.external.filesharing.servlets.FileSharingConstants.ATT_DOCUMENTVERSION;
import static com.silverpeas.external.filesharing.servlets.FileSharingConstants.ATT_TICKET;
import static com.silverpeas.external.filesharing.servlets.FileSharingConstants.PARAM_KEYFILE;
import static com.silverpeas.external.filesharing.servlets.FileSharingConstants.ATT_KEYFILE;
import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.silverpeas.external.filesharing.model.FileSharingFactory;
import com.silverpeas.external.filesharing.model.TicketDetail;
import com.stratelia.silverpeas.versioning.model.Document;
import com.stratelia.silverpeas.versioning.model.DocumentPK;
import com.stratelia.silverpeas.versioning.model.DocumentVersion;
import com.stratelia.silverpeas.versioning.util.VersioningUtil;
import com.stratelia.webactiv.util.attachment.control.AttachmentController;
import com.stratelia.webactiv.util.attachment.ejb.AttachmentPK;

public class GetInfoFromKeyServlet extends HttpServlet {
  private static final long serialVersionUID = 1L;

  @Override
  protected void service(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {
    String keyFile = request.getParameter(PARAM_KEYFILE);
    TicketDetail ticket = FileSharingFactory.getFileSharing().getTicket(keyFile);
    request.setAttribute(ATT_TICKET, ticket);
    if (!ticket.isValid()) {
      getServletContext().getRequestDispatcher("/fileSharing/jsp/invalidTicket.jsp").forward(
          request, response);
      return;
    }
    if (!ticket.isVersioning())
      request.setAttribute(ATT_ATTACHMENT, AttachmentController
          .searchAttachmentByPK(new AttachmentPK("" + ticket.getFileId())));
    else {
      VersioningUtil versioningUtil = new VersioningUtil();
      DocumentPK documentPK = new DocumentPK(ticket.getFileId(), ticket.getComponentId());
      Document document = versioningUtil.getDocument(documentPK);
      DocumentVersion version = versioningUtil.getLastPublicVersion(documentPK);

      request.setAttribute(ATT_DOCUMENT, document);
      request.setAttribute(ATT_DOCUMENTVERSION, version);
    }
    request.setAttribute(ATT_KEYFILE, keyFile);
    getServletContext().getRequestDispatcher("/fileSharing/jsp/displayTicketInfo.jsp").forward(
        request, response);

  }
}