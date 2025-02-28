/*
 * Copyright (C) 2000 - 2024 Silverpeas
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
import org.silverpeas.core.admin.component.model.LocalizedProfile;
import org.silverpeas.core.admin.component.model.LocalizedWAComponent;
import org.silverpeas.core.admin.component.model.Message;
import org.silverpeas.core.admin.component.model.Option;
import org.silverpeas.core.admin.component.model.Parameter;
import org.silverpeas.core.admin.component.model.Profile;
import org.silverpeas.core.admin.component.model.WAComponent;
import org.silverpeas.core.admin.service.OrganizationController;
import org.silverpeas.core.contribution.template.publication.PublicationTemplateManager;
import org.silverpeas.core.test.unit.extention.JEETestContext;
import org.silverpeas.kernel.test.annotations.TestManagedMocks;
import org.silverpeas.kernel.test.extension.EnableSilverTestEnv;
import org.silverpeas.kernel.test.annotations.TestManagedBeans;
import org.silverpeas.kernel.util.SystemWrapper;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import static org.apache.commons.lang3.reflect.FieldUtils.readDeclaredField;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

/**
 * Unit test on the services provided by the WAComponentRegistry.
 * @author ehugonnet
 * @author mmoquillon
 */
@EnableSilverTestEnv(context = JEETestContext.class)
@TestManagedBeans(PublicationTemplateManager.class)
@TestManagedMocks(OrganizationController.class)
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
    LocalizedWAComponent frAlmanach = new LocalizedWAComponent(almanach, "fr");
    LocalizedWAComponent enAlmanach = new LocalizedWAComponent(almanach, "en");
    assertThat(almanach.getName(), is("almanach"));
    assertThat(frAlmanach.getDescription(), not(emptyString()));
    assertThat(enAlmanach.getDescription(), not(emptyString()));
    assertThat(frAlmanach.getDescription(), not(is(enAlmanach.getDescription())));
    assertThat(almanach.isPortlet(), is(true));
    assertThat(almanach.isInheritSpaceRightsByDefault(), is(true));
    assertThat(almanach.isPublicByDefault(), is(false));
    assertThat(almanach.isVisible(), is(true));
    assertThat(almanach.isVisibleInPersonalSpace(), is(false));
    assertThat(frAlmanach.getSuite(), is("02 Gestion Collaborative"));
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
    LocalizedProfile enProfile = enAlmanach.getProfile("admin");
    assertThat(enProfile, notNullValue());
    assertThat(profile.getName(), is("admin"));
    assertThat(profile.getName(), is(enProfile.getName()));
    assertThat(enProfile.getHelp(), nullValue());
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
    LocalizedWAComponent frComponent = new LocalizedWAComponent(component, "fr");
    LocalizedWAComponent enComponent = new LocalizedWAComponent(component, "en");
    assertThat(component.getName(), is("testComponent"));
    assertThat(frComponent.getDescription(), not(emptyString()));
    assertThat(enComponent.getDescription(), not(emptyString()));
    assertThat(frComponent.getDescription(), not(is(enComponent.getDescription())));
    assertThat(component.isPortlet(), is(true));
    assertThat(component.isInheritSpaceRightsByDefault(), is(false));
    assertThat(component.isPublicByDefault(), is(true));
    assertThat(component.isVisible(), is(true));
    assertThat(component.isVisibleInPersonalSpace(), is(false));
    assertThat(frComponent.getSuite(), is("10 Tests unitaires"));
    assertThat(component.getParameters(), empty());
    final List<Profile> profiles = component.getProfiles();
    assertThat(profiles.size(), is(3));
    Profile profile = profiles.get(0);
    LocalizedProfile enProfile = enComponent.getProfile("admin");
    assertThat(profile.getName(), is("admin"));
    assertThat(profile.getName(), is(enProfile.getName()));
    assertThat(enProfile.getHelp(), nullValue());
    ComponentSpaceProfileMapping spaceMapping = profile.getSpaceProfileMapping();
    assertThat(spaceMapping, notNullValue());
    assertThat(spaceMapping.getProfiles().size(), is(1));
    assertThat(spaceMapping.getProfiles().get(0).getValue(), is("admin"));
    profile = profiles.get(2);
    enProfile = enComponent.getProfile("user");
    assertThat(profile.getName(), is("user"));
    assertThat(profile.getName(), is(enProfile.getName()));
    assertThat(enProfile.getHelp(), is("Reader (read only)"));
    spaceMapping = profile.getSpaceProfileMapping();
    assertThat(spaceMapping, notNullValue());
    assertThat(spaceMapping.getProfiles().size(), is(2));
    assertThat(spaceMapping.getProfiles().get(0).getValue(), is("writer"));
    assertThat(spaceMapping.getProfiles().get(1).getValue(), is("reader"));
  }

  @SuppressWarnings("unchecked")
  @Test
  void testLoadComponentWithXmlTemplates() throws Exception {
    Optional<WAComponent> result = registry.getWAComponent("kmelia");
    assertThat(result.isPresent(), is(true));
    WAComponent kmelia = result.get();
    LocalizedWAComponent frKmelia = new LocalizedWAComponent(kmelia, "fr");
    LocalizedWAComponent enKmelia = new LocalizedWAComponent(kmelia, "en");
    assertThat(kmelia.getName(), is("kmelia"));
    assertThat(frKmelia.getDescription(), not(emptyString()));
    assertThat(enKmelia.getDescription(), not(emptyString()));
    assertThat(frKmelia.getDescription(), not(is(enKmelia.getDescription())));
    assertThat(kmelia.isPortlet(), is(true));
    assertThat(kmelia.isVisible(), is(true));
    assertThat(kmelia.isVisibleInPersonalSpace(), is(true));
    assertThat(frKmelia.getSuite(), is("01 Gestion Documentaire"));
    assertThat(kmelia.getParameters().size(), is(39));
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
    Map<String, String> optionalLabels = (Map<String, String>) readDeclaredField(option, "name", true);
    assertThat(option.getValue(), is("classifieds.xml"));
    assertThat(optionalLabels.get("fr"), is("Classifieds"));
    option = options.get(1);
    optionalLabels = (Map<String, String>) readDeclaredField(option, "name", true);
    assertThat(option.getValue(), is("sandbox.xml"));
    assertThat(optionalLabels.get("fr"), is("Sandbox"));
    option = options.get(2);
    optionalLabels = (Map<String, String>) readDeclaredField(option, "name", true);
    assertThat(option.getValue(), is("template.xml"));
    assertThat(optionalLabels.get("fr"), is("template"));

    assertThat(versionControl, is(notNullValue()));
    assertThat(versionControl.getWarning().isPresent(), is(true));
    assertThat(versionControl.getWarning().get().isAlways(), is(true));
    final List<Message> message = (List<Message>) readDeclaredField(
        versionControl.getWarning().get(), "message", true);
    assertThat(message, hasSize(3));
  }

  @SuppressWarnings("unchecked")
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
    Map<String, String> optionalLabels = (Map<String, String>) readDeclaredField(option, "name", true);
    assertThat(option.getValue(), is("classifieds.xml"));
    assertThat(optionalLabels.get("fr"), is("Classifieds"));
  }

  @Test
  void testSaveWorkflow() throws Exception {
    final String componentName = NEW_WORKFLOW_COMPONENT_NAME;
    final Path expectedDescriptor =
        Paths.get(getWorkflowRepoPath().toString(), componentName + ".xml");
    assertThat(Files.exists(expectedDescriptor), is(false));
    assertThat(streamComponentDescriptors(componentName).count(), is(0L));
    String label = "Nouveau Workflow";
    String description = "Le nouveau workflow";
    String suite = "80 Nouvelles applications";

    WAComponent component = new WAComponent();
    component.setName(componentName);
    component.putLabel("fr", label);
    component.putDescription("fr", description);
    component.putSuite("fr", suite);
    component.setPublicByDefault(true);
    component.setInheritSpaceRightsByDefault(false);
    component.setVisible(true);
    component.setPortlet(false);

    registry.putWorkflow(component);
    Optional<WAComponent> result = registry.getWAComponent(componentName);
    assertThat(result.isPresent(), is(true));
    LocalizedWAComponent frComponent = new LocalizedWAComponent(result.get(), "fr");
    assertThat(frComponent.getLabel(), is(label));
    assertThat(frComponent.getDescription(), is(description));
    assertThat(frComponent.getSuite(), is(suite));
    assertThat(Files.exists(expectedDescriptor), is(true));
    assertThat(streamComponentDescriptors(componentName).count(), is(1L));

    // saving when already existing makes a copy
    registry.putWorkflow(component);
    assertThat(Files.exists(expectedDescriptor), is(true));
    assertThat(streamComponentDescriptors(componentName).count(), is(2L));
  }

  private Path getWorkflowRepoPath() {
    return Paths.get(SystemWrapper.getInstance().getenv("SILVERPEAS_HOME"), "xmlcomponents", "workflows");
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
