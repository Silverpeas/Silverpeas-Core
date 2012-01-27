/*
 * Copyright (C) 2000 - 2012 Silverpeas
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
 * "http://www.silverpeas.org/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.silverpeas.profile.web;

import static com.silverpeas.profile.web.UserProfileTestResources.*;
import com.silverpeas.rest.ResourceGettingTest;
import com.stratelia.webactiv.beans.admin.UserDetail;
import org.junit.Before;
import org.junit.Ignore;

/**
 * Unit tests on the operations published by the UserProfileResource REST service.
 */
public class UserProfileResourceTest extends ResourceGettingTest<UserProfileTestResources> {
  
  private String sessionKey;
  private UserDetail caller;
  
  public UserProfileResourceTest() {
    super(JAVA_PACKAGE, SPRING_CONTEXT);
  }
  
  @Before
  public void prepareTestResources() {
    caller = aUser();
    sessionKey = authenticate(caller);
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
    return new String[] { };
  }

  @Override
  public String aResourceURI() {
    return USER_PROFILE_PATH;
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
  public Class<?> getWebEntityClass() {
    throw new UnsupportedOperationException("Not supported yet.");
  }
  
  
}
