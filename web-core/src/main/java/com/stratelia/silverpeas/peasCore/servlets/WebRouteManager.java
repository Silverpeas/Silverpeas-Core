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

import com.silverpeas.peasUtil.AccessForbiddenException;
import com.stratelia.silverpeas.peasCore.ComponentSessionController;
import com.stratelia.silverpeas.peasCore.servlets.annotation.Homepage;
import com.stratelia.silverpeas.peasCore.servlets.annotation.LowestRoleAccess;
import com.stratelia.webactiv.SilverpeasRole;
import com.stratelia.webactiv.util.exception.SilverpeasException;
import org.silverpeas.cache.service.CacheServiceFactory;
import org.silverpeas.servlet.HttpRequest;

import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * This class handles all the route paths of Web Resources.
 * Fisrt time the manager is called by a resource, an initialization is done. It consits of
 * scanning all the methods of the resource to extract these the can be associated to a route (URI
 * path).
 * @author Yohann Chastagnier
 */
public class WebRouteManager {

  private static final Map<String, WebRouteManager> managedWebComponentRouters =
      new HashMap<String, WebRouteManager>();

  private com.stratelia.silverpeas.peasCore.servlets.Path defaultPath = null;
  private Map<String, HttpMethodPaths> httpMethodPaths = new HashMap<String, HttpMethodPaths>();

  /**
   * This method must be called before all treatments in order to initilize the Web Component
   * Context associated to the current request.
   * @param resource the resource which exposes the method that will be invoked.
   * @param httpMethodClass the annotation class associated to the current http method of the
   * request.
   * @param request the request itself.
   * @param response the response itself.
   * @param <T> the type of the Component Session Controller that provides a lot of stuff around
   * the component, the user, etc.
   * @param <WEB_COMPONENT_CONTEXT> the type of the web component context.
   * @param <R> the type of the resource which hosts the method that must be invoked.
   * @return
   */
  @SuppressWarnings("unchecked")
  public static <T extends ComponentSessionController, WEB_COMPONENT_CONTEXT extends
      WebComponentContext<T>, R extends WebComponentRequestRouter<T,
      WEB_COMPONENT_CONTEXT>> R manageRequestOf(
      R resource, Class<? extends Annotation> httpMethodClass, HttpRequest request,
      HttpServletResponse response) {

    // If the request is already managed, then bypassing the treatment of this method. This
    // avoids to override the context of subcalls of HTTP servlet methods.
    if (CacheServiceFactory.getRequestCacheService().get(WebComponentContext.class.getName()) ==
        null) {

      // Getting the manager associated to the type of the given resource.
      WebRouteManager webRouteManager =
          managedWebComponentRouters.get(resource.getClass().getName());

      // Initializing it (one time) if it is not already done.
      if (webRouteManager == null) {
        webRouteManager = initialize(resource);
        managedWebComponentRouters.put(resource.getClass().getName(), webRouteManager);
      }

      // Retrieving the class of the web context associated to the given resource.
      Class<WEB_COMPONENT_CONTEXT> webComponentContextClass =
          ((Class<WEB_COMPONENT_CONTEXT>) ((ParameterizedType) resource.getClass().
              getGenericSuperclass()).getActualTypeArguments()[1]);
      try {

        // Instanciating, and caching into the request, the web context.
        Constructor<WEB_COMPONENT_CONTEXT> webComponentContext = webComponentContextClass
            .getConstructor(Class.class, HttpRequest.class, HttpServletResponse.class);
        CacheServiceFactory.getRequestCacheService().put(WebComponentContext.class.getName(),
            webComponentContext.newInstance(httpMethodClass, request, response));
      } catch (Exception e) {
        throw new IllegalArgumentException(
            "Constructor (Class, HttpRequest, HttpServletResponse) doesn't exist...", e);
      }
    }
    return resource;
  }

