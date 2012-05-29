/**
 * Copyright (C) 2000 - 2011 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU Affero General Public License as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of the GPL, you may
 * redistribute this Program in connection with Free/Libre Open Source Software ("FLOSS")
 * applications as described in Silverpeas's FLOSS exception. You should have received a copy of the
 * text describing the FLOSS exception, and it is also available here:
 * "http://repository.silverpeas.com/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License along with this program.
 * If not, see <http://www.gnu.org/licenses/>.
 */
package com.stratelia.webactiv.util.indexEngine.parser;

import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.util.GeneralPropertiesManager;
import com.stratelia.webactiv.util.ResourceLocator;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.MissingResourceException;
import org.silverpeas.attachment.BinaryInputStream;

/**
 * Class declaration
 *
 * @author $Author: neysseri $
 */
public class ParserHelper {

  /**
   * @return the path to the temp directory or null
   */
  static public String getTempDirectory() {
    String tempDirectory = null;
    try {
      tempDirectory = GeneralPropertiesManager.getString("tempPath");
    } catch (MissingResourceException e) {
      SilverTrace.warn("indexEngine", "ParserHelper",
          "indexEngine.MSG_MISSING_GENERAL_PROPERTIES", null, e);
    }
    return tempDirectory;
  }

  /**
   * @get the time out parameter or 30000
   */
  static public int getTimeOutParameter() {
    ResourceLocator resourceLocator = null;
    int timeOutParameter = 0;

    try {
      resourceLocator = new ResourceLocator(
          "com.stratelia.webactiv.util.indexEngine.IndexEngine", "");

      if (resourceLocator != null) {
        timeOutParameter = Integer.parseInt(resourceLocator.getString("TimeOutParameter"));
      }
    } catch (MissingResourceException e) {
      SilverTrace.warn("indexEngine", "ParserHelper",
          "indexEngine.MSG_MISSING_INDEXENGINE_PROPERTIES", null, e);
    } catch (NumberFormatException e) {
      SilverTrace.warn("indexEngine", "ParserHelper",
          "indexEngine.MSG_PARSE_STRING_FAIL", resourceLocator.getString("TimeOutParameter"), e);
    }
    return timeOutParameter;
  }

  public static InputStream getContent(String path) throws IOException {
    File file = new File(path);
    if (file.exists()) {
      return new BufferedInputStream(new FileInputStream(file));
    }
    //else let's look int the JCR
    return new BinaryInputStream(path);
  }

 
}
