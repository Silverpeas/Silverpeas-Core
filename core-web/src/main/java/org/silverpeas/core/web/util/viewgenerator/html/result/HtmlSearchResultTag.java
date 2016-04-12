/*
 * Copyright (C) 2000 - 2016 Silverpeas
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
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.silverpeas.core.web.util.viewgenerator.html.result;


import org.silverpeas.core.personalization.UserPreferences;
import org.silverpeas.core.personalization.service.PersonalizationServiceProvider;
import org.silverpeas.core.util.URLUtil;
import org.apache.commons.io.FilenameUtils;
import org.silverpeas.core.admin.component.model.ComponentInstLight;
import org.silverpeas.core.admin.service.OrganizationControllerProvider;
import org.silverpeas.core.pdc.pdc.model.GlobalSilverResult;
import org.silverpeas.core.util.ResourceLocator;
import org.silverpeas.core.util.StringUtil;
import org.silverpeas.core.web.search.ResultDisplayer;
import org.silverpeas.core.web.search.ResultDisplayerProvider;
import org.silverpeas.core.web.search.ResultSearchRendererUtil;
import org.silverpeas.core.web.search.SearchResultContentVO;
import org.silverpeas.core.web.util.viewgenerator.html.ImageTag;
import org.silverpeas.core.util.EncodeHelper;
import org.silverpeas.core.util.file.FileRepositoryManager;
import org.silverpeas.core.util.MultiSilverpeasBundle;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspTagException;
import javax.servlet.jsp.tagext.TagSupport;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Tag to display result search element (GlobalSilverResult) object. Add extra information from
 * publication if needed
 */
public class HtmlSearchResultTag extends TagSupport {
  private static final long serialVersionUID = -7747695403360864218L;

  private static final String PDC_BUNDLE_PREFIX_KEY = "result.template.";
  protected static final String RESULT_TEMPLATE_ALL = "ALL";
  protected static final String RESULT_TEMPLATE_NONE = "NONE";

  /*
   * List of Tag attributes
   */
  private String userId = null;
  private GlobalSilverResult gsr = null;
  private Integer sortValue = null;
  private boolean activeSelection = false;
  private boolean exportEnabled = false;
  private MultiSilverpeasBundle resources = null;
  private Map<String, Boolean> componentSettings = new HashMap<String, Boolean>();

  @Override
  public int doStartTag() throws JspException {
    try {
      pageContext.getOut().print(getHtmlResult());
    } catch (IOException ex) {
      throw new JspException("Silverpeas Java to html paragraph Converter Tag", ex);
    }
    return EVAL_PAGE;
  }

  /**
   * Get user identifier.
   * @return the user identifier.
   */
  public String getUserId() {
    return userId;
  }

  /**
   * Set user idenfier.
   * @param userId the user identifier.
   */
  public void setUserId(String userId) {
    this.userId = userId;
  }

  /**
   * @return the global silver result
   */
  public GlobalSilverResult getGsr() {
    return gsr;
  }

  /**
   * @param gsr the GlobalSilverResult to set
   */
  public void setGsr(GlobalSilverResult gsr) {
    this.gsr = gsr;
  }

  /**
   * @return the sortValue
   */
  public Integer getSortValue() {
    return sortValue;
  }

  /**
   * @param sortValue the sortValue to set
   */
  public void setSortValue(Integer sortValue) {
    this.sortValue = sortValue;
  }

  /**
   * @return the activeSelection
   */
  public boolean getActiveSelection() {
    return activeSelection;
  }

  /**
   * @param activeSelection the activeSelection to set
   */
  public void setActiveSelection(boolean activeSelection) {
    this.activeSelection = activeSelection;
  }

  /**
   * @return the exportEnabled
   */
  public boolean getExportEnabled() {
    return exportEnabled;
  }

  /**
   * @param exportEnabled the exportEnabled to set
   */
  public void setExportEnabled(boolean exportEnabled) {
    this.exportEnabled = exportEnabled;
  }

