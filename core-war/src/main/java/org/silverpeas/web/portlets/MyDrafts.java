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

import org.silverpeas.core.admin.user.model.UserDetail;
import org.silverpeas.core.contribution.publication.service.PublicationService;
import org.silverpeas.core.util.StringUtil;
import org.silverpeas.core.web.portlets.FormNames;

import javax.portlet.GenericPortlet;
import javax.portlet.PortletException;
import javax.portlet.PortletRequestDispatcher;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;
import java.io.IOException;

public class MyDrafts extends GenericPortlet implements FormNames {

  @Override
  public void doView(RenderRequest request, RenderResponse response)
      throws PortletException, IOException {
    request.setAttribute("Publications",
        getPublicationBm().getDraftsByUser(UserDetail.getCurrentRequester().getId()));
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
          getPortletContext().getRequestDispatcher("/portlets/jsp/myDrafts/" + pageName);
      dispatcher.include(request, response);
    } catch (IOException ioe) {
      throw new PortletException(ioe);
    }
  }

  private PublicationService getPublicationBm() {
    return PublicationService.get();
  }

}