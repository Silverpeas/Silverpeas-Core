/*
 * Copyright (C) 2000 - 2022 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Lib
 * Open Source Software ("FLOSS") applications as described in Silverpeas
 * FLOSS exception. You should have received a copy of the text describin
 * the FLOSS exception, and it is also available here:
 * "https://www.silverpeas.org/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public Licen
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 *
 */

package org.silverpeas.web.filter;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import java.io.IOException;

import static java.util.Optional.ofNullable;
import static java.util.function.Predicate.not;

/**
 * Filter that allows one to specify a character encoding for requests. This is useful because
 * current browsers typically do not set a character encoding even if specified in the HTML page or
 * form.
 *
 * <p>This filter can either apply its encoding if the request does not already
 * specify an encoding, or enforce this filter's encoding in any case ("forceEncoding"="true"). In
 * the latter case, the encoding will also be applied as default response encoding (although this
 * will usually be overridden by a full content type set in the view).
 * @author mmoquillon
 */
public class CharsetFilter implements Filter {
  private String encoding;
  private boolean forceEncoding;

  @Override
  public void init(final FilterConfig config) throws ServletException {
    encoding = ofNullable(config.getInitParameter("encoding"))
        .filter(not(String::isBlank)).orElse("UTF-8");
    forceEncoding = Boolean.parseBoolean(
        ofNullable(config.getInitParameter("forceEncoding"))
            .filter(not(String::isBlank)).orElse("false"));
  }

  @Override
  public void doFilter(final ServletRequest request, final ServletResponse response,
      final FilterChain chain)
      throws IOException, ServletException {
    if (forceEncoding || request.getCharacterEncoding() == null) {
      request.setCharacterEncoding(encoding);
    }
    if (forceEncoding) {
      response.setCharacterEncoding(encoding);
    }
    chain.doFilter(request, response);
  }
}
