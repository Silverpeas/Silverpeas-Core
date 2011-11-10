/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.silverpeas.publicationTemplate;

import com.silverpeas.form.RecordTemplate;
import com.silverpeas.form.record.GenericRecordTemplate;
import java.io.FileInputStream;
import org.exolab.castor.mapping.Mapping;
import org.exolab.castor.xml.Unmarshaller;
import org.junit.BeforeClass;
import org.junit.Test;
import org.xml.sax.InputSource;
import static com.silverpeas.publicationTemplate.Assertion.*;
import static com.silverpeas.util.PathTestUtil.*;

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
