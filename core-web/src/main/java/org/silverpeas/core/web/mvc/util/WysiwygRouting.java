/*
 * Copyright (C) 2000 - 2024 Silverpeas
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
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package org.silverpeas.core.web.mvc.util;

import org.apache.commons.lang3.CharEncoding;
import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.contribution.model.ContributionIdentifier;
import org.silverpeas.core.i18n.I18NHelper;
import org.silverpeas.core.util.StringUtil;
import org.silverpeas.core.web.mvc.controller.ComponentSessionController;

import javax.servlet.http.HttpServletRequest;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

/**
 * A routing definition to the WYSIWYG editor in Silverpeas. It is an HTML page in which is
 * displayed the WYSIWYG text editor tool.
 * @author mmoquillon
 */
public class WysiwygRouting {

  private static final String EDITOR_PAGE_PATH = "/wysiwyg/jsp/htmlEditor.jsp";


  /**
   * Gets the path of the WYSIWYG editor page relative to the Silverpeas web context.
   * @param context the routing context from which properties are set.
   * @param request the current HTTP request. If null, then the path will have query-parameters to
   * set some required properties.
   * @return the path relative to the Silverpeas web context.
   * @throws RoutingException if an error occurs while building the path.
   */
  public String getWysiwygEditorPath(final WysiwygRoutingContext context,
      final HttpServletRequest request) throws RoutingException {
    if (request == null) {
      return getDestinationToWysiwygEditor(context);
    }
    try {
      request.setAttribute("SpaceName", URLEncoder.encode(context.spaceLabel, CharEncoding.UTF_8));
      request.setAttribute("ComponentId", context.contributionId.getComponentInstanceId());
      request.setAttribute("ComponentLabel",
          URLEncoder.encode(context.componentLabel, CharEncoding.UTF_8));
      request.setAttribute("ObjectId", context.contributionId.getLocalId());
      request.setAttribute("ObjectType", context.contributionId.getType());
      request.setAttribute("Language", context.language);
      request.setAttribute("BrowseInfo", context.browseInfo);
      request.setAttribute("ReturnUrl", context.comeBackUrl);
      request.setAttribute("UserId", User.getCurrentRequester().getId());
      request.setAttribute("IndexIt", String.valueOf(context.indexation));
      if (StringUtil.isDefined(context.contentLanguage)) {
        request.setAttribute("ContentLanguage", context.contentLanguage);
      }
      if (StringUtil.isDefined(context.browseInfo)) {
        request.setAttribute("BrowseInfo", context.browseInfo);
      }
      if (StringUtil.isDefined(context.fileName)) {
        request.setAttribute("FileName", context.fileName);
      }
      return EDITOR_PAGE_PATH;
    } catch (UnsupportedEncodingException e) {
      throw new RoutingException(e);
    }
  }

  /**
   * Gets the path of the WYSIWYG editor path relative to the Silverpeas web context enriched with
   * query parameters to set the required properties asked by the editor.
   * @param context the routing context from which properties are set.
   * @return the path with query parameters relative to the Silverpeas web context.
   * @throws RoutingException if an error occurs while building the path.
   */
  public String getDestinationToWysiwygEditor(final WysiwygRoutingContext context)
      throws RoutingException {
    try {
      StringBuilder destination = new StringBuilder(EDITOR_PAGE_PATH);
      destination.append("?")
          .append("SpaceLabel=")
          .append(URLEncoder.encode(context.spaceLabel, "UTF-8"))
          .append("&ComponentId=")
          .append(context.contributionId.getComponentInstanceId())
          .append("&ComponentLabel=")
          .append(URLEncoder.encode(context.componentLabel, "UTF-8"))
          .append("&ObjectId=")
          .append(context.contributionId.getLocalId())
          .append("&ObjectType=")
          .append(context.contributionId.getType())
          .append("&Language=")
          .append(context.language)
          .append("&ReturnUrl=")
          .append(context.comeBackUrl);
      if (StringUtil.isDefined(context.contentLanguage)) {
        destination.append("&ContentLanguage=").append(context.contentLanguage);
      }
      if (StringUtil.isDefined(context.browseInfo)) {
        destination.append("&BrowseInfo=").append(context.browseInfo);
      }
      if (StringUtil.isDefined(context.fileName)) {
        destination.append("&FileName=").append(context.fileName);
      }
      return destination.toString();
    } catch (UnsupportedEncodingException e) {
      throw new RoutingException(e);
    }
  }

  /**
   * Context for which the routing to the WYSIWYG editor will be done.
   */
  public static class WysiwygRoutingContext {
    private String spaceLabel;
    private String componentLabel;
    private ContributionIdentifier contributionId;
    private String language = I18NHelper.defaultLanguage;
    private boolean indexation = true;
    private String comeBackUrl;
    private String browseInfo;
    private String contentLanguage;
    private String fileName;

    public static WysiwygRoutingContext fromComponentSessionController(
        final ComponentSessionController controller) {
      return new WysiwygRoutingContext().withSpaceLabel(controller.getSpaceLabel())
          .withComponentLabel(controller.getComponentLabel())
          .withLanguage(controller.getLanguage());
    }

    public WysiwygRoutingContext withSpaceLabel(final String spaceLabel) {
      this.spaceLabel = spaceLabel;
      return this;
    }

    public WysiwygRoutingContext withContributionId(final ContributionIdentifier contributionId) {
      this.contributionId = contributionId;
      return this;
    }

    public WysiwygRoutingContext withComponentLabel(final String componentLabel) {
      this.componentLabel = componentLabel;
      return this;
    }

    public WysiwygRoutingContext withLanguage(final String language) {
      this.language = language;
      return this;
    }

    public WysiwygRoutingContext withIndexation(final boolean indexation) {
      this.indexation = indexation;
      return this;
    }

    public WysiwygRoutingContext withComeBackUrl(final String url) {
      this.comeBackUrl = url;
      return this;
    }

    public WysiwygRoutingContext withBrowseInfo(final String browseInfo) {
      this.browseInfo = browseInfo;
      return this;
    }

    public WysiwygRoutingContext withContentLanguage(final String language) {
      this.contentLanguage = language;
      return this;
    }

    public WysiwygRoutingContext withFileName(final String wysiwygFileName) {
      this.fileName = wysiwygFileName;
      return this;
    }
  }
}
  