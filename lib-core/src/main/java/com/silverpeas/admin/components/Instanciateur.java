/**
 * Copyright (C) 2000 - 2013 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU Affero General Public License as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of the GPL, you may
 * redistribute this Program in connection with Free/Libre Open Source Software ("FLOSS")
 * applications as described in Silverpeas's FLOSS exception. You should have received a copy of the
 * text describing the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License along with this program.
 * If not, see <http://www.gnu.org/licenses/>.
 */
package com.silverpeas.admin.components;

/**
 *
 * @author akhadrou
 * @version
 */
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.io.filefilter.NameFileFilter;
import org.apache.commons.io.filefilter.TrueFileFilter;

import com.silverpeas.util.i18n.I18NHelper;

import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.util.ResourceLocator;
import com.stratelia.webactiv.util.exception.SilverpeasException;

public class Instanciateur {

  private final static ResourceLocator resources = new ResourceLocator(
      "org.silverpeas.beans.admin.instance.control.instanciator", "");
  private final static String xmlPackage = resources.getString("xmlPackage").trim();
  private static Connection connection = null;
  private static String spaceId = "";
  private static String componentId = "";
  private static String userId = "";
  private final static Map<String, WAComponent> componentsByName =
      new HashMap<String, WAComponent>();
  private static final ObjectFactory objectFactory = new ObjectFactory();
  private static final XMLInputFactory factory = XMLInputFactory.newFactory();

  // Init Function
  static {
    try {
      buildWAComponentList();
    } catch (Exception mre) {
      SilverTrace.fatal("admin", "Instanciateur.static",
          "admin.MSG_INSTANCIATEUR_RESOURCES_NOT_FOUND", mre);
    }
  }

  public static void addWAComponentForTest(WAComponent component) {
    componentsByName.put(component.getName(), component);
  }

  /**
   * Creates new instantiator
   */
  public Instanciateur() {
  }

  public Connection getConnection() {
    return connection;
  }

  public void setConnection(Connection connection) {
    this.connection = connection;
  }

  public String getSpaceId() {
    return spaceId;
  }

  public void setSpaceId(String sSpaceId) {
    this.spaceId = sSpaceId;
  }

  public String getComponentId() {
    return componentId;
  }

  public void setComponentId(String sComponentId) {
    this.componentId = sComponentId;
  }

  public String getUserId() {
    return userId;
  }

  public void setUserId(String sUserId) {
    this.userId = sUserId;
  }

  public void instantiateComponentName(String componentName) throws InstanciationException {
    WAComponent waComponent = getWAComponent(componentName);
    if (waComponent == null) {
      // load dynamically new component descriptor (not loaded on startup)
      try {
        String fullPath = getDescriptorFullPath(componentName);
        waComponent = loadComponent(fullPath);
      } catch (IOException e) {
        throw new InstanciationException("Instanciateur.instantiateComponentName",
            InstanciationException.FATAL, e.getMessage(), e);
      } catch (JAXBException e) {
        throw new InstanciationException("Instanciateur.instantiateComponentName",
            InstanciationException.FATAL, e.getMessage(), e);
      } catch (XMLStreamException e) {
        throw new InstanciationException("Instanciateur.instantiateComponentName",
            InstanciationException.FATAL, e.getMessage(), e);
      }

    }
    instantiateComponent(waComponent);
  }

  public void instantiateComponent(WAComponent wac) throws InstanciationException {
    try {
      SilverTrace.info("admin", "Instanciateur.instantiateComponent",
          "admin.MSG_INFO_INSTANCIATE_COMPONENT", wac.toString());
      Class c = Class.forName(wac.getInstanceClassName());
      ComponentsInstanciatorIntf myInstantiator = (ComponentsInstanciatorIntf) c.newInstance();
      myInstantiator.create(connection, spaceId, componentId, userId);
    } catch (ClassNotFoundException cnfe) {
      throw new InstanciationException("Instanciateur.instantiateComponent",
          SilverpeasException.FATAL, "root.EX_CLASS_NOT_FOUND", cnfe);
    } catch (InstantiationException ie) {
      throw new InstanciationException("Instanciateur.instantiateComponent",
          SilverpeasException.FATAL, "root.EX_INSTANTIATION", ie);
    } catch (IllegalAccessException iae) {
      throw new InstanciationException("Instanciateur.instantiateComponent",
          SilverpeasException.FATAL, "root.EX_ILLEGAL_ACCESS", iae);
    }
  }

