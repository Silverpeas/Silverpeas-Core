package com.silverpeas.workflow.api;

import com.stratelia.webactiv.util.exception.*;

/**
 * Thrown when a fatal error occured in a workflow engine component.
 */
public class WorkflowFatalException extends WorkflowException {
  /**
   * Set the caller and the error message
   */
  public WorkflowFatalException(String caller, String message) {
    super(caller, SilverpeasException.FATAL, message);
  }

  /**
   * Set the caller, the error message and the nested exception.
   */
  public WorkflowFatalException(String caller, String message,
      Exception nestedException) {
    super(caller, SilverpeasException.FATAL, message, nestedException);
  }
}
