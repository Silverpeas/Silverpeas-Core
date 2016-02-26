/*
 * CDDL HEADER START
 * The contents of this file are subject to the terms
 * of the Common Development and Distribution License
 * (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at
 * http://www.sun.com/cddl/cddl.html and legal/CDDLv1.0.txt
 * See the License for the specific language governing
 * permission and limitations under the License.
 *
 * When distributing Covered Code, include this CDDL
 * Header Notice in each file and include the License file
 * at legal/CDDLv1.0.txt.
 * If applicable, add the following below the CDDL Header,
 * with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Copyright 2006 Sun Microsystems Inc. All Rights Reserved
 * CDDL HEADER END
 */
package com.sun.portal.portletcontainer.admin;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.jar.JarFile;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;

import com.sun.portal.container.EntityID;
import com.sun.portal.container.PortletID;
import com.sun.portal.container.PortletLang;
import com.sun.portal.portletcontainer.admin.registry.PortletApp;
import com.sun.portal.portletcontainer.admin.registry.PortletAppRegistryWriter;
import com.sun.portal.portletcontainer.admin.registry.PortletRegistryTags;
import com.sun.portal.portletcontainer.admin.registry.PortletWindow;
import com.sun.portal.portletcontainer.admin.registry.PortletWindowPreference;
import com.sun.portal.portletcontainer.admin.registry.PortletWindowPreferenceRegistryWriter;
import com.sun.portal.portletcontainer.admin.registry.PortletWindowRegistryContext;
import com.sun.portal.portletcontainer.admin.registry.PortletWindowRegistryContextImpl;
import com.sun.portal.portletcontainer.admin.registry.PortletWindowRegistryWriter;
import com.sun.portal.portletcontainer.common.PortletDeployConfigReader;
import com.sun.portal.portletcontainer.common.PortletPreferencesUtility;
import com.sun.portal.portletcontainer.common.descriptor.DeploymentDescriptorException;
import com.sun.portal.portletcontainer.common.descriptor.DeploymentDescriptorReader;
import com.sun.portal.portletcontainer.common.descriptor.PortletAppDescriptor;
import com.sun.portal.portletcontainer.common.descriptor.PortletDescriptor;
import com.sun.portal.portletcontainer.common.descriptor.PortletInfoDescriptor;
import com.sun.portal.portletcontainer.common.descriptor.PortletPreferencesDescriptor;
import com.sun.portal.portletcontainer.common.descriptor.PortletsDescriptor;
import com.sun.portal.portletcontainer.common.descriptor.PreferenceDescriptor;
import com.sun.portal.portletcontainer.common.descriptor.SecurityConstraintDescriptor;
import com.sun.portal.portletcontainer.common.descriptor.SecurityRoleRefDescriptor;
import com.sun.portal.portletcontainer.common.descriptor.SupportsDescriptor;
import com.sun.portal.portletcontainer.common.descriptor.UserAttributeDescriptor;
import com.sun.portal.portletcontainer.context.registry.PortletRegistryContext;
import com.sun.portal.portletcontainer.context.registry.PortletRegistryContextAbstractFactory;
import com.sun.portal.portletcontainer.context.registry.PortletRegistryContextFactory;
import com.sun.portal.portletcontainer.context.registry.PortletRegistryException;
import com.sun.portal.portletcontainer.warupdater.PortletWebAppUpdater;

/**
 * PortletRegistryGenerator is responsible for parsing the portlet.xml using the
 * DeploymentDescriptorReader and generating Portlet Registry Elements like PortletAppRegistry,
 * PortletWindowRegistry and PortletWindowPreferenceRegistry
 */
public class PortletRegistryGenerator implements PortletRegistryTags {
  private static final String WEB_INF_PREFIX = "WEB-INF" + "/";
  private static final String WEB_XML = "web.xml";

  private static final String PORTLET_XML = "portlet.xml";
  private static final String SUN_PORTLET_XML = "sun-portlet.xml";
  private static final String DD_SUFFIX = "_portlet.xml";
  private static final String WAR_SUFFIX = ".war";

