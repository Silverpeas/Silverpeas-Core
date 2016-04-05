/*
 * Copyright (C) 2000 - 2016 Silverpeas
 * <p>
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU Affero General Public License as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 * <p>
 * As a special exception to the terms and conditions of version 3.0 of the GPL, you may
 * redistribute this Program in connection with Free/Libre Open Source Software ("FLOSS")
 * applications as described in Silverpeas's FLOSS exception. You should have received a copy of
 * the
 * text describing the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 * <p>
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Affero General Public License for more details.
 * <p>
 * You should have received a copy of the GNU Affero General Public License along with this
 * program.
 * If not, see <http://www.gnu.org/licenses/>.
 */
package org.silverpeas.core.web.mvc.route;

import org.silverpeas.core.web.look.LookHelper;
import org.silverpeas.core.security.session.SessionManagement;
import org.silverpeas.core.security.session.SessionManagementProvider;
import org.silverpeas.core.notification.user.UserSubscriptionNotificationSendingHandler;
import org.silverpeas.core.web.mvc.controller.ComponentContext;
import org.silverpeas.core.web.mvc.controller.ComponentSessionController;
import org.silverpeas.core.web.mvc.controller.MainSessionController;
import org.silverpeas.core.web.mvc.controller.PeasCoreException;
import org.silverpeas.core.web.mvc.controller.SilverpeasWebUtil;
import org.silverpeas.core.util.URLUtil;
import org.silverpeas.core.web.mvc.webcomponent.SilverpeasAuthenticatedHttpServlet;
import org.silverpeas.core.web.mvc.processor.UserAndGroupSelectionProcessor;
import org.silverpeas.core.silverstatistics.volume.service.SilverStatisticsManager;
import org.silverpeas.core.admin.service.OrganizationController;
import org.silverpeas.core.web.http.HttpRequest;
import org.silverpeas.core.security.token.Token;
import org.silverpeas.core.util.MultiSilverpeasBundle;
import org.silverpeas.core.util.ResourceLocator;
import org.silverpeas.core.util.ServiceProvider;
import org.silverpeas.core.exception.SilverpeasException;
import org.silverpeas.core.util.logging.SilverLogger;
import org.silverpeas.core.web.util.security.SecuritySettings;
import org.silverpeas.core.web.util.viewgenerator.html.GraphicElementFactory;
import org.silverpeas.core.web.token.SynchronizerTokenService;
import org.silverpeas.core.web.util.SilverpeasTransverseWebErrorUtil;

import javax.inject.Inject;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.regex.Pattern;

import static org.silverpeas.core.util.StringUtil.defaultStringIfNotDefined;
import static org.silverpeas.core.util.StringUtil.isDefined;

