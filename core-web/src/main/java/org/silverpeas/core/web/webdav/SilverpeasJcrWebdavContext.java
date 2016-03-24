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
 * FLOSS exception. You should have recieved a copy of the text describing
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
package org.silverpeas.core.web.webdav;

import org.apache.jackrabbit.webdav.util.EncodeUtil;
import org.silverpeas.core.cache.service.CacheService;
import org.silverpeas.core.cache.service.CacheServiceProvider;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Yohann Chastagnier
 */
public class SilverpeasJcrWebdavContext {

  protected static final String WEBDAV_JCR_URL_SUFFIX = "webdav-jcr-url-suffix-";
  private static final Pattern TOKEN_PATTERN = Pattern.compile(".*/webdav/([a-zA-Z0-9]{16}+)/.*");

  private final String documentUrlLocation;
  private final String webDavUrl;
  private final String token;

  /**
   * Clears the application cache from the data handled by {@link SilverpeasJcrWebdavContext} and
   * associated to the given token.
   * @param token the token from which the cache must be cleared.
   */
  public static void clearFromToken(final String token) {
    getCacheService().remove(WEBDAV_JCR_URL_SUFFIX + token);
  }

  /**
   * Initializing an instance from the document URL location into the JCR and a token.
   * @param jcrDocumentUrlLocation the document url location into the JCR.
   * @param token the token to introduce.
   * @return the {@link SilverpeasJcrWebdavContext} instance initialized with the given data. The
   * webdavUrl has been computed.
   */
  public static SilverpeasJcrWebdavContext createWebdavContext(final String jcrDocumentUrlLocation,
      final String token) {

    // Handle the filename
    String fileName = EncodeUtil.escape(jcrDocumentUrlLocation.replaceFirst("^.*/", ""));

    // Cache the real path
    String webdavUrlSuffix =
        jcrDocumentUrlLocation.replaceFirst("^.*/webdav/", "").replaceFirst("[^/]*$", "") +
            fileName;
    getCacheService().put(WEBDAV_JCR_URL_SUFFIX + token, webdavUrlSuffix);

    // Compute a small url
    String webdavUrl =
        jcrDocumentUrlLocation.replaceFirst("/webdav/.*", "/webdav/" + token + "/" + fileName);

    // Return the final data
    return new SilverpeasJcrWebdavContext(jcrDocumentUrlLocation, token, webdavUrl);
  }

  /**
   * Initializing an instance from a computed webdavUrl.<br/>
   * If no token is found from the given URL, no exception is thrown and {@link #getToken()} will
   * return an empty string.
   * @param webDavUrl the webdav url.
   * @return the {@link SilverpeasJcrWebdavContext} instance initialized with the given webdavUrl.
   * The document URL location into the JCR is decoded and the token has been extracted.
   */
  public static SilverpeasJcrWebdavContext getWebdavContext(final String webDavUrl) {
    String token = "";
    String clearedWebdavUrl = webDavUrl;
    Matcher tokenMatcher = TOKEN_PATTERN.matcher(webDavUrl);
    if (tokenMatcher.matches()) {
      token = tokenMatcher.group(1);
      clearedWebdavUrl = clearedWebdavUrl.replace("/webdav/" + token, "/webdav");
    }
    if (!token.isEmpty()) {
      clearedWebdavUrl = clearedWebdavUrl.replaceFirst("/webdav/.*$", "/webdav/") +
          getCacheService().get(WEBDAV_JCR_URL_SUFFIX + token, String.class);
      if (webDavUrl.endsWith("/")) {
        clearedWebdavUrl = clearedWebdavUrl.replaceFirst("[^/]*$", "");
      }
    }
    return new SilverpeasJcrWebdavContext(clearedWebdavUrl, token, webDavUrl);
  }

  /**
   * Hidden constructor.
   * @param documentUrlLocation the document url location into the JCR.
   * @param token the token from which the cache must be cleared.
   * @param webDavUrl the webdav url.
   */
  private SilverpeasJcrWebdavContext(final String documentUrlLocation, final String token,
      final String webDavUrl) {
    this.documentUrlLocation = documentUrlLocation;
    this.webDavUrl = webDavUrl;
    this.token = token;
  }

  /**
   * Gets the document URL location into the JCR.
   * @return the URL as String.
   */
  public String getJcrDocumentUrlLocation() {
    return documentUrlLocation;
  }

  /**
   * Gets the computed webdav URL.
   * @return the URL as String.
   */
  public String getWebDavUrl() {
    return webDavUrl;
  }

  /**
   * The authentication token.
   * @return the token as String.
   */
  public String getToken() {
    return token;
  }

  private static CacheService getCacheService() {
    return CacheServiceProvider.getApplicationCacheService();
  }
}
