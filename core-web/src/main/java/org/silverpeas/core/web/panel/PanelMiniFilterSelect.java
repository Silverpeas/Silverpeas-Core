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

package org.silverpeas.core.web.panel;

public class PanelMiniFilterSelect extends PanelMiniFilterToken {
  public String m_Text = "";
  public String m_IconS = "";
  public String m_AltS = "";
  public String m_TitleS = "";
  public String m_IconU = "";
  public String m_AltU = "";
  public String m_TitleU = "";
  public boolean m_isSelectAllFunction = true;

  public PanelMiniFilterSelect(int index, String label, String valueText,
      String iconS, String iconU, String altS, String altU, String titleS,
      String titleU) {
    m_Index = index;
    m_Type = TYPE_EDIT;
    m_Label = label;
    m_Text = valueText;
    m_IconS = iconS;
    m_AltS = altS;
    m_TitleS = titleS;
    m_IconU = iconU;
    m_AltU = altU;
    m_TitleU = titleU;
  }

  public boolean isSelectAllFunction() {
    return m_isSelectAllFunction;
  }

  public void setSelectAllFunction(boolean isSelectAllFunction) {
    m_isSelectAllFunction = isSelectAllFunction;
  }

  public String getHTMLDisplay() {
    StringBuilder sb = new StringBuilder();

    sb.append("<input type='checkbox' name='").append(m_Text).append(
        "All' value='' onClick=selectAll('").append(m_Text).append("')>&nbsp;");
    sb.append("<a href=\"javascript:submitOperation('GENERICPANELMINIFILTER" + '_').append(
        m_Label).append('_').append(Integer.toString(m_Index)).append("','')\">");
    sb.append("<img src=\"").append((m_isSelectAllFunction) ? m_IconS : m_IconU).append(
        "\" border=0 align=absmiddle alt=\"").append(
        (m_isSelectAllFunction) ? m_AltS : m_AltU).append("\" title=\"").append(
        (m_isSelectAllFunction) ? m_TitleS : m_TitleU).append("\"></a>");
    return sb.toString();
  }
}
