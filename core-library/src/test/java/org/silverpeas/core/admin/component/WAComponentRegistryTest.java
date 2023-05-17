/*
 * Copyright (C) 2000 - 2022 Silverpeas
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
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.silverpeas.core.admin.component;

import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.silverpeas.core.admin.component.model.ComponentSpaceProfileMapping;
import org.silverpeas.core.admin.component.model.GlobalContext;
import org.silverpeas.core.admin.component.model.Option;
import org.silverpeas.core.admin.component.model.Parameter;
import org.silverpeas.core.admin.component.model.Profile;
import org.silverpeas.core.admin.component.model.WAComponent;
import org.silverpeas.core.contribution.template.publication.PublicationTemplateManager;
import org.silverpeas.core.test.unit.extention.EnableSilverTestEnv;
import org.silverpeas.core.test.unit.extention.TestManagedBeans;
import org.silverpeas.core.util.lang.SystemWrapper;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

/**
 * Unit test on the services provided by the WAComponentRegistry.
 * @author ehugonnet
 * @author mmoquillon
 */
@EnableSilverTestEnv
@TestManagedBeans(PublicationTemplateManager.class)
class WAComponentRegistryTest {

  private static final String NEW_WORKFLOW_COMPONENT_NAME = "newWorkflow";
  private WAComponentRegistry registry;

  @BeforeEach
  void setup() throws Exception {
    // Tested registry
    registry = new WAComponentRegistry();
    registry.init();
  }

  @AfterEach
  void clear() throws Exception {
    streamComponentDescriptors(NEW_WORKFLOW_COMPONENT_NAME).map(Path::toFile)
        .forEach(FileUtils::deleteQuietly);
  }

  @Test
  void testLoadAlmanachComponent() {
    Optional<WAComponent> result = registry.getWAComponent("almanach");
    assertThat(result.isPresent(), is(true));

    WAComponent almanach = result.get();
    assertThat(almanach.getName(), is("almanach"));
    assertThat(almanach.getDescription(), hasKey("fr"));
    assertThat(almanach.getDescription(), hasKey("en"));
    assertThat(almanach.isPortlet(), is(true));
    assertThat(almanach.isInheritSpaceRightsByDefault(), is(true));
    assertThat(almanach.isPublicByDefault(), is(false));
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
    final List<Profile> profiles = almanach.getProfiles();
    assertThat(profiles.size(), is(3));
    Profile profile = profiles.get(0);
    assertThat(profile.getName(), is("admin"));
    assertThat(profile.getHelp("en"), nullValue());
    assertThat(profile.getSpaceProfileMapping(), notNullValue());
    assertThat(profile.getSpaceProfileMapping().getProfiles().size(), is(1));
    assertThat(profile.getSpaceProfileMapping().getProfiles().get(0).getValue(),
        is(profile.getName()));
  }

  @Test
  void testLoadTestComponent() {
    Optional<WAComponent> result = registry.getWAComponent("testComponent");
    assertThat(result.isPresent(), is(true));

    WAComponent component = result.get();
    assertThat(component.getName(), is("testComponent"));
    assertThat(component.getDescription(), hasKey("fr"));
    assertThat(component.getDescription(), hasKey("en"));
    assertThat(component.isPortlet(), is(true));
    assertThat(component.isInheritSpaceRightsByDefault(), is(false));
    assertThat(component.isPublicByDefault(), is(true));
    assertThat(component.isVisible(), is(true));
    assertThat(component.isVisibleInPersonalSpace(), is(false));
    assertThat(component.getSuite().get("fr"), is("10 Tests unitaires"));
    assertThat(component.getParameters(), empty());
    final List<Profile> profiles = component.getProfiles();
    assertThat(profiles.size(), is(3));
    Profile profile = profiles.get(0);
    assertThat(profile.getName(), is("admin"));
    assertThat(profile.getHelp("en"), nullValue());
    ComponentSpaceProfileMapping spaceMapping = profile.getSpaceProfileMapping();
    assertThat(spaceMapping, notNullValue());
    assertThat(spaceMapping.getProfiles().size(), is(1));
    assertThat(spaceMapping.getProfiles().get(0).getValue(), is("admin"));
    profile = profiles.get(2);
    assertThat(profile.getName(), is("user"));
    assertThat(profile.getHelp("en"), is("Reader (read only)"));
    spaceMapping = profile.getSpaceProfileMapping();
    assertThat(spaceMapping, notNullValue());
    assertThat(spaceMapping.getProfiles().size(), is(2));
    assertThat(spaceMapping.getProfiles().get(0).getValue(), is("writer"));
    assertThat(spaceMapping.getProfiles().get(1).getValue(), is("reader"));
  }

