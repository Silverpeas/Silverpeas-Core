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

import org.silverpeas.core.util.LocalizationBundle;
import org.silverpeas.core.util.ResourceLocator;
import org.silverpeas.core.util.SettingBundle;
import org.silverpeas.core.util.SilverpeasBundle;
import org.silverpeas.core.util.StringUtil;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.jstl.core.Config;
import javax.servlet.jsp.jstl.fmt.LocalizationContext;
import javax.servlet.jsp.tagext.TagSupport;
import java.util.Locale;
import java.util.ResourceBundle;

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

  public void setBundle(SilverpeasBundle bundle) {
    if (bundle instanceof LocalizationBundle) {
      this.bundle = (LocalizationBundle) bundle;
    } else if (bundle instanceof SettingBundle) {
      this.bundle = ((SettingBundle) bundle).asResourceBundle();
    } else {
      throw new IllegalArgumentException("bundle must be of type LocalizationBundle or SettingBundle");
    }
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
      bundle =
          ResourceLocator.getLocalizationBundle(basename, locale.getLanguage());
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