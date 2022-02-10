/*
 * Copyright (C) 2000 - 2022 Silverpeas
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
 * "https://www.silverpeas.org/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.silverpeas.core.pdc.pdc.service;

import org.silverpeas.core.SilverpeasRuntimeException;
import org.silverpeas.core.admin.component.ComponentInstanceDeletion;
import org.silverpeas.core.annotation.Service;
import org.silverpeas.core.pdc.classification.ClassifyEngine;
import org.silverpeas.core.pdc.pdc.model.AxisHeader;
import org.silverpeas.core.pdc.pdc.model.AxisHeaderPersistence;
import org.silverpeas.core.pdc.pdc.model.AxisPK;
import org.silverpeas.core.pdc.pdc.model.PdcException;
import org.silverpeas.core.pdc.pdc.model.UsedAxis;
import org.silverpeas.core.pdc.pdc.model.UsedAxisPK;
import org.silverpeas.core.persistence.jdbc.DBUtil;
import org.silverpeas.core.persistence.jdbc.bean.PersistenceException;
import org.silverpeas.core.persistence.jdbc.bean.SilverpeasBeanDAO;
import org.silverpeas.core.persistence.jdbc.bean.SilverpeasBeanDAOFactory;
import org.silverpeas.core.util.logging.SilverLogger;

import javax.inject.Inject;
import javax.transaction.Transactional;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

/**
 * @author
 */