  private PortletsDescriptor portletsDescriptor;
  private PortletAppDescriptor portletAppDescriptor;
  private String portletAppName;
  private String warName;
  private Properties configProps = null;
  private List<PortletRegistryElement> portletAppElementList;
  private List<PortletRegistryElement> portletWindowElementList;
  private List<PortletRegistryElement> portletWindowPreferenceElementList;

  // Create a logger for this class
  private static Logger logger = Logger.getLogger("com.sun.portal.portletcontainer.admin",
      "org.silverpeas.portlets.PALogMessages");

  public PortletRegistryGenerator() {
    configProps = new Properties();
    portletAppElementList = new ArrayList();
    portletWindowElementList = new ArrayList();
    portletWindowPreferenceElementList = new ArrayList();
  }

  // get the portlet.xml as InputStream
  private InputStream getPortletXmlStream(JarFile jar) throws Exception {
    InputStream in = null;
    try {
      ZipEntry portletXMLEntry = jar.getEntry(WEB_INF_PREFIX + PORTLET_XML);
      in = jar.getInputStream(portletXMLEntry);
    } catch (IOException ioe) {
      Object[] tokens = { jar.getName() };
      throw new PortletRegistryException("errorStreamRead", ioe, tokens);
    } catch (Exception ex) {
      String[] tokens = { WEB_INF_PREFIX + PORTLET_XML };
      throw new PortletRegistryException("invalidWar", ex, tokens);
    }
    return in;
  }

  private List<String> getWebAppRoles(JarFile jar) throws Exception {
    InputStream webXMLStream = null;
    List<String> roles = new ArrayList<>();
    try {
      ZipEntry webXMLEntry = jar.getEntry(WEB_INF_PREFIX + WEB_XML);
      webXMLStream = jar.getInputStream(webXMLEntry);
      roles = PortletWebAppUpdater.getRoles(webXMLStream);
    } catch (IOException ioe) {
      throw new PortletRegistryException("errorGettingRoles", ioe);
    } catch (Exception ex) {
      String[] tokens = { WEB_INF_PREFIX + WEB_XML };
      throw new PortletRegistryException("invalidWar", ex, tokens);
    } finally {
      try {
        if (webXMLStream != null) {
          webXMLStream.close();
        }
      } catch (IOException ignored) {
      }
    }
    return roles;
  }

  public void register(File updatedArchiveFile, String warFileLocation,
      Properties roleProperties, Properties userInfoProperties,
      PortletLang portletLang) throws Exception {
    JarFile jar = new JarFile(updatedArchiveFile);
    String configFileLocation = PortletRegistryHelper.getConfigFileLocation();
    DeploymentDescriptorReader ddReader = new DeploymentDescriptorReader(
        configFileLocation);

    String registryLocation = PortletRegistryHelper.getRegistryLocation();
    logger.log(Level.FINE, "PSPL_CSPPAM0006", registryLocation);

    warName = updatedArchiveFile.getName();
    logger.log(Level.FINE, "PSPL_CSPPAM0005", warName);
    portletAppName = warName.substring(0, warName.lastIndexOf('.'));

    InputStream portletXmlStream = null;
    try {
      portletXmlStream = getPortletXmlStream(jar);
      portletAppDescriptor = ddReader.loadPortletAppDescriptor(portletAppName,
          portletXmlStream);
      ddReader.processDeployPortletExtensionDescriptor(updatedArchiveFile,
          configFileLocation);
    } catch (DeploymentDescriptorException dde) {
      Object[] tokens = { dde.toString() };
      throw new PortletRegistryException("errorReadingPortletDD", tokens);
    } finally {
      try {
        if (portletXmlStream != null) {
          portletXmlStream.close();
        }
      } catch (IOException ignored) {
      }
    }
    portletsDescriptor = portletAppDescriptor.getPortletsDescriptor();
    createPortletRegistryElements(roleProperties, userInfoProperties,
        getWebAppRoles(jar), portletLang);

    PortletRegistryWriter portletAppRegistryWriter = new PortletAppRegistryWriter(
        registryLocation);
    portletAppRegistryWriter.appendDocument(portletAppElementList);
    logger.log(Level.FINE, "PSPL_CSPPAM0010", "portlet-app-registry.xml");

    PortletRegistryWriter portletWindowRegistryWriter = new PortletWindowRegistryWriter(
        registryLocation, null);
    portletWindowRegistryWriter.appendDocument(portletWindowElementList);
    logger.log(Level.FINE, "PSPL_CSPPAM0010", "portlet-window-registry.xml");

    PortletRegistryWriter portletWindowPreferenceRegistryWriter =
        new PortletWindowPreferenceRegistryWriter(
        registryLocation, null);
    portletWindowPreferenceRegistryWriter
        .appendDocument(portletWindowPreferenceElementList);
    logger.log(Level.FINE, "PSPL_CSPPAM0010",
        "portlet-window-preference-registry.xml");

    try {
      portletXmlStream = getPortletXmlStream(jar);
      copyPortletXML(portletXmlStream, warFileLocation, portletAppName);
    } catch (IOException ioe) {
      throw new PortletRegistryException("errorSavingFile", ioe);
    } finally {
      try {
        if (portletXmlStream != null) {
          portletXmlStream.close();
        }
      } catch (IOException ignored) {
      }
    }
  }

