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
package org.silverpeas.look.web;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.notNullValue;
import static org.silverpeas.look.web.LookResourceURIs.DISPLAY_BASE_URI;
import static org.silverpeas.look.web.LookResourceURIs.DISPLAY_USER_CONTEXT_URI_PART;
import static org.silverpeas.look.web.LookTestResources.JAVA_PACKAGE;
import static org.silverpeas.look.web.LookTestResources.SPRING_CONTEXT;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import com.silverpeas.web.ResourceGettingTest;
import com.stratelia.webactiv.beans.admin.UserDetail;

/**
 * Tests on the user display context getting by the DisplayResource web service.
 * @author Yohann Chastagnier
 */
public class DisplayUserContextGettingTest extends ResourceGettingTest<LookTestResources> {

  private UserDetail user;
  private String sessionKey;

  public DisplayUserContextGettingTest() {
    super(JAVA_PACKAGE, SPRING_CONTEXT);
  }

  @Before
  public void prepareTestResources() {
    user = aUser();
    sessionKey = authenticate(user);
  }

  @Test
  public void getUserContext() {
    final DisplayUserContextEntity entity = getAt(aResourceURI(), DisplayUserContextEntity.class);
    assertThat(entity, notNullValue());
  }

  @Override
  @Ignore
  public void gettingAResourceByAnUnauthorizedUser() {
  }

  @Override
  public String aResourceURI() {
    return DISPLAY_BASE_URI + "/" + DISPLAY_USER_CONTEXT_URI_PART;
  }

  @Override
  public String anUnexistingResourceURI() {
    return aResourceURI() + "a";
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
    return DisplayUserContextEntity.class;
  }

  @Override
  public String[] getExistingComponentInstances() {
    return new String[] {};
  }
}
