/*
 * Copyright (C) 2000 - 2021 Silverpeas
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
package org.silverpeas.core.web.util.viewgenerator.html;

import org.apache.ecs.ElementContainer;
import org.silverpeas.core.html.SupportedWebPlugins;
import org.silverpeas.core.html.WebPlugin;
import org.silverpeas.core.ui.DisplayI18NHelper;
import org.silverpeas.core.util.StringUtil;
import org.silverpeas.core.util.logging.SilverLogger;
import org.silverpeas.core.web.look.LookHelper;
import org.silverpeas.core.web.mvc.controller.MainSessionController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.SimpleTagSupport;
import java.io.IOException;

import static org.silverpeas.core.html.SupportedWebPlugins.LAYOUT;
import static org.silverpeas.core.html.SupportedWebPlugins.USERSESSION;
import static org.silverpeas.core.web.util.viewgenerator.html.JavascriptPluginInclusion.includeLayout;
import static org.silverpeas.core.web.util.viewgenerator.html.JavascriptPluginInclusion.includeUserSession;

/**
 * This tag is for including javascript plugins with their stylesheets.
 */
public class IncludeJSPluginTag extends SimpleTagSupport {

  private static final String MAIN_SESSION_CONTROLLER = "SilverSessionController";
  private String plugin;

  public String getName() {
    return plugin;
  }

  public void setName(String plugin) {
    this.plugin = plugin.toUpperCase();
  }

  @Override
  public void doTag() throws JspException, IOException {
    ElementContainer xhtml = new ElementContainer();
    try {
      SupportedWebPlugins jsPlugin = SupportedWebPlugins.valueOf(getName());
      xhtml = WebPlugin.get().getHtml(jsPlugin, getLanguage());
      if (LAYOUT == jsPlugin) {
        includeLayout(xhtml, getLookHelper());
      } else if (USERSESSION == jsPlugin) {
        includeUserSession(xhtml, getLookHelper());
      }
    } catch (IllegalArgumentException ex) {
      SilverLogger.getLogger(this).error(ex.getMessage(), ex);
    }
    xhtml.output(getJspContext().getOut());
  }

  protected LookHelper getLookHelper() {
    return getSessionAttribute(LookHelper.SESSION_ATT);
  }

  protected String getLanguage() {
    final String language;
    MainSessionController controller = getSessionAttribute(MAIN_SESSION_CONTROLLER);
    if (controller != null) {
      language = controller.getFavoriteLanguage();
    } else if (StringUtil.isDefined(getRequestAttribute("userLanguage"))) {
      language = getRequestAttribute("userLanguage");
    } else {
      final PageContext pageContext = (PageContext) getJspContext();
      final HttpServletRequest request = (HttpServletRequest) pageContext.getRequest();
      if (request.getLocale() != null) {
        language = DisplayI18NHelper.verifyLanguage(request.getLocale().getLanguage());
      } else {
        language = DisplayI18NHelper.getDefaultLanguage();
      }
    }
    return language;
  }

  @SuppressWarnings("unchecked")
  protected <T> T getRequestAttribute(String name) {
    return (T) getJspContext().getAttribute(name, PageContext.REQUEST_SCOPE);
  }

  @SuppressWarnings("unchecked")
  protected <T> T getSessionAttribute(String name) {
    return (T) getJspContext().getAttribute(name, PageContext.SESSION_SCOPE);
  }
}
