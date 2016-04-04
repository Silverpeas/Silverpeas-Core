/*
 * Copyright (C) 2000 - 2016 Silverpeas
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
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.silverpeas.web.portlets.portal;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.silverpeas.core.web.portlets.portal.PortletWindowData;
import com.sun.portal.container.ChannelMode;
import com.sun.portal.container.ChannelState;
import com.sun.portal.portletcontainer.admin.PortletUndeployerInfo;
import com.sun.portal.portletcontainer.common.PortletContainerConstants;
import com.sun.portal.portletcontainer.context.registry.PortletRegistryContext;
import com.sun.portal.portletcontainer.context.registry.PortletRegistryContextAbstractFactory;
import com.sun.portal.portletcontainer.context.registry.PortletRegistryContextFactory;
import com.sun.portal.portletcontainer.context.registry.PortletRegistryException;
import com.sun.portal.portletcontainer.driver.admin.AdminConstants;
import com.sun.portal.portletcontainer.invoker.WindowInvokerConstants;

/**
 * DriverUtil has utility methods needed for the driver
 */
public class DriverUtil {

  private static final Logger logger = Logger.getLogger("org.silverpeas.web.portlets.portal",
      "org.silverpeas.portlets.PCDLogMessages");
  private static int renderParameterPrefixLength =
      PortletContainerConstants.RENDER_PARAM_PREFIX.length();
  private static int scopedAttributesPrefixLength =
      PortletContainerConstants.SCOPED_ATTRIBUTES_PREFIX.length();

  public static void init(HttpServletRequest request) {
    initParamMap(request);
    removeUnusedObjects(request);
  }

  public static String getAdminURL(HttpServletRequest request) {
    StringBuilder urlBuilder = new StringBuilder();
    urlBuilder.append(request.getScheme());
    urlBuilder.append("://");
    urlBuilder.append(request.getServerName());
    urlBuilder.append(":");
    urlBuilder.append(request.getServerPort());
    urlBuilder.append(request.getContextPath());
    urlBuilder.append("/portletAdmin");
    return urlBuilder.toString();
  }

  public static String getDeployerURL(HttpServletRequest request) {
    StringBuilder urlBuilder = new StringBuilder();
    urlBuilder.append(request.getScheme());
    urlBuilder.append("://");
    urlBuilder.append(request.getServerName());
    urlBuilder.append(":");
    urlBuilder.append(request.getServerPort());
    urlBuilder.append(request.getContextPath());
    urlBuilder.append("/portletDeployer");
    return urlBuilder.toString();
  }

  public static String getPortletsURL(HttpServletRequest request) {
    StringBuilder urlBuilder = new StringBuilder();
    urlBuilder.append(request.getScheme());
    urlBuilder.append("://");
    urlBuilder.append(request.getServerName());
    urlBuilder.append(":");
    urlBuilder.append(request.getServerPort());
    urlBuilder.append(request.getContextPath());
    urlBuilder.append("/dt");
    return urlBuilder.toString();
  }

  public static String getWSRPURL(HttpServletRequest request) {
    StringBuilder urlBuilder = new StringBuilder();
    urlBuilder.append(request.getScheme());
    urlBuilder.append("://");
    urlBuilder.append(request.getServerName());
    urlBuilder.append(":");
    urlBuilder.append(request.getServerPort());
    urlBuilder.append(request.getContextPath());
    urlBuilder.append("/rdt");
    return urlBuilder.toString();
  }

  public static String getWSRPTabName() {
    return DesktopMessages.getLocalizedString(AdminConstants.WSRP_TAB);
  }

  public static boolean isWSRPAvailable() {
    boolean available = false;
    try {
      // If following is successful, that means, we have wsrp consumer available
      Class.forName("com.sun.portal.wsrp.consumer.markup.WSRPContainer");
      available = true;
    } catch (Exception e) {
      logger.finest("PSPCD_CSPPD0034");
    }
    return available;
  }

  public static String getDriverAction(HttpServletRequest request) {
    return request.getParameter(WindowInvokerConstants.DRIVER_ACTION);
  }

  public static String getPortletRemove(HttpServletRequest request) {
    return request.getParameter(WindowInvokerConstants.PORTLET_REMOVE_KEY);
  }

  public static String getPortletWindowFromRequest(HttpServletRequest request) {
    return request.getParameter(WindowInvokerConstants.PORTLET_WINDOW_KEY);
  }

  private static String getPortletWindowModeFromRequest(HttpServletRequest request) {
    return request.getParameter(WindowInvokerConstants.PORTLET_WINDOW_MODE_KEY);
  }

  private static String getPortletWindowStateFromRequest(HttpServletRequest request) {
    return request.getParameter(WindowInvokerConstants.PORTLET_WINDOW_STATE_KEY);
  }

  public static ChannelMode getPortletWindowModeOfPortletWindow(HttpServletRequest request,
      String portletWindowName) {
    String portletWindowMode = getPortletWindowModeFromRequest(request);
    String portletWindowNameInRequest = getPortletWindowFromRequest(request);
    if (portletWindowMode != null && portletWindowName.equals(portletWindowNameInRequest)) {
      return new ChannelMode(portletWindowMode);
    }
    return null;
  }

  public static ChannelState getPortletWindowStateOfPortletWindow(HttpServletRequest request,
      String portletWindowName) {
    String portletWindowState = getPortletWindowStateFromRequest(request);
    String portletWindowNameInRequest = getPortletWindowFromRequest(request);
    if (portletWindowState != null && portletWindowName.equals(portletWindowNameInRequest)) {
      return new ChannelState(portletWindowState);
    }
    return null;
  }