  public String getPortletAppName() {
    return portletAppName;
  }

  public String getPortletWarName() {
    return warName;
  }

  private void createPortletRegistryElements(Properties roleProperties,
      Properties userInfoProperties, List webAppRoles, PortletLang portletLang)
      throws PortletRegistryException {
    List<PortletDescriptor> portletDescriptors = portletsDescriptor.getPortletDescriptors();
    PortletRegistryElement portletApp, portletWindow, portletWindowPreference;
    if (portletDescriptors.isEmpty()) {
      Object[] tokens = { "portlet.xml" };
      throw new PortletRegistryException("invalidWar", tokens);
    }
    for (PortletDescriptor portletDescriptor: portletDescriptors) {
      // Instantiate the objects required to write to the registry files
      portletApp = new PortletApp();
      portletWindow = new PortletWindow();
      portletWindowPreference = new PortletWindowPreference();

      portletAppElementList.add(portletApp);
      portletWindowElementList.add(portletWindow);
      portletWindowPreferenceElementList.add(portletWindowPreference);

      String portletName = portletDescriptor.getPortletName();
      logger.log(Level.FINE, "PSPL_CSPPAM0007", portletName);

      PortletID portletID = new PortletID(getPortletAppName(), portletName);
      String portletIDValue = portletID.toString();
      logger.log(Level.FINEST, "PSPL_CSPPAM0008", portletIDValue);
      portletApp.setPortletName(portletIDValue);
      portletApp.setName(portletIDValue);
      portletApp.setStringProperty(ARCHIVE_NAME_KEY, getPortletWarName());
      portletApp.setStringProperty(ARCHIVE_TYPE_KEY, getPortletWarName().substring(
          getPortletWarName().lastIndexOf('.') + 1));

      portletWindow.setPortletName(portletIDValue);
      portletWindow.setName(portletIDValue);
      portletWindow.setRemote(Boolean.FALSE.toString());
      portletWindow.setLang(portletLang.toString());

      portletWindowPreference.setPortletName(portletIDValue);
      portletWindowPreference.setName(portletIDValue);

      EntityID entityID = new EntityID(portletID);
      String entityIDPrefix = entityID.getPrefix();
      logger.log(Level.FINE, "PSPL_CSPPAM0009", entityIDPrefix);
      portletWindow.setStringProperty(ENTITY_ID_PREFIX_KEY, entityIDPrefix);

      PortletInfoDescriptor portletInfo = portletDescriptor.getPortletInfoDescriptor();
      String title = portletName;
      String shortTitle = null;
      List keywords = null;

      if (portletInfo != null) {
        title = portletInfo.getTitle();
        shortTitle = portletInfo.getShortTitle();
        keywords = portletInfo.getKeywords();
      }
      portletApp.setStringProperty(TITLE_KEY, title);
      portletApp.setStringProperty(SHORT_TITLE_KEY, shortTitle);
      portletApp.setCollectionProperty(KEYWORDS_KEY, keywords);
      portletWindow.setStringProperty(TITLE_KEY, title);
      portletWindow.setStringProperty(VISIBLE_KEY, "false");
      // When created through deploy, its thick

      String description = "";
      if (portletDescriptor.getDescription() != null) {
        description = portletDescriptor.getDescription();
      }
      portletApp.setStringProperty(DESCRIPTION_KEY, description);

      PortletPreferencesDescriptor ppd = portletDescriptor.getPortletPreferencesDescriptor();
      List preferenceDescriptors = null;
      if (ppd != null) {
        preferenceDescriptors = ppd.getPreferenceDescriptors();
        if (preferenceDescriptors != null && !preferenceDescriptors.isEmpty()) {
          Map<String, Object> preferences = new HashMap<>();
          Map<String, Object> preferencesReadOnly = new HashMap<>();
          for (int j = 0; j < preferenceDescriptors.size(); j++) {
            PreferenceDescriptor prd = (PreferenceDescriptor) preferenceDescriptors.get(j);
            String name = prd.getPrefName();
            List values = prd.getPrefValues();
            String value = PortletPreferencesUtility.getPreferenceString(values);
            preferences.put(name, value);
            String isReadOnly = String.valueOf(prd.getReadOnly());
            preferencesReadOnly.put(name, isReadOnly);
          }
          portletWindowPreference.setCollectionProperty(PREFERENCE_PROPERTIES_KEY, preferences);
          portletWindowPreference.setCollectionProperty(PREFERENCE_READ_ONLY_KEY,
              preferencesReadOnly);
        }
      }

      // Create roleMapping collection
      // validate roles
      List<SecurityRoleRefDescriptor> securityRoleRefDescriptors =
          portletDescriptor.getSecurityRoleRefDescriptors();
      if (securityRoleRefDescriptors != null && securityRoleRefDescriptors.size() > 0) {
        List<String> roles = new ArrayList<>();
        for (SecurityRoleRefDescriptor srd: securityRoleRefDescriptors) {
          String roleLink = srd.getRoleLink();
          String portletRole = srd.getRoleName();
          if (webAppRoles.contains(roleLink)) {
            roles.add(roleLink);
          } else if (webAppRoles.contains(portletRole)) {
            roles.add(portletRole);
          } else {
            Object[] tokens = { portletRole };
            throw new PortletRegistryException("errorRoleValidation", tokens);
          }
        }

        Map<String, Object> roleMap = new HashMap<>();
        for (Iterator j = roleProperties.entrySet().iterator(); j.hasNext();) {
          Map.Entry entry = (Map.Entry) j.next();
          String key = (String) entry.getKey(); // webcontainer role
          String value = (String) entry.getValue(); // web.xml role
          roleMap.put(key, value);
        }
        // Check if role mapping is provided for all roles defined in web.xml
        if (!roles.isEmpty()) {
          for (String role : roles) {
            if (!roleMap.containsValue(role)) {
              Object[] tokens = { role };
              throw new PortletRegistryException("errorReverseRoleMapping", tokens);
            }
          }
        }
        if (!roleMap.isEmpty()) {
          portletApp.setCollectionProperty(ROLE_MAP_KEY, roleMap);
        }

        // create role descriptions
        boolean hasRoleDescriptions = false;
        HashMap<String, Object> roleDescriptions = new HashMap<>();
        for (SecurityRoleRefDescriptor srd: securityRoleRefDescriptors) {
          Map roleDescMap = srd.getDescriptionMap();
          if (roleDescMap != null && roleDescMap.size() > 0) {
            hasRoleDescriptions = true;

            String name = srd.getRoleName();
            if (!roles.contains(name)) {
              String[] tokens = { srd.getRoleName() };
              throw new PortletRegistryException("errorReverseRoleMapping", tokens);
            }
            HashMap<String, Object> descriptions = new HashMap<>();
            for (Iterator j = roleDescMap.entrySet().iterator(); j.hasNext();) {
              Map.Entry entry = (Map.Entry) j.next();
              String lang = (String) entry.getKey();
              String desc = (String) entry.getValue();
              descriptions.put(lang, desc);
            }
            roleDescriptions.put(name, descriptions);
          }
        }

        if (hasRoleDescriptions) {
          portletApp.setCollectionProperty(ROLE_DESCRIPTIONS_KEY, roleDescriptions);
        }
      }

      // create user info collection
      if (userInfoProperties != null && !userInfoProperties.isEmpty()) {
        Map<String, Object> userInfoMap = new HashMap<>();
        Set keys = userInfoProperties.keySet();
        for (Iterator j = keys.iterator(); j.hasNext();) {
          String key = (String) j.next();
          String value = userInfoProperties.getProperty(key);
          userInfoMap.put(key, value);
        }
        portletApp.setCollectionProperty(USER_INFO_MAP_KEY, userInfoMap);
      } else {
        // Check if portlet.xml has user info attributes
        List<UserAttributeDescriptor> userAttrDescriptors =
            portletAppDescriptor.getUserAttributeDescriptors();
        if (userAttrDescriptors != null && userAttrDescriptors.size() > 0) {
          Map<String, Object> userInfoMap = new HashMap<>();

          for (UserAttributeDescriptor uad: userAttrDescriptors) {
            String attrName = uad.getName();
            userInfoMap.put(attrName, attrName);
          }
          if (userAttrDescriptors.size() > 0)
            portletApp.setCollectionProperty(USER_INFO_MAP_KEY, userInfoMap);
        }
      }
      // create user info descriptions
      boolean hasUserAttrDescriptions = false;
      List<UserAttributeDescriptor> userAttrDescriptors =
          portletAppDescriptor.getUserAttributeDescriptors();
      if (userAttrDescriptors != null && userAttrDescriptors.size() > 0) {
        Map<String, Object> userAttrDescriptions = new HashMap<>();

        for (UserAttributeDescriptor uad: userAttrDescriptors) {
          Map userAttrDescMap = uad.getDescriptionMap();
          if (userAttrDescMap.size() > 0) {
            Map<String, Object> descriptions = new HashMap<>();
            for (Iterator j = userAttrDescMap.entrySet().iterator(); j.hasNext();) {
              Map.Entry entry = (Map.Entry) j.next();
              String lang = (String) entry.getKey();
              String desc = (String) entry.getValue();
              descriptions.put(lang, desc);
            }
            userAttrDescriptions.put(uad.getName(), descriptions);
          }
        }

        if (hasUserAttrDescriptions) {
          portletApp.setCollectionProperty(USER_INFO_DESCRIPTIONS_KEY, userAttrDescriptions);
        }
      }

      // create supports collection
      List<SupportsDescriptor> supportsDescriptors = portletDescriptor.getSupportsDescriptors();
      if (supportsDescriptors != null && supportsDescriptors != null) {
        Map<String, Object> supportsMap = new HashMap<>();
        List<String> contentTypes = new ArrayList<>();
        for (SupportsDescriptor sd: supportsDescriptors) {
          String mimeType = sd.getMimeType();
          contentTypes.add(mimeType);
          List<String> portletModes = sd.getPortletModes();
          supportsMap.put(mimeType, portletModes);
        }
        portletApp.setCollectionProperty(SUPPORTS_MAP_KEY, supportsMap);
        portletApp.setCollectionProperty(SUPPORTED_CONTENT_TYPES_KEY, contentTypes);
      }

      // create display name properties
      Map displayNameMap = portletDescriptor.getDisplayNameMap();
      // TODO Why we are creating HashMap again????
      if (displayNameMap != null) {
        Map<String, Object> displayNames = new HashMap<>();

        for (Iterator j = displayNameMap.entrySet().iterator(); j.hasNext();) {
          Map.Entry entry = (Map.Entry) j.next();
          String lang = (String) entry.getKey();
          String dn = (String) entry.getValue();
          displayNames.put(lang, dn);
        }
        portletApp.setCollectionProperty(DISPLAY_NAME_MAP_KEY, displayNames);
      }

      // create descriptions properties
      Map<String, String> descriptionMap = portletDescriptor.getDescriptionMap();
      // TODO Why we are creating HashMap again????
      if (descriptionMap != null) {
        Map<String, Object> descriptions = new HashMap<>();

        for (Iterator j = descriptionMap.entrySet().iterator(); j.hasNext();) {
          Map.Entry entry = (Map.Entry) j.next();
          String lang = (String) entry.getKey();
          String desc = (String) entry.getValue();
          descriptions.put(lang, desc);
        }
        portletApp.setCollectionProperty(DESCRIPTION_MAP_KEY, descriptions);
      }

      // create supported locales collection
      List<String> supportedLocales = portletDescriptor.getSupportedLocales();
      if (supportedLocales != null) {
        portletApp.setCollectionProperty(SUPPORTED_LOCALES_KEY, supportedLocales);
      }

      // create transport-guarantee property, if required
      SecurityConstraintDescriptor scd = portletAppDescriptor.getSecurityConstraintDescriptor();
      if (scd != null) {
        List<String> constrainedPortlets = scd.getConstrainedPortlets();
        if (constrainedPortlets != null && constrainedPortlets.contains(portletName)) {
          String tgType = scd.getTransportGuaranteeType();
          if (tgType != null && tgType.length() > 0) {
            portletApp.setStringProperty(TRANSPORT_GUARANTEE_KEY, tgType);
          }
        }
      }
    }
  }

