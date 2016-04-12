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
import org.silverpeas.core.admin.component.ComponentInstanceDeletion;
import org.silverpeas.core.admin.user.model.UserDetail;
import org.silverpeas.core.admin.service.OrganizationControllerProvider;
import org.silverpeas.core.silverstatistics.access.dao.HistoryObjectDAO;
import org.silverpeas.core.silverstatistics.access.model.HistoryByUser;
import org.silverpeas.core.silverstatistics.access.model.HistoryObjectDetail;
import org.silverpeas.core.silverstatistics.access.model.StatisticRuntimeException;
import org.silverpeas.core.silvertrace.SilverTrace;
import org.silverpeas.core.persistence.jdbc.DBUtil;
import org.silverpeas.core.ForeignPK;
import org.silverpeas.core.WAPrimaryKey;
import org.silverpeas.core.exception.SilverpeasRuntimeException;

import javax.inject.Singleton;
import javax.transaction.Transactional;
import java.sql.Connection;
import java.util.*;

/**
 * Default implementation of Statistic service layer which manage statistics
 */

@Singleton
@Transactional(Transactional.TxType.SUPPORTS)
public class DefaultStatisticService implements StatisticService, ComponentInstanceDeletion {

  public final static int ACTION_ACCESS = 1;

  public DefaultStatisticService() {
  }

  private Connection getConnection() {
    try {
      return DBUtil.openConnection();
    } catch (Exception e) {
      throw new StatisticRuntimeException("DefaultStatisticService().getConnection()",
          SilverpeasRuntimeException.ERROR, "root.EX_CONNECTION_OPEN_FAILED", e);
    }
  }

  @Override
  public void addStat(String userId, ForeignPK foreignPK, int actionType, String objectType) {

    Connection con = getConnection();
    try {
      HistoryObjectDAO.add(con, userId, foreignPK, actionType, objectType);
    } catch (Exception e) {
      throw new StatisticRuntimeException("DefaultStatisticService().addStat()",
          SilverpeasRuntimeException.ERROR, "statistic.CANNOT_ADD_VISITE_NODE", e);
    } finally {
      DBUtil.close(con);
    }
  }

  @Override
  public void addStat(String userId, SilverpeasContent content) {
    addStat(userId, getForeignPK(content), ACTION_ACCESS, content.getContributionType());
  }

  @Override
  public int getCount(List<ForeignPK> foreignPKs, int action, String objectType) {
    Connection con = getConnection();
    try {
      return HistoryObjectDAO.getCount(con, foreignPKs, objectType);
    } catch (Exception e) {
      throw new StatisticRuntimeException("DefaultStatisticService().getCount()",
          SilverpeasRuntimeException.ERROR, "statistic.CANNOT_GET_HISTORY_STATISTICS_PUBLICATION",
          e);
    } finally {
      DBUtil.close(con);
    }
  }

  @Override
  public int getCount(ForeignPK foreignPK, int action, String objectType) {
    Connection con = getConnection();
    try {
      return HistoryObjectDAO.getCount(con, foreignPK, objectType);
    } catch (Exception e) {
      throw new StatisticRuntimeException("DefaultStatisticService().getCount()",
          SilverpeasRuntimeException.ERROR, "statistic.CANNOT_GET_HISTORY_STATISTICS_PUBLICATION",
          e);
    } finally {
      DBUtil.close(con);
    }
  }

  @Override
  public int getCount(SilverpeasContent content, int action) {
    return getCount(getForeignPK(content), content.getContributionType());
  }

  @Override
  public int getCount(ForeignPK foreignPK, String objectType) {
    return getCount(foreignPK, ACTION_ACCESS, objectType);
  }

  @Override
  public int getCount(SilverpeasContent content) {
    return getCount(getForeignPK(content), content.getContributionType());
  }

  @Override
  public Collection<HistoryObjectDetail> getHistoryByAction(ForeignPK foreignPK, int action,
      String objectType) {

    Connection con = getConnection();
    try {
      return HistoryObjectDAO.getHistoryDetailByObject(con, foreignPK, objectType);
    } catch (Exception e) {
      throw new StatisticRuntimeException("DefaultStatisticService().getHistoryByAction()",
          SilverpeasRuntimeException.ERROR, "statistic.CANNOT_GET_HISTORY_STATISTICS_PUBLICATION",
          e);
    } finally {
      DBUtil.close(con);
    }
  }

