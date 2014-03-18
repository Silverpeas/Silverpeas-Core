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
import com.stratelia.webactiv.SilverpeasRole;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Class that represents a web route.
 * It contains the associated method to call and can verify if the call is authorized.
 * @author: Yohann Chastagnier
 */
class Path {
  private final static String REGEXP_SEPARATOR = "@#@#@";
  private final static Pattern VARIABLE_MATCHER = Pattern.compile("(\\{[^/\\?]+\\})");
  private final static Pattern VARIABLE_NAME = Pattern.compile("\\{\\s*([\\w_]+)");
  private final static Pattern VARIABLE_REGEXP_CHECK = Pattern.compile(":\\s*(.+)\\s*\\}");

  private final String path;
  private List<String> pathVariables = null;
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
    this.resourceMethod = resourceMethod;
    this.lowestRoleAccess = lowestRoleAccess;
    this.redirectTo = redirectTo;
    this.invokeBeforeIdentifiers = invokeBefore != null ? invokeBefore.value() : new String[0];
    this.invokeAfterIdentifiers = invokeAfter != null ? invokeAfter.value() : new String[0];
    this.path = parsePath(path);
  }

  /**
   * Parse the path in order to handle variables.
   * @param aPath the path to handle
   * @return the new path that is the result of parsing.
   */
  private String parsePath(String aPath) {
    StringBuilder newPath = new StringBuilder();
    StringTokenizer aPathTokenizer = new StringTokenizer(aPath, "/");
    for (; aPathTokenizer.hasMoreTokens(); ) {
      StringBuilder newPathPart = new StringBuilder();
      String pathPart = aPathTokenizer.nextToken();

      Matcher variableMatcher = VARIABLE_MATCHER.matcher(pathPart);
      if (variableMatcher.find()) {
        newPathPart.append(pathPart.substring(0, variableMatcher.start()));
        String variableDefinition = variableMatcher.group(1);
        Matcher variableName = VARIABLE_NAME.matcher(variableDefinition);
        if (!variableName.find()) {
          throw new IllegalArgumentException(
              "trying to handle a path variable " + variableDefinition +
                  ", but variable name not found");
        }
        if (pathVariables == null) {
          pathVariables = new ArrayList<String>();
        }
        pathVariables.add(variableName.group(1).trim());
        newPathPart.append(REGEXP_SEPARATOR);
        newPathPart.append("(");
        Matcher variableRegexpCheck = VARIABLE_REGEXP_CHECK.matcher(variableDefinition);
        if (variableRegexpCheck.find()) {
          newPathPart.append(variableRegexpCheck.group(1).trim());
        } else {
          newPathPart.append(".*");
        }
        newPathPart.append(")");
        newPathPart.append(pathPart.substring(variableMatcher.end()));
      }

      if (newPath.length() > 0) {
        newPath.append("/");
      }
      newPath.append(newPathPart.length() > 0 ? newPathPart.toString() : pathPart);
    }
    return newPath.toString();
  }

  /**
   * Indicates if the path instance matches the given path.
   * @param path the path to match.
   * @param requestContext the request context.
   * @return true if it matches, false otherwise. If it matches, the request context if filled
   * with name/value of variables.
   */
  public boolean matches(String path, WebComponentRequestContext requestContext) {
    StringTokenizer pathTokenizer = new StringTokenizer(path, "/");
    StringTokenizer registredPathTokenizer = new StringTokenizer(this.path, "/");
    Iterator<String> pathVariablesIt = pathVariables != null ? pathVariables.iterator() : null;

    if (pathTokenizer.countTokens() != registredPathTokenizer.countTokens()) {
      return false;
    }

    for (; pathTokenizer.hasMoreTokens(); ) {
      String pathPart = pathTokenizer.nextToken();
      String registredPathPart = registredPathTokenizer.nextToken();
      if (!pathPart.equals(registredPathPart)) {
        if (pathVariablesIt == null || !registredPathPart.contains(REGEXP_SEPARATOR)) {
          return false;
        }
        Matcher matcher =
            Pattern.compile(registredPathPart.replaceFirst(REGEXP_SEPARATOR, "")).matcher(pathPart);
        if (!matcher.find()) {
          return false;
        }
        requestContext.addPathVariables(pathVariablesIt.next(), matcher.group(1));
      }
    }

    return true;
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