  public Boolean unregister(String configFileLocation, String warFileLocation,
      String warName) throws Exception {
    File warFile = new File(warFileLocation, warName + WAR_SUFFIX);
    String ddName = warName + DD_SUFFIX;
    InputStream in = null;
    try {
      File portletFile = new File(warFileLocation, ddName);
      if (!portletFile.exists()) {
        if (logger.isLoggable(Level.WARNING)) {
          logger.log(Level.WARNING, "PSPL_CSPPAM0034", portletFile.getPath());
        }
        return Boolean.FALSE;
      }
      in = new FileInputStream(portletFile);
      Properties properties = new Properties();
      properties.put(PortletDeployConfigReader.VALIDATE_PROPERTY, "false");
      DeploymentDescriptorReader ddReader = new DeploymentDescriptorReader(
          properties);
      portletAppDescriptor = ddReader.loadPortletAppDescriptor(warName, in);
      ddReader.processUndeployPortletExtensionDescriptor(warFile,
          configFileLocation);
    } catch (IOException ioe) {
      Object[] tokens = { warFileLocation + File.separator + ddName,
          warName + WAR_SUFFIX };
      throw new PortletRegistryException("errorStreamReadWhileUndeploy", ioe,
          tokens);
    } finally {
      try {
        if (in != null) {
          in.close();
        }
      } catch (Exception ignoreit) {
      }
    }
    this.portletAppName = warName;

    portletsDescriptor = portletAppDescriptor.getPortletsDescriptor();
    List<PortletDescriptor> portletDescriptors = portletsDescriptor.getPortletDescriptors();
    PortletRegistryContext portletRegistryContext = getPortletRegistryContext();
    for (PortletDescriptor portletDescriptor: portletDescriptors) {
      String portletName = portletDescriptor.getPortletName();
      logger.log(Level.FINE, "PSPL_CSPPAM0007", portletName);
      PortletID portletID = new PortletID(getPortletAppName(), portletName);
      if (logger.isLoggable(Level.FINEST)) {
        logger.log(Level.FINEST, "PSPL_CSPPAM0008", portletID);
      }
      portletRegistryContext.removePortlet(portletID.toString());
    }
    return Boolean.TRUE;
  }

