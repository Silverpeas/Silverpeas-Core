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
 * FLOSS exception.  You should have recieved a copy of the text describing
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
/*--- formatted by Jindent 2.1, (www.c-lab.de/~jindent) 
 ---*/

/**
 * Title:        portlets
 * Description:  Enable portlet management in Silverpeas
 * Copyright:    Copyright (c) 2001
 * Company:      Stratelia
 * @author       Eric BURGEL
 * @version 1.0
 */

package com.stratelia.silverpeas.portlet.control;

import java.io.IOException;
import java.util.Enumeration;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.stratelia.silverpeas.peasCore.MainSessionController;
import com.stratelia.silverpeas.portlet.Portlet;
import com.stratelia.silverpeas.portlet.PortletException;
import com.stratelia.silverpeas.portlet.SpaceColumn;
import com.stratelia.silverpeas.portlet.SpaceModel;
import com.stratelia.silverpeas.portlet.SpaceModelFactory;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.util.GeneralPropertiesManager;
import com.stratelia.webactiv.util.exception.SilverpeasException;

/*
 * CVS Informations
 * 
 * $Id: PortletRequestRooter.java,v 1.3 2005/12/09 12:23:09 neysseri Exp $
 * 
 * $Log: PortletRequestRooter.java,v $
 * Revision 1.3  2005/12/09 12:23:09  neysseri
 * Nettoyage code + bug si composant introuvable lors de l'affichage des composants portletisables
 *
 * Revision 1.2  2003/11/28 13:58:58  tleroi
 * no message
 *
 * Revision 1.1.1.1  2002/08/06 14:47:52  nchaix
 * no message
 *
 * Revision 1.19  2002/04/19 06:15:30  tleroi
 * no message
 *
 * Revision 1.18  2002/04/05 10:37:24  emouchel
 * suppression des traces
 *
 * Revision 1.17  2002/04/03 08:26:02  emouchel
 * no message
 *
 * Revision 1.16  2002/04/02 08:37:45  emouchel
 * modification des actions sur les boutons
 * ajout d'une action admin1 permettant retour au main via goBack
 * ajout d'un go back sur l'action save
 *
 * Revision 1.15  2002/01/30 12:17:01  tleroi
 * no message
 *
 * Revision 1.14  2002/01/28 15:22:08  tleroi
 * Split clipboard and personalization
 *
 * Revision 1.13  2002/01/09 09:56:57  groccia
 * stabilisation lot2
 *
 */

/**
 * Class declaration
 * @author
 */
public class PortletRequestRooter extends HttpServlet {

  /**
   * <init>
   */
  public PortletRequestRooter() {
  }

