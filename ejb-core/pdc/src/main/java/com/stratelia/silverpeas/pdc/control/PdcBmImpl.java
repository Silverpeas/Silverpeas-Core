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

import com.silverpeas.pdc.PdcServiceFactory;
import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import com.silverpeas.pdc.dao.PdcRightsDAO;
import com.silverpeas.pdc.model.PdcAxisValue;
import com.silverpeas.pdc.service.PdcClassificationService;
import com.silverpeas.pdcSubscription.util.PdcSubscriptionUtil;
import com.silverpeas.util.i18n.I18NHelper;
import com.silverpeas.util.security.ComponentSecurity;
import com.stratelia.silverpeas.classifyEngine.ClassifyEngine;
import com.stratelia.silverpeas.classifyEngine.ObjectValuePair;
import com.stratelia.silverpeas.classifyEngine.PertinentAxis;
import com.stratelia.silverpeas.classifyEngine.PertinentValue;
import com.stratelia.silverpeas.classifyEngine.Position;
import com.stratelia.silverpeas.containerManager.ContainerInterface;
import com.stratelia.silverpeas.containerManager.ContainerManagerException;
import com.stratelia.silverpeas.containerManager.ContainerPositionInterface;
import com.stratelia.silverpeas.pdc.model.Axis;
import com.stratelia.silverpeas.pdc.model.AxisHeader;
import com.stratelia.silverpeas.pdc.model.AxisHeaderI18N;
import com.stratelia.silverpeas.pdc.model.AxisHeaderPersistence;
import com.stratelia.silverpeas.pdc.model.AxisPK;
import com.stratelia.silverpeas.pdc.model.ClassifyPosition;
import com.stratelia.silverpeas.pdc.model.ClassifyValue;
import com.stratelia.silverpeas.pdc.model.PdcException;
import com.stratelia.silverpeas.pdc.model.SearchAxis;
import com.stratelia.silverpeas.pdc.model.SearchContext;
import com.stratelia.silverpeas.pdc.model.SearchCriteria;
import com.stratelia.silverpeas.pdc.model.UsedAxis;
import com.stratelia.silverpeas.pdc.model.Value;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.silverpeas.treeManager.control.TreeBm;
import com.stratelia.silverpeas.treeManager.control.TreeBmImpl;
import com.stratelia.silverpeas.treeManager.model.TreeNode;
import com.stratelia.silverpeas.treeManager.model.TreeNodePK;
import com.stratelia.silverpeas.util.JoinStatement;
import com.stratelia.webactiv.beans.admin.OrganizationController;
import com.stratelia.webactiv.persistence.PersistenceException;
import com.stratelia.webactiv.persistence.SilverpeasBeanDAO;
import com.stratelia.webactiv.persistence.SilverpeasBeanDAOFactory;
import com.stratelia.webactiv.searchEngine.model.AxisFilter;
import com.stratelia.webactiv.util.DBUtil;
import com.stratelia.webactiv.util.JNDINames;
import com.stratelia.webactiv.util.exception.SilverpeasException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import javax.inject.Named;

@Named("pdcBm")
public class PdcBmImpl implements PdcBm, ContainerInterface {

  /**
   * SilverpeasBeanDAO is the main link with the SilverPeas persistence. We indicate the Object
   * SilverPeas which map the database.
   */
  private SilverpeasBeanDAO<AxisHeaderPersistence> dao = null;
  private AxisHeaderI18NDAO axisHeaderI18NDAO = new AxisHeaderI18NDAO();
  /**
   * PdcUtilizationBm, the pdc utilization interface to manage which axis are used by which instance
   */
  private PdcUtilizationBm pdcUtilizationBm = new PdcUtilizationBmImpl();
  /**
   * PdcClassifyBm, the pdc classify interface to manage how are classified object in the pdc
   */
  private PdcClassifyBm pdcClassifyBm = new PdcClassifyBmImpl();
  /**
   * TreeBm, the node interface to manage operations user
   */
  private TreeBm tree = new TreeBmImpl();
  private static Map<String, AxisHeader> axisHeaders =
          Collections.synchronizedMap(new HashMap<String, AxisHeader>());

  /**
   * Constructor declaration
   * @see
   */
  public PdcBmImpl() {
    try {
      dao = SilverpeasBeanDAOFactory.<AxisHeaderPersistence>getDAO(
              "com.stratelia.silverpeas.pdc.model.AxisHeaderPersistence");
    } catch (PersistenceException exce_DAO) {
      SilverTrace.error("Pdc", "PdcBmImpl", "Pdc.CANNOT_CONSTRUCT_PERSISTENCE",
              exce_DAO);
    }
  }

  /**
   * Returns a list of axes sorted in according to the axe type.
   * @param type - the whished type of the axe.
   * @return a sorted list.
   */
  @Override
  public List<AxisHeader> getAxisByType(String type) throws PdcException {
    try {
      Collection<AxisHeaderPersistence> axis = dao.findByWhereClause(new AxisPK("useless"),
              " AxisType='" + type + "' order by AxisOrder ");
      return persistence2AxisHeaders(axis);
    } catch (PersistenceException exce_select) {
      throw new PdcException("PdcBmImpl.getAxisByType",
              SilverpeasException.ERROR, "Pdc.CANNOT_FIND_AXES_TYPE", exce_select);
    }
  }

  private List<AxisHeader> persistence2AxisHeaders(Collection<AxisHeaderPersistence> silverpeasBeans)
          throws PersistenceException, PdcException {
    List<AxisHeader> resultingAxisHeaders = new ArrayList<AxisHeader>();
    if (silverpeasBeans != null) {
      for (AxisHeaderPersistence silverpeasBean : silverpeasBeans) {
        AxisHeader axisHeader = new AxisHeader(silverpeasBean);
        // ajout des traductions
        setTranslations(axisHeader);
        resultingAxisHeaders.add(axisHeader);
      }
    }
    return resultingAxisHeaders;
  }

  private void setTranslations(AxisHeader axisHeader) {
    // ajout de la traduction par defaut
    int axisId = Integer.parseInt(axisHeader.getPK().getId());
    AxisHeaderI18N translation = new AxisHeaderI18N(axisId, axisHeader.getLanguage(), axisHeader.
            getName(), axisHeader.getDescription());
    axisHeader.addTranslation(translation);
    // récupération des autres traductions
    Connection con = null;
    try {
      con = openConnection();
      List<AxisHeaderI18N> translations = axisHeaderI18NDAO.getTranslations(con, axisId);
      for (AxisHeaderI18N tr : translations) {
        axisHeader.addTranslation(tr);
      }
    } catch (Exception e) {
    } finally {
      DBUtil.close(con);
    }
  }

  /**
   * Returns a list of axes sorted.
   * @return a list sorted or null otherwise
   */
  @Override
  public List<AxisHeader> getAxis() throws PdcException {
    try {
      Collection<AxisHeaderPersistence> axis = dao.findByWhereClause(new AxisPK("useless"),
              " 1=1 order by AxisType asc, AxisOrder asc ");
      return persistence2AxisHeaders(axis);
    } catch (PersistenceException exce_select) {
      throw new PdcException("PdcBmImpl.getAxis", SilverpeasException.ERROR,
              "Pdc.CANNOT_FIND_AXES", exce_select);
    }

  }

  /**
   * Return the number of axe.
   * @return the number of axe
   */
  @Override
  public int getNbAxis() throws PdcException {
    return getAxis().size();
  }

  /**
   * Return the max number of axis.
   * @return the max number of axis
   */
  @Override
  public int getNbMaxAxis() throws PdcException {
    return ClassifyEngine.getMaxAxis();
  }

  /**
   * Create an axe into the data base.
   * @param axisHeader - the object which contains all data about an axe
   * @return 1 if the maximun of axe is atteignable, 2 if the axe already exist, 0 otherwise
   */
  @Override
  public int createAxis(AxisHeader axisHeader) throws PdcException {

    int status = 0;
    List<AxisHeader> axis = getAxis();

    // search if the maximun number of axes is atteignable
    if (axis.size() > getNbMaxAxis()) {
      status = 1;
    } else if (isAxisNameExist(axis, axisHeader)) {
      status = 2;
    } else {
      Connection con = null;
      try {
        con = openTransaction();
        int order = axisHeader.getAxisOrder();
        String type = axisHeader.getAxisType();
        // recupere les axes de meme type ordonnés qui ont un numéro d'ordre
        // >= à celui de l'axe à inserer
        String whereClause = "AxisType = '" + type + "' and AxisOrder >= "
                + order + " ORDER BY AxisOrder ASC";

        // ATTENTION il faut traiter l'ordre des autres axes
        Collection<AxisHeaderPersistence> axisToUpdate = dao.findByWhereClause(axisHeader.getPK(),
                whereClause);

        for (AxisHeaderPersistence axisToMove : axisToUpdate) {
          // On modifie l'ordre de l'axe en ajoutant 1 par rapport au nouvel axe
          order++;
          axisToMove.setAxisOrder(order);
          dao.update(con, axisToMove);
        }

        // build of the Value
        Value value = new Value("unknown", Integer.toString(axisHeader.getRootId()),
                axisHeader.getName(), axisHeader.getDescription(), axisHeader.getCreationDate(),
                axisHeader.getCreatorId(), "unknown", -1, -1, "unknown");

        value.setLanguage(axisHeader.getLanguage());
        value.setRemoveTranslation(axisHeader.isRemoveTranslation());
        value.setTranslationId(axisHeader.getTranslationId());

        String treeId = tree.createRoot(con, value);

        axisHeader.setRootId(Integer.parseInt(treeId));
        SilverTrace.info("Pdc", "PdcBmImpl.createAxis()", "root.MSG_GEN_PARAM_VALUE",
                "axisHeader = " + axisHeader.toString());
        AxisHeaderPersistence ahp = new AxisHeaderPersistence(axisHeader);
        AxisPK axisPK = (AxisPK) dao.add(con, ahp);

        // Register new axis to classifyEngine
        pdcClassifyBm.registerAxis(con, Integer.parseInt(axisPK.getId()));

        commitTransaction(con);
      } catch (Exception exce_create) {
        rollbackTransaction(con);
        throw new PdcException("PdcBmImpl.createAxis",
                SilverpeasException.ERROR, "Pdc.CANNOT_CREATE_AXE", exce_create);
      } finally {
        DBUtil.close(con);
      }
    }

    return status;
  }

