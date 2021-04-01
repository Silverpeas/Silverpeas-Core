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

package org.silverpeas.core.webapi.wbe;

import org.silverpeas.core.util.StringUtil;
import org.silverpeas.core.web.WebResourceUri;
import org.silverpeas.core.webapi.base.ProtectedWebResource;
import org.silverpeas.core.webapi.base.UserPrivilegeValidation;

import javax.servlet.http.HttpServletRequest;

import static org.silverpeas.core.util.StringUtil.defaultStringIfNotDefined;
import static org.silverpeas.core.webapi.base.UserPrivilegeValidation.HTTP_ACCESS_TOKEN;
import static org.silverpeas.core.webapi.base.UserPrivilegeValidation.HTTP_AUTHORIZATION;

/**
 * <p>
 * All WEB services handling the WBE host requests must implement this interface.
 * </p>
 * @author silveryocha
 */
public interface WbeProtectedWebResource extends ProtectedWebResource {

  @Override
  WbeRequestContext getSilverpeasContext();

  @Override
  default void validateUserAuthentication(final UserPrivilegeValidation validation) {
    final HttpServletRequest request = getSilverpeasContext().getRequest();
    final String authorizationValue = request.getHeader(HTTP_AUTHORIZATION);
    final String accessToken;
    if (StringUtil.isDefined(authorizationValue)) {
      accessToken = authorizationValue.substring("Bearer ".length());
    } else {
      accessToken = defaultStringIfNotDefined(request.getHeader(HTTP_ACCESS_TOKEN),
          request.getParameter(HTTP_ACCESS_TOKEN));
    }
    getSilverpeasContext().setAccessToken(accessToken);
  }

  @Override
  default HttpServletRequest getHttpRequest() {
    return getSilverpeasContext().getRequest();
  }

  @Override
  default WebResourceUri getUri() {
    return null;
  }

  @Override
  default String getComponentId() {
    return null;
  }
}
