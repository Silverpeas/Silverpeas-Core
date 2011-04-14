/**
 * Copyright (C) 2000 - 2011 Silverpeas
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

package com.stratelia.webactiv.jaas;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.jcr.Credentials;
import javax.jcr.SimpleCredentials;
import javax.security.auth.Subject;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.security.auth.login.FailedLoginException;
import javax.security.auth.login.LoginException;
import javax.security.auth.spi.LoginModule;

import org.apache.jackrabbit.core.security.AnonymousPrincipal;

import com.silverpeas.jcrutil.security.impl.SilverpeasCredentials;
import com.silverpeas.jcrutil.security.impl.SilverpeasSystemCredentials;
import com.silverpeas.jcrutil.security.impl.SilverpeasSystemPrincipal;
import com.stratelia.silverpeas.authentication.LoginPasswordAuthentication;
import com.stratelia.webactiv.beans.admin.Admin;
import com.stratelia.webactiv.beans.admin.AdminException;
import com.stratelia.webactiv.beans.admin.Domain;
import com.stratelia.webactiv.beans.admin.OrganizationController;
import com.stratelia.webactiv.beans.admin.UserDetail;
import com.stratelia.webactiv.util.exception.WithNested;

import org.apache.jackrabbit.core.security.authentication.CredentialsCallback;

public class SilverpeasLoginModule implements LoginModule {

  private String userId;

  // initial state
  private Subject subject;
  private CallbackHandler callbackHandler;
  private Set principals = new HashSet();

  private LoginPasswordAuthentication authenticator;

  private OrganizationController controller;

  private Admin administrator;

  public String getUserId() {
    return userId;
  }

  public Subject getSubject() {
    return subject;
  }

  public void setAuthenticator(LoginPasswordAuthentication authenticator) {
    this.authenticator = authenticator;
  }

  public void setController(OrganizationController controller) {
    this.controller = controller;
  }

  public void setAdministrator(Admin administrator) {
    this.administrator = administrator;
  }

  public boolean abort() throws LoginException {
    if (principals.isEmpty()) {
      return false;
    } else {
      logout();
    }
    return true;
  }

  public boolean commit() throws LoginException {
    if (principals.isEmpty()) {
      return false;
    } else {
      subject.getPrincipals().addAll(principals);
      return true;
    }
  }

  public void initialize(Subject subject, CallbackHandler callbackHandler,
      Map sharedState, Map options) {
    this.subject = subject;
    this.callbackHandler = callbackHandler;
  }

  public boolean login() throws LoginException {
    // prompt for a user name and password
    if (callbackHandler == null) {
      throw new LoginException("no CallbackHandler available");
    }
    boolean authenticated = false;
    principals.clear();
    try {
      // Get credentials using a JAAS callback
      CredentialsCallback ccb = new CredentialsCallback();
      callbackHandler.handle(new Callback[] { ccb });
      Credentials creds = ccb.getCredentials();
      // Use the credentials to set up principals
      if (creds != null) {
        if (creds instanceof SimpleCredentials) {
          SimpleCredentials sc = (SimpleCredentials) creds;
          // authenticate
          Domain[] domains = controller.getAllDomains();
          for (int i = 0; i < domains.length; i++) {
            String key = authenticator.authenticate(sc.getUserID(),
                new String(sc.getPassword()), domains[i]
                .getId(), null);
            if (key != null && !key.startsWith("Error_")) {
              String userId = administrator.authenticate(key,
                  null, false);
              SilverpeasUserPrincipal principal = new SilverpeasUserPrincipal(
                  userId);
              fillPrincipal(principal);
              principals.add(principal);
            }
          }

          if (principals.isEmpty()
              && UserDetail.isAnonymousUser(sc.getUserID())) {
            principals.add(new AnonymousPrincipal());
          }
          authenticated = true;
        } else if (creds instanceof SilverpeasCredentials) {
          String userId = ((SilverpeasCredentials) creds).getUserId();
          SilverpeasUserPrincipal principal = new SilverpeasUserPrincipal(
              userId);
          fillPrincipal(principal);
          principals.add(principal);
          authenticated = true;
        } else if (creds instanceof SilverpeasSystemCredentials) {
          SilverpeasSystemPrincipal principal = new SilverpeasSystemPrincipal();
          principals.add(principal);
          authenticated = true;
        }
      } else {
        principals.add(new AnonymousPrincipal());
        authenticated = true;
      }
    } catch (java.io.IOException ioe) {
      throw new LoginException(ioe.toString());
    } catch (UnsupportedCallbackException uce) {
      throw new LoginException(uce.getCallback().toString()
          + " not available");
    } catch (AdminException e) {
      StringBuffer message = new StringBuffer();

      Throwable ex = e;
      int i = 0;
      while ((ex != null) && (i < 10)) {
        i++;
        message.append(" - ").append(ex.getMessage());
        if (ex instanceof WithNested) {
          ex = ((WithNested) ex).getNested();
        } else {
          ex = null;
        }
      }
      throw new LoginException(message.toString());
    }
    if (authenticated) {
      return !principals.isEmpty();
    } else {
      principals.clear();
      throw new FailedLoginException();
    }
  }

  public boolean logout() throws LoginException {
    subject.getPrincipals().removeAll(principals);
    principals.clear();
    return true;
  }

  protected void fillPrincipal(SilverpeasUserPrincipal principal) {
    String[] spaceIds = controller.getAllSpaceIds(principal.getUserId());
    for (int i = 0; i < spaceIds.length; i++) {
      String[] componentIds = controller.getAvailCompoIds(spaceIds[i],
          principal.getUserId());
      for (int j = 0; j < componentIds.length; j++) {
        String[] profiles = controller.getUserProfiles(principal
            .getUserId(), componentIds[j]);
        for (int k = 0; k < profiles.length; k++) {
          principal.addUserProfile(new SilverpeasUserProfileEntry(
              componentIds[j], profiles[k]));
        }
      }
    }
  }

}
