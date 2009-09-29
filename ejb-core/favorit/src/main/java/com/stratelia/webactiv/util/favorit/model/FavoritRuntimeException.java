/*--- formatted by Jindent 2.1, (www.c-lab.de/~jindent) 
 ---*/

package com.stratelia.webactiv.util.favorit.model;

import com.stratelia.webactiv.util.exception.*;

/**
 * Class declaration
 * 
 * 
 * @author
 * @version %I%, %G%
 */
public class FavoritRuntimeException extends SilverpeasRuntimeException {

  /**
   * Constructor declaration
   * 
   * 
   * @param message
   * @param nested
   * 
   * @see
   */
  public FavoritRuntimeException(String message, Exception nested) {
    super(message, nested);
  }

  /**
   * Method declaration
   * 
   * 
   * @return
   * 
   * @see
   */
  public String getModule() {
    return "favorit";
  }

}
