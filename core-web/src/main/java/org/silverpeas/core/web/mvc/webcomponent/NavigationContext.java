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

import org.silverpeas.core.web.mvc.webcomponent.annotation.RedirectToNavigationStep;
import org.silverpeas.core.web.mvc.webcomponent.annotation.RedirectToPreviousNavigationStep;
import org.silverpeas.core.cache.service.CacheServiceProvider;
import org.silverpeas.core.web.util.viewgenerator.html.browsebars.BrowseBarTag;

import javax.ws.rs.core.UriBuilder;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

/**
 * This class permits to handle a context according to the user navigation.
 * <p/>
 * The navigation context can be see as a stack of {@link NavigationStep} elements. Each time a
 * new step is defined, it is pushed into the stack. If the step already exists from the stack,
 * then the navigation context is reset to this. The usual needs of the use of a navigation context
 * for a developer is to track the different steps of the user's Web navigation in Silverpeas in
 * order to be able to go back to a previous step (generally a Web page) whatever it is. It is
 * useful when it exists several navigation paths to a given Web page and from which the user can
 * be go back to one of its previous steps of its navigation.
 * <p/>
 * The creation of navigation steps must be defined manually in web controllers by the programmer.
 * For doing, it can use the following tools:
 * <ul>
 *   <li>{@link org.silverpeas.core.web.mvc.webcomponent.annotation.NavigationStep}: it permits
 *   to define a navigation step identifier and optionally a context identifier</li>
 *   <li>{@link WebComponentRequestContext#getNavigationContext()}: provides methods that permits
 *   to the user to set manually a navigation step without defining navigation step annotations</li>
 *   <li>{@link RedirectToPreviousNavigationStep}: when defined the user is redirected to the
 *   previous navigation step from the navigation stack</li>
 *   <li>{@link RedirectToNavigationStep}: when defined the user is redirected to the
 *   navigation step from the navigation stack that is identifier by {@link
 *   RedirectToNavigationStep#value()}</li>
 * </ul>
 * The navigation context can be specified to the path attribute of TAG <view:browseBar .../>.
 * In that case, the complete stack of navigation steps is browsed to generate browse bar element
 * parts. {@link NavigationContext.NavigationStep} element with no label defined is ignored in
 * this generation treatment.
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
        CacheServiceProvider.getSessionCacheService().get(cacheKey, NavigationContext.class);
    if (navigationContext == null) {
      navigationContext = new NavigationContext<WEB_COMPONENT_REQUEST_CONTEXT>(context);
      CacheServiceProvider.getSessionCacheService().put(cacheKey, navigationContext);
    } else {
      navigationContext.webComponentRequestContext = context;
    }
    return navigationContext;
  }

  private final NavigationStep baseNavigationStep;
  private WEB_COMPONENT_REQUEST_CONTEXT webComponentRequestContext;
  private NavigationStep currentNavigationStep;
  private NavigationStep previousNavigationStep;
  private final List<NavigationContextListener> listeners =
      new ArrayList<NavigationContextListener>();

  /**
   * Default hidden constructor.
   * @param webComponentRequestContext the context of the request associated to the current web
   * component controller.
   */
  NavigationContext(WEB_COMPONENT_REQUEST_CONTEXT webComponentRequestContext) {
    this.webComponentRequestContext = webComponentRequestContext;
    baseNavigationStep = new NavigationStep(null).withSuffixUri("Main");
    currentNavigationStep = baseNavigationStep;
    previousNavigationStep = baseNavigationStep;
  }

  /**
   * Adds a listener to trigger.
   * @param listener a listener.
   */
  public void addListener(NavigationContextListener listener) {
    listeners.add(listener);
  }

  /**
   * Gets the context of the request associated to the current web component controller.
   * @return the above described context.
   */
  public WEB_COMPONENT_REQUEST_CONTEXT getWebComponentRequestContext() {
    return webComponentRequestContext;
  }

  /**
   * Gets in any cases of navigation the right previous {@link NavigationContext.NavigationStep}
   * instance.
   * <p/>
   * If user has just performed a web treatment that resulting to a navigation step creation or
   * reset, then the returned navigation step is the previous of the one created or reset.
   * <p/>
   * If user has performed a web treatment that not resulting to a navigation step creation or
   * reset, then the previous navigation step returned is the last created or reset.
   * @return the right previous {@link NavigationContext.NavigationStep} as above described.
   */
  public NavigationStep getPreviousNavigationStep() {
    return previousNavigationStep;
  }

  /**
   * Gets the current step of the navigation. It is the last created or reset one.
   * @return the above described navigation step.
   */
  public NavigationStep getCurrentNavigationStep() {
    return currentNavigationStep;
  }

  /**
   * Gets the first step of the navigation. It represents the one of the homepage ("Main").
   * @return the above described navigation step.
   */
  public NavigationStep getBaseNavigationStep() {
    return baseNavigationStep;
  }

  /**
   * Method to specify a navigation step creation/reset on a HTTP method of a {@link
   * WebComponentController} without using the
   * {@link org.silverpeas.core.web.mvc.webcomponent.annotation.NavigationStep} annotation.
   * <p/>
   * When a HTTP method with this annotation is called, one of the following internal treatment is
   * performed:
   * <ul>
   *   <li>if no navigation step is referenced by specified identifier,
   *   then a navigation step is created and referenced with it</li>
   *   <li>if a navigation step with specified identifier already exists,
   *   then the navigation step stack is reset to this</li>
   * </ul>
   * Then, in any cases, the following internal treatments are performed:
   * <ul>
   *   <li>{@link NavigationContext.NavigationStep#withFullUri(String)}: the current requested path
   *   URI (with URL parameters) is set</li>
   *   <li>{@link NavigationContextListener} necessary methods are triggered</li>
   * </ul>
   * @param stepIdentifier the identifier of a navigation step.
   * @return the {@link NavigationStep} instance related to the specified identifier.
   */
  @SuppressWarnings("unchecked")
  public NavigationStep navigationStepFrom(String stepIdentifier) {
    NavigationStep current = findNavigationStepFrom(stepIdentifier);
    if (current != null) {
      trashAfter(current);
      currentNavigationStep = current;
      previousNavigationStep = current.getPrevious();
      for (NavigationContextListener listener : listeners) {
        listener.navigationStepReset(this);
      }
    } else {
      currentNavigationStep.withNext(new NavigationStep(stepIdentifier));
      currentNavigationStep.getNext().withPrevious(currentNavigationStep);
      currentNavigationStep = currentNavigationStep.getNext();
      previousNavigationStep = currentNavigationStep.getPrevious();
      for (NavigationContextListener listener : listeners) {
        listener.navigationStepCreated(this);
      }
    }
    getWebComponentRequestContext().markNavigationStepContextPerformed();
    return currentNavigationStep;
  }

  /**
   * When no {@link org.silverpeas.core.web.mvc.webcomponent.annotation.NavigationStep} is not
   * specified to a called HTTP Web Controller method, then the mechanism calls this method.
   * <p/>
   * It takes into account the case that a navigation step is created or reset by using
   * directly the {@link #navigationStepFrom(String)} method from the Web Controller.
   * <p/>
   * The aim of this method is to set the right navigation step returned by {@link
   * #getPreviousNavigationStep()} method.
   */
  @SuppressWarnings("unchecked")
  void noNavigationStep() {
    if (!getWebComponentRequestContext().isNavigationStepContextPerformed()) {
      previousNavigationStep = currentNavigationStep;
      for (NavigationContextListener listener : listeners) {
        listener.noNavigationStepPerformed(this);
      }
    }
  }

  /**
   * Clears the navigation context and reset it to the base one.
   * @return result of {@link #getBaseNavigationStep()}
   */
  @SuppressWarnings("unchecked")
  public NavigationStep clear() {
    trashAfter(baseNavigationStep);
    currentNavigationStep = baseNavigationStep;
    previousNavigationStep = baseNavigationStep;
    for (NavigationContextListener listener : listeners) {
      listener.navigationContextCleared(this);
    }
    return currentNavigationStep;
  }

  /**
   * Trashes all navigation steps after the specified one.
   * @param navigationStep the navigation step that must be the current.
   */
  private void trashAfter(NavigationStep navigationStep) {
    NavigationStep toTrash = navigationStep.getNext();
    navigationStep.withNext(null);
    while (toTrash != null) {
      toTrash.withPrevious(null);
      for (NavigationContextListener listener : listeners) {
        listener.navigationStepTrashed(toTrash);
      }
      toTrash = toTrash.getNext();
    }
  }

  /**
   * Finds an existing navigation step from the stack by the specified identifier.
   * @param stepIdentifier the identifier that references the requested navigation step in the
   * stack.
   * @return the {@link NavigationContext.NavigationStep} instance if any, null otherwise.
   */
  NavigationStep findNavigationStepFrom(String stepIdentifier) {
    NavigationStep current = getBaseNavigationStep().getNext();
    while (current != null) {
      if (current.getIdentifier().equals(stepIdentifier)) {
        break;
      }
      current = current.getNext();
    }
    return current;
  }

  /**
   * Class that represents the data of a navigation step.
   * This class is used for automatically set informatons to a browse bar element.
   */
  public class NavigationStep {
    private final String identifier;
    private String contextIdentifier;
    private NavigationStep previous;
    private NavigationStep next;
    private URI uri = URI.create("");
    private boolean uriMustBeUsedByBrowseBar = true;
    private String label;

    /**
     * Default constructor.
     * @param identifier the identifier of the navigation step.
     */
    private NavigationStep(String identifier) {
      this.identifier = identifier;
    }

    /**
     * Gets the navigation context the navigation step is associated to.
     * @return
     */
    public NavigationContext<WEB_COMPONENT_REQUEST_CONTEXT> getNavigationContext() {
      return NavigationContext.this;
    }

    /**
     * Gets the identifier of the navigation step.
     * @return the above described identifier.
     */
    public String getIdentifier() {
      return identifier;
    }

    /**
     * Gets the identifier of a context associated to the navigation step.
     * @return the above described identifier.
     */
    public String getContextIdentifier() {
      return contextIdentifier;
    }

    /**
     * Sets the identifier of a context associated to the navigation step.
     * @param contextIdentifier a context identifier.
     * @return itself.
     */
    public NavigationStep withContextIdentifier(final String contextIdentifier) {
      String oldContextIdentifier = this.contextIdentifier;
      this.contextIdentifier = contextIdentifier;
      for (NavigationContextListener listener : listeners) {
        listener.navigationStepContextIdentifierSet(this, oldContextIdentifier);
      }
      return this;
    }

    /**
     * Gets a functional label of the navigation step. It is especially used by {@link
     * BrowseBarTag} tag.
     * @return the above described label.
     */
    public String getLabel() {
      return label;
    }

    /**
     * Sets a functional label of the navigation step. It is especially used by {@link
     * BrowseBarTag} tag.
     * @param label a label.
     * @return itself.
     */
    public NavigationStep withLabel(final String label) {
      String oldLabel = this.label;
      this.label = label;
      for (NavigationContextListener listener : listeners) {
        listener.navigationStepLabelSet(this, oldLabel);
      }
      return this;
    }

    /**
     * Gets the URI associated to the navigation step. It is normally the URI performed when the
     * navigation step has been created or reset. But in some cases, it has been modified
     * manually from Web Controller.
     * @return the above described URI.
     */
    public URI getUri() {
      return uri;
    }

    /**
     * Sets the URI associated to the navigation step.
     * @param fullUri the URI to set.
     * @return itself.
     */
    public NavigationStep withFullUri(final String fullUri) {
      this.uri = URI.create(fullUri);
      return this;
    }

    /**
     * Sets the URI associated to the navigation step.
     * @param suffixUri the suffic URI that will be added to the prefix composed with
     * {@link WebComponentRequestContext#getComponentUriBase()} information.
     * @return itself.
     */
    public NavigationStep withSuffixUri(final String suffixUri) {
      this.uri =
          UriBuilder.fromUri(webComponentRequestContext.getComponentUriBase()).path(suffixUri)
              .build();
      return this;
    }

    /**
     * Gets the previous navigation step.
     * @return the previous navigation step if any, null otherwise.
     */
    public NavigationStep getPrevious() {
      return previous;
    }

    /**
     * Sets the previous navigation step.
     * @param previous the previous navigation step to set.
     * @return itself.
     */
    NavigationStep withPrevious(final NavigationStep previous) {
      this.previous = previous;
      return this;
    }

    /**
     * Gets the next navigation step.
     * @return the next navigation step if any, null otherwise.
     */
    public NavigationStep getNext() {
      return next;
    }

    /**
     * Sets the next navigation step.
     * @param next the next navigation step to set.
     * @return itself.
     */
    NavigationStep withNext(final NavigationStep next) {
      this.next = next;
      return this;
    }

    /**
     * Indicates if the URI of the navigation step must be used by the browsbar mechanism.
     * True by default.
     * @return true if must be used, false otherwise.
     */
    public boolean isUriMustBeUsedByBrowseBar() {
      return uriMustBeUsedByBrowseBar;
    }

    /**
     * Sets if the URI of the navigation step must be used by the browsbar mechanism.
     * True by default.
     * @param uriMustBeUsedByBrowseBar true if must be used, false otherwise.
     * @return itself.
     */
    public NavigationStep setUriMustBeUsedByBrowseBar(final boolean uriMustBeUsedByBrowseBar) {
      this.uriMustBeUsedByBrowseBar = uriMustBeUsedByBrowseBar;
      return this;
    }
  }
}
