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
package org.silverpeas.core.admin.component;

import org.apache.commons.io.FilenameUtils;
import org.silverpeas.core.SilverpeasRuntimeException;
import org.silverpeas.core.admin.component.model.ObjectFactory;
import org.silverpeas.core.admin.component.model.WAComponent;
import org.silverpeas.core.annotation.Service;
import org.silverpeas.core.initialization.Initialization;
import org.silverpeas.core.util.ServiceProvider;
import org.silverpeas.core.util.file.FileUtil;
import org.silverpeas.core.util.lang.SystemWrapper;

import javax.inject.Singleton;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
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
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import static org.apache.commons.io.FilenameUtils.getBaseName;

/**
 * A registry of Web Application Components available in Silverpeas.
 * </p>
 * The {@code WAComponent} available in Silverpeas are defined by an XML descriptor located in the
 * <code>SILVERPEAS_HOME/xmlcomponents</code> directory. The descriptors that must satisfy the XSD
 * <a href="http://www.silverpeas.org/xsd/component.xsd">component.xsd</a>. The WAComponent
 * instances are then constructed from their XML descriptor by the registry at initialization time
 * so that they are available by the core component management service.
 * @author mmoquillon
 */
@Service
@Singleton
public class WAComponentRegistry implements Initialization {

  private Map<String, WAComponent> componentsByName = new HashMap<>();

  /**
   * Gets an instance of this WAComponentRegistry registry.
   * @return a WAComponentRegistry instance.
   */
  public static WAComponentRegistry get() {
    return ServiceProvider.getSingleton(WAComponentRegistry.class);
  }

  WAComponentRegistry() {
  }

  /**
   * Initializes some resources required by the services or performs some initialization processes
   * at Silverpeas startup.
   * @throws Exception if an error occurs during the initialization process. In this case
   * the Silverpeas startup fails.
   */
  @Override
  public void init() throws Exception {
    Path descriptorHome = getWAComponentDescriptorHome();
    try (Stream<Path> paths = Files.find(descriptorHome, 2, (p, a) ->
        p.toFile().isFile() &&
        "xml".equalsIgnoreCase(FilenameUtils.getExtension(p.toString())) &&
        !p.getParent().toString().endsWith("personals"))) {
      paths.forEach(p -> {
        WAComponent component = loadComponent(p.toFile());
        componentsByName.put(component.getName(), component);
      });
    }
  }

  /**
   * Gets the WAComponent instance registered under the specified name.
   * @param componentName the name of the Silverpeas component.
   * @return an optional WAComponent instance if such instance exists under the given name.
   */
  public Optional<WAComponent> getWAComponent(String componentName) {
    return Optional.ofNullable(componentsByName.get(componentName));
  }

  /**
   * Gets all the registered WAComponent instances indexed by their name.
   * @return a dictionary of the available WAComponent instances indexed by their name.
   */
  public Map<String, WAComponent> getAllWAComponents() {
    return Collections.unmodifiableMap(componentsByName);
  }

  /**
   * Registers the specified workflow as a WAComponent so that it will be available among
   * the instantiable applications in Silverpeas. (A workflow that is instantiated as an
   * application instance is then taken in charge by the Silverpeas Workflow Engine.)
   *
   * @param waComponent the WAComponent instance representing a workflow application.
   * @throws SilverpeasRuntimeException if the registration failed.
   */
  public void putWorkflow(WAComponent waComponent) {
    try {
      Path descriptor =
          Paths.get(getWAComponentDescriptorHome().toString(), "workflows",
              waComponent.getName() + ".xml");
      storeComponent(waComponent, descriptor.toFile());
      componentsByName.put(waComponent.getName(), waComponent);
    } catch (JAXBException e) {
      throw new SilverpeasRuntimeException(e.getMessage(), e);
    }
  }

  public void putWorkflow(File xmlComponent) {
    WAComponent waComponent = loadComponent(xmlComponent);
    putWorkflow(waComponent);
  }

  /**
   * Removes from the registry the specified workflow application. Once removed, it will be no
   * longer available among the instantiable applications in Silverpeas.
   * </p>
   * If a such workflow application doesn't exist, nothing is done.
   * @param waComponent the WAComponent instance representing a workflow application.
   * @throws SilverpeasRuntimeException if the remove failed.
   */
  public void removeWorkflow(WAComponent waComponent) {
    try {
      if (componentsByName.containsKey(waComponent.getName())) {
        Path descriptor =
            Paths.get(getWAComponentDescriptorHome().toString(), "workflows",
                waComponent.getName() + ".xml");
        FileUtil.forceDeletion(descriptor.toFile());
        componentsByName.remove(waComponent.getName());
      }
    } catch (IOException e) {
      throw new SilverpeasRuntimeException(e.getMessage(), e);
    }
  }

  /**
   * Get the directory where the component descriptors are stored.
   * @return the path to the directory
   */
  private static Path getWAComponentDescriptorHome() {
    SystemWrapper system = SystemWrapper.get();
    return Paths.get(system.getenv("SILVERPEAS_HOME"), "xmlcomponents");
  }

  private static WAComponent loadComponent(File descriptor) {
    try {
      JAXBContext context = JAXBContext.newInstance("org.silverpeas.core.admin.component.model");
      Unmarshaller unmarshaller = context.createUnmarshaller();
      try (InputStream in = new FileInputStream(descriptor)) {
        XMLInputFactory factory = XMLInputFactory.newFactory();
        return (unmarshaller.unmarshal(factory.createXMLStreamReader(in), WAComponent.class)).
            getValue();
      }
    } catch (IOException | JAXBException | XMLStreamException e) {
      throw new SilverpeasRuntimeException(e.getMessage(), e);
    }
  }

  private static void storeComponent(WAComponent component, File descriptor) throws JAXBException {
    if (descriptor.exists()) {
      final Path descriptorCopy = Paths.get(descriptor.getParent(),
          getBaseName(descriptor.getName()).concat(".")
              .concat(LocalDateTime.now().toString().replaceAll("[^0-9]", "")));
      try {
        Files.move(descriptor.toPath(), descriptorCopy);
      } catch (IOException e) {
        throw new SilverpeasRuntimeException(e);
      }
    }
    JAXBContext context = JAXBContext.newInstance("org.silverpeas.core.admin.component.model");
    Marshaller marshaller = context.createMarshaller();
    marshaller.setProperty(Marshaller.JAXB_ENCODING, "UTF-8");
    marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
    marshaller.setProperty(Marshaller.JAXB_SCHEMA_LOCATION,
        "http://silverpeas.org/xml/ns/component http://www.silverpeas.org/xsd/component.xsd");
    ObjectFactory objectFactory = new ObjectFactory();
    marshaller.marshal(objectFactory.createWAComponent(component), descriptor);
  }
}
