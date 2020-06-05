/*
 * Copyright (C) 2000 - 2020 Silverpeas
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
 * "https://www.silverpeas.org/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.silverpeas.core.web.util.viewgenerator.html.operationpanes;

import org.apache.ecs.html.LI;
import org.silverpeas.core.util.StringUtil;
import org.silverpeas.core.util.WebEncodeHelper;
import org.silverpeas.core.web.util.viewgenerator.html.TagUtil;

import java.util.Vector;

public class OperationPaneSilverpeasV5Web20 extends AbstractOperationPane {

  private static final String LINE = "</ul>\n<ul>";

  /**
   * Constructor declaration
   * @see
   */
  public OperationPaneSilverpeasV5Web20() {
    super();
  }

  @Override
  public void addOperation(final String iconPath, final String label, final String action,
      final String id) {
    String operationLabel = label;
    if (!StringUtil.isDefined(label)) {
      operationLabel = action;
    }

    LI li = new LI();
    li.setClass("yuimenuitem");
    if (StringUtil.isDefined(id)) {
      li.setID(id);
    }
    String href = TagUtil.formatHrefFromAction(action);
    String a = "<a class=\"yuimenuitemlabel\" "+href+">"+operationLabel+"</a></li>";
    li.addElement(a);

    getStack().add(li.toString());
  }

  @Override
  public void addOperationOfCreation(final String icon, final String label, final String action,
      final String classes) {
    addOperation(icon, label, action, classes);
    String href = TagUtil.formatHrefFromAction(action);
    getCreationItems().add("<a " + href +
        " class=\"menubar-creation-actions-item\" style=\"display:none\"><span><img src=\"" + icon +
        "\" alt=\"\"/>" + WebEncodeHelper.javaStringToHtmlString(label) + "</span></a>");
  }

  @Override
  public void addLine() {
    getStack().add(LINE);
  }

  /**
   * Method declaration
   * @return
   * @see
   */
  @Override
  public String print() {
    StringBuilder result = new StringBuilder();
    Vector<String> stack = getStack();

    String alt = getMultilang().getString("GEF.operations.label");

    result.append("<div id='whatNextMenu' align='right'><span id='menutoggle'>").append(alt)
        .append("<img src=\"").append(getIconsPath()).append("/ptr.gif\" alt=\"").append(alt).append(
            "\"/></span></div>");

    result.append("<div id=\"menuwithgroups\" class=\"yuimenu\">");
    result.append("<div class=\"bd\">");
    result.append("<ul class=\"first-of-type\">");

    // prevents to display a line as last entry
    String lastElement = stack.lastElement();
    if (lastElement.equals(LINE)) {
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

    if(highlightCreationItems()) {
      getCreationItems().forEach(result::append);
    }

    result.append("<!-- Page-specific script -->");

    result.append("<script type=\"text/javascript\">");
    result.append("var oMenu;");
    result.append("var menuRenderedDeferred = new jQuery.Deferred();");
    result.append("var menuRenderedPromise = menuRenderedDeferred.promise();");
    // Instantiate and render the menu when it is available in the DOM
    result
        .append("YAHOO.util.Event.onContentReady(\"menuwithgroups\", function () {");

    /*
     * Instantiate a Menu: The first argument passed to the constructor is the id of the element in
     * the page representing the Menu; the second is an object literal of configuration properties.
     */
    result
        .append(
            "oMenu = new YAHOO.widget.Menu(\"menuwithgroups\", { position: \"dynamic\", iframe: true, context: [\"menutoggle\", \"tr\", \"br\"] });");

    /*
     * Call the "render" method with no arguments since the markup for this Menu instance is already
     * exists in the page.
     */
    result.append("oMenu.render();");

    // Set focus to the Menu when it is made visible
    result.append("oMenu.subscribe(\"show\", oMenu.focus);");

    result
        .append(
            "YAHOO.util.Event.addListener(\"menutoggle\", \"mouseover\", oMenu.show, null, oMenu);");

    result
        .append(
            "if (('ontouchstart' in window) || (navigator.maxTouchPoints > 0) || (navigator.msMaxTouchPoints > 0)) ")
        .append("$('#whatNextMenu').bind('touchstart', function() { oMenu.show(); });");

    if (highlightCreationItems()) {
      result.append(
          "var $creationArea = $('#" + OperationsOfCreationAreaTag.CREATION_AREA_ID + "');");
      result.append("if ($creationArea.length > 0) {");
      if (!getCreationItems().isEmpty()) {
        result.append("$('.menubar-creation-actions-item:not(.menubar-creation-actions-move-ignored)').appendTo($creationArea);");
        result.append("$('a', $creationArea).css({'display':''});");
        result.append("$creationArea.css({'display':'block'});");
      }
      result.append("}");
    }

    // Adjusting the position of the drop down menu in order to be rightly displayed in any case
    result.append("var __setDropDownMenuPosition = function() {");
    result.append("var $menu = document.querySelector('#whatNextMenu');");
    result.append("var menuWidth = $menu.offsetWidth;");
    result.append("var $dropDownMenu = oMenu.element;");
    result.append("var dropDownMenuWidth = $dropDownMenu.offsetWidth;");
    result.append("var left = menuWidth - dropDownMenuWidth;");
    result.append("$dropDownMenu.style.left = left + 'px';");
    result.append("};");
    result.append("oMenu.subscribe('show', __setDropDownMenuPosition);");
    result.append("__setDropDownMenuPosition();");

    // Once the menu is rendered this below event is triggered
    result.append("setTimeout(applyTokenSecurityOnMenu, 0);");
    result.append("menuRenderedDeferred.resolve();");

    result.append("});");
    result.append("</script>");

    return result.toString();
  }

}
