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

package org.silverpeas.core.web.portlets.context.window.impl;

import com.sun.portal.container.EntityID;
import com.sun.portal.container.PortletID;
import com.sun.portal.portletcontainer.common.PortletPreferencesUtility;
import com.sun.portal.portletcontainer.common.PreferencesValidatorSetter;
import com.sun.portal.portletcontainer.context.registry.PortletRegistryContext;
import com.sun.portal.portletcontainer.portlet.impl.PortletResourceBundle;

import javax.portlet.PortletPreferences;
import javax.portlet.PortletRequest;
import javax.portlet.PreferencesValidator;
import javax.portlet.ReadOnlyException;
import javax.portlet.ValidatorException;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * The <code>PortletPreferencesImpl</code> is the default implementation for the
 * <code>PortletPreferences</code> interface. This implementation uses the portlet window preference
 * registry as the backend storage of the preferences data. This implementation caches the
 * preferences and only write to the registry when the method store() is called. The registry is
 * expected to have the following structure defined:
 * @see javax.portlet.PortletPreferences
 **/
public class PortletPreferencesImpl implements PortletPreferences, PreferencesValidatorSetter {

  private String portletWindowName;
  private PortletID portletID;
  private String userID;
  private PreferencesValidator preferencesValidator;
  private PortletRegistryContext portletRegistryContext;
  private HttpServletRequest request;
  private ResourceBundle bundle;
  private Map<String, String> rbPreferenceNames;
  private boolean useRBPreferenceName;
  private boolean useRBPreferenceValue;
  private boolean readOnly;
  // Map to hold the isReadOnly information
  protected Map predefinedPrefReadOnlyMap;
  // Map to hold user defined preferences information and a list to record
  // preferences that have changed since last store().
  protected Map userPrefMap;
  protected Map defaultMap;
  protected Set modifiedList;
  // Create a logger for this class
  private static final Logger logger =
      Logger.getLogger("org.silverpeas.core.web.portlets.context.window.impl",
      "org.silverpeas.portlets.PCCTXLogMessages");

  public PortletPreferencesImpl(HttpServletRequest request,
      PortletRegistryContext portletRegistryContext,
      EntityID entityID, String userID, ResourceBundle bundle, boolean isReadOnly) {

    this.portletWindowName = entityID.getPortletWindowName();
    this.portletID = entityID.getPortletID();
    this.userID = userID;
    this.portletRegistryContext = portletRegistryContext;
    this.request = request;
    this.bundle = bundle;
    this.useRBPreferenceName = false;
    this.useRBPreferenceValue = false;
    if (this.bundle != null) {
      // Create preferenceNames map only if the resource bundle contains
      // preference name. This is used later in the code.
      Enumeration<String> keys = this.bundle.getKeys();
      while (keys.hasMoreElements()) {
        String key = keys.nextElement();
        if (key.startsWith(PortletResourceBundle.RB_PREFERENCE_NAME)) {
          rbPreferenceNames = new HashMap<String, String>();
          useRBPreferenceName = true;
        } else if (key.startsWith(PortletResourceBundle.RB_PREFERENCE_VALUE)) {
          useRBPreferenceValue = true;
        }
      }

    }
    this.readOnly = isReadOnly;
    modifiedList = new HashSet();

    try {
      predefinedPrefReadOnlyMap =
          portletRegistryContext.getPreferencesReadOnly(this.portletWindowName, this.userID);
    } catch (Exception ioe) {
      logger.log(Level.WARNING, "PSPL_PCCTXCSPPCI0013 : {0}", ioe.getMessage());
    }

    try {
      userPrefMap = portletRegistryContext.getPreferences(this.portletWindowName, this.userID);
      defaultMap =
          portletRegistryContext.getPreferences(this.portletWindowName,
          PortletRegistryContext.USER_NAME_DEFAULT);
    } catch (Exception ioe) {
      logger.log(Level.SEVERE, "PSPL_PCCTXCSPPCI0013", ioe);
    }
  }

  @Override
  public boolean isReadOnly(String key) {
    if (key == null) {
      throw new IllegalArgumentException();
    }
    return getIsReadOnly(key);
  }

  @Override
  public String getValue(String key, String def) {
    String defs[] = { def };
    String values[] = getValues(key, defs);

    if (values == null || values.length == 0) {
      return def;
    } else {
      return values[0];
    }
  }

