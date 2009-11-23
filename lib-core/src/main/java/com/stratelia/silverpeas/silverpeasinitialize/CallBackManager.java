/**
 * Copyright (C) 2000 - 2009 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have recieved a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "http://repository.silverpeas.com/legal/licensing"
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

import java.util.*;

/**
 * Title: Description: Copyright: Copyright (c) 2001 Company:
 * @author TLE
 * @version 1.0
 */
public class CallBackManager {
  // Actions [int parameter, string parameter, extra parameter]
  // ----------------------------------------------------------
  public final static int ACTION_AFTER_CREATE_USER = 0; // [userId,,]
  public final static int ACTION_BEFORE_REMOVE_USER = 1; // [userId,,]
  public final static int ACTION_AFTER_CREATE_GROUP = 2; // [groupId,,]
  public final static int ACTION_BEFORE_REMOVE_GROUP = 3; // [groupId,,]
  public final static int ACTION_AFTER_CREATE_SPACE = 4; // [spaceId(ex : 59),,]
  public final static int ACTION_BEFORE_REMOVE_SPACE = 5; // [spaceId(ex :
  // 59),,]
  public final static int ACTION_AFTER_CREATE_COMPONENT = 6; // [componentId(ex
  // : 59),,]
  public final static int ACTION_BEFORE_REMOVE_COMPONENT = 7; // [componentId(ex
  // : 59),,]
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
  public final static int ACTION_LAST = 21;

  // HashTables
  // ----------
  protected static List[] subscribers = null;

  static {
    subscribers = new List[ACTION_LAST];
    for (int i = 0; i < ACTION_LAST; i++) {
      subscribers[i] = Collections.synchronizedList(new LinkedList());
    }
  }

  // Subscriptions functions
  // -----------------------
  static public void subscribeAction(int action, CallBack theObj) {
    subscribers[action].add(theObj);
  }

  static public void unsubscribeAction(int action, CallBack theObj) {
    subscribers[action].remove(theObj);
  }

  static public void subscribeAll(CallBack theObj) {
    for (int i = 0; i < ACTION_LAST; i++) {
      subscribers[i].add(theObj);
    }
  }

  static public void unsubscribeAll(CallBack theObj) {
    for (int i = 0; i < ACTION_LAST; i++) {
      subscribers[i].remove(theObj);
    }
  }

  // Call functions
  // --------------
  static public void invoke(int action, int iParam, String sParam,
      Object extraParam) {
    Iterator it = subscribers[action].iterator();

    while (it.hasNext()) {
      ((CallBack) (it.next())).doInvoke(action, iParam, sParam, extraParam);
    }
  }

  static public String getInvokeString(int action, int iParam, String sParam,
      Object extraParam) {
    StringBuffer sb = new StringBuffer("Invoke Action = ");
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
      default:
        sb.append("ACTION_UNKNOWN");
    }
    sb.append(" iParam = " + Integer.toString(iParam) + " sParam = " + sParam
        + " extraParam = ");
    if (extraParam == null) {
      sb.append("null");
    } else {
      sb.append(extraParam.toString());
    }
    return sb.toString();
  }
}
