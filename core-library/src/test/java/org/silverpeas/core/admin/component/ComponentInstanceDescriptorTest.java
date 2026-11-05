/*
 * Copyright (C) 2000 - 2026 Silverpeas
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
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package org.silverpeas.core.admin.component;

import java.io.StringReader;
import java.util.List;
import jakarta.xml.bind.JAXBContext;
import javax.xml.transform.stream.StreamSource;
import org.junit.jupiter.api.Test;
import org.silverpeas.core.admin.component.model.*;
import static org.junit.jupiter.api.Assertions.*;

class ComponentInstanceDescriptorTest {
  @Test
  void xmlDescriptorsDefaultToUnlimitedAndCanDeclareAnyNonNegativeLimit() throws Exception {
    var reader = JAXBContext.newInstance(WAComponent.class).createUnmarshaller();
    for (int maximum : List.of(0,1,3)) {
      String field = maximum == 0 ? "" : "<maxInstancesPerSpace>" + maximum + "</maxInstancesPerSpace>";
      var descriptor = reader.unmarshal(new StreamSource(new StringReader(
          "<WAComponent xmlns='http://silverpeas.org/xml/ns/component'><name>app</name>" + field + "</WAComponent>")), WAComponent.class).getValue();
      assertEquals(maximum, descriptor.getMaxInstancesPerSpace());
    }
    assertThrows(IllegalArgumentException.class, () -> new WAComponent().setMaxInstancesPerSpace(-1));
  }

  @Test
  void catalogueLimitSupportsOneAndManyInstancesWithoutCountingOtherTypesOrRemovedInstances() {
    var descriptor = new WAComponent(); descriptor.setName("app"); descriptor.setMaxInstancesPerSpace(1);
    var existing = new ComponentInst(); existing.setName("app"); existing.setLocalId(12);
    var other = new ComponentInst(); other.setName("other"); other.setLocalId(13);
    var limited = ComponentInstanceAvailability.from(descriptor, List.of(existing,other));
    assertFalse(limited.isAvailable()); assertEquals("app12", limited.getExistingInstanceId());
    var removed = new ComponentInst(); removed.setName("app"); removed.setLocalId(14); removed.setStatus(ComponentInst.STATUS_REMOVED);
    assertTrue(ComponentInstanceAvailability.from(descriptor,List.of(removed,other)).isAvailable());
    descriptor.setMaxInstancesPerSpace(2);
    assertTrue(ComponentInstanceAvailability.from(descriptor, List.of(existing,other)).isAvailable());
    descriptor.setMaxInstancesPerSpace(0);
    assertTrue(ComponentInstanceAvailability.from(descriptor, List.of(existing)).isAvailable());
  }
  @Test
  void platformDescriptorsAndCatalogueAreGenericAndIndependentOfTheSelectedSpace() throws Exception {
    var reader = JAXBContext.newInstance(WAComponent.class).createUnmarshaller();
    var descriptor = reader.unmarshal(new StreamSource(new StringReader(
        "<WAComponent xmlns='http://silverpeas.org/xml/ns/component'><name>global</name>"
        + "<maxInstancesPerPlatform>1</maxInstancesPerPlatform></WAComponent>")),WAComponent.class).getValue();
    assertEquals(1,descriptor.getMaxInstancesPerPlatform());
    assertEquals(0,descriptor.getMaxInstancesPerSpace());
    var availability = ComponentInstanceAvailability.from(descriptor,List.of(),List.of("global12"));
    assertFalse(availability.isAvailable()); assertTrue(availability.isPlatform());
    assertEquals("global12",availability.getExistingInstanceId());
    var available = ComponentInstanceAvailability.from(descriptor,List.of(),List.of());
    assertTrue(available.isAvailable()); assertEquals(1,available.getMaximum()); assertTrue(available.isPlatform());
    assertThrows(IllegalArgumentException.class, () -> descriptor.setMaxInstancesPerPlatform(-1));
  }

  @Test
  void bundledSchemaAcceptsBothLimitsAndRejectsNegativeValues() throws Exception {
    var schema = javax.xml.validation.SchemaFactory.newInstance(javax.xml.XMLConstants.W3C_XML_SCHEMA_NS_URI)
        .newSchema(getClass().getResource("component.xsd"));
    var validator = schema.newValidator();
    String descriptor = "<WAComponent xmlns='http://silverpeas.org/xml/ns/component'><name>app</name>"
        + "<visible>true</visible><portlet>false</portlet><profiles/>"
        + "<maxInstancesPerSpace>2</maxInstancesPerSpace><maxInstancesPerPlatform>5</maxInstancesPerPlatform></WAComponent>";
    validator.validate(new StreamSource(new StringReader(descriptor)));
    assertThrows(org.xml.sax.SAXException.class, () -> validator.validate(
        new StreamSource(new StringReader(descriptor.replace(">2<",">-1<")))));
  }

}
