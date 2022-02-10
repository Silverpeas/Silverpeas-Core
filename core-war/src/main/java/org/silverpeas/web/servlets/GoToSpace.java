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
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.silverpeas.web.servlets;

import org.silverpeas.core.admin.service.OrganizationControllerProvider;
import org.silverpeas.core.admin.space.SpaceInstLight;
import org.silverpeas.core.security.authorization.SpaceAccessControl;
import org.silverpeas.core.util.StringUtil;
import org.silverpeas.core.util.URLUtil;
import org.silverpeas.core.web.look.LookHelper;
import org.silverpeas.core.web.util.servlet.GoTo;
import org.silverpeas.core.web.util.viewgenerator.html.GraphicElementFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

public class GoToSpace extends GoTo {

  private static final long serialVersionUID = 8638938283373035004L;

  @Override
  public String getDestination(String objectId, HttpServletRequest req,
      HttpServletResponse res) throws Exception {
    SpaceInstLight space = OrganizationControllerProvider
        .getOrganisationController().getSpaceInstLightById(objectId);

    boolean fallback = StringUtil.getBooleanValue(req.getParameter("Fallback"));
    if (fallback) {

      // When fallback is requested, if the user has no access right to aimed space (or if the
      // space is deleted) then the first parent the user can access is searched.
      // It is possible that no space is found when user has no more access on the entire path.

      while (space != null &&
          !SpaceAccessControl.get().isUserAuthorized(getUserId(req), space.getId())) {
        space = OrganizationControllerProvider.getOrganisationController()
            .getSpaceInstLightById(space.getFatherId());
      }
    }

    if (fallback || space != null) {
      HttpSession session = req.getSession(true);
      GraphicElementFactory gef = (GraphicElementFactory) session.getAttribute(
          GraphicElementFactory.GE_FACTORY_SESSION_ATT);
      LookHelper helper = LookHelper.getLookHelper(session);
      if (space != null) {

        // This is the case where a space has been found directly or by the fallback processing.

        if (gef != null && helper != null) {
          helper.setSpaceIdAndSubSpaceId(space.getId());
        }
        return "SpaceId=" + space.getId();

      } else {

        // This is the case where fallback treatment has been performed without finding a space
        // target. The user will be redirected on the default homepage.

        if (gef != null && helper != null) {
          // Cleaning the location registered into look helper before returning the homepage url
          helper.setSpaceId(null);
          helper.setSubSpaceId(null);
          helper.setComponentId(null);
          String serverUrl = URLUtil.getFullApplicationURL(req);
          return (gef.getLookFrame().startsWith("/") ? serverUrl : serverUrl + "/admin/jsp/") +
              gef.getLookFrame() + "?Login=1";
        }
      }
    }
    return null;
  }
}
