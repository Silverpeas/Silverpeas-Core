/**
 * Copyright (C) 2000 - 2012 Silverpeas
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
 * "http://www.silverpeas.org/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.stratelia.webactiv.util.viewGenerator.html;

import java.util.Locale;
import java.util.ResourceBundle;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.jstl.core.Config;
import javax.servlet.jsp.jstl.fmt.LocalizationContext;
import javax.servlet.jsp.tagext.TagSupport;

import org.apache.taglibs.standard.tag.common.core.Util;

import com.silverpeas.util.StringUtil;

import com.stratelia.webactiv.util.ResourceLocator;

public class SetBundleTag extends TagSupport {

  private static final long serialVersionUID = 7549830839518939649L;
  private int scope = PageContext.PAGE_SCOPE;
  private String var;
  private String basename;

  private ResourceBundle bundle;

  public void setVar(String var) {
    this.var = var;
  }

  public void setScope(String scope) {
    this.scope = Util.getScope(scope);
  }

  public void setBundle(ResourceBundle bundle) {
    this.bundle = bundle;
  }

  public void setBasename(String basename) {
    this.basename = basename;
  }

  @Override
  public int doEndTag() throws JspException {
    Locale locale = (Locale) Config.find(pageContext, Config.FMT_LOCALE);
    if (locale == null) {
      locale = Locale.getDefault();
    }
    if (StringUtil.isDefined(basename)) {
      ResourceLocator locator = new ResourceLocator(basename, locale.getLanguage());
      bundle = locator.getResourceBundle();
    }
    LocalizationContext locCtxt = new LocalizationContext(bundle);
    if (var != null) {
      pageContext.setAttribute(var, locCtxt, scope);
    } else {
      Config.set(pageContext, Config.FMT_LOCALIZATION_CONTEXT, locCtxt, scope);
    }
    return EVAL_PAGE;
  }
}