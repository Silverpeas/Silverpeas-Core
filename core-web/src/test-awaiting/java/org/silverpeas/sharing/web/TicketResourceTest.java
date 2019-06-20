/*
 * Copyright (C) 2000 - 2019 Silverpeas
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
 * "https://www.silverpeas.org/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.silverpeas.sharing.web;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import com.silverpeas.web.ResourceCreationTest;
import com.silverpeas.web.mock.UserDetailWithProfiles;
import com.stratelia.webactiv.beans.admin.UserDetail;

public class TicketResourceTest extends ResourceCreationTest<TicketTestRessources> {

  private UserDetail user;
  private String sessionKey;

//  @BeforeClass
//  public static void setUpBeforeClass() throws Exception {
//  }
//
//  @AfterClass
//  public static void tearDownAfterClass() throws Exception {
//  }

//  @Before
//  public void setUp() throws Exception {
//    super.setUp();
//  }
//
//  @After
//  public void tearDown() throws Exception {
//    super.tearDown();
//  }

  @Before
  public void prepareTestResources() {
    user = aUser();
    sessionKey = authenticate(user);
  }

  public TicketResourceTest() {
    super(TicketTestRessources.JAVA_PACKAGE, TicketTestRessources.SPRING_CONTEXT);
  }

  @Override
  public String aResourceURI() {
    return TicketTestRessources.A_URI;
  }

  @Override
  public String anUnexistingResourceURI() {
    return TicketTestRessources.UNEXISTING_URI;
  }

  @Override
  public TicketEntity aResource() {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("not yet implemented");
  }

  @Override
  public String getSessionKey() {
   return sessionKey;
  }

  @Override
  public Class<TicketEntity> getWebEntityClass() {
    return TicketEntity.class;
  }

  @Override
  public String[] getExistingComponentInstances() {
    return new String[]{TicketTestRessources.INSTANCE_ID};
  }

  @Test
  @Ignore
  @Override
  public void creationOfANewResourceByANonAuthorizedUser() {
  }

  @Test
  @Ignore
  @Override
  public void creationOfANewResourceWithADeprecatedSession() {
  }

  @Test
  @Ignore
  @Override
  public void creationOfANewResourceByANonAuthenticatedUser() {
  }

  @Test
  @Ignore
  @Override
  public void postAnInvalidResourceState() {
  }
}
