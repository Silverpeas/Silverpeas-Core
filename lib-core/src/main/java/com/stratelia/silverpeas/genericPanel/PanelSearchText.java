/*--- formatted by Jindent 2.1, (www.c-lab.de/~jindent) ---*/

package com.stratelia.silverpeas.genericPanel;


public class PanelSearchText extends PanelSearchToken
{
    public String   m_Text = "";

    public PanelSearchText(int index, String label, String text)
    {
        m_Index = index;
        m_Type = TYPE_TEXT;
        m_Label = label;
        m_Text = text;
    }

    public String getHTMLSpecific()
    {
        return "<span class=\"txtlibform\">" + m_Text + "</span><input type=\"hidden\" name=\"filter" + Integer.toString(m_Index) + "\" value=\"\">";
    }
}
