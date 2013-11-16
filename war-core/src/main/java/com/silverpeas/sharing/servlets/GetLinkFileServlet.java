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
package com.silverpeas.sharing.servlets;

import com.silverpeas.sharing.model.DownloadDetail;
import com.silverpeas.sharing.model.SimpleFileTicket;
import com.silverpeas.sharing.model.Ticket;
import com.silverpeas.sharing.model.VersionFileTicket;
import com.silverpeas.sharing.services.SharingServiceFactory;
import com.silverpeas.util.web.servlet.RestRequest;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.silverpeas.attachment.AttachmentServiceFactory;
import org.silverpeas.attachment.model.HistorisedDocument;
import org.silverpeas.attachment.model.SimpleDocument;
import org.silverpeas.attachment.model.SimpleDocumentPK;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Date;

import static com.silverpeas.sharing.servlets.FileSharingConstants.PARAM_KEYFILE;

public class GetLinkFileServlet extends HttpServlet {

  private static final long serialVersionUID = -3386956747183311695L;

  @Override
  protected void service(HttpServletRequest request,
      HttpServletResponse response) throws ServletException, IOException {
    RestRequest rest = new RestRequest(request, "myFile");
    String keyFile = rest.getElementValue(PARAM_KEYFILE);
    Ticket ticket = SharingServiceFactory.getSharingTicketService().getTicket(keyFile);
    if (ticket != null && ticket.isValid()) {
      // recherche des infos sur le fichier...
      String filePath = null;
      String fileType = null;
      String fileName = null;
      long fileSize = 0;
      if (ticket instanceof SimpleFileTicket) {
        SimpleDocumentPK pk = new SimpleDocumentPK(null, ticket.getComponentId());
        pk.setOldSilverpeasId(ticket.getSharedObjectId());
        SimpleDocument document = AttachmentServiceFactory.getAttachmentService().
            searchDocumentById(pk, null);
        filePath = document.getAttachmentPath();
        fileType = document.getContentType();
        fileName = document.getFilename();
        fileSize = document.getSize();
      } else if (ticket instanceof VersionFileTicket) {
        SimpleDocumentPK pk = new SimpleDocumentPK(null, ticket.getComponentId());
        pk.setOldSilverpeasId(ticket.getSharedObjectId());
        HistorisedDocument versionedDocument = (HistorisedDocument) AttachmentServiceFactory.
            getAttachmentService().searchDocumentById(pk, null);
        SimpleDocument document = versionedDocument.getLastPublicVersion();
        filePath = document.getAttachmentPath();
        fileType = document.getContentType();
        fileName = document.getFilename();
        fileSize = document.getSize();
      }
      File realFile = new File(filePath);
      BufferedInputStream input = null;
      OutputStream out = response.getOutputStream();
      try {
        response.setContentType(fileType);
        response.setHeader("Content-Disposition", "inline; filename=\"" + fileName + "\"");
        response.setHeader( "Content-Length", String.valueOf(fileSize));
        input = new BufferedInputStream(FileUtils.openInputStream(realFile));
        IOUtils.copy(input, out);
        DownloadDetail download = new DownloadDetail(ticket, new Date(), request.getRemoteAddr());
        SharingServiceFactory.getSharingTicketService().addDownload(download);
        return;
      } catch (Exception ignored) {
      } finally {
        if (input != null) {
          IOUtils.closeQuietly(input);
        }
        IOUtils.closeQuietly(out);
      }
    }
    getServletContext().getRequestDispatcher("/sharing/jsp/invalidTicket.jsp").forward(request,
        response);
  }
}