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

package com.stratelia.silverpeas.personalizationPeas.servlets;

import javax.servlet.http.HttpServletRequest;

import com.stratelia.silverpeas.peasCore.ComponentContext;
import com.stratelia.silverpeas.peasCore.MainSessionController;
import com.stratelia.silverpeas.peasCore.servlets.ComponentRequestRouter;
import com.stratelia.silverpeas.personalizationPeas.control.PersonalizationSessionController;
import com.stratelia.silverpeas.silvertrace.SilverTrace;

/**
 * Class declaration
 * @author
 */
public class PersoPeasRequestRouter extends
    ComponentRequestRouter<PersonalizationSessionController> {

  private static final long serialVersionUID = 1L;

  /**
   * Method declaration
   * @param mainSessionCtrl
   * @param componentContext
   * @return
   * @see
   */
  @Override
  public PersonalizationSessionController createComponentSessionController(
      final MainSessionController mainSessionCtrl, final ComponentContext componentContext) {
    return new PersonalizationSessionController(mainSessionCtrl, componentContext);
  }

  /**
   * This method has to be implemented in the component request rooter class. returns the session
   * control bean name to be put in the request object ex : for notificationUser, returns
   * "notificationUser"
   */
  @Override
  public String getSessionControlBeanName() {
    return "personalizationPeas";
  }

  /**
   * This method has to be implemented by the component request rooter it has to compute a
   * destination page
   * @param function The entering request function (ex : "Main.jsp")
   * @param personalizationScc The component Session Control, build and initialised.
   * @param request The entering request. The request rooter need it to get parameters
   * @return The complete destination URL for a forward (ex : "/notificationUser/jsp/notificationUser.jsp?flag=user")
   */
  @Override
  public String getDestination(final String function,
      final PersonalizationSessionController personalizationScc, final HttpServletRequest request) {
    SilverTrace.info(getSessionControlBeanName(),
        "PersoPeasRequestRouter.getDestination()", "root.MSG_GEN_PARAM_VALUE",
        "function = " + function);

    String destination = "";

    try {
      if (function.startsWith("SaveChannels")) {
        final String selectedChannels = request.getParameter("SelectedChannels");
        final String selectedFrequency = request.getParameter("SelectedFrequency");
        personalizationScc.saveChannels(selectedChannels);
        personalizationScc.saveDelayedUserNotificationFrequency(selectedFrequency);
        request.setAttribute("validationMessage",
            personalizationScc.getMultilang().getString("GML.validation.update"));
        destination = "/personalizationPeas/jsp/personalization_Notification.jsp";
      } else {
        destination = "/personalizationPeas/jsp/" + function;
      }
      performDelayedNotificationFrequency(personalizationScc, request);
    } catch (final Exception e) {
      request.setAttribute("javax.servlet.jsp.jspException", e);
      destination = "/admin/jsp/errorpageMain.jsp";
    }

    SilverTrace.info(getSessionControlBeanName(),
        "PersoPeasRequestRouter.getDestination()", "root.MSG_GEN_PARAM_VALUE",
        "destination = " + destination);
    return destination;
  }

  /**
   * This method handles data about delayed notification
   * @param componentSC
   * @param request
   */
  private void performDelayedNotificationFrequency(
      final PersonalizationSessionController componentSC, final HttpServletRequest request) {
    request.setAttribute("delayedNotification", componentSC.getDelayedNotificationBean());
  }
}