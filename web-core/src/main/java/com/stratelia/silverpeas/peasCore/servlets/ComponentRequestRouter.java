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

package com.stratelia.silverpeas.peasCore.servlets;

import com.silverpeas.util.StringUtil;
import com.stratelia.silverpeas.peasCore.ComponentContext;
import com.stratelia.silverpeas.peasCore.ComponentSessionController;
import com.stratelia.silverpeas.peasCore.MainSessionController;
import com.stratelia.silverpeas.peasCore.PeasCoreException;
import com.stratelia.silverpeas.peasCore.SessionManager;
import com.stratelia.silverpeas.peasCore.SilverpeasWebUtil;
import com.stratelia.silverpeas.silverstatistics.control.SilverStatisticsManager;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.silverpeas.util.ResourcesWrapper;
import com.stratelia.webactiv.util.GeneralPropertiesManager;
import com.stratelia.webactiv.util.exception.SilverpeasException;
import com.stratelia.webactiv.util.viewGenerator.html.GraphicElementFactory;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.Date;

import static com.stratelia.silverpeas.peasCore.MainSessionController.MAIN_SESSION_CONTROLLER_ATT;

public abstract class ComponentRequestRouter extends HttpServlet {

  private static final long serialVersionUID = -8055016885655445663L;
  private static final SilverpeasWebUtil webUtil = new SilverpeasWebUtil();

  /**
   * This method has to be implemented in the component request Router class. returns the session
   * control bean name to be put in the request object ex : for almanach, returns "almanach"
   * @return the name of the session controller.
   */
  public abstract String getSessionControlBeanName();

  /**
   * This method has to be implemented by the component request Router it has to compute a
   * destination page
   * @param function The entering request function (ex : "Main.jsp", when accessing
   * "http://localhost/webactiv/Ralmanach/jsp/Main.jsp")
   * @param componentSC The component Session Controller, build and initialised.
   * @param request The entering request. The request Router need it to get parameters
   * @return The complete destination URL for a forward (ex :
   * "/almanach/jsp/almanach.jsp?flag=user")
   */
  public abstract String getDestination(String function,
      ComponentSessionController componentSC, HttpServletRequest request);

  public abstract ComponentSessionController createComponentSessionController(
      MainSessionController mainSessionCtrl, ComponentContext componentContext);

