/*
 * Copyright (C) 2000 - 2021 Silverpeas
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
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.silverpeas.core.webapi.wbe;

import org.silverpeas.core.annotation.Bean;
import org.silverpeas.core.security.session.SessionInfo;
import org.silverpeas.core.util.ServiceProvider;
import org.silverpeas.core.wbe.WbeEdition;
import org.silverpeas.core.wbe.WbeFile;
import org.silverpeas.core.wbe.WbeHostManager;

import javax.servlet.http.HttpServletRequest;
import java.util.Optional;

import static org.silverpeas.core.security.session.SessionManagementProvider.getSessionManagement;

/**
 * In charge of the management of WBE contexts.
 * <p>
 * The exposed signatures permits to caller to initialize a WBE edition.
 * </p>
 * <p>
 * WBE services can use directly the implementation of this interface in order to get more
 * functionality.
 * </p>
 * @author silveryocha
 */
@Bean
public class WbeFileEdition {

  public static final String ACCESS_TOKEN_PARAM = "access_token";

  public static WbeFileEdition get() {
    return ServiceProvider.getService(WbeFileEdition.class);
  }

  protected WbeFileEdition() {
  }

  /**
   * Initializing a WBE edition from given data.
   * @param request the current request from which the edition is started.
   * @param file a Silverpeas's WBE file.
   * @return an optional URL of the Silverpeas's editor page.
   */
  @SuppressWarnings("unchecked")
  public Optional<String> initializeWith(final HttpServletRequest request, final WbeFile file) {
    final String userSessionId = request.getSession(false).getId();
    final SessionInfo sessionInfo = getSessionManagement().getSessionInfo(userSessionId);
    return WbeHostManager.get().prepareEditionWith(sessionInfo, file).flatMap(e ->
        ServiceProvider.getAllServices(ClientRequestDispatcher.class).stream()
            .filter(d -> d.canHandle(e))
            .findFirst()
            .flatMap(d -> d.dispatch(request, e)));
  }

  public interface ClientRequestDispatcher {
    <E extends WbeEdition> boolean canHandle(final E edition);
    <T extends WbeEdition> Optional<String> dispatch(final HttpServletRequest request, final T edition);
  }
}
