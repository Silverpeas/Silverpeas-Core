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

import org.apache.ecs.xhtml.form;
import org.apache.ecs.xhtml.input;
import org.silverpeas.core.security.token.Token;
import org.silverpeas.core.util.Charsets;
import org.silverpeas.core.util.StringUtil;
import org.silverpeas.core.web.util.security.SecuritySettings;
import org.silverpeas.core.web.token.SynchronizerTokenService;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.BodyTagSupport;
import javax.ws.rs.core.MediaType;

import static org.silverpeas.core.web.token.SynchronizerTokenService.NAVIGATION_TOKEN_KEY;
import static org.silverpeas.core.web.token.SynchronizerTokenService.SESSION_TOKEN_KEY;

public class FormTag extends BodyTagSupport {
  private static final long serialVersionUID = -4465070678331028270L;

  private String name;
  private String action;
  private String method = "POST";

  public void setName(final String name) {
    this.name = name;
  }

  public void setAction(final String action) {
    this.action = action;
  }

  public void setMethod(final String method) {
    this.method = StringUtil.isDefined(method) ? method : this.method;
  }

  @Override
  public int doEndTag() throws JspException {
    form form = new form();
    form.setName(name);
    form.setAction(action);
    form.setMethod(method);
    form.setAcceptCharset(Charsets.UTF_8.name());
    form.setEncType(MediaType.MULTIPART_FORM_DATA);
    if (StringUtil.isDefined(getId())) {
      form.setID(getId());
    }
    if (SecuritySettings.isWebSecurityByTokensEnabled()) {
      SynchronizerTokenService service = SynchronizerTokenService.getInstance();
      Token token = service.getSessionToken((HttpServletRequest) pageContext.getRequest());
      if (token.isDefined()) {
        form.addElement(
            new input().setType("hidden").setName(SESSION_TOKEN_KEY).setValue(token.getValue()));
      }
      token = service.getNavigationToken((HttpServletRequest) pageContext.getRequest());
      if (token.isDefined()) {
        form.addElement(
            new input().setType("hidden").setName(NAVIGATION_TOKEN_KEY).setValue(token.getValue()));
      }
    }
    form.addElement(getBodyContent().getString());
    form.output(pageContext.getOut());
    return EVAL_PAGE;
  }
}
