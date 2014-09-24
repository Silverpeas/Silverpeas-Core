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
package org.silverpeas.util.viewGenerator.html;

import org.silverpeas.util.i18n.I18NHelper;
import com.stratelia.silverpeas.peasCore.MainSessionController;
import java.io.IOException;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.SimpleTagSupport;
import org.apache.ecs.ElementContainer;

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
          JavascriptPluginInclusion.includeEmbedPlayer(xhtml);
          break;
        case audioPlayer:
          JavascriptPluginInclusion.includeAudioPlayer(xhtml);
          break;
        case videoPlayer:
          JavascriptPluginInclusion.includeVideoPlayer(xhtml);
          break;
        case qtip:
          JavascriptPluginInclusion.includeQTip(xhtml);
          break;
        case datepicker:
          JavascriptPluginInclusion.includeDatePicker(xhtml, getLanguage());
          break;
        case pagination:
          JavascriptPluginInclusion.includePagination(xhtml);
          break;
        case breadcrumb:
          JavascriptPluginInclusion.includeBreadCrumb(xhtml);
          break;
        case userZoom:
          JavascriptPluginInclusion.includeUserZoom(xhtml);
          break;
        case invitme:
          JavascriptPluginInclusion.includeInvitMe(xhtml);
          break;
        case messageme:
          JavascriptPluginInclusion.includeMessageMe(xhtml);
          break;
        case wysiwyg:
          JavascriptPluginInclusion.includeWysiwygEditor(xhtml);
          break;
        case responsibles:
          JavascriptPluginInclusion.includeResponsibles(xhtml, getLanguage());
          break;
        case popup:
          JavascriptPluginInclusion.includePopup(xhtml);
          break;
        case calendar:
          JavascriptPluginInclusion.includeCalendar(xhtml);
          break;
        case iframeajaxtransport:
          JavascriptPluginInclusion.includeIFrameAjaxTransport(xhtml);
          break;
        case preview:
          JavascriptPluginInclusion.includePreview(xhtml);
          break;
        case notifier:
          JavascriptPluginInclusion.includeNotifier(xhtml);
          break;
        case password:
          JavascriptPluginInclusion.includePassword(xhtml);
          break;
        case gauge:
          JavascriptPluginInclusion.includeGauge(xhtml);
          break;
        case jquery:
          JavascriptPluginInclusion.includeJQuery(xhtml);
          break;
        case tags:
          JavascriptPluginInclusion.includeTags(xhtml);
          break;
        case pdc:
          JavascriptPluginInclusion.includePdc(xhtml);
          break;
        case tkn:
          JavascriptPluginInclusion.includeSecurityTokenizing(xhtml);
          break;
        case rating:
          JavascriptPluginInclusion.includeRating(xhtml);
          break;
        case toggle:
          JavascriptPluginInclusion.includeToggle(xhtml);
          break;
        case lightslideshow:
          JavascriptPluginInclusion.includeLightweightSlideshow(xhtml);
          break;
        case lang:
          JavascriptPluginInclusion.includeLang(xhtml);
          break;
        case ticker:
          JavascriptPluginInclusion.includeTicker(xhtml);
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
