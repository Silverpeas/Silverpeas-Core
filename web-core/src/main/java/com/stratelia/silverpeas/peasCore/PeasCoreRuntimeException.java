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

package com.stratelia.silverpeas.peasCore;

import com.stratelia.webactiv.util.exception.*;

/*
 * CVS Informations
 * 
 * $Id: PeasCoreRuntimeException.java,v 1.2 2002/10/28 16:45:37 neysseri Exp $
 * 
 * $Log: PeasCoreRuntimeException.java,v $
 * Revision 1.2  2002/10/28 16:45:37  neysseri
 * Branch "InterestCenters" merging
 *
 * Revision 1.1.1.1  2002/08/06 14:47:52  nchaix
 * no message
 *
 * Revision 1.1  2002/01/28 15:28:21  tleroi
 * Split clipboard and personalization
 *
 * Revision 1.2  2002/01/18 18:04:07  tleroi
 * Centralize URLS + Stabilisation Lot 2 - SilverTrace et Exceptions
 *
 */

/**
 * Class declaration
 * @author t.leroi
 */
public class PeasCoreRuntimeException extends SilverpeasRuntimeException {
  /**
   * -------------------------------------------------------------------------- constructor
   * constructor
   */
  public PeasCoreRuntimeException(String callingClass, int errorLevel,
      String message) {
    super(callingClass, errorLevel, message);
  }

  public PeasCoreRuntimeException(String callingClass, int errorLevel,
      String message, String extraParams) {
    super(callingClass, errorLevel, message, extraParams);
  }

  public PeasCoreRuntimeException(String callingClass, int errorLevel,
      String message, Exception nested) {
    super(callingClass, errorLevel, message, nested);
  }

  public PeasCoreRuntimeException(String callingClass, int errorLevel,
      String message, String extraParams, Exception nested) {
    super(callingClass, errorLevel, message, extraParams, nested);
  }

  /**
   * Method declaration
   * @return
   * @see
   */
  public String getModule() {
    return "peasCore";
  }
}