  /**
   * @return an HTML result search of a searched element
   * @throws JspTagException
   */
  private String getHtmlResult() throws JspTagException {
    // Get settings from MultiSilverpeasBundle
    MultiSilverpeasBundle settings = getResources();
    // Retrieve result instance identifier
    String instanceId = gsr.getInstanceId();
    String componentName = instanceId;

    // Create a new added information
    String addedInformation = null;

    if (StringUtil.isDefined(instanceId) &&
        !(instanceId.startsWith("user") || instanceId.startsWith("pdc"))) {

      // Check if this component has a specific template result
      ComponentInstLight component = OrganizationControllerProvider.getOrganisationController()
          .getComponentInstLight(instanceId);
      if (component != null) {
        componentName = component.getName();

        boolean processResultTemplating = isResultTemplating(instanceId, componentName);
        if (processResultTemplating) {
          // Retrieve the component result displayer class from a CDI provider
          ResultDisplayer resultDisplayer =
              ResultDisplayerProvider.getResultDisplayer(componentName);
          if (resultDisplayer != null) {
            addedInformation = resultDisplayer.getResultContent(
                new SearchResultContentVO(this.userId, this.gsr, this.sortValue,
                    this.activeSelection, this.exportEnabled, settings));
          }
        }
      }
    }
    return generateHTMLSearchResult(settings, componentName, addedInformation);
  }

  /**
   * Check if a component instance must process a specific result template. Then this method adds
   * the result of this check inside a cache.
   * @param instanceId : the component instance identifier
   * @param componentName : the component name
   * @return true if this instance need to generate a specific result template, false else if
   * @throws JspTagException
   */
  private boolean isResultTemplating(String instanceId, String componentName) {
    boolean doResultTemplating = false;
    Boolean cacheResult = componentSettings.get(componentName);
    if (cacheResult != null) {
      return cacheResult;
    } else {
      String compConfig = PDC_BUNDLE_PREFIX_KEY + componentName;
      String listComponent = getResources().getSetting(compConfig, "");
      if (StringUtil.isDefined(listComponent)) {
        for (String curInstanceId : listComponent.split(",")) {
          if (curInstanceId.equals(instanceId) || RESULT_TEMPLATE_ALL.equals(curInstanceId)) {
            componentSettings.put(instanceId, Boolean.TRUE);
            return true;
          }
        }
      }
      componentSettings.put(instanceId, Boolean.FALSE);
      return doResultTemplating;
    }
  }

