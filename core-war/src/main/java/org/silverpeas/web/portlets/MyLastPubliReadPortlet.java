/*
 * Copyright (C) 2000 - 2020 Silverpeas
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

import org.silverpeas.core.contribution.publication.model.PublicationDetail;
import org.silverpeas.core.contribution.publication.model.PublicationPK;
import org.silverpeas.core.contribution.publication.service.PublicationService;
import org.silverpeas.core.silverstatistics.access.model.HistoryObjectDetail;
import org.silverpeas.core.silverstatistics.access.service.StatisticService;
import org.silverpeas.core.util.StringUtil;
import org.silverpeas.core.web.mvc.controller.MainSessionController;
import org.silverpeas.core.web.portlets.FormNames;

import javax.portlet.GenericPortlet;
import javax.portlet.PortletException;
import javax.portlet.PortletPreferences;
import javax.portlet.PortletRequestDispatcher;
import javax.portlet.PortletSession;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

public class MyLastPubliReadPortlet extends GenericPortlet implements FormNames {

  private static final String NB_PUBLIS_PARAM = "nbPublis";
  private static final String DEFAULT_NB_PUBLIS = "5";

  @Override
  public void doView(RenderRequest request, RenderResponse response)
      throws PortletException, IOException {
    PortletSession session = request.getPortletSession();
    MainSessionController mainSessionController = (MainSessionController) session.getAttribute(
        MainSessionController.MAIN_SESSION_CONTROLLER_ATT, PortletSession.APPLICATION_SCOPE);
    Collection<PublicationDetail> listPubli = new ArrayList<>();
    PortletPreferences pref = request.getPreferences();
    int nbPublis = Integer.parseInt(DEFAULT_NB_PUBLIS);
    if (StringUtil.isInteger(pref.getValue(NB_PUBLIS_PARAM, DEFAULT_NB_PUBLIS))) {
      nbPublis = Integer.parseInt(pref.getValue(NB_PUBLIS_PARAM, DEFAULT_NB_PUBLIS));
    }
    Collection<HistoryObjectDetail> listObject =
        getStatisticBm().getLastHistoryOfObjectsForUser(mainSessionController.getUserId(), 1,
            "Publication", nbPublis);
    for (HistoryObjectDetail object : listObject) {
      PublicationPK pubPk = new PublicationPK(object.getResourceReference().getId(),
          object.getResourceReference().getComponentName());
      PublicationDetail pubDetail = getPublicationBm().getDetail(pubPk);
      if (pubDetail != null) {
        // the publication exists (it wasn't deleted)
        listPubli.add(pubDetail);
      }
    }
    request.setAttribute("Publications", listPubli);
    request.setAttribute("ZoneId", mainSessionController.getFavoriteZoneId());
    include(request, response, "portlet.jsp");
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
          getPortletContext().getRequestDispatcher("/portlets/jsp/myLastPubliRead/" + pageName);
      dispatcher.include(request, response);
    } catch (IOException ioe) {
      throw new PortletException(ioe);
    }
  }

  private StatisticService getStatisticBm() {
    return StatisticService.get();
  }

  private PublicationService getPublicationBm() {
    return PublicationService.get();
  }

  @Override
  public void doHelp(RenderRequest request, RenderResponse response)
      throws PortletException {
    include(request, response, "help.jsp");
  }
}
