/*
 * Copyright (C) 2000 - 2016 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU Affero General Public License as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of the GPL, you may
 * redistribute this Program in connection with Free/Libre Open Source Software ("FLOSS")
 * applications as described in Silverpeas's FLOSS exception. You should have received a copy of the
 * text describing the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License along with this program.
 * If not, see <http://www.gnu.org/licenses/>.
 */

/*
 * Aliaksei_Budnikau
 * Date: Oct 14, 2002
 */
package org.silverpeas.core.pdc.interests.service;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import javax.inject.Singleton;
import javax.transaction.Transactional;

import org.silverpeas.core.security.authorization.ForbiddenRuntimeException;
import org.silverpeas.core.pdc.interests.model.Interests;

import org.silverpeas.core.persistence.jdbc.DBUtil;
import org.silverpeas.core.exception.SilverpeasRuntimeException;

/**
 * Default implementation of the service on the interest centers of users.
 *
 * @see InterestsService
 */
@Singleton
@Transactional
public class DefaultInterestsService implements InterestsService {

  @Override
  public List<Interests> getInterestsByUserId(int userID) {
    try(Connection con = DBUtil.openConnection()) {
      return InterestsDAO.getInterestsByUserID(con, userID);
    } catch (SQLException | InterestsDAOException e) {
      throw new InterestsRuntimeException("DefaultInterestsService.getInterestsByUserId()",
          "Pdc.CANNOT_GET_INTEREST_CENTERS", String.valueOf(userID), e);
    }
  }

  @Override
  public Interests getInterestsById(int id) {
    try(Connection con = DBUtil.openConnection()) {
      return InterestsDAO.getInterestsByPK(con, id);
    } catch (SQLException | InterestsDAOException e) {
      throw new InterestsRuntimeException("DefaultInterestsService.getInterestsByPK()",
          "Interests.CANNOT_LOAD_LIST_OF_IC", String.valueOf(id), e);
    }
  }

  @Override
  public int createInterests(Interests interests) {
    try(Connection con = DBUtil.openConnection()) {
      return InterestsDAO.saveInterests(con, interests);
    } catch (SQLException | InterestsDAOException e) {
      throw new InterestsRuntimeException("DefaultInterestsService.createInterests()",
          "Pdc.CANNOT_CREATE_INTEREST_CENTER", interests.toString(), e);
    }
  }

  @Override
  public void updateInterests(Interests interests) {
    try(Connection con = DBUtil.openConnection()) {
      InterestsDAO.updateInterests(con, interests);
    } catch (SQLException | InterestsDAOException e) {
      throw new InterestsRuntimeException("DefaultInterestsService.updateInterests()",
          "Pdc.CANNOT_UPDATE_INTEREST_CENTER", interests.toString(), e);
    }
  }

  @Override
  public void removeInterestsById(List<Integer> ids, String userId) {
    try(Connection con = DBUtil.openConnection()) {
      //check rights : check that the current user has the rights to delete the interest center
      int userIdInt = Integer.parseInt(userId);
      for (Integer icPk : ids) {
        Interests interests = getInterestsById(icPk);

        if(userIdInt != interests.getOwnerID()) {
          throw new ForbiddenRuntimeException("DefaultInterestsService.removeInterestsById(ArrayList pks)",
            SilverpeasRuntimeException.ERROR, "peasCore.RESOURCE_ACCESS_UNAUTHORIZED", "interest center id="+icPk+", userId="+userId);
        }
      }

      //remove
      InterestsDAO.removeInterestsByPK(con, ids);

    } catch (SQLException | InterestsDAOException e) {
      throw new InterestsRuntimeException("DefaultInterestsService.removeInterestsById(ArrayList pks)",
          "Pdc.CANNOT_DELETE_INTEREST_CENTERS", ids.toString(), e);
    }
  }

  @Override
  public void removeInterestsById(int id) {
    try(Connection con = DBUtil.openConnection()) {
      InterestsDAO.removeInterestsByPK(con, id);
    } catch (SQLException | InterestsDAOException e) {
      throw new InterestsRuntimeException("DefaultInterestsService.removeInterestsById(int pk)",
          "Pdc.CANNOT_DELETE_INTEREST_CENTER", String.valueOf(id), e);
    }
  }
}
