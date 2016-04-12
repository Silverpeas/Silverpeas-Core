/*
 * Copyright (C) 2000 - 2014 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU Affero General Public License as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of the GPL, you may
 * redistribute this Program in connection with Free/Libre Open Source Software ("FLOSS")
 * applications as described in Silverpeas's FLOSS exception. You should have received a copy of the
 * text describing the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License along with this program.
 * If not, see <http://www.gnu.org/licenses/>.
 */

package org.silverpeas.web.webdav;

import org.silverpeas.core.admin.user.model.UserDetail;
import org.apache.jackrabbit.api.security.authentication.token.TokenCredentials;
import org.apache.jackrabbit.server.CredentialsProvider;
import org.silverpeas.core.cache.service.CacheServiceProvider;

import javax.jcr.Credentials;
import javax.jcr.LoginException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import java.text.MessageFormat;

import static org.silverpeas.core.web.webdav.SilverpeasJcrWebdavContext.getWebdavContext;

/**
 * A provider of WebDav credentials for the WebDAV servlet.
 * @author mmoquillon
 */
public class WebDavCredentialsProvider implements CredentialsProvider {

  private static final String USERID_TEMPLATE = "{0}@domain{1}";
  private static final String USERID_TOKEN_ATTRIBUTE = "UserID";

  @Override
  public Credentials getCredentials(final HttpServletRequest request)
      throws LoginException, ServletException {
    Credentials credentials;
    String authToken = getWebdavContext(request.getPathInfo()).getToken();
    if (!authToken.isEmpty()) {
      UserDetail user =
          CacheServiceProvider.getApplicationCacheService().get(authToken, UserDetail.class);
      if (user != null) {
        String userID = MessageFormat.format(USERID_TEMPLATE, user.getLogin(), user.getDomainId());
        credentials = new TokenCredentials(authToken);
        ((TokenCredentials) credentials).setAttribute(USERID_TOKEN_ATTRIBUTE, userID);
      } else {
        throw new LoginException("No user matching the credentials!");
      }
    } else {
      throw new LoginException("Bad credentials!");
    }
    return credentials;
  }
}
