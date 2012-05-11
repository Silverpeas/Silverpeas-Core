/**
 * Copyright (C) 2000 - 2012 Silverpeas
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
 * "http://www.silverpeas.org/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.stratelia.webactiv.beans.admin;

import com.stratelia.webactiv.util.ResourceLocator;

public class DomainProperty {
  final static public String PROPERTY_TYPE_STRING = "STRING";
  final static public String PROPERTY_TYPE_USERID = "USERID";
  final static public String PROPERTY_TYPE_BOOLEAN = "BOOLEAN";

  final static public String PROPERTY_UPDATEALLOWED_ADMIN = "A";
  final static public String PROPERTY_UPDATEALLOWED_USER = "U";
  final static public String PROPERTY_UPDATE_NOT_ALLOWED = "N";

  private String m_sName = null;
  private String m_iType = PROPERTY_TYPE_STRING;
  private String m_sMapParameter = null;
  private boolean usedToImport = false;
  private String redirectOU = null;
  private String redirectAttribute = null;
  private String updateAllowedTo = PROPERTY_UPDATE_NOT_ALLOWED;

  private String label = null;
  private String description = null;

  public DomainProperty() {
  }

  public DomainProperty(ResourceLocator rs, String num) {
    String s;

    m_sName = rs.getString("property_" + num + ".Name");
    m_iType = PROPERTY_TYPE_STRING;
    s = rs.getString("property_" + num + ".Type");
    if ((s != null) && (s.length() > 0)) {
      if (s.equalsIgnoreCase("USERID")) {
        m_iType = PROPERTY_TYPE_USERID;
      } else if (s.equalsIgnoreCase("BOOLEAN")) {
        m_iType = PROPERTY_TYPE_BOOLEAN;
      }
    }
    m_sMapParameter = rs.getString("property_" + num + ".MapParameter");
    usedToImport = rs.getBoolean("property_" + num + ".UsedToImport", false);
    redirectOU = rs.getString("property_" + num + ".RedirectOU");
    redirectAttribute = rs.getString("property_" + num + ".RedirectAttribute");
    updateAllowedTo = rs.getString("property_" + num + ".UpdateAllowedTo",
        PROPERTY_UPDATE_NOT_ALLOWED);
  }

  public void setName(String propertyName) {
    m_sName = propertyName;
  }

  public String getName() {
    return m_sName;
  }

  public void setType(String propertyType) {
    m_iType = propertyType;
  }

  public String getType() {
    return m_iType;
  }

  public void setMapParameter(String mapParameter) {
    m_sMapParameter = mapParameter;
  }

  public String getMapParameter() {
    return m_sMapParameter;
  }

  public boolean isUsedToImport() {
    return usedToImport;
  }

  public String getLabel() {
    return label;
  }

  public void setLabel(String label) {
    this.label = label;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public String getRedirectAttribute() {
    return redirectAttribute;
  }

  public String getRedirectOU() {
    return redirectOU;
  }

  public boolean isUpdateAllowedToUser() {
    return PROPERTY_UPDATEALLOWED_USER.equalsIgnoreCase(updateAllowedTo);
  }

  public boolean isUpdateAllowedToAdmin() {
    return PROPERTY_UPDATEALLOWED_ADMIN.equalsIgnoreCase(updateAllowedTo);
  }

}