/*--- formatted by Jindent 2.1, (www.c-lab.de/~jindent) 
 ---*/

package com.stratelia.silverpeas.silverpeasinitialize;

import java.io.*;
import java.util.*;
import java.security.Security;

/**
 * Title: Description: Copyright: Copyright (c) 2001 Company:
 * 
 * @author EDurand
 * @version 1.0
 */
public class SilverpeasInitializer {
  private static boolean isInitialized = false;
  private static String INITIALIZESETTINGS = "InitializeSettings.properties";

  private java.util.ResourceBundle _silverpeasinitializeSettings;

  /**
   * Constructor declaration
   * 
   * 
   * @see
   */
  public SilverpeasInitializer() {
    boolean localIsInitialized;

    synchronized (INITIALIZESETTINGS) {
      localIsInitialized = isInitialized;
      isInitialized = true;
    }
    if (!localIsInitialized) // Initialize only once !!!
    {
      startInitialize();
    }
  }

  public void startInitialize() {
    try {
      // Add properties
      _silverpeasinitializeSettings = java.util.ResourceBundle
          .getBundle(
              "com.stratelia.silverpeas._silverpeasinitialize.settings._silverpeasinitializeSettings",
              new Locale("fr", ""));
      if (_silverpeasinitializeSettings.getString("initialize")
          .equalsIgnoreCase("true") == true) {

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
   * 
   * 
   * @return
   * 
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
   * 
   * 
   * @param initializeSettingsFile
   * 
   * @see
   */
  private void processInitializeSettingsFile(File initializeSettingsFile) {
    Thread t = new Thread(new ProcessInitialize(initializeSettingsFile));

    t.start();
  }

  /**
   * Method declaration
   * 
   * 
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
   * 
   * 
   * @param obj
   * @param debugLevel
   * @param fct
   * @param msg
   * 
   * @see
   */
  private static void LogMsg(Object obj, int debugLevel, String fct, String msg) {
    String from = obj.getClass().getName() + "." + fct + "()";

    System.out.println(from + " : " + msg);
  }

}
