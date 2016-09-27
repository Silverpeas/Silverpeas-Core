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

package org.silverpeas.core.web.calendar;

import org.silverpeas.core.admin.component.model.PersonalComponentInstance;
import org.silverpeas.core.admin.user.model.SilverpeasRole;
import org.silverpeas.core.util.Pair;
import org.silverpeas.core.util.ResourceLocator;
import org.silverpeas.core.util.StringUtil;
import org.silverpeas.core.util.URLUtil;
import org.silverpeas.core.web.mvc.controller.ComponentContext;
import org.silverpeas.core.web.mvc.controller.MainSessionController;
import org.silverpeas.core.web.mvc.webcomponent.WebComponentRequestContext;
import org.silverpeas.core.web.mvc.webcomponent.annotation.LowestRoleAccess;
import org.silverpeas.core.web.mvc.webcomponent.annotation.RedirectTo;
import org.silverpeas.core.web.selection.Selection;
import org.silverpeas.core.web.selection.SelectionUsersGroups;
import org.silverpeas.core.webapi.calendar.CalendarWebServiceProvider;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.List;

/**
 * Common behaviors about WEB component controllers which handle the rendering of a calendar.
 * @param <WEB_COMPONENT_REQUEST_CONTEXT>
 */
public abstract class AbstractCalendarWebController<WEB_COMPONENT_REQUEST_CONTEXT extends
    WebComponentRequestContext>
    extends
    org.silverpeas.core.web.mvc.webcomponent.WebComponentController<WEB_COMPONENT_REQUEST_CONTEXT> {

  private Selection userPanelSelection = null;

  public AbstractCalendarWebController(final MainSessionController controller,
      final ComponentContext context, final String multilangFileName, final String iconFileName,
      final String settingsFileName) {
    super(controller, context, multilangFileName, iconFileName, settingsFileName);
    userPanelSelection = getSelection();
  }

  protected CalendarWebServiceProvider getWebServiceProvider() {
    return CalendarWebServiceProvider.get();
  }

  protected abstract <T extends CalendarTimeWindowViewContext> T getCalendarTimeWindowContext();

  /**
   * Handles the time window context.
   * @param context the context of the incoming request.
   */
  @POST
  @Path("calendars/context")
  @Produces(MediaType.APPLICATION_JSON)
  public <T extends CalendarTimeWindowViewContext> T view(WEB_COMPONENT_REQUEST_CONTEXT context) {
    CalendarViewType calendarViewType =
        CalendarViewType.from(context.getRequest().getParameter("view"));
    if (calendarViewType != null) {
      getCalendarTimeWindowContext().setViewType(calendarViewType);
    }
    String timeWindow = context.getRequest().getParameter("timeWindow");
    if (StringUtil.isDefined(timeWindow)) {
      switch (timeWindow) {
        case "previous":
          getCalendarTimeWindowContext().previous();
          break;
        case "next":
          getCalendarTimeWindowContext().next();
          break;
        case "today":
          getCalendarTimeWindowContext().today();
          break;
      }
    }
    return getCalendarTimeWindowContext();
  }

  @POST
  @Path("calendars/events/users/participation")
  @RedirectTo("{userPanelUri}")
  @LowestRoleAccess(SilverpeasRole.admin)
  public void viewParticipationOfUsers(WEB_COMPONENT_REQUEST_CONTEXT context) {
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
   * Gets the selection parameters about the view of user participation.<br/>
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
  @LowestRoleAccess(SilverpeasRole.admin)
  public void modifyAttendees(WEB_COMPONENT_REQUEST_CONTEXT context) {
    List<String> userIds = (List<String>) StringUtil
        .splitString(context.getRequest().getParameter("UserPanelCurrentUserIds"), ',');
    List<String> groupIds = (List<String>) StringUtil
        .splitString(context.getRequest().getParameter("UserPanelCurrentGroupIds"), ',');
    String selectionUri =
        initSelection(getAttendeeSelectionParams(), "attendees", userIds, groupIds);
    context.addRedirectVariable("userPanelUri", selectionUri);
  }

  /**
   * Gets the selection parameters about attendees.<br/>
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

  private String initSelection(SelectionUsersGroups sug, String goFunction, List<String> userIds,
      List<String> groupIds) {
    String url = ResourceLocator.getGeneralSettingBundle().getString("ApplicationURL") +
        URLUtil.getURL(getSpaceId(), getComponentId());
    String goUrl = url + goFunction;

    userPanelSelection.resetAll();
    userPanelSelection.setGoBackURL(goUrl);
    userPanelSelection.setElementSelectable(true);
    userPanelSelection.setSelectedElements(userIds);
    // TODO for now groups are not handled, but it will be the case in thr future
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
}