@Service
public class DefaultPdcUtilizationService implements PdcUtilizationService,
    ComponentInstanceDeletion {

  private static final String USELESS = "useless";
  private static final String AXIS_ID_EQUALS = " axisId = ";
  @Inject
  private ClassifyEngine classifyEngine;

  /**
   * SilverpeasBeanDAO is the main link with the SilverPeas persitence. We indicate the Object
   * SilverPeas which map the database.
   */
  private SilverpeasBeanDAO<UsedAxis> dao = null;

  @Inject
  private PdcUtilizationDAO utilizationDAO;

  protected DefaultPdcUtilizationService() {
    try {
      dao = SilverpeasBeanDAOFactory.getDAO("org.silverpeas.core.pdc.pdc.model.UsedAxis");
    } catch (PersistenceException e) {
      SilverLogger.getLogger(this).error("Failed to get the DAO for UsedAxis", e);
    }
  }

  /**
   * Returns an axis used by an instance
   * @param usedAxisId - the whished used axis.
   * @return an UsedAxis
   */
  @Override
  public UsedAxis getUsedAxis(String usedAxisId) throws PdcException {
    UsedAxis usedAxis;

    try {
      usedAxis = dao.findByPrimaryKey(new UsedAxisPK(usedAxisId));
    } catch (PersistenceException e) {
      throw new PdcException(e);
    }
    return usedAxis;
  }

  /**
   * Returns a list of used axis sorted.
   * @return a list sorted or null otherwise
   */
  @Override
  public List<UsedAxis> getUsedAxisByInstanceId(String instanceId) throws PdcException {
    final List<UsedAxis> usedAxis;
    try (final Connection con = DBUtil.openConnection()) {
      usedAxis = utilizationDAO.getUsedAxisByInstanceId(con, instanceId);
    } catch (Exception e) {
      throw new PdcException(e);
    }

    return usedAxis;
  }

  @Override
  public List<AxisHeader> getAxisHeaderUsedByInstanceIds(List<String> instanceIds)
      throws PdcException {
    if (instanceIds == null || instanceIds.isEmpty()) {
      return new ArrayList<>(0);
    }

    try (Connection con = DBUtil.openConnection()) {
      List<Integer> ids = classifyEngine.getPertinentAxisByInstanceIds(instanceIds);

      if (ids == null || ids.isEmpty()) {
        return new ArrayList<>(0);
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

      SilverpeasBeanDAO<AxisHeaderPersistence> theDao = SilverpeasBeanDAOFactory.getDAO(
              "org.silverpeas.core.pdc.pdc.model.AxisHeaderPersistence");
      Collection<AxisHeaderPersistence> result =
          theDao.findByWhereClause(con, new AxisPK(USELESS), "id IN (" + inClause.toString() + ")");

      List<AxisHeader> axisHeaders = new ArrayList<>();
      if (result != null) {
        for (AxisHeaderPersistence silverpeasBean : result) {
          AxisHeader axisHeader = new AxisHeader(silverpeasBean);
          axisHeaders.add(axisHeader);
        }
      }

      return axisHeaders;
    } catch (Exception e) {
      throw new PdcException(e);
    }
  }

  /**
   * Returns the usedAxis based on a defined axis
   * @param axisId - the id of the axis
   */
  private List<UsedAxis> getUsedAxisByAxisId(Connection con, int axisId) throws PdcException {
    try {
      return (List<UsedAxis>) dao.findByWhereClause(con, new UsedAxisPK(USELESS),
          "axisId = " + axisId);
    } catch (Exception e) {
      throw new PdcException(e);
    }
  }

  /**
   * Create an used axis into the data base.
   * @param usedAxis - the object which contains all data about utilization of an axis
   * @param treeId
   * @return usedAxisId
   */
  @Override
  public int addUsedAxis(UsedAxis usedAxis, String treeId) throws PdcException {
    try (final Connection con = DBUtil.openConnection()) {
      if (utilizationDAO
          .isAlreadyAdded(con, usedAxis.getInstanceId(), Integer.parseInt(usedAxis.getPK().getId()),
              usedAxis.getAxisId(), usedAxis.getBaseValue(), treeId)) {
        return 1;
      } else {
        dao.add(usedAxis);
      }
    } catch (Exception e) {
      throw new PdcException(e);
    }
    return 0;
  }

  /**
   * Update an used axis into the data base.
   * @param usedAxis - the object which contains all data about utilization of the axis
   */
  @Override
  public int updateUsedAxis(UsedAxis usedAxis, String treeId) throws PdcException {
    try (Connection con = DBUtil.openConnection()) {
      // test si la valeur de base a été modifiée
      int newBaseValue = usedAxis.getBaseValue();
      int oldBaseValue = (getUsedAxis(usedAxis.getPK().getId())).getBaseValue();
      // si elle a été modifiée alors on reporte la modification.
      if (newBaseValue != oldBaseValue &&
          utilizationDAO.isAlreadyAdded(con, usedAxis.getInstanceId(),
            Integer.parseInt(usedAxis.getPK().getId()), usedAxis.getAxisId(),
            usedAxis.getBaseValue(), treeId)) {
          return 1;
      }
      dao.update(usedAxis);
      // une fois cette axe modifié, il faut tenir compte de la propagation des
      // choix aux niveaux obligatoire/facultatif et variant/invariante
      utilizationDAO.updateAllUsedAxis(con, usedAxis);
      return 0;
    } catch (Exception e) {
      throw new PdcException(e);
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
    } catch (Exception e) {
      throw new PdcException(e);
    }
  }

  /**
   * @param usedAxisIds
   * @throws PdcException
   */
  @Override
  public void deleteUsedAxis(Collection<String> usedAxisIds) throws PdcException {
    try {
      Iterator<String> it = usedAxisIds.iterator();
      StringBuilder whereClause = new StringBuilder(" 0 = 1 ");
      while (it.hasNext()) {
        final String usedAxisId = it.next();
        whereClause.append(" or " + usedAxisId);
      }
      dao.removeWhere(new UsedAxisPK(USELESS), whereClause.toString());
    } catch (Exception e) {
      throw new PdcException(e);
    }
  }

  /**
   * Method declaration
   * @param con
   * @param axisId
   * @throws PdcException
   * @see
   */
  @Override
  public void deleteUsedAxisByAxisId(Connection con, String axisId) throws PdcException {
    try {
      Objects.requireNonNull(con);
      dao.removeWhere(con, new UsedAxisPK(USELESS), AXIS_ID_EQUALS + axisId);
    } catch (Exception e) {
      throw new PdcException(e);
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
      dao.removeWhere(con, new UsedAxisPK(USELESS),
          AXIS_ID_EQUALS + axisId + " and baseValue = " + valueId);
    } catch (Exception e) {
      throw new PdcException(e);
    }
  }

  /**
   * @param con
   * @param valueId
   * @param axisId
   * @param treeId
   * @throws PdcException
   */
  @Override
  public void deleteUsedAxisByMotherValue(Connection con, String valueId, String axisId,
      String treeId) throws PdcException {
    try {
      dao.removeWhere(con, new UsedAxisPK(USELESS), AXIS_ID_EQUALS + axisId +
          " and baseValue in ( select id from SB_Tree_Tree where treeId = " + treeId +
          " and (path like '%/" + valueId + "/%' or id = " + valueId + " ))");
    } catch (Exception e) {
      throw new PdcException(e);
    }
  }

  /**
   * Update a base value from the PdcUtilizationDAO
   */
  private void updateBaseValue(Connection con, int oldBaseValue, int newBaseValue, int axisId,
      String treeId, String instanceId) throws PdcException {
    try {
      utilizationDAO.updateBaseValue(con, oldBaseValue, newBaseValue, axisId, treeId, instanceId);
    } catch (Exception e) {
      throw new PdcException(e);
    }
  }

  @Override
  public void updateOrDeleteBaseValue(Connection con, int baseValueToUpdate, int newBaseValue,
      int axisId, String treeId) throws PdcException {
    Objects.requireNonNull(con);
    final List<UsedAxis> usedAxisList = getUsedAxisByAxisId(con, axisId);
    // pour chaque instance, on vérifie que la modification est possible
    boolean updateAllowed;
    for (UsedAxis anUsedAxisList : usedAxisList) {
      final UsedAxis usedAxis = anUsedAxisList;
      final String instanceId = usedAxis.getInstanceId();
      if (usedAxis.getBaseValue() == baseValueToUpdate) {
        try {
          // test si la nouvelle valeur est autorisée comme nouvelle valeur de base
          updateAllowed = !utilizationDAO
              .isAlreadyAdded(con, instanceId, Integer.parseInt(usedAxis.getPK().getId()), axisId,
                  newBaseValue, treeId);
        } catch (Exception e) {
          throw new PdcException(e);
        }

        if (updateAllowed) {
          updateBaseValue(con, baseValueToUpdate, newBaseValue, axisId, treeId,
              instanceId); // replace this value by its mother
        }
      }
    }
    deleteUsedAxisByValueId(con, baseValueToUpdate, axisId);
  }

  /**
   * Deletes the resources belonging to the specified component instance. This method is invoked
   * by Silverpeas when a component instance is being deleted.
   * @param componentInstanceId the unique identifier of a component instance.
   */
  @Override
  @Transactional
  public void delete(final String componentInstanceId) {
    try (Connection connection = DBUtil.openConnection()) {
      utilizationDAO.deleteAllAxisUsedByInstanceId(connection, componentInstanceId);
    } catch (SQLException e) {
      throw new SilverpeasRuntimeException(e.getMessage(), e);
    }
  }
}
