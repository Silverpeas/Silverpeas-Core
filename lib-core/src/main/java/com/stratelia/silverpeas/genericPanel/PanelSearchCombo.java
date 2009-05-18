/*--- formatted by Jindent 2.1, (www.c-lab.de/~jindent) ---*/

package com.stratelia.silverpeas.genericPanel;


public class PanelSearchCombo extends PanelSearchToken
{
    public String[] m_ValuesId   = new String[0];
    public String[] m_ValuesText = new String[0];
    public String   m_Selected = "";
    public int      m_NbValues = 0;

    public PanelSearchCombo(int index, String label, String[] valueIds, String[] valueTexts, String currentSelected)
    {
        m_Index = index;
        m_Type = TYPE_COMBO;
        m_Label = label;
        m_ValuesId   = valueIds;
        m_ValuesText = valueTexts;
        m_NbValues = m_ValuesId.length;
        m_Selected = currentSelected;
    }

    public String getHTMLSpecific()
    {
        StringBuffer sb = new StringBuffer();
        int          i;

        sb.append("<select name=\"filter" + Integer.toString(m_Index) + "\" size=\"1\">\n");
        for (i = 0; i < m_NbValues ; i++)
        {
            if ((!m_ReadOnly) || (m_Selected.equals(m_ValuesId[i])))
            {
                sb.append("<option value=\"" + m_ValuesId[i] + "\"");
                if (m_Selected.equals(m_ValuesId[i]))
                {
                    sb.append(" selected>");
                }
                else
                {
                    sb.append(">");
                }
                sb.append(m_ValuesText[i] + "</option>\n");
            }
        }
        sb.append("</select>");
        return sb.toString();
    }
}
