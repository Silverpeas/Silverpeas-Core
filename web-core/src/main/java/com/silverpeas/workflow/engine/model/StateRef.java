package com.silverpeas.workflow.engine.model;

import java.io.Serializable;

import com.silverpeas.workflow.api.model.State;
import com.silverpeas.workflow.api.model.StateSetter;

/**
 * Class implementing the representation of the &lt;set&gt; and &lt;unset&gt; elements of a Process Model.
**/
public class StateRef implements Serializable, StateSetter
{
	private State state;

    /**
	 * Get the referred state
     */
    public State getState()
    {
        return this.state;
    }

    /**
	 * Set the referred state
     * @param state state to refer
     */
    public void setState(State state)
    {
        this.state = state;
    }
}