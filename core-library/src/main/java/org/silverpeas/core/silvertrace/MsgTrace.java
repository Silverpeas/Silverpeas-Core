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

package org.silverpeas.core.silvertrace;

import org.silverpeas.core.util.StringUtil;
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.ResourceBundle;

/**
 * This class is deprecated as the Silver Trace API is now replaced by the Silverpeas Logger API.
 * @author Thierry Leroi
 * @version %I%, %G%
 */
@Deprecated
final class MsgTrace {
  private Properties allMessages = new Properties();
  private String languageMessages = "";
  private String pathMessages = "";
  private String defaultLanguage = "fr";

  /**
   * Constructor declaration
   * @see
   */
  MsgTrace() {
  }

  /**
   * Initialize all trace messages from a path containing the module's messages files
   * @param filePath the path to the ancestor of the property files that contains modules' messages
   * files
   * @param language name of the sub directory containing the error messages (empty = default = "en"
   * = english)
   * @see
   */
  public void initFromProperties(String filePath, String language) {
    allMessages.clear();
    pathMessages = filePath + "/messages";
    if (isDefaultLanguage(language)) {
      languageMessages = defaultLanguage;
    } else {
      languageMessages = language;
    }
    List<File> theFiles = getPropertyFiles(pathMessages, '_' + languageMessages.toUpperCase());
    for (File currentFile : theFiles) {
      InputStream is = null;
      try {
        is = new FileInputStream(currentFile);
        allMessages.load(is);
      } catch (IOException e) {
        SilverTrace.error("silvertrace", "MsgTrace.initFromProperties()",
            "silvertrace.ERR_TRACE_MESSAGES_FILE_ERROR", "File:[" + currentFile.getAbsolutePath()
            + ']', e);
      } finally {
        IOUtils.closeQuietly(is);
      }
    }
  }

  /**
   * Return the string associated to the message ID
   * @param messageID looks like "modulename.MSG_..."
   * @return the message string (could be language dependant)
   * @see
   */
  public String getMsgString(String messageID) {
    return allMessages.getProperty(messageID, "!!! Message " + messageID + " NOT INITIALIZED !!!");
  }

  /**
   * Return the string associated to the message ID
   * @param messageID looks like "modulename.MSG_..."
   * @return the message string (could be language dependant)
   * @see
   */
  public String getMsgString(String messageID, String language) {
    if (!StringUtil.isDefined(language) || language.equalsIgnoreCase(languageMessages)) {
      return getMsgString(messageID);
    }
    Properties theMessages = new Properties();
    String valret = "!!! Messages " + messageID + " NOT INITIALIZED !!!";
    FileInputStream is = null;
    if (messageID.indexOf('.') > 0) {
      String fileName = pathMessages + '/' + messageID.substring(0, messageID.indexOf('.'))
          + "Messages_" + language + ".properties";
      try {
        File thePropFile = new File(fileName);
        is = new FileInputStream(thePropFile);
        theMessages.load(is);
        valret = theMessages.getProperty(messageID, valret);
      } catch (IOException e) {
        SilverTrace.error("silvertrace", "MsgTrace.getMsgString()",
            "silvertrace.ERR_TRACE_MESSAGES_FILE_ERROR", "File:[" + fileName + ']', e);
      } finally {
        IOUtils.closeQuietly(is);
      }
    }
    return valret;
  }

  /**
   * Reads a boolean property and return it's boolean value
   * @param theProps the Properties object
   * @param propertyName the name of the property to test
   * @param defaultValue the default value to set to the property if it doesn't exist
   * @return true/false
   * @see
   */
  static public boolean getBooleanProperty(Properties theProps, String propertyName,
      boolean defaultValue) {
    boolean valret = defaultValue;
    String value = theProps.getProperty(propertyName);
    if (value != null) {
      valret = "true".equalsIgnoreCase(value);
    }
    return valret;
  }

  /**
   * Reads a boolean property and return it's boolean value
   * @param resource the Resource object
   * @param propertyName the name of the property to test
   * @param defaultValue the default value to set to the property if it doesn't exist
   * @return true/false
   * @see
   */
  static public boolean getBooleanProperty(ResourceBundle resource,
      String propertyName, boolean defaultValue) {
    boolean valret = defaultValue;
    String value = resource.getString(propertyName);
    if (value != null) {
      valret = StringUtil.getBooleanValue(value);
    }
    return valret;
  }

  /**
   * Method declaration
   * @param pathFiles
   * @param suffix
   * @return
   * @see
   */
  public List<File> getPropertyFiles(String pathFiles, String suffix) {
    File pathMessagesFile = new File(pathFiles);
    if (pathMessagesFile.exists() && pathMessagesFile.isDirectory()) {
      File[] messageFiles = pathMessagesFile.listFiles();
      List<File> valret = new ArrayList<File>(messageFiles.length);
      for (File currentFile : messageFiles) {
        if (currentFile.getName().toUpperCase().endsWith(suffix + ".PROPERTIES")) {
          valret.add(currentFile);
        }
      }
      return valret;
    }
    return new ArrayList<File>();
  }

  /**
   * Method declaration
   * @param language
   * @return
   * @see
   */
  protected boolean isDefaultLanguage(String language) {
    return (!StringUtil.isDefined(language) || defaultLanguage.equalsIgnoreCase(language));
  }

}
