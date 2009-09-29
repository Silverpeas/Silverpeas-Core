package com.stratelia.silverpeas.pdcPeas.servlets;

import javax.servlet.http.HttpServletRequest;

import com.silverpeas.interestCenter.model.InterestCenter;
import com.stratelia.silverpeas.pdc.model.PdcException;
import com.stratelia.silverpeas.pdc.model.SearchContext;
import com.stratelia.silverpeas.pdcPeas.control.PdcSearchSessionController;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.util.DateUtil;

public class InterestCentersHelper {

  public static String putSelectedInterestCenterId(HttpServletRequest request)
      throws Exception {
    String icId = (String) request.getParameter("iCenterId");
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
    String mode = (String) request.getParameter("mode");
    // if mode is SaveRequest it saves whole search request to DB
    if ("SaveRequest".equals(mode)) {
      InterestCenter ic = new InterestCenter();
      ic.setName(request.getParameter("requestName"));
      ic.setQuery(request.getParameter("query"));
      ic.setWorkSpaceID(request.getParameter("spaces"));
      ic.setPeasID(request.getParameter("componentSearch"));
      ic.setAfterDate(getDate(request.getParameter("afterdate"), pdcSC));
      ic.setBeforeDate(getDate(request.getParameter("beforedate"), pdcSC));
      ic.setAuthorID(request.getParameter("authorSearch"));

      SearchContext pdcContext = PdcSubscriptionHelper
          .getSearchContextFromRequest(request);
      ic.setPdcContext(pdcContext.getCriterias());

      int icId = pdcSC.saveICenter(ic);
      request.setAttribute("requestSaved", "yes");
      request.setAttribute("RequestSelected", String.valueOf(icId));
    }
  }

  private static java.util.Date getDate(String date,
      PdcSearchSessionController pdcSC) throws Exception {
    SilverTrace.info("pdcPeas", "InterestCentersHelper.getDate()",
        "root.MSG_GEN_PARAM_VALUE", "date= " + date);
    java.util.Date utilDate = null;
    if ((date != null) && (!date.equals(""))) {
      // java.text.SimpleDateFormat formatter = new
      // java.text.SimpleDateFormat(pdcSC.getString("pdcPeas.DateFormat"));
      // utilDate = formatter.parse(date);
      utilDate = DateUtil.stringToDate(date, pdcSC.getLanguage());
    }
    return utilDate;
  }
}