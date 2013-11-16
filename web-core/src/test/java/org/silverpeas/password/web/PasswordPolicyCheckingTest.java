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
package org.silverpeas.password.web;

import com.silverpeas.web.ResourceCreationTest;
import com.sun.jersey.api.client.ClientResponse;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.silverpeas.password.constant.PasswordRuleType;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertNotNull;
import static org.silverpeas.password.web.PasswordTestResources.JAVA_PACKAGE;
import static org.silverpeas.password.web.PasswordTestResources.SPRING_CONTEXT;

/**
 * Tests on the gallery photo getting by the GalleryResource web service.
 * @author Yohann Chastagnier
 */
public class PasswordPolicyCheckingTest extends ResourceCreationTest<PasswordTestResources> {

  private String sessionKey;

  public PasswordPolicyCheckingTest() {
    super(JAVA_PACKAGE, SPRING_CONTEXT);
  }

  @Before
  public void prepareTestResources() {
    sessionKey = authenticate(aUser());
  }

  @Test
  public void checking() {
    final ClientResponse response = post(PasswordEntity.createFrom("aA0$1234"), aResourceURI());
    final PasswordCheckEntity passwordCheck = response.getEntity(PasswordCheckEntity.class);
    assertNotNull(passwordCheck);
    assertThat(passwordCheck.isCorrect(), is(true));
  }

  @Test
  @SuppressWarnings("unchecked")
  public void checkingWithErrors() {
    final ClientResponse response = post(PasswordEntity.createFrom("AA0ç123"), aResourceURI());
    final PasswordCheckEntity passwordCheck = response.getEntity(PasswordCheckEntity.class);
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
  public String getSessionKey() {
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