  public void unInstantiateComponentName(String componentName) throws InstanciationException {
    try {
      WAComponent component = componentsByName.get(componentName);
      if (component == null) {
        String fullPath = getDescriptorFullPath(componentName);
        component = loadComponent(fullPath);
      }
      unInstantiateComponent(component);
    } catch (IOException e) {
      throw new InstanciationException("Instanciateur.unInstantiateComponentName",
          InstanciationException.FATAL, e.getMessage(), e);
    } catch (JAXBException e) {
      throw new InstanciationException("Instanciateur.unInstantiateComponentName",
          InstanciationException.FATAL, e.getMessage(), e);
    } catch (XMLStreamException e) {
      throw new InstanciationException("Instanciateur.unInstantiateComponentName",
          InstanciationException.FATAL, e.getMessage(), e);
    }

  }

  public void unInstantiateComponent(WAComponent wac) throws InstanciationException {
    try {
      SilverTrace.info("admin", "Instanciateur.unInstantiateComponent",
          "admin.MSG_INFO_UNINSTANCIATE_COMPONENT", wac.toString());
      Class c = Class.forName(wac.getInstanceClassName());
      ComponentsInstanciatorIntf myInstantiator = (ComponentsInstanciatorIntf) c.newInstance();
      myInstantiator.delete(connection, spaceId, componentId, userId);
    } catch (ClassNotFoundException cnfe) {
      throw new InstanciationException("Instanciateur.unInstantiateComponent",
          SilverpeasException.FATAL, "root.EX_CLASS_NOT_FOUND", cnfe);
    } catch (InstantiationException ie) {
      throw new InstanciationException("Instanciateur.unInstantiateComponent",
          SilverpeasException.FATAL, "root.EX_INSTANTIATION", ie);
    } catch (IllegalAccessException iae) {
      throw new InstanciationException("Instanciateur.unInstantiateComponent",
          SilverpeasException.FATAL, "root.EX_ILLEGAL_ACCESS", iae);
    }
  }

  public synchronized static WAComponent getWAComponent(String componentName) {
    return componentsByName.get(componentName);
  }

  public static boolean isWorkflow(String componentName) {
    WAComponent descriptor = getWAComponent(componentName);
    if (descriptor != null && "RprocessManager".equalsIgnoreCase(descriptor.getRouter())) {
      return true;
    }
    return false;
  }

  public synchronized static Map<String, WAComponent> getWAComponents() {
    return Collections.unmodifiableMap(componentsByName);
  }

  public synchronized static Map<String, String> getAllComponentsNames() {
    Map<String, String> hComponents = new HashMap<String, String>();
    Collection<WAComponent> components = componentsByName.values();
    for (WAComponent component : components) {
      hComponents.put(component.getName(), component.getLabel().get(I18NHelper.defaultLanguage));
    }
    return hComponents;
  }

  public synchronized static List<WAComponent> getVisibleComponentsForPersonalSpace() {
    List<WAComponent> visibleComponents = new ArrayList<WAComponent>();
    Collection<WAComponent> components = componentsByName.values();
    for (WAComponent component : components) {
      if (component.isVisibleInPersonalSpace()) {
        visibleComponents.add(component);
      }
    }
    return visibleComponents;
  }

