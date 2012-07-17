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

package com.stratelia.silverpeas.pdcPeas.servlets;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import com.silverpeas.interestCenter.model.InterestCenter;
import com.silverpeas.util.StringUtil;
import com.stratelia.silverpeas.classifyEngine.Criteria;
import com.stratelia.silverpeas.pdc.model.PdcException;
import com.stratelia.silverpeas.pdcPeas.control.PdcSearchSessionController;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.util.DateUtil;

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
      throws Exception, PdcException {
    pdcSC.loadICenter(icId);
  }

  public static void processICenterSaving(PdcSearchSessionController pdcSC,
      HttpServletRequest request) throws Exception, PdcException {
    String mode = request.getParameter("mode");
    // if mode is SaveRequest it saves whole search request to DB
    if ("SaveRequest".equals(mode)) {
      InterestCenter ic = new InterestCenter();
      ic.setName(request.getParameter("requestName"));
      ic.setQuery(request.getParameter("query"));
      ic.setWorkSpaceID(request.getParameter("spaces"));
      ic.setPeasID(request.getParameter("componentSearch"));
      ic.setAfterDate(getDate(request.getParameter("createafterdate"), pdcSC));
      ic.setBeforeDate(getDate(request.getParameter("createbeforedate"), pdcSC));
      ic.setAuthorID(request.getParameter("authorSearch"));

      List<Criteria> criteria = PdcSubscriptionHelper.getCriteriasFromRequest(request);
      ic.setPdcContext(criteria);

      int icId = pdcSC.saveICenter(ic);
      request.setAttribute("requestSaved", "yes");
      request.setAttribute("RequestSelected", String.valueOf(icId));
    }
  }

  private static java.util.Date getDate(String date, PdcSearchSessionController pdcSC) throws
      Exception {
    SilverTrace.info("pdcPeas", "InterestCentersHelper.getDate()", "root.MSG_GEN_PARAM_VALUE",
        "date= " + date);
    if (StringUtil.isDefined(date)) {
      return DateUtil.stringToDate(date, pdcSC.getLanguage());
    }
    return null;
  }

  private InterestCentersHelper() {
  }
}