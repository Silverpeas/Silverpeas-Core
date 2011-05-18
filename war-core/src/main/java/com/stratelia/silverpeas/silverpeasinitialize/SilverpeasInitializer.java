/**
 * Copyright (C) 2000 - 2011 Silverpeas
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
import java.util.Locale;

/**
 * Title: Description: Copyright: Copyright (c) 2001 Company:
 * @author EDurand
 * @version 1.0
 */
public final class SilverpeasInitializer {

  private static boolean isInitialized = false;
  private static final String INITIALIZESETTINGS = "InitializeSettings.properties";
  private java.util.ResourceBundle silverpeasInitialisationSettings;

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
      silverpeasInitialisationSettings = FileUtil.loadBundle(
          "com.stratelia.silverpeas._silverpeasinitialize.settings._silverpeasinitializeSettings",
          new Locale("fr", ""));
      if (StringUtil.getBooleanValue(silverpeasInitialisationSettings.getString("initialize"))) {
        File[] listFileInitialize = getListInitializeSettingsFile();
        if (listFileInitialize != null) {
          int len = listFileInitialize.length;
          int lengthRef = INITIALIZESETTINGS.length();
          for (int i = 0; i < len; i++) {
            String tmpFileName = listFileInitialize[i].getName();
            int index = tmpFileName.indexOf(INITIALIZESETTINGS);
            if (tmpFileName.length() - lengthRef == index) {
              processInitializeSettingsFile(listFileInitialize[i].getAbsoluteFile());
            }
          }
        }
      } else {
        LogMsg(this, "SilverpeasInitializer",
            "Do not initialize because of _silverpeasinitializeSettings.initialize=false.");
      }
    } catch (Exception e) {
      LogMsg(this, "SilverpeasInitializer", e.getMessage());
    }
  }

  /**
   * Method declaration
   * @return
   * @see
   */
  private File[] getListInitializeSettingsFile() {
    File[] result = null;
    File pathInitialize = new File(silverpeasInitialisationSettings.getString("pathInitialize"));
    if (pathInitialize == null) {
      LogMsg(this, "getListInitializeSettingsFile",
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
   * @param obj
   * @param fct
   * @param msg
   * @see
   */
  private static void LogMsg(Object obj, String fct, String msg) {
    String from = obj.getClass().getName() + "." + fct + "()";
    System.out.println(from + " : " + msg);
  }

  private static synchronized void initialized() {
    isInitialized = true;
  }
}
