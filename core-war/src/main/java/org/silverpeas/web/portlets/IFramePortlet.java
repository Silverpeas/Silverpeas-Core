/*
 * Copyright (C) 2000 - 2024 Silverpeas
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

import org.silverpeas.kernel.util.StringUtil;

import javax.portlet.*;
import java.io.IOException;

public class IFramePortlet extends SilverpeasEditablePortlet {

  @Override
  public void doView(RenderRequest request, RenderResponse response)
      throws PortletException, IOException {
    include(request, response, "portlet.jsp");
  }

  @Override
  public void doEdit(RenderRequest request, RenderResponse response)
      throws PortletException {
    include(request, response, "edit.jsp");
  }

  /** Include "help" JSP. */
  @Override
  public void doHelp(RenderRequest request, RenderResponse response)
      throws PortletException {
    include(request, response, "help.jsp");
  }

  /*
   * Process the "cancel" action for the edit page.
   */
  @Override
  protected void processEditCancelAction(ActionRequest request, ActionResponse response)
      throws PortletException {
    response.setPortletMode(PortletMode.VIEW);
  }

  @Override
  protected void processEditFinishedAction(ActionRequest request, ActionResponse response)
      throws PortletException {
    String url = request.getParameter("url");

    // Check if it is a number
    // store preference
    PortletPreferences pref = request.getPreferences();
    try {
      pref.setValue("url", url);
      pref.store();
    } catch (ValidatorException | IOException ve) {
      getPortletContext().log("could not set url", ve);
      throw new PortletException("IFramePortlet.processEditFinishedAction", ve);
    }
    response.setPortletMode(PortletMode.VIEW);
  }

  /** Include a page. */
  private void include(RenderRequest request, RenderResponse response,
      String pageName) throws PortletException {
    response.setContentType(request.getResponseContentType());
    if (!StringUtil.isDefined(pageName)) {
      // assert
      throw new NullPointerException("null or empty page name");
    }
    try {
      PortletRequestDispatcher dispatcher = getPortletContext()
          .getRequestDispatcher("/portlets/jsp/iframe/" + pageName);
      dispatcher.include(request, response);
    } catch (IOException ioe) {
      throw new PortletException(ioe);
    }
  }
}
