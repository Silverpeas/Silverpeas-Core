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
* "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
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
import org.apache.ecs.ElementContainer;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.SimpleTagSupport;
import java.io.IOException;

import static com.stratelia.webactiv.util.viewGenerator.html.JavascriptPluginInclusion.*;
import static com.stratelia.webactiv.util.viewGenerator.html.SupportedJavaScriptPlugins.*;

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
    this.plugin = plugin;
  }

  @Override
  public void doTag() throws JspException, IOException {
    ElementContainer xhtml = new ElementContainer();
    if (qtip.name().equals(getName())) {
      includeQTip(xhtml);
    } else if (datepicker.name().equals(getName())) {
      includeDatePicker(xhtml, getLanguage());
    } else if (pagination.name().equals(getName())) {
      includePagination(xhtml);
    } else if (breadcrumb.name().equals(getName())) {
      includeBreadCrumb(xhtml);
    } else if (userZoom.name().equals(getName())) {
      includeUserZoom(xhtml);
    } else if (invitme.name().equals(getName())) {
      includeInvitMe(xhtml);
    } else if (messageme.name().equals(getName())) {
      includeMessageMe(xhtml);
    } else if (wysiwyg.name().equals(getName())) {
      includeWysiwygEditor(xhtml);
    } else if (popup.name().equals(getName())) {
      includePopup(xhtml);
    } else if (calendar.name().equals(getName())) {
      includeCalendar(xhtml);
    } else if (preview.name().equals(getName())) {
      includePreview(xhtml);
    } else if (notifier.name().equals(getName())) {
      includeNotifier(xhtml);
    } else if (password.name().equals(getName())) {
      includePassword(xhtml);
    } else if (jquery.name().equals(getName())) {
      includeJQuery(xhtml);
    } else if (tags.name().equals(getName())) {
      includeTags(xhtml);
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
