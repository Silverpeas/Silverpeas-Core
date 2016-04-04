/*
 * Copyright (C) 2000 - 2013 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection withWriter Free/Libre
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
package org.silverpeas.core.socialnetwork.invitation.web;

import com.silverpeas.web.ResourceGettingTest;
import com.stratelia.webactiv.beans.admin.UserDetail;
import com.stratelia.webactiv.util.GeneralPropertiesManagerHelper;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import static org.silverpeas.core.socialnetwork.invitation.web.InvitationTestResources.JAVA_PACKAGE;
import static org.silverpeas.core.socialnetwork.invitation.web.InvitationTestResources.SPRING_CONTEXT;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

/**
 * Unit tests on the operations published by the InvitationResource REST service.
 */
public class SentInvitationResourceTest extends ResourceGettingTest<InvitationTestResources> {

  private String sessionKey;
  private UserDetail currentUser;

  public SentInvitationResourceTest() {
    super(JAVA_PACKAGE, SPRING_CONTEXT);
  }

  @Before
  public void prepareTestResources() {
    GeneralPropertiesManagerHelper.setDomainVisibility(DomainProperties.DVIS_ALL);
    currentUser = aUser();
    sessionKey = authenticate(currentUser);
  }

  @Test
  public void getEmptyInvitations() {
    InvitationEntity[] invitations = getAt(aResourceURI(), getWebEntityClass());
    assertThat(invitations.length, is(0));
  }

  @Test
  public void getSomeSentInvitations() {
    InvitationEntity[] expectedInvitations = getTestResources().saveSomeSentInvitations(
            currentUser);
    InvitationEntity[] actualInvitations = getAt(aResourceURI(), getWebEntityClass());
    assertThat(actualInvitations.length, is(expectedInvitations.length));
    assertThat(actualInvitations, equalTo(expectedInvitations));
  }

  @Override
  @Ignore
  public void gettingAResourceByAnUnauthorizedUser() {
  }

  @Override
  @Ignore
  public void gettingAnUnexistingResource() {
  }

  @Override
  public String[] getExistingComponentInstances() {
    return new String[]{};
  }

  @Override
  public String aResourceURI() {
    return "/invitations/outbox";
  }

  @Override
  public String anUnexistingResourceURI() {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public <T> T aResource() {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public String getSessionKey() {
    return sessionKey;
  }

  @Override
  public Class<InvitationEntity[]> getWebEntityClass() {
    return InvitationEntity[].class;
  }
}
