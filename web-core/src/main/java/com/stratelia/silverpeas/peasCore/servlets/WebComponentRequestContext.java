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
package com.stratelia.silverpeas.peasCore.servlets;

import com.stratelia.silverpeas.peasCore.URLManager;
import com.stratelia.silverpeas.peasCore.servlets.annotation.RedirectTo;
import com.stratelia.silverpeas.peasCore.servlets.annotation.RedirectToInternal;
import com.stratelia.silverpeas.peasCore.servlets.annotation.RedirectToInternalJsp;
import com.stratelia.webactiv.SilverpeasRole;
import com.stratelia.webactiv.beans.admin.UserDetail;
import org.apache.commons.lang3.CharEncoding;
import org.silverpeas.notification.message.MessageContainer;
import org.silverpeas.notification.message.MessageManager;
import org.silverpeas.servlet.HttpRequest;

import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.UriBuilder;
import java.io.UnsupportedEncodingException;
import java.lang.annotation.Annotation;
import java.net.URLEncoder;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @param <CONTROLLER>
 * @author Yohann Chastagnier
 */
public class WebComponentRequestContext<CONTROLLER extends WebComponentController> {
  private final static Pattern REDIRECT_VARIABLE_MATCHER = Pattern.compile("(\\{[\\w_]+\\})+");

  private Class<? extends Annotation> httpMethodClass;
  private HttpRequest request;
  private HttpServletResponse response;
  private CONTROLLER controller = null;

  private Map<String, String> pathVariables = new LinkedHashMap<String, String>();
  private Map<String, String> redirectVariables = new LinkedHashMap<String, String>();
  private Set<SilverpeasRole> userRoles;
  private SilverpeasRole greaterUserRole;

  /**
   * This methods permits to perform initializations before the HTTP method (and associated
   * method invokation) aimed is performed.
   */
  public void beforeRequestInitialize() {
    // Nothing to do by default.
  }

  void setHttpMethodClass(final Class<? extends Annotation> httpMethodClass) {
    this.httpMethodClass = httpMethodClass;
  }

  void setRequest(final HttpRequest request) {
    this.request = request;
  }

  void setResponse(final HttpServletResponse response) {
    this.response = response;
  }

  public Class<? extends Annotation> getHttpMethodClass() {
    return httpMethodClass;
  }

  public HttpRequest getRequest() {
    return request;
  }

  public HttpServletResponse getResponse() {
    return response;
  }

  public CONTROLLER getController() {
    return controller;
  }

  void setController(final CONTROLLER controller) {
    this.controller = controller;
  }

  public Map<String, String> getPathVariables() {
    return Collections.unmodifiableMap(pathVariables);
  }

  void addPathVariable(final String variableName, final String variableValue) {
    if (pathVariables.containsKey(variableName) &&
        !pathVariables.get(variableName).equals(variableValue)) {
      pathVariables.clear();
      throw new IllegalArgumentException(
          "trying to set different values for the same variable: " + variableName);
    }
    pathVariables.put(variableName, variableValue);
  }

  public void addRedirectVariable(final String variableName, final String variableValue) {
    if ((pathVariables.containsKey(variableName) &&
        !pathVariables.get(variableName).equals(variableValue)) ||
        (redirectVariables.containsKey(variableName) &&
            !redirectVariables.get(variableName).equals(variableValue))) {
      pathVariables.clear();
      redirectVariables.clear();
      throw new IllegalArgumentException(
          "trying to set different values for the same variable: " + variableName);
    }
    redirectVariables.put(variableName, variableValue);
  }

  public MessageContainer getMessageManager() {
    return MessageManager.getMessageContainer(MessageManager.getRegistredKey());
  }

  public String getComponentInstanceId() {
    return controller.getComponentId();
  }

  public String getComponentInstanceLabel() {
    return controller.getComponentLabel();
  }

  public String getComponentName() {
    return controller.getComponentName();
  }

  public String getSpaceId() {
    return controller.getSpaceId();
  }

  public String getSpaceLabel() {
    return controller.getSpaceLabel();
  }

  public UserDetail getUser() {
    return controller.getUserDetail();
  }

  public Set<SilverpeasRole> getUserRoles() {
    if (userRoles == null) {
      userRoles = SilverpeasRole.from(controller.getUserRoles());
    }
    return userRoles;
  }

  public SilverpeasRole getGreaterUserRole() {
    if (greaterUserRole == null) {
      greaterUserRole = SilverpeasRole.getGreaterFrom(getUserRoles());
    }
    return greaterUserRole;
  }

