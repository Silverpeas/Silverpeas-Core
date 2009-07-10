package com.silverpeas.workflow.engine.instance;

import com.silverpeas.workflow.api.*;
import com.silverpeas.workflow.api.user.*;
import com.silverpeas.workflow.engine.*;

/**
 * @table SB_Workflow_InterestedUser
 * @depends com.silverpeas.workflow.engine.instance.ProcessInstanceImpl
 * @key-generator MAX
 */
public class InterestedUser extends AbstractReferrableObject 
{
	/**
	 * Used for persistence
	 * @primary-key
	 * @field-name id
	 * @field-type string	
	 * @sql-type integer
	 */
	private String id		= null;

	/**
	 * @field-name userId
	 */
	private String userId		= null;

	/**
	 * @field-name processInstance
	 * @field-type com.silverpeas.workflow.engine.instance.ProcessInstanceImpl
	 * @sql-name instanceId
	 */
	private ProcessInstanceImpl processInstance	= null;

	/**
	 * @field-name state
	 */
	private String state		= null;

	/**
	 * @field-name role
	 */
	private String role		= null;

	/**
	 * Default Constructor
	 */
	public InterestedUser()
	{
	}

	/**
	 * For persistence in database
	 * Get this object id
	 * @return this object id
	 */
	public String getId()
	{
		return id;
	}

	/**
	 * For persistence in database
	 * Set this object id
	 * @param this object id
	 */
	public void setId(String id)
	{
		this.id = id;
	}

	/**
	 * Get role name under which user can access to this instance
	 * @return role name 
	 */
	public String getRole()
	{
		 return role;
	}

	/**
	 * Set role name for which user is affected
	 * @param	role	role name
	 */
	public void setRole(String role)
	{
		this.role = role;
	}

	/**
	 * Get state name for which user can access to this instance
	 * @return state name 
	 */
	public String getState()
	{
		 return state;
	}

	/**
	 * Set state name for which user can access to this instance
	 * @param	state	state name
	 */
	public void setState(String state)
	{
		this.state = state;
	}

	/**
	 * Get the user id
	 * @return	user id
	 */
	public String getUserId()
	{
		return userId;
	}

	/**
	 * Set the user id
	 * @param	userId	user id
	 */
	public void setUserId(String userId)
	{
		this.userId = userId;
	}

	/**
	 * Get the instance to which user is interested
	 * @return	instance
	 */
	public ProcessInstanceImpl getProcessInstance()
	{
		return processInstance;
	}

	/**
	 * Set the instance to which user is interested
	 * @param	processInstance	instance
	 */
	public void setProcessInstance(ProcessInstanceImpl processInstance)
	{
		this.processInstance = processInstance;
	}

	/**
	 * Converts InterestedUser to User
	 * @return an object implementing User interface and containing user details
	 */
	public User toUser()
		throws WorkflowException
	{
		return WorkflowHub.getUserManager().getUser(this.getUserId());
	}

	/**
	 * Get User information from an array of workingUsers
	 * @param	workingUsers	an array of WorkingUser objects
	 * @return	an array of objects implementing User interface and containing user details
	 */
	static public User[] toUser(InterestedUser[] interestedUsers)
		throws WorkflowException
	{
		String[] userIds = new String[interestedUsers.length];
		
		for (int i=0; i<interestedUsers.length; i++)
		{
			userIds[i] = interestedUsers[i].getUserId();
		}
		
		return WorkflowHub.getUserManager().getUsers(userIds);
	}

    /**
     * This method has to be implemented by the referrable object
     * it has to compute the unique key
     * @return The unique key.
     */
    public String getKey()
	{
		return (this.getUserId() + "##" + this.getState() + "##" + this.getRole());
	}
}