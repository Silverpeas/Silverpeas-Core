/**
 * Copyright (C) 2000 - 2012 Silverpeas
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
 * "http://www.silverpeas.org/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.silverpeas.portlets;

import java.io.IOException;
import java.util.ArrayList;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.GenericPortlet;
import javax.portlet.PortletException;
import javax.portlet.PortletMode;
import javax.portlet.PortletRequestDispatcher;
import javax.portlet.PortletSession;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;

import com.silverpeas.util.StringUtil;
import com.stratelia.silverpeas.notificationserver.channel.silvermail.SILVERMAILMessage;
import com.stratelia.silverpeas.notificationserver.channel.silvermail.SILVERMAILUtil;
import com.stratelia.silverpeas.peasCore.MainSessionController;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import static com.stratelia.webactiv.util.viewGenerator.html.arrayPanes.ArrayPane.*;
import java.util.Collection;

public class MyNotificationsPortlet extends GenericPortlet implements FormNames {

  @Override
  public void doView(RenderRequest request, RenderResponse response) throws PortletException,
      IOException {
    PortletSession session = request.getPortletSession();
    MainSessionController m_MainSessionCtrl = (MainSessionController) session.getAttribute(
        MainSessionController.MAIN_SESSION_CONTROLLER_ATT, PortletSession.APPLICATION_SCOPE);
    SILVERMAILUtil silvermailUtil = new SILVERMAILUtil(m_MainSessionCtrl.getUserId());
    Collection<SILVERMAILMessage> messages = new ArrayList<SILVERMAILMessage>();
    try {
      messages = silvermailUtil.getFolderMessageList("INBOX");
    } catch (Exception e) {
      SilverTrace.error("portlet", "MyNotificationsPortlet", "portlet.ERROR", e);
    }
    request.setAttribute("Messages", messages.iterator());
    include(request, response, "portlet.jsp");
  }

  @Override
  public void doEdit(RenderRequest request, RenderResponse response) throws PortletException {
    include(request, response, "edit.jsp");
  }

  @Override
  public void doHelp(RenderRequest request, RenderResponse response) throws PortletException {
    include(request, response, "help.jsp");
  }

  @Override
  public void processAction(ActionRequest request, ActionResponse response) throws PortletException {
    response.setRenderParameter(ACTION_PARAMETER_NAME, request.getParameter(ACTION_PARAMETER_NAME));
    response.setRenderParameter(COLUMN_PARAMETER_NAME, request.getParameter(COLUMN_PARAMETER_NAME));
    response.setRenderParameter(INDEX_PARAMETER_NAME, request.getParameter(INDEX_PARAMETER_NAME));
    response.setRenderParameter(TARGET_PARAMETER_NAME, request.getParameter(TARGET_PARAMETER_NAME));
    response.setPortletMode(PortletMode.VIEW);
  }

  /** Include a page. */
  private void include(RenderRequest request, RenderResponse response, String pageName) throws
      PortletException {
    response.setContentType(request.getResponseContentType());
    if (!StringUtil.isDefined(pageName)) {
      throw new NullPointerException("null or empty page name");
    }
    try {
      PortletRequestDispatcher dispatcher = getPortletContext().getRequestDispatcher(
          "/portlets/jsp/myNotifications/" + pageName);
      dispatcher.include(request, response);
    } catch (IOException ioe) {
      throw new PortletException(ioe);
    }
  }
}
