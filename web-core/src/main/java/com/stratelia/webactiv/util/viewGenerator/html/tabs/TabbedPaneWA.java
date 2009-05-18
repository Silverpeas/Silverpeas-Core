/*--- formatted by Jindent 2.1, (www.c-lab.de/~jindent) ---*/

/*
 * TabbedPane.java
 *
 * Created on 10 octobre 2000, 16:11
 */

package com.stratelia.webactiv.util.viewGenerator.html.tabs;

import java.util.Collection;
import java.util.Iterator;
import java.util.Vector;

/**
 *
 * @author squere
 * @version
 */
public class TabbedPaneWA extends AbstractTabbedPane {

  /**
   * Constructor declaration
   *
   *
   * @see
   */
  public TabbedPaneWA() {
    super();
  }

  /**
   * Method declaration
   *
   *
   * @return
   *
   * @see
   */
  public String print() {
    StringBuffer result = new StringBuffer();
    String iconsPath = getIconsPath();
    Vector tabLines = getTabLines();
    Collection tabs = null;
    int nbLines = tabLines.size();
    int incr = nbLines - 1;

    for (int j = 0; j < nbLines; j++) {
      tabs = (Collection) tabLines.get(j);
      result.append("<table id=\"tabbedPane\" cellpadding=\"0\" ")
          .append("cellspacing=\"0\" border=\"0\" width=\"100%\">\r\n")
          .append("<tr><td align=\"right\" width=\"100%\">");
      result.append(printTabLine(tabs));
      result.append("</td><td><img src=\"").append(iconsPath).append(
          "/tabs/1px.gif\" width=").append(incr * 17).append(
          " height=1></td></tr></table>");
      incr--;
    }
    return result.toString();
  }

  /**
   * Method declaration
   *
   *
   * @param tabs
   *
   * @return
   *
   * @see
   */
  private String printTabLine(Collection tabs) {

    StringBuffer result = new StringBuffer();
    String iconsPath = getIconsPath();
    int indentation = getIndentation();

    result
        .append("<table cellpadding=\"0\" cellspacing=\"0\" border=\"0\" " +
        		"width=\"100%\">");
    result.append("<tr align=\"right\">");
    if (indentation == RIGHT) {
      result.append("<td width=\"100%\">&nbsp;</td>\n");
    }

    Iterator i = tabs.iterator();
    while (i.hasNext()) {
      Tab tab = (Tab) i.next();
      String style = "ongletOff";

      if (tab.getSelected()) {
        style = "ongletOn";
      }

      result.append("<td>\n");
      result.append(
          "<table cellpadding=\"0\" cellspacing=\"0\" border=\"0\" class=\"")
          .append(style).append("\">\n");
      result.append("<tr>");
      result.append("<td valign=top rowspan=2><img src=\"").append(iconsPath)
          .append("/tabs/comp_onglet_g.gif\"></td>\n");
      result.append("<td nowrap bgcolor=\"999999\"><img src=\"").append(
          iconsPath).append("/tabs/1px.gif\"></td>\n");
      result.append("<td rowspan=2 valign=top><img src=\"").append(iconsPath)
          .append("/tabs/comp_onglet_d.gif\"></td>\n");
      result.append("</tr>\n");
      result.append("<tr>");
      if (tab.getEnabled()) {
        result.append("<td align=center nowrap height=24><a href=\"").append(
            tab.getAction()).append("\" class=\"").append(style).append("\">")
            .append(tab.getLabel()).append("</a></td>");
      } else {
        result.append("<td align=center nowrap height=24><span class=\"")
            .append(style).append("\">").append(tab.getLabel()).append(
                "</span></td>");
      }
      result.append("</tr>\n");
      result.append("</table>\n");
      result.append("</td>\n");
    }

    if (indentation == LEFT) {
      result.append("<td width=\"100%\">&nbsp;</td>\n");

    }
    result.append("</tr>\n");
    result.append("</table>\n");
    return result.toString();
  }

}