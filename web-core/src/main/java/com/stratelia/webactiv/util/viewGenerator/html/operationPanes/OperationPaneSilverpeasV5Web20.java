/**
 * Copyright (C) 2000 - 2012 Silverpeas
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
 * "http://www.silverpeas.org/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.stratelia.webactiv.util.viewGenerator.html.operationPanes;

import com.silverpeas.util.EncodeHelper;
import com.silverpeas.util.StringUtil;

import java.util.Vector;

public class OperationPaneSilverpeasV5Web20 extends AbstractOperationPane {

  private final static String line = "</ul>\n<ul>";

  /**
   * Constructor declaration
   * @see
   */
  public OperationPaneSilverpeasV5Web20() {
    super();
  }

  /**
   * Method declaration
   * @param iconPath
   * @param label
   * @param action
   * @see
   */
  public void addOperation(String iconPath, String label, String action) {
    StringBuilder operation = new StringBuilder();

    if (!StringUtil.isDefined(label)) {
      label = action;
    }

    operation.append(
        "<li class=\"yuimenuitem\"><a title=\"\" class=\"yuimenuitemlabel\" href=\"")
        .append(action).append("\">").append(label).append("</a></li>");

    getStack().add(operation.toString());
  }
  
  public void addOperationOfCreation(String icon, String label, String action) {
    addOperation(icon, label, action);
    getCreationItems().add(
        "<a href=\"" + EncodeHelper.javaStringToJsString(action) + "\" class=\"menubar-creation-actions-item\"><span><img src=\"" + icon +
            "\" alt=\"\"/>" + EncodeHelper.javaStringToJsString(label) + "</span></a>");
  }

  /**
   * Method declaration
   * @see
   */
  public void addLine() {
    getStack().add(line);
  }

  /**
   * Method declaration
   * @return
   * @see
   */
  public String print() {
    StringBuilder result = new StringBuilder();
    Vector<String> stack = getStack();

    String alt = getMultilang().getString("GEF.operations.label", "Opérations");

    result.append("<div align=\"right\"><span id=\"menutoggle\">").append(alt).append(
        "<img src=\"").append(getIconsPath()).append("/ptr.gif\" alt=\"").append(alt).append(
        "\"/></span></div>");

    result.append("<div id=\"menuwithgroups\" class=\"yuimenu\">");
    result.append("<div class=\"bd\">");
    result.append("<ul class=\"first-of-type\">");

    // prevents to display a line as last entry
    String lastElement = stack.lastElement();
    if (lastElement.equals(line)) {
      stack.removeElementAt(stack.size() - 1);
    }

    String lastItem = "";
    for (String item : stack) {
      if (!item.equals(lastItem)) {
        // prevents to display two same items
        result.append(item);
      }
      lastItem = item;
    }
    result.append("</ul>");
    result.append("</div>");
    result.append("</div>");

    result.append("<!-- Page-specific script -->");

    result.append("<script type=\"text/javascript\">");
    result.append("var oMenu;");
    // Instantiate and render the menu when it is available in the DOM
    result
        .append("YAHOO.util.Event.onContentReady(\"menuwithgroups\", function () {");

    /*
     * Instantiate a Menu: The first argument passed to the constructor is the id of the element in
     * the page representing the Menu; the second is an object literal of configuration properties.
     */
    result
        .append("oMenu = new YAHOO.widget.Menu(\"menuwithgroups\", { position: \"dynamic\", iframe: true, context: [\"menutoggle\", \"tr\", \"br\"] });");

    /*
     * Call the "render" method with no arguments since the markup for this Menu instance is already
     * exists in the page.
     */
    result.append("oMenu.render();");

    // Set focus to the Menu when it is made visible
    result.append("oMenu.subscribe(\"show\", oMenu.focus);");

    result
        .append("YAHOO.util.Event.addListener(\"menutoggle\", \"mouseover\", oMenu.show, null, oMenu);");
    
    if (highlightCreationItems()) {
      result.append("if ($('#").append(OperationsOfCreationAreaTag.CREATION_AREA_ID)
          .append("').length > 0) {");
      if (getCreationItems().isEmpty()) {
        result.append("$('#").append(OperationsOfCreationAreaTag.CREATION_AREA_ID)
          .append("').css({'display':'none'});");
      } else {
        for (String item : getCreationItems()) {
          result.append("$('#").append(OperationsOfCreationAreaTag.CREATION_AREA_ID)
              .append("').append('").append(item).append("');");
        }
      }
      result.append("}");
    }

    result.append("});");
    result.append("</script>");

    return result.toString();
  }

}