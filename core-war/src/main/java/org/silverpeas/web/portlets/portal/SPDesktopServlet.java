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
package org.silverpeas.web.portlets.portal;

import org.silverpeas.core.util.URLUtil;
import org.silverpeas.core.web.portlets.portal.PortletWindowData;
import org.silverpeas.core.web.portlets.portal.PortletWindowDataImpl;
import org.silverpeas.core.web.mvc.controller.MainSessionController;
import org.silverpeas.core.silvertrace.SilverTrace;
import org.silverpeas.core.admin.space.SpaceInst;
import org.silverpeas.core.admin.user.model.UserDetail;
import com.sun.portal.container.ChannelMode;
import com.sun.portal.container.ChannelState;
import com.sun.portal.container.PortletType;
import com.sun.portal.portletcontainer.admin.PortletRegistryCache;
import com.sun.portal.portletcontainer.admin.registry.PortletRegistryConstants;
import com.sun.portal.portletcontainer.context.ServletContextThreadLocalizer;
import com.sun.portal.portletcontainer.context.registry.PortletRegistryContext;
import com.sun.portal.portletcontainer.context.registry.PortletRegistryException;
import com.sun.portal.portletcontainer.invoker.InvokerException;
import com.sun.portal.portletcontainer.invoker.WindowInvokerConstants;
import com.sun.portal.portletcontainer.invoker.util.InvokerUtil;
import org.silverpeas.core.admin.service.OrganizationController;
import org.silverpeas.core.admin.service.OrganizationControllerProvider;
import org.silverpeas.core.util.LocalizationBundle;
import org.silverpeas.core.util.ResourceLocator;
import org.silverpeas.core.util.SettingBundle;
import org.silverpeas.core.util.StringUtil;
import org.silverpeas.core.web.util.viewgenerator.html.GraphicElementFactory;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.silverpeas.core.util.MimeTypes.SERVLET_HTML_CONTENT_TYPE;
import static org.silverpeas.core.util.StringUtil.isDefined;

public class SPDesktopServlet extends HttpServlet {

  private static final long serialVersionUID = -3241648887903159985L;
  private ServletContext context;
  private static final Logger logger = Logger.getLogger("org.silverpeas.web.portlets.portal",
      "org.silverpeas.portlets.PCDLogMessages");

  /**
   * Reads the DriverConfig.properties file. Initializes the Portlet Registry files.
   *
   * @param config the ServletConfig Object
   * @throws javax.servlet.ServletException
   */
  @Override
  public void init(ServletConfig config)
      throws ServletException {
    super.init(config);
    context = config.getServletContext();
    PortletRegistryCache.init();
  }

