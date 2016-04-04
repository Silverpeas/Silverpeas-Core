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

package org.silverpeas.core.web.portlets.portal.portletwindow;

import org.silverpeas.core.web.mvc.controller.MainSessionController;
import com.sun.portal.container.*;
import com.sun.portal.portletcontainer.invoker.InvokerException;
import com.sun.portal.portletcontainer.invoker.WindowErrorCode;
import com.sun.portal.portletcontainer.invoker.WindowInvoker;
import org.silverpeas.core.util.LocalizationBundle;
import org.silverpeas.core.util.ResourceLocator;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.silverpeas.core.util.StringUtil.isDefined;

/**
 * PortletWindowInvoker, is dervied from the abstract base class WindowInvoker.
 */

public class PortletWindowInvoker extends WindowInvoker {
  public static final String JAVAX_PORTLET_TITLE = "javax.portlet.title";
  private WindowRequestReader windowRequestReader = null;

  private static final Logger logger =
      Logger.getLogger("org.silverpeas.core.web.portlets.portal.portletwindow",
      "org.silverpeas.portlets.PCDLogMessages");
  private LocalizationBundle messages =
      ResourceLocator.getLocalizationBundle("org.silverpeas.portlet.multilang.portletBundle");

  @Override
  public void init(ServletContext servletContext, HttpServletRequest request,
      HttpServletResponse response) throws InvokerException {
    super.init(servletContext, request, response);
    this.windowRequestReader = new PortletWindowRequestReader();
  }

  /**
   * Implementation of the abstract method defined in the base class. Get the user profile
   * information for the current user. The implementation uses the configured logical to physical
   * user info mapping configured at deployment time to calculate this.
   */
  @Override
  public Map getUserInfoMap(HttpServletRequest request) throws InvokerException {
    return PortletWindowInvokerUtils.getUserInfoMap(getPortletWindowContext(),
        getPortletWindowName());
  }

  /**
   * Implementation of the abstract method defined in the base class. Gets the container
   * implementation configured to be used
   */
  @Override
  public Container getContainer() {
    return ContainerFactory.getContainer(ContainerType.PORTLET_CONTAINER);
  }

  /**
   * Implementation of the abstract method defined in the base class. EntityID is represented as
   * <web application name>/<portlet name>/<portletWindow name> <web application name>/<portlet
   * name> is stored in the display profile during deployment time.
   */
  @Override
  public EntityID getEntityID(HttpServletRequest request) throws InvokerException {
    return PortletWindowInvokerUtils.getEntityID(getPortletWindowContext(), getPortletWindowName());
  }

  /**
   * Implementation of the abstract method defined in the base class.
   */
  @Override
  public ChannelURLFactory getPortletWindowURLFactory(String desktopURLPrefix,
      HttpServletRequest request) throws InvokerException {
    return new PortletWindowURLFactory(desktopURLPrefix);
  }

  /**
   * Implementation of the abstract method defined in the base class.
   */

  @Override
  public WindowRequestReader getWindowRequestReader() throws InvokerException {
    return windowRequestReader;
  }

  /**
   * Implementation of the abstract method defined in the base class.
   */
  @Override
  public boolean isMarkupSupported(String contentType, String locale, ChannelMode mode,
      ChannelState state)
      throws InvokerException {
    boolean supported = false;
    List supportedContentTypes;
    try {
      supportedContentTypes = getPortletWindowContext().getMarkupTypes(getPortletWindowName());
      if (ChannelMode.VIEW.equals(mode)) {
        if (supportedContentTypes.contains(contentType) || supportedContentTypes.contains("text/*")
            || supportedContentTypes.contains("*/*")) {
          supported = true;
        }
      } else if (ChannelMode.HELP.equals(mode)) {
        if (supportedContentTypes.contains(contentType)) {
          supported = true;
        }
      } else if (ChannelMode.EDIT.equals(mode)) {
        if (supportedContentTypes.contains(contentType)) {
          supported = true;
        }
      }
    } catch (PortletWindowContextException ex) {
      logger.log(Level.SEVERE, ex.getMessage(), ex);
    }
    return supported;
  }

