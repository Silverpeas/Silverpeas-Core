/*
 * Copyright (C) 2000 - 2016 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception. You should have received a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.silverpeas.web.sharing.servlets;

import org.silverpeas.core.web.look.SilverpeasLook;
import org.silverpeas.core.sharing.model.NodeTicket;
import org.silverpeas.core.sharing.model.PublicationTicket;
import org.silverpeas.core.sharing.model.SimpleFileTicket;
import org.silverpeas.core.sharing.model.Ticket;
import org.silverpeas.core.sharing.model.VersionFileTicket;
import org.silverpeas.core.sharing.security.ShareableAttachment;
import org.silverpeas.core.sharing.security.ShareableResource;
import org.silverpeas.core.sharing.services.SharingServiceProvider;
import org.silverpeas.core.util.URLUtil;
import org.silverpeas.core.admin.component.model.ComponentInstLight;
import org.silverpeas.core.admin.service.OrganizationControllerProvider;
import org.silverpeas.core.util.file.FileRepositoryManager;
import org.silverpeas.core.util.ResourceLocator;
import org.silverpeas.core.contribution.attachment.model.SimpleDocument;
import org.silverpeas.core.util.SettingBundle;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static org.silverpeas.web.sharing.servlets.FileSharingConstants.*;

public class GetInfoFromKeyServlet extends HttpServlet {

  private static final long serialVersionUID = 1541425708777215319L;
  private static final SettingBundle settings =
      ResourceLocator.getSettingBundle("org.silverpeas.sharing.settings.sharing");

  @Override
  protected void service(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {
    String token = request.getParameter(PARAM_KEYFILE);
    Ticket ticket = SharingServiceProvider.getSharingTicketService().getTicket(token);
    request.setAttribute(ATT_TICKET, ticket);
    if (ticket == null || !ticket.isValid()) {
      getServletContext().getRequestDispatcher("/sharing/jsp/invalidTicket.jsp")
          .forward(request, response);
    } else if (ticket instanceof NodeTicket || ticket instanceof PublicationTicket) {
      String url = getURLForSharedObject(request, ticket);
      response.sendRedirect(url);
    } else {
      SimpleDocument document = null;
      if (ticket instanceof SimpleFileTicket) {
        document = ((SimpleFileTicket) ticket).getResource().getAccessedObject();
      } else if (ticket instanceof VersionFileTicket) {
        document = ((VersionFileTicket) ticket).getResource().getAccessedObject();
      }
      if (document != null) {
        SimpleDocument lastPublicVersion = document.getLastPublicVersion();
        ShareableResource<SimpleDocument> lastPublicVersionResource =
            new ShareableAttachment(ticket.getToken(), lastPublicVersion);
        request.setAttribute(ATT_TICKET + "Resource", lastPublicVersionResource);
        request.setAttribute("fileIcon", lastPublicVersion.getDisplayIcon());
        request.setAttribute("fileSize",
            FileRepositoryManager.formatFileSize(lastPublicVersion.getSize()));
        request.setAttribute(ATT_WALLPAPER, getWallpaperFor(ticket));
        request.setAttribute(ATT_KEYFILE, token);
        getServletContext().getRequestDispatcher("/sharing/jsp/displayTicketInfo.jsp")
            .forward(request, response);
      } else {
        getServletContext().getRequestDispatcher("/sharing/jsp/invalidTicket.jsp")
            .forward(request, response);
      }
    }
  }

  /**
   * Gets the wallpaper of the space to which the component corresponding to the ticket belongs.
   * The
   * wallpaper is fetched from the direct space of the component upto the first parent space that
   * have a specific wallpapers.
   * @return the URL of the wallpaper.
   */
  private String getWallpaperFor(final Ticket ticket) {
    ComponentInstLight component = OrganizationControllerProvider.getOrganisationController()
        .getComponentInstLight(ticket.getComponentId());
    return SilverpeasLook.getSilverpeasLook().getWallpaperOfSpaceOrDefaultOne(component.
        getDomainFatherId());
  }

  private String getURLForSharedObject(HttpServletRequest request, Ticket ticket) {
    String url = settings.getString("sharing.folder.webapp");
    if (ticket instanceof PublicationTicket) {
      url = settings.getString("sharing.publication.webapp");
    }
    if (!url.startsWith("http")) {
      url = URLUtil.getServerURL(request) + url;
    }
    return url + "?" + ticket.getToken();
  }
}