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

package org.silverpeas.core.index.indexing.parser;

import org.silverpeas.core.silvertrace.SilverTrace;
import org.silverpeas.core.util.ResourceLocator;
import org.silverpeas.core.util.SettingBundle;

import java.util.MissingResourceException;

/*
 * CVS Informations
 *
 * $Id: ParserHelper.java,v 1.3 2006/07/10 16:22:58 neysseri Exp $
 *
 * $Log: ParserHelper.java,v $
 * Revision 1.3  2006/07/10 16:22:58  neysseri
 * no message
 *
 * Revision 1.2  2004/06/22 15:10:35  neysseri
 * Les titres et descriptions ne sont plus restitues en lowerCase.
 *
 * Revision 1.1.1.1  2002/08/06 14:47:48  nchaix
 * no message
 *
 * Revision 1.2  2002/02/06 11:38:14  mhguig
 * stabilisation moteur de recherche
 *
 * Revision 1.1  2002/01/09 17:22:29  dwenzek
 * mise en place des silvertrace
 *
 */

/**
 * Class declaration
 * @author $Author: neysseri $
 */
public class ParserHelper {

  /**
   * @return the path to the temp directory or null
   */
  static public String getTempDirectory() {
    String tempDirectory = null;
    try {
      tempDirectory = ResourceLocator.getGeneralSettingBundle().getString("tempPath");
    } catch (MissingResourceException e) {
      SilverTrace.warn("indexing", "ParserHelper",
          "indexing.MSG_MISSING_GENERAL_PROPERTIES", null, e);
    }
    return tempDirectory;
  }

  /**
   * @get the time out parameter or 30000
   */
  static public int getTimeOutParameter() {
    SettingBundle settings = null;
    int timeOutParameter = 0;

    try {
      settings =
          ResourceLocator.getSettingBundle("org.silverpeas.index.indexing.IndexEngine");

      if (settings != null) {
        timeOutParameter = Integer.parseInt(settings
            .getString("TimeOutParameter"));
      }
    } catch (MissingResourceException e) {
      SilverTrace.warn("indexing", "ParserHelper",
          "indexing.MSG_MISSING_INDEXENGINE_PROPERTIES", null, e);
    } catch (NumberFormatException e) {
      SilverTrace.warn("indexing", "ParserHelper",
          "indexing.MSG_PARSE_STRING_FAIL", settings
          .getString("TimeOutParameter"), e);
    }
    return timeOutParameter;
  }

}