  /**
   * Update an axe into the data base.
   * @param axisHeader - the object which contains all data about an axe
   * @return 2 if the axe already exist, 0 otherwise
   */
  @Override
  public int updateAxis(AxisHeader axisHeader) throws PdcException {
    SilverTrace.info("Pdc", "PdcBmImpl.updateAxis()",
            "root.MSG_GEN_PARAM_VALUE", "axisHeader = " + axisHeader.toString());
    int status = 0;
    List<AxisHeader> axis = getAxis();

    if (isAxisNameExist(axis, axisHeader)) {
      status = 2;
    } else {
      Connection con = null;
      try {
        con = openTransaction();
        int order = axisHeader.getAxisOrder(); // si order = -1 alors l'ordre ne
        // doit pas être modifié

        if (order != -1) {
          String type = axisHeader.getAxisType();
          String axisId = axisHeader.getPK().getId();
          // recupere les axes de meme type ordonnés qui ont un numéro d'ordre
          // >= à celui de l'axe à inserer
          String whereClause = "AxisType = '" + type + "' and AxisOrder >= "
                  + order + " ORDER BY AxisOrder ASC";

          // ATTENTION il faut traiter l'ordre des autres axes
          Collection<AxisHeaderPersistence> axisToUpdate = dao.findByWhereClause(con, axisHeader.
                  getPK(), whereClause);

          boolean axisHasMoved = true;
          Iterator<AxisHeaderPersistence> it = axisToUpdate.iterator();
          AxisHeaderPersistence firstAxis = null;

          if (it.hasNext()) {
            // Test si l'axe n'a pas changé de place
            firstAxis = it.next();
            if (firstAxis.getPK().getId().equals(axisId)) {
              axisHasMoved = false;
            }
          }

          if (axisHasMoved) {
            for (AxisHeaderPersistence axisToMove : axisToUpdate) {
              // On modifie l'ordre de l'axe en ajoutant 1 par rapport au nouvel axe
              order++;
              axisToMove.setAxisOrder(order);
              dao.update(con, axisToMove);
              // remove axisheader from cache
              axisHeaders.remove(axisHeader.getPK().getId());
            }
          }
        }
        // update root value linked to this axis
        SilverTrace.info("Pdc", "PdcBmImpl.updateAxis()",
                "root.MSG_GEN_PARAM_VALUE", "axisHeader.getPK().getId() = "
                + axisHeader.getPK().getId());
        AxisHeader oldAxisHeader = getAxisHeader(con, axisHeader.getPK().getId());

        SilverTrace.info("Pdc", "PdcBmImpl.updateAxis()",
                "root.MSG_GEN_PARAM_VALUE", "oldAxisHeader.getRootId() = "
                + oldAxisHeader.getRootId());

        // regarder si le nom et la description ont changé en fonction de la
        // langue
        boolean axisNameHasChanged = false;
        boolean axisDescHasChanged = false;

        if (oldAxisHeader.getName() != null
                && !oldAxisHeader.getName().equalsIgnoreCase(axisHeader.getName())) {
          axisNameHasChanged = true;
        }
        if (oldAxisHeader.getDescription() != null
                && !oldAxisHeader.getDescription().equalsIgnoreCase(axisHeader.getDescription())) {
          axisDescHasChanged = true;
        } else if (oldAxisHeader.getDescription() == null
                && axisHeader.getDescription() != null) {
          axisDescHasChanged = true;
        }

        if (axisNameHasChanged || axisDescHasChanged) {
          // The name of the axis has changed, We must change the name of the
          // root to
          String treeId = Integer.toString(oldAxisHeader.getRootId());
          TreeNode root = tree.getRoot(con, treeId);
          TreeNode node = new TreeNode(root.getPK().getId(), root.getTreeId(),
                  axisHeader.getName(), axisHeader.getDescription(), root.getCreationDate(), root.
                  getCreatorId(), root.getPath(), root.getLevelNumber(), root.getOrderNumber(),
                  root.getFatherId());
          node.setLanguage(axisHeader.getLanguage());
          node.setRemoveTranslation(axisHeader.isRemoveTranslation());
          node.setTranslationId(axisHeader.getTranslationId());
          node.setTranslations(axisHeader.getTranslations());
          tree.updateRoot(con, node);
        }

        // update axis
        axisHeader.setRootId(oldAxisHeader.getRootId());
        axisHeader.setCreationDate(oldAxisHeader.getCreationDate());
        axisHeader.setCreatorId(oldAxisHeader.getCreatorId());
        if (order == -1) {
          // order has not changed
          axisHeader.setAxisOrder(oldAxisHeader.getAxisOrder());
        }

        // gestion des traductions
        if (axisHeader.isRemoveTranslation()) {
          if (oldAxisHeader.getLanguage() == null) {
            // translation for the first time
            oldAxisHeader.setLanguage(I18NHelper.defaultLanguage);
          }
          if (oldAxisHeader.getLanguage().equalsIgnoreCase(
                  axisHeader.getLanguage())) {
            List<AxisHeaderI18N> translations = axisHeaderI18NDAO.getTranslations(con, Integer.
                    parseInt(axisHeader.getPK().getId()));

            if (translations != null && !translations.isEmpty()) {
              AxisHeaderI18N translation = translations.get(0);

              axisHeader.setLanguage(translation.getLanguage());
              axisHeader.setName(translation.getName());
              axisHeader.setDescription(translation.getDescription());

              AxisHeaderPersistence axisHP = new AxisHeaderPersistence(
                      axisHeader);
              dao.update(con, axisHP);

              axisHeaderI18NDAO.deleteTranslation(con, translation.getId());
            }
          } else {
            axisHeaderI18NDAO.deleteTranslation(con, Integer.parseInt(axisHeader.getTranslationId()));
          }
        } else {
          if (axisHeader.getLanguage() != null) {
            if (oldAxisHeader.getLanguage() == null) {
              // translation for the first time
              oldAxisHeader.setLanguage(I18NHelper.defaultLanguage);
            }
            if (!axisHeader.getLanguage().equalsIgnoreCase(
                    oldAxisHeader.getLanguage())) {
              AxisHeaderI18N newAxis = new AxisHeaderI18N(Integer.parseInt(
                      axisHeader.getPK().getId()), axisHeader.getLanguage(), axisHeader.getName(),
                      axisHeader.getDescription());
              String translationId = axisHeader.getTranslationId();
              if (translationId != null && !translationId.equals("-1")) {
                // update translation
                newAxis.setId(Integer.parseInt(axisHeader.getTranslationId()));

                axisHeaderI18NDAO.updateTranslation(con, newAxis);
              } else {
                axisHeaderI18NDAO.createTranslation(con, newAxis);
              }

              axisHeader.setLanguage(oldAxisHeader.getLanguage());
              axisHeader.setName(oldAxisHeader.getName());
              axisHeader.setDescription(oldAxisHeader.getDescription());
            }
          }

          AxisHeaderPersistence axisHP = new AxisHeaderPersistence(axisHeader);
          dao.update(con, axisHP);
        }

        // remove axisheader from cache
        axisHeaders.remove(axisHeader.getPK().getId());

        commitTransaction(con);
      } catch (Exception exce_update) {
        rollbackTransaction(con);
        throw new PdcException("PdcBmImpl.updateAxis",
                SilverpeasException.ERROR, "Pdc.CANNOT_UPDATE_AXE", exce_update);
      } finally {
        DBUtil.close(con);
      }
    }

    return status;
  }

  /**
   * delete the axe from the data base and all its subtrees.
   * @param axisId - the id of the selected axe
   */
  @Override
  public void deleteAxis(Connection con, String axisId) throws PdcException {
    try {
      AxisHeader axisHeader = getAxisHeader(con, axisId); // get the header of
      // the axe to obtain
      // the rootId.

      PdcRightsDAO.deleteAxisRights(con, axisId);
      
      PdcClassificationService classificationService = PdcServiceFactory.getFactory().
              getPdcClassificationService();
      classificationService.axisDeleted(axisId);

      // delete data in the tree table
      tree.deleteTree(con, Integer.toString(axisHeader.getRootId()));
      // delete data in the pdc utilization table
      pdcUtilizationBm.deleteUsedAxisByAxisId(con, axisId);
      dao.remove(con, new AxisPK(axisId));

      // Unregister axis to classifyEngine
      pdcClassifyBm.unregisterAxis(con, Integer.parseInt(axisId));
      (new PdcSubscriptionUtil()).checkAxisOnDelete(Integer.parseInt(axisId),
              axisHeader.getName());

      // remove axisheader from cache
      axisHeaders.remove(axisId);

      // suppression des traductions
      axisHeaderI18NDAO.deleteTranslations(con, Integer.parseInt(axisId));
    } catch (Exception exce_delete) {
      throw new PdcException("PdcBmImpl.deleteAxis", SilverpeasException.ERROR,
              "Pdc.CANNOT_DELETE_AXE", exce_delete);
    }
  }

  /**
   * Returns a detail axe (header,values).
   * @param axisId - the id of the selected axe.
   * @return the Axis Object
   */
  @Override
  public Axis getAxisDetail(String axisId) throws PdcException {
    return getAxisDetail(axisId, new AxisFilter());
  }

  @Override
  public Axis getAxisDetail(String axisId, AxisFilter filter)
          throws PdcException {
    Axis axis = null;
    AxisHeader axisHeader = getAxisHeader(axisId); // get the header of the axe
    // to obtain the rootId.
    if (axisHeader != null) {
      int treeId = axisHeader.getRootId();
      axis = new Axis(axisHeader, getAxisValues(treeId, filter));
    }
    return axis;
  }

  @Override
  public String getTreeId(String axisId) throws PdcException {
    // get the header of the axis to obtain the rootId.
    AxisHeaderPersistence axisHeader = getAxisHeaderPersistence(axisId);
    int treeId = -1;
    if (axisHeader != null) {
      treeId = axisHeader.getRootId();
    }
    return Integer.toString(treeId);
  }

  /**
   * Returns a value from an axe.
   * @param valueId - the id of the selected value
   * @return the Value object
   */
  @Override
  public com.stratelia.silverpeas.pdc.model.Value getValue(String axisId,
          String valueId) throws PdcException {
    com.stratelia.silverpeas.pdc.model.Value value = null;
    TreeNode node = null;

    Connection con = openConnection();

    try {
      node = tree.getNode(con, new TreeNodePK(valueId), getTreeId(axisId));
      value = createValue(node);
    } catch (Exception exce_select) {
      throw new PdcException("PdcBmImpl.getValue", SilverpeasException.ERROR,
              "Pdc.CANNOT_ACCESS_VALUE", exce_select);
    } finally {
      DBUtil.close(con);
    }

    return value;
  }

  /**
   * Returns a value from an axe.
   * @param valueId - the id of the selected value
   * @return the Value object
   */
  @Override
  public com.stratelia.silverpeas.pdc.model.Value getAxisValue(String valueId,
          String treeId) throws PdcException {
    Connection con = null;
    try {
      con = openConnection();
      return createValue(tree.getNode(con, new TreeNodePK(valueId), treeId));
    } catch (Exception exce_select) {
      throw new PdcException("PdcBmImpl.getAxisValue",
              SilverpeasException.ERROR, "Pdc.CANNOT_ACCESS_VALUE", exce_select);
    } finally {
      DBUtil.close(con);
    }
  }

  /**
   * Return a list of axis values having the value name in parameter
   * @param valueName - the name of the value.
   * @return List
   * @throws PdcException
   * @see
   */
  @Override
  public List<Value> getAxisValuesByName(String valueName) throws PdcException {
    Connection con = openConnection();

    try {
      List<TreeNode> listTreeNodes = tree.getNodesByName(con, valueName);
      return createValuesList(listTreeNodes);
    } catch (Exception exce_select) {
      throw new PdcException("PdcBmImpl.getAxisValuesByName",
              SilverpeasException.ERROR, "Pdc.CANNOT_FIND_VALUES", exce_select);
    } finally {
      DBUtil.close(con);
    }
  }

  /**
   * Return a list of String corresponding to the valueId of the value in parameter
   * @param axisId
   * @param valueId
   * @return List of String
   * @throws PdcException
   * @see getDaughters
   */
  @Override
  public List<String> getDaughterValues(String axisId, String valueId)
          throws PdcException {
    List<String> listValuesString = new ArrayList<String>();
    Connection con = openConnection();

    try {
      List<Value> listValues = getDaughters(con, valueId, axisId);
      for (Value value : listValues) {
        listValuesString.add(value.getPK().getId());
      }
    } catch (Exception exce_select) {
      throw new PdcException("PdcBmImpl.getDaughterValues",
              SilverpeasException.ERROR, "Pdc.CANNOT_RETRIEVE_SUBNODES",
              exce_select);
    } finally {
      DBUtil.close(con);
    }

    return listValuesString;
  }

  /**
   * Return a list of String corresponding to the valueId of the value in parameter
   * @param axisId
   * @param valueId
   * @return List of String
   * @throws PdcException
   * @see getDaughters
   */
  @Override
  public List<Value> getFilteredAxisValues(String rootId, AxisFilter filter)
          throws PdcException {
    List<Value> values = new ArrayList<Value>();
    try {
      values = getAxisValues(Integer.parseInt(rootId), filter);

      // for each filtered value, get all values from root to this finded value
      for (com.stratelia.silverpeas.pdc.model.Value value : values) {
        value.setPathValues(getFullPath(value.getValuePK().getId(), value.getTreeId()));
      }
    } catch (Exception exce_select) {
      throw new PdcException("PdcBmImpl.getFilteredAxisValues",
              SilverpeasException.ERROR, "Pdc.CANNOT_RETRIEVE_SUBNODES",
              exce_select);
    }

    return values;
  }

