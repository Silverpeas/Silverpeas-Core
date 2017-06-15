package org.silverpeas.core.workflow.engine;

import org.silverpeas.core.persistence.Transaction;
import org.silverpeas.core.util.Mutable;
import org.silverpeas.core.util.ServiceProvider;
import org.silverpeas.core.workflow.api.WorkflowException;
import org.silverpeas.core.workflow.api.event.TaskSavedEvent;
import org.silverpeas.core.workflow.api.instance.UpdatableHistoryStep;
import org.silverpeas.core.workflow.api.instance.UpdatableProcessInstance;
import org.silverpeas.core.workflow.api.model.State;
import org.silverpeas.core.workflow.engine.instance.ProcessInstanceImpl;

/**
 * A TaskSaved indicates the workflow engine that a task has been saved
 */
class TaskSavedRequest extends AbstractRequest {

  private TaskSavedEvent event;

  protected TaskSavedRequest() {
  }

  public static TaskSavedRequest get(final TaskSavedEvent event) {
    TaskSavedRequest request = ServiceProvider.getService(TaskSavedRequest.class);
    request.event = event;
    return request;
  }

  @Override
  public void process(final Object context) throws InterruptedException {

    // Get the process instance
    UpdatableProcessInstance instance = (UpdatableProcessInstance) event.getProcessInstance();
    String id = instance.getInstanceId();
    Mutable<UpdatableHistoryStep> step = Mutable.of(null);

    Transaction.performInOne(()-> {
      UpdatableHistoryStep newStep;
      UpdatableProcessInstance processInstance = getProcessInstanceRepository().getById(id);

      if (!event.isFirstTimeSaved()) {
        // reload historystep that has been already created
        newStep = (UpdatableHistoryStep) processInstance.getSavedStep(event.getUser().getUserId());
      } else {
        newStep = createHistoryNewStep(event, processInstance);
      }
      step.set(newStep);
      return newStep;
    });

    Transaction.performInOne(()-> {
      UpdatableProcessInstance processInstance = getProcessInstanceRepository().getById(id);

      // Do workflow stuff
      try {
        processEvent(processInstance, step.get().getId(), event.getResolvedState());
        getProcessInstanceRepository().save((ProcessInstanceImpl) processInstance);
      } catch (WorkflowException we) {
        saveError(processInstance, event, we);

        throw new WorkflowException("WorkflowEngineThread.process",
            "workflowEngine.EX_ERR_PROCESS_ADD_TASKSAVED_REQUEST", we);
      }

      return null;
    });
  }

  /**
   * Method declaration
   */
  private boolean processEvent(UpdatableProcessInstance instance, String stepId, State state)
      throws WorkflowException {

    UpdatableHistoryStep step = (UpdatableHistoryStep) instance.getHistoryStep(stepId);
    // only to set the current step of instance to that step
    instance.updateHistoryStep(step);

    // Saving data of step and process instance
    if (event.getDataRecord() != null) {
      instance.saveActionRecord(step, event.getDataRecord());
    }

    // Add current user as working user
    instance.addWorkingUser(step.getUser(), state, step.getUserRoleName());

    // unlock process instance
    instance.unLock();

    // Saved
    step.setActionStatus(UpdatableHistoryStep.ACTION_STATUS_SAVED);

    return false;
  }

  @Override
  protected boolean processEvent(final UpdatableProcessInstance instance, final String stepId)
      throws WorkflowException {
    return false;
  }
}