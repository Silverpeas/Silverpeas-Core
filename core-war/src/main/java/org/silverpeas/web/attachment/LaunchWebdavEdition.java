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
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package org.silverpeas.web.attachment;

import org.silverpeas.core.admin.user.model.UserDetail;
import org.silverpeas.core.contribution.attachment.AttachmentService;
import org.silverpeas.core.contribution.attachment.model.SimpleDocument;
import org.silverpeas.core.contribution.attachment.model.SimpleDocumentPK;
import org.silverpeas.core.contribution.attachment.webdav.WebdavWbeFile;
import org.silverpeas.core.util.StringUtil;
import org.silverpeas.core.util.URLUtil;
import org.silverpeas.core.util.logging.SilverLogger;
import org.silverpeas.core.web.mvc.webcomponent.SilverpeasAuthenticatedHttpServlet;
import org.silverpeas.core.webapi.wbe.WbeFileEdition;
import org.silverpeas.core.jcr.webdav.WebDavAccessOpener;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Optional;

/**
 * @author ehugonnet
 */
public class LaunchWebdavEdition extends SilverpeasAuthenticatedHttpServlet {
  private static final long serialVersionUID = 3738081252893759397L;

  /**
   * Processes requests for both HTTP <code>GET</code> and <code>POST</code> methods.
   * @param request servlet request
   * @param response servlet response
   */
  protected void processRequest(final HttpServletRequest request,
      final HttpServletResponse response) {
    UserDetail user = getMainSessionController(request).getCurrentUserDetail();
    String id = request.getParameter("id");
    String language = request.getParameter("lang");
    boolean wbe = StringUtil.getBooleanValue(request.getParameter("wbe"));
    SimpleDocument document =
        AttachmentService.get().searchDocumentById(new SimpleDocumentPK(id), language);

    if (!document.isEdited() || !document.canBeModifiedBy(user) ||
        (!document.isEditedBy(user) &&
            (!wbe || !document.editableSimultaneously().orElse(false)))) {
      throwHttpForbiddenError();
    }

    try {
      final Optional<String> wbeEditorUrl = Optional.of(wbe).filter(w -> w)
          .flatMap(w -> WbeFileEdition.get().initializeWith(request, new WebdavWbeFile(document)));
      if (wbeEditorUrl.isPresent()) {
        final RequestDispatcher requestDispatcher =
            request.getRequestDispatcher(wbeEditorUrl.get());
        requestDispatcher.forward(request, response);
      } else {
        String documentUrl = URLUtil.getServerURL(request) + document.getWebdavUrl();
        new WebDavAccessOpener().open(user, documentUrl, response);
      }
    } catch (ServletException | IOException e) {
      SilverLogger.getLogger(this).error(e);
      response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
    }
  }

  /**
   * Handles the HTTP <code>GET</code> method.
   * @param request servlet request
   * @param response servlet response
   */
  @Override
  protected void doGet(HttpServletRequest request, HttpServletResponse response) {
    processRequest(request, response);
  }

  /**
   * Handles the HTTP <code>POST</code> method.
   * @param request servlet request
   * @param response servlet response
   */
  @Override
  protected void doPost(HttpServletRequest request, HttpServletResponse response) {
    processRequest(request, response);
  }

  /**
   * Returns a short description of the servlet.
   * @return a String containing servlet description
   */
  @Override
  public String getServletInfo() {
    return "Generating the JNLP for direct edition";
  }

}
