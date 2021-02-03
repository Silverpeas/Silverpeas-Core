/*
 * Copyright (C) 2000 - 2021 Silverpeas
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

package org.silverpeas.core.web.calendar;

import org.silverpeas.core.admin.component.model.PersonalComponentInstance;
import org.silverpeas.core.admin.user.model.SilverpeasRole;
import org.silverpeas.core.calendar.CalendarEventOccurrence;
import org.silverpeas.core.util.Pair;
import org.silverpeas.core.util.ResourceLocator;
import org.silverpeas.core.util.StringUtil;
import org.silverpeas.core.util.URLUtil;
import org.silverpeas.core.web.mvc.controller.ComponentContext;
import org.silverpeas.core.web.mvc.controller.MainSessionController;
import org.silverpeas.core.web.mvc.webcomponent.WebComponentController;
import org.silverpeas.core.web.mvc.webcomponent.annotation.LowestRoleAccess;
import org.silverpeas.core.web.mvc.webcomponent.annotation.RedirectTo;
import org.silverpeas.core.web.mvc.webcomponent.annotation.RedirectToInternal;
import org.silverpeas.core.web.selection.Selection;
import org.silverpeas.core.web.selection.SelectionUsersGroups;
import org.silverpeas.core.webapi.calendar.CalendarEventOccurrenceEntity;
import org.silverpeas.core.webapi.calendar.CalendarWebManager;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.sql.Date;
import java.time.LocalDate;
import java.time.temporal.Temporal;
import java.util.List;

import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.silverpeas.core.util.StringUtil.*;
import static org.silverpeas.core.util.URLUtil.getApplicationURL;

/**
 * Common behaviors about WEB component controllers which handle the rendering of a calendar.
 * @param <C>
 */
