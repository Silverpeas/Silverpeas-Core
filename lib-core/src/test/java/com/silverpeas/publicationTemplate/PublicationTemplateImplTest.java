/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.silverpeas.publicationTemplate;

import java.io.File;
import com.silverpeas.form.RecordTemplate;
import com.silverpeas.form.record.GenericRecordTemplate;
import com.silverpeas.util.PathTestUtil;
import java.io.FileInputStream;
import org.exolab.castor.mapping.Mapping;
import org.exolab.castor.xml.Unmarshaller;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.xml.sax.InputSource;
import static com.silverpeas.publicationTemplate.Assertion.*;

/**
 *
 * @author ehugonnet
 */
public class PublicationTemplateImplTest {

  public static final String MAPPINGS_PATH = PathTestUtil.TARGET_DIR
          + "test-classes" + File.separatorChar + "templateRepository" + File.separatorChar + "mapping";
  public static final String TEMPLATES_PATH = PathTestUtil.TARGET_DIR + "test-classes" + File.separatorChar + "templateRepository"
          + File.separatorChar + "template";

  public PublicationTemplateImplTest() {
  }

  @BeforeClass
  public static void setUpClass() throws Exception {
  }

  @AfterClass
  public static void tearDownClass() throws Exception {
  }

  @Before
  public void setUp() {
    PublicationTemplateManager.mappingRecordTemplateFilePath = MAPPINGS_PATH +
            File.separatorChar + "templateMapping.xml";
    PublicationTemplateManager.mappingPublicationTemplateFilePath = MAPPINGS_PATH +
            File.separatorChar + "templateFilesMapping.xml";
    PublicationTemplateManager.templateDir = TEMPLATES_PATH;
  }

  @After
  public void tearDown() {
  }

  @Test
  public void testGetRecordTemplateSimple() throws Exception {
    System.out.println("getRecordTemplate");
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
    RecordTemplate template = null;

    Mapping mapping = new Mapping();
    mapping.loadMapping(MAPPINGS_PATH + File.separatorChar + "templateMapping.xml");
    Unmarshaller unmarshaller = new Unmarshaller(mapping);
    template = (GenericRecordTemplate) unmarshaller.unmarshal(new InputSource(new FileInputStream(
            TEMPLATES_PATH + File.separatorChar + XmlFileName)));

    return template;
  }
}
