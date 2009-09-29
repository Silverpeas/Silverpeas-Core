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
 * FLOSS exception.  You should have recieved a copy of the text describing
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
/*--- formatted by Jindent 2.1, (www.c-lab.de/~jindent) 
 ---*/

package com.stratelia.silverpeas.silverpeasinitialize;

import java.util.*;
import java.io.*;

/**
 * Title: Description: Copyright: Copyright (c) 2001 Company:
 * 
 * @author eDurand
 * @version 1.0
 */

public class ProcessInitialize implements Runnable {
  protected File m_InitializeSettingsFile;

  /**
   * Constructor declaration
   * 
   * 
   * @param p_InitializeSettingsFile
   * 
   * @see
   */
  public ProcessInitialize(File p_InitializeSettingsFile) {
    m_InitializeSettingsFile = p_InitializeSettingsFile;
  }

  /**
   * Method declaration
   * 
   * 
   * @see
   */
  public void run() {
    processInitializeSettingsFile();
  }

  /**
   * Method declaration
   * 
   * 
   * @see
   */
  private void processInitializeSettingsFile() {
    try {
      Properties p = getPropertiesOfFile(m_InitializeSettingsFile
          .getAbsolutePath());

      if ((p.getProperty("Initialize") != null)
          && (p.getProperty("Initialize").equalsIgnoreCase("true") == true)) {

        String InitializeClass = p.getProperty("InitializeClass");
        Class c = Class.forName(InitializeClass);
        IInitialize init = (IInitialize) c.newInstance();

        if (!(init instanceof IInitialize)) {
          throw new Exception("Class " + InitializeClass
              + " isn't a IInitialize.");
        }

        if (init.Initialize() == false) {
          LogMsg(this, LOG_ERROR, "processInitializeSettingsFile",
              InitializeClass + ".Initialize() failed.");
        }

      }

      if ((p.getProperty("CallBack") != null)
          && (p.getProperty("CallBack").equalsIgnoreCase("true") == true)) {

        String CallBackClass = p.getProperty("CallBackClass");
        Class c = Class.forName(CallBackClass);
        CallBack cb = (CallBack) c.newInstance();

        if (!(cb instanceof CallBack)) {
          throw new Exception("Class " + CallBackClass + " isn't a CallBack.");
        }

        cb.subscribe();
      }
    } catch (Exception e) {
      LogMsg(this, LOG_ERROR, "processInitializeSettingsFile", e.getMessage());
    }
  }

  /**
   * Method declaration
   * 
   * 
   * @param fileName
   * 
   * @return
   * 
   * @see
   */
  private Properties getPropertiesOfFile(String fileName) {
    Properties result = null;

    try {
      InputStream is = new FileInputStream(fileName);

      result = new Properties();
      result.load(is);
    } catch (Exception e) {
      LogMsg(this, LOG_ERROR, "getPropertiesOfFile( " + fileName + " )", e
          .getMessage());
    }
    return result;
  }

  private static int LOG_ERROR = 0;

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
