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

package org.silverpeas.core.web.util.viewgenerator.html;

import org.silverpeas.core.util.URLUtil;
import org.apache.ecs.ElementContainer;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.TagSupport;
import java.io.IOException;

public class LookAndStyleTag extends TagSupport {

  private static final long serialVersionUID = 1L;

  private boolean withFieldsetStyle;
  private boolean withCheckFormScript;

  public void setWithFieldsetStyle(final boolean withFieldsetStyle) {
    this.withFieldsetStyle = withFieldsetStyle;
  }

  public void setWithCheckFormScript(final boolean withCheckFormScript) {
    this.withCheckFormScript = withCheckFormScript;
  }

  @Override
  public int doStartTag() throws JspException {
    try {
      pageContext.getOut().println(WebCommonLookAndFeel.getInstance()
          .getCommonHeader((HttpServletRequest) pageContext.getRequest()));


      ElementContainer elements = new ElementContainer();
      if (withFieldsetStyle) {
        elements.addElement(JavascriptPluginInclusion
            .link(URLUtil.getApplicationURL() + "/util/styleSheets/fieldset.css"));
      }
      if (withCheckFormScript) {
        elements.addElement(JavascriptPluginInclusion
            .script(URLUtil.getApplicationURL() + "/util/javaScript/checkForm.js"));
      }
      elements.output(pageContext.getOut());
    } catch (IOException e) {
      throw new JspException("LookAndStyle Tag", e);
    }
    return SKIP_BODY;
  }
}
