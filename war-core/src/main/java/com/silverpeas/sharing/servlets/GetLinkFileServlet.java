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
package com.silverpeas.sharing.servlets;

import static com.silverpeas.sharing.servlets.FileSharingConstants.PARAM_KEYFILE;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Date;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.silverpeas.sharing.model.Ticket;
import com.silverpeas.sharing.services.SharingServiceFactory;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import com.silverpeas.sharing.model.DownloadDetail;
import com.silverpeas.sharing.model.SimpleFileTicket;
import com.silverpeas.sharing.model.VersionFileTicket;
import com.silverpeas.util.web.servlet.RestRequest;
import com.stratelia.silverpeas.versioning.model.DocumentPK;
import com.stratelia.silverpeas.versioning.model.DocumentVersion;
import com.stratelia.silverpeas.versioning.util.VersioningUtil;
import com.stratelia.webactiv.util.FileRepositoryManager;
import com.stratelia.webactiv.util.attachment.control.AttachmentController;
import com.stratelia.webactiv.util.attachment.ejb.AttachmentPK;
import com.stratelia.webactiv.util.attachment.model.AttachmentDetail;

public class GetLinkFileServlet extends HttpServlet {

  private static final long serialVersionUID = -3386956747183311695L;

  @Override
  protected void service(HttpServletRequest request,
      HttpServletResponse response) throws ServletException, IOException {
    RestRequest rest = new RestRequest(request, "myFile");
    String keyFile = rest.getElementValue(PARAM_KEYFILE);
    Ticket ticket = SharingServiceFactory.getSharingTicketService().getTicket(keyFile);
    if (ticket.isValid()) {
      // recherche des infos sur le fichier...
      String filePath = null;
      String fileType = null;
      String fileName = null;
      long fileSize = 0;
      if (ticket instanceof SimpleFileTicket) {
        AttachmentDetail attachment = AttachmentController.
            searchAttachmentByPK(new AttachmentPK("" +
            ticket.getSharedObjectId()));
        filePath = FileRepositoryManager.getAbsolutePath(attachment.getInstanceId() +
            File.separatorChar + FileRepositoryManager.getRelativePath(FileRepositoryManager.
            getAttachmentContext(attachment.getContext()))) + attachment.getPhysicalName();
        fileType = attachment.getType();
        fileName = attachment.getLogicalName();
        fileSize = attachment.getSize();
      } else if (ticket instanceof VersionFileTicket) {
        DocumentVersion version = new VersioningUtil().
            getLastPublicVersion(new DocumentPK((int) ticket.getSharedObjectId(), ticket.
            getComponentId()));
        filePath = FileRepositoryManager.getAbsolutePath(ticket.getComponentId()) + File.separatorChar +
            "Versioning" + File.separatorChar + version.getPhysicalName();
        fileType = version.getMimeType();
        fileName = version.getLogicalName();
        fileSize = version.getSize();
      }
      File realFile = new File(filePath);
      BufferedInputStream input = null;
      OutputStream out = response.getOutputStream();
      try {
        response.setContentType(fileType);
        response.setHeader("Content-Disposition", "inline; filename=\"" + fileName + "\"");
        response.setContentLength((int) fileSize);
        input = new BufferedInputStream(FileUtils.openInputStream(realFile));
        IOUtils.copy(input, out);
        DownloadDetail download = new DownloadDetail(ticket, new Date(), request.getRemoteAddr());
        SharingServiceFactory.getFactory().getSharingTicketService().addDownload(download);
        return;
      } catch (Exception ex) {
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