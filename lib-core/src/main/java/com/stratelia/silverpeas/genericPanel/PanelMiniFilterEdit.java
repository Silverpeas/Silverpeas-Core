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
