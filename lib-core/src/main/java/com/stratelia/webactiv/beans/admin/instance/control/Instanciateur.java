/**
 * Copyright (C) 2000 - 2009 Silverpeas
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
 * "http://repository.silverpeas.com/legal/licensing"
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
 * instanciateur.java
 *
 * Created on 13 juillet 2000, 09:33
 */

package com.stratelia.webactiv.beans.admin.instance.control;

/**
 *
 * @author  akhadrou
 * @version
 */

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.util.*;

import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.util.ResourceLocator;
import com.stratelia.webactiv.util.exception.SilverpeasException;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.io.filefilter.NameFileFilter;
import org.apache.commons.io.filefilter.TrueFileFilter;

public class Instanciateur extends Object {

  private static ResourceLocator resources = null;
  private static String xmlPackage = "";

  private static Connection m_Connection = null;
  private static String m_sSpaceId = "";
  private static String m_sComponentId = "";
  private static String m_sUserId = "";
  private static Hashtable<String, WAComponent> WAComponents = new Hashtable<String, WAComponent>();

  // Init Function
  static {
    try {
      resources = new ResourceLocator(
          "com.stratelia.webactiv.beans.admin.instance.control.instanciator",
          "");
      xmlPackage = resources.getString("xmlPackage");
      xmlPackage = xmlPackage.trim();

      buildWAComponentList();
    } catch (Exception mre) {
      SilverTrace.fatal("admin", "Instanciateur.static",
          "admin.MSG_INSTANCIATEUR_RESOURCES_NOT_FOUND", mre);
    }
  }

  /** Creates new instantiator */
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
      } catch (IOException e) {
        throw new InstanciationException("Instanciateur.instantiateComponentName",
            InstanciationException.FATAL, e.getMessage(), e);
      }
      waComponent = new WAComponent(fullPath);
    }
    instantiateComponent(waComponent);
  }

  @SuppressWarnings("unchecked")
  public void instantiateComponent(WAComponent wac)
      throws InstanciationException {
    try {
      SilverTrace.info("admin", "Instanciateur.instantiateComponent",
          "admin.MSG_INFO_INSTANCIATE_COMPONENT", wac.toString());
      Class c = Class.forName(wac.getInstanceClassName());
      ComponentsInstanciatorIntf myInstantiator = (ComponentsInstanciatorIntf) c
          .newInstance();
      myInstantiator
          .create(m_Connection, m_sSpaceId, m_sComponentId, m_sUserId);
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

  public void unInstantiateComponentName(String componentName)
      throws InstanciationException {
    String fullPath = null;
    try {
      fullPath = getDescriptorFullPath(componentName);
    } catch (IOException e) {
      throw new InstanciationException("Instanciateur.unInstantiateComponentName",
          InstanciationException.FATAL, e.getMessage(), e);
    }
    unInstantiateComponent(new WAComponent(fullPath));
  }

  @SuppressWarnings("unchecked")
  public void unInstantiateComponent(WAComponent wac) throws InstanciationException {
    try {
      SilverTrace.info("admin", "Instanciateur.unInstantiateComponent",
          "admin.MSG_INFO_UNINSTANCIATE_COMPONENT", wac.toString());
      Class c = Class.forName(wac.getInstanceClassName());
      ComponentsInstanciatorIntf myInstantiator = (ComponentsInstanciatorIntf) c
          .newInstance();
      myInstantiator
          .delete(m_Connection, m_sSpaceId, m_sComponentId, m_sUserId);
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

  public static WAComponent getWAComponent(String componentName) {
    return WAComponents.get(componentName);
  }

  public static Hashtable<String, WAComponent> getWAComponents() {
    return WAComponents;
  }

  public static Map<String, String> getAllComponentsNames() {
    Map<String, String> hComponents = new HashMap<String, String>();

    Enumeration<WAComponent> e = WAComponents.elements();
    while (e.hasMoreElements()) {
      WAComponent waComponent = e.nextElement();
      hComponents.put(waComponent.getName(), waComponent.getLabel());
    }

    return hComponents;
  }

  public static List<WAComponent> getVisibleComponentsForPersonalSpace() {
    List<WAComponent> visibleComponents = new ArrayList<WAComponent>();
    Enumeration<WAComponent> e = WAComponents.elements();
    while (e.hasMoreElements()) {
      WAComponent component = e.nextElement();
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
  public static void rebuildWAComponentCache() throws InstanciationException {
    // Synchronised on WAComponents because
    // will erase and rebuild them
    //
    synchronized (WAComponents) {
      WAComponents.clear();

      try {
        buildWAComponentList();
      } catch (IOException e) {
        SilverTrace.fatal("admin", "Instanciateur.rebuildWAComponentCache()",
            "admin.MSG_INSTANCIATEUR_RESOURCES_NOT_FOUND", e);
        throw new InstanciationException(
            "Instanciateur.rebuildWAComponentCache()",
            SilverpeasException.FATAL, "admin.EX_ERR_INSTANTIATE_COMPONENTS", e);
      }
    }
  }

  private static Collection<File> getFileList() {
    return FileUtils.listFiles(new File(xmlPackage), new String[] { "xml" }, true);
  }

  private static String getDescriptorFullPath(String componentName) throws IOException {
    IOFileFilter filter = new NameFileFilter(componentName + ".xml");
    List<File> list = new ArrayList<File>(FileUtils.listFiles(new File(xmlPackage), filter,
        TrueFileFilter.INSTANCE));
    if (!list.isEmpty()) {
      return list.get(0).getCanonicalPath();
    }
    return new File(xmlPackage, componentName + ".xml").getCanonicalPath();
  }

  private static void buildWAComponentList() throws IOException {
    Collection<File> files = getFileList();

    for (File xmlFile : files) {
      String componentName = FilenameUtils.getBaseName(xmlFile.getName());
      String fullPath = xmlFile.getCanonicalPath();
      SilverTrace.info("admin", "Instanciateur.buildWAComponentList",
          "admin.MSG_INFO_BUILD_WA_COMPONENT_LIST", "component name: '"
          + componentName + "', full path: '" + fullPath + "'");
      WAComponents.put(componentName, new WAComponent(fullPath));
    }
  }

  /**
   * Get the directory where the component descriptors are stored
   * @return the path to the directory
   */
  public static String getXMLPackage() {
    return xmlPackage;
  }
}