  @Override
  public Collection<HistoryObjectDetail> getHistoryByObjectAndUser(ForeignPK foreignPK, int action,
      String objectType, String userId) {
    SilverTrace
        .info("statistic", "DefaultStatisticService.getHistoryByObjectAndUser", "root.MSG_GEN_ENTER_METHOD");
    Connection con = getConnection();
    try {
      return HistoryObjectDAO.getHistoryDetailByObjectAndUser(con, foreignPK, objectType, userId);
    } catch (Exception e) {
      throw new StatisticRuntimeException("DefaultStatisticService().getHistoryByObjectAndUser()",
          SilverpeasRuntimeException.ERROR, "statistic.CANNOT_GET_HISTORY_STATISTICS_PUBLICATION",
          e);
    } finally {
      DBUtil.close(con);
    }
  }

  @Override
  public Collection<HistoryByUser> getHistoryByObject(ForeignPK foreignPK, int action,
      String objectType) {
    UserDetail[] allUsers = OrganizationControllerProvider.getOrganisationController()
        .getAllUsers(foreignPK.getInstanceId());
    return getHistoryByObject(foreignPK, action, objectType, allUsers);
  }

  @Override
  public Collection<HistoryByUser> getHistoryByObject(ForeignPK foreignPK, int action,
      String objectType, List<String> userIds) {
    if (userIds == null || userIds.isEmpty()) {
      return getHistoryByObject(foreignPK, action, objectType);
    }
    UserDetail[] users = OrganizationControllerProvider.getOrganisationController()
        .getUserDetails(userIds.toArray(new String[userIds.size()]));
    return getHistoryByObject(foreignPK, action, objectType, users);
  }

  private Collection<HistoryByUser> getHistoryByObject(ForeignPK foreignPK, int action,
      String objectType, UserDetail[] users) {
    SilverTrace
        .info("statistic", "DefaultStatisticService.getHistoryByObject()", "root.MSG_GEN_ENTER_METHOD");
    Collection<HistoryObjectDetail> list;
    try {
      list = getHistoryByAction(foreignPK, action, objectType);
    } catch (Exception e) {
      throw new StatisticRuntimeException("DefaultStatisticService.getHistoryByObject()",
          SilverpeasRuntimeException.ERROR, "statistic.EX_IMPOSSIBLE_DOBTENIR_LETAT_DES_LECTURES",
          e);
    }
    String[] readerIds = new String[list.size()];
    Date[] date = new Date[list.size()];
    Iterator<HistoryObjectDetail> it = list.iterator();
    int i = 0;
    while (it.hasNext()) {
      HistoryObjectDetail historyObject = it.next();
      readerIds[i] = historyObject.getUserId();
      date[i] = historyObject.getDate();
      i++;
    }
    UserDetail[] controlledUsers =
        OrganizationControllerProvider.getOrganisationController().getUserDetails(readerIds);

    // ajouter à la liste "allUsers" (liste des users des rôles) les users ayant lu mais ne faisant
    // pas partis d'un rôle
    int compteur = 0;
    Collection<UserDetail> allUsers = new ArrayList<>(users.length + controlledUsers.length);
    for (int j = 0; j < users.length; j++) {
      allUsers.add(users[j]);
      compteur = j + 1;
    }
    for (int j = compteur; j < controlledUsers.length; j++) {
      if (!allUsers.contains(controlledUsers[j])) {
        allUsers.add(controlledUsers[j]);
      }
    }

    // création de la liste de tous les utilisateur ayant le droit de lecture
    Collection<HistoryByUser> statByUser = new ArrayList<>(allUsers.size());
    for (UserDetail user : allUsers) {
      if (user != null) {
        HistoryByUser historyByUser = new HistoryByUser(user, null, 0);
        statByUser.add(historyByUser);
      }
    }

    // création d'une liste des accès par utilisateur
    Map<UserDetail, Date> byUser = new HashMap<>(controlledUsers.length);
    Map<UserDetail, Integer> nbAccessbyUser = new HashMap<>(controlledUsers.length);
    for (int j = 0; j < controlledUsers.length; j++) {
      if (controlledUsers[j] != null) {
        // regarder si la date en cours est > à la date enregistrée...
        Object obj = byUser.get(controlledUsers[j]);
        if (obj != null && !obj.toString().equals("Never")) {
          Date dateTab = (Date) obj;
          if (date[j].after(dateTab)) {
            byUser.put(controlledUsers[j], date[j]);
          }
          Object objNb = nbAccessbyUser.get(controlledUsers[j]);
          int nbAccess = 0;
          if (objNb != null) {
            nbAccess = (Integer) objNb;
            nbAccess = nbAccess + 1;
          }
          nbAccessbyUser.put(controlledUsers[j], nbAccess);
        } else {
          byUser.put(controlledUsers[j], date[j]);
          nbAccessbyUser.put(controlledUsers[j], 1);
        }
      }
    }

    // mise à jour de la date de dernier accès et du nombre d'accès pour les utilisateurs ayant lu
    for (final HistoryByUser historyByUser : statByUser) {
      UserDetail user = historyByUser.getUser();
      // recherche de la date de dernier accès
      Date lastAccess = byUser.get(user);
      if (lastAccess != null) {
        historyByUser.setLastAccess(lastAccess);
      }
      // retrieve access number
      Integer nbAccess = nbAccessbyUser.get(user);
      if (nbAccess != null) {
        historyByUser.setNbAccess(nbAccess);
      }
    }

    // Sort list to get readers first
    LastAccessComparatorDesc comparator = new LastAccessComparatorDesc();
    Collections.sort((List<HistoryByUser>) statByUser, comparator);

    SilverTrace
        .info("statistic", "DefaultStatisticService.getHistoryByObject()", "root.MSG_GEN_EXIT_METHOD");
    return statByUser;
  }

