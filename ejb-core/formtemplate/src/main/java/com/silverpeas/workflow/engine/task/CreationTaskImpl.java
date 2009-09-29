package com.silverpeas.workflow.engine.task;

import com.silverpeas.workflow.api.WorkflowException;
import com.silverpeas.workflow.api.instance.HistoryStep;
import com.silverpeas.workflow.api.instance.ProcessInstance;
import com.silverpeas.workflow.api.instance.Question;
import com.silverpeas.workflow.api.model.Action;
import com.silverpeas.workflow.api.model.ProcessModel;
import com.silverpeas.workflow.api.model.State;
import com.silverpeas.workflow.api.user.User;

/**
 * A creation task is the first task of a creation instance.
 */
public class CreationTaskImpl extends AbstractTaskImpl {
  String[] actionNames = null;

  /**
   * Builds a CreationTaskImpl.
   */
  public CreationTaskImpl(User user, String roleName, ProcessModel processModel)
      throws WorkflowException {
    super(user, roleName, processModel);
    Action creation = processModel.getCreateAction();
    actionNames = new String[] { creation.getName() };
  }

  /**
   * At this time there is no instance !
   */
  public ProcessInstance getProcessInstance() {
    return null;
  }

  /**
   * Returns the state to be resolved by the user.
   */
  public State getState() {
    return null;
  }

  /**
   * Returns the action names list from which the user must choose to resolve
   * the activity.
   */
  public String[] getActionNames() {
    return actionNames;
  }

  /**
   * no back action possible, return null.
   */
  public HistoryStep[] getBackSteps() {
    return null;
  }

  /**
   * no question possible, return null.
   */
  public Question[] getPendingQuestions() {
    return null;
  }

  /**
   * no question possible, return null.
   */
  public Question[] getSentQuestions() {
    return null;
  }

  /**
   * no question possible, return null.
   */
  public Question[] getRelevantQuestions() {
    return null;
  }
}
