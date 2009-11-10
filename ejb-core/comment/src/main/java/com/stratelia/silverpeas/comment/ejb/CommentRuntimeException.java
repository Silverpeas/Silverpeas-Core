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

/**
 * Titre : Silverpeas<p>
 * Description : This object provides the versioning runtime execption<p>
 * Copyright : Copyright (c) Stratelia<p>
 * Société : Stratelia<p>
 * @author Jean-Claude Groccia
 * @version 1.0
 */
package com.stratelia.silverpeas.comment.ejb;

import com.stratelia.webactiv.util.exception.SilverpeasRuntimeException;

/*
 * CVS Informations
 *
 * $Id: CommentRuntimeException.java,v 1.1.1.1 2002/08/06 14:47:46 nchaix Exp $
 *
 * $Log: CommentRuntimeException.java,v $
 * Revision 1.1.1.1  2002/08/06 14:47:46  nchaix
 * no message
 *
 * Revision 1.1.2.2  2002/07/30 08:54:35  pbialevich
 * working version
 *
 * Revision 1.1.2.1  2002/07/29 07:46:43  pbialevich
 * draft
 *
 * Revision 1.1.2.1  2002/07/26 12:02:15  pbialevich
 * draft
 *
 * Revision 1.2  2002/07/17 16:15:44  nchaix
 * Merge branche EPAM_130602
 *
 * Revision 1.1.2.1  2002/07/02 14:04:08  pbialevich
 * First version
 *
 * Revision 1.5  2002/01/21 18:00:31  mguillem
 * Stabilisation Lot2
 * Réorganisation des Router et SessionController
 * Suppression dans les fichiers *Exception de 'implements FromModule'
 *
 * Revision 1.4  2002/01/21 17:55:19  mguillem
 * Stabilisation Lot2
 * Réorganisation des Router et SessionController
 * Suppression dans les fichiers *Exception de 'implements FromModule'
 *
 * Revision 1.3  2002/01/08 10:28:13  groccia
 * no message
 *
 * Revision 1.2  2001/12/31 15:43:32  groccia
 * stabilisation
 *
 */

/**
 * Class declaration
 * 
 * 
 * @author
 */
public class CommentRuntimeException extends SilverpeasRuntimeException {

  /**
   * method of interface FromModule
   */
  public String getModule() {
    return "comment";
  }

  /**
   * constructors
   */

  public CommentRuntimeException(String callingClass, int errorLevel,
      String message) {
    super(callingClass, errorLevel, message);
  }

  /**
   * Constructor declaration
   * 
   * 
   * @param callingClass
   * @param errorLevel
   * @param message
   * @param extraParams
   * 
   * @see
   */
  public CommentRuntimeException(String callingClass, int errorLevel,
      String message, String extraParams) {
    super(callingClass, errorLevel, message, extraParams);
  }

  /**
   * Constructor declaration
   * 
   * 
   * @param callingClass
   * @param errorLevel
   * @param message
   * @param nested
   * 
   * @see
   */
  public CommentRuntimeException(String callingClass, int errorLevel,
      String message, Exception nested) {
    super(callingClass, errorLevel, message, nested);
  }

  /**
   * Constructor declaration
   * 
   * 
   * @param callingClass
   * @param errorLevel
   * @param message
   * @param extraParams
   * @param nested
   * 
   * @see
   */
  public CommentRuntimeException(String callingClass, int errorLevel,
      String message, String extraParams, Exception nested) {
    super(callingClass, errorLevel, message, extraParams, nested);
  }

}
