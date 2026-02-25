/*
 * Copyright (C) 2000 - 2025 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have received a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "https://www.silverpeas.org/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package org.silverpeas.web.portlets;

import org.silverpeas.core.web.portlets.FormNames;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.GenericPortlet;
import javax.portlet.PortletException;

/**
 * A portlet in Silverpeas that is editable by the user. It provides common behaviors for such
 * portlets deployed in Silverpeas.
 *
 * @author mmoquillon
 */
public abstract class SilverpeasEditablePortlet extends GenericPortlet implements FormNames {

  /**
   * Called by the portlet container to allow the portlet to process an action request. This method
   * is called if the client request was originated by a URL created (by the portlet) with the
   * <code>RenderResponse.createActionURL()</code> method.
   * <p>
   * This method answers to the edition ending action: either at the edition validation or at the
   * edition cancellation request.
   *
   * @param request the action request
   * @param response the action response
   * @throws PortletException if the portlet cannot fulfill the request
   */
  @Override
  public void processAction(ActionRequest request, ActionResponse response)
      throws PortletException {
    if (request.getParameter(SUBMIT_FINISHED) != null) {
      //
      // handle "finished" button on edit page
      // return to view mode
      //
      processEditFinishedAction(request, response);
    } else if (request.getParameter(SUBMIT_CANCEL) != null) {
      //
      // handle "cancel" button on edit page
      // return to view mode
      //
      processEditCancelAction(request, response);
    }
  }

  /**
   * Process the "cancel" action for the edit page. The method is called by the
   * {@link SilverpeasEditablePortlet#processAction(ActionRequest, ActionResponse)} method.
   *
   * @param request the action request.
   * @param response the action response.
   * @throws PortletException if an error occurs while processing the action.
   */
  protected abstract void processEditCancelAction(ActionRequest request, ActionResponse response)
      throws PortletException;

  /**
   * Process the "finished" action for the edit page. Set the "url" to the value specified in the
   * edit page. The method is called by the
   * {@link SilverpeasEditablePortlet#processAction(ActionRequest, ActionResponse)} method.
   *
   * @param request the action request.
   * @param response the action response.
   * @throws PortletException if an error occurs while processing the action.
   */
  protected abstract void processEditFinishedAction(ActionRequest request,
      ActionResponse response) throws PortletException;
}
  