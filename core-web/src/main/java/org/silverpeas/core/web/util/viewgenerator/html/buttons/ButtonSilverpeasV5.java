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
 * FLOSS exception. You should have received a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.silverpeas.core.web.util.viewgenerator.html.buttons;

/**
 * @author neysseri
 * @version
 */
public class ButtonSilverpeasV5 extends AbstractButton {

  /**
   * Creates new ButtonWA
   */
  public ButtonSilverpeasV5() {
  }

  /**
   * Method declaration
   * @return
   * @see
   */
  @Override
  public String renderButtonHtml() {
    String theAction = getAction();
    String iconsPath = getIconsPath();

    if (disabled) {
      theAction = "#";
    } else if (theAction.startsWith("angularjs:")) {
      theAction = theAction.substring(10);
      if (theAction.contains("{{") && theAction.contains("}}")) {
        theAction = "ng-href=\"" + theAction + "\"";
      } else {
        theAction = "href=\"#\" ng-click=\"" + theAction + "\"";
      }
    } else {
      theAction = "href=\"" + theAction + "\"";
    }

    StringBuilder str = new StringBuilder();
    str.append("<table border=\"0\" cellspacing=\"0\" cellpadding=\"0\">");
    str.append("<tr>");
    str.append("<td align=\"left\" class=\"gaucheBoutonV5\"><img src=\"")
        .append(iconsPath).append("/px.gif\" alt=\"\"/></td>");
    str.append("<td nowrap=\"nowrap\" class=\"milieuBoutonV5\"><a ").append(theAction)
        .append(" >").append(label).append("</a></td>");
    str.append("<td align=\"right\" class=\"droiteBoutonV5\"><img src=\"")
        .append(iconsPath).append("/px.gif\" alt=\"\"/></td>");
    str.append("</tr>");
    str.append("</table>");

    return str.toString();
  }

}
