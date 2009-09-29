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
package com.silverpeas.util.web.servlet;

import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

import javax.servlet.http.HttpServletRequest;

import com.stratelia.silverpeas.silvertrace.SilverTrace;

public class RestRequest {
  public static final int UPDATE = 1;
  public static final int DELETE = 2;
  public static final int FIND = 3;
  public static final int CREATE = 4;

  public static final String UPDATE_ACTION = "put";
  public static final String DELETE_ACTION = "delete";

  private String id;
  private Map<String, String[]> elements;
  private int action;
  private HttpServletRequest request;

  public RestRequest(HttpServletRequest request, String componentId) {
    this.request = request;
    elements = new HashMap<String, String[]>(10);
    if ("POST".equalsIgnoreCase(request.getMethod())) {
      action = CREATE;
    } else if ("GET".equalsIgnoreCase(request.getMethod())) {
      action = FIND;
    } else if ("PUT".equalsIgnoreCase(request.getMethod())) {
      action = UPDATE;
    } else if ("DELETE".equalsIgnoreCase(request.getMethod())) {
      action = DELETE;
    }
    String pathInfo = request.getRequestURI();
    String context = request.getContextPath();
    int startIndex = pathInfo.indexOf(context) + context.length();
    pathInfo = pathInfo.substring(startIndex);
    int routingIndex = pathInfo.indexOf(componentId);
    if (routingIndex >= 0) {
      pathInfo = pathInfo.substring(routingIndex + componentId.length());
    }
    // substring du context
    if (pathInfo.startsWith("/")) {
      pathInfo = pathInfo.substring(1);
    }

    SilverTrace.debug("util", "RestRequest()",
        "root.MSG_GEN_ENTER_METHOD", "Parsing:" + pathInfo);
    StringTokenizer tokenizer = new StringTokenizer(pathInfo, "/", false);
    String element = tokenizer.nextToken();
    this.id = tokenizer.nextToken();
    if (DELETE_ACTION.equalsIgnoreCase(element)) {
      action = DELETE;
    } else if (UPDATE_ACTION.equalsIgnoreCase(element)) {
      action = UPDATE;
    } else {
      this.id = element;
      tokenizer = new StringTokenizer(pathInfo, "/", false);
      tokenizer.nextToken();
    }
    boolean isKey = true;
    String key = null;
    String value = null;
    while (tokenizer.hasMoreTokens()) {
      value = tokenizer.nextToken();
      if (isKey) {
        key = value;
        isKey = false;
      } else {
        this.elements.put(key, new String[] { value });
        SilverTrace.debug("util", "RestRequest()",
            "root.MSG_GEN_ENTER_METHOD", key + '=' + value);
        isKey = true;
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
    return (String[]) this.elements.get(name);
  }

  public int getAction() {
    return action;
  }

  public String getId() {
    return this.id;
  }

  public HttpServletRequest getWebRequest() {
    return this.request;
  }

}
