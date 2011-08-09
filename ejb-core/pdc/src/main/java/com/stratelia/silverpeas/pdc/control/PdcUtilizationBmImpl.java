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
package com.stratelia.silverpeas.pdc.control;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import com.stratelia.silverpeas.classifyEngine.ClassifyEngine;
import com.stratelia.silverpeas.pdc.model.AxisHeader;
import com.stratelia.silverpeas.pdc.model.AxisHeaderPersistence;
import com.stratelia.silverpeas.pdc.model.AxisPK;
import com.stratelia.silverpeas.pdc.model.PdcException;
import com.stratelia.silverpeas.pdc.model.UsedAxis;
import com.stratelia.silverpeas.pdc.model.UsedAxisPK;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.persistence.PersistenceException;
import com.stratelia.webactiv.persistence.SilverpeasBeanDAO;
import com.stratelia.webactiv.persistence.SilverpeasBeanDAOFactory;
import com.stratelia.webactiv.searchEngine.model.AxisFilter;
import com.stratelia.webactiv.util.DBUtil;
import com.stratelia.webactiv.util.JNDINames;
import com.stratelia.webactiv.util.exception.SilverpeasException;

/**
 * Class declaration
 * @author
 */
public class PdcUtilizationBmImpl implements PdcUtilizationBm {

  /**
   * SilverpeasBeanDAO is the main link with the SilverPeas persitence. We indicate the Object
   * SilverPeas which map the database.
   */
  private SilverpeasBeanDAO<UsedAxis> dao = null;
  private PdcUtilizationDAO utilizationDAO = new PdcUtilizationDAO();

  public PdcUtilizationBmImpl() {
    try {
      dao = SilverpeasBeanDAOFactory.getDAO("com.stratelia.silverpeas.pdc.model.UsedAxis");
    } catch (PersistenceException exce_DAO) {
      SilverTrace.error("PDC", "PdcUtilizationBmImpl",
          "Pdc.CANNOT_CONSTRUCT_PERSISTENCE", exce_DAO);
    }
  }

  /**
   * Returns an axis used by an instance
   * @param usedAxisId - the whished used axis.
   * @return an UsedAxis
   */
  @Override
  public UsedAxis getUsedAxis(String usedAxisId) throws PdcException {
    UsedAxis usedAxis = null;

    try {
      usedAxis = (UsedAxis) dao.findByPrimaryKey(new UsedAxisPK(usedAxisId));
    } catch (PersistenceException exce_select) {
      throw new PdcException("PdcUtilizationBmImpl.getUsedAxis",
          SilverpeasException.ERROR, "Pdc.CANNOT_FIND_USED_AXIS", exce_select);
    }
    return usedAxis;
  }

  /**
   * Returns a list of used axis sorted.
   * @return a list sorted or null otherwise
   */
  @Override
  public List<UsedAxis> getUsedAxisByInstanceId(String instanceId) throws PdcException {
    List<UsedAxis> usedAxis = null;
    Connection con = openConnection();

    try {
      usedAxis = utilizationDAO.getUsedAxisByInstanceId(con, instanceId);
    } catch (Exception e) {
      throw new PdcException("PdcUtilizationBmImpl.getUsedAxisByInstanceId",
          SilverpeasException.ERROR, "Pdc.CANNOT_FIND_USED_AXIS", e);
    } finally {
      closeConnection(con);
    }

    return usedAxis;
  }

  /**
   * Returns a list of axis header sorted.
   * @return a list sorted or null otherwise
   */
  @Override
  public List<AxisHeader> getAxisHeaderUsedByInstanceId(String instanceId)
      throws PdcException {
    List<String> instanceIds = new ArrayList<String>();
    instanceIds.add(instanceId);
    return getAxisHeaderUsedByInstanceIds(instanceIds);
  }

  @Override
  public List<AxisHeader> getAxisHeaderUsedByInstanceIds(List<String> instanceIds)
      throws PdcException {
    return getAxisHeaderUsedByInstanceIds(instanceIds, new AxisFilter());
  }

