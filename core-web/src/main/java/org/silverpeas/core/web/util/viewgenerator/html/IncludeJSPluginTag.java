/*
 * Copyright (C) 2000 - 2013 Silverpeas
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
package org.silverpeas.core.web.util.viewgenerator.html;

import org.silverpeas.core.i18n.I18NHelper;
import org.silverpeas.core.web.mvc.controller.MainSessionController;
import java.io.IOException;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.SimpleTagSupport;
import org.apache.ecs.ElementContainer;

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
    this.plugin = plugin;
  }

  @Override
  public void doTag() throws JspException, IOException {
    ElementContainer xhtml = new ElementContainer();
    try {
      SupportedJavaScriptPlugins jsPlugin = SupportedJavaScriptPlugins.valueOf(getName());
      switch (jsPlugin) {
        case embedPlayer:
          includeEmbedPlayer(xhtml);
          break;
        case audioPlayer:
          includeAudioPlayer(xhtml);
          break;
        case videoPlayer:
          includeVideoPlayer(xhtml);
          break;
        case qtip:
          includeQTip(xhtml);
          break;
        case datepicker:
          includeDatePicker(xhtml, getLanguage());
          break;
        case pagination:
          includePagination(xhtml);
          break;
        case breadcrumb:
          includeBreadCrumb(xhtml);
          break;
        case userZoom:
          includeUserZoom(xhtml);
          break;
        case invitme:
          includeInvitMe(xhtml);
          break;
        case messageme:
          includeMessageMe(xhtml);
          break;
        case wysiwyg:
          includeWysiwygEditor(xhtml);
          break;
        case responsibles:
          includeResponsibles(xhtml, getLanguage());
          break;
        case popup:
          includePopup(xhtml);
          break;
        case calendar:
          includeCalendar(xhtml);
          break;
        case iframeajaxtransport:
          includeIFrameAjaxTransport(xhtml);
          break;
        case preview:
          includePreview(xhtml);
          break;
        case notifier:
          includeNotifier(xhtml);
          break;
        case password:
          includePassword(xhtml);
          break;
        case gauge:
          includeGauge(xhtml);
          break;
        case jquery:
          includeJQuery(xhtml);
          break;
        case tags:
          includeTags(xhtml);
          break;
        case pdc:
          includePdc(xhtml);
          break;
        case tkn:
          includeSecurityTokenizing(xhtml);
          break;
        case rating:
          includeRating(xhtml);
          break;
        case toggle:
          includeToggle(xhtml);
          break;
        case lightslideshow:
          includeLightweightSlideshow(xhtml);
          break;
        case lang:
          includeLang(xhtml);
          break;
        case ticker:
          includeTicker(xhtml);
          break;
        case subscription:
          includeDynamicallySubscription(xhtml, null);
          break;
        case dragAndDropUpload:
          includeDragAndDropUpload(xhtml, getLanguage());
          break;
        case chart:
          includeChart(xhtml, getLanguage());
          break;
        case listOfUsersAndGroups:
          includeListOfUsersAndGroups(xhtml, getLanguage());
          break;
      }
    } catch (IllegalArgumentException ex) {
      //ignore
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

  @SuppressWarnings("unchecked")
  protected <T> T getRequestAttribute(String name) {
    return (T) getJspContext().getAttribute(name, PageContext.REQUEST_SCOPE);
  }

  @SuppressWarnings("unchecked")
  protected <T> T getSessionAttribute(String name) {
    return (T) getJspContext().getAttribute(name, PageContext.SESSION_SCOPE);
  }
}
