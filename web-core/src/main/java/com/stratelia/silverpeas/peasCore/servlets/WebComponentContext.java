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

import com.stratelia.silverpeas.peasCore.ComponentSessionController;
import com.stratelia.webactiv.SilverpeasRole;
import com.stratelia.webactiv.beans.admin.UserDetail;
import org.silverpeas.servlet.HttpRequest;

import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.UriBuilder;
import java.lang.annotation.Annotation;
import java.util.Set;

/**
 * @author Yohann Chastagnier
 */
public abstract class WebComponentContext<T extends ComponentSessionController> {

  private final Class<? extends Annotation> httpMethodClass;
  private final HttpRequest request;
  private final HttpServletResponse response;
  private T controller = null;

  private Set<SilverpeasRole> userRoles;
  private SilverpeasRole greaterUserRole;

  /**
   * Default hidden constructor.
   * @param httpMethodClass
   * @param request
   * @param response
   */
  public WebComponentContext(final Class<? extends Annotation> httpMethodClass,
      final HttpRequest request, final HttpServletResponse response) {
    this.httpMethodClass = httpMethodClass;
    this.request = request;
    this.response = response;
  }

  /**
   * This methods permits to perform specific initializations.
   */
  public abstract void initialize();

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

  public Navigation navigateToInternalJsp(String jspPathname) {
    return navigateTo(
        UriBuilder.fromUri("/").path(getComponentName()).path("jsp").path(jspPathname).build()
            .toString());
  }

  public Navigation navigateTo(String path) {
    return new Navigation(path);
  }
}