  @Test
  void testLoadComponentWithXmlTemplates() throws Exception {
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
    assertThat(kmelia.getParameters().size(), is(40));
    Parameter paramWithXMLTemplate = null;
    Parameter versionControl = null;
    for (Parameter parameter : kmelia.getParameters()) {
      if ("XmlFormForFiles".equals(parameter.getName())) {
        paramWithXMLTemplate = parameter;
      }
      if ("versionControl".equals(parameter.getName())) {
        versionControl = parameter;
      }
    }
    assertThat(paramWithXMLTemplate, is(notNullValue()));
    assertThat(paramWithXMLTemplate.getWarning().isPresent(), is(false));
    assertThat(paramWithXMLTemplate.isXmlTemplate(), is(true));
    GlobalContext context = new GlobalContext("WA1");
    context.setComponentName("kmelia");
    assertThat(PublicationTemplateManager.getInstance().getPublicationTemplates(context).size(),
        is(1));
    assertThat(paramWithXMLTemplate, is(notNullValue()));
    List<Option> options = paramWithXMLTemplate.getOptions();
    assertThat(options.size(), is(3));
    options.sort(new OptionComparator());
    Option option = options.get(0);
    assertThat(option.getValue(), is("classifieds.xml"));
    assertThat(option.getName().get("fr"), is("Classifieds"));
    option = options.get(1);
    assertThat(option.getValue(), is("sandbox.xml"));
    assertThat(option.getName().get("fr"), is("Sandbox"));
    option = options.get(2);
    assertThat(option.getValue(), is("template.xml"));
    assertThat(option.getName().get("fr"), is("template"));

    assertThat(versionControl, is(notNullValue()));
    assertThat(versionControl.getWarning().isPresent(), is(true));
    assertThat(versionControl.getWarning().get().isAlways(), is(true));
    assertThat(versionControl.getWarning().get().getMessages(), aMapWithSize(3));
  }

  @Test
  void testLoadComponentWithOneTemplate() throws Exception {
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
  void testSaveWorkflow() throws Exception {
    final String componentName = NEW_WORKFLOW_COMPONENT_NAME;
    final Path expectedDescriptor =
        Paths.get(getWorkflowRepoPath().toString(), componentName + ".xml");
    assertThat(Files.exists(expectedDescriptor), is(false));
    assertThat(streamComponentDescriptors(componentName).count(), is(0L));
    String label = "Nouveau Workflow";

    WAComponent component = new WAComponent();
    component.setName(componentName);
    component.getLabel().put("fr", label);
    component.getDescription().put("fr", "Le nouveau workflow");
    component.getSuite().put("fr", "80 Nouvelles applications");
    component.setPublicByDefault(true);
    component.setInheritSpaceRightsByDefault(false);
    component.setVisible(true);
    component.setPortlet(false);

    registry.putWorkflow(component);
    Optional<WAComponent> result = registry.getWAComponent(componentName);
    assertThat(result.isPresent(), is(true));
    assertThat(result.get().getLabel().get("fr"), is(label));
    assertThat(registry.getAllWAComponents().size(), is(6));
    assertThat(Files.exists(expectedDescriptor), is(true));
    assertThat(streamComponentDescriptors(componentName).count(), is(1L));

    // saving when already existing makes a copy
    registry.putWorkflow(component);
    assertThat(Files.exists(expectedDescriptor), is(true));
    assertThat(streamComponentDescriptors(componentName).count(), is(2L));
  }

  private Path getWorkflowRepoPath() {
    return Paths.get(SystemWrapper.get().getenv("SILVERPEAS_HOME"), "xmlcomponents", "workflows");
  }

  private Stream<Path> streamComponentDescriptors(final String componentName) throws IOException {
    //noinspection resource
    return Files.list(getWorkflowRepoPath())
        .filter(p -> p.getFileName().toString().contains(componentName));
  }

  private static class OptionComparator implements Comparator<Option> {

    @Override
    public int compare(Option o1, Option o2) {
      return o1.getValue().compareTo(o2.getValue());
    }

  }
}
