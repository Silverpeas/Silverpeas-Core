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

package com.stratelia.silverpeas.silvertrace;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.ResourceBundle;

import com.silverpeas.util.StringUtil;

/**
 * Class declaration
 * @author Thierry Leroi
 * @version %I%, %G%
 */
public class MsgTrace {
  private Properties allMessages = new Properties();
  private String languageMessages = "";
  private String pathMessages = "";
  private String defaultLanguage = "fr";

  /**
   * Constructor declaration
   * @see
   */
  public MsgTrace() {
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
    int nbFiles;
    int i;
    InputStream is = null;
    List<File> theFiles = null;

    allMessages.clear();
    pathMessages = filePath + "/messages";
    if (IsDefaultLanguage(language)) {
      languageMessages = defaultLanguage;
    } else {
      languageMessages = language;
    }
    theFiles = getPropertyFiles(pathMessages, "_"
        + languageMessages.toUpperCase());
    nbFiles = theFiles.size();
    for (i = 0; i < nbFiles; i++) {
      try {
        is = new FileInputStream(theFiles.get(i));
        allMessages.load(is);
        is.close();
      } catch (IOException e) {
        SilverTrace.error("silvertrace", "MsgTrace.initFromProperties()",
            "silvertrace.ERR_TRACE_MESSAGES_FILE_ERROR", "File:["
            + ((File) theFiles.get(i)).getAbsolutePath() + "]", e);
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
    return allMessages.getProperty(messageID, "!!! Message " + messageID
        + " NOT INITIALIZED !!!");
  }

  /**
   * Return the string associated to the message ID
   * @param messageID looks like "modulename.MSG_..."
   * @return the message string (could be language dependant)
   * @see
   */
  public String getMsgString(String messageID, String language) {
    if ((stringValid(language) == false)
        || (language.equalsIgnoreCase(languageMessages))) {
      return getMsgString(messageID);
    } else {
      File thePropFile = null;
      Properties theMessages = new Properties();
      String valret = "!!! Messages " + messageID + " NOT INITIALIZED !!!";
      FileInputStream is;
      String fileName;

      if (messageID.indexOf('.') > 0) {
        fileName = pathMessages + "/"
            + messageID.substring(0, messageID.indexOf('.')) + "Messages_"
            + language + ".properties";
        try {
          thePropFile = new File(fileName);
          is = new FileInputStream(thePropFile);
          theMessages.load(is);
          is.close();
          valret = theMessages.getProperty(messageID, valret);
        } catch (IOException e) {
          SilverTrace.error("silvertrace", "MsgTrace.getMsgString()",
              "silvertrace.ERR_TRACE_MESSAGES_FILE_ERROR", "File:[" + fileName
              + "]", e);
        }
      }
      return valret;
    }
  }

  /**
   * Return true if the string is not null and not empty
   * @param Str the string to test
   * @return true if Str not null and not equal to ""
   * @see
   */
  protected static boolean stringValid(String Str) {
    if (Str != null) {
      if (Str.length() > 0) {
        return true;
      }
    }
    return false;
  }

  /**
   * Reads a boolean property and return it's boolean value
   * @param theProps the Properties object
   * @param propertyName the name of the property to test
   * @param defaultValue the default value to set to the property if it doesn't exist
   * @return true/false
   * @see
   */
  static public boolean getBooleanProperty(Properties theProps,
      String propertyName, boolean defaultValue) {
    String value;
    boolean valret = defaultValue;

    value = theProps.getProperty(propertyName);
    if (value != null) {
      if (value.equalsIgnoreCase("true")) {
        valret = true;
      } else {
        valret = false;
      }
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
    String value;
    boolean valret = defaultValue;
    value = resource.getString(propertyName);
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
    List<File> valret = new ArrayList<File>();
    File[] messageFiles = null;
    File pathMessagesFile = null;
    int nbFiles;
    int i;

    pathMessagesFile = new File(pathFiles);
    if (pathMessagesFile == null) {
      SilverTrace.error("silvertrace", "MsgTrace.getPropertyFiles()",
          "silvertrace.ERR_PATH_NOT_FOUND", "FileMessagesPath:[" + pathFiles
          + "]");
    } else {
      messageFiles = pathMessagesFile.listFiles();
      if (messageFiles != null) {
        nbFiles = messageFiles.length;
        for (i = 0; i < nbFiles; i++) {
          if (messageFiles[i].getName().toUpperCase().endsWith(
              suffix + ".PROPERTIES")) {
            valret.add(messageFiles[i]);
          }
        }
      } else {
        SilverTrace.error("silvertrace", "MsgTrace.getPropertyFiles()",
            "silvertrace.ERR_NO_PROP_FILES_FOUND", "FileMessagesPath:["
            + pathFiles + "]");
      }
    }
    return valret;
  }

  /**
   * Method declaration
   * @param language
   * @return
   * @see
   */
  protected boolean IsDefaultLanguage(String language) {
    if ((stringValid(language) == false)
        || (language.equalsIgnoreCase(defaultLanguage))) {
      return true;
    } else {
      return false;
    }
  }

}
