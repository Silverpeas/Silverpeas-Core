/*
 * Copyright (C) 2000 - 2016 Silverpeas
 * <p>
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * <p>
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have received a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 * <p>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 * <p>
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.silverpeas.core.admin.domain.model;

import org.silverpeas.core.util.ResourceLocator;

/**
 * Properties about the management of the domains in Silverpeas.
 * @author miguel
 */
public class DomainProperties {

  private static final String DOMAIN_VISIBILITY_KEY = "domainVisibility";
  private static final String DEFAULT_DOMAIN_ID = "0";

  public static final int DVIS_ALL = 0;
  public static final int DVIS_ONE = 1;
  public static final int DVIS_EACH = 2;

  public static boolean isDefaultDomain(String domainId) {
    return DEFAULT_DOMAIN_ID.equals(domainId);
  }

  /**
   * Are the domains in Silverpeas visible to all users, whatever their own domain?
   * @return true if the domains aren't compartmentalized and hence the users can see the users from
   * others domains. False otherwise.
   */
  public static boolean areDomainsVisibleToAll() {
    return getDomainVisibility() == DVIS_ALL;
  }

  /**
   * Are the domains in Silverpeas no visible to users from others domains?
   * @return true if the users of a domain can be seen only by users from the same domain. False
   * otherwise.
   */
  public static boolean areDomainsNonVisibleToOthers() {
    return getDomainVisibility() == DVIS_EACH;
  }

  /**
   * Are the domains in Silverpeas visible only to the users of the default domain (Silverpeas
   * domain) ?
   * @return true if the users in all the domains can be seen only by the users of the default
   * domain. False otherwise.
   */
  public static boolean areDomainsVisibleOnlyToDefaultOne() {
    return getDomainVisibility() == DVIS_ONE;
  }

  private static int getDomainVisibility() {
    return ResourceLocator.getGeneralSettingBundle().getInteger(DOMAIN_VISIBILITY_KEY, 0);
  }
}
