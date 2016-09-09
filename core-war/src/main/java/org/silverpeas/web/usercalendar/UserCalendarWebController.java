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
 * FLOSS exception. You should have received a copy of the text describing
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
package org.silverpeas.web.usercalendar;

import org.silverpeas.core.admin.user.model.SilverpeasRole;
import org.silverpeas.core.calendar.Calendar;
import org.silverpeas.core.calendar.event.CalendarEvent;
import org.silverpeas.core.date.Period;
import org.silverpeas.core.web.calendar.CalendarViewType;
import org.silverpeas.core.web.calendar.service.CalendarEventEntity;
import org.silverpeas.core.web.calendar.service.CalendarWebServiceProvider;
import org.silverpeas.core.web.mvc.controller.ComponentContext;
import org.silverpeas.core.web.mvc.controller.MainSessionController;
import org.silverpeas.core.web.mvc.webcomponent.annotation.Homepage;
import org.silverpeas.core.web.mvc.webcomponent.annotation.LowestRoleAccess;
import org.silverpeas.core.web.mvc.webcomponent.annotation.NavigationStep;
import org.silverpeas.core.web.mvc.webcomponent.annotation.RedirectToInternalJsp;
import org.silverpeas.core.web.mvc.webcomponent.annotation.RedirectToPreviousNavigationStep;
import org.silverpeas.core.web.mvc.webcomponent.annotation.WebComponentController;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import java.time.LocalDate;
import java.util.Date;

@WebComponentController(UserCalendarSettings.COMPONENT_NAME)
public class UserCalendarWebController extends
    org.silverpeas.core.web.mvc.webcomponent.WebComponentController<UserCalendarWebRequestContext> {

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

  public static CalendarWebServiceProvider getWebServiceProvider() {
    return CalendarWebServiceProvider.get();
  }

  @Override
  protected void onInstantiation(final UserCalendarWebRequestContext context) {
    timeWindowViewContext =
        new UserCalendarTimeWindowViewContext(context.getComponentInstanceId(), getLanguage());
  }

  @Override
  protected void beforeRequestProcessing(final UserCalendarWebRequestContext context) {
    super.beforeRequestProcessing(context);
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
    // Nothing to do for now...
  }

  /**
   * Prepares the rendering of the home page.
   * @param context the context of the incoming request.
   */
  @GET
  @Path("calendars/context")
  @RedirectToPreviousNavigationStep
  public void view(UserCalendarWebRequestContext context) {
    CalendarViewType calendarViewType =
        CalendarViewType.from(context.getRequest().getParameter("view"));
    if (calendarViewType != null) {
      timeWindowViewContext.setViewType(calendarViewType);
    }
  }

  /**
   * Asks for purposing a new calendar. It renders an HTML page to input the content of a new
   * calendar.
   * @param context the context of the incoming request.
   */
  @GET
  @Path("calendars/new")
  @RedirectToInternalJsp("calendarEdit.jsp")
  @LowestRoleAccess(SilverpeasRole.admin)
  public void newCalendar(UserCalendarWebRequestContext context) {
  }

  /**
   * Adds a new event into the current calendar. The event's data are
   * carried within the request's context.
   * @param context the context of the incoming request.
   */
  @POST
  @Path("calendars/add")
  @RedirectToPreviousNavigationStep
  @LowestRoleAccess(SilverpeasRole.admin)
  public void addCalendar(UserCalendarWebRequestContext context) {
    Calendar calendar = new Calendar(context.getComponentInstanceId());
    calendar.setTitle(context.getRequest().getParameter("title"));
    getWebServiceProvider().saveCalendar(calendar);
  }

  /**
   * Asks for purposing a new event. It renders an HTML page to input the content of a new
   * event.
   * @param context the context of the incoming request.
   */
  @GET
  @Path("calendars/events/new")
  @RedirectToInternalJsp("eventEdit.jsp")
  @LowestRoleAccess(SilverpeasRole.admin)
  public void newEvent(UserCalendarWebRequestContext context) {
  }

  /**
   * Adds a new event into the current calendar. The event's data are
   * carried within the request's context.
   * @param context the context of the incoming request.
   */
  @POST
  @Path("calendars/{calendarId}/events/add")
  @RedirectToPreviousNavigationStep
  @LowestRoleAccess(SilverpeasRole.admin)
  public void addEvent(UserCalendarWebRequestContext context) throws Exception {
    Calendar userCalendar = context.getUserCalendarById();
    Date startDate = context.getRequest().getParameterAsDate("startDate", "startHour");
    Date endDate = context.getRequest().getParameterAsDate("endDate", "endHour");
    CalendarEvent event = CalendarEvent.on(Period
        .between(LocalDate.from(startDate.toInstant()), LocalDate.from(endDate.toInstant())));
    getWebServiceProvider().planEvent(userCalendar, event);
  }

  /**
   * Asks for purposing a new event. It renders an HTML page to input the content of a new
   * event.
   * @param context the context of the incoming request.
   */
  @GET
  @Path("calendars/events/{eventId}")
  @NavigationStep(identifier = EVENT_VIEW_NS_ID)
  @RedirectToInternalJsp("eventView.jsp")
  public void viewEvent(UserCalendarWebRequestContext context) {
    CalendarEvent userCalendarEvent = context.getUserCalendarEventById();
    if (userCalendarEvent != null) {
      CalendarEventEntity entity = getWebServiceProvider().asEventWebEntity(userCalendarEvent);
      context.getRequest().setAttribute("event", entity);
    } else {
      throw new WebApplicationException(Response.Status.NOT_FOUND);
    }
  }

  /**
   * Asks for purposing a new event. It renders an HTML page to input the content of a new
   * event.
   * @param context the context of the incoming request.
   */
  @GET
  @Path("calendars/events/{eventId}/edit")
  @RedirectToInternalJsp("eventEdit.jsp")
  @LowestRoleAccess(SilverpeasRole.admin)
  public void editEvent(UserCalendarWebRequestContext context) {
    CalendarEvent userCalendarEvent = context.getUserCalendarEventById();
    if (userCalendarEvent != null) {
      CalendarEventEntity entity = getWebServiceProvider().asEventWebEntity(userCalendarEvent);
      context.getRequest().setAttribute("event", entity);
    } else {
      throw new WebApplicationException(Response.Status.NOT_FOUND);
    }
  }

  /**
   * Adds a new event into the current calendar. The event's data are
   * carried within the request's context.
   * @param context the context of the incoming request.
   */
  @POST
  @Path("calendars/events/{eventId}")
  @RedirectToPreviousNavigationStep
  @LowestRoleAccess(SilverpeasRole.admin)
  public void updateEvent(UserCalendarWebRequestContext context) throws Exception {
    CalendarEvent userCalendarEvent = context.getUserCalendarEventById();
    if (userCalendarEvent != null) {

    } else {
      throw new WebApplicationException(Response.Status.NOT_FOUND);
    }
  }

  @POST
  @Path("calendars/events/{eventId}/delete")
  @RedirectToPreviousNavigationStep
  @LowestRoleAccess(SilverpeasRole.admin)
  public void deleteEvent(UserCalendarWebRequestContext context) {
    CalendarEvent userCalendarEvent = context.getUserCalendarEventById();
    if (userCalendarEvent != null) {

    } else {
      throw new WebApplicationException(Response.Status.NOT_FOUND);
    }
  }
}
