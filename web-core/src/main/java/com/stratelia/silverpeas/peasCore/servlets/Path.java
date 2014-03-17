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

import com.stratelia.silverpeas.peasCore.servlets.annotation.InvokeAfter;
import com.stratelia.silverpeas.peasCore.servlets.annotation.InvokeBefore;
import com.stratelia.silverpeas.peasCore.servlets.annotation.RedirectTo;
import com.stratelia.webactiv.SilverpeasRole;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

/**
 * Class that represents a web route.
 * It contains the associated method to call and can verify if the call is authorized.
 * @author: Yohann Chastagnier
 */
class Path {

  private final String path;
  private final Method resourceMethod;
  private final SilverpeasRole lowestRoleAccess;
  private final Annotation redirectTo;
  private final String[] invokeBeforeIdentifiers;
  private final String[] invokeAfterIdentifiers;

  /**
   * Default and unique constructor.
   * @param path the path.
   * @param lowestRoleAccess the lowest role that permits the access to resourceMethod.
   * @param resourceMethod the method that has to be called for the path.
   * @param redirectTo the redirection to be performed after the end of the treatment.
   * @param invokeBefore the list of identifiers of methods to invoke before the treatment of
   * HTTP method.
   * @param invokeAfter the list of identifiers of methods to invoke before the treatment of HTTP
   */
  Path(final String path, final SilverpeasRole lowestRoleAccess, final Method resourceMethod,
      final Annotation redirectTo, final InvokeBefore invokeBefore, final InvokeAfter invokeAfter) {
    this.path = path;
    this.resourceMethod = resourceMethod;
    this.lowestRoleAccess = lowestRoleAccess;
    this.redirectTo = redirectTo;
    this.invokeBeforeIdentifiers = invokeBefore != null ? invokeBefore.value() : new String[0];
    this.invokeAfterIdentifiers = invokeAfter != null ? invokeAfter.value() : new String[0];
  }

  /**
   * Gets the path.
   * @return the path.
   */
  public String getPath() {
    return path;
  }

  /**
   * Gets the lowest role that permits the access to the method.
   * @return the lowest role that permits the access to the aimed method.
   */
  public SilverpeasRole getLowestRoleAccess() {
    return lowestRoleAccess;
  }

  /**
   * Gets the resource method to call.
   * @return the method to call.
   */
  public Method getResourceMethod() {
    return resourceMethod;
  }

  /**
   * Gets the redirection.
   * @return
   */
  public Annotation getRedirectTo() {
    return redirectTo;
  }

  /**
   * Gets the list of identifiers of methods to invoke before the treatment of HTTP method.
   * @return
   */
  public String[] getInvokeBeforeIdentifiers() {
    return invokeBeforeIdentifiers;
  }

  /**
   * Gets the list of identifiers of methods to invoke after the treatment of HTTP method.
   * @return
   */
  public String[] getInvokeAfterIdentifiers() {
    return invokeAfterIdentifiers;
  }
}