  /**
   * Return the Value corresponding to the axis done
   * @param axisId
   * @return com.stratelia.silverpeas.pdc.model.Value
   * @throws PdcException
   * @see
   */
  @Override
  public com.stratelia.silverpeas.pdc.model.Value getRoot(String axisId)
          throws PdcException {
    Connection con = openConnection();
    SilverTrace.info("Pdc", "PdcBmImpl.getRoot", "root.MSG_GEN_PARAM_VALUE",
            "axisId = " + axisId);
    try {
      // get the header of the axis to obtain the rootId.
      AxisHeader axisHeader = getAxisHeader(axisId, false);
      int treeId = axisHeader.getRootId();
      TreeNode treeNode = tree.getRoot(con, Integer.toString(treeId));
      return createValue(treeNode);
    } catch (Exception e) {
      throw new PdcException("PdcBmImpl.getRoot", SilverpeasException.ERROR,
              "Pdc.CANNOT_GET_VALUE", e);
    } finally {
      DBUtil.close(con);
    }
  }

  /**
   * @param treeId The id of the selected axis.
   * @return The list of values of the axis.
   */
  @Override
  public List<Value> getAxisValues(int treeId) throws PdcException {
    return getAxisValues(treeId, new AxisFilter());
  }

  private List<Value> getAxisValues(int treeId, AxisFilter filter) throws PdcException {
    Connection con = openConnection();

    try {
      return createValuesList(tree.getTree(con, Integer.toString(treeId), filter));
    } catch (Exception exce_select) {
      throw new PdcException("PdcBmImpl.getAxisValues",
              SilverpeasException.ERROR, "Pdc.CANNOT_ACCESS_LIST_OF_VALUES",
              exce_select);
    } finally {
      DBUtil.close(con);
    }
  }

  /**
   * insert a value which is defined like a mother value
   * @param valueToInsert - a Value object
   * @param refValue - the id of the Value to insert
   * @return 1 if the name already exist 0 otherwise
   */
  @Override
  public int insertMotherValue(
          com.stratelia.silverpeas.pdc.model.Value valueToInsert, String refValue,
          String axisId) throws PdcException {
    int status = 0;
    // get the header of the axis to obtain the treeId.
    AxisHeader axisHeader = getAxisHeader(axisId, false);
    String treeId = Integer.toString(axisHeader.getRootId());

    // get the mother value of the value which have the refValue
    // to find sisters of the valueToInsert
    TreeNode refNode = getAxisValue(refValue, treeId);

    Connection con = null;

    try {
      con = openTransaction();
      // Avant l'insertion de la mere, on recupere les vieux chemins
      ArrayList<String> oldPath = getPathes(con, refValue, treeId);

      if (refNode.getLevelNumber() != 0) {
        status = insertMotherValueToValue(con, valueToInsert, refValue, treeId);
      } else {
        insertMotherValueToRootValue(con, valueToInsert, refValue, axisId,
                treeId);
        status = 0;
      }

      // Warning, we must update the path of the Value(Classify)
      if ((status == 0) && !oldPath.isEmpty()) {
        // the mother value is created

        // Après l'insertion de la mere, on recupere les nouveaux chemins
        ArrayList<String> newPath = getPathes(con, refValue, treeId);
        // call the ClassifyBm to create oldValue and newValue
        // and to replace the oldValue by the newValue
        pdcClassifyBm.createValuesAndReplace(con, axisId, oldPath, newPath);
      }

      commitTransaction(con);
    } catch (Exception e) {
      rollbackTransaction(con);
      throw new PdcException("PdcBmImpl.insertMotherValue",
              SilverpeasException.ERROR, "Pdc.Pdc.CANNOT_UPDATE_POSTION", e);
    } finally {
      DBUtil.close(con);
    }

    return status;
  }

  /**
   * Move a value under a new father
   * @param axis : l'axe concerné
   * @param valueToMove - a Value object
   * @param newFatherId - the id of the new father
   * @return 1 if the name already exist 0 otherwise
   */
  @Override
  public int moveValueToNewFatherId(Axis axis, Value valueToMove,
          String newFatherId, int orderNumber) throws PdcException {
    int status = 0;
    String treeId = Integer.toString(axis.getAxisHeader().getRootId());
    String valueToMoveId = valueToMove.getPK().getId();
    Connection con = null;

    try {
      con = openTransaction();
      // Avant le déplassement de la valeur, on recupere les vieux chemins afin
      // de reclasser les associations après
      ArrayList<String> oldPath = getPathes(con, valueToMoveId, treeId);
      // il ne faut pas que la valeur que l'on insère ai une soeur du même nom
      List<Value> daughters = getDaughters(con, newFatherId, treeId);
      if (isValueNameExist(daughters, valueToMove)) {
        status = 1;
      } else {
        try {
          // l'idée : passer en paramètres : des TreeNodePK car le métier est
          // basé sur les Tree
          tree.moveSubTreeToNewFather(con, new TreeNodePK(valueToMoveId),
                  new TreeNodePK(newFatherId), treeId, orderNumber);
        } catch (Exception exce_insert) {
          throw new PdcException("PdcBmImpl.moveValueToNewFatherId",
                  SilverpeasException.ERROR, "Pdc.CANNOT_MOVE_VALUE", exce_insert);
        }
      }

      // Warning, we must update the path of the Value(Classify)
      if ((status == 0) && !oldPath.isEmpty()) {
        // the mother value is created
        // Après l'insertion de la mere, on recupere les nouveaux chemins
        ArrayList<String> newPath = getPathes(con, valueToMoveId, treeId);
        // call the ClassifyBm to create oldValue and newValue
        // and to replace the oldValue by the newValue
        pdcClassifyBm.createValuesAndReplace(con,
                Integer.toString(axis.getAxisHeader().getRootId()), oldPath, newPath);
      }
      commitTransaction(con);
    } catch (Exception e) {
      rollbackTransaction(con);
      throw new PdcException("PdcBmImpl.moveValueToNewFatherId",
              SilverpeasException.ERROR, "Pdc.CANNOT_MOVE_VALUE", e);
    } finally {
      DBUtil.close(con);
    }
    return status;
  }

  /**
   * retourne les droits hérités sur la valeur
   * @param current value
   * @return ArrayList( ArrayList UsersId, ArrayList GroupsId)
   * @throws PdcException
   */
  @Override
  public List<List<String>> getInheritedManagers(Value value) throws PdcException {
    String axisId = value.getAxisId();
    String path = value.getPath();
    String[] explosedPath = path.split("/");
    List<List<String>> usersAndgroups = new ArrayList<List<String>>();
    List<String> usersInherited = new ArrayList<String>();
    List<String> groupsInherited = new ArrayList<String>();
    for (int i = 1; i < explosedPath.length; i++) {
      List<List<String>> managers = getManagers(axisId, explosedPath[i]);
      List<String> usersId = managers.get(0);
      List<String> groupsId = managers.get(1);
      for (String userId : usersId) {
        // si le userId n'est pas déjà dans la liste
        if (!usersInherited.contains(userId)) {
          usersInherited.add(userId);
        }
      }
      for (String groupId : groupsId) {
        // si le groupid n'est pas déjà dans la liste
        if (!groupsInherited.contains(groupId)) {
          groupsInherited.add(groupId);
        }
      }
    }
    usersAndgroups.add(usersInherited);
    usersAndgroups.add(groupsInherited);

    return usersAndgroups;
  }

  /**
   * retourne les droits sur la valeur
   * @param current value
   * @return List(List userIds, List groupIds)
   * @throws PdcException
   */
  @Override
  public List<List<String>> getManagers(String axisId, String valueId) throws PdcException {
    List<String> usersId;
    List<String> groupsId;
    Connection con = openConnection();
    try {
      usersId = PdcRightsDAO.getUserIds(con, axisId, valueId);
      groupsId = PdcRightsDAO.getGroupIds(con, axisId, valueId);
    } catch (SQLException e) {
      throw new PdcException("PdcBmImpl.getManagers",
              SilverpeasException.ERROR, "Pdc.CANNOT_GET_MANAGERS", e);
    } finally {
      DBUtil.close(con);
    }
    List<List<String>> usersAndgroups = new ArrayList<List<String>>();
    usersAndgroups.add(usersId);
    usersAndgroups.add(groupsId);

    return usersAndgroups;
  }

  @Override
  public boolean isUserManager(String userId) throws PdcException {
    if (!PdcSettings.delegationEnabled) {
      return false;
    }

    Connection con = openConnection();

    try {
      // First, check if user is directly manager of a part of PDC
      boolean isManager = PdcRightsDAO.isUserManager(con, userId);

      if (!isManager) {
        // If not, check if at least one of his groups it is
        OrganizationController controller = new OrganizationController();
        String[] groupIds = controller.getAllGroupIdsOfUser(userId);

        isManager = isGroupManager(groupIds);
      }

      return isManager;
    } catch (Exception e) {
      throw new PdcException("PdcBmImpl.isUserManager",
              SilverpeasException.ERROR, "Pdc.CANNOT_GET_MANAGERS", e);
    } finally {
      DBUtil.close(con);
    }
  }

  private boolean isGroupManager(String[] groupIds) throws PdcException {
    Connection con = openConnection();
    try {
      return PdcRightsDAO.isGroupManager(con, groupIds);
    } catch (SQLException e) {
      throw new PdcException("PdcBmImpl.isGroupManager",
              SilverpeasException.ERROR, "Pdc.CANNOT_GET_MANAGERS", e);
    } finally {
      DBUtil.close(con);
    }
  }

  /**
   * met à jour les droits sur la valeur
   * @param ArrayList ( ArrayList UsersId, ArrayList GroupsId), current value
   * @return
   * @throws PdcException
   */
  @Override
  public void setManagers(List<String> userIds, List<String> groupIds, String axisId,
          String valueId) throws PdcException {
    Connection con = null;

    try {
      con = openTransaction();
      // supprime tous les droits sur la valeur
      PdcRightsDAO.deleteRights(con, axisId, valueId);

      for (String userId : userIds) {
        PdcRightsDAO.insertUserId(con, axisId, valueId, userId);
      }
      for (String groupId : groupIds) {
        PdcRightsDAO.insertGroupId(con, axisId, valueId, groupId);
      }
      commitTransaction(con);
    } catch (SQLException e) {
      rollbackTransaction(con);
      throw new PdcException("PdcBmImpl.setManagers",
              SilverpeasException.ERROR, "Pdc.CANNOT_SET_MANAGER", e);
    } finally {
      DBUtil.close(con);
    }
  }

  @Override
  public void razManagers(String axisId, String valueId) throws PdcException {
    Connection con = openConnection();
    try {
      PdcRightsDAO.deleteRights(con, axisId, valueId);
    } catch (SQLException e) {
      throw new PdcException("PdcBmImpl.razManagers",
              SilverpeasException.ERROR, "Pdc.CANNOT_REMOVE_MANAGER", e);
    } finally {
      DBUtil.close(con);
    }
  }

  /**
   * Supprime les droits associés au userid
   * @param userId
   * @throws SQLException
   */
  @Override
  public void deleteManager(String userId) throws PdcException {
    Connection con = openConnection();
    try {
      PdcRightsDAO.deleteManager(con, userId);
    } catch (SQLException e) {
      throw new PdcException("PdcBmImpl.deleteManager",
              SilverpeasException.ERROR, "Pdc.CANNOT_REMOVE_MANAGER", e);
    } finally {
      DBUtil.close(con);
    }
  }

