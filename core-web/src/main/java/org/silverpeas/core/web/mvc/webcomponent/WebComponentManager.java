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
package org.silverpeas.core.web.mvc.webcomponent;

import org.silverpeas.core.web.mvc.util.AccessForbiddenException;
import org.silverpeas.core.web.mvc.webcomponent.annotation.*;
import org.silverpeas.core.cache.service.CacheServiceProvider;
import org.silverpeas.core.web.http.HttpRequest;
import org.silverpeas.core.util.StringUtil;
import org.silverpeas.core.util.annotation.AnnotationUtil;
import org.silverpeas.core.exception.SilverpeasException;

import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.UriBuilder;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * This class handles all the route paths of Web Resources.
 * First time the manager is called by a resource, an initialization is done. It consists of
 * scanning all the methods of the resource to extract these the can be associated to a route (URI
 * path).
 * @author Yohann Chastagnier
 */
public class WebComponentManager {

  static final Map<String, WebComponentManager> managedWebComponentRouters =
      new ConcurrentHashMap<>();

  private org.silverpeas.core.web.mvc.webcomponent.Path defaultPath = null;
  private Map<String, HttpMethodPaths> httpMethodPaths = new HashMap<>();
  private Map<String, Method> invokables = new HashMap<>();

  /**
   * This method must be called before all treatments in order to initilize the Web Component
   * Context associated to the current request.
   * @param webComponentControllerClass the class resource which exposes the methods that will be
   * invoked.
   * @param httpMethodClass the annotation class associated to the current http method of the
   * request.
   * @param request the request itself.
   * @param response the response itself.
   * @param <CONTROLLER> the type of the Web Component Controller that provides a lot of stuff
   * around
   * the component, the user, etc.
   * @param <WEB_COMPONENT_REQUEST_CONTEXT> the type of the web component request context.
   */
  public static <CONTROLLER extends WebComponentController<WEB_COMPONENT_REQUEST_CONTEXT>,
      WEB_COMPONENT_REQUEST_CONTEXT extends WebComponentRequestContext> void manageRequestFor(
      Class<CONTROLLER> webComponentControllerClass, Class<? extends Annotation> httpMethodClass,
      HttpRequest request, HttpServletResponse response) {

    // If the request is already managed, then bypassing the treatment of this method. This
    // avoids to override the context of subcalls of HTTP servlet methods.
    if (CacheServiceProvider.getRequestCacheService()
        .get(WebComponentRequestContext.class.getName()) == null) {

      // Getting the manager associated to the type of the given resource.
      WebComponentManager webComponentManager =
          managedWebComponentRouters.get(webComponentControllerClass.getName());

      // Initializing it (one time) if it is not already done.
      if (webComponentManager == null) {
        webComponentManager = initialize(webComponentControllerClass);
        managedWebComponentRouters.put(webComponentControllerClass.getName(), webComponentManager);
      }

      // Retrieving the class of the web context associated to the given resource.
      Class<WEB_COMPONENT_REQUEST_CONTEXT> webComponentContextClass = AnnotationUtil
          .searchParameterizedTypeFrom(WebComponentRequestContext.class,
              webComponentControllerClass);
      try {

        // Instantiating, and caching into the request, the web context.
        WEB_COMPONENT_REQUEST_CONTEXT webComponentContext = webComponentContextClass.newInstance();
        webComponentContext.setHttpMethodClass(httpMethodClass);
        webComponentContext.setRequest(request);
        webComponentContext.setResponse(response);
        CacheServiceProvider.getRequestCacheService()
            .put(WebComponentRequestContext.class.getName(), webComponentContext);
      } catch (Exception e) {
        throw new IllegalArgumentException(
            "Constructor (Class, HttpRequest, HttpServletResponse) doesn't exist...", e);
      }
    }
  }

