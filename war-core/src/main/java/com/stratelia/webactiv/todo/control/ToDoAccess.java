package com.stratelia.webactiv.todo.control;

import java.util.Collection;

import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.calendar.control.CalendarBm;
import com.stratelia.webactiv.calendar.control.CalendarBmHome;
import com.stratelia.webactiv.util.EJBUtilitaire;
import com.stratelia.webactiv.util.JNDINames;
import com.stratelia.webactiv.util.exception.SilverpeasException;

public class ToDoAccess {
  static private CalendarBm calendarBm = null;

  /**
   * getEJB
   * 
   * @return instance of CalendarBmHome
   */
  static private CalendarBm getEJB() throws TodoException {
    if (calendarBm == null) {
      try {
        calendarBm = ((CalendarBmHome) EJBUtilitaire.getEJBObjectRef(
            JNDINames.CALENDARBM_EJBHOME, CalendarBmHome.class)).create();
      } catch (Exception e) {
        throw new TodoException("ToDoAccess.getEJB()",
            SilverpeasException.ERROR, "root.EX_CANT_GET_REMOTE_OBJECT", e);
      }
    }
    return calendarBm;
  }

  /**
   * getNotCompletedToDos
   * 
   * @return Collection of TodoHeaders
   * 
   * @throws TodoException
   */
  static public Collection getNotCompletedToDos(String userId)
      throws TodoException {
    Collection result;

    SilverTrace.info("todo", "ToDoAccess.getNotCompletedToDos()",
        "root.MSG_GEN_ENTER_METHOD");
    try {
      result = getEJB().getNotCompletedToDosForUser(userId);
    } catch (Exception e) {
      throw new TodoException("ToDoAccess.getNotCompletedToDos()",
          SilverpeasException.ERROR, "todo.MSG_CANT_GET_ENDED_TODOS", e);
    }
    SilverTrace.info("todo", "ToDoAccess.getNotCompletedToDos()",
        "root.MSG_GEN_EXIT_METHOD");
    return result;
  }
}