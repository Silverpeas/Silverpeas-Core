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

package org.silverpeas.core.admin.component.model;

public class CompoSpace {
  private String m_sComponentId = "";
  private String m_sComponentLabel = "";
  private String m_sTablePrefixSpaceId = "";
  private String m_sSpaceLabel = "";
  private int spaceLevel = 1;

  public CompoSpace() {
  }

  public void setComponentId(String sComponentId) {
    m_sComponentId = sComponentId;
  }

  public String getComponentId() {
    return m_sComponentId;
  }

  public void setComponentLabel(String sComponentLabel) {
    m_sComponentLabel = sComponentLabel;
  }

  public String getComponentLabel() {
    return m_sComponentLabel;
  }

  public void setSpaceId(String sTablePrefixSpaceId) {
    m_sTablePrefixSpaceId = sTablePrefixSpaceId;
  }

  public String getSpaceId() {
    return m_sTablePrefixSpaceId;
  }

  public void setSpaceLabel(String sSpaceLabel) {
    m_sSpaceLabel = sSpaceLabel;
  }

  public String getSpaceLabel() {
    return m_sSpaceLabel;
  }

  public void setSpaceLevel(int level) {
    spaceLevel = level;
  }

  public int getSpaceLevel() {
    return spaceLevel;
  }
}