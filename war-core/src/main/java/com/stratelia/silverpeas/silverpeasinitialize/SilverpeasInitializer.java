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

package com.stratelia.silverpeas.silverpeasinitialize;

import com.silverpeas.util.FileUtil;
import com.silverpeas.util.StringUtil;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.Security;
import java.util.Enumeration;
import java.util.Locale;
import java.util.Properties;

/**
 * Title: Description: Copyright: Copyright (c) 2001 Company:
 * @author EDurand
 * @version 1.0
 */
public final class SilverpeasInitializer {
  private static boolean isInitialized = false;
  private static final String INITIALIZESETTINGS = "InitializeSettings.properties";

  private java.util.ResourceBundle _silverpeasinitializeSettings;

  /**
   * Constructor declaration
   * @see
   */
  public SilverpeasInitializer() {
  }
  
  /**
   * Is the Silverpeas context already initialized?
   * @return true if the Silverpeas is initialized, false otherwise.
   */
  public static synchronized boolean isInitialized() {
      return isInitialized;
  }

  /**
   * Starts the Silverpeas context initialization.
   * 
   * If the context is already initialized, then nothing is performed.
   * It initializes each Silverpeas components declared as to be initialized (through their
   * properties file located at a well-defined dedicated locations). Whatever the success of the
   * components initialization, the Silverpeas context is marked as initialized.
   */
  public synchronized void startInitialize() {
    if (isInitialized()) {
      return;
    }
    initialized();
    try {
      // Add properties
      _silverpeasinitializeSettings = FileUtil.loadBundle(
          "com.stratelia.silverpeas._silverpeasinitialize.settings._silverpeasinitializeSettings",
          new Locale("fr", ""));
      if (StringUtil.getBooleanValue(_silverpeasinitializeSettings.getString("initialize"))) {
        initPropertySystem();
        File[] listFileInitialize = getListInitializeSettingsFile();

        if (listFileInitialize != null) {
          int len = listFileInitialize.length;
          int lengthRef = INITIALIZESETTINGS.length();

          for (int i = 0; i < len; i++) {
            String tmpFileName = listFileInitialize[i].getName();
            int index = tmpFileName.indexOf(INITIALIZESETTINGS);

            if (tmpFileName.length() - lengthRef == index) {
              processInitializeSettingsFile(listFileInitialize[i]
                  .getAbsoluteFile());
            }
          }
        }

      } else {
        LogMsg(this, LOG_DEBUG, "SilverpeasInitializer",
            "Do not initialize because of _silverpeasinitializeSettings.initialize=false.");
      }
      // Add SSL support
      Security.addProvider(new com.sun.net.ssl.internal.ssl.Provider());
    } catch (Exception e) {
      LogMsg(this, LOG_ERROR, "SilverpeasInitializer", e.getMessage());
    }
  }

  /**
   * Method declaration
   * @return
   * @see
   */
  private File[] getListInitializeSettingsFile() {
    File[] result = null;
    File pathInitialize = new File(_silverpeasinitializeSettings
        .getString("pathInitialize"));

    if (pathInitialize == null) {
      LogMsg(
          this,
          LOG_ERROR,
          "getListInitializeSettingsFile",
          "Repository Initialize for InitializeSettings.xml files is not defined in Settings.");
    } else {
      result = pathInitialize.listFiles();
    }
    return result;
  }

  /**
   * Method declaration
   * @param initializeSettingsFile
   * @see
   */
  private void processInitializeSettingsFile(File initializeSettingsFile) {
    Thread t = new Thread(new ProcessInitialize(initializeSettingsFile));
    t.start();
  }

  /**
   * Method declaration
   * @see
   */
  private void initPropertySystem() {
    File pathInitialize = new File(_silverpeasinitializeSettings
        .getString("pathInitialize"));

    if (pathInitialize == null) {
      LogMsg(
          this,
          LOG_ERROR,
          "initPropertySystem",
          "Repository Initialize for systemSettings.properties file is not defined in Settings.");
    } else {
      try {
        FileInputStream fis = new FileInputStream(new File(pathInitialize,
            "systemSettings.properties"));
        Properties systemFileProperties = new Properties();

        systemFileProperties.load(fis);
        Properties systemProperties = System.getProperties();

        for (Enumeration e = systemFileProperties.propertyNames(); e
            .hasMoreElements();) {
          String key = (String) e.nextElement();

          if (systemProperties.getProperty(key) != null) {
            System.out
                .println("_SilverpeasInitialize : override system property ["
                + key + "] = " + "(" + systemProperties.getProperty(key)
                + ") -> (" + systemFileProperties.getProperty(key) + ")");
          }
          systemProperties.put(key, systemFileProperties.getProperty(key));
        }
        System.setProperties(systemProperties);
      } catch (FileNotFoundException e) {
        LogMsg(this, LOG_ERROR, "initPropertySystem",
            "File systemSettings.properties in directory " + pathInitialize
            + " not found.");
      } catch (IOException e) {
        LogMsg(this, LOG_ERROR, "initPropertySystem",
            "Unable to read systemSettings.properties.");
      }
    }
  }

  private static int LOG_ERROR = 0;
  private static int LOG_DEBUG = 600;

  /**
   * Method declaration
   * @param obj
   * @param debugLevel
   * @param fct
   * @param msg
   * @see
   */
  private static void LogMsg(Object obj, int debugLevel, String fct, String msg) {
    String from = obj.getClass().getName() + "." + fct + "()";

    System.out.println(from + " : " + msg);
  }

  private static synchronized void initialized() {
    isInitialized = true;
  }
}
