/**
 * Copyright (C) 2000 - 2011 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU Affero General Public License as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of the GPL, you may
 * redistribute this Program in connection with Free/Libre Open Source Software ("FLOSS")
 * applications as described in Silverpeas's FLOSS exception. You should have received a copy of
 * the text describing the FLOSS exception, and it is also available here:
 * "http://repository.silverpeas.com/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See
 * the GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License along with this
 * program. If not, see <http://www.gnu.org/licenses/>.
 */
package com.stratelia.webactiv.calendar.backbone;

import com.stratelia.silverpeas.notificationManager.NotificationMetaData;
import com.stratelia.silverpeas.notificationManager.NotificationParameters;
import com.stratelia.silverpeas.notificationManager.NotificationSender;
import com.stratelia.silverpeas.notificationManager.UserRecipient;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.calendar.control.CalendarBm;
import com.stratelia.webactiv.calendar.control.CalendarBmHome;
import com.stratelia.webactiv.calendar.control.CalendarRuntimeException;
import com.stratelia.webactiv.calendar.model.Attendee;
import com.stratelia.webactiv.calendar.model.ToDoHeader;
import com.stratelia.webactiv.util.EJBUtilitaire;
import com.stratelia.webactiv.util.JNDINames;
import com.stratelia.webactiv.util.exception.SilverpeasException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Vector;

public class TodoBackboneAccess {

  private static CalendarBm calendarBm = null;

  private static TodoDetail todoHeaderToDetail(final ToDoHeader header) {
    TodoDetail detail = new TodoDetail();
    detail.setName(header.getName());
    detail.setId(header.getId());
    detail.setDescription(header.getDescription());
    detail.setDelegatorId(header.getDelegatorId());
    // detail.setPriority(header.getPriority());
    detail.setStartDate(header.getStartDate());
    detail.setEndDate(header.getEndDate());
    detail.setDuration(header.getDuration());
    detail.setPercentCompleted(header.getPercentCompleted());
    detail.setComponentId(header.getComponentId());
    detail.setSpaceId(header.getSpaceId());
    detail.setExternalId(header.getExternalId());
    return detail;
  }

  private static ToDoHeader todoDetailToHeader(final TodoDetail detail) {
    ToDoHeader head = new ToDoHeader();
    head.setName(detail.getName());
    head.setId(detail.getId());
    head.setDescription(detail.getDescription());
    head.setDelegatorId(detail.getDelegatorId());
    head.setStartDate(detail.getStartDate());
    head.setEndDate(detail.getEndDate());
    head.setDuration((int) detail.getDuration());
    head.setPercentCompleted(detail.getPercentCompleted());
    head.setComponentId(detail.getComponentId());
    head.setSpaceId(detail.getSpaceId());
    head.setExternalId(detail.getExternalId());
    return head;
  }

  public TodoBackboneAccess() {
    SilverTrace.info("calendar", "TodoBackboneAcess.TodoBackboneAccess()",
            "calendar.MSG_NEW_BB");
  }

  public String addEntry(TodoDetail todo) {
    return addEntry(todo, false, "", "");
  }

  public String addEntry(TodoDetail todo, boolean notifyAttendees, String txtTitle,
          String txtMessage) {
    SilverTrace.info("calendar", "TodoBackboneAcess.addEntry()", "root.MSG_GEN_ENTER_METHOD");
    NotificationSender notifSender = new NotificationSender(todo.getComponentId());
    try {
      ToDoHeader header = todoDetailToHeader(todo);
      SilverTrace.info("calendar", "TodoBackboneAcess.addEntry()",
              "root.MSG_GEN_ENTER_METHOD", "apres header");
      String id = getCalendarBm().addToDo(header);
      SilverTrace.info("calendar", "TodoBackboneAcess.addEntry()",
              "root.MSG_GEN_ENTER_METHOD", "id=" + id);

      if (todo.getAttendees() != null) {
        Collection<UserRecipient> selectedUsers = new ArrayList<UserRecipient>();
        for (Attendee attendee : todo.getAttendees()) {
          getCalendarBm().addToDoAttendee(id, attendee);
          if (notifyAttendees && (!todo.getDelegatorId().equals(attendee.getUserId()))) {
            selectedUsers.add(new UserRecipient(attendee.getUserId()));
          }
        }
        if (!selectedUsers.isEmpty()) {
          NotificationMetaData notifMetaData = new NotificationMetaData(
                  NotificationParameters.NORMAL, txtTitle, txtMessage);
          notifMetaData.setSender(todo.getDelegatorId());
          notifMetaData.setUserRecipients(selectedUsers);
          notifSender.notifyUser(notifMetaData);
        }
      }
      return id;
    } catch (Exception e) {
      SilverTrace.error("calendar", "TodoBackboneAcess.addEntry()",
              "calendar.MSG_ADD_ENTRY_FAILED", "value return id= null", e);
      return null;
    }
  }

  public void updateEntry(TodoDetail todo) {
    SilverTrace.info("calendar", "TodoBackboneAcess.updateEntry(TodoDetail todo)",
            "root.MSG_GEN_ENTER_METHOD");
    try {
      ToDoHeader header = todoDetailToHeader(todo);
      getCalendarBm().updateToDo(header);
      if (todo.getAttendees() != null) {
        String[] userIds = new String[todo.getAttendees().size()];
        int posit = 0;
        for (Attendee attendee : todo.getAttendees()) {
          userIds[posit++] = attendee.getUserId();
        }
        getCalendarBm().setToDoAttendees(header.getId(), userIds);
      }
    } catch (Exception e) {
      SilverTrace.error("calendar", "TodoBackboneAcess.addEntry(TodoDetail todo)",
              "calendar.MSG_UPDATE_ENTRY_FAILED", "id=" + todo.getId(), e);
    }
  }

