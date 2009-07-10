package com.silverpeas.workflow.api.instance;

import com.silverpeas.workflow.api.model.State;
import com.silverpeas.workflow.api.user.User;

/**
 * A Actor object represents a 3-tuple user/roleName/state
 */
public interface Actor
{
    /**
	 * Returns the actor as a User object
	 */
    public User getUser();

    /**
	 * get the name of the role under which the user was/may be an actor
	 * @return the role's name
	 */
    public String getUserRoleName();

    /**
	 * get the name of the state for which the user was/may be an actor
	 * @return the state's name
	 */
    public State getState();

}