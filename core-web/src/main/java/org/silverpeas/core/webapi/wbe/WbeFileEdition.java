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

package org.silverpeas.core.webapi.wbe;

import org.silverpeas.kernel.exception.NotFoundException;
import org.silverpeas.core.annotation.Bean;
import org.silverpeas.core.security.session.SessionInfo;
import org.silverpeas.core.util.ServiceProvider;
import org.silverpeas.core.wbe.WbeEdition;
import org.silverpeas.core.wbe.WbeFile;
import org.silverpeas.core.wbe.WbeHostManager;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.Optional;
import java.util.function.Consumer;

import static java.util.Optional.ofNullable;
import static java.util.function.Predicate.not;
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
  private static final Consumer<WbeEdition.Configuration> NO_ADDITIONAL_CONFIGURATION = c -> {
  };

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
  public Optional<String> initializeWith(final HttpServletRequest request, final WbeFile file) {
    return initializeWith(request, file, NO_ADDITIONAL_CONFIGURATION);
  }

  /**
   * Initializing a WBE edition from given data.
   * @param request the current request from which the edition is started.
   * @param file a Silverpeas's WBE file.
   * @param configSetter permits to configure some details of the edition.
   * @return an optional URL of the Silverpeas's editor page.
   */
  public Optional<String> initializeWith(final HttpServletRequest request, final WbeFile file,
      final Consumer<WbeEdition.Configuration> configSetter) {
    return WbeHostManager.get()
        .prepareEditionWith(ofNullable(request.getSession(false))
            .map(HttpSession::getId)
            .map(getSessionManagement()::getSessionInfo)
            .filter(not(SessionInfo::isAnonymous))
            .orElseThrow(() -> new NotFoundException("User Session does not exist")), file)
        .flatMap(e ->
            ServiceProvider.getAllServices(ClientRequestDispatcher.class).stream()
                .filter(d -> d.canHandle(e))
                .findFirst()
                .flatMap(d -> {
                  configSetter.accept(e.getConfiguration());
                  return d.dispatch(request, e);
                })
        );
  }

  public interface ClientRequestDispatcher {
    <E extends WbeEdition> boolean canHandle(final E edition);
    <T extends WbeEdition> Optional<String> dispatch(final HttpServletRequest request, final T edition);
  }
}
