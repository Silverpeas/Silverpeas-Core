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
import java.util.Arrays;
import java.util.Collection;
import java.util.HexFormat;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Validator of the source referred by a resource embedded within a content: an iframe, a script,
 * but also an image, a video or an audio. A source is allowed only if it is either an HTTPS URL on
 * one of the allowed hosts or a relative URL within the Silverpeas application or within the
 * weblib one, which serves beside Silverpeas the resources specific to the platform. A relative
 * URL must not attempt to go up in the paths (no ".." sequence) and, when it is an absolute path,
 * it must start with the path of one of these two applications. In other terms, the resources the
 * platform hosts itself are allowed, whereas the external ones have to be explicitly declared.
 * <p>
 * Among the resources Silverpeas hosts, the files attached to the contributions are particular:
 * they are uploaded by the users themselves. Loaded as a script, such a file would run in the
 * browser of the readers of a content, and in their name, a code nobody has checked. So a
 * validator can be asked to reject the sources served by the file servers of Silverpeas, which is
 * expected for the scripts. The iframes aren't concerned: the document they embed is served by
 * those file servers along with a content security policy forbidding it any script, so that a
 * PDF document or an HTML page attached to a contribution can be safely embedded.
 * </p>
 * <p>
 * This rule is shared by the two ends at which such resources are taken in charge: the filtering
 * of the incoming requests, which rejects the contents embedding a non-allowed iframe or script,
 * and the sanitization of the contents being rendered or exported, which drops the non-allowed
 * resources. Both have hence to agree on what an allowed source is.
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
  private static final String WEBLIB_PATH = "/weblib";

  /**
   * The routes, relative to the Silverpeas application, by which the files attached to the
   * contributions are served: by their own URL, by their permalink which redirects to it, or by
   * the other file servers.
   */
  private static final Set<String> FILE_SERVERS = Set.of("attached_file", "File", "Document",
      "FileServer", "OnlineFileServer", "TempFileServer");
  /**
   * The characters forbidden by the URI syntax that a browser percent-encodes before sending the
   * request, without any further interpretation.
   */
  private static final String CHARACTERS_ENCODED_BY_BROWSERS = " \"<>[]^`{|}";

  private final Set<String> allowedHosts;
  private final String applicationPath;
  private final boolean attachedFilesAllowed;

  /**
   * Constructs a validator accepting only the sources referring the specified hosts, the
   * specified application or the weblib one.
   * @param allowedHosts the hosts from which a resource can be embedded within a content. The
   * {@link #ANY_HOST} value allows them all.
   * @param applicationPath the path of the Silverpeas application (for example /silverpeas).
   */
  public EmbeddedSourceValidator(final Collection<String> allowedHosts,
      final String applicationPath) {
    this(allowedHosts, applicationPath, true);
  }

  /**
   * Constructs a validator accepting only the sources referring the specified hosts, the
   * specified application or the weblib one, and accepting or not the files attached to the
   * contributions among them.
   * @param allowedHosts the hosts from which a resource can be embedded within a content. The
   * {@link #ANY_HOST} value allows them all.
   * @param applicationPath the path of the Silverpeas application (for example /silverpeas).
   * @param attachedFilesAllowed is a file attached to a contribution an allowed source? It
   * shouldn't for a script, unless the users are all trusted.
   */
  public EmbeddedSourceValidator(final Collection<String> allowedHosts,
      final String applicationPath, final boolean attachedFilesAllowed) {
    this.allowedHosts = allowedHosts.stream()
        .map(h -> h.toLowerCase(Locale.ROOT))
        .collect(Collectors.toSet());
    this.applicationPath = TRAILING_SLASHES_PATTERN.matcher(applicationPath).replaceFirst("");
    this.attachedFilesAllowed = attachedFilesAllowed;
  }

  /**
   * Is the specified source allowed to be referred by an embedded resource?
   * <p>
   * The source is read the way a browser reads it. In particular, a browser accepts a URL with
   * some characters the URI syntax forbids, a whitespace for example, by encoding them before
   * sending the request: the name of a file is commonly made of such characters. So, when the
   * source isn't a well-formed URI, it is checked as the browser would send it. The backslashes
   * and the control characters aren't encoded, because a browser gives them a meaning of their
   * own (a backslash is read as a slash and the tabulations and line breaks are removed), which
   * would make the validation disagree with the URL the browser actually resolves.
   * </p>
   * @param src the value of the src attribute, with any HTML entity already decoded.
   * @return true if the source can be embedded, false otherwise and in particular if it is null.
   */
  public boolean isAllowed(final String src) {
    if (src == null) {
      return false;
    }
    try {
      return isAllowed(new URI(src));
    } catch (URISyntaxException e) {
      return isAllowedAsSentByBrowsers(src);
    }
  }

  private boolean isAllowedAsSentByBrowsers(final String src) {
    final String encoded = src.chars()
        .mapToObj(c -> CHARACTERS_ENCODED_BY_BROWSERS.indexOf(c) < 0 ? String.valueOf((char) c) :
            "%" + HexFormat.of().withUpperCase().toHexDigits((byte) c))
        .collect(Collectors.joining());
    try {
      return isAllowed(new URI(encoded));
    } catch (URISyntaxException e) {
      return false;
    }
  }

  private boolean isAllowed(final URI uri) {
    final boolean relative = uri.getScheme() == null && uri.getRawAuthority() == null;
    final boolean allowed = relative ? isAllowedRelativeURI(uri) : isAllowedAbsoluteURI(uri);
    return allowed && (attachedFilesAllowed || !isAttachedFile(uri));
  }

  private boolean isAllowedAbsoluteURI(final URI uri) {
    return HTTPS_SCHEME.equalsIgnoreCase(uri.getScheme()) && uri.getHost() != null &&
        (allowedHosts.contains(ANY_HOST) ||
            allowedHosts.contains(uri.getHost().toLowerCase(Locale.ROOT)));
  }

  private boolean isAllowedRelativeURI(final URI uri) {
    // the path and the query are here decoded, so any encoded path traversal is also detected
    final String path = uri.getPath();
    final String query = Objects.toString(uri.getQuery(), "");
    final boolean inApplication =
        !path.startsWith("/") || isWithin(applicationPath, path) || isWithin(WEBLIB_PATH, path);
    return inApplication && !path.contains(PATH_TRAVERSAL) && !query.contains(PATH_TRAVERSAL);
  }

  private static boolean isWithin(final String applicationPath, final String path) {
    return path.equals(applicationPath) || path.startsWith(applicationPath + "/");
  }

  private boolean isAttachedFile(final URI uri) {
    final String path = Objects.toString(uri.getPath(), "");
    final List<String> segments = segmentsOf(path);
    // a relative path is resolved against the one of the page rendering the content
    final boolean relativePath =
        uri.getScheme() == null && uri.getRawAuthority() == null && !path.startsWith("/");
    final List<String> application = relativePath ? List.of() : segmentsOf(applicationPath);
    final int route = application.size();
    return segments.size() > route && segments.subList(0, route).equals(application) &&
        FILE_SERVERS.contains(segments.get(route));
  }

  /**
   * Gets the segments of the specified path the way the server reads them to find out the
   * resource to serve: without the parameters a segment can carry, and without the empty and the
   * dot ones.
   */
  private static List<String> segmentsOf(final String path) {
    return Arrays.stream(path.split("/"))
        .map(s -> s.contains(";") ? s.substring(0, s.indexOf(';')) : s)
        .filter(s -> !s.isEmpty() && !".".equals(s))
        .collect(Collectors.toList());
  }
}
