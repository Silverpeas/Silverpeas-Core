/*--- formatted by Jindent 2.1, (www.c-lab.de/~jindent) ---*/

package com.stratelia.webactiv.util.viewGenerator.html.tabs;

import com.stratelia.webactiv.util.viewGenerator.html.GraphicElementFactory;

public class TabbedPaneWA2StringFactory extends Object
{
    private static String printBeforeString = null;
    private static String printAfterString = null;

	public static String getPrintBeforeString() {
		if (printBeforeString == null) {
			synchronized (TabbedPaneWA2StringFactory.class) {
				if (printBeforeString == null) {
					String iconsPath = GraphicElementFactory.getIconsPath();
					StringBuffer buffer = new StringBuffer();

					buffer.append("<td>\n");

					buffer.append("<table border=\"0\" cellspacing=\"0\" cellpadding=\"0\">\n");
					buffer.append("<tr>\n");
					buffer.append("<td colspan=3 rowspan=3><img src=\"").append(iconsPath).append("/tabs/bt2_hg.gif\"></td>\n");
					buffer.append("<td bgcolor=#999999><img src=\"").append(iconsPath).append("/tabs/1px.gif\"></td>\n");
					buffer.append("<td colspan=3 rowspan=3><img src=\"").append(iconsPath).append("/tabs/bt2_hd.gif\"></td>\n");
					buffer.append("</tr>\n");
					buffer.append("<tr>\n");
					buffer.append("<td bgcolor=#FFFFFF width=1><img src=\"").append(iconsPath).append("/tabs/1px.gif\"></td>\n");
					buffer.append("</tr>\n");
					buffer.append("<tr>\n");
					buffer.append("<td class=ongletColorLight><img src=\"").append(iconsPath).append("/tabs/1px.gif\"></td>\n");
					buffer.append("</tr>\n");
					buffer.append("<tr>\n");
					buffer.append("<td bgcolor=#666666><img src=\"").append(iconsPath).append("/tabs/1px.gif\"></td>\n");
					buffer.append("<td bgcolor=#CCCCCC><img src=\"").append(iconsPath).append("/tabs/1px.gif\"></td>\n");
					buffer.append("<td class=ongletColorLight><img src=\"").append(iconsPath).append("/tabs/1px.gif\"></td>\n");

					printBeforeString = buffer.toString();
				}
			}
		}
		return printBeforeString;
	}

	public static String getPrintAfterString() {
		if (printAfterString == null) {
			synchronized (TabbedPaneWA2StringFactory.class) {
				if (printAfterString == null) {
					String iconsPath = GraphicElementFactory.getIconsPath();
					StringBuffer buffer = new StringBuffer();

					buffer.append("<td class=\"ongletColorDark\"><img src=\"").append(iconsPath).append("/tabs/1px.gif\"></td>\n");
					buffer.append("<td bgcolor=#666666><img src=\"").append(iconsPath).append("/tabs/1px.gif\"></td>\n");
					buffer.append("<td bgcolor=#000000><img src=\"").append(iconsPath).append("/tabs/1px.gif\"></td>\n");
					buffer.append("</tr>\n");
					buffer.append("</table>\n");
					buffer.append("</td>\n");

					printAfterString = buffer.toString();
				}
			}
		}
		return printAfterString;
	}
}