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

package org.silverpeas.core.webapi.wopi;

import org.silverpeas.core.annotation.Bean;
import org.silverpeas.core.security.session.SessionInfo;
import org.silverpeas.core.util.ServiceProvider;
import org.silverpeas.core.wopi.WopiFile;
import org.silverpeas.core.wopi.WopiFileEditionManager;
import org.silverpeas.core.wopi.WopiUser;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.UriBuilder;
import java.util.Optional;

import static java.text.MessageFormat.format;
import static java.util.Optional.of;
import static javax.ws.rs.core.UriBuilder.fromUri;
import static org.silverpeas.core.security.session.SessionManagementProvider.getSessionManagement;
import static org.silverpeas.core.wopi.WopiLogger.logger;
import static org.silverpeas.core.wopi.WopiSettings.getWopiHostServiceBaseUrl;

/**
 * In charge of the management of WOPI contexts.
 * <p>
 * The exposed signatures permits to caller to initialize a WOPI edition.
 * </p>
 * <p>
 * WOPI services can use directly the implementation of this interface in order to get more
 * functionality.
 * </p>
 * @author silveryocha
 */
@Bean
public class WebWopiFileEdition {

  private static final String WOPI_SRC_PARAM = "WOPISrc";
  private static final String ACCESS_TOKEN_PARAM = "access_token";

  public static WebWopiFileEdition get() {
    return ServiceProvider.getService(WebWopiFileEdition.class);
  }

  protected WebWopiFileEdition() {
  }

  /**
   * Initializing a WOPI edition from given data.
   * @param request the current request from which the edition is started.
   * @param file a Silverpeas's WOPI file.
   * @return an optional URL of the Silverpeas's editor page.
   */
  public Optional<String> initializeWith(final HttpServletRequest request, final WopiFile file) {
    final String userSessionId = request.getSession(false).getId();
    final SessionInfo sessionInfo = getSessionManagement().getSessionInfo(userSessionId);
    return WopiFileEditionManager.get().prepareEditionWith(sessionInfo, file).flatMap(e -> {
      final WopiUser editionUser = e.getUser();
      final WopiFile editionFile = e.getFile();
      final String fileId = editionFile.id();
      final UriBuilder hostUriBuilder = fromUri(getWopiHostServiceBaseUrl()).path(fileId);
      final UriBuilder uriBuilder = fromUri(e.getClientBaseUrl())
          .queryParam(WOPI_SRC_PARAM, hostUriBuilder.build())
          .queryParam(ACCESS_TOKEN_PARAM, editionUser.getAccessToken());
      final String clientUrl = uriBuilder.build().toString();
      logger().debug(() -> format(
          "from {0} initializing WOPI edition for {1} and for user {2} with WOPI client URL {3}",
          userSessionId, editionFile, editionUser, clientUrl));
      return of(clientUrl).map(u -> {
        request.setAttribute("WopiClientUrl", u);
        request.setAttribute("WopiUser", editionUser);
        request.setAttribute("WopiFile", editionFile);
        return "/media/jsp/wopi/editor.jsp";
      });
    });
  }
}
