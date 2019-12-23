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

import org.apache.ecs.xhtml.body;

import javax.servlet.jsp.JspException;

import static org.silverpeas.core.util.StringUtil.isDefined;
import static org.silverpeas.core.web.util.viewgenerator.html.JavascriptPluginInclusion.scriptContent;

/**
 * This tag MUST be the second included into a {@link HtmlLayoutTag}, after
 * {@link HeadLayoutPartTag}.
 */
public class BodyPartLayoutTag extends SilverpeasLayout {
  private static final long serialVersionUID = 7740509977305998444L;

  @Override
  public int doEndTag() throws JspException {
    body body = new body();
    body.addElement(getBodyContent().getString());
    renderAngularJs(body);
    body.output(pageContext.getOut());
    return EVAL_PAGE;
  }

  private void renderAngularJs(final body body) {
    final String angularJsAppName = getParent().getAngularJsAppName();
    if (isDefined(angularJsAppName)) {
      // declare the module myapp and its dependencies (here in the silverpeas module)
      body.addElement(scriptContent("var myapp = angular.module('" + angularJsAppName + "', ['silverpeas.services', 'silverpeas.directives'])"));
    }
  }

  @Override
  public HtmlLayoutTag getParent() {
    return (HtmlLayoutTag) super.getParent();
  }
}
