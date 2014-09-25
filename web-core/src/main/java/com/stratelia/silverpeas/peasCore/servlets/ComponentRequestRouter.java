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
package com.stratelia.silverpeas.peasCore.servlets;

import com.silverpeas.look.LookHelper;
import com.silverpeas.session.SessionManagement;
import com.silverpeas.session.SessionManagementFactory;
import com.silverpeas.util.StringUtil;
import com.stratelia.silverpeas.peasCore.ComponentContext;
import com.stratelia.silverpeas.peasCore.ComponentSessionController;
import com.stratelia.silverpeas.peasCore.MainSessionController;
import com.stratelia.silverpeas.peasCore.PeasCoreException;
import com.stratelia.silverpeas.peasCore.SilverpeasWebUtil;
import com.stratelia.silverpeas.peasCore.URLManager;
import com.stratelia.silverpeas.peasCore.UserAndGroupSelectionProcessor;
import com.stratelia.silverpeas.silverstatistics.control.SilverStatisticsManager;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.silverpeas.util.ResourcesWrapper;
import com.stratelia.webactiv.util.GeneralPropertiesManager;
import com.stratelia.webactiv.util.exception.SilverpeasException;
import com.stratelia.webactiv.util.viewGenerator.html.GraphicElementFactory;
import org.silverpeas.servlet.HttpRequest;
import org.silverpeas.token.Token;
import org.silverpeas.util.security.SecuritySettings;
import org.silverpeas.web.token.SynchronizerTokenService;
import org.silverpeas.web.token.SynchronizerTokenServiceFactory;
import org.silverpeas.web.util.SilverpeasTransverseWebErrorUtil;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.regex.Pattern;

