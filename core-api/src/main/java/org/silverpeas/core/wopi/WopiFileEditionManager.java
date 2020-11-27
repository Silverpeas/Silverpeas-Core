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

package org.silverpeas.core.wopi;

import org.silverpeas.core.security.session.SilverpeasUserSession;
import org.silverpeas.core.util.ServiceProvider;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiFunction;

/**
 * In charge of the management of WOPI contexts.
 * <p>
 * WOPI services can use directly the implementation of this interface in order to use the provided
 * functionalities.
 * </p>
 * @author silveryocha
 */
public interface WopiFileEditionManager {

  static WopiFileEditionManager get() {
    return ServiceProvider.getService(WopiFileEditionManager.class);
  }

  /**
   * Notifies the manager of the number of users which are editing the given file at an instant.
   * @param file a {@link WopiFile} a file.
   * @param userIds set of wopi user identifiers.
   */
  void notifyEditionWith(final WopiFile file, final Set<String> userIds);

  /**
   * Gets the list of file edited by the given user.
   * @param user a {@link WopiUser} instance.
   * @return a list of {@link WopiFile}.
   */
  List<WopiFile> getEditedFilesBy(final WopiUser user);

  /**
   * Gets the list of users which are editor of the given file.
   * @param file a {@link WopiFile} instance.
   * @return a list of {@link WopiUser}.
   */
  List<WopiUser> getEditorsOfFile(final WopiFile file);

  /**
   * Enables the services.
   * <p>
   * This usable only in the case of WOPI is enable by setting property file.<br/>
   * When enabled by setting file, it is possible the enable/disable the services in order to get
   * as fast as possible control on WOPI exchanges.
   * </p>
   * @param enable true to enable, false to disable.
   */
  void enable(final boolean enable);

  /**
   * Indicates if the WOPI edition is enabled.
   * @return true if enabled, false otherwise.
   */
  boolean isEnabled();

  /**
   * Indicates if given {@link WopiFile} is handled by WOPI client.
   * @param file the wopi file to check.
   * @return true if handled, false otherwise.
   */
  boolean isHandled(WopiFile file);

  /**
   * Prepares a WOPI edition from given data.
   * <p>
   * If WOPI is enabled and if the file is handled by the WOPI client, a {@link WopiEdition}
   * instance is returned. It contains the necessary data to initialize an edition from the WEB
   * services.
   * </p>
   * @param spUserSession the silverpeas session from which the edition is needed.
   * @param file a Silverpeas's WOPI file.
   * @return an optional {@link WopiEdition} instance.
   */
  Optional<WopiEdition> prepareEditionWith(SilverpeasUserSession spUserSession, WopiFile file);

  /**
   * Gets the edition context from a WOPI file identifier and an access token.
   * <p>
   * If no file exists into context against the given file identifier, then the given optional
   * WOPI file to context initializer is empty.
   * </p>
   * <p>
   * If no user exists into context against the given access token, then the given optional WOPI
   * user to context initializer is empty.
   * </p>
   * @param fileId a file identifier as string.
   * @param accessToken an access token as string.
   * @param contextInitializer the context initializer.
   * @param <R> the type of the context.
   * @return an initialized context.
   */
  <R> R getEditionContextFrom(final String fileId, final String accessToken,
      BiFunction<Optional<WopiUser>, Optional<WopiFile>, R> contextInitializer);

  /**
   * Revokes from the WOPI edition context the given WOPI user.
   * <p>
   * The revocation is done from instance data and not directly from the instance reference.
   * </p>
   * @param user a {@link WopiUser} instance.
   */
  void revokeUser(final WopiUser user);

  /**
   * Revokes from the WOPI edition context the given WOPI file.
   * <p>
   * The revocation is done from instance data and not directly from the instance reference.
   * </p>
   * @param file a {@link WopiFile} instance.
   */
  void revokeFile(final WopiFile file);

  /**
   * Gets the list of WOPI users from the context.
   * @return a list of {@link WopiUser} instances.
   */
  List<WopiUser> listCurrentUsers();

  /**
   * Gets the list of WOPI files from the context.
   * @return a list of {@link WopiFile} instances.
   */
  List<WopiFile> listCurrentFiles();
}
