/*
 * CDDL HEADER START
 * The contents of this file are subject to the terms
 * of the Common Development and Distribution License
 * (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at
 * http://www.sun.com/cddl/cddl.html and legal/CDDLv1.0.txt
 * See the License for the specific language governing
 * permission and limitations under the License.
 *
 * When distributing Covered Code, include this CDDL
 * Header Notice in each file and include the License file
 * at legal/CDDLv1.0.txt.
 * If applicable, add the following below the CDDL Header,
 * with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Copyright 2006 Sun Microsystems Inc. All Rights Reserved
 * CDDL HEADER END
 */
package com.sun.portal.portletcontainer.admin.registry;

import java.util.List;

import org.silverpeas.core.web.portlets.portal.PortletWindowData;
import com.sun.portal.container.EntityID;
import com.sun.portal.container.PortletLang;
import com.sun.portal.container.PortletType;
import com.sun.portal.portletcontainer.context.registry.PortletRegistryException;

/**
 * PortletWindowRegistryContext provides information pertaining to the portlet window registry. This
 * includes information about portlet windows.
 */
public interface PortletWindowRegistryContext {

  public EntityID getEntityId(String portletWindowName) throws PortletRegistryException;

  public List<EntityID> getEntityIds() throws PortletRegistryException;

  public String getPortletWindowTitle(String portletWindowName) throws PortletRegistryException;

  public void setPortletWindowTitle(String portletWindowName, String title)
      throws PortletRegistryException;

  public void createPortletWindow(String portletName, String portletWindowName)
      throws PortletRegistryException;

  public void createPortletWindow(String portletName, String portletWindowName, String title,
      String locale) throws PortletRegistryException;

  public void removePortletWindow(String portletWindowName) throws PortletRegistryException;

  public void removePortletWindows(String portletName) throws PortletRegistryException;

  public void movePortletWindows(List<PortletWindowData> portletWindows) throws PortletRegistryException;

  public void showPortletWindow(String portletWindowName, boolean visible)
      throws PortletRegistryException;

  public boolean isVisible(String portletWindowName) throws PortletRegistryException;

  public String getPortletName(String portletWindowName) throws PortletRegistryException;

  public List<String> getPortletWindows(String portletName) throws PortletRegistryException;

  public Integer getRowNumber(String portletWindowName) throws PortletRegistryException;

  public String getWidth(String portletWindowName) throws PortletRegistryException;

  public void setWidth(String portletWindowName, String width, String row)
      throws PortletRegistryException;

  public String getProducerEntityID(String portletWindowName) throws PortletRegistryException;

  public String getConsumerID(String portletWindowName) throws PortletRegistryException;

  public String getPortletID(String portletWindowName) throws PortletRegistryException;

  public boolean isRemote(String portletWindowName) throws PortletRegistryException;

  public List<String> getVisiblePortletWindows(PortletType portletType) throws PortletRegistryException;

  public List<String> getAllPortletWindows(PortletType portletType) throws PortletRegistryException;

  public List<String> getRemotePortletWindows() throws PortletRegistryException;

  public PortletLang getPortletLang(String portletWindowName) throws PortletRegistryException;
}
