/*
 * Copyright (C) 2000 - 2024 Silverpeas
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
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package org.silverpeas.core.web.util.viewgenerator.html;

import org.apache.ecs.ElementContainer;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.TagSupport;

import static org.silverpeas.core.util.URLUtil.getApplicationURL;
import static org.silverpeas.core.web.util.viewgenerator.html.JavascriptPluginInclusion.link;
import static org.silverpeas.core.web.util.viewgenerator.html.JavascriptPluginInclusion.script;
import static org.silverpeas.core.web.util.viewgenerator.html.WebCommonLookAndFeel.LOOK_CONTEXT_MANAGER_CALLBACK_ONLY_ATTR;

public class LookAndStyleTag extends TagSupport {

  private static final long serialVersionUID = 1L;

  private boolean lookContextManagerCallbackOnly;
  private boolean withFieldsetStyle;
  private boolean withCheckFormScript;

  public void setLookContextManagerCallbackOnly(final boolean lookContextManagerCallbackOnly) {
    this.lookContextManagerCallbackOnly = lookContextManagerCallbackOnly;
  }

  public void setWithFieldsetStyle(final boolean withFieldsetStyle) {
    this.withFieldsetStyle = withFieldsetStyle;
  }

  public void setWithCheckFormScript(final boolean withCheckFormScript) {
    this.withCheckFormScript = withCheckFormScript;
  }

  @Override
  public int doStartTag() throws JspException {
    getContent().output(pageContext.getOut());
    return SKIP_BODY;
  }

  public ElementContainer getContent() {
    final ElementContainer elements = new ElementContainer();
    final HttpServletRequest request = (HttpServletRequest) pageContext.getRequest();
    request.setAttribute(LOOK_CONTEXT_MANAGER_CALLBACK_ONLY_ATTR, lookContextManagerCallbackOnly);
    elements.addElement(WebCommonLookAndFeel.getInstance().getCommonHeader(request));
    if (withFieldsetStyle) {
      elements.addElement(link(getApplicationURL() + "/util/styleSheets/fieldset.css"));
    }
    if (withCheckFormScript) {
      elements.addElement(script(getApplicationURL() + "/util/javaScript/checkForm.js"));
    }
    return elements;
  }
}