  /**
   * Initializes all the route paths from the method analyzing of the given
   * webComponentControllerClass instance.
   * The webComponentControllerClass must provide methods with only one parameter :
   * WebRouteContext.
   * This methods can be annotated by :
   * <ul>
   * <li>{@link javax.ws.rs.GET}</li>
   * <li>{@link javax.ws.rs.POST}</li>
   * <li>{@link javax.ws.rs.PUT}</li>
   * <li>{@link javax.ws.rs.DELETE}</li>
   * <li>{@link javax.ws.rs.Path} (multiple)</li>
   * <li>{@link org.silverpeas.core.web.mvc.webcomponent.annotation.LowestRoleAccess} (at most
   * one)</li>
   * <li>{@link org.silverpeas.core.web.mvc.webcomponent.annotation.InvokeBefore} </li>
   * <li>{@link org.silverpeas.core.web.mvc.webcomponent.annotation.InvokeAfter} </li>
   * <li>{@link org.silverpeas.core.web.mvc.webcomponent.annotation.Invokable} </li>
   * </ul>
   * The webComponentControllerClass methods must also return a {@link com.stratelia.silverpeas
   * .peasCore.servlets.Navigation} instance or must be annotated by a @RedirectTo...
   * @param webComponentControllerClass the webComponentControllerClass which exposes the method
   * that will be invoked.
   * @param <C> the type of the WebComponentController which exposes the method that will be
   * invoked.
   * @return the manager initialized for the resource.
   */
  private static <C> WebComponentManager initialize(Class<C> webComponentControllerClass) {
    WebComponentManager webComponentManager = new WebComponentManager();
    List<org.silverpeas.core.web.mvc.webcomponent.Path> allRegisteredPaths = new ArrayList<>();

    // Scanning the methods of the resources
    for (Method resourceMethod : webComponentControllerClass.getMethods()) {

      // Taking into account only those that has one WebRouterContext parameter and no other one.
      // The method must return nothing.
      Class<?>[] parameterTypes = resourceMethod.getParameterTypes();
      if (parameterTypes.length == 1 &&
          WebComponentRequestContext.class.isAssignableFrom(parameterTypes[0])) {
        Set<Class<? extends Annotation>> httpMethods = new LinkedHashSet<>();
        Set<Path> paths = new LinkedHashSet<>();
        LowestRoleAccess lowestRoleAccess = null;
        boolean isDefaultPaths = false;
        Annotation redirectTo = null;
        Invokable invokable = null;
        InvokeBefore invokeBefore = null;
        InvokeAfter invokeAfter = null;
        NavigationStep navigationStep = null;
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
          } else if (annotation instanceof NavigationStep) {
            navigationStep = (NavigationStep) annotation;
          } else if (annotation instanceof LowestRoleAccess) {
            if (lowestRoleAccess != null) {
              throw new IllegalArgumentException(
                  "Only one greatest Silverpeas Role must be specified for method: " +
                      resourceMethod.getName()
              );
            }
            lowestRoleAccess = (LowestRoleAccess) annotation;
          } else if (annotation instanceof Homepage) {
            if (isDefaultPaths) {
              throw new IllegalArgumentException(
                  "The homepage method is already specified, error on method " +
                      resourceMethod.getName()
              );
            }
            isDefaultPaths = true;
          } else if (annotation instanceof RedirectTo ||
              annotation instanceof RedirectToInternal ||
              annotation instanceof RedirectToInternalJsp ||
              annotation instanceof RedirectToNavigationStep||
              annotation instanceof RedirectToPreviousNavigationStep) {
            if (redirectTo != null) {
              throw new IllegalArgumentException(
                  "One, and only one, redirection must be specified for method " +
                      resourceMethod.getName()
              );
            }
            redirectTo = annotation;
          } else if (annotation instanceof Invokable) {
            if (invokable != null) {
              throw new IllegalArgumentException(
                  "@Invokable must be specified one time for method " + resourceMethod.getName());
            }
            invokable = (Invokable) annotation;
          } else if (annotation instanceof InvokeBefore) {
            if (invokeBefore != null) {
              throw new IllegalArgumentException(
                  "@InvokeBefore must be specified one time for method " +
                      resourceMethod.getName());
            }
            invokeBefore = (InvokeBefore) annotation;
          } else if (annotation instanceof InvokeAfter) {
            if (invokeAfter != null) {
              throw new IllegalArgumentException(
                  "@InvokeAfter must be specified one time for method " + resourceMethod.getName());
            }
            invokeAfter = (InvokeAfter) annotation;
          }
        }

        if (!httpMethods.isEmpty()) {
          if (invokable != null) {
            throw new IllegalArgumentException(
                "Http Method " + resourceMethod.getName() + " can not be annoted with @Invokable");
          }

          if (redirectTo == null &&
              !resourceMethod.getReturnType().isAssignableFrom(Navigation.class)) {
            throw new IllegalArgumentException(resourceMethod.getName() +
                " method must return a Navigation instance or be annoted by one of @RedirectTo..." +
                " annotations");
          }

          if (redirectTo != null &&
              resourceMethod.getReturnType().isAssignableFrom(Navigation.class)) {
            throw new IllegalArgumentException(resourceMethod.getName() +
                " method must, either return a Navigation instance, either be annoted by one of " +
                "@RedirectTo... annotation");
          }

          for (Class<? extends Annotation> httpMethodClass : httpMethods) {
            HttpMethodPaths httpMethodPaths =
                webComponentManager.httpMethodPaths.get(httpMethodClass.getName());
            if (httpMethodPaths == null) {
              httpMethodPaths = new HttpMethodPaths(httpMethodClass);
              webComponentManager.httpMethodPaths.put(httpMethodClass.getName(), httpMethodPaths);
            }
            List<org.silverpeas.core.web.mvc.webcomponent.Path> registredPaths = httpMethodPaths
                .addPaths(paths, lowestRoleAccess, resourceMethod, navigationStep, redirectTo,
                    invokeBefore, invokeAfter);
            if (isDefaultPaths) {
              if (webComponentManager.defaultPath != null) {
                throw new IllegalArgumentException(
                    "@Homepage is specified on " + resourceMethod.getName() +
                        " method, but @Homepage has already been defined one another one"
                );
              }
              webComponentManager.defaultPath = registredPaths.get(0);
            }
            allRegisteredPaths.addAll(registredPaths);
          }
        }

        if (invokable != null) {
          if (webComponentManager.invokables.containsKey(invokable.value())) {
            throw new IllegalArgumentException(
                invokable.value() + " invokable identifier has already been set");
          }
          webComponentManager.invokables.put(invokable.value(), resourceMethod);
        }
      }
    }

    if (webComponentManager.defaultPath == null) {
      throw new IllegalArgumentException("The homepage method must be specified with @Homepage");
    }

    // Verifying method identifier invokation
    for (org.silverpeas.core.web.mvc.webcomponent.Path path : allRegisteredPaths) {
      for (String invokableIdentifier : path.getInvokeBeforeIdentifiers()) {
        if (!webComponentManager.invokables.containsKey(invokableIdentifier)) {
          throw new IllegalArgumentException("method behind '" + invokableIdentifier +
              "' invokable identifier must be performed before the execution of HTTP method " +
              path.getResourceMethod().getName() + ", but it is not registred");
        }
      }
      for (String invokableIdentifier : path.getInvokeAfterIdentifiers()) {
        if (!webComponentManager.invokables.containsKey(invokableIdentifier)) {
          throw new IllegalArgumentException("method behind '" + invokableIdentifier +
              "' invokable identifier must be performed after the execution of HTTP method " +
              path.getResourceMethod().getName() + ", but it is not registred");
        }
      }
    }

    return webComponentManager;
  }

  /**
   * Performs a request path by executing the right method behind and returning the navigation
   * object instance.
   * @param webComponentController the handled component controller.
   * @param path the path that must be matched in finding of the method to invoke.
   * @param <CONTROLLER> the type of the resource which hosts the method that must be invoked.
   * @param <WEB_COMPONENT_REQUEST_CONTEXT> the type of the web component context.
   * @return the resulting navigation.
   * @throws Exception
   */
  @SuppressWarnings({"unchecked", "ConstantConditions"})
  public static <CONTROLLER extends WebComponentController<WEB_COMPONENT_REQUEST_CONTEXT>,
      WEB_COMPONENT_REQUEST_CONTEXT extends WebComponentRequestContext> Navigation perform(
      CONTROLLER webComponentController, String path) throws Exception {

    // Retrieving the web component request context
    WEB_COMPONENT_REQUEST_CONTEXT webComponentRequestContext =
        (WEB_COMPONENT_REQUEST_CONTEXT) CacheServiceProvider.getRequestCacheService()
            .get(WebComponentRequestContext.class.getName());
    webComponentRequestContext.setController(webComponentController);

    // Just after the instantiation of the Web Controller
    if (!webComponentController.onCreationCalled) {
      webComponentController.onInstantiation(webComponentRequestContext);
      webComponentController.onCreationCalled = true;
    }

    // Common processing
    webComponentRequestContext.beforeRequestProcessing();
    webComponentController.beforeRequestProcessing(webComponentRequestContext);

    // Retrieving the web component manager
    WebComponentManager webComponentManager =
        managedWebComponentRouters.get(webComponentController.getClass().getName());

    // Exucuting the treatments associated to the path
    try {
      return webComponentManager
          .exucutePath(webComponentController, path, webComponentRequestContext);
    } catch (Exception e) {
      if (e instanceof WebApplicationException || (e instanceof InvocationTargetException &&
          ((InvocationTargetException) e)
              .getTargetException() instanceof WebApplicationException)) {
        webComponentRequestContext.getResponse().sendError(
            ((WebApplicationException) ((InvocationTargetException) e).getTargetException())
                .getResponse().getStatus()
        );
      }
      throw e;
    }
  }

  /**
   * Performs a request path by executing the right method behind and returning the navigation.
   * @param webComponentController the resource which exposes the method that will be invoked.
   * @param path the path that must be matched in finding of the method to invoke.
   * @param webComponentContext the context of the web component routing.
   * @param <CONTROLLER> the type of the resource which hosts the method that must be invoked.
   * @param <WEB_COMPONENT_REQUEST_CONTEXT> the type of the web component context.
   * @return the {@link Navigation} instance that
   * contains the destination.
   * @throws Exception
   */
  private <CONTROLLER extends WebComponentController<WEB_COMPONENT_REQUEST_CONTEXT>,
      WEB_COMPONENT_REQUEST_CONTEXT extends WebComponentRequestContext> Navigation exucutePath(
      CONTROLLER webComponentController, String path,
      WEB_COMPONENT_REQUEST_CONTEXT webComponentContext) throws Exception {
    org.silverpeas.core.web.mvc.webcomponent.Path pathToPerform = null;

    // If coming from a redirect, trying to find path to perform in these registred for GET HTTP
    // METHOD.
    HttpMethodPaths methodPaths = httpMethodPaths.get(
        webComponentContext.isComingFromRedirect() ? GET.class.getName() :
            webComponentContext.getHttpMethodClass().getName());
    if (methodPaths != null) {
      pathToPerform = methodPaths.findPath(path, true, webComponentContext);
      if (pathToPerform == null) {
        pathToPerform = methodPaths.findPath(path, false, webComponentContext);
      }
    }

    // If no path is found, then the default one is selected.
    if (pathToPerform == null) {
      webComponentContext.getMessager()
          .addError(webComponentContext.getMultilang().getString("GML.action.user.forbidden"));
      pathToPerform = defaultPath;
    }

    // Reset navigation context if necessary
    if (pathToPerform == defaultPath) {
      webComponentContext.getNavigationContext().clear();
    }

    // Has the user enough rights to access the aimed treatment?
    if (pathToPerform.getLowestRoleAccess() != null &&
        (webComponentContext.getGreaterUserRole() == null ||
            !webComponentContext.getGreaterUserRole()
                .isGreaterThanOrEquals(pathToPerform.getLowestRoleAccess().value()))) {
      RedirectTo redirectTo = pathToPerform.getLowestRoleAccess().onError();
      if (StringUtil.isNotDefined(redirectTo.value())) {
        // No redirection on access error
        webComponentContext.getResponse().sendError(HttpServletResponse.SC_FORBIDDEN);
        throw new AccessForbiddenException("WebRouteManager.executePath", SilverpeasException.ERROR,
            "User id " + webComponentContext.getUser().getId() + " has not right access to " +
                webComponentContext.getRequest().getRequestURI()
        );
      }
      // A redirection is asked on an error
      return webComponentContext.redirectTo(redirectTo);
    }

    // Invoking all methods before the execution of the HTTP method.
    for (String invokeBeforeId : pathToPerform.getInvokeBeforeIdentifiers()) {
      invokables.get(invokeBeforeId).invoke(webComponentController, webComponentContext);
    }

    // Calling finally the method behind the idenfied path.
    final Navigation navigation;
    if (pathToPerform.getResourceMethod().getReturnType().isAssignableFrom(Navigation.class)) {
      navigation = (Navigation) pathToPerform.getResourceMethod()
          .invoke(webComponentController, webComponentContext);
    } else {
      pathToPerform.getResourceMethod().invoke(webComponentController, webComponentContext);
      navigation = webComponentContext.redirectTo(pathToPerform.getRedirectTo());
    }

    // Invoking all methods after the execution of the HTTP method.
    for (String invokeAfterId : pathToPerform.getInvokeAfterIdentifiers()) {
      invokables.get(invokeAfterId).invoke(webComponentController, webComponentContext);
    }

    // Navigation navigation step
    NavigationContext navigationContext = webComponentContext.getNavigationContext();
    final NavigationStep navigationStepIdentifier = pathToPerform.getNavigationStep();
    if (navigationStepIdentifier != null) {
      NavigationContext.NavigationStep navigationStep =
          navigationContext.navigationStepFrom(navigationStepIdentifier.identifier());
      // updating the URI of the navigation step
      UriBuilder fullUriBuilder =
          UriBuilder.fromUri(webComponentContext.getComponentUriBase()).path(path);
      for (Map.Entry<String, String[]> entry : webComponentContext.getRequest().getParameterMap()
          .entrySet()) {
        fullUriBuilder.queryParam(entry.getKey(), entry.getValue());
      }
      navigationStep.withFullUri(fullUriBuilder.build().toString());
      navigationStep.withContextIdentifier(navigationStepIdentifier.contextIdentifier());
    } else {
      navigationContext.noNavigationStep();
    }

    // Setting the navigation context attribute
    webComponentContext.getRequest().setAttribute("navigationContext", navigationContext);

    // Returning the navigation
    return navigation;
  }
}
