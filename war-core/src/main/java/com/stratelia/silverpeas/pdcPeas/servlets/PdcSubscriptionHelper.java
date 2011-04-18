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

package com.stratelia.silverpeas.pdcPeas.servlets;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import javax.servlet.http.HttpServletRequest;

import com.silverpeas.pdcSubscription.model.PDCSubscription;
import com.silverpeas.pdcSubscription.util.PdcSubscriptionUtil;
import com.stratelia.silverpeas.classifyEngine.Criteria;
import com.stratelia.silverpeas.pdc.model.SearchCriteria;
import com.stratelia.silverpeas.pdcPeas.control.PdcSearchSessionController;

public class PdcSubscriptionHelper {

  public static void init(PdcSearchSessionController pdcSC,
      HttpServletRequest request) throws Exception {
    String pdcSubscr = request.getParameter("isPDCSubscription");
    if (pdcSubscr != null && pdcSubscr.equalsIgnoreCase("true")) {
      request.setAttribute("isPDCSubscription", "true");
      pdcSC.setShowOnlyPertinentAxisAndValues(false);
    }
    String newPdcSubscr = request.getParameter("isNewPDCSubscription");
    if (newPdcSubscr != null && newPdcSubscr.equalsIgnoreCase("true")) {
      request.setAttribute("isNewPDCSubscription", "true");
    }
  }

  public static void loadSubscription(PdcSearchSessionController pdcSC,
      HttpServletRequest request) throws RemoteException {
    request.setAttribute("isPDCSubscription", "true");
    doPdcSubscription(pdcSC, request);
  }

  public static void addSubscription(PdcSearchSessionController pdcSC,
      HttpServletRequest request) throws RemoteException {
    doPdcSubscriptionAdd(request, pdcSC);
  }

  public static void updateSubscription(PdcSearchSessionController pdcSC,
      HttpServletRequest request) throws RemoteException {
    doPdcSubscriptionUpdate(pdcSC, request);
  }

  private static void doPdcSubscription(PdcSearchSessionController pdcSC,
      HttpServletRequest request) throws RemoteException {
    pdcSC.getSearchContext().clearCriterias();
    pdcSC.setPDCSubscription(null);

    boolean isNewSubcription = true;
    String requestId = request.getParameter("pdcSId");
    PDCSubscription pdcSubscription = null;
    if (requestId != null) {
      int pdcScId = -1;
      try {
        pdcScId = Integer.parseInt(requestId);
        isNewSubcription = false;
      } catch (NumberFormatException e) {
      }
      pdcSubscription = (new PdcSubscriptionUtil())
          .getPDCSubsriptionById(pdcScId);
      pdcSC.getSearchContext().clearCriterias();

      if (pdcSubscription != null && pdcSubscription.getPdcContext() != null) {
        for (int i = 0; i < pdcSubscription.getPdcContext().size(); i++) {
          Criteria c = (Criteria) pdcSubscription.getPdcContext().get(i);
          pdcSC.getSearchContext().addCriteria(makeSearchCriteria(c));
        }
      }
      pdcSC.setPDCSubscription(pdcSubscription);
    }

    request.setAttribute("PDCSubscription", pdcSubscription);
    if (isNewSubcription) {
      request.setAttribute("isNewPDCSubscription", "true");
    }
  }

  private static void doPdcSubscriptionAdd(HttpServletRequest request,
      PdcSearchSessionController pdcSC) throws RemoteException {
    String name = request.getParameter("scName");
    if (name == null) {
      name = "";
    }

    PDCSubscription subscription =
        new PDCSubscription(-1, name, getCriteriasFromRequest(request), Integer.parseInt(pdcSC
            .getUserId()));
    (new PdcSubscriptionUtil()).createPDCSubsription(subscription);
    request.setAttribute("requestSaved", "yes");
  }

  private static void doPdcSubscriptionUpdate(PdcSearchSessionController pdcSC,
      HttpServletRequest request) throws RemoteException {
    PDCSubscription subscription = pdcSC.getPDCSubscription();
    if (subscription != null) {
      String name = request.getParameter("scName");
      if (name == null) {
        name = "";
      }
      subscription.setName(name);

      subscription.setPdcContext(getCriteriasFromRequest(request));
      (new PdcSubscriptionUtil()).updatePDCSubsription(subscription);
    }
    request.setAttribute("requestSaved", "yes");
  }

  private static SearchCriteria makeSearchCriteria(Criteria c) {
    return new SearchCriteria(c.getAxisId(), c.getValue());
  }

  public static List<Criteria> getCriteriasFromRequest(HttpServletRequest request) {
    String axisValueCouples = request.getParameter("AxisValueCouples");
    StringTokenizer tokenizer = new StringTokenizer(axisValueCouples, ",");
    List<Criteria> criterias = new ArrayList<Criteria>();
    int i = -1;
    while (tokenizer.hasMoreTokens()) {
      String axisValueCouple = tokenizer.nextToken();
      i = axisValueCouple.indexOf('-');
      if (i != -1) {
        String axisId = axisValueCouple.substring(0, i);
        String valuePath = axisValueCouple.substring(i + 1);
        criterias.add(new Criteria(Integer.parseInt(axisId), valuePath));
      }
    }
    return criterias;
  }

}