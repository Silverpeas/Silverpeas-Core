/**
 * Copyright (C) 2000 - 2012 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU Affero General Public License as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of the GPL, you may
 * redistribute this Program in connection with Free/Libre Open Source Software ("FLOSS")
 * applications as described in Silverpeas's FLOSS exception. You should have received a copy of the
 * text describing the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License along with this program.
 * If not, see <http://www.gnu.org/licenses/>.
 */
package com.stratelia.webactiv.todo.control;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.stratelia.silverpeas.notificationManager.NotificationMetaData;
import com.stratelia.silverpeas.notificationManager.NotificationParameters;
import com.stratelia.silverpeas.notificationManager.NotificationSender;
import com.stratelia.silverpeas.notificationManager.UserRecipient;
import com.stratelia.silverpeas.peasCore.AbstractComponentSessionController;
import com.stratelia.silverpeas.peasCore.ComponentContext;
import com.stratelia.silverpeas.peasCore.MainSessionController;
import com.stratelia.silverpeas.peasCore.URLManager;
import com.stratelia.silverpeas.selection.Selection;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.silverpeas.util.PairObject;
import com.stratelia.webactiv.beans.admin.ComponentInstLight;
import com.stratelia.webactiv.beans.admin.SpaceInstLight;
import com.stratelia.webactiv.beans.admin.UserDetail;
import com.stratelia.webactiv.calendar.control.SilverpeasCalendar;
import com.stratelia.webactiv.calendar.model.Attendee;
import com.stratelia.webactiv.calendar.model.ToDoHeader;
import com.stratelia.webactiv.util.EJBUtilitaire;
import com.stratelia.webactiv.util.JNDINames;
import com.stratelia.webactiv.util.ResourceLocator;
import com.stratelia.webactiv.util.exception.SilverpeasException;
import com.stratelia.webactiv.util.exception.UtilException;

/**
 * Class declaration
 *
 * @author
 */
public class ToDoSessionController extends AbstractComponentSessionController {

  public final static int PARTICIPANT_TODO_VIEW = 1;
  public final static int ORGANIZER_TODO_VIEW = 2;
  public final static int CLOSED_TODO_VIEW = 3;
  private int viewType = PARTICIPANT_TODO_VIEW;
  private SilverpeasCalendar calendarBm;
  private ResourceLocator settings;
  private ToDoHeader currentToDoHeader = null;
  private Collection<Attendee> currentAttendees = null;
  private NotificationSender notifSender = null;
  private Map<String, ComponentInstLight> componentsMap = new HashMap<String, ComponentInstLight>();
  private Map<String, SpaceInstLight> spacesMap = new HashMap<String, SpaceInstLight>();

  /**
   * Constructor declaration
   *
   * @see
   */
  public ToDoSessionController(MainSessionController mainSessionCtrl, ComponentContext context) {
    super(mainSessionCtrl, context, "org.silverpeas.todo.multilang.todo");
    setComponentRootName(URLManager.CMP_TODO);
    try {
      calendarBm = EJBUtilitaire.getEJBObjectRef(JNDINames.CALENDARBM_EJBHOME,
          SilverpeasCalendar.class);
    } catch (Exception e) {
      throw new UtilException("ToDoSessionControl.ToDoSessionControl()", SilverpeasException.ERROR,
          "root.EX_CANT_GET_REMOTE_OBJECT", e);
    }
  }

  protected String getComponentInstName() {
    return URLManager.CMP_TODO;
  }

  /**
   * methods for ToDo
   */
  public Collection<ToDoHeader> getToDos() throws TodoException {
    if (getViewType() == PARTICIPANT_TODO_VIEW) {
      return getNotCompletedToDos();
    }
    if (getViewType() == ORGANIZER_TODO_VIEW) {
      return getOrganizerToDos();
    }
    if (getViewType() == CLOSED_TODO_VIEW) {
      return getClosedToDos();
    }
    return null;
  }

  /**
   * Method declaration
   *
   * @return
   * @throws TodoException
   * @see
   */
  public Collection<ToDoHeader> getNotCompletedToDos() throws TodoException {
    SilverTrace.info("todo", "ToDoSessionController.getNotCompletedToDos()",
        "root.MSG_GEN_ENTER_METHOD");
    return calendarBm.getNotCompletedToDosForUser(getUserId());
  }

