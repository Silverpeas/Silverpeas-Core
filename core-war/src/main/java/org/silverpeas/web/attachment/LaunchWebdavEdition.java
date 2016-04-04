/*
 * Copyright (C) 2000 - 2016 Silverpeas
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
package org.silverpeas.web.attachment;

import org.silverpeas.core.util.URLUtil;
import org.silverpeas.core.web.mvc.webcomponent.SilverpeasAuthenticatedHttpServlet;
import org.silverpeas.core.admin.user.model.UserDetail;
import org.apache.commons.lang3.CharEncoding;
import org.silverpeas.core.contribution.attachment.AttachmentService;
import org.silverpeas.core.contribution.attachment.model.SimpleDocument;
import org.silverpeas.core.contribution.attachment.model.SimpleDocumentPK;
import org.silverpeas.core.web.attachment.WebDavTokenProducer;
import org.silverpeas.core.util.ResourceLocator;
import org.silverpeas.core.util.SettingBundle;
import org.silverpeas.core.web.webdav.SilverpeasJcrWebdavContext;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import static org.silverpeas.core.web.webdav.SilverpeasJcrWebdavContext.createWebdavContext;

/**
 * @author ehugonnet
 */
public class LaunchWebdavEdition extends SilverpeasAuthenticatedHttpServlet {

  private static final SettingBundle resources =
      ResourceLocator.getSettingBundle("org.silverpeas.util.attachment.Attachment");

  /**
   * Processes requests for both HTTP <code>GET</code> and <code>POST</code> methods.
   *
   * @param request servlet request
   * @param response servlet response
   * @throws ServletException if a servlet-specific error occurs
   * @throws IOException if an I/O error occurs
   */
  protected void processRequest(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {

    UserDetail user = getMainSessionController(request).getCurrentUserDetail();
    String id = request.getParameter("id");
    String language = request.getParameter("lang");
    SimpleDocument document =
        AttachmentService.get().searchDocumentById(new SimpleDocumentPK(id), language);

    if (!document.canBeModifiedBy(user)) {
      throwHttpForbiddenError();
    }

    String documentUrl = URLUtil.getServerURL(request) + document.getWebdavUrl();
    String token = WebDavTokenProducer.generateToken(user, fetchDocumentId(documentUrl));
    SilverpeasJcrWebdavContext silverpeasJcrWebdavContext = createWebdavContext(documentUrl, token);
    if (resources.getBoolean("attachment.onlineEditing.customProtocol", false)) {
      response.setContentType("application/javascript");
      response.setHeader("Content-Disposition", "inline; filename=launch.js");
      String webDavUrl =
          silverpeasJcrWebdavContext.getWebDavUrl().replaceFirst("^http", "spwebdav");
      response.getWriter().append("window.location.href='").append(webDavUrl).append("';");
    } else {
      response.setContentType("application/x-java-jnlp-file");
      response.setHeader("Content-Disposition", "inline; filename=launch.jnlp");
      prepareJNLP(request, response.getWriter(), user.getLogin(),
          silverpeasJcrWebdavContext.getWebDavUrl());
    }
  }

  private void prepareJNLP(HttpServletRequest request, PrintWriter out, String login,
      String documentUrl) throws UnsupportedEncodingException {
    out.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
    out.print("<jnlp spec=\"1.0+\" codebase=\"");
    out.print(URLUtil.getServerURL(request));
    out.print(request.getContextPath());
    out.println("/attachment/webdav\" >");
    out.println("\t<information>");
    out.println("\t\t<title>Edition WebDAV</title>");
    out.println("\t\t<vendor>Silverpeas</vendor>");
    out.println("\t\t<homepage href=\"http://www.silverpeas.com\"/>");
    out.println(
        "\t\t<description>A simple Java webstart application to launch Online Document Edition</description>");
    out.println("\t\t<description kind=\"short\">Online Document Editor</description>");
    out.println("\t\t<icon href=\"logo.png\" kind=\"default\"/>");
    out.println("\t\t<icon href=\"logo.png\" kind=\"splash\"/>");
    out.println("\t\t<offline-allowed/>");
    out.println("\t</information>");
    out.println("\t<security>");
    out.println("\t\t<all-permissions/>");
    out.println("\t</security>");
    out.println("\t<update check=\"timeout\" policy=\"always\"/>");
    out.println("\t<resources>");
    out.println("\t\t<j2se href=\"http://java.sun.com/products/autodl/j2se\" version=\"1.7+\" />");
    out.println("\t\t<jar href=\"OpenOfficeLauncher.jar\" download=\"eager\"/>");
    out.println("\t\t<jar href=\"xercesImpl-2.10.0.jar\" download=\"eager\"/>");
    out.println("\t\t<jar href=\"jcl-over-slf4j-1.7.7.jar\" download=\"eager\"/>");
    out.println("\t\t<jar href=\"slf4j-log4j12-1.7.7.jar\" download=\"eager\"/>");
    out.println("\t\t<jar href=\"slf4j-api-1.7.7.jar\" download=\"eager\"/>");
    out.println("\t\t<jar href=\"log4j-1.2.17.jar\" download=\"eager\"/>");
    out.println("\t\t<jar href=\"xml-apis-1.4.01.jar\" download=\"eager\"/>");
    out.println("\t</resources>");
    out.println("\t<application-desc main-class=\"org.silverpeas.openoffice.OfficeOnline\">");
    out.print("\t\t<argument>");
    out.print(URLEncoder.encode(documentUrl, CharEncoding.UTF_8));
    out.println("</argument>");
    out.print("\t\t<argument>");
    out.print(URLEncoder.encode(resources.getString("ms.office.installation.path"),
        CharEncoding.UTF_8));
    out.println("</argument>");
    out.print("\t\t<argument>");
    out.print(URLEncoder.encode(login, CharEncoding.UTF_8));
    out.println("</argument>");
    out.print("\t\t<argument>");
    out.print(resources.getBoolean("deconnectedMode", false));
    out.println("</argument>");
    out.println("\t</application-desc>");
    out.println(" </jnlp>");
  }

  /**
   * Handles the HTTP <code>GET</code> method.
   *
   * @param request servlet request
   * @param response servlet response
   * @throws ServletException if a servlet-specific error occurs
   * @throws IOException if an I/O error occurs
   */
  @Override
  protected void doGet(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {
    processRequest(request, response);
  }

  /**
   * Handles the HTTP <code>POST</code> method.
   *
   * @param request servlet request
   * @param response servlet response
   * @throws ServletException if a servlet-specific error occurs
   * @throws IOException if an I/O error occurs
   */
  @Override
  protected void doPost(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {
    processRequest(request, response);
  }

  /**
   * Returns a short description of the servlet.
   *
   * @return a String containing servlet description
   */
  @Override
  public String getServletInfo() {
    return "Generating the JNLP for direct edition";
  }

  private static String fetchDocumentId(String documentUrl) {
    String[] paths = documentUrl.split("/");
    if (paths.length > 3) {
      return paths[paths.length - 3];
    }
    return null;
  }
}