public abstract class ComponentRequestRouter<T extends ComponentSessionController>
    extends SilverpeasAuthenticatedHttpServlet {

  private static final long serialVersionUID = -8055016885655445663L;
  @Inject
  private UserAndGroupSelectionProcessor selectionProcessor;
  @Inject
  private OrganizationController organizationController;
  private static final Collection<Pattern> SESSION_SECURITY_GENERATION_FUNCTION_PATTERNS;

  static {
    SESSION_SECURITY_GENERATION_FUNCTION_PATTERNS = new ArrayList<>();
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

  /**
   * Check that user can access the component session controller function. Default behavior return
   * true in order to limit impact for each component session controller.
   * Each session controller must override this method in order to add access control.
   * @param function the current function action
   * @param componentSC the component session controller
   * @return true if user can process function, false else if.
   */
  protected boolean checkUserAuthorization(String function, T componentSC) {
    return true;
  }


  public abstract T createComponentSessionController(MainSessionController mainSessionCtrl,
      ComponentContext componentContext);

  /**
   * Indicates if the session security token has to be renewed from the given requested function?
   * The answer depends first on the <code>security.web.protection.sessiontoken.renew</code>
   * parameter defined in <code>org/silverpeas/util/security.properties</code> and secondly on the
   * choice made by some of the component request router.<br/>
   * The Referer information from headers is verified. If the referer is equals to the current
   * request URI, then the session security token is never renewed.
   * @return true if the token has to be renewed
   */
  protected final boolean hasTheSessionSecurityTokenToBeRenewed(final HttpServletRequest request,
      final String function) {
    boolean hasToBeRenewed = SecuritySettings.isSessionTokenRenewEnabled();
    if (hasToBeRenewed) {
      for (Pattern sessionSecurityTokenPattern : SESSION_SECURITY_GENERATION_FUNCTION_PATTERNS) {
        hasToBeRenewed = sessionSecurityTokenPattern.matcher(function).matches();
        if (hasToBeRenewed) {
          break;
        }
      }
    }
    if (hasToBeRenewed) {
      final String referer = defaultStringIfNotDefined(request.getHeader("Referer"), "");
      hasToBeRenewed = !request.getRequestURL().toString().equals(referer);
    }
    return hasToBeRenewed;
  }

  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) {

    String destination = computeDestination(request);
    if (!isDefined(destination)) {
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
    if (mainSessionCtrl.isAppInMaintenance() && !mainSessionCtrl.getCurrentUserDetail().
        isAccessAdmin()) {
      return ResourceLocator.getGeneralSettingBundle().getString("redirectAppInMaintenance");
    }

    // Get the space id and the component id required by the user
    String[] context = getComponentId(request, mainSessionCtrl);
    String spaceId = context[0];
    String componentId = context[1];
    String function = context[2];

    // Set gef space space identifier for dynamic look purpose
    setGefSpaceId(request, componentId, spaceId);

    boolean isSpaceInMaintenance = mainSessionCtrl.isSpaceInMaintenance(spaceId);

    // Space in Maintenance ?
    if (isSpaceInMaintenance && !mainSessionCtrl.getCurrentUserDetail().isAccessAdmin()) {
      return "/admin/jsp/spaceInMaintenance.jsp";
    }

    T component = this.getComponentSessionController(session, componentId);
    if (component == null) {
      // isUserStateValid that the user has an access to this component instance
      boolean bCompoAllowed = isUserAllowed(mainSessionCtrl, componentId);
      if (!bCompoAllowed) {
        SilverLogger.getLogger(this)
            .warn("User {0} not allowed to access application {1} in space {2}",
                mainSessionCtrl.getUserId(), componentId, spaceId);
        destination = ResourceLocator.getGeneralSettingBundle()
            .getString("accessForbidden", "/admin/jsp/accessForbidden.jsp");
        return destination;
      }
      component = setComponentSessionController(session, mainSessionCtrl, spaceId, componentId);
    }

    MultiSilverpeasBundle resources =
        new MultiSilverpeasBundle(component.getMultilang(), component.getIcon(), component.getSettings(),
            component.getLanguage());
    request.setAttribute("resources", resources);
    String[] browseContext = new String[]{component.getSpaceLabel(), component.getComponentLabel(),
        component.getSpaceId(), component.getComponentId(), component.getComponentUrl()};
    request.setAttribute("browseContext", browseContext);
    request.setAttribute("myComponentURL", URLUtil.getApplicationURL() + component.
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
      if (isDefined(destination)) {
        return destination;
      }
    }

    // Verifying the subscription notification sending parameters
    UserSubscriptionNotificationSendingHandler.verifyRequest(request);

    // retourne la page jsp de destination et place dans la request les objets
    // utilises par cette page
    if (checkUserAuthorization(function, component)) {
      destination = getDestination(function, component, httpRequest);
    } else {
      SilverLogger.getLogger(this)
          .warn("User {0} not allowed to invoke {1} for application {2}",
              component.getUserId(), function, componentId);
      destination = "/admin/jsp/accessForbidden.jsp";
    }

    // Session security token management
    if (isDefined(componentId) && hasTheSessionSecurityTokenToBeRenewed(request, function)) {
      renewSessionSecurityToken(request);
    }

    // Check existence of a transverse exception
    SilverpeasTransverseWebErrorUtil.verifyErrorFromRequest(request, component.getLanguage());

    if (selectionProcessor.isSelectionAsked(destination)) {
      selectionProcessor.prepareSelection(mainSessionCtrl.getSelection(), request);
    }

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
    SessionManagement sessionManagement = SessionManagementProvider.getSessionManagement();
    sessionManagement.validateSession(session.getId());
  }

  // isUserStateValid if the user is allowed to access the required component
  private boolean isUserAllowed(MainSessionController controller, String componentId) {
    return componentId == null || getOrganizationController()
        .isComponentAvailable(componentId, controller.getUserId());
  }

  private void redirectService(HttpServletRequest request, HttpServletResponse response,
      String destination) {
    // Open the destination page
    try {
      if (destination.startsWith("http") || destination.startsWith("ftp")) {
        response.sendRedirect(destination);
      } else {
        request.setAttribute("org.silverpeas.servlets.ComponentRequestRouter.requestURI",
            request.getRequestURI());
        // set the session token so that is can be available for all JSPs in the case some of them
        // require it for some specific protected actions.
        Token token = getSynchronizerTokenService().getSessionToken(request);
        request.setAttribute(SynchronizerTokenService.SESSION_TOKEN_KEY, token.getValue());

        RequestDispatcher requestDispatcher =
            getServletConfig().getServletContext().getRequestDispatcher(destination);
        if (requestDispatcher != null) {
          requestDispatcher.forward(request, response);
        } else {
          SilverLogger.getLogger(this)
              .warn("Web page dispatching failure: destination {0} not found!", destination);
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
        SilverLogger.getLogger(this)
            .error("Web page dispatching to {0} failed: {1}",
                new String[]{destination, e.getMessage()}, e);
      }
    }
  }

  // Get the space id and the component id required by the user
  @SuppressWarnings("UnusedParameters")
  static public String[] getComponentId(HttpServletRequest request,
      MainSessionController mainSessionCtrl) {
    SilverpeasWebUtil webUtil = ServiceProvider.getService(SilverpeasWebUtil.class);
    return webUtil.getComponentId(request);
  }

  protected OrganizationController getOrganizationController() {
    return organizationController;
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
    LookHelper helper = LookHelper.getLookHelper(session);
    if (isDefined(componentId)) {
      if (gef != null && helper != null) {
        helper.setComponentIdAndSpaceIds(null, null, componentId);
        String helperSpaceId = helper.getSubSpaceId();
        if (!isDefined(helperSpaceId)) {
          helperSpaceId = helper.getSpaceId();
        }
        gef.setSpaceIdForCurrentRequest(helperSpaceId);
      }
    } else if (isDefined(spaceId)) {
      if (gef != null && helper != null) {
        helper.setSpaceId(spaceId);
        gef.setSpaceIdForCurrentRequest(spaceId);
      }
    }
  }
}
