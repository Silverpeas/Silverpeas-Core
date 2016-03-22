/*
 * Copyright (C) 2000 - 2013 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have recieved a copy of the text describing
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
package org.silverpeas.web.subscription.servlets;

import org.silverpeas.core.web.mvc.controller.ComponentContext;
import org.silverpeas.core.web.mvc.controller.MainSessionController;
import org.silverpeas.core.web.mvc.route.ComponentRequestRouter;
import org.silverpeas.core.web.http.HttpRequest;
import org.silverpeas.web.subscription.constant.SubscriptionFunction;
import org.silverpeas.web.subscription.control.SubscriptionSessionController;

/**
 * User: Yohann Chastagnier
 * Date: 04/03/13
 */
public class SubscriptionRequestRouter
    extends ComponentRequestRouter<SubscriptionSessionController> {

  @Override
  public String getSessionControlBeanName() {
    return "subscriptionManagement";
  }

  @Override
  public SubscriptionSessionController createComponentSessionController(
      final MainSessionController mainSessionCtrl, final ComponentContext componentContext) {
    return new SubscriptionSessionController(mainSessionCtrl, componentContext);
  }

  @Override
  public String getDestination(final String function,
      final SubscriptionSessionController subscriptionSC, final HttpRequest request) {

    // Initializing destination
    String destination = "";

    // Setting the context
    request.setAttribute("context", subscriptionSC.getContext());

    switch (SubscriptionFunction.from(function)) {
      case ToUserPanel:
        destination = subscriptionSC.toUserPanel();
        break;
      case FromUserPanel:
        subscriptionSC.fromUserPanel();
        destination = getDestination(SubscriptionFunction.Main.name(), subscriptionSC, request);
        break;
      case Main:
      default:
        destination = "/subscription/jsp/subscriptionpanel.jsp";
        break;
    }

    // Returning the destination
    return destination;
  }
}