  @Override
  public void deleteStats(ForeignPK foreignPK, String objectType) {

    Connection con = getConnection();
    try {
      HistoryObjectDAO.deleteHistoryByObject(con, foreignPK, objectType);
    } catch (Exception e) {
      throw new StatisticRuntimeException("DefaultStatisticService().deleteHistoryByAction",
          SilverpeasRuntimeException.ERROR,
          "statistic.CANNOT_DELETE_HISTORY_STATISTICS_PUBLICATION", e);
    } finally {
      DBUtil.close(con);
    }
  }

  @Override
  public void deleteStats(SilverpeasContent content) {
    deleteStats(getForeignPK(content), content.getContributionType());
  }

  @Override
  public void moveStat(ForeignPK toForeignPK, int actionType, String objectType) {
    SilverTrace
        .info("statistic", "DefaultStatisticService.deleteHistoryByAction", "root.MSG_GEN_ENTER_METHOD");
    Connection con = getConnection();
    try {
      HistoryObjectDAO.move(con, toForeignPK, actionType, objectType);
    } catch (Exception e) {
      throw new StatisticRuntimeException("DefaultStatisticService().addObjectToHistory()",
          SilverpeasRuntimeException.ERROR, "statistic.CANNOT_ADD_VISITE_NODE", e);
    } finally {
      DBUtil.close(con);
    }
  }

  @Override
  public int getCountByPeriod(List<WAPrimaryKey> primaryKeys, int action, String objectType,
      Date startDate, Date endDate) {
    int nb = 0;
    Connection con = getConnection();
    try {
      for (WAPrimaryKey primaryKey : primaryKeys) {
        nb += HistoryObjectDAO.getCountByPeriod(con, primaryKey, objectType, startDate, endDate);
      }
    } catch (Exception e) {
      throw new StatisticRuntimeException("DefaultStatisticService().getCountByPeriod()",
          SilverpeasRuntimeException.ERROR, "statistic.CANNOT_GET_HISTORY_STATISTICS_PUBLICATION",
          e);
    } finally {
      DBUtil.close(con);
    }
    return nb;
  }

  @Override
  public int getCountByPeriodAndUser(List<WAPrimaryKey> primaryKeys, String objectType,
      Date startDate, Date endDate, List<String> userIds) {
    int nb = 0;
    Connection con = getConnection();
    try {
      if (!userIds.isEmpty()) {
        for (String userId : userIds) {
          for (WAPrimaryKey primaryKey : primaryKeys) {
            nb += HistoryObjectDAO
                .getCountByPeriodAndUser(con, primaryKey, objectType, startDate, endDate, userId);
          }
        }
      }
    } catch (Exception e) {
      throw new StatisticRuntimeException("DefaultStatisticService().getCountByPeriodAndUser()",
          SilverpeasRuntimeException.ERROR, "statistic.CANNOT_GET_HISTORY_STATISTICS_PUBLICATION",
          e);
    } finally {
      DBUtil.close(con);
    }
    return nb;
  }

