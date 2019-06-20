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
package org.silverpeas.core.web.util.viewgenerator.html;

import org.apache.ecs.ElementContainer;
import org.silverpeas.core.html.SupportedWebPlugins;
import org.silverpeas.core.html.WebPlugin;
import org.silverpeas.core.i18n.I18NHelper;
import org.silverpeas.core.util.logging.SilverLogger;
import org.silverpeas.core.web.look.LookHelper;
import org.silverpeas.core.web.mvc.controller.MainSessionController;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.SimpleTagSupport;
import java.io.IOException;

import static org.silverpeas.core.html.SupportedWebPlugins.*;
import static org.silverpeas.core.web.util.viewgenerator.html.JavascriptPluginInclusion.*;

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
    String language = I18NHelper.defaultLanguage;
    MainSessionController controller = getSessionAttribute(MAIN_SESSION_CONTROLLER);
    if (controller != null) {
      language = controller.getFavoriteLanguage();
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