  /**
   * Supprime les droits associés au groupid
   * @param groupId
   * @throws SQLException
   */
  @Override
  public void deleteGroupManager(String groupId) throws PdcException {
    Connection con = openConnection();
    try {
      PdcRightsDAO.deleteGroupManager(con, groupId);
    } catch (SQLException e) {
      throw new PdcException("PdcBmImpl.deleteGroupManager",
              SilverpeasException.ERROR, "Pdc.CANNOT_REMOVE_MANAGER", e);
    } finally {
      DBUtil.close(con);
    }
  }

  /**
   * Return tree where the root value is the refValue.
   * @param con - the connection to the database
   * @param refValue - the id of the reference Value Object
   * @return a list of each pathes found
   * @throws PdcException
   */
  private ArrayList<String> getPathes(Connection con, String refValue, String treeId)
          throws PdcException {
    ArrayList<String> pathList = new ArrayList<String>();
    TreeNodePK refNodePK = new TreeNodePK(refValue);
    try {
      // get a list of tree node
      // for one tree node, get its path
      List<TreeNode> treeList = tree.getSubTree(con, refNodePK, treeId);
      for (TreeNode nodeTree : treeList) {
        pathList.add(nodeTree.getPath() + nodeTree.getPK().getId() + "/");
      }
    } catch (Exception e) {
      SilverTrace.info("PDC", "PdcBmImpl.getPathes",
              "Pdc.CANNOT_RETRIEVE_PATH", e);
    }

    return pathList;
  }

  /**
   * Method declaration
   * @param valueToInsert
   * @param refValue
   * @param axisId
   * @throws PdcException
   * @see
   */
  private void insertMotherValueToRootValue(Connection con,
          com.stratelia.silverpeas.pdc.model.Value valueToInsert, String refValue,
          String axisId, String treeId) throws PdcException {
    try {
      // Insertion de la nouvelle racine
      tree.insertFatherToNode(con, valueToInsert, new TreeNodePK(
              refValue), treeId);
    } catch (Exception exce_insert) {
      throw new PdcException("PdcBmImpl.insertMotherValue",
              SilverpeasException.ERROR, "Pdc.CANNOT_INSERT_VALUE", exce_insert);
    }
  }

  /**
   * Method declaration
   * @param valueToInsert
   * @param refValue
   * @return
   * @throws PdcException
   * @see
   */
  private int insertMotherValueToValue(Connection con,
          com.stratelia.silverpeas.pdc.model.Value valueToInsert, String refValue,
          String treeId) throws PdcException {
    int status = 0;
    // get the mother value of the value which have the refValue
    // to find sisters of the valueToInsert
    TreeNode refNode = getAxisValue(refValue, treeId);
    List<Value> daughters = getDaughters(con, refNode.getFatherId(), treeId);

    if (isValueNameExist(daughters, valueToInsert)) {
      status = 1;
    } else {
      try {
        tree.insertFatherToNode(con, valueToInsert, new TreeNodePK(
                refValue), treeId);
      } catch (Exception exce_insert) {
        throw new PdcException("PdcBmImpl.insertMotherValue",
                SilverpeasException.ERROR, "Pdc.CANNOT_INSERT_VALUE", exce_insert);
      }
    }
    return status;
  }

  /**
   * insert a value which is defined like a daughter value
   * @param valueToInsert - a Value object
   * @param refValue - the id of the Value to insert
   * @return 1 if the name already exist 0 otherwise
   */
  @Override
  public int createDaughterValue(
          com.stratelia.silverpeas.pdc.model.Value valueToInsert, String refValue,
          String treeId) throws PdcException {
    // get the Connection object
    Connection con = openConnection();

    int status = 0;
    List<Value> daughters = getDaughters(con, refValue, treeId);

    if (isValueNameExist(daughters, valueToInsert)) {
      status = 1;
      DBUtil.close(con);
    } else {
      try {
        tree.createSonToNode(con, valueToInsert, new TreeNodePK(
                refValue), treeId);
      } catch (Exception exce_create) {
        throw new PdcException("PdcBmImpl.createDaughterValue",
                SilverpeasException.ERROR, "Pdc.CANNOT_CREATE_VALUE", exce_create);
      } finally {
        DBUtil.close(con);
      }

    }

    return status;
  }

  /**
   * insert a value which is defined like a daughter value
   * @param valueToInsert - a Value object
   * @param refValue - the id of the Value to insert
   * @return -1 if the name already exists id otherwise
   */
  @Override
  public String createDaughterValueWithId(
          com.stratelia.silverpeas.pdc.model.Value valueToInsert, String refValue,
          String treeId) throws PdcException {
    // get the Connection object
    Connection con = openConnection();

    String daughterId = null;
    List<Value> daughters = getDaughters(con, refValue, treeId);

    if (isValueNameExist(daughters, valueToInsert)) {
      daughterId = "-1";
      DBUtil.close(con);
    } else {
      try {
        daughterId = tree.createSonToNode(con, valueToInsert, new TreeNodePK(
                refValue), treeId);
      } catch (Exception exce_create) {
        throw new PdcException("PdcBmImpl.createDaughterValueWithId",
                SilverpeasException.ERROR, "Pdc.CANNOT_CREATE_VALUE", exce_create);
      } finally {
        DBUtil.close(con);
      }
    }
    return daughterId;
  }

  /**
   * Update the selected value
   * @param value - a Value object
   * @return 1 if the name already exist 0 otherwise
   */
  @Override
  public int updateValue(com.stratelia.silverpeas.pdc.model.Value value,
          String treeId) throws PdcException {
    // get the Connection object
    Connection con = openConnection();

    int status = 0;

    try {
      com.stratelia.silverpeas.pdc.model.Value oldValue =
              getAxisValue(value.getPK().getId(), treeId);
      List<Value> daughters = getDaughters(con, oldValue.getMotherId(), treeId);

      if (isValueNameExist(daughters, value)) {
        status = 1;
      } else {
        TreeNode node = new TreeNode(value.getPK().getId(), treeId, value.getName(), value.
                getDescription(), oldValue.getCreationDate(),
                oldValue.getCreatorId(), oldValue.getPath(), oldValue.getLevelNumber(), value.
                getOrderNumber(), oldValue.getFatherId());
        node.setLanguage(value.getLanguage());
        node.setRemoveTranslation(value.isRemoveTranslation());
        node.setTranslationId(value.getTranslationId());
        tree.updateNode(con, node);
      }
    } catch (Exception exce_update) {
      throw new PdcException("PdcBmImpl.updateValue", SilverpeasException.ERROR,
              "Pdc.CANNOT_UPDATE_VALUE", exce_update);
    } finally {
      DBUtil.close(con);
    }

    return status;
  }

  /**
   * Delete a value and it's sub tree
   * @param valueId - the id of the select value
   */
  @Override
  public void deleteValueAndSubtree(Connection con, String valueId,
          String axisId, String treeId) throws PdcException {
    SilverTrace.info("Pdc", "PdcBmImpl.deleteValueAndSubtree",
            "root.MSG_GEN_PARAM_VALUE", "axisId = " + axisId);

    try {
      // first update any predefined classifications
      PdcClassificationService classificationService = PdcServiceFactory.getFactory().
              getPdcClassificationService();
      List<PdcAxisValue> valuesToDelete = new ArrayList<PdcAxisValue>();
      PdcAxisValue aValueToDelete = PdcAxisValue.aPdcAxisValue(valueId, axisId);
      valuesToDelete.add(aValueToDelete);
      valuesToDelete.addAll(findRecursivelyAllChildrenOf(aValueToDelete));
      classificationService.axisValuesDeleted(valuesToDelete);

      List<Value> pathInfo = getFullPath(valueId, treeId);

      // Mise à jour de la partie utilisation
      updateBaseValuesInInstances(con, valueId, axisId, treeId);

      // Avant l'effacement de la valeur, on recupere les vieux chemins
      ArrayList<String> oldPath = getPathes(con, valueId, treeId);

      SilverTrace.info("Pdc", "PdcBmImpl.deleteValueAndSubtree",
              "root.MSG_GEN_PARAM_VALUE", "oldPath.size() = " + oldPath.size()
              + ", oldPath =" + oldPath.toString());

      TreeNodePK treeNodePK = new TreeNodePK(valueId);

      // On recupere le chemin de la mère
      String motherId = tree.getNode(con, treeNodePK, treeId).getFatherId();
      TreeNodePK motherPK = new TreeNodePK(motherId);
      TreeNode mother = tree.getNode(con, motherPK, treeId);
      String motherPath = mother.getPath() + motherId + "/";

      SilverTrace.info("Pdc", "PdcBmImpl.deleteValueAndSubtree",
              "root.MSG_GEN_PARAM_VALUE", "motherId = " + motherId);

      AxisHeader axisHeader = getAxisHeader(con, axisId);
      String axisName = axisHeader.getName();

      List<TreeNode> subtree = tree.getSubTree(con, treeNodePK, treeId);
      tree.deleteSubTree(con, treeNodePK, treeId);

      Value value = new Value();
      // on efface les droits sur les valeurs
      for (TreeNode node : subtree) {
        value = createValue(node);
        PdcRightsDAO.deleteRights(con, axisId, value.getPK().getId());
      }
      // Warning, we must update the path of the Value(Classify)
      if (!oldPath.isEmpty()) {
        // Après l'effacement de la valeur et de son arborescence, on recupere
        // les nouveaux chemins
        // ArrayList newPath = getPathes(con,valueId);
        // Les nouveaux chemins sont tous identiques, c'est celui de la mère
        ArrayList<String> newPath = new ArrayList<String>();
        for (String anOldPath : oldPath) {
          newPath.add(motherPath);
        }
        SilverTrace.info("Pdc", "PdcBmImpl.deleteValueAndSubtree",
                "root.MSG_GEN_PARAM_VALUE", "newPath.size() = " + newPath.size());

        (new PdcSubscriptionUtil()).checkValueOnDelete(
                Integer.parseInt(axisId), axisName, oldPath, newPath, pathInfo);

        // call the ClassifyBm to create oldValue and newValue
        // and to replace the oldValue by the newValue
        pdcClassifyBm.createValuesAndReplace(con, axisId, oldPath, newPath);
      }
    } catch (Exception exce_delete) {
      throw new PdcException("PdcBmImpl.deleteValueAndSubtree",
              SilverpeasException.ERROR, "Pdc.CANNOT_DELETE_VALUE_SUBTREE",
              exce_delete);
    }
  }

