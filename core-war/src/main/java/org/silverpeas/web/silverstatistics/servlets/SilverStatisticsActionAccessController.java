/*
 * Copyright (C) 2000 - 2015 Silverpeas
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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package org.silverpeas.web.silverstatistics.servlets;

import org.silverpeas.core.admin.user.constant.UserAccessLevel;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author ebonnet
 */
public class SilverStatisticsActionAccessController {

  private Map<String, List<UserAccessLevel>> actionRole =
      new HashMap<String, List<UserAccessLevel>>();

  public SilverStatisticsActionAccessController() {
    actionRole.put("Main", Collections.singletonList(UserAccessLevel.ADMINISTRATOR));
  }


  /**
   * Check if user role has right access to the given action
   * @param action the checked action
   * @param level the current user access level
   * @return true if given user access level has right access to the action
   */
  public boolean hasRightAccess(String action, UserAccessLevel level) {
    boolean actionExist = actionRole.containsKey(action);
    if (actionExist && actionRole.get(action).contains(level)) {
      return true;
    } else if (!actionExist) {
      return true;
    }
    return false;
  }
}
