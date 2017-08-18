package org.silverpeas.core.workflow.engine;

import org.silverpeas.core.persistence.Transaction;
import org.silverpeas.core.thread.task.AbstractRequestTask;
import org.silverpeas.core.util.Mutable;
import org.silverpeas.core.workflow.api.ProcessInstanceManager;
import org.silverpeas.core.workflow.api.WorkflowException;
import org.silverpeas.core.workflow.api.event.GenericEvent;
import org.silverpeas.core.workflow.api.instance.UpdatableHistoryStep;
import org.silverpeas.core.workflow.api.instance.UpdatableProcessInstance;
import org.silverpeas.core.workflow.engine.instance.HistoryStepImpl;
import org.silverpeas.core.workflow.engine.instance.HistoryStepRepository;
import org.silverpeas.core.workflow.engine.instance.ProcessInstanceImpl;
import org.silverpeas.core.workflow.engine.instance.ProcessInstanceManagerImpl;
import org.silverpeas.core.workflow.engine.instance.ProcessInstanceRepository;

import javax.inject.Inject;

/**
 * Created by Nicolas on 07/06/2017.
 */
public abstract class AbstractRequest implements AbstractRequestTask.Request {

  @Inject
  private ProcessInstanceRepository repository;

  @Inject
  private HistoryStepRepository historyStepRepository;

  protected ProcessInstanceRepository getProcessInstanceRepository() {
    return repository;
  }

  protected HistoryStepRepository getHistoryStepRepository() {
    return historyStepRepository;
  }

  protected void saveError(UpdatableProcessInstance instance, GenericEvent event, Exception we) {
    WorkflowHub.getErrorManager().saveError(instance, event, we);
    setInstanceInError(instance.getInstanceId());
  }

  protected void setInstanceInError(String id) {
    UpdatableProcessInstance processInstance = repository.getById(id);

    // set errorStatus to true
    processInstance.setErrorStatus(true);

    repository.save((ProcessInstanceImpl) processInstance);
  }

  protected UpdatableHistoryStep createHistoryNewStep(final GenericEvent event,
      final UpdatableProcessInstance processInstance) {
    ProcessInstanceManager instanceManager = WorkflowHub.getProcessInstanceManager();
    final UpdatableHistoryStep newStep = (UpdatableHistoryStep) instanceManager.createHistoryStep();
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
    return newStep;
  }

  protected void createProcessInstance(final String id,
      final GenericEvent event,
      final Mutable<UpdatableHistoryStep> step) {
    Transaction.performInOne(()-> {
      ProcessInstanceManager instanceManager = WorkflowHub.getProcessInstanceManager();
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

  protected abstract boolean processEvent(UpdatableProcessInstance instance, String stepId)
      throws WorkflowException;
}