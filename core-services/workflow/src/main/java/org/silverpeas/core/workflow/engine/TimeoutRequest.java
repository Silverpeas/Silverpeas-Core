package org.silverpeas.core.workflow.engine;

import org.silverpeas.core.persistence.Transaction;
import org.silverpeas.core.util.Mutable;
import org.silverpeas.core.util.ServiceProvider;
import org.silverpeas.core.workflow.api.ProcessInstanceManager;
import org.silverpeas.core.workflow.api.WorkflowException;
import org.silverpeas.core.workflow.api.event.TimeoutEvent;
import org.silverpeas.core.workflow.api.instance.UpdatableHistoryStep;
import org.silverpeas.core.workflow.api.instance.UpdatableProcessInstance;
import org.silverpeas.core.workflow.engine.instance.HistoryStepImpl;

import java.util.Date;

/**
 * A TimeoutRequest indicates the workflow engine that an instance is in an active state since a
 * too long period
 */
class TimeoutRequest extends AbstractRequest {

  private TimeoutEvent event;

  protected TimeoutRequest() {
  }

  public static TimeoutRequest get(final TimeoutEvent event) {
    TimeoutRequest request = ServiceProvider.getService(TimeoutRequest.class);
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
      UpdatableProcessInstance processInstance = getProcessInstanceRepository().getById(id);

        // first create the history newStep
      UpdatableHistoryStep newStep = (UpdatableHistoryStep) instanceManager.createHistoryStep();
      newStep.setAction(event.getActionName());
      newStep.setActionDate(new Date());
      newStep.setUserRoleName("supervisor");
      if (event.getResolvedState() != null) {
        newStep.setResolvedState(event.getResolvedState().getName());
      }
      // To be processed
      newStep.setActionStatus(0);
      newStep.setProcessInstance(processInstance);

      // add the new newStep to the processInstance
      getHistoryStepRepository().save((HistoryStepImpl) newStep);

      step.set(newStep);
      return newStep;
    });

    createProcessInstance(id, event, step);
  }

  protected boolean processEvent(UpdatableProcessInstance instance, String stepId)
      throws WorkflowException {
    // get the history step
    UpdatableHistoryStep step = (UpdatableHistoryStep) instance.getHistoryStep(stepId);
    // only to set the current step to that step
    instance.updateHistoryStep(step);

    // set timeout status on given state
    instance.addTimeout(event.getResolvedState());

    // process Action
    return WorkflowTools.processAction(instance, event, step, false);
  }

}