  /*
   * Remove the driver params and retains rest of the params
   */
  private static void initParamMap(HttpServletRequest request) {
    Map<String, String[]> parsedMap = new HashMap<String, String[]>();
    Map<String, String[]> parameterMap = request.getParameterMap();

    Set<Map.Entry<String, String[]>> entries = parameterMap.entrySet();
    for (Map.Entry<String, String[]> mapEntry : entries) {
      String key = mapEntry.getKey();
      if (!key.startsWith(WindowInvokerConstants.DRIVER_PARAM_PREFIX)
          && (!key.startsWith(WindowInvokerConstants.KEYWORD_PREFIX))) {
        parsedMap.put(key, mapEntry.getValue());
      }
    }
    String query = request.getQueryString();
    if (query != null) {
      int index = query.indexOf("?");
      if (index != -1) {
        String queryString = query.substring(index + 1).replace('?', '&');
        StringTokenizer andTokens = new StringTokenizer(queryString, "&");
        while (andTokens.hasMoreTokens()) {
          StringTokenizer equalTokens = new StringTokenizer(andTokens.nextToken(), "=");
          if (equalTokens.countTokens() == 2) {
            String key = equalTokens.nextToken();
            String value = equalTokens.nextToken();
            String[] values = parsedMap.get(key);
            if (values != null) {
              List<String> list = new ArrayList(Arrays.asList(values));
              list.add(value);
              values = list.toArray(new String[0]);
            } else {
              values = new String[1];
              values[0] = value;
            }
            parsedMap.put(key, values);
          } else {
            logger.log(Level.WARNING, "PSPCD_CSPPD0026", queryString);
            break;
          }
        }
      }
    }

    // Get charset and decode the parameters
    String reqEncoding = I18n.DEFAULT_CHARSET;
    if (request.getCharacterEncoding() != null) {
      reqEncoding = request.getCharacterEncoding();
    }
    parsedMap = decodeParams(reqEncoding, parsedMap);

    request.setAttribute(WindowInvokerConstants.PORTLET_PARAM_MAP, parsedMap);
  }

  private static void removeUnusedObjects(HttpServletRequest request) {
    HttpSession session = request.getSession(true);
    List<String> undeployedPortlets = null;
    PortletUndeployerInfo portletUndeployerInfo;
    try {
      portletUndeployerInfo = new PortletUndeployerInfo();
      undeployedPortlets = portletUndeployerInfo.read();
    } catch (PortletRegistryException pre) {
      logger.log(Level.WARNING, "PSPCD_CSPPD0027", pre);
    }
    if (undeployedPortlets != null && !undeployedPortlets.isEmpty()) {
      // Remove the render parameters for the undeployed portlets from the session
      Enumeration attrNames = session.getAttributeNames();
      String attrName, entityId, warName;
      while (attrNames.hasMoreElements()) {
        attrName = (String) attrNames.nextElement();
        int index = attrName.indexOf(PortletContainerConstants.RENDER_PARAM_PREFIX);
        if (index != -1) {
          entityId = attrName.substring(renderParameterPrefixLength);
          try {
            int delimiter = entityId.indexOf("|");
            if (delimiter != -1) {
              warName = entityId.substring(0, delimiter);
              if (undeployedPortlets.contains(warName)) {
                session.removeAttribute(attrName);
              }
            }
          } catch (Exception e) {
            // If the war name is not present, exception is thrown, ignore it
            // as some entity ids may not contain warname, for example WSRP portlet windows
          }
        }
        index = attrName.indexOf(PortletContainerConstants.SCOPED_ATTRIBUTES_PREFIX);
        if (index != -1) {
          entityId = attrName.substring(scopedAttributesPrefixLength);
          try {
            int delimiter = entityId.indexOf("|");
            if (delimiter != -1) {
              warName = entityId.substring(0, delimiter);
              if (undeployedPortlets.contains(warName)) {
                session.removeAttribute(attrName);
              }
            }
          } catch (Exception e) {
            // If the war name is not present, exception is thrown, ignore it
            // as some entity ids may not contain warname, for example WSRP portlet windows
          }
        }
      }
      // Remove PortletWindowData Object for the undeployed portlets from the session
      removePortletWindowData(session, undeployedPortlets);
    }
  }

  // If the session contains a PortletWindowData object corresponding to the war that was
  // deployed, remove it
  private static void removePortletWindowData(HttpSession session, List<String> undeployedPortlets) {
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
          String portletName = portletWindowData.getPortletName();
          int delimiter = portletName.indexOf(".");
          if (delimiter != -1) {
            String warName = portletName.substring(0, delimiter);
            if (undeployedPortlets.contains(warName)) {
              itr.remove();
            }
          }
        }
      }
    }
  }

  /**
   * decode the request parameters using the character set
   */
  private static Map<String, String[]> decodeParams(String charset, Map<String, String[]> parsedMap) {
    Map<String, String[]> decodedMap = new HashMap<String, String[]>();
    if (parsedMap != null) {
      Set<Map.Entry<String, String[]>> entries = parsedMap.entrySet();
      for (Map.Entry<String, String[]> mapEntry : entries) {
        String key = mapEntry.getKey();
        String[] values = mapEntry.getValue();
        String decodedkey = I18n.decodeCharset(key, charset);
        for (int i = 0; i < values.length; i++) {
          values[i] = I18n.decodeCharset(values[i], charset);
        }
        // put it back in the hashmap
        decodedMap.put(decodedkey, values);
      }
    }
    return decodedMap;
  }

  public static PortletRegistryContext getPortletRegistryContext(String context)
      throws PortletRegistryException {
    PortletRegistryContextAbstractFactory afactory = new PortletRegistryContextAbstractFactory();
    PortletRegistryContextFactory factory = afactory.getPortletRegistryContextFactory();
    return factory.getPortletRegistryContext(context);
  }
}
