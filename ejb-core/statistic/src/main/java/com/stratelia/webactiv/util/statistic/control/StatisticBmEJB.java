/**
 * Copyright (C) 2000 - 2012 Silverpeas
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
package com.stratelia.webactiv.util.statistic.control;

import java.sql.Connection;
import java.util.*;

import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;

import org.silverpeas.core.admin.OrganisationControllerFactory;

import com.silverpeas.util.ForeignPK;

import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.beans.admin.UserDetail;
import com.stratelia.webactiv.util.DBUtil;
import com.stratelia.webactiv.util.JNDINames;
import com.stratelia.webactiv.util.WAPrimaryKey;
import com.stratelia.webactiv.util.exception.SilverpeasRuntimeException;
import com.stratelia.webactiv.util.statistic.ejb.HistoryObjectDAO;
import com.stratelia.webactiv.util.statistic.model.HistoryByUser;
import com.stratelia.webactiv.util.statistic.model.HistoryObjectDetail;
import com.stratelia.webactiv.util.statistic.model.StatisticRuntimeException;

@Stateless(name = "Statistics", description = "EJB to manage statistics")
@TransactionAttribute(TransactionAttributeType.SUPPORTS)
public class StatisticBmEJB implements StatisticBm {

  private static final long serialVersionUID = 1L;
  private final static String historyTableName = "SB_Statistic_History";
  private final static int ACTION_ACCESS = 1;

  public StatisticBmEJB() {
  }

  private Connection getConnection() {
    try {
      return DBUtil.makeConnection(JNDINames.STATISTIC_DATASOURCE);
    } catch (Exception e) {
      throw new StatisticRuntimeException("StatisticBmEJB().getConnection()",
          SilverpeasRuntimeException.ERROR, "root.EX_CONNECTION_OPEN_FAILED", e);
    }
  }

  @Override
  public void addStat(String userId, ForeignPK foreignPK, int actionType, String objectType) {
    SilverTrace.info("statistic", "StatisticBmEJB.addObjectToHistory", "root.MSG_GEN_ENTER_METHOD");
    Connection con = getConnection();
    try {
      HistoryObjectDAO.add(con, historyTableName, userId, foreignPK, actionType, objectType);
    } catch (Exception e) {
      throw new StatisticRuntimeException("StatisticBmEJB().addObjectToHistory()",
          SilverpeasRuntimeException.ERROR, "statistic.CANNOT_ADD_VISITE_NODE", e);
    } finally {
      DBUtil.close(con);
    }
  }

  @Override
  public int getCount(List<ForeignPK> foreignPKs, int action, String objectType) {
    Connection con = getConnection();
    try {
      return HistoryObjectDAO.getCount(con, foreignPKs, action, historyTableName, objectType);
    } catch (Exception e) {
      throw new StatisticRuntimeException("StatisticBmEJB().getCount()",
          SilverpeasRuntimeException.ERROR, "statistic.CANNOT_GET_HISTORY_STATISTICS_PUBLICATION", e);
    } finally {
      DBUtil.close(con);
    }
  }

  @Override
  public int getCount(ForeignPK foreignPK, int action, String objectType) {
    Connection con = getConnection();
    try {
      return HistoryObjectDAO.getCount(con, foreignPK, action, historyTableName, objectType);
    } catch (Exception e) {
      throw new StatisticRuntimeException("StatisticBmEJB().getCount()",
          SilverpeasRuntimeException.ERROR, "statistic.CANNOT_GET_HISTORY_STATISTICS_PUBLICATION", e);
    } finally {
      DBUtil.close(con);
    }
  }

  @Override
  public int getCount(ForeignPK foreignPK, String objectType) {
    return getCount(foreignPK, ACTION_ACCESS, objectType);
  }

  @Override
  public Collection<HistoryObjectDetail> getHistoryByAction(ForeignPK foreignPK, int action,
      String objectType) {
    SilverTrace.info("statistic", "StatisticBmEJB.getHistoryByAction", "root.MSG_GEN_ENTER_METHOD");
    Connection con = getConnection();
    try {
      return HistoryObjectDAO.getHistoryDetailByObject(con, historyTableName, foreignPK, objectType);
    } catch (Exception e) {
      throw new StatisticRuntimeException("StatisticBmEJB().getHistoryByAction()",
          SilverpeasRuntimeException.ERROR, "statistic.CANNOT_GET_HISTORY_STATISTICS_PUBLICATION", e);
    } finally {
      DBUtil.close(con);
    }
  }

  @Override
  public Collection<HistoryObjectDetail> getHistoryByObjectAndUser(ForeignPK foreignPK, int action,
      String objectType, String userId) {
    SilverTrace.info("statistic", "StatisticBmEJB.getHistoryByObjectAndUser",
        "root.MSG_GEN_ENTER_METHOD");
    Connection con = getConnection();
    try {
      return HistoryObjectDAO.getHistoryDetailByObjectAndUser(con, historyTableName, foreignPK,
          objectType, userId);
    } catch (Exception e) {
      throw new StatisticRuntimeException("StatisticBmEJB().getHistoryByObjectAndUser()",
          SilverpeasRuntimeException.ERROR, "statistic.CANNOT_GET_HISTORY_STATISTICS_PUBLICATION", e);
    } finally {
      DBUtil.close(con);
    }
  }

  @Override
  public Collection<HistoryByUser> getHistoryByObject(ForeignPK foreignPK, int action,
      String objectType) {
    UserDetail[] allUsers = OrganisationControllerFactory.getOrganisationController().getAllUsers(
        foreignPK.getInstanceId());
    return getHistoryByObject(foreignPK, action, objectType, allUsers);
  }

  @Override
  public Collection<HistoryByUser> getHistoryByObject(ForeignPK foreignPK, int action,
      String objectType, List<String> userIds) {
    if (userIds == null || userIds.isEmpty()) {
      return getHistoryByObject(foreignPK, action, objectType);
    }
    UserDetail[] users = OrganisationControllerFactory.getOrganisationController()
        .getUserDetails(userIds.toArray(new String[userIds.size()]));
    return getHistoryByObject(foreignPK, action, objectType, users);
  }

  private Collection<HistoryByUser> getHistoryByObject(ForeignPK foreignPK, int action,
      String objectType, UserDetail[] users) {
    SilverTrace.info("statistic", "StatisticBmEJB.getHistoryByObject()",
        "root.MSG_GEN_ENTER_METHOD");
    Collection<HistoryObjectDetail> list = null;
    try {
      list = getHistoryByAction(foreignPK, action, objectType);
    } catch (Exception e) {
      throw new StatisticRuntimeException("StatisticBmEJB.getHistoryByObject()",
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
    UserDetail[] allUsersByComponent = users;
    UserDetail[] controlledUsers = OrganisationControllerFactory.getOrganisationController()
        .getUserDetails(readerIds);

    // ajouter à la liste "allUsers" (liste des users des rôles) les users ayant lu mais ne faisant
    // pas partis d'un rôle
    Collection<UserDetail> allUsers = new ArrayList<UserDetail>(allUsersByComponent.length
        + controlledUsers.length);
    int compteur = 0;
    for (int j = 0; j < allUsersByComponent.length; j++) {
      allUsers.add(allUsersByComponent[j]);
      compteur = j + 1;
    }
    for (int j = compteur; j < controlledUsers.length; j++) {
      if (!allUsers.contains(controlledUsers[j])) {
        allUsers.add(controlledUsers[j]);
      }
    }

    // création de la liste de tous les utilisateur ayant le droit de lecture
    Collection<HistoryByUser> statByUser = new ArrayList<HistoryByUser>(allUsers.size());
    for (UserDetail user : allUsers) {
      if (user != null) {
        HistoryByUser historyByUser = new HistoryByUser(user, null, 0);
        statByUser.add(historyByUser);
      }
    }

    // création d'une liste des accès par utilisateur
    Map<UserDetail, Date> byUser = new HashMap<UserDetail, Date>(controlledUsers.length);
    Map<UserDetail, Integer> nbAccessbyUser = new HashMap<UserDetail, Integer>(
        controlledUsers.length);
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
            Integer nb = (Integer) objNb;
            nbAccess = nb.intValue();
            nbAccess = nbAccess + 1;
          }
          nbAccessbyUser.put(controlledUsers[j], Integer.valueOf(nbAccess));
        } else {
          byUser.put(controlledUsers[j], date[j]);
          nbAccessbyUser.put(controlledUsers[j], Integer.valueOf(1));
        }
      }
    }

    // mise à jour de la date de dernier accès et du nombre d'accès pour les
    // utilisateurs ayant lu
    Iterator<HistoryByUser> itStat = statByUser.iterator();
    while (itStat.hasNext()) {
      HistoryByUser historyByUser = itStat.next();
      UserDetail user = historyByUser.getUser();
      // recherche de la date de dernier accès
      Date lastAccess = byUser.get(user);
      if (lastAccess != null) {
        historyByUser.setLastAccess(lastAccess);
      }
      // recherche du nombre d'accès
      Integer nbAccess = nbAccessbyUser.get(user);
      if (nbAccess != null) {
        historyByUser.setNbAccess(nbAccess.intValue());
      }
    }

    // tri de la liste pour mettre en premier les users ayant consulté
    LastAccessComparatorDesc comparateur = new LastAccessComparatorDesc();
    Collections.sort((List<HistoryByUser>) statByUser, comparateur);

    SilverTrace.info("statistic", "StatisticBmEJB.getHistoryByObject()",
        "root.MSG_GEN_EXIT_METHOD");
    return statByUser;
  }

  @Override
  public void deleteHistoryByAction(ForeignPK foreignPK, int action, String objectType) {
    SilverTrace.info("statistic", "StatisticBmEJB.deleteHistoryByAction",
        "root.MSG_GEN_ENTER_METHOD");
    Connection con = getConnection();
    try {
      HistoryObjectDAO.deleteHistoryByObject(con, historyTableName, foreignPK, objectType);
    } catch (Exception e) {
      throw new StatisticRuntimeException("StatisticBmEJB().deleteHistoryByAction",
          SilverpeasRuntimeException.ERROR, "statistic.CANNOT_DELETE_HISTORY_STATISTICS_PUBLICATION",
          e);
    } finally {
      DBUtil.close(con);
    }
  }

  @Override
  public void moveStat(ForeignPK toForeignPK, int actionType, String objectType) {
    SilverTrace.info("statistic", "StatisticBmEJB.deleteHistoryByAction",
        "root.MSG_GEN_ENTER_METHOD");
    Connection con = getConnection();
    try {
      HistoryObjectDAO.move(con, historyTableName, toForeignPK, actionType, objectType);
    } catch (Exception e) {
      throw new StatisticRuntimeException("StatisticBmEJB().addObjectToHistory()",
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
      throw new StatisticRuntimeException("StatisticBmEJB().getCountByPeriod()",
          SilverpeasRuntimeException.ERROR, "statistic.CANNOT_GET_HISTORY_STATISTICS_PUBLICATION", e);
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
            nb += HistoryObjectDAO.getCountByPeriodAndUser(con, primaryKey, objectType, startDate,
                endDate, userId);
          }
        }
      }
    } catch (Exception e) {
      throw new StatisticRuntimeException("StatisticBmEJB().getCountByPeriodAndUser()",
          SilverpeasRuntimeException.ERROR, "statistic.CANNOT_GET_HISTORY_STATISTICS_PUBLICATION", e);
    } finally {
      DBUtil.close(con);
    }
    return nb;
  }

  @Override
  public int getDistinctCountByPeriod(List<WAPrimaryKey> primaryKeys, int action,
      String objectType, Date startDate, Date endDate) {
    int nb = 0;
    Connection con = getConnection();
    try {
      List<Integer> objectIds = HistoryObjectDAO.getListObjectAccessByPeriod(con, primaryKeys,
          objectType, startDate, endDate);
      Set<Integer> distinctObjectIds = new HashSet<Integer>(objectIds);
      nb = distinctObjectIds.size();
    } catch (Exception e) {
      throw new StatisticRuntimeException("StatisticBmEJB().getDistinctCountByPeriod()",
          SilverpeasRuntimeException.ERROR, "statistic.CANNOT_GET_HISTORY_STATISTICS_PUBLICATION", e);
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
      Set<Integer> distinctObjectIds = new HashSet<Integer>(userIds.size());
      try {
        for (String userId : userIds) {
          List<Integer> objectIds = HistoryObjectDAO.getListObjectAccessByPeriodAndUser(con,
              primaryKeys, objectType, startDate, endDate, userId);
          distinctObjectIds.addAll(objectIds);
        }
        nb = distinctObjectIds.size();
      } catch (Exception e) {
        throw new StatisticRuntimeException("StatisticBmEJB().getDistinctCountByPeriod()",
            SilverpeasRuntimeException.ERROR, "statistic.CANNOT_GET_HISTORY_STATISTICS_PUBLICATION",
            e);
      } finally {
        DBUtil.close(con);
      }

    }
    return nb;
  }
}
