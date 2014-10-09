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
package org.silverpeas.attachment.web;

import com.stratelia.webactiv.beans.admin.UserDetail;
import org.silverpeas.cache.service.CacheService;
import org.silverpeas.cache.service.CacheServiceFactory;
import org.silverpeas.cache.service.EhCacheService;
import org.silverpeas.util.StringUtil;
import com.stratelia.silverpeas.peasCore.MainSessionController;
import com.stratelia.silverpeas.peasCore.URLManager;
import org.silverpeas.util.GeneralPropertiesManager;
import org.silverpeas.util.ResourceLocator;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Random;
import java.util.UUID;
import javax.inject.Inject;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.lang3.CharEncoding;

/**
 * @author ehugonnet
 */
public class LaunchWebdavEdition extends HttpServlet {

  private static final ResourceLocator resources = new ResourceLocator(
      "org.silverpeas.util.attachment.Attachment", "");

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
    response.setContentType("application/x-java-jnlp-file");
    response.setHeader("Content-Disposition", "inline; filename=launch.jnlp");
    PrintWriter out = response.getWriter();
    try {
      MainSessionController mainSessionController = (MainSessionController) request.getSession().
          getAttribute(MainSessionController.MAIN_SESSION_CONTROLLER_ATT);
      if (mainSessionController == null) {
        String sessionTimeout = GeneralPropertiesManager.getString("sessionTimeout");
        getServletContext().getRequestDispatcher(sessionTimeout).forward(request, response);
        return;
      }
      UserDetail user = mainSessionController.getCurrentUserDetail();
      String token = generateAuthToken();
      CacheService cacheService = CacheServiceFactory.getApplicationCacheService();
      cacheService.put(token, user); // 12h by default of TTL
      prepareJNLP(request, out, user.getLogin(), token);
    } finally {
      out.close();
    }
  }

  private void prepareJNLP(HttpServletRequest request, PrintWriter out, String login,
      String token)
      throws UnsupportedEncodingException {
    out.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
    out.print("<jnlp spec=\"1.0+\" codebase=\"");
    out.print(URLManager.getServerURL(request));
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
    out.println("\t\t<jar href=\"commons-codec-1.7.jar\" download=\"eager\"/>");
    out.println("\t\t<jar href=\"commons-httpclient-3.1.jar\" download=\"eager\"/>");
    out.println("\t\t<jar href=\"jackrabbit-jcr-commons-2.8.0.jar\" download=\"eager\"/>");
    out.println("\t\t<jar href=\"jackrabbit-webdav-2.8.0.jar\" download=\"eager\"/>");
    out.println("\t\t<jar href=\"jcl-over-slf4j-1.7.7.jar\" download=\"eager\"/>");
    out.println("\t\t<jar href=\"slf4j-log4j12-1.7.7.jar\" download=\"eager\"/>");
    out.println("\t\t<jar href=\"slf4j-api-1.7.7.jar\" download=\"eager\"/>");
    out.println("\t\t<jar href=\"log4j-1.2.17.jar\" download=\"eager\"/>");
    out.println("\t\t<jar href=\"xml-apis-1.4.01.jar\" download=\"eager\"/>");
    out.println("\t</resources>");
    out.println("\t<application-desc main-class=\"org.silverpeas.openoffice.OfficeOnline\">");
    out.print("\t\t<argument>");
    out.print(URLEncoder.encode(request.getParameter("documentUrl"), CharEncoding.UTF_8));
    out.println("</argument>");
    out.print("\t\t<argument>");
    out.print(URLEncoder.encode(resources.getString("ms.office.installation.path"),
        CharEncoding.UTF_8));
    out.println("</argument>");
    out.print("\t\t<argument>");
    out.print(login);
    out.println("</argument>");
    out.print("\t\t<argument>");
    out.print(token);
    out.println("</argument>");
    out.print("\t\t<argument>");
    out.print(resources.getBoolean("disconnectedMode", false));
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

  private static String generateAuthToken() {
    String[] parts = UUID.randomUUID().toString().split("-");
    return StringUtil.asBase64(parts[parts.length - 1].getBytes());
  }
}
