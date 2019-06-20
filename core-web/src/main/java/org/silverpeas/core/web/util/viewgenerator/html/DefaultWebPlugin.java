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
import org.silverpeas.core.html.WebPluginConsumerRegistry;
import org.silverpeas.core.initialization.Initialization;

import javax.inject.Singleton;
import java.util.function.BiConsumer;

import static org.silverpeas.core.html.SupportedWebPlugins.*;
import static org.silverpeas.core.html.WebPluginConsumerRegistry.add;
import static org.silverpeas.core.web.util.viewgenerator.html.JavascriptPluginInclusion.*;

/**
 * @author Yohann Chastagnier
 */
@SuppressWarnings("Duplicates")
@Singleton
public class DefaultWebPlugin implements WebPlugin, Initialization {

  /**
   * Using here {@link WebPluginConsumerRegistry#add}
   */
  @Override
  public void init() {
    add(POLYFILLS, (xhtml, language) -> includePolyfills(xhtml));
    add(EMBEDPLAYER, (xhtml, language) -> includeEmbedPlayer(xhtml));
    add(MEDIAPLAYER, (xhtml, language) -> includeMediaPlayer(xhtml));
    add(QTIP, JavascriptPluginInclusion::includeQTip);
    add(DATEPICKER, JavascriptPluginInclusion::includeDatePicker);
    add(PAGINATION, (xhtml, language) -> includePagination(xhtml));
    add(BREADCRUMB, (xhtml, language) -> includeBreadCrumb(xhtml));
    add(USERZOOM, JavascriptPluginInclusion::includeUserZoom);
    add(RELATIONSHIP, JavascriptPluginInclusion::includeRelationship);
    add(WYSIWYG, JavascriptPluginInclusion::includeWysiwygEditor);
    add(RESPONSIBLES, JavascriptPluginInclusion::includeResponsibles);
    add(POPUP, (xhtml, language) -> includePopup(xhtml));
    add(CALENDAR, JavascriptPluginInclusion::includeCalendar);
    add(IFRAMEAJAXTRANSPORT, (xhtml, language) -> includeIFrameAjaxTransport(xhtml));
    add(PREVIEW, (xhtml, language) -> includePreview(xhtml));
    add(FPVIEWER, (xhtml, language) -> includeFlexPaperViewer(xhtml));
    add(PDFVIEWER, JavascriptPluginInclusion::includePdfViewer);
    add(NOTIFIER, (xhtml, language) -> includeNotifier(xhtml));
    add(PASSWORD, (xhtml, language) -> includePassword(xhtml));
    add(GAUGE, (xhtml, language) -> includeGauge(xhtml));
    add(JQUERY, (xhtml, language) -> includeJQuery(xhtml));
    add(TAGS, (xhtml, language) -> includeTags(xhtml));
    add(PDC, (xhtml, language) -> JavascriptPluginInclusion.includePdc(xhtml, language, false));
    add(PDCDYNAMICALLY, (xhtml, language) -> JavascriptPluginInclusion.includePdc(xhtml, language, true));
    add(TKN, (xhtml, language) -> includeSecurityTokenizing(xhtml));
    add(RATING, (xhtml, language) -> includeRating(xhtml));
    add(TOGGLE, (xhtml, language) -> includeToggle(xhtml));
    add(TABS, (xhtml, language) -> includeTabsWebComponent(xhtml));
    add(COLORPICKER, JavascriptPluginInclusion::includeColorPickerWebComponent);
    add(LIGHTSLIDESHOW, (xhtml, language) -> includeLightweightSlideshow(xhtml));
    add(LANG, (xhtml, language) -> includeLang(xhtml));
    add(TICKER, JavascriptPluginInclusion::includeTicker);
    add(SUBSCRIPTION, JavascriptPluginInclusion::includeDynamicallySubscription);
    add(DRAGANDDROPUPLOAD, JavascriptPluginInclusion::includeDragAndDropUpload);
    add(CHART, JavascriptPluginInclusion::includeChart);
    add(CHAT, (xhtml, language) -> includeChat(xhtml));
    add(SELECTIZE, (xhtml, language) -> includeSelectize(xhtml));
    add(LISTOFUSERSANDGROUPS, JavascriptPluginInclusion::includeListOfUsersAndGroups);
    add(USERNOTIFICATION, (xhtml, language) -> includeUserNotification(xhtml));
    add(ATTACHMENT, (xhtml, language) -> includeAttachment(xhtml));
    add(CRUD, (xhtml, language) -> includeCrud(xhtml));
    add(PANES, (xhtml, language) -> includePanes(xhtml));
    add(CONTRIBUTIONREMINDER, JavascriptPluginInclusion::includeContributionReminder);
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