  /**
   * Method declaration
   *
   * @return
   * @throws TodoException
   * @see
   */
  public Collection<ToDoHeader> getOrganizerToDos() throws TodoException {
    SilverTrace.info("todo", "ToDoSessionController.getOrganizerToDos()",
        "root.MSG_GEN_ENTER_METHOD");
    return calendarBm.getOrganizerToDos(getUserId());

  }

  /**
   * Method declaration
   *
   * @return
   * @throws TodoException
   * @see
   */
  public Collection<ToDoHeader> getClosedToDos() throws TodoException {
    SilverTrace.info("todo", "ToDoSessionController.getClosedToDos()", "root.MSG_GEN_ENTER_METHOD");
    return calendarBm.getClosedToDos(getUserId());
  }

  /**
   * Method declaration
   *
   * @param todoId
   * @return
   * @throws TodoException
   * @see
   */
  public ToDoHeader getToDoHeader(String todoId) throws TodoException {
    SilverTrace.info("todo", "ToDoSessionController.getToDoHeader()", "root.MSG_GEN_ENTER_METHOD");
    ToDoHeader result = calendarBm.getToDoHeader(todoId);
    SilverTrace.info("todo", "ToDoSessionController.getToDoHeader()", "root.MSG_GEN_EXIT_METHOD");
    return result;
  }

  /**
   * Method declaration
   *
   * @param id
   * @param name
   * @param description
   * @param priority
   * @param classification
   * @param startDay
   * @param startHour
   * @param endDay
   * @param endHour
   * @param percent
   * @throws TodoException
   * @see
   */
  public void updateToDo(String id, String name, String description,
      String priority, String classification, Date startDay, String startHour,
      Date endDay, String endHour, String percent) throws TodoException {
    SilverTrace.info("todo", "ToDoSessionController.updateToDo()", "root.MSG_GEN_ENTER_METHOD");
    ToDoHeader todo = getToDoHeader(id);
    todo.setName(name);
    todo.setDescription(description);
    try {
      todo.getPriority().setValue(Integer.parseInt(priority));
    } catch (Exception e) {
      SilverTrace.warn("todo", "ToDoSessionController.updateToDo()",
          "todo.MSG_CANT_SET_TODO_PRIORITY");
    }
    try {
      todo.setPercentCompleted(Integer.parseInt(percent));
    } catch (Exception e) {
      SilverTrace.warn("todo", "ToDoSessionController.updateToDo()",
          "todo.MSG_CANT_SET_TODO_PERCENTCOMPLETED");
    }
    try {
      todo.getClassification().setString(classification);
      todo.setStartDate(startDay);
      todo.setStartHour(startHour);
      todo.setEndDate(endDay);
      todo.setEndHour(endHour);
      calendarBm.updateToDo(todo);
      SilverTrace.info("todo", "ToDoSessionController.updateToDo()",
          "root.MSG_GEN_EXIT_METHOD");
    } catch (ParseException e) {
      throw new TodoException("ToDoSessionController.updateToDo()",
          SilverpeasException.ERROR, "todo.MSG_CANT_UPDATE_TODO_DETAIL", e);
    }
  }

  /**
   * Method declaration
   *
   * @param id
   * @param percent
   * @throws TodoException
   * @see
   */
  public void setToDoPercentCompleted(String id, String percent) throws TodoException {
    SilverTrace.info("todo", "ToDoSessionController.setToDoPercentCompleted()",
        "root.MSG_GEN_ENTER_METHOD");
    ToDoHeader todo = getToDoHeader(id);
    try {
      todo.setPercentCompleted(Integer.parseInt(percent));
    } catch (Exception e) {
      SilverTrace.warn("todo", "ToDoSessionController.setToDoPercentCompleted()",
          "todo.MSG_CANT_SET_TODO_PERCENTCOMPLETED");
    }
    calendarBm.updateToDo(todo);
    SilverTrace.info("todo", "ToDoSessionController.setToDoPercentCompleted()",
        "root.MSG_GEN_EXIT_METHOD");
  }

