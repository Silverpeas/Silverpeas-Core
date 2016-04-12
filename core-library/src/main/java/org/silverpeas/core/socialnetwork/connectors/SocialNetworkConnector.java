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

package org.silverpeas.core.socialnetwork.connectors;

import javax.servlet.http.HttpServletRequest;

import org.silverpeas.core.socialnetwork.service.SocialNetworkAuthorizationException;
import org.springframework.social.connect.UserProfile;

import org.silverpeas.core.socialnetwork.service.AccessToken;

public interface SocialNetworkConnector {

  /**
   * Build URL to call for authentication over social network
   * @param callBackURL the URL to call to after authentication
   * @return authentication URL as String
   */
  String buildAuthenticateUrl(String callBackURL);

  AccessToken exchangeForAccessToken(HttpServletRequest request, String callBackURL)
      throws SocialNetworkAuthorizationException;

  UserProfile getUserProfile(AccessToken authorizationToken);

  String getUserProfileId(AccessToken authorizationToken);

  void updateStatus(AccessToken authorizationToken, String status);

  void setJavascriptAttributes(HttpServletRequest request);
}