  @Override
  public String[] getValues(String key, String[] def) {
    String value = getPrefValue(key, arrayToString(def));
    String prefs[] = stringToArray(value);

    if (prefs == null || prefs.length == 0) {
      return def;
    } else {
      int length = prefs.length;
      for (int i = 0; i < length; i++) {
        if (PortletPreferencesUtility.NULL_STRING.equals(prefs[i])) {
          prefs[i] = null;
        } else {
          // Check for preferences in ResourceBundle
          // The key can be from RB
          // If its from RB get the actual preference name
          String prefName = null;
          if (rbPreferenceNames != null) {
            prefName = rbPreferenceNames.get(key);
          }
          if (prefName == null) {
            prefName = key;
          }
          prefs[i] = getPreferenceValueFromRB(prefName, prefs[i]);
        }
      }
      return prefs;
    }
  }

  @Override
  public void setValue(String key, String value) throws ReadOnlyException {
    if (key == null) {
      throw new IllegalArgumentException();
    }

    if (readOnly) {
      throw new ReadOnlyException("");
    }

    if (!getIsReadOnly(key)) {
      userPrefMap.put(key, value);
      modifiedList.add(key);
    } else {
      throw new ReadOnlyException("");
    }
  }

  @Override
  public void setValues(String key, String[] values) throws ReadOnlyException {
    setValue(key, arrayToString(values));
  }

  @Override
  public Enumeration<String> getNames() {
    Set<String> names = userPrefMap.keySet();
    List list = new ArrayList();
    for (String name : names) {
      String prefName = getPreferenceNameFromRB(name);
      if (rbPreferenceNames != null) {
        rbPreferenceNames.put(prefName, name);
      }
      list.add(prefName);
    }
    return Collections.enumeration(list);
  }

  @Override
  public Map<String, String[]> getMap() {
    Map prefMap = new HashMap();

    Enumeration<String> names = getNames();
    while (names.hasMoreElements()) {
      // Name can be from RB
      // If its from RB get the actual preference name
      String name = names.nextElement();
      String prefName = null;
      if (rbPreferenceNames != null) {
        prefName = rbPreferenceNames.get(name);
      }
      if (prefName == null) {
        prefName = name;
      }
      prefMap.put(name, getValues(prefName, null));
    }

    if (prefMap.size() > 0) {
      return prefMap;
    } else {
      return Collections.EMPTY_MAP;
    }
  }

  @Override
  public void reset(String key) throws ReadOnlyException {

    if (key == null) {
      throw new IllegalArgumentException();
    }

    String prefName = null;
    if (rbPreferenceNames != null) {
      prefName = rbPreferenceNames.get(key);
    }
    if (prefName == null) {
      prefName = key;
    }

    if (!getIsReadOnly(prefName)) {
      userPrefMap.put(prefName, getDefault(prefName));
      modifiedList.add(prefName);
    } else {
      throw new ReadOnlyException("");
    }
  }

  @Override
  public void store() throws IOException, ValidatorException {
    String lifecyclePhase = (String) request.getAttribute(PortletRequest.LIFECYCLE_PHASE);
    if (PortletRequest.RENDER_PHASE.equals(lifecyclePhase)) {
      throw new IllegalStateException("Not allowed to store preferences during render");
    } else {
      if (preferencesValidator != null) {
        preferencesValidator.validate(this);
      }
      savePrefMap();
    }
  }

  @Override
  public void setPreferencesValidator(PreferencesValidator pv) {
    preferencesValidator = pv;
  }

  /**
   * Gets the preference value back in raw (String) format, regardless of type.
   **/
  private String getPrefValue(String key, String def) {
    String pref = null;

    if (key == null) {
      throw new IllegalArgumentException();
    }

    loadPrefMap(key, def);
    // if (userPrefMap.containsKey(key)) {
    // pref = (String) userPrefMap.get(key);
    // } else {
    // pref = def;
    // }
    pref = (String) userPrefMap.get(key);
    return pref;
  }

  private String getPreferenceNameFromRB(String key) {
    if (!useRBPreferenceName) {
      return key;
    }
    StringBuilder attribute = new StringBuilder();
    attribute.append(PortletResourceBundle.RB_PREFERENCE_NAME);
    attribute.append(key);
    try {
      return this.bundle.getString(attribute.toString());
    } catch (MissingResourceException mse) {
      return key;
    }
  }

