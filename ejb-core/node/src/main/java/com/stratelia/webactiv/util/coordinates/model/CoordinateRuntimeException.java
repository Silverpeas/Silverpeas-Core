/*--- formatted by Jindent 2.1, (www.c-lab.de/~jindent) 
 ---*/

package com.stratelia.webactiv.util.coordinates.model;

import com.stratelia.webactiv.util.exception.*;

/**
 * Class declaration
 * 
 * 
 * @author
 * @version %I%, %G%
 */
public class CoordinateRuntimeException extends SilverpeasRuntimeException {

  /**
   * --------------------------------------------------------------------------
   * constructors constructors
   */
  public CoordinateRuntimeException(String callingClass, int errorLevel,
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
  public CoordinateRuntimeException(String callingClass, int errorLevel,
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
  public CoordinateRuntimeException(String callingClass, int errorLevel,
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
  public CoordinateRuntimeException(String callingClass, int errorLevel,
      String message, String extraParams, Exception nested) {
    super(callingClass, errorLevel, message, extraParams, nested);
  }

  /**
   * --------------------------------------------------------------------------
   * getModule getModule
   */
  public String getModule() {
    return "coordinates";
  }

}
