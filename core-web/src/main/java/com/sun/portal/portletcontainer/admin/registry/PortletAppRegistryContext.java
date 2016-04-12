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
import java.util.Map;

import com.sun.portal.portletcontainer.context.registry.PortletRegistryException;

/**
 * PortletAppRegistryContext provides information pertaining to the portlet app registry. This
 * includes information about portlets.
 */
public interface PortletAppRegistryContext {

  public List<String> getMarkupTypes(String portletName)
      throws PortletRegistryException;

  public String getDescription(String portletName, String desiredLocale)
      throws PortletRegistryException;

  public String getShortTitle(String portletName, String desiredLocale)
      throws PortletRegistryException;

  public String getTitle(String portletName, String desiredLocales)
      throws PortletRegistryException;

  public List<String> getKeywords(String portletName, String desiredLocale)
      throws PortletRegistryException;

  public String getDisplayName(String portletName, String desiredLocale)
      throws PortletRegistryException;

  public Map<String, Object> getRoleMap(String portletName) throws PortletRegistryException;

  public Map<String, Object> getUserInfoMap(String portletName) throws PortletRegistryException;

  public void removePortlet(String portletName) throws PortletRegistryException;

  public boolean hasView(String portletName) throws PortletRegistryException;

  public boolean hasEdit(String portletName) throws PortletRegistryException;

  public boolean hasHelp(String portletName) throws PortletRegistryException;

  public List<String> getAvailablePortlets() throws PortletRegistryException;
}
