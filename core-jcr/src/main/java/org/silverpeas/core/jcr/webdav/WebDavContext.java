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

import org.apache.jackrabbit.webdav.util.EncodeUtil;
import org.silverpeas.kernel.cache.model.Cache;
import org.silverpeas.core.cache.service.CacheAccessorProvider;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Context of a WebDAV access to a document in the JCR. It refers the accessed document and the
 * token allowing the user to access this resource. It handles both the Web URL of the document in
 * the JCR and the WebDAV one from which it is accessed through the WebDAV protocol. In Silverpeas,
 * each document can be accessed through a Web link (his Web URL) but his access through the WebDAV
 * protocol is done by a custom tiny URL in which a token is generated for the user accessing the
 * document.
 * @author Yohann Chastagnier
 * @implNote The path in Web URL locating the document in the JCR should satisfy the following
 * pattern: <code>/webdav/DOCUMENT_ID/DOCUMENT_FILENAME</code>. This knowledge allows the context to
 * compute the tiny URL of the document for its WebDAV access.
 */
public class WebDavContext {

  static final String WEBDAV_JCR_URL_SUFFIX = "webdav-jcr-url-suffix-";
  private static final Pattern TOKEN_PATTERN = Pattern.compile(".*/webdav/([a-zA-Z0-9]{16}+).*");
  private static final String WEBDAV = "/webdav/";

  private final String documentUrl;
  private final String webDavUrl;
  private final String token;

  /**
   * Clears the WebDAV context mapped with the specified access token identifying the user behind a
   * WebDAV access of a document.
   * @param token the token identifying here the WebDAV context that was created for a given user to
   * access a document by WebDAV.
   */
  public static void clearWebDavContext(final String token) {
    getCache().remove(WEBDAV_JCR_URL_SUFFIX + token);
  }

  /**
   * Creates a new WebDAV context for the specified access token identifying the user accessing the
   * document and from the specified Web URL locating the document into the JCR.
   * @param token the access token identifying the user accessing the document.
   * @param documentUrl URL of the document in the JCR in the context of a WebDAV access.
   * @return a new {@link WebDavContext} instance referring the accessed document and the user
   * behind the access.
   */
  public static WebDavContext createWebDavContext(final String token, final String documentUrl) {

    // Extract the document filename
    String fileName = EncodeUtil.escape(documentUrl.replaceFirst("^.*/", ""));

    // Cache the real path of the document in the JCR
    String webdavUrlSuffix =
        documentUrl.replaceFirst("^.*" + WEBDAV, "").replaceFirst("[^/]*$", "") + fileName;
    getCache().put(WEBDAV_JCR_URL_SUFFIX + token, webdavUrlSuffix);

    // Compute a small url for the WebDAV access of the document in the JCR.
    String webdavUrl =
        documentUrl.replaceFirst(WEBDAV + ".*", WEBDAV + token + "/" + fileName);

    // Return the final data
    return new WebDavContext(token, documentUrl, webdavUrl);
  }

  /**
   * Gets the WebDAV context that has generated the specified tiny URL locating by WebDAV a document
   * for a given user. The URL should contain the access token from which the user can be
   * identified. If no token is found in the given URL, no exception is thrown and
   * {@link WebDavContext#getToken()} will return an empty string.
   * @param webDavUrl the tiny URL of a document in a WebDAV access.
   * @return the {@link WebDavContext} instance referring the document located at the given URL or
   * an invalid {@link WebDavContext} object.
   */
  public static WebDavContext getWebDavContext(final String webDavUrl) {
    String token = "";
    String clearedWebdavUrl = webDavUrl;
    Matcher tokenMatcher = TOKEN_PATTERN.matcher(webDavUrl);
    if (tokenMatcher.matches()) {
      token = tokenMatcher.group(1);
    }
    if (!token.isEmpty()) {
      clearedWebdavUrl = clearedWebdavUrl.replace(WEBDAV + token, "/webdav");
      boolean webdavUrlContainsFileName = clearedWebdavUrl.matches(".*" + WEBDAV + ".+");
      clearedWebdavUrl = clearedWebdavUrl.replaceFirst("/webdav.*$", WEBDAV) +
          getCache().get(WEBDAV_JCR_URL_SUFFIX + token, String.class);
      if (!webdavUrlContainsFileName) {
        clearedWebdavUrl = clearedWebdavUrl.replaceFirst("/[^/]*$", "");
        if (webDavUrl.endsWith("/")) {
          clearedWebdavUrl += "/";
        }
      }
    }
    return new WebDavContext(token, clearedWebdavUrl, webDavUrl);
  }

  /**
   * Constructs a new {@link WebDavContext} instance.
   * @param token the temporary access token identifying the user behind a WebDAV access of the
   * document
   * @param documentUrl the Web URL locating the document in the JCR.
   * @param webDavUrl the tiny URL used to access the document through the WebDAV protocol.
   */
  private WebDavContext(final String token, final String documentUrl,
      final String webDavUrl) {
    this.documentUrl = documentUrl;
    this.webDavUrl = webDavUrl;
    this.token = token;
  }

  /**
   * Gets the Web URL of the accessed document in the JCR.
   * @return the URL as a {@link String}
   */
  public String getDocumentURL() {
    return documentUrl;
  }

  /**
   * Gets the tiny computed and unique URL of the document with which it can be accessed by WebDAV
   * @return the URL as {@link String}
   */
  public String getWebDavUrl() {
    return webDavUrl;
  }

  /**
   * The temporary access token.
   * @return the token as {@link String}.
   */
  public String getToken() {
    return token;
  }

  private static Cache getCache() {
    return CacheAccessorProvider.getApplicationCacheAccessor().getCache();
  }
}
