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

package org.silverpeas.core.admin.domain.model;

import org.silverpeas.core.util.SettingBundle;

public class DomainProperty {
  final static public String PROPERTY_TYPE_STRING = "STRING";
  final static public String PROPERTY_TYPE_USERID = "USERID";
  final static public String PROPERTY_TYPE_BOOLEAN = "BOOLEAN";

  final static public int DEFAULT_MAX_LENGTH = 50;

  final static public String PROPERTY_UPDATEALLOWED_ADMIN = "A";
  final static public String PROPERTY_UPDATEALLOWED_USER = "U";
  final static public String PROPERTY_UPDATE_NOT_ALLOWED = "N";

  private String name = null;
  private String type = PROPERTY_TYPE_STRING;
  private int maxLength = DEFAULT_MAX_LENGTH;
  private String mapParameter = null;
  private boolean usedToImport = false;
  private String redirectOU = null;
  private String redirectAttribute = null;
  private String updateAllowedTo = PROPERTY_UPDATE_NOT_ALLOWED;

  private String label = null;
  private String description = null;

  public DomainProperty() {
  }

  public DomainProperty(SettingBundle rs, String num) {
    String s;

    name = rs.getString("property_" + num + ".Name");
    type = PROPERTY_TYPE_STRING;
    s = rs.getString("property_" + num + ".Type");
    if ((s != null) && (s.length() > 0)) {
      if (s.equalsIgnoreCase("USERID")) {
        type = PROPERTY_TYPE_USERID;
      } else if (s.equalsIgnoreCase("BOOLEAN")) {
        type = PROPERTY_TYPE_BOOLEAN;
      }
    }
    maxLength = rs.getInteger("property_" + num + ".MaxLength", DEFAULT_MAX_LENGTH);
    mapParameter = rs.getString("property_" + num + ".MapParameter", null);
    usedToImport = rs.getBoolean("property_" + num + ".UsedToImport", false);
    redirectOU = rs.getString("property_" + num + ".RedirectOU", null);
    redirectAttribute = rs.getString("property_" + num + ".RedirectAttribute", null);
    updateAllowedTo = rs.getString("property_" + num + ".UpdateAllowedTo",
        PROPERTY_UPDATE_NOT_ALLOWED);
  }

  public void setName(String propertyName) {
    name = propertyName;
  }

  public String getName() {
    return name;
  }

  public void setType(String propertyType) {
    type = propertyType;
  }

  public String getType() {
    return type;
  }

  public void setMaxLength(final int maxLength) {
    this.maxLength = maxLength;
  }

  public int getMaxLength() {
    return maxLength;
  }

  public void setMapParameter(String mapParameter) {
    this.mapParameter = mapParameter;
  }

  public String getMapParameter() {
    return mapParameter;
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