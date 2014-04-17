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

import org.silverpeas.cache.service.CacheServiceFactory;

import javax.ws.rs.core.UriBuilder;
import java.net.URI;

/**
 * This class permits to handle a context according to the user navigation.
 * @param <WEB_COMPONENT_REQUEST_CONTEXT> the type of the web component request context.
 * @author: Yohann Chastagnier
 */
public class NavigationContext<WEB_COMPONENT_REQUEST_CONTEXT extends WebComponentRequestContext> {

  /**
   * Sets the navigation context to the specified context associated to the current web controller.
   * @param context the context into which the navigation context must be set.
   */
  @SuppressWarnings("unchecked")
  public static <WEB_COMPONENT_REQUEST_CONTEXT extends WebComponentRequestContext>
  NavigationContext<WEB_COMPONENT_REQUEST_CONTEXT> get(
      WEB_COMPONENT_REQUEST_CONTEXT context) {
    String cacheKey = "NavigationContext@" + context.getComponentUriBase();
    NavigationContext<WEB_COMPONENT_REQUEST_CONTEXT> navigationContext =
        CacheServiceFactory.getSessionCacheService().get(cacheKey, NavigationContext.class);
    if (navigationContext == null) {
      navigationContext = new NavigationContext(context);
      CacheServiceFactory.getSessionCacheService().put(cacheKey, navigationContext);
    } else {
      navigationContext.webComponentRequestContext = context;
    }
    return navigationContext;
  }

  private final ViewPoint baseViewPoint;
  private WEB_COMPONENT_REQUEST_CONTEXT webComponentRequestContext;
  private ViewPoint currentViewPoint;
  private ViewPoint previousViewPoint;

  /**
   * Default hidden constructor.
   * @param webComponentRequestContext the context of the request associated to the current web
   * component controller.
   */
  NavigationContext(WEB_COMPONENT_REQUEST_CONTEXT webComponentRequestContext) {
    this.webComponentRequestContext = webComponentRequestContext;
    baseViewPoint = new ViewPoint(null).withSuffixUri("Main");
    currentViewPoint = baseViewPoint;
    previousViewPoint = baseViewPoint;
  }

  /**
   * Gets the context of the request associated to the current web component controller.
   * @return the above described context.
   */
  public WEB_COMPONENT_REQUEST_CONTEXT getWebComponentRequestContext() {
    return webComponentRequestContext;
  }

  public ViewPoint getPreviousViewPoint() {
    return previousViewPoint;
  }

  /**
   * Gets the current point of the navigation.
   * @return the above described point.
   */
  public ViewPoint getCurrentViewPoint() {
    return currentViewPoint;
  }

  /**
   * Gets the first point of the navigation. It represents the one of the homepage ("Main").
   * @return the above described point.
   */
  public ViewPoint getBaseViewPoint() {
    return baseViewPoint;
  }

  /**
   * If the view point is not registred, then it pushes into a linked list a new view point
   * identified by the specified view identifier.
   * Otherwise, it pushes nothing and reset last view point of the stack to the one associated to
   * the specified view identifier.
   * @param viewIdentifier the identifier of the view that will be associated to the point.
   * @return the {@link ViewPoint} instance related to the specified view identifier.
   */
  public ViewPoint viewPointFrom(String viewIdentifier) {
    ViewPoint current = findViewPointFrom(viewIdentifier);
    if (current != null) {
      currentViewPoint = current;
      current.withNext(null);
      previousViewPoint = current.getPrevious();
    } else {
      currentViewPoint.withNext(new ViewPoint(viewIdentifier));
      currentViewPoint.getNext().withPrevious(currentViewPoint);
      currentViewPoint = currentViewPoint.getNext();
      previousViewPoint = currentViewPoint.getPrevious();
    }
    getWebComponentRequestContext().markViewPointContextPerformed();
    return currentViewPoint;
  }

  /**
   * Indicates that a no view point is defined for the current request processing.
   */
  public void noViewPoint() {
    if (!getWebComponentRequestContext().isViewPointContextPerformed()) {
      previousViewPoint = currentViewPoint;
    }
  }

  /**
   * Clears the navigation context and reset it to the base one.
   * @return result of {@link #getBaseViewPoint()}
   */
  public ViewPoint clear() {
    baseViewPoint.withNext(null);
    currentViewPoint = baseViewPoint;
    previousViewPoint = baseViewPoint;
    return currentViewPoint;
  }

  /**
   * Finds an existing view point from the specified view identifier.
   * @param viewIdentifier the view identifier that aimed the requested view point.
   * @return the {@link ViewPoint} instance if any, null otherwise.
   */
  ViewPoint findViewPointFrom(String viewIdentifier) {
    ViewPoint current = getBaseViewPoint().getNext();
    while (current != null) {
      if (current.getViewIdentifier().equals(viewIdentifier)) {
        break;
      }
      current = current.getNext();
    }
    return current;
  }

  /**
   * Class that represents the data of a point of a navigation.
   */
  public class ViewPoint {
    private final String viewIdentifier;
    private String viewContextIdentifier;
    private ViewPoint previous;
    private ViewPoint next;
    private URI uri = URI.create("");
    private String label;

    private ViewPoint(String viewIdentifier) {
      this.viewIdentifier = viewIdentifier;
    }

    public String getViewIdentifier() {
      return viewIdentifier;
    }

    public String getViewContextIdentifier() {
      return viewContextIdentifier;
    }

    public ViewPoint withViewContext(final String viewContext) {
      this.viewContextIdentifier = viewContext;
      return this;
    }

    public String getLabel() {
      return label;
    }

    public ViewPoint withLabel(final String label) {
      this.label = label;
      return this;
    }

    public URI getUri() {
      return uri;
    }

    public ViewPoint withFullUri(final String fullUri) {
      this.uri = URI.create(fullUri);
      return this;
    }

    public ViewPoint withSuffixUri(final String suffixUri) {
      this.uri =
          UriBuilder.fromUri(webComponentRequestContext.getComponentUriBase()).path(suffixUri)
              .build();
      return this;
    }


    public ViewPoint getPrevious() {
      return previous;
    }

    ViewPoint withPrevious(final ViewPoint previous) {
      this.previous = previous;
      return this;
    }

    public ViewPoint getNext() {
      return next;
    }

    ViewPoint withNext(final ViewPoint next) {
      this.next = next;
      return this;
    }
  }
}
