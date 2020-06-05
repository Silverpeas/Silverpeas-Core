/*
 * Copyright (C) 2000 - 2020 Silverpeas
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
package org.silverpeas.web.portlets.portal;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;

/**
 * I18n class provides methods to decode the value based on the character sets.
 */

class I18n {
  static final String DEFAULT_CHARSET = StandardCharsets.UTF_8.name();

  static String decodeCharset(String s, String charset) {
    if (s == null) {
      return null;
    }
    try {
      byte buf[] = s.getBytes(StandardCharsets.ISO_8859_1);
      return new String(buf, 0, buf.length, charset);
    } catch (UnsupportedEncodingException uee) {
      return s;
    }
  }
}
