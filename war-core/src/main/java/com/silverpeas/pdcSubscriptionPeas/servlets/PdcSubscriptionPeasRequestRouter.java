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

package com.silverpeas.pdcSubscriptionPeas.servlets;

import com.silverpeas.pdcSubscription.model.PDCSubscription;
import com.silverpeas.pdcSubscriptionPeas.control.PdcSubscriptionSessionController;
import com.stratelia.silverpeas.peasCore.ComponentContext;
import com.stratelia.silverpeas.peasCore.MainSessionController;
import com.stratelia.silverpeas.peasCore.servlets.ComponentRequestRouter;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.util.node.model.NodeDetail;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class PdcSubscriptionPeasRequestRouter extends
    ComponentRequestRouter<PdcSubscriptionSessionController> {

  private static final long serialVersionUID = -441269066150311066L;

  public PdcSubscriptionSessionController createComponentSessionController(
      MainSessionController mainSessionCtrl, ComponentContext componentContext) {
    return new PdcSubscriptionSessionController(mainSessionCtrl,
        componentContext);
  }

  /**
   * This method has to be implemented in the component request rooter class. returns the session
   * control bean name to be put in the request object ex : for notificationUser, returns
   * "notificationUser"
   */
  public String getSessionControlBeanName() {
    return "pdcSubscriptionPeas";
  }

  /**
   * This method has to be implemented by the component request rooter it has to compute a
   * destination page
   * @param function The entering request function (ex : "Main.jsp")
   * @param pdcSC The component Session Control, build and initialised.
   * @param request The entering request. The request rooter need it to get parameters
   * @return The complete destination URL for a forward (ex :
   * "/notificationUser/jsp/notificationUser.jsp?flag=user")
   */
  public String getDestination(String function, PdcSubscriptionSessionController pdcSC,
      HttpServletRequest request) {
    String destination = "";
    request.setAttribute("language", pdcSC.getLanguage());
    String rootDest = "/pdcSubscriptionPeas/jsp/";

    try {
      if (function.startsWith("subscriptionList")) {
        destination = rootDest + processSubscriptionList(request, pdcSC);
      } else if (function.startsWith("showUserSubscriptions")) {
        String reqUserId = request.getParameter("userId");
        if (reqUserId != null && !reqUserId.equals("")) {
          int userId = Integer.parseInt(reqUserId);
          destination = rootDest + processUserSubscriptions(request, pdcSC, userId);
        }
      } else if (function.equals("ViewSubscriptionTheme")) {
        String userId = request.getParameter("userId");
        String action = request.getParameter("action");
        // passage des parametres ...
        Collection<Collection<NodeDetail>> subscribeThemeList = pdcSC.getUserSubscribe(userId);
        request.setAttribute("SubscribeThemeList", subscribeThemeList);
        request.setAttribute("action", action);
        request.setAttribute("userId", userId);
        destination = rootDest + "viewSubscriptionTheme.jsp";
      } else if (function.equals("DeleteTheme")) {
        Object o = request.getParameterValues("themeCheck");
        if (o != null) {
          String[] themes = (String[]) o;
          pdcSC.deleteThemes(themes);
        }
        destination = getDestination("ViewSubscriptionTheme", pdcSC, request);
      }
    } catch (Exception e) {
      SilverTrace.error("pdcSubscriptionPeas",
          "PdcSubscriptionPeasRequestRouter.getDestination",
          "root.EX_GET_DESTINATION_ERROR", "", e);
      request.setAttribute("javax.servlet.jsp.jspException", e);
      return "/admin/jsp/errorpageMain.jsp";
    }

    return destination;
  }

  /**
   * Process required operations for showing <b>current</b> user subscription
   */
  private String processSubscriptionList(HttpServletRequest request,
      PdcSubscriptionSessionController pdcSC)
      throws Exception {
    request.setAttribute("action", "subscriptionList");

    String mode = request.getParameter("mode");
    if ("delete".equals(mode)) {
      Object o = request.getParameterValues("pdcCheck");
      if (o != null) {
        String[] iDs = (String[]) o;
        int[] ids_i = new int[iDs.length];
        for (int i = 0; i < iDs.length; i++) {
          String id = iDs[i];
          ids_i[i] = Integer.parseInt(id);
        }
        pdcSC.removeICByPK(ids_i);
      }
    }
    List<PDCSubscription> list = pdcSC.getUserPDCSubscription();

    return doInitSubscrListRequest(request, pdcSC, list);
  }

  /**
   * Process required operations for showing user subscription
   */
  private String processUserSubscriptions(HttpServletRequest request,
      PdcSubscriptionSessionController pdcSC, int userId)
      throws Exception {
    request.setAttribute("action", "showUserSubscriptions");
    request.setAttribute("userId", String.valueOf(userId));
    List<PDCSubscription> list = pdcSC.getUserPDCSubscription(userId);
    return doInitSubscrListRequest(request, pdcSC, list);
  }

  /**
   * Performs <code>Request</code> initialization for furure use in subscriptionList.jsp
   * @param request a <code>HttpServletRequest</code> to be forwarded
   * @param subscriptions a list of loaded PDCSubscription to be shown
   * @return jsp name
   */
  private String doInitSubscrListRequest(HttpServletRequest request,
      PdcSubscriptionSessionController pdcSC, List<PDCSubscription> subscriptions)
      throws Exception {
    request.setAttribute("subscriptionList", subscriptions);
    List pathContext = new ArrayList();
    for (PDCSubscription subscription : subscriptions) {
      pathContext.add(pdcSC.getPathCriterias(subscription.getPdcContext()));
    }
    request.setAttribute("PathContext", pathContext);
    return "subscriptionList.jsp";
  }

}
