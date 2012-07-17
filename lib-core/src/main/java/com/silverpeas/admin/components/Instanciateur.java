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

package com.silverpeas.admin.components;

/**
 *
 * @author akhadrou
 * @version
 */
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
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
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.io.filefilter.NameFileFilter;
import org.apache.commons.io.filefilter.TrueFileFilter;

import com.silverpeas.util.i18n.I18NHelper;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.util.ResourceLocator;
import com.stratelia.webactiv.util.exception.SilverpeasException;

public class Instanciateur {

  private final static ResourceLocator resources = new ResourceLocator(
      "com.stratelia.webactiv.beans.admin.instance.control.instanciator", "");
  private final static String xmlPackage = resources.getString("xmlPackage").trim();
  private static Connection m_Connection = null;
  private static String m_sSpaceId = "";
  private static String m_sComponentId = "";
  private static String m_sUserId = "";
  private final static Map<String, WAComponent> WAComponents = new HashMap<String, WAComponent>();
  private static final ObjectFactory factory = new ObjectFactory();

  // Init Function
  static {
    try {
      buildWAComponentList();
    } catch (Exception mre) {
      SilverTrace.fatal("admin", "Instanciateur.static",
          "admin.MSG_INSTANCIATEUR_RESOURCES_NOT_FOUND", mre);
    }
  }

  /**
   * Creates new instantiator
   */
  public Instanciateur() {
  }

  public Connection getConnection() {
    return m_Connection;
  }

  public void setConnection(Connection connection) {
    m_Connection = connection;
  }

  public String getSpaceId() {
    return m_sSpaceId;
  }

  public void setSpaceId(String sSpaceId) {
    m_sSpaceId = sSpaceId;
  }

  public String getComponentId() {
    return m_sComponentId;
  }

  public void setComponentId(String sComponentId) {
    m_sComponentId = sComponentId;
  }

  public String getUserId() {
    return m_sUserId;
  }

  public void setUserId(String sUserId) {
    m_sUserId = sUserId;
  }

  public void instantiateComponentName(String componentName)
      throws InstanciationException {
    WAComponent waComponent = getWAComponent(componentName);
    if (waComponent == null) {
      // load dynamically new component descriptor (not loaded on startup)
      String fullPath = null;
      try {
        fullPath = getDescriptorFullPath(componentName);
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

  public void instantiateComponent(WAComponent wac)
      throws InstanciationException {
    try {
      SilverTrace.info("admin", "Instanciateur.instantiateComponent",
          "admin.MSG_INFO_INSTANCIATE_COMPONENT", wac.toString());
      Class c = Class.forName(wac.getInstanceClassName());
      ComponentsInstanciatorIntf myInstantiator = (ComponentsInstanciatorIntf) c.newInstance();
      myInstantiator.create(m_Connection, m_sSpaceId, m_sComponentId, m_sUserId);
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
    String fullPath = null;
    try {
      fullPath = getDescriptorFullPath(componentName);
      unInstantiateComponent(loadComponent(fullPath));
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

  public void unInstantiateComponent(
      WAComponent wac) throws InstanciationException {
    try {
      SilverTrace.info("admin", "Instanciateur.unInstantiateComponent",
          "admin.MSG_INFO_UNINSTANCIATE_COMPONENT", wac.toString());
      Class c = Class.forName(wac.getInstanceClassName());
      ComponentsInstanciatorIntf myInstantiator = (ComponentsInstanciatorIntf) c.newInstance();
      myInstantiator.delete(m_Connection, m_sSpaceId, m_sComponentId, m_sUserId);
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
    return WAComponents.get(componentName);
  }

  public synchronized static Map<String, WAComponent> getWAComponents() {
    return Collections.unmodifiableMap(WAComponents);
  }

  public synchronized static Map<String, String> getAllComponentsNames() {
    Map<String, String> hComponents = new HashMap<String, String>();
    Collection<WAComponent> components = WAComponents.values();
    for (WAComponent component : components) {
      hComponents.put(component.getName(), component.getLabel().get(I18NHelper.defaultLanguage));
    }
    return hComponents;
  }

  public synchronized static List<WAComponent> getVisibleComponentsForPersonalSpace() {
    List<WAComponent> visibleComponents = new ArrayList<WAComponent>();
    Collection<WAComponent> components = WAComponents.values();
    for (WAComponent component : components) {
      if (component.isVisibleInPersonalSpace()) {
        visibleComponents.add(component);
      }
    }
    return visibleComponents;
  }

  /**
   * Method reads the WAComponent descriptor files again and rebuild the component descriptor cache
   * @throws InstanciationException when something goes wrong
   */
  public synchronized static void rebuildWAComponentCache() throws InstanciationException {
    WAComponents.clear();
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
    return FileUtils.listFiles(new File(xmlPackage), new String[] { "xml" }, true);
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
    JAXBContext context = JAXBContext.newInstance("com.silverpeas.admin.components");
    XMLInputFactory factory = XMLInputFactory.newFactory();
    Unmarshaller unmarshaller = context.createUnmarshaller();
    for (File xmlFile : files) {
      String componentName = FilenameUtils.getBaseName(xmlFile.getName());
      String fullPath = xmlFile.getCanonicalPath();
      SilverTrace.info("admin", "Instanciateur.buildWAComponentList",
          "admin.MSG_INFO_BUILD_WA_COMPONENT_LIST", "component name: '"
          + componentName + "', full path: '" + fullPath + "'");
      WAComponents.put(componentName, (unmarshaller.unmarshal(factory.createXMLStreamReader(
          new FileInputStream(xmlFile)), WAComponent.class)).getValue());
    }
  }

  WAComponent loadComponent(String path) throws IOException, JAXBException,
      XMLStreamException {
    JAXBContext context = JAXBContext.newInstance("com.silverpeas.admin.components");
    XMLInputFactory factory = XMLInputFactory.newFactory();
    Unmarshaller unmarshaller = context.createUnmarshaller();
    File file = new File(path);
    return (unmarshaller.unmarshal(factory.createXMLStreamReader(new FileInputStream(file)),
        WAComponent.class)).getValue();

  }

  public static void saveComponent(WAComponent waComponent, String fileName) throws JAXBException {
    saveComponent(waComponent, fileName, false);
  }
  
  public static void saveComponent(WAComponent waComponent, String fileName, boolean workflow) throws JAXBException {
    JAXBContext context = JAXBContext.newInstance("com.silverpeas.admin.components");
    Marshaller marshaller = context.createMarshaller();
    marshaller.setProperty(Marshaller.JAXB_SCHEMA_LOCATION, "http://silverpeas.org/xml/ns/component http://www.silverpeas.org/xsd/component.xsd");
    marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
    String path = getXMLPackage() + File.separatorChar;
    if (workflow) {
      path += "workflows" + File.separatorChar;
    }
    File file = new File(path + fileName);    
    marshaller.marshal(factory.createWAComponent(waComponent), file);
  }

  /**
   * Get the directory where the component descriptors are stored
   * @return the path to the directory
   */
  public static String getXMLPackage() {
    return xmlPackage;
  }
}