  public void removePortletWar(String warFileLocation, String warName)
      throws Exception {
    String ddName = warName + DD_SUFFIX;
    // Remove the portlet war and portlet xml created in pc.home/war directory
    File portletFile = new File(warFileLocation, ddName);
    boolean portletFileRemoved = portletFile.delete();
    if (logger.isLoggable(Level.FINEST)) {
      logger.log(Level.FINEST, "PSPL_CSPPAM0018", new String[] {
          portletFile.getAbsolutePath(), String.valueOf(portletFileRemoved) });
    }
    File warFile = new File(warFileLocation, warName + WAR_SUFFIX);
    boolean warFileRemoved = warFile.delete();
    if (logger.isLoggable(Level.FINEST)) {
      logger.log(Level.FINEST, "PSPL_CSPPAM0019", new String[] {
          warFile.getAbsolutePath(), String.valueOf(warFileRemoved) });
    }
  }

  public void registerRemote(String portletWindowName, String consumerId,
      String producerEntityId, String portletHandle, String portletId)
      throws Exception {

    PortletRegistryElement portletWindow = new PortletWindow();
    String registryLocation = PortletRegistryHelper.getRegistryLocation();

    portletWindow.setName(portletWindowName);
    portletWindow.setPortletName(portletId);
    portletWindow.setRemote(Boolean.TRUE.toString());

    portletWindow.setStringProperty(CONSUMER_ID, consumerId);
    portletWindow.setStringProperty(PRODUCER_ENTITY_ID, producerEntityId);
    portletWindow.setStringProperty(PORTLET_HANDLE, portletHandle);
    portletWindow.setStringProperty(PORTLET_ID, portletId);

    portletWindowElementList.add(portletWindow);
    PortletRegistryWriter portletWindowRegistryWriter = new PortletWindowRegistryWriter(
        registryLocation, null);
    portletWindowRegistryWriter.appendDocument(portletWindowElementList);
    logger.log(Level.FINE, "PSPL_CSPPAM0010", "portlet-window-registry.xml");

    PortletWindowPreference portletWindowPreference = new PortletWindowPreference();
    portletWindowPreferenceElementList.add(portletWindowPreference);
    portletWindowPreference.setPortletName(portletId);
    portletWindowPreference.setName(portletWindowName);
    Map<String, Object> preferences = new HashMap<>();
    preferences.put(PORTLET_HANDLE, portletHandle);
    portletWindowPreference.setCollectionProperty(PREFERENCE_PROPERTIES_KEY,
        preferences);
    PortletRegistryWriter portletWindowPreferenceRegistryWriter =
        new PortletWindowPreferenceRegistryWriter(
        registryLocation, null);
    portletWindowPreferenceRegistryWriter
        .appendDocument(portletWindowPreferenceElementList);
    logger.log(Level.FINE, "PSPL_CSPPAM0010",
        "portlet-window-preference-registry.xml");
  }

