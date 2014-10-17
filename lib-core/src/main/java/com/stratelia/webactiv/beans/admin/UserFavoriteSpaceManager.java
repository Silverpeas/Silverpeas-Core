/**
 * Copyright (C) 2000 - 2013 Silverpeas
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

package com.stratelia.webactiv.beans.admin;

import java.util.List;

import org.silverpeas.core.admin.OrganisationController;

import com.stratelia.webactiv.organization.UserFavoriteSpaceVO;
import org.silverpeas.util.ServiceProvider;

public class UserFavoriteSpaceManager {

  /**
   * @param listUFS : the list of user favorite space
   * @param space: a space instance
   * @return true if list of user favorites space contains spaceId identifier, false else if
   */
  public static boolean isUserFavoriteSpace(List<UserFavoriteSpaceVO> listUFS, SpaceInstLight space) {
    boolean result = false;
    if (listUFS != null && !listUFS.isEmpty()) {
      for (UserFavoriteSpaceVO userFavoriteSpaceVO : listUFS) {
        if (space.getLocalId() == userFavoriteSpaceVO.getSpaceId()) {
          return true;
        }
      }
    }
    return result;
  }

  /**
   * @param space : space instance
   * @param listUFS : the list of user favorite space
   * @return true if the current space contains user favorites sub space, false else if
   */
  public static boolean containsFavoriteSubSpace(SpaceInstLight space,
      List<UserFavoriteSpaceVO> listUFS, String userId) {
    boolean result = false;
    OrganisationController organisationController =
        ServiceProvider.getService(OrganisationController.class);
    String[] userSubSpaceIds = organisationController.getAllowedSubSpaceIds(userId, space.getId());
    for (String curSpaceId : userSubSpaceIds) {
      // Recursive loop on each subspace
      SpaceInstLight subSpace = organisationController.getSpaceInstLightById(curSpaceId);
      if (isUserFavoriteSpace(listUFS, subSpace) ||
          containsFavoriteSubSpace(subSpace, listUFS, userId)) {
        return true;
      }
    }
    return result;
  }
}