  /**
   * Delete the selected value. If a daughter of the selected value is named like a sister of her
   * mother the delete is not possible.
   * @param valueId - the id of the select value
   * @return null if the delete is possible, the name of her daughter else.
   */
  @Override
  public String deleteValue(Connection con, String valueId, String axisId,
          String treeId) throws PdcException {
    String possibleDaughterName = null;
    try {
      // first update any predefined classifications
      List<PdcAxisValue> valuesToDelete = new ArrayList<PdcAxisValue>();
      valuesToDelete.add(PdcAxisValue.aPdcAxisValue(valueId, axisId));
      PdcClassificationService classificationService = PdcServiceFactory.getFactory().
              getPdcClassificationService();
      classificationService.axisValuesDeleted(valuesToDelete);

      // then run the old legacy code about content classification on the PdC
      Value valueToDelete = getAxisValue(valueId, treeId);
      // filles de la mère = soeurs des filles de la valeur à supprimer
      List<Value> daughtersOfMother = getDaughters(con, valueToDelete.getMotherId(),
              treeId);
      // filles de la valeur à supprimer
      List<Value> daughtersOfValueToDelete = getDaughters(con, valueId, treeId);

      possibleDaughterName = isValueNameExist(daughtersOfMother, daughtersOfValueToDelete);
      if (possibleDaughterName == null) {

        // Mise à jour de la partie utilisation
        updateBaseValueInInstances(con, valueId, axisId, treeId);

        // Avant l'effacement de la valeur, on recupere les vieux chemins
        ArrayList<String> oldPath = getPathes(con, valueId, treeId);

        AxisHeader axisHeader = getAxisHeader(con, axisId);
        String axisName = axisHeader.getName();
        List<Value> pathInfo = getFullPath(valueId, treeId);

        tree.deleteNode(con, new TreeNodePK(valueId), treeId);

        // on efface les droits sur la valeur
        PdcRightsDAO.deleteRights(con, axisId, valueId);

        // Warning, we must update the path of the Value(Classify)
        if (!oldPath.isEmpty()) {
          // Après l'effacement de la valeur, on creait les nouveaux chemins

          ArrayList<String> newPath = getPathes(con, valueId, treeId);
          // lecture de l'arrayList oldPath et on retire la valueId
          String pattern = "/" + valueId; // motif que l'on doit rechercher dans
          // l'ancien chemin pour le supprimer
          int lenOfPattern = pattern.length(); // longueur du motif
          int pattern_idx; // position du pattern rechercher
          for (String path : oldPath) {
            pattern_idx = path.indexOf(pattern); // ne peux etre à -1
            path = path.substring(0, pattern_idx)
                    + path.substring(pattern_idx + lenOfPattern); // retire le motif
            if (path.split("/").length <= 2) { // the split of /NODE_ID/ results to { "", NODE_IDE }
              path = null;
            }
            newPath.add(path);
          }

          // call the ClassifyBm to create oldValue and newValue
          // and to replace the oldValue by the newValue
          (new PdcSubscriptionUtil()).checkValueOnDelete(Integer.parseInt(axisId), axisName, oldPath,
                  newPath, pathInfo);
          pdcClassifyBm.createValuesAndReplace(con, axisId, oldPath, newPath);
        }
      }
    } catch (Exception exce_delete) {
      throw new PdcException("PdcBmImpl.deleteValue",
              SilverpeasException.ERROR, "Pdc.CANNOT_DELETE_VALUE", exce_delete);
    }
    return possibleDaughterName;
  }

  /**
   * Returns the full path of the value
   * @param valueId - the id of the selected value (value is not empty)
   * @return the complet path - It's a List of ArrayList. Each ArrayList contains the name, the id
   * and the treeId of the value in the path.
   */
  @Override
  public List<Value> getFullPath(String valueId, String treeId) throws PdcException {
    Connection con = openConnection();
    try {
      // récupère une collection de Value
      List<TreeNode> listTreeNode = tree.getFullPath(con, new TreeNodePK(valueId), treeId);
      return createValuesList(listTreeNode);
    } catch (Exception exce_delete) {
      throw new PdcException("PdcBmImpl.deleteValue",
              SilverpeasException.ERROR, "Pdc.CANNOT_DELETE_VALUE", exce_delete);
    } finally {
      DBUtil.close(con);
    }
  }

  /**
   * This method searches if a name of axes is alreadey used !
   * @param axis - a list of axes
   * @param name - the name of the axe
   * @return true if the name of the axe exists, false otherwise
   */
  private boolean isAxisNameExist(List<AxisHeader> axis, AxisHeader axisToCheck) {
    String axisIdToCheck = axisToCheck.getPK().getId();
    String axisNameToCheck = axisToCheck.getName();

    boolean isExist = false; // by default, the name don't exist
    Iterator<AxisHeader> it = axis.iterator();
    AxisHeader axisHeader = null;

    while (it.hasNext()) {
      axisHeader = it.next();
      if (axisHeader.getName().equalsIgnoreCase(axisNameToCheck)) {
        if (!axisHeader.getPK().getId().equals(axisIdToCheck)) {
          isExist = true;
          break;
        }
      }
    }

    return isExist;
  }

  /**
   * This method searches if a name of values is alreadey used !
   * @param values - a list of values
   * @param name - the name of the value
   * @return true if the name of the value exists, false otherwise
   */
  private boolean isValueNameExist(List<Value> values, Value valueToCheck) {
    String valueIdToCheck = valueToCheck.getPK().getId();
    String valueNameToCheck = valueToCheck.getName();
    boolean isExist = false; // by default, the name don't exist
    Iterator<Value> it = values.iterator();
    Value value = null;

    while (it.hasNext()) {
      value = it.next();
      if (value.getName().equalsIgnoreCase(valueNameToCheck)) {
        if (!value.getPK().getId().equals(valueIdToCheck)) {
          isExist = true;
          break;
        }
      }
    }

    return isExist;
  }

  /**
   * This method searches if one name of valuesToCheck is alreadey used !
   * @param values - a list of values
   * @param valuesToCheck - a list of values to check
   * @return the name of the value if the name of one value exists, null otherwise
   */
  private String isValueNameExist(List<Value> values, List<Value> valuesToCheck) {
    Iterator<Value> it = valuesToCheck.iterator();
    Value valueToCheck = null;
    String valueName = null;

    while (it.hasNext()) {
      valueToCheck = it.next();
      if (isValueNameExist(values, valueToCheck)) {
        valueName = valueToCheck.getName();
        break;
      }
    }

    return valueName;
  }

  @Override
  public AxisHeader getAxisHeader(String axisId) {
    return getAxisHeader(axisId, true);
  }

  public AxisHeader getAxisHeader(String axisId, boolean setTranslations) {
    AxisHeader axisHeader = axisHeaders.get(axisId);
    if (axisHeader == null) {
      try {
        AxisHeaderPersistence axisHeaderPersistence =
                dao.findByPrimaryKey(new AxisPK(
                axisId));
        axisHeader = new AxisHeader(axisHeaderPersistence);

        axisHeaders.put(axisHeader.getPK().getId(), axisHeader);
      } catch (PersistenceException err_select) {
        SilverTrace.info("PDC", "PdcBmImpl.getAxisHeader",
                "Pdc.CANNOT_RETRIEVE_HEADER_AXE", err_select);
      }
    }
    if (setTranslations) {
      setTranslations(axisHeader);
    }
    return axisHeader;
  }

  private AxisHeaderPersistence getAxisHeaderPersistence(String axisId) {
    try {
      return dao.findByPrimaryKey(new AxisPK(axisId));
    } catch (PersistenceException err_select) {
      SilverTrace.error("PDC", "PdcBmImpl.getAxisHeaderPersistence",
              "Pdc.CANNOT_RETRIEVE_HEADER_AXE", "axisId = " + axisId, err_select);
    }
    return null;
  }

  /**
   * Returns an AxisHeader Object. (pass the connection WORK AROUND FOR THE connection BUG !!!!!!!)
   * @param axisId - the id of the selected axe
   * @return an AxisHeader
   */
  private AxisHeader getAxisHeader(Connection connection, String axisId) {
    AxisHeader axisHeader = null;

    try {
      AxisHeaderPersistence axisHeaderPersistence = dao.findByPrimaryKey(
              connection, new AxisPK(axisId));
      axisHeader = new AxisHeader(axisHeaderPersistence);
    } catch (PersistenceException err_select) {
      SilverTrace.info("PDC", "PdcBmImpl.getAxisHeader",
              "Pdc.CANNOT_RETRIEVE_HEADER_AXE", err_select);
    }

    return axisHeader;
  }

  /**
   * Returns a list of Value Object.
   * @param con - a connection
   * @param refValue - the id of the selected axe
   * @return a list
   */
  private List<Value> getDaughters(Connection con, String refValue, String treeId) {
    List<Value> daughters = new ArrayList<Value>();

    try {
      daughters = createValuesList(tree.getSonsToNode(con,
              new TreeNodePK(refValue), treeId));
    } catch (Exception err_list) {
      SilverTrace.info("PDC", "PdcBmImpl.getDaughters",
              "Pdc.CANNOT_RETRIEVE_SUBNODES", err_list);
    }

    return daughters;
  }

  @Override
  public List<Value> getDaughters(String axisId, String valueId) {
    List<Value> daughters = new ArrayList<Value>();
    Connection con = null;
    try {
      con = openConnection();
      AxisHeader axisHeader = getAxisHeader(axisId, false);
      int tId = axisHeader.getRootId();
      return getAxisValues(tId);
    } catch (Exception err_list) {
      SilverTrace.info("PDC", "PdcBmImpl.getDaughters",
              "Pdc.CANNOT_RETRIEVE_SUBNODES", err_list);
    } finally {
      DBUtil.close(con);
    }
    return daughters;
  }

  @Override
  public List<Value> getSubAxisValues(String axisId, String valueId) {
    List<Value> daughters = new ArrayList<Value>();
    Connection con = null;
    try {
      con = openConnection();
      AxisHeader axisHeader = getAxisHeader(axisId, false);
      int tId = axisHeader.getRootId();

      daughters = createValuesList(tree.getSubTree(con,
              new TreeNodePK(valueId), Integer.toString(tId)));
    } catch (Exception err_list) {
      SilverTrace.info("PDC", "PdcBmImpl.getSubAxis",
              "Pdc.CANNOT_RETRIEVE_SUBNODES", err_list);
    } finally {
      DBUtil.close(con);
    }
    return daughters;
  }

  /**
   * Creates a list of Value objects with a list of treeNodes objects
   * @param treeNodes - a list of TreeNode objects
   * @return a Value list
   */
  private List<Value> createValuesList(List<TreeNode> treeNodes) {
    List<Value> values = new ArrayList<Value>();

    for (TreeNode node : treeNodes) {
      values.add(createValue(node));
    }

    return values;
  }

  /**
   * Creates a Value object with a TreeNode object
   * @param treeNode - a TreeNode Object
   * @return a Value Object
   */
  private Value createValue(TreeNode treeNode) {
    if (treeNode != null) {
      Value value = new Value(treeNode.getPK().getId(), treeNode.getTreeId(), treeNode.getName(),
              treeNode.getDescription(), treeNode.getCreationDate(), treeNode.getCreatorId(),
              treeNode.getPath(), treeNode.getLevelNumber(),
              treeNode.getOrderNumber(), treeNode.getFatherId());
      value.setTranslations(treeNode.getTranslations());
      return value;
    }
    return null;
  }

  /**
   * **************************************************
   */
  /**
   * ******** PDC Utilization Settings Methods ********
   */
  /**
   * **************************************************
   */
  @Override
  public UsedAxis getUsedAxis(String usedAxisId) throws PdcException {
    return pdcUtilizationBm.getUsedAxis(usedAxisId);
  }

  /**
   * Method declaration
   * @param instanceId
   * @return
   * @throws PdcException
   * @see
   */
  @Override
  public List<UsedAxis> getUsedAxisByInstanceId(String instanceId) throws PdcException {
    return pdcUtilizationBm.getUsedAxisByInstanceId(instanceId);
  }

  /**
   * Method declaration
   * @param usedAxis
   * @return
   * @throws PdcException
   * @see
   */
  @Override
  public int addUsedAxis(UsedAxis usedAxis) throws PdcException {
    AxisHeader axisHeader = getAxisHeader(Integer.toString(usedAxis.getAxisId()), false); // get the header of the axe to
    // obtain the treeId.
    String treeId = Integer.toString(axisHeader.getRootId());
    return pdcUtilizationBm.addUsedAxis(usedAxis, treeId);
  }

