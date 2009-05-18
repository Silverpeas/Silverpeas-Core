/*--- formatted by Jindent 2.1, (www.c-lab.de/~jindent) ---*/

package com.stratelia.silverpeas.genericPanel;


public class PanelSearchLink extends PanelSearchToken
{
    public String   m_Text = "";
    public String   m_URL = "";

    public PanelSearchLink(int index, String label, String linkText, String linkURL)
    {
        m_Index = index;
        m_Type = TYPE_LINK;
        m_Label = label;
        m_Text = linkText;
        m_URL = linkURL;
    }

    public String getHTMLSpecific()
    {
        return "<span class=\"txtlibform\"><A href=\"" + m_URL + "\">" + m_Text + "</A></span><input type=\"hidden\" name=\"filter" + Integer.toString(m_Index) + "\" value=\"\">";
    }
}
