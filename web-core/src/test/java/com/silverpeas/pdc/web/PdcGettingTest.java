/*
 * Copyright (C) 2000 - 2011 Silverpeas
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
package com.silverpeas.pdc.web;

import com.silverpeas.thesaurus.ThesaurusException;
import java.util.List;
import com.silverpeas.pdc.web.mock.PdcBmMock;
import javax.ws.rs.core.Response.Status;
import com.sun.jersey.api.client.UniformInterfaceException;
import org.junit.Test;
import com.stratelia.webactiv.beans.admin.UserDetail;
import org.junit.Before;
import com.silverpeas.rest.ResourceGettingTest;
import com.stratelia.silverpeas.pdc.model.UsedAxis;
import javax.inject.Inject;
import static org.junit.Assert.*;
import static org.hamcrest.Matchers.*;
import static com.silverpeas.pdc.web.TestResources.*;
import static com.silverpeas.pdc.web.TestConstants.*;
import static com.silverpeas.pdc.web.PdcEntityMatcher.*;

/**
 * Unit tests on the getting of the PdC configured for a given component instance.
 */
public class PdcGettingTest extends ResourceGettingTest {
  
  @Inject
  private TestResources resources;
  private String sessionKey;
  private UserDetail theUser;
  
  public PdcGettingTest() {
    super(JAVA_PACKAGE, SPRING_CONTEXT);
  }
  
  @Before
  public void setUpUserSessionAndPdC() {
    theUser = aUser();
    sessionKey = authenticate(theUser);
  }
  
  @Test
  @Override
  public void gettingAnUnexistingResource() {
    try {
      getAt(anUnexistingResourceURI(), getWebEntityClass());
      fail("A user shouldn't get an unexisting resource");
    } catch (UniformInterfaceException ex) {
      int receivedStatus = ex.getResponse().getStatus();
      int badRequest = Status.BAD_REQUEST.getStatusCode();
      assertThat(receivedStatus, is(badRequest));
    }
  }
  
  @Test
  public void gettingAPdC() {
    PdcEntity pdc = getAt(aResourceURI(), aPdcEntity());
    assertNotNull(pdc);
    assertThat(pdc, is(equalTo(pdc)));
  }

  @Override
  public String aResourceURI() {
    return CONTENT_PDC_PATH;
  }

  @Override
  public String anUnexistingResourceURI() {
    return UNKNOWN_CONTENT_PDC_PATH;
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
    return PdcEntity.class;
  }
  
  public Class<PdcEntity> aPdcEntity() {
    return PdcEntity.class;
  }
  
  public PdcEntity toWebEntity(List<UsedAxis> axis) throws ThesaurusException {
    return PdcEntity.aPdcEntity(axis, FRENCH, null, resources.aThesaurusHolderFor(theUser));
  }
}