public abstract class ComponentRequestRouter<T extends ComponentSessionController>
    extends SilverpeasAuthenticatedHttpServlet {

  private static final long serialVersionUID = -8055016885655445663L;
  private static final SilverpeasWebUtil webUtil = new SilverpeasWebUtil();
  private final UserAndGroupSelectionProcessor selectionProcessor =
      new UserAndGroupSelectionProcessor();
  private static final Collection<Pattern> SESSION_SECURITY_GENERATION_FUNCTION_PATTERNS;

  static {
    SESSION_SECURITY_GENERATION_FUNCTION_PATTERNS = new ArrayList<Pattern>();
    SESSION_SECURITY_GENERATION_FUNCTION_PATTERNS.add(Pattern.compile("^(?i)(main)"));
  }

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
  public abstract String getDestination(String function, T componentSC, HttpRequest request);

  public abstract T createComponentSessionController(MainSessionController mainSessionCtrl,
      ComponentContext componentContext);

  /**
   * Indicates if the session security token has to be renewed from the given requested function?
   * The answer depends first on the <code>security.web.protection.sessiontoken.renew</code>
   * parameter defined in <code>org/silverpeas/util/security.properties</code> and secondly on the
   * choice made by some of the component request router.
   * @return true if the the token has to be renewed
   */
  protected final boolean hasTheSessionSecurityTokenToBeRenewed(String function) {
    boolean hasToBeRenewed = SecuritySettings.isSessionTokenRenewEnabled();
    if (hasToBeRenewed) {
      for (Pattern sessionSecurityTokenPattern : SESSION_SECURITY_GENERATION_FUNCTION_PATTERNS) {
        hasToBeRenewed = sessionSecurityTokenPattern.matcher(function).matches();
        if (hasToBeRenewed) {
          break;
        }
      }
    }
    return hasToBeRenewed;
  }

  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) {

    String destination = computeDestination(request);
    SilverTrace.debug("peasCore", "RR", "root.MSG_GEN_PARAM_VALUE", "response = " + response);
    if (!StringUtil.isDefined(destination)) {
      throwHttpNotFoundError();
    }
    redirectService(request, response, destination);
  }

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response)
      throws ServletException {
    doPost(request, response);
  }

  private String computeDestination(HttpServletRequest request) {
    String destination;
    HttpSession session = request.getSession(false);

    // Get the main session controller
    MainSessionController mainSessionCtrl = getMainSessionController(request);

    // App in Maintenance ?
    SilverTrace.debug("peasCore", "ComponentRequestRouter.computeDestination()",
        "root.MSG_GEN_PARAM_VALUE",
        "appInMaintenance = " + String.valueOf(mainSessionCtrl.isAppInMaintenance()));
    SilverTrace.debug("peasCore", "ComponentRequestRouter.computeDestination()",
        "root.MSG_GEN_PARAM_VALUE", "type User = " + mainSessionCtrl.getUserAccessLevel());
    if (mainSessionCtrl.isAppInMaintenance() && !mainSessionCtrl.getCurrentUserDetail().
        isAccessAdmin()) {
      return GeneralPropertiesManager.getString("redirectAppInMaintenance");
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

    // Set gef space space identifier for dynamic look purpose
    setGefSpaceId(request, componentId, spaceId);

    boolean isSpaceInMaintenance = mainSessionCtrl.isSpaceInMaintenance(spaceId);
    SilverTrace.debug("peasCore", "ComponentRequestRouter.computeDestination()",
        "root.MSG_GEN_PARAM_VALUE", "spaceIsMaintenance = " + isSpaceInMaintenance);

    // Space in Maintenance ?
    if (isSpaceInMaintenance && !mainSessionCtrl.getCurrentUserDetail().isAccessAdmin()) {
      return "/admin/jsp/spaceInMaintenance.jsp";
    }

    T component = this.getComponentSessionController(session, componentId);
    if (component == null) {
      // isUserStateValid that the user has an acces to this component instance
      boolean bCompoAllowed = isUserAllowed(mainSessionCtrl, componentId);
      if (!bCompoAllowed) {
        SilverTrace.warn("peasCore", "ComponentRequestRouter.computeDestination",
            "peasCore.MSG_USER_NOT_ALLOWED",
            "User=" + mainSessionCtrl.getUserId() + " | componentId=" + componentId +
                " | spaceId=" + spaceId);
        destination =
            GeneralPropertiesManager.getString("accessForbidden", "/admin/jsp/accessForbidden.jsp");
        return destination;
      }
      component = setComponentSessionController(session, mainSessionCtrl, spaceId, componentId);
    }

    ResourcesWrapper resources =
        new ResourcesWrapper(component.getMultilang(), component.getIcon(), component.getSettings(),
            component.getLanguage());
    request.setAttribute("resources", resources);
    String[] browseContext = new String[]{component.getSpaceLabel(), component.getComponentLabel(),
        component.getSpaceId(), component.getComponentId(), component.getComponentUrl()};
    request.setAttribute("browseContext", browseContext);
    request.setAttribute("myComponentURL", URLManager.getApplicationURL() + component.
        getComponentUrl());

    HttpRequest httpRequest = HttpRequest.decorate(request);
    if (!"Idle.jsp".equals(function) && !"IdleSilverpeasV5.jsp".equals(function) &&
        !"ChangeSearchTypeToExpert".equals(function) && !"markAsRead".equals(function)) {
      GraphicElementFactory gef = (GraphicElementFactory) session
          .getAttribute(GraphicElementFactory.GE_FACTORY_SESSION_ATT);
      gef.setComponentIdForCurrentRequest(component.getComponentId());
      gef.setHttpRequest(httpRequest);
    }

    // notify silverstatistics
    if (function.equals("Main") || function.startsWith("searchResult") ||
        function.startsWith("portlet") || function.equals("GoToFilesTab")) {
      // only for instanciable components
      if (componentId != null) {
        SilverStatisticsManager.getInstance()
            .addStatAccess(component.getUserId(), new Date(), component.getComponentName(),
                component.getSpaceId(), component.getComponentId());
      }
    }

    if (selectionProcessor.isComeFromSelectionPanel(request)) {
      destination = selectionProcessor.processSelection(mainSessionCtrl.getSelection(), request);
      if (StringUtil.isDefined(destination)) {
        return destination;
      }
    }

    // retourne la page jsp de destination et place dans la request les objets
    // utilises par cette page
    destination = getDestination(function, component, httpRequest);

    // Session security token management
    if (StringUtil.isDefined(componentId) && hasTheSessionSecurityTokenToBeRenewed(function)) {
      renewSessionSecurityToken(request);
    }

    // Check existence of a transverse exception
    SilverpeasTransverseWebErrorUtil.verifyErrorFromRequest(request, component.getLanguage());

    if (selectionProcessor.isSelectionAsked(destination)) {
      selectionProcessor.prepareSelection(mainSessionCtrl.getSelection(), request);
    }

    SilverTrace
        .info("couverture", "ComponentRequestRouter.computeDestination()", "couverture.MSG_RR_JSP",
            "destination = '" + destination + "'");

    // Update last accessed time depending on destination
    // see ClipboardRequestRouter
    // Except for notifications
    if (!("POPUP".equals(getSessionControlBeanName()) && function.startsWith("ReadMessage"))) {
      updateSessionManagement(session, destination);
    }

    request.setAttribute(getSessionControlBeanName(), component);

    return destination;

  }

  public void updateSessionManagement(HttpSession session, String destination) {
    SilverTrace.info("peasCore", "ComponentRequestRouter.updateSessionManagement",
        "root.MSG_GEN_PARAM_VALUE", "dest=" + destination);
    SessionManagementFactory factory = SessionManagementFactory.getFactory();
    SessionManagement sessionManagement = factory.getSessionManagement();
    sessionManagement.validateSession(session.getId());
  }

  // isUserStateValid if the user is allowed to access the required component
  private boolean isUserAllowed(MainSessionController controller, String componentId) {
    return componentId == null || controller.getOrganisationController()
        .isComponentAvailable(componentId, controller.getUserId());
  }

  private void redirectService(HttpServletRequest request, HttpServletResponse response,
      String destination) {
    // Open the destination page
    SilverTrace
        .info("peasCore", "ComponentRequestRouter.redirectService", "root.MSG_GEN_PARAM_VALUE",
            "dest=" + destination);
    try {
      if (destination.startsWith("http") || destination.startsWith("ftp")) {
        response.sendRedirect(destination);
      } else {
        request.setAttribute("org.silverpeas.servlets.ComponentRequestRouter.requestURI",
            request.getRequestURI());
        // set the session token so that is can be available for all JSPs in the case some of them
        // require it for some specific protected actions.
        SynchronizerTokenService tokenService = SynchronizerTokenServiceFactory.
            getSynchronizerTokenService();
        Token token = tokenService.getSessionToken(request);
        request.setAttribute(SynchronizerTokenService.SESSION_TOKEN_KEY, token.getValue());

        RequestDispatcher requestDispatcher =
            getServletConfig().getServletContext().getRequestDispatcher(destination);
        if (requestDispatcher != null) {
          requestDispatcher.forward(request, response);
        } else {
          SilverTrace.info("peasCore", "ComponentRequestRouter.redirectService",
              "peasCore.EX_REDIRECT_SERVICE_FAILED",
              "Destination '" + destination + "' not found !");
        }
      }
    } catch (Exception e) {
      try {
        request.setAttribute("javax.servlet.jsp.jspException",
            new PeasCoreException("ComponentRequestRouter.redirectService",
                SilverpeasException.ERROR, "peasCore.EX_REDIRECT_SERVICE_FAILED",
                "Destination=" + destination, e));
        getServletConfig().getServletContext().getRequestDispatcher("/admin/jsp/errorpageMain.jsp")
            .forward(request, response);
      } catch (Exception ex) {
        if ((e.getMessage() != null) &&
            (e.getMessage().contains("Connection reset by peer: socket write error")) &&
            (!e.getMessage().contains("SQL"))) { // This is a
          // "Connection reset by peer" exception due to user quick clicks -> Forget
          // it unless we are in Info Mode
          SilverTrace.info("peasCore", "ComponentRequestRouter.redirectService",
              "peasCore.EX_REDIRECT_SERVICE_FAILED", "Destination=" + destination, e);
        } else {
          SilverTrace.info("peasCore", "ComponentRequestRouter.redirectService",
              "peasCore.EX_REDIRECT_SERVICE_FAILED", "Destination=" + destination, e);
          SilverTrace.info("peasCore", "ComponentRequestRouter.redirectService",
              "peasCore.EX_REDIRECT_ERROR_PAGE_FAILED", "/admin/jsp/errorpage.jsp", ex);
        }
      }
    }
  }

  // Get the space id and the component id required by the user
  @SuppressWarnings("UnusedParameters")
  static public String[] getComponentId(HttpServletRequest request,
      MainSessionController mainSessionCtrl) {
    return webUtil.getComponentId(request);
  }

  // Get xoxoxSessionController from session
  // It must be stored with two differents id :
  // 1 - the componentId if component is instanciable
  // 2 - the component bean name if component is not instanciable (Personal
  // space)
  @SuppressWarnings("unchecked")
  private T getComponentSessionController(HttpSession session, String componentId) {
    if (componentId == null) {
      return (T) session.getAttribute("Silverpeas_" + getSessionControlBeanName());
    }
    return (T) session
        .getAttribute("Silverpeas_" + getSessionControlBeanName() + "_" + componentId);
  }

  private T setComponentSessionController(HttpSession session,
      MainSessionController mainSessionCtrl, String spaceId, String componentId) {
    // ask to MainSessionController to create the ComponentContext
    ComponentContext componentContext =
        mainSessionCtrl.createComponentContext(spaceId, componentId);
    // instanciate a new CSC
    T component = createComponentSessionController(mainSessionCtrl, componentContext);
    if (componentId == null) {
      session.setAttribute("Silverpeas_" + getSessionControlBeanName(), component);
    } else {
      session
          .setAttribute("Silverpeas_" + getSessionControlBeanName() + "_" + componentId, component);
    }
    SilverTrace.info("peasCore", "ComponentRequestRouter.setComponentSessionController",
        "peasCore.MSG_SESSION_CONTROLLER_INSTANCIATED",
        "spaceId=" + spaceId + " | componentId=" + componentId);
    return component;
  }

  /**
   * Set GEF and look helper space identifier
   * @param req current HttpServletRequest
   * @param componentId the component identifier
   */
  private void setGefSpaceId(HttpServletRequest req, String componentId, String spaceId) {
    HttpSession session = req.getSession(true);
    GraphicElementFactory gef =
        (GraphicElementFactory) session.getAttribute(GraphicElementFactory.GE_FACTORY_SESSION_ATT);
    LookHelper helper = (LookHelper) session.getAttribute(LookHelper.SESSION_ATT);
    if (StringUtil.isDefined(componentId)) {
      if (gef != null && helper != null) {
        helper.setComponentIdAndSpaceIds(null, null, componentId);
        String helperSpaceId = helper.getSubSpaceId();
        if (!StringUtil.isDefined(helperSpaceId)) {
          helperSpaceId = helper.getSpaceId();
        }
        gef.setSpaceIdForCurrentRequest(helperSpaceId);
      }
    } else if (StringUtil.isDefined(spaceId)) {
      if (gef != null && helper != null) {
        helper.setSpaceId(spaceId);
        gef.setSpaceIdForCurrentRequest(spaceId);
      }
    }
  }
}
