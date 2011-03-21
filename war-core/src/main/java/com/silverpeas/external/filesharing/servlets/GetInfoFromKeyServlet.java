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

import com.stratelia.webactiv.util.FileServerUtils;
import com.silverpeas.util.FileUtil;
import java.io.File;
import com.stratelia.webactiv.util.FileRepositoryManager;
import java.util.Collections;
import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.silverpeas.external.filesharing.model.FileSharingFactory;
import com.silverpeas.external.filesharing.model.TicketDetail;
import com.silverpeas.look.SilverpeasLook;
import com.stratelia.silverpeas.versioning.model.Document;
import com.stratelia.silverpeas.versioning.model.DocumentPK;
import com.stratelia.silverpeas.versioning.model.DocumentVersion;
import com.stratelia.silverpeas.versioning.util.VersioningUtil;
import com.stratelia.webactiv.beans.admin.Admin;
import com.stratelia.webactiv.beans.admin.ComponentInstLight;
import com.stratelia.webactiv.beans.admin.OrganizationController;
import com.stratelia.webactiv.beans.admin.SpaceInst;
import com.stratelia.webactiv.util.attachment.control.AttachmentController;
import com.stratelia.webactiv.util.attachment.ejb.AttachmentPK;
import java.util.List;
import static com.silverpeas.util.StringUtil.*;
import static com.silverpeas.external.filesharing.servlets.FileSharingConstants.*;

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
    } else {
      if (!ticket.isVersioning()) {
        request.setAttribute(ATT_ATTACHMENT,
            AttachmentController.searchAttachmentByPK(new AttachmentPK("" + ticket.getFileId())));
      } else {
        VersioningUtil versioningUtil = new VersioningUtil();
        DocumentPK documentPK = new DocumentPK(ticket.getFileId(), ticket.getComponentId());
        Document document = versioningUtil.getDocument(documentPK);
        DocumentVersion version = versioningUtil.getLastPublicVersion(documentPK);

        request.setAttribute(ATT_DOCUMENT, document);
        request.setAttribute(ATT_DOCUMENTVERSION, version);
      }
      request.setAttribute(ATT_WALLPAPER, getSpaceWallPaper(ticket));
      request.setAttribute(ATT_KEYFILE, keyFile);
      getServletContext().getRequestDispatcher("/fileSharing/jsp/displayTicketInfo.jsp").forward(
          request, response);
    }
  }

  /**
   * Gets the wallpaper of the space to which the component corresponding to the ticket belongs.
   * The wallpaper is fetched from the direct space of the component upto the first parent space
   * that have a specific wallpapers.
   * @return the URL of the wallpaper.
   */
  private String getWallpaper(final TicketDetail ticket) {
    OrganizationController organizationController = new OrganizationController();
    ComponentInstLight component = organizationController.getComponentInstLight(ticket.
        getComponentId());
    return SilverpeasLook.getSilverpeasLook().getWallpaperOfSpace(component.getDomainFatherId());
  }

  public String getSpaceWallPaper(final TicketDetail ticket) {
    OrganizationController organizationController = new OrganizationController();
    ComponentInstLight component = organizationController.getComponentInstLight(ticket.
        getComponentId());
    String spaceId = component.getDomainFatherId();
    if (!isDefined(spaceId)) {
      return null;
    }
    //return SilverpeasLook.getSilverpeasLook().getWallpaperOfSpace(getSpaceId());

      // get wallpaper of current subspace or first super space
      List<SpaceInst> spaces = organizationController.getSpacePath(spaceId);
      Collections.reverse(spaces);

      String wallpaper = null;
      for (int i = 0; wallpaper == null && i < spaces.size(); i++) {
        SpaceInst space = spaces.get(i);
        wallpaper = getSpaceWallPaper(space.getId());
      }
      return wallpaper;
  }

  private String getSpaceWallPaper(String id) {
    if (id.startsWith(Admin.SPACE_KEY_PREFIX)) {
      id = id.substring(2);
    }
    String path =
        FileRepositoryManager.getAbsolutePath("Space" + id, new String[]{"look"});

    String filePath = getWallPaper(path, id, "jpg");
    if (!isDefined(filePath)) {
      filePath = getWallPaper(path, id, "gif");
      if (!isDefined(filePath)) {
        filePath = getWallPaper(path, id, "png");
      }
    }
    return filePath;
  }

  private String getWallPaper(String path, String spaceId, String extension) {
    String image = "wallPaper." + extension;
    File file = new File(path + image);
    if (file.isFile()) {
      return FileServerUtils.getOnlineURL("Space" + spaceId, file.getName(), file.getName(), FileUtil.
          getMimeType(image), "look");
    }
    return null;
  }
}