/**
 * Copyright (C) 2000 - 2011 Silverpeas
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
 * "http://repository.silverpeas.com/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.silverpeas.portlets.portal;

import java.io.UnsupportedEncodingException;

/**
 * I18n class provides methods to decode the value based on the character sets.
 */

public class I18n {
  public static final String DEFAULT_CHARSET = "UTF-8";
  public static final String ASCII_CHARSET = "ISO-8859-1";

  public static String decodeCharset(String s, String charset) {
    if (s == null) {
      return null;
    }

    try {
      byte buf[] = s.getBytes(ASCII_CHARSET);
      return new String(buf, 0, buf.length, charset);
    } catch (UnsupportedEncodingException uee) {
      return s;
    }
  }

  public static String encodeCharset(String s, String charset) {
    if (s == null) {
      return null;
    }

    try {
      byte buf[] = s.getBytes(charset);
      return new String(buf, 0, buf.length, ASCII_CHARSET);
    } catch (UnsupportedEncodingException uee) {
      return s;
    }
  }
}
