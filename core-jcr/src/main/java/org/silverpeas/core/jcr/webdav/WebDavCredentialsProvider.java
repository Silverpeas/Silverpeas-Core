/*
 * Copyright (C) 2000 - 2023 Silverpeas
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
 * "https://www.silverpeas.org/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 *
 */
package org.silverpeas.core.jcr.webdav;

import org.apache.jackrabbit.api.security.authentication.token.TokenCredentials;
import org.apache.jackrabbit.server.CredentialsProvider;
import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.admin.user.model.UserReference;
import org.silverpeas.core.annotation.Provider;
import org.silverpeas.core.cache.service.CacheServiceProvider;
import org.silverpeas.core.jcr.security.JCRUserCredentialsProvider;
import org.silverpeas.core.jcr.security.WebDavAccessContext;
import org.silverpeas.core.security.token.exception.TokenException;
import org.silverpeas.core.security.token.persistent.PersistentResourceToken;

import javax.jcr.Credentials;
import javax.jcr.LoginException;
import javax.servlet.http.HttpServletRequest;

/**
 * A provider of credentials for the WebDAV servlet to permit the authentication of the user
 * accessing the JCR by WebDAV. The user is identified by an access token, generated for the
 * circumstance, and encoded into the WebDAV URL of the accessed path in the JCR.
 * @author mmoquillon
 * @implSpec A temporary access token is expected to be encoded with the requested URL. From this
 * token, the user behind the request is identified and then its API token is used for the
 * authentication.
 */
@Provider
public class WebDavCredentialsProvider implements CredentialsProvider {

  protected WebDavCredentialsProvider() {
  }

  @Override
  public Credentials getCredentials(final HttpServletRequest request) throws LoginException {
    final TokenCredentials credentials;
    final WebDavContext webdavContext = WebDavContext.getWebDavContext(request.getPathInfo());
    final String accessToken = webdavContext.getToken();
    if (!accessToken.isEmpty()) {
      User user = CacheServiceProvider.getApplicationCacheService()
          .getCache()
          .get(accessToken, User.class);
      if (user != null) {
        String authToken;
        try {
          UserReference ref = UserReference.fromUser(user);
          authToken = PersistentResourceToken.getOrCreateToken(ref).getValue();
        } catch (TokenException e) {
          throw new LoginException("Failure to get the API token of user " + user.getId(), e);
        }
        credentials = (TokenCredentials) JCRUserCredentialsProvider.getUserCredentials(authToken);
        String documentURL =
            webdavContext.getDocumentURL().replaceFirst("^/[^/]+", "");
        credentials.setAttribute(WebDavAccessContext.AUTHORIZED_DOCUMENT_PATH_ATTRIBUTE,
            documentURL);
      } else {
        throw new LoginException("No user matching the credentials!");
      }
    } else {
      throw new LoginException("Bad credentials!");
    }
    return credentials;
  }
}
