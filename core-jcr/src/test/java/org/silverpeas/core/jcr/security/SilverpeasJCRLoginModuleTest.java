/*
 * Copyright (C) 2000 - 2023 Silverpeas
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
 * "https://www.silverpeas.org/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 *
 */

package org.silverpeas.core.jcr.security;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.silverpeas.core.SilverpeasRuntimeException;
import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.jcr.impl.oak.security.SilverpeasCallbackHandler;

import javax.jcr.Credentials;
import javax.security.auth.Subject;
import javax.security.auth.login.LoginException;
import javax.security.auth.spi.LoginModule;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.invoke.MethodType;
import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.util.HashMap;
import java.util.Set;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Test the authentication of a user is correctly performed by the different JAAS login modules
 * according to the type of the used authentication (by user id/password pair or by token)
 * @author mmoquillon
 */
class SilverpeasJCRLoginModuleTest extends SecurityTest {

  @ParameterizedTest
  @DisplayName("Only one dedicated login module should be obtained for each of the authentication" +
      " method supported by Silverpeas")
  @MethodSource("getSupportedCredentials")
  void onlyOneLoginModuleShouldBeGetForEachSupportedCredentialsType(final Credentials credentials) {
    Set<SilverpeasJCRLoginModule> loginModules =
        LoginModuleRegistry.getInstance().getLoginModule(credentials.getClass());
    assertThat(loginModules.size(), is(1));

    //noinspection rawtypes
    Set<Class> supportedCredentials = getCredentialsSupportedBy(loginModules.iterator().next());
    assertThat(supportedCredentials.size(), is(1));
    assertThat(supportedCredentials, contains(credentials.getClass()));
  }

  @Test
  @DisplayName("A different login module instances should be provided each time")
  void differentLoginModuleInstances() {
    Credentials credentials = getAnyCredentials();
    LoginModule loginModule1 =
        LoginModuleRegistry.getInstance().getLoginModule(credentials.getClass()).iterator().next();

    LoginModule loginModule2 =
        LoginModuleRegistry.getInstance().getLoginModule(credentials.getClass()).iterator().next();

    assertThat(loginModule1.getClass(), is(loginModule2.getClass()));
    assertThat(loginModule1, not(loginModule2));
  }

  @ParameterizedTest
  @DisplayName("Applying a login module to authenticate a user by his credentials should succeed")
  @MethodSource("getSupportedCredentials")
  void authenticateAUserByHisValidCredentials(final Credentials credentials) throws LoginException {
    LoginModule loginModule =
        LoginModuleRegistry.getInstance().getLoginModule(credentials.getClass()).iterator().next();
    final Subject subject = new Subject();
    loginModule.initialize(subject, new TestCallBackHandler(credentials), new HashMap<>(),
        new HashMap<>());
    assertThat(loginModule.login(), is(true));
    assertThat(loginModule.commit(), is(true));
    assertThat(subject.getPrincipals(),
        Matchers.hasItem(new SilverpeasUserPrincipal(context.user.toUser())));
    // the credentials shouldn't set in the subject as they are sensitive information
    assertThat(subject.getPrivateCredentials(), not(hasItem(credentials)));
    assertThat(subject.getPublicCredentials(), not(hasItem(credentials)));
  }

  @Test
  @DisplayName("Applying a login module to authenticate the system user should succeed")
  void authenticateSystemUser() throws LoginException {
    Credentials credentials = JCRUserCredentialsProvider.getJcrSystemCredentials();
    LoginModule loginModule =
        LoginModuleRegistry.getInstance().getLoginModule(credentials.getClass()).iterator().next();
    final Subject subject = new Subject();
    loginModule.initialize(subject, new TestCallBackHandler(credentials), new HashMap<>(),
        new HashMap<>());
    assertThat(loginModule.login(), is(true));
    assertThat(loginModule.commit(), is(true));
    assertThat(subject.getPrincipals(),
        Matchers.hasItem(new SilverpeasUserPrincipal(User.getSystemUser())));
    assertThat(subject.getPrincipals(SilverpeasUserPrincipal.class).iterator().next().isSystem(),
        is(true));
    // the credentials shouldn't set in the subject as they are sensitive information
    assertThat(subject.getPrivateCredentials(), not(hasItem(credentials)));
    assertThat(subject.getPublicCredentials(), not(hasItem(credentials)));
  }

  @ParameterizedTest
  @DisplayName(
      "Applying a login module to authenticate a user with invalid credentials shouldn't " +
          "succeed")
  @MethodSource("getInvalidCredentials")
  void authenticateAUserByInvalidCredentials(final Credentials credentials) {
    LoginModule loginModule =
        LoginModuleRegistry.getInstance().getLoginModule(credentials.getClass()).iterator().next();
    final Subject subject = new Subject();
    loginModule.initialize(subject, new TestCallBackHandler(credentials), new HashMap<>(),
        new HashMap<>());
    assertThrows(LoginException.class, loginModule::login, "The login shouldn't succeed!");
  }

  @SuppressWarnings("rawtypes")
  Set<Class> getCredentialsSupportedBy(final SilverpeasJCRLoginModule loginModule) {
    PrivilegedExceptionAction<Set<Class>> getter = () -> {
      Lookup lookup = MethodHandles.lookup();
      Lookup privateLookup = MethodHandles.privateLookupIn(loginModule.getClass(), lookup);
      MethodType methodType = MethodType.methodType(Set.class);
      MethodHandle getCredentials =
          privateLookup.findVirtual(loginModule.getClass(), "getSupportedCredentials", methodType);
      try {
        //noinspection unchecked
        return (Set<Class>) getCredentials.invoke(loginModule);
      } catch (Throwable e) {
        throw new SilverpeasRuntimeException(e);
      }
    };

    try {
      return AccessController.doPrivileged(getter);
    } catch (PrivilegedActionException e) {
      throw new SilverpeasRuntimeException(e);
    }
  }

  static class TestCallBackHandler extends SilverpeasCallbackHandler {
    TestCallBackHandler(final Credentials credentials) {
      super(credentials);
    }
  }

}