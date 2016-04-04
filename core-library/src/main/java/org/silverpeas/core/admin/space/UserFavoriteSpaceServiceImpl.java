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

import org.silverpeas.core.admin.space.model.UserFavoriteSpaceBean;
import org.silverpeas.core.admin.space.model.UserFavoriteSpaceVO;
import org.silverpeas.core.persistence.jdbc.bean.IdPK;
import org.silverpeas.core.persistence.jdbc.bean.PersistenceException;
import org.silverpeas.core.persistence.jdbc.bean.SilverpeasBeanDAO;
import org.silverpeas.core.persistence.jdbc.bean.SilverpeasBeanDAOFactory;
import org.silverpeas.core.admin.service.OrganizationController;
import org.silverpeas.core.util.ServiceProvider;
import org.silverpeas.core.util.logging.SilverLogger;

import javax.enterprise.inject.Default;
import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Singleton
@Default
public class UserFavoriteSpaceServiceImpl implements UserFavoriteSpaceService {

  public List<UserFavoriteSpaceVO> getListUserFavoriteSpace(String userId) {
    List<UserFavoriteSpaceVO> listUserFavoriteSpaces = new ArrayList<>();
    SilverpeasBeanDAO<UserFavoriteSpaceBean> dao;
    IdPK pk = new IdPK();
    Collection<UserFavoriteSpaceBean> beansUserFavoriteSpaces;
    String whereClause = " userid = " + userId;
    try {

      // find all message to display
      dao = SilverpeasBeanDAOFactory
          .getDAO("org.silverpeas.core.admin.space.model.UserFavoriteSpaceBean");
      beansUserFavoriteSpaces = dao.findByWhereClause(pk, whereClause);
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
        dao = SilverpeasBeanDAOFactory
            .getDAO("org.silverpeas.core.admin.space.model.UserFavoriteSpaceBean");
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
    IdPK pk = new IdPK();
    Collection<UserFavoriteSpaceBean> beansUserFavoriteSpaces;
    String whereClause = " userid = " + userId + " AND spaceId = " + spaceId;
    try {

      // find all message to display
      dao = SilverpeasBeanDAOFactory
          .getDAO("org.silverpeas.core.admin.space.model.UserFavoriteSpaceBean");
      beansUserFavoriteSpaces = dao.findByWhereClause(pk, whereClause);
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
    StringBuilder whereBuff = new StringBuilder();
    int removedUserId = ufsVO.getUserId();
    int removedSpaceId = ufsVO.getSpaceId();
    if (removedUserId == -1 && removedSpaceId == -1) {
      return true;
    } else {
      boolean firstCondition = false;
      if (removedUserId != -1) {
        whereBuff.append(" userid=").append(removedUserId);
        firstCondition = true;
      }
      if (removedSpaceId != -1) {
        if (firstCondition) {
          whereBuff.append(" AND");
        }
        whereBuff.append(" spaceid=").append(removedSpaceId);
      }
    }
    try {
      // find all message to display
      dao = SilverpeasBeanDAOFactory
          .getDAO("org.silverpeas.core.admin.space.model.UserFavoriteSpaceBean");
      dao.removeWhere(new IdPK(), whereBuff.toString());
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

  public UserFavoriteSpaceServiceImpl() {
  }
}