  /**
   * Method declaration
   * @param usedAxis
   * @return
   * @throws PdcException
   * @see
   */
  @Override
  public int updateUsedAxis(UsedAxis usedAxis) throws PdcException {
    AxisHeader axisHeader = getAxisHeader(Integer.toString(usedAxis.getAxisId()), false); // get the header of the axe to
    // obtain the treeId.
    String treeId = Integer.toString(axisHeader.getRootId());

    // on recherche si la nouvelle valeur de base est une valeur ascendante à
    // la
    // valeur de base originelle
    // si c'est le cas alors on peut faire un update.
    // sinon, il faut vérifier qu'aucune valeur fille de cet axe n'est
    // positionnée.
    // si une valeur fille est positionnée, on ne peut pas modifier la valeur
    // de
    // base du UsedAxis

    // on récupère la valeur de base que l'on veut modifier de l'objet
    // UsedAxis
    String id = usedAxis.getPK().getId();
    UsedAxis currentUsedAxis = pdcUtilizationBm.getUsedAxis(id);

    // on récupère la liste des objets pour une instance de jobPeas donnée
    List<Integer> objectIdList = pdcClassifyBm.getObjectsByInstance(usedAxis.getInstanceId());

    // on vérifie d'abord que la nouvelle valeur de base est une valeur
    // ascendante
    // de la valeur de base que l'on souhaite modifié
    if (objectIdList.size() > 0
            && !isAscendanteBaseValue(objectIdList, usedAxis)) {
      // la nouvelle valeur de base est soit une valeur d'un autre axe
      // soit une valeur fille de la valeur de base que l'on veut modifier
      // on vérifie que l'axe courant n'a pas de documents positionnés
      if (pdcClassifyBm.hasAlreadyPositions(objectIdList, currentUsedAxis)) {
        return 2;
      } else {
        return pdcUtilizationBm.updateUsedAxis(usedAxis, treeId);
      }
    } else {
      // la nouvelle valeur de base est ascendante. On peut donc modifier
      return pdcUtilizationBm.updateUsedAxis(usedAxis, treeId);
    }
  }

  /**
   * recherche si la valeur de base de l'axe est une valeur ascendante par rapport aux valeurs se
   * trouvant dans SB_Classify...
   * @param objectIdList - une list d'objets se trouvant dans une instance donnée
   * @param usedAxis - l'objet UsedAxis contenant la nouvelle valeur de base
   * @return vrai si la valeur de base est une valeur ascendante sinon faux
   */
  private boolean isAscendanteBaseValue(List<Integer> objectIdList, UsedAxis usedAxis)
          throws PdcException {
    return pdcClassifyBm.hasAlreadyPositions(objectIdList, usedAxis);
  }

  /**
   * Update a base value from the PdcUtilization table
   * @param valueId - the base value that must be updated
   */
  private void updateBaseValueInInstances(Connection con,
          String baseValueToUpdate, String axisId, String treeId)
          throws PdcException {

    // recherche la valeur mère de baseValueToUpdate
    com.stratelia.silverpeas.pdc.model.Value value = getAxisValue(
            baseValueToUpdate, treeId);
    int newBaseValue = Integer.parseInt(value.getMotherId());

    SilverTrace.info("Pdc", "PdcBmImpl.updateBaseValueInInstances",
            "root.MSG_GEN_PARAM_VALUE", "newBaseValue = " + newBaseValue);

    pdcUtilizationBm.updateOrDeleteBaseValue(con, Integer.parseInt(baseValueToUpdate),
            newBaseValue, Integer.parseInt(axisId), treeId);
  }

  /**
   * Update some base values from the PdcUtilization table
   * @param baseValuesToUpdate - the base values that must be updated
   */
  private void updateBaseValuesInInstances(Connection con,
          String baseValueToUpdate, String axisId, String treeId)
          throws PdcException {

    List<TreeNode> descendants = null;

    try {
      descendants = tree.getSubTree(con, new TreeNodePK(baseValueToUpdate),
              treeId);
    } catch (Exception e) {
      throw new PdcException("PdcBmImpl.updateBaseValuesInInstances",
              SilverpeasException.ERROR, "Pdc.CANNOT_DELETE_VALUE", e);
    }

    // recherche la valeur mère de baseValueToUpdate
    com.stratelia.silverpeas.pdc.model.Value value = getAxisValue(
            baseValueToUpdate, treeId);
    int newBaseValue = Integer.parseInt(value.getMotherId());

    SilverTrace.info("Pdc", "PdcBmImpl.updateBaseValuesInInstances",
            "root.MSG_GEN_PARAM_VALUE", "newBaseValue = " + newBaseValue);

    String descendantId = null;
    for (TreeNode descendant : descendants) {
      descendantId = descendant.getPK().getId();

      SilverTrace.info("Pdc", "PdcBmImpl.updateBaseValuesInInstances",
              "root.MSG_GEN_PARAM_VALUE", "descendantId = " + descendantId);

      pdcUtilizationBm.updateOrDeleteBaseValue(con, Integer.parseInt(descendantId), newBaseValue,
              Integer.parseInt(axisId), treeId);
    }
  }

  /**
   * Method declaration
   * @param usedAxisId
   * @throws PdcException
   * @see
   */
  @Override
  public void deleteUsedAxis(String usedAxisId) throws PdcException {
    pdcUtilizationBm.deleteUsedAxis(usedAxisId);
  }

  /**
   * Method declaration
   * @param usedAxisIds
   * @throws PdcException
   * @see
   */
  @Override
  public void deleteUsedAxis(Collection<String> usedAxisIds) throws PdcException {
    pdcUtilizationBm.deleteUsedAxis(usedAxisIds);
  }

  /**
   * *********************************************
   */
  /**
   * ******** PDC CLASSIFY METHODS ***************
   */
  /**
   * *********************************************
   */
  @Override
  public List<UsedAxis> getUsedAxisToClassify(String instanceId, int silverObjectId)
          throws PdcException {
    List<UsedAxis> usedAxis = getUsedAxisByInstanceId(instanceId);
    if (usedAxis.isEmpty()) {
      List<AxisHeader> headers = getAxis();
      for (AxisHeader axisHeader : headers) {
        UsedAxis axis = new UsedAxis(axisHeader.getPK().getId(), instanceId, axisHeader.getRootId(),
                0,
                0, 1);
        axis._setAxisHeader(axisHeader);
        axis._setAxisName(axisHeader.getName());
        axis._setAxisType(axisHeader.getAxisType());
        axis._setBaseValueName(axisHeader.getName());
        axis._setAxisRootId(axisHeader.getRootId());
        axis._setAxisValues(getAxisValues(axisHeader.getRootId()));
        usedAxis.add(axis);
      }
    } else {
      for (UsedAxis axis : usedAxis) {

        if (I18NHelper.isI18N) {
          AxisHeader header = getAxisHeader(Integer.toString(axis.getAxisId()));
          axis._setAxisHeader(header);
        }

        int axisRootId = axis._getAxisRootId();
        axis._setAxisValues(getAxisValues(axisRootId));
        if (axis.getVariant() == 0 && silverObjectId >= 0) {
          // Si l'axe est invariant, il faut préciser la valeur obligatoire
          List<ClassifyPosition> positions = getPositions(silverObjectId, instanceId);
          String invariantValue = null;
          if (!positions.isEmpty()) {
            for (ClassifyPosition position : positions) {
              invariantValue = position.getValueOnAxis(axis.getAxisId());
              axis._setInvariantValue(invariantValue);
            }
          }
        }
      }
    }
    return usedAxis;
  }

  /*
   * (non-Javadoc)
   * @see com.stratelia.silverpeas.pdc.control.PdcBm#copyPositions(int, java.lang.String, int,
   * java.lang.String)
   */
  @Override
  public void copyPositions(int fromObjectId, String fromInstanceId,
          int toObjectId, String toInstanceId) throws PdcException {
    List<ClassifyPosition> positions = getPositions(fromObjectId, fromInstanceId);

    List<UsedAxis> usedAxis = getUsedAxisByInstanceId(toInstanceId);

    ClassifyPosition newPosition = null;
    for (ClassifyPosition position : positions) {
      newPosition = checkClassifyPosition(position, usedAxis);

      if (newPosition != null) {
        // copy position
        addPosition(toObjectId, newPosition, toInstanceId);
      }
    }
  }

  private ClassifyPosition checkClassifyPosition(ClassifyPosition position,
          List<UsedAxis> usedAxis) {
    ClassifyPosition newPosition = new ClassifyPosition();

    List<ClassifyValue> values = position.getListClassifyValue();
    for (ClassifyValue value : values) {
      value = checkClassifyValue(value, usedAxis);
      if (value != null) {
        newPosition.addValue(value);
      }
    }

    if (newPosition.getValues() == null) {
      return null;
    }

    return newPosition;
  }

  private ClassifyValue checkClassifyValue(ClassifyValue value, List<UsedAxis> usedAxis) {
    UsedAxis uAxis = getUsedAxis(usedAxis, value.getAxisId());
    if (uAxis == null) {
      // This axis is not used by the instance
      return null;
    } else {
      // Check base value
      String baseValuePath = uAxis._getBaseValuePath();
      if (!("/" + value.getValue() + "/").contains(baseValuePath)) {
        return null;
      }
    }
    return value;
  }

  /**
   * From the usedAxis, retrieve the UsedAxis corresponding to axisId
   * @param usedAxis a List of UsedAxis
   * @param axisId the axis id to search
   * @return the UsedAxis found or null if no object found
   */
  private UsedAxis getUsedAxis(List<UsedAxis> usedAxis, int axisId) {
    Iterator<UsedAxis> iterator = usedAxis.iterator();
    UsedAxis uAxis = null;
    while (iterator.hasNext()) {
      uAxis = iterator.next();
      if (uAxis.getAxisId() == axisId) {
        return uAxis;
      }
    }
    return null;
  }

  @Override
  public int addPosition(int silverObjectId, ClassifyPosition position,
          String sComponentId) throws PdcException {
    return addPosition(silverObjectId, position, sComponentId, true);
  }

  @Override
  public int addPosition(int silverObjectId, ClassifyPosition position,
          String sComponentId, boolean alertSubscribers) throws PdcException {
    // First check if the object is already classified on the position
    int positionId = pdcClassifyBm.isPositionAlreadyExists(silverObjectId, position);

    if (positionId == -1) {
      // The position doesn't exists. We add it.
      positionId = pdcClassifyBm.addPosition(silverObjectId, position, sComponentId);

      if (alertSubscribers) {
        // Alert subscribers to the position
        try {
          (new PdcSubscriptionUtil()).checkSubscriptions(position.getValues(), sComponentId,
                  silverObjectId);
        } catch (RemoteException e) {
          throw new PdcException("PdcBmImpl.addPosition", PdcException.ERROR,
                  "pdcPeas.EX_CHECK_SUBSCRIPTION", e);
        }
      }
    }

    return positionId;
  }

  @Override
  public int updatePosition(ClassifyPosition position, String instanceId,
          int silverObjectId) throws PdcException {
    return updatePosition(position, instanceId, silverObjectId, true);
  }

  @Override
  public int updatePosition(ClassifyPosition position, String instanceId,
          int silverObjectId, boolean alertSubscribers) throws PdcException {

    List<UsedAxis> usedAxisList = getUsedAxisToClassify(instanceId, silverObjectId);
    List<Integer> invariantUsedAxis = new ArrayList<Integer>();
    for (UsedAxis ua : usedAxisList) {
      // on cherche les axes invariants
      if (ua.getVariant() == 0) {
        invariantUsedAxis.add(ua.getAxisId());
      }
    }

    // maintenant, on cherche les valeurs qui sont sur un axe invariant
    List<ClassifyValue> classifyValueList = position.getValues();
    List classifyValues = new ArrayList();
    for (ClassifyValue cv : classifyValueList) {
      if (invariantUsedAxis.contains(new Integer(cv.getAxisId()))) {
        classifyValues.add(cv);
      }
    }

    pdcClassifyBm.updatePosition(position);

    // on update les axes invariants
    if (classifyValues.size() > 0) {
      pdcClassifyBm.updatePositions(classifyValues, silverObjectId);
    }

    if (alertSubscribers) {
      try {
        (new PdcSubscriptionUtil()).checkSubscriptions(position.getValues(),
                instanceId, silverObjectId);
      } catch (RemoteException e) {
        throw new PdcException("PdcBmImpl.updatePosition", PdcException.ERROR,
                "pdcPeas.EX_CHECH_SUBSCRIPTION", e);
      }
    }

    return 0;
  }

