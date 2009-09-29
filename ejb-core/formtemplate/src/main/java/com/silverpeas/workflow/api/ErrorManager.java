package com.silverpeas.workflow.api;

import com.silverpeas.workflow.api.error.WorkflowError;
import com.silverpeas.workflow.api.event.GenericEvent;
import com.silverpeas.workflow.api.instance.ProcessInstance;
import com.silverpeas.workflow.api.instance.HistoryStep;

/**
 * The workflow engine services relate to error management.
 */
public interface ErrorManager {
  /**
   * Save an error
   */
  public WorkflowError saveError(ProcessInstance instance, GenericEvent event,
      HistoryStep step, Exception exception);

  /**
   * Get all the errors that occured for a given instance
   */
  public WorkflowError[] getErrorsOfInstance(String instanceId);

  /**
   * Remove all the errors that occured for a given instance Must be called when
   * instance is removed
   */
  public void removeErrorsOfInstance(String instanceId);
}