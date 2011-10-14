/*
 * Copyright (C) 2000 - 2011 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU Affero General Public License as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of the GPL, you may
 * redistribute this Program in connection with Free/Libre Open Source Software ("FLOSS")
 * applications as described in Silverpeas's FLOSS exception. You should have recieved a copy of the
 * text describing the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.com/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License along with this program.
 * If not, see <http://www.gnu.org/licenses/>.
 *
 */
package com.silverpeas.core;

import com.silverpeas.util.StringUtil;
import com.stratelia.silverpeas.peasCore.MainSessionController;
import com.stratelia.silverpeas.peasCore.URLManager;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.beans.admin.UserDetail;
import com.stratelia.webactiv.util.viewGenerator.html.GraphicElementFactory;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.PrintWriter;

import static com.silverpeas.util.MimeTypes.SERVLET_HTML_CONTENT_TYPE;
import static javax.servlet.http.HttpServletResponse.*;

/**
 *
 * @author ehugonnet
 */
public class AutoRedirectServlet extends HttpServlet {

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
    prepareResponseHeader(response);
    String strGoTo = request.getParameter("goto");
    String domainId = request.getParameter("domainId");
    String componentGoTo = request.getParameter("ComponentId");
    String spaceGoTo = request.getParameter("SpaceId");
    String attachmentGoTo = request.getParameter("AttachmentId");
    HttpSession session = request.getSession();
    PrintWriter out = response.getWriter();
    try {

      String mainFrameParams = "";
      String componentId = null;
      String spaceId = null;
      if (strGoTo != null) {
        session.setAttribute("gotoNew", strGoTo);
        String urlToParse = strGoTo;
        if (strGoTo.startsWith("/RpdcSearch/")) {
          int indexOf = urlToParse.indexOf("&componentId=");
          componentId = urlToParse.substring(indexOf + 13, urlToParse.length());
        } else {
          urlToParse = urlToParse.substring(1); //remove first "/"
          int indexBegin = urlToParse.indexOf('/') + 1;
          int indexEnd = urlToParse.indexOf('/', indexBegin);
          componentId = urlToParse.substring(indexBegin, indexEnd);
          //Agenda
          if (strGoTo.startsWith("/Ragenda/")) {
            componentId = null;
          }
        }
        if(StringUtil.isDefined(componentId) && !StringUtil.isAlpha(componentId)) {
          mainFrameParams = "?ComponentIdFromRedirect=" + componentId;
          session.setAttribute("RedirectToComponentId", componentId);
        }
      } else if (componentGoTo != null) {
        componentId = componentGoTo;
        mainFrameParams = "?ComponentIdFromRedirect=" + componentId;
        session.setAttribute("RedirectToComponentId", componentId);
        if (attachmentGoTo != null) {
          String foreignId = request.getParameter("ForeignId");
          String type = request.getParameter("Mapping");

          //Contruit l'url vers l'objet du composant contenant le fichier
          strGoTo = URLManager.getURL(null, componentId) + "searchResult?Type=Publication&Id=" + foreignId;
          session.setAttribute("gotoNew", strGoTo);

          //Ajoute l'id de l'attachment pour ouverture automatique
          session.setAttribute("RedirectToAttachmentId", attachmentGoTo);
          session.setAttribute("RedirectToMapping", type);
        }
      } else if (spaceGoTo != null) {
        spaceId = spaceGoTo;
        session.setAttribute("RedirectToSpaceId", spaceId);
      }

      SilverTrace.info("authentication", "autoRedirect.jsp", "root.MSG_GEN_PARAM_VALUE",
              "componentId = " + componentId + ", spaceId = " + spaceId);

      MainSessionController mainController = (MainSessionController) session.getAttribute(
              MainSessionController.MAIN_SESSION_CONTROLLER_ATT);
      GraphicElementFactory gef = (GraphicElementFactory) session.getAttribute(GraphicElementFactory.GE_FACTORY_SESSION_ATT);

      //The user is either not connector or as the anonymous user. He comes back to the login page.
      if (mainController == null || (gef != null && UserDetail.isAnonymousUser(mainController.
              getUserId()) && (strGoTo == null && componentGoTo == null && spaceGoTo == null))) {
        String loginUrl = response.encodeRedirectURL(
                URLManager.getApplicationURL() + "/Login.jsp?DomainId=" + domainId);
        out.println("<script>");
        out.print("top.location=\"");
        out.print(loginUrl);
        out.println("\";");
        out.println("</script>");
      } else {
        if (isAccessibleComponent(componentId, mainController) || isAccessibleSpace(spaceId, mainController)) {
          response.sendRedirect(URLManager.getApplicationURL() + "/admin/jsp/accessForbidden.jsp");
        } else if (mainController.isAppInMaintenance() && !mainController.getCurrentUserDetail().
                isAccessAdmin()) {
          out.println("<script>");
          out.println("top.location=\"admin/jsp/appInMaintenance.jsp\";");
          out.println("</script>");
        } else {
          out.println("<script>");
          out.print("top.location=\"admin/jsp/");
          out.print(gef.getLookFrame());
          out.print(mainFrameParams);
          out.println("\";");
          out.println("</script>");
        }
      }
    } finally {
      out.close();
    }
  }

  private void prepareResponseHeader(HttpServletResponse response) {
    response.setContentType(SERVLET_HTML_CONTENT_TYPE);
    response.setHeader("Expires", "Tue, 21 Dec 1993 23:59:59 GMT");
    response.setHeader("Pragma", "no-cache");
    response.setHeader("Cache-control", "no-cache");
    response.setHeader("Last-Modified", "Fri, Jan 25 2099 23:59:59 GMT");
    response.setStatus(SC_CREATED);
  }

  private boolean isAccessibleSpace(String spaceId, MainSessionController mainController) {
    return StringUtil.isDefined(spaceId)&& !mainController.getOrganizationController().
    isSpaceAvailable(spaceId, mainController.getUserId());
  }

  private boolean isAccessibleComponent(String componentId, MainSessionController mainController) {
    return StringUtil.isDefined(componentId) && !StringUtil.isAlpha(componentId) && !mainController
        .getOrganizationController().isComponentAvailable(componentId, mainController.getUserId());
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
