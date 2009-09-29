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

/*
 * ArrayPaneWA.java
 * 
 * Created on 10 octobre 2000, 16:11
 */

package com.stratelia.webactiv.util.viewGenerator.html.operationPanes;

import java.util.Vector;

import com.silverpeas.util.StringUtil;
import com.stratelia.webactiv.util.viewGenerator.html.GraphicElementFactory;

/**
 * The default implementation of ArrayPane interface
 * 
 * @author squere
 * @version 1.0
 */
public class OperationPaneWeb20 extends AbstractOperationPane {

  /**
   * Constructor declaration
   * 
   * 
   * @see
   */
  public OperationPaneWeb20() {
    super();
  }

  /**
   * Method declaration
   * 
   * 
   * @param iconPath
   * @param altText
   * @param action
   * 
   * @see
   */
  public void addOperation(String iconPath, String altText, String action) {
    Vector stack = getStack();
    StringBuffer operation = new StringBuffer();

    if (!StringUtil.isDefined(altText))
      altText = action;

    operation.append(
        "<li class=\"yuimenuitem\"><a class=\"yuimenuitemlabel\" href=\"")
        .append(action).append("\">").append(altText).append("</a></li>");

    stack.add(operation.toString());
  }

  /**
   * Method declaration
   * 
   * 
   * @see
   */
  public void addLine() {
    Vector stack = getStack();

    stack.add("</ul>\n<ul>");
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
    Vector stack = getStack();

    // result.append(OperationPaneSilverpeasV4StringFactory.getPrintString1());

    result.append(
        "<div align=\"right\">"
            + GraphicElementFactory.getSettings().getString("ActionsLabel",
                "Opérations") + "<span id=\"menutoggle\"><img src=\"").append(
        getIconsPath()).append("/ptr.gif\"></span></div>");

    result.append("<div id=\"menuwithgroups\" class=\"yuimenu\">");
    result.append("<div class=\"bd\">");
    result.append("<ul class=\"first-of-type\">");
    for (int i = 0; i < stack.size(); i++) {
      result.append((String) stack.elementAt(i));
    }
    result.append("</ul>");
    result.append("</div>");
    result.append("</div>");

    result.append("<!-- Page-specific script -->");

    result.append("<script type=\"text/javascript\">");

    // Instantiate and render the menu when it is available in the DOM
    result
        .append("YAHOO.util.Event.onContentReady(\"menuwithgroups\", function () {");

    /*
     * Instantiate a Menu: The first argument passed to the constructor is the
     * id of the element in the page representing the Menu; the second is an
     * object literal of configuration properties.
     */
    result
        .append("var oMenu = new YAHOO.widget.Menu(\"menuwithgroups\", { position: \"dynamic\", iframe: true, context: [\"menutoggle\", \"tr\", \"br\"] });");

    /*
     * Call the "render" method with no arguments since the markup for this Menu
     * instance is already exists in the page.
     */
    result.append("oMenu.render();");

    // Set focus to the Menu when it is made visible
    result.append("oMenu.subscribe(\"show\", oMenu.focus);");

    result
        .append("YAHOO.util.Event.addListener(\"menutoggle\", \"mouseover\", oMenu.show, null, oMenu);");

    result.append("});");
    result.append("</script>");

    return result.toString();
  }

}