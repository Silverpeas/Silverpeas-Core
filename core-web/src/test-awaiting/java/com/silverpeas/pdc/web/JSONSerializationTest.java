/*
 * Copyright (C) 2000 - 2016 Silverpeas
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

package com.silverpeas.pdc.web;

import com.silverpeas.pdc.model.PdcClassification;
import javax.inject.Inject;
import com.silverpeas.thesaurus.ThesaurusException;
import com.sun.jersey.api.json.JSONConfiguration;
import com.sun.jersey.api.json.JSONJAXBContext;
import com.sun.jersey.api.json.JSONUnmarshaller;
import com.sun.jersey.json.impl.JSONUnmarshallerImpl;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import java.io.StringReader;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;
import static org.hamcrest.Matchers.*;
import static com.silverpeas.pdc.web.TestConstants.*;
import static com.silverpeas.pdc.web.beans.PdcClassificationBuilder.*;
import static com.silverpeas.pdc.web.matchers.PdcClassificationEntityMatcher.*;

/**
 * Unit tests on the serialization/deserialization of Pdc entities in/from JSON by using the
 * Jersey JSON marshaller.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "/spring-pdc-webservice.xml")
public class JSONSerializationTest {

  @Inject
  private PdcTestResources resources;

  public JSONSerializationTest() {
  }

  @Before
  public void setUp() {
    assertNotNull(resources);
  }

  @After
  public void tearDown() {
  }

  @Test
  public void deserializePositionsFromJSON() throws Exception {
    PdcClassificationEntity theExpectedClassification = aPdcClassificationEntity();
    JSONJAXBContext context = new JSONJAXBContext(PdcClassificationEntity.class,
            PdcPositionEntity.class, PdcPositionValueEntity.class);
    JSONUnmarshaller um = new JSONUnmarshallerImpl(context, JSONConfiguration.DEFAULT);
    PdcClassificationEntity classification = um.unmarshalFromJSON(
            new StringReader(resources.toJSON(theExpectedClassification)),
            PdcClassificationEntity.class);
    assertNotNull(classification);
    assertThat(classification, is(equalTo(theExpectedClassification)));
  }

  private PdcClassificationEntity aPdcClassificationEntity() throws ThesaurusException {
    PdcClassification classification = aPdcClassification().onContent(CONTENT_ID).inComponent(
            COMPONENT_INSTANCE_ID).build();
    return resources.toWebEntity(classification, resources.aUser());
  }
}
