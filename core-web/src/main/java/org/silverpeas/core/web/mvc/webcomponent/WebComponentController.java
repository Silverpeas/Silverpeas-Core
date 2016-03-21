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

import org.silverpeas.core.web.mvc.controller.AbstractComponentSessionController;
import org.silverpeas.core.web.mvc.controller.ComponentContext;
import org.silverpeas.core.web.mvc.controller.MainSessionController;

/**
 * Base class for all web component controller.
 * Each implementation must be specified in web component servlet declaration into the linked
 * web.xml.
 * @param <WEB_COMPONENT_REQUEST_CONTEXT>
 * @author Yohann Chastagnier
 */
public abstract class WebComponentController<WEB_COMPONENT_REQUEST_CONTEXT extends
    WebComponentRequestContext>
    extends AbstractComponentSessionController {

  boolean onCreationCalled = false;

  public WebComponentController(final MainSessionController controller, final String spaceId,
      final String componentId) {
    super(controller, spaceId, componentId);
  }

  public WebComponentController(final MainSessionController controller,
      final ComponentContext context) {
    super(controller, context);
  }

  public WebComponentController(final MainSessionController controller,
      final ComponentContext context, final String resourceFileName) {
    super(controller, context, resourceFileName);
  }

  public WebComponentController(final MainSessionController controller,
      final ComponentContext context, final String multilangFileName, final String iconFileName) {
    super(controller, context, multilangFileName, iconFileName);
  }

  public WebComponentController(final MainSessionController controller,
      final ComponentContext context, final String multilangFileName, final String iconFileName,
      final String settingsFileName) {
    super(controller, context, multilangFileName, iconFileName, settingsFileName);
  }

  /**
   * This method is called one times just after the web controller is instantiated and just before
   * the call of the HTTP web controller method.
   * @param context the web request context.
   */
  protected abstract void onInstantiation(final WEB_COMPONENT_REQUEST_CONTEXT context);

  /**
   * Permits to perform some common initializations. The method is called just before the method
   * behing the identified path is invoked.
   * @param context the context of the request in relation with the web controller
   */
  protected void beforeRequestProcessing(WEB_COMPONENT_REQUEST_CONTEXT context) {
    context.getRequest().setAttribute("currentUser", context.getUser());
    context.getRequest().setAttribute("componentUriBase", context.getComponentUriBase());
    context.getRequest().setAttribute("greaterUserRole", context.getGreaterUserRole());
  }
}