  Navigation redirectTo(Annotation redirectToAnnotation) {
    if (redirectToAnnotation instanceof RedirectToInternalJsp) {
      return redirectToInternalJsp(((RedirectToInternalJsp) redirectToAnnotation).value());
    } else if (redirectToAnnotation instanceof RedirectToInternal) {
      return redirectToInternal(((RedirectToInternal) redirectToAnnotation).value());
    }
    RedirectTo redirectTo = (RedirectTo) redirectToAnnotation;
    switch (redirectTo.type()) {
      case INTERNAL_JSP:
        return redirectToInternalJsp(redirectTo.value());
      case INTERNAL:
        return redirectToInternal(redirectTo.value());
      default:
        return redirectTo(normalizeRedirectPath(UriBuilder.fromUri("/"),
            ((RedirectTo) redirectToAnnotation).value()).build().toString());
    }
  }

  private Navigation redirectToInternal(String internalPath) {
    return redirectTo(
        normalizeRedirectPath(UriBuilder.fromUri("/").path(getComponentName()), internalPath)
            .build().toString());
  }

  private Navigation redirectToInternalJsp(String jspPathname) {
    return redirectTo(
        normalizeRedirectPath(UriBuilder.fromUri("/").path(getComponentName()).path("jsp"),
            jspPathname).build().toString());
  }

  private Navigation redirectTo(String path) {
    return new Navigation(path);
  }

  private UriBuilder normalizeRedirectPath(UriBuilder uriBuilder, String path) {
    int indexOfUriParamSplit = path.indexOf('?');
    if (indexOfUriParamSplit >= 0) {

      // URI part
      String uriPart = path.substring(0, indexOfUriParamSplit);
      uriBuilder.path(replaceRedirectVariables(uriPart.replaceAll("/\\s*$", "")));

      // Params part
      String paramPart = path.substring(indexOfUriParamSplit + 1);
      StringTokenizer paramPartTokenizer = new StringTokenizer(paramPart, "&");
      while (paramPartTokenizer.hasMoreTokens()) {
        String param = paramPartTokenizer.nextToken();
        int indexOfEqual = param.indexOf('=');
        if (indexOfEqual > 0) {
          String paramName = param.substring(0, indexOfEqual);
          String paramValue = param.substring(indexOfEqual + 1);
          uriBuilder.queryParam(paramName, replaceRedirectVariables(paramValue));
        } else {
          uriBuilder.queryParam(replaceRedirectVariables(param));
        }
      }
    } else {
      uriBuilder.path(replaceRedirectVariables(path.replaceAll("/\\s*$", "")));
    }
    return uriBuilder;
  }

  /**
   * @param redirectPath
   * @return
   */
  private String replaceRedirectVariables(String redirectPath) {
    String newPath = redirectPath;

    Matcher variableMatcher = REDIRECT_VARIABLE_MATCHER.matcher(redirectPath);
    while (variableMatcher.find()) {
      String variableName = variableMatcher.group(1).replaceAll("[\\{\\}]", "");
      String variableValue =
          pathVariables.containsKey(variableName) ? pathVariables.get(variableName) :
              redirectVariables.get(variableName);
      if (variableValue != null) {
        newPath = newPath.replace(variableMatcher.group(1), variableValue);
      }
    }

    return newPath;
  }

  /**
   * Handled the navigation to the html editor.
   * @param objectId
   * @param returnPath
   * @param indexIt
   * @return
   */
  public Navigation redirectToHtmlEditor(String objectId, String returnPath, boolean indexIt) {
    try {
      getRequest().setAttribute("SpaceId", getSpaceId());
      getRequest()
          .setAttribute("SpaceName", URLEncoder.encode(getSpaceLabel(), CharEncoding.UTF_8));
      getRequest().setAttribute("ComponentId", getComponentInstanceId());
      getRequest().setAttribute("ComponentName",
          URLEncoder.encode(getComponentInstanceLabel(), CharEncoding.UTF_8));
      getRequest().setAttribute("ObjectId", objectId);
      getRequest().setAttribute("Language", null);
      getRequest().setAttribute("ReturnUrl", URLManager.getApplicationURL() +
          URLManager.getURL(getComponentName(), "useless", getComponentInstanceId()) +
          returnPath);
      getRequest().setAttribute("UserId", String.valueOf(indexIt));
      return redirectTo("/wysiwyg/jsp/htmlEditor.jsp");
    } catch (UnsupportedEncodingException e) {
      throw new RuntimeException(e);
    }
  }
}
