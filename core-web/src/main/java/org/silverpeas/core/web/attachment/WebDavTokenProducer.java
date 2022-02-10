/*
 * Copyright (C) 2000 - 2022 Silverpeas
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
package org.silverpeas.core.web.attachment;

import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.cache.model.Cache;
import org.silverpeas.core.web.webdav.SilverpeasJcrWebdavContext;
import org.silverpeas.core.cache.service.CacheServiceProvider;
import org.silverpeas.core.util.StringUtil;

import java.text.MessageFormat;
import java.util.UUID;

/**
 * A producer of a token to access by WebDAV a document.
 * @author miguel
 */
public class WebDavTokenProducer {

  private static final String DOCUMENT_TOKEN_PATTERN = "webdav-token:{0}:{1}";

  /**
   * Generates a token for the specified user to edit by WebDAV the specified document. The token
   * is like a key to edit a given document.
   * @param user the user for which a token is generated.
   * @param documentId the unique identifier of the document to access by WebDAV.
   * @return the generated token.
   */
  public static String generateToken(User user, String documentId) {
    String token = generateToken();
    Cache cache = getCacheService();
    cache.put(token, user); // 12h by default of TTL
    String documentTokenKey = MessageFormat.format(DOCUMENT_TOKEN_PATTERN, user.getId(), documentId);
    cache.put(documentTokenKey, token);
    return token;
  }

  /**
   * Deletes the token that was generated for the specified user to access the specified document.
   * If no such tokens exist, then nothing is done.
   * If the token doesn't belong to the user, then an IllegalArgumentException is thrown.
   * @param documentId the unique identifier of the document.
   * @throws IllegalArgumentException if the specified user has no token to access the specified
   * document.
   */
  public static void deleteToken(User user, String documentId) throws
                                                                     IllegalArgumentException {
    Cache cache = getCacheService();
    String documentTokenKey = MessageFormat.format(DOCUMENT_TOKEN_PATTERN, user.getId(), documentId);
    String token = (String) cache.get(documentTokenKey);
    if (token != null) {
      User actualUser = (User) cache.get(token);
      if (actualUser == null || !actualUser.getId().equals(user.getId())) {
        throw new IllegalArgumentException("No token for user " + user.getId() +
            " to access document " + documentId);
      }
      SilverpeasJcrWebdavContext.clearFromToken(token);
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
