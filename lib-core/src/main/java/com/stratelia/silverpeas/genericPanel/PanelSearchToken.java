/*--- formatted by Jindent 2.1, (www.c-lab.de/~jindent) ---*/

package com.stratelia.silverpeas.genericPanel;


abstract public class PanelSearchToken
{
    static public final int TYPE_UNKNOWN  = -1;
    static public final int TYPE_TEXT  = 0;
    static public final int TYPE_LINK  = 1;
    static public final int TYPE_EDIT  = 2;
    static public final int TYPE_COMBO = 3;

    public boolean  m_ReadOnly = false;
    public int      m_Type = TYPE_UNKNOWN;
    public String   m_Label = "";
    public int      m_Index = 0;

    public String getHTMLDisplay()
    {
        StringBuffer sb = new StringBuffer();

        sb.append("<tr>\n<td nowrap><span class=\"txtlibform\">");
        sb.append(m_Label);
        sb.append(" : </span></td>\n<td nowrap>");
        sb.append(getHTMLSpecific());
        sb.append("\n</td>\n</tr>");
        return sb.toString();
    }

    public String getHTMLSpecific()
    {
        return "";
    }
}
