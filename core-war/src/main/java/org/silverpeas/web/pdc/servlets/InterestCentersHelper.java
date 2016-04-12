/*
 * Copyright (C) 2000 - 2016 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU Affero General Public License as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of the GPL, you may
 * redistribute this Program in connection with Free/Libre Open Source Software ("FLOSS")
 * applications as described in Silverpeas's FLOSS exception. You should have received a copy of the
 * text describing the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License along with this program.
 * If not, see <http://www.gnu.org/licenses/>.
 */
package org.silverpeas.web.pdc.servlets;

import org.silverpeas.core.pdc.interests.model.Interests;
import org.silverpeas.web.pdc.control.PdcSearchSessionController;
import org.silverpeas.core.security.authorization.ComponentAccessController;
import org.silverpeas.core.util.DateUtil;
import org.silverpeas.core.util.ServiceProvider;
import org.silverpeas.core.util.StringUtil;

import javax.servlet.http.HttpServletRequest;

public class InterestCentersHelper {

  public static String putSelectedInterestCenterId(HttpServletRequest request)
      throws Exception {
    String icId = request.getParameter("iCenterId");
    if (icId != null) {
      request.setAttribute("RequestSelected", icId);
    }
    return icId;
  }

  public static void loadICenter(PdcSearchSessionController pdcSC, String icId)
      throws Exception {
    pdcSC.loadICenter(icId);
  }

  public static void processICenterSaving(PdcSearchSessionController pdcSC,
      HttpServletRequest request) throws Exception {
    String mode = request.getParameter("mode");
    // if mode is SaveRequest it saves whole search request to DB
    if ("SaveRequest".equals(mode)) {
      Interests ic = new Interests();
      ic.setName(request.getParameter("requestName"));
      ic.setQuery(request.getParameter("query"));
      ic.setWorkSpaceID(request.getParameter("spaces"));
      String componentId = request.getParameter("componentSearch");
      if (StringUtil.isDefined(componentId)) {
        ComponentAccessController componentAccessController =
            ServiceProvider.getService(ComponentAccessController.class);
        if (componentAccessController.isUserAuthorized(pdcSC.getUserId(), componentId)) {
          ic.setPeasID(componentId);
        }
      }
      ic.setAfterDate(getDate(request.getParameter("createafterdate"), pdcSC));
      ic.setBeforeDate(getDate(request.getParameter("createbeforedate"), pdcSC));
      ic.setAuthorID(request.getParameter("authorSearch"));

      int icId = pdcSC.saveICenter(ic);
      request.setAttribute("requestSaved", "yes");
      request.setAttribute("RequestSelected", String.valueOf(icId));
    }
  }

  private static java.util.Date getDate(String date, PdcSearchSessionController pdcSC) throws
      Exception {

    if (StringUtil.isDefined(date)) {
      return DateUtil.stringToDate(date, pdcSC.getLanguage());
    }
    return null;
  }

  private InterestCentersHelper() {
  }
}
