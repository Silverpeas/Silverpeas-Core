package org.silverpeas.core.workflow.engine;

import org.silverpeas.core.persistence.Transaction;
import org.silverpeas.core.thread.task.AbstractRequestTask;
import org.silverpeas.core.util.ServiceProvider;
import org.silverpeas.core.workflow.api.WorkflowException;
import org.silverpeas.core.workflow.api.event.TimeoutEvent;
import org.silverpeas.core.workflow.api.instance.UpdatableHistoryStep;
import org.silverpeas.core.workflow.api.instance.UpdatableProcessInstance;

import java.util.Date;

/**
 * A TimeoutRequest indicates the workflow engine that an instance is in an active state since a too
 * long period
 */
class TimeoutRequest extends AbstractRequest {

  protected TimeoutRequest() {
  }

  public static TimeoutRequest get(final TimeoutEvent event) {
    TimeoutRequest request = ServiceProvider.getService(TimeoutRequest.class);
    request.setEvent(event);
    return request;
  }

  @Override
  public void process(final AbstractRequestTask.ProcessContext context)
      throws InterruptedException {
    TimeoutEvent event = getEvent();

    // Get the process instance
    UpdatableProcessInstance instance = (UpdatableProcessInstance) event.getProcessInstance();
    String id = instance.getInstanceId();

    UpdatableHistoryStep step = Transaction.performInOne(() -> createHistoryNewStep(
        new HistoryStepDescriptor().withUserRoleName("supervisor")
            .withActionDate(new Date())
            .withProcessInstance(instance)));

    processProcessInstance(id, event, step);
  }

  @Override
  protected boolean processEvent(UpdatableProcessInstance instance, String stepId)
      throws WorkflowException {
    // get the history step
    UpdatableHistoryStep step = (UpdatableHistoryStep) instance.getHistoryStep(stepId);
    // only to set the current step to that step
    instance.updateHistoryStep(step);

    // set timeout status on given state
    TimeoutEvent event = getEvent();
    instance.addTimeout(event.getResolvedState());

    // process Action
    return WorkflowTools.processAction(instance, event, step, false);
  }

}