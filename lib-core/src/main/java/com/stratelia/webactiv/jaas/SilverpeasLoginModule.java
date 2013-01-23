/**
 * Copyright (C) 2000 - 2012 Silverpeas
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

package com.stratelia.webactiv.jaas;

import com.silverpeas.jcrutil.security.impl.DigestCredentials;
import com.silverpeas.jcrutil.security.impl.SilverpeasCredentials;
import com.silverpeas.jcrutil.security.impl.SilverpeasSystemCredentials;
import com.silverpeas.jcrutil.security.impl.SilverpeasSystemPrincipal;
import com.stratelia.silverpeas.authentication.AuthenticationCredential;
import com.stratelia.silverpeas.authentication.AuthenticationService;
import com.stratelia.webactiv.beans.admin.Admin;
import com.stratelia.webactiv.beans.admin.AdminException;
import com.stratelia.webactiv.beans.admin.AdminReference;
import com.stratelia.webactiv.beans.admin.Domain;
import com.stratelia.webactiv.beans.admin.OrganizationController;
import com.stratelia.webactiv.beans.admin.UserDetail;
import com.stratelia.webactiv.beans.admin.UserFull;
import com.stratelia.webactiv.util.exception.WithNested;
import java.security.Principal;
import org.apache.jackrabbit.core.security.AnonymousPrincipal;
import org.apache.jackrabbit.core.security.authentication.CredentialsCallback;

import javax.jcr.Credentials;
import javax.jcr.SimpleCredentials;
import javax.security.auth.Subject;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.security.auth.login.FailedLoginException;
import javax.security.auth.login.LoginException;
import javax.security.auth.spi.LoginModule;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.apache.jackrabbit.util.Text;

public class SilverpeasLoginModule implements LoginModule {

  private String userId;
  // initial state
  private Subject subject;
  private CallbackHandler callbackHandler;
  private Set<Principal> principals = new HashSet<Principal>();
  private AuthenticationService authenticator;
  private OrganizationController controller;
  private Admin administrator;

  public String getUserId() {
    return userId;
  }

  public Subject getSubject() {
    return subject;
  }

  public void setAuthenticator(AuthenticationService authenticator) {
    this.authenticator = authenticator;
  }

  public void setController(OrganizationController controller) {
    this.controller = controller;
  }

  public void setAdministrator(Admin administrator) {
    this.administrator = administrator;
  }

  @Override
  public boolean abort() throws LoginException {
    if (principals.isEmpty()) {
      return false;
    } else {
      logout();
    }
    return true;
  }

  @Override
  public boolean commit() throws LoginException {
    if (principals.isEmpty()) {
      return false;
    } else {
      subject.getPrincipals().addAll(principals);
      return true;
    }
  }

  @Override
  public void initialize(Subject subject, CallbackHandler callbackHandler, Map sharedState,
      Map options) {
    this.subject = subject;
    this.callbackHandler = callbackHandler;
  }

  @Override
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
          for (Domain domain : domains) {
            AuthenticationCredential credential = AuthenticationCredential
                .newWithAsLogin(sc.getUserID())
                .withAsPassword(new String(sc.getPassword()))
                .withAsDomainId(domain.getId());
            String key = authenticator.authenticate(credential);
            if (key != null && !key.startsWith("Error_")) {
              userId = administrator.authenticate(key, null, false);
              SilverpeasUserPrincipal principal = new SilverpeasUserPrincipal(userId);
              fillPrincipal(principal);
              principals.add(principal);
            }
          }
          if (principals.isEmpty() && UserDetail.isAnonymousUser(sc.getUserID())) {
            principals.add(new AnonymousPrincipal());
          }
          authenticated = true;
        } else if (creds instanceof SilverpeasCredentials) {
          String userId = ((SilverpeasCredentials) creds).getUserId();
          SilverpeasUserPrincipal principal = new SilverpeasUserPrincipal(userId);
          fillPrincipal(principal);
          principals.add(principal);
          authenticated = true;
        } else if (creds instanceof SilverpeasSystemCredentials) {
          SilverpeasSystemPrincipal principal = new SilverpeasSystemPrincipal();
          principals.add(principal);
          authenticated = true;
        } else if (creds instanceof DigestCredentials) {
          DigestCredentials sc = (DigestCredentials) creds;
          // authenticate
          Domain[] domains = controller.getAllDomains();
          for (Domain domain : domains) {
            AuthenticationCredential credential = AuthenticationCredential
                .newWithAsLogin(sc.getUsername())
                .withAsDomainId(domain.getId());
            String key = authenticator.authenticate(credential);
            if (key != null && !key.startsWith("Error_")) {
              userId = administrator.authenticate(key, null, false);
              SilverpeasUserPrincipal principal = new SilverpeasUserPrincipal(userId);
              validateDigestUser(principal, sc);
              fillPrincipal(principal);
              principals.add(principal);
            }
          }
          if (principals.isEmpty() && UserDetail.isAnonymousUser(sc.getUsername())) {
            principals.add(new AnonymousPrincipal());
          }
          authenticated = true;
        }
      } else {
        principals.add(new AnonymousPrincipal());
        authenticated = true;
      }
    } catch (java.io.IOException ioe) {
      throw new LoginException(ioe.toString());
    } catch (UnsupportedCallbackException uce) {
      throw new LoginException(uce.getCallback().toString() + " not available");
    } catch (AdminException e) {
      StringBuilder message = new StringBuilder();

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

  @Override
  public boolean logout() throws LoginException {
    subject.getPrincipals().removeAll(principals);
    principals.clear();
    return true;
  }

  protected void fillPrincipal(SilverpeasUserPrincipal principal) {
    String[] componentIds = controller.getAllowedComponentIds(principal.getUserId());
    for (String componentId : componentIds) {
      String[] profiles = controller.getUserProfiles(principal.getUserId(), componentId);
      for (String profile : profiles) {
        principal.addUserProfile(new SilverpeasUserProfileEntry(componentId, profile));
      }
    }
  }

  public boolean validateDigestUser(SilverpeasUserPrincipal principal, DigestCredentials sc) throws
      AdminException {
    UserFull user = AdminReference.getAdminService().getUserFull(userId);
    String md5a1 = Text.md5(user.getPassword());
    String serverDigestValue = md5a1 + ":" + sc.getNonce() + ":" + sc.getNc() + ":" +
        sc.getCnonce() + ":" + sc.getQop() + ":" + sc.getMd5a2();
    String serverDigest = Text.md5(serverDigestValue);
    return serverDigest.equals(sc.getClientDigest());
  }
}
