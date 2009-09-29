package com.silverpeas.workflow.engine.instance;

import com.silverpeas.workflow.api.instance.Actor;
import com.silverpeas.workflow.api.user.User;
import com.silverpeas.workflow.api.model.State;

/**
 * A Actor object represents a person that can act on a state
 */
public class ActorImpl implements Actor {
  final private User user;
  final private String userRoleName;
  final private State state;

  /**
   * Constructor Definitivly set the user, userRoleName and the state
   * 
   * @param user
   *          user aka the actor
   * @param userRoleName
   *          name of the role under which the user was/may be an actor
   * @param state
   *          state for which the user was/may be an actor
   */
  public ActorImpl(User user, String userRoleName, State state) {
    this.user = user;
    this.userRoleName = userRoleName;
    this.state = state;
  }

  /**
   * Returns the actor as a User object
   */
  public User getUser() {
    return user;
  }

  /**
   * get the name of the role under which the user was/may be an actor
   * 
   * @return the role's name
   */
  public String getUserRoleName() {
    return userRoleName;
  }

  /**
   * get the name of the state for which the user was/may be an actor
   * 
   * @return the state as a State object
   */
  public State getState() {
    return state;
  }
}