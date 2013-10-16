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

import com.silverpeas.interestCenter.InterestCenterRuntimeException;
import com.silverpeas.interestCenter.model.InterestCenter;

import com.stratelia.webactiv.util.DBUtil;
import com.stratelia.webactiv.util.JNDINames;

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
          "InterestCenter.CANNOT_LOAD_IC", String.valueOf(userID), e);
    } catch (DAOException e) {
      throw new InterestCenterRuntimeException("InterestCenterBmEJB.getICByUserID()",
          "InterestCenter.CANNOT_LOAD_IC", String.valueOf(userID), e);
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
          "InterestCenter.CANNOT_CREATE_IC", ic.toString(), e);
    } catch (DAOException e) {
      throw new InterestCenterRuntimeException("InterestCenterBmEJB.createIC()",
          "InterestCenter.CANNOT_CREATE_IC", ic.toString(), e);
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
          "InterestCenter.CANNOT_UPDATE_IC", ic.toString(), e);
    } catch (DAOException e) {
      throw new InterestCenterRuntimeException("InterestCenterBmEJB.updateIC()",
          "InterestCenter.CANNOT_UPDATE_IC", ic.toString(), e);
    } finally {
      DBUtil.close(con);
    }
  }

  @Override
  @TransactionAttribute(TransactionAttributeType.SUPPORTS)
  public void removeICByPK(List<Integer> pks) {
    Connection con = DBUtil.makeConnection(JNDINames.INTEREST_CENTER_DATASOURCE);
    try {
      InterestCenterDAO.removeICByPK(con, pks);
    } catch (SQLException e) {
      throw new InterestCenterRuntimeException("InterestCenterBmEJB.removeICByPK(ArrayList pks)",
          "InterestCenter.CANNOT_DELETE_IC", pks.toString(), e);
    } catch (DAOException e) {
      throw new InterestCenterRuntimeException("InterestCenterBmEJB.removeICByPK(ArrayList pks)",
          "InterestCenter.CANNOT_DELETE_IC", pks.toString(), e);
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
          "InterestCenter.CANNOT_DELETE_IC", String.valueOf(pk), e);
    } catch (DAOException e) {
      throw new InterestCenterRuntimeException("InterestCenterBmEJB.removeICByPK(int pk)",
          "InterestCenter.CANNOT_DELETE_IC", String.valueOf(pk), e);
    } finally {
      DBUtil.close(con);
    }
  }
}
