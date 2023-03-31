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

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * An opener of the access to a given document through the WebDAV protocol. It has to be invoked
 * when an HTTP request is asking for accessing a document in the JCR. The opener takes the HTTP
 * response that will be sent back to the client to inform it to redirect its request to a given
 * WebDAV URL at which the document is accessible. The WebDAV URL is based upon the Custom Protocol
 * (see {@link WebDavProtocol} class) defined here for the circumstance. This Custom Protocol allows
 * to customize the behaviour of the client when receiving such a URL; the client, a web browser, is
 * expected to delegate the interpretation of the URL to an external tool (provided by Silverpeas)
 * which will look for a program in the host knowing how to open by WebDAV the document referred by
 * the URL.
 * @author mmoquillon
 */
@SuppressWarnings("unused")
public class WebDavAccessOpener {

  /**
   * Opens for the specified user the access to the document referred by the given URL through the
   * WebDAV protocol. The given HTTP response will be enriched with information required for the
   * requester to redirect its request to a WebDAV URL locating the document in the JCR by a WebDAV
   * access. The URL is minted for the user behind the request and as such it cannot be reused by
   * another user. The schema of the URL is a Custom Protocol to get the client to delegate its
   * treatment to an external tool developed by Silverpeas for the circumstance.
   * @param user the requester of the access to the document.
   * @param documentURL the Web URL of the document in the JCR.
   * @param response the HTTP response to use to inform the client how to access the document by
   * WebDAV.
   * @throws IOException if an error occurs with the HTTP response.
   */
  public void open(final User user, final String documentURL, final HttpServletResponse response)
      throws IOException {
    User aUser = user == null ? User.getCurrentRequester() : user;
    String token = WebDavTokenGenerator.getFor(aUser).generateToken(fetchDocumentId(documentURL));
    WebDavContext webDAVContext = WebDavContext.createWebDavContext(token, documentURL);
    String webDavUrl = webDAVContext.getWebDavUrl();
    response.setContentType("application/javascript");
    response.setHeader("Content-Disposition", "inline; filename=launch.js");
    String customProtocolUrl = webDavUrl.startsWith("https:") ?
        webDavUrl.replaceFirst("^https", WebDavProtocol.SECURED_WEBDAV_SCHEME) :
        webDavUrl.replaceFirst("^http", WebDavProtocol.WEBDAV_SCHEME);
    response.getWriter().append("window.location.href='").append(customProtocolUrl).append("';");
  }

  private static String fetchDocumentId(String documentUrl) {
    String[] paths = documentUrl.split("/");
    if (paths.length > 3) {
      return paths[paths.length - 3];
    }
    return null;
  }
}
