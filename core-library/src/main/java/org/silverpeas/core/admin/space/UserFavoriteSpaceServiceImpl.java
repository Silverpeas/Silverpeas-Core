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
 * FLOSS exception.  You should have received a copy of the text describing
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
package org.silverpeas.core.admin.space;

import org.silverpeas.core.admin.space.model.UserFavoriteSpaceBean;
import org.silverpeas.core.admin.space.model.UserFavoriteSpaceVO;
import org.silverpeas.core.annotation.Service;
import org.silverpeas.core.persistence.jdbc.bean.*;
import org.silverpeas.core.admin.service.OrganizationController;
import org.silverpeas.core.util.ServiceProvider;
import org.silverpeas.kernel.logging.SilverLogger;

import javax.enterprise.inject.Default;
import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Service
@Singleton
@Default
@SuppressWarnings("deprecation")
public class UserFavoriteSpaceServiceImpl implements UserFavoriteSpaceService {

  private static final String USER_ID = "userId";

  public List<UserFavoriteSpaceVO> getListUserFavoriteSpace(String userId) {
    List<UserFavoriteSpaceVO> listUserFavoriteSpaces = new ArrayList<>();
    SilverpeasBeanDAO<UserFavoriteSpaceBean> dao;
    Collection<UserFavoriteSpaceBean> beansUserFavoriteSpaces;
    try {

      // find all message to display
      BeanCriteria criteria = BeanCriteria.addCriterion(USER_ID, Integer.parseInt(userId));
      dao = SilverpeasBeanDAOFactory.getDAO(UserFavoriteSpaceBean.class);
      beansUserFavoriteSpaces = dao.findBy(criteria);
      // if any
      if (!beansUserFavoriteSpaces.isEmpty()) {
        for (UserFavoriteSpaceBean userFavoriteSpaceBean : beansUserFavoriteSpaces) {
          listUserFavoriteSpaces.add(new UserFavoriteSpaceVO(userFavoriteSpaceBean.getUserId(),
              userFavoriteSpaceBean.getSpaceId()));
        }
      }
    } catch (PersistenceException e) {
      SilverLogger.getLogger(this)
          .error("Cannot get user favorite space for user {0}", new String[] {userId}, e);
    }

    return listUserFavoriteSpaces;
  }

  public boolean addUserFavoriteSpace(UserFavoriteSpaceVO ufsVO) {
    boolean result = false;
    SilverpeasBeanDAO<UserFavoriteSpaceBean> dao;
    try {
      if (isUserFavoriteSpaceAlreadyExist(ufsVO.getUserId(), ufsVO.getSpaceId())) {
        result = true;
      } else {
        // find all message to display
        dao = SilverpeasBeanDAOFactory.getDAO(UserFavoriteSpaceBean.class);
        dao.add(new UserFavoriteSpaceBean(ufsVO.getUserId(), ufsVO.getSpaceId()));
        result = true;
      }
    } catch (PersistenceException e) {
      SilverLogger.getLogger(this)
          .error("User Favorite space error for user {0}", new Integer[] {ufsVO.getUserId()}, e);
    }
    return result;
  }

  private boolean isUserFavoriteSpaceAlreadyExist(int userId, int spaceId) {
    boolean exist = false;
    SilverpeasBeanDAO<UserFavoriteSpaceBean> dao;
    Collection<UserFavoriteSpaceBean> beansUserFavoriteSpaces;
    try {
      BeanCriteria criteria = BeanCriteria.addCriterion(USER_ID, userId)
          .and("spaceId", spaceId);
      // find all message to display
      dao = SilverpeasBeanDAOFactory.getDAO(UserFavoriteSpaceBean.class);
      beansUserFavoriteSpaces = dao.findBy(criteria);
      // if any
      if (!beansUserFavoriteSpaces.isEmpty()) {
        exist = true;
      }
    } catch (PersistenceException e) {
      SilverLogger.getLogger(this)
          .error("User Favorite space error for user {0}", new Integer[] {userId}, e);
    }
    return exist;
  }

  public boolean removeUserFavoriteSpace(UserFavoriteSpaceVO ufsVO) {
    boolean result = false;
    SilverpeasBeanDAO<UserFavoriteSpaceBean> dao;
    BeanCriteria criteria = BeanCriteria.emptyCriteria();
    int removedUserId = ufsVO.getUserId();
    int removedSpaceId = ufsVO.getSpaceId();
    if (removedUserId == -1 && removedSpaceId == -1) {
      return true;
    } else {
      if (removedUserId != -1) {
        criteria.and(USER_ID, removedUserId);
      }
      if (removedSpaceId != -1) {
        criteria.and("spaceId", removedSpaceId);
      }
    }
    try {
      // find all message to display
      dao = SilverpeasBeanDAOFactory.getDAO(UserFavoriteSpaceBean.class);
      dao.removeBy(criteria);
      result = true;
    } catch (PersistenceException e) {
      SilverLogger.getLogger(this)
          .error("Cannot remove user favorite space for user {0}",
              new Integer[] {ufsVO.getUserId()}, e);
    }
    return result;
  }

  /**
   * @param listUFS : the list of user favorite space
   * @param space: a space instance
   * @return true if list of user favorites space contains spaceId identifier, false else if
   */
  public boolean isUserFavoriteSpace(List<UserFavoriteSpaceVO> listUFS, SpaceInstLight space) {
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
  public boolean containsFavoriteSubSpace(SpaceInstLight space,
      List<UserFavoriteSpaceVO> listUFS, String userId) {
    boolean result = false;
    OrganizationController organisationController =
        ServiceProvider.getService(OrganizationController.class);
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
