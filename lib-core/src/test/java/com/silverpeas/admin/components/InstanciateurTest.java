/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.silverpeas.admin.components;

import com.silverpeas.publicationTemplate.PublicationTemplateManager;
import java.io.File;
import com.silverpeas.util.PathTestUtil;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import static org.hamcrest.Matchers.*;

/**
 *
 * @author ehugonnet
 */
public class InstanciateurTest {

  public static final String MAPPINGS_PATH = PathTestUtil.TARGET_DIR
      + "test-classes" + File.separatorChar + "templateRepository" + File.separatorChar + "mapping";
  public static final String TEMPLATES_PATH = PathTestUtil.TARGET_DIR + "test-classes" + File.separatorChar + "templateRepository"
      + File.separatorChar;

  @BeforeClass
  public static void setUpClass() throws Exception {
  }

  @AfterClass
  public static void tearDownClass() throws Exception {
  }

  @Before
  public void setUp() {
    PublicationTemplateManager.mappingRecordTemplateFilePath = MAPPINGS_PATH
        + File.separatorChar + "templateMapping.xml";
    PublicationTemplateManager.mappingPublicationTemplateFilePath = MAPPINGS_PATH
        + File.separatorChar + "templateFilesMapping.xml";
    PublicationTemplateManager.templateDir = TEMPLATES_PATH;
  }

  public InstanciateurTest() {
  }

  /**
   * Test of loadComponent method, of class Instanciateur.
   * @throws Exception 
   */
  @Test
  public void testLoadComponent() throws Exception {
    String path = PathTestUtil.TARGET_DIR + "test-classes" + File.separatorChar + "xmlComponent"
        + File.separatorChar + "almanach.xml";
    Instanciateur instance = new Instanciateur();
    WAComponent result = instance.loadComponent(path);
    assertNotNull(result);
    assertThat(result.getName(), is("almanach"));
    assertThat(result.getDescription(), hasKey("fr"));
    assertThat(result.getDescription(), hasKey("en"));

    assertThat(result.getInstanceClassName(), is(
        "com.stratelia.webactiv.almanach.AlmanachInstanciator"));
    assertThat(result.isPortlet(), is(true));
    assertThat(result.isVisible(), is(true));
    assertThat(result.isVisibleInPersonalSpace(), is(false));
    assertThat(result.getSuite().get("fr"), is("02 Gestion Collaborative"));
    assertThat(result.getParameters().size(), is(5));
    Parameter paramWithOption = null;
    for (Parameter parameter : result.getParameters()) {
      if ("directAccess".equals(parameter.getName())) {
        paramWithOption = parameter;
      }
    }
    assertThat(paramWithOption, is(notNullValue()));
    assertThat(paramWithOption.getOptions().size(), is(4));
    assertThat(result.isSearchTemplating(), is(true));
  }

  /**
   * Test of loadComponent method, of class Instanciateur.
   * @throws Exception 
   */
  @Test
  public void testLoadComponentWithXmlTemplates() throws Exception {
    String path = PathTestUtil.TARGET_DIR + "test-classes" + File.separatorChar + "xmlComponent"
        + File.separatorChar + "kmelia.xml";
    Instanciateur instance = new Instanciateur();
    WAComponent result = instance.loadComponent(path);
    assertNotNull(result);
    assertThat(result.getName(), is("kmelia"));
    assertThat(result.getDescription(), hasKey("fr"));
    assertThat(result.getDescription(), hasKey("en"));

    assertThat(result.getInstanceClassName(), is(
        "com.stratelia.webactiv.kmelia.KmeliaInstanciator"));
    assertThat(result.isPortlet(), is(true));
    assertThat(result.isVisible(), is(true));
    assertThat(result.isVisibleInPersonalSpace(), is(true));
    assertThat(result.getSuite().get("fr"), is("01 Gestion Documentaire"));
    assertThat(result.getParameters().size(), is(42));
    Parameter paramWithXMLTemplate = null;
    for (Parameter parameter : result.getParameters()) {
      if ("XmlFormForFiles".equals(parameter.getName())) {
        paramWithXMLTemplate = parameter;
      }
    }
    assertThat(paramWithXMLTemplate.isXmlTemplate(), is(true));
    assertThat(PublicationTemplateManager.getInstance().getPublicationTemplates(true).size(), is(1));
    assertThat(paramWithXMLTemplate, is(notNullValue()));
    assertThat(paramWithXMLTemplate.getOptions().size(), is(1));
    assertThat(paramWithXMLTemplate.getOptions().get(0).getValue(), is("template.xml"));
    assertThat(paramWithXMLTemplate.getOptions().get(0).getName().get("fr"), is("template"));
    assertThat(result.isSearchTemplating(), is(false));
  }
}
