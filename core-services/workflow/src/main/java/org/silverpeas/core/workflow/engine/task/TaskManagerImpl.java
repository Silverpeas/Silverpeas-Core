/*
 * Copyright (C) 2000 - 2014 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception. You should have recieved a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.silverpeas.core.workflow.engine.task;

import org.silverpeas.core.contribution.content.form.DataRecord;
import org.silverpeas.core.contribution.content.form.DataRecordUtil;
import org.silverpeas.core.workflow.api.WorkflowException;
import org.silverpeas.core.workflow.api.task.Task;
import org.silverpeas.core.workflow.api.user.User;
import org.silverpeas.core.notification.user.client.NotificationManagerException;
import org.silverpeas.core.notification.user.client.NotificationMetaData;
import org.silverpeas.core.notification.user.client.NotificationParameters;
import org.silverpeas.core.notification.user.client.NotificationSender;
import org.silverpeas.core.notification.user.client.UserRecipient;
import org.silverpeas.core.admin.service.AdminException;
import org.silverpeas.core.admin.service.AdministrationServiceProvider;
import org.silverpeas.core.admin.component.model.ComponentInst;
import org.silverpeas.core.calendar.service.SilverpeasCalendar;
import org.silverpeas.core.calendar.model.TodoDetail;
import org.silverpeas.core.calendar.model.Attendee;
import org.silverpeas.core.util.StringUtil;
import org.silverpeas.core.util.logging.SilverLogger;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.StringTokenizer;
import java.util.stream.Collectors;

/**
 * The workflow engine services relate to task management.
 */
@Singleton
public class TaskManagerImpl extends AbstractTaskManager {
  static Hashtable<String, NotificationSender> notificationSenders = new Hashtable<>();

  @Inject
  private SilverpeasCalendar calendar;

  /**
   * Adds a new task in the user's todos. Returns the external id given by the external todo system.
   */
  @Override
  public void assignTask(Task task, User delegator) throws WorkflowException {
    String componentId = task.getProcessInstance().getModelId();
    ComponentInst compoInst;

    try {
      compoInst = AdministrationServiceProvider.getAdminService().getComponentInst(componentId);
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
      SilverLogger.getLogger(this).error("Undefined delegator for new task {0}", todo.getName());
    }

    List<Attendee> attendees = new ArrayList<>();
    if (task.getUser() != null) {
      // add todo to specified user
      attendees.add(new Attendee(task.getUser().getUserId()));
      todo.setAttendees(attendees);
      todo.setExternalId(getExternalId(task));
      calendar.addToDo(todo);
    } else {
      List<User> users;
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
        calendar.addToDo(todo);
      }
    }
  }

  /**
   * Removes a task.
   */
  @Override
  public void unAssignTask(Task task) throws WorkflowException {
    String componentId = task.getProcessInstance().getModelId();
    ComponentInst compoInst;

    try {
      compoInst = AdministrationServiceProvider.getAdminService().getComponentInst(componentId);
    } catch (AdminException e) {
      throw new WorkflowException("TaskManagerImpl.unassignTask",
          "workflowEngine.EX_GET_COMPONENT_INST", e);
    }

    if (task.getUser() != null) {
      calendar.removeToDoFromExternal(compoInst.getDomainFatherId(), componentId,
          getExternalId(task));
    } else {
      String role = task.getUserRoleName();
      List<User> usersInRole = task.getProcessInstance().getUsersInRole(role);
      for (User userInRole : usersInRole) {
        TaskImpl taskImpl =
            new TaskImpl(userInRole, role, task.getProcessInstance(), task.getState());
        calendar.removeToDoFromExternal(compoInst.getDomainFatherId(), componentId,
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
    final List<String> userIds = new ArrayList<>();
    if (user != null) {
      userIds.add(user.getUserId());
    } else if (StringUtil.isDefined(task.getGroupId())) {
      List<User> usersInGroup = task.getProcessInstance().getUsersInGroup(task.getGroupId());
      userIds.addAll(usersInGroup.stream().map(User::getUserId).collect(Collectors.toList()));
    } else {
      String role = task.getUserRoleName();
      List<User> usersInRole = task.getProcessInstance().getUsersInRole(role);
      userIds.addAll(usersInRole.stream().map(User::getUserId).collect(Collectors.toList()));
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
      } catch (WorkflowException | NotificationManagerException e) {
        SilverLogger.getLogger(this).error(e.getMessage(), e);
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