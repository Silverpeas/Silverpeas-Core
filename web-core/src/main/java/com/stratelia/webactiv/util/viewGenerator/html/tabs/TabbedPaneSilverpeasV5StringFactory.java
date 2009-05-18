/*--- formatted by Jindent 2.1, (www.c-lab.de/~jindent) ---*/

package com.stratelia.webactiv.util.viewGenerator.html.tabs;

import com.stratelia.webactiv.util.viewGenerator.html.GraphicElementFactory;

public class TabbedPaneSilverpeasV5StringFactory extends Object
{
    private static String printBeforeString = null;
    private static String printAfterString = null;
    private static String printEndString = null;

	public static String getPrintBeforeString() {
		if (printBeforeString == null) {
			String iconsPath = GraphicElementFactory.getIconsPath();
			StringBuffer buffer = new StringBuffer();
            buffer.append( "<td>\n");
            buffer.append( "<table border=0 cellspacing=0 cellpadding=0>\n");
            buffer.append( "<tr>\n");
            buffer.append( "<td colspan=3 rowspan=3><img src=\"").append(iconsPath).append("/tabs/bt2_hg.gif\"></td>\n");
            buffer.append( "<td bgcolor=#999999><img src=\"").append(iconsPath).append("/tabs/1px.gif\"></td>\n");
            buffer.append( "<td colspan=3 rowspan=3><img src=\"").append(iconsPath).append("/tabs/bt2_hd.gif\"></td>\n");
            buffer.append( "</tr>\n");
            buffer.append( "<tr>\n");
            buffer.append( "<td bgcolor=#FFFFFF width=1><img src=\"").append(iconsPath).append("/tabs/1px.gif\"></td>\n");
            buffer.append( "</tr>\n");
            buffer.append( "<tr>\n");
            buffer.append( "<td class=ongletColorLight><img src=\"").append(iconsPath).append("/tabs/1px.gif\"></td>\n");
            buffer.append( "</tr>\n");
            buffer.append( "<tr>\n");
            buffer.append( "<td bgcolor=#666666><img src=\"").append(iconsPath).append("/tabs/1px.gif\"></td>\n");
            buffer.append( "<td bgcolor=#CCCCCC><img src=\"").append(iconsPath).append("/tabs/1px.gif\"></td>\n");
            buffer.append( "<td class=ongletColorLight><img src=\"").append(iconsPath).append("/tabs/1px.gif\"></td>\n");

			printBeforeString = buffer.toString();
		}
		return printBeforeString;
	}

	public static String getPrintAfterString() {
		if (printAfterString == null) {
			String iconsPath = GraphicElementFactory.getIconsPath();
			StringBuffer buffer = new StringBuffer();
			
            buffer.append( "<td class=ongletColorDark><img src=\"").append(iconsPath).append("/tabs/1px.gif\"></td>\n");
            buffer.append( "<td bgcolor=#666666><img src=\"").append(iconsPath).append("/tabs/1px.gif\"></td>\n");
            buffer.append( "<td bgcolor=#000000><img src=\"").append(iconsPath).append("/tabs/1px.gif\"></td>\n");
            buffer.append( "</tr>\n");
            buffer.append( "</table>\n");
            buffer.append( "</td>\n");
			
			printAfterString = buffer.toString();
		}
		return printAfterString;
	}

	public static String getPrintEndString() {
		if (printEndString == null) {
			String iconsPath = GraphicElementFactory.getIconsPath();
			StringBuffer buffer = new StringBuffer();
				buffer.append( "<table width=\"100%\" border=0 cellspacing=0 cellpadding=0>\n");
				buffer.append( "<tr>\n<td class=\"intfdcolor6\">\n");
				buffer.append( "<img src=\"").append(iconsPath).append("/tabs/1px.gif\" width=\"1\" height=\"3\"></td>\n");
				buffer.append( "</tr>\n");
				buffer.append( "<tr>\n<td>\n");
				buffer.append( "<img src=\"").append(iconsPath).append("/tabs/1px.gif\" width=\"1\" height=\"6\"></td>\n");
				buffer.append( "</tr>\n");
				buffer.append( "</table>\n");
			printEndString = buffer.toString();
		}
		return printEndString;
	}
}