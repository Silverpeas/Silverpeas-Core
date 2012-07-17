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

package com.stratelia.silverpeas.silverpeasinitialize;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * The CallBackManager centralizes and dispatches the notification events coming from the different
 * actions performed within Silverpeas components. The CallBackManager is a way to loosly couply the
 * differents components between them. In effect, some components can be interested by some kind of
 * events, whatever their origin, in order to perform some useful computation. For doing, they have
 * just to subscribe a callback function for the kind of event they are interested. The callback
 * function will be invoked by a CallBackManager at the firing of a such event. The callback
 * function is represented by an object implementing the CallBack interface.
 * @deprecated Please use the Silverpeas Notification API defined in the package
 * com.silverpeas.notification
 */
@Deprecated
public class CallBackManager {
  // Actions [int parameter, string parameter, extra parameter]
  // ----------------------------------------------------------

  public final static int ACTION_AFTER_CREATE_USER = 0; // [userId,,]
  public final static int ACTION_BEFORE_REMOVE_USER = 1; // [userId,,]
  public final static int ACTION_AFTER_CREATE_GROUP = 2; // [groupId,,]
  public final static int ACTION_BEFORE_REMOVE_GROUP = 3; // [groupId,,]
  public final static int ACTION_AFTER_CREATE_SPACE = 4; // [spaceId(ex : 59),,]
  public final static int ACTION_BEFORE_REMOVE_SPACE = 5; // [spaceId(ex: 59),,]
  public final static int ACTION_AFTER_CREATE_COMPONENT = 6; // [componentId(ex: 59),,]
  public final static int ACTION_BEFORE_REMOVE_COMPONENT = 7; // [componentId(ex: 59),,]
  public final static int ACTION_ON_WYSIWYG = 8;
  public final static int ACTION_ATTACHMENT_ADD = 9;
  public final static int ACTION_ATTACHMENT_UPDATE = 10;
  public final static int ACTION_ATTACHMENT_REMOVE = 11;
  public final static int ACTION_VERSIONING_ADD = 12;
  public final static int ACTION_VERSIONING_UPDATE = 13;
  public final static int ACTION_VERSIONING_REMOVE = 14;
  public final static int ACTION_COMMENT_ADD = 15;
  public final static int ACTION_COMMENT_REMOVE = 16;
  public final static int ACTION_CUTANDPASTE = 17;
  public final static int ACTION_XMLCONTENT_CREATE = 18;
  public final static int ACTION_XMLCONTENT_UPDATE = 19;
  public final static int ACTION_XMLCONTENT_DELETE = 20;
  public final static int ACTION_HEADER_PUBLICATION_UPDATE = 21;
  public final static int ACTION_PUBLICATION_REMOVE = 22;

  public final static int ACTION_LAST = 23;

  private static final CallBackManager instance = new CallBackManager();

  /**
   * Gets an instance of a CallBackManager.
   * @return an instance of this class.
   */
  public static CallBackManager get() {
    return instance;
  }

  private List<CallBack>[] subscribers = new List[ACTION_LAST];

  /**
   * Subscribes the specified callback function for the given notification event.
   * @param action the action at the origin of a notification event.
   * @param theObj the callback function to invoke when a such action is performed.
   */
  synchronized public void subscribeAction(int action, CallBack theObj) {
    subscribers[action].add(theObj);
  }

  /**
   * Unsubscribes the specified callback function for the given notification event.
   * @param action the action at the origin of a notification event.
   * @param theObj the callback function to unsubscribe.
   */
  synchronized public void unsubscribeAction(int action, CallBack theObj) {
    subscribers[action].remove(theObj);
  }

  /**
   * Subscribes the specified callback function for all notification events, whatever they are.
   * @param theObj the callback function to invoke when an action, responsible of a notification
   * event firing, is performed.
   */
  synchronized public void subscribeAll(CallBack theObj) {
    for (int i = 0; i < ACTION_LAST; i++) {
      subscribers[i].add(theObj);
    }
  }

  /**
   * Unsubscribes the specified callback function for all notification events, whatever they are.
   * @param theObj the callback function to unsubscribe.
   */
  synchronized public void unsubscribeAll(CallBack theObj) {
    for (int i = 0; i < ACTION_LAST; i++) {
      subscribers[i].remove(theObj);
    }
  }