  /**
   * Initializes all the route paths from the method analizing of the given resource instance.
   * The resource must provide methods with only one parameter : WebRouteContext.
   * This method must be annoted by :
   * <ul>
   * <li>{@link javax.ws.rs.GET}</li>
   * <li>{@link javax.ws.rs.POST}</li>
   * <li>{@link javax.ws.rs.PUT}</li>
   * <li>{@link javax.ws.rs.DELETE}</li>
   * <li>{@link javax.ws.rs.Path} (multiple)</li>
   * <li>{@link com.stratelia.silverpeas.peasCore.servlets.annotation.LowestRoleAccess} (at most
   * one)</li>
   * </ul>
   * The resource methods must also return a {@link com.stratelia.silverpeas.peasCore.servlets
   * .Navigation} instance.
   * @param resource the resource which exposes the method that will be invoked.
   * @param <R> the type of the resource which exposes the method that will be invoked.
   * @return the manager initialized for the resoure.
   */
  private static <R> WebRouteManager initialize(R resource) {
    WebRouteManager webRouteManager = new WebRouteManager();

    // Scanning the methods of the resources
    for (Method resourceMethod : resource.getClass().getMethods()) {

      // Taking into account only those that has one WebRouterContext parameter and no other one.
      // The return value must be a Navigation instance.
      Class<?>[] parameterTypes = resourceMethod.getParameterTypes();
      if (parameterTypes.length == 1 &&
          WebComponentContext.class.isAssignableFrom(parameterTypes[0])) {
        Set<Class<? extends Annotation>> httpMethods =
            new LinkedHashSet<Class<? extends Annotation>>();
        Set<Path> paths = new LinkedHashSet<Path>();
        SilverpeasRole lowestRoleAccess = null;
        boolean isDefaultPaths = false;
        for (Annotation annotation : resourceMethod.getDeclaredAnnotations()) {
          if (annotation instanceof GET) {
            httpMethods.add(GET.class);
          } else if (annotation instanceof POST) {
            httpMethods.add(POST.class);
          } else if (annotation instanceof PUT) {
            httpMethods.add(PUT.class);
          } else if (annotation instanceof DELETE) {
            httpMethods.add(DELETE.class);
          } else if (annotation instanceof Path) {
            paths.add((Path) annotation);
          } else if (annotation instanceof LowestRoleAccess) {
            if (lowestRoleAccess != null) {
              throw new IllegalArgumentException(
                  "Only one greatest Silverpeas Role must be specified for method: " +
                      resourceMethod.getName());
            }
            lowestRoleAccess = ((LowestRoleAccess) annotation).value();
          } else if (annotation instanceof Homepage) {
            if (isDefaultPaths) {
              throw new IllegalArgumentException(
                  "The homepage method is already specified, error on method " +
                      resourceMethod.getName());
            }
            isDefaultPaths = true;
          }
        }

        if (httpMethods.isEmpty()) {
          throw new IllegalArgumentException(
              "Http Method must be specified for method: " + resourceMethod.getName());
        }

        if (!resourceMethod.getReturnType().isAssignableFrom(Navigation.class)) {
          throw new IllegalArgumentException(
              resourceMethod.getName() + " method must return a Navigation instance.");
        }

        for (Class<? extends Annotation> httpMethodClass : httpMethods) {
          HttpMethodPaths httpMethodPaths =
              webRouteManager.httpMethodPaths.get(httpMethodClass.getName());
          if (httpMethodPaths == null) {
            httpMethodPaths = new HttpMethodPaths(httpMethodClass);
            webRouteManager.httpMethodPaths.put(httpMethodClass.getName(), httpMethodPaths);
          }
          List<com.stratelia.silverpeas.peasCore.servlets.Path> registredPaths =
              httpMethodPaths.addPaths(paths, lowestRoleAccess, resourceMethod);
          if (isDefaultPaths) {
            webRouteManager.defaultPath = registredPaths.get(0);
          }
        }
      }
    }

    if (webRouteManager.defaultPath == null) {
      throw new IllegalArgumentException("The homepage method must be specified");
    }

    return webRouteManager;
  }

