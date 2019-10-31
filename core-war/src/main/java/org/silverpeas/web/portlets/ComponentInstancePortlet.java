/*
 * Copyright (C) 2000 - 2019 Silverpeas
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
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.silverpeas.web.portlets;

import org.silverpeas.core.admin.service.OrganizationControllerProvider;
import org.silverpeas.core.admin.user.model.UserDetail;
import org.silverpeas.core.util.StringUtil;
import org.silverpeas.core.util.URLUtil;
import org.silverpeas.core.web.portlets.FormNames;

import javax.portlet.*;
import java.io.IOException;

public class ComponentInstancePortlet extends GenericPortlet implements FormNames {

  private static final String PREFINSTANCEID = "instanceId";

  @Override
  public void doView(RenderRequest request, RenderResponse response)
      throws PortletException, IOException {
    PortletPreferences pref = request.getPreferences();
    String instanceId = pref.getValue(PREFINSTANCEID, "");

    if (OrganizationControllerProvider.getOrganisationController().isComponentAvailableToUser(instanceId,
        UserDetail.getCurrentRequester().getId())) {
      request.setAttribute("URL", URLUtil.getURL(null, null, instanceId) + "portlet");
    } else {
      // get portlet window name to hide it (client side)
      setPortletWindowName(request);
    }
    include(request, response, "portlet.jsp");
  }

  private void setPortletWindowName(RenderRequest request) {
    String windowId = request.getWindowID();
    if (StringUtil.isDefined(windowId)) {
      String portletWindowName = windowId.substring(windowId.lastIndexOf('|')+1);
      if (StringUtil.isDefined(portletWindowName)) {
        request.setAttribute("PortletWindowName", portletWindowName);
      }
    }
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

  /*
   * Process Action.
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
      processEditCancelAction(response);
    }
  }

  /*
   * Process the "cancel" action for the edit page.
   */
  private void processEditCancelAction(ActionResponse response) throws PortletException {
    response.setPortletMode(PortletMode.VIEW);
  }

  /*
   * Process the "finished" action for the edit page. Set the "url" to the value specified in the
   * edit page.
   */
  private void processEditFinishedAction(ActionRequest request, ActionResponse response) throws
      PortletException {
    String instanceId = request.getParameter(PREFINSTANCEID);

    // store preference
    PortletPreferences pref = request.getPreferences();
    try {
      pref.setValue(PREFINSTANCEID, instanceId);
      pref.store();
    } catch (Exception e) {
      getPortletContext().log("could not set instanceId", e);
      throw new PortletException("IFramePortlet.processEditFinishedAction", e);
    }
    response.setPortletMode(PortletMode.VIEW);
  }

  /**
   * Include a page.
   */
  private void include(RenderRequest request, RenderResponse response,
      String pageName) throws PortletException {
    response.setContentType(request.getResponseContentType());
    if (!StringUtil.isDefined(pageName)) {
      // assert
      throw new NullPointerException("null or empty page name");
    }
    try {
      PortletRequestDispatcher dispatcher = getPortletContext()
          .getRequestDispatcher("/portlets/jsp/componentInstance/" + pageName);
      dispatcher.include(request, response);
    } catch (IOException ioe) {
      throw new PortletException(ioe);
    }
  }
}