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

package com.silverpeas.portlets.portal;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.silverpeas.util.StringUtil;
import com.stratelia.silverpeas.peasCore.MainSessionController;
import com.stratelia.silverpeas.peasCore.URLManager;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.beans.admin.OrganizationController;
import com.stratelia.webactiv.beans.admin.SpaceInst;
import com.stratelia.webactiv.util.GeneralPropertiesManager;
import com.stratelia.webactiv.util.viewGenerator.html.GraphicElementFactory;
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

public class SPDesktopServlet extends HttpServlet {

  ServletContext context;
  String spContext;

  private static Logger logger = Logger.getLogger("com.silverpeas.portlets.portal",
      "com.silverpeas.portlets.PCDLogMessages");

  /**
   * Reads the DriverConfig.properties file. Initializes the Portlet Registry files.
   * @param config the ServletConfig Object
   * @throws javax.servlet.ServletException
   */
  public void init(ServletConfig config)
      throws ServletException {
    super.init(config);
    context = config.getServletContext();
    PropertiesContext.init();
    PortletRegistryCache.init();
  }

  public void doGet(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {
    try {
      doGetPost(request, response);
    } catch (Exception e) {
      System.out.println(e);
    }
  }

  public void doPost(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {
    doGetPost(request, response);
  }

  private void doGetPost(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {

    String spaceHomePage = null;
    String spaceId = request.getParameter("SpaceId");
    request.getSession().setAttribute("Silverpeas_Portlet_SpaceId", spaceId);
    if (StringUtil.isDefined(spaceId)) {
      spaceHomePage = getSpaceHomepage(spaceId, request);
    }

    if (StringUtil.isDefined(spaceHomePage)) {
      response.sendRedirect(spaceHomePage);
    } else {
      spContext = getUserIdOrSpaceId(request, false);
      setUserIdAndSpaceIdInRequest(request);

      DesktopMessages.init(request);
      SilverTrace.debug("portlet", "SPDesktopServlet.doGetPost", "root.MSG_GEN_PARAM_VALUE",
          "DesktopMessages initialized !");
      DriverUtil.init(request);
      SilverTrace.debug("portlet", "SPDesktopServlet.doGetPost", "root.MSG_GEN_PARAM_VALUE",
          "DriverUtil initialized !");
      response.setContentType("text/html;charset=ISO-8859-1");

      // Get the list of visible portlets(sorted by the row number)
      try {
        ServletContextThreadLocalizer.set(context);
        PortletRegistryContext portletRegistryContext =
            DriverUtil.getPortletRegistryContext(spContext);
        SilverTrace.debug("portlet", "SPDesktopServlet.doGetPost", "root.MSG_GEN_PARAM_VALUE",
            "portletRegistryContext retrieved !");
        PortletContent portletContent = getPortletContentObject(context, request, response);
        String portletWindowName = DriverUtil.getPortletWindowFromRequest(request);
        String portletRemove = DriverUtil.getPortletRemove(request);
        if (portletRemove != null && portletWindowName != null) {
          portletRegistryContext.removePortletWindow(portletWindowName);
          // portletRegistryContext.showPortletWindow(portletWindowName, false);
          portletWindowName = null; // re-render all portlets
        }
        Map portletContents = null;
        if (portletWindowName == null) {
          portletContents = getAllPortletContents(request, portletContent, portletRegistryContext);
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
                getAllPortletContents(request, portletContent, portletRegistryContext);
          } else if (WindowInvokerConstants.RESOURCE.equals(driverAction)) {
            portletContent.setPortletWindowName(portletWindowName);
            ChannelMode portletWindowMode = getCurrentPortletWindowMode(request, portletWindowName);
            ChannelState portletWindowState =
                getCurrentPortletWindowState(request, portletWindowName);
            portletContent.setPortletWindowState(portletWindowState);
            portletContent.setPortletWindowMode(portletWindowMode);
            portletContent.getResources();
          }
        }
        if (portletContents != null) {
          Map<String, SortedSet<PortletWindowData>> portletWindowContents =
              getPortletWindowContents(request, portletContents, portletRegistryContext);
          setPortletWindowData(request, portletWindowContents);
          InvokerUtil.setResponseProperties(request, response, portletContent
              .getResponseProperties());
          RequestDispatcher rd = context.getRequestDispatcher(getPresentationURI(request));
          rd.forward(request, response);
          InvokerUtil.clearResponseProperties(portletContent.getResponseProperties());
        }
      } catch (Exception e) {
        SilverTrace.error("portlet", "SPDesktopServlet.doGetPost", "root.MSG_GEN_PARAM_VALUE",
            "Portlets exception !", e);
      } finally {
        ServletContextThreadLocalizer.set(null);
      }
    }
  }

  /**
   * Returns the PortletWindowData object for the portlet window. A Set of PortletWindowData for all
   * portlet windows is stored in the Session.
   * @param request the HttpServletRequest Object
   * @param portletWindowName the name of the portlet window
   * @return the PortletWindowData object for the portlet window.
   */
  private PortletWindowData getPortletWindowData(HttpServletRequest request,
      String portletWindowName) {
    HttpSession session = request.getSession(true);
    PortletWindowData portletWindowData = null;
    Map<String, SortedSet<PortletWindowData>> portletWindowContents =
        (Map) session.getAttribute(DesktopConstants.PORTLET_WINDOWS);
    boolean found = false;
    if (portletWindowContents != null) {
      Set set = portletWindowContents.entrySet();
      Iterator<Map.Entry> setItr = set.iterator();
      while (setItr.hasNext()) {
        Map.Entry<String, SortedSet<PortletWindowData>> mapEntry = setItr.next();
        SortedSet<PortletWindowData> portletWindowDataSet = mapEntry.getValue();
        for (Iterator<PortletWindowData> itr = portletWindowDataSet.iterator(); itr.hasNext();) {
          portletWindowData = itr.next();
          if (portletWindowName.equals(portletWindowData.getPortletWindowName())) {
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
      return portletWindowData;
    } else {
      return null;
    }
  }

  private void setPortletWindowData(HttpServletRequest request,
      Map<String, SortedSet<PortletWindowData>> portletWindowContents) {
    HttpSession session = request.getSession(true);
    session.removeAttribute(DesktopConstants.PORTLET_WINDOWS);
    session.setAttribute(DesktopConstants.PORTLET_WINDOWS, portletWindowContents);
  }

  /**
   * Returns a Map of portlet data and title for all portlet windows. In the portlet window is
   * maximized, only the data and title for that portlet is displayed. For any portlet window that
   * is minimized , only the title is shown.
   * @param request the HttpServletRequest Object
   * @param portletContent the PortletContent Object
   * @param portletRegistryContext the PortletRegistryContext Object
   * @return a Map of portlet data and title for all portlet windows.
   */
  private Map getAllPortletContents(HttpServletRequest request, PortletContent portletContent,
      PortletRegistryContext portletRegistryContext) throws InvokerException {
    String portletWindowName = DriverUtil.getPortletWindowFromRequest(request);
    ChannelState portletWindowState = getCurrentPortletWindowState(request, portletWindowName);
    Map portletContents;
    if (portletWindowState.equals(ChannelState.MAXIMIZED)) {
      portletContent.setPortletWindowState(ChannelState.MAXIMIZED);
      portletContents = getPortletContent(request, portletContent, portletWindowName);
    } else {
      List visiblePortletWindows = getVisiblePortletWindows(portletRegistryContext);
      int numPortletWindows = visiblePortletWindows.size();
      List portletList = new ArrayList();
      List portletMinimizedList = new ArrayList();
      for (int i = 0; i < numPortletWindows; i++) {
        portletWindowName = (String) visiblePortletWindows.get(i);
        portletWindowState = getCurrentPortletWindowState(request, portletWindowName);
        if (portletWindowState.equals(ChannelState.MINIMIZED)) {
          portletMinimizedList.add(portletWindowName);
        } else {
          portletList.add(portletWindowName);
        }
      }
      portletContents = getPortletContents(request, portletContent, portletList);
      if (!portletMinimizedList.isEmpty()) {
        Map portletTitles = getPortletTitles(request, portletContent, portletMinimizedList);
        portletContents.putAll(portletTitles);
      }
    }
    return portletContents;
  }

  /**
   * Returns a Map of portlet data and title for a portlet window.
   * @param request the HttpServletRequest Object
   * @param portletContent the PortletContent Object
   * @param portletWindowName the name of the portlet window
   * @return a Map of portlet data and title for a portlet window.
   */
  private Map getPortletContent(HttpServletRequest request, PortletContent portletContent,
      String portletWindowName) throws InvokerException {
    portletContent.setPortletWindowName(portletWindowName);
    ChannelMode portletWindowMode = getCurrentPortletWindowMode(request, portletWindowName);
    portletContent.setPortletWindowMode(portletWindowMode);
    StringBuffer buffer = portletContent.getContent();
    String title = portletContent.getTitle();
    Map portletContents = new HashMap();
    portletContents.put(DesktopConstants.PORTLET_CONTENT, buffer);
    portletContents.put(DesktopConstants.PORTLET_TITLE, title);
    Map portletContentMap = new HashMap();
    portletContentMap.put(portletWindowName, portletContents);
    return portletContentMap;
  }

  /**
   * Returns a Map of portlet data and title for the portlet windows specified in the portletList
   * @param request the HttpServletRequest Object
   * @param portletContent the PortletContent Cobject
   * @param portletList the List of portlet windows
   * @return a Map of portlet data and title for the portlet windows specified in the portletList
   */
  private Map getPortletContents(HttpServletRequest request, PortletContent portletContent,
      List portletList) throws InvokerException {
    String portletWindowName;
    int numPortletWindows = portletList.size();
    Map portletContentMap = new HashMap();
    for (int i = 0; i < numPortletWindows; i++) {
      portletWindowName = (String) portletList.get(i);
      portletContent.setPortletWindowName(portletWindowName);
      portletContent.setPortletWindowMode(getCurrentPortletWindowMode(request, portletWindowName));
      portletContent
          .setPortletWindowState(getCurrentPortletWindowState(request, portletWindowName));
      StringBuffer buffer;
      try {
        buffer = portletContent.getContent();
      } catch (InvokerException ie) {
        buffer = new StringBuffer(ie.getMessage());
      }
      String title = null;
      try {
        title = portletContent.getTitle();
      } catch (InvokerException iex) {
        // Just logging
        if (logger.isLoggable(Level.SEVERE)) {
          LogRecord logRecord = new LogRecord(Level.SEVERE, "PSPCD_CSPPD0048");
          logRecord.setLoggerName(logger.getName());
          logRecord.setThrown(iex);
          logRecord.setParameters(new String[] { portletWindowName });
          logger.log(logRecord);
        }
        title = "";
      }
      Map portletContents = new HashMap();
      portletContents.put(DesktopConstants.PORTLET_CONTENT, buffer);
      portletContents.put(DesktopConstants.PORTLET_TITLE, title);
      portletContentMap.put(portletWindowName, portletContents);
    }
    return portletContentMap;
  }

  /**
   * Returns a Map of portlet title for the portlet windows specified in the portletMinimizedList
   * @param request the HttpServletRequest Object
   * @param portletContent the PortletContent Cobject
   * @param portletMinimizedList the List of portlet windows that are minimized
   * @return a Map of portlet title for the portlet windows that are minimized.
   */
  private Map getPortletTitles(HttpServletRequest request, PortletContent portletContent,
      List portletMinimizedList) throws InvokerException {
    String portletWindowName;
    int numPortletWindows = portletMinimizedList.size();
    Map portletTitlesMap = new HashMap();
    for (int i = 0; i < numPortletWindows; i++) {
      portletWindowName = (String) portletMinimizedList.get(i);
      portletContent.setPortletWindowName(portletWindowName);
      portletContent.setPortletWindowMode(getCurrentPortletWindowMode(request, portletWindowName));
      portletContent
          .setPortletWindowState(getCurrentPortletWindowState(request, portletWindowName));
      StringBuffer buffer = null;
      String title = portletContent.getDefaultTitle();
      Map portletContents = new HashMap();
      portletContents.put(DesktopConstants.PORTLET_CONTENT, buffer);
      portletContents.put(DesktopConstants.PORTLET_TITLE, title);
      portletTitlesMap.put(portletWindowName, portletContents);
    }
    return portletTitlesMap;
  }

  /**
   * Returns a Map of PortletWindowData for the portlet windows for both thick and think widths.
   * @param request the HttpServletRequest Object
   * @param portletContents a Map of portlet data and title for the portlet windows
   * @param portletRegistryContext the PortletRegistryContext Object
   * @return a Map of PortletWindowData for the portlet windows
   */
  private Map<String, SortedSet<PortletWindowData>> getPortletWindowContents(
      HttpServletRequest request,
      Map portletContents, PortletRegistryContext portletRegistryContext) {
    Iterator itr = portletContents.keySet().iterator();
    String portletWindowName;
    SortedSet<PortletWindowData> portletWindowContentsThin = new TreeSet<PortletWindowData>();
    SortedSet<PortletWindowData> portletWindowContentsThick = new TreeSet<PortletWindowData>();
    int thinCount = 0;
    int thickCount = 0;
    while (itr.hasNext()) {
      portletWindowName = (String) itr.next();
      try {
        PortletWindowData portletWindowData =
            getPortletWindowDataObject(request, portletContents, portletRegistryContext,
            portletWindowName);

        if (portletWindowData.isThin()) {
          portletWindowContentsThin.add(portletWindowData);
          thinCount++;
        } else if (portletWindowData.isThick()) {
          portletWindowContentsThick.add(portletWindowData);
          thickCount++;
        } else {
          throw new PortletRegistryException(portletWindowName + " is neither thick or thin!!");
        }
      } catch (PortletRegistryException pre) {
        pre.printStackTrace();
      }
    }
    Map portletWindowContents = new HashMap();
    portletWindowContents.put(PortletRegistryConstants.WIDTH_THICK, portletWindowContentsThick);
    portletWindowContents.put(PortletRegistryConstants.WIDTH_THIN, portletWindowContentsThin);
    logger.log(Level.INFO, "PSPCD_CSPPD0022", new String[] { String.valueOf(thinCount),
        String.valueOf(thickCount) });

    return portletWindowContents;
  }

  private URL executeProcessAction(HttpServletRequest request, PortletContent portletContent)
      throws InvokerException {
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
   * @param portletRegistryContext the PortletRegistryContext Object
   * @return the list of visible portlet windows from the portlet registry.
   */
  protected List getVisiblePortletWindows(PortletRegistryContext portletRegistryContext)
      throws InvokerException {
    List visiblePortletWindows = null;
    try {
      visiblePortletWindows = portletRegistryContext.getVisiblePortletWindows(PortletType.LOCAL);
    } catch (PortletRegistryException pre) {
      visiblePortletWindows = Collections.EMPTY_LIST;
      throw new InvokerException("Cannot get Portlet List", pre);
    }
    return visiblePortletWindows;
  }

  /**
   * Returns the current portlet window state for the portlet window. First it checks in the request
   * and then checks in the session.
   * @param request the HttpServletRequest Object
   * @param portletWindowName the name of the portlet window
   * @return the current portlet window state for the portlet window.
   */
  protected ChannelState getCurrentPortletWindowState(HttpServletRequest request,
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
   * Returns the current portlet window mode for the portlet window. First it checks in the request
   * and then checks in the session.
   * @param request the HttpServletRequest Object
   * @param portletWindowName the name of the portlet window
   * @return the current portlet window mode for the portlet window.
   */
  protected ChannelMode getCurrentPortletWindowMode(HttpServletRequest request,
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
   * @param request the HttpServletRequest Object
   * @param portletWindowName the name of the portlet window
   * @return the portlet window state for the portlet window from session.
   */
  protected ChannelState getPortletWindowStateFromSavedData(HttpServletRequest request,
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
   * @param request the HttpServletRequest Object
   * @param portletWindowName the name of the portlet window
   * @return the portlet window mode for the portlet window from session.
   */
  protected ChannelMode getPortletWindowModeFromSavedData(HttpServletRequest request,
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

  protected PortletContent getPortletContentObject(ServletContext context,
      HttpServletRequest request, HttpServletResponse response) throws InvokerException {
    return new PortletContent(context, request, response);
  }

  protected PortletWindowData getPortletWindowDataObject(HttpServletRequest request,
      Map portletContents, PortletRegistryContext portletRegistryContext,
      String portletWindowName) throws PortletRegistryException {

    PortletWindowDataImpl portletWindowData = new PortletWindowDataImpl();
    Map portletContentMap = (Map) portletContents.get(portletWindowName);
    portletWindowData.init(request, portletRegistryContext, portletWindowName);
    portletWindowData.setContent((StringBuffer) portletContentMap
        .get(DesktopConstants.PORTLET_CONTENT));
    portletWindowData.setTitle((String) portletContentMap.get(DesktopConstants.PORTLET_TITLE));
    portletWindowData.setCurrentMode(getCurrentPortletWindowMode(request, portletWindowName));
    portletWindowData
        .setCurrentWindowState(getCurrentPortletWindowState(request, portletWindowName));
    if (spContext.startsWith("space"))
      portletWindowData.setSpaceId(getUserIdOrSpaceId(request, true));
    if (isSpaceFrontOffice(request) || isAnonymousUser(request)) {
      portletWindowData.setEdit(false);
      portletWindowData.setHelp(false);
      portletWindowData.setRemove(false);
      portletWindowData.setRole(null);
    } else {
      portletWindowData.setRole("admin");
    }

    return portletWindowData;
  }

  protected String getPresentationURI(HttpServletRequest request) {
    String spaceId = getSpaceId(request);

    if (!StringUtil.isDefined(spaceId) || isSpaceBackOffice(request)) {
      request.setAttribute("SpaceId", spaceId);

      if (isAnonymousUser(request))
        request.setAttribute("DisableMove", Boolean.TRUE);

      return "/portlet/jsp/jsr/desktop.jsp";
    } else {
      request.setAttribute("DisableMove", Boolean.TRUE);
      return "/portlet/jsp/jsr/spaceDesktop.jsp";
    }
  }

  private boolean isAnonymousUser(HttpServletRequest request) {
    HttpSession session = request.getSession();
    MainSessionController m_MainSessionCtrl =
        (MainSessionController) session.getAttribute("SilverSessionController");
    GraphicElementFactory gef =
        (GraphicElementFactory) session.getAttribute("SessionGraphicElementFactory");

    return m_MainSessionCtrl.getUserId().equals(gef.getFavoriteLookSettings().getString("guestId"));
  }

  private void setUserIdAndSpaceIdInRequest(HttpServletRequest request) {
    String spaceId = getSpaceId(request);
    request.setAttribute("SpaceId", spaceId);
    request.setAttribute("UserId", getMainSessionController(request).getUserId());
    SilverTrace.debug("portlet", "SPDesktopServlet.setUserIdAndSpaceIdInRequest", "userId = " +
        getMainSessionController(request).getUserId());
  }

  private String getUserIdOrSpaceId(HttpServletRequest request, boolean getSpaceIdOnly) {
    String spaceId = getSpaceId(request);
    if (StringUtil.isDefined(spaceId))
      return spaceId;

    return getMainSessionController(request).getUserId();
  }

  private String prefixSpaceId(String spaceId) {
    if (StringUtil.isDefined(spaceId)) {
      // Display the space homepage
      if (spaceId.startsWith("WA"))
        spaceId = spaceId.substring("WA".length());

      if (!spaceId.startsWith("space"))
        spaceId = "space" + spaceId;
    }
    return spaceId;
  }

  private String unprefixSpaceId(String spaceId) {
    if (StringUtil.isDefined(spaceId)) {
      spaceId = spaceId.substring("space".length());
    }
    return spaceId;
  }

  private String getSpaceId(HttpServletRequest request) {
    String spaceId = request.getParameter(WindowInvokerConstants.DRIVER_SPACEID);

    if (!StringUtil.isDefined(spaceId)) {
      spaceId = request.getParameter("SpaceId");

      if (StringUtil.isDefined(spaceId)) {
        MainSessionController m_MainSessionCtrl = getMainSessionController(request);

        SpaceInst spaceStruct =
            m_MainSessionCtrl.getOrganizationController().getSpaceInstById(spaceId);
        // Page d'accueil de l'espace = Portlet ?
        if (spaceStruct == null || spaceStruct.getFirstPageType() != SpaceInst.FP_TYPE_PORTLET)
          spaceId = null;
      }
    }

    return prefixSpaceId(spaceId);
  }

  private MainSessionController getMainSessionController(HttpServletRequest request) {
    HttpSession session = request.getSession();
    MainSessionController m_MainSessionCtrl =
        (MainSessionController) session.getAttribute("SilverSessionController");

    return m_MainSessionCtrl;
  }

  private OrganizationController getOrganizationController(HttpServletRequest request) {
    return getMainSessionController(request).getOrganizationController();
  }

  private boolean isSpaceBackOffice(HttpServletRequest request) {
    return (StringUtil.isDefined(getSpaceId(request)) && "admin".equalsIgnoreCase(request
        .getParameter(WindowInvokerConstants.DRIVER_ROLE)));
  }

  private boolean isSpaceFrontOffice(HttpServletRequest request) {
    return (StringUtil.isDefined(getSpaceId(request)) && !StringUtil.isDefined(request
        .getParameter(WindowInvokerConstants.DRIVER_ROLE)));
  }

  private String getSpaceHomepage(String spaceId, HttpServletRequest request)
      throws UnsupportedEncodingException {
    OrganizationController organizationCtrl = getOrganizationController(request);
    SpaceInst spaceStruct = organizationCtrl.getSpaceInstById(spaceId);

    if (spaceStruct != null) {
      MainSessionController m_MainSessionCtrl = getMainSessionController(request);
      String userId = m_MainSessionCtrl.getUserId();

      // Maintenance Mode
      if (m_MainSessionCtrl.isSpaceInMaintenance(spaceId) &&
          m_MainSessionCtrl.getUserAccessLevel().equals("U"))
        return GeneralPropertiesManager.getGeneralResourceLocator().getString("ApplicationURL") +
            "admin/jsp/spaceInMaintenance.jsp";

      // Page d'accueil de l'espace = Composant
      if (spaceStruct.getFirstPageType() == SpaceInst.FP_TYPE_COMPONENT_INST &&
          StringUtil.isDefined(spaceStruct.getFirstPageExtraParam())) {
        String componentId = spaceStruct.getFirstPageExtraParam();
        if (organizationCtrl.isComponentAvailable(componentId, userId)) {
          return GeneralPropertiesManager.getGeneralResourceLocator().getString("ApplicationURL") +
              URLManager.getURL("useless", componentId) + "Main";
        }
      }

      // Page d'accueil de l'espace = URL
      if (spaceStruct.getFirstPageType() == SpaceInst.FP_TYPE_HTML_PAGE &&
          StringUtil.isDefined(spaceStruct.getFirstPageExtraParam())) {
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
            getParsedDestination(destination, s_sUserLogin, m_MainSessionCtrl
            .getCurrentUserDetail().getLogin());
        destination =
            getParsedDestination(destination, s_sUserFullName, URLEncoder.encode(m_MainSessionCtrl
            .getCurrentUserDetail().getDisplayedName(), "ISO-8859-1"));
        destination =
            getParsedDestination(destination, s_sUserId, URLEncoder.encode(m_MainSessionCtrl
            .getUserId(), "ISO-8859-1"));
        destination =
            getParsedDestination(destination, s_sSessionId, URLEncoder.encode(request.getSession()
            .getId(), "ISO-8859-1"));
        destination =
            getParsedDestination(destination, s_sUserEmail, m_MainSessionCtrl
            .getCurrentUserDetail().geteMail());
        destination =
            getParsedDestination(destination, s_sUserFirstName, URLEncoder.encode(m_MainSessionCtrl
            .getCurrentUserDetail().getFirstName(), "ISO-8859-1"));
        destination =
            getParsedDestination(destination, s_sUserLastName, URLEncoder.encode(m_MainSessionCtrl
            .getCurrentUserDetail().getLastName(), "ISO-8859-1"));

        // !!!! Add the password : this is an uggly patch that use a session variable set in the
        // "AuthenticationServlet" servlet
        destination =
            this.getParsedDestination(destination, s_sUserPassword, (String) request.getSession()
            .getAttribute("Silverpeas_pwdForHyperlink"));

        return destination;
      }
    }
    return null;
  }

  private String getParsedDestination(String sDestination, String sKeyword, String sValue) {
    int nLoginIndex = sDestination.indexOf(sKeyword);
    if (nLoginIndex != -1) {
      // Replace the keyword with the actual value
      String sParsed = sDestination.substring(0, nLoginIndex);
      sParsed += sValue;
      if (sDestination.length() > nLoginIndex + sKeyword.length())
        sParsed += sDestination.substring(
            nLoginIndex + sKeyword.length(),
            sDestination.length());
      sDestination = sParsed;
    }
    return sDestination;
  }
}
