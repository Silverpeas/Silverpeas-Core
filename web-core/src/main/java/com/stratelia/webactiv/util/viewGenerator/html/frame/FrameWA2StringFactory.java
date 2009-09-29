/**
 * Copyright (C) 2000 - 2009 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have recieved a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "http://repository.silverpeas.com/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
/*--- formatted by Jindent 2.1, (www.c-lab.de/~jindent) 
 ---*/

package com.stratelia.webactiv.util.viewGenerator.html.frame;

import com.stratelia.webactiv.util.viewGenerator.html.GraphicElementFactory;

public class FrameWA2StringFactory extends Object {
  /**
   * Hashtable which contains the specifics code encoded as key and their values
   * are right code encoded
   */
  private static String printBeforeString = null;
  private static String printMiddleString = null;
  private static String printAfterString = null;

  public static String getPrintBeforeString() {
    if (printBeforeString == null) {
      synchronized (FrameWA2StringFactory.class) {
        if (printBeforeString == null) {
          String iconsPath = GraphicElementFactory.getIconsPath();
          StringBuffer buffer = new StringBuffer();
          buffer
              .append("<table width=\"100%\" border=\"0\" cellspacing=\"0\" cellpadding=\"0\">\n");
          buffer.append("\t<tr>\n");
          buffer.append("\t\t<td>\n");
          buffer
              .append("\t\t\t<table width=\"100%\" border=\"0\" cellspacing=\"0\" cellpadding=\"0\">\n");
          buffer.append("\t\t\t\t<tr>\n");
          buffer.append("\t\t\t\t\t<td>\n");
          buffer
              .append("\t\t\t\t\t\t<table width=\"100%\" border=\"0\" cellspacing=\"0\" cellpadding=\"0\">\n");
          buffer.append("\t\t\t\t\t\t\t<tr>\n");
          buffer
              .append(
                  "\t\t\t\t\t\t\t\t<td rowspan=\"2\" class=intfdcolor11 width=\"1\"><img src=\"")
              .append(iconsPath).append(
                  "/1px.gif\" width=\"1\" height=\"1\"></td>\n");
          buffer
              .append(
                  "\t\t\t\t\t\t\t\t<td colspan=\"2\" class=intfdcolor11><img src=\"")
              .append(iconsPath).append(
                  "/1px.gif\" width=\"1\" height=\"1\"></td>\n");
          buffer.append("\t\t\t\t\t\t\t\t<td rowspan=\"2\"><img src=\"")
              .append(iconsPath).append(
                  "/frame/htdroit.gif\" width=\"6\" height=\"5\"></td>\n");
          buffer.append("\t\t\t\t\t\t\t</tr>\n");
          buffer.append("\t\t\t\t\t\t\t<tr>\n");
          buffer.append(
              "\t\t\t\t\t\t\t\t<td class=intfdcolor4 width=\"1\"><img src=\"")
              .append(iconsPath).append(
                  "/1px.gif\" width=\"1\" height=\"1\"></td>\n");
          buffer
              .append(
                  "\t\t\t\t\t\t\t\t<td class=intfdcolor51 width=\"100%\" height=\"4\"><img src=\"")
              .append(iconsPath).append(
                  "/1px.gif\" width=\"1\" height=\"4\"></td>\n");
          buffer.append("\t\t\t\t\t\t\t</tr>\n");
          buffer.append("\t\t\t\t\t\t</table>\n");
          buffer.append("\t\t\t\t\t</td>\n");
          buffer.append("\t\t\t\t</tr>\n");
          buffer.append("\t\t\t</table>\n");
          buffer
              .append("\t\t\t<table width=\"100%\" border=\"0\" cellspacing=\"0\" cellpadding=\"0\">\n");
          buffer.append("\t\t\t\t<tr>\n");
          buffer.append(
              "\t\t\t\t\t<td class=intfdcolor11 width=\"1\"><img src=\"")
              .append(iconsPath).append(
                  "/1px.gif\" width=\"1\" height=\"1\"></td>\n");
          buffer.append(
              "\t\t\t\t\t<td class=intfdcolor4 width=\"1\"><img src=\"")
              .append(iconsPath).append(
                  "/1px.gif\" width=\"1\" height=\"1\"></td>\n");
          buffer.append("\t\t\t\t\t<td class=intfdcolor51 width=\"100%\">");
          printBeforeString = buffer.toString();
        }
      }
    }
    return printBeforeString;
  }

