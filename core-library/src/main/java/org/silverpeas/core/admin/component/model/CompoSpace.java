/*
 * Copyright (C) 2000 - 2024 Silverpeas
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
 * "https://www.silverpeas.org/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package org.silverpeas.core.admin.component.model;

public class CompoSpace {
  private String componentId = "";
  private String componentLabel = "";
  private String tablePrefixSpaceId = "";
  private String spaceLabel = "";
  private int spaceLevel = 1;

  public void setComponentId(String sComponentId) {
    componentId = sComponentId;
  }

  public String getComponentId() {
    return componentId;
  }

  public void setComponentLabel(String sComponentLabel) {
    componentLabel = sComponentLabel;
  }

  public String getComponentLabel() {
    return componentLabel;
  }

  public void setSpaceId(String sTablePrefixSpaceId) {
    tablePrefixSpaceId = sTablePrefixSpaceId;
  }

  public String getSpaceId() {
    return tablePrefixSpaceId;
  }

  public void setSpaceLabel(String sSpaceLabel) {
    spaceLabel = sSpaceLabel;
  }

  public String getSpaceLabel() {
    return spaceLabel;
  }

  public void setSpaceLevel(int level) {
    spaceLevel = level;
  }

  public int getSpaceLevel() {
    return spaceLevel;
  }
}