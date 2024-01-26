package org.silverpeas.core.workflow.engine;

import org.silverpeas.core.cache.service.CacheAccessorProvider;
import org.silverpeas.core.contribution.content.form.Field;
import org.silverpeas.core.contribution.content.form.FormException;
import org.silverpeas.core.persistence.datasource.OperationContext;
import org.silverpeas.core.util.ServiceProvider;
import org.silverpeas.kernel.util.StringUtil;
import org.silverpeas.kernel.logging.SilverLogger;
import org.silverpeas.core.workflow.api.TaskManager;
import org.silverpeas.core.workflow.api.WorkflowException;
import org.silverpeas.core.workflow.api.event.GenericEvent;
import org.silverpeas.core.workflow.api.instance.ActionStatus;
import org.silverpeas.core.workflow.api.instance.Actor;
import org.silverpeas.core.workflow.api.instance.Question;
import org.silverpeas.core.workflow.api.instance.UpdatableHistoryStep;
import org.silverpeas.core.workflow.api.instance.UpdatableProcessInstance;
import org.silverpeas.core.workflow.api.model.Action;
import org.silverpeas.core.workflow.api.model.Consequence;
import org.silverpeas.core.workflow.api.model.Consequences;
import org.silverpeas.core.workflow.api.model.ProcessModel;
import org.silverpeas.core.workflow.api.model.QualifiedUsers;
import org.silverpeas.core.workflow.api.model.State;
import org.silverpeas.core.workflow.api.model.Trigger;
import org.silverpeas.core.workflow.api.task.Task;
import org.silverpeas.core.workflow.api.user.User;
import org.silverpeas.core.workflow.external.ExternalAction;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

/**
 * A TimeoutRequest indicates the workflow engine that an instance is in an active state since a
 * too long period
 */
class WorkflowTools {

  private static final String ADMIN_ID = "0";

  private WorkflowTools() {
  }

  /**
   * Compute the new active states and updates affections
   * @param event the task done event
   */
  public static boolean processAction(UpdatableProcessInstance instance, GenericEvent event,
      UpdatableHistoryStep step, boolean unactivateResolvedState) throws WorkflowException {
    // Get the process model
    ProcessModel model = instance.getProcessModel();

    // Get the task manager
    TaskManager taskManager = WorkflowHub.getTaskManager();

    // All active states must be eligible
    String[] states = instance.getActiveStates();
    String resolvedStateName = (event.getResolvedState() == null) ? "" : event.getResolvedState().
        getName();
    HashMap<String, String> oldActiveStates = new HashMap<>();
    HashMap<String, String> eligibleStates = new HashMap<>();

    // Check for actions "redone"
    checkReDoStates(instance, event, states, resolvedStateName);

    // Check for questions with no response sent from the resolved state
    checkQuestions(instance, resolvedStateName);

    // Compute eligibility for states
    states = setEligibleStates(instance, oldActiveStates, eligibleStates);

    Consequence consequence;
    try {
      // Saving data of step and process instance
      if (event.getDataRecord() != null) {
        instance.saveActionRecord(step, event.getDataRecord());
      }

      // removes eligibility of resolved state
      // and removes the resolved state from active state list
      if (unactivateResolvedState && event.getResolvedState() != null) {
        eligibleStates.remove(event.getResolvedState().getName());
      }

      // Retrieves action's consequences
      Action action = model.getAction(event.getActionName());
      Consequences consequences = action.getConsequences();

      // Find first consequence according to comparisons
      consequence = getFirstMatchingConsequence(instance, consequences);
      Objects.requireNonNull(consequence);

      SilverLogger.getLogger(WorkflowTools.class)
          .info("Process action {0}: item = {1}, operator = {2}, value = {3}",
              event.getActionName(), consequence.getItem(), consequence.getOperator(),
              consequence.getValue());

      // if no consequence is verified, then last one will be used
      if (consequence.getKill()) {
        for (final String state : states) {
          removeAffectations(instance, model.getState(state));
        }

        // process external actions
        processTriggers(consequence, instance, event);

        return true;
      }
      State[] targetStates = consequence.getTargetStates();
      State[] unsetStates = consequence.getUnsetStates();

      // for each target state, set eligibility to true
      addStatesToEligibility(eligibleStates, targetStates);

      // for each unset state, set eligibility to false
      removeStatesFromEligibility(eligibleStates, unsetStates);

      // notify user
      List<QualifiedUsers> notifiedUsersList = consequence.getNotifiedUsers();
      notifyUsers(instance, event, taskManager, action, notifiedUsersList);
    } catch (Exception e) {
      // change the action status of the step : Process Failed
      step.setActionStatus(ActionStatus.PROCESS_FAILED);
      instance.updateHistoryStep(step);
      throw new WorkflowException("WorkflowEngineThread.process",
          "workflowEngine.EX_ERR_PROCESS_EVENT", e);
    }

    // change the action status of the step
    step.setActionStatus(ActionStatus.PROCESS_FAILED); // Processed
    instance.updateHistoryStep(step);

    // Compute states and affectations
    computeStates(instance, event, eligibleStates, oldActiveStates);

    // change the action status of the step
    // Affectations done
    step.setActionStatus(ActionStatus.AFFECTATIONS_DONE);
    instance.updateHistoryStep(step);

    // Process external actions
    processTriggers(consequence, instance, event);

    return false;
  }

