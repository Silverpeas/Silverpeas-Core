/*
 * Copyright (C) 2000 - 2013 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection withWriter Free/Libre
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
package org.silverpeas.core.web.mvc.processor;

import static org.silverpeas.core.util.StringUtil.getBooleanValue;
import static org.silverpeas.core.util.StringUtil.isDefined;
import org.silverpeas.core.web.selection.Selection;

import javax.inject.Singleton;
import javax.servlet.http.HttpServletRequest;

/**
 * Processor of the selection of some users and user groups with a dedicated form. This processor
 * both prepares the required resources required by the selection form and provides to the selection
 * caller (a component instance) the users and group users that were selected throught the form.
 */
@Singleton
public class UserAndGroupSelectionProcessor {
  // resource provided to the selection form

  private static final String SELECTION_PARAMETER = "SELECTION";
  // parameters of the selection form
  private static final String USER_GROUP_SELECTION_PARAMETER = "UserOrGroupSelection";
  private static final String USER_SELECTION_PARAMETER = "UserSelection";
  private static final String GROUP_SELECTION_PARAMETER = "GroupSelection";
  // URI of the selection form
  private static final String SELECTION_FORM_PATH = Selection.USER_SELECTION_PANEL_PATH;

  protected UserAndGroupSelectionProcessor() {

  }

  /**
   * Prepares the specified selection to be carried by the specified HTTP request.
   *
   * @param selection an object representing the selection that will be done with the user/group
   * selection panel. The users or groups selected by the user will be set into this object.
   * @param request the HTTP request driving the web flow to the user/group selection panel.
   */
  public void prepareSelection(final Selection selection, final HttpServletRequest request) {
    request.setAttribute(SELECTION_PARAMETER, selection);
  }

  /**
   * Processes the specified selection object from the parameters set into the incoming HTTP
   * request.
   *
   * @param selection the object representing the selection done by the user.
   * @param request the HTTP request with the actually selected users and groups.
   * @return the next destination that is the view or the service that has to use the selection.
   */
  public String processSelection(final Selection selection, final HttpServletRequest request) {
    if (isSelectionValidated(request)) {
      String userSelection = request.getParameter(USER_SELECTION_PARAMETER);
      String[] selectedUsers = null;
      if (isDefined(userSelection)) {
        selectedUsers = userSelection.split(",");
      }
      String groupSelection = request.getParameter(GROUP_SELECTION_PARAMETER);
      String[] selectedGroups = null;
      if (isDefined(groupSelection)) {
        selectedGroups = groupSelection.split(",");
      }
      selection.setSelectedElements(selectedUsers);
      selection.setSelectedSets(selectedGroups);

      // a workaround to clients that use the user panel within a window popup without taking avantage
      // of the hot settings
      if (!selection.isHotSetting() && selection.isPopupMode()) {
        request.setAttribute("RedirectionURL", selection.getGoBackURL());
        return "/selection/jsp/redirect.jsp";
      }
    // a workaround to clients that use the user panel within a window popup without taking avantage
    // of the hot settings
    } else if (!selection.isHotSetting() && selection.isPopupMode()) {
      request.setAttribute("RedirectionURL", selection.getCancelURL());
        return "/selection/jsp/redirect.jsp";
    }

    return null;
  }

  /**
   * Is a selection of users or groups is asked by a client (a JSP or request router)?
   *
   * @param destination the destination of a request processing (a view rendering the response, a
   * service, ...)
   * @return true if the user panel is asked, false otherwise.
   */
  public boolean isSelectionAsked(String destination) {
    return isDefined(destination) && destination.endsWith(SELECTION_FORM_PATH);
  }

  /**
   * Is the selection was validated by the user? A selection is said validated wether the user
   * pushed the validation button of the selection panel, whatever some users or groups were
   * actually selected.
   *
   * @param request the HTTP request embodied the selection validation event.
   * @return true if the user validated a selection of users or groups, false if it canceled it.
   */
  public boolean isSelectionValidated(final HttpServletRequest request) {
    String selectionDone = request.getParameter(USER_GROUP_SELECTION_PARAMETER);
    return isDefined(selectionDone) && getBooleanValue(selectionDone);
  }

  /**
   * Is the incoming request comes from the user/group selection panel?
   *
   * @param request the incoming HTTP request.
   * @return true if the request was triggered by the user/group selection panel.
   */
  public boolean isComeFromSelectionPanel(final HttpServletRequest request) {
    String selectionDone = request.getParameter(USER_GROUP_SELECTION_PARAMETER);
    return isDefined(selectionDone);
  }
}
