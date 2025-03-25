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
package org.silverpeas.web.sharing.servlets;

import org.silverpeas.core.sharing.model.DownloadDetail;
import org.silverpeas.core.sharing.model.SimpleFileTicket;
import org.silverpeas.core.sharing.model.Ticket;
import org.silverpeas.core.sharing.model.VersionFileTicket;
import org.silverpeas.core.sharing.services.SharingServiceProvider;
import org.silverpeas.web.servlets.RestRequest;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.silverpeas.core.contribution.attachment.model.SimpleDocument;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Date;

import static java.text.MessageFormat.format;
import static org.silverpeas.web.sharing.servlets.FileSharingConstants.PARAM_KEYFILE;

public class GetLinkFileServlet extends HttpServlet {

  private static final long serialVersionUID = -3386956747183311695L;

  @Override
  protected void service(HttpServletRequest request,
      HttpServletResponse response) throws ServletException, IOException {
    RestRequest rest = new RestRequest(request, "myFile");
    String keyFile = rest.getElementValue(PARAM_KEYFILE);
    Ticket ticket = SharingServiceProvider.getSharingTicketService().getTicket(keyFile);
    if (ticket != null && ticket.isValid()) {
      // recherche des infos sur le fichier...
      SimpleDocument document = null;
      if (ticket instanceof SimpleFileTicket) {
        document = ((SimpleFileTicket) ticket).getResource().getAccessedObject();
      } else if (ticket instanceof VersionFileTicket) {
        document = ((VersionFileTicket) ticket).getResource().getAccessedObject();
      }
      if (document != null) {
        String filePath = document.getAttachmentPath();
        String fileType = document.getContentType();
        String fileName = document.getFilename();
        long fileSize = document.getSize();
        OutputStream out = response.getOutputStream();
        File realFile = new File(filePath);
        try(BufferedInputStream input =
                new BufferedInputStream(FileUtils.openInputStream(realFile))) {
          response.setContentType(fileType);
          response.setHeader("Content-Disposition", format("inline; filename=\"{0}\"", fileName));
          response.setHeader("Content-Length", String.valueOf(fileSize));
          IOUtils.copy(input, out);
          DownloadDetail download = new DownloadDetail(ticket, new Date(), request.getRemoteAddr());
          SharingServiceProvider.getSharingTicketService().addDownload(download);
        } catch (Exception ignored) {
          sendBackInvalidTicket(request, response);
        }
      } else {
        sendBackInvalidTicket(request, response);
      }
    } else {
      sendBackInvalidTicket(request, response);
    }
  }

  private void sendBackInvalidTicket(final HttpServletRequest req, final HttpServletResponse resp)
      throws IOException, ServletException {
    getServletContext().getRequestDispatcher("/sharing/jsp/invalidTicket.jsp")
        .forward(req, resp);
  }
}