  @Override
  public String getDefaultTitle() throws InvokerException {
    // First obtain title from portlet window registry
    String title = PortletWindowInvokerUtils.getPortletWindowTitle(
        getPortletWindowContext(), getPortletWindowName());
    // if resource bundle does not contain the title, then
    // obtain the default title from portlet app registry
    if (title == null) {
      logger.log(Level.INFO, "PSPCD_CSPPD0019", getPortletWindowName());
      title =
          PortletWindowInvokerUtils.getPortletTitle(getPortletWindowName(),
          getPortletWindowContext());
    }
    return title;
  }

  @Override
  public String getTitle() throws InvokerException {
    if (!isDefined(super.getTitle()) || super.getTitle().equals(getDefaultTitle())) {
      MainSessionController sessionController =
          (MainSessionController) getOriginalRequest().getSession().
          getAttribute(MainSessionController.MAIN_SESSION_CONTROLLER_ATT);
      messages =
          ResourceLocator.getLocalizationBundle("org.silverpeas.portlet.multilang.portletBundle",
              sessionController.getFavoriteLanguage());
      String portletTitle = getDefaultTitle();
      String translation;
      try {
        translation = messages.getString(portletTitle);
      } catch (MissingResourceException ex) {
        translation = portletTitle;
      }
      setTitle(translation);
    }
    return super.getTitle();
  }

  /**
   * Implementation of the abstract method defined in the base class. Get the list of logical roles
   * that the current user belongs to. The implementation uses the configured logical to physical
   * role mapping configured at deployment time to calculate this.
   */
  @Override
  public List getRoleList(HttpServletRequest request) throws InvokerException {
    //
    // set logical roles only if roleMapping is not empty
    //
    List rolesList = null;
    try {
      Map roleMap = getConfiguredRoleMap();
      if (roleMap != null) {
        List<String> roles = getPortletWindowContext().getRoles();
        if (roles.isEmpty()) {
          rolesList = Collections.EMPTY_LIST;
        } else {
          // Before comparing roles convert the roles obtained from WebContainer
          // and the roles obtained from registry to lowercase.
          Map lowerCaseRoleMap = convertMapToLowerCase(roleMap);
          List lowerCaseWebContainerRoles = convertListToLowerCase(roles);
          rolesList = new ArrayList();
          Iterator itr = lowerCaseWebContainerRoles.iterator();
          while (itr.hasNext()) {
            String webcontainerRole = (String) itr.next();
            if (lowerCaseRoleMap.containsKey(webcontainerRole)) {
              String logicalRole = (String) lowerCaseRoleMap.get(webcontainerRole);
              rolesList.add(logicalRole);
            }
          }
        }
      } else {
        rolesList = Collections.EMPTY_LIST;
      }
    } catch (InvokerException pce) {
      throw new InvokerException("PortletWindowInvoker.getRoleList():"
          + " couldn't check for exists on roleMap collection", pce);
    }
    return rolesList;
  }

  private List convertListToLowerCase(List list) {
    List lowerCaseKeys = new ArrayList();
    Iterator itr = list.iterator();
    while (itr.hasNext()) {
      String role = (String) itr.next();
      lowerCaseKeys.add(role.toLowerCase());
    }
    return lowerCaseKeys;
  }

  private Map convertMapToLowerCase(Map map) {
    Map lowerCaseMap = new HashMap();
    Iterator itr = map.entrySet().iterator();
    Map.Entry mapEntry;
    String key;
    while (itr.hasNext()) {
      mapEntry = (Map.Entry) itr.next();
      key = (String) mapEntry.getKey();
      lowerCaseMap.put(key.toLowerCase(), mapEntry.getValue());
    }
    return lowerCaseMap;
  }

  private Map getConfiguredRoleMap() throws InvokerException {
    return PortletWindowInvokerUtils.getRoleMap(getPortletWindowContext(), getPortletWindowName());
  }

  @Override
  protected ErrorCode getErrorCode(ContentException ex) {
    return WindowErrorCode.CONTENT_EXCEPTION;
  }
}
