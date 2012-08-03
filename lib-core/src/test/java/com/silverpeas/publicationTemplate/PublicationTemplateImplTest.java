/**
 * Copyright (C) 2000 - 2012 Silverpeas
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
package com.silverpeas.publicationTemplate;

import java.io.FileInputStream;

import org.exolab.castor.mapping.Mapping;
import org.exolab.castor.xml.Unmarshaller;
import org.junit.BeforeClass;
import org.junit.Test;
import org.xml.sax.InputSource;

import com.silverpeas.form.RecordTemplate;
import com.silverpeas.form.record.GenericRecordTemplate;

import static com.silverpeas.publicationTemplate.Assertion.assertEquals;
import static com.silverpeas.util.PathTestUtil.SEPARATOR;
import static com.silverpeas.util.PathTestUtil.TARGET_DIR;

/**
 *
 * @author ehugonnet
 */
public class PublicationTemplateImplTest {

  public static final String MAPPINGS_PATH = TARGET_DIR
          + "test-classes" + SEPARATOR + "templateRepository" + SEPARATOR + "mapping";
  public static final String TEMPLATES_PATH = TARGET_DIR + "test-classes" + SEPARATOR + "templateRepository"
          + SEPARATOR + "template";

  public PublicationTemplateImplTest() {
  }

  @BeforeClass
  public static void setUpClass() throws Exception {
        PublicationTemplateManager.mappingRecordTemplateFilePath = MAPPINGS_PATH +
            SEPARATOR + "templateMapping.xml";
    PublicationTemplateManager.mappingPublicationTemplateFilePath = MAPPINGS_PATH +
            SEPARATOR + "templateFilesMapping.xml";
    PublicationTemplateManager.templateDir = TEMPLATES_PATH;
  }

  @Test
  public void testGetRecordTemplateSimple() throws Exception {
    String xmlFileName = "data.xml";
    RecordTemplate expectedTemplate = getExpectedRecordTemplate(xmlFileName);
    PublicationTemplateImpl instance = new PublicationTemplateImpl();
    instance.setDataFileName(xmlFileName);
    instance.setFileName("personne.xml");
    RecordTemplate result = instance.getRecordTemplate();
    assertEquals(expectedTemplate, result);

  }

  /**
   * Gets the expected record template from the specified file in which it is serialized in XML.
   * @param XmlFileName the name of the file in which is serialized the record template.
   * @return the record template that is serialized in the specified XML file.
   * @throws Exception if the record template cannot be deserialized.
   */
  private RecordTemplate getExpectedRecordTemplate(final String XmlFileName) throws Exception {
    Mapping mapping = new Mapping();
    mapping.loadMapping(MAPPINGS_PATH + SEPARATOR + "templateMapping.xml");
    Unmarshaller unmarshaller = new Unmarshaller(mapping);
    RecordTemplate template = (GenericRecordTemplate) unmarshaller.unmarshal(new InputSource(
            new FileInputStream(TEMPLATES_PATH + SEPARATOR + XmlFileName)));
    return template;
  }
}