  /**
   * Method declaration
   *
   * @param id
   * @param title
   * @param text
   * @see
   */
  protected void notifyAttendees(String id, String title, String text) {
    try {
      Collection<Attendee> attendees = getToDoAttendees(id);
      NotificationMetaData notifMetaData = new NotificationMetaData(
          NotificationParameters.NORMAL, title, text);
      notifMetaData.setSender(getUserId());
      notifMetaData.setSource(getString("todo"));
      for (Attendee attendee : attendees) {
        notifMetaData.addUserRecipient(new UserRecipient(attendee.getUserId()));
      }
      getNotificationSender().notifyUser(notifMetaData);
    } catch (Exception e) {
      SilverTrace.error("todo", "ToDoSessionController.notifyAttendees()",
          "todo.MSG_CANT_SEND_MAIL");
    }
  }

  /**
   * Method declaration
   *
   * @param id
   * @throws TodoException
   * @see
   */
  public void closeToDo(String id) throws TodoException {
    SilverTrace.info("todo", "ToDoSessionController.closeToDo()", "root.MSG_GEN_ENTER_METHOD");
    ToDoHeader todo = getToDoHeader(id);
    todo.setCompletedDate(new java.util.Date());
    calendarBm.updateToDo(todo);
    notifyAttendees(id, getString("closingTodo") +" '" + todo.getName() + "'",
        getString("todoCalled") +" '" + todo.getName() + "' "+getString("todoClosed")+"\n");
    SilverTrace.info("todo", "ToDoSessionController.closeToDo()", "root.MSG_GEN_EXIT_METHOD");
  }

  /**
   * Method declaration
   *
   * @param id
   * @throws TodoException
   * @see
   */
  public void reopenToDo(String id) throws TodoException {
    SilverTrace.info("todo", "ToDoSessionController.reopenToDo()", "root.MSG_GEN_ENTER_METHOD");
    ToDoHeader todo = getToDoHeader(id);
    todo.setCompletedDate(null);
    calendarBm.updateToDo(todo);
    SilverTrace.info("todo", "ToDoSessionController.reopenToDo()", "root.MSG_GEN_EXIT_METHOD");
  }

  /**
   * Method declaration
   *
   * @param name
   * @param description
   * @param priority
   * @param classification
   * @param startDay
   * @param startHour
   * @param endDay
   * @param endHour
   * @param percent
   * @return
   * @throws TodoException
   * @see
   */
  public String addToDo(String name, String description, String priority,
      String classification, Date startDay, String startHour, Date endDay,
      String endHour, String percent) throws TodoException {

    SilverTrace.info("todo", "ToDoSessionController.addToDo()",
        "root.MSG_GEN_ENTER_METHOD");
    ToDoHeader todo = new ToDoHeader(name, getUserId());

    todo.setDescription(description);
    try {
      todo.getPriority().setValue(Integer.parseInt(priority));
    } catch (Exception e) {
      SilverTrace.warn("todo", "ToDoSessionController.addToDo()",
          "todo.MSG_CANT_SET_TODO_PRIORITY");
    }
    try {
      todo.setPercentCompleted(Integer.parseInt(percent));
    } catch (Exception e) {
      SilverTrace.warn("todo", "ToDoSessionController.addToDo()",
          "todo.MSG_CANT_SET_TODO_PERCENTCOMPLETED");
    }
    try {
      todo.getClassification().setString(classification);
      todo.setStartDate(startDay);
      todo.setStartHour(startHour);
      todo.setEndDate(endDay);
      todo.setEndHour(endHour);
      String result = calendarBm.addToDo(todo);
      SilverTrace.info("todo", "ToDoSessionController.addToDo()", "root.MSG_GEN_EXIT_METHOD");
      return result;
    } catch (Exception e) {
      throw new TodoException("ToDoSessionController.addToDo()",
          SilverpeasException.ERROR, "todo.MSG_CANT_ADD_TODO", e);
    }

  }

  /**
   * Method declaration
   *
   * @param id
   * @throws TodoException
   * @see
   */
  public void removeToDo(String id) throws TodoException {
    SilverTrace.info("todo", "ToDoSessionController.removeToDo()",
        "root.MSG_GEN_ENTER_METHOD");
    try {
      calendarBm.removeToDo(id);
      SilverTrace.info("todo", "ToDoSessionController.removeToDo()",
          "root.MSG_GEN_EXIT_METHOD");
    } catch (Exception e) {
      throw new TodoException("ToDoSessionController.removeToDo()",
          SilverpeasException.ERROR, "todo.MSG_CANT_REMOVE_TODO", e);
    }
  }

