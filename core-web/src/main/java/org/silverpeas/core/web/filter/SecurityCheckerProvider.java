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

import org.silverpeas.core.util.URLUtil;
import org.silverpeas.core.util.security.SecuritySettings;

import javax.servlet.http.HttpServletRequest;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import static java.util.Optional.ofNullable;

/**
 * Provider of the security checkers against which the values of an incoming request are checked.
 * It is the only one to know the existing implementations of {@link SecurityChecker} and how to
 * set them up for a given request: a new checker has just to be added here to be applied.
 *
 * @author mmoquillon
 */
final class SecurityCheckerProvider {

  /**
   * Gets the security checkers to apply to the values of the specified request, in the order they
   * have to be consulted, which fixes the reason told when several of them reject a value. The
   * server the request is addressed to is always among the hosts allowed to be referred by an
   * iframe or by a script.
   *
   * @param request the incoming request.
   * @return the security checkers.
   */
  List<SecurityChecker> getCheckersFor(final HttpServletRequest request) {
    final String applicationPath = URLUtil.getApplicationURL();
    final String serverHost = URI.create(URLUtil.getServerURL(request)).getHost();
    return List.of(new XssPatternChecker(),
        new IFrameChecker(withServerHost(SecuritySettings.getAllowedHostsForIFrame(), serverHost),
            applicationPath),
        new ScriptChecker(withServerHost(SecuritySettings.getAllowedHostsForScript(), serverHost),
            applicationPath, SecuritySettings.areScriptsFromAttachedFilesAllowed()));
  }

  private static List<String> withServerHost(final List<String> hosts, final String serverHost) {
    final List<String> allowedHosts = new ArrayList<>(hosts);
    ofNullable(serverHost).ifPresent(allowedHosts::add);
    return allowedHosts;
  }
}
