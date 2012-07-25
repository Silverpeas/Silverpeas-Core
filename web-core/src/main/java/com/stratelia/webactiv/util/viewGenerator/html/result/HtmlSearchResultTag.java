/**
 * Copyright (C) 2000 - 2012 Silverpeas
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
 * "http://www.silverpeas.org/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.stratelia.webactiv.util.viewGenerator.html.result;

import com.silverpeas.SilverpeasServiceProvider;
import com.silverpeas.personalization.UserPreferences;
import com.silverpeas.search.ResultDisplayer;
import com.silverpeas.search.ResultDisplayerFactory;
import com.silverpeas.search.ResultSearchRendererUtil;
import com.silverpeas.search.SearchResultContentVO;
import com.silverpeas.util.EncodeHelper;
import com.silverpeas.util.StringUtil;
import com.stratelia.silverpeas.pdcPeas.model.GlobalSilverResult;
import com.stratelia.silverpeas.peasCore.URLManager;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.silverpeas.util.ResourcesWrapper;
import com.stratelia.webactiv.beans.admin.ComponentInstLight;
import com.stratelia.webactiv.beans.admin.OrganizationController;
import com.stratelia.webactiv.util.FileRepositoryManager;
import com.stratelia.webactiv.util.ResourceLocator;

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
  private Boolean activeSelection = false;
  private Boolean exportEnabled = false;

  /*
   * object helper
   */
  private OrganizationController orga = new OrganizationController();
  private ResourcesWrapper settings = null;
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
  public Boolean getActiveSelection() {
    return activeSelection;
  }

  /**
   * @param activeSelection the activeSelection to set
   */
  public void setActiveSelection(Boolean activeSelection) {
    this.activeSelection = activeSelection;
  }

  /**
   * @return the exportEnabled
   */
  public Boolean getExportEnabled() {
    return exportEnabled;
  }

  /**
   * @param exportEnabled the exportEnabled to set
   */
  public void setExportEnabled(Boolean exportEnabled) {
    this.exportEnabled = exportEnabled;
  }

  /**
   * @return an HTML result search of a searched element
   * @throws JspTagException
   */
  private String getHtmlResult() throws JspTagException {
    // Get settings from ResourcesWrapper
    ResourcesWrapper settings = getSettings();
    // Retrieve result instance identifier
    String instanceId = gsr.getInstanceId();
    String componentName = instanceId;

    // Create a new added information
    String addedInformation = null;

    if (StringUtil.isDefined(instanceId) && !(instanceId.startsWith("user")
        || instanceId.startsWith("pdc"))) {

      // Check if this component has a specific template result
      ComponentInstLight component = orga.getComponentInstLight(instanceId);
      if (component != null) {
        componentName = component.getName();

        boolean processResultTemplating = isResultTemplating(instanceId, componentName);
        if (processResultTemplating) {
          // Retrieve the component result displayer class from a factory
          ResultDisplayer resultDisplayer =
              ResultDisplayerFactory.getResultDisplayerFactory().getResultDisplayer(componentName);
          SilverTrace.debug("viewgenerator", HtmlSearchResultTag.class.getName(),
              "load specific for current result: instanceid=" + instanceId + ", contentid=" +
              gsr.getId());
          if (resultDisplayer != null) {
            addedInformation = resultDisplayer.getResultContent(new SearchResultContentVO(
                this.userId, this.gsr, this.sortValue, this.activeSelection, this.exportEnabled,
                settings));
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
  private boolean isResultTemplating(String instanceId, String componentName)
      throws JspTagException {
    boolean doResultTemplating = false;
    Boolean cacheResult = componentSettings.get(componentName);
    if (cacheResult != null) {
      return cacheResult;
    } else {
      String compConfig = PDC_BUNDLE_PREFIX_KEY + componentName;
      String listComponent = getSettings().getSetting(compConfig, "");
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
   * @param settings
   * @param componentName
   * @param extraInformation
   * @return the default HTML result search of a searched element
   * @throws JspTagException
   */
  private String generateHTMLSearchResult(ResourcesWrapper settings, String componentName,
      String extraInformation) throws JspTagException {
    // initialize html result
    StringBuilder result = new StringBuilder();

    String downloadSrc = "<img src=\"" + settings.getIcon("pdcPeas.download") +
        "\" class=\"fileDownload\" alt=\"" + settings.getString("pdcPeas.DownloadInfo") +
        "\"/>";

    String sName = EncodeHelper.javaStringToHtmlString(gsr.getName());
    String sDescription = gsr.getDescription();
    if (sDescription != null && sDescription.length() > 400) {
      sDescription = sDescription.substring(0, 400) + "...";
    }
    String sURL = gsr.getTitleLink();
    String sDownloadURL = gsr.getDownloadLink();
    String sLocation = gsr.getLocation();
    String sCreatorName = gsr.getCreatorName();
    String sCreationDate = null;
    try {
      if (sortValue == 4) {
        sCreationDate = settings.getOutputDate(gsr.getCreationDate());
      } else {
        sCreationDate = settings.getOutputDate(gsr.getDate());
      }
    } catch (Exception e) {
      sCreationDate = null;
    }

    String serverName = "";
    if (settings.getSetting("external.search.enable", false) && gsr.getIndexEntry() != null) {
      serverName = "external_server_" +
          (StringUtil.isDefined(gsr.getIndexEntry().getServerName()) ? gsr.getIndexEntry()
          .getServerName() : "unknown");
    }

    result.append("<tr class=\"lineResult ").append(gsr.getSpaceId()).append(" ");
    result.append(componentName).append(" ");
    result.append(gsr.getInstanceId()).append(" ");
    result.append(serverName).append("\">");

    if (settings.getSetting("PertinenceVisible", false)) {
      result.append("<td class=\"pertinence\">").append(
          ResultSearchRendererUtil.displayPertinence(gsr.getRawScore())).append("&nbsp;</td>");
    }

    if (activeSelection || exportEnabled) {
      if (gsr.isExportable()) {
        String checked = "";
        if (gsr.isSelected()) {
          checked = "checked";
        }
        result.append("<td class=\"selection\"><input type=\"checkbox\" ").append(checked).append(
            " name=\"resultObjects\" value=\"").append(gsr.getId()).append("-").append(
            gsr.getInstanceId()).append("\"></td>");
      } else {
        result
            .append(
                "<td class=\"selection\"><input type=\"checkbox\" disabled name=\"resultObjects\" value=\"")
            .append(
                gsr.getId()).append("-").append(gsr.getInstanceId()).append("\"></td>");
      }
    }

    if (gsr.getType() != null &&
        (gsr.getType().startsWith("Attachment") || gsr.getType().startsWith("Versioning") || gsr
        .getType().equals("LinkedFile"))) {
      String fileType = sName.substring(sName.lastIndexOf(".") + 1, sName.length());
      String fileIcon = FileRepositoryManager.getFileIcon(fileType);
      sName = "<img src=\"" + fileIcon + "\" class=\"fileIcon\"/>" + sName;
      // no preview, display this is an attachment
      if (gsr.getType().startsWith("Attachment") || gsr.getType().equals("LinkedFile")) {
        sDescription = null;
      }
    }

    result.append("<td class=\"content\">");

    result.append("<table cellspacing=\"0\" cellpadding=\"0\"><tr>");

    if (gsr.getThumbnailURL() != null && gsr.getThumbnailURL().length() > 0) {
      if ("UserFull".equals(gsr.getType())) {
        result.append("<td><img class=\"avatar\" src=\"").append(
            URLManager.getApplicationURL()).append(gsr.getThumbnailURL()).append("\" /></td>");
      } else {
        result.append("<td><img src=\"").append(gsr.getThumbnailURL()).append(
            "\" border=\"0\" width=\"").append(gsr.getThumbnailWidth()).append(
            "\" height=\"").append(gsr.getThumbnailHeight()).append("\"/></td>");
      }
      result.append("<td>&nbsp;</td>");
    }

    result.append("<td>");
    String curResultId = "readSpanId_" + gsr.getResultId();
    if (activeSelection) {
      result.append("<span id=\"").append(curResultId).append(
          "\" class=\"textePetitBold\">").append(sName).append("</span>");
    } else {
      String cssClass = "textePetitBold";
      String cssClassDisableVisited = "";
      if (gsr.isHasRead()) {
        cssClass = "markedkAsRead";
        cssClassDisableVisited = "markedkAsReadDisableVisited";
      }
      result.append("<a href=\"").append(sURL).append("\" class=\"").append(
          cssClassDisableVisited).append("\"><span id=\"").append(curResultId).append(
          "\" class=\"").append(cssClass).append("\">").append(sName).append("</span></a>");
    }
    if (StringUtil.isDefined(sDownloadURL)) {
      // affiche le lien pour le téléchargement
      result.append("<a href=\"").append(sDownloadURL).append("\" target=\"_blank\">").append(
          downloadSrc).append("</a>");
    }
    if (StringUtil.isDefined(sCreatorName)) {
      result.append(" <span class=\"creatorName\"> - ").append(
          EncodeHelper.javaStringToHtmlString(sCreatorName)).append("</span>");
    }
    if (StringUtil.isDefined(sCreationDate)) {
      result.append(" <span class=\"creationDate\"> (").append(sCreationDate).append(") </span>");
    }

    if (StringUtil.isDefined(sDescription)) {
      result.append("<span class=\"description\"><br/><i> ").append(
          EncodeHelper.javaStringToHtmlParagraphe(sDescription)).append("</i></span>");
    }

    if (sortValue == 7 && gsr.getHits() >= 0) {
      result.append("<br/><span class=\"popularity\">").append(
          settings.getStringWithParam("pdcPeas.popularity",
          Integer.toString(gsr.getHits()))).append("</span>");
    }

    if (StringUtil.isDefined(sLocation)) {
      result.append("<span class=\"location\"> <br/>").append(
          EncodeHelper.javaStringToHtmlString(sLocation)).append("</span>");
    }
    if (StringUtil.isDefined(extraInformation)) {
      result.append("<div class=\"extra\">");
      result.append(extraInformation);
      result.append("</div>");
    }
    result.append("<td>");
    result.append("</tr></table>");
    result.append("</td>");
    result.append("</tr>");

    return result.toString();
  }

  /**
   * @return a UserPreferences object from Personalization service.
   * @throws JspTagException
   */
  private UserPreferences getUserPreferences() throws JspTagException {
    return SilverpeasServiceProvider.getPersonalizationService().getUserSettings(getUserId());
  }

  /**
   * @return a ResourcesWrapper which encapsulate pdcPeas settings and bundles
   * @throws JspTagException
   */
  private ResourcesWrapper getSettings() throws JspTagException {
    if (settings == null) {
      String language = getUserPreferences().getLanguage();
      ResourceLocator messages = new ResourceLocator(
          "com.stratelia.silverpeas.pdcPeas.multilang.pdcBundle", language);
      settings =
          new ResourcesWrapper(messages,
          new ResourceLocator("com.stratelia.silverpeas.pdcPeas.settings.pdcPeasIcons", ""),
          new ResourceLocator("com.stratelia.silverpeas.pdcPeas.settings.pdcPeasSettings", ""),
          language);
    }
    return settings;
  }

}
