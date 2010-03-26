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
 * FLOSS exception.  You should have recieved a copy of the text describing
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

public class PanelSearchCombo extends PanelSearchToken {
  public String[] m_ValuesId = new String[0];
  public String[] m_ValuesText = new String[0];
  public String m_Selected = "";
  public int m_NbValues = 0;

  public PanelSearchCombo(int index, String label, String[] valueIds,
      String[] valueTexts, String currentSelected) {
    m_Index = index;
    m_Type = TYPE_COMBO;
    m_Label = label;
    m_ValuesId = valueIds;
    m_ValuesText = valueTexts;
    m_NbValues = m_ValuesId.length;
    m_Selected = currentSelected;
  }

  public String getHTMLSpecific() {
    StringBuffer sb = new StringBuffer();
    int i;

    sb.append("<select name=\"filter" + Integer.toString(m_Index)
        + "\" size=\"1\">\n");
    for (i = 0; i < m_NbValues; i++) {
      if ((!m_ReadOnly) || (m_Selected.equals(m_ValuesId[i]))) {
        sb.append("<option value=\"" + m_ValuesId[i] + "\"");
        if (m_Selected.equals(m_ValuesId[i])) {
          sb.append(" selected>");
        } else {
          sb.append(">");
        }
        sb.append(m_ValuesText[i] + "</option>\n");
      }
    }
    sb.append("</select>");
    return sb.toString();
  }
}
