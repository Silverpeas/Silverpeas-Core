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
 * "http://www.silverpeas.org/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.stratelia.silverpeas.portlet;

import com.stratelia.webactiv.util.exception.SilverpeasException;

/*
 * CVS Informations
 * 
 * $Id: PortletException.java,v 1.1.1.1 2002/08/06 14:47:52 nchaix Exp $
 * 
 * $Log: PortletException.java,v $
 * Revision 1.1.1.1  2002/08/06 14:47:52  nchaix
 * no message
 *
 * Revision 1.4  2002/01/22 08:52:25  mguillem
 * Stabilisation Lot2
 * R�organisation des Router et SessionController
 * Suppression dans les fichiers *Exception de 'implements FromModule'
 *
 * Revision 1.3  2002/01/09 09:56:57  groccia
 * stabilisation lot2
 *
 */

/**
 * Class declaration
 * @author
 */
public class PortletException extends SilverpeasException {

  /**
   * Constructor declaration
   * @param callingClass
   * @param errorLevel
   * @param message
   * @see
   */
  public PortletException(String callingClass, int errorLevel, String message) {
    super(callingClass, errorLevel, message);
  }

  /**
   * Constructor declaration
   * @param callingClass
   * @param errorLevel
   * @param message
   * @param extraParams
   * @see
   */
  public PortletException(String callingClass, int errorLevel, String message,
      String extraParams) {
    super(callingClass, errorLevel, message, extraParams);
  }

  /**
   * Constructor declaration
   * @param callingClass
   * @param errorLevel
   * @param message
   * @param nested
   * @see
   */
  public PortletException(String callingClass, int errorLevel, String message,
      Exception nested) {
    super(callingClass, errorLevel, message, nested);
  }

  /**
   * Constructor declaration
   * @param callingClass
   * @param errorLevel
   * @param message
   * @param extraParams
   * @param nested
   * @see
   */
  public PortletException(String callingClass, int errorLevel, String message,
      String extraParams, Exception nested) {
    super(callingClass, errorLevel, message, extraParams, nested);
  }

  /**
   * method of interface FromModule
   */
  public String getModule() {
    return "portlet";
  }

}