  private static void notifyUsers(final UpdatableProcessInstance instance, final GenericEvent event,
      final TaskManager taskManager, final Action action,
      final List<QualifiedUsers> notifiedUsersList) throws WorkflowException {
    for (QualifiedUsers notifiedUsers : notifiedUsersList) {
      Actor[] actors = instance.getActors(notifiedUsers, null);
      Task[] tasks = taskManager.createTasks(actors, instance);
      String message = notifiedUsers.getMessage();
      if (StringUtil.isNotDefined(message)) {
        message = action.getDescription("", "");
      }

      // check if link has been disabled in the model
      boolean linkDisabled = notifiedUsers.getLinkDisabled() != null && notifiedUsers.getLinkDisabled();

      // check if sender has been hardcoded in the model
      String senderId = notifiedUsers.getSenderId();
      User forcedUser = null;
      if (senderId != null) {
        forcedUser = getForcedUser(senderId, event.getActionName());
      }
      User sender = (forcedUser == null) ? event.getUserOrSubstitute() : forcedUser;

      for (int i = 0; i < actors.length; i++) {
        taskManager.notifyActor(tasks[i], sender, actors[i], message, linkDisabled);
      }
    }
  }

  private static void removeStatesFromEligibility(final HashMap<String, String> eligibleStates,
      final State[] unsetStates) {
    for (final State unsetState : unsetStates) {
      if (unsetState != null) {
        String name = unsetState.getName();
        eligibleStates.remove(name);
      }
    }
  }

  private static void addStatesToEligibility(final HashMap<String, String> eligibleStates,
      final State[] targetStates) {
    for (final State targetState : targetStates) {
      if (targetState != null) {
        String name = targetState.getName();
        eligibleStates.put(name, name);
      }
    }
  }

  private static Consequence getFirstMatchingConsequence(final UpdatableProcessInstance instance,
      final Consequences consequences) throws FormException, WorkflowException {
    Iterator<Consequence> iConsequences = consequences.getConsequenceList().iterator();
    boolean verified = false;
    Consequence consequence = null;
    while (!verified && iConsequences.hasNext()) {
      consequence = iConsequences.next();
      if (consequence.getItem() != null) {
        Field fieldToCompare = instance.getFolder().getField(consequence.getItem());
        if (fieldToCompare != null && fieldToCompare.getStringValue() != null) {
          verified = consequence.isVerified(fieldToCompare.getStringValue());
        }
      } else {
        verified = true;
      }
    }
    return consequence;
  }

  private static String[] setEligibleStates(final UpdatableProcessInstance instance,
      final HashMap<String, String> oldActiveStates, final HashMap<String, String> eligibleStates) {
    final String[] states = instance.getActiveStates();
    for (final String state : states) {
      eligibleStates.put(state, state);
      oldActiveStates.put(state, state);
    }
    return states;
  }

  private static void checkReDoStates(final UpdatableProcessInstance instance,
      final GenericEvent event, final String[] states, final String resolvedStateName)
      throws WorkflowException {
    boolean backStatus =
        (event.getResolvedState() != null) && instance.isStateInBackStatus(resolvedStateName);
    for (int i = 0; backStatus && i < states.length; i++) {
      if (states[i].equals(resolvedStateName)) {
        instance.reDoState(resolvedStateName, event.getActionDate());
      }
    }
  }

