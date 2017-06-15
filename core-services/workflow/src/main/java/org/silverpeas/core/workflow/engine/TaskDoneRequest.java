package org.silverpeas.core.workflow.engine;

import org.silverpeas.core.persistence.Transaction;
import org.silverpeas.core.util.Mutable;
import org.silverpeas.core.util.ServiceProvider;
import org.silverpeas.core.workflow.api.ProcessInstanceManager;
import org.silverpeas.core.workflow.api.WorkflowException;
import org.silverpeas.core.workflow.api.event.TaskDoneEvent;
import org.silverpeas.core.workflow.api.instance.UpdatableHistoryStep;
import org.silverpeas.core.workflow.api.instance.UpdatableProcessInstance;
import org.silverpeas.core.workflow.engine.instance.HistoryStepImpl;
import org.silverpeas.core.workflow.engine.instance.ProcessInstanceImpl;
import org.silverpeas.core.workflow.engine.instance.ProcessInstanceManagerImpl;
import org.silverpeas.core.workflow.engine.model.StateImpl;

/**
 * A TaskDone indicates the workflow engine that a task has been done
 */
class TaskDoneRequest extends AbstractRequest {

  private TaskDoneEvent event;

  protected TaskDoneRequest() {
  }

  public static TaskDoneRequest get(final TaskDoneEvent event) {
    TaskDoneRequest request = ServiceProvider.getService(TaskDoneRequest.class);
    request.event = event;
    return request;
  }

  @Override
  public void process(final Object context) throws InterruptedException {

    // Get the process instance
    UpdatableProcessInstance instance = (UpdatableProcessInstance) event.getProcessInstance();
    String id = instance.getInstanceId();
    ProcessInstanceManager instanceManager = WorkflowHub.getProcessInstanceManager();
    Mutable<UpdatableHistoryStep> step = Mutable.of(null);

    Transaction.performInOne(()-> {
      UpdatableHistoryStep newStep = null;
      UpdatableProcessInstance processInstance = getProcessInstanceRepository().getById(id);

      if (event.isResumingAction()) {
        newStep = (UpdatableHistoryStep) processInstance.getSavedStep(event.getUser().getUserId());
      } else {
        // first create the history newStep
        newStep = (UpdatableHistoryStep) instanceManager.createHistoryStep();
        newStep.setUserId(event.getUser().getUserId());
        newStep.setAction(event.getActionName());
        newStep.setActionDate(event.getActionDate());
        newStep.setUserRoleName(event.getUserRoleName());
        if (event.getResolvedState() != null) {
          newStep.setResolvedState(event.getResolvedState().getName());
        }
        // To be processed
        newStep.setActionStatus(0);
        newStep.setProcessInstance(processInstance);

        // add the new newStep to the processInstance
        getHistoryStepRepository().save((HistoryStepImpl) newStep);
      }
      step.set(newStep);
      return newStep;
    });

    Transaction.performInOne(()-> {
      UpdatableProcessInstance processInstance = getProcessInstanceRepository().getById(id);

      // Do workflow stuff
      try {
        boolean removeInstance = processEvent(processInstance, step.get().getId());
        if (removeInstance) {
          // remove data associated to forms and tasks
          ((ProcessInstanceManagerImpl) instanceManager).removeProcessInstance(id);
        } else {
          getProcessInstanceRepository().save((ProcessInstanceImpl) processInstance);
        }
      } catch (WorkflowException we) {
        saveError(processInstance, event, we);

        throw new WorkflowException("WorkflowEngineThread.process",
            "workflowEngine.EX_ERR_PROCESS_ADD_TASKDONE_REQUEST", we);
      }

      return null;
    });
  }

  /**
   * Method declaration
   */
  private boolean processEvent(UpdatableProcessInstance instance, String stepId)
      throws WorkflowException {
    UpdatableHistoryStep step = (UpdatableHistoryStep) instance.getHistoryStep(stepId);

    // only to set the current step of instance to that step
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