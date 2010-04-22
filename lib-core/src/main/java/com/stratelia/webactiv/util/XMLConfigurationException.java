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

/*
 * XMLConfigurationException.java
 *
 * Created on 5 mars 2001, 10:57
 */

package com.stratelia.webactiv.util;

/**
 * @author jpouyadou
 * @version
 */
public class XMLConfigurationException extends Exception {
  public final static int KEY_NOT_FOUND = 1;
  public final static int INVALID_VALUE = 2;
  public int m_Code = 0;

  /** Creates new XMLConfigurationException */
  static String getMessageForCode(int code) {
    String msg;
    switch (code) {
      case KEY_NOT_FOUND:
        msg = "The requested key was not found in the configuration store";
        break;
      case INVALID_VALUE:
        msg = "The value for the requested key is invalid";
        break;
      default:
        msg = "";
        break;
    }
    return (msg);
  }

  public XMLConfigurationException(int code) {
    super(getMessageForCode(code));
    m_Code = code;
  }

  public XMLConfigurationException(int code, String msg) {
    super(getMessageForCode(code) + ":" + msg);
    m_Code = code;
  }

  public int getCode() {
    return (m_Code);
  }
}
