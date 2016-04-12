/*
 * Copyright (C) 2000 - 2015 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception. You should have recieved a copy of the text describing
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

package org.silverpeas.core.web.util.viewgenerator.html.override;

import org.silverpeas.core.util.URLUtil;
import org.apache.ecs.ElementContainer;
import org.silverpeas.core.web.util.viewgenerator.html.JavascriptPluginInclusion;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.TagSupport;

public class LinkTag extends TagSupport {
  private static final long serialVersionUID = -302801539118908018L;

  private String webContext = URLUtil.getApplicationURL();
  private String href;

  public void setWebContext(final String webContext) {
    this.webContext = webContext;
  }

  public String getWebContext() {
    return webContext;
  }

  public void setHref(final String href) {
    this.href = href;
  }

  public String getHref() {
    return href;
  }

  @Override
  public int doEndTag() throws JspException {
    ElementContainer script = new ElementContainer();
    String source = href.startsWith("/") && !href.startsWith(webContext) ? webContext + href : href;
    script.addElement(JavascriptPluginInclusion.link(source));
    script.output(pageContext.getOut());
    return EVAL_PAGE;
  }
}
