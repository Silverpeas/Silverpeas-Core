/*
 * Copyright (C) 2000 - 2022 Silverpeas
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
import org.silverpeas.core.annotation.Service;
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
@Service
@Singleton
public class DefaultWebPlugin implements WebPlugin, Initialization {

  /**
   * Using here {@link WebPluginConsumerRegistry#add}
   */
  @Override
  public void init() {
    add(MINIMALSILVERPEAS, (x, l) -> includeMinimalSilverpeas(x));
    add(POLYFILLS, (x, l) -> includePolyfills(x));
    add(EMBEDPLAYER, (x, l) -> includeEmbedPlayer(x));
    add(ADMINSERVICES, (x, l) -> includeAdminServices(x));
    add(MEDIAPLAYER, (x, l) -> includeMediaPlayer(x));
    add(QTIP, JavascriptPluginInclusion::includeQTip);
    add(DATEPICKER, JavascriptPluginInclusion::includeDatePicker);
    add(PAGINATION, (x, l) -> includePagination(x));
    add(BREADCRUMB, (x, l) -> includeBreadCrumb(x));
    add(USERZOOM, JavascriptPluginInclusion::includeUserZoom);
    add(RELATIONSHIP, JavascriptPluginInclusion::includeRelationship);
    add(WYSIWYG, JavascriptPluginInclusion::includeWysiwygEditor);
    add(RESPONSIBLES, JavascriptPluginInclusion::includeResponsibles);
    add(POPUP, (x, l) -> includePopup(x));
    add(CALENDAR, JavascriptPluginInclusion::includeCalendar);
    add(IFRAMEAJAXTRANSPORT, (x, l) -> includeIFrameAjaxTransport(x));
    add(PREVIEW, (x, l) -> includePreview(x));
    add(FPVIEWER, (x, l) -> includeFlexPaperViewer(x));
    add(PDFVIEWER, (x, l) -> JavascriptPluginInclusion.includePdfViewer(x));
    add(NOTIFIER, (x, l) -> includeNotifier(x));
    add(PASSWORD, (x, l) -> includePassword(x));
    add(GAUGE, (x, l) -> includeGauge(x));
    add(JQUERY, (x, l) -> includeJQuery(x));
    add(TAGS, (x, l) -> includeTags(x));
    add(PDC, (x, l) -> JavascriptPluginInclusion.includePdc(x, l, false));
    add(PDCDYNAMICALLY, (x, l) -> JavascriptPluginInclusion.includePdc(x, l, true));
    add(TKN, (x, l) -> includeSecurityTokenizing(x));
    add(RATING, (x, l) -> includeRating(x));
    add(TOGGLE, (x, l) -> includeToggle(x));
    add(TABS, (x, l) -> includeTabsWebComponent(x));
    add(COLORPICKER, JavascriptPluginInclusion::includeColorPickerWebComponent);
    add(LIGHTSLIDESHOW, (x, l) -> includeLightweightSlideshow(x));
    add(LANG, (x, l) -> includeLang(x));
    add(TICKER, JavascriptPluginInclusion::includeTicker);
    add(SUBSCRIPTION, JavascriptPluginInclusion::includeDynamicallySubscription);
    add(CONTRIBUTIONMODICTX, (x, l) -> JavascriptPluginInclusion.includeContributionModificationContext(x));
    add(DRAGANDDROPUPLOAD, JavascriptPluginInclusion::includeDragAndDropUpload);
    add(IMAGESELECTOR, JavascriptPluginInclusion::includeImageSelector);
    add(BASKETSELECTION, JavascriptPluginInclusion::includeBasketSelection);
    add(CHART, JavascriptPluginInclusion::includeChart);
    add(CHAT, (x, l) -> includeChat(x));
    add(SELECTIZE, (x, l) -> includeSelectize(x));
    add(LISTOFUSERSANDGROUPS, JavascriptPluginInclusion::includeListOfUsersAndGroups);
    add(USERNOTIFICATION, (x, l) -> includeUserNotification(x));
    add(ATTACHMENT, (x, l) -> includeAttachment(x));
    add(CRUD, (x, l) -> includeCrud(x));
    add(PANES, (x, l) -> includePanes(x));
    add(CONTRIBUTIONREMINDER, JavascriptPluginInclusion::includeContributionReminder);
    add(VIRTUALKEYBOARD, JavascriptPluginInclusion::includeVirtualKeyboard);
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