public abstract class AbstractCalendarWebController<C extends AbstractCalendarWebRequestContext>
    extends WebComponentController<C> {

  private static final int STRING_MAX_LENGTH = 50;

  private Selection userPanelSelection = null;

  public AbstractCalendarWebController(final MainSessionController controller,
      final ComponentContext context, final String multilangFileName, final String iconFileName,
      final String settingsFileName) {
    super(controller, context, multilangFileName, iconFileName, settingsFileName);
    userPanelSelection = getSelection();
  }

  protected abstract <T extends CalendarTimeWindowViewContext> T getCalendarTimeWindowContext();

  /**
   * Handles the time window context.
   * @param context the context of the incoming request.
   */
  @POST
  @Path("calendars/context")
  @Produces(MediaType.APPLICATION_JSON)
  public <T extends CalendarTimeWindowViewContext> T view(C context) {
    CalendarViewType calendarViewType =
        CalendarViewType.from(context.getRequest().getParameter("view"));
    if (calendarViewType != null) {
      getCalendarTimeWindowContext().setViewType(calendarViewType);
    }
    String listViewMode = context.getRequest().getParameter("listViewMode");
    if (isDefined(listViewMode)) {
      getCalendarTimeWindowContext().setListViewMode(getBooleanValue(listViewMode));
    }
    String timeWindow = context.getRequest().getParameter("timeWindow");
    if (StringUtil.isDefined(timeWindow)) {
      if ("previous".equals(timeWindow)) {
        getCalendarTimeWindowContext().previous();
      } else if ("next".equals(timeWindow)) {
        getCalendarTimeWindowContext().next();
      } else if ("today".equals(timeWindow)) {
        getCalendarTimeWindowContext().today();
      } else if ("referenceDay".equals(timeWindow)) {
        LocalDate date =
            LocalDate.parse(context.getRequest().getParameter("timeWindowDate").split("T")[0]);
        getCalendarTimeWindowContext().setReferenceDay(Date.valueOf(date));
      }
    }
    return getCalendarTimeWindowContext();
  }

  @POST
  @Path("calendars/events/users/participation")
  @RedirectTo("{userPanelUri}")
  @LowestRoleAccess(SilverpeasRole.WRITER)
  public void viewParticipationOfUsers(C context) {
    List<String> userIds = (List<String>) StringUtil
        .splitString(context.getRequest().getParameter("UserPanelCurrentUserIds"), ',');
    List<String> groupIds = (List<String>) StringUtil
        .splitString(context.getRequest().getParameter("UserPanelCurrentGroupIds"), ',');
    String selectionUri =
        initSelection(getUserParticipationSelectionParams(), "userParticipation", userIds,
            groupIds);
    context.addRedirectVariable("userPanelUri", selectionUri);
  }

  /**
   * Gets the selection parameters about the view of user participation.<br>
   * This method must be overrated in case of additional settings to apply.
   * @return the selection parameters.
   */
  protected SelectionUsersGroups getUserParticipationSelectionParams() {
    SelectionUsersGroups attendeeSelectionParams = new SelectionUsersGroups();
    if(!PersonalComponentInstance.from(getComponentId()).isPresent()) {
      attendeeSelectionParams.setComponentId(getComponentId());
    }
    return attendeeSelectionParams;
  }

  @POST
  @Path("calendars/events/attendees/select")
  @RedirectTo("{userPanelUri}")
  @LowestRoleAccess(SilverpeasRole.WRITER)
  public void modifyAttendees(C context) {
    List<String> userIds = (List<String>) StringUtil
        .splitString(context.getRequest().getParameter("UserPanelCurrentUserIds"), ',');
    List<String> groupIds = (List<String>) StringUtil
        .splitString(context.getRequest().getParameter("UserPanelCurrentGroupIds"), ',');
    String selectionUri =
        initSelection(getAttendeeSelectionParams(), "attendees", userIds, groupIds);
    context.addRedirectVariable("userPanelUri", selectionUri);
  }

  /**
   * Gets the selection parameters about attendees.<br>
   * This method must be overrated in case of additional settings to apply.
   * @return the selection parameters.
   */
  protected SelectionUsersGroups getAttendeeSelectionParams() {
    SelectionUsersGroups attendeeSelectionParams = new SelectionUsersGroups();
    if(!PersonalComponentInstance.from(getComponentId()).isPresent()) {
      attendeeSelectionParams.setComponentId(getComponentId());
    }
    return attendeeSelectionParams;
  }

  protected void processNewEvent(final AbstractCalendarWebRequestContext context) {
    final Temporal startDate = context.getOccurrenceStartDate();
    if (startDate != null) {
      context.getRequest().setAttribute("occurrenceStartDate", startDate.toString());
    }
  }

  protected void processViewOccurrence(final AbstractCalendarWebRequestContext context,
      final String navigationStepId) {
    CalendarEventOccurrence occurrence = context.getCalendarEventOccurrenceById();
    if (occurrence != null) {
      CalendarEventOccurrenceEntity entity =
          CalendarEventOccurrenceEntity.fromOccurrence(occurrence, context.getComponentInstanceId(),
              getCalendarTimeWindowContext().getZoneId(), false).withOccurrenceURI(
              context.uri().ofOccurrence(occurrence));
      context.getRequest().setAttribute("occurrence", entity);

      context.getNavigationContext()
          .navigationStepFrom(navigationStepId)
          .withLabel(StringUtil.truncate(entity.getTitle(), STRING_MAX_LENGTH))
          .withFullUri(context.uri().ofOccurrenceView(occurrence).getPath().replaceFirst(
              getApplicationURL(), EMPTY));
    } else {
      throw new WebApplicationException(Response.Status.NOT_FOUND);
    }
  }

  private String initSelection(SelectionUsersGroups sug, String goFunction, List<String> userIds,
      List<String> groupIds) {
    String url = ResourceLocator.getGeneralSettingBundle().getString("ApplicationURL") +
        URLUtil.getURL(getSpaceId(), getComponentId());
    String goUrl = url + goFunction;

    userPanelSelection.resetAll();
    userPanelSelection.setGoBackURL(goUrl);
    userPanelSelection.setElementSelectable(true);
    userPanelSelection.setSelectedElements(userIds);
    // for now groups are not handled, but it will be the case in the future
    userPanelSelection.setSetSelectable(false);
    userPanelSelection.setSelectedSets(groupIds);
    userPanelSelection.setHostPath(null);
    Pair<String, String> hostComponentName = new Pair<>(getComponentLabel(), null);
    userPanelSelection.setHostComponentName(hostComponentName);
    userPanelSelection.setHostSpaceName(getSpaceLabel());
    userPanelSelection.setPopupMode(true);
    userPanelSelection.setHtmlFormElementId(goFunction);
    userPanelSelection.setHtmlFormName("dummy");

    // Add extra params
    userPanelSelection.setExtraParams(sug);
    return Selection.getSelectionURL();
  }

  /**
   * Handles the incoming from a search result URL.
   * @param context the context of the incoming request.
   */
  @GET
  @Path("searchResult")
  @RedirectToInternal("calendars/occurrences/{occurrenceId}")
  public void searchResult(AbstractCalendarWebRequestContext context) {
    context.getNavigationContext().clear();
    final CalendarEventOccurrence occurrence = CalendarWebManager.get(getComponentName())
        .getFirstCalendarEventOccurrenceFromEventId(context.getRequest().getParameter("Id"));
    context.addRedirectVariable("occurrenceId", asBase64(occurrence.getId().getBytes()));
  }
}
