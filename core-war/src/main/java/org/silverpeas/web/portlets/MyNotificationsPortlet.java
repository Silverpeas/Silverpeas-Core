/*
 * Copyright (C) 2000 - 2022 Silverpeas
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

import org.apache.commons.lang3.tuple.Pair;
import org.silverpeas.core.admin.PaginationPage;
import org.silverpeas.core.admin.user.model.UserDetail;
import org.silverpeas.core.notification.user.server.channel.silvermail.SILVERMAILMessage;
import org.silverpeas.core.notification.user.server.channel.silvermail.SILVERMAILPersistence;
import org.silverpeas.core.notification.user.server.channel.silvermail.SilvermailCriteria.QUERY_ORDER_BY;
import org.silverpeas.core.util.StringUtil;
import org.silverpeas.core.util.logging.SilverLogger;
import org.silverpeas.core.web.portlets.FormNames;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.GenericPortlet;
import javax.portlet.PortletException;
import javax.portlet.PortletMode;
import javax.portlet.PortletRequestDispatcher;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import static org.silverpeas.core.notification.user.server.channel.silvermail.SilvermailCriteria.QUERY_ORDER_BY.*;
import static org.silverpeas.core.web.util.viewgenerator.html.arraypanes.ArrayPane.*;
import static org.silverpeas.core.web.util.viewgenerator.html.pagination.Pagination.INDEX_PARAMETER_NAME;
import static org.silverpeas.core.web.util.viewgenerator.html.pagination.Pagination.ITEMS_PER_PAGE_PARAM;

public class MyNotificationsPortlet extends GenericPortlet implements FormNames {

  private static final Map<Integer, Pair<QUERY_ORDER_BY,QUERY_ORDER_BY>> PORTLET_ORDER_BIES;
  private static final int RECEPTION_DATE_INDEX = 1;
  private static final int SUBJECT_INDEX = 3;
  private static final int FROM_INDEX = 4;
  private static final int SOURCE_INDEX = 5;

  @Override
  public void doView(RenderRequest request, RenderResponse response) throws PortletException,
      IOException {
    String userId = UserDetail.getCurrentRequester().getId();
    Collection<SILVERMAILMessage> messages = new ArrayList<>();
    try {
      messages = SILVERMAILPersistence
          .getMessageOfFolder(userId, "INBOX", getPaginationPage(request), getOrderBy(request));
    } catch (Exception e) {
      SilverLogger.getLogger(this).error(e);
    }
    request.setAttribute("Messages", messages);
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
    response.setRenderParameter(ITEMS_PER_PAGE_PARAM, request.getParameter(ITEMS_PER_PAGE_PARAM));
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

  private PaginationPage getPaginationPage(RenderRequest request) {
    return getPaginationPageFrom(request, "userNotificationPortlet");
  }

  private QUERY_ORDER_BY getOrderBy(RenderRequest request) {
    return getOrderByFrom(request, PORTLET_ORDER_BIES, "userNotificationPortlet");
  }

  static {
    PORTLET_ORDER_BIES = new HashMap<>();
    PORTLET_ORDER_BIES.put(RECEPTION_DATE_INDEX, Pair.of(RECEPTION_DATE_ASC, RECEPTION_DATE_DESC));
    PORTLET_ORDER_BIES.put(SUBJECT_INDEX, Pair.of(SUBJECT_ASC, SUBJECT_DESC));
    PORTLET_ORDER_BIES.put(FROM_INDEX, Pair.of(FROM_ASC, FROM_DESC));
    PORTLET_ORDER_BIES.put(SOURCE_INDEX, Pair.of(SOURCE_ASC, SOURCE_DESC));
  }
}
