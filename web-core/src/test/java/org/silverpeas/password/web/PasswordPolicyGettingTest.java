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

import com.silverpeas.web.ResourceGettingTest;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.silverpeas.password.constant.PasswordRuleType;

import javax.ws.rs.core.MediaType;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertNotNull;
import static org.silverpeas.password.web.PasswordTestResources.JAVA_PACKAGE;
import static org.silverpeas.password.web.PasswordTestResources.SPRING_CONTEXT;

/**
 * Tests on the gallery photo getting by the GalleryResource web service.
 * @author Yohann Chastagnier
 */
public class PasswordPolicyGettingTest extends ResourceGettingTest<PasswordTestResources> {

  private String sessionKey;

  public PasswordPolicyGettingTest() {
    super(JAVA_PACKAGE, SPRING_CONTEXT);
  }

  @Before
  public void prepareTestResources() {
    sessionKey = authenticate(aUser());
  }

  @Test
  public void getPolicy() {
    assertEntity(getAt(aResourceURI(), PasswordPolicyEntity.class));
  }

  @Override
  public void gettingAResourceByANonAuthenticatedUser() {
    assertEntity(resource().path(aResourceURI()).
        accept(MediaType.APPLICATION_JSON).
        get(PasswordPolicyEntity.class));
  }

  /**
   * Centralization of asserts.
   * @param entity
   */
  private void assertEntity(PasswordPolicyEntity entity) {
    assertNotNull(entity);
    assertThat(entity.getRules().size(), is(8));
    assertThat(entity.getRules().get(PasswordRuleType.MIN_LENGTH.name()).getDescription(),
        is("au moins 8 caractères"));
    assertThat(entity.getRules().get(PasswordRuleType.AT_LEAST_X_UPPERCASE.name()).getDescription(),
        is("au moins 1 majuscule(s)"));
    assertThat(
        entity.getRules().get(PasswordRuleType.AT_LEAST_X_SPECIAL_CHAR.name()).getDescription(),
        is("au moins 1 caractère(s) spécial(aux) (%*!?$-+#&=.,;)"));
    assertThat(entity.getExtraRuleMessage(), is("règles supplémentaires non vérifiables ..."));
  }

  @Ignore
  @Override
  public void gettingAResourceWithAnExpiredSession() {
  }

  @Override
  @Ignore
  public void gettingAResourceByAnUnauthorizedUser() {
  }

  @Override
  public String aResourceURI() {
    return "password/policy";
  }

  @Override
  public String anUnexistingResourceURI() {
    return "password/policies";
  }

  @Override
  public Object aResource() {
    return null;
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
