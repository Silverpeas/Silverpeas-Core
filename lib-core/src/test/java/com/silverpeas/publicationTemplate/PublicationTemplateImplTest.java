/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.silverpeas.publicationTemplate;

import java.io.File;
import com.silverpeas.form.RecordTemplate;
import com.silverpeas.util.PathTestUtil;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author ehugonnet
 */
public class PublicationTemplateImplTest {

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
  }

  @After
  public void tearDown() {
  }

  @Test
  public void testGetRecordTemplateSimple() throws Exception {
    String xmlFileName =  "data.xml";
    PublicationTemplateManager.mappingRecordTemplateFilePath =  PathTestUtil.TARGET_DIR
        + "test-classes"+ File.separatorChar + "templateRepository" + File.separatorChar + "mapping"
        + File.separatorChar + "templateMapping.xml";

    PublicationTemplateManager.templateDir = PathTestUtil.TARGET_DIR + "test-classes"+ File.separatorChar + "templateRepository"
        + File.separatorChar + "template";
    System.out.println("getRecordTemplate");
    PublicationTemplateImpl instance = new PublicationTemplateImpl();
    instance.setDataFileName(xmlFileName);
    instance.setFileName("personne.xml");
    RecordTemplate result = instance.getRecordTemplate();
  }
}
