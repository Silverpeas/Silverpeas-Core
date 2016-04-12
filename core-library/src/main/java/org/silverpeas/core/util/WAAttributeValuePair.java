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

/*
 * WAAttributeValuePair.java
 *
 * Created on 3 octobre 2000, 17:14
 */

package org.silverpeas.core.util;

/**
 * @author jpouyadou
 * @version
 */
public class WAAttributeValuePair {
  public static final int SEARCH_MODE_PARTIAL = 0x0001;
  public static final int SEARCH_MODE_STARTSWITH = 0x0002;
  public static final int SEARCH_MODE_EXACT = ~(SEARCH_MODE_PARTIAL | SEARCH_MODE_STARTSWITH);
  private String m_Value = null;
  private String m_Name = null;
  private int m_SearchMode = SEARCH_MODE_STARTSWITH;

  /** Creates new WAAttributeValuePair */
  public WAAttributeValuePair(String m, String v) {
    m_Name = m;
    m_Value = v;
  }

  public String getName() {
    return (m_Name);
  }

  public String getValue() {
    return (m_Value);
  }

  public void setName(String n) {
    m_Name = n;
  }

  public void setValue(String v) {
    m_Value = v;
  }

  public void setSearchMode(int mode) {
    m_SearchMode = mode;
  }

  public int getSearchMode() {
    return (m_SearchMode);
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((m_Name == null) ? 0 : m_Name.hashCode());
    result = prime * result + ((m_Value == null) ? 0 : m_Value.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    WAAttributeValuePair other = (WAAttributeValuePair) obj;
    if (m_Name == null) {
      if (other.m_Name != null)
        return false;
    } else if (!m_Name.equals(other.m_Name))
      return false;
    if (m_Value == null) {
      if (other.m_Value != null)
        return false;
    } else if (!m_Value.equals(other.m_Value))
      return false;
    return true;
  }
}