  @Override
  public void deletePosition(int positionId, String sComponentId)
          throws PdcException {
    pdcClassifyBm.deletePosition(positionId, sComponentId);
  }

  @Override
  public List<ClassifyPosition> getPositions(int silverObjectId, String sComponentId)
          throws PdcException {
    List<Position> positions = pdcClassifyBm.getPositions(silverObjectId, sComponentId);
    ArrayList<ClassifyPosition> classifyPositions = new ArrayList<ClassifyPosition>();

    // transform Position to ClassifyPosition
    ClassifyPosition classifyPosition = null;
    for (Position position : positions) {
      List values = position.getValues();

      // transform Value to ClassifyValue
      ClassifyValue classifyValue = null;
      com.stratelia.silverpeas.classifyEngine.Value value = null;
      ArrayList<ClassifyValue> classifyValues = new ArrayList<ClassifyValue>();
      String valuePath = "";
      String valueId = "";
      for (Object value1 : values) {
        value = (com.stratelia.silverpeas.classifyEngine.Value) value1;
        classifyValue = new ClassifyValue(value.getAxisId(), value.getValue());

        if (value.getAxisId() != -1) {
          int treeId = Integer.parseInt(getTreeId(Integer.toString(value.getAxisId())));
          /*
           * AxisHeader axisHeader = getAxisHeader(Integer.toString(value.getAxisId()), false); //
           * get the header of the axe to obtain the rootId. int treeId = axisHeader.getRootId();
           */

          // enrichit le classifyValue avec le chemin complet de la racine
          // jusqu'à la valeur
          valuePath = value.getValue();
          if (valuePath != null) {
            // enleve le dernier /
            valuePath = valuePath.substring(0, valuePath.length() - 1);
            valueId = valuePath.substring(valuePath.lastIndexOf("/") + 1, valuePath.length());
            classifyValue.setFullPath(getFullPath(valueId, String.valueOf(treeId)));
            classifyValues.add(classifyValue);
          }
        }
      }

      classifyPosition = new ClassifyPosition(classifyValues);
      classifyPosition.setPositionId(position.getPositionId());
      classifyPositions.add(classifyPosition);
    }
    return classifyPositions;
  }

  // recherche globale
  @Override
  public List<SearchAxis> getPertinentAxis(SearchContext searchContext, String axisType)
          throws PdcException {
    List<AxisHeader> axis = getAxisByType(axisType);
    ArrayList<Integer> axisIds = new ArrayList<Integer>();
    String axisId = null;
    for (AxisHeader axisHeader : axis) {
      axisId = axisHeader.getPK().getId();
      axisIds.add(new Integer(axisId));
    }
    List<PertinentAxis> pertinentAxis = pdcClassifyBm.getPertinentAxis(searchContext, axisIds);

    return transformPertinentAxisIntoSearchAxis(pertinentAxis, axis);
  }

  // recherche à l'intérieur d'une instance
  @Override
  public List<SearchAxis> getPertinentAxisByInstanceId(SearchContext searchContext,
          String axisType, String instanceId) throws PdcException {
    return getPertinentAxisByInstanceId(searchContext, axisType, instanceId,
            new AxisFilter());
  }

  @Override
  public List<SearchAxis> getPertinentAxisByInstanceId(SearchContext searchContext,
          String axisType, String instanceId, AxisFilter filter)
          throws PdcException {
    List<String> instanceIds = new ArrayList<String>();
    instanceIds.add(instanceId);
    return getPertinentAxisByInstanceIds(searchContext, axisType, instanceIds,
            filter);
  }

  // recherche à l'intérieur d'une liste d'instance
  @Override
  public List<SearchAxis> getPertinentAxisByInstanceIds(SearchContext searchContext,
          String axisType, List<String> instanceIds) throws PdcException {
    return getPertinentAxisByInstanceIds(searchContext, axisType, instanceIds,
            new AxisFilter());
  }

  @Override
  public List<SearchAxis> getPertinentAxisByInstanceIds(SearchContext searchContext,
          String axisType, List<String> instanceIds, AxisFilter filter) throws PdcException {
    SilverTrace.info("Pdc", "PdcBmImpl.getPertinentAxisByInstanceIds",
            "root.MSG_GEN_ENTER_METHOD");
    // quels sont les axes utilisés par l'instance
    List<AxisHeader> axis = pdcUtilizationBm.getAxisHeaderUsedByInstanceIds(instanceIds,
            filter);
    SilverTrace.info("Pdc", "PdcBmImpl.getPertinentAxisByInstanceIds",
            "root.MSG_GEN_PARAM_VALUE", axis.size() + " axis used !");

    ArrayList<Integer> axisIds = new ArrayList<Integer>();
    String axisId = null;
    for (AxisHeader axisHeader : axis) {
      if (axisHeader.getAxisType().equals(axisType)) {
        axisId = axisHeader.getPK().getId();
        axisIds.add(new Integer(axisId));
      }
    }

    List<PertinentAxis> pertinentAxis = pdcClassifyBm.getPertinentAxis(searchContext, axisIds,
            pdcClassifyBm.getPositionsJoinStatement(instanceIds));
    SilverTrace.info("Pdc", "PdcBmImpl.getPertinentAxisByInstanceIds",
            "root.MSG_GEN_EXIT_METHOD", pertinentAxis.size() + " pertinent axis !");
    return transformPertinentAxisIntoSearchAxis(pertinentAxis, axis);
  }

  private List<SearchAxis> transformPertinentAxisIntoSearchAxis(
          List<PertinentAxis> pertinentAxisList,
          List<AxisHeader> axis) throws PdcException {
    List<SearchAxis> searchAxisList = new ArrayList<SearchAxis>();
    SearchAxis searchAxis = null;
    String axisId = null;
    for (PertinentAxis pertinentAxis : pertinentAxisList) {
      axisId = Integer.toString(pertinentAxis.getAxisId());
      searchAxis = new SearchAxis(pertinentAxis.getAxisId(), pertinentAxis.getNbObjects());
      for (AxisHeader axisHeader : axis) {
        if (axisHeader.getPK().getId().equals(axisId)) {
          setTranslations(axisHeader);

          searchAxis.setAxis(axisHeader);
          searchAxis.setAxisRootId(Integer.parseInt(getRootId(axisHeader.getPK().getId())));
          searchAxisList.add(searchAxis);
        }
      }
    }
    return searchAxisList;
  }

  // recherche à l'intérieur d'une instance
  @Override
  public List<Value> getPertinentDaughterValuesByInstanceId(
          SearchContext searchContext, String axisId, String valueId,
          String instanceId) throws PdcException {
    return getPertinentDaughterValuesByInstanceId(searchContext, axisId,
            valueId, instanceId, new AxisFilter());
  }

  @Override
  public List<Value> getPertinentDaughterValuesByInstanceId(
          SearchContext searchContext, String axisId, String valueId,
          String instanceId, AxisFilter filter) throws PdcException {
    List<String> instanceIds = new ArrayList<String>();
    instanceIds.add(instanceId);
    return getPertinentDaughterValuesByInstanceIds(searchContext, axisId,
            valueId, instanceIds, filter);
  }

  // recherche à l'intérieur d'une liste d'instance
  @Override
  public List<Value> getPertinentDaughterValuesByInstanceIds(
          SearchContext searchContext, String axisId, String valueId,
          List<String> instanceIds) throws PdcException {
    return getPertinentDaughterValuesByInstanceIds(searchContext, axisId,
            valueId, instanceIds, new AxisFilter());
  }

  @Override
  public List<Value> getPertinentDaughterValuesByInstanceIds(
          SearchContext searchContext, String axisId, String valueId,
          List<String> instanceIds, AxisFilter filter) throws PdcException {
    SilverTrace.info("Pdc",
            "PdcBmImpl.getPertinentDaughterValuesByInstanceIds",
            "root.MSG_GEN_ENTER_METHOD", "axisId = " + axisId + ", valueId = "
            + valueId + ", userId = " + searchContext.getUserId());

    List<Value> pertinentDaughters =
            filterValues(searchContext, axisId, valueId, instanceIds, filter);

    for (Value value : pertinentDaughters) {
      SilverTrace.debug(
              "pdcPeas",
              "PdcSearchSessionController.getPertinentDaughterValuesByInstanceIds()",
              "root.MSG_GEN_PARAM_VALUE", "valueId = " + value.getPK().getId()
              + ", valueName = " + value.getName() + ", nbObjects = "
              + value.getNbObjects());
    }

    return pertinentDaughters;
  }

  @Override
  public List<Value> getFirstLevelAxisValuesByInstanceId(SearchContext searchContext,
          String axisId, String instanceId) throws PdcException {
    List<String> instanceIds = new ArrayList<String>();
    instanceIds.add(instanceId);
    return getFirstLevelAxisValuesByInstanceIds(searchContext, axisId,
            instanceIds);
  }

  @Override
  public List<Value> getFirstLevelAxisValuesByInstanceIds(SearchContext searchContext,
          String axisId, List<String> instanceIds) throws PdcException {
    SilverTrace.info("Pdc", "PdcBmImpl.getFirstLevelAxisValuesByInstanceIds",
            "root.MSG_GEN_ENTER_METHOD", "axisId = " + axisId);

    // quelle est la racine de l'axe
    String rootId = getRootId(axisId);

    List<Value> pertinentDaughters = filterValues(searchContext, axisId, rootId, instanceIds);

    for (Value value : pertinentDaughters) {
      SilverTrace.debug("pdcPeas",
              "PdcSearchSessionController.getFirstLevelAxisValuesByInstanceIds()",
              "root.MSG_GEN_PARAM_VALUE", "valueId = " + value.getPK().getId()
              + ", valueName = " + value.getName() + ", nbObjects = "
              + value.getNbObjects());
    }

    return pertinentDaughters;

  }

  private String getRootId(String axisId) throws PdcException {
    Connection con = openConnection();
    SilverTrace.info("Pdc", "PdcBmImpl.getRootId", "root.MSG_GEN_PARAM_VALUE",
            "axisId = " + axisId);
    String rootId = null;
    try {
      AxisHeader axisHeader = getAxisHeader(axisId, false); // get the header of
      // the axe to obtain
      // the rootId.
      int treeId = axisHeader.getRootId();
      TreeNode root = tree.getRoot(con, Integer.toString(treeId));
      rootId = root.getPK().getId();
    } catch (Exception e) {
      throw new PdcException("PdcBmImpl.getRootId", SilverpeasException.ERROR,
              "Pdc.CANNOT_GET_VALUE", e);
    } finally {
      DBUtil.close(con);
    }
    SilverTrace.info("Pdc", "PdcBmImpl.getRootId", "root.MSG_GEN_PARAM_VALUE",
            "rootId = " + rootId);
    return rootId;
  }

  private List<Value> filterValues(SearchContext searchContext, String axisId,
          String motherId, List<String> instanceIds)
          throws PdcException {
    return filterValues(searchContext, axisId, motherId, instanceIds, new AxisFilter());
  }

