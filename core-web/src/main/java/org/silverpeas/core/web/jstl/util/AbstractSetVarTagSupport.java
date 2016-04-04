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
package org.silverpeas.core.web.jstl.util;

import org.silverpeas.core.util.StringUtil;

import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.TagSupport;

/**
 * Abstract tag support class for implementing SimpleTag custom actions that specify
 * <code>var</code> and <code>scope</code> attributes.
 */
public abstract class AbstractSetVarTagSupport extends TagSupport {

  private String var;
  private int scope = PageContext.PAGE_SCOPE;

  public void setVar(String value) {
    this.var = value;
  }

  public void setScope(String scope) {
    if (StringUtil.isDefined(scope)) {
      if ("request".equalsIgnoreCase(scope)) {
        this.scope = PageContext.REQUEST_SCOPE;
      } else if ("session".equalsIgnoreCase(scope)) {
        this.scope = PageContext.SESSION_SCOPE;
      } else if ("application".equalsIgnoreCase(scope)) {
        this.scope = PageContext.APPLICATION_SCOPE;
      } else {
        this.scope = PageContext.PAGE_SCOPE; // default
      }
    }
  }

  protected int getScope() {
    return this.scope;
  }

  protected String getVar() {
    return this.var;
  }

  protected void setScopedVariable(Object value) {
    pageContext.setAttribute(this.var, value, getScope());
  }
}
