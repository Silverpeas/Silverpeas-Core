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
 * FLOSS exception. You should have received a copy of the text describing
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
package org.silverpeas.web.usercalendar;

import org.silverpeas.core.admin.user.model.SilverpeasRole;
import org.silverpeas.core.calendar.Calendar;
import org.silverpeas.core.calendar.CalendarEventOccurrence;
import org.silverpeas.core.web.calendar.AbstractCalendarWebController;
import org.silverpeas.core.web.mvc.controller.ComponentContext;
import org.silverpeas.core.web.mvc.controller.MainSessionController;
import org.silverpeas.core.web.mvc.webcomponent.annotation.Homepage;
import org.silverpeas.core.web.mvc.webcomponent.annotation.LowestRoleAccess;
import org.silverpeas.core.web.mvc.webcomponent.annotation.NavigationStep;
import org.silverpeas.core.web.mvc.webcomponent.annotation.RedirectToInternalJsp;
import org.silverpeas.core.web.mvc.webcomponent.annotation.WebComponentController;
import org.silverpeas.core.webapi.calendar.CalendarEntity;

import javax.ws.rs.GET;
import javax.ws.rs.Path;

@WebComponentController(UserCalendarSettings.COMPONENT_NAME)
public class UserCalendarWebController extends
    AbstractCalendarWebController<UserCalendarWebRequestContext> {

  // Some navigation step identifier definitions
  private static final String EVENT_VIEW_NS_ID = "eventViewNavStepIdentifier";

  private UserCalendarTimeWindowViewContext timeWindowViewContext;

  /**
   * Standard Session Controller Constructor
   * @param mainSessionCtrl The user's profile
   * @param componentContext The component's profile
   */
  public UserCalendarWebController(MainSessionController mainSessionCtrl,
      ComponentContext componentContext) {
    super(mainSessionCtrl, componentContext, UserCalendarSettings.MESSAGES_PATH,
        UserCalendarSettings.ICONS_PATH, UserCalendarSettings.SETTINGS_PATH);
  }

  @SuppressWarnings("unchecked")
  @Override
  protected UserCalendarTimeWindowViewContext getCalendarTimeWindowContext() {
    return timeWindowViewContext;
  }

  @Override
  protected void onInstantiation(final UserCalendarWebRequestContext context) {
    timeWindowViewContext =
        new UserCalendarTimeWindowViewContext(context.getComponentInstanceId(), getLanguage(),
            getPersonalization().getZoneId());
  }

  @Override
  protected void beforeRequestProcessing(final UserCalendarWebRequestContext context) {
    super.beforeRequestProcessing(context);
    CalendarEventOccurrence userOccurrence = context.getCalendarEventOccurrenceById();
    if (userOccurrence != null &&
        !userOccurrence.getCalendarEvent().canBeModifiedBy(context.getUser())) {
      context.getRequest().setAttribute("highestUserRole", SilverpeasRole.USER);
    }
    Calendar userMainCalendar = context.getMainCalendar();
    context.getRequest().setAttribute("userMainCalendar",
        CalendarEntity.fromCalendar(userMainCalendar)
            .withURI(context.uri().ofCalendar(userMainCalendar)));
    timeWindowViewContext.setZoneId(userMainCalendar.getZoneId());
    context.getRequest().setAttribute("timeWindowViewContext", timeWindowViewContext);
  }

  /**
   * Prepares the rendering of the home page.
   * @param context the context of the incoming request.
   */
  @GET
  @Path("Main")
  @Homepage
  @RedirectToInternalJsp("userCalendar.jsp")
  public void home(UserCalendarWebRequestContext context) {
    // Nothing to do
  }

  /**
   * Asks for purposing a new event. It renders an HTML page to input the content of a new
   * event.
   * @param context the context of the incoming request.
   */
  @GET
  @Path("calendars/events/new")
  @RedirectToInternalJsp("occurrenceEdit.jsp")
  @LowestRoleAccess(SilverpeasRole.ADMIN)
  public void newEvent(UserCalendarWebRequestContext context) {
    processNewEvent(context);
  }

  /**
   * Asks for purposing a new event. It renders an HTML page to input the content of a new
   * event.
   * @param context the context of the incoming request.
   */
  @GET
  @Path("calendars/occurrences/{occurrenceId}")
  @NavigationStep(identifier = EVENT_VIEW_NS_ID)
  @RedirectToInternalJsp("occurrenceView.jsp")
  public void viewOccurrence(UserCalendarWebRequestContext context) {
    processViewOccurrence(context, EVENT_VIEW_NS_ID);
  }

  /**
   * Asks for purposing a new event. It renders an HTML page to input the content of a new
   * event.
   * @param context the context of the incoming request.
   */
  @GET
  @Path("calendars/occurrences/{occurrenceId}/edit")
  @RedirectToInternalJsp("occurrenceEdit.jsp")
  @LowestRoleAccess(SilverpeasRole.ADMIN)
  public void editOccurrence(UserCalendarWebRequestContext context) {
    viewOccurrence(context);
  }
}
