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

import java.util.Optional;

/**
 * In charge of the management of Web Browser Edition (WBE) clients (LibreOffice Online for example
 * which uses WOPI protocol).
 * <p>
 * Web Browser Edition of Silverpeas deals with this interface in order to manage properly the
 * data about an edition according to a client. For each client, or dor each client protocol,
 * this interface MUST be implemented.
 * </p>
 * @author silveryocha
 */
public interface WbeClientManager {

  /**
   * Clears all the context of the Web Browser Edition.
   */
  void clear();

  /**
   * Indicates if the WBE edition is enabled.
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
   * If the client is enabled at Silverpeas's side and if the file is handled by the client, a {@link WbeEdition}
   * instance is returned. It contains the necessary data to initialize an edition from the WEB
   * services.
   * </p>
   * @param user a WBE user.
   * @param file a Silverpeas's WBE file.
   * @param <T> a {@link WbeEdition} type.
   * @return an optional {@link WbeEdition} instance.
   */
  <T extends WbeEdition> Optional<T> prepareEditionWith(final WbeUser user, WbeFile file);

  /**
   * Provides the URL that permits to access the administration of Web Browser Edition client.
   * @return an optional URL as string.
   */
  Optional<String> getAdministrationUrl();
}
