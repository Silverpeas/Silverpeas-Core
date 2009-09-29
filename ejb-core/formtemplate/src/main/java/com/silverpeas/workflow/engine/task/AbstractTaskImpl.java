package com.silverpeas.workflow.engine.task;

import com.silverpeas.form.DataRecord;
import com.silverpeas.workflow.api.WorkflowException;
import com.silverpeas.workflow.api.event.QuestionEvent;
import com.silverpeas.workflow.api.event.ResponseEvent;
import com.silverpeas.workflow.api.event.TaskDoneEvent;
import com.silverpeas.workflow.api.model.ProcessModel;
import com.silverpeas.workflow.api.task.Task;
import com.silverpeas.workflow.api.user.User;
import com.silverpeas.workflow.engine.event.QuestionEventImpl;
import com.silverpeas.workflow.engine.event.ResponseEventImpl;
import com.silverpeas.workflow.engine.event.TaskDoneEventImpl;

/**
 * AbstractTaskImpl implements methods shared by TaskImpl ans CreateTaskImpl.
 */
public abstract class AbstractTaskImpl implements Task {
  /**
   * Builds a TaskImpl.
   */
  public AbstractTaskImpl(User user, String roleName, ProcessModel processModel)
      throws WorkflowException {
    this.user = user;
    this.roleName = roleName;
    this.processModel = processModel;
  }

  /**
   * Returns the user.
   */
  public User getUser() {
    return user;
  }

  /**
   * Returns the name of the role which gived the responsability of this task to
   * the user.
   */
  public String getUserRoleName() {
    return roleName;
  }

  /**
   * Returns the process model.
   */
  public ProcessModel getProcessModel() {
    return processModel;
  }

  /**
   * Builds a TaskDoneEvent from this Task.
   */
  public TaskDoneEvent buildTaskDoneEvent(String actionName, DataRecord data) {
    return (TaskDoneEvent) new TaskDoneEventImpl(this, actionName, data);
  }

  /**
   * Builds a QuestionEvent from this Task.
   */
  public QuestionEvent buildQuestionEvent(String stepId, DataRecord data) {
    return (QuestionEvent) new QuestionEventImpl(this, stepId, data);
  }

  /**
   * Builds a ResponseEvent from this Task.
   */
  public ResponseEvent buildResponseEvent(String questionId, DataRecord data) {
    return (ResponseEvent) new ResponseEventImpl(this, questionId, data);
  }

  /*
   * Internal fields
   */
  private User user = null;
  private String roleName = null;
  private ProcessModel processModel = null;
}