  public static String getPrintMiddleString() {
    if (printMiddleString == null) {
      synchronized (FrameWA2StringFactory.class) {
        if (printMiddleString == null) {
          String iconsPath = GraphicElementFactory.getIconsPath();
          StringBuffer buffer = new StringBuffer();
          buffer.append("</td>\n");
          buffer.append(
              "\t\t\t\t\t<td class=intfdcolor11 width=\"1\"><img src=\"")
              .append(iconsPath).append(
                  "/1px.gif\" width=\"1\" height=\"1\"></td>\n");
          buffer.append(
              "\t\t\t\t\t<td class=intfdcolor12 width=\"1\"><img src=\"")
              .append(iconsPath).append(
                  "/1px.gif\" width=\"1\" height=\"1\"></td>\n");
          buffer.append("\t\t\t\t</tr>\n");
          buffer.append("\t\t\t</table>\n");
          buffer
              .append("\t\t\t<table width=\"100%\" border=\"0\" cellspacing=\"0\" cellpadding=\"0\">\n");
          buffer.append("\t\t\t\t<tr>\n");
          buffer.append(
              "\t\t\t\t\t<td class=intfdcolor11 width=\"1\"><img src=\"")
              .append(iconsPath).append(
                  "/1px.gif\" width=\"1\" height=\"1\"></td>\n");
          buffer.append(
              "\t\t\t\t\t<td class=intfdcolor4 width=\"1\"><img src=\"")
              .append(iconsPath).append(
                  "/1px.gif\" width=\"1\" height=\"1\"></td>\n");
          buffer.append("\t\t\t\t\t<td class=intfdcolor51 width=\"100%\">\n");
          buffer.append("\t\t\t\t\t");
          printMiddleString = buffer.toString();
        }
      }
    }
    return printMiddleString;
  }

  public static String getPrintAfterString() {
    if (printAfterString == null) {
      synchronized (FrameWA2StringFactory.class) {
        if (printAfterString == null) {
          String iconsPath = GraphicElementFactory.getIconsPath();
          StringBuffer buffer = new StringBuffer();
          buffer.append("\t\t\t\t\t</td>\n");
          buffer.append(
              "\t\t\t\t\t<td class=intfdcolor11 width=\"1\"><img src=\"")
              .append(iconsPath).append(
                  "/1px.gif\" width=\"1\" height=\"1\"></td>\n");
          buffer.append(
              "\t\t\t\t\t<td class=intfdcolor12 width=\"1\"><img src=\"")
              .append(iconsPath).append(
                  "/1px.gif\" width=\"1\" height=\"1\"></td>\n");
          buffer.append("\t\t\t\t</tr>\n");
          buffer.append("\t\t\t</table>\n");
          buffer
              .append("\t\t\t<table width=\"100%\" border=\"0\" cellspacing=\"0\" cellpadding=\"0\">\n");
          buffer.append("\t\t\t\t<tr>\n");
          buffer
              .append(
                  "\t\t\t\t\t<td rowspan=\"2\" class=intfdcolor11 width=\"1\"><img src=\"")
              .append(iconsPath).append(
                  "/1px.gif\" width=\"1\" height=\"1\"></td>\n");
          buffer.append(
              "\t\t\t\t\t<td class=intfdcolor4 width=\"1\"><img src=\"")
              .append(iconsPath).append(
                  "/1px.gif\" width=\"1\" height=\"1\"></td>\n");
          buffer
              .append(
                  "\t\t\t\t\t<td class=intfdcolor51 width=\"100%\" height=\"100%\"><img src=\"")
              .append(iconsPath).append(
                  "/1px.gif\" width=\"1\" height=\"4\"></td>\n");
          buffer.append("\t\t\t\t\t<td rowspan=\"3\"><img src=\"").append(
              iconsPath).append(
              "/frame/bsdroit.gif\" width=\"6\" height=\"6\"></td>\n");
          buffer.append("\t\t\t\t</tr>\n");
          buffer.append("\t\t\t\t<tr>\n");
          buffer
              .append(
                  "\t\t\t\t\t<td colspan=\"2\" class=intfdcolor11 height=\"1\"><img src=\"")
              .append(iconsPath).append(
                  "/1px.gif\" width=\"1\" height=\"1\"></td>\n");
          buffer.append("\t\t\t\t</tr>\n");
          buffer.append("\t\t\t\t<tr>\n");
          buffer.append("\t\t\t\t\t<td width=\"1\"><img src=\"").append(
              iconsPath).append("/1px.gif\" width=\"1\" height=\"1\"></td>\n");
          buffer.append("\t\t\t\t\t<td width=\"1\"><img src=\"").append(
              iconsPath).append("/1px.gif\" width=\"1\" height=\"1\"></td>\n");
          buffer.append(
              "\t\t\t\t\t<td class=intfdcolor12 height=\"2\"><img src=\"")
              .append(iconsPath).append(
                  "/1px.gif\" width=\"1\" height=\"2\"></td>\n");
          buffer.append("\t\t\t\t</tr>\n");
          buffer.append("\t\t\t</table>\n");
          buffer.append("\t\t</td>\n");
          buffer.append("\t</tr>\n");
          buffer.append("</table>\n");

          printAfterString = buffer.toString();
        }
      }
    }
    return printAfterString;
  }
}