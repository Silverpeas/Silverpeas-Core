/*
 * Copyright (C) 2000 - 2013 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection withWriter Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have recieved a copy of the text describing
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
package org.silverpeas.core.util;

import org.apache.commons.lang3.CharEncoding;

import java.nio.charset.Charset;
import java.nio.charset.UnsupportedCharsetException;

/**
 *
 * @author ehugonnet
 */
public class Charsets {

  /**
   * US-ASCII: seven-bit ASCII.
   */
  public static final Charset US_ASCII = Charset.forName(CharEncoding.US_ASCII);
  /**
   * ISO-8859-1 : ISO-LATIN-1.
   */
  public static final Charset ISO_8859_1 = Charset.forName(CharEncoding.ISO_8859_1);
  /**
   * UTF-8.
   */
  public static final Charset UTF_8 = Charset.forName(CharEncoding.UTF_8);
  /**
   * UTF-16BE: UTF-16 big-endian byte order.
   */
  public static final Charset UTF_16BE = Charset.forName(CharEncoding.UTF_16BE);
  /**
   * UTF-16LE: UTF-16 little-endian byte order.
   */
  public static final Charset UTF_16LE = Charset.forName(CharEncoding.UTF_16LE);
  /**
   * UTF-16: UTF-16 byte order identified by an optional byte-order mark.
   */
  public static final Charset UTF_16 = Charset.forName(CharEncoding.UTF_16);

  private Charsets() {
  }


  /**
   * Returns a Charset for the named charset. If the name is null, return the default Charset.
   *
   * @param charset The name of the requested charset, may be null.
   * @return a Charset for the named charset
   * @throws java.nio.charset.UnsupportedCharsetException If the named charset is unavailable
   */
  public static Charset toCharset(String charset) throws UnsupportedCharsetException {
    if (charset != null) {
      return Charset.forName(charset);
    }
    return Charset.defaultCharset();
  }
}
