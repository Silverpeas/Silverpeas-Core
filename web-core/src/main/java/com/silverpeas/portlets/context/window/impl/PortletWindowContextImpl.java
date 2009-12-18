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

package com.silverpeas.portlets.context.window.impl;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.security.Principal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.portlet.PortletException;
import javax.portlet.PortletPreferences;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import com.silverpeas.util.StringUtil;
import com.sun.portal.container.ContainerLogger;
import com.sun.portal.container.EntityID;
import com.sun.portal.container.PortletLang;
import com.sun.portal.container.PortletType;
import com.sun.portal.container.PortletWindowContext;
import com.sun.portal.container.PortletWindowContextException;
import com.sun.portal.container.service.EventHolder;
import com.sun.portal.container.service.PortletDescriptorHolder;
import com.sun.portal.container.service.PortletDescriptorHolderFactory;
import com.sun.portal.container.service.PublicRenderParameterHolder;
import com.sun.portal.container.service.policy.DistributionType;
import com.sun.portal.portletcontainer.context.registry.PortletRegistryContext;
import com.sun.portal.portletcontainer.context.registry.PortletRegistryContextAbstractFactory;
import com.sun.portal.portletcontainer.context.registry.PortletRegistryContextFactory;
import com.sun.portal.portletcontainer.context.registry.PortletRegistryException;

/**
 * The <code>PortletWindowContextImpl</code> class provides a default implementation for the
 * <code>PortletWindowContext</code> interface.
 */
public class PortletWindowContextImpl implements PortletWindowContext {

  private PortletRegistryContext portletRegistryContext;
  private HttpServletRequest request;
  private static final String ENC = "UTF-8";
  private static final String PORTLET_HANDLE_PREF_NAME = "portletHandle";
  private static String AUTHLESS_USER_ID = "NONE";
  private static final String IS_WSRP_REQ = "is.wsrp.request";
  private static Logger logger = ContainerLogger.getLogger(PortletWindowContextImpl.class,
      "com.silverpeas.portlets.PCCTXLogMessages");

  private static List<String> roles = Arrays.asList("role1", "role2", "role3",
      "role4", "role5", "role6",
      "role7", "role8", "role9");
  private String elementId = null;

  public PortletWindowContextImpl() {
  }

  public PortletWindowContextImpl(String userID) {
    this.elementId = userID;
  }

  public void init(HttpServletRequest request) {
    this.request = request;

    // retrieve userId from session
    /*
     * HttpSession session = request.getSession(); MainSessionController m_MainSessionCtrl =
     * (MainSessionController) session.getAttribute("SilverSessionController"); if
     * (m_MainSessionCtrl != null) this.elementId = m_MainSessionCtrl.getUserId();
     */

    String elementId = (String) request.getAttribute("SpaceId");
    if (!StringUtil.isDefined(elementId))
      elementId = (String) request.getAttribute("UserId");

    this.elementId = elementId;

    try {
      PortletRegistryContextAbstractFactory afactory = new PortletRegistryContextAbstractFactory();
      PortletRegistryContextFactory factory = afactory.getPortletRegistryContextFactory();
      this.portletRegistryContext = factory.getPortletRegistryContext(this.elementId);
    } catch (PortletRegistryException pre) {
      logger.log(Level.SEVERE, "PSPL_PCCTXCSPPCI0012", pre);
    }
  }

  public String getDesktopURL(HttpServletRequest request) {
    StringBuffer requestURL = request.getRequestURL();
    return requestURL.toString();
  }

  public String getDesktopURL(HttpServletRequest request, String query, boolean escape) {
    StringBuffer urlBuffer = new StringBuffer(getDesktopURL(request));
    if (query != null && query.length() != 0) {
      urlBuffer.append("?").append(query);
    }
    String url = urlBuffer.toString();
    if (escape) {
      try {
        url = URLEncoder.encode(url, ENC);
      } catch (UnsupportedEncodingException ex) {
        // ignore
      }
    }
    return url;
  }

  public String getLocaleString() {
    Locale locale = getLocale();
    return locale.toString();
  }

