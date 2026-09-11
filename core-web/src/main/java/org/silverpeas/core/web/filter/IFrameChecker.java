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
package org.silverpeas.core.web.filter;

import org.apache.commons.text.StringEscapeUtils;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Checks the iframe elements present in a given text. An iframe is allowed only if it is a
 * well-formed opening tag whose single src attribute refers either an HTTPS URL on one of the
 * allowed hosts or a relative URL within the Silverpeas application. A relative URL must not
 * attempt to go up in the paths (no ".." sequence) and, when it is an absolute path, it must
 * start with the application path. Any iframe that cannot be strictly parsed is rejected, as are
 * the iframes with a srcdoc attribute (which would take precedence over the src one). The closing
 * tags are harmless and then always accepted.
 *
 * @author mmoquillon
 */
final class IFrameChecker {

  private static final Pattern IFRAME_PATTERN = Pattern.compile("(?i)<[\\s/]*iframe");
  private static final Pattern IFRAME_OPENING_PATTERN = Pattern.compile("(?i)<iframe");
  private static final Pattern ATTRIBUTE_PATTERN = Pattern.compile(
      "\\G\\s+([^\\s\"'>/=]+)(?:\\s*=\\s*(?:\"([^\"]*)\"|'([^']*)'|([^\\s\"'=<>`]+)))?");
  private static final Pattern TAG_END_PATTERN = Pattern.compile("\\G\\s*/?>");
  // the lookbehind and the possessive quantifier avoid any backtracking
  private static final Pattern TRAILING_SLASHES_PATTERN = Pattern.compile("(?<!/)/++$");
  private static final String SRC = "src";
  private static final String SRCDOC = "srcdoc";

  private static final String PATH_TRAVERSAL = "..";

  private final Set<String> allowedHosts;
  private final String applicationPath;

  /**
   * Constructs a checker of iframes accepting only those referring the specified hosts or
   * the specified application.
   *
   * @param allowedHosts the hosts from which the content can be embedded within an iframe.
   * @param applicationPath the path of the Silverpeas application (for example /silverpeas).
   */
  IFrameChecker(final Collection<String> allowedHosts, final String applicationPath) {
    this.allowedHosts = allowedHosts.stream()
        .map(h -> h.toLowerCase(Locale.ROOT))
        .collect(Collectors.toSet());
    this.applicationPath = TRAILING_SLASHES_PATTERN.matcher(applicationPath).replaceFirst("");
  }

  /**
   * Are all the iframes in the given text allowed?
   *
   * @param text the text to check.
   * @return true if there is no iframe in the text or if all of them are allowed. False otherwise.
   */
  boolean areAllAllowedIn(final String text) {
    final Matcher iframe = IFRAME_PATTERN.matcher(text);
    while (iframe.find()) {
      final boolean isClosingTag = iframe.group().contains("/");
      if (!isClosingTag && !isAllowed(text, iframe.start())) {
        return false;
      }
    }
    return true;
  }

  private boolean isAllowed(final String text, final int tagStart) {
    final Matcher opening = IFRAME_OPENING_PATTERN.matcher(text).region(tagStart, text.length());
    if (!opening.lookingAt()) {
      return false;
    }
    final Map<String, String> attributes = new HashMap<>();
    final Matcher attribute = ATTRIBUTE_PATTERN.matcher(text).region(opening.end(), text.length());
    int position = opening.end();
    while (attribute.find()) {
      final String name = attribute.group(1).toLowerCase(Locale.ROOT);
      if (attributes.containsKey(name)) {
        return false;
      }
      attributes.put(name, valueOf(attribute));
      position = attribute.end();
    }
    final Matcher end = TAG_END_PATTERN.matcher(text).region(position, text.length());
    return end.lookingAt() && !attributes.containsKey(SRCDOC) &&
        isAllowedSource(attributes.get(SRC));
  }

  private static String valueOf(final Matcher attribute) {
    for (int group = 2; group <= 4; group++) {
      if (attribute.group(group) != null) {
        return attribute.group(group);
      }
    }
    return "";
  }

  private boolean isAllowedSource(final String src) {
    if (src == null) {
      return false;
    }
    try {
      final URI uri = new URI(StringEscapeUtils.unescapeHtml4(src));
      if (uri.getScheme() == null && uri.getRawAuthority() == null) {
        return isAllowedRelativeURI(uri);
      }
      return "https".equalsIgnoreCase(uri.getScheme()) && uri.getHost() != null &&
          allowedHosts.contains(uri.getHost().toLowerCase(Locale.ROOT));
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
