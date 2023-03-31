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

import org.junit.jupiter.api.BeforeEach;
import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.admin.user.model.UserReference;
import org.silverpeas.core.admin.user.service.UserProvider;
import org.silverpeas.core.security.authentication.Authentication;
import org.silverpeas.core.security.authentication.AuthenticationCredential;
import org.silverpeas.core.security.authentication.AuthenticationResponse;
import org.silverpeas.core.security.authentication.exception.AuthenticationException;
import org.silverpeas.core.security.token.persistent.PersistentResourceToken;
import org.silverpeas.core.security.token.persistent.service.PersistentResourceTokenService;
import org.silverpeas.core.test.extention.EnableSilverTestEnv;
import org.silverpeas.core.test.extention.TestManagedMock;
import org.silverpeas.test.TestUser;

import javax.jcr.Credentials;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

/**
 * Base class for all the unit tests about testing the security bridge between the JCR and
 * Silverpeas. This class defines the required resources and beans to perform an authentication or
 * an access authorization of a user in Silverpeas.
 * @author mmoquillon
 */
@EnableSilverTestEnv
public abstract class SecurityTest {

  /**
   * Environment context of this test. If defines the resources required by the test.
   */
  static class Context {
    /**
     * The test requires a user to be defined in order to authenticate him. This user is represented
     * by a principal.
     */
    static class Principal {
      String id;
      String login;
      String domainId;
      String password;
      String token;

      User toUser() {
        return new TestUser.Builder()
            .setId(id)
            .setLogin(login)
            .setDomainId(domainId)
            .setToken(token)
            .setFirstName("Toto")
            .setLastName("Tartemption")
            .build();
      }
    }

    final Principal user;
    final String authKey = "42";

    Context() {
      user = new Principal();
      user.id = "0";
      user.login = "admin";
      user.domainId = "0";
      user.password =
          "$6$g/28GunwANvF4$RJwEUNQPF6q3zZsxcdW.x1jHKfG8/MP0/A3dAF1ZbmcDm" +
              ".iuSJcKJAp7dQTUQpmQVdTBbVstkAmoyLKZM7M.t.";
      user.token = "3bda82f29c8747d8bbff72b053da91f1";
    }
  }

  @TestManagedMock
  Authentication authentication;

  @TestManagedMock
  PersistentResourceTokenService tokenService;

  @TestManagedMock
  UserProvider userProvider;

  static Context context = new Context();

  @BeforeEach
  public void mockAuthenticationBehaviour() throws AuthenticationException {
    // authentication by login/domain/password
    when(authentication.authenticate(any(AuthenticationCredential.class))).thenAnswer(
        invocationOnMock -> {
          AuthenticationCredential credential = invocationOnMock.getArgument(0);
          String login = credential.getLogin();
          String domainId = credential.getDomainId();
          String password = credential.getPassword();
          if (login.equals(context.user.login) && domainId.equals(context.user.domainId) &&
              password.equals(context.user.password)) {
            return AuthenticationResponse.succeed(context.authKey);
          } else {
            return AuthenticationResponse.error(
                AuthenticationResponse.Status.BAD_LOGIN_PASSWORD_DOMAIN);
          }
        }
    );

    // authentication by token
    when(tokenService.get(anyString())).thenAnswer(
        invocationOnMock -> {
          String tokenValue = invocationOnMock.getArgument(0);
          if (tokenValue.equals(context.user.token)) {
            return new UserToken(context.user.id, tokenValue);
          } else {
            return UserToken.NoneToken;
          }
        }
    );

    // identification a successful authenticated user
    when(authentication.getUserByAuthToken(anyString())).thenAnswer(
        invocationOnMock -> {
          String authKey = invocationOnMock.getArgument(0);
          if (authKey.equals(context.authKey)) {
            return context.user.toUser();
          } else {
            throw new AuthenticationException("No such user");
          }
        }
    );

    when(userProvider.getUser(anyString())).thenAnswer(
        invocationOnMock -> {
          String userId = invocationOnMock.getArgument(0);
          if (userId.equals(context.user.id)) {
            return context.user.toUser();
          } else {
            return null;
          }
        }
    );

    when(userProvider.getUserByToken(anyString())).thenAnswer(
        invocationOnMock -> {
          String token = invocationOnMock.getArgument(0);
          if (token.equals(context.user.token)) {
            return context.user.toUser();
          } else {
            return null;
          }
        }
    );

    when(userProvider.getUserByLoginAndDomainId(anyString(), anyString())).thenAnswer(
        invocationOnMock -> {
          String login = invocationOnMock.getArgument(0);
          String domainId = invocationOnMock.getArgument(1);
          if (login.equals(context.user.login) && domainId.equals(context.user.domainId)) {
            return context.user.toUser();
          } else {
            return null;
          }
        }
    );

    when(userProvider.getSystemUser()).thenReturn(
        new TestUser.Builder()
            .setId("-1")
            .setDomainId("0")
            .build());
  }

  /**
   * Gets an instance for each type of credentials supported in Silverpeas.
   * @return a stream of supported credentials of the user defined for this test.
   */
  static Stream<Credentials> getSupportedCredentials() {
    return Stream.of(
        JCRUserCredentialsProvider.getUserCredentials(context.user.login, context.user.domainId,
            context.user.password),
        JCRUserCredentialsProvider.getUserCredentials(context.user.token));
  }

  /**
   * Gets an instance for each type of credentials supported in Silverpeas.
   * @return a stream of supported credentials of the user defined for this test.
   */
  static Stream<Credentials> getInvalidCredentials() {
    return Stream.of(
        JCRUserCredentialsProvider.getUserCredentials("toto", context.user.domainId,
            context.user.password),
        JCRUserCredentialsProvider.getUserCredentials("666"));
  }

  /**
   * Gets randomly a credentials among all the credentials predefined in the test.
   * @return a credentials.
   */
  static Credentials getAnyCredentials() {
    List<Credentials> allCredentials = Stream.concat(getSupportedCredentials(),
        Stream.of(JCRUserCredentialsProvider.getJcrSystemCredentials())).collect(
        Collectors.toList());
    Random random = new Random();
    return allCredentials.get(random.nextInt(allCredentials.size()));
  }

  static class UserToken extends PersistentResourceToken {

    UserToken(final String userId, final String token) {
      super(new UserReference(userId), token);
    }

  }
}