  /**
   * Method reads the WAComponent descriptor files again and rebuild the component descriptor cache
   *
   * @throws InstanciationException when something goes wrong
   */
  public synchronized static void rebuildWAComponentCache() throws InstanciationException {
    componentsByName.clear();
    try {
      buildWAComponentList();
    } catch (IOException e) {
      SilverTrace.fatal("admin", "Instanciateur.rebuildWAComponentCache()",
          "admin.MSG_INSTANCIATEUR_RESOURCES_NOT_FOUND", e);
      throw new InstanciationException("Instanciateur.rebuildWAComponentCache()",
          SilverpeasException.FATAL, "admin.EX_ERR_INSTANTIATE_COMPONENTS", e);
    } catch (JAXBException e) {
      SilverTrace.fatal("admin", "Instanciateur.rebuildWAComponentCache()",
          "admin.MSG_INSTANCIATEUR_RESOURCES_NOT_FOUND", e);
      throw new InstanciationException("Instanciateur.rebuildWAComponentCache()",
          SilverpeasException.FATAL, "admin.EX_ERR_INSTANTIATE_COMPONENTS", e);
    } catch (XMLStreamException e) {
      SilverTrace.fatal("admin", "Instanciateur.rebuildWAComponentCache()",
          "admin.MSG_INSTANCIATEUR_RESOURCES_NOT_FOUND", e);
      throw new InstanciationException("Instanciateur.rebuildWAComponentCache()",
          SilverpeasException.FATAL, "admin.EX_ERR_INSTANTIATE_COMPONENTS", e);
    }
  }

  private static Collection<File> getFileList() {
    return FileUtils.listFiles(new File(xmlPackage), new String[]{"xml"}, true);
  }

  static String getDescriptorFullPath(String componentName) throws IOException {
    IOFileFilter filter = new NameFileFilter(componentName + ".xml");
    List<File> list = new ArrayList<File>(FileUtils.listFiles(new File(xmlPackage), filter,
        TrueFileFilter.INSTANCE));
    if (!list.isEmpty()) {
      return list.get(0).getCanonicalPath();
    }
    return new File(xmlPackage, componentName + ".xml").getCanonicalPath();
  }

  private synchronized static void buildWAComponentList() throws IOException, JAXBException,
      XMLStreamException {
    Collection<File> files = getFileList();
    for (File xmlFile : files) {
      String componentName = FilenameUtils.getBaseName(xmlFile.getName());
      SilverTrace.info("admin", "Instanciateur.buildWAComponentList",
          "admin.MSG_INFO_BUILD_WA_COMPONENT_LIST", "component name: '"
          + componentName + "', full path: '" + xmlFile.getCanonicalPath() + "'");
      componentsByName.put(componentName, loadComponent(xmlFile));
    }
  }

  static WAComponent loadComponent(File file) throws IOException, JAXBException,
      XMLStreamException {
    JAXBContext context = JAXBContext.newInstance("com.silverpeas.admin.components");
    Unmarshaller unmarshaller = context.createUnmarshaller();
    InputStream in = new FileInputStream(file);
    try {
      return (unmarshaller.unmarshal(factory.createXMLStreamReader(in), WAComponent.class)).
          getValue();
    } finally {
      IOUtils.closeQuietly(in);
    }

  }

  WAComponent loadComponent(String path) throws IOException, JAXBException, XMLStreamException {
    File file = new File(path);
    return loadComponent(file);
  }

  public static void saveComponent(WAComponent waComponent, String fileName) throws JAXBException {
    saveComponent(waComponent, fileName, false);
  }

  public static void saveComponent(WAComponent waComponent, String fileName, boolean workflow)
      throws JAXBException {
    JAXBContext context = JAXBContext.newInstance("com.silverpeas.admin.components");
    Marshaller marshaller = context.createMarshaller();
    marshaller.setProperty(Marshaller.JAXB_SCHEMA_LOCATION,
        "http://silverpeas.org/xml/ns/component http://www.silverpeas.org/xsd/component.xsd");
    marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
    String path = getXMLPackage() + File.separatorChar;
    if (workflow) {
      path += "workflows" + File.separatorChar;
    }
    File file = new File(path + fileName);
    marshaller.marshal(objectFactory.createWAComponent(waComponent), file);
  }

  /**
   * Get the directory where the component descriptors are stored
   *
   * @return the path to the directory
   */
  public static String getXMLPackage() {
    return xmlPackage;
  }
}
