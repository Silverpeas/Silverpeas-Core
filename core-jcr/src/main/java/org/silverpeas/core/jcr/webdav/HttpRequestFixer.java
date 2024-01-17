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

import org.apache.jackrabbit.webdav.DavConstants;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.util.regex.Pattern.compile;

/**
 * A wrapper of an incoming HTTP request. Its goal is to fix the broken lock token crafted by some
 * JCR implementation (Apache Jackrabbit Oak for example. See <a
 * href="https://issues.apache.org/jira/browse/OAK-10166">https://issues.apache
 * .org/jira/browse/OAK-10166</a>).
 * @author mmoquillon
 */
public class HttpRequestFixer extends HttpServletRequestWrapper {

  private static final Pattern UPPERCASE_ENCODED_CHARS_PATTERN = compile("%[0-9A-F][0-9A-F]");

  /**
   * Constructs a request object wrapping the given request.
   * @param request the {@link HttpServletRequest} to be wrapped.
   * @throws IllegalArgumentException if the request is null
   */
  public HttpRequestFixer(final HttpServletRequest request) {
    super(request);
  }

  @Override
  public String getHeader(final String name) {
    String value = super.getHeader(name);
    if ((DavConstants.HEADER_LOCK_TOKEN.equals(name) || DavConstants.HEADER_IF.equals(name))
        && value != null) {
      final Map<String, String> fixes = new HashMap<>();
      final Matcher matcher = UPPERCASE_ENCODED_CHARS_PATTERN.matcher(value);
      while (matcher.find()) {
        final String match = matcher.group();
        if (!fixes.containsKey(match)) {
          final String lowerCase = match.toLowerCase();
          if (!match.equals(lowerCase)) {
            fixes.put(match, lowerCase);
          }
        }
      }
      for (Map.Entry<String, String> entry : fixes.entrySet()) {
        value = value.replace(entry.getKey(), entry.getValue());
      }
    }
    return value;
  }
}
