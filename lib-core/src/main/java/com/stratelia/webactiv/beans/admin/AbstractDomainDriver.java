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

package com.stratelia.webactiv.beans.admin;

import com.silverpeas.util.StringUtil;
import com.stratelia.webactiv.util.ResourceLocator;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

abstract public class AbstractDomainDriver implements DomainDriver {

  protected int domainId = -1; // The domainId of this instance of domain
  // driver
  protected List<DomainProperty> domainProperties = new ArrayList<DomainProperty>(); // liste
  // ordonnée
  // des properties du bundle domainSP
  protected String[] keys = null;
  protected String m_PropertiesMultilang = "";
  protected Map<String, HashMap<String, String>> m_PropertiesLabels =
      new HashMap<String, HashMap<String, String>>();
  protected Map<String, HashMap<String, String>> m_PropertiesDescriptions =
      new HashMap<String, HashMap<String, String>>();
  protected String[] m_mapParameters = null;
  protected boolean synchroInProcess = false;
  protected boolean x509Enabled = false;

  /**
   * Initialize the domain driver with the initialization parameter stocked in table This parameter
   * could be a table name or a ressource file name or whatever specified by the domain driver
   * Default : ressource file name
   * @param domainId id of domain
   * @param initParam name of resource file
   * @param authenticationServer name of the authentication server (no more used yet)
   * @throws Exception
   */
  @Override
  public void init(int domainId, String initParam, String authenticationServer)
      throws Exception {
    ResourceLocator rs = new ResourceLocator(initParam, "");
    int nbProps = 0;

    this.domainId = domainId;

    // Init the domain's users properties
    domainProperties.clear();
    m_PropertiesMultilang = rs.getString("property.ResourceFile");
    String s = rs.getString("property.Number");
    if (StringUtil.isDefined(s)) {
      nbProps = Integer.parseInt(s);
    }
    keys = new String[nbProps];
    m_mapParameters = new String[nbProps];
    for (int i = 1; i <= nbProps; i++) {
      s = rs.getString("property_" + Integer.toString(i) + ".Name");
      if ((s != null) && (s.length() > 0)) {
        DomainProperty newElmt = new DomainProperty(rs, String.valueOf(i)); // Retreives all
        // property's
        // infos
        domainProperties.add(newElmt);
        keys[i - 1] = newElmt.getName();
        m_mapParameters[i - 1] = newElmt.getMapParameter();
      }
    }

    // X509 Certificates management is enable ?
    x509Enabled = rs.getBoolean("security.x509.enabled", false);

    // Init the domain's properties
    initFromProperties(rs);
  }

  @Override
  public String[] getPropertiesNames() {
    return keys;
  }

  @Override
  public DomainProperty getProperty(String propName) {
    for (DomainProperty domainProp : domainProperties) {
      if (domainProp.getName().equals(propName)) {
        return domainProp;
      }
    }
    return null;
  }

  @Override
  public String[] getMapParameters() {
    return m_mapParameters;
  }

  @Override
  public List<DomainProperty> getPropertiesToImport(String language) {
    List<DomainProperty> props = new ArrayList<DomainProperty>();

    Map<String, String> theLabels = getPropertiesLabels(language);
    Map<String, String> theDescriptions = getPropertiesDescriptions(language);
    addPropertiesToImport(props, theDescriptions);

    for (DomainProperty domainProp : domainProperties) {
      if (domainProp.isUsedToImport()) {
        String propLabel = theLabels.get(domainProp.getName());
        String propDescription = theDescriptions.get(domainProp.getName());
        domainProp.setLabel(propLabel);
        domainProp.setDescription(propDescription);
        props.add(domainProp);
      }
    }
    return props;
  }

  @Override
  public void addPropertiesToImport(List<DomainProperty> props) {
  }

  /**
   * @param props
   * @param theDescriptions
   */
  @Override
  public void addPropertiesToImport(List<DomainProperty> props, Map<String, String> theDescriptions) {
  }

  @Override
  public Map<String, String> getPropertiesLabels(String language) {
    HashMap<String, String> valret = m_PropertiesLabels.get(language);
    if (valret == null) {
      HashMap<String, String> newLabels = new HashMap<String, String>();
      ResourceLocator rs = new ResourceLocator(m_PropertiesMultilang, language);
      for (String key : keys) {
        newLabels.put(key, rs.getString(key));
      }
      m_PropertiesLabels.put(language, newLabels);
      valret = newLabels;
    }
    return valret;
  }

  @Override
  public Map<String, String> getPropertiesDescriptions(String language) {
    Map<String, String> valret = m_PropertiesDescriptions.get(language);

    if (valret == null) {
      HashMap<String, String> newDescriptions = new HashMap<String, String>();
      ResourceLocator rs = new ResourceLocator(m_PropertiesMultilang, language);
      for (String key : keys) {
        newDescriptions.put(key, rs.getString(key + ".description"));
      }
      m_PropertiesDescriptions.put(language, newDescriptions);
      valret = newDescriptions;
    }
    return valret;
  }

  /**
   * Virtual method that performs extra initialization from a properties file. To overload by the
   * class who need it.
   * @param rs name of resource file
   */
  @Override
  public void initFromProperties(ResourceLocator rs) throws Exception {
  }

  /**
   * Called when Admin starts the synchronization
   */
  @Override
  public long getDriverActions() {
    if (x509Enabled) {
      return ACTION_MASK_RW | AbstractDomainDriver.ACTION_X509_USER;
    }
    return ACTION_MASK_RW;
  }

  @Override
  public boolean isSynchroOnLoginEnabled() {
    return false;
  }

  @Override
  public boolean isSynchroThreaded() {
    return false;
  }

  @Override
  public boolean isSynchroOnLoginRecursToGroups() {
    return true;
  }

  @Override
  public boolean isGroupsInheritProfiles() {
    return false;
  }

  @Override
  public boolean mustImportUsers() {
    return true;
  }

  @Override
  public String getTimeStamp(String minTimeStamp) throws Exception {
    return "";
  }

  @Override
  public String getTimeStampField() throws Exception {
    return null;
  }

  @Override
  public boolean isX509CertificateEnabled() {
    return false;
  }

  /**
   * Called when Admin starts the synchronization
   */
  @Override
  public void beginSynchronization() throws Exception {
    synchroInProcess = true;
  }

  @Override
  public boolean isSynchroInProcess() throws Exception {
    return synchroInProcess;
  }

  /**
   * Called when Admin ends the synchronization
   * @param cancelSynchro true if the synchronization is cancelled, false if it ends normally
   */
  @Override
  public String endSynchronization(boolean cancelSynchro) throws Exception {
    synchroInProcess = false;
    return "";
  }

  static protected int idAsInt(String id) {
    if (!StringUtil.isDefined(id)) {
      return -1; // the null id.
    }
    try {
      return Integer.parseInt(id);
    } catch (NumberFormatException e) {
      return -1; // the null id.
    }
  }

  /**
   * Convert int Id to String Id
   * @param id id to convert
   */
  static protected String idAsString(int id) {
    return String.valueOf(id);
  }
}