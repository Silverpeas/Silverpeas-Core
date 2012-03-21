/**
 * Copyright (C) 2000 - 2011 Silverpeas
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
package com.silverpeas.sharing.servlets;

import com.silverpeas.look.SilverpeasLook;
import com.silverpeas.sharing.model.SimpleFileTicket;
import com.silverpeas.sharing.model.Ticket;
import com.silverpeas.sharing.model.VersionFileTicket;
import com.silverpeas.sharing.services.SharingServiceFactory;
import com.stratelia.silverpeas.versioning.model.Document;
import com.stratelia.silverpeas.versioning.model.DocumentVersion;
import com.stratelia.silverpeas.versioning.util.VersioningUtil;
import com.stratelia.webactiv.beans.admin.ComponentInstLight;
import com.stratelia.webactiv.beans.admin.OrganizationController;
import com.stratelia.webactiv.util.attachment.model.AttachmentDetail;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static com.silverpeas.sharing.servlets.FileSharingConstants.*;

public class GetInfoFromKeyServlet extends HttpServlet {

  private static final long serialVersionUID = 1L;
  private OrganizationController organizationController = new OrganizationController();

  @Override
  protected void service(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {
    String keyFile = request.getParameter(PARAM_KEYFILE);
    Ticket ticket = SharingServiceFactory.getSharingTicketService().getTicket(keyFile);
    request.setAttribute(ATT_TICKET, ticket);
    if (ticket == null || !ticket.isValid()) {
      getServletContext().getRequestDispatcher("/sharing/jsp/invalidTicket.jsp").forward(
          request, response);
    } else {

      if (ticket instanceof SimpleFileTicket) {
        AttachmentDetail attachment = ((SimpleFileTicket) ticket).getResource().getAccessedObject();
        request.setAttribute("fileIcon", attachment.getAttachmentIcon());
        request.setAttribute("fileSize", attachment.getSize());
      } else if (ticket instanceof VersionFileTicket) {
        Document document = ((VersionFileTicket) ticket).getResource().getAccessedObject();
        DocumentVersion version = new VersioningUtil().getLastPublicVersion(document.getPk());
        request.setAttribute("fileIcon", version.getDocumentIcon());
        request.setAttribute("fileSize", version.getSize());
      }
      request.setAttribute(ATT_WALLPAPER, getWallpaperFor(ticket));
      request.setAttribute(ATT_KEYFILE, keyFile);
      getServletContext().getRequestDispatcher("/sharing/jsp/displayTicketInfo.jsp").forward(
          request, response);
    }
  }

  /**
   * Gets the wallpaper of the space to which the component corresponding to the ticket belongs. The
   * wallpaper is fetched from the direct space of the component upto the first parent space that
   * have a specific wallpapers.
   *
   * @return the URL of the wallpaper.
   */
  private String getWallpaperFor(final Ticket ticket) {
    ComponentInstLight component = getOrganizationController().getComponentInstLight(ticket.
        getComponentId());
    return SilverpeasLook.getSilverpeasLook().getWallpaperOfSpaceOrDefaultOne(component.
        getDomainFatherId());
  }

  private OrganizationController getOrganizationController() {
    return organizationController;
  }
}