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

package org.silverpeas.core.security.html;

import org.silverpeas.core.util.ServiceProvider;

/**
 * In data sanitization, HTML sanitization is the process of examining an HTML document and
 * producing a new HTML document that preserves only whatever tags are designated "safe" and
 * desired. HTML sanitization can be used to protect against attacks such as cross-site scripting
 * (XSS) by sanitizing any HTML code submitted by a user.
 * @author silveryocha
 */
public interface HtmlSanitizer {

  static HtmlSanitizer get() {
    return ServiceProvider.getSingleton(HtmlSanitizer.class);
  }

  /**
   * @see #sanitize(String)
   */
  static String ofHtml(final String html) {
    return HtmlSanitizer.get().sanitize(html);
  }

  /**
   * Sanitizing the given content by keeping:
   * <ul>
   *   <li>safe formatting</li>
   *   <li>safe blocks</li>
   *   <li>safe images</li>
   *   <li>safe links</li>
   *   <li>safe tables</li>
   *   <li>safe styles</li>
   * </ul>
   * <p>
   *   All links are modified in order to be opened safely into a new blank page.
   * </p>
   * @param html a string representing an HTML content.
   * @return a string representing the sanitized version of given parameter.
   */
  String sanitize(final String html);
}
