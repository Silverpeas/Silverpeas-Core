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
public class ButtonWA2 extends AbstractButton
{

    /**
     * Creates new ButtonWA
     */
    public ButtonWA2() {}

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
        String action		= this.action;
        String iconsPath	= getIconsPath();

        if (disabled)
        {
            action = "#";
        }

        StringBuffer str = new StringBuffer();
		str.append("<table border=\"0\" cellspacing=\"0\" cellpadding=\"0\">");
		str.append("<tr>");
		str.append("<td colspan=\"3\" rowspan=\"3\"><img src=\"").append(iconsPath).append("/buttons/bt2_hg.gif\"></td>");
		str.append("<td bgcolor=\"#999999\"><img src=\"").append(iconsPath).append("/1px.gif\"></td>");
		str.append("<td colspan=\"2\" rowspan=\"3\"><img src=\"").append(iconsPath).append("/buttons/bt2_hd.gif\"></td>");
		str.append("</tr>");
		str.append("<tr>");
		str.append("<td bgcolor=\"#FFFFFF\"><img src=\"").append(iconsPath).append("/1px.gif\"></td>");
		str.append("</tr>");
		str.append("<tr>");
		str.append("<td class=\"buttonColorLight\"><img src=\"").append(iconsPath).append("/1px.gif\"></td>");
		str.append("</tr>");
		str.append("<tr>");
		str.append("<td bgcolor=\"#666666\"><img src=\"").append(iconsPath).append("/1px.gif\"></td>");
		str.append("<td bgcolor=\"#CCCCCC\"><img src=\"").append(iconsPath).append("/1px.gif\"></td>");
		str.append("<td class=\"buttonColorLight\"><img src=\"").append(iconsPath).append("/1px.gif\"></td>");
		str.append("<td nowrap class=\"buttonStyle\"><a href=\"").append(action).append("\" class=\"buttonStyle\">&nbsp;").append(label).append("&nbsp;</a></td>");
		str.append("<td class=\"buttonColorDark\"><img src=\"").append(iconsPath).append("/1px.gif\"></td>");
		str.append("<td bgcolor=\"#666666\"><img src=\"").append(iconsPath).append("/1px.gif\" width=2></td>");
		str.append("</tr>");
		str.append("<tr>").append("<td colspan=\"3\" rowspan=\"2\"><img src=\"").append(iconsPath).append("/buttons/bt2_bg.gif\"></td>");
		str.append("<td class=\"buttonColorDark\"><img src=\"").append(iconsPath).append("/1px.gif\"></td>");
		str.append("<td colspan=\"2\" rowspan=\"2\"><img src=\"").append(iconsPath).append("/buttons/bt2_bd.gif\"></td>");
		str.append("</tr>");
		str.append("<tr>");
		str.append("<td bgcolor=\"#666666\"><img src=\"").append(iconsPath).append("/1px.gif\" height=2></td>");
		str.append("</tr>");
		str.append("</table>");

        return str.toString();
    }

}