  public void unregisterRemote(String portletWindowName) throws Exception {
    PortletWindowRegistryContext pwrContext = new PortletWindowRegistryContextImpl(
        null);
    // String portletName = pwrContext.getPortletName(portletWindowName);
    PortletRegistryContext portletRegistryContext = getPortletRegistryContext();
    portletRegistryContext.removePortletWindow(portletWindowName);
  }

  private void copyPortletXML(InputStream portletXMLStream,
      String warFileLocation, String portletAppName) throws Exception {
    String destFile = warFileLocation + "/" + portletAppName + DD_SUFFIX;
    FileOutputStream out = null;
    try {
      out = new FileOutputStream(destFile);
      byte[] buffer = new byte[8 * 1024];
      int count = 0;
      do {
        out.write(buffer, 0, count);
        count = portletXMLStream.read(buffer, 0, buffer.length);
      } while (count != -1);
    } catch (IOException ioe) {
      throw new PortletRegistryException("errorStreamRead", ioe);

    } finally {
      if (out != null) {
        out.close();
      }
    }
    logger.log(Level.FINE, "PSPL_CSPPAM0011", destFile);
  }

  private PortletRegistryContext getPortletRegistryContext()
      throws PortletRegistryException {
    PortletRegistryContextAbstractFactory afactory = new PortletRegistryContextAbstractFactory();
    PortletRegistryContextFactory factory = afactory
        .getPortletRegistryContextFactory();
    return factory.getPortletRegistryContext();
  }
}
