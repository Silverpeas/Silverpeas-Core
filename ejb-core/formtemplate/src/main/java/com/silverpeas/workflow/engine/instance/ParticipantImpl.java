package com.silverpeas.workflow.engine.instance;

import com.silverpeas.workflow.api.instance.Participant;
import com.silverpeas.workflow.api.model.State;
import com.silverpeas.workflow.api.user.User;

public class ParticipantImpl extends ActorImpl implements Participant {
  final private String action;

  /**
   * Constructor Definitivly set the user, userRoleName, state and the action
   * 
   * @param user
   *          user aka the actor
   * @param userRoleName
   *          name of the role under which the user was/may be an actor
   * @param state
   *          state for which the user was/may be an actor
   * @param action
   *          name of the action in which has acted the participant
   */
  public ParticipantImpl(User user, String userRoleName, State state,
      String action) {
    super(user, userRoleName, state);
    this.action = action;
  }

  /**
   * Get the action in which has acted the participant
   * 
   * @return Action name
   */
  public String getAction() {
    return action;
  }
}