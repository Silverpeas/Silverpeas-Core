/*
 * Copyright (C) 2000 - 2013 Silverpeas
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

package org.silverpeas.web.portlets;

import org.silverpeas.core.web.portlets.FormNames;
import org.silverpeas.core.admin.user.model.UserDetail;
import org.silverpeas.core.util.StringUtil;

import javax.portlet.*;
import java.io.IOException;

public class SpaceResponsiblesPortlet extends GenericPortlet implements FormNames {

  @Override
  public void doView(RenderRequest request, RenderResponse response)
      throws PortletException, IOException {
    PortletSession session = request.getPortletSession();
    String spaceId = (String) session
        .getAttribute("Silverpeas_Portlet_SpaceId", PortletSession.APPLICATION_SCOPE);

    request.setAttribute("currentUserId", UserDetail.getCurrentRequester().getId());
    request.setAttribute("spaceId", spaceId);

    PortletPreferences pref = request.getPreferences();
    if ("checked".equalsIgnoreCase(pref.getValue("displayOnlySpaceManagers",""))) {
      request.setAttribute("displayOnlySpaceManagers", "true");
    } else {
      request.setAttribute("displayOnlySpaceManagers", "false");
    }

    include(request, response, "portlet.jsp");
  }

  @Override
  public void doEdit(RenderRequest request, RenderResponse response) throws PortletException {
    include(request, response, "edit.jsp");
  }

  /**
   * Include "help" JSP.
   */
  @Override
  public void doHelp(RenderRequest request, RenderResponse response) throws PortletException {
    include(request, response, "help.jsp");
  }

  /**
   * Include a page.
   */
  private void include(RenderRequest request, RenderResponse response, String pageName)
      throws PortletException {
    response.setContentType(request.getResponseContentType());
    if (!StringUtil.isDefined(pageName)) {
      // assert
      throw new NullPointerException("null or empty page name");
    }
    try {
      PortletRequestDispatcher dispatcher =
          getPortletContext().getRequestDispatcher("/portlets/jsp/spaceResponsibles/" + pageName);
      dispatcher.include(request, response);
    } catch (IOException ioe) {
      throw new PortletException(ioe);
    }
  }

  /*
   * Process Action.
   */
  @Override
  public void processAction(ActionRequest request, ActionResponse response)
      throws PortletException {
    if (request.getParameter(SUBMIT_FINISHED) != null) {
      processEditFinishedAction(request, response);
    } else if (request.getParameter(SUBMIT_CANCEL) != null) {
      processEditCancelAction(request, response);
    }
  }

  /*
   * Process the "cancel" action for the edit page.
   */
  private void processEditCancelAction(ActionRequest request,
      ActionResponse response) throws PortletException {
    response.setPortletMode(PortletMode.VIEW);
  }

  /*
   * Process the "finished" action for the edit page. Set the "url" to the value specified in the
   * edit page.
   */
  private void processEditFinishedAction(ActionRequest request, ActionResponse response)
      throws PortletException {

    String displayOnlySpaceManagers = request.getParameter("displayOnlySpaceManagers");

    // store preference
    PortletPreferences pref = request.getPreferences();
    try {
      pref.setValue("displayOnlySpaceManagers", displayOnlySpaceManagers);
      pref.store();
    } catch (IOException ioe) {
      log("could not set displayOnlySpaceManagers parameter", ioe);
      throw new PortletException("SpaceResponsiblesPortlet.processEditFinishedAction", ioe);
    }
    response.setPortletMode(PortletMode.VIEW);
  }

  private void log(String message, Exception ex) {
    getPortletContext().log(message, ex);
  }
}
