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

package org.silverpeas.core.web.util;

import org.owasp.encoder.Encode;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import static org.silverpeas.kernel.util.StringUtil.isDefined;

/**
 * Web redirection URL computer based upon the some request or session parameters. Those parameters
 * are defined under the name whose definition is provided by this class.
 *
 * @author mmoquillon
 */
public final class WebRedirection {

  /**
   * Unique identifier of the attachment to redirect to.
   */
  public static final String REDIRECT_TO_ATTACHMENT = "RedirectToAttachmentId";

  /**
   * Unique identifier of the component instance to redirect to.
   */
  public static final String REDIRECT_TO_COMPONENT = "RedirectToComponentId";

  /**
   * Unique identifier of the workspace to redirect to.
   */
  public static final String REDIRECT_TO_SPACE = "RedirectToSpaceId";

  /**
   * The type of the resource targeted by a redirection. Usually used with a targeted attachment.
   */
  public static final String REDIRECT_RESOURCE_TYPE = "RedirectToMapping";

  /**
   * URL to which the web redirection has to be done.
   */
  public static final String REDIRECT_URL = "gotoNew";

  /**
   * Computes the redirection URL if some redirection properties have been set into the specified
   * request (or in the user session).
   * @param request the incoming HTTP request.
   * @return the redirection URL or an empty string if there is no redirection properties set.
   */
  public String getRedirectionURL(final HttpServletRequest request) {
    HttpSession session = request.getSession(false);
    String gotoUrl = (String) session.getAttribute(REDIRECT_URL);
    String componentId = (String) session.getAttribute(REDIRECT_TO_COMPONENT);
    String spaceId = (String) session.getAttribute(REDIRECT_TO_SPACE);
    StringBuilder builder = new StringBuilder();
    if (isDefined(gotoUrl)) {
      builder.append("goto=").append(Encode.forUriComponent(gotoUrl));
    } else if (isDefined(componentId)) {
      builder.append("ComponentId=").append(componentId);
    } else if (isDefined(spaceId)) {
      builder.append("SpaceId=").append(spaceId);
    }
    if (builder.length() > 0) {
      builder.insert(0, "/autoRedirect.jsp?");
    }
    return builder.toString();
  }
}
  