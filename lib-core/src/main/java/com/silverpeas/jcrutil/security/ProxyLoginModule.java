/**
 * Copyright (C) 2000 - 2009 Silverpeas
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
 * "http://repository.silverpeas.com/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.silverpeas.jcrutil.security;

import java.util.Map;

import javax.security.auth.Subject;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.login.LoginException;
import javax.security.auth.spi.LoginModule;

import com.silverpeas.jcrutil.security.impl.RepositoryHelper;

public class ProxyLoginModule implements LoginModule {
  private LoginModule realModule;

  public ProxyLoginModule() {
    this.realModule = RepositoryHelper.getJcrLoginModule();
  }

  public boolean abort() throws LoginException {
    return realModule.abort();
  }

  public boolean commit() throws LoginException {
    return realModule.commit();
  }

  @SuppressWarnings("unchecked")
  public void initialize(Subject subject, CallbackHandler callbackHandler,
      Map sharedState, Map options) {
    realModule.initialize(subject, callbackHandler, sharedState, options);
  }

  public boolean login() throws LoginException {
    return realModule.login();
  }

  public boolean logout() throws LoginException {
    return realModule.logout();
  }

}
