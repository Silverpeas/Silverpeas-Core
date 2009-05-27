package com.silverpeas.workflow.api;

import com.silverpeas.workflow.api.user.User;
import com.silverpeas.workflow.api.user.UserSettings;

/**
 * The workflow engine services relate to user management.
 */
public interface UserManager 
{
   /**
    * Returns the user with the given userId
	*
	* @return the user with the given userId.
	* @throw  WorkflowException if the userId is unknown.
	*/
	public User getUser(String userId) throws WorkflowException;

   /**
	* Make a User[] from a userIds' String[].
	*
	* @throw  WorkflowException if a userId is unknown.
	*/
	public User[] getUsers(String[] userIds) throws WorkflowException;

   /**
    * Returns all the roles of a given user relative to a processModel.
	*/
	public String[] getRoleNames(User user, String processModelId)
	   throws WorkflowException;

   /**
    * Returns all the users having a given role relative to a processModel.
	*/
	public User[] getUsersInRole(String roleName, String processModelId)
		throws WorkflowException;

   /**
    * returns all the known info for an user;
    * Each returned value can be used as a parameter to the
    * User method getInfo().
    */
   public String[] getUserInfoNames();

	/**
     * Get a user from a given user and relation
	 * @param		user		reference user
	 * @param		relation	relation between given user and searched user
	 * @param	peasId	the id of workflow peas associated to that information
	 * @return	the user that has the given relation with given user
	 */
	public User	getRelatedUser(User user, String relation, String peasId) throws WorkflowException;


	/**
	 * Get the user settings in database
	 * The full list of information is described in the process model
	 * @param	userId	the user Id
	 * @param	peasId	the id of workflow peas associated to that information
	 * @return	UserSettings 
	 * @see ProcessModel
	 */
	public UserSettings getUserSettings(String userId, String peasId) throws WorkflowException;
	
	public void resetUserSettings(String userId, String peasId) throws WorkflowException;

	/**
	 * Get an empty user settings in database
	 * The full list of information is described in the process model
	 * @param	userId	the user Id
	 * @param	peasId	the id of workflow peas associated to that information
	 * @return	UserSettings 
	 * @see ProcessModel
	 */
	public UserSettings getEmptyUserSettings(String userId, String peasId);
}
