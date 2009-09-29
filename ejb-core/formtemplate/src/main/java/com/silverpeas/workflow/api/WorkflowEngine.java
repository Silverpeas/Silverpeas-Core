package com.silverpeas.workflow.api;

import com.silverpeas.workflow.api.user.User;
import com.silverpeas.workflow.api.instance.Actor;
import com.silverpeas.workflow.api.instance.UpdatableProcessInstance;
import com.silverpeas.workflow.api.event.TaskDoneEvent;
import com.silverpeas.workflow.api.event.QuestionEvent;
import com.silverpeas.workflow.api.event.ResponseEvent;
import com.silverpeas.workflow.api.WorkflowException;

/**
 * The workflow engine main services.
 */
public interface WorkflowEngine {
  /**
   * A task has been done and sent to the workflow Enginewhich has to process
   * it.
   * 
   * @param event
   *          the task event that has been done.
   */
  public void process(TaskDoneEvent event) throws WorkflowException;

  /**
   * A question has been sent to a previous participant
   * 
   * @param event
   *          the question event containing all necessary information
   */
  public void process(QuestionEvent event) throws WorkflowException;

  /**
   * A question had been sent to a previous participant. A response is sent !
   * 
   * @param event
   *          the response event containing all necessary information
   */
  public void process(ResponseEvent event) throws WorkflowException;

  /**
   * Do re-affectation for given states Remove users as working users and
   * unassign corresponding tasks Add users as working users and assign
   * corresponding tasks
   */
  public void reAssignActors(UpdatableProcessInstance instance,
      Actor[] unAssignedActors, Actor[] assignedActors, User user)
      throws WorkflowException;

}
