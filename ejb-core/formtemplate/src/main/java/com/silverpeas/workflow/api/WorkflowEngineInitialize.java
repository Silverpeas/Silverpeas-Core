/*--- formatted by Jindent 2.1, (www.c-lab.de/~jindent) 
 ---*/

package com.silverpeas.workflow.api;

import com.stratelia.silverpeas.silverpeasinitialize.IInitialize;

/**
 * Class declaration
 * 
 * 
 * @author
 */
public class WorkflowEngineInitialize implements IInitialize {

  /**
   * Constructor declaration
   * 
   * 
   * @see
   */
  public WorkflowEngineInitialize() {
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
    // Initialize SilverTrace

    Workflow.initialize();

    return true;
  }

}
