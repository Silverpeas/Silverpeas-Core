/**
 * Copyright (C) 2000 - 2012 Silverpeas
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
 * "http://www.silverpeas.org/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.stratelia.webactiv.util.statistic.control;

import java.rmi.RemoteException;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import javax.ejb.EJBObject;

import com.silverpeas.util.ForeignPK;
import com.stratelia.webactiv.util.WAPrimaryKey;
import com.stratelia.webactiv.util.statistic.model.HistoryByUser;
import com.stratelia.webactiv.util.statistic.model.HistoryObjectDetail;

public interface StatisticBm extends EJBObject {

  /**
   * Method declaration
   * @param userId
   * @param foreignPK
   * @throws RemoteException
   * @see
   */
  public void addStat(String userId, ForeignPK foreignPK, int action,
      String objectType) throws RemoteException;

  /**
   * Method declaration
   * @param foreignPK
   * @return
   * @throws RemoteException
   * @see
   */
  public Collection<HistoryObjectDetail> getHistoryByAction(ForeignPK foreignPK, int action,
      String objectType) throws RemoteException;

  public Collection<HistoryObjectDetail> getHistoryByObjectAndUser(ForeignPK foreignPK, int action,
      String objectType, String userId) throws RemoteException;

  public Collection<HistoryByUser> getHistoryByObject(ForeignPK foreignPK, int action,
      String objectType) throws RemoteException;

  public Collection<HistoryByUser> getHistoryByObject(ForeignPK foreignPK, int action,
      String objectType, List<String> userIds) throws RemoteException;

  /**
   * Method declaration
   * @param foreignPK
   * @return
   * @throws RemoteException
   */
  public void deleteHistoryByAction(ForeignPK foreignPK, int action,
      String objectType) throws RemoteException;

  /**
   * Method declaration
   * @param foreignPKs
   * @param action
   * @return
   * @throws RemoteException
   */
  public int getCount(List<ForeignPK> foreignPKs, int action, String objectType)
      throws RemoteException;

  /**
   * Method declaration
   * @param foreignPK
   * @param action
   * @return
   * @throws RemoteException
   */
  public int getCount(ForeignPK foreignPK, int action, String objectType)
      throws RemoteException;

  /**
   * Method declaration
   * @param foreignPK
   * @return
   * @throws RemoteException
   */
  public int getCount(ForeignPK foreignPK, String objectType)
      throws RemoteException;

  /**
   * Methode declaration
   * @param foreignPK
   * @param actionType
   * @param objectType
   * @return
   * @throws RemoteException
   */
  public void moveStat(ForeignPK toForeignPK, int actionType, String objectType)
      throws RemoteException;

  /**
   * @param primaryKeys
   * @param objectType String representation of an object type
   * @param startDate the start date
   * @param endDate the end date
   * @return the number of access over a list of publications between startDate and endDate
   * @throws RemoteException
   */
  public int getCountByPeriod(List<WAPrimaryKey> primaryKeys, int action, String objectType,
      Date startDate, Date endDate) throws RemoteException;

  /**
   * @param primaryKeys
   * @param objectType String representation of an object type
   * @param startDate the start date
   * @param endDate the end date
   * @param userId the user identifier
   * @return the number of access over a list of publications between startDate and endDate for
   * specific user
   * @throws SQLException
   */
  public int getCountByPeriodAndUser(List<WAPrimaryKey> primaryKeys, String objectType,
      Date startDate,
      Date endDate, List<String> userIds) throws RemoteException;

  /**
   * @param primaryKeys
   * @param objectType String representation of an object type
   * @param startDate the start date
   * @param endDate the end date
   * @return the number of access over a list of publications between startDate and endDate
   * @throws RemoteException
   */
  public int getDistinctCountByPeriod(List<WAPrimaryKey> primaryKeys, int action,
      String objectType,
      Date startDate, Date endDate) throws RemoteException;

  /**
   * @param primaryKeys
   * @param action
   * @param objectType
   * @param startDate
   * @param endDate
   * @param userIds
   * @return
   * @throws RemoteException
   */
  public int getDistinctCountByPeriodUser(List<WAPrimaryKey> primaryKeys, int action,
      String objectType, Date startDate, Date endDate, List<String> userIds) throws RemoteException;

}
