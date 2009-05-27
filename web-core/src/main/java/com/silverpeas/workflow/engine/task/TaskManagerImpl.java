package com.silverpeas.workflow.engine.task;

import java.util.Hashtable;
import java.util.StringTokenizer;
import java.util.Vector;

import com.silverpeas.form.DataRecord;
import com.silverpeas.form.DataRecordUtil;
import com.silverpeas.workflow.api.WorkflowException;
import com.silverpeas.workflow.api.task.Task;
import com.silverpeas.workflow.api.user.User;
import com.stratelia.silverpeas.notificationManager.NotificationManagerException;
import com.stratelia.silverpeas.notificationManager.NotificationMetaData;
import com.stratelia.silverpeas.notificationManager.NotificationParameters;
import com.stratelia.silverpeas.notificationManager.NotificationSender;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.beans.admin.Admin;
import com.stratelia.webactiv.beans.admin.AdminException;
import com.stratelia.webactiv.beans.admin.ComponentInst;
import com.stratelia.webactiv.calendar.backbone.TodoBackboneAccess;
import com.stratelia.webactiv.calendar.backbone.TodoDetail;
import com.stratelia.webactiv.calendar.model.Attendee;


/**
 * The workflow engine services relate to task management.
 */
public class TaskManagerImpl extends AbstractTaskManager 
{
	final static Admin admin = new Admin();
	static Hashtable notificationSenders = new Hashtable();

	/**
	* Adds a new task in the user's todos.
	*
	* Returns the external id given by the external todo system.
	*/
	public void assignTask(Task task) throws WorkflowException
	{
		String componentId = task.getProcessInstance().getModelId();
		ComponentInst compoInst = null;

		try
		{
			compoInst = admin.getComponentInst(componentId);
		}
		catch (AdminException e)
		{
			throw new WorkflowException("TaskManagerImpl.assignTask", "workflowEngine.EX_GET_COMPONENT_INST", e);
		}
		
		TodoDetail todo = new TodoDetail();
		todo.setId(task.getProcessInstance().getInstanceId());
		todo.setSpaceId(compoInst.getDomainFatherId());
		todo.setComponentId(componentId);
		todo.setName("activité : " + task.getState().getLabel(task.getUserRoleName(), "fr"));

		Vector attendees = new Vector();
		attendees.add(new Attendee(task.getUser().getUserId()));
		todo.setAttendees(attendees);

		todo.setDelegatorId(task.getUser().getUserId());
		todo.setExternalId( getExternalId(task) );

        TodoBackboneAccess todoBBA = new TodoBackboneAccess();
        todoBBA.addEntry(todo);
	}

	/**
	* Removes a task.
	*/
	public void unAssignTask(Task task) throws WorkflowException
	{
		String componentId = task.getProcessInstance().getModelId();
		ComponentInst compoInst = null;

		try
		{
			compoInst = admin.getComponentInst(componentId);
		}
		catch (AdminException e)
		{
			throw new WorkflowException("TaskManagerImpl.unassignTask", "workflowEngine.EX_GET_COMPONENT_INST", e);
		}
		
		TodoBackboneAccess todoBBA = new TodoBackboneAccess();
        todoBBA.removeEntriesFromExternal(compoInst.getDomainFatherId(), componentId, getExternalId(task));
	}

	/**
	 * Get the process instance Id referred by the todo with the given todo id
	 */
	/*public String getProcessInstanceIdFromTodoId(String todoId) throws WorkflowException
	{
        TodoBackboneAccess todoBBA = new TodoBackboneAccess();
		TodoDetail todo = todoBBA.getEntry(todoId);

		if (todo==null)
			throw new WorkflowException("TaskManagerImpl.getProcessInstanceIdFromTodoId", "workflowEngine.EX_TODO_NOT_FOUND", "todo id : " + todoId);

		return getProcessId( todo.getExternalId() );
	}*/
	
