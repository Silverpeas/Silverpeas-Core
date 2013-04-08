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

/*--- formatted by Jindent 2.1, (www.c-lab.de/~jindent) 
 ---*/

package com.stratelia.silverpeas.silverpeasinitialize;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Properties;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ProcessInitialize implements Runnable {

  private static Logger logger = LoggerFactory.getLogger(ProcessInitialize.class);
  private File initConfiguration;

  /**
   * Constructor declaration
   * @param configurationFile
   * @see
   */
  public ProcessInitialize(File configurationFile) {
    initConfiguration = configurationFile;
  }

  /**
   * Method declaration
   * @see
   */
  @Override
  public void run() {
    processInitializeSettingsFile();
  }

  /**
   * Method declaration
   * @see
   */
  private void processInitializeSettingsFile() {
    try {
      Properties p = getPropertiesOfFile(initConfiguration);
      String initialize = p.getProperty("Initialize");
      if (initialize != null && Boolean.parseBoolean(initialize)) {
        String initClassName = p.getProperty("InitializeClass");
        Class<?> initClass = Class.forName(initClassName);
        IInitialize init = (IInitialize) initClass.newInstance();
        if (!init.Initialize()) {
          logMessage(this, "processInitializeSettingsFile", initClassName + ".Initialize() failed.",
              null);
        }
      }
      if (Boolean.parseBoolean(p.getProperty("CallBack"))) {
        String CallBackClass = p.getProperty("CallBackClass");
        Class<?> callBackClass = Class.forName(CallBackClass);
        CallBack callBack = (CallBack) callBackClass.newInstance();
        if (!(CallBack.class.isInstance(callBack))) {
          throw new Exception("Class " + CallBackClass + " isn't a CallBack.");
        }
        callBack.subscribe();
      }
    } catch (Exception e) {
      logMessage(this, "processInitializeSettingsFile", e.getMessage(), e);
    }
  }

  /**
   * Method declaration
   * @param fileName
   * @return
   * @see
   */
  private Properties getPropertiesOfFile(File fileName) {
    Properties result = new Properties();
    InputStream is = null;
    try {
      is = new FileInputStream(fileName);
      result.load(is);
    } catch (Exception e) {
      logMessage(this, "getPropertiesOfFile( " + fileName + " )", e.getMessage(), e);
    } finally {
      IOUtils.closeQuietly(is);
    }
    return result;
  }

  /**
   * Method declaration
   * @param obj
   * @param debugLevel
   * @param fct
   * @param msg
   * @see
   */
  private static void logMessage(Object obj, String fct, String msg, Exception e) {
    String from = obj.getClass().getName() + "." + fct + "()";
    logger.error(from + " : " + msg, e);
    System.err.println(from + " : " + msg);
  }

}
