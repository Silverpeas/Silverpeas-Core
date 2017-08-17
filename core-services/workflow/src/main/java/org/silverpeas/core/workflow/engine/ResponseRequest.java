package org.silverpeas.core.workflow.engine;

import org.silverpeas.core.contribution.content.form.FormException;
import org.silverpeas.core.persistence.Transaction;
import org.silverpeas.core.util.Mutable;
import org.silverpeas.core.util.ServiceProvider;
import org.silverpeas.core.workflow.api.ProcessInstanceManager;
import org.silverpeas.core.workflow.api.TaskManager;
import org.silverpeas.core.workflow.api.WorkflowException;
import org.silverpeas.core.workflow.api.event.ResponseEvent;
import org.silverpeas.core.workflow.api.instance.Participant;
import org.silverpeas.core.workflow.api.instance.UpdatableHistoryStep;
import org.silverpeas.core.workflow.api.instance.UpdatableProcessInstance;
import org.silverpeas.core.workflow.api.model.State;
import org.silverpeas.core.workflow.api.task.Task;
import org.silverpeas.core.workflow.engine.instance.HistoryStepImpl;
import org.silverpeas.core.workflow.engine.instance.ProcessInstanceImpl;

/**
 * A ResponseRequest indicates the workflow engine that a user answer a question to an user who had
 * requested a back to a precedent actor/activity
 */
class ResponseRequest extends AbstractRequest {

  private ResponseEvent event;

  protected ResponseRequest() {
  }

  public static ResponseRequest get(final ResponseEvent event) {
    ResponseRequest request = ServiceProvider.getService(ResponseRequest.class);
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
      newStep.setUserId(event.getUser().getUserId());
      newStep.setAction("#response#");
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

      step.set(newStep);
      return newStep;
    });

    Transaction.performInOne(()-> {
      UpdatableProcessInstance processInstance = getProcessInstanceRepository().getById(id);

      // Do workflow stuff
      try {
        processEvent(processInstance, step.get().getId());
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
  private void processEvent(UpdatableProcessInstance instance, String stepId)
      throws WorkflowException {
    // get the history step
    UpdatableHistoryStep step = (UpdatableHistoryStep) instance.getHistoryStep(stepId);
    // only to set the current step to that step
    instance.updateHistoryStep(step);

    // add the answer
    String answer = null;
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
    step.setActionStatus(UpdatableHistoryStep.ACTION_STATUS_PROCESSED);
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
    step.setActionStatus(UpdatableHistoryStep.ACTION_STATUS_AFFECTATIONSDONE);
    instance.updateHistoryStep(step);

    // unlock process instance
    instance.unLock();
  }

}