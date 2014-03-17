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
import org.silverpeas.servlet.HttpRequest;

import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.UriBuilder;
import java.io.UnsupportedEncodingException;
import java.lang.annotation.Annotation;
import java.net.URLEncoder;
import java.util.Set;

/**
 * @author Yohann Chastagnier
 */
public abstract class WebComponentRequestContext<T extends WebComponentController> {

  private Class<? extends Annotation> httpMethodClass;
  private HttpRequest request;
  private HttpServletResponse response;
  private T controller = null;

  private Set<SilverpeasRole> userRoles;
  private SilverpeasRole greaterUserRole;

  /**
   * This methods permits to perform specific initializations.
   */
  public abstract void beforeRequestInitialize();

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

  public T getController() {
    return controller;
  }

  void setController(final T controller) {
    this.controller = controller;
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

  Navigation redirectTo(Annotation redirectTo) {
    if (redirectTo instanceof RedirectToInternalJsp) {
      return navigateToInternalJsp(((RedirectToInternalJsp) redirectTo).value());
    } else if (redirectTo instanceof RedirectToInternal) {
      return navigateToInternal(((RedirectToInternal) redirectTo).value());
    }
    return navigateTo(((RedirectTo) redirectTo).value());
  }

  private Navigation navigateToInternal(String internalPath) {
    return navigateTo(
        UriBuilder.fromUri("/").path(getComponentName()).path(internalPath).build().toString());
  }

  private Navigation navigateToInternalJsp(String jspPathname) {
    return navigateTo(
        UriBuilder.fromUri("/").path(getComponentName()).path("jsp").path(jspPathname).build()
            .toString());
  }

  private Navigation navigateTo(String path) {
    return new Navigation(path);
  }

  /**
   * Handled the navigation to the html editor.
   * @param objectId
   * @param returnPath
   * @param indexIt
   * @return
   * @throws UnsupportedEncodingException
   */
  public Navigation redirectToHtmlEditor(String objectId, String returnPath, boolean indexIt)
      throws UnsupportedEncodingException {
    getRequest().setAttribute("SpaceId", getSpaceId());
    getRequest().setAttribute("SpaceName", URLEncoder.encode(getSpaceLabel(), CharEncoding.UTF_8));
    getRequest().setAttribute("ComponentId", getComponentInstanceId());
    getRequest().setAttribute("ComponentName",
        URLEncoder.encode(getComponentInstanceLabel(), CharEncoding.UTF_8));
    getRequest().setAttribute("ObjectId", objectId);
    getRequest().setAttribute("Language", null);
    getRequest().setAttribute("ReturnUrl", URLManager.getApplicationURL() +
        URLManager.getURL(getComponentName(), "useless", getComponentInstanceId()) +
        returnPath);
    getRequest().setAttribute("UserId", String.valueOf(indexIt));
    return navigateTo("/wysiwyg/jsp/htmlEditor.jsp");
  }
}
