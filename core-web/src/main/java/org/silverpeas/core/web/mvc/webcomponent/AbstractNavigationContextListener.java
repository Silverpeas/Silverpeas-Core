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

import javax.servlet.http.HttpSession;

/**
 * @author: Yohann Chastagnier
 */
public abstract class AbstractNavigationContextListener<WEB_COMPONENT_REQUEST_CONTEXT extends
    WebComponentRequestContext>
    implements NavigationContextListener<WEB_COMPONENT_REQUEST_CONTEXT> {

  @Override
  public void navigationContextCleared(
      final NavigationContext<WEB_COMPONENT_REQUEST_CONTEXT> navigationContext) {
  }

  @Override
  public void navigationStepCreated(
      final NavigationContext<WEB_COMPONENT_REQUEST_CONTEXT> navigationContext) {
  }

  @Override
  public void navigationStepReset(
      final NavigationContext<WEB_COMPONENT_REQUEST_CONTEXT> navigationContext) {
  }

  @Override
  public void noNavigationStepPerformed(
      final NavigationContext<WEB_COMPONENT_REQUEST_CONTEXT> navigationContext) {
  }

  @Override
  public void navigationStepTrashed(
      final NavigationContext<WEB_COMPONENT_REQUEST_CONTEXT>.NavigationStep trashedNavigationStep) {
  }

  @Override
  public void navigationStepContextIdentifierSet(
      final NavigationContext<WEB_COMPONENT_REQUEST_CONTEXT>.NavigationStep navigationStep,
      final String oldContextIdentifier) {
  }

  @Override
  public void navigationStepLabelSet(
      final NavigationContext<WEB_COMPONENT_REQUEST_CONTEXT>.NavigationStep navigationStep,
      final String oldLabel) {
  }

  /**
   * Sets an attribute into the session.
   * @param context
   * @param name
   * @param value
   */
  protected void setSessionAttribute(WEB_COMPONENT_REQUEST_CONTEXT context, String name,
      Object value) {
    HttpSession session = context.getRequest().getSession(false);
    session.setAttribute(name, value);
  }
}
