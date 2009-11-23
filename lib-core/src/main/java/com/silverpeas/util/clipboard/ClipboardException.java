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
/*--- formatted by Jindent 2.1, (www.c-lab.de/~jindent) ---*/

/*
 * @author Dominique Blot
 * @version 1.0
 */

package com.silverpeas.util.clipboard;

import com.stratelia.webactiv.util.exception.*;

/*
 * CVS Informations
 * 
 * $Id: ClipboardException.java,v 1.1.1.1 2002/08/06 14:47:46 nchaix Exp $
 * 
 * $Log: ClipboardException.java,v $
 * Revision 1.1.1.1  2002/08/06 14:47:46  nchaix
 * no message
 *
 * Revision 1.1  2002/01/04 14:03:48  mmarengo
 * Stabilisation Lot 2
 * SilverTrace
 * Exception
 *
 */

/**
 * Class declaration
 * @author
 */
public class ClipboardException extends SilverpeasException {

  /**
   * -------------------------------------------------------------------------- constructor
   * constructor
   */
  public ClipboardException(String callingClass, int errorLevel, String message) {
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
  public ClipboardException(String callingClass, int errorLevel,
      String message, String extraParams) {
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
  public ClipboardException(String callingClass, int errorLevel,
      String message, Exception nested) {
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
  public ClipboardException(String callingClass, int errorLevel,
      String message, String extraParams, Exception nested) {
    super(callingClass, errorLevel, message, extraParams, nested);
  }

  /**
   * -------------------------------------------------------------------------- getModule getModule
   */
  public String getModule() {
    return "clipboard";
  }

}
