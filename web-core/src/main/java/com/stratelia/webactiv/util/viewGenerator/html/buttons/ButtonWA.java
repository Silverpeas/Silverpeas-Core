/*--- formatted by Jindent 2.1, (www.c-lab.de/~jindent) ---*/

/*
 * ButtonWA.java
 * 
 * Created on 10 octobre 2000, 16:18
 */

package com.stratelia.webactiv.util.viewGenerator.html.buttons;

/**
 * 
 * @author  neysseri
 * @version
 */
public class ButtonWA extends AbstractButton
{

    /**
     * Creates new ButtonWA
     */
    public ButtonWA() {}

    /**
     * Method declaration
     *
     *
     * @return
     *
     * @see
     */
    public String print()
    {
        String cssStyle		= "enableButtonText";
        String action		= this.action;
        String iconsPath	= getIconsPath();

        if (disabled)
        {
            cssStyle = "disableButtonText";
            action = "#";
        }

        StringBuffer str = new StringBuffer(); 
		str.append("<table border=\"0\" cellspacing=\"0\" cellpadding=\"0\" class=\"buttonStyle\">");
		str.append("<tr>");
		str.append("<td rowspan=\"3\"><a href=\"").append(action).append("\"><img src=\"").append(iconsPath).append("/buttons/g.gif\" border=\"0\"></a></td>");
		str.append("<td bgcolor=\"CCCCCC\" colspan=2><img src=\"").append(iconsPath).append("/1px.gif\"></td>");
        str.append("<td rowspan=\"3\"><a href=\"").append(action).append("\"><img src=\"").append(iconsPath).append("/buttons/d.gif\" border=\"0\"></a></td>");
		str.append("</tr>");
		str.append("<tr>");
		str.append("<td><img src=\"").append(iconsPath).append("/1px.gif\" height=17 width=1></td>");
		str.append("<td nowrap><a href=\"").append(action).append("\" class=\"").append(cssStyle).append("\">").append(label).append("</a></td>");
		str.append("</tr>");
		str.append("<tr>");
		str.append("<td bgcolor=\"000000\" colspan=2><img src=\"").append(iconsPath).append("/1px.gif\"></td>");
        str.append("</tr>");
		str.append("</table>");

        return str.toString();
    }

}