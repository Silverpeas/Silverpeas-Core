/*
 * Copyright (C) 2000 - 2020 Silverpeas
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
package org.silverpeas.web.templatedesigner.servlets;

import org.silverpeas.core.contribution.template.publication.PublicationTemplateManager;
import org.silverpeas.core.web.mvc.controller.MainSessionController;
import org.silverpeas.core.util.URLUtil;
import org.silverpeas.core.admin.user.model.UserDetail;
import org.apache.commons.io.FileUtils;
import org.silverpeas.core.util.file.FileUtil;
import org.silverpeas.core.util.ResourceLocator;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.File;
import java.io.IOException;

public class FormLayerServlet extends HttpServlet {

  private static final long serialVersionUID = 1013565640540446129L;

  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {

    HttpSession session = request.getSession(true);
    MainSessionController mainSessionCtrl = (MainSessionController) session.getAttribute("SilverSessionController");
    if (mainSessionCtrl == null) {
      response.sendRedirect(URLUtil.getApplicationURL() +
          ResourceLocator.getGeneralSettingBundle().getString("sessionTimeout"));
    }

    String pathInfo = request.getPathInfo();

    String form = pathInfo.substring(1);
    if (UserDetail.getCurrentRequester().isAccessAdmin()) {
      String filename = request.getParameter("Layer");
      String dir = PublicationTemplateManager.getInstance().makePath(form);
      File file = new File(dir, filename);
      response.setContentType(FileUtil.getMimeType(filename));
      response.setHeader("Content-Disposition", "inline; filename=\"" + filename + '"');
      response.setHeader("Content-Length", String.valueOf(file.length()));
      FileUtils.copyFile(file, response.getOutputStream());
      response.getOutputStream().flush();
    } else {
      response.sendRedirect(URLUtil.getApplicationURL() +
          ResourceLocator.getGeneralSettingBundle().getString("sessionTimeout"));
    }
  }

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {
    doPost(request, response);
  }

}
