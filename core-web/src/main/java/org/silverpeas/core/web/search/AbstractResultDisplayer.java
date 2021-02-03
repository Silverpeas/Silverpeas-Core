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
package org.silverpeas.core.web.search;

import org.silverpeas.core.date.TemporalFormatter;
import org.silverpeas.core.pdc.pdc.model.GlobalSilverResult;
import org.silverpeas.core.personalization.UserPreferences;
import org.silverpeas.core.personalization.service.PersonalizationServiceProvider;
import org.silverpeas.core.template.SilverpeasTemplate;
import org.silverpeas.core.util.MultiSilverpeasBundle;
import org.silverpeas.core.util.StringUtil;
import org.silverpeas.core.util.WebEncodeHelper;

/**
 * This class shares common method to implement ResultDisplayer interface.
 */
public abstract class AbstractResultDisplayer implements ResultDisplayer {

  /**
   * @param searchResult the SearchResultContentVO which encapsulates result data
   * @param componentTemplate the silverpeas template where we set attributes
   * @return a SilverpeasTemplate with additional common attributes.
   */
  protected SilverpeasTemplate setCommonAttributes(SearchResultContentVO searchResult,
      SilverpeasTemplate componentTemplate) {

    GlobalSilverResult silverResult = searchResult.getGsr();
    MultiSilverpeasBundle settings = searchResult.getSettings();

    componentTemplate.setAttribute("gsr", silverResult);
    componentTemplate.setAttribute("name", WebEncodeHelper.javaStringToHtmlString(silverResult
        .getName()));
    String sDescription = silverResult.getDescription();
    if (sDescription != null && sDescription.length() > 400) {
      sDescription = sDescription.substring(0, 400) + "...";
    }
    if (StringUtil.isDefined(sDescription)) {
      componentTemplate.setAttribute("description", WebEncodeHelper
          .javaStringToHtmlParagraphe(sDescription));
    }
    componentTemplate.setAttribute("url", silverResult.getTitleLink());
    String sDownload = silverResult.getDownloadLink();
    if (StringUtil.isDefined(sDownload)) {
      componentTemplate.setAttribute("downloadUrl", sDownload);
    }
    String location = silverResult.getLocation();
    if (StringUtil.isDefined(location)) {
      componentTemplate.setAttribute("location", WebEncodeHelper.javaStringToHtmlString(location));
    }
    String sCreatorName = silverResult.getCreatorName();
    if (StringUtil.isDefined(sCreatorName)) {
      componentTemplate.setAttribute("creatorName", WebEncodeHelper
          .javaStringToHtmlString(sCreatorName));
    }

    componentTemplate.setAttribute("instanceId", silverResult.getInstanceId());

    String sCreationDate;
    try {
      if (searchResult.getSortValue() == 4) {
        sCreationDate = TemporalFormatter.toLocalized(silverResult.getCreationDate(),
            getUserPreferences(searchResult.getUserId()).getLanguage());
      } else {
        sCreationDate = TemporalFormatter.toLocalized(silverResult.getLastUpdateDate(),
            getUserPreferences(searchResult.getUserId()).getLanguage());
      }
    } catch (Exception e) {
      sCreationDate = null;
    }
    if (StringUtil.isDefined(sCreationDate)) {
      componentTemplate.setAttribute("creationDate", sCreationDate);
    }

    String serverName = silverResult.getServerName();
    if (StringUtil.isDefined(serverName)) {
      componentTemplate.setAttribute("serverName", serverName);
    }
    componentTemplate.setAttribute("pertinenceVisible", settings.getSetting("PertinenceVisible",
        false));

    if (settings.getSetting("PertinenceVisible", false)) {
      componentTemplate.setAttribute("pertinence", ResultSearchRendererUtil
          .displayPertinence(silverResult.getScore()));
    }

    componentTemplate.setAttribute("exportEnabled", searchResult.getExportEnabled());
    componentTemplate.setAttribute("type", silverResult.getType());

    return componentTemplate;
  }

  /**
   * @return a UserPreferences object from Personalization service.
   */
  protected UserPreferences getUserPreferences(String userId) {
    return PersonalizationServiceProvider.getPersonalizationService().getUserSettings(userId);
  }

}