  private List<Value> filterValues(SearchContext searchContext, String axisId,
          String motherId, List<String> instanceIds, AxisFilter filter)
          throws PdcException {
    SilverTrace.info("Pdc", "PdcBmImpl.filterValues",
            "root.MSG_GEN_ENTER_METHOD", "axisId = " + axisId + ", motherId = "
            + motherId);
    List<Value> descendants = null;
    ArrayList<String> emptyValues = new ArrayList<String>();
    Value descendant = null;
    Value nextDescendant = null;
    boolean isLeaf = false;
    boolean leafFind = false;
    PertinentValue pertinentValue = null;

    // get the header of the axe to obtain the treeId.
    AxisHeader axisHeader = getAxisHeader(axisId, false);
    int treeId = axisHeader.getRootId();

    List<ObjectValuePair> objectValuePairs = null;
    List<Integer> countedObjects = null;

    SilverTrace.info("Pdc", "PdcBmImpl.filterValues",
            "root.MSG_GEN_PARAM_VALUE", "apres getHeader()");

    ComponentSecurity componentSecurity = null;

    try {
      // Get all the values for this tree
      descendants = getAxisValues(treeId, filter);

      SilverTrace.info("Pdc", "PdcBmImpl.filterValues",
              "root.MSG_GEN_PARAM_VALUE", "apres getAxisValues()");

      JoinStatement joinStatement = pdcClassifyBm.getPositionsJoinStatement(instanceIds);

      SilverTrace.info("Pdc", "PdcBmImpl.filterValues",
              "root.MSG_GEN_PARAM_VALUE", "apres getPositionsJoinStatement()");

      List<PertinentValue> pertinentValues = pdcClassifyBm.getPertinentValues(searchContext,
              Integer.parseInt(axisId), joinStatement);

      SilverTrace.info("Pdc", "PdcBmImpl.filterValues",
              "root.MSG_GEN_PARAM_VALUE", "apres getPertinentValues()");

      // Set the NbObject for all the pertinent values
      String descendantPath = null;
      for (int nI = 0; nI < descendants.size(); nI++) {
        // Get the i descendant
        descendant = descendants.get(nI);
        descendantPath = descendant.getFullPath();

        // check if it's a leaf or not
        if (nI + 1 < descendants.size()) {
          nextDescendant = descendants.get(nI + 1);
          if (nextDescendant != null) {
            isLeaf = (nextDescendant.getLevelNumber() <= descendant.getLevelNumber());
          } else {
            isLeaf = false;
          }
        } else {
          isLeaf = true;
        }

        if (isLeaf) {
          // C'est une feuille, est-ce une feuille pertinente ?
          // le calcul a déjà été fait par getPertinentValues()
          pertinentValue = null;
          leafFind = false;
          for (int pv = 0; pv < pertinentValues.size() && !leafFind; pv++) {
            pertinentValue = pertinentValues.get(pv);
            if (pertinentValue.getValue().equals(descendantPath)) {
              leafFind = true;
              descendant.setNbObjects(pertinentValue.getNbObjects());
            }
          }
          if (!leafFind) {
            // Cette feuille n'est pas pertinente
            emptyValues.add(descendantPath);
            descendants.remove(nI--);
          }
        } else {
          // OPTIMIZATION : Checks if it is a descendant of an empty value
          boolean isEmpty = false;
          String emptyPath = null;
          for (int nJ = 0; nJ < emptyValues.size() && !isEmpty; nJ++) {
            emptyPath = emptyValues.get(nJ);
            if (descendantPath.startsWith(emptyPath)) {
              isEmpty = true;
            }
          }

          // Set the real number of objects or remove the empty values
          if (isEmpty) {
            descendants.remove(nI--);
          } else {
            if (objectValuePairs == null) {
              objectValuePairs = pdcClassifyBm.getObjectValuePairs(
                      searchContext, Integer.parseInt(axisId), joinStatement);
            }

            countedObjects = new ArrayList<Integer>();

            int nbObjects = 0;
            Integer objectId = null;
            String instanceId = null;
            for (ObjectValuePair ovp : objectValuePairs) {
              objectId = ovp.getObjectId();
              instanceId = ovp.getInstanceId();
              if (ovp.getValuePath().startsWith(descendantPath)
                      && !countedObjects.contains(objectId)) {
                // check if object is available for user
                if (instanceId.startsWith("kmelia")) {
                  if (componentSecurity == null) {
                    componentSecurity = (ComponentSecurity) Class.forName(
                            "com.stratelia.webactiv.kmelia.KmeliaSecurity").newInstance();
                    componentSecurity.enableCache();
                  }

                  if (componentSecurity.isObjectAvailable(instanceId,
                          searchContext.getUserId(), objectId.toString(),
                          "Publication")) {
                    nbObjects++;
                    countedObjects.add(objectId);
                  }
                } else {
                  nbObjects++;
                  countedObjects.add(objectId);
                }
              }
            }

            if (nbObjects > 0) {
              descendant.setNbObjects(nbObjects);
            } else {
              emptyValues.add(descendantPath);
              descendants.remove(nI--);
            }

            countedObjects = null;
          }
        }
        nextDescendant = null;
      }

      SilverTrace.info("Pdc", "PdcBmImpl.filterValues",
              "root.MSG_GEN_EXIT_METHOD");
      return descendants;
    } catch (Exception e) {
      throw new PdcException("PdcBmImpl.getPertinentDaughterValues",
              SilverpeasException.ERROR, "Pdc.CANNOT_FILTER_VALUES", e);
    } finally {
      if (componentSecurity != null) {
        componentSecurity.disableCache();
      }
    }
  }

  /**
   * To know if classifying is mandatory on a given component
   * @param componentId - id of the component to test
   * @return true if at least one axis has been selected on component AND at least one axis is
   * mandatory
   * @throws PdcException
   */
  @Override
  public boolean isClassifyingMandatory(String componentId) throws PdcException {
    List<UsedAxis> axisUsed = getUsedAxisByInstanceId(componentId);
    if (axisUsed == null) {
      return false;
    } else {
      for (UsedAxis axis : axisUsed) {
        if (axis.getMandatory() == 1) {
          return true;
        }
      }
      return false;
    }
  }

  @Override
  public void indexAllAxis() throws PdcException {
    Iterator<AxisHeader> axis = getAxis().iterator();
    AxisHeader a = null;
    Connection con = openConnection();
    try {
      while (axis.hasNext()) {
        a = axis.next();
        int rootId = a.getRootId();
        tree.indexTree(con, rootId);
      }
    } catch (Exception e) {
      throw new PdcException("PdcBmImpl.indexAllAxis()",
              SilverpeasException.ERROR, "Pdc.INDEXING_AXIS_FAILED", e);
    } finally {
      DBUtil.close(con);
    }
  }

  /**
   * Method declaration
   * @param con
   * @see
   */
  private Connection openConnection() throws PdcException {
    Connection con = null;
    try {
      con = DBUtil.makeConnection(JNDINames.PDC_DATASOURCE);
    } catch (Exception e) {
      throw new PdcException("PdcBmImpl.openConnection()",
              SilverpeasException.ERROR, "root.EX_CONNECTION_OPEN_FAILED", e);
    }
    return con;
  }

  private Connection openTransaction() throws PdcException {
    Connection con = null;
    try {
      con = DBUtil.makeConnection(JNDINames.PDC_DATASOURCE);
      con.setAutoCommit(false);
    } catch (Exception e) {
      throw new PdcException("PdcBmImpl.openTransaction()",
              SilverpeasException.ERROR, "root.EX_CONNECTION_OPEN_FAILED", e);
    }
    return con;
  }

  /**
   * Method declaration
   * @param con
   * @see
   */
  private void rollbackTransaction(Connection con) {
    if (con != null) {
      try {
        con.rollback();
      } catch (Exception e) {
        SilverTrace.error("Pdc", "PdcBmImpl.rollbackConnection()",
                "root.EX_CONNECTION_ROLLBACK_FAILED", e);
      }
    }
  }

  /**
   * Method declaration
   * @param con
   * @see
   */
  private void commitTransaction(Connection con) {
    if (con != null) {
      try {
        con.commit();
      } catch (Exception e) {
        SilverTrace.error("Pdc", "PdcBmImpl.commitTransaction()",
                "root.EX_CONNECTION_COMMIT_FAILED", e);
      }
    }
  }

  /**
   * *********************************************
   */
  /**
   * ******** CONTAINER INTERFACE METHODS ********
   */
  /**
   * *********************************************
   */
  /** Return the parameters for the HTTP call on the classify */
  @Override
  public String getCallParameters(String sComponentId, String sSilverContentId) {
    return "ComponentId=" + sComponentId + "&SilverObjectId="
            + sSilverContentId;
  }

  /** Remove all the positions of the given content */
  @Override
  public List<Integer> removePosition(Connection connection, int nSilverContentId)
          throws ContainerManagerException {
    try {
      return pdcClassifyBm.removePosition(connection, nSilverContentId);
    } catch (Exception e) {
      throw new ContainerManagerException("PdcBmImpl.removePosition",
              SilverpeasException.ERROR,
              "containerManager.EX_INTERFACE_REMOVE_FUNCTIONS", e);
    }

  }

  /** Get the SearchContext of the first position for the given SilverContentId */
  @Override
  public ContainerPositionInterface getSilverContentIdSearchContext(
          int nSilverContentId, String sComponentId)
          throws ContainerManagerException {
    try {
      // Get the positions
      List alPositions = pdcClassifyBm.getPositions(nSilverContentId,
              sComponentId);

      // Convert the first position in SearchContext
      SearchContext searchContext = new SearchContext();
      if (alPositions != null && alPositions.size() > 0) {
        Position pos = (Position) alPositions.get(0);
        List alValues = pos.getValues();
        for (int nI = 0; alValues != null && nI < alValues.size(); nI++) {
          com.stratelia.silverpeas.classifyEngine.Value value =
                  (com.stratelia.silverpeas.classifyEngine.Value) alValues.get(nI);
          if (value.getAxisId() != -1 && value.getValue() != null) {
            searchContext.addCriteria(new SearchCriteria(value.getAxisId(),
                    value.getValue()));
          }
        }
      }

      return searchContext;
    } catch (Exception e) {
      throw new ContainerManagerException(
              "PdcBmImpl.getSilverContentIdPositions", SilverpeasException.ERROR,
              "containerManager.EX_INTERFACE_FIND_FUNCTIONS", e);
    }
  }

  @Override
  public List<Integer> findSilverContentIdByPosition(
          ContainerPositionInterface containerPosition, List<String> alComponentId,
          String authorId, String afterDate, String beforeDate)
          throws ContainerManagerException {
    return findSilverContentIdByPosition(containerPosition, alComponentId,
            authorId, afterDate, beforeDate, true, true);
  }

  /** Find all the SilverContentId with the given position */
  @Override
  public List<Integer> findSilverContentIdByPosition(
          ContainerPositionInterface containerPosition, List<String> alComponentId,
          String authorId, String afterDate, String beforeDate,
          boolean recursiveSearch, boolean visibilitySensitive)
          throws ContainerManagerException {
    try {
      // Get the objects
      return pdcClassifyBm.findSilverContentIdByPosition(
              containerPosition, alComponentId, authorId, afterDate, beforeDate,
              recursiveSearch, visibilitySensitive);
    } catch (Exception e) {
      throw new ContainerManagerException(
              "PdcBmImpl.findSilverContentIdByPosition", SilverpeasException.ERROR,
              "containerManager.EX_INTERFACE_FIND_FUNCTIONS", e);
    }
  }

  @Override
  public List<Integer> findSilverContentIdByPosition(
          ContainerPositionInterface containerPosition, List<String> alComponentId)
          throws ContainerManagerException {
    return findSilverContentIdByPosition(containerPosition, alComponentId,
            true, true);
  }

  @Override
  public List<Integer> findSilverContentIdByPosition(
          ContainerPositionInterface containerPosition, List<String> alComponentId,
          boolean recursiveSearch, boolean visibilitySensitive)
          throws ContainerManagerException {
    return findSilverContentIdByPosition(containerPosition, alComponentId,
            null, null, null, recursiveSearch, visibilitySensitive);
  }

  private List<PdcAxisValue> findRecursivelyAllChildrenOf(final PdcAxisValue axisValue) {
    List<PdcAxisValue> allChildren = new ArrayList<PdcAxisValue>();
    Set<PdcAxisValue> directChildrenOfValue = axisValue.getChildValues();
    allChildren.addAll(directChildrenOfValue);
    for (PdcAxisValue aChild : directChildrenOfValue) {
      allChildren.addAll(findRecursivelyAllChildrenOf(aChild));
    }
    return allChildren;
  }
}
