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

/*--- formatted by Jindent 2.1, (www.c-lab.de/~jindent)
 ---*/

package com.stratelia.webactiv.util.statistic.control;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;

import javax.ejb.CreateException;
import javax.ejb.SessionBean;
import javax.ejb.SessionContext;

import com.silverpeas.util.ForeignPK;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.beans.admin.OrganizationController;
import com.stratelia.webactiv.beans.admin.UserDetail;
import com.stratelia.webactiv.util.DBUtil;
import com.stratelia.webactiv.util.JNDINames;
import com.stratelia.webactiv.util.exception.SilverpeasRuntimeException;
import com.stratelia.webactiv.util.node.model.NodePK;
import com.stratelia.webactiv.util.publication.model.PublicationPK;
import com.stratelia.webactiv.util.statistic.ejb.HistoryNodePublicationActorDAO;
import com.stratelia.webactiv.util.statistic.ejb.HistoryObjectDAO;
import com.stratelia.webactiv.util.statistic.model.HistoryByUser;
import com.stratelia.webactiv.util.statistic.model.HistoryObjectDetail;
import com.stratelia.webactiv.util.statistic.model.StatisticResultDetail;
import com.stratelia.webactiv.util.statistic.model.StatisticRuntimeException;

/**
 * Class declaration
 * @author
 */
public class StatisticBmEJB implements SessionBean {

  private static final long serialVersionUID = 1L;

  private final String historyRootTableName = "SB_Publication_History";
  private final String historyTableName = "SB_Statistic_History";
  private String dbName = JNDINames.STATISTIC_DATASOURCE;

  private final int ACTION_ACCESS = 1;

  // private final int ACTION_DOWNLOAD = 2;

  /**
   * Constructor declaration
   * @see
   */
  public StatisticBmEJB() {
  }

  /**
   * Method declaration
   * @return
   * @see
   */
  private Connection getConnection() {
    try {
      Connection con = DBUtil.makeConnection(dbName);

      return con;
    } catch (Exception e) {
      throw new StatisticRuntimeException("StatisticBmEJB().getConnection()",
          SilverpeasRuntimeException.ERROR, "root.EX_CONNECTION_OPEN_FAILED", e);
    }
  }

  /**
   * Method declaration
   * @param con
   * @see
   */
  private void freeConnection(Connection con) {
    if (con != null) {
      try {
        con.close();
      } catch (Exception e) {
        SilverTrace.error("statistic", "StatisticBmEJB.freeConnection",
            "root.MSG_GEN_CONNECTION_CLOSE_FAILED");
      }
    }
  }

  /**
   * Method declaration
   * @param fatherPK
   * @deprecated : A SUPPRIMER APRES TESTS
   * @return
   * @see
   */
  public Collection<StatisticResultDetail> getNodesUsage(NodePK fatherPK) {
    SilverTrace.info("statistic", "StatisticBmEJB.getNodesUsage",
        "root.MSG_GEN_ENTER_METHOD");
    Connection con = null;

    try {
      con = getConnection();
      return HistoryNodePublicationActorDAO.getNodesUsage(con,
          historyRootTableName, fatherPK);
    } catch (Exception e) {
      throw new StatisticRuntimeException("StatisticBmEJB().getNodesUsage()",
          SilverpeasRuntimeException.ERROR,
          "statistic.CANNOT_GET_STATISTICS_NODE", e);
    } finally {
      freeConnection(con);
    }
  }

  /**
   * Method declaration
   * @param userId
   * @param nodePK
   * @param pubPK
   * @deprecated : utiliser addStat
   * @see
   */
  public void addReading(String userId, NodePK nodePK, PublicationPK pubPK) {
    SilverTrace.info("statistic", "StatisticBmEJB.addReading",
        "root.MSG_GEN_ENTER_METHOD");
    Connection con = null;

    try {
      ForeignPK foreignPK = new ForeignPK(pubPK.getId(), pubPK.getInstanceId());
      String objectType = "Publication";
      addStat(userId, foreignPK, ACTION_ACCESS, objectType);

    } catch (Exception e) {
      throw new StatisticRuntimeException("StatisticBmEJB().addReading()",
          SilverpeasRuntimeException.ERROR, "statistic.CANNOT_ADD_VISITE_NODE",
          e);
    } finally {
      freeConnection(con);
    }
  }

