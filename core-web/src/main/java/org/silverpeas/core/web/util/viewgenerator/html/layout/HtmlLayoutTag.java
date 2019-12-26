/*
 * Copyright (C) 2000 - 2019 Silverpeas
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
package org.silverpeas.core.web.util.viewgenerator.html.layout;

import org.apache.ecs.ElementContainer;
import org.apache.ecs.xhtml.html;
import org.silverpeas.core.util.StringUtil;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.JspException;

/**
 * This tag MUST be the first called from a JSP page providing HTML code.
 * <ul>
 * It MUST includes directly the following ordered children:
 * <li>{@link HeadLayoutPartTag}</li>
 * <li>{@link BodyPartLayoutTag}</li>
 * </ul>
 * <p>
 * Attribute {@link #angularJsAppName} permits to indicate the AngularJS application name. If any
 * the HTML tag is parametrized rightly (id and ng-app attribute) with the given name and the
 * JavaScript bloc to start the AngularJS application is added automatically at the end of the
 * BODY part.
 * </p>
 */
public class HtmlLayoutTag extends SilverpeasLayout {
  private static final long serialVersionUID = 6951194048886306279L;
  private static final String DOCTYPE = "<!DOCTYPE html PUBLIC " +
      "\"-//W3C//DTD XHTML 1.0 Transitional//EN\" " +
      "\"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\">";

  private String angularJsAppName;

  public void setAngularJsAppName(final String angularJsAppName) {
    this.angularJsAppName = angularJsAppName;
  }

  String getAngularJsAppName() {
    return angularJsAppName;
  }

  @Override
  public int doStartTag() throws JspException {
    final HttpServletResponse response = (HttpServletResponse) pageContext.getResponse();
    response.setHeader("Cache-Control", "no-store");
    response.setHeader("Pragma", "no-cache");
    response.setDateHeader("Expires", -1);
    pageContext.setAttribute("userLanguage", getUserLanguage());
    pageContext.setAttribute("componentId", getComponentId());
    return super.doStartTag();
  }

  @Override
  public int doEndTag() throws JspException {
    final ElementContainer elements = new ElementContainer();
    elements.addElement(DOCTYPE);
    final html html = new html();
    html.addAttribute("xml:lang", getUserLanguage());
    renderAngularJs(html);
    html.addElement(getBodyContent().getString());
    elements.addElement(html);
    elements.output(pageContext.getOut());
    return EVAL_PAGE;
  }

  private void renderAngularJs(final html html) {
    if (StringUtil.isDefined(getAngularJsAppName())) {
      html.addAttribute("id", "ng-app");
      html.addAttribute("ng-app", getAngularJsAppName());
    }
  }
}