  @Override
  public int getDistinctCountByPeriod(List<WAPrimaryKey> primaryKeys, int action, String objectType,
      Date startDate, Date endDate) {
    int nb = 0;
    Connection con = getConnection();
    try {
      List<String> objectIds = HistoryObjectDAO
          .getListObjectAccessByPeriod(con, primaryKeys, objectType, startDate, endDate);
      Set<String> distinctObjectIds = new HashSet<String>(objectIds);
      nb = distinctObjectIds.size();
    } catch (Exception e) {
      throw new StatisticRuntimeException("DefaultStatisticService().getDistinctCountByPeriod()",
          SilverpeasRuntimeException.ERROR, "statistic.CANNOT_GET_HISTORY_STATISTICS_PUBLICATION",
          e);
    } finally {
      DBUtil.close(con);
    }
    return nb;
  }

  @Override
  public int getDistinctCountByPeriodUser(List<WAPrimaryKey> primaryKeys, int action,
      String objectType, Date startDate, Date endDate, List<String> userIds) {
    int nb = 0;
    Connection con = getConnection();
    if (userIds != null && !userIds.isEmpty()) {
      Set<String> distinctObjectIds = new HashSet<>(userIds.size());
      try {
        for (String userId : userIds) {
          List<String> objectIds = HistoryObjectDAO
              .getListObjectAccessByPeriodAndUser(con, primaryKeys, objectType, startDate, endDate,
                  userId);
          distinctObjectIds.addAll(objectIds);
        }
        nb = distinctObjectIds.size();
      } catch (Exception e) {
        throw new StatisticRuntimeException("DefaultStatisticService().getDistinctCountByPeriod()",
            SilverpeasRuntimeException.ERROR, "statistic.CANNOT_GET_HISTORY_STATISTICS_PUBLICATION",
            e);
      } finally {
        DBUtil.close(con);
      }

    }
    return nb;
  }

  @Override
  public Collection<HistoryObjectDetail> getLastHistoryOfObjectsForUser(String userId,
      int actionType, String objectType, int nbObjects) {

    Connection con = getConnection();
    try {
      return HistoryObjectDAO
          .getLastHistoryDetailOfObjectsForUser(con, userId, actionType, objectType, nbObjects);
    } catch (Exception e) {
      throw new StatisticRuntimeException("DefaultStatisticService().getLastHistoryOfObjectsForUser()",
          SilverpeasRuntimeException.ERROR, "statistic.CANNOT_GET_HISTORY_STATISTICS_PUBLICATION",
          e);
    } finally {
      DBUtil.close(con);
    }
  }

  public boolean isRead(SilverpeasContent content, String userId) {
    Connection con = getConnection();
    try {
      int numberOfReading = HistoryObjectDAO
          .getCountByPeriodAndUser(con, getForeignPK(content), content.getContributionType(), null,
              null, userId);
      return numberOfReading > 0;
    } catch (Exception e) {
      throw new StatisticRuntimeException("DefaultStatisticService().isRead()",
          SilverpeasRuntimeException.ERROR, "statistic.CANNOT_GET_HISTORY_STATISTICS_PUBLICATION",
          e);
    } finally {
      DBUtil.close(con);
    }
  }

  private ForeignPK getForeignPK(SilverpeasContent content) {
    return new ForeignPK(content.getId(), content.getComponentInstanceId());
  }

  /**
   * Deletes the resources belonging to the specified component instance. This method is invoked
   * by Silverpeas when a component instance is being deleted.
   * @param componentInstanceId the unique identifier of a component instance.
   */
  @Override
  @Transactional
  public void delete(final String componentInstanceId) {
    Connection con = getConnection();
    try {
      HistoryObjectDAO.deleteStatsOfComponent(con, componentInstanceId);
    } catch (Exception e) {
      throw new RuntimeException(
          "A failure occurred when deleting the statistics relative to the component instance " +
              componentInstanceId, e);
    } finally {
      DBUtil.close(con);
    }
  }
}