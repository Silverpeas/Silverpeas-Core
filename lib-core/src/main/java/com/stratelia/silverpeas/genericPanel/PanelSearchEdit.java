/*--- formatted by Jindent 2.1, (www.c-lab.de/~jindent) ---*/

package com.stratelia.silverpeas.genericPanel;


public class PanelSearchEdit extends PanelSearchToken
{
    public String   m_Text = "";

    public PanelSearchEdit(int index, String label, String valueText)
    {
        m_Index = index;
        m_Type = TYPE_EDIT;
        m_Label = label;
        m_Text = valueText;
    }

    public String getHTMLSpecific()
    {
        StringBuffer sb = new StringBuffer();

        sb.append("<input type=\"text\" size=\"15\" maxlength=\"100\" name=\"filter" + Integer.toString(m_Index) + "\" value=\"" + m_Text + "\"");
        if (m_ReadOnly)
        {
            sb.append(" readonly>");
        }
        else
        {
            sb.append(">");
        }
        return sb.toString();
    }
}
