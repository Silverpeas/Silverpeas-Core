/**
 * Copyright (C) 2000 - 2013 Silverpeas
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
package com.silverpeas.interestCenter.ejb;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;

import com.silverpeas.accesscontrol.ForbiddenRuntimeException;
import com.silverpeas.interestCenter.InterestCenterRuntimeException;
import com.silverpeas.interestCenter.model.InterestCenter;

import org.silverpeas.util.DBUtil;
import org.silverpeas.util.JNDINames;
import org.silverpeas.util.exception.SilverpeasRuntimeException;

/**
 * InterestCenterBm EJB implementation for detailed comments for each method see remote interface
 * class
 *
 * @see InterestCenterBm
 */
@Stateless(name = "InterestCenter", description =
    "Stateless session bean to manage interest centers.")
@TransactionAttribute(TransactionAttributeType.SUPPORTS)
public class InterestCenterBmEJB implements InterestCenterBm {

  private static final long serialVersionUID = -5867239072798551540L;

  @Override
  public List<InterestCenter> getICByUserID(int userID) {
    Connection con = DBUtil.makeConnection(JNDINames.INTEREST_CENTER_DATASOURCE);
    try {
      return InterestCenterDAO.getICByUserID(con, userID);
    } catch (SQLException e) {
      throw new InterestCenterRuntimeException("InterestCenterBmEJB.getICByUserID()",
          "Pdc.CANNOT_GET_INTEREST_CENTERS", String.valueOf(userID), e);
    } catch (DAOException e) {
      throw new InterestCenterRuntimeException("InterestCenterBmEJB.getICByUserID()",
          "Pdc.CANNOT_GET_INTEREST_CENTERS", String.valueOf(userID), e);
    } finally {
      DBUtil.close(con);
    }
  }

  @Override
  public InterestCenter getICByID(int icPK) {
    Connection con = DBUtil.makeConnection(JNDINames.INTEREST_CENTER_DATASOURCE);
    try {
      return InterestCenterDAO.getICByPK(con, icPK);
    } catch (SQLException e) {
      throw new InterestCenterRuntimeException("InterestCenterBmEJB.getICByID()",
          "InterestCenter.CANNOT_LOAD_LIST_OF_IC", String.valueOf(icPK), e);
    } catch (DAOException e) {
      throw new InterestCenterRuntimeException("InterestCenterBmEJB.getICByID()",
          "InterestCenter.CANNOT_LOAD_LIST_OF_IC", String.valueOf(icPK), e);
    } finally {
      DBUtil.close(con);
    }
  }

  @Override
  @TransactionAttribute(TransactionAttributeType.SUPPORTS)
  public int createIC(InterestCenter ic) {
    Connection con = DBUtil.makeConnection(JNDINames.INTEREST_CENTER_DATASOURCE);
    try {
      return InterestCenterDAO.createIC(con, ic);
    } catch (SQLException e) {
      throw new InterestCenterRuntimeException("InterestCenterBmEJB.createIC()",
          "Pdc.CANNOT_CREATE_INTEREST_CENTER", ic.toString(), e);
    } catch (DAOException e) {
      throw new InterestCenterRuntimeException("InterestCenterBmEJB.createIC()",
          "Pdc.CANNOT_CREATE_INTEREST_CENTER", ic.toString(), e);
    } finally {
      DBUtil.close(con);
    }
  }

  @Override
  @TransactionAttribute(TransactionAttributeType.SUPPORTS)
  public void updateIC(InterestCenter ic) {
    Connection con = DBUtil.makeConnection(JNDINames.INTEREST_CENTER_DATASOURCE);
    try {
      InterestCenterDAO.updateIC(con, ic);
    } catch (SQLException e) {
      throw new InterestCenterRuntimeException("InterestCenterBmEJB.updateIC()",
          "Pdc.CANNOT_UPDATE_INTEREST_CENTER", ic.toString(), e);
    } catch (DAOException e) {
      throw new InterestCenterRuntimeException("InterestCenterBmEJB.updateIC()",
          "Pdc.CANNOT_UPDATE_INTEREST_CENTER", ic.toString(), e);
    } finally {
      DBUtil.close(con);
    }
  }

  @Override
  @TransactionAttribute(TransactionAttributeType.SUPPORTS)
  public void removeICByPK(List<Integer> pks, String userId) {
    Connection con = DBUtil.makeConnection(JNDINames.INTEREST_CENTER_DATASOURCE);
    
    try {
      //check rights : check that the current user has the rights to delete the interest center
      int userIdInt = Integer.parseInt(userId);
      for (Integer icPk : pks) {
        InterestCenter interestCenter = getICByID(icPk);
        
        if(userIdInt != interestCenter.getOwnerID()) {
          throw new ForbiddenRuntimeException("InterestCenterBmEJB.removeICByPK(ArrayList pks)",
            SilverpeasRuntimeException.ERROR, "peasCore.RESOURCE_ACCESS_UNAUTHORIZED", "interest center id="+icPk+", userId="+userId);
        }
      }
    
      //remove
      InterestCenterDAO.removeICByPK(con, pks);
      
    } catch (SQLException e) {
      throw new InterestCenterRuntimeException("InterestCenterBmEJB.removeICByPK(ArrayList pks)",
          "Pdc.CANNOT_DELETE_INTEREST_CENTERS", pks.toString(), e);
    } catch (DAOException e) {
      throw new InterestCenterRuntimeException("InterestCenterBmEJB.removeICByPK(ArrayList pks)",
          "Pdc.CANNOT_DELETE_INTEREST_CENTERS", pks.toString(), e);
    } finally {
      DBUtil.close(con);
    }
  }

  @Override
  @TransactionAttribute(TransactionAttributeType.SUPPORTS)
  public void removeICByPK(int pk) {
    Connection con = DBUtil.makeConnection(JNDINames.INTEREST_CENTER_DATASOURCE);
    try {
      InterestCenterDAO.removeICByPK(con, pk);
    } catch (SQLException e) {
      throw new InterestCenterRuntimeException("InterestCenterBmEJB.removeICByPK(int pk)",
          "Pdc.CANNOT_DELETE_INTEREST_CENTER", String.valueOf(pk), e);
    } catch (DAOException e) {
      throw new InterestCenterRuntimeException("InterestCenterBmEJB.removeICByPK(int pk)",
          "Pdc.CANNOT_DELETE_INTEREST_CENTER", String.valueOf(pk), e);
    } finally {
      DBUtil.close(con);
    }
  }
}
