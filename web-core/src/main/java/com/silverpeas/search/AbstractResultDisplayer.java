/**
 * Copyright (C) 2000 - 2011 Silverpeas
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
 * "http://repository.silverpeas.com/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.silverpeas.search;

import com.silverpeas.SilverpeasServiceProvider;
import com.silverpeas.personalization.UserPreferences;
import com.silverpeas.util.EncodeHelper;
import com.silverpeas.util.StringUtil;
import com.silverpeas.util.template.SilverpeasTemplate;
import com.stratelia.silverpeas.pdcPeas.model.GlobalSilverResult;
import com.stratelia.silverpeas.util.ResourcesWrapper;
import com.stratelia.webactiv.util.DateUtil;

import javax.servlet.jsp.JspTagException;

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
    ResourcesWrapper settings = searchResult.getSettings();

    componentTemplate.setAttribute("gsr", silverResult);
    componentTemplate.setAttribute("name", EncodeHelper.javaStringToHtmlString(silverResult
        .getName()));
    String sDescription = silverResult.getDescription();
    if (sDescription != null && sDescription.length() > 400) {
      sDescription = sDescription.substring(0, 400) + "...";
    }
    if (StringUtil.isDefined(sDescription)) {
      componentTemplate.setAttribute("description", EncodeHelper
          .javaStringToHtmlParagraphe(sDescription));
    }
    componentTemplate.setAttribute("url", silverResult.getTitleLink());
    String sDownload = silverResult.getDownloadLink();
    if (StringUtil.isDefined(sDownload)) {
      componentTemplate.setAttribute("downloadUrl", sDownload);
    }
    String location = silverResult.getLocation();
    if (StringUtil.isDefined(location)) {
      componentTemplate.setAttribute("location", EncodeHelper.javaStringToHtmlString(location));
    }
    String sCreatorName = silverResult.getCreatorName();
    if (StringUtil.isDefined(sCreatorName)) {
      componentTemplate.setAttribute("creatorName", EncodeHelper
          .javaStringToHtmlString(sCreatorName));
    }
    componentTemplate.setAttribute("spaceId", silverResult.getSpaceId());
    componentTemplate.setAttribute("instanceId", silverResult.getInstanceId());

    String sCreationDate = null;
    try {
      if (searchResult.getSortValue() == 4) {
        sCreationDate =
            DateUtil.getOutputDate(silverResult.getCreationDate(), getUserPreferences(
                searchResult.getUserId()).getLanguage());
      } else {
        sCreationDate =
            DateUtil.getOutputDate(silverResult.getDate(), getUserPreferences(
                searchResult.getUserId()).getLanguage());
      }
    } catch (Exception e) {
      sCreationDate = null;
    }
    if (StringUtil.isDefined(sCreationDate)) {
      componentTemplate.setAttribute("creationDate", sCreationDate);
    }

    String serverName = "";
    if (settings.getSetting("external.search.enable", false) &&
        silverResult.getIndexEntry() != null) {
      serverName =
          "external_server_" + (StringUtil.isDefined(silverResult.getIndexEntry().getServerName()) ?
                  silverResult.getIndexEntry().getServerName() : "unknown");
    }
    if (StringUtil.isDefined(serverName)) {
      componentTemplate.setAttribute("serverName", serverName);
    }
    componentTemplate.setAttribute("pertinenceVisible", settings.getSetting("PertinenceVisible",
        false));

    if (settings.getSetting("PertinenceVisible", false)) {
      componentTemplate.setAttribute("pertinence", ResultSearchRendererUtil
          .displayPertinence(silverResult
              .getRawScore()));
    }

    componentTemplate.setAttribute("activeSelection", searchResult.getActiveSelection());
    componentTemplate.setAttribute("exportEnabled", searchResult.getExportEnabled());
    componentTemplate.setAttribute("type", silverResult.getType());

    // if (silverResult.getType() != null &&
    // (silverResult.getType().startsWith("Attachment") ||
    // silverResult.getType().startsWith("Versioning") || silverResult
    // .getType().equals("LinkedFile"))) {
    // String fileType = sName.substring(sName.lastIndexOf(".") + 1, sName.length());
    // String fileIcon = FileRepositoryManager.getFileIcon(fileType);
    // sName = "<img src=\"" + fileIcon + "\" class=\"fileIcon\"/>" + sName;
    // // no preview, display this is an attachment
    // if (silverResult.getType().startsWith("Attachment") ||
    // silverResult.getType().equals("LinkedFile")) {
    // sDescription = null;
    // }
    // }
    //
    // result.append("<td class=\"content\">");
    //
    // result.append("<table cellspacing=\"0\" cellpadding=\"0\"><tr>");
    //
    // if (silverResult.getThumbnailURL() != null && silverResult.getThumbnailURL().length() > 0) {
    // if ("UserFull".equals(silverResult.getType())) {
    // result.append("<td><img class=\"avatar\" src=\"" + URLManager.getApplicationURL() +
    // silverResult.getThumbnailURL() +
    // "\" /></td>");
    // } else {
    // result.append("<td><img src=\"" + silverResult.getThumbnailURL() +
    // "\" border=\"0\" width=\""
    // +
    // silverResult.getThumbnailWidth() + "\" height=\"" + silverResult.getThumbnailHeight() +
    // "\"/></td>");
    // }
    // result.append("<td>&nbsp;</td>");
    // }

    return componentTemplate;
  }

  /**
   * @return a UserPreferences object from Personalization service.
   * @throws JspTagException
   */
  protected UserPreferences getUserPreferences(String userId) {
    return SilverpeasServiceProvider.getPersonalizationService().getUserSettings(userId);
  }

}