  public Locale getLocale() {
    return request.getLocale();
  }

  public String getContentType() {
    String contentType = "text/html";
    return contentType;
  }

  public String encodeURL(String url) {
    try {
      return URLEncoder.encode(url, ENC);
    } catch (UnsupportedEncodingException usee) {
      return url;
    }
  }

  public boolean isAuthless(HttpServletRequest request) {
    // For now authless can also edit , hence return false
    return false;
  }

  public String getAuthenticationType() {
    return request.getAuthType();
  }

  public String getUserID() {
    // The order in which this is suppose to be done
    // 1. Check if userID is explicity set , If yes use it always
    // 2. If user ID is null, Get from request - getPrincipal
    // 3. If userID is null, see if wsrp is sending it (in case of resource URL)
    // 4. else retun null.
    if (elementId == null) {
      Principal principal = request.getUserPrincipal();
      if (principal != null) {
        elementId = principal.getName();
      } else {
        elementId = request.getParameter("wsrp.userID");
      }
    }
    return elementId;
  }

  public Object getProperty(String name) {
    Object value = null;
    if (request != null) {
      HttpSession session = request.getSession(false);
      if (session != null)
        value = session.getAttribute(name);
    }
    return value;
  }

  public void setProperty(String name, Object value) {
    if (request != null) {
      request.getSession(true).setAttribute(name, value);
    }
  }

  public List getRoles() {
    // Check if any in the roles is in role
    List<String> currentRoles = new ArrayList();
    for (String role : roles) {
      if (this.request.isUserInRole(role)) {
        currentRoles.add(role);
      }
    }
    return currentRoles;
  }

  public Map<String, String> getUserInfo() {
    // TODO
    return Collections.EMPTY_MAP;
  }

  public List getMarkupTypes(String portletName) throws PortletWindowContextException {
    try {
      return portletRegistryContext.getMarkupTypes(portletName);
    } catch (PortletRegistryException pre) {
      throw new PortletWindowContextException(pre.getMessage());
    }
  }

  public String getDescription(String portletName, String desiredLocale)
      throws PortletWindowContextException {
    try {
      return portletRegistryContext.getDescription(portletName, desiredLocale);
    } catch (PortletRegistryException pre) {
      throw new PortletWindowContextException(pre.getMessage());
    }
  }

  public String getShortTitle(String portletName, String desiredLocale)
      throws PortletWindowContextException {
    try {
      return portletRegistryContext.getShortTitle(portletName, desiredLocale);
    } catch (PortletRegistryException pre) {
      throw new PortletWindowContextException(pre.getMessage());
    }
  }

  public String getTitle(String portletName, String desiredLocale)
      throws PortletWindowContextException {
    try {
      return portletRegistryContext.getTitle(portletName, desiredLocale);
    } catch (PortletRegistryException pre) {
      throw new PortletWindowContextException(pre.getMessage());
    }
  }

  public List getKeywords(String portletName, String desiredLocale)
      throws PortletWindowContextException {
    try {
      return portletRegistryContext.getKeywords(portletName, desiredLocale);
    } catch (PortletRegistryException pre) {
      throw new PortletWindowContextException(pre.getMessage());
    }
  }

  public String getDisplayName(String portletName, String desiredLocale)
      throws PortletWindowContextException {
    try {
      return portletRegistryContext.getDisplayName(portletName, desiredLocale);
    } catch (PortletRegistryException pre) {
      throw new PortletWindowContextException(pre.getMessage());
    }
  }

  public String getPortletName(String portletWindowName) throws PortletWindowContextException {
    try {
      return portletRegistryContext.getPortletName(portletWindowName);
    } catch (PortletRegistryException pre) {
      throw new PortletWindowContextException(pre.getMessage());
    }
  }

