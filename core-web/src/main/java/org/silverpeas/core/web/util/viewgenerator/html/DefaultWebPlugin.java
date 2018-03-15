/*
 * Copyright (C) 2000 - 2018 Silverpeas
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
import org.silverpeas.core.html.WebPluginConsumerRegistry;
import org.silverpeas.core.initialization.Initialization;

import javax.inject.Singleton;
import java.util.function.BiConsumer;

import static org.silverpeas.core.html.SupportedWebPlugins.*;
import static org.silverpeas.core.web.util.viewgenerator.html.JavascriptPluginInclusion.*;

/**
 * @author Yohann Chastagnier
 */
@Singleton
public class DefaultWebPlugin implements WebPlugin, Initialization {

  @Override
  public void init() {
    WebPluginConsumerRegistry.add(POLYFILLS, (xhtml, language) -> includePolyfills(xhtml));
    WebPluginConsumerRegistry.add(EMBEDPLAYER, (xhtml, language) -> includeEmbedPlayer(xhtml));
    WebPluginConsumerRegistry.add(MEDIAPLAYER, (xhtml, language) -> includeMediaPlayer(xhtml));
    WebPluginConsumerRegistry.add(QTIP, (xhtml, language) -> includeQTip(xhtml));
    WebPluginConsumerRegistry.add(DATEPICKER, JavascriptPluginInclusion::includeDatePicker);
    WebPluginConsumerRegistry.add(PAGINATION, (xhtml, language) -> includePagination(xhtml));
    WebPluginConsumerRegistry.add(BREADCRUMB, (xhtml, language) -> includeBreadCrumb(xhtml));
    WebPluginConsumerRegistry.add(USERZOOM, JavascriptPluginInclusion::includeUserZoom);
    WebPluginConsumerRegistry.add(RELATIONSHIP, JavascriptPluginInclusion::includeRelationship);
    WebPluginConsumerRegistry.add(MESSAGEME, (xhtml, language) -> includeMessageMe(xhtml));
    WebPluginConsumerRegistry.add(WYSIWYG, (xhtml, language) -> includeWysiwygEditor(xhtml));
    WebPluginConsumerRegistry.add(RESPONSIBLES, JavascriptPluginInclusion::includeResponsibles);
    WebPluginConsumerRegistry.add(POPUP, (xhtml, language) -> includePopup(xhtml));
    WebPluginConsumerRegistry.add(CALENDAR, JavascriptPluginInclusion::includeCalendar);
    WebPluginConsumerRegistry.add(IFRAMEAJAXTRANSPORT, (xhtml, language) -> includeIFrameAjaxTransport(xhtml));
    WebPluginConsumerRegistry.add(PREVIEW, (xhtml, language) -> includePreview(xhtml));
    WebPluginConsumerRegistry.add(FPVIEWER, (xhtml, language) -> includeFlexPaperViewer(xhtml));
    WebPluginConsumerRegistry.add(PDFVIEWER, JavascriptPluginInclusion::includePdfViewer);
    WebPluginConsumerRegistry.add(NOTIFIER, (xhtml, language) -> includeNotifier(xhtml));
    WebPluginConsumerRegistry.add(PASSWORD, (xhtml, language) -> includePassword(xhtml));
    WebPluginConsumerRegistry.add(GAUGE, (xhtml, language) -> includeGauge(xhtml));
    WebPluginConsumerRegistry.add(JQUERY, (xhtml, language) -> includeJQuery(xhtml));
    WebPluginConsumerRegistry.add(TAGS, (xhtml, language) -> includeTags(xhtml));
    WebPluginConsumerRegistry.add(PDC, JavascriptPluginInclusion::includePdc);
    WebPluginConsumerRegistry.add(TKN, (xhtml, language) -> includeSecurityTokenizing(xhtml));
    WebPluginConsumerRegistry.add(RATING, (xhtml, language) -> includeRating(xhtml));
    WebPluginConsumerRegistry.add(TOGGLE, (xhtml, language) -> includeToggle(xhtml));
    WebPluginConsumerRegistry.add(TABS, (xhtml, language) -> includeTabsWebComponent(xhtml));
    WebPluginConsumerRegistry.add(COLORPICKER, (xhtml, language) -> includeColorPickerWebComponent(xhtml));
    WebPluginConsumerRegistry.add(LIGHTSLIDESHOW, (xhtml, language) -> includeLightweightSlideshow(xhtml));
    WebPluginConsumerRegistry.add(LANG, (xhtml, language) -> includeLang(xhtml));
    WebPluginConsumerRegistry.add(TICKER, JavascriptPluginInclusion::includeTicker);
    WebPluginConsumerRegistry.add(SUBSCRIPTION, (xhtml, language) -> includeDynamicallySubscription(xhtml, null));
    WebPluginConsumerRegistry.add(DRAGANDDROPUPLOAD, JavascriptPluginInclusion::includeDragAndDropUpload);
    WebPluginConsumerRegistry.add(CHART, JavascriptPluginInclusion::includeChart);
    WebPluginConsumerRegistry.add(CHAT, (xhtml, language) -> includeChat(xhtml));
    WebPluginConsumerRegistry.add(SELECTIZE, (xhtml, language) -> includeSelectize(xhtml));
    WebPluginConsumerRegistry.add(LISTOFUSERSANDGROUPS, JavascriptPluginInclusion::includeListOfUsersAndGroups);
    WebPluginConsumerRegistry.add(USERNOTIFICATION, (xhtml, language) -> includeUserNotification(xhtml));
    WebPluginConsumerRegistry.add(ATTACHMENT, (xhtml, language) -> includeAttachment(xhtml));
    WebPluginConsumerRegistry.add(CRUD, (xhtml, language) -> includeCrud(xhtml));
    WebPluginConsumerRegistry.add(PANES, (xhtml, language) -> includePanes(xhtml));
    WebPluginConsumerRegistry.add(CONTRIBUTIONREMINDER, JavascriptPluginInclusion::includeContributionReminder);
  }

  @Override
  public ElementContainer getHtml(final SupportedWebPlugins plugin, final String language) {
    ElementContainer xhtml = new ElementContainer();
    BiConsumer<ElementContainer, String> inclusion = WebPluginConsumerRegistry.get(plugin);
    if (inclusion != null) {
      inclusion.accept(xhtml, language);
    }
    return xhtml;
  }
}