  @Override
  public List<AxisHeader> getAxisHeaderUsedByInstanceIds(List<String> instanceIds, AxisFilter filter)
      throws PdcException {
    List<AxisHeader> axisHeaders = new ArrayList<AxisHeader>();
    if (instanceIds == null || instanceIds.isEmpty()) {
      return axisHeaders;
    }

    Connection con = null;
    try {
      ClassifyEngine classifyEngine = new ClassifyEngine();
      List<Integer> ids = classifyEngine.getPertinentAxisByInstanceIds(instanceIds);

      if (ids == null || ids.isEmpty()) {
        return axisHeaders;
      }

      StringBuilder inClause = new StringBuilder(1000);
      boolean first = true;
      for (Integer instanceId : ids) {
        if (!first) {
          inClause.append(",");
        }
        inClause.append(instanceId);
        first = false;
      }

      SilverpeasBeanDAO<AxisHeaderPersistence> dao =
          SilverpeasBeanDAOFactory.<AxisHeaderPersistence> getDAO(
              "com.stratelia.silverpeas.pdc.model.AxisHeaderPersistence");
      con = openConnection();
      Collection<AxisHeaderPersistence> result =
          dao.findByWhereClause(con, new AxisPK("useless"), "id IN (" + inClause.toString() + ")");

      if (result != null) {
        for (AxisHeaderPersistence silverpeasBean : result) {
          AxisHeader axisHeader = new AxisHeader(silverpeasBean);
          axisHeaders.add(axisHeader);
        }
      }

      return axisHeaders;
    } catch (Exception e) {
      throw new PdcException("PdcUtilizationBmImpl.getUsedAxisByInstanceId",
          SilverpeasException.ERROR, "Pdc.CANNOT_FIND_USED_AXIS", e);
    } finally {
      closeConnection(con);
    }
  }

  /**
   * Returns the usedAxis based on a defined axis
   * @param axisId - the id of the axis
   */
  private List<UsedAxis> getUsedAxisByAxisId(Connection con, int axisId)
      throws PdcException {
    try {
      return (List<UsedAxis>) dao.findByWhereClause(con, new UsedAxisPK("useless"),
          "axisId = " + axisId);
    } catch (Exception e) {
      throw new PdcException("PdcUtilizationBmImpl.getUsedAxisByAxisId",
          SilverpeasException.ERROR, "Pdc.CANNOT_FIND_USED_AXIS", e);
    }
  }

  /**
   * Create an used axis into the data base.
   * @param usedAxis - the object which contains all data about utilization of an axis
   * @return usedAxisId
   */
  @Override
  public int addUsedAxis(UsedAxis usedAxis, String treeId) throws PdcException {
    Connection con = openConnection();

    try {
      if (utilizationDAO.isAlreadyAdded(con, usedAxis.getInstanceId(),
          Integer.parseInt(usedAxis.getPK().getId()), usedAxis
          .getAxisId(), usedAxis.getBaseValue(), treeId)) {
        return 1;
      } else {
        dao.add(usedAxis);
        // une fois cette axe rajouté, il faut tenir compte de la propagation
        // des choix aux niveaux
        // obligatoire/facultatif et variant/invariante
        // PAS ENCORE UTILE
        // PdcUtilizationDAO.updateAllUsedAxis(con, usedAxis);
      }
    } catch (Exception exce_create) {
      throw new PdcException("PdcUtilizationBmImpl.addUsedAxis",
          SilverpeasException.ERROR, "Pdc.CANNOT_ADD_USED_AXIS", exce_create);
    } finally {
      closeConnection(con);
    }
    return 0;
  }