  /**
   * @param resources
   * @param componentName
   * @param extraInformation
   * @return the default HTML result search of a searched element
   * @throws JspTagException
   */
  private String generateHTMLSearchResult(MultiSilverpeasBundle resources, String componentName,
      String extraInformation) {
    // initialize html result
    StringBuilder result = new StringBuilder();
    String downloadSrc = "<img src=\"" + resources.getIcon("pdcPeas.download") +
        "\" class=\"fileDownload\" alt=\"" + resources.getString("pdcPeas.DownloadInfo") +
        "\" title=\"" + resources.getString("pdcPeas.DownloadInfo") + "\"/>";
    String language = getResources().getLanguage();
    String sName = EncodeHelper.javaStringToHtmlString(gsr.getName(language));
    String sDescription = StringUtil.abbreviate(gsr.getDescription(language), 400);
    String sURL = gsr.getTitleLink();
    String sDownloadURL = gsr.getDownloadLink();
    String sLocation = gsr.getLocation();
    String sCreatorName = gsr.getCreatorName();
    String sCreationDate;
    try {
      if (sortValue == 4) {
        sCreationDate = resources.getOutputDate(gsr.getCreationDate());
      } else {
        sCreationDate = resources.getOutputDate(gsr.getDate());
      }
    } catch (Exception e) {
      sCreationDate = null;
    }

    String serverName = "";
    if (resources.getSetting("external.search.enable", false) && gsr.getIndexEntry() != null) {
      serverName = "external_server_" + (StringUtil.isDefined(gsr.getIndexEntry().getServerName()) ?
          gsr.getIndexEntry().getServerName() : "unknown");
    }

    result.append("<li class=\"lineResult ").append(gsr.getSpaceId()).append(" ");
    result.append(componentName).append(" ");
    result.append(gsr.getInstanceId()).append(" ");
    result.append(serverName).append("\">");

    if (activeSelection || exportEnabled) {
      if (gsr.isExportable() && gsr.isUserAllowedToDownloadFile()) {
        String checked = "";
        if (gsr.isSelected()) {
          checked = "checked=\"checked\"";
        }
        result.append("<input class=\"selection\" type=\"checkbox\" ").append(checked)
            .append(" name=\"resultObjects\" value=\"").append(gsr.getId()).append("-")
            .append(gsr.getInstanceId()).append("\"/>");
      }
    }

    if (resources.getSetting("PertinenceVisible", false)) {
      result.append("<div class=\"pertinence\">")
          .append(ResultSearchRendererUtil.displayPertinence(gsr.getRawScore())).append("</div>");
    }

    result.append("<div class=\"content\">");

    if (StringUtil.isDefined(gsr.getThumbnailURL())) {
      result.append("<div class=\"thumb\">");
      if ("UserFull".equals(gsr.getType())) {
        result.append("<img class=\"avatar\" src=\"").append(URLUtil.getApplicationURL())
            .append(gsr.getThumbnailURL()).append("\" border=\"0\" />");
      } else {
        ImageTag image = new ImageTag();
        image.setSrc(gsr.getThumbnailURL());
        image.setSize("60x");
        result.append(image.toString());
      }
      result.append("</div>");
    }

    if (gsr.getType() != null &&
        (gsr.getType().startsWith("Attachment") || gsr.getType().startsWith("Versioning") ||
            gsr.getType().equals("LinkedFile"))) {
      String fileType = FilenameUtils.getExtension(gsr.getAttachmentFilename());
      String fileIcon = FileRepositoryManager.getFileIcon(fileType);
      if (!StringUtil.isDefined(sName)) {
        sName = gsr.getAttachmentFilename();
      }
      sName = "<img src=\"" + fileIcon + "\" class=\"fileIcon\"/>" + sName;
    }

    result.append("<div class=\"locationTitle\">");
    String curResultId = "readSpanId_" + gsr.getResultId();
    if (activeSelection) {
      result.append("<span id=\"").append(curResultId).append("\" class=\"textePetitBold")
          .append((gsr.isUserAllowedToDownloadFile()) ? "" : " forbidden-download").append("\">")
          .append(sName).append("</span>");
    } else {
      String cssClass = "";
      String cssClassDisableVisited = "";
      if (gsr.isHasRead()) {
        cssClass = "markedAsRead";
        cssClassDisableVisited = "markedAsReadDisableVisited";
      }
      if (!gsr.isUserAllowedToDownloadFile()) {
        cssClass += " forbidden-download";
      }
      if (gsr.isUserAllowedToDownloadFile()) {
        result.append("<a href=\"").append(sURL).append("\" class=\"")
            .append(cssClassDisableVisited).append("\">");
      }
      result.append("<span id=\"").append(curResultId).append("\" class=\"").append(cssClass)
          .append("\">").append(sName).append("</span>");
      if (gsr.isUserAllowedToDownloadFile()) {
        result.append("</a>");
      }
    }
    if (gsr.getIndexEntry() != null && gsr.getIndexEntry().isAlias()) {
      result.append(" (").append(resources.getString("GML.alias")).append(")");
    }

    if (StringUtil.isDefined(sDownloadURL) && gsr.isUserAllowedToDownloadFile()) {
      // affiche le lien pour le téléchargement
      result.append("<a href=\"").append(sDownloadURL)
          .append("\" class=\"fileDownload\" target=\"_blank\">").append(downloadSrc)
          .append("</a>");
    }

    if (gsr.isPreviewable()) {
      result.append(" <img onclick=\"javascript:previewFile(this, '").append(gsr.getAttachmentId())
          .append("',").append(gsr.isVersioned()).append(",'").append(gsr.getInstanceId())
          .append("');\" class=\"preview-file\" src=\"")
          .append(resources.getIcon("pdcPeas.file.preview")).append("\" alt=\"")
          .append(resources.getString("GML.preview")).append("\" title=\"")
          .append(resources.getString("GML.preview")).append("\"/>");
    }
    if (gsr.isViewable()) {
      result.append(" <img onclick=\"javascript:viewFile(this, '").append(gsr.getAttachmentId())
          .append("',").append(gsr.isVersioned()).append(",'").append(gsr.getInstanceId())
          .append("');\" class=\"view-file\" src=\"").append(resources.getIcon("pdcPeas.file.view"))
          .append("\" alt=\"").append(resources.getString("GML.view")).append("\" title=\"")
          .append(resources.getString("GML.view")).append("\"/>");
    }
    if (!gsr.isDownloadAllowedForReaders()) {
      String forbiddenDownloadHelp = "";
      forbiddenDownloadHelp =
          gsr.isUserAllowedToDownloadFile() ? resources.getString("GML.download.forbidden.readers") :
              resources.getString("GML.download.forbidden");
      result.append(" <img class=\"forbidden-download-file\" src=\"")
          .append(resources.getIcon("pdcPeas.file.forbidden-download")).append("\" alt=\"")
          .append(forbiddenDownloadHelp).append("\" title=\"").append(forbiddenDownloadHelp)
          .append("\"/>");
    }

    result.append("</div>");

    if (StringUtil.isDefined(sDescription)) {
      result.append("<div class=\"description\">")
          .append(EncodeHelper.javaStringToHtmlParagraphe(sDescription)).append("</div>");
    }

    if (StringUtil.isDefined(extraInformation)) {
      result.append("<div class=\"extra\">");
      result.append(extraInformation);
      result.append("</div>");
    }

    if (sortValue == 7 && gsr.getHits() >= 0) {
      result.append("<div class=\"popularity\">").append(
          resources.getStringWithParams("pdcPeas.popularity", Integer.toString(gsr.getHits())))
          .append(" | </div>");
    }

    if (StringUtil.isDefined(sCreationDate)) {
      result.append("<div class=\"creationDate\"> ").append(sCreationDate).append(" | </div>");
    }

    if (StringUtil.isDefined(sCreatorName)) {
      result.append("<div class=\"creatorName\">")
          .append(EncodeHelper.javaStringToHtmlString(sCreatorName)).append(" | </div>");
    }

    if (StringUtil.isDefined(serverName)) {
      result.append("<div class=\"serveurName\"> ").append(serverName).append(" | </div>");
    }

    if (StringUtil.isDefined(sLocation)) {
      result.append("<div class=\"location\">")
          .append(EncodeHelper.javaStringToHtmlString(sLocation)).append("</div>");
    }

    result.append("</div>");

    result.append("</li>");

    return result.toString();
  }

  /**
   * @return a UserPreferences object from Personalization service.
   * @throws JspTagException
   */
  private UserPreferences getUserPreferences() {
    return PersonalizationServiceProvider.getPersonalizationService().getUserSettings(getUserId());
  }

  /**
   * @return a MultiSilverpeasBundle which encapsulate pdcPeas settings and bundles
   */
  private MultiSilverpeasBundle getResources() {
    if (resources == null) {
      String language = getUserPreferences().getLanguage();
      resources = new MultiSilverpeasBundle(
          ResourceLocator.getLocalizationBundle("org.silverpeas.pdcPeas.multilang.pdcBundle",
              language),
          ResourceLocator.getSettingBundle("org.silverpeas.pdcPeas.settings.pdcPeasIcons"),
          ResourceLocator.getSettingBundle("org.silverpeas.pdcPeas.settings.pdcPeasSettings"), language);
    }
    return resources;
  }

  public void setResources(MultiSilverpeasBundle resources) {
    this.resources = resources;
  }

}
