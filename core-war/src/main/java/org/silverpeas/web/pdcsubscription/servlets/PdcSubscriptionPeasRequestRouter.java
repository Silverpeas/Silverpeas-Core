/*
 * Copyright (C) 2000 - 2026 Silverpeas
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

import jakarta.servlet.http.HttpServletRequest;
import org.apache.commons.lang3.ArrayUtils;
import org.silverpeas.core.pdc.pdc.model.AxisValueCriterion;
import org.silverpeas.core.pdc.pdc.model.PdcException;
import org.silverpeas.core.pdc.subscription.model.PdcSubscriptionPositionCriteria;
import org.silverpeas.core.web.http.HttpRequest;
import org.silverpeas.core.web.mvc.controller.ComponentContext;
import org.silverpeas.core.web.mvc.controller.MainSessionController;
import org.silverpeas.core.web.mvc.route.ComponentRequestRouter;
import org.silverpeas.kernel.logging.SilverLogger;
import org.silverpeas.kernel.util.StringUtil;
import org.silverpeas.web.pdcsubscription.control.PdcSubscriptionData;
import org.silverpeas.web.pdcsubscription.control.PdcSubscriptionSessionController;
import org.silverpeas.web.pdcsubscription.control.SubscriptionCategory;

import java.util.List;

import static org.silverpeas.kernel.util.StringUtil.defaultStringIfNotDefined;

public class PdcSubscriptionPeasRequestRouter extends ComponentRequestRouter<PdcSubscriptionSessionController> {

  private static final String SUB_RES_CATEGORY_ATTR = "subResCategory";
  private static final String VIEW_SUBSCRIPTION_OF_CATEGORY = "ViewSubscriptionOfCategory";
  private static final String VIEW_SUBSCRIPTION_TAXONOMY = "ViewSubscriptionTaxonomy";
  private static final String USER_ID_PARAM = "userId";
  private static final String ACTION_PARAM = "action";
  private static final String PDC_CONTEXT = "pdc";
  private static final String SCOPE_PARAM = "scope";
  private static final String POSITIONS_ATTR = "PdcSubscriptionPositions";

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
   * @param function The entering request function (ex : "Main.jsp")
   * @param pdcSC The component Session Control, build and initialized.
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
        destination = getDestination(VIEW_SUBSCRIPTION_OF_CATEGORY, pdcSC, request);
      } else if (VIEW_SUBSCRIPTION_TAXONOMY.equals(function)) {
        destination = rootDest + processSubscriptionList(request, pdcSC);
      } else if (function.startsWith("showUserSubscriptions")) {
        final String reqUserId = defaultStringIfNotDefined(request.getParameter(USER_ID_PARAM),
            pdcSC.getUserId());
        destination = rootDest + processUserSubscriptions(request, pdcSC, reqUserId);
      } else if (function.equals(VIEW_SUBSCRIPTION_OF_CATEGORY)) {
        destination = rootDest + viewSubscriptionOfCategory(pdcSC, request);
      } else if (function.equals("DeleteSubscriptionOfCategory")) {
        destination = deleteSubscriptionOfCategory(pdcSC, request);
      } else if (function.startsWith("PdcSubscription")) {
        final String subscriptionId = request.getParameter("pdcSId");
        final String context = request.getParameter("context");
        if (isForcedContext(context)) {
          pdcSC.startForcedPdcSubscriptionEdition(subscriptionId,
              request.getParameter(SCOPE_PARAM));
        } else if (StringUtil.isDefined(subscriptionId)) {
          pdcSC.setAsCurrentPdcSubscription(subscriptionId);
        }
        request.setAttribute("path", request.getParameter("path"));
        destination = rootDest + editSubscription(pdcSC, request, context,
            !StringUtil.isDefined(subscriptionId));
      } else if (function.startsWith("ToUserPanel")) {
        pdcSC.keepForcedPdcSubscriptionEdition(request.getParameter("SubscriptionName"),
            criteriaOf(request));
        destination = pdcSC.toUserPanel();
      } else if (function.startsWith("FromUserPanel")) {
        pdcSC.fromUserPanel();
        destination = rootDest + editSubscription(pdcSC, request, PDC_CONTEXT, false);
      } else if (function.startsWith("BackToPdcSubscription")) {
        destination = rootDest + editSubscription(pdcSC, request, PDC_CONTEXT, false);
      } else if (function.startsWith("addSubscription") ||
          function.startsWith("updateSubscription")) {
        final String name = request.getParameter("SubscriptionName");
        final String context = request.getParameter("context");
        if (isForcedContext(context)) {
          pdcSC.saveForcedPdcSubscription(name, criteriaOf(request));
        } else if (function.startsWith("addSubscription")) {
          pdcSC.createPdcSubscription(name, criteriaOf(request));
        } else {
          pdcSC.updateCurrentPdcSubscription(name, criteriaOf(request));
        }
        destination = getDestination(VIEW_SUBSCRIPTION_TAXONOMY, pdcSC, request);
      }
    } catch (Exception e) {
      SilverLogger.getLogger(this).error(e);
      request.setAttribute("jakarta.servlet.jsp.jspException", e);
      return "/admin/jsp/errorpageMain.jsp";
    }
    return destination;
  }

  private static boolean isForcedContext(final String context) {
    return PDC_CONTEXT.equals(context);
  }

  private String forcedSubscriptionsPath(final PdcSubscriptionSessionController pdcSC,
      final String scope) {
    return scope + " > " + pdcSC.getString("Title.forced");
  }

  private List<AxisValueCriterion> criteriaOf(final HttpRequest request) {
    return AxisValueCriterion.fromFlattenedAxisValues(request.getParameter("AxisValueCouples"));
  }

  /**
   * Prepares the screen of edition of a subscription on the PdC. For a forced subscription, the
   * data come from the edition kept in the session as it has to survive the round trip to the user
   * panel from which the subscribers are designated.
   */
  private String editSubscription(final PdcSubscriptionSessionController pdcSC,
      final HttpRequest request, final String context, final boolean isNew) {
    request.setAttribute("context", context);
    if (isForcedContext(context)) {
      final String scope = pdcSC.getEditedPdcSubscriptionScope();
      request.setAttribute("IsNewPDCSubscription", pdcSC.isEditedPdcSubscriptionNew());
      request.setAttribute("PDCSubscriptionName", pdcSC.getEditedPdcSubscriptionName());
      request.setAttribute(POSITIONS_ATTR, pdcSC.getEditedPdcSubscriptionPositions());
      request.setAttribute("SubscribedUsers", pdcSC.getEditedPdcSubscriptionUsers());
      request.setAttribute("SubscribedGroups", pdcSC.getEditedPdcSubscriptionGroups());
      request.setAttribute(SCOPE_PARAM, scope);
      request.setAttribute("path", forcedSubscriptionsPath(pdcSC, scope));
    } else if (isNew) {
      request.setAttribute("IsNewPDCSubscription", true);
      request.setAttribute("PDCSubscriptionName", "");
      request.setAttribute(POSITIONS_ATTR, List.of());
    } else {
      final PdcSubscriptionPositionCriteria current = pdcSC.getCurrentPdcSubscription();
      request.setAttribute("IsNewPDCSubscription", false);
      request.setAttribute("PDCSubscriptionName", current.getName());
      request.setAttribute(POSITIONS_ATTR, current.getCriteria());
    }
    return "subscription.jsp";
  }

  private String viewSubscriptionOfCategory(final PdcSubscriptionSessionController pdcSC,
      final HttpRequest request) {
    final SubscriptionCategory category =
        pdcSC.getSubscriptionCategory(request.getParameter(SUB_RES_CATEGORY_ATTR));
    final String userId = request.getParameter(USER_ID_PARAM);
    final String action = request.getParameter(ACTION_PARAM);
    request.setAttribute("subscriptions", pdcSC.getUserSubscriptionsOfCategory(userId, category));
    request.setAttribute(SUB_RES_CATEGORY_ATTR, category);
    request.setAttribute(ACTION_PARAM, action);
    request.setAttribute(USER_ID_PARAM, userId);

    return "viewSubscriptionsOfCategory.jsp";
  }

  private String deleteSubscriptionOfCategory(final PdcSubscriptionSessionController pdcSC,
      final HttpRequest request) {
    final String[] selectedItems = request.getParameterValues("subscriptionCheckbox");
    if (!ArrayUtils.isEmpty(selectedItems)) {
      pdcSC.deleteUserSubscriptions(selectedItems);
    }
    return getDestination(VIEW_SUBSCRIPTION_OF_CATEGORY, pdcSC, request);
  }

  /**
   * Process required operations for showing <b>current</b> user subscription
   */
  private String processSubscriptionList(HttpServletRequest request,
      PdcSubscriptionSessionController pdcSC)
      throws PdcException {
    request.setAttribute(ACTION_PARAM, VIEW_SUBSCRIPTION_TAXONOMY);

    String context = request.getParameter("context");
    final boolean forced = isForcedContext(context) && pdcSC.isPdcManager();

    if ("delete".equals(request.getParameter("mode"))) {
      final String[] ids = request.getParameterValues("pdcCheck");
      if (!ArrayUtils.isEmpty(ids)) {
        if (forced) {
          // a forced subscription is entirely managed by the managers of the PdC: the set of
          // positions is deleted whatever its subscribers
          pdcSC.deleteForcedPdcSubscriptions(ids);
        } else {
          pdcSC.deletePdcSubscriptions(ids);
        }
      }
    }

    List<PdcSubscriptionData> list;
    if (forced) {
      final String scope = request.getParameter(SCOPE_PARAM);
      request.setAttribute("path", forcedSubscriptionsPath(pdcSC, scope));
      request.setAttribute(USER_ID_PARAM, "all");
      request.setAttribute(SCOPE_PARAM, scope);
      list = pdcSC.getForcedPdcSubscriptions();
    } else {
      context = "userSubscriptions";
      request.setAttribute("path", pdcSC.getString("Title"));
      list = pdcSC.getUserPdcSubscriptions(pdcSC.getUserId());
    }
    return initializeSubscriptionsInRequest(request, list, context);
  }

  /**
   * Process required operations for showing user subscription
   */
  private String processUserSubscriptions(HttpServletRequest request,
      PdcSubscriptionSessionController pdcSC, String userId)
      throws PdcException {
    request.setAttribute(ACTION_PARAM, "showUserSubscriptions");
    request.setAttribute(USER_ID_PARAM, userId);
    request.setAttribute("path", pdcSC.getString("Title"));
    List<PdcSubscriptionData> list = pdcSC.getUserPdcSubscriptions(userId);
    String context = "userSubscriptions";
    return initializeSubscriptionsInRequest(request, list, context);
  }

  /**
   * Performs
   * <code>Request</code> initialization for future use in subscriptionList.jsp
   *
   * @param request a <code>HttpServletRequest</code> to be forwarded
   * @param subscriptions a list of loaded PdcSubscription to be shown
   * @return jsp name
   */
  private String initializeSubscriptionsInRequest(HttpServletRequest request,
      List<PdcSubscriptionData> subscriptions, String context) {
    request.setAttribute("context", context);
    request.setAttribute("subscriptionList", subscriptions);
    return "subscriptionList.jsp";
  }
}