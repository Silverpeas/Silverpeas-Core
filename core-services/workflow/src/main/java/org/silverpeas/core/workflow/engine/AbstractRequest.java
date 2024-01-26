package org.silverpeas.core.workflow.engine;

import org.silverpeas.core.persistence.Transaction;
import org.silverpeas.core.thread.task.AbstractRequestTask;
import org.silverpeas.kernel.logging.SilverLogger;
import org.silverpeas.core.workflow.api.ProcessInstanceManager;
import org.silverpeas.core.workflow.api.WorkflowException;
import org.silverpeas.core.workflow.api.event.GenericEvent;
import org.silverpeas.core.workflow.api.instance.ActionStatus;
import org.silverpeas.core.workflow.api.instance.ProcessInstance;
import org.silverpeas.core.workflow.api.instance.UpdatableHistoryStep;
import org.silverpeas.core.workflow.api.instance.UpdatableProcessInstance;
import org.silverpeas.core.workflow.engine.instance.HistoryStepImpl;
import org.silverpeas.core.workflow.engine.instance.HistoryStepRepository;
import org.silverpeas.core.workflow.engine.instance.ProcessInstanceImpl;
import org.silverpeas.core.workflow.engine.instance.ProcessInstanceManagerImpl;
import org.silverpeas.core.workflow.engine.instance.ProcessInstanceRepository;

import javax.inject.Inject;
import java.util.Date;

/**
 * Created by Nicolas on 07/06/2017.
 */
public abstract class AbstractRequest
    implements AbstractRequestTask.Request<AbstractRequestTask.ProcessContext> {

  @Inject
  private ProcessInstanceRepository repository;

  @Inject
  private HistoryStepRepository historyStepRepository;

  private GenericEvent event;

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
    ProcessInstanceImpl processInstance = repository.getById(id);

    // set errorStatus to true
    processInstance.setErrorStatus(true);

    repository.save(processInstance);
  }

  protected UpdatableHistoryStep createHistoryNewStep(final HistoryStepDescriptor descriptor) {
    ProcessInstanceManager instanceManager = WorkflowHub.getProcessInstanceManager();
    SilverLogger.getLogger(this)
        .info("createHistoryNewStep() - InstanceId = {0}",
            descriptor.getProcessInstance().getInstanceId());
    final UpdatableHistoryStep newStep = (UpdatableHistoryStep) instanceManager.createHistoryStep();
    if (event.getUser() != null) {
      newStep.setUserId(event.getUser().getUserId());
    }
    if (event.getSubstitute() != null) {
      newStep.setSubstituteId(event.getSubstitute().getUserId());
    }
    SilverLogger.getLogger(this)
        .info("createHistoryNewStep() - ActionName = {0}", descriptor.getActionName());
    newStep.setAction(descriptor.getActionName());
    newStep.setActionDate(descriptor.getActionDate());
    newStep.setUserRoleName(descriptor.getUserRoleName());
    if (event.getResolvedState() != null) {
      newStep.setResolvedState(event.getResolvedState().getName());
    }
    // To be processed
    newStep.setActionStatus(ActionStatus.TO_BE_PROCESSED);
    newStep.setProcessInstance(descriptor.getProcessInstance());

    // add the new newStep to the processInstance
    getHistoryStepRepository().save((HistoryStepImpl) newStep);
    return newStep;
  }

  protected UpdatableHistoryStep fetchHistoryStep(final String processInstanceId,
      final boolean existingStep) {
    return Transaction.performInOne(() -> {
      UpdatableProcessInstance processInstance =
          getProcessInstanceRepository().getById(processInstanceId);
      if (existingStep) {
        return (UpdatableHistoryStep) processInstance.getSavedStep(event.getUser().getUserId());
      } else {
        return createHistoryNewStep(
            new HistoryStepDescriptor().withProcessInstance(processInstance));

      }
    });
  }

  protected void processProcessInstance(final String id, final GenericEvent event,
      final UpdatableHistoryStep step) {
    SilverLogger.getLogger(this).info("processProcessInstance() - instanceId = {0}",id);
    Transaction.performInOne(() -> {
      ProcessInstanceManager instanceManager = WorkflowHub.getProcessInstanceManager();
      ProcessInstanceImpl processInstance = getProcessInstanceRepository().getById(id);

      // Do workflow stuff
      try {
        boolean removeInstance = processEvent(processInstance, step.getId());
        if (removeInstance) {
          // remove data associated to forms and tasks
          ((ProcessInstanceManagerImpl) instanceManager).removeProcessInstance(id);
        } else {
          getProcessInstanceRepository().save(processInstance);
        }
      } catch (WorkflowException we) {
        saveError(processInstance, event, we);

        throw new WorkflowException("WorkflowEngineThread.process", we.getMessage(), we);
      }

      return null;
    });
  }

  /**
   * Processes the event mapped with the specified process instance and for the specified step.
   * @param instance a process instance.
   * @param stepId the identifier of the step.
   * @return true if the process instance is done, false otherwise. If done, the process instance
   * can be removed from the persistence context.
   * @throws WorkflowException if an error occurs.
   */
  protected abstract boolean processEvent(UpdatableProcessInstance instance, String stepId)
      throws WorkflowException;

  void setEvent(final GenericEvent event) {
    this.event = event;
  }

  @SuppressWarnings("unchecked")
  <T extends GenericEvent> T getEvent() {
    return (T) this.event;
  }

  /**
   * Descriptor used to create a new history step.
   */
  protected class HistoryStepDescriptor {
    private ProcessInstance processInstance;
    private String actionName;
    private Date actionDate;
    private String userRoleName;

    public ProcessInstance getProcessInstance() {
      return processInstance;
    }

    public HistoryStepDescriptor withProcessInstance(final ProcessInstance processInstance) {
      this.processInstance = processInstance;
      return this;
    }

    public String getActionName() {
      return actionName == null ? event.getActionName() : actionName;
    }

    public HistoryStepDescriptor withActionName(final String actionName) {
      this.actionName = actionName;
      return this;
    }

    public String getUserRoleName() {
      return userRoleName == null ? event.getUserRoleName() : userRoleName;
    }

    public HistoryStepDescriptor withUserRoleName(final String userRoleName) {
      this.userRoleName = userRoleName;
      return this;
    }

    public Date getActionDate() {
      return actionDate == null ? event.getActionDate() : actionDate;
    }

    public HistoryStepDescriptor withActionDate(final Date actionDate) {
      this.actionDate = actionDate;
      return this;
    }
  }
}