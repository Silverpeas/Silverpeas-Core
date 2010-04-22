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

public class PanelSearchEdit extends PanelSearchToken {
  public String m_Text = "";

  public PanelSearchEdit(int index, String label, String valueText) {
    m_Index = index;
    m_Type = TYPE_EDIT;
    m_Label = label;
    m_Text = valueText;
  }

  public String getHTMLSpecific() {
    StringBuffer sb = new StringBuffer();

    sb
        .append("<input type=\"text\" size=\"15\" maxlength=\"100\" name=\"filter"
        + Integer.toString(m_Index) + "\" value=\"" + m_Text + "\"");
    if (m_ReadOnly) {
      sb.append(" readonly>");
    } else {
      sb.append(">");
    }
    return sb.toString();
  }
}
