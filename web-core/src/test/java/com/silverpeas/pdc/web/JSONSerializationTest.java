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
 * Open Source Software (\"FLOSS\") applications as described in Silverpeas\"s
 * FLOSS exception.  You should have recieved a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * \"http://www.silverpeas.org/legal/licensing\"
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
import com.stratelia.webactiv.beans.admin.UserDetail;
import com.silverpeas.pdc.web.beans.PdcClassification;
import com.sun.jersey.api.json.JSONConfiguration;
import com.sun.jersey.api.json.JSONJAXBContext;
import com.sun.jersey.api.json.JSONUnmarshaller;
import com.sun.jersey.json.impl.JSONUnmarshallerImpl;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import java.io.StringReader;
import javax.inject.Inject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;
import static org.hamcrest.Matchers.*;
import static com.silverpeas.pdc.web.TestConstants.*;
import static com.silverpeas.pdc.web.beans.PdcClassification.*;
import static com.silverpeas.pdc.web.PdcClassificationEntityMatcher.*;

/**
 * Unit tests on the serialization/deserialization of Pdc entities in/from JSON by using the 
 * Jersey JSON marshaller.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "/spring-pdc-webservice.xml")
public class JSONSerializationTest {

  @Inject
  private TestResources resources;

  public JSONSerializationTest() {
  }

  @Before
  public void setUp() {
    assertNotNull(resources);
    resources.init();
  }

  @After
  public void tearDown() {
  }

  @Test
  public void deserializePositionsFromJSON() throws Exception {
    PdcClassificationEntity theExpectedClassification = aPdcClassificationEntity();
    JSONJAXBContext context = new JSONJAXBContext(PdcClassificationEntity.class,
            PdcPositionEntity.class, PdcPositionValue.class);
    JSONUnmarshaller um = new JSONUnmarshallerImpl(context, JSONConfiguration.DEFAULT);
    PdcClassificationEntity classification = um.unmarshalFromJSON(
            new StringReader(resources.toJSON(theExpectedClassification)),
            PdcClassificationEntity.class);
    assertNotNull(classification);
    assertThat(classification, is(equalTo(theExpectedClassification)));
  }

  private PdcClassificationEntity aPdcClassificationEntity() throws ThesaurusException {
    PdcClassification classification = aPdcClassification().onResource(CONTENT_ID).inComponent(
            COMPONENT_INSTANCE_ID);
    return resources.toWebEntity(classification, aUser());
  }

  private UserDetail aUser() {
    UserDetail user = new UserDetail();
    user.setFirstName("Toto");
    user.setLastName("Chez-les-papoos");
    user.setId("2");
    return user;
  }
}
