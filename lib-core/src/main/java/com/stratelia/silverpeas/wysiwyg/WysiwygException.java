/*--- formatted by Jindent 2.1, (www.c-lab.de/~jindent) 
 ---*/

/**
 * Titre : Silverpeas<p>
 * Description : Exception calls<p>
 * Copyright : Copyright (c) Stratelia<p>
 * Société : Stratelia<p>
 * @author Pascale Chaillet
 * @version 1.0
 */
package com.stratelia.silverpeas.wysiwyg;

import com.stratelia.webactiv.util.exception.*;

public class WysiwygException extends SilverpeasException {

  public WysiwygException(String callingClass, int errorLevel, String message) {
    super(callingClass, errorLevel, message);
  }

  public WysiwygException(String callingClass, int errorLevel, String message,
      String extraParams) {
    super(callingClass, errorLevel, message, extraParams);
  }

  public WysiwygException(String callingClass, int errorLevel, String message,
      Exception nested) {
    super(callingClass, errorLevel, message, nested);
  }

  public WysiwygException(String callingClass, int errorLevel, String message,
      String extraParams, Exception nested) {
    super(callingClass, errorLevel, message, extraParams, nested);
  }

  public String getModule() {
    return "wysiwyg";
  }

}