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
package com.silverpeas.jcrutil.security.impl;

import java.util.Map;

import javax.security.auth.Subject;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.security.auth.login.LoginException;
import javax.security.auth.spi.LoginModule;

import org.apache.jackrabbit.core.security.CredentialsCallback;
import org.apache.jackrabbit.core.security.SimpleLoginModule;

import com.silverpeas.jcrutil.security.impl.SilverpeasSystemCredentials;
import com.silverpeas.jcrutil.security.impl.SilverpeasSystemPrincipal;

public class BasicLoginModule implements LoginModule {
  private SimpleLoginModule module;
  private CallbackHandler callbackHandler;
  private Subject subject;
  private boolean isRoot = false;

  public boolean abort() throws LoginException {
    return this.module.abort();
  }

  public boolean commit() throws LoginException {
    if (isRoot) {
      subject.getPrincipals().add(new SilverpeasSystemPrincipal());
    }
    return this.module.commit();
  }

  public void initialize(Subject subject, CallbackHandler callbackHandler,
      Map sharedState, Map options) {
    this.module = new SimpleLoginModule();
    this.module.initialize(subject, callbackHandler, sharedState, options);
    this.callbackHandler = callbackHandler;
    this.subject = subject;
  }

  public boolean login() throws LoginException {
    try {
      CredentialsCallback ccb = new CredentialsCallback();
      callbackHandler.handle(new Callback[] { ccb });
      isRoot = (ccb.getCredentials() instanceof SilverpeasSystemCredentials);
      return isRoot || this.module.login();
    } catch (java.io.IOException ioe) {
      throw new LoginException(ioe.toString());
    } catch (UnsupportedCallbackException uce) {
      throw new LoginException(uce.getCallback().toString() + " not available");
    }

  }

  public boolean logout() throws LoginException {
    this.callbackHandler = null;
    return this.module.logout();
  }

}
