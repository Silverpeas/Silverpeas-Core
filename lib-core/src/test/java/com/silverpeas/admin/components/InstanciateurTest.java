/**
 * Copyright (C) 2000 - 2013 Silverpeas
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

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.silverpeas.admin.components;

import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.silverpeas.util.GlobalContext;

import com.silverpeas.publicationTemplate.PublicationTemplateManager;
import com.silverpeas.util.PathTestUtil;

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
    GlobalContext context = new GlobalContext("WA1");
    context.setComponentName("kmelia");
    assertThat(PublicationTemplateManager.getInstance().getPublicationTemplates(context).size(),
        is(1));
    assertThat(paramWithXMLTemplate, is(notNullValue()));
    List<Option> options = paramWithXMLTemplate.getOptions();
    assertThat(options.size(), is(3));
    
    Collections.sort(options, new OptionComparator());
    Option option = options.get(0);
    assertThat(option.getValue(), is("classifieds.xml"));
    assertThat(option.getName().get("fr"), is("Classifieds"));
    option = options.get(1);
    assertThat(option.getValue(), is("sandbox.xml"));
    assertThat(option.getName().get("fr"), is("Sandbox"));
    option = options.get(2);
    assertThat(option.getValue(), is("template.xml"));
    assertThat(option.getName().get("fr"), is("template"));
  }
  
  private class OptionComparator implements Comparator<Option> {

    @Override
    public int compare(Option o1, Option o2) {
      return o1.getValue().compareTo(o2.getValue());
    }
    
  }
  
  @Test
  public void testLoadComponentWithOneTemplate() throws Exception {
    String path = PathTestUtil.TARGET_DIR + "test-classes" + File.separatorChar + "xmlComponent"
        + File.separatorChar + "classifieds.xml";
    Instanciateur instance = new Instanciateur();
    WAComponent result = instance.loadComponent(path);
    assertNotNull(result);
    assertThat(result.getName(), is("classifieds"));

    assertThat(result.isVisibleInPersonalSpace(), is(false));
    Parameter paramWithXMLTemplate = null;
    for (Parameter parameter : result.getParameters()) {
      if ("XMLFormName".equals(parameter.getName())) {
        paramWithXMLTemplate = parameter;
      }
    }
    assertThat(paramWithXMLTemplate.isXmlTemplate(), is(true));
    GlobalContext context = new GlobalContext("WA1");
    context.setComponentName("classifieds");
    PublicationTemplateManager templateManager = PublicationTemplateManager.getInstance();
    assertThat(templateManager.getPublicationTemplates(context).size(),
        is(1));
    assertThat(paramWithXMLTemplate, is(notNullValue()));
    List<Option> visibleOptions = new ArrayList<Option>();
    List<Option> options = paramWithXMLTemplate.getOptions();
    for (Option option : options) {
      String templateName = option.getValue();
      if (templateManager.isPublicationTemplateVisible(templateName, context)) {
        visibleOptions.add(option);
      }
    }
    assertThat(visibleOptions.size(), is(1));
    Option option = options.get(0);
    assertThat(option.getValue(), is("classifieds.xml"));
    assertThat(option.getName().get("fr"), is("Classifieds"));
  }
  
  @Test
  public void testSaveAndLoadComponent() throws Exception {
    String componentName = "newApplication";
    String xmlComponentFileName = "newApplication.xml";
    String label = "Nouvelle application";
    WAComponent component = new WAComponent();
    component.setName(componentName);
    component.getLabel().put("fr", label);
    component.getDescription().put("fr", "La nouvelle application");
    component.getSuite().put("fr", "80 Nouvelles applications");
    component.setVisible(true);
    component.setPortlet(false);
    component.setInstanceClassName("org.silverpeas.components.newly.Instanciator");
    Instanciateur.saveComponent(component, xmlComponentFileName);
    
    Instanciateur.rebuildWAComponentCache();
    
    String path = Instanciateur.getDescriptorFullPath(componentName);
    Instanciateur instance = new Instanciateur();
    WAComponent loadedComponent = instance.loadComponent(path);
    assertEquals(label, loadedComponent.getLabel().get("fr"));
    
    Map<String, WAComponent> components = Instanciateur.getWAComponents();
    assertEquals(4, components.size());
    component = Instanciateur.getWAComponent(componentName);
    assertEquals(label, component.getLabel().get("fr"));
  }
}