  @Override
  protected void service(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {

    String spaceHomePage = null;
    String spaceId = request.getParameter("SpaceId");
    if (SpaceInst.PERSONAL_SPACE_ID.equals(spaceId) || SpaceInst.DEFAULT_SPACE_ID.equals(spaceId)) {
      request.getSession().removeAttribute("Silverpeas_Portlet_SpaceId");
    } else if (isDefined(spaceId)) {
      request.getSession().setAttribute("Silverpeas_Portlet_SpaceId", spaceId);
      spaceHomePage = getSpaceHomepageURL(spaceId, request);
    }

    if (isDefined(spaceHomePage)) {
      String sRequestURL = request.getRequestURL().toString();
      String m_sAbsolute =
          sRequestURL.substring(0, sRequestURL.length() - request.getRequestURI().length());
      String baseURL =
          ResourceLocator.getGeneralSettingBundle().getString("httpServerBase", m_sAbsolute);
      if (spaceHomePage.startsWith("/")) {
        // case of forward inside application /silverpeas
        String applicationContext = URLUtil.getApplicationURL();
        if (spaceHomePage.startsWith(applicationContext)) {
          spaceHomePage = baseURL + spaceHomePage;
        } else {
          spaceHomePage = baseURL + applicationContext + spaceHomePage;
        }
      } else {
        if (spaceHomePage.startsWith("$")) {
          // case of redirection to another webapp (/weblib for example)
          spaceHomePage = baseURL + spaceHomePage.substring(1);
        }
      }
      response.sendRedirect(spaceHomePage);
    } else {
      spaceId = getSpaceId(request);
      UserDetail currentUser = UserDetail.getCurrentRequester();
      String userId = currentUser.getId();
      String spContext = userId;
      if (isDefined(spaceId)) {
        spContext = spaceId;
      }
      setUserIdAndSpaceIdInRequest(spaceId, userId, request);

      DesktopMessages.init(currentUser.getUserPreferences().getLanguage());
      DriverUtil.init(request);
      response.setContentType(SERVLET_HTML_CONTENT_TYPE);

      // Get the list of visible portlets(sorted by the row number)
      try {
        ServletContextThreadLocalizer.set(context);
        PortletRegistryContext portletRegistryContext =
            DriverUtil.getPortletRegistryContext(spContext);
        String portletWindowName = DriverUtil.getPortletWindowFromRequest(request);
        PortletContent portletContent =
            getPortletContentObject(portletWindowName, context, request, response);
        String portletRemove = DriverUtil.getPortletRemove(request);
        if (portletRemove != null && portletWindowName != null) {
          portletRegistryContext.removePortletWindow(portletWindowName);
          // portletRegistryContext.showPortletWindow(portletWindowName, false);
          portletWindowName = null; // re-render all portlets
        }
        Set<PortletContent> portletContents = null;
        if (portletWindowName == null) {
          portletContents =
              getAllPortletContents(request, response, portletContent, portletRegistryContext);
        } else {
          String driverAction = DriverUtil.getDriverAction(request);
          if (WindowInvokerConstants.ACTION.equals(driverAction)) {
            URL url = executeProcessAction(request, portletContent);
            try {
              if (url != null) {
                response.sendRedirect(url.toString());
              } else {
                response.sendRedirect(request.getRequestURL().toString());
              }
            } catch (IOException ioe) {
              throw new InvokerException("Failed during sendRedirect", ioe);
            }
          } else if (WindowInvokerConstants.RENDER.equals(driverAction)) {
            portletContents =
                getAllPortletContents(request, response, portletContent, portletRegistryContext);
          } else if (WindowInvokerConstants.RESOURCE.equals(driverAction)) {
            initPortletContent(portletContent, request);
            portletContent.getResources();
          }
        }
        if (portletContents != null) {
          Map<String, SortedSet<PortletWindowData>> portletWindowContents =
              getPortletWindowContents(request, portletContents, portletRegistryContext,
              spContext);
          setPortletWindowData(request, portletWindowContents);

          //set all existing portlets in session
          List<String> portletsName = portletRegistryContext.getAvailablePortlets();
          setPortletNames(request, currentUser.getUserPreferences().getLanguage(), portletsName);

          InvokerUtil.setResponseProperties(request, response,
              portletContent.getResponseProperties());
          RequestDispatcher rd = context.getRequestDispatcher(getPresentationURI(request));
          rd.forward(request, response);
          InvokerUtil.clearResponseProperties(portletContent.getResponseProperties());
        }
      } catch (Exception e) {
        SilverTrace.error("portlet", "SPDesktopServlet.service", "root.MSG_GEN_PARAM_VALUE",
            "Portlets exception !", e);
      } finally {
        ServletContextThreadLocalizer.set(null);
      }
    }
  }

  /**
   * Returns the PortletWindowData object for the portlet window. A Set of PortletWindowData for all
   * portlet windows is stored in the Session.
   *
   * @param request the HttpServletRequest Object
   * @param portletWindowName the name of the portlet window
   * @return the PortletWindowData object for the portlet window.
   */
  private PortletWindowData getPortletWindowData(final HttpServletRequest request,
      String portletWindowName) {
    HttpSession session = request.getSession(true);
    PortletWindowData portletWindowDataToFind = null;
    Map<String, SortedSet<PortletWindowData>> portletWindowContents =
        (Map<String, SortedSet<PortletWindowData>>) session.getAttribute(
        DesktopConstants.PORTLET_WINDOWS);
    boolean found = false;
    if (portletWindowContents != null) {
      Set<Map.Entry<String, SortedSet<PortletWindowData>>> set = portletWindowContents.entrySet();
      for (Map.Entry<String, SortedSet<PortletWindowData>> mapEntry : set) {
        SortedSet<PortletWindowData> portletWindowDataSet = mapEntry.getValue();
        for (PortletWindowData portletWindowData : portletWindowDataSet) {
          if (portletWindowName.equals(portletWindowData.getPortletWindowName())) {
            portletWindowDataToFind = portletWindowData;
            found = true;
            break;
          }
        }
        if (found) {
          break;
        }
      }
    }
    if (found) {
      return portletWindowDataToFind;
    } else {
      return null;
    }
  }

  private void setPortletWindowData(final HttpServletRequest request,
      final Map<String, SortedSet<PortletWindowData>> portletWindowContents) {
    HttpSession session = request.getSession(true);
    session.removeAttribute(DesktopConstants.PORTLET_WINDOWS);
    session.setAttribute(DesktopConstants.PORTLET_WINDOWS, portletWindowContents);
  }

  /**
   * Returns a Map of portlet data and title for all portlet windows. In the portlet window is
   * maximized, only the data and title for that portlet is displayed. For any portlet window that
   * is minimized , only the title is shown.
   *
   * @param request the HttpServletRequest Object
   * @param portletContent the PortletContent Object
   * @param portletRegistryContext the PortletRegistryContext Object
   * @return a Map of portlet data and title for all portlet windows.
   */
  private Set<PortletContent> getAllPortletContents(HttpServletRequest request,
      HttpServletResponse response,
      PortletContent portletContent, PortletRegistryContext portletRegistryContext) throws
      InvokerException {
    String portletWindowName = DriverUtil.getPortletWindowFromRequest(request);
    ChannelState portletWindowState = getPortletWindowState(request, portletWindowName);
    Set<PortletContent> portletContents;
    if (portletWindowState.equals(ChannelState.MAXIMIZED)) {
      portletContent.setPortletWindowState(ChannelState.MAXIMIZED);
      portletContents = new HashSet<PortletContent>();
      portletContents.add(initPortletContent(portletContent, request));
    } else {
      List<String> visiblePortletWindows = getVisiblePortletWindows(portletRegistryContext);
      portletContents = getPortletContents(request, response, visiblePortletWindows);
    }
    return portletContents;
  }

  /**
   * Initializes the specified portlet content from the data carries by specified HTTP request.
   *
   * @param portletContent the portlet content to initialize.
   * @param request the HTTP request carrying the data (by itself or through the HTTP session) about
   * the portlet.
   * @return the initialized portlet content.
   */
  private PortletContent initPortletContent(final PortletContent portletContent,
      final HttpServletRequest request) {
    String portletWindowName = portletContent.getPortletWindowName();
    portletContent.setPortletWindowMode(getPortletWindowMode(request, portletWindowName));
    portletContent.setPortletWindowState(getPortletWindowState(request, portletWindowName));
    return portletContent;
  }

  /**
   * Returns a Map of portlet data and title for the portlet windows specified in the portletList
   *
   * @param request the HttpServletRequest Object
   * @param response the HttpServletResponse object
   * @param portletList the List of portlet windows
   * @return a Map of portlet data and title for the portlet windows specified in the portletList
   */
  private Set<PortletContent> getPortletContents(final HttpServletRequest request,
      final HttpServletResponse response,
      final List<String> portletList) throws InvokerException {
    Set<PortletContent> portletContents = new HashSet<PortletContent>();
    for (String portletWindowName : portletList) {
      PortletContent portletContent =
          getPortletContentObject(portletWindowName, context, request, response);
      portletContent = initPortletContent(portletContent, request);
      portletContents.add(portletContent);
    }
    return portletContents;
  }

  /**
   * Returns a Map of PortletWindowData for the portlet windows for both thick and think widths.
   *
   * @param request the HttpServletRequest Object
   * @param portletContents a Map of portlet data and title for the portlet windows
   * @param portletRegistryContext the PortletRegistryContext Object
   * @return a Map of PortletWindowData for the portlet windows
   */
  private Map<String, SortedSet<PortletWindowData>> getPortletWindowContents(
      final HttpServletRequest request, final Set<PortletContent> portletContents,
      final PortletRegistryContext portletRegistryContext, String spContext) {
    SortedSet<PortletWindowData> portletWindowContentsThin = new TreeSet<PortletWindowData>();
    SortedSet<PortletWindowData> portletWindowContentsThick = new TreeSet<PortletWindowData>();
    for (PortletContent portletContent : portletContents) {
      String portletWindowName = portletContent.getPortletWindowName();
      try {
        PortletWindowData portletWindowData =
            getPortletWindowDataObject(request, portletContent, portletRegistryContext, spContext);

        if (portletWindowData.isThin()) {
          portletWindowContentsThin.add(portletWindowData);
        } else if (portletWindowData.isThick()) {
          portletWindowContentsThick.add(portletWindowData);
        } else {
          throw new PortletRegistryException(portletWindowName + " is neither thick or thin!!");
        }
      } catch (PortletRegistryException pre) {
        logger.log(Level.SEVERE, pre.getMessage(), pre);
      }
    }
    Map<String, SortedSet<PortletWindowData>> portletWindowContents =
        new HashMap<String, SortedSet<PortletWindowData>>();
    portletWindowContents.put(PortletRegistryConstants.WIDTH_THICK, portletWindowContentsThick);
    portletWindowContents.put(PortletRegistryConstants.WIDTH_THIN, portletWindowContentsThin);
    logger.log(Level.INFO, "PSPCD_CSPPD0022", new String[]{
          String.valueOf(portletWindowContentsThin.
          size()), String.valueOf(portletWindowContentsThick.size())});

    return portletWindowContents;
  }

  private URL executeProcessAction(final HttpServletRequest request,
      final PortletContent portletContent) throws InvokerException {
    String portletWindowName = DriverUtil.getPortletWindowFromRequest(request);
    ChannelMode portletWindowMode =
        DriverUtil.getPortletWindowModeOfPortletWindow(request, portletWindowName);
    ChannelState portletWindowState =
        DriverUtil.getPortletWindowStateOfPortletWindow(request, portletWindowName);
    portletContent.setPortletWindowName(portletWindowName);
    portletContent.setPortletWindowMode(portletWindowMode);
    portletContent.setPortletWindowState(portletWindowState);
    URL url = portletContent.executeAction();
    return url;
  }

  /**
   * Returns the list of visible portlet windows from the portlet registry.
   *
   * @param portletRegistryContext the PortletRegistryContext Object
   * @return the list of visible portlet windows from the portlet registry.
   */
  protected List<String> getVisiblePortletWindows(
      final PortletRegistryContext portletRegistryContext) throws InvokerException {
    List<String> visiblePortletWindows = null;
    try {
      visiblePortletWindows = portletRegistryContext.getVisiblePortletWindows(PortletType.LOCAL);
    } catch (PortletRegistryException pre) {
      visiblePortletWindows = Collections.EMPTY_LIST;
      throw new InvokerException("Cannot get Portlet List", pre);
    }
    return visiblePortletWindows;
  }

  /**
   * Gets the state of the portlet window identified by the specified name. It looks for the state
   * in the request (current portlet window). If not found, it looks for it among the available
   * portlets in the session.
   *
   * @param request the HttpServletRequest Object
   * @param portletWindowName the name of the portlet window
   * @return the state of the specified portlet window.
   */
  protected ChannelState getPortletWindowState(final HttpServletRequest request,
      String portletWindowName) {
    ChannelState portletWindowState = ChannelState.NORMAL;
    if (portletWindowName != null) {
      portletWindowState =
          DriverUtil.getPortletWindowStateOfPortletWindow(request, portletWindowName);
      if (portletWindowState == null) {
        portletWindowState = getPortletWindowStateFromSavedData(request, portletWindowName);
      }
    }
    return portletWindowState;
  }

  /**
   * Gets the mode of the portlet window identified by the specified name. It looks for the mode in
   * the request (current portlet window). If not found, it looks for it among the available
   * portlets in the session.
   *
   * @param request the HttpServletRequest Object
   * @param portletWindowName the name of the portlet window
   * @return the mode of the specified portlet window.
   */
  protected ChannelMode getPortletWindowMode(final HttpServletRequest request,
      String portletWindowName) {
    ChannelMode portletWindowMode = ChannelMode.VIEW;
    if (portletWindowName != null) {
      portletWindowMode =
          DriverUtil.getPortletWindowModeOfPortletWindow(request, portletWindowName);
      if (portletWindowMode == null) {
        portletWindowMode = getPortletWindowModeFromSavedData(request, portletWindowName);
      }
    }
    return portletWindowMode;
  }

  /**
   * Returns the portlet window state for the portlet window from the PortletWindowData that is in
   * the session.
   *
   * @param request the HttpServletRequest Object
   * @param portletWindowName the name of the portlet window
   * @return the portlet window state for the portlet window from session.
   */
  protected ChannelState getPortletWindowStateFromSavedData(final HttpServletRequest request,
      String portletWindowName) {
    PortletWindowData portletWindowContent = getPortletWindowData(request, portletWindowName);
    ChannelState portletWindowState = ChannelState.NORMAL;
    if (portletWindowContent != null) {
      String currentPortletWindowState = portletWindowContent.getCurrentWindowState();
      if (currentPortletWindowState != null) {
        portletWindowState = new ChannelState(currentPortletWindowState);
      }
    }
    return portletWindowState;
  }

  /**
   * Returns the portlet window mode for the portlet window from the PortletWindowData that is in
   * the session.
   *
   * @param request the HttpServletRequest Object
   * @param portletWindowName the name of the portlet window
   * @return the portlet window mode for the portlet window from session.
   */
  protected ChannelMode getPortletWindowModeFromSavedData(final HttpServletRequest request,
      String portletWindowName) {
    PortletWindowData portletWindowContent = getPortletWindowData(request, portletWindowName);
    ChannelMode portletWindowMode = ChannelMode.VIEW;
    if (portletWindowContent != null) {
      String currentPortletWindowMode = portletWindowContent.getCurrentMode();
      if (currentPortletWindowMode != null) {
        portletWindowMode = new ChannelMode(currentPortletWindowMode);
      }
    }
    return portletWindowMode;
  }

  protected PortletContent getPortletContentObject(String portletWindowName,
      final ServletContext context, final HttpServletRequest request,
      final HttpServletResponse response) throws InvokerException {
    PortletContent portletContent = new PortletContent(context, request, response);
    portletContent.setPortletWindowName(portletWindowName);
    return portletContent;
  }

  protected PortletWindowData getPortletWindowDataObject(final HttpServletRequest request,
      final PortletContent portletContent, final PortletRegistryContext portletRegistryContext,
      String spContext) throws PortletRegistryException {
    PortletWindowDataImpl portletWindowData = new PortletWindowDataImpl();
    portletWindowData.init(request, portletRegistryContext, portletContent.getPortletWindowName());

    if (!portletContent.isInMinimizedWindowState()) {
      portletWindowData.setContent(portletContent.getContent());
    }
    portletWindowData.setTitle(portletContent.getTitle());
    portletWindowData.setCurrentMode(portletContent.getPortletWindowMode());
    portletWindowData.setCurrentWindowState(portletContent.getPortletWindowState());
    if (spContext.startsWith("space")) {
      portletWindowData.setSpaceId(spContext);
    }
    if (isSpaceFrontOffice(request) || isAnonymousUser()) {
      portletWindowData.setEdit(false);
      portletWindowData.setHelp(false);
      portletWindowData.setRemove(false);
      portletWindowData.setRole(null);
    } else {
      portletWindowData.setRole("admin");
    }

    return portletWindowData;
  }

  protected String getPresentationURI(final HttpServletRequest request) {
    String spaceId = getSpaceId(request);

    if (!isDefined(spaceId) || isSpaceBackOffice(request)) {
      request.setAttribute("SpaceId", spaceId);

      if (isAnonymousUser()) {
        request.setAttribute("DisableMove", Boolean.TRUE);
      }

      return "/portlet/jsp/jsr/desktop.jsp";
    } else {
      request.setAttribute("DisableMove", Boolean.TRUE);
      return "/portlet/jsp/jsr/spaceDesktop.jsp";
    }
  }

  private boolean isAnonymousUser() {
    return UserDetail.getCurrentRequester().isAnonymous();
  }

  private void setUserIdAndSpaceIdInRequest(String spaceId, String userId,
      final HttpServletRequest request) {
    request.setAttribute("SpaceId", spaceId);
    request.setAttribute("UserId", userId);
  }

  private String prefixSpaceId(String spaceId) {
    String id = spaceId;
    if (isDefined(id)) {
      // Display the space homepage
      if (id.startsWith("WA")) {
        id = id.substring("WA".length());
      }

      if (!id.startsWith("space")) {
        id = "space" + id;
      }
    }
    return id;
  }

  /**
   * Gets the identifier of the selected space from the request. If the space is the user personal
   * one or it is not of type portlet, then null is returned (no specific space)
   *
   * @param request the HTTP request.
   * @return the space identifier or null if the space hasn't to be taken into account in the
   * portlets rendering.
   */
  private String getSpaceId(final HttpServletRequest request) {
    String spaceId = request.getParameter(WindowInvokerConstants.DRIVER_SPACEID);

    if (!isDefined(spaceId)) {
      spaceId = request.getParameter("SpaceId");

      if (isDefined(spaceId)) {
        if (SpaceInst.PERSONAL_SPACE_ID.equals(spaceId)) {
          return null;
        }
        SpaceInst spaceStruct =
            getOrganizationController().getSpaceInstById(spaceId);
        // Page d'accueil de l'espace = Portlet ?
        if (spaceStruct == null || spaceStruct.getFirstPageType() != SpaceInst.FP_TYPE_PORTLET) {
          return null;
        }
      }
    }

    return prefixSpaceId(spaceId);
  }

  private MainSessionController getMainSessionController(final HttpServletRequest request) {
    HttpSession session = request.getSession();
    MainSessionController m_MainSessionCtrl =
        (MainSessionController) session.getAttribute(
        MainSessionController.MAIN_SESSION_CONTROLLER_ATT);

    return m_MainSessionCtrl;
  }

  private String getDefaultSpaceHomepageURL(final HttpServletRequest request) {
    HttpSession session = request.getSession();
    GraphicElementFactory gef =
        (GraphicElementFactory) session.getAttribute(GraphicElementFactory.GE_FACTORY_SESSION_ATT);
    SettingBundle settings = gef.getFavoriteLookSettings();
    if (settings != null) {
      return settings.getString("spaceHomepage", null);
    }
    return null;
  }

  private OrganizationController getOrganizationController() {
    return OrganizationControllerProvider.getOrganisationController();
  }

  private boolean isSpaceBackOffice(final HttpServletRequest request) {
    return (isDefined(getSpaceId(request)) && "admin".equalsIgnoreCase(request
        .getParameter(WindowInvokerConstants.DRIVER_ROLE)));
  }

  private boolean isSpaceFrontOffice(final HttpServletRequest request) {
    String spaceId = getSpaceId(request);
    return (isDefined(spaceId) && !isDefined(request.getParameter(
        WindowInvokerConstants.DRIVER_ROLE)));
  }

  private String getSpaceHomepageURL(String spaceId, final HttpServletRequest request)
      throws UnsupportedEncodingException {
    OrganizationController organizationCtrl = getOrganizationController();
    SpaceInst spaceStruct = organizationCtrl.getSpaceInstById(spaceId);

    if (spaceStruct != null) {
      MainSessionController m_MainSessionCtrl = getMainSessionController(request);
      String userId = m_MainSessionCtrl.getUserId();

      // Force context reset
      GraphicElementFactory gef =
          (GraphicElementFactory) request.getSession().getAttribute(
              GraphicElementFactory.GE_FACTORY_SESSION_ATT);
      gef.setSpaceIdForCurrentRequest(spaceId);
      gef.setComponentIdForCurrentRequest(null);

      // Maintenance Mode
      UserDetail currentUser = UserDetail.getCurrentRequester();
      if (m_MainSessionCtrl.isSpaceInMaintenance(spaceId) &&
          (currentUser.isAccessUser() || currentUser.isAccessGuest())) {
        return URLUtil.getApplicationURL() + "/admin/jsp/spaceInMaintenance.jsp";
      }

      String defaultSpaceHomepageURL = getDefaultSpaceHomepageURL(request);
      if (StringUtil.isDefined(defaultSpaceHomepageURL)) {
        defaultSpaceHomepageURL =
            addSpaceIdToURL(URLUtil.getApplicationURL() + defaultSpaceHomepageURL, spaceId);
      }

      // Default home page
      if (spaceStruct.getFirstPageType() == SpaceInst.FP_TYPE_STANDARD) {
        return defaultSpaceHomepageURL;
      }

      // Home page = one app
      if (spaceStruct.getFirstPageType() == SpaceInst.FP_TYPE_COMPONENT_INST
          && isDefined(spaceStruct.getFirstPageExtraParam())) {
        String componentId = spaceStruct.getFirstPageExtraParam();
        if (organizationCtrl.isComponentAvailable(componentId, userId)) {
          return URLUtil.getApplicationURL() + URLUtil.getURL("useless", componentId) + "Main";
        } else {
          // component does not exist anymore or component is not available to current user
          // so default page is used
          return defaultSpaceHomepageURL;
        }
      }

      // Home page = custom URL
      if (spaceStruct.getFirstPageType() == SpaceInst.FP_TYPE_HTML_PAGE
          && isDefined(spaceStruct.getFirstPageExtraParam())) {
        String s_sUserLogin = "%ST_USER_LOGIN%";
        String s_sUserPassword = "%ST_USER_PASSWORD%";
        String s_sUserEmail = "%ST_USER_EMAIL%";
        String s_sUserFirstName = "%ST_USER_FIRSTNAME%";
        String s_sUserLastName = "%ST_USER_LASTNAME%";
        String s_sUserFullName = "%ST_USER_FULLNAME%";
        String s_sUserId = "%ST_USER_ID%";
        String s_sSessionId = "%ST_SESSION_ID%";

        String destination = spaceStruct.getFirstPageExtraParam();
        destination =
            getParsedDestination(destination, s_sUserLogin, m_MainSessionCtrl.
            getCurrentUserDetail().
            getLogin());
        destination =
            getParsedDestination(destination, s_sUserFullName,
            URLEncoder.encode(m_MainSessionCtrl.getCurrentUserDetail().getDisplayedName(),
            "UTF-8"));
        destination =
            getParsedDestination(destination, s_sUserId, URLEncoder.encode(m_MainSessionCtrl.
            getUserId(), "UTF-8"));
        destination =
            getParsedDestination(destination, s_sSessionId,
            URLEncoder.encode(request.getSession().
            getId(), "UTF-8"));
        destination =
            getParsedDestination(destination, s_sUserEmail, m_MainSessionCtrl.
            getCurrentUserDetail().
            geteMail());
        destination =
            getParsedDestination(destination, s_sUserFirstName,
            URLEncoder.encode(m_MainSessionCtrl.getCurrentUserDetail().getFirstName(), "UTF-8"));
        destination =
            getParsedDestination(destination, s_sUserLastName,
            URLEncoder.encode(m_MainSessionCtrl.getCurrentUserDetail().getLastName(), "UTF-8"));

        // !!!! Add the password : this is an uggly patch that use a session variable set in the
        // "AuthenticationServlet" servlet
        destination =
            this.getParsedDestination(destination, s_sUserPassword,
            (String) request.getSession().
            getAttribute("Silverpeas_pwdForHyperlink"));

        return addSpaceIdToURL(destination, spaceId);
      }
    }
    return null;
  }

  private String addSpaceIdToURL(String url, String spaceId) {
    return url + (!url.contains("?") ? "?" : "&") + "SpaceId=" + spaceId;
  }

  private String getParsedDestination(String sDestination, String sKeyword, String sValue) {
    int nLoginIndex = sDestination.indexOf(sKeyword);
    if (nLoginIndex != -1) {
      // Replace the keyword with the actual value
      String sParsed = sDestination.substring(0, nLoginIndex);
      sParsed += sValue;
      if (sDestination.length() > nLoginIndex + sKeyword.length()) {
        sParsed += sDestination.substring(
            nLoginIndex + sKeyword.length(),
            sDestination.length());
      }
      sDestination = sParsed;
    }
    return sDestination;
  }


  /**
   * Set the existing portletNames in session
   * @param request
   * @param language
   * @param portletNames
   */
  private void setPortletNames(final HttpServletRequest request, final String language,
      final List<String> portletNames) {
    HttpSession session = request.getSession(true);
    LocalizationBundle resource = ResourceLocator.getLocalizationBundle("org.silverpeas.portlet.multilang.portletBundle", language);
    Collection<Map> listPortlet = new ArrayList<>();//list of HashMap key=portletName, value=title of the portlet
    for(String portletName : portletNames) {
      String shortName = portletName.substring(11); //portletName = silverpeas.LastPublicationsPortlet
      String defaultTitle;
      try {
        defaultTitle = resource.getString("portlet." + shortName +
            ".title"); //shortName = LastPublicationsPortlet, key = portlet.LastPublicationsPortlet.title

      } catch (MissingResourceException ex) {
        defaultTitle = "";
      }
      Map<String, String> hashPortlet = new HashMap<String, String>();//key=portletName, value=title of the portlet
      hashPortlet.put(portletName, defaultTitle);
      listPortlet.add(hashPortlet);
    }

    session.removeAttribute(DesktopConstants.AVAILABLE_PORTLET_WINDOWS);
    session.setAttribute(DesktopConstants.AVAILABLE_PORTLET_WINDOWS, listPortlet);
  }
}
