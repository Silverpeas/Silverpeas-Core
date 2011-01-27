/**
 * Copyright (C) 2000 - 2009 Silverpeas
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

package com.stratelia.webactiv.beans.admin;

import java.util.List;

import com.stratelia.webactiv.organization.UserFavoriteSpaceVO;

public class UserFavoriteSpaceManager {
  /**
   * Default Constructor
   */
  public UserFavoriteSpaceManager() {
  }

  /**
   * @param listUFS : the list of user favorite space
   * @param spaceId : space identifier
   * @return true if list of user favorites space contains spaceId identifier, false else if
   */
  public static boolean isUserFavoriteSpace(List<UserFavoriteSpaceVO> listUFS, String spaceId) {
    boolean result = false;
    String cleanSpaceId =(spaceId.startsWith(Admin.SPACE_KEY_PREFIX)) ? spaceId.substring(
        Admin.SPACE_KEY_PREFIX.length()) : spaceId;
    if (listUFS != null && !listUFS.isEmpty()) {
      for (UserFavoriteSpaceVO userFavoriteSpaceVO : listUFS) {
        if (Integer.parseInt(cleanSpaceId) == userFavoriteSpaceVO.getSpaceId()) {
          return true;
        }
      }
    }
    return result;
  }

  /**
   * @param spaceId : space identifier
   * @param listUFS : the list of user favorite space
   * @param orgaController : the OrganizationController object
   * @return true if the current space contains user favorites sub space, false else if
   */
  public static boolean containsFavoriteSubSpace(String spaceId, List<UserFavoriteSpaceVO> listUFS,
      OrganizationController orgaController, String userId) {
    boolean result = false;
    String cleanSpaceId =
        (spaceId.startsWith(Admin.SPACE_KEY_PREFIX)) ? spaceId.substring(Admin.SPACE_KEY_PREFIX
        .length()) : spaceId;
    String[] userSubSpaceIds = orgaController.getAllowedSubSpaceIds(userId, cleanSpaceId);
    for (String curSpaceId : userSubSpaceIds) {
      // Recursive loop on each subspace
      if (isUserFavoriteSpace(listUFS, curSpaceId) ||
          containsFavoriteSubSpace(curSpaceId, listUFS, orgaController, userId)) {
        return true;
      }
    }
    return result;
  }
}
