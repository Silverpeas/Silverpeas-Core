/*--- formatted by Jindent 2.1, (www.c-lab.de/~jindent) 
 ---*/

/**
 * Titre : Silverpeas<p>
 * Description : This object provides the calendar exeception<p>
 * Copyright : Copyright (c) Stratelia<p>
 * Société : Stratelia<p>
 * @author Jean-Claude Groccia
 * @version 1.0
 * Created on 26 decembre 2001, 16:38
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
 * Réorganisation des Router et SessionController
 * Suppression dans les fichiers *Exception de 'implements FromModule'
 *
 * Revision 1.3  2002/01/09 09:56:57  groccia
 * stabilisation lot2
 *
 */

/**
 * Class declaration
 * 
 * 
 * @author
 */
public class PortletException extends SilverpeasException {

  /**
   * Constructor declaration
   * 
   * 
   * @param callingClass
   * @param errorLevel
   * @param message
   * 
   * @see
   */
  public PortletException(String callingClass, int errorLevel, String message) {
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
  public PortletException(String callingClass, int errorLevel, String message,
      String extraParams) {
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
  public PortletException(String callingClass, int errorLevel, String message,
      Exception nested) {
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
