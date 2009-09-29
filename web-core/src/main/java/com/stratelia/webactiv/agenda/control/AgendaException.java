/**
 * Titre : Silverpeas<p>
 * Description : This object provides the calendar exeception<p>
 * Copyright : Copyright (c) Stratelia<p>
 * Société : Stratelia<p>
 * @author Jean-Claude Groccia
 * @version 1.0
 * Created on 26 decembre 2001, 16:38
 */

package com.stratelia.webactiv.agenda.control;

import com.stratelia.webactiv.util.exception.SilverpeasException;

public class AgendaException extends SilverpeasException {

  public AgendaException(String callingClass, int errorLevel, String message) {
    super(callingClass, errorLevel, message);
  }

  public AgendaException(String callingClass, int errorLevel, String message,
      String extraParams) {
    super(callingClass, errorLevel, message, extraParams);
  }

  public AgendaException(String callingClass, int errorLevel, String message,
      Exception nested) {
    super(callingClass, errorLevel, message, nested);
  }

  public AgendaException(String callingClass, int errorLevel, String message,
      String extraParams, Exception nested) {
    super(callingClass, errorLevel, message, extraParams, nested);
  }

  /**
   * method of interface FromModule
   */
  public String getModule() {
    return "agenda";
  }

}
