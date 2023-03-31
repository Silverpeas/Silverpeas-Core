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

import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.cache.model.Cache;
import org.silverpeas.core.cache.service.CacheServiceProvider;
import org.silverpeas.core.util.StringUtil;

import java.text.MessageFormat;
import java.util.UUID;

/**
 * A generator of a temporary token to allow a user to access by WebDAV a document in the JCR. All
 * the mechanism to access by WebDAV a document in the JCR is based upon the access token generated
 * by this generator. So, before opening a WebDAV access to a document in the JCR, and hence before
 * the {@link JCRWebDavServlet} has to be invoked, a token has to be generated and a
 * {@link WebDavContext} has to be created to allow the WebDAV mechanism of the JCR.
 * @author mmoquillon
 */
@SuppressWarnings("unused")
public class WebDavTokenGenerator {

  private static final String DOCUMENT_TOKEN_PATTERN = "webdav-token:{0}:{1}";
  private final User user;

  /**
   * Gets a token generator for the specified user. If no such generator exists for the user, a new
   * one is created.
   * @param user the user for whom the generator has to be got.
   * @return a token generator instance.
   * @implSpec a generator instance is always created as the data it handles is cached.
   */
  public static WebDavTokenGenerator getFor(final User user) {
    return new WebDavTokenGenerator(user);
  }

  /**
   * Constructs a new token generator for the specified user in Silverpeas.
   * @param user a user in Silverpeas wishing to access documents in the JCR.
   */
  private WebDavTokenGenerator(final User user) {
    this.user = user;
  }

  /**
   * Generates a token for the underlying user to edit by WebDAV the specified document. The token
   * is like a key to edit a given document.
   * @param documentId the unique identifier of the document to access by WebDAV.
   * @return the generated token.
   */
  public String generateToken(String documentId) {
    String token = generateToken();
    Cache cache = getCacheService();
    cache.put(token, user); // 12h by default of TTL
    String documentTokenKey =
        MessageFormat.format(DOCUMENT_TOKEN_PATTERN, user.getId(), documentId);
    cache.put(documentTokenKey, token);
    return token;
  }

  /**
   * Deletes the token that was generated for the underlying user to access the specified document.
   * If no such tokens exist, then nothing is done. If the token doesn't belong to the user, then an
   * IllegalArgumentException is thrown.
   * @param documentId the unique identifier of the document.
   * @throws IllegalArgumentException if the specified user has no token to access the specified
   * document.
   */
  public void deleteToken(String documentId) throws IllegalArgumentException {
    Cache cache = getCacheService();
    String documentTokenKey =
        MessageFormat.format(DOCUMENT_TOKEN_PATTERN, user.getId(), documentId);
    String token = (String) cache.get(documentTokenKey);
    if (token != null) {
      User actualUser = (User) cache.get(token);
      if (actualUser == null || !actualUser.getId().equals(user.getId())) {
        throw new IllegalArgumentException("No token for user " + user.getId() +
            " to access document " + documentId);
      }
      WebDavContext.clearWebDavContext(token);
      cache.remove(token);
      cache.remove(documentTokenKey);
    }
  }

  private static String generateToken() {
    String[] parts = UUID.randomUUID().toString().split("-");
    return StringUtil.asBase64(parts[parts.length - 1].getBytes());
  }

  private static Cache getCacheService() {
    return CacheServiceProvider.getApplicationCacheService().getCache();
  }
}