  /**
   * Invokes all of the callback functions registered for the specified event matching a
   * well-defined action.
   * @param action the event fired and matching a specific context of an action.
   * @param iParam a parameter as integer.
   * @param sParam another parameter as string.
   * @param extraParam an extra parameter as an object.
   */
  synchronized public void invoke(final int action, final int iParam, final String sParam,
      final Object extraParam) {
    for (CallBack callback : subscribers[action]) {
      callback.doInvoke(action, iParam, sParam, extraParam);
    }
  }

  /**
   * Gets the string representation of the invocation of the callbacks registered for the specified
   * event.
   * @param action the event matching a specific context of an action.
   * @param iParam a parameter as integer.
   * @param sParam another parameter as string.
   * @param extraParam an extra parameter as an object.
   * @return a String representation of the invocation.
   */
  public String getInvokeString(int action, int iParam, String sParam,
      Object extraParam) {
    StringBuilder sb = new StringBuilder("Invoke Action = ");
    switch (action) {
      case ACTION_AFTER_CREATE_USER:
        sb.append("ACTION_AFTER_CREATE_USER");
        break;
      case ACTION_BEFORE_REMOVE_USER:
        sb.append("ACTION_BEFORE_REMOVE_USER");
        break;
      case ACTION_AFTER_CREATE_GROUP:
        sb.append("ACTION_AFTER_CREATE_GROUP");
        break;
      case ACTION_BEFORE_REMOVE_GROUP:
        sb.append("ACTION_BEFORE_REMOVE_GROUP");
        break;
      case ACTION_AFTER_CREATE_SPACE:
        sb.append("ACTION_AFTER_CREATE_SPACE");
        break;
      case ACTION_BEFORE_REMOVE_SPACE:
        sb.append("ACTION_BEFORE_REMOVE_SPACE");
        break;
      case ACTION_AFTER_CREATE_COMPONENT:
        sb.append("ACTION_AFTER_CREATE_COMPONENT");
        break;
      case ACTION_BEFORE_REMOVE_COMPONENT:
        sb.append("ACTION_BEFORE_REMOVE_COMPONENT");
        break;
      case ACTION_ON_WYSIWYG:
        sb.append("ACTION_ON_WYSIWYG");
        break;
      case ACTION_ATTACHMENT_ADD:
        sb.append("ACTION_ATTACHMENT_ADD");
        break;
      case ACTION_ATTACHMENT_UPDATE:
        sb.append("ACTION_ATTACHMENT_UPDATE");
        break;
      case ACTION_ATTACHMENT_REMOVE:
        sb.append("ACTION_ATTACHMENT_REMOVE");
        break;
      case ACTION_VERSIONING_ADD:
        sb.append("ACTION_VERSIONING_ADD");
        break;
      case ACTION_VERSIONING_UPDATE:
        sb.append("ACTION_VERSIONING_UPDATE");
        break;
      case ACTION_VERSIONING_REMOVE:
        sb.append("ACTION_VERSIONING_REMOVE");
        break;
      case ACTION_COMMENT_ADD:
        sb.append("ACTION_COMMENT_ADD");
        break;
      case ACTION_COMMENT_REMOVE:
        sb.append("ACTION_COMMENT_REMOVE");
        break;
      case ACTION_CUTANDPASTE:
        sb.append("ACTION_CUTANDPASTE");
        break;
      case ACTION_XMLCONTENT_CREATE:
        sb.append("ACTION_XMLCONTENT_CREATE");
        break;
      case ACTION_XMLCONTENT_UPDATE:
        sb.append("ACTION_XMLCONTENT_UPDATE");
        break;
      case ACTION_XMLCONTENT_DELETE:
        sb.append("ACTION_XMLCONTENT_DELETE");
        break;
      case ACTION_HEADER_PUBLICATION_UPDATE:
        sb.append("ACTION_HEADER_PUBLICATION_UPDATE");
        break;
      default:
        sb.append("ACTION_UNKNOWN");
    }
    sb.append(" iParam = ").append(String.valueOf(iParam)).append(" sParam = ").append(sParam);
    sb.append(" extraParam = ");
    if (extraParam == null) {
      sb.append("null");
    } else {
      sb.append(extraParam.toString());
    }
    return sb.toString();
  }

  /**
   * Constructs a new CallBackManager instance.
   */
  private CallBackManager() {
    for (int i = 0; i < ACTION_LAST; i++) {
      subscribers[i] = new CopyOnWriteArrayList<CallBack>();
    }
  }
}
