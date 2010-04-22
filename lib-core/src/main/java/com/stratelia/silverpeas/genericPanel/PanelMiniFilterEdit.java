/**
 * Copyright (C) 2000 - 2009 Silverpeas
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
 * "http://repository.silverpeas.com/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

/*--- formatted by Jindent 2.1, (www.c-lab.de/~jindent) 
 ---*/

package com.stratelia.silverpeas.genericPanel;

public class PanelMiniFilterEdit extends PanelMiniFilterToken {
  public String m_Text = "";
  public String m_Icon = "";
  public String m_Alt = "";
  public String m_Title = "";

  public PanelMiniFilterEdit(int index, String label, String valueText,
      String icon, String alt, String title) {
    m_Index = index;
    m_Type = TYPE_EDIT;
    m_Label = label;
    m_Text = valueText;
    m_Icon = icon;
    m_Alt = alt;
    m_Title = title;
  }

  public String getHTMLDisplay() {
    StringBuffer sb = new StringBuffer();

    sb.append("&nbsp;&nbsp;<input type=text size=5 name=\"miniFilter" + '_'
        + m_Label + '_' + Integer.toString(m_Index)
        + "\" style=\"font-size:9;\" value=\"" + m_Text + "\"");
    if (m_ReadOnly) {
      sb.append(" readonly>&nbsp;");
    } else {
      sb.append(">&nbsp;");
    }
    sb.append("<a href=\"javascript:submitOperation('GENERICPANELMINIFILTER"
        + '_' + m_Label + '_' + Integer.toString(m_Index) + "','')\">");
    sb.append("<img src=\"" + m_Icon + "\" border=0 align=absmiddle alt=\""
        + m_Alt + "\" title=\"" + m_Title + "\"></a>");
    // sb.append("<img src=\""+resource.getIcon("selectionPeas.filter")+"\" border=0 align=absmiddle alt=\""+resource.getString("selectionPeas.filter")+"\" title=\""+resource.getString("selectionPeas.filter")+"\"></a>");
    return sb.toString();
  }
}
