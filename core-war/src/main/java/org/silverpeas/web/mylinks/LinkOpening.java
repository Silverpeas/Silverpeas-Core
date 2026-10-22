/*
 * Copyright (C) 2000 - 2026 Silverpeas
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
package org.silverpeas.web.mylinks;

import java.util.Locale;

/**
 * The way a favorite link is opened when the user clicks it in the page of the links.
 * <p>
 * The pages of Silverpeas are displayed in the content area of Silverpeas. An external web page
 * is isolated from Silverpeas within a sandboxed frame: it can neither access nor navigate the
 * pages of Silverpeas. Nevertheless, a site can forbid its pages to be framed by another site;
 * the page displaying the sandboxed frame then provides a link to open it in a new window. A page
 * that can't be framed by Silverpeas (an unsecured HTTP page or a non web resource) is opened in a
 * new window, as are the links for which the user asked a new window.
 * </p>
 */
public enum LinkOpening {
  /**
   * The link is opened in the content area of Silverpeas.
   */
  IN_CONTENT,
  /**
   * The link is opened in a sandboxed frame within the content area of Silverpeas.
   */
  IN_SANDBOXED_FRAME,
  /**
   * The link is opened in a new window (or a new tab, according to the browser settings).
   */
  IN_NEW_WINDOW;

  private static final String SCHEME_SEPARATOR = "://";
  private static final String SECURED_WEB_SCHEME = "https" + SCHEME_SEPARATOR;

  /**
   * Gets the way the specified link has to be opened.
   * @param url the URL of the link, either absolute or relative to the server.
   * @param popup true if the user asked the link to be opened in a new window.
   * @param serverUrl the URL of the server of Silverpeas (scheme, host and port).
   * @return the way the link has to be opened.
   */
  public static LinkOpening of(final String url, final boolean popup, final String serverUrl) {
    final LinkOpening opening;
    if (popup) {
      opening = IN_NEW_WINDOW;
    } else if (!url.contains(SCHEME_SEPARATOR) || isOnServer(url, serverUrl)) {
      opening = IN_CONTENT;
    } else if (lowerCase(url).startsWith(SECURED_WEB_SCHEME)) {
      opening = IN_SANDBOXED_FRAME;
    } else {
      opening = IN_NEW_WINDOW;
    }
    return opening;
  }

  private static boolean isOnServer(final String url, final String serverUrl) {
    final String server = lowerCase(serverUrl);
    final String link = lowerCase(url);
    return link.equals(server) || link.startsWith(server + "/");
  }

  private static String lowerCase(final String text) {
    return text.toLowerCase(Locale.ROOT);
  }
}