  /**
   * Update an used axis into the data base.
   * @param usedAxis - the object which contains all data about utilization of the axis
   */
  @Override
  public int updateUsedAxis(UsedAxis usedAxis, String treeId)
      throws PdcException {
    Connection con = openConnection();

    try {
      // test si la valeur de base a été modifiée
      int newBaseValue = usedAxis.getBaseValue();
      int oldBaseValue = (getUsedAxis(usedAxis.getPK().getId())).getBaseValue();
      // si elle a été modifiée alors on reporte la modification.
      if (newBaseValue != oldBaseValue) {
        if (utilizationDAO.isAlreadyAdded(con, usedAxis.getInstanceId(),
            new Integer(usedAxis.getPK().getId()).intValue(), usedAxis
            .getAxisId(), usedAxis.getBaseValue(), treeId))
          return 1;
      }
      dao.update(usedAxis);
      // une fois cette axe modifié, il faut tenir compte de la propagation des
      // choix aux niveaux
      // obligatoire/facultatif et variant/invariante
      utilizationDAO.updateAllUsedAxis(con, usedAxis);
      return 0;
    } catch (Exception exce_create) {
      throw new PdcException("PdcUtilizationBmImpl.updateUsedAxis",
          SilverpeasException.ERROR, "Pdc.CANNOT_UPDATE_USED_AXIS", exce_create);
    } finally {
      closeConnection(con);
    }
  }

  /**
   * delete the used axis from the data base
   * @param usedAxisId - the id of the used axe
   */
  @Override
  public void deleteUsedAxis(String usedAxisId) throws PdcException {
    try {
      dao.remove(new UsedAxisPK(usedAxisId));
    } catch (Exception exce_delete) {
      throw new PdcException("PdcUtilizationBmImpl.deleteUsedAxis",
          SilverpeasException.ERROR, "Pdc.CANNOT_DELETE_USED_AXIS", exce_delete);
    }
  }

  /**
   * Method declaration
   * @param usedAxisIds
   * @throws PdcException
   * @see
   */
  @Override
  public void deleteUsedAxis(Collection<String> usedAxisIds) throws PdcException {
    try {
      Iterator<String> it = usedAxisIds.iterator();
      String usedAxisId = "";
      String whereClause = " 0 = 1 ";

      while (it.hasNext()) {
        usedAxisId = it.next();
        whereClause += " or " + usedAxisId;
      }
      dao.removeWhere(new UsedAxisPK("useless"), whereClause);
    } catch (Exception exce_delete) {
      throw new PdcException("PdcUtilizationBmImpl.deleteUsedAxis",
          SilverpeasException.ERROR, "Pdc.CANNOT_DELETE_USED_AXIS", exce_delete);
    }
  }

  /**
   * Method declaration
   * @param axisId
   * @throws PdcException
   * @see
   */
  @Override
  public void deleteUsedAxisByAxisId(Connection con, String axisId)
      throws PdcException {
    try {
      dao.removeWhere(con, new UsedAxisPK("useless"), " axisId = " + axisId);
    } catch (Exception exce_delete) {
      throw new PdcException("PdcUtilizationBmImpl.deleteUsedAxisByAxisId",
          SilverpeasException.ERROR, "Pdc.CANNOT_DELETE_USED_AXIS", exce_delete);
    }
  }

  /**
   * Method declaration
   * @param valueId
   * @throws PdcException
   * @see
   */
  private void deleteUsedAxisByValueId(Connection con, int valueId, int axisId)
      throws PdcException {
    try {
      dao.removeWhere(con, new UsedAxisPK("useless"), " axisId = " + axisId
          + " and baseValue = " + valueId);
    } catch (Exception exce_delete) {
      throw new PdcException("PdcUtilizationBmImpl.deleteUsedAxisByValueId",
          SilverpeasException.ERROR, "Pdc.CANNOT_DELETE_USED_AXIS", exce_delete);
    }
  }

  /**
   * Method declaration
   * @param valueId
   * @throws PdcException
   * @see
   */

  @Override
  public void deleteUsedAxisByMotherValue(Connection con, String valueId,
      String axisId, String treeId) throws PdcException {
    try {
      dao.removeWhere(con, new UsedAxisPK("useless"), " axisId = " + axisId
          + " and baseValue in ( select id from SB_Tree_Tree where treeId = "
          + treeId + " and (path like '%/" + valueId + "/%' or id = " + valueId
          + " ))");
    } catch (Exception exce_delete) {
      throw new PdcException("PdcUtilizationBmImpl.deleteUsedAxisByValueId",
          SilverpeasException.ERROR, "Pdc.CANNOT_DELETE_USED_AXIS", exce_delete);
    }
  }

