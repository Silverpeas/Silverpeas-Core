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

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.silverpeas.core.admin.component;

import org.silverpeas.core.contribution.template.publication.PublicationTemplateManager;
import org.jglue.cdiunit.CdiRunner;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.silverpeas.core.test.rule.CommonAPI4Test;
import org.silverpeas.core.admin.component.model.Option;
import org.silverpeas.core.admin.component.model.Parameter;
import org.silverpeas.core.admin.component.model.WAComponent;
import org.silverpeas.core.admin.component.model.GlobalContext;
import org.silverpeas.core.util.lang.SystemWrapper;

import javax.inject.Inject;
import javax.inject.Provider;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import static org.apache.commons.io.FileUtils.getFile;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

/**
 * Unit test on the services provided by the WAComponentRegistry.
 * @author ehugonnet
 * @author mmoquillon
 */
@RunWith(CdiRunner.class)
public class WAComponentRegistryTest {

  private File TEMPLATES_PATH;
  private File TARGET_DIR;

  private CommonAPI4Test commonAPI4Test = new CommonAPI4Test();

  @Rule
  public CommonAPI4Test getCommonAPI4Test() {
    return commonAPI4Test;
  }

  @Inject
  private Provider<PublicationTemplateManager> managerProvider;

  @Before
  public void setup() throws Exception {
    TARGET_DIR = getFile(
        WAComponentRegistryTest.class.getProtectionDomain().getCodeSource().getLocation()
            .getFile());
    TEMPLATES_PATH = getFile(TARGET_DIR, "templateRepository");

    PublicationTemplateManager.templateDir = TEMPLATES_PATH.getPath();
    SystemWrapper.get().getenv().put("SILVERPEAS_HOME", TARGET_DIR.getPath());

    commonAPI4Test.injectIntoMockedBeanContainer(managerProvider.get());
  }

  @Test
  public void testLoadComponent() throws Exception {
    WAComponentRegistry registry = new WAComponentRegistry();
    registry.init();

    Optional<WAComponent> result = registry.getWAComponent("almanach");
    assertThat(result.isPresent(), is(true));

    WAComponent almanach = result.get();
    assertThat(almanach.getName(), is("almanach"));
    assertThat(almanach.getDescription(), hasKey("fr"));
    assertThat(almanach.getDescription(), hasKey("en"));
    assertThat(almanach.isPortlet(), is(true));
    assertThat(almanach.isVisible(), is(true));
    assertThat(almanach.isVisibleInPersonalSpace(), is(false));
    assertThat(almanach.getSuite().get("fr"), is("02 Gestion Collaborative"));
    assertThat(almanach.getParameters().size(), is(5));
    Parameter paramWithOption = null;
    for (Parameter parameter : almanach.getParameters()) {
      if ("directAccess".equals(parameter.getName())) {
        paramWithOption = parameter;
      }
    }
    assertThat(paramWithOption, is(notNullValue()));
    assertThat(paramWithOption.getOptions().size(), is(4));
  }

  @Test
  public void testLoadComponentWithXmlTemplates() throws Exception {
    WAComponentRegistry registry = new WAComponentRegistry();
    registry.init();

    Optional<WAComponent> result = registry.getWAComponent("kmelia");
    assertThat(result.isPresent(), is(true));
    WAComponent kmelia = result.get();

    assertThat(kmelia.getName(), is("kmelia"));
    assertThat(kmelia.getDescription(), hasKey("fr"));
    assertThat(kmelia.getDescription(), hasKey("en"));
    assertThat(kmelia.isPortlet(), is(true));
    assertThat(kmelia.isVisible(), is(true));
    assertThat(kmelia.isVisibleInPersonalSpace(), is(true));
    assertThat(kmelia.getSuite().get("fr"), is("01 Gestion Documentaire"));
    assertThat(kmelia.getParameters().size(), is(42));
    Parameter paramWithXMLTemplate = null;
    for (Parameter parameter : kmelia.getParameters()) {
      if ("XmlFormForFiles".equals(parameter.getName())) {
        paramWithXMLTemplate = parameter;
      }
    }
    assertThat(paramWithXMLTemplate, is(notNullValue()));
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

  @Test
  public void testLoadComponentWithOneTemplate() throws Exception {
    WAComponentRegistry registry = new WAComponentRegistry();
    registry.init();

    Optional<WAComponent> result = registry.getWAComponent("classifieds");
    assertThat(result.isPresent(), is(true));
    WAComponent classifieds = result.get();

    assertThat(classifieds.getName(), is("classifieds"));
    assertThat(classifieds.isVisibleInPersonalSpace(), is(false));
    Parameter paramWithXMLTemplate = null;
    for (Parameter parameter : classifieds.getParameters()) {
      if ("XMLFormName".equals(parameter.getName())) {
        paramWithXMLTemplate = parameter;
      }
    }
    assertThat(paramWithXMLTemplate, is(notNullValue()));
    assertThat(paramWithXMLTemplate.isXmlTemplate(), is(true));
    GlobalContext context = new GlobalContext("WA1");
    context.setComponentName("classifieds");
    PublicationTemplateManager templateManager = PublicationTemplateManager.getInstance();
    assertThat(templateManager.getPublicationTemplates(context).size(),
        is(1));
    assertThat(paramWithXMLTemplate, is(notNullValue()));
    List<Option> visibleOptions = new ArrayList<>();
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
  public void testSaveWorkflow() throws Exception {
    WAComponentRegistry registry = new WAComponentRegistry();
    registry.init();

    String componentName = "newWorkflow";
    String label = "Nouveau Workflow";

    WAComponent component = new WAComponent();
    component.setName(componentName);
    component.getLabel().put("fr", label);
    component.getDescription().put("fr", "Le nouveau workflow");
    component.getSuite().put("fr", "80 Nouvelles applications");
    component.setVisible(true);
    component.setPortlet(false);

    registry.putWorkflow(component);
    Optional<WAComponent> result = registry.getWAComponent(componentName);
    assertThat(result.isPresent(), is(true));
    assertThat(result.get().getLabel().get("fr"), is(label));
    assertThat(registry.getAllWAComponents().size(), is(5));

    Path descriptor = Paths.get(SystemWrapper.get().getenv("SILVERPEAS_HOME"), "xmlcomponents",
        "workflows", componentName + ".xml");
    assertThat(Files.exists(descriptor), is(true));
  }

  private class OptionComparator implements Comparator<Option> {

    @Override
    public int compare(Option o1, Option o2) {
      return o1.getValue().compareTo(o2.getValue());
    }

  }
}