  public List<EntityID> getPortletWindows(PortletType portletType, DistributionType distributionType)
      throws PortletWindowContextException {
    List<EntityID> portletList = new ArrayList();
    try {
      List<String> portlets = null;
      if (DistributionType.ALL_PORTLETS.equals(distributionType)) {
        portlets = getAllPortletWindows(portletType);
      } else if (DistributionType.ALL_PORTLETS_ON_PAGE.equals(distributionType)) {
        portlets = getAvailablePortletWindows(portletType);
      } else if (DistributionType.VISIBLE_PORTLETS_ON_PAGE.equals(distributionType)) {
        portlets = getVisiblePortletWindows(portletType);
      }
      if (portlets != null) {
        for (String portletWindowName : portlets) {
          portletList.add(getEntityID(portletWindowName));
        }
      }
    } catch (PortletWindowContextException pre) {
      logger.log(Level.SEVERE, "PSPL_PCCTXCSPPCI0011", pre);
    }
    return portletList;
  }

  private List getVisiblePortletWindows(PortletType portletType)
      throws PortletWindowContextException {
    try {
      return portletRegistryContext.getVisiblePortletWindows(portletType);
    } catch (PortletRegistryException pre) {
      throw new PortletWindowContextException(pre.getMessage());
    }
  }

  private List getAvailablePortletWindows(PortletType portletType)
      throws PortletWindowContextException {
    try {
      return portletRegistryContext.getAllPortletWindows(portletType);
    } catch (PortletRegistryException pre) {
      throw new PortletWindowContextException(pre.getMessage());
    }
  }

  private List getAllPortletWindows(PortletType portletType) throws PortletWindowContextException {
    try {
      return portletRegistryContext.getAllPortletWindows(portletType);
    } catch (PortletRegistryException pre) {
      throw new PortletWindowContextException(pre.getMessage());
    }
  }

  public EntityID getEntityID(String portletWindowName) throws PortletWindowContextException {
    try {
      return portletRegistryContext.getEntityId(portletWindowName);
    } catch (PortletRegistryException pre) {
      throw new PortletWindowContextException(pre.getMessage());
    }
  }

  public String getPortletWindowTitle(String portletWindowName, String locale)
      throws PortletWindowContextException {
    try {
      // locale ignore for now.
      return portletRegistryContext.getPortletWindowTitle(portletWindowName);
    } catch (PortletRegistryException pre) {
      throw new PortletWindowContextException(pre.getMessage());
    }
  }

  public Map getRoleMap(String portletWindowName) throws PortletWindowContextException {
    try {
      return portletRegistryContext.getRoleMap(portletWindowName);
    } catch (PortletRegistryException pre) {
      throw new PortletWindowContextException(pre.getMessage());
    }
  }

  public Map getUserInfoMap(String portletWindowName) throws PortletWindowContextException {
    try {
      return portletRegistryContext.getUserInfoMap(portletWindowName);
    } catch (PortletRegistryException pre) {
      throw new PortletWindowContextException(pre.getMessage());
    }
  }

  public PortletPreferences getPreferences(String portletWindowName, ResourceBundle bundle,
      boolean isReadOnly) throws PortletWindowContextException {
    String userId = checkUserID();
    return new PortletPreferencesImpl(this.request, this.portletRegistryContext,
        getEntityID(portletWindowName), userId, bundle, isReadOnly);
  }

  private String checkUserID() {
    String userId = getUserID();
    if (userId == null) {
      userId = AUTHLESS_USER_ID;
    }
    if (isWSRPRequest()) {
      return Base64.encode(userId);
    }
    return userId;
  }

  private boolean isWSRPRequest() {
    Object isWSRPReq = request.getAttribute(IS_WSRP_REQ);
    return isWSRPReq == null ? false : true;
  }

  public EventHolder verifySupportedPublishingEvent(EntityID portletEntityId,
      EventHolder eventHolder) {
    PortletDescriptorHolder portletDescriptorHolder = getPortletDescriptorHolder();
    if (portletDescriptorHolder == null)
      return null;
    return portletDescriptorHolder.verifySupportedPublishingEvent(portletEntityId, eventHolder);
  }

  public List<EventHolder> getSupportedPublishingEventHolders(EntityID portletEntityId) {
    PortletDescriptorHolder portletDescriptorHolder = getPortletDescriptorHolder();
    if (portletDescriptorHolder == null)
      return null;
    return portletDescriptorHolder.getSupportedPublishingEventHolders(portletEntityId);
  }

