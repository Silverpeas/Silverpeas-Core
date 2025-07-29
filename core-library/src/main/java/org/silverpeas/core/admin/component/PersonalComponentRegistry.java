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
 * FLOSS exception. You should have received a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package org.silverpeas.core.admin.component;

import org.apache.commons.io.FilenameUtils;
import org.silverpeas.kernel.SilverpeasRuntimeException;
import org.silverpeas.core.admin.component.model.PersonalComponent;
import org.silverpeas.core.annotation.Service;
import org.silverpeas.core.initialization.Initialization;
import org.silverpeas.core.util.ServiceProvider;
import org.silverpeas.kernel.util.SystemWrapper;

import javax.inject.Singleton;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * A registry of Web Application Personal Components available in Silverpeas.
 * </p>
 * The {@code PersonalComponent} available in Silverpeas are defined by an XML descriptor located in
 * the
 * <code>SILVERPEAS_HOME/xmlcomponents</code> directory. The descriptors that must satisfy the XSD
 * <a href="http://www.silverpeas.org/xsd/component.xsd">component.xsd</a>. The PersonalComponent
 * instances are then constructed from their XML descriptor by the registry at initialization time
 * so that they are available by the core component management service.
 * @author mmoquillon
 */
@Service
@Singleton
public class PersonalComponentRegistry implements Initialization {

  private static final int MAX_DEPTH = 2;
  private final Map<String, PersonalComponent> componentsByName = new HashMap<>();

  PersonalComponentRegistry() {
  }

  /**
   * Get the directory where the personal component descriptors are stored.
   * @return the path to the directory
   */
  private static Path getPersonalComponentDescriptorHome() {
    SystemWrapper system = SystemWrapper.getInstance();
    return Paths.get(system.getenv("SILVERPEAS_HOME"), "xmlcomponents", "personals");
  }

  private static PersonalComponent loadComponent(File descriptor) {
    try {
      JAXBContext context = JAXBContext.newInstance("org.silverpeas.core.admin.component.model");
      Unmarshaller unmarshaller = context.createUnmarshaller();
      try (InputStream in = new FileInputStream(descriptor)) {
        XMLInputFactory factory = XMLInputFactory.newFactory();
        return (unmarshaller.unmarshal(factory.createXMLStreamReader(in), PersonalComponent.class)).
            getValue();
      }
    } catch (IOException | JAXBException | XMLStreamException e) {
      throw new SilverpeasRuntimeException(e.getMessage(), e);
    }
  }

  /**
   * Gets an instance of this PersonalComponentRegistry registry.
   * @return a PersonalComponentRegistry instance.
   */
  public static PersonalComponentRegistry get() {
    return ServiceProvider.getService(PersonalComponentRegistry.class);
  }

  @Override
  public void init() {
    Path descriptorHome = getPersonalComponentDescriptorHome();
    try (Stream<Path> paths = Files.find(descriptorHome, MAX_DEPTH,
        (p, a) -> p.toFile().isFile() &&
            "xml".equalsIgnoreCase(FilenameUtils.getExtension(p.toString())))) {
      paths.forEach(p -> {
        PersonalComponent component = loadComponent(p.toFile());
        componentsByName.put(component.getName(), component);
      });
    } catch (IOException e) {
      throw new SilverpeasRuntimeException(e);
    }
  }

  /**
   * Gets the PersonalComponent instance registered under the specified name.
   * @param componentName the name of the Silverpeas personal component.
   * @return an optional PersonalComponent instance if such instance exists under the given name.
   */
  public Optional<PersonalComponent> getPersonalComponent(String componentName) {
    return Optional.ofNullable(componentsByName.get(componentName));
  }

  /**
   * Gets all the registered PersonalComponent instances indexed by their name.
   * @return a dictionary of the available PersonalComponent instances indexed by their name.
   */
  public Map<String, PersonalComponent> getAllPersonalComponents() {
    return Collections.unmodifiableMap(componentsByName);
  }
}
