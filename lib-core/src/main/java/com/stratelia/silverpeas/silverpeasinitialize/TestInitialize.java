/*--- formatted by Jindent 2.1, (www.c-lab.de/~jindent) 
 ---*/

package com.stratelia.silverpeas.silverpeasinitialize;

import com.stratelia.silverpeas.silverpeasinitialize.IInitialize;

/**
 * Title: Description: Copyright: Copyright (c) 2001 Company:
 * 
 * @author EDurand
 * @version 1.0
 */
public class TestInitialize implements IInitialize {

  /**
   * Constructor declaration
   * 
   * 
   * @see
   */
  public TestInitialize() {
  }

  /**
   * Method declaration
   * 
   * 
   * @return
   * 
   * @see
   */
  public boolean Initialize() {
    System.out.println("Silverpeas Initializer well started.");
    return true;
  }

}
