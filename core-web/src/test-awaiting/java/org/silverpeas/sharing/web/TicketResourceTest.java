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
