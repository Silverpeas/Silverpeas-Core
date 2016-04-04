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

package org.silverpeas.core.admin.space;

import org.silverpeas.core.admin.space.model.UserFavoriteSpaceVO;

import java.util.List;

/**
 * UserFavoriteSpace DAO interface
 */
public interface UserFavoriteSpaceService {

  /**
   * Retrieve the list of user favorite space
   * @param userId : the user identifier
   * @return the list of User Favorite Space Value Object
   */
  public List<UserFavoriteSpaceVO> getListUserFavoriteSpace(String userId);

  /**
   * Add given User Favorite Space Value Object parameter in Database <br>
   * @param ufsVO a UserFavoriteSpaceVO
   * @return true if action was successful, false otherwise
   */
  public boolean addUserFavoriteSpace(UserFavoriteSpaceVO ufsVO);

  /**
   * Remove given User Favorite Space Value Object from Database
   * <ul>
   * <li>remove one record if ufsVO.userId and ufsVO.spaceId is not null</li>
   * <li>remove all ufsVO.spaceId if ufsVO.userId is null</li>
   * <li>remove all ufsVO.userId if ufsVO.spaceId is null</li>
   * </ul>
   * @param ufsVO a UserFavoriteSpaceVO
   * @return true if action was successful, false otherwise
   */
  public boolean removeUserFavoriteSpace(UserFavoriteSpaceVO ufsVO);

  /**
   * Is the specified space instance is one of the user's favorite spaces?
   * @param listUFS : the list of user favorite space
   * @param space: a space instance
   * @return true if list of user favorites space contains spaceId identifier, false otherwise.
   */
  public boolean isUserFavoriteSpace(List<UserFavoriteSpaceVO> listUFS, SpaceInstLight space);

  /**
   * Is the specified space contains the specified user's favorite spaces as subspaces?
   * @param space : space instance
   * @param listUFS : the list of user favorite space
   * @return true if the current space contains user favorites sub space, false otherwise
   */
  public boolean containsFavoriteSubSpace(SpaceInstLight space, List<UserFavoriteSpaceVO> listUFS,
      String userId);

}
