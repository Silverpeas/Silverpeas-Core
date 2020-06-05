/*
 * Copyright (C) 2000 - 2020 Silverpeas
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
 * "https://www.silverpeas.org/legal/floss_exception.html"
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

import org.silverpeas.core.admin.component.model.ComponentInst;
import org.silverpeas.core.admin.service.AdminException;
import org.silverpeas.core.admin.service.AdministrationServiceProvider;
import org.silverpeas.core.admin.user.model.UserDetail;
import org.silverpeas.core.personalorganizer.model.Attendee;
import org.silverpeas.core.personalorganizer.model.TodoDetail;
import org.silverpeas.core.personalorganizer.service.SilverpeasCalendar;
import org.silverpeas.core.util.LocalizationBundle;
import org.silverpeas.core.util.ResourceLocator;
import org.silverpeas.core.util.StringUtil;
import org.silverpeas.core.util.logging.SilverLogger;
import org.silverpeas.core.workflow.api.WorkflowException;
import org.silverpeas.core.workflow.api.instance.Actor;
import org.silverpeas.core.workflow.api.task.Task;
import org.silverpeas.core.workflow.api.user.Replacement;
import org.silverpeas.core.workflow.api.user.User;
import org.silverpeas.core.workflow.engine.notification.UserNotificationBuilder;
import org.silverpeas.core.workflow.engine.user.UserImpl;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * The workflow engine services relate to task management.
 */
@Singleton
public class TaskManagerImpl extends AbstractTaskManager {

  private static final String MULTILANG_BUNDLE =
      "org.silverpeas.workflow.multilang.usernotification";

  @Inject
  private SilverpeasCalendar calendar;

  /**
   * Adds a new task in the user's todos. Returns the external id given by the external task to
   * do system.
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

    if (task.getUser() != null) {
      calendar.removeToDoFromExternal(null, componentId, getExternalId(task));
    } else {
      String role = task.getUserRoleName();
      List<User> usersInRole = task.getProcessInstance().getUsersInRole(role);
      for (User userInRole : usersInRole) {
        TaskImpl taskImpl =
            new TaskImpl(userInRole, role, task.getProcessInstance(), task.getState());
        calendar.removeToDoFromExternal(null, componentId,
            getExternalId(taskImpl, userInRole.getUserId()));
      }
    }
  }

  /**
   * Get the process instance Id referred by the task to do with the given identifier
   */
  @Override
  public String getProcessInstanceIdFromExternalTodoId(String externalTodoId)
      throws WorkflowException {
    return getProcessId(externalTodoId);
  }

  /**
   * Get the role name of task referred by the task to do with the given identifier
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
  public void notifyActor(Task task, User sender, Actor user, String text, boolean linkDisabled)
      throws WorkflowException {
    String componentId = task.getProcessInstance().getModelId();
    final List<String> userIds = new ArrayList<>();
    String role = task.getUserRoleName();
    if (user != null && user.getUser() != null) {
      role = user.getUserRoleName();
      userIds.add(user.getUser().getUserId());
    } else if (StringUtil.isDefined(task.getGroupId())) {
      List<User> usersInGroup = task.getProcessInstance().getUsersInGroup(task.getGroupId());
      userIds.addAll(usersInGroup.stream().map(User::getUserId).collect(Collectors.toList()));
    } else {
      List<User> usersInRole = task.getProcessInstance().getUsersInRole(role);
      userIds.addAll(usersInRole.stream().map(User::getUserId).collect(Collectors.toList()));
    }

    sendNotification(userIds, task, sender, text, linkDisabled, null);

    // notify substitute(s) according to role (excluding potential substitutes who are
    // already regular actors)
    for (String userId : userIds) {
      List<User> substitutes = getSubstitutes(userId, role, componentId)
          .stream()
          .filter(s -> !userIds.contains(s.getUserId())).collect(Collectors.toList());
      for (User substitute : substitutes) {
        sendNotificationToSubstitute(substitute.getUserId(), task, sender, text, linkDisabled,
            userId);
      }
    }
  }

  private void sendNotificationToSubstitute(String userId, Task task, User sender, String text,
      boolean linkDisabled, String incumbentId) {
    LocalizationBundle bundle = ResourceLocator.getLocalizationBundle(MULTILANG_BUNDLE,
        UserDetail.getById(userId).getUserPreferences().getLanguage());
    String incumbent = UserDetail.getById(incumbentId).getDisplayedName();
    String substituteMessage = text + "\n\n" +
        bundle.getStringWithParams("replacement.message.substitute.extra", incumbent);
    sendNotification(Collections.singletonList(userId), task, sender, substituteMessage,
        linkDisabled, incumbentId);
  }

  private void sendNotification(List<String> userIds, Task task, User sender, String text,
      boolean linkDisabled, String incumbentId) {

    new UserNotificationBuilder(userIds, task, sender, text, linkDisabled, incumbentId).build()
        .send();
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
    return getItems(externalId)[0];
  }

  /**
   * Extract role name from external Id
   */
  private String getRoleName(String externalId) throws WorkflowException {
    return getItems(externalId)[3];
  }

  private String[] getItems(String externalId) throws WorkflowException {
    String[] items = StringUtil.split(externalId, "$$");

    // The number of token must be : 4
    if (items.length != 4) {
      throw new WorkflowException("TaskManagerImpl.getItems",
          "workflowEngine.EX_ERR_ILLEGAL_EXTERNALID", "external Id : " + externalId);
    }

    return items;
  }

  private static List<User> getSubstitutes(String userId, String role, String componentInstanceId) {
    User user = new UserImpl(UserDetail.getById(userId));
    return Replacement.getAllOf(user, componentInstanceId)
        .stream()
        .filterCurrentAt(LocalDate.now())
        .filterOnAtLeastOneRole(role)
        .map(Replacement::getSubstitute)
        .collect(Collectors.toList());
  }
}