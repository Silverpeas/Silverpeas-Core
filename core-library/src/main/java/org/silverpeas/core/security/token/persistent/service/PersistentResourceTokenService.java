/*
 * Copyright (C) 2000 - 2013 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have recieved a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.silverpeas.core.security.token.persistent.service;

import org.silverpeas.core.persistence.EntityReference;
import org.silverpeas.core.security.token.exception.TokenException;
import org.silverpeas.core.security.token.persistent.PersistentResourceToken;
import org.silverpeas.core.security.token.exception.TokenException;
import org.silverpeas.core.security.token.persistent.PersistentResourceToken;

/**
 * A service on the persistent token for Silverpeas entities. It wraps the mechanism to compute and
 * to retrieve a token for a given resource handled in Silverpeas.
 *
 * @author Yohann Chastagnier
 */
public interface PersistentResourceTokenService {

  /**
   * Initializes the token of the resource referred by the specified {@link EntityReference}. If the
   * resource has already a token, this token is then renewed.
   *
   * @param resource a reference to the resource for which a token will be initialized.
   * @return a token persisted for the specified resource.
   * @throws TokenException if an unexpected error occurs while initializing a token.
   */
  PersistentResourceToken initialize(EntityReference resource) throws TokenException;

  /**
   * Gets the token of the resource referred by the specified {@link EntityReference}. If no token
   * exists for the resource, then {@link PersistentResourceToken#NoneToken} is returned.
   *
   * @param resource a reference to the resource for which a token will be initialized.
   * @return either the token associated with the specified resource or NoneToken if no such token
   * exists.
   */
  PersistentResourceToken get(final EntityReference resource);

  /**
   * Gets the token with the specified value from the data source. If no token exists with a such
   * value, then {@link PersistentResourceToken#NoneToken} is returned.
   *
   * @param token the value of the token to get.
   * @return either the token with the specified value or NoneToken if no such token exists.
   */
  PersistentResourceToken get(String token);

  /**
   * Removes quietly the token of the resource referred by the specified entity reference.
   *
   * @param resource the resource for which the token has to be removed.
   */
  void remove(final EntityReference resource);

  /**
   * Removes quietly the specified token.
   *
   * @param token the String representation of the token to remove.
   */
  void remove(final String token);
}
