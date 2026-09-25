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
package org.silverpeas.core.security.html;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Validator of the source referred by a resource embedded within a content: an iframe, but also an
 * image, a video or an audio. A source is allowed only if it is either an HTTPS URL on one of the
 * allowed hosts or a relative URL within the Silverpeas application. A relative URL must not
 * attempt to go up in the paths (no ".." sequence) and, when it is an absolute path, it must start
 * with the application path. In other terms, the resources Silverpeas hosts itself are allowed,
 * whereas the external ones have to be explicitly declared.
 * <p>
 * This rule is shared by the two ends at which such resources are taken in charge: the filtering
 * of the incoming requests, which rejects the contents embedding a non-allowed iframe, and the
 * sanitization of the contents being rendered or exported, which drops the non-allowed resources.
 * Both have hence to agree on what an allowed source is.
 * </p>
 * @author mmoquillon
 */
public final class EmbeddedSourceValidator {

  /**
   * The value declaring any host as being allowed, whatever it is.
   */
  public static final String ANY_HOST = "*";

  // the lookbehind and the possessive quantifier avoid any backtracking
  private static final Pattern TRAILING_SLASHES_PATTERN = Pattern.compile("(?<!/)/++$");
  private static final String PATH_TRAVERSAL = "..";
  private static final String HTTPS_SCHEME = "https";

  private final Set<String> allowedHosts;
  private final String applicationPath;

  /**
   * Constructs a validator accepting only the sources referring the specified hosts or the
   * specified application.
   * @param allowedHosts the hosts from which a resource can be embedded within a content. The
   * {@link #ANY_HOST} value allows them all.
   * @param applicationPath the path of the Silverpeas application (for example /silverpeas).
   */
  public EmbeddedSourceValidator(final Collection<String> allowedHosts,
      final String applicationPath) {
    this.allowedHosts = allowedHosts.stream()
        .map(h -> h.toLowerCase(Locale.ROOT))
        .collect(Collectors.toSet());
    this.applicationPath = TRAILING_SLASHES_PATTERN.matcher(applicationPath).replaceFirst("");
  }

  /**
   * Is the specified source allowed to be referred by an embedded resource?
   * @param src the value of the src attribute, with any HTML entity already decoded.
   * @return true if the source can be embedded, false otherwise and in particular if it is null.
   */
  public boolean isAllowed(final String src) {
    if (src == null) {
      return false;
    }
    try {
      final URI uri = new URI(src);
      if (uri.getScheme() == null && uri.getRawAuthority() == null) {
        return isAllowedRelativeURI(uri);
      }
      return HTTPS_SCHEME.equalsIgnoreCase(uri.getScheme()) && uri.getHost() != null &&
          (allowedHosts.contains(ANY_HOST) ||
              allowedHosts.contains(uri.getHost().toLowerCase(Locale.ROOT)));
    } catch (URISyntaxException e) {
      return false;
    }
  }

  private boolean isAllowedRelativeURI(final URI uri) {
    // the path and the query are here decoded, so any encoded path traversal is also detected
    final String path = uri.getPath();
    final String query = Objects.toString(uri.getQuery(), "");
    final boolean inApplication = !path.startsWith("/") || path.equals(applicationPath) ||
        path.startsWith(applicationPath + "/");
    return inApplication && !path.contains(PATH_TRAVERSAL) && !query.contains(PATH_TRAVERSAL);
  }
}
