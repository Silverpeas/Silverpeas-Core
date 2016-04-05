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
package org.silverpeas.core.silverstatistics.access.service;

import org.silverpeas.core.contribution.model.SilverpeasContent;
import org.silverpeas.core.silverstatistics.access.model.HistoryByUser;
import org.silverpeas.core.silverstatistics.access.model.HistoryObjectDetail;
import org.silverpeas.core.ForeignPK;
import org.silverpeas.core.WAPrimaryKey;

import java.util.Collection;
import java.util.Date;
import java.util.List;

/**
 * contract interface to manage silverpeas statistics
 */
public interface StatisticService {

  void addStat(String userId, ForeignPK foreignPK, int action, String objectType);

  void addStat(String userId, SilverpeasContent content);

  Collection<HistoryObjectDetail> getHistoryByAction(ForeignPK foreignPK, int action,
      String objectType);

  Collection<HistoryObjectDetail> getHistoryByObjectAndUser(ForeignPK foreignPK, int action,
      String objectType, String userId);

  Collection<HistoryByUser> getHistoryByObject(ForeignPK foreignPK, int action,
      String objectType);

  Collection<HistoryByUser> getHistoryByObject(ForeignPK foreignPK, int action,
      String objectType, List<String> userIds);

  void deleteStats(ForeignPK foreignPK, String objectType);

  void deleteStats(SilverpeasContent content);

  int getCount(List<ForeignPK> foreignPKs, int action, String objectType);

  int getCount(SilverpeasContent content, int action);

  int getCount(ForeignPK foreignPK, int action, String objectType);

  int getCount(ForeignPK foreignPK, String objectType);

  int getCount(SilverpeasContent content);

  void moveStat(ForeignPK toForeignPK, int actionType, String objectType);

  /**
   * @param primaryKeys
   * @param action
   * @param objectType String representation of an object type
   * @param startDate the start date
   * @param endDate the end date
   * @return the number of access over a list of publications between startDate and endDate
   */
  int getCountByPeriod(List<WAPrimaryKey> primaryKeys, int action, String objectType,
      Date startDate, Date endDate);

  /**
   * @param primaryKeys
   * @param objectType String representation of an object type
   * @param startDate the start date
   * @param endDate the end date
   * @param userIds the user identifiers
   * @return the number of access over a list of publications between startDate and endDate for
   * specific user
   */
  int getCountByPeriodAndUser(List<WAPrimaryKey> primaryKeys, String objectType,
      Date startDate, Date endDate, List<String> userIds);

  /**
   * @param primaryKeys
   * @param action
   * @param objectType String representation of an object type
   * @param startDate the start date
   * @param endDate the end date
   * @return the number of access over a list of publications between startDate and endDate
   * @
   */
  int getDistinctCountByPeriod(List<WAPrimaryKey> primaryKeys, int action,
      String objectType, Date startDate, Date endDate);

  int getDistinctCountByPeriodUser(List<WAPrimaryKey> primaryKeys, int action,
      String objectType, Date startDate, Date endDate, List<String> userIds);



  /**
   * Gets the last history detail of each object associated to a user. The result is sorted on
   * the date time from the youngest to the oldest and limited according to the nbObjects parameter.
   * @param userId
   * @param actionType
   * @param objectType
   * @param nbObjects
   * @return ordered list of unique objects used by the user
   */
  Collection<HistoryObjectDetail> getLastHistoryOfObjectsForUser(String userId,
      int actionType, String objectType, int nbObjects);

  boolean isRead(SilverpeasContent content, String userId);
}