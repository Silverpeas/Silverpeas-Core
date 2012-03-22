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
  }
}
