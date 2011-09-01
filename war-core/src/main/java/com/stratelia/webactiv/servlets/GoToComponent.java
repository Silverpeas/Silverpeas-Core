/**
 * Copyright (C) 2000 - 2011 Silverpeas
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
 * "http://repository.silverpeas.com/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.stratelia.webactiv.servlets;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.silverpeas.look.LookHelper;
import com.silverpeas.peasUtil.GoTo;
import com.silverpeas.util.StringUtil;
import com.stratelia.webactiv.beans.admin.ComponentInstLight;
import com.stratelia.webactiv.beans.admin.OrganizationController;
import com.stratelia.webactiv.util.viewGenerator.html.GraphicElementFactory;

public class GoToComponent extends GoTo {

  private static final long serialVersionUID = -7281629150484820205L;

  public String getDestination(String objectId, HttpServletRequest req,
      HttpServletResponse res) throws Exception {
    OrganizationController organization = new OrganizationController();
    ComponentInstLight component = organization.getComponentInstLight(objectId);

    if (component != null) {
      HttpSession session = req.getSession(true);
      GraphicElementFactory gef = (GraphicElementFactory) session.getAttribute(
          GraphicElementFactory.GE_FACTORY_SESSION_ATT);
      LookHelper helper = (LookHelper) session.getAttribute(LookHelper.SESSION_ATT);
      if (gef != null && helper != null) {
        helper.setComponentIdAndSpaceIds(null, null, objectId);
        String helperSpaceId = helper.getSubSpaceId();
        if (!StringUtil.isDefined(helperSpaceId)) {
          helperSpaceId = helper.getSpaceId();
        }
        gef.setSpaceId(helperSpaceId);
      }
      return "ComponentId=" + objectId;
    }

    return null;
  }
}