  public EventHolder verifySupportedProcessingEvent(EntityID portletEntityId,
      EventHolder eventHolder) {
    PortletDescriptorHolder portletDescriptorHolder = getPortletDescriptorHolder();
    if (portletDescriptorHolder == null)
      return null;
    return portletDescriptorHolder.verifySupportedProcessingEvent(portletEntityId, eventHolder);
  }

  public List<EventHolder> getSupportedProcessingEventHolders(EntityID portletEntityId) {
    PortletDescriptorHolder portletDescriptorHolder = getPortletDescriptorHolder();
    if (portletDescriptorHolder == null)
      return null;
    return portletDescriptorHolder.getSupportedProcessingEventHolders(portletEntityId);
  }

  public Map<String, String> verifySupportedPublicRenderParameters(EntityID portletEntityId,
      List<PublicRenderParameterHolder> publicRenderParameterHolders) {
    PortletDescriptorHolder portletDescriptorHolder = getPortletDescriptorHolder();
    if (portletDescriptorHolder == null)
      return Collections.emptyMap();
    return portletDescriptorHolder.verifySupportedPublicRenderParameters(portletEntityId,
        publicRenderParameterHolders);
  }

  public List<PublicRenderParameterHolder> getSupportedPublicRenderParameterHolders(
      EntityID portletEntityId, Map<String, String[]> renderParameters) {
    PortletDescriptorHolder portletDescriptorHolder = getPortletDescriptorHolder();
    if (portletDescriptorHolder == null)
      return Collections.emptyList();
    return portletDescriptorHolder.getSupportedPublicRenderParameterHolders(portletEntityId,
        renderParameters);
  }

  private PortletDescriptorHolder getPortletDescriptorHolder() {
    PortletDescriptorHolder portletDescriptorHolder = null;
    try {
      portletDescriptorHolder = PortletDescriptorHolderFactory.getPortletDescriptorHolder();
    } catch (Exception ex) {
      logger.log(Level.SEVERE, "PSPL_PCCTXCSPPCI0010", ex);
      return null;
    }
    return portletDescriptorHolder;
  }

  public String getPortletID(String portletWindowName) throws PortletWindowContextException {
    try {
      return portletRegistryContext.getPortletID(portletWindowName);
    } catch (PortletRegistryException pre) {
      throw new PortletWindowContextException(pre.getMessage());
    }
  }

  public String getConsumerID(String portletWindowName) throws PortletWindowContextException {
    try {
      return portletRegistryContext.getConsumerID(portletWindowName);
    } catch (PortletRegistryException pre) {
      throw new PortletWindowContextException(pre.getMessage());
    }
  }

  public String getPortletHandle(String portletWindowName) throws PortletWindowContextException {
    PortletPreferences prefs = getPreferences(portletWindowName, null, true);
    return (String) prefs.getValue(PORTLET_HANDLE_PREF_NAME, null);
  }

  public void setPortletHandle(String portletWindowName, String portletHandle)
      throws PortletWindowContextException {
    PortletPreferences prefs = getPreferences(portletWindowName, null, false);
    try {
      prefs.setValue(PORTLET_HANDLE_PREF_NAME, portletHandle);
      prefs.store();
    } catch (PortletException pe) {
      throw new PortletWindowContextException(pe);
    } catch (IOException ioe) {
      throw new PortletWindowContextException(ioe);
    }
  }

  public String getProducerEntityID(String portletWindowName) throws PortletWindowContextException {
    try {
      return portletRegistryContext.getProducerEntityID(portletWindowName);
    } catch (PortletRegistryException pre) {
      throw new PortletWindowContextException(pre.getMessage());
    }
  }

  public PortletLang getPortletLang(String portletWindowName) throws PortletWindowContextException {
    try {
      return portletRegistryContext.getPortletLang(portletWindowName);
    } catch (PortletRegistryException pre) {
      throw new PortletWindowContextException(pre.getMessage());
    }
  }

  // TODO
  public void store() throws PortletWindowContextException {

  }
}
