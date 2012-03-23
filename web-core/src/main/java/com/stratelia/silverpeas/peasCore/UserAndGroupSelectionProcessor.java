/*
 * Copyright (C) 2000 - 2012 Silverpeas
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
 * "http://www.silverpeas.org/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.stratelia.silverpeas.peasCore;

import static com.silverpeas.util.StringUtil.getBooleanValue;
import static com.silverpeas.util.StringUtil.isDefined;
import com.stratelia.silverpeas.selection.Selection;
import javax.servlet.http.HttpServletRequest;

/**
 * Processor of the selection of some users and user groups with a dedicated form.
 * This processor both prepares the required resources required by the selection form and provides
 * to the selection caller (a component instance) the users and group users that were selected
 * throught the form.
 */
public class UserAndGroupSelectionProcessor {
  // resource provided to the selection form
  private static final String SELECTION_PARAMETER = "SELECTION";
  
  // parameters of the selection form
  private static final String USER_GROUP_SELECTION_PARAMETER = "UserOrGroupSelection";
  private static final String USER_SELECTION_PARAMETER = "UserSelection";
  private static final String GROUP_SELECTION_PARAMETER = "GroupSelection";
  
  // URI of the selection form
  private static final String SELECTION_FORM_PATH = Selection.USER_SELECTION_PANEL_PATH;
  
  public void prepareSelection(final Selection selection, final HttpServletRequest request) {
    request.setAttribute(SELECTION_PARAMETER, selection);
  }
  
  public void processSelection(final Selection selection, final HttpServletRequest request) {
    String userSelection = request.getParameter(USER_SELECTION_PARAMETER);
    String[] selectedUsers = null;
    if (isDefined(userSelection)) {
      selectedUsers = userSelection.split(",");
    }
    String groupSelection = request.getParameter(GROUP_SELECTION_PARAMETER);
    String[] selectedGroups = null;
    if(isDefined(groupSelection)) {
      selectedGroups = groupSelection.split(",");
    }
    selection.setSelectedElements(selectedUsers);
    selection.setSelectedSets(selectedGroups);
  }
  
  public boolean isSelectionAsked(String destination) {
    return isDefined(destination) && destination.endsWith(SELECTION_FORM_PATH);
  }
  
  public boolean isSelectionDone(final HttpServletRequest request) {
    String selectionDone = request.getParameter(USER_GROUP_SELECTION_PARAMETER);
    return isDefined(selectionDone) && getBooleanValue(selectionDone);
  }
}