  public TodoDetail getEntry(String id) {
    SilverTrace.info("calendar", "TodoBackboneAcess.getEntry(String id)",
            "root.MSG_GEN_ENTER_METHOD", "id=" + id);
    try {
      ToDoHeader header = getCalendarBm().getToDoHeader(id);
      TodoDetail detail = todoHeaderToDetail(header);

      Collection<Attendee> list = getCalendarBm().getToDoAttendees(id);
      Vector<Attendee> vector = new Vector<Attendee>(list);
      detail.setAttendees(vector);
      return detail;
    } catch (Exception e) {
      SilverTrace.error("calendar", "TodoBackboneAcess.getEntry(TodoDetail todo)",
              "calendar.MSG_CANT_GET", "return null", e);
      return null;
    }
  }

  public Vector<TodoDetail> getEntriesFromExternal(String spaceId, String componentId,
          String externalId) {
    SilverTrace.info("calendar",
            "TodoBackboneAcess.getEntriesFromExternal(String spaceId, String componentId, String externalId)",
            "root.MSG_GEN_ENTER_METHOD", "spaceId=" + spaceId
            + ", componentId=" + componentId + ", externalId=" + externalId);
    try {
      Collection<ToDoHeader> headers = getCalendarBm().getExternalTodos(spaceId,
              componentId, externalId);
      Vector<TodoDetail> result = new Vector<TodoDetail>();
      for (ToDoHeader header : headers) {
        TodoDetail detail = todoHeaderToDetail(header);

        Collection<Attendee> list = getCalendarBm().getToDoAttendees(detail.getId());
        detail.setAttendees(new Vector<Attendee>(list));
        result.add(detail);
      }
      return result;
    } catch (Exception e) {
      SilverTrace.error("calendar",
              "TodoBackboneAcess.getEntriesFromExternal(String spaceId, String componentId, String externalId)",
              "calendar.MSG_CANT_GET", "return null", e);
      return null;
    }

  }

  public void removeEntry(String id) {
    SilverTrace.info("calendar", "TodoBackboneAcess.removeEntry(String id)",
            "root.MSG_GEN_ENTER_METHOD", "id=" + id);
    try {
      getCalendarBm().removeToDo(id);
    } catch (Exception e) {
      SilverTrace.error("calendar", "TodoBackboneAcess.removeEntry(String id)",
              "calendar.MSG_CANT_REMOVE", "", e);
    }
  }

  public void removeEntriesFromExternal(String spaceId, String componentId, String externalId) {
    SilverTrace.info("calendar",
            "TodoBackboneAcess.removeEntriesFromExternal(String spaceId, String componentId, String externalId)",
            "root.MSG_GEN_ENTER_METHOD", "spaceId=" + spaceId
            + ", componentId=" + componentId + ", externalId=" + externalId);
    try {
      Collection<ToDoHeader> headers = getCalendarBm().getExternalTodos(spaceId,
              componentId, externalId);
      for (ToDoHeader header : headers) {
        getCalendarBm().removeToDo(header.getId());
      }
    } catch (Exception e) {
      SilverTrace.error("calendar",
              "TodoBackboneAcess.removeEntriesFromExternal(String spaceId, String componentId, String externalId)",
              "calendar.MSG_CANT_REMOVE", "return null", e);
    }
  }

  public void removeAttendeeToEntryFromExternal(String componentId, String externalId, String userId) {
    SilverTrace.info("calendar",
            "TodoBackboneAcess.removeAttendeeToEntryFromExternal(String componentId, String externalId, String userId)",
            "root.MSG_GEN_ENTER_METHOD", "componentId=" + componentId
            + ", externalId=" + externalId + ", userId = " + userId);
    try {
      Attendee attendee = new Attendee();
      attendee.setUserId(userId);

      Collection<ToDoHeader> headers = getCalendarBm().getExternalTodos("useless",
              componentId, externalId);
      for (ToDoHeader header : headers) {
        if (header != null) {
          getCalendarBm().removeToDoAttendee(header.getId(), attendee);
        }
      }
    } catch (Exception e) {
      SilverTrace.error("calendar",
              "TodoBackboneAcess.removeAttendeeToEntryFromExternal(String componentId, String externalId)",
              "calendar.MSG_CANT_REMOVE", "return null", e);
    }
  }

  public void removeEntriesByInstanceId(String instanceId) {
    SilverTrace.info("calendar", "TodoBackboneAcess.removeEntriesByInstanceId(String instanceId)",
            "root.MSG_GEN_ENTER_METHOD", "instanceId=" + instanceId);
    try {
      getCalendarBm().removeToDoByInstanceId(instanceId);
    } catch (Exception e) {
      SilverTrace.error("calendar", "TodoBackboneAcess.removeEntriesByInstanceId(String instanceId)",
              "calendar.MSG_CANT_REMOVE", "return null", e);
    }
  }

  private synchronized CalendarBm getCalendarBm() {
    if (calendarBm == null) {
      try {
        calendarBm = EJBUtilitaire.getEJBObjectRef(JNDINames.CALENDARBM_EJBHOME,
                CalendarBmHome.class).create();
      } catch (Exception e) {
        throw new CalendarRuntimeException("TodoBackboneAcessB.getCalendarBm()",
                SilverpeasException.ERROR, "root.EX_CANT_GET_REMOTE_OBJECT", e);
      }
    }
    return calendarBm;
  }
}
