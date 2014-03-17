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

import com.stratelia.webactiv.SilverpeasRole;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

/**
 * Class that registers for an http method all the paths.
 * Each path knows the method to call and the lowest role to access it.
 * @author Yohann Chastagnier
 */
class HttpMethodPaths {

  private final Class<? extends Annotation> httpMethodClass;

  private Map<String, Path> pathRoutes = new HashMap<String, Path>();

  /**
   * Default and unique constructor.
   * @param httpMethodClass the http method class handled by the instance.
   */
  HttpMethodPaths(final Class<? extends Annotation> httpMethodClass) {
    this.httpMethodClass = httpMethodClass;
  }

  /**
   * Gets the class of Http Method annotation handled by this instance.
   * @return
   */
  public Class<? extends Annotation> getHttpMethodClass() {
    return httpMethodClass;
  }

  /**
   * Adds to the container the given paths and set for each the lowest role to access the behind
   * method.
   * @param paths the paths to register.
   * @param lowestRoleAccess the lowest role that permits the access to the resourceMethod.
   * @param resourceMethod the method that has to be called for the path.
   * @return registred paths.
   */
  List<Path> addPaths(Set<javax.ws.rs.Path> paths, SilverpeasRole lowestRoleAccess,
      Method resourceMethod) {
    List<Path> registredPaths = new ArrayList<Path>();
    if (paths.isEmpty()) {
      registredPaths.add(addPath("/", lowestRoleAccess, resourceMethod));
    } else {
      for (javax.ws.rs.Path path : paths) {
        registredPaths.add(addPath(path.value(), lowestRoleAccess, resourceMethod));
      }
    }
    return registredPaths;
  }

  /**
   * Adds to the container the given path and set to it the lowest role to access the behind
   * method.
   * @param path the path to register.
   * @param lowestRoleAccess the lowest role that permits the access to the resourceMethod.
   * @param resourceMethod the method that has to be called for the path.
   * @return the registred path.
   */
  private Path addPath(String path, SilverpeasRole lowestRoleAccess, Method resourceMethod) {
    path = path.replaceFirst("^/", "");
    Path pathRoute = pathRoutes.get(path);
    if (pathRoute != null) {
      throw new IllegalArgumentException(
          "Specified path for method " + resourceMethod.getName() + " already exists for method " +
              pathRoute.getResourceMethod().getName());
    }
    Path pathToRegister = new Path(path, lowestRoleAccess, resourceMethod);
    pathRoutes.put(path, pathToRegister);
    return pathToRegister;
  }

  /**
   * Finds the right paths
   * @param path
   * @return
   */
  public Path findPath(String path) {
    Path foundPath = null;
    for (Map.Entry<String, Path> entry : pathRoutes.entrySet()) {
      StringTokenizer pathTokenizer = new StringTokenizer(path, "/");
      StringTokenizer registredPathTokenizer = new StringTokenizer(entry.getKey(), "/");

      if (pathTokenizer.countTokens() != registredPathTokenizer.countTokens()) {
        continue;
      }

      foundPath = entry.getValue();

      for (; pathTokenizer.hasMoreTokens(); ) {
        String pathPart = pathTokenizer.nextToken();
        String resgistredPathPart = registredPathTokenizer.nextToken();
        if (!pathPart.equals(resgistredPathPart)) {
          foundPath = null;
          break;
        }
      }
      if (foundPath != null) {
        break;
      }
    }
    return foundPath;
  }
}