  /**
   * Method declaration
   * @param userId
   * @param foreignPK
   * @see
   */
  public void addStat(String userId, ForeignPK foreignPK, int actionType,
      String objectType) {
    SilverTrace.info("statistic", "StatisticBmEJB.addObjectToHistory",
        "root.MSG_GEN_ENTER_METHOD");
    Connection con = null;

    try {
      con = getConnection();
      HistoryObjectDAO.add(con, historyTableName, userId, foreignPK,
          actionType, objectType);
    } catch (Exception e) {
      throw new StatisticRuntimeException(
          "StatisticBmEJB().addObjectToHistory()",
          SilverpeasRuntimeException.ERROR, "statistic.CANNOT_ADD_VISITE_NODE",
          e);
    } finally {
      freeConnection(con);
    }
  }

  public int getCount(List<ForeignPK> foreignPKs, int action, String objectType) {
    int nb = 0;
    Connection con = null;
    try {
      con = getConnection();
      nb = HistoryObjectDAO.getCount(con, foreignPKs, action, historyTableName,
          objectType);
      return nb;
    } catch (Exception e) {
      throw new StatisticRuntimeException("StatisticBmEJB().getCount()",
          SilverpeasRuntimeException.ERROR,
          "statistic.CANNOT_GET_HISTORY_STATISTICS_PUBLICATION", e);
    } finally {
      freeConnection(con);
    }
  }

  public int getCount(ForeignPK foreignPK, int action, String objectType) {
    int nb = 0;
    Connection con = null;
    try {
      con = getConnection();
      nb = HistoryObjectDAO.getCount(con, foreignPK, action, historyTableName,
          objectType);
      return nb;
    } catch (Exception e) {
      throw new StatisticRuntimeException("StatisticBmEJB().getCount()",
          SilverpeasRuntimeException.ERROR,
          "statistic.CANNOT_GET_HISTORY_STATISTICS_PUBLICATION", e);
    } finally {
      freeConnection(con);
    }
  }

  public int getCount(ForeignPK foreignPK, String objectType) {
    return getCount(foreignPK, ACTION_ACCESS, objectType);
  }

  /**
   * Method declaration
   * @param foreignPK
   * @return
   * @see
   */
  public Collection<HistoryObjectDetail> getHistoryByAction(ForeignPK foreignPK, int action,
      String objectType) {
    SilverTrace.info("statistic", "StatisticBmEJB.getHistoryByAction",
        "root.MSG_GEN_ENTER_METHOD");
    Connection con = null;

    try {
      con = getConnection();
      Collection<HistoryObjectDetail> result = HistoryObjectDAO.getHistoryDetailByObject(con,
          historyTableName, foreignPK, objectType);

      return result;
    } catch (Exception e) {
      throw new StatisticRuntimeException(
          "StatisticBmEJB().getHistoryByAction()",
          SilverpeasRuntimeException.ERROR,
          "statistic.CANNOT_GET_HISTORY_STATISTICS_PUBLICATION", e);
    } finally {
      freeConnection(con);
    }
  }

  public Collection<HistoryObjectDetail> getHistoryByObjectAndUser(ForeignPK foreignPK, int action,
      String objectType, String userId) {
    SilverTrace.info("statistic", "StatisticBmEJB.getHistoryByObjectAndUser",
        "root.MSG_GEN_ENTER_METHOD");
    Connection con = null;

    try {
      con = getConnection();
      return HistoryObjectDAO.getHistoryDetailByObjectAndUser(con,
          historyTableName, foreignPK, objectType, userId);
    } catch (Exception e) {
      throw new StatisticRuntimeException(
          "StatisticBmEJB().getHistoryByObjectAndUser()",
          SilverpeasRuntimeException.ERROR,
          "statistic.CANNOT_GET_HISTORY_STATISTICS_PUBLICATION", e);
    } finally {
      freeConnection(con);
    }
  }

  public Collection<HistoryByUser> getHistoryByObject(ForeignPK foreignPK, int action,
      String objectType) {
    OrganizationController orga = new OrganizationController();
    UserDetail[] allUsers = orga.getAllUsers(foreignPK.getInstanceId());

    return getHistoryByObject(foreignPK, action, objectType, allUsers);
  }

  public Collection<HistoryByUser> getHistoryByObject(ForeignPK foreignPK, int action,
      String objectType,
      List<String> userIds) {
    if (userIds == null || userIds.size() == 0) {
      return getHistoryByObject(foreignPK, action, objectType);
    } else {
      OrganizationController orga = new OrganizationController();
      UserDetail[] users = orga.getUserDetails((String[]) userIds.toArray(new String[] {}));

      return getHistoryByObject(foreignPK, action, objectType, users);
    }
  }

