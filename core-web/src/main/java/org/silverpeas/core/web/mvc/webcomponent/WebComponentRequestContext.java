/*
 * Copyright (C) 2000 - 2019 Silverpeas
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
package org.silverpeas.core.web.mvc.webcomponent;

import org.silverpeas.core.SilverpeasRuntimeException;
import org.silverpeas.core.admin.user.model.SilverpeasRole;
import org.silverpeas.core.admin.user.model.UserDetail;
import org.silverpeas.core.contribution.model.ContributionIdentifier;
import org.silverpeas.core.subscription.util.SubscriptionManagementContext;
import org.silverpeas.core.util.LocalizationBundle;
import org.silverpeas.core.util.URLUtil;
import org.silverpeas.core.web.http.HttpRequest;
import org.silverpeas.core.web.mvc.util.RoutingException;
import org.silverpeas.core.web.mvc.util.WysiwygRouting;
import org.silverpeas.core.web.mvc.webcomponent.annotation.RedirectTo;
import org.silverpeas.core.web.mvc.webcomponent.annotation.RedirectToInternal;
import org.silverpeas.core.web.mvc.webcomponent.annotation.RedirectToInternalJsp;
import org.silverpeas.core.web.mvc.webcomponent.annotation.RedirectToNavigationStep;
import org.silverpeas.core.web.mvc.webcomponent.annotation.RedirectToPreviousNavigationStep;

import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.UriBuilder;
import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @param <T> The type of the implementation of the {@link WebComponentController} abstract class.
 * @author Yohann Chastagnier
 */
public abstract class WebComponentRequestContext<T extends WebComponentController> {
  private static final Pattern REDIRECT_VARIABLE_MATCHER = Pattern.compile("(\\{[\\w_]+\\})+");

  private Class<? extends Annotation> httpMethodClass;
  private HttpRequest request;
  private HttpServletResponse response;
  private T controller = null;
  private boolean comingFromRedirect = false;
  private NavigationContext navigationContext;
  private boolean navigationStepContextPerformed = false;

  private Map<String, String> pathVariables = new LinkedHashMap<>();
  private Map<String, String> redirectVariables = new LinkedHashMap<>();
  private Collection<SilverpeasRole> userRoles;
  private SilverpeasRole highestUserRole;

  /**
   * This methods permits to perform initializations before the HTTP method (and associated
   * method invocation) aimed is performed.
   */
  public void beforeRequestProcessing() {
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

  void setController(final T controller) {
    this.controller = controller;
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

  /**
   * @see NavigationContext
   * @return the navigation context associated to the current instancied component.
   */
  public NavigationContext getNavigationContext() {
    if (navigationContext == null) {
      navigationContext = NavigationContext.get(this);
    }
    return navigationContext;
  }

  T getController() {
    return controller;
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

  public LocalizationBundle getMultilang() {
    return controller.getMultilang();
  }

  public WebMessager getMessager() {
    return WebMessager.getInstance();
  }

  public String getComponentUriBase() {
    return controller.getComponentUrl();
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

  public Collection<SilverpeasRole> getUserRoles() {
    if (userRoles == null) {
      userRoles = controller.getSilverpeasUserRoles();
    }
    return userRoles;
  }

  public SilverpeasRole getHighestUserRole() {
    if (highestUserRole == null) {
      highestUserRole = SilverpeasRole.getHighestFrom(getUserRoles());
      if (highestUserRole == null) {
        highestUserRole = SilverpeasRole.reader;
      }
    }
    return highestUserRole;
  }

  public boolean isComingFromRedirect() {
    return comingFromRedirect;
  }

  boolean isNavigationStepContextPerformed() {
    return navigationStepContextPerformed;
  }

  void markNavigationStepContextPerformed() {
    this.navigationStepContextPerformed = true;
  }

  Navigation redirectTo(Annotation redirectToAnnotation) {
    if (redirectToAnnotation instanceof RedirectToInternalJsp) {
      return redirectToInternalJsp(((RedirectToInternalJsp) redirectToAnnotation).value());
    } else if (redirectToAnnotation instanceof RedirectToInternal) {
      return redirectToInternal(((RedirectToInternal) redirectToAnnotation).value());
    } else if (redirectToAnnotation instanceof RedirectToNavigationStep) {
      return redirectToNavigationStep(((RedirectToNavigationStep) redirectToAnnotation).value());
    } else if (redirectToAnnotation instanceof RedirectToPreviousNavigationStep) {
      return redirectToNavigationStep("previous");
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

  private Navigation redirectToNavigationStep(String navigationStepIdentifier) {
    NavigationContext.NavigationStep navigationStep = null;
    if (!"previous".equals(navigationStepIdentifier)) {
      navigationStep = getNavigationContext().findNavigationStepFrom(navigationStepIdentifier);
    }
    if (navigationStep == null) {
      navigationStep = getNavigationContext().getPreviousNavigationStep();
    }
    return redirectTo(navigationStep.getUri().toString());
  }

  private Navigation redirectToInternal(String internalPath) {
    return redirectTo(
        normalizeRedirectPath(UriBuilder.fromUri(getComponentUriBase()), internalPath).build()
            .toString()
    );
  }

  private Navigation redirectToInternalJsp(String jspPathname) {
    return redirectTo(
        normalizeRedirectPath(UriBuilder.fromUri("/").path(getComponentName()).path("jsp"),
            jspPathname).build().toString());
  }

  private Navigation redirectTo(String path) {
    comingFromRedirect = true;
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
   * @param objectType
   * @param returnPath
   * @param indexIt
   * @return
   */
  public Navigation redirectToHtmlEditor(String objectId, final String objectType,
      String returnPath, boolean indexIt) {
    return redirectToHtmlEditor(null, objectId, objectType, returnPath, indexIt);
  }

  /**
   * Handled the navigation to the html editor.
   * @param subscriptionManagementContext
   * @param objectId
   * @param objectType
   * @param returnPath
   * @param indexIt
   * @return
   */
  public Navigation redirectToHtmlEditor(
      SubscriptionManagementContext subscriptionManagementContext, String objectId,
      final String objectType, String returnPath, boolean indexIt) {
    try {
      getRequest().setAttribute("subscriptionManagementContext", subscriptionManagementContext);

      WysiwygRouting routing = new WysiwygRouting();
      WysiwygRouting.WysiwygRoutingContext context =
          new WysiwygRouting.WysiwygRoutingContext().withSpaceLabel(getSpaceLabel())
              .withComponentLabel(getComponentInstanceLabel()).withContributionId(
              ContributionIdentifier.from(getComponentInstanceId(), objectId, objectType))
              .withLanguage(controller.getLanguage())
              .withComeBackUrl(URLUtil.getApplicationURL() +
                  URLUtil.getURL(getComponentName(), "useless", getComponentInstanceId()) +
                  returnPath)
              .withIndexation(indexIt);

      return redirectTo(routing.getWysiwygEditorPath(context, getRequest()));
    } catch (RoutingException e) {
      throw new SilverpeasRuntimeException(e);
    }
  }
}
