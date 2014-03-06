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
package com.silverpeas.web.mock;

import javax.inject.Named;
import org.silverpeas.EntityReference;
import org.silverpeas.token.exception.TokenException;
import org.silverpeas.token.persistent.PersistentResourceToken;
import org.silverpeas.token.persistent.service.PersistentResourceTokenService;

import static org.mockito.Mockito.mock;

/**
 * @author Yohann Chastagnier
 */
@Named("persistentResourceTokenService")
public class TokenServiceMockWrapper implements PersistentResourceTokenService {

  private final PersistentResourceTokenService mock;

  /**
   * Default constructor into which mock is initialized
   */
  public TokenServiceMockWrapper() {
    mock = mock(PersistentResourceTokenService.class);
  }

  /**
   * Gets the mock
   *
   * @return
   */
  public PersistentResourceTokenService getTokenServiceMock() {
    return mock;
  }

  @Override
  public PersistentResourceToken initialize(final EntityReference ref) throws TokenException {
    return mock.initialize(ref);
  }

  @Override
  public PersistentResourceToken get(final EntityReference ref) {
    final PersistentResourceToken token = mock.get(ref);
    return (token == null) ? PersistentResourceToken.NoneToken : token;
  }

  @Override
  public PersistentResourceToken get(String tokenValue) {
    final PersistentResourceToken token = mock.get(tokenValue);
    return (token == null) ? PersistentResourceToken.NoneToken : token;
  }

  @Override
  public void remove(final EntityReference ref) {
    mock.remove(ref);
  }

  @Override
  public void remove(String tokenValue) {
    mock.remove(tokenValue);
  }
}
