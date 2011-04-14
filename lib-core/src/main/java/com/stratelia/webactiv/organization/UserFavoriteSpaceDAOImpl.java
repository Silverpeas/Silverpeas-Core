/**
 * Copyright (C) 2000 - 2011 Silverpeas
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

package com.stratelia.webactiv.organization;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.persistence.IdPK;
import com.stratelia.webactiv.persistence.PersistenceException;
import com.stratelia.webactiv.persistence.SilverpeasBeanDAO;
import com.stratelia.webactiv.persistence.SilverpeasBeanDAOFactory;

public class UserFavoriteSpaceDAOImpl implements UserFavoriteSpaceDAO {

  public List<UserFavoriteSpaceVO> getListUserFavoriteSpace(String userId) {
    List<UserFavoriteSpaceVO> listUserFavoriteSpaces = new ArrayList<UserFavoriteSpaceVO>();
    SilverpeasBeanDAO dao;
    IdPK pk = new IdPK();
    Collection<UserFavoriteSpaceBean> beansUserFavoriteSpaces = null;
    String whereClause = " userid = " + userId;
    try {

      // find all message to display
      dao = SilverpeasBeanDAOFactory
          .getDAO("com.stratelia.webactiv.organization.UserFavoriteSpaceBean");
      beansUserFavoriteSpaces = dao.findByWhereClause(pk, whereClause);
      // if any
      if (!beansUserFavoriteSpaces.isEmpty()) {
        for (UserFavoriteSpaceBean userFavoriteSpaceBean : beansUserFavoriteSpaces) {
          listUserFavoriteSpaces.add(new UserFavoriteSpaceVO(userFavoriteSpaceBean.getUserId(),
              userFavoriteSpaceBean.getSpaceId()));
        }
      }
    } catch (PersistenceException e) {
      SilverTrace.error("server", "UserFavoriteSpaceDAO.getListUserFavoriteSpace()",
          "server.EX_CANT_GET_FAVORITE_SPACES", "userid=" + userId, e);
    }

    return listUserFavoriteSpaces;
  }

  public boolean addUserFavoriteSpace(UserFavoriteSpaceVO ufsVO) {
    boolean result = false;
    SilverpeasBeanDAO dao;
    try {
      if (isUserFavoriteSpaceAlreadyExist(ufsVO.getUserId(), ufsVO.getSpaceId())) {
        result = true;
      } else {
        // find all message to display
        dao = SilverpeasBeanDAOFactory
            .getDAO("com.stratelia.webactiv.organization.UserFavoriteSpaceBean");
        dao.add(new UserFavoriteSpaceBean(ufsVO.getUserId(), ufsVO.getSpaceId()));
        result = true;
      }
    } catch (PersistenceException e) {
      SilverTrace.error("server", "UserFavoriteSpaceDAO.addUserFavoriteSpace", "", "userid= " +
          ufsVO.getUserId() + ", spaceid=" + ufsVO.getSpaceId(), e);
    }
    return result;
  }

  private boolean isUserFavoriteSpaceAlreadyExist(int userId, int spaceId) {
    boolean exist = false;
    SilverpeasBeanDAO dao;
    IdPK pk = new IdPK();
    Collection<UserFavoriteSpaceBean> beansUserFavoriteSpaces = null;
    String whereClause = " userid = " + userId + " AND spaceId = " + spaceId;
    try {

      // find all message to display
      dao = SilverpeasBeanDAOFactory
          .getDAO("com.stratelia.webactiv.organization.UserFavoriteSpaceBean");
      beansUserFavoriteSpaces = dao.findByWhereClause(pk, whereClause);
      // if any
      if (!beansUserFavoriteSpaces.isEmpty()) {
        exist = true;
      }
    } catch (PersistenceException e) {
      SilverTrace.error("server", "UserFavoriteSpaceDAO.getListUserFavoriteSpace()",
          "server.EX_CANT_GET_FAVORITE_SPACES", "userid=" + userId, e);
    }
    return exist;
  }

  public boolean removeUserFavoriteSpace(UserFavoriteSpaceVO ufsVO) {
    boolean result = false;
    SilverpeasBeanDAO dao;
    StringBuffer whereBuff = new StringBuffer();
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
          .getDAO("com.stratelia.webactiv.organization.UserFavoriteSpaceBean");
      dao.removeWhere(new IdPK(), whereBuff.toString());
      result = true;
    } catch (PersistenceException e) {
      SilverTrace.error("server", "UserFavoriteSpaceDAO.addUserFavoriteSpace", "", "userid= " +
          ufsVO.getUserId() + ", spaceid=" + ufsVO.getSpaceId(), e);
    }
    return result;
  }

}
