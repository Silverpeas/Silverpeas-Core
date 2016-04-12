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
package org.silverpeas.core.webapi.password;

import com.silverpeas.web.ResourceCreationTest;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.silverpeas.core.security.authentication.password.constant.PasswordRuleType;
import org.silverpeas.core.web.test.WarBuilder4WebCore;

import javax.ws.rs.core.Response;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertNotNull;

/**
 * Tests on the gallery photo getting by the GalleryResource web service.
 * @author Yohann Chastagnier
 */
@RunWith(Arquillian.class)
public class PasswordPolicyCheckingTest extends ResourceCreationTest {

  private String sessionKey;

  @Deployment
  public static Archive<?> createTestArchive() {
    return WarBuilder4WebCore.onWarForTestClass(PasswordPolicyGettingTest.class)
        .addRESTWebServiceEnvironment()
        .addStringTemplateFeatures()
        .testFocusedOn(warBuilder -> {
          warBuilder.addPackages(true, "org.silverpeas.core.webapi.password");
          warBuilder.addAsResource("org/silverpeas/password/multilang/passwordBundle.properties");
          warBuilder.addAsResource("org/silverpeas/password/settings/password.properties");
        }).build();
  }

  @Before
  public void prepareTestResources() {
    sessionKey = getTokenKeyOf(getSilverpeasEnvironmentTest().createDefaultUser());
  }

  @Test
  public void checking() {
    final Response response = post(PasswordEntity.createFrom("aA0$1234"), aResourceURI());
    final PasswordCheckEntity passwordCheck = response.readEntity(PasswordCheckEntity.class);
    assertNotNull(passwordCheck);
    assertThat(passwordCheck.isCorrect(), is(true));
  }

  @Test
  @SuppressWarnings("unchecked")
  public void checkingWithErrors() {
    final Response response = post(PasswordEntity.createFrom("AA0ç123"), aResourceURI());
    final PasswordCheckEntity passwordCheck = response.readEntity(PasswordCheckEntity.class);
    assertNotNull(passwordCheck);
    assertThat(passwordCheck.getRequiredRuleIdsInError(),
        contains(PasswordRuleType.MIN_LENGTH.name(), PasswordRuleType.SEQUENTIAL_FORBIDDEN.name(),
            PasswordRuleType.AT_LEAST_X_LOWERCASE.name(),
            PasswordRuleType.AT_LEAST_X_SPECIAL_CHAR.name()));
  }

  @Ignore
  @Override
  public void creationOfANewResourceWithADeprecatedSession() {
  }

  @Ignore
  @Override
  public void creationOfANewResourceByANonAuthenticatedUser() {
  }

  @Ignore
  @Override
  public void creationOfANewResourceByANonAuthorizedUser() {
  }

  @Override
  public String aResourceURI() {
    return "password/policy/checking";
  }

  @Override
  public String anUnexistingResourceURI() {
    return "password/policies";
  }

  @Override
  public PasswordEntity aResource() {
    return PasswordEntity.createFrom("");
  }

  @Override
  public String getTokenKey() {
    return sessionKey;
  }

  @Override
  public Class<?> getWebEntityClass() {
    return PasswordPolicyEntity.class;
  }

  @Override
  public String[] getExistingComponentInstances() {
    return new String[]{};
  }
}
