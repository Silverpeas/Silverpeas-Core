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

package org.silverpeas.core.web.portlets.context.window.impl;

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

import org.silverpeas.core.util.StringUtil;
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
  private static final Logger logger = ContainerLogger.getLogger(PortletWindowContextImpl.class,
      "org.silverpeas.portlets.PCCTXLogMessages");
  private static List<String> roles = Arrays.asList("role1", "role2", "role3",
      "role4", "role5", "role6",
      "role7", "role8", "role9");
  private String elementId = null;

  public PortletWindowContextImpl() {
  }

  public PortletWindowContextImpl(String userID) {
    this.elementId = userID;
  }

  @Override
  public void init(HttpServletRequest request) {
    this.request = request;
    String spaceId = (String) request.getAttribute("SpaceId");
    this.elementId = spaceId;
    if (!StringUtil.isDefined(elementId)) {
      elementId = (String) request.getAttribute("UserId");
    }

    try {
      PortletRegistryContextAbstractFactory afactory = new PortletRegistryContextAbstractFactory();
      PortletRegistryContextFactory factory = afactory.getPortletRegistryContextFactory();
      this.portletRegistryContext = factory.getPortletRegistryContext(this.elementId);
    } catch (PortletRegistryException pre) {
      logger.log(Level.SEVERE, "PSPL_PCCTXCSPPCI0012", pre);
    }
  }

  @Override
  public String getDesktopURL(HttpServletRequest request) {
    StringBuffer requestURL = request.getRequestURL();
    return requestURL.toString();
  }

  @Override
  public String getDesktopURL(HttpServletRequest request, String query, boolean escape) {
    StringBuilder urlBuffer = new StringBuilder(getDesktopURL(request));
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

  @Override
  public String getLocaleString() {
    Locale locale = request.getLocale();
    return locale.toString();
  }

  @Override
  public String getContentType() {
    String contentType = "text/html";
    return contentType;
  }

  @Override
  public String encodeURL(String url) {
    try {
      return URLEncoder.encode(url, ENC);
    } catch (UnsupportedEncodingException usee) {
      return url;
    }
  }

  @Override
  public boolean isAuthless(HttpServletRequest request) {
    // For now authless can also edit , hence return false
    return false;
  }

  @Override
  public String getAuthenticationType() {
    return request.getAuthType();
  }

  @Override
  public Object getProperty(String name) {
    Object value = null;
    if (request != null) {
      HttpSession session = request.getSession(false);
      if (session != null) {
        value = session.getAttribute(name);
      }
    }
    return value;
  }

  @Override
  public void setProperty(String name, Object value) {
    if (request != null) {
      request.getSession(true).setAttribute(name, value);
    }
  }

  @Override
  public List getRoles() {
    // Check if any in the roles is in role
    List<String> currentRoles = new ArrayList<String>();
    for (String role : roles) {
      if (this.request.isUserInRole(role)) {
        currentRoles.add(role);
      }
    }
    return currentRoles;
  }

  @Override
  public Map<String, String> getUserInfo() {
    // TODO
    return Collections.EMPTY_MAP;
  }

  @Override
  public List getMarkupTypes(String portletName) throws PortletWindowContextException {
    try {
      return portletRegistryContext.getMarkupTypes(portletName);
    } catch (PortletRegistryException pre) {
      throw new PortletWindowContextException(pre.getMessage());
    }
  }

  @Override
  public String getDescription(String portletName, String desiredLocale)
      throws PortletWindowContextException {
    try {
      return portletRegistryContext.getDescription(portletName, desiredLocale);
    } catch (PortletRegistryException pre) {
      throw new PortletWindowContextException(pre.getMessage());
    }
  }

  @Override
  public String getShortTitle(String portletName, String desiredLocale)
      throws PortletWindowContextException {
    try {
      return portletRegistryContext.getShortTitle(portletName, desiredLocale);
    } catch (PortletRegistryException pre) {
      throw new PortletWindowContextException(pre.getMessage());
    }
  }

  @Override
  public String getTitle(String portletName, String desiredLocale)
      throws PortletWindowContextException {
    try {
      return portletRegistryContext.getTitle(portletName, desiredLocale);
    } catch (PortletRegistryException pre) {
      throw new PortletWindowContextException(pre.getMessage());
    }
  }

  @Override
  public List getKeywords(String portletName, String desiredLocale)
      throws PortletWindowContextException {
    try {
      return portletRegistryContext.getKeywords(portletName, desiredLocale);
    } catch (PortletRegistryException pre) {
      throw new PortletWindowContextException(pre.getMessage());
    }
  }

  @Override
  public String getDisplayName(String portletName, String desiredLocale)
      throws PortletWindowContextException {
    try {
      return portletRegistryContext.getDisplayName(portletName, desiredLocale);
    } catch (PortletRegistryException pre) {
      throw new PortletWindowContextException(pre.getMessage());
    }
  }

  @Override
  public String getPortletName(String portletWindowName) throws PortletWindowContextException {
    try {
      return portletRegistryContext.getPortletName(portletWindowName);
    } catch (PortletRegistryException pre) {
      throw new PortletWindowContextException(pre.getMessage());
    }
  }

  @Override
  public List<EntityID> getPortletWindows(PortletType portletType, DistributionType distributionType)
      throws PortletWindowContextException {
    List<EntityID> portletList = new ArrayList<EntityID>();
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

  @Override
  public EntityID getEntityID(String portletWindowName) throws PortletWindowContextException {
    try {
      return portletRegistryContext.getEntityId(portletWindowName);
    } catch (PortletRegistryException pre) {
      throw new PortletWindowContextException(pre.getMessage());
    }
  }

  @Override
  public String getPortletWindowTitle(String portletWindowName, String locale)
      throws PortletWindowContextException {
    try {
      // locale ignore for now.
      return portletRegistryContext.getPortletWindowTitle(portletWindowName);
    } catch (PortletRegistryException pre) {
      throw new PortletWindowContextException(pre.getMessage());
    }
  }

  @Override
  public Map getRoleMap(String portletWindowName) throws PortletWindowContextException {
    try {
      return portletRegistryContext.getRoleMap(portletWindowName);
    } catch (PortletRegistryException pre) {
      throw new PortletWindowContextException(pre.getMessage());
    }
  }

  @Override
  public Map getUserInfoMap(String portletWindowName) throws PortletWindowContextException {
    try {
      return portletRegistryContext.getUserInfoMap(portletWindowName);
    } catch (PortletRegistryException pre) {
      throw new PortletWindowContextException(pre.getMessage());
    }
  }

  @Override
  public PortletPreferences getPreferences(String portletWindowName, ResourceBundle bundle,
      boolean isReadOnly) throws PortletWindowContextException {
    String userId = checkUserID();
    return new PortletPreferencesImpl(this.request, this.portletRegistryContext,
        getEntityID(portletWindowName), userId, bundle, isReadOnly);
  }

  private String checkUserID() {
    String userId = getUserRepresentation();
    if (userId == null) {
      userId = AUTHLESS_USER_ID;
    }
    if (isWSRPRequest()) {
      return StringUtil.asBase64(userId.getBytes());
    }
    return userId;
  }

  private boolean isWSRPRequest() {
    Object isWSRPReq = request.getAttribute(IS_WSRP_REQ);
    return isWSRPReq == null ? false : true;
  }

  @Override
  public EventHolder verifySupportedPublishingEvent(EntityID portletEntityId,
      EventHolder eventHolder) {
    PortletDescriptorHolder portletDescriptorHolder = getPortletDescriptorHolder();
    if (portletDescriptorHolder == null) {
      return null;
    }
    return portletDescriptorHolder.verifySupportedPublishingEvent(portletEntityId, eventHolder);
  }

  @Override
  public List<EventHolder> getSupportedPublishingEventHolders(EntityID portletEntityId) {
    PortletDescriptorHolder portletDescriptorHolder = getPortletDescriptorHolder();
    if (portletDescriptorHolder == null) {
      return null;
    }
    return portletDescriptorHolder.getSupportedPublishingEventHolders(portletEntityId);
  }

  @Override
  public EventHolder verifySupportedProcessingEvent(EntityID portletEntityId,
      EventHolder eventHolder) {
    PortletDescriptorHolder portletDescriptorHolder = getPortletDescriptorHolder();
    if (portletDescriptorHolder == null) {
      return null;
    }
    return portletDescriptorHolder.verifySupportedProcessingEvent(portletEntityId, eventHolder);
  }

  @Override
  public List<EventHolder> getSupportedProcessingEventHolders(EntityID portletEntityId) {
    PortletDescriptorHolder portletDescriptorHolder = getPortletDescriptorHolder();
    if (portletDescriptorHolder == null) {
      return null;
    }
    return portletDescriptorHolder.getSupportedProcessingEventHolders(portletEntityId);
  }

  @Override
  public Map<String, String> verifySupportedPublicRenderParameters(EntityID portletEntityId,
      List<PublicRenderParameterHolder> publicRenderParameterHolders) {
    PortletDescriptorHolder portletDescriptorHolder = getPortletDescriptorHolder();
    if (portletDescriptorHolder == null) {
      return Collections.emptyMap();
    }
    return portletDescriptorHolder.verifySupportedPublicRenderParameters(portletEntityId,
        publicRenderParameterHolders);
  }

  @Override
  public List<PublicRenderParameterHolder> getSupportedPublicRenderParameterHolders(
      EntityID portletEntityId, Map<String, String[]> renderParameters) {
    PortletDescriptorHolder portletDescriptorHolder = getPortletDescriptorHolder();
    if (portletDescriptorHolder == null) {
      return Collections.emptyList();
    }
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

  @Override
  public String getPortletID(String portletWindowName) throws PortletWindowContextException {
    try {
      return portletRegistryContext.getPortletID(portletWindowName);
    } catch (PortletRegistryException pre) {
      throw new PortletWindowContextException(pre.getMessage());
    }
  }

  @Override
  public String getConsumerID(String portletWindowName) throws PortletWindowContextException {
    try {
      return portletRegistryContext.getConsumerID(portletWindowName);
    } catch (PortletRegistryException pre) {
      throw new PortletWindowContextException(pre.getMessage());
    }
  }

  @Override
  public String getPortletHandle(String portletWindowName) throws PortletWindowContextException {
    PortletPreferences prefs = getPreferences(portletWindowName, null, true);
    return prefs.getValue(PORTLET_HANDLE_PREF_NAME, null);
  }

  @Override
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

  @Override
  public String getProducerEntityID(String portletWindowName) throws PortletWindowContextException {
    try {
      return portletRegistryContext.getProducerEntityID(portletWindowName);
    } catch (PortletRegistryException pre) {
      throw new PortletWindowContextException(pre.getMessage());
    }
  }

  @Override
  public PortletLang getPortletLang(String portletWindowName) throws PortletWindowContextException {
    try {
      return portletRegistryContext.getPortletLang(portletWindowName);
    } catch (PortletRegistryException pre) {
      throw new PortletWindowContextException(pre.getMessage());
    }
  }

  // TODO
  @Override
  public void store() throws PortletWindowContextException {
  }

  @Override
  public String getUserRepresentation() {
    // The order in which this is suppose to be done
    // 1. Check if elementId is explicity set , If yes use it always
    // 2. If elementId is null, Get from request - getPrincipal
    // 3. If elementId is null, see if wsrp is sending it (in case of resource URL)
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
}
