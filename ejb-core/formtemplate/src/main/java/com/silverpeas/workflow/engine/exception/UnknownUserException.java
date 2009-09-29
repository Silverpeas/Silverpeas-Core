package com.silverpeas.workflow.engine.exception;

import com.silverpeas.workflow.api.*;

/**
 * Thrown when a user is unknown
 */
public class UnknownUserException extends WorkflowException {
  /**
   * Set the caller
   */
  public UnknownUserException(String caller, String userId) {
    super(caller, "workflowEngine.EXP_UNKNOWN_USER", userId);
  }

  /**
   * Set the caller and the nested exception
   */
  public UnknownUserException(String caller, String userId, Exception nested) {
    super(caller, "workflowEngine.EXP_UNKNOWN_USER", userId, nested);
  }
}