	public String getProcessInstanceIdFromExternalTodoId(String externalTodoId) throws WorkflowException
	{
		return getProcessId(externalTodoId);
	}

	/**
	 * Get the role name of task referred by the todo with the given todo id
	 */
	/*public String getRoleNameFromTodoId(String todoId) throws WorkflowException
	{
        TodoBackboneAccess todoBBA = new TodoBackboneAccess();
		TodoDetail todo = todoBBA.getEntry(todoId);

		if (todo==null)
			throw new WorkflowException("TaskManagerImpl.getProcessInstanceIdFromTodoId", "workflowEngine.EX_TODO_NOT_FOUND", "todo id : " + todoId);

		return getRoleName( todo.getExternalId() );
	}*/
	
	public String getRoleNameFromExternalTodoId(String externalTodoId) throws WorkflowException
	{
        return getRoleName(externalTodoId);
	}

	/**
	 * Notify user that an action has been done
	 */
	public void notifyUser(Task task, User sender, User user, String text)
	{
		String componentId = task.getProcessInstance().getModelId();
		String userId = user.getUserId();

		NotificationSender notifSender = (NotificationSender) notificationSenders.get(componentId);
		if (notifSender==null)
		{
			notifSender = new NotificationSender(componentId);
			notificationSenders.put(componentId, notifSender);
		}

		try 
		{
			String title = task.getProcessInstance().getTitle(task.getUserRoleName(), "");

			DataRecord data = task.getProcessInstance().getAllDataRecord(task.getUserRoleName(), "");
			text = DataRecordUtil.applySubstitution(text, data, "");

			NotificationMetaData notifMetaData = new NotificationMetaData(NotificationParameters.NORMAL, title, text);
			if (sender != null)
				notifMetaData.setSender(sender.getUserId());
			else
				notifMetaData.setSender(userId);
			notifMetaData.addUserRecipient(userId);
			String link = "/RprocessManager/" + componentId + "/searchResult?Type=ProcessInstance&Id=" + task.getProcessInstance().getInstanceId();
			notifMetaData.setLink(link);
			notifSender.notifyUser(notifMetaData);
		} 
		catch (WorkflowException e)
		{
			SilverTrace.warn("workflowEngine","TaskManagerImpl.notifyUser()", "workflowEngine.EX_ERR_NOTIFY", "user = " + userId, e);
		}
		catch (NotificationManagerException e) 
		{
			SilverTrace.warn("workflowEngine","TaskManagerImpl.notifyUser()", "workflowEngine.EX_ERR_NOTIFY", "user = " + userId, e);
		}
	}

	/**
	 * Build the externalId from a task
	 */
	private String getExternalId(Task task)
	{
		return task.getProcessInstance().getInstanceId()
				+ "##" + task.getUser().getUserId()
				+ "##" + task.getState().getName()
				+ "##" + task.getUserRoleName();
	}

	/**
	 * Extract processId from external Id
	 */
	private String getProcessId(String externalId) throws WorkflowException
	{
		//Separator '#' has been replaced by '_' due to HTML's URL limitation
		StringTokenizer st = new StringTokenizer(externalId, "__");
		
		// The number of token must be : 4
		if (st.countTokens() != 4)
			throw new WorkflowException("TaskManagerImpl.getProcessId", "workflowEngine.EX_ERR_ILLEGAL_EXTERNALID", "external Id : " + externalId);

		return st.nextToken();
	}

	/**
	 * Extract role name from external Id
	 */
	private String getRoleName(String externalId) throws WorkflowException
	{
		//Separator '#' has been replaced by '_' due to HTML's URL limitation
		StringTokenizer st = new StringTokenizer(externalId, "__");
		
		// The number of token must be : 4
		if (st.countTokens() != 4)
			throw new WorkflowException("TaskManagerImpl.getProcessId", "workflowEngine.EX_ERR_ILLEGAL_EXTERNALID", "external Id : " + externalId);

		st.nextToken();
		st.nextToken();
		st.nextToken();

		return st.nextToken();
	}
}