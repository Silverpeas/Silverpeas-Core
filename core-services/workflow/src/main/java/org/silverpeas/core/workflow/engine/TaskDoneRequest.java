package org.silverpeas.core.workflow.engine;

import org.silverpeas.core.util.ServiceProvider;
import org.silverpeas.core.workflow.api.WorkflowException;
import org.silverpeas.core.workflow.api.event.TaskDoneEvent;
import org.silverpeas.core.workflow.api.instance.UpdatableHistoryStep;
import org.silverpeas.core.workflow.api.instance.UpdatableProcessInstance;
import org.silverpeas.core.workflow.engine.model.StateImpl;

/**
 * A TaskDone indicates the workflow engine that a task has been done
 */
class TaskDoneRequest extends AbstractRequest {

  protected TaskDoneRequest() {
  }

  public static TaskDoneRequest get(final TaskDoneEvent event) {
    TaskDoneRequest request = ServiceProvider.getService(TaskDoneRequest.class);
    request.setEvent(event);
    return request;
  }

  @Override
  public void process(final Object context) throws InterruptedException {
    TaskDoneEvent event = getEvent();

    // Get the process instance
    UpdatableProcessInstance instance = (UpdatableProcessInstance) event.getProcessInstance();
    String id = instance.getInstanceId();
    UpdatableHistoryStep step = fetchHistoryStep(id, event.isResumingAction());
    processProcessInstance(id, event, step);
  }

  @Override
  protected boolean processEvent(UpdatableProcessInstance instance, String stepId)
      throws WorkflowException {
    TaskDoneEvent event = getEvent();

    // only to set the current step of instance to that step
    UpdatableHistoryStep step = (UpdatableHistoryStep) instance.getHistoryStep(stepId);
    if (event.isResumingAction()) {
      // set user and date of last action
      step.setActionDate(event.getActionDate());
      if (event.getUser().getUserId().equals(step.getUser().getUserId())) {
        step.setSubstituteId(null);
      }
    }
    instance.updateHistoryStep(step);

    // special case : user is resuming creation action - working user must be explicitly removed
    if (event.getResolvedState() == null) {
      instance.removeWorkingUser(event.getUser(), new StateImpl(""), event.getUserRoleName());
    }

    // Remove user from locking users
    instance.unLock(event.getResolvedState(), event.getUser());

    if (WorkflowTools.processAction(instance, event, step, true)) {
      return true;
    }

    // unlock process instance
    instance.unLock();

    return false;
  }
}