/*
 * Copyright (C) 2000 - 2016 Silverpeas
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

package org.silverpeas.web.servlets;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

public class RestRequest {

  public enum Action {
    UPDATE("put"),
    DELETE("delete"),
    FIND("get"),
    CREATE("post");

    private final String type;

    Action(String type) {
      this.type = type;
    }

    public String type() {
      return this.type;
    }

    public static Action fromType(String actionType) {
      if (actionType.equalsIgnoreCase(UPDATE.type())) {
        return UPDATE;
      } else if (actionType.equalsIgnoreCase(DELETE.type())) {
        return DELETE;
      } else if (actionType.equalsIgnoreCase(FIND.type())) {
        return FIND;
      } else if (actionType.equalsIgnoreCase(CREATE.type())) {
        return CREATE;
      } else {
        return null;
      }
    }
  }

  private String id;
  private Map<String, String[]> elements;
  private Action action;
  private HttpServletRequest request;

  public RestRequest(HttpServletRequest request, String componentId) {
    this.request = request;
    elements = new HashMap<String, String[]>(10);

    String pathInfo = getPathInfo(request, componentId);
    String[] pathItems = pathInfo.split("/");
    if (pathItems.length > 0) {
      // first, parse the path for optionally an action and for an identifier
      int itemParsingStartIndex;
      this.action = Action.fromType(pathItems[0]);
      if (action != null) {
        this.id = pathItems[1];
        itemParsingStartIndex = 2;
      } else {
        this.action = Action.fromType(request.getMethod());
        this.id = pathItems[0];
        itemParsingStartIndex = 1;
      }
      // last, parse the other path parts for key-value parameters
      for (int i = itemParsingStartIndex; i < pathItems.length; i++) {
        String key = pathItems[i++];
        if (i < pathItems.length) {
          String value = pathItems[i];
          this.elements.put(key, new String[]{value});
        }
      }
    }

    this.elements.putAll(request.getParameterMap());
  }

  public Map<String, String[]> getElements() {
    return this.elements;
  }

  public String getElementValue(String name) {
    if (this.elements.containsKey(name)) {
      return this.getElements(name)[0];
    }
    return null;
  }

  public String[] getElements(String name) {
    return this.elements.get(name);
  }

  public Action getAction() {
    return action;
  }

  public String getId() {
    return this.id;
  }

  public HttpServletRequest getWebRequest() {
    return this.request;
  }

  private static String getPathInfo(HttpServletRequest request, String componentId) {
    String pathInfo = request.getRequestURI();
    String context = request.getContextPath();
    int startIndex = pathInfo.indexOf(context) + context.length();
    pathInfo = pathInfo.substring(startIndex);
    if (componentId != null) {
      int routingIndex = pathInfo.indexOf(componentId);
      if (routingIndex >= 0) {
        pathInfo = pathInfo.substring(routingIndex + componentId.length());
      }
    }
    // remove the first / if any for further parsing
    if (pathInfo.startsWith("/")) {
      pathInfo = pathInfo.substring(1);
    }
    return pathInfo;
  }

}
