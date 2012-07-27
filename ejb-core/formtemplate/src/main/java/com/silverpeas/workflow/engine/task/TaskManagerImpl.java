/**
 * Copyright (C) 2000 - 2012 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have received a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.silverpeas.workflow.engine.task;

import com.silverpeas.form.DataRecord;
import com.silverpeas.form.DataRecordUtil;
import com.silverpeas.util.StringUtil;
import com.silverpeas.workflow.api.WorkflowException;
import com.silverpeas.workflow.api.task.Task;
import com.silverpeas.workflow.api.user.User;
import com.stratelia.silverpeas.notificationManager.NotificationManagerException;
import com.stratelia.silverpeas.notificationManager.NotificationMetaData;
import com.stratelia.silverpeas.notificationManager.NotificationParameters;
import com.stratelia.silverpeas.notificationManager.NotificationSender;
import com.stratelia.silverpeas.notificationManager.UserRecipient;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.beans.admin.AdminException;
import com.stratelia.webactiv.beans.admin.AdminReference;
import com.stratelia.webactiv.beans.admin.ComponentInst;
import com.stratelia.webactiv.calendar.backbone.TodoBackboneAccess;
import com.stratelia.webactiv.calendar.backbone.TodoDetail;
import com.stratelia.webactiv.calendar.model.Attendee;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.StringTokenizer;
import java.util.Vector;

/**
 * The workflow engine services relate to task management.
 */
public class TaskManagerImpl extends AbstractTaskManager {
  static Hashtable<String, NotificationSender> notificationSenders =
      new Hashtable<String, NotificationSender>();

  /**
   * Adds a new task in the user's todos. Returns the external id given by the external todo system.
   */
  @Override
  public void assignTask(Task task, User delegator) throws WorkflowException {
    String componentId = task.getProcessInstance().getModelId();
    ComponentInst compoInst = null;

    try {
      compoInst = AdminReference.getAdminService().getComponentInst(componentId);
    } catch (AdminException e) {
      throw new WorkflowException("TaskManagerImpl.assignTask",
          "workflowEngine.EX_GET_COMPONENT_INST", e);
    }

    TodoDetail todo = new TodoDetail();
    todo.setId(task.getProcessInstance().getInstanceId());
    todo.setSpaceId(compoInst.getDomainFatherId());
    todo.setComponentId(componentId);
    todo.setName("activite : "
        + task.getState().getLabel(task.getUserRoleName(), "fr"));
    if (delegator != null) {
      todo.setDelegatorId(delegator.getUserId());
    } else {
      SilverTrace.error("workflowEngine", "TaskManagerImpl.assignTask", "root.MSG_GEN_PARAM_VALUE",
          "Undefined delegator for new task : " + todo.getName());
    }

    TodoBackboneAccess todoBBA = new TodoBackboneAccess();
    Vector<Attendee> attendees = new Vector<Attendee>();
    if (task.getUser() != null) {
      // add todo to specified user
      attendees.add(new Attendee(task.getUser().getUserId()));
      todo.setAttendees(attendees);
      todo.setExternalId(getExternalId(task));
      todoBBA.addEntry(todo);
    } else {
      List<User> users = null;
      if (StringUtil.isDefined(task.getGroupId())) {
        // get users according to group
        users = task.getProcessInstance().getUsersInGroup(task.getGroupId());
      } else {
        // get users according to role
        users = task.getProcessInstance().getUsersInRole(task.getUserRoleName());
      }
      for (User user : users) {
        attendees.clear();
        attendees.add(new Attendee(user.getUserId()));
        todo.setAttendees(attendees);
        todo.setExternalId(getExternalId(task, user.getUserId()));
        todoBBA.addEntry(todo);
      }
    }
  }

  /**
   * Removes a task.
   */
  @Override
  public void unAssignTask(Task task) throws WorkflowException {
    String componentId = task.getProcessInstance().getModelId();
    ComponentInst compoInst = null;

    try {
      compoInst = AdminReference.getAdminService().getComponentInst(componentId);
    } catch (AdminException e) {
      throw new WorkflowException("TaskManagerImpl.unassignTask",
          "workflowEngine.EX_GET_COMPONENT_INST", e);
    }

    TodoBackboneAccess todoBBA = new TodoBackboneAccess();

    if (task.getUser() != null) {
      todoBBA.removeEntriesFromExternal(compoInst.getDomainFatherId(), componentId,
          getExternalId(task));
    } else {
      String role = task.getUserRoleName();
      List<User> usersInRole = task.getProcessInstance().getUsersInRole(role);
      for (User userInRole : usersInRole) {
        TaskImpl taskImpl =
            new TaskImpl(userInRole, role, task.getProcessInstance(), task.getState());
        todoBBA.removeEntriesFromExternal(compoInst.getDomainFatherId(), componentId,
            getExternalId(taskImpl, userInRole.getUserId()));
      }
    }
  }

