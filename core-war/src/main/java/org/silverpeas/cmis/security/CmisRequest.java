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
 * FLOSS exception. You should have received a copy of the text describing
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

package org.silverpeas.cmis.security;

import org.silverpeas.core.util.StringUtil;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * A wrapper of an HTTP request decorating it to support additional features.
 * @author mmoquillon
 */
public class CmisRequest extends HttpServletRequestWrapper {

  private final Map<String, String> headers = new HashMap<>();

  public static CmisRequest decorate(final HttpServletRequest request) {
    return new CmisRequest(request);
  }

  /**
   * Constructs a request object wrapping the given request.
   * @param request
   * @throws IllegalArgumentException if the request is null
   */
  private CmisRequest(final HttpServletRequest request) {
    super(request);
    final Enumeration<String> headerNames = request.getHeaderNames();
    while(headerNames.hasMoreElements()) {
      final String currentHeaderName = headerNames.nextElement();
      headers.put(currentHeaderName, request.getHeader(currentHeaderName));
    }
  }

  @Override
  public String getHeader(final String name) {
    return headers.get(name);
  }

  /**
   * Adds an HTTP header to the request.
   * @param name the name of the header.
   * @param value the value of the header.
   */
  public void addHeader(final String name, final String value) {
    Objects.requireNonNull(name);
    Objects.requireNonNull(value);
    headers.put(name, value);
  }

  @Override
  public Enumeration<String> getHeaderNames() {
    return Collections.enumeration(headers.keySet());
  }

  @Override
  public long getDateHeader(final String name) {
    final String value = this.getHeader(name);
    return value != null ? Long.parseLong(value) : 0L;
  }

  @Override
  public int getIntHeader(final String name) {
    final String value = this.getHeader(name);
    return value != null ? Integer.parseInt(value) : 0;
  }

  @Override
  public Enumeration<String> getHeaders(final String name) {
    final String value = this.getHeader(name);
    if (StringUtil.isNotDefined(value)) {
      return Collections.emptyEnumeration();
    }
    final List<String> values =
        Stream.of(value.split(",")).map(String::trim).collect(Collectors.toList());
    return Collections.enumeration(values);
  }
}
  