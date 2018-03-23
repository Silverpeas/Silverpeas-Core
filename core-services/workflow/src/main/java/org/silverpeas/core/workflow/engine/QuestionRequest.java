package org.silverpeas.core.workflow.engine;

import org.silverpeas.core.contribution.content.form.FormException;
import org.silverpeas.core.persistence.Transaction;
import org.silverpeas.core.util.ServiceProvider;
import org.silverpeas.core.workflow.api.TaskManager;
import org.silverpeas.core.workflow.api.WorkflowException;
import org.silverpeas.core.workflow.api.event.GenericEvent;
import org.silverpeas.core.workflow.api.event.QuestionEvent;
import org.silverpeas.core.workflow.api.instance.ActionStatus;
import org.silverpeas.core.workflow.api.instance.Participant;
import org.silverpeas.core.workflow.api.instance.UpdatableHistoryStep;
import org.silverpeas.core.workflow.api.instance.UpdatableProcessInstance;
import org.silverpeas.core.workflow.api.model.State;
import org.silverpeas.core.workflow.api.task.Task;

/**
 * A QuestionRequest indicates the workflow engine that a user ask a back to a precedent
 * actor/activity
 */
class QuestionRequest extends AbstractRequest {

  protected QuestionRequest() {
  }

  public static QuestionRequest get(final QuestionEvent event) {
    QuestionRequest request = ServiceProvider.getService(QuestionRequest.class);
    request.setEvent(event);
    return request;
  }

  @Override
  public void process(final Object context) throws InterruptedException {
    // Get the process instance
    GenericEvent event = getEvent();
    UpdatableProcessInstance instance = (UpdatableProcessInstance) event.getProcessInstance();
    String id = instance.getInstanceId();

    UpdatableHistoryStep step =
        Transaction.performInOne(() -> createHistoryNewStep(new HistoryStepDescriptor()
            .withActionName("#question#")
            .withProcessInstance(instance)));

    processProcessInstance(id, event, step);
  }

  protected boolean processEvent(UpdatableProcessInstance instance, String stepId)
      throws WorkflowException {

    // Create a history step for the question
    UpdatableHistoryStep step = (UpdatableHistoryStep) instance.getHistoryStep(stepId);
    // only to set the current step to that step
    instance.updateHistoryStep(step);

    // add the question
    String question;
    QuestionEvent event = getEvent();
    try {
      question = (String) event.getDataRecord().getField("Content").getObjectValue();
    } catch (FormException fe) {
      throw new WorkflowException("WorkflowEngineThread.process", "workflowEngine.EXP_UNKNOWN_ITEM",
          fe);
    }
    State state = instance.addQuestion(question, event.getStepId(), event.getResolvedState(), event.
        getUser());

    // add the state that is discussed in the list of active states
    instance.addActiveState(state);

    // change the action status of the step
    // Processed
    step.setActionStatus(ActionStatus.PROCESSED);
    instance.updateHistoryStep(step);

    // get the last participant for the discussed state
    Participant participant = instance.getParticipant(state.getName());

    // Get the task manager
    TaskManager taskManager = WorkflowHub.getTaskManager();

    // Assign task to this participant
    Task task = taskManager.createTask(participant, instance);
    taskManager.assignTask(task, participant.getUser());

    // Declare this user as a working user in instance
    instance.addWorkingUser(participant.getUser(), state, participant.getUserRoleName());

    // change the action status of the step
    // Affectations done
    step.setActionStatus(ActionStatus.AFFECTATIONS_DONE);
    instance.updateHistoryStep(step);

    // unlock process instance
    instance.unLock();

    return false;
  }
}