  /**
   * Update a base value from the PdcUtilizationDAO
   * @param valueId - the base value that must be updated
   */
  private void updateBaseValue(Connection con, int oldBaseValue,
      int newBaseValue, int axisId, String treeId, String instanceId)
      throws PdcException {
    try {
      utilizationDAO.updateBaseValue(con, oldBaseValue, newBaseValue,
          axisId, treeId, instanceId);
    } catch (Exception exce_update) {
      throw new PdcException("PdcUtilizationBmImpl.updateUsedAxis",
          SilverpeasException.ERROR, "Pdc.CANNOT_UPDATE_USED_AXIS", exce_update);
    }
  }

  @Override
  public void updateOrDeleteBaseValue(Connection con, int baseValueToUpdate,
      int newBaseValue, int axisId, String treeId) throws PdcException {
    SilverTrace.info("Pdc", "PdcBmImpl.updateOrDeleteBaseValue",
        "root.MSG_GEN_PARAM_VALUE", "baseValueToUpdate = " + baseValueToUpdate);
    SilverTrace.info("Pdc", "PdcBmImpl.updateOrDeleteBaseValue",
        "root.MSG_GEN_PARAM_VALUE", "newBaseValue = " + newBaseValue);
    SilverTrace.info("Pdc", "PdcBmImpl.updateOrDeleteBaseValue",
        "root.MSG_GEN_PARAM_VALUE", "axisId = " + axisId);
    SilverTrace.info("Pdc", "PdcBmImpl.updateOrDeleteBaseValue",
        "root.MSG_GEN_PARAM_VALUE", "treeId = " + treeId);

    List<UsedAxis> usedAxisList = getUsedAxisByAxisId(con, axisId);
    // pour chaque instance, on vérifie que la modification est possible
    String instanceId = null;
    UsedAxis usedAxis = null;
    boolean updateAllowed = false;
    for (int i = 0; i < usedAxisList.size(); i++) {
      usedAxis = (UsedAxis) usedAxisList.get(i);
      instanceId = usedAxis.getInstanceId();
      SilverTrace.info("Pdc", "PdcBmImpl.updateOrDeleteBaseValue",
          "root.MSG_GEN_PARAM_VALUE", "instanceId = " + instanceId);

      if (usedAxis.getBaseValue() == baseValueToUpdate) {
        try {
          // test si la nouvelle valeur est autorisée comme nouvelle valeur de
          // base
          updateAllowed = !utilizationDAO.isAlreadyAdded(con, instanceId,
              new Integer(usedAxis.getPK().getId()).intValue(), axisId,
              newBaseValue, treeId);
        } catch (Exception e) {
          throw new PdcException(
              "PdcUtilizationBmImpl.updateOrDeleteBaseValue",
              SilverpeasException.ERROR, "Pdc.CANNOT_UPDATE_USED_AXIS", e);
        }

        SilverTrace.info("Pdc", "PdcBmImpl.updateOrDeleteBaseValue",
            "root.MSG_GEN_PARAM_VALUE", "updateAllowed = " + updateAllowed);

        if (updateAllowed) {
          updateBaseValue(con, baseValueToUpdate, newBaseValue, axisId, treeId,
              instanceId); // replace this value by its mother
        }
      }
    }
    deleteUsedAxisByValueId(con, baseValueToUpdate, axisId);
  }

  /**
   * *********************************************
   */

  /**
   * ******** DATABASE CONNECTION MANAGER ********
   */

  /**
   * *********************************************
   */

  /**
   * Method declaration
   * @return
   * @see
   */
  private Connection openConnection() throws PdcException {
    try {
      Connection con = DBUtil.makeConnection(JNDINames.PDC_DATASOURCE);

      return con;
    } catch (Exception e) {
      throw new PdcException("PdcUtilizationBmImpl.openConnection()",
          SilverpeasException.ERROR, "root.EX_CONNECTION_OPEN_FAILED", e);
    }
  }

  /**
   * Method declaration
   * @param con
   * @see
   */
  private void closeConnection(Connection con) {
    if (con != null) {
      try {
        con.close();
      } catch (Exception e) {
        SilverTrace.error("Pdc", "PdcUtilizationBmImpl.closeConnection()",
            "root.EX_CONNECTION_CLOSE_FAILED", "", e);
      }
    }
  }

}
