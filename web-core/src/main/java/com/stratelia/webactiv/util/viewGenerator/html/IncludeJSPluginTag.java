/*
* Copyright (C) 2000 - 2012 Silverpeas
*
* This program is free software: you can redistribute it and/or modify
* it under the terms of the GNU Affero General Public License as
* published by the Free Software Foundation, either version 3 of the
* License, or (at your option) any later version.
*
* As a special exception to the terms and conditions of version 3.0 of
* the GPL, you may redistribute this Program in connection withWriter Free/Libre
* Open Source Software ("FLOSS") applications as described in Silverpeas's
* FLOSS exception. You should have recieved a copy of the text describing
* the FLOSS exception, and it is also available here:
* "http://www.silverpeas.org/legal/licensing"
*
* This program is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
* GNU Affero General Public License for more details.
*
* You should have received a copy of the GNU Affero General Public License
* along with this program. If not, see <http://www.gnu.org/licenses/>.
*/
package com.stratelia.webactiv.util.viewGenerator.html;

import com.silverpeas.util.i18n.I18NHelper;
import com.stratelia.silverpeas.peasCore.MainSessionController;
import com.stratelia.silverpeas.peasCore.URLManager;
import static com.stratelia.webactiv.util.viewGenerator.html.SupportedJavaScriptPlugins.*;
import java.io.IOException;
import java.text.MessageFormat;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.SimpleTagSupport;
import org.apache.ecs.ElementContainer;
import org.apache.ecs.xhtml.link;
import org.apache.ecs.xhtml.script;

/**
* This tag is for including javascript plugins with their stylesheets.
*/
public class IncludeJSPluginTag extends SimpleTagSupport {

  private static final String MAIN_SESSION_CONTROLLER = "SilverSessionController";
  private static final String javascriptPath = URLManager.getApplicationURL() + "/util/javaScript/";
  private static final String stylesheetPath = URLManager.getApplicationURL() + "/util/styleSheets/";
  private static final String jqueryPath = javascriptPath + "jquery/";
  private static final String jqueryCssPath = stylesheetPath + "jquery/";
  private static final String JQUERY_QTIP = "jquery.qtip-1.0.0-rc3.min.js";
  private static final String SILVERPEAS_QTIP = "silverpeas-qtip-style.js";
  private static final String JQUERY_DATEPICKER = "jquery.ui.datepicker-{0}.js";
  private static final String SILVERPEAS_DATEPICKER = "silverpeas-defaultDatePicker.js";
  private static final String SILVERPEAS_DATE_UTILS = "dateUtils.js";
  private static final String PAGINATION_TOOL = "smartpaginator";
  private static final String JQUERY_BREADCRUMB = "silverpeas-breadcrumb.js";
  private static final String JAVASCRIPT_TYPE = "text/javascript";
  private static final String STYLESHEET_TYPE = "text/css";
  private static final String STYLESHEET_REL = "stylesheet";
  private String plugin;

  public String getName() {
    return plugin;
  }

  public void setName(String plugin) {
    this.plugin = plugin;
  }

  @Override
  public void doTag() throws JspException, IOException {
    ElementContainer xhtml = new ElementContainer();
    if (qtip.name().equals(getName())) {
      script qtip = new script().setType(JAVASCRIPT_TYPE).setSrc(jqueryPath + JQUERY_QTIP);
      script silverpeasQtip = new script().setType(JAVASCRIPT_TYPE).setSrc(jqueryPath
              + SILVERPEAS_QTIP);
      xhtml.addElement(qtip);
      xhtml.addElement(silverpeasQtip);
    } else if (datepicker.name().equals(getName())) {
      script datePicker = new script().setType(JAVASCRIPT_TYPE).setSrc(jqueryPath
              + MessageFormat.format(JQUERY_DATEPICKER, getLanguage()));
      script silverpeasDatePicker = new script().setType(JAVASCRIPT_TYPE).setSrc(javascriptPath
              + SILVERPEAS_DATEPICKER);
      script silverpeasDateUtils = new script().setType(JAVASCRIPT_TYPE).setSrc(javascriptPath
              + SILVERPEAS_DATE_UTILS);
      xhtml.addElement(datePicker);
      xhtml.addElement(silverpeasDatePicker);
      xhtml.addElement(silverpeasDateUtils);
    } else if (pagination.name().equals(getName())) {
      script pagination = new script().setType(JAVASCRIPT_TYPE).setSrc(jqueryPath + PAGINATION_TOOL
              + ".js");
      link css = new link().setType(STYLESHEET_TYPE).setRel(STYLESHEET_REL).setHref(jqueryCssPath
              + PAGINATION_TOOL + ".css");
      xhtml.addElement(css);
      xhtml.addElement(pagination);
    } else if (breadcrumb.name().equals(getName())) {
      script breadcrumb = new script().setType(JAVASCRIPT_TYPE).setSrc(javascriptPath + JQUERY_BREADCRUMB);
      xhtml.addElement(breadcrumb);
    }
    xhtml.output(getJspContext().getOut());
  }

  protected String getLanguage() {
    String language = I18NHelper.defaultLanguage;
    MainSessionController controller = getSessionAttribute(MAIN_SESSION_CONTROLLER);
    if (controller != null) {
      language = controller.getFavoriteLanguage();
    }
    return language;
  }

  protected <T> T getRequestAttribute(String name) {
    return (T) getJspContext().getAttribute(name, PageContext.REQUEST_SCOPE);
  }

  protected <T> T getSessionAttribute(String name) {
    return (T) getJspContext().getAttribute(name, PageContext.SESSION_SCOPE);
  }
}