  /**
   * Performs a request path by executing the right method behind and returning the navigation
   * object instance.
   * @param resource the resource which exposes the method that will be invoked.
   * @param controller the component controller to put into the web component context before the
   * right method invokation.
   * @param path the path that must be matched in finding of the method to invoke.
   * @param <T> the type of the Component Session Controller that provides a lot of stuff around
   * the component, the user, etc.
   * @param <WEB_COMPONENT_CONTEXT> the type of the web component context.
   * @param <R> the type of the resource which hosts the method that must be invoked.
   * @return
   * @throws AccessForbiddenException
   * @throws IOException
   * @throws InvocationTargetException
   * @throws IllegalAccessException
   */
  @SuppressWarnings("unchecked")
  public static <T extends ComponentSessionController, WEB_COMPONENT_CONTEXT extends
      WebComponentContext<T>, R extends WebComponentRequestRouter<T,
      WEB_COMPONENT_CONTEXT>> Navigation perform(
      R resource, T controller, String path)
      throws AccessForbiddenException, IOException, InvocationTargetException,
      IllegalAccessException {

    // Retrieving
    WEB_COMPONENT_CONTEXT webComponentContext =
        (WEB_COMPONENT_CONTEXT) CacheServiceFactory.getRequestCacheService()
            .get(WebComponentContext.class.getName());
    webComponentContext.setController(controller);

    webComponentContext.initialize();
    resource.commonContextInitialization(webComponentContext);

    WebRouteManager webRouteManager = managedWebComponentRouters.get(resource.getClass().getName());

    return webRouteManager.exucutePath(resource, path, webComponentContext);
  }

  /**
   * Performs a request path by executing the right method behind and returning the navigation.
   * @param resource the resource which exposes the method that will be invoked.
   * @param path the path that must be matched in finding of the method to invoke.
   * @param webComponentContext the context of the web component routing.
   * @param <T> the type of the Component Session Controller that provides a lot of stuff around
   * the component, the user, etc.
   * @param <WEB_COMPONENT_CONTEXT> the type of the web component context.
   * @param <R> the type of the resource which hosts the method that must be invoked.
   * @return the {@link com.stratelia.silverpeas.peasCore.servlets.Navigation} instance that
   * contains the destination.
   * @throws AccessForbiddenException
   * @throws IOException
   * @throws InvocationTargetException
   * @throws IllegalAccessException
   */
  private <T extends ComponentSessionController, WEB_COMPONENT_CONTEXT extends
      WebComponentContext<T>, R extends WebComponentRequestRouter<T,
      WEB_COMPONENT_CONTEXT>> Navigation exucutePath(
      R resource, String path, WEB_COMPONENT_CONTEXT webComponentContext)
      throws AccessForbiddenException, IOException, InvocationTargetException,
      IllegalAccessException {
    com.stratelia.silverpeas.peasCore.servlets.Path pathToPerform = null;

    // Finding a registred path that matches the required path.
    HttpMethodPaths methodPaths =
        httpMethodPaths.get(webComponentContext.getHttpMethodClass().getName());
    if (methodPaths != null) {
      pathToPerform = methodPaths.findPath(path);
    }

    // If no path is found, then the default one is selected.
    if (pathToPerform == null) {
      pathToPerform = defaultPath;
    }

    // Has the user enough rights to access the aimed treatment?
    if (pathToPerform.getLowestRoleAccess() != null && !webComponentContext.getGreaterUserRole()
        .isGreaterThanOrEquals(pathToPerform.getLowestRoleAccess())) {
      webComponentContext.getResponse().sendError(HttpServletResponse.SC_FORBIDDEN);
      throw new AccessForbiddenException("WebRouteManager.executePath", SilverpeasException.ERROR,
          "User id " + webComponentContext.getUser().getId() + " has not right access to " +
              webComponentContext.getRequest().getRequestURI());
    }

    // Calling finally the method behinf the idenfied path.
    return (Navigation) pathToPerform.getResourceMethod().invoke(resource, webComponentContext);
  }
}