  /**
   * Method declaration
   *
   * @throws TodoException
   * @see
   */
  public void indexAll() throws TodoException {
    SilverTrace.info("todo", "ToDoSessionController.indexAll()",
        "root.MSG_GEN_ENTER_METHOD");
    try {
      calendarBm.indexAllTodo();
      SilverTrace.info("todo", "ToDoSessionController.indexAll()",
          "root.MSG_GEN_EXIT_METHOD");
    } catch (Exception e) {
      throw new TodoException("ToDoSessionController.indexAll()",
          SilverpeasException.ERROR, "todo.MSG_CANT_INDEX_TODOS", e);
    }
  }

  /**
   * methods for attendees
   */
  public Collection<Attendee> getToDoAttendees(String todoId) throws TodoException {
    SilverTrace.info("todo", "ToDoSessionController.getToDoAttendees()",
        "root.MSG_GEN_ENTER_METHOD");
    try {
      return calendarBm.getToDoAttendees(todoId);
    } catch (Exception e) {
      throw new TodoException("ToDoSessionController.getToDoAttendees()",
          SilverpeasException.ERROR, "todo.MSG_CANT_GET_TODO_ATTENDEES", e);
    }
  }

  /**
   * Method declaration
   *
   * @param todoId
   * @param userIds
   * @throws TodoException
   * @see
   */
  public void setToDoAttendees(String todoId, String[] userIds)
      throws TodoException {
    SilverTrace.info("todo", "ToDoSessionController.setToDoAttendees()",
        "root.MSG_GEN_ENTER_METHOD");
    try {
      calendarBm.setToDoAttendees(todoId, userIds);
      SilverTrace.info("todo", "ToDoSessionController.setToDoAttendees()",
          "root.MSG_GEN_EXIT_METHOD");
    } catch (Exception e) {
      throw new TodoException("ToDoSessionController.setToDoAttendees()",
          SilverpeasException.ERROR, "todo.MSG_CANT_SET_TODO_ATTENDEES", e);
    }
  }

  /**
   * Method declaration
   *
   * @return
   * @see
   */
  public UserDetail[] getUserList() {
    return getOrganisationController().getAllUsers();
  }

  /**
   * Method declaration
   *
   * @return
   * @see
   */
  public NotificationSender getNotificationSender() {
    if (notifSender == null) {
      notifSender = new NotificationSender(null);
    }
    return notifSender;
  }

  /**
   * Method declaration
   *
   * @return
   * @see
   */
  public ResourceLocator getSettings() {
    if (settings == null) {
      settings = new ResourceLocator(
          "com.stratelia.webactiv.todo.settings.todoSettings", "");
    }
    return settings;
  }

  /**
   * Method declaration
   *
   * @param viewType
   * @see
   */
  public void setViewType(int viewType) {
    this.viewType = viewType;
  }

  /**
   * Method declaration
   *
   * @return
   * @see
   */
  public int getViewType() {
    return viewType;
  }

  /**
   * Method declaration
   *
   * @return
   * @see
   */
  public ToDoHeader getCurrentToDoHeader() {
    return currentToDoHeader;
  }

  /**
   * Method declaration
   *
   * @param todo
   * @see
   */
  public void setCurrentToDoHeader(ToDoHeader todo) {
    currentToDoHeader = todo;
  }

  /**
   * Method declaration
   *
   * @return
   * @see
   */
  public Collection<Attendee> getCurrentAttendees() {
    return currentAttendees;
  }

  /**
   * Method declaration
   *
   * @param attendees
   * @see
   */
  public void setCurrentAttendees(Collection<Attendee> attendees) {
    currentAttendees = attendees;
  }

