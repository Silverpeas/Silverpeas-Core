/*
 * Copyright (C) 2000 - 2016 Silverpeas
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

package com.silverpeas.jcrutil.security.impl;

import java.util.Map;
import javax.security.auth.Subject;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.security.auth.login.LoginException;
import javax.security.auth.spi.LoginModule;
import org.apache.jackrabbit.core.config.LoginModuleConfig;
import org.apache.jackrabbit.core.security.authentication.CredentialsCallback;
import org.apache.jackrabbit.core.security.simple.SimpleLoginModule;

public class BasicLoginModule implements LoginModule {

  private SimpleLoginModule module;
  private CallbackHandler callbackHandler;
  private Subject subject;
  private boolean isRoot = false;

  @Override
  public boolean abort() throws LoginException {
    return this.module.abort();
  }

  @Override
  public boolean commit() throws LoginException {
    if (isRoot) {
      subject.getPrincipals().add(new SilverpeasSystemPrincipal());
      return true;
    }
    return this.module.commit();
  }

  @Override
  public void initialize(Subject subject, CallbackHandler callbackHandler,
      Map sharedState, Map options) {
    if(! options.containsKey(LoginModuleConfig.PARAM_ANONYMOUS_ID)) {
      options.put(LoginModuleConfig.PARAM_ANONYMOUS_ID, "anonymous");
    }
    if(! options.containsKey(LoginModuleConfig.PARAM_ADMIN_ID)) {
      options.put(LoginModuleConfig.PARAM_ADMIN_ID, SilverpeasSystemPrincipal.SYSTEM);
    }
    this.module = new SimpleLoginModule();
    this.module.initialize(subject, callbackHandler, sharedState, options);
    this.callbackHandler = callbackHandler;
    this.subject = subject;
  }

  @Override
  public boolean login() throws LoginException {
    try {
      CredentialsCallback ccb = new CredentialsCallback();
      callbackHandler.handle(new Callback[]{ccb});
      isRoot = (ccb.getCredentials() instanceof SilverpeasSystemCredentials);
      return isRoot || this.module.login();
    } catch (java.io.IOException ioe) {
      throw new LoginException(ioe.toString());
    } catch (UnsupportedCallbackException uce) {
      throw new LoginException(uce.getCallback().toString() + " not available");
    }

  }

  @Override
  public boolean logout() throws LoginException {
    this.callbackHandler = null;
    return this.module.logout();
  }
}
