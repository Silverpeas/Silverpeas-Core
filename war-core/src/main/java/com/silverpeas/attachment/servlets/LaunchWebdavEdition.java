/*
 * Copyright (C) 2000 - 2011 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection withWriter Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have recieved a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.silverpeas.attachment.servlets;

import com.stratelia.silverpeas.peasCore.MainSessionController;
import com.stratelia.silverpeas.peasCore.URLManager;
import com.stratelia.webactiv.util.GeneralPropertiesManager;
import com.stratelia.webactiv.util.ResourceLocator;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * @author ehugonnet
 */
public class LaunchWebdavEdition extends HttpServlet {

  private final static byte[] KEY = new byte[]{-23, -75, -2, -17, 79, -94, -125, -14};
  private final static String DIGITS = "0123456789abcdef";
  private static final ResourceLocator resources = new ResourceLocator(
      "com.stratelia.webactiv.util.attachment.Attachment", "");

  /**
   * Processes requests for both HTTP
   * <code>GET</code> and
   * <code>POST</code> methods.
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
        String sessionTimeout = GeneralPropertiesManager.getGeneralResourceLocator().getString(
            "sessionTimeout");
        getServletContext().getRequestDispatcher(sessionTimeout).forward(request, response);
      }
      String login = mainSessionController.getCurrentUserDetail().getLogin();
      String password = (String) request.getSession().getAttribute("Silverpeas_pwdForHyperlink");
      Cipher cipher = Cipher.getInstance("DES");
      cipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(KEY, "DES"));
      byte[] cipherText = cipher.doFinal(password.getBytes("UTF-8"));
      String encPassword = toHex(cipherText);
      prepareJNLP(request, out, login, encPassword);
    } catch (NoSuchAlgorithmException ex) {
      throw new ServletException(ex);
    } catch (NoSuchPaddingException ex) {
      throw new ServletException(ex);
    } catch (InvalidKeyException ex) {
      throw new ServletException(ex);
    } catch (IllegalBlockSizeException ex) {
      throw new ServletException(ex);
    } catch (BadPaddingException ex) {
      throw new ServletException(ex);
    } finally {
      out.close();
    }
  }

  /**
   * Return length many bytes of the passed in byte array as a hex string.
   *
   * @param data the bytes to be converted.
   * @return a hex representation of length bytes of data.
   */
  String toHex(byte[] data) {
    StringBuilder buf = new StringBuilder();
    for (int i = 0; i != data.length; i++) {
      int v = data[i] & 0xff;
      buf.append(DIGITS.charAt(v >> 4));
      buf.append(DIGITS.charAt(v & 0xf));
    }
    return buf.toString();
  }

  private void prepareJNLP(HttpServletRequest request, PrintWriter out, String login,
      String password) throws UnsupportedEncodingException {
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
    out.println("\t\t<icon href=\"logo.PNG\" kind=\"default\"/>");
    out.println("\t\t<icon href=\"logo.PNG\" kind=\"splash\"/>");
    out.println("\t\t<offline-allowed/>");
    out.println("\t</information>");
    out.println("\t<security>");
    out.println("\t\t<all-permissions/>");
    out.println("\t</security>");
    out.println("\t<update check=\"timeout\" policy=\"always\"/>");
    out.println("\t<resources>");
    out.println("\t\t<j2se href=\"http://java.sun.com/products/autodl/j2se\" version=\"1.6+\" />");
    out.println("\t\t<jar href=\"OpenOfficeLauncher.jar\" download=\"eager\"/>");
    out.println("\t\t<jar href=\"xercesImpl-2.10.0.jar\" download=\"eager\"/>");
    out.println("\t\t<jar href=\"commons-codec-1.5.jar\" download=\"eager\"/>");
    out.println("\t\t<jar href=\"commons-httpclient-3.1.jar\" download=\"eager\"/>");
    out.println("\t\t<jar href=\"jackrabbit-jcr-commons-2.2.10.jar\" download=\"eager\"/>");
    out.println("\t\t<jar href=\"jackrabbit-webdav-2.2.10.jar\" download=\"eager\"/>");
    out.println("\t\t<jar href=\"jcl-over-slf4j-1.5.6.jar\" download=\"eager\"/>");
    out.println("\t\t<jar href=\"slf4j-log4j12-1.5.6.jar\" download=\"eager\"/>");
    out.println("\t\t<jar href=\"slf4j-api-1.5.6.jar\" download=\"eager\"/>");
    out.println("\t\t<jar href=\"log4j-1.2.16.jar\" download=\"eager\"/>");
    out.println("\t\t<jar href=\"xml-apis-1.4.01.jar\" download=\"eager\"/>");
    out.println("\t</resources>");
    out.println("\t<application-desc main-class=\"com.silverpeas.openoffice.Launcher\">");
    out.print("\t\t<argument>");
    out.print(URLEncoder.encode(request.getParameter("documentUrl"), "UTF-8"));
    out.println("</argument>");
    out.print("\t\t<argument>");
    out.print(URLEncoder.encode(resources.getString("ms.office.installation.path"), "UTF-8"));
    out.println("</argument>");
    out.print("\t\t<argument>");
    out.print(URLEncoder.encode(login, "UTF-8"));
    out.println("</argument>");
    out.print("\t\t<argument>");
    out.print(URLEncoder.encode(password, "UTF-8"));
    out.println("</argument>");
    out.println("\t</application-desc>");
    out.println(" </jnlp>");
  }
  // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">

  /**
   * Handles the HTTP
   * <code>GET</code> method.
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
   * Handles the HTTP
   * <code>POST</code> method.
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
    return "Short description";
  }// </editor-fold>
}