  private static void processTriggers(Consequence consequence, UpdatableProcessInstance instance,
      GenericEvent event) {
    if (consequence != null) {
      Iterator<Trigger> triggers = consequence.getTriggers().iterateTrigger();
      while (triggers.hasNext()) {
        CacheAccessorProvider.getThreadCacheAccessor().getCache().clear();
        Trigger trigger = triggers.next();
        if (trigger != null) {
          try {
            setUpOperationContext(event);
            ExternalAction externalAction = ServiceProvider.getService(trigger.getHandler());
            externalAction.setProcessInstance(instance);
            externalAction.setEvent(event);
            externalAction.setTrigger(trigger);
            externalAction.execute();
          } catch (Exception e) {
            SilverLogger.getLogger(WorkflowTools.class)
                .error("Error while processing triggers: action = {0}, trigger = {1}",
                    new String[] {event.getActionName(), trigger.getName()}, e);
          }
        }
      }
    }
  }

  private static void setUpOperationContext(final GenericEvent event) {
    String currentUserId = ADMIN_ID;
    // For a manual action (event)
    if (event.getUser() != null) {
      currentUserId = event.getUser().getUserId();
    }
    OperationContext.fromUser(org.silverpeas.core.admin.user.model.User.getById(currentUserId));
  }

  /**
   * Compute the new active states and updates affections
   * @param event the task done event
   */
  private static void computeStates(UpdatableProcessInstance instance, GenericEvent event,
      HashMap<String, String> eligibleStates, HashMap<String, String> oldActiveStates)
      throws WorkflowException {
    // Get the process model associated to this event
    ProcessModel model = instance.getProcessModel();

    // Get all the states defined in the abstract model
    State[] states = model.getStates();

    // Test if each state must be activated or not
    for (final State state : states) {
      boolean eligible = eligibleStates.containsKey(state.getName());

      if (eligible) {
        instance.addActiveState(state);
        if (oldActiveStates.containsKey(state.getName())) {
          // State is already affected (it's a loop) ==> remove old tasks and
          // add new ones
          removeAffectations(instance, state);
        }
        computeAffectations(instance, event, state);
      } else {
        if (oldActiveStates.containsKey(state.getName())) {
          // State was affected ==> unassign tasks and remove Active state
          instance.removeActiveState(state);
          removeAffectations(instance, state);
        }
      }
    }
  }

  /**
   * Compute the new affectations for a given state
   * @param event the event
   */
  private static void computeAffectations(UpdatableProcessInstance instance, GenericEvent event,
      State state) throws WorkflowException {
    // Get the task manager
    TaskManager taskManager = WorkflowHub.getTaskManager();

    // Get the working users
    QualifiedUsers wkUsers = state.getWorkingUsers();
    Actor[] actors = instance.getActors(wkUsers, state);

    // Assign tasks to these working users (except for automatic event, in this cas no user for this
    // event)
    if (event.getUser() != null) {
      Task[] tasks = taskManager.createTasks(actors, instance);
      for (final Task task : tasks) {
        taskManager.assignTask(task, event.getUser());
      }
    }

    // Declare these working users in instance
    for (final Actor actor : actors) {
      instance.addWorkingUser(actor, state);
    }

    // Get the interested users
    QualifiedUsers intUsers = state.getInterestedUsers();
    actors = instance.getActors(intUsers, state);

    // Declare these interested users in instance
    for (final Actor actor : actors) {
      instance.addInterestedUser(actor, state);
    }
  }

  /**
   * Remove the old affectations for this state and unassign tasks
   */
  private static void removeAffectations(UpdatableProcessInstance instance, State state)
      throws WorkflowException {
    // Get the task manager
    TaskManager taskManager = WorkflowHub.getTaskManager();

    // Get the working users
    Actor[] actors = instance.getWorkingUsers(state.getName());

    // Unassign tasks to these working users
    Task[] tasks = taskManager.createTasks(actors, instance);
    for (final Task task : tasks) {
      taskManager.unAssignTask(task);
    }

    // removes interested users and working users for resolved state
    instance.removeInterestedUsers(state);
    instance.removeWorkingUsers(state);
  }

  /**
   * Cancel pending questions sent from the given state (cascading is done to the questions from
   * questions and so...)
   * @param instance Process instance
   * @param resolvedStateName state name
   */
  private static void checkQuestions(UpdatableProcessInstance instance, String resolvedStateName)
      throws WorkflowException {
    Question[] questions = instance.getSentQuestions(resolvedStateName);

    for (int i = 0; questions != null && i < questions.length; i++) {
      instance.cancelQuestion(questions[i]);
    }
  }

  private static User getForcedUser(String senderId, String actionName) {
    try {
      return WorkflowHub.getUserManager().getUser(senderId);
    } catch (WorkflowException we) {
      SilverLogger.getLogger(WorkflowTools.class)
          .error("Error while processing {0}: impossible to find the sender with id {1}",
              actionName, senderId, we);
    }
    return null;
  }
}