  /**
   * doPost Controleur in the MVC paradigm
   * @param request parameter for doPost
   * @throws ServletException -
   */
  protected void doPost(HttpServletRequest request, HttpServletResponse response)
      throws ServletException {
    String destination;
    SpaceModel space;
    MainSessionController msc;

    try {
      HttpSession session = request.getSession();

      msc = (MainSessionController) session
          .getAttribute("SilverSessionController");
      // String function = request.getParameter("function") ;
      String function = request.getPathInfo().substring(1); // ,
      // request.getPathInfo().length()-4)
      // ;
      String spaceId = request.getParameter("spaceId");
      // Requested portlet state : min, max or empty (empty means normal)
      String portletState = request.getParameter("portletState");

      if (portletState == null) {
        portletState = "";

      }
      SilverTrace.info("portlet", "PortletRequestRooter.doPost",
          "root.MSG_GEN_ENTER_METHOD", "function=" + function + ", spaceId="
          + spaceId + ", portletState=" + portletState);
      // Compute the destination page

      if (msc == null) {
        SilverTrace.error("portlet", "PortletRequestRooter.doPost",
            "portlet.MSG_NOT_MSC");
        destination = GeneralPropertiesManager.getGeneralResourceLocator()
            .getString("sessionTimeout");

      } else if (function == null) {
        function = "null";
        destination = "/portlet/jsp/unknown.jsp?function='" + function + "'";

      } else if (function.equalsIgnoreCase("main")) {
        // Get the required space model from the Factory (from database)
        space = SpaceModelFactory.getSpaceModel(msc, spaceId, false);
        // Cache it in the current session for later use
        session.setAttribute("spaceModel", space);
        // Put the required space model in the request for the JSP page.
        request.setAttribute("spaceModel", space);
        destination = "/portlet/jsp/main.jsp";
      } else if (function.equalsIgnoreCase("column")) {
        // Retrieve the current space from the session
        space = (SpaceModel) session.getAttribute("spaceModel");
        if (space == null) {
          throw new PortletException("PortletRequestRooter.doPost",
              SilverpeasException.ERROR, "portlet.EX_SPACE_NULL");
          // get the column number to display
        }
        String sCol = request.getParameter("col");
        int col = Integer.parseInt(sCol);

        // set the "column" objet in the request for use by the JSP page
        request.setAttribute("column", space.getColumn(col));
        // prepare the forward to the JSP column
        destination = "/portlet/jsp/column.jsp"; // ?col=" + sCol ;

      } else if (function.equalsIgnoreCase("portlet")) {
        // get the portlet index
        String sIndex = request.getParameter("id");
        int index = Integer.parseInt(sIndex);

        // Retrieve the current space from the session
        space = (SpaceModel) session.getAttribute("spaceModel");
        // Extract the required portlet from the space
        Portlet portlet = space.getPortlets(index);

        // set the "portlet" object in the request for use by the JSP page
        request.setAttribute("portlet", portlet);
        destination = "/portlet/jsp/portlet" + portletState + ".jsp";

      } else if (function.equalsIgnoreCase("portletTitle")) {
        // get the portlet index
        String sIndex = request.getParameter("id");
        int index = Integer.parseInt(sIndex);

        // Retrieve the current space from the session
        space = (SpaceModel) session.getAttribute("spaceModel");
        // Extract the required portlet from the space
        Portlet portlet = space.getPortlets(index);

        // set the "portlet" object in the request for use by the JSP page
        request.setAttribute("portlet", portlet);
        destination = "/portlet/jsp/portletTitle" + portletState + ".jsp";

      } else if (function.equalsIgnoreCase("portletContent")) {
        // get the portlet index
        String sIndex = request.getParameter("id");
        int index = Integer.parseInt(sIndex);

        // Retrieve the current space from the session
        space = (SpaceModel) session.getAttribute("spaceModel");
        // Extract the required portlet from the space
        Portlet portlet = space.getPortlets(index);

        // set the "portlet" object in the request for use by the JSP page
        request.setAttribute("portlet", portlet);
        // destination ex :
        // "/Rquickinfo/jsp/Main.jsp?Space=WA8&Component=quickinfo24"
        destination = portlet.getRequestRooter() + portlet.getContentUrl()
            + "?Space=WA" + spaceId + "&Component="
            + portlet.getComponentName() + portlet.getComponentInstanceId()
            + "&portletState=" + portletState;
      } else if (function.equalsIgnoreCase("portletFooter")) {
        destination = "/portlet/jsp/portletFooter" + portletState + ".jsp";

      } else if (function.equalsIgnoreCase("state")) {
        // Retrieve the current space from the session
        space = (SpaceModel) session.getAttribute("spaceModel");
        if (space == null) {
          throw new PortletException("PortletRequestRooter.doPost",
              SilverpeasException.ERROR, "portlet.EX_SPACE_NULL");
          // get the portlet index
        }
        String sIndex = request.getParameter("id");
        int index = Integer.parseInt(sIndex);
        // Extract the required portlet from the space
        Portlet portlet = space.getPortlets(index);

        // Set the new state for this portlet (min, max or normal)
        portlet.setState(portletState);
        // Save the current portlet State for next session

        SpaceModelFactory.portletSaveState(space, portlet);
        // set the "column" objet in the request for use by the JSP page
        request.setAttribute("column", space.getColumn(portlet
            .getColumnNumber()));
        destination = "/portlet/jsp/column.jsp"; // ?col=" +
        // portlet.getColumnNumber();

      } else if (function.equalsIgnoreCase("standard")) {
        // Retrieve the current space from the session
        space = (SpaceModel) session.getAttribute("spaceModel");
        if (space == null) {
          throw new PortletException("PortletRequestRooter.doPost",
              SilverpeasException.ERROR, "portlet.EX_SPACE_NULL");
          // get the portlet index
        }
        String sIndex = request.getParameter("id");
        int index = Integer.parseInt(sIndex);
        // Extract the required portlet from the space
        Portlet portlet = space.getPortlets(index);

        // Set the new state for this portlet (min, max or normal)
        portlet.setState(Portlet.NORMAL);
        // Save the current portlet State for next session

        SpaceModelFactory.portletSaveState(space, portlet);
        // Put the required space model in the request for the JSP page.
        request.setAttribute("spaceModel", space);
        destination = "/portlet/jsp/main.jsp";

        // *******************************************
        // Administration specific actions
        // *******************************************

        // main action for administration
      } else if (function.equalsIgnoreCase("admin")) {
        // Retrieve the current space from the database
        space = SpaceModelFactory.getSpaceModel(msc, spaceId, true);
        // Cache it in the current session for later use
        session.setAttribute("spaceModel", space);
        // Put the required space model in the request for the JSP page.
        request.setAttribute("spaceModel", space);

        destination = "/portlet/jsp/admin/adminPortlet.jsp";

      }

      else if (function.equalsIgnoreCase("admin1")) {
        // Retrieve the current space from the database
        space = SpaceModelFactory.getSpaceModel(msc, spaceId, true);
        // Cache it in the current session for later use
        session.setAttribute("spaceModel", space);
        // Put the required space model in the request for the JSP page.
        request.setAttribute("spaceModel", space);
        request
            .setAttribute("fullURL", GeneralPropertiesManager
            .getGeneralResourceLocator().getString("ApplicationURL")
            + "/RjobStartPagePeas/jsp/SetPortlet?spaceId="
            + space.getSpaceId());
        destination = "/jobStartPagePeas/jsp/goBack.jsp";

      } else if (function.equalsIgnoreCase("portletAdminBarre")) {
        // Retrieve the current space from the session
        space = (SpaceModel) session.getAttribute("spaceModel");
        if (space == null) {
          throw new PortletException("PortletRequestRooter.doPost",
              SilverpeasException.ERROR, "portlet.EX_SPACE_NULL");
          // Put the required space model in the request for the JSP page.
        }
        request.setAttribute("spaceModel", space);
        destination = "/portlet/jsp/admin/portletAdminBarre.jsp";

      } else if (function.equalsIgnoreCase("adminMain")) {
        // Retrieve the current space from the session
        space = (SpaceModel) session.getAttribute("spaceModel");
        if (space == null) {
          // throw new ServletException("no spaceModel in session");
          throw new PortletException("PortletRequestRooter.doPost",
              SilverpeasException.ERROR, "portlet.EX_SPACE_NULL");
          // Put the required space model in the request for the JSP page.
        }
        request.setAttribute("spaceModel", space);
        destination = "/portlet/jsp/admin/main.jsp";

      } else if (function.equalsIgnoreCase("columnSet")) {
        destination = "/portlet/jsp/admin/columnSet.jsp";

      } else if (function.equalsIgnoreCase("colHeader")) {
        // Retrieve the current space from the session
        space = (SpaceModel) session.getAttribute("spaceModel");
        // get the column number to display
        String sCol = request.getParameter("col");
        int col = Integer.parseInt(sCol);
        // Retrieve the current SpaceColumn
        SpaceColumn sc = space.getColumn(col);

        // set the "column" objet in the request for use by the JSP page
        request.setAttribute("column", sc);
        // prepare the forward to the JSP column
        destination = "/portlet/jsp/admin/colHeader.jsp";

      } else if (function.equalsIgnoreCase("adminColumn")) {
        // Retrieve the current space from the session
        space = (SpaceModel) session.getAttribute("spaceModel");
        if (space == null) {
          // throw new ServletException("no spaceModel in session");
          throw new PortletException("PortletRequestRooter.doPost",
              SilverpeasException.ERROR, "portlet.EX_SPACE_NULL");
          // get the column number to display
        }
        String sCol = request.getParameter("col");
        int col = Integer.parseInt(sCol);

        // set the "column" objet in the request for use by the JSP page
        request.setAttribute("column", space.getColumn(col));
        // prepare the forward to the JSP column
        destination = "/portlet/jsp/admin/column.jsp";

      } else if (function.equalsIgnoreCase("adminPortletDummy")) {
        // get the portlet index
        String sIndex = request.getParameter("id");
        int index = Integer.parseInt(sIndex);

        // Retrieve the current space from the session
        space = (SpaceModel) session.getAttribute("spaceModel");
        // Extract the required portlet from the space
        Portlet portlet = space.getPortlets(index);

        // set the "portlet" object in the request for use by the JSP page
        request.setAttribute("portlet", portlet);
        destination = "/portlet/jsp/admin/portletDummyComponent.jsp";
      } else if (function.equalsIgnoreCase("adminPortlet")) {
        // get the portlet index
        String sIndex = request.getParameter("id");
        int index = Integer.parseInt(sIndex);

        // Retrieve the current space from the session
        space = (SpaceModel) session.getAttribute("spaceModel");
        // Extract the required portlet from the space
        Portlet portlet = space.getPortlets(index);

        // set the "portlet" object in the request for use by the JSP page
        request.setAttribute("portlet", portlet);
        destination = "/portlet/jsp/admin/portlet" + portletState + ".jsp";

      } else if (function.equalsIgnoreCase("adminPortletTitle")) {
        String sRow = request.getParameter("row");
        int row = Integer.parseInt(sRow);
        String sCol = request.getParameter("col");
        int col = Integer.parseInt(sCol);

        // Retrieve the current space from the session
        space = (SpaceModel) session.getAttribute("spaceModel");
        // Retrieve the required portlet from the space
        Portlet portlet = space.getPortlet(col, row);

        // set the "portlet" object in the request for use by the JSP page
        request.setAttribute("portlet", portlet);
        destination = "/portlet/jsp/admin/portletTitle.jsp";

        // Liste des composants portletises
      } else if (function.equalsIgnoreCase("componentList")) {
        destination = "/portlet/jsp/admin/componentList.jsp";

      } else if (function.equalsIgnoreCase("portletList")) {
        // Retrieve the current space from the session
        space = (SpaceModel) session.getAttribute("spaceModel");
        request.setAttribute("portletList", SpaceModelFactory
            .getPortletList(space));
        destination = "/portlet/jsp/admin/portletList.jsp";

      } else if (function.equalsIgnoreCase("addColumnList")) {
        // Retrieve the current space from the session
        space = (SpaceModel) session.getAttribute("spaceModel");
        request.setAttribute("portletList", SpaceModelFactory
            .getPortletList(space));
        destination = "/portlet/jsp/admin/addColumnList.jsp";

        // Add a portlet to the specified column
      } else if (function.equalsIgnoreCase("addPortlet")) {
        String sInstanceId = request.getParameter("instanceId");
        int instanceId = Integer.parseInt(sInstanceId);
        String sCol = request.getParameter("col");
        int col = Integer.parseInt(sCol);

        // Retrieve the current space from the session
        space = (SpaceModel) session.getAttribute("spaceModel");

        Portlet portlet = SpaceModelFactory.getPortlet(instanceId);
        // add the portlet to the current user space
        space.addPortlet(col, portlet);
        // set the "column" objet in the request for use by the JSP page
        request.setAttribute("column", space.getColumn(col));

        destination = "/portlet/jsp/admin/column.jsp";

        // Add a portlet to a new column
      } else if (function.equalsIgnoreCase("addColumn")) {
        String sInstanceId = request.getParameter("instanceId");
        int instanceId = Integer.parseInt(sInstanceId);

        // Retrieve the current space from the session
        space = (SpaceModel) session.getAttribute("spaceModel");
        // Put the required space model in the request for the JSP page.
        request.setAttribute("spaceModel", space);
        if (space == null) {
          throw new PortletException("PortletRequestRooter.doPost",
              SilverpeasException.ERROR, "portlet.EX_SPACE_NULL");
        }

        Portlet portlet = SpaceModelFactory.getPortlet(instanceId);
        // add the portlet to the current user space
        space.addPortlet(-1, portlet);
        destination = "/portlet/jsp/admin/main.jsp";

      } else if (function.equalsIgnoreCase("removePortlet")) {
        String sRow = request.getParameter("row");
        int row = Integer.parseInt(sRow);

        String sCol = request.getParameter("col");
        int col = Integer.parseInt(sCol);

        // Retrieve the current space from the session
        space = (SpaceModel) session.getAttribute("spaceModel");
        space.removePortlet(col, row);
        if (request.getParameter("lastPortlet").equalsIgnoreCase("yes")) {
          // Put the required space model in the request for the JSP page.
          request.setAttribute("spaceModel", space);
          destination = "/portlet/jsp/admin/main.jsp";
        } else {
          // set the "column" objet in the request for use by the JSP page
          request.setAttribute("column", space.getColumn(col));
          destination = "/portlet/jsp/admin/column.jsp";
        }

      } else if (function.equalsIgnoreCase("save")) {
        // Retrieve the current space from the session
        space = (SpaceModel) session.getAttribute("spaceModel");
        SpaceModelFactory.saveSpaceModel(space);

        // Retrieve the current space from the database
        space = SpaceModelFactory.getSpaceModel(msc, spaceId, true);
        // Cache it in the current session for later use
        session.setAttribute("spaceModel", space);
        // Put the required space model in the request for the JSP page.
        request.setAttribute("spaceModel", space);
        request
            .setAttribute("fullURL", GeneralPropertiesManager
            .getGeneralResourceLocator().getString("ApplicationURL")
            + "/RjobStartPagePeas/jsp/SetPortlet?spaceId="
            + space.getSpaceId());
        destination = "/jobStartPagePeas/jsp/goBack.jsp";

      } else if ((function.equalsIgnoreCase("down"))
          || (function.equalsIgnoreCase("up"))) {
        // move the portlet one step up or down
        // Retrieve the parameters
        String sCol = request.getParameter("col");
        int col = Integer.parseInt(sCol);
        String sRow = request.getParameter("row");
        int row = Integer.parseInt(sRow);

        // Retrieve the current space from the session
        space = (SpaceModel) session.getAttribute("spaceModel");
        // Retrieve the portlet
        Portlet portlet = space.getPortlet(col, row);

        // move the portlet
        if (function.equalsIgnoreCase("down")) {
          space.addPortlet(col, row + 2, portlet);
          space.removePortlet(col, row);
        } else {
          if (row > 0) {
            space.addPortlet(col, row - 1, portlet);
            space.removePortlet(col, row + 1);
          }
        }
        // set the "column" objet in the request for use by the JSP page
        request.setAttribute("column", space.getColumn(col));
        destination = "/portlet/jsp/admin/column.jsp";

      } else if ((function.equalsIgnoreCase("left"))
          || (function.equalsIgnoreCase("right"))) {
        // move the portlet one step left or right
        // Retrieve the parameters
        String sCol = request.getParameter("col");
        int col = Integer.parseInt(sCol);
        String sRow = request.getParameter("row");
        int row = Integer.parseInt(sRow);

        // Retrieve the current space from the session
        space = (SpaceModel) session.getAttribute("spaceModel");
        // Retrieve the portlet
        Portlet portlet = space.getPortlet(col, row);

        // move the portlet
        if (function.equalsIgnoreCase("right")) {
          space.addPortlet(col + 1, row, portlet);
        } else {
          space.addPortlet(col - 1, row, portlet);
        }
        space.removePortlet(col, row);
        // Put the required space model in the request for the JSP page.
        request.setAttribute("spaceModel", space);
        destination = "/portlet/jsp/admin/main.jsp";

      } else if (function.equalsIgnoreCase("setColSize")) {
        // Retrieve the parameters
        String sCol = request.getParameter("col");
        int col = Integer.parseInt(sCol);

        // Retrieve the current space from the session
        space = (SpaceModel) session.getAttribute("spaceModel");
        // Retrieve the current column
        SpaceColumn sc = space.getColumn(col);

        // Retrieve the parameters to set to column with
        String sWidth = request.getParameter("textWidth");

        // set the column width
        sc.setColumnWidth(sWidth);

        // set the "column" objet in the request for use by the JSP page
        request.setAttribute("column", sc);

        destination = "/portlet/jsp/admin/colHeader.jsp";

        // **********************************
        // Debug Code : to be moved elsewhere
        // **********************************

      } else if (function.equalsIgnoreCase("debug")) {
        destination = null;
        try {
          ServletOutputStream out = response.getOutputStream();

          out.println("Objets in the request<p>");
          out.println("<table>");
          out.println("  <tr>");
          out.println("    <td>Name</td><td>type</td><td>Content</td>");
          out.println("  </tr>");
          out.println("");
          Enumeration enume = request.getAttributeNames();

          while (enume.hasMoreElements()) {
            String name = (String) enume.nextElement();
            Object obj = request.getAttribute(name);

            out.println("  <tr>");
            out.print("    <td>" + name + "</td>");
            out.print("<td>" + obj.getClass().getName() + "</td>");
            out.println("<td>" + obj.toString() + "</td>");
            out.println("  </tr>");
          }
          out.println("</table>");

          out.println("Objets in the session<p>");
          out.println("<table>");
          out.println("  <tr>");
          out.println("    <td>Name</td><td>type</td><td>Content</td>");
          out.println("  </tr>");
          out.println("");
          enume = session.getAttributeNames();
          while (enume.hasMoreElements()) {
            String name = (String) enume.nextElement();
            Object obj = session.getAttribute(name);

            out.println("  <tr>");
            out.print("    <td>" + name + "</td>");
            out.print("<td>" + obj.getClass().getName() + "</td>");
            out.println("<td>" + obj.toString() + "</td>");
            out.println("  </tr>");
          }
          out.println("</table>");

        } catch (IOException e) {
          SilverTrace.error("portlet", "PortletRequestRooter.doPost",
              "portlet.MSG_IO_ERROR");
        }

      } else if (function.endsWith(".htm")) {
        destination = "/portlet/jsp/" + function;

      } else {
        destination = "/portlet/jsp/unknown.jsp?function='" + function + "'";
      }

      // forward the request to the destination page
      try {
        if (destination != null) {
          getServletConfig().getServletContext().getRequestDispatcher(
              destination).forward(request, response);
        }
      }

      catch (IOException e) {
        throw new PortletException("PortletRequestRooter.doPost",
            SilverpeasException.ERROR, "portlet.EX_IO_ERROR_DESTINATION",
            "Destination=" + destination, e);
      } catch (ServletException e) {
        throw new PortletException("PortletRequestRooter.doPost",
            SilverpeasException.ERROR, "portlet.EX_S_ERROR_DESTINATION",
            "Destination=" + destination, e);
      }
    }

    catch (PortletException e) {
      request.setAttribute("javax.servlet.jsp.jspException", e);
      try {
        getServletConfig().getServletContext().getRequestDispatcher(
            "/admin/jsp/errorpagePopup.jsp").forward(request, response);
      } catch (IOException ioe) {
        SilverTrace.error("portlet", "PortletRequestRooter.doPost",
            "portlet.MSG_CANT_GET_DESTINATION", e);
        SilverTrace.fatal("portlet", "PortletRequestRooter.doPost",
            "portlet.MSG_CANT_GET_ERRORPAGE", ioe);
        destination = GeneralPropertiesManager.getGeneralResourceLocator()
            .getString("sessionTimeout");
        try {
          getServletConfig().getServletContext().getRequestDispatcher(
              destination).forward(request, response);
        } catch (IOException ioe2) {
          SilverTrace.fatal("portlet", "PortletRequestRooter.doPost",
              "portlet.MSG_CANT_GET_LOGINPAGE", ioe2);
        }
      }
    }
  }

  /**
   * doGet
   * @param request parameter for doGet
   * @throws ServletException -
   */
  public void doGet(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {
    doPost(request, response);
  }

  /**
   * getSessionControlBeanName
   * @return the returned String
   */
  public String getSessionControlBeanName() {
    return "portlet";
  }

}
