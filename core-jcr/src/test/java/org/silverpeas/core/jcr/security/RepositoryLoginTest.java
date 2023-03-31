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

import org.jboss.weld.executor.FixedThreadPoolExecutorServices;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.silverpeas.core.jcr.impl.RepositorySettings;
import org.silverpeas.core.test.extention.SystemProperty;
import org.silverpeas.core.test.extention.TestManagedBean;
import org.silverpeas.core.jcr.JCRSession;
import org.silverpeas.core.jcr.RepositoryProvider;

import javax.jcr.Credentials;
import javax.jcr.LoginException;
import javax.jcr.Repository;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.not;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.silverpeas.core.jcr.security.RepositoryLoginTest.JCR_HOME;
import static org.silverpeas.core.jcr.security.RepositoryLoginTest.OAK_CONFIG;


/**
 * Test the login of a user in Silverpeas to the JCR is correctly performed and taken in charge by
 * Silverpeas itself. The success of the test proves the binding of the JCR implementation with
 * Silverpeas works fine.
 * @author mmoquillon
 */
@SystemProperty(key = RepositorySettings.JCR_HOME, value = JCR_HOME)
@SystemProperty(key = RepositorySettings.JCR_CONF, value = OAK_CONFIG)
class RepositoryLoginTest extends SecurityTest {

  public static final String JCR_HOME = "target/";
  public static final String OAK_CONFIG = "classpath:/silverpeas-oak.properties";

  @TestManagedBean
  RepositoryProvider repositoryProvider;

  @ParameterizedTest
  @DisplayName("Login of a user of Silverpeas to the JCR with his credentials should succeed")
  @MethodSource("getSupportedCredentials")
  void userLogInToRepository(final Credentials credentials) throws RepositoryException {
    Repository repository = repositoryProvider.getRepository();
    Session session = repository.login(credentials);
    assertThat(session, notNullValue());
    assertThat(session, is(instanceOf(JCRSession.class)));
    session.logout();
  }

  @Test
  @DisplayName("Login of the JCR system user to the JCR should always succeed")
  void systemUserLogInToRepository() throws RepositoryException {
    Credentials credentials = JCRUserCredentialsProvider.getJcrSystemCredentials();
    Repository repository = repositoryProvider.getRepository();
    Session session = repository.login(credentials);
    assertThat(session, notNullValue());
    session.logout();
  }

  @ParameterizedTest
  @DisplayName("Login to the JCR should fail if the credentials aren't valid")
  @MethodSource("getInvalidCredentials")
  void badLoginToRepository(final Credentials credentials) {
    Repository repository = repositoryProvider.getRepository();
    assertThrows(LoginException.class, () -> repository.login(credentials),
        "The login shouldn't succeed!");
  }

  @Test
  @DisplayName("The JCR session should be reentrant; as such login several times to the JCR " +
      "should return the same JCR session")
  void loginSeveralTimeToRepositoryShouldReturnSameSession()
      throws RepositoryException {
    Credentials credentials = JCRUserCredentialsProvider.getUserCredentials(context.user.token);
    Repository repository = repositoryProvider.getRepository();
    Session session1 = repository.login(credentials);
    Session session2 = repository.login(credentials);
    assertThat(session1, is(session2));
    session1.logout();
    session2.logout();
  }

  @Test
  @DisplayName("Login to the JCR by the same user in different threads should open a different " +
      "session")
  void loginSeveralTimeToRepositoryInDifferentThreadShouldOpenANewSession()
      throws ExecutionException, InterruptedException {
    Credentials credentials = getAnyCredentials();
    Repository repository = repositoryProvider.getRepository();

    Callable<Session> loginToJCR = () -> repository.login(credentials);

    FixedThreadPoolExecutorServices executors = new FixedThreadPoolExecutorServices(3);
    ExecutorService executor = executors.getTaskExecutor();

    Future<Session> future1 = executor.submit(loginToJCR);
    Future<Session> future2 = executor.submit(loginToJCR);
    Future<Session> future3 = executor.submit(loginToJCR);

    assertThat(future1.get(), not(future2.get()));
    assertThat(future1.get(), not(future3.get()));
    assertThat(future2.get(), not(future3.get()));
  }

  @Test
  @DisplayName("Login to the JCR by two users within the same single thread should fail")
  void loginToJCRByDifferentUsersInSameThread() throws RepositoryException {
    Credentials systemCredentials = JCRUserCredentialsProvider.getJcrSystemCredentials();
    Credentials userCredentials =
        JCRUserCredentialsProvider.getUserCredentials(context.user.login, context.user.domainId,
            context.user.password);

    Repository repository = repositoryProvider.getRepository();
    repository.login(systemCredentials);
    assertThrows(IllegalStateException.class, () -> repository.login(userCredentials));
  }

  @Test
  @DisplayName("Login and logout to the JCR several times should open a new session")
  void loginAndLogoutToJCRSeveralTimes() throws RepositoryException {
    Credentials credentials = JCRUserCredentialsProvider.getJcrSystemCredentials();
    Repository repository = repositoryProvider.getRepository();

    Session session1 = repository.login(credentials);
    session1.logout();

    Session session2 = repository.login(credentials);
    session2.logout();

    assertThat(session1, not(session2));
  }

}
