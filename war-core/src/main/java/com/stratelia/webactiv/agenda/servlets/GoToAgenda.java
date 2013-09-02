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

package com.stratelia.webactiv.agenda.servlets;

import java.net.URLEncoder;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.silverpeas.peasUtil.GoTo;
import com.stratelia.silverpeas.peasCore.URLManager;
import com.stratelia.silverpeas.silvertrace.SilverTrace;

public class GoToAgenda extends GoTo {

  private static final long serialVersionUID = -6521628766331966518L;

  public String getDestination(String objectId, HttpServletRequest req,
      HttpServletResponse res) throws Exception {
    String url = "ViewOtherAgenda?Id=" + objectId;

    SilverTrace.info("agenda", "GoToAgenda.getDestination",
        "root.MSG_GEN_PARAM_VALUE", "Url = " + url);

    String gotoURL = URLManager.getURL(URLManager.CMP_AGENDA) + url;

    SilverTrace.info("agenda", "GoToAgenda.getDestination",
        "root.MSG_GEN_PARAM_VALUE", "gotoURL = " + gotoURL);

    return "goto=" + URLEncoder.encode(gotoURL);
  }

}