  @Override
  public void doPost(HttpServletRequest SPrequest, HttpServletResponse response) {
    // SilverHttpServletRequest SPrequest = new
    // SilverHttpServletRequest(request);

    String destination = computeDestination(SPrequest, response);
    SilverTrace.debug("peasCore", "RR", "root.MSG_GEN_PARAM_VALUE",
        "response = " + response);
    if (!StringUtil.isDefined(destination)) {
      destination = GeneralPropertiesManager.getGeneralResourceLocator()
          .getString("sessionTimeout");
    }
    redirectService(SPrequest, response, destination);

  }

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response)
      throws ServletException {
    doPost(request, response);
  }

  private String computeDestination(HttpServletRequest request, HttpServletResponse response) {
    String destination = null;
    request.getParameterNames(); // flush for orion
    // get the main session controller
    HttpSession session = request.getSession(true);
    MainSessionController mainSessionCtrl = (MainSessionController) session.getAttribute(
        MAIN_SESSION_CONTROLLER_ATT);
    if (mainSessionCtrl == null) {
      SilverTrace.warn("peasCore", "ComponentRequestRouter.computeDestination",
          "root.MSG_GEN_SESSION_TIMEOUT", "NewSessionId=" + session.getId());
      return GeneralPropertiesManager.getGeneralResourceLocator().getString("sessionTimeout");
    }
    // App in Maintenance ?
    SilverTrace.debug("peasCore", "ComponentRequestRouter.computeDestination()",
        "root.MSG_GEN_PARAM_VALUE", "appInMaintenance = "
        + String.valueOf(mainSessionCtrl.isAppInMaintenance()));
    SilverTrace.debug("peasCore","ComponentRequestRouter.computeDestination()",
        "root.MSG_GEN_PARAM_VALUE", "type User = " + mainSessionCtrl.getUserAccessLevel());
    if (mainSessionCtrl.isAppInMaintenance()
        && !mainSessionCtrl.getCurrentUserDetail().isAccessAdmin()) {
      return GeneralPropertiesManager.getGeneralResourceLocator().getString("redirectAppInMaintenance");
    }

    // Get the space id and the component id required by the user
    String[] context = getComponentId(request, mainSessionCtrl);
    String spaceId = context[0];
    String componentId = context[1];
    String function = context[2];

    SilverTrace.debug("peasCore", "ComponentRequestRouter.computeDestination()",
        "root.MSG_GEN_PARAM_VALUE", "spaceId= " + spaceId);
    SilverTrace.debug("peasCore", "ComponentRequestRouter.computeDestination()",
        "root.MSG_GEN_PARAM_VALUE", "type User = " + mainSessionCtrl.getUserAccessLevel());

    boolean isSpaceInMaintenance = mainSessionCtrl.isSpaceInMaintenance(spaceId);
    SilverTrace.debug("peasCore", "ComponentRequestRouter.computeDestination()",
        "root.MSG_GEN_PARAM_VALUE", "spaceIsMaintenance = " + isSpaceInMaintenance);

    // Space in Maintenance ?
    if (isSpaceInMaintenance && !mainSessionCtrl.getCurrentUserDetail().isAccessAdmin()) {
      return "/admin/jsp/spaceInMaintenance.jsp";
    }

    ComponentSessionController component = getComponentSessionController(
        session, componentId);
    if (component == null) {
      // check that the user has an acces to this component instance
      boolean bCompoAllowed = isUserAllowed(mainSessionCtrl, componentId);
      if (!bCompoAllowed) {
        SilverTrace.warn("peasCore",
            "ComponentRequestRouter.computeDestination",
            "peasCore.MSG_USER_NOT_ALLOWED", "User="
            + mainSessionCtrl.getUserId() + " | componentId=" + componentId
            + " | spaceId=" + spaceId);
        destination = GeneralPropertiesManager.getGeneralResourceLocator()
            .getString("accessForbidden", "/admin/jsp/accessForbidden.jsp");
        return destination;
      }
      component = setComponentSessionController(session, mainSessionCtrl,
          spaceId, componentId);
    }

    ResourcesWrapper resources = new ResourcesWrapper(component.getMultilang(),
        component.getIcon(), component.getSettings(), component.getLanguage());
    request.setAttribute("resources", resources);
    request.setAttribute("browseContext", new String[] {
        component.getSpaceLabel(), component.getComponentLabel(),
        component.getSpaceId(), component.getComponentId(),
        component.getComponentUrl() });
    request.setAttribute("myComponentURL", GeneralPropertiesManager
        .getGeneralResourceLocator().getString("ApplicationURL")
        + component.getComponentUrl());

    if (!"Idle.jsp".equals(function) && !"IdleSilverpeasV5.jsp".equals(function) &&
        !"ChangeSearchTypeToExpert".equals(function) && !"markAsRead".equals(function)) {
      GraphicElementFactory gef =
          (GraphicElementFactory) session
          .getAttribute(GraphicElementFactory.GE_FACTORY_SESSION_ATT);
      gef.setComponentId(component.getComponentId());
      gef.setMainSessionController(mainSessionCtrl);
    }

    // notify silverstatistics
    if (function.equals("Main") || function.startsWith("searchResult")
        || function.startsWith("portlet") || function.equals("GoToFilesTab")) {
      // only for instanciable components
      if (componentId != null) {
        SilverStatisticsManager.getInstance().addStatAccess(
            component.getUserId(), new Date(), component.getComponentName(),
            component.getSpaceId(), component.getComponentId());
      }
    }
    // retourne la page jsp de destination et place dans la request les objets
    // utilises par cette page
    destination = getDestination(function, component, request);
    SilverTrace.info("couverture",
        "ComponentRequestRouter.computeDestination()", "couverture.MSG_RR_JSP",
        "destination = '" + destination + "'");

    // Update last accessed time depending on destination
    // see ClipboardRequestRouter
    // Except for notifications
    if (!("POPUP".equals(getSessionControlBeanName()) && function
        .startsWith("ReadMessage"))) {
      updateSessionManagement(session, destination);
    }

    request.setAttribute(getSessionControlBeanName(), component);

    return destination;

  }

  public void updateSessionManagement(HttpSession session, String destination) {
    SilverTrace.info("peasCore",
        "ComponentRequestRouter.updateSessionManagement",
        "root.MSG_GEN_PARAM_VALUE", "dest=" + destination);
    SessionManager.getInstance().setLastAccess(session);
  }

  // check if the user is allowed to access the required component
  private boolean isUserAllowed(MainSessionController controller,
      String componentId) {
    boolean isAllowed = false;

    if (componentId == null) { // Personal space
      isAllowed = true;
    } else {
      isAllowed = controller.getOrganizationController().isComponentAvailable(
          componentId, controller.getUserId());
    }
    return isAllowed;
  }

  private void redirectService(HttpServletRequest request,
      HttpServletResponse response, String destination) {
    // Open the destination page
    SilverTrace.info("peasCore", "ComponentRequestRouter.redirectService",
        "root.MSG_GEN_PARAM_VALUE", "dest=" + destination);
    try {
      if (destination.startsWith("http") || destination.startsWith("ftp")) {
        response.sendRedirect(destination);
      } else {
        request
            .setAttribute(
            "com.stratelia.webactiv.servlets.ComponentRequestRouter.requestURI",
            request.getRequestURI());
        RequestDispatcher requestDispatcher = getServletConfig()
            .getServletContext().getRequestDispatcher(destination);
        if (requestDispatcher != null) {
          requestDispatcher.forward(request, response);
        }
        else {
          SilverTrace.info("peasCore",
              "ComponentRequestRouter.redirectService",
              "peasCore.EX_REDIRECT_SERVICE_FAILED", "Destination '"
              + destination + "' not found !");
        }
      }
    } catch (Exception e) {
      try {
        request.setAttribute("javax.servlet.jsp.jspException",
            new PeasCoreException("ComponentRequestRouter.redirectService",
            SilverpeasException.ERROR,
            "peasCore.EX_REDIRECT_SERVICE_FAILED", "Destination="
            + destination, e));
        getServletConfig().getServletContext().getRequestDispatcher(
            "/admin/jsp/errorpageMain.jsp").forward(request, response);
      } catch (Exception ex) {
        if ((e.getMessage() != null)
            && (e.getMessage().contains("Connection reset by peer: socket write error"))
            && (!e.getMessage().contains("SQL"))) { // This is a
          // "Connection reset by peer" exception due to user quick clicks -> Forget
          // it unless we are in Info Mode
          SilverTrace.info("peasCore",
              "ComponentRequestRouter.redirectService",
              "peasCore.EX_REDIRECT_SERVICE_FAILED", "Destination="
              + destination, e);
        } else {
          SilverTrace.info("peasCore",
              "ComponentRequestRouter.redirectService",
              "peasCore.EX_REDIRECT_SERVICE_FAILED", "Destination="
              + destination, e);
          SilverTrace.info("peasCore",
              "ComponentRequestRouter.redirectService",
              "peasCore.EX_REDIRECT_ERROR_PAGE_FAILED",
              "/admin/jsp/errorpage.jsp", ex);
        }
      }
    }
  }

  // Get the space id and the component id required by the user
  static public String[] getComponentId(HttpServletRequest request,
      MainSessionController mainSessionCtrl) {
    return webUtil.getComponentId(request);
  }

  // Get xoxoxSessionController from session
  // It must be stored with two differents id :
  // 1 - the componentId if component is instanciable
  // 2 - the component bean name if component is not instanciable (Personal
  // space)
  private ComponentSessionController getComponentSessionController(
      HttpSession session, String componentId) {
    if (componentId == null) {
      return (ComponentSessionController) session.getAttribute("Silverpeas_"
          + getSessionControlBeanName());
    }
    return (ComponentSessionController) session.getAttribute("Silverpeas_"
        + getSessionControlBeanName() + "_" + componentId);
  }

  private ComponentSessionController setComponentSessionController(
      HttpSession session, MainSessionController mainSessionCtrl,
      String spaceId, String componentId) {
    // ask to MainSessionController to create the ComponentContext
    ComponentContext componentContext = mainSessionCtrl.createComponentContext(
        spaceId, componentId);
    // instanciate a new CSC
    ComponentSessionController component = createComponentSessionController(
        mainSessionCtrl, componentContext);
    if (componentId == null) {
      session.setAttribute("Silverpeas_" + getSessionControlBeanName(),
          component);
    }
    else {
      session.setAttribute("Silverpeas_" + getSessionControlBeanName() + "_"
          + componentId, component);
    }
    SilverTrace.info("peasCore",
        "ComponentRequestRouter.setComponentSessionController",
        "peasCore.MSG_SESSION_CONTROLLER_INSTANCIATED", "spaceId=" + spaceId
        + " | componentId=" + componentId);
    return component;
  }
}