  /**
   * Get the process instance Id referred by the todo with the given todo id
   */
  @Override
  public String getProcessInstanceIdFromExternalTodoId(String externalTodoId)
      throws WorkflowException {
    return getProcessId(externalTodoId);
  }

  /**
   * Get the role name of task referred by the todo with the given todo id
   */
  @Override
  public String getRoleNameFromExternalTodoId(String externalTodoId)
      throws WorkflowException {
    return getRoleName(externalTodoId);
  }

  /**
   * Notify user that an action has been done
   * @throws WorkflowException
   */
  @Override
  public void notifyActor(Task task, User sender, User user, String text) throws WorkflowException {
    String componentId = task.getProcessInstance().getModelId();
    List<String> userIds = new ArrayList<String>();
    if (user != null) {
      userIds.add(user.getUserId());
    } else if (StringUtil.isDefined(task.getGroupId())) {
      List<User> usersInGroup = task.getProcessInstance().getUsersInGroup(task.getGroupId());
      for (User userInGroup : usersInGroup) {
        userIds.add(userInGroup.getUserId());
      }
    } else {
      String role = task.getUserRoleName();
      List<User> usersInRole = task.getProcessInstance().getUsersInRole(role);
      for (User userInRole : usersInRole) {
        userIds.add(userInRole.getUserId());
      }
    }

    NotificationSender notifSender = notificationSenders.get(componentId);
    if (notifSender == null) {
      notifSender = new NotificationSender(componentId);
      notificationSenders.put(componentId, notifSender);
    }

    for (String userId : userIds) {
      try {
        String title = task.getProcessInstance().getTitle(task.getUserRoleName(),
            "");

        DataRecord data = task.getProcessInstance().getAllDataRecord(task.getUserRoleName(), "");
        text = DataRecordUtil.applySubstitution(text, data, "");

        NotificationMetaData notifMetaData = new NotificationMetaData(
            NotificationParameters.NORMAL, title, text);
        if (sender != null) {
          notifMetaData.setSender(sender.getUserId());
        } else {
          notifMetaData.setSender(userId);
        }
        notifMetaData.addUserRecipient(new UserRecipient(userId));
        String link = "/RprocessManager/" + componentId
            + "/searchResult?Type=ProcessInstance&Id="
            + task.getProcessInstance().getInstanceId() + "&role=" + task.getUserRoleName();
        notifMetaData.setLink(link);
        notifSender.notifyUser(notifMetaData);
      } catch (WorkflowException e) {
        SilverTrace.warn("workflowEngine", "TaskManagerImpl.notifyUser()",
            "workflowEngine.EX_ERR_NOTIFY", "user = " + userId, e);
      } catch (NotificationManagerException e) {
        SilverTrace.warn("workflowEngine", "TaskManagerImpl.notifyUser()",
            "workflowEngine.EX_ERR_NOTIFY", "user = " + userId, e);
      }
    }
  }

  /**
   * Build the externalId from a task
   */
  private String getExternalId(Task task) {
    return getExternalId(task, task.getUser().getUserId());
  }

  private String getExternalId(Task task, String userId) {
    return task.getProcessInstance().getInstanceId() + "##"
        + userId + "##" + task.getState().getName() + "##"
        + task.getUserRoleName();
  }

  /**
   * Extract processId from external Id
   */
  private String getProcessId(String externalId) throws WorkflowException {
    // Separator '#' has been replaced by '_' due to HTML's URL limitation
    StringTokenizer st = new StringTokenizer(externalId, "__");

    // The number of token must be : 4
    if (st.countTokens() != 4)
      throw new WorkflowException("TaskManagerImpl.getProcessId",
          "workflowEngine.EX_ERR_ILLEGAL_EXTERNALID", "external Id : "
          + externalId);

    return st.nextToken();
  }

  /**
   * Extract role name from external Id
   */
  private String getRoleName(String externalId) throws WorkflowException {
    // Separator '#' has been replaced by '_' due to HTML's URL limitation
    StringTokenizer st = new StringTokenizer(externalId, "__");

    // The number of token must be : 4
    if (st.countTokens() != 4)
      throw new WorkflowException("TaskManagerImpl.getProcessId",
          "workflowEngine.EX_ERR_ILLEGAL_EXTERNALID", "external Id : "
          + externalId);

    st.nextToken();
    st.nextToken();
    st.nextToken();

    return st.nextToken();
  }
}