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

package org.silverpeas.core.wbe;

import org.silverpeas.core.security.session.SilverpeasUserSession;
import org.silverpeas.core.util.ServiceProvider;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiFunction;

/**
 * In charge of the management of Web Browser Edition contexts.
 * <p>
 * Web Browser Edition services (WBE services) can use directly the implementation of this interface in order to use the provided
 * functionalities.
 * </p>
 * @author silveryocha
 */
public interface WbeHostManager {

  static WbeHostManager get() {
    return ServiceProvider.getService(WbeHostManager.class);
  }

  /**
   * Clears all the context of the Web Browser Edition.
   * <p>
   *   Client implementations will also be cleared.
   * </p>
   */
  void clear();

  /**
   * Provides the URL that permits to access the administration of Web Browser Edition client.
   * @return an optional URL as string.
   */
  Optional<String> getClientAdministrationUrl();

  /**
   * Notifies the manager of the number of users which are editing the given file at an instant.
   * @param file a {@link WbeFile} a file.
   * @param userIds set of WBE user identifiers.
   */
  void notifyEditionWith(final WbeFile file, final Set<String> userIds);

  /**
   * Gets the list of file edited by the given user.
   * @param user a {@link WbeUser} instance.
   * @return a list of {@link WbeFile}.
   */
  List<WbeFile> getEditedFilesBy(final WbeUser user);

  /**
   * Gets the list of users which are editor of the given file.
   * @param file a {@link WbeFile} instance.
   * @return a list of {@link WbeUser}.
   */
  List<WbeUser> getEditorsOfFile(final WbeFile file);

  /**
   * Enables the services.
   * <p>
   * This usable only in the case of WBE is enable by setting property file.<br/>
   * When enabled by setting file, it is possible the enable/disable the services in order to get
   * as fast as possible control on WBE exchanges.
   * </p>
   * @param enable true to enable, false to disable.
   */
  void enable(final boolean enable);

  /**
   * Indicates if the Web Browser Edition is enabled.
   * @return true if enabled, false otherwise.
   */
  boolean isEnabled();

  /**
   * Indicates if given {@link WbeFile} is handled by WBE client.
   * @param file the WBE file to check.
   * @return true if handled, false otherwise.
   */
  boolean isHandled(WbeFile file);

  /**
   * Prepares a WBE edition from given data.
   * <p>
   * If WBE is enabled and if the file is handled by the WBE client, a {@link WbeEdition}
   * instance is returned. It contains the necessary data to initialize an edition from the WEB
   * services.
   * </p>
   * @param spUserSession the silverpeas session from which the edition is needed.
   * @param file a Silverpeas's WBE file.
   * @param <T> a {@link WbeEdition} type.
   * @return an optional {@link WbeEdition} instance.
   */
  <T extends WbeEdition> Optional<T> prepareEditionWith(SilverpeasUserSession spUserSession, WbeFile file);

  /**
   * Gets the edition context from a WBE file identifier and an access token.
   * <p>
   * If no file exists into context against the given file identifier, then the given optional
   * WBE file to context initializer is empty.
   * </p>
   * <p>
   * If no user exists into context against the given access token, then the given optional WBE
   * user to context initializer is empty.
   * </p>
   * @param fileId a file identifier as string.
   * @param accessToken an access token as string.
   * @param contextInitializer the context initializer.
   * @param <R> the type of the context.
   * @return an initialized context.
   */
  <R> R getEditionContextFrom(final String fileId, final String accessToken,
      BiFunction<Optional<WbeUser>, Optional<WbeFile>, R> contextInitializer);

  /**
   * Revokes from the WBE context the given WBE user.
   * <p>
   * The revocation is done from instance data and not directly from the instance reference.
   * </p>
   * @param user a {@link WbeUser} instance.
   */
  void revokeUser(final WbeUser user);

  /**
   * Revokes from the WBE edition context the given WBE file.
   * <p>
   * The revocation is done from instance data and not directly from the instance reference.
   * </p>
   * @param file a {@link WbeFile} instance.
   */
  void revokeFile(final WbeFile file);

  /**
   * Gets the list of WBE users from the context.
   * @return a list of {@link WbeUser} instances.
   */
  List<WbeUser> listCurrentUsers();

  /**
   * Gets the list of WBE files from the context.
   * @return a list of {@link WbeFile} instances.
   */
  List<WbeFile> listCurrentFiles();
}