  /**
   * Paramètre le userPannel => tous les users, sélection des users participants
   *
   * @param
   * @return
   * @throws
   * @see
   */
  public String initSelectionPeas() {
    String m_context = URLManager.getApplicationURL();
    PairObject hostComponentName = new PairObject(getString("todo"), m_context
        + "/Rtodo/jsp/Main");
    PairObject[] hostPath = new PairObject[1];
    hostPath[0] = new PairObject(getString("editionListeDiffusion"), m_context
        + "/Rtodo/jsp/Main");
    String hostUrl = m_context + "/Rtodo/jsp/saveMembers";
    String cancelUrl = m_context + "/Rtodo/jsp/saveMembers";

    Selection sel = getSelection();
    sel.resetAll();
    sel.setHostSpaceName("");
    sel.setHostPath(hostPath);
    sel.setHostComponentName(hostComponentName);

    sel.setGoBackURL(hostUrl);
    sel.setCancelURL(cancelUrl);

    // set les users deja selectionnés
    Collection<Attendee> members = getCurrentAttendees();
    if (members != null) {
      String[] usersSelected = new String[members.size()];
      Iterator<Attendee> i = members.iterator();
      int j = 0;
      while (i.hasNext()) {
        Attendee attendee = i.next();
        usersSelected[j] = attendee.getUserId();
        j++;
      }
      sel.setSelectedElements(usersSelected);
    }

    // Contraintes
    sel.setPopupMode(true);
    sel.setSetSelectable(false);
    sel.setFirstPage(Selection.FIRST_PAGE_CART);
    return Selection.getSelectionURL(Selection.TYPE_USERS_GROUPS);
  }

  /**
   * Retourne une Collection de UserDetail des utilisateurs selectionnés via le userPanel
   *
   * @param
   * @return
   * @throws TodoException
   * @see
   */
  public Collection<Attendee> getUserSelected() throws TodoException {
    Selection sel = getSelection();
    List<Attendee> attendees = new ArrayList<Attendee>();
    Collection<Attendee> oldAttendees = null;

    ToDoHeader todo = getCurrentToDoHeader();
    if (todo.getId() != null) {
      oldAttendees = getToDoAttendees(todo.getId());
    }

    String[] selectedUsers = sel.getSelectedElements();
    if (selectedUsers != null) {
      for (String selectedUserId : selectedUsers) {
        Attendee newAttendee = null;
        if (oldAttendees != null) {
          for (Attendee attendee : oldAttendees) {
            if (attendee.getUserId().equals(selectedUserId)) {
              newAttendee = attendee;
            }
          } // fin while
        } // fin if

        if (newAttendee == null) {
          newAttendee = new Attendee(selectedUserId);
        }
        attendees.add(newAttendee);
      } // fin for
    } // fin if

    return attendees;
  }

  @Override
  public void close() {
    calendarBm = null;
    super.close();
  }

  /**
   * ComponentInst cache mechanism
   *
   * @param componentId
   * @return
   */
  public ComponentInstLight getComponentInst(String componentId) {
    ComponentInstLight resultComp = null;
    ComponentInstLight cachedComp = componentsMap.get(componentId);
    if (cachedComp != null) {
      resultComp = cachedComp;
    } else {
      resultComp = getOrganisationController().getComponentInstLight(componentId);
      componentsMap.put(componentId, resultComp);
    }
    return resultComp;
  }

  /**
   * SpaceInst cache mechanism
   *
   * @param spaceId
   * @return
   */
  public SpaceInstLight getSpaceInst(String spaceId) {
    SpaceInstLight resultSpace = null;
    SpaceInstLight cachedSpace = spacesMap.get(spaceId);
    if (cachedSpace != null) {
      resultSpace = cachedSpace;
    } else {
      resultSpace = getOrganisationController().getSpaceInstLightById(spaceId);
      spacesMap.put(spaceId, resultSpace);
    }
    return resultSpace;
  }
  
  /**
   * Remove all the todo passed in parameter
   * @param tabTodoId
   * @throws TodoException
   * @see
   */
  public void removeTabToDo(String[] tabTodoId) throws TodoException {
    SilverTrace.info("todo", "ToDoSessionController.removeTabToDo()",
        "root.MSG_GEN_ENTER_METHOD");
    try {
      for(String todoId : tabTodoId) {
        removeToDo(todoId);
      }
      SilverTrace.info("todo", "ToDoSessionController.removeTabToDo()",
          "root.MSG_GEN_EXIT_METHOD");
    } catch (Exception e) {
      throw new TodoException("ToDoSessionController.removeTabToDo()",
          SilverpeasException.ERROR, "todo.MSG_CANT_REMOVE_TODO", e);
    }
  }
}
