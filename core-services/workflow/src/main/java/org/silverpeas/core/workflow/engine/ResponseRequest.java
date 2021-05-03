package org.silverpeas.core.workflow.engine;

import org.silverpeas.core.contribution.content.form.FormException;
import org.silverpeas.core.persistence.Transaction;
import org.silverpeas.core.thread.task.AbstractRequestTask;
import org.silverpeas.core.util.ServiceProvider;
import org.silverpeas.core.workflow.api.TaskManager;
import org.silverpeas.core.workflow.api.WorkflowException;
import org.silverpeas.core.workflow.api.event.ResponseEvent;
import org.silverpeas.core.workflow.api.instance.ActionStatus;
import org.silverpeas.core.workflow.api.instance.Participant;
import org.silverpeas.core.workflow.api.instance.UpdatableHistoryStep;
import org.silverpeas.core.workflow.api.instance.UpdatableProcessInstance;
import org.silverpeas.core.workflow.api.model.State;
import org.silverpeas.core.workflow.api.task.Task;

/**
 * A ResponseRequest indicates the workflow engine that a user answer a question to an user who had
 * requested a back to a precedent actor/activity
 */
class ResponseRequest extends AbstractRequest {

  protected ResponseRequest() {
  }

  public static ResponseRequest get(final ResponseEvent event) {
    ResponseRequest request = ServiceProvider.getService(ResponseRequest.class);
    request.setEvent(event);
    return request;
  }

  @Override
  public void process(final AbstractRequestTask.ProcessContext context)
      throws InterruptedException {
    ResponseEvent event = getEvent();

    // Get the process instance
    UpdatableProcessInstance instance = (UpdatableProcessInstance) event.getProcessInstance();
    String id = instance.getInstanceId();

    UpdatableHistoryStep step = Transaction.performInOne(() -> createHistoryNewStep(
        new HistoryStepDescriptor().withActionName("#response#").withProcessInstance(instance)));
    processProcessInstance(id, event, step);
  }

  protected boolean processEvent(UpdatableProcessInstance instance, String stepId)
      throws WorkflowException {
    // get the history step
    UpdatableHistoryStep step = (UpdatableHistoryStep) instance.getHistoryStep(stepId);
    // only to set the current step to that step
    instance.updateHistoryStep(step);

    // add the answer
    String answer;
    ResponseEvent event = getEvent();
    try {
      answer = (String) event.getDataRecord().getField("Content").getObjectValue();
    } catch (FormException fe) {
      throw new WorkflowException("WorkflowEngineThread.process", "workflowEngine.EXP_UNKNOWN_ITEM",
          fe);
    }
    State state = instance.answerQuestion(answer, event.getQuestionId());

    // remove the state that was discussed from the list of active states
    instance.removeActiveState(event.getResolvedState());

    // change the action status of the step : Processed
    step.setActionStatus(ActionStatus.PROCESSED);
    instance.updateHistoryStep(step);

    // get the last participant for the discussed state
    Participant participant = instance.getParticipant(state.getName());

    // Get the task manager
    TaskManager taskManager = WorkflowHub.getTaskManager();

    // Unassign task to this participant
    Task task = taskManager.createTask(participant, instance);
    taskManager.unAssignTask(task);

    // Remove this user of working user list
    instance.removeWorkingUser(participant.getUser(), state, participant.getUserRoleName());

    // change the action status of the step : Affectations done
    step.setActionStatus(ActionStatus.AFFECTATIONS_DONE);
    instance.updateHistoryStep(step);

    // unlock process instance
    instance.unLock();

    return false;
  }

}