  private Collection<HistoryByUser> getHistoryByObject(ForeignPK foreignPK, int action,
      String objectType,
      UserDetail[] users) {
    SilverTrace.info("statistic", "StatisticBmEJB.getHistoryByObject()",
        "root.MSG_GEN_ENTER_METHOD");
    Collection<HistoryObjectDetail> list = null;
    try {
      list = getHistoryByAction(foreignPK, action, objectType);
    } catch (Exception e) {
      throw new StatisticRuntimeException(
          "StatisticBmEJB.getHistoryByObject()",
          SilverpeasRuntimeException.ERROR,
          "statistic.EX_IMPOSSIBLE_DOBTENIR_LETAT_DES_LECTURES", e);
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
    OrganizationController orga = new OrganizationController();
    UserDetail[] allUsersByComponent = users;
    UserDetail[] controlledUsers = orga.getUserDetails(readerIds);

    // ajouter à la liste "allUsers" (liste des users des rôles) les users ayant lu mais ne faisant pas partis d'un rôle
    Collection<UserDetail> allUsers = new ArrayList<UserDetail>();
    int compteur = 0;
    for (int j = 0; j < allUsersByComponent.length; j++) {
      allUsers.add(allUsersByComponent[j]);
      compteur = j + 1;
    }
    for (int j = compteur; j < controlledUsers.length; j++) {
      if (!allUsers.contains(controlledUsers[j]))
      allUsers.add(controlledUsers[j]);
    }
    
    // création de la liste de tous les utilisateur ayant le droit de lecture
    Collection<HistoryByUser> statByUser = new ArrayList<HistoryByUser>();
    for (UserDetail user : allUsers) {
      if (user != null) {
        HistoryByUser historyByUser = new HistoryByUser(user, null, 0);
        statByUser.add(historyByUser);
      }
    }

    // création d'une liste des accès par utilisateur
    Hashtable<UserDetail, Date> byUser = new Hashtable<UserDetail, Date>();
    Hashtable<UserDetail, Integer> nbAccessbyUser = new Hashtable<UserDetail, Integer>();
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
          nbAccessbyUser.put(controlledUsers[j], new Integer(nbAccess));
        } else {
          byUser.put(controlledUsers[j], date[j]);
          nbAccessbyUser.put(controlledUsers[j], new Integer(1));
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
      Date lastAccess = (Date) byUser.get(user);
      if (lastAccess != null)
        historyByUser.setLastAccess(lastAccess);
      // recherche du nombre d'accès
      Integer nbAccess = (Integer) nbAccessbyUser.get(user);
      if (nbAccess != null)
        historyByUser.setNbAccess(nbAccess.intValue());
    }

    // tri de la liste pour mettre en premier les users ayant consulté
    LastAccessComparatorDesc comparateur = new LastAccessComparatorDesc();

    Collections.sort((List<HistoryByUser>) statByUser, comparateur);

    SilverTrace.info("statistic", "StatisticBmEJB.getHistoryByObject()",
        "root.MSG_GEN_EXIT_METHOD");
    return statByUser;
  }

  /**
   * Method declaration
   * @param foreignPK
   * @return
   * @see
   */
  public void deleteHistoryByAction(ForeignPK foreignPK, int action,
      String objectType) {
    SilverTrace.info("statistic", "StatisticBmEJB.deleteHistoryByAction",
        "root.MSG_GEN_ENTER_METHOD");
    Connection con = null;

    try {
      con = getConnection();
      HistoryObjectDAO.deleteHistoryByObject(con, historyTableName, foreignPK,
          objectType);
    } catch (Exception e) {
      throw new StatisticRuntimeException(
          "StatisticBmEJB().deleteHistoryByAction",
          SilverpeasRuntimeException.ERROR,
          "statistic.CANNOT_DELETE_HISTORY_STATISTICS_PUBLICATION", e);
    } finally {
      freeConnection(con);
    }
  }

  public void moveStat(ForeignPK toForeignPK, int actionType, String objectType) {
    SilverTrace.info("statistic", "StatisticBmEJB.deleteHistoryByAction",
        "root.MSG_GEN_ENTER_METHOD");
    Connection con = null;
    try {
      con = getConnection();
      HistoryObjectDAO.move(con, historyTableName, toForeignPK, actionType, objectType);
    } catch (Exception e) {
      throw new StatisticRuntimeException("StatisticBmEJB().addObjectToHistory()",
          SilverpeasRuntimeException.ERROR, "statistic.CANNOT_ADD_VISITE_NODE", e);
    } finally {
      freeConnection(con);
    }
  }

  /**
   * Method declaration
   * @throws CreateException
   * @see
   */
  public void ejbCreate() throws CreateException {
  }

  /**
   * Method declaration
   * @see
   */
  public void ejbRemove() {
  }

  /**
   * Method declaration
   * @see
   */
  public void ejbActivate() {
  }

  /**
   * Method declaration
   * @see
   */
  public void ejbPassivate() {
  }

  /**
   * Method declaration
   * @param sc
   * @see
   */
  public void setSessionContext(SessionContext sc) {
  }
}
