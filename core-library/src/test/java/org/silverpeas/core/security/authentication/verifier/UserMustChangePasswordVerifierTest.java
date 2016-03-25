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
package org.silverpeas.core.security.authentication.verifier;

import org.silverpeas.core.admin.user.model.UserDetail;
import org.junit.Rule;
import org.junit.Test;
import org.silverpeas.core.security.authentication.exception.AuthenticationException;
import org.silverpeas.core.security.authentication.exception.AuthenticationPasswordAboutToExpireException;
import org.silverpeas.core.security.authentication.exception.AuthenticationPasswordExpired;
import org.silverpeas.core.security.authentication.exception.AuthenticationPasswordMustBeChangedOnFirstLogin;
import org.silverpeas.core.test.rule.CommonAPI4Test;

import java.util.Date;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

/**
 * User: Yohann Chastagnier
 * Date: 15/02/13
 */
public class UserMustChangePasswordVerifierTest {

  private static final int MAX_CONNECTIONS_FORCING = 8;
  private static final int MAX_CONNECTIONS_PROPOSING = 5;

  @Rule
  public CommonAPI4Test commonAPI4Test = new CommonAPI4Test();

  @Test
  public void verifierNotActivated() throws AuthenticationException {
    UserDetail user = createUser(0);

    UserMustChangePasswordVerifier verifier = createVerifierInstance(user, false, 0, 0);
    for (int i = 0; i < 1000; i++) {
      verifier.verify();
      user.setNbSuccessfulLoginAttempts(i + 1);
    }

    verifier = createVerifierInstance(user, false, -1, 20);
    for (int i = 0; i < 1000; i++) {
      verifier.verify();
      user.setNbSuccessfulLoginAttempts(i + 1);
    }
  }

  @Test
  public void verifyFromFirstUserConnectionWithoutProposing() throws AuthenticationException {
    UserDetail user = createUser(0);
    UserMustChangePasswordVerifier verifier =
        createVerifierInstance(user, false, MAX_CONNECTIONS_FORCING, 0);
    int nbAsserts = 0;
    for (int i = 0; i < 1000; i++) {
      try {
        verifier.verify();
        // User connected : +1 for successful connection counter
        user.setNbSuccessfulLoginAttempts(user.getNbSuccessfulLoginAttempts() + 1);
      } catch (AuthenticationPasswordExpired e) {
        assertThat(user.getNbSuccessfulLoginAttempts(), is(MAX_CONNECTIONS_FORCING));
        nbAsserts++;
        // Simlulate that the password is changed : reset of successful connection counter
        user.setNbSuccessfulLoginAttempts(0);
      }
    }
    assertThat(nbAsserts, is(1000 / (MAX_CONNECTIONS_FORCING + 1)));
  }

  @Test
  public void verifyFromFirstUserConnectionWithProposing() throws AuthenticationException {
    UserDetail user = createUser(0);
    UserMustChangePasswordVerifier verifier =
        createVerifierInstance(user, false, MAX_CONNECTIONS_FORCING, MAX_CONNECTIONS_PROPOSING);
    int nbAssertsForForcing = 0;
    int nbAssertsForProposing = 0;
    for (int i = 0; i < 1000; i++) {
      try {
        verifier.verify();
        // User connected : +1 for successful connection counter
        user.setNbSuccessfulLoginAttempts(user.getNbSuccessfulLoginAttempts() + 1);
      } catch (AuthenticationPasswordAboutToExpireException e) {
        nbAssertsForProposing++;
        // User connected : +1 for successful connection counter
        user.setNbSuccessfulLoginAttempts(user.getNbSuccessfulLoginAttempts() + 1);
      } catch (AuthenticationPasswordExpired e) {
        assertThat(user.getNbSuccessfulLoginAttempts(), is(MAX_CONNECTIONS_FORCING));
        nbAssertsForForcing++;
        // Simlulate that the password is changed : reset of successful connection counter
        user.setNbSuccessfulLoginAttempts(0);
      }
    }
    assertThat(nbAssertsForForcing, is(1000 / (MAX_CONNECTIONS_FORCING + 1)));
    assertThat(nbAssertsForProposing, is((1000 / (MAX_CONNECTIONS_FORCING + 1)) *
        (MAX_CONNECTIONS_FORCING - MAX_CONNECTIONS_PROPOSING)));
  }

  @Test(expected = AuthenticationPasswordMustBeChangedOnFirstLogin.class)
  public void verifyUserMustChangePasswordOnFirstLogin() throws AuthenticationException {
    UserDetail user = createUser(0);
    user.setLastLoginDate(null);
    UserMustChangePasswordVerifier verifier = createVerifierInstance(user, true, 0, 0);
    verifier.verify();
  }

  @Test
  public void verifyUserMustChangePasswordOnFirstLoginButNotTheFirstUserLogin()
      throws AuthenticationException {
    UserDetail user = createUser(0);
    user.setLastLoginDate(new Date());
    UserMustChangePasswordVerifier verifier = createVerifierInstance(user, true, 0, 0);
    verifier.verify();
  }

  /**
   * Gets a new verifier instance.
   * @param user
   * @param nbSuccessfulUserConnectionsBeforeForcingPasswordChange
   * @param nbSuccessfulUserConnectionsBeforeProposingToChangePassword
   * @return
   */
  private UserMustChangePasswordVerifier createVerifierInstance(UserDetail user,
      boolean userMustChangePasswordOnFirstLogin,
      int nbSuccessfulUserConnectionsBeforeForcingPasswordChange,
      int nbSuccessfulUserConnectionsBeforeProposingToChangePassword) {
    UserMustChangePasswordVerifierForTest verifier =
        new UserMustChangePasswordVerifierForTest(user);
    verifier.initialize(userMustChangePasswordOnFirstLogin, false,
        nbSuccessfulUserConnectionsBeforeForcingPasswordChange,
        nbSuccessfulUserConnectionsBeforeProposingToChangePassword);
    return verifier;
  }

  /**
   * Create a UserDetail
   * @param withXSuccessfulConnections
   * @return
   */
  private UserDetail createUser(int withXSuccessfulConnections) {
    UserDetail user = new UserDetail();
    user.setId("0");
    user.setNbSuccessfulLoginAttempts(withXSuccessfulConnections);
    return user;
  }

  /**
   * Wrapper for tests.
   */
  private class UserMustChangePasswordVerifierForTest extends UserMustChangePasswordVerifier {

    /**
     * Default constructor.
     * @param user
     */
    protected UserMustChangePasswordVerifierForTest(final UserDetail user) {
      super(user);
    }

    public void initialize(boolean userMustChangePasswordOnFirstLogin,
        boolean userMustFillEmailAddressOnFirstLogin,
        int nbSuccessfulUserConnectionsBeforeForcingPasswordChange,
        int nbSuccessfulUserConnectionsBeforeProposingToChangePassword) {
      setup(userMustChangePasswordOnFirstLogin, userMustFillEmailAddressOnFirstLogin,
          nbSuccessfulUserConnectionsBeforeForcingPasswordChange,
          nbSuccessfulUserConnectionsBeforeProposingToChangePassword);
    }
  }
}
