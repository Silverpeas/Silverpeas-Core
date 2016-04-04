/*
 * Copyright (C) 2000 - 2016 Silverpeas
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

package org.silverpeas.web.personalization.servlets;

import javax.servlet.http.HttpServletRequest;

import org.silverpeas.core.web.mvc.controller.ComponentContext;
import org.silverpeas.core.web.mvc.controller.MainSessionController;
import org.silverpeas.core.web.mvc.route.ComponentRequestRouter;
import org.silverpeas.web.personalization.control.PersonalizationSessionController;
import org.silverpeas.core.util.LocalizationBundle;
import org.silverpeas.core.web.http.HttpRequest;

import java.util.ArrayList;
import java.util.Properties;

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
   *
   * @param function The entering request function (ex : "Main.jsp")
   * @param personalizationScc The component Session Control, build and initialised.
   * @param request The entering request. The request rooter need it to get parameters
   * @return The complete destination URL for a forward (ex : "/notificationUser/jsp/notificationUser.jsp?flag=user")
   */
  @Override
  public String getDestination(final String function,
      final PersonalizationSessionController personalizationScc, final HttpRequest request) {
    String destination = "";

    try {
      if (function.startsWith("SaveChannels")) {
        saveChannels(personalizationScc, request);
        destination = "/personalizationPeas/jsp/personalization_Notification.jsp";
      } else if (function.startsWith("ParametrizeNotification")){
        parametrizeNotification(personalizationScc, request);
        destination = "/personalizationPeas/jsp/personalization_Notification.jsp";
      } else {
        destination = "/personalizationPeas/jsp/" + function;
      }
      performDelayedNotificationFrequency(personalizationScc, request);
    } catch (final Exception e) {
      request.setAttribute("javax.servlet.jsp.jspException", e);
      destination = "/admin/jsp/errorpageMain.jsp";
    }

    return destination;
  }

  private void saveChannels(final PersonalizationSessionController personalizationScc,
      final HttpServletRequest request) throws Exception {
    final String selectedChannels = request.getParameter("SelectedChannels");
    final String selectedFrequency = request.getParameter("SelectedFrequency");
    personalizationScc.saveChannels(selectedChannels);
    personalizationScc.saveDelayedUserNotificationFrequency(selectedFrequency);
    request.setAttribute("validationMessage",
        personalizationScc.getMultilang().getString("GML.validation.update"));
    setCommonRequestAttributes(personalizationScc, request);
  }

  private void parametrizeNotification(final PersonalizationSessionController personalizationScc,
      final HttpServletRequest request) throws Exception {
    String action = (String) request.getParameter("Action");
    String id = (String) request.getParameter("id");
    NotificationParametrizationAction parametrizationAction =
        NotificationParametrizationAction.from(action);
    LocalizationBundle messages = personalizationScc.getMultilang();
    switch (parametrizationAction) {
      case Test:
        String testExplanation;
        personalizationScc.testNotifAddress(id);
        if (id.equals("-10")) {
          testExplanation = messages.getString("TestPopUpExplanation");
        } else if (id.equals("-12")) {
          testExplanation = messages.getString("TestSilverMailExplanation");
        } else {
          testExplanation = messages.getString("TestSMTPExplanation");
        }
        request.setAttribute("testExplanation", testExplanation);
        break;
      case SetDefault:
        personalizationScc.setDefaultAddress(id);
        request.setAttribute("validationMessage", messages.getString("GML.validation.update"));
        break;
      case SetFrequency:
        personalizationScc.saveDelayedUserNotificationFrequency(id);
        request.setAttribute("validationMessage", messages.getString("GML.validation.update"));
        break;
      case Delete:
        personalizationScc.deleteNotifAddress(id);
        request.setAttribute("validationMessage", messages.getString("GML.validation.delete"));
        break;
    }
    setCommonRequestAttributes(personalizationScc, request);
  }

  private void setCommonRequestAttributes(final PersonalizationSessionController personalizationScc,
      final HttpServletRequest request) throws Exception {
    ArrayList<Properties> notifAddresses = personalizationScc.getNotificationAddresses();
    request.setAttribute("notificationAddresses", notifAddresses);
    request.setAttribute("multichannel", personalizationScc.isMultiChannelNotification());
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