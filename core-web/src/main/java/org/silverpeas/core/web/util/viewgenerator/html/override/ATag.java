/*
 * Copyright (C) 2000 - 2014 Silverpeas
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

import org.apache.ecs.xhtml.a;
import org.silverpeas.core.security.token.Token;
import org.silverpeas.core.util.StringUtil;
import org.silverpeas.core.web.util.security.SecuritySettings;
import org.silverpeas.core.web.token.SynchronizerTokenService;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.BodyTagSupport;

import static org.silverpeas.core.web.token.SynchronizerTokenService.NAVIGATION_TOKEN_KEY;
import static org.silverpeas.core.web.token.SynchronizerTokenService.SESSION_TOKEN_KEY;

public class ATag extends BodyTagSupport {
  private static final long serialVersionUID = -1083049090612214503L;

  private String name;
  private String href = "#";
  private String classes;
  private String style;
  private String onClick;

  public void setName(final String name) {
    this.name = name;
  }

  public void setHref(final String href) {
    this.href = StringUtil.isDefined(href) ? href : this.href;
  }

  public void setClasses(final String classes) {
    this.classes = classes;
  }

  public void setStyle(final String style) {
    this.style = style;
  }

  public void setOnClick(final String onClick) {
    this.onClick = onClick;
  }

  @Override
  public int doEndTag() throws JspException {
    a a = new a();
    if (StringUtil.isDefined(getId())) {
      a.setID(getId());
    }
    if (StringUtil.isDefined(name)) {
      a.setName(name);
    }
    if (SecuritySettings.isWebSecurityByTokensEnabled() && !"#".equals(href)) {
      SynchronizerTokenService service = SynchronizerTokenService.getInstance();
      Token token = service.getSessionToken((HttpServletRequest) pageContext.getRequest());
      if (token.isDefined()) {
        href += (href.contains("?") ? "&" : "?");
        href += SESSION_TOKEN_KEY + "=" + token.getValue();
      }
      token = service.getNavigationToken((HttpServletRequest) pageContext.getRequest());
      if (token.isDefined()) {
        href += (href.contains("?") ? "&" : "?");
        href += NAVIGATION_TOKEN_KEY + "=" + token.getValue();
      }
    }
    a.setHref(href);
    if (StringUtil.isDefined(classes)) {
      a.setClass(classes);
    }
    if (StringUtil.isDefined(style)) {
      a.setStyle(style);
    }
    if (StringUtil.isDefined(onClick)) {
      a.setOnClick(onClick);
    }
    a.addElement(getBodyContent().getString());
    a.output(pageContext.getOut());
    return EVAL_PAGE;
  }
}
