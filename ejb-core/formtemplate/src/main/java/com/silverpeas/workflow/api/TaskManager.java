package com.silverpeas.workflow.api;

import com.silverpeas.workflow.api.instance.Actor;
import com.silverpeas.workflow.api.instance.ProcessInstance;
import com.silverpeas.workflow.api.model.ProcessModel;
import com.silverpeas.workflow.api.task.Task;
import com.silverpeas.workflow.api.user.User;

/**
 * The workflow engine services relate to task management.
 */
public interface TaskManager {
  /**
   * Adds a new task in the user's todos.
   */
  public void assignTask(Task task) throws WorkflowException;

  /**
   * Removes a task from the user's todos.
   */
  public void unAssignTask(Task task) throws WorkflowException;

  /**
   * Builds a new task (assigned or assignable).
   */
  public Task createTask(Actor actor, ProcessInstance processInstance)
      throws WorkflowException;

  /**
   * Builds new tasks (assigned or assignable).
   */
  public Task[] createTasks(Actor[] actors, ProcessInstance processInstance)
      throws WorkflowException;

  /**
   * Returns the tasks assigned to a user on a processInstance.
   */
  public Task[] getTasks(User user, String roleName,
      ProcessInstance processInstance) throws WorkflowException;

  /**
   * Returns the creation task of a processModel or null if the user is not
   * allowed to create a new instance.
   */
  public Task getCreationTask(User user, String roleName,
      ProcessModel processModel) throws WorkflowException;

  /**
   * Get the process instance Id referred by the todo with the given todo id
   */
  public String getProcessInstanceIdFromExternalTodoId(String externalTodoId)
      throws WorkflowException;

  /**
   * Get the role name of task referred by the todo with the given todo id
   */
  public String getRoleNameFromExternalTodoId(String externalTodoId)
      throws WorkflowException;

  /**
   * Notify user that an action has been done
   */
  public void notifyUser(Task task, User sender, User user, String text);
}
