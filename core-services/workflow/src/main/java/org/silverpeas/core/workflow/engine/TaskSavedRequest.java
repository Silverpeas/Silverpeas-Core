package org.silverpeas.core.workflow.engine;

import org.silverpeas.core.thread.task.AbstractRequestTask;
import org.silverpeas.core.util.ServiceProvider;
import org.silverpeas.core.util.logging.SilverLogger;
import org.silverpeas.core.workflow.api.WorkflowException;
import org.silverpeas.core.workflow.api.event.TaskSavedEvent;
import org.silverpeas.core.workflow.api.instance.ActionStatus;
import org.silverpeas.core.workflow.api.instance.UpdatableHistoryStep;
import org.silverpeas.core.workflow.api.instance.UpdatableProcessInstance;

/**
 * A TaskSaved indicates the workflow engine that a task has been saved
 */
class TaskSavedRequest extends AbstractRequest {

  protected TaskSavedRequest() {
  }

  public static TaskSavedRequest get(final TaskSavedEvent event) {
    TaskSavedRequest request = ServiceProvider.getService(TaskSavedRequest.class);
    request.setEvent(event);
    return request;
  }

  @Override
  public void process(final AbstractRequestTask.ProcessContext context)
      throws InterruptedException {
    TaskSavedEvent event = getEvent();

    // Get the process instance
    UpdatableProcessInstance instance = (UpdatableProcessInstance) event.getProcessInstance();
    String id = instance.getInstanceId();

    UpdatableHistoryStep step = fetchHistoryStep(id, !event.isFirstTimeSaved());
    processProcessInstance(id, event, step);
  }

  @Override
  protected boolean processEvent(UpdatableProcessInstance instance, String stepId)
      throws WorkflowException {
    SilverLogger.getLogger(this).info("processEvent() - instanceId = {0} stepId = {1}",instance.getInstanceId(), stepId);

    TaskSavedEvent event = getEvent();

    // only to set the current step of instance to that step
    UpdatableHistoryStep step = (UpdatableHistoryStep) instance.getHistoryStep(stepId);
    instance.updateHistoryStep(step);

    // Saving data of step and process instance
    if (event.getDataRecord() != null) {
      instance.saveActionRecord(step, event.getDataRecord());
    }

    // Add current user as working user
    instance.addWorkingUser(step.getUser(), event.getResolvedState(), step.getUserRoleName());

    // unlock process instance
    instance.unLock();

    // Saved
    step.setActionStatus(ActionStatus.SAVED);

    return false;
  }

}