  private String getPreferenceValueFromRB(String key, String value) {
    if (!useRBPreferenceValue) {
      return value;
    }
    StringBuilder attribute = new StringBuilder();
    attribute.append(PortletResourceBundle.RB_PREFERENCE_VALUE);
    attribute.append(key);
    attribute.append(".");
    attribute.append(value);
    try {
      return this.bundle.getString(attribute.toString());
    } catch (MissingResourceException mse) {
      return value;
    }
  }

  /**
   * Converts an encoded string array back into String[].
   **/
  private String[] stringToArray(String value) {
    List valueArray = PortletPreferencesUtility.getPreferenceValues(value);
    return (String[]) valueArray.toArray(new String[valueArray.size()]);

  }

  /**
   * Encodes a String[] into a String form.
   **/
  private String arrayToString(String[] values) {
    return PortletPreferencesUtility.getPreferenceString(values);
  }

  /**
   * Clone a map.
   **/
  protected HashMap cloneMap(Map original) {

    HashMap clone = null;

    if (original instanceof HashMap) {
      return (HashMap) ((HashMap) original).clone();
    } else {
      clone = new HashMap();
      Iterator names = original.entrySet().iterator();
      while (names.hasNext()) {
        Map.Entry entry = (Map.Entry) names.next();
        String name = (String) entry.getKey();
        clone.put(name, entry.getValue());

      }
    }
    return clone;
  }

  /**
   * Loads a particular predefined preferences from the preference registry and store it in the
   * predefinedPrefMap.
   */
  protected void loadPrefMap(String prefName, String def) {
    if (!userPrefMap.containsKey(prefName)) {
      // Get the preference value from User PrefMap, if its not there
      // get it from the pre defined map.
      String pref = (String) defaultMap.get(prefName);
      if (pref == null) {
        pref = def;
      }
      if (logger.isLoggable(Level.INFO)) {
        logger.log(Level.INFO, "PSPL_PCCTXCSPPCI0014", new Object[] { prefName, pref });
      }
      userPrefMap.put(prefName, pref);
    }
  }

  /**
   * Writes all the changes made to the predefined preferences back to registry and reset the
   * modifiedList.
   **/
  protected void savePrefMap() {
    Iterator iter = modifiedList.iterator();
    Map prefMap = new HashMap();
    while (iter.hasNext()) {
      String key = (String) iter.next();
      String value = (String) userPrefMap.get(key);
      prefMap.put(key, value);
      if (logger.isLoggable(Level.INFO)) {
        logger.log(Level.INFO, "PSPL_PCCTXCSPPCI0015", new Object[] { key, value });
      }
    }
    writePortletPreferences(prefMap);
    modifiedList.clear();
  }

  /**
   * Returns the isReadOnly value of a given key
   **/
  private boolean getIsReadOnly(String key) {
    Boolean retval = Boolean.FALSE;
    if (predefinedPrefReadOnlyMap != null) {
      String isReadOnly = (String) predefinedPrefReadOnlyMap.get(key);
      if (isReadOnly != null) {
        retval = Boolean.valueOf(isReadOnly);
      }
    }
    return retval.booleanValue();
  }

  /**
   * Returns the default value of a given key.
   **/
  private String getDefault(String key) {
    String def = null;
    if (defaultMap != null) {
      def = (String) defaultMap.get(key);
    }
    return def;
  }

  private void writePortletPreferences(Map prefMap) {
    try {
      if (logger.isLoggable(Level.FINE)) {
        logger.log(Level.FINE, "PSPL_PCCTXCSPPCI0016",
            new Object[] { userID, portletID, portletWindowName });
      }
      if (portletID != null) {
        portletRegistryContext.savePreferences(this.portletID.toString(), this.portletWindowName,
            this.userID, prefMap);
      } else {
        if (logger.isLoggable(Level.WARNING)) {
          logger.log(Level.WARNING, "PSPL_PCCTXCSPPCI0017", new String[] { userID,
              portletWindowName });
        }
      }
    } catch (Exception e) {
      if (logger.isLoggable(Level.SEVERE)) {
        logger.log(Level.SEVERE, "PSPL_PCCTXCSPPCI0018",
            new Object[] { userID, portletID, portletWindowName });
      }
    }
  }
}
