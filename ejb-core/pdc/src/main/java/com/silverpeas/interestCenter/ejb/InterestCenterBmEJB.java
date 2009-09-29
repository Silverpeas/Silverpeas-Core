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
 * FLOSS exception.  You should have recieved a copy of the text describing
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
/*
 * Aliaksei_Budnikau
 * Date: Oct 14, 2002
 */
package com.silverpeas.interestCenter.ejb;

import com.silverpeas.interestCenter.InterestCenterRuntimeException;
import com.silverpeas.interestCenter.model.InterestCenter;
import com.stratelia.webactiv.util.DBUtil;
import com.stratelia.webactiv.util.JNDINames;
import com.stratelia.silverpeas.silvertrace.SilverTrace;

import javax.ejb.SessionBean;
import javax.ejb.EJBException;
import javax.ejb.SessionContext;
import javax.ejb.CreateException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.sql.Connection;

/**
 * InterestCenterBm EJB implementation for detailed comments for each method see
 * remote interface class
 * 
 * @see InterestCenterBm
 */
public class InterestCenterBmEJB implements SessionBean {

  public ArrayList getICByUserID(int userID) {
    Connection con = openConnection();
    ArrayList result = null;
    try {
      result = InterestCenterDAO.getICByUserID(con, userID);
    } catch (Exception e) {
      throw new InterestCenterRuntimeException(
          "InterestCenterBmEJB.getICByUserID()",
          "InterestCenter.CANNOT_LOAD_IC", String.valueOf(userID), e);
    } finally {
      closeConnection(con);
    }
    return result;
  }

  public InterestCenter getICByID(int icPK) {
    Connection con = openConnection();
    InterestCenter result = null;
    try {
      result = InterestCenterDAO.getICByPK(con, icPK);
    } catch (Exception e) {
      throw new InterestCenterRuntimeException(
          "InterestCenterBmEJB.getICByID()",
          "InterestCenter.CANNOT_LOAD_LIST_OF_IC", String.valueOf(icPK), e);
    } finally {
      closeConnection(con);
    }
    return result;
  }

  public int createIC(InterestCenter ic) {
    Connection con = openConnection();
    int result = -1;
    try {
      result = InterestCenterDAO.createIC(con, ic);
    } catch (Exception e) {
      throw new InterestCenterRuntimeException(
          "InterestCenterBmEJB.createIC()", "InterestCenter.CANNOT_CREATE_IC",
          ic.toString(), e);
    } finally {
      closeConnection(con);
    }
    return result;
  }

  public void updateIC(InterestCenter ic) {
    Connection con = openConnection();
    try {
      InterestCenterDAO.updateIC(con, ic);
    } catch (Exception e) {
      throw new InterestCenterRuntimeException(
          "InterestCenterBmEJB.updateIC()", "InterestCenter.CANNOT_UPDATE_IC",
          ic.toString(), e);
    } finally {
      closeConnection(con);
    }
  }

  public void removeICByPK(ArrayList pks) {
    Connection con = openConnection();
    try {
      InterestCenterDAO.removeICByPK(con, pks);
    } catch (Exception e) {
      throw new InterestCenterRuntimeException(
          "InterestCenterBmEJB.removeICByPK(ArrayList pks)",
          "InterestCenter.CANNOT_DELETE_IC", pks.toString(), e);
    } finally {
      closeConnection(con);
    }
  }

  public void removeICByPK(int pk) {
    Connection con = openConnection();
    try {
      InterestCenterDAO.removeICByPK(con, pk);
    } catch (Exception e) {
      throw new InterestCenterRuntimeException(
          "InterestCenterBmEJB.removeICByPK(int pk)",
          "InterestCenter.CANNOT_DELETE_IC", String.valueOf(pk), e);
    } finally {
      closeConnection(con);
    }
  }

  private Connection openConnection() {
    try {
      Connection con = DBUtil
          .makeConnection(JNDINames.INTEREST_CENTER_DATASOURCE);
      return con;
    } catch (Exception e) {
      throw new InterestCenterRuntimeException(
          "InterestCenterBmEJB.getConnection()",
          "root.EX_CONNECTION_OPEN_FAILED", e);
    }
  }

  private void closeConnection(Connection con) {
    if (con != null) {
      try {
        con.close();
      } catch (Exception e) {
        SilverTrace.error("InterestCenter",
            "InterestCenterBmEJB.closeConnection()",
            "root.EX_CONNECTION_CLOSE_FAILED", e);
      }
    }
  }

  public void ejbCreate() throws CreateException {
  }

  public void ejbActivate() throws EJBException, RemoteException {
  }

  public void ejbPassivate() throws EJBException, RemoteException {
  }

  public void ejbRemove() throws EJBException, RemoteException {
  }

  public void setSessionContext(SessionContext context) throws EJBException,
      RemoteException {

  }

}
