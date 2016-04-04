/*
 * Copyright (C) 2000 - 2016 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have received a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.silverpeas.core.web.util.viewgenerator.html.tabs;

import org.silverpeas.core.web.util.viewgenerator.html.GraphicElementFactory;

public class TabbedPaneSilverpeasV5StringFactory {
  private static String printBeforeString = null;
  private static String printAfterString = null;
  private static String printEndString = null;

  static {
    String iconsPath = GraphicElementFactory.getIconsPath();
    StringBuilder buffer = new StringBuilder();
    buffer.append("<td>\n");
    buffer.append("<table border=0 cellspacing=0 cellpadding=0>\n");
    buffer.append("<tr>\n");
    buffer.append("<td colspan=3 rowspan=3><img src=\"").append(iconsPath)
        .append("/tabs/bt2_hg.gif\"></td>\n");
    buffer.append("<td bgcolor=#999999><img src=\"").append(iconsPath)
        .append("/tabs/1px.gif\"></td>\n");
    buffer.append("<td colspan=3 rowspan=3><img src=\"").append(iconsPath)
        .append("/tabs/bt2_hd.gif\"></td>\n");
    buffer.append("</tr>\n");
    buffer.append("<tr>\n");
    buffer.append("<td bgcolor=#FFFFFF width=1><img src=\"")
        .append(iconsPath).append("/tabs/1px.gif\"></td>\n");
    buffer.append("</tr>\n");
    buffer.append("<tr>\n");
    buffer.append("<td class=ongletColorLight><img src=\"").append(iconsPath)
        .append("/tabs/1px.gif\"></td>\n");
    buffer.append("</tr>\n");
    buffer.append("<tr>\n");
    buffer.append("<td bgcolor=#666666><img src=\"").append(iconsPath)
        .append("/tabs/1px.gif\"></td>\n");
    buffer.append("<td bgcolor=#CCCCCC><img src=\"").append(iconsPath)
        .append("/tabs/1px.gif\"></td>\n");
    buffer.append("<td class=ongletColorLight><img src=\"").append(iconsPath)
        .append("/tabs/1px.gif\"></td>\n");
    printBeforeString = buffer.toString();

    buffer = new StringBuilder();
    buffer.append("<td class=ongletColorDark><img src=\"").append(iconsPath)
        .append("/tabs/1px.gif\"></td>\n");
    buffer.append("<td bgcolor=#666666><img src=\"").append(iconsPath)
        .append("/tabs/1px.gif\"></td>\n");
    buffer.append("<td bgcolor=#000000><img src=\"").append(iconsPath)
        .append("/tabs/1px.gif\"></td>\n");
    buffer.append("</tr>\n");
    buffer.append("</table>\n");
    buffer.append("</td>\n");
    printAfterString = buffer.toString();

    buffer = new StringBuilder();
    buffer.append("<table width=\"100%\" border=0 cellspacing=0 cellpadding=0>\n");
    buffer.append("<tr>\n<td class=\"intfdcolor6\">\n");
    buffer.append("<img src=\"").append(iconsPath);
    buffer.append("/tabs/1px.gif\" width=\"1\" height=\"3\"></td>\n");
    buffer.append("</tr>\n");
    buffer.append("<tr>\n<td>\n");
    buffer.append("<img src=\"").append(iconsPath);
    buffer.append("/tabs/1px.gif\" width=\"1\" height=\"6\"></td>\n");
    buffer.append("</tr>\n");
    buffer.append("</table>\n");
    printEndString = buffer.toString();
  }

  public static String getPrintBeforeString() {
    return printBeforeString;
  }

  public static String getPrintAfterString() {
    return printAfterString;
  }

  public static String getPrintEndString() {
    return printEndString;
  }
}