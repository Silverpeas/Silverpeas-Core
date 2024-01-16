/*
 * Copyright (C) 2000 - 2024 Silverpeas
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
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package org.silverpeas.web.pdcsubscription.servlets;

import org.apache.commons.lang3.ArrayUtils;
import org.silverpeas.core.pdc.classification.Criteria;
import org.silverpeas.core.pdc.pdc.model.AxisValueCriterion;
import org.silverpeas.core.pdc.pdc.model.PdcException;
import org.silverpeas.core.pdc.pdc.model.Value;
import org.silverpeas.core.pdc.subscription.model.PdcSubscription;
import org.silverpeas.core.subscription.SubscriptionResourceType;
import org.silverpeas.core.subscription.constant.CommonSubscriptionResourceConstants;
import org.silverpeas.core.util.StringUtil;
import org.silverpeas.core.util.logging.SilverLogger;
import org.silverpeas.core.web.http.HttpRequest;
import org.silverpeas.core.web.mvc.controller.ComponentContext;
import org.silverpeas.core.web.mvc.controller.MainSessionController;
import org.silverpeas.core.web.mvc.route.ComponentRequestRouter;
import org.silverpeas.web.pdcsubscription.control.PdcSubscriptionSessionController;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;

import static org.silverpeas.core.util.StringUtil.defaultStringIfNotDefined;

public class PdcSubscriptionPeasRequestRouter extends ComponentRequestRouter<PdcSubscriptionSessionController> {

  private static final long serialVersionUID = -441269066150311066L;
  private static final String SUB_RES_TYPE_ATTR = "subResType";
  private static final String VIEW_SUBSCRIPTION_OF_TYPE = "ViewSubscriptionOfType";
  private static final String VIEW_SUBSCRIPTION_TAXONOMY = "ViewSubscriptionTaxonomy";
  private static final String USER_ID_PARAM = "userId";
  private static final String ACTION_PARAM = "action";

  @Override
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
  @Override
  public String getSessionControlBeanName() {
    return "pdcSubscriptionPeas";
  }

  /**
   * This method has to be implemented by the component request rooter it has to compute a
   * destination page
   *
   *
   * @param function The entering request function (ex : "Main.jsp")
   * @param pdcSC The component Session Control, build and initialised.
   * @param request The entering request. The request rooter need it to get parameters
   * @return The complete destination URL for a forward (ex :
   * "/notificationUser/jsp/notificationUser.jsp?flag=user")
   */
  @Override
  public String getDestination(String function, PdcSubscriptionSessionController pdcSC,
      HttpRequest request) {
    final String rootDest = "/pdcSubscriptionPeas/jsp/";
    String destination = "";
    request.setAttribute("language", pdcSC.getLanguage());
    request.setAttribute("currentUserId", pdcSC.getUserId());
    try {
      if (function.startsWith("subscriptionList")) {
        destination = getDestination(VIEW_SUBSCRIPTION_OF_TYPE, pdcSC, request);
      } else if (VIEW_SUBSCRIPTION_TAXONOMY.equals(function)) {
        destination = rootDest + processSubscriptionList(request, pdcSC);
      } else if (function.startsWith("showUserSubscriptions")) {
        final String reqUserId = defaultStringIfNotDefined(request.getParameter(USER_ID_PARAM), pdcSC.getUserId());
        final int userId = Integer.parseInt(reqUserId);
        destination = rootDest + processUserSubscriptions(request, pdcSC, userId);
      } else if (function.equals(VIEW_SUBSCRIPTION_OF_TYPE)) {
        destination = rootDest + viewSubscriptionOfType(pdcSC, request);
      } else if (function.equals("DeleteSubscriptionOfType")) {
        destination = deleteSubscriptionOfType(pdcSC, request);
      } else if (function.startsWith("PdcSubscription")) {
        String subscriptionId = request.getParameter("pdcSId");
        if (StringUtil.isDefined(subscriptionId)) {
          PdcSubscription pdcSubscription = pdcSC.setAsCurrentPDCSubscription(subscriptionId);
          request.setAttribute("PdcSubscription", pdcSubscription);
          request.setAttribute("PDCSubscriptionName", pdcSubscription.getName());
          request.setAttribute("IsNewPDCSubscription", false);
        } else {
          request.setAttribute("PDCSubscriptionName", "");
          request.setAttribute("IsNewPDCSubscription", true);
        }
        destination = rootDest + "subscription.jsp";
      } else if (function.startsWith("addSubscription")) {
        String name = request.getParameter("SubscriptionName");
        String values = request.getParameter("AxisValueCouples");
        List<? extends Criteria> criteria = AxisValueCriterion.fromFlattenedAxisValues(values);
        pdcSC.createPDCSubscription(name, criteria);
        destination = getDestination(VIEW_SUBSCRIPTION_TAXONOMY, pdcSC, request);
      } else if (function.startsWith("updateSubscription")) {
        String name = request.getParameter("SubscriptionName");
        String values = request.getParameter("AxisValueCouples");
        List<? extends Criteria> criteria = AxisValueCriterion.fromFlattenedAxisValues(values);
        pdcSC.updateCurrentSubscription(name, criteria);
        destination = getDestination(VIEW_SUBSCRIPTION_TAXONOMY, pdcSC, request);
      }
    } catch (Exception e) {
      SilverLogger.getLogger(this).error(e);
      request.setAttribute("javax.servlet.jsp.jspException", e);
      return "/admin/jsp/errorpageMain.jsp";
    }
    return destination;
  }

  private String viewSubscriptionOfType(final PdcSubscriptionSessionController pdcSC,
      final HttpRequest request) {
    final String userId = request.getParameter(USER_ID_PARAM);
    SubscriptionResourceType subResType = SubscriptionResourceType.from(request.getParameter(SUB_RES_TYPE_ATTR));
    if (!subResType.isValid()) {
      subResType = CommonSubscriptionResourceConstants.COMPONENT;
    }
    final String action = request.getParameter(ACTION_PARAM);
    request.setAttribute("subscriptions", pdcSC.getUserSubscriptionsOfType(userId, subResType));
    request.setAttribute(SUB_RES_TYPE_ATTR, subResType);
    request.setAttribute(ACTION_PARAM, action);
    request.setAttribute(USER_ID_PARAM, userId);
    return "viewSubscriptionsOfType.jsp";
  }

  private String deleteSubscriptionOfType(final PdcSubscriptionSessionController pdcSC,
      final HttpRequest request) {
    final SubscriptionResourceType subResType = SubscriptionResourceType.from(request.getParameter(SUB_RES_TYPE_ATTR));
    final String[] selectedItems = request.getParameterValues("subscriptionCheckbox");
    if (!ArrayUtils.isEmpty(selectedItems)) {
      pdcSC.deleteUserSubscriptionsOfType(selectedItems, subResType);
    }
    return getDestination(VIEW_SUBSCRIPTION_OF_TYPE, pdcSC, request);
  }

  /**
   * Process required operations for showing <b>current</b> user subscription
   */
  private String processSubscriptionList(HttpServletRequest request,
      PdcSubscriptionSessionController pdcSC)
      throws PdcException {
    request.setAttribute(ACTION_PARAM, VIEW_SUBSCRIPTION_TAXONOMY);

    String mode = request.getParameter("mode");
    if ("delete".equals(mode)) {
      Object o = request.getParameterValues("pdcCheck");
      if (o != null) {
        String[] iDs = (String[]) o;
        int[] idsI = new int[iDs.length];
        for (int i = 0; i < iDs.length; i++) {
          String id = iDs[i];
          idsI[i] = Integer.parseInt(id);
        }
        pdcSC.removeICByPK(idsI);
      }
    }
    List<PdcSubscription> list = pdcSC.getUserPDCSubscription();

    return initializeSubscrListRequest(request, pdcSC, list);
  }

  /**
   * Process required operations for showing user subscription
   */
  private String processUserSubscriptions(HttpServletRequest request,
      PdcSubscriptionSessionController pdcSC, int userId)
      throws PdcException {
    request.setAttribute(ACTION_PARAM, "showUserSubscriptions");
    request.setAttribute(USER_ID_PARAM, String.valueOf(userId));
    List<PdcSubscription> list = pdcSC.getUserPDCSubscription(userId);
    return initializeSubscrListRequest(request, pdcSC, list);
  }

  /**
   * Performs
   * <code>Request</code> initialization for furure use in subscriptionList.jsp
   *
   * @param request a <code>HttpServletRequest</code> to be forwarded
   * @param subscriptions a list of loaded PdcSubscription to be shown
   * @return jsp name
   */
  private String initializeSubscrListRequest(HttpServletRequest request,
      PdcSubscriptionSessionController pdcSC, List<PdcSubscription> subscriptions)
      throws PdcException {
    request.setAttribute("subscriptionList", subscriptions);
    final List<List<List<Value>>> pathContext = new ArrayList<>();
    for (PdcSubscription subscription : subscriptions) {
      pathContext.add(pdcSC.getPathCriterias(subscription.getPdcContext()));
    }
    request.setAttribute("PathContext", pathContext);
    return "subscriptionList.jsp";
  }
}