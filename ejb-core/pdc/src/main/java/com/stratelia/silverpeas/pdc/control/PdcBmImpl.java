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

package com.stratelia.silverpeas.pdc.control;

import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;

import com.silverpeas.pdc.DAO.PdcRightsDAO;
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

/*
 * CVS Informations
 *
 * $Id: PdcBmImpl.java,v 1.27 2009/03/16 10:04:22 neysseri Exp $
 *
 * $Id: PdcBmImpl.java,v 1.27 2009/03/16 10:04:22 neysseri Exp $
 *
 * Revision 1.2  2002/10/17 13:33:21  neysseri
 * Glossary report from VSIC to KMedition
 *
 * Revision 1.1.1.1  2002/08/06 14:47:52  nchaix
 * no message
 *
 * Revision 1.1.1.1.10.3  2002/10/28 15:14:06  neysseri
 * no message
 *
 * Revision 1.1.1.1.10.2  2002/10/28 12:21:46  gshakirin
 * no message
 *
 * Revision 1.1.1.1.10.1  2002/10/28 11:10:10  gshakirin
 * no message
 *
 * Revision 1.1.1.1  2002/08/06 14:47:52  nchaix
 * no message
 *
 * Revision 1.80  2002/06/10 13:10:29  nchaix
 * MEC02
 *
 * Revision 1.79.16.3  2002/06/03 15:19:36  neysseri
 * Optimisations
 *
 * Revision 1.79.16.2  2002/06/03 15:13:14  neysseri
 * Optimisations
 *
 * Revision 1.79.16.1  2002/05/31 08:39:35  neysseri
 * optimisation 1
 *
 * Revision 1.79  2002/04/18 14:29:36  neysseri
 * Prise en compte des transactions depuis le pdcPeas sur les fonctions de suppression
 *
 * Revision 1.78  2002/04/09 12:29:00  cbonin
 * Enleve les System.out.println
 *
 * Revision 1.77  2002/04/09 12:28:02  cbonin
 * Ajout de la methode public List getDaughterValues(String axisId, String valueId)
 *
 * Revision 1.76  2002/04/08 15:44:06  cbonin
 * return null au lieu de lancer des exceptions nullPointer
 *
 * Revision 1.75  2002/04/05 13:23:56  cbonin
 * no message
 *
 * Revision 1.74  2002/04/04 13:10:06  santonio
 * Tient compte de la recherche global (PDC + Classique)
 * Généralisation de certaines méthodes
 *
 * Revision 1.73  2002/03/29 09:41:10  cbonin
 * Ajout de la methode :
 *
 * public Value getRoot(String axisId)
 *
 * Revision 1.72  2002/03/28 09:16:40  cbonin
 * Ajout de la methode :
 * public List getAxisValuesByName(String valueName)
 *
 * Revision 1.71  2002/03/22 10:33:00  nchaix
 * no message
 *
 * Revision 1.70  2002/03/21 18:25:52  nchaix
 * no message
 *
 * Revision 1.69  2002/03/21 16:51:25  nchaix
 * no message
 *
 * Revision 1.68  2002/03/21 15:38:35  nchaix
 * no message
 *
 * Revision 1.67  2002/03/20 13:33:33  santonio
 * no message
 *
 * Revision 1.66  2002/03/14 09:33:27  nchaix
 * no message
 *
 * Revision 1.65  2002/03/11 09:21:29  neysseri
 * no message
 *
 * Revision 1.64  2002/03/11 09:20:43  neysseri
 * no message
 *
 * Revision 1.63  2002/03/08 15:51:11  nchaix
 * no message
 *
 * Revision 1.62  2002/03/08 13:42:25  santonio
 * no message
 *
 * Revision 1.61  2002/03/08 11:52:38  santonio
 * fonctionnalités sur les invariances dans le metier
 *
 * Revision 1.60  2002/03/07 16:04:10  neysseri
 * no message
 *
 * Revision 1.59  2002/03/06 09:20:16  neysseri
 * no message
 *
 * Revision 1.58  2002/03/05 14:58:01  neysseri
 * no message
 *
 * Revision 1.57  2002/03/05 12:51:30  neysseri
 * no message
 *
 * Revision 1.56  2002/03/05 08:27:16  santonio
 * no message
 *
 * Revision 1.55  2002/03/05 08:18:39  santonio
 * no message
 *
 * Revision 1.54  2002/03/04 17:27:02  nchaix
 * no message
 *
 * Revision 1.53  2002/03/04 16:51:40  santonio
 * no message
 *
 * Revision 1.52  2002/03/04 16:44:55  nchaix
 * no message
 *
 * Revision 1.51  2002/03/04 16:43:09  nchaix
 * no message
 *
 * Revision 1.50  2002/03/04 15:57:19  nchaix
 * no message
 *
 * Revision 1.49  2002/03/04 14:05:06  nchaix
 * no message
 *
 * Revision 1.48  2002/03/04 08:34:19  nchaix
 * no message
 *
 * Revision 1.47  2002/03/01 16:31:28  neysseri
 * no message
 *
 * Revision 1.46  2002/03/01 15:54:08  santonio
 * no message
 *
 * Revision 1.45  2002/02/28 16:58:02  nchaix
 * no message
 *
 * Revision 1.44  2002/02/28 16:06:32  neysseri
 * no message
 *
 * Revision 1.43  2002/02/28 13:56:34  nchaix
 * no message
 *
 * Revision 1.42  2002/02/28 13:51:56  nchaix
 * no message
 *
 * Revision 1.41  2002/02/28 12:15:19  neysseri
 * no message
 *
 * Revision 1.40  2002/02/28 11:01:34  santonio
 * add getNbMaxAxis() method used by PdcSessionController
 *
 * Revision 1.39  2002/02/28 09:57:46  nchaix
 * no message
 *
 * Revision 1.38  2002/02/27 19:59:18  neysseri
 * no message
 *
 * Revision 1.37  2002/02/27 16:42:05  neysseri
 * gestion des transactions
 *
 * Revision 1.36  2002/02/27 11:10:07  santonio
 * no message
 *
 * Revision 1.35  2002/02/27 10:06:44  santonio
 * no message
 *
 * Revision 1.34  2002/02/26 18:48:32  neysseri
 * no message
 *
 * Revision 1.33  2002/02/26 16:38:17  santonio
 * no message
 *
 * Revision 1.32  2002/02/25 16:20:37  neysseri
 * no message
 *
 * Revision 1.31  2002/02/22 17:13:17  neysseri
 * no message
 *
 * Revision 1.30  2002/02/22 12:20:15  neysseri
 * modif de getPositions()
 *
 * Revision 1.29  2002/02/22 12:01:05  santonio
 * no message
 *
 * Revision 1.28  2002/02/21 18:38:08  neysseri
 * no message
 *
 * Revision 1.27  2002/02/21 18:32:59  santonio
 * no message
 *
 * Revision 1.26  2002/02/19 17:16:44  neysseri
 * jindent + javadoc
 *
 */

/**
 * Class declaration
 * @author
 */
public class PdcBmImpl implements PdcBm, ContainerInterface {
  /**
   * SilverpeasBeanDAO is the main link with the SilverPeas persitence. We indicate the Object
   * SilverPeas which map the database.
   */
  private SilverpeasBeanDAO dao = null;

  private AxisHeaderI18NDAO axisHeaderI18NDAO = (AxisHeaderI18NDAO) new AxisHeaderI18NDAO();

  /**
   * PdcUtilizationBm, the pdc utilization interface to manage which axis are used by which instance
   */
  private PdcUtilizationBm pdcUtilizationBm = (PdcUtilizationBm) new PdcUtilizationBmImpl();

  /**
   * PdcClassifyBm, the pdc classify interface to manage how are classified object in the pdc
   */
  private PdcClassifyBm pdcClassifyBm = (PdcClassifyBm) new PdcClassifyBmImpl();

  /**
   * TreeBm, the node interface to manage operations user
   */
  private TreeBm tree = (TreeBm) new TreeBmImpl();

  private static Hashtable<String, AxisHeader> axisHeaders = new Hashtable<String, AxisHeader>();

  /**
   * Constructor declaration
   * @see
   */
  public PdcBmImpl() {
    try {
      dao = SilverpeasBeanDAOFactory
          .getDAO("com.stratelia.silverpeas.pdc.model.AxisHeaderPersistence");
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
  public List<AxisHeader> getAxisByType(String type) throws PdcException {
    List<AxisHeaderPersistence> axis = null;

    try {
      axis = (List) dao.findByWhereClause(new AxisPK("useless"), " AxisType='"
          + type + "' order by AxisOrder ");
      return persistence2AxisHeaders(axis);
    } catch (PersistenceException exce_select) {
      throw new PdcException("PdcBmImpl.getAxisByType",
          SilverpeasException.ERROR, "Pdc.CANNOT_FIND_AXES_TYPE", exce_select);
    }
  }

  private List<AxisHeader> persistence2AxisHeaders(List<AxisHeaderPersistence> silverpeasBeans)
      throws PersistenceException, PdcException {
    List<AxisHeader> axisHeaders = new ArrayList<AxisHeader>();
    if (silverpeasBeans != null) {
      Iterator it = silverpeasBeans.iterator();
      while (it.hasNext()) {
        AxisHeaderPersistence silverpeasBean = (AxisHeaderPersistence) it
            .next();
        AxisHeader axisHeader = new AxisHeader(silverpeasBean);

        // ajout des traductions
        setTranslations(axisHeader);

        axisHeaders.add(axisHeader);
      }
    }
    return axisHeaders;
  }

  private void setTranslations(AxisHeader axisHeader) {
    // ajout de la traduction par defaut
    int axisId = Integer.parseInt(axisHeader.getPK().getId());
    AxisHeaderI18N translation = new AxisHeaderI18N(axisId, axisHeader
        .getLanguage(), axisHeader.getName(), axisHeader.getDescription());
    axisHeader.addTranslation(translation);
    // récupération des autres traductions
    Connection con = null;
    try {
      con = openConnection(false);

      List<AxisHeaderI18N> translations = axisHeaderI18NDAO.getTranslations(con, axisId);
      for (int t = 0; translations != null && t < translations.size(); t++) {
        AxisHeaderI18N tr = (AxisHeaderI18N) translations.get(t);
        axisHeader.addTranslation(tr);
      }
    } catch (Exception e) {
    } finally {
      closeConnection(con);
    }
  }

  /**
   * Returns a list of axes sorted.
   * @return a list sorted or null otherwise
   */
  public List<AxisHeader> getAxis() throws PdcException {
    List axis = null;

    try {
      axis = (List) dao.findByWhereClause(new AxisPK("useless"),
          " 1=1 order by AxisType asc, AxisOrder asc ");
      return persistence2AxisHeaders(axis);
    } catch (PersistenceException exce_select) {
      throw new PdcException("PdcBmImpl.getAxis", SilverpeasException.ERROR,
          "Pdc.CANNOT_FIND_AXES", exce_select);
    }

  }

  /**
   * Return the number of axe for one type.
   * @return the number of axe
   */
  public int getNbAxisByType(String type) throws PdcException {
    return getAxisByType(type).size();
  }

  /**
   * Return the number of axe.
   * @return the number of axe
   */
  public int getNbAxis() throws PdcException {
    return getAxis().size();
  }

  /**
   * Return the max number of axis.
   * @return the max number of axis
   */
  public int getNbMaxAxis() throws PdcException {
    return ClassifyEngine.getMaxAxis();
  }

  /**
   * Create an axe into the data base.
   * @param axisHeader - the object which contains all data about an axe
   * @return 1 if the maximun of axe is atteignable, 2 if the axe already exist, 0 otherwise
   */
  public int createAxis(AxisHeader axisHeader) throws PdcException {

    int status = 0;
    List<AxisHeader> axis = getAxis();

    // search if the maximun number of axes is atteignable
    if (axis.size() > getNbMaxAxis()) {
      status = 1;
    } else if (isAxisNameExist(axis, axisHeader)) {
      status = 2;
    } else {
      // get the Connection object
      Connection con = openConnection(false);

      try {
        int order = axisHeader.getAxisOrder();
        String type = axisHeader.getAxisType();
        // recupere les axes de meme type ordonnés qui ont un numéro d'ordre
        // >=
        // à celui de l'axe à inserer
        String whereClause = "AxisType = '" + type + "' and AxisOrder >= "
            + order + " ORDER BY AxisOrder ASC";

        // ATTENTION il faut traiter l'ordre des autres axes
        Collection axisToUpdate = dao.findByWhereClause(axisHeader.getPK(),
            whereClause);

        Iterator it = axisToUpdate.iterator();
        AxisHeaderPersistence axisToMove = null;

        while (it.hasNext()) {
          axisToMove = (AxisHeaderPersistence) it.next();
          // On modifie l'ordre de l'axe en ajoutant 1 par rapport au nouvel axe
          order++;
          axisToMove.setAxisOrder(order);

          dao.update(axisToMove);

        }

        // build of the Value
        Value value =
            new Value(
                "unknown", Integer.valueOf(axisHeader.getRootId()).toString(),
                axisHeader.getName(), axisHeader.getDescription(), axisHeader
                    .getCreationDate(), axisHeader.getCreatorId(), "unknown", -1,
                -1, "unknown");

        value.setLanguage(axisHeader.getLanguage());
        value.setRemoveTranslation(axisHeader.isRemoveTranslation());
        value.setTranslationId(axisHeader.getTranslationId());

        String treeId = tree.createRoot(con, (TreeNode) value);

        axisHeader.setRootId((new Integer(treeId)).intValue());
        SilverTrace
            .info("Pdc", "PdcBmImpl.createAxis()", "root.MSG_GEN_PARAM_VALUE",
                "axisHeader = " + axisHeader.toString());
        AxisHeaderPersistence ahp = new AxisHeaderPersistence(axisHeader);
        AxisPK axisPK = (AxisPK) dao.add(con, ahp);

        // Register new axis to classifyEngine
        pdcClassifyBm.registerAxis(con, new Integer(axisPK.getId()).intValue());

        commitConnection(con);
      } catch (Exception exce_create) {
        rollbackConnection(con);
        throw new PdcException("PdcBmImpl.createAxis",
            SilverpeasException.ERROR, "Pdc.CANNOT_CREATE_AXE", exce_create);
      } finally {
        closeConnection(con);
      }
    }

    return status;
  }

  /**
   * Update an axe into the data base.
   * @param axisHeader - the object which contains all data about an axe
   * @return 2 if the axe already exist, 0 otherwise
   */
  public int updateAxis(AxisHeader axisHeader) throws PdcException {
    SilverTrace.info("Pdc", "PdcBmImpl.updateAxis()",
        "root.MSG_GEN_PARAM_VALUE", "axisHeader = " + axisHeader.toString());
    int status = 0;
    List<AxisHeader> axis = getAxis();

    if (isAxisNameExist(axis, axisHeader)) {
      status = 2;
    } else {
      // get the Connection object
      Connection con = openConnection(false);

      try {
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
          Collection axisToUpdate = dao.findByWhereClause(con, axisHeader
              .getPK(), whereClause);

          boolean axisHasMoved = true;
          Iterator it = axisToUpdate.iterator();
          AxisHeaderPersistence firstAxis = null;

          if (it.hasNext()) {
            // Test si l'axe n'a pas changé de place
            firstAxis = (AxisHeaderPersistence) it.next();
            if (firstAxis.getPK().getId().equals(axisId)) {
              axisHasMoved = false;
            }
          }

          if (axisHasMoved) {
            it = axisToUpdate.iterator();
            AxisHeaderPersistence axisToMove = null;

            while (it.hasNext()) {
              axisToMove = (AxisHeaderPersistence) it.next();
              // On modifie l'ordre de l'axe en ajoutant 1 par rapport au nouvel
              // axe
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
        AxisHeader oldAxisHeader = getAxisHeader(con, axisHeader.getPK()
            .getId());

        SilverTrace.info("Pdc", "PdcBmImpl.updateAxis()",
            "root.MSG_GEN_PARAM_VALUE", "oldAxisHeader.getRootId() = "
                + oldAxisHeader.getRootId());

        // regarder si le nom et la description ont changé en fonction de la
        // langue
        boolean axisNameHasChanged = false;
        boolean axisDescHasChanged = false;

        if (oldAxisHeader.getName() != null
            && !oldAxisHeader.getName().equalsIgnoreCase(axisHeader.getName()))
          axisNameHasChanged = true;
        if (oldAxisHeader.getDescription() != null
            && !oldAxisHeader.getDescription().equalsIgnoreCase(axisHeader.getDescription()))
          axisDescHasChanged = true;
        else if (oldAxisHeader.getDescription() == null
            && axisHeader.getDescription() != null)
          axisDescHasChanged = true;

        if (axisNameHasChanged || axisDescHasChanged) {
          // The name of the axis has changed, We must change the name of the
          // root to
          String treeId = Integer.valueOf(oldAxisHeader.getRootId()).toString();
          /*
           * TreeNode root = tree.getRoot(con, treeId); TreeNode node = new
           * TreeNode(root.getPK().getId(), root.getTreeId(), axisHeader.getName(),
           * axisHeader.getDescription(), root.getCreationDate(), root.getCreatorId(),
           * root.getPath(), root.getLevelNumber(), root.getOrderNumber(), root.getFatherId());
           * node.setLanguage(root.getLanguage());
           * node.setRemoveTranslation(root.isRemoveTranslation());
           * node.setTranslationId(root.getTranslationId());
           */
          TreeNode root = tree.getRoot(con, treeId);
          TreeNode node = new TreeNode(root.getPK().getId(), root.getTreeId(),
              axisHeader.getName(), axisHeader.getDescription(), root
                  .getCreationDate(), root.getCreatorId(), root.getPath(), root
                  .getLevelNumber(), root.getOrderNumber(), root.getFatherId());
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
            List<AxisHeaderI18N> translations = axisHeaderI18NDAO.getTranslations(con, Integer
                .parseInt(axisHeader.getPK().getId()));

            if (translations != null && translations.size() > 0) {
              AxisHeaderI18N translation = (AxisHeaderI18N) translations.get(0);

              axisHeader.setLanguage(translation.getLanguage());
              axisHeader.setName(translation.getName());
              axisHeader.setDescription(translation.getDescription());

              AxisHeaderPersistence axisHP = new AxisHeaderPersistence(
                  axisHeader);
              dao.update(con, axisHP);

              axisHeaderI18NDAO.deleteTranslation(con, translation.getId());
            }
          } else {
            axisHeaderI18NDAO.deleteTranslation(con, Integer
                .parseInt(axisHeader.getTranslationId()));
          }
        } else {
          if (axisHeader.getLanguage() != null) {
            if (oldAxisHeader.getLanguage() == null) {
              // translation for the first time
              oldAxisHeader.setLanguage(I18NHelper.defaultLanguage);
            }
            if (!axisHeader.getLanguage().equalsIgnoreCase(
                oldAxisHeader.getLanguage())) {
              AxisHeaderI18N newAxis = new AxisHeaderI18N(Integer
                  .parseInt(axisHeader.getPK().getId()), axisHeader
                  .getLanguage(), axisHeader.getName(), axisHeader
                  .getDescription());
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

        commitConnection(con);
      } catch (Exception exce_update) {
        rollbackConnection(con);
        throw new PdcException("PdcBmImpl.updateAxis",
            SilverpeasException.ERROR, "Pdc.CANNOT_UPDATE_AXE", exce_update);
      } finally {
        closeConnection(con);
      }
    }

    return status;
  }

  /**
   * delete the axe from the data base and all its subtrees.
   * @param axisId - the id of the selected axe
   */
  public void deleteAxis(Connection con, String axisId) throws PdcException {
    try {
      AxisHeader axisHeader = getAxisHeader(con, axisId); // get the header of
      // the axe to obtain
      // the rootId.

      PdcRightsDAO.deleteAxisRights(con, axisId);

      tree.deleteTree(con, new Integer(axisHeader.getRootId()).toString()); // delete
      // data
      // in
      // the
      // tree
      // table
      pdcUtilizationBm.deleteUsedAxisByAxisId(con, axisId); // delete data in
      // the pdc
      // utilization table
      dao.remove(con, new AxisPK(axisId));

      // Unregister axis to classifyEngine
      pdcClassifyBm.unregisterAxis(con, new Integer(axisId).intValue());
      (new PdcSubscriptionUtil()).checkAxisOnDelete(Integer.parseInt(axisId),
          axisHeader.getName());

      // remove axisheader from cache
      axisHeaders.remove(axisId);

      // suppression des traductions
      axisHeaderI18NDAO.deleteTranslations(con, new Integer(axisId).intValue());
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
  public Axis getAxisDetail(String axisId) throws PdcException {
    return getAxisDetail(axisId, new AxisFilter());
  }

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

  private String getTreeId(String axisId) throws PdcException {
    AxisHeaderPersistence axisHeader = getAxisHeaderPersistence(axisId); // get
    // the
    // header
    // of
    // the
    // axe
    // to
    // obtain
    // the
    // rootId.
    int treeId = -1;
    if (axisHeader != null) {
      treeId = axisHeader.getRootId();
    }
    return new Integer(treeId).toString();
  }

  /**
   * Returns a value from an axe.
   * @param valueId - the id of the selected value
   * @return the Value object
   */
  public com.stratelia.silverpeas.pdc.model.Value getValue(String axisId,
      String valueId) throws PdcException {
    com.stratelia.silverpeas.pdc.model.Value value = null;
    TreeNode node = null;

    Connection con = openConnection(false);

    try {
      node = tree.getNode(con, new TreeNodePK(valueId), getTreeId(axisId));
      value = createValue(node);
    } catch (Exception exce_select) {
      throw new PdcException("PdcBmImpl.getValue", SilverpeasException.ERROR,
          "Pdc.CANNOT_ACCESS_VALUE", exce_select);
    } finally {
      closeConnection(con);
    }

    return value;
  }

  /**
   * Returns a value from an axe.
   * @param valueId - the id of the selected value
   * @return the Value object
   */
  public com.stratelia.silverpeas.pdc.model.Value getAxisValue(String valueId,
      String treeId) throws PdcException {
    com.stratelia.silverpeas.pdc.model.Value value = null;

    Connection con = openConnection(false);

    try {
      value = createValue(tree.getNode(con, new TreeNodePK(valueId), treeId));
    } catch (Exception exce_select) {
      throw new PdcException("PdcBmImpl.getAxisValue",
          SilverpeasException.ERROR, "Pdc.CANNOT_ACCESS_VALUE", exce_select);
    } finally {
      closeConnection(con);
    }

    return value;
  }

  /**
   * Return a list of axis values having the value name in parameter
   * @param valueName - the name of the value.
   * @return List
   * @throws PdcException
   * @see
   */
  public List<Value> getAxisValuesByName(String valueName) throws PdcException {
    List<Value> listValues = null;
    Connection con = openConnection(false);

    try {
      List<TreeNode> listTreeNodes = tree.getNodesByName(con, valueName);
      listValues = createValuesList(listTreeNodes);

    } catch (Exception exce_select) {
      throw new PdcException("PdcBmImpl.getAxisValuesByName",
          SilverpeasException.ERROR, "Pdc.CANNOT_FIND_VALUES", exce_select);
    } finally {
      closeConnection(con);
    }

    return listValues;
  }

  /**
   * Return a list of String corresponding to the valueId of the value in parameter
   * @param axisId
   * @param valueId
   * @return List of String
   * @throws PdcException
   * @see getDaughters
   */
  public List<String> getDaughterValues(String axisId, String valueId)
      throws PdcException {
    List<String> listValuesString = new ArrayList<String>();
    Connection con = openConnection(false);

    try {
      List<Value> listValues = getDaughters(con, valueId, axisId);

      Iterator<Value> i = listValues.iterator();
      while (i.hasNext()) {
        Value value = i.next();
        listValuesString.add(value.getPK().getId());
      }
    } catch (Exception exce_select) {
      throw new PdcException("PdcBmImpl.getDaughterValues",
          SilverpeasException.ERROR, "Pdc.CANNOT_RETRIEVE_SUBNODES",
          exce_select);
    } finally {
      closeConnection(con);
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
  public List<Value> getFilteredAxisValues(String rootId, AxisFilter filter)
      throws PdcException {
    List<Value> values = new ArrayList<Value>();
    com.stratelia.silverpeas.pdc.model.Value value = null;
    try {
      values = getAxisValues(new Integer(rootId).intValue(), filter);

      // for each filtered value, get all values from root to this finded value
      for (int i = 0; i < values.size(); i++) {
        value = (com.stratelia.silverpeas.pdc.model.Value) values.get(i);
        value.setPathValues(getFullPath(value.getValuePK().getId(), value
            .getTreeId()));
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
  public com.stratelia.silverpeas.pdc.model.Value getRoot(String axisId)
      throws PdcException {
    Connection con = openConnection(false);
    SilverTrace.info("Pdc", "PdcBmImpl.getRoot", "root.MSG_GEN_PARAM_VALUE",
        "axisId = " + axisId);
    com.stratelia.silverpeas.pdc.model.Value root = null;
    try {
      AxisHeader axisHeader = getAxisHeader(axisId, false); // get the header of
      // the axe to obtain
      // the rootId.
      int treeId = axisHeader.getRootId();
      TreeNode treeNode = tree.getRoot(con, new Integer(treeId).toString());
      root = createValue(treeNode);
    } catch (Exception e) {
      throw new PdcException("PdcBmImpl.getRoot", SilverpeasException.ERROR,
          "Pdc.CANNOT_GET_VALUE", e);
    } finally {
      closeConnection(con);
    }
    return root;
  }

  /**
   * @param treeId The id of the selected axis.
   * @return The list of values of the axis.
   */
  public List<Value> getAxisValues(int treeId) throws PdcException {
    return getAxisValues(treeId, new AxisFilter());
  }

  private List<Value> getAxisValues(int treeId, AxisFilter filter) throws PdcException {
    List<Value> values = null;

    Connection con = openConnection(false);

    try {
      values = createValuesList(tree.getTree(con, new Integer(treeId)
          .toString(), filter));
    } catch (Exception exce_select) {
      throw new PdcException("PdcBmImpl.getAxisValues",
          SilverpeasException.ERROR, "Pdc.CANNOT_ACCESS_LIST_OF_VALUES",
          exce_select);
    } finally {
      closeConnection(con);
    }

    return values;
  }

  /**
   * insert a value which is defined like a mother value
   * @param valueToInsert - a Value object
   * @param refValue - the id of the Value to insert
   * @return 1 if the name already exist 0 otherwise
   */
  public int insertMotherValue(
      com.stratelia.silverpeas.pdc.model.Value valueToInsert, String refValue,
      String axisId) throws PdcException {
    int status = 0;
    AxisHeader axisHeader = getAxisHeader(axisId, false); // get the header of
    // the axe to obtain
    // the treeId.
    String treeId = new Integer(axisHeader.getRootId()).toString();

    // get the mother value of the value which have the refValue
    // to find sisters of the valueToInsert
    TreeNode refNode = (TreeNode) getAxisValue(refValue, treeId);

    Connection con = openConnection(false);

    try {
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
      if ((status == 0) && oldPath.size() != 0) {
        // the mother value is created

        // Après l'insertion de la mere, on recupere les nouveaux chemins
        ArrayList<String> newPath = getPathes(con, refValue, treeId);
        // call the ClassifyBm to create oldValue and newValue
        // and to replace the oldValue by the newValue
        pdcClassifyBm.createValuesAndReplace(con, axisId, oldPath, newPath);

      }

      commitConnection(con);
    } catch (Exception e) {
      rollbackConnection(con);
      throw new PdcException("PdcBmImpl.insertMotherValue",
          SilverpeasException.ERROR, "Pdc.Pdc.CANNOT_UPDATE_POSTION", e);
    } finally {
      closeConnection(con);
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
  public int moveValueToNewFatherId(Axis axis, Value valueToMove,
      String newFatherId, int orderNumber) throws PdcException {
    int status = 0;
    String treeId = Integer.toString(axis.getAxisHeader().getRootId());
    String valueToMoveId = valueToMove.getPK().getId();
    Connection con = openConnection(false);

    try {
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
      if ((status == 0) && oldPath.size() != 0) {
        // the mother value is created
        // Après l'insertion de la mere, on recupere les nouveaux chemins
        ArrayList<String> newPath = getPathes(con, valueToMoveId, treeId);
        // call the ClassifyBm to create oldValue and newValue
        // and to replace the oldValue by the newValue
        pdcClassifyBm.createValuesAndReplace(con, new Integer(axis
            .getAxisHeader().getRootId()).toString(), oldPath, newPath);
      }
      commitConnection(con);
    } catch (Exception e) {
      rollbackConnection(con);
      throw new PdcException("PdcBmImpl.moveValueToNewFatherId",
          SilverpeasException.ERROR, "Pdc.CANNOT_MOVE_VALUE", e);
    } finally {
      closeConnection(con);
    }
    return status;
  }

  /**
   * retourne les droits hérités sur la valeur
   * @param current value
   * @return ArrayList( ArrayList UsersId, ArrayList GroupsId)
   * @throws PdcException
   */
  public List<List<String>> getInheritedManagers(Value value) throws PdcException {
    String axisId = value.getAxisId();
    String path = value.getPath();
    String[] explosedPath = path.split("/");
    List<List<String>> usersAndgroups = new ArrayList<List<String>>();
    List<String> usersInherited = new ArrayList<String>();
    List<String> groupsInherited = new ArrayList<String>();
    for (int i = 1; i < explosedPath.length; i++) {
      // Value valuePath = getAxisValue(explosedPath[i], value.getTreeId());
      List<List<String>> managers = getManagers(axisId, explosedPath[i]);
      List<String> usersId = managers.get(0);
      List<String> groupsId = managers.get(1);
      for (int j = 0; j < usersId.size(); j++) {
        // si le userId n'est pas déjà dans la liste
        if (!usersInherited.contains(usersId.get(j)))
          usersInherited.add(usersId.get(j));
      }
      for (int j = 0; j < groupsId.size(); j++) {
        // si le groupid n'est pas déjà dans la liste
        if (!groupsInherited.contains(groupsId.get(j)))
          groupsInherited.add(groupsId.get(j));
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
  public List<List<String>> getManagers(String axisId, String valueId) throws PdcException {
    List<String> usersId;
    List<String> groupsId;
    Connection con = openConnection(false);
    try {
      usersId = PdcRightsDAO.getUserIds(con, axisId, valueId);
      groupsId = PdcRightsDAO.getGroupIds(con, axisId, valueId);
    } catch (SQLException e) {
      throw new PdcException("PdcBmImpl.getManagers",
          SilverpeasException.ERROR, "Pdc.CANNOT_GET_MANAGERS", e);
    } finally {
      closeConnection(con);
    }
    List<List<String>> usersAndgroups = new ArrayList<List<String>>();
    usersAndgroups.add(usersId);
    usersAndgroups.add(groupsId);

    return usersAndgroups;
  }

  public boolean isUserManager(String userId) throws PdcException {
    Connection con = openConnection(false);

    if (!PdcSettings.delegationEnabled)
      return false;

    boolean isManager = false;

    try {
      // First, check if user is directly manager of a part of PDC
      isManager = PdcRightsDAO.isUserManager(con, userId);

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
      closeConnection(con);
    }
  }

  private boolean isGroupManager(String[] groupIds) throws PdcException {
    Connection con = openConnection(false);
    try {
      return PdcRightsDAO.isGroupManager(con, groupIds);
    } catch (SQLException e) {
      throw new PdcException("PdcBmImpl.isGroupManager",
          SilverpeasException.ERROR, "Pdc.CANNOT_GET_MANAGERS", e);
    } finally {
      closeConnection(con);
    }
  }

  /**
   * met à jour les droits sur la valeur
   * @param ArrayList ( ArrayList UsersId, ArrayList GroupsId), current value
   * @return
   * @throws PdcException
   */
  public void setManagers(List<String> userIds, List<String> groupIds, String axisId,
      String valueId) throws PdcException {
    Connection con = openConnection(true);

    try {
      // supprime tous les droits sur la valeur
      PdcRightsDAO.deleteRights(con, axisId, valueId);

      for (int i = 0; i < userIds.size(); i++) {
        PdcRightsDAO
            .insertUserId(con, axisId, valueId, userIds.get(i));
      }
      for (int i = 0; i < groupIds.size(); i++) {
        PdcRightsDAO.insertGroupId(con, axisId, valueId, groupIds
            .get(i));
      }
    } catch (SQLException e) {
      throw new PdcException("PdcBmImpl.setManagers",
          SilverpeasException.ERROR, "Pdc.CANNOT_SET_MANAGER", e);
    } finally {
      closeConnection(con);
    }
  }

  public void razManagers(String axisId, String valueId) throws PdcException {
    Connection con = openConnection(true);
    try {
      PdcRightsDAO.deleteRights(con, axisId, valueId);
    } catch (SQLException e) {
      throw new PdcException("PdcBmImpl.razManagers",
          SilverpeasException.ERROR, "Pdc.CANNOT_REMOVE_MANAGER", e);
    } finally {
      closeConnection(con);
    }
  }

  /**
   * Supprime les droits associés au userid
   * @param userId
   * @throws SQLException
   */
  public void deleteManager(String userId) throws PdcException {
    Connection con = openConnection(true);
    try {
      PdcRightsDAO.deleteManager(con, userId);
    } catch (SQLException e) {
      throw new PdcException("PdcBmImpl.deleteManager",
          SilverpeasException.ERROR, "Pdc.CANNOT_REMOVE_MANAGER", e);
    } finally {
      closeConnection(con);
    }
  }

  /**
   * Supprime les droits associés au groupid
   * @param groupId
   * @throws SQLException
   */
  public void deleteGroupManager(String groupId) throws PdcException {
    Connection con = openConnection(true);
    try {
      PdcRightsDAO.deleteGroupManager(con, groupId);
    } catch (SQLException e) {
      throw new PdcException("PdcBmImpl.deleteGroupManager",
          SilverpeasException.ERROR, "Pdc.CANNOT_REMOVE_MANAGER", e);
    } finally {
      closeConnection(con);
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
    TreeNode nodeTree = null;
    ArrayList<String> pathList = new ArrayList<String>();
    TreeNodePK refNodePK = new TreeNodePK(refValue);
    try {
      List<TreeNode> treeList = tree.getSubTree(con, refNodePK, treeId); // get
      // a
      // list
      // of
      // tree
      // node
      // for one tree node, get its path
      for (int i = 0; i < treeList.size(); i++) {
        nodeTree = (TreeNode) treeList.get(i);
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
      tree.insertFatherToNode(con, (TreeNode) valueToInsert, new TreeNodePK(
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
    TreeNode refNode = (TreeNode) getAxisValue(refValue, treeId);
    List<Value> daughters = getDaughters(con, refNode.getFatherId(), treeId);

    if (isValueNameExist(daughters, valueToInsert)) {
      status = 1;
    } else {
      try {
        tree.insertFatherToNode(con, (TreeNode) valueToInsert, new TreeNodePK(
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
  public int createDaughterValue(
      com.stratelia.silverpeas.pdc.model.Value valueToInsert, String refValue,
      String treeId) throws PdcException {
    // get the Connection object
    Connection con = openConnection(true);

    int status = 0;
    List<Value> daughters = getDaughters(con, refValue, treeId);

    if (isValueNameExist(daughters, valueToInsert)) {
      status = 1;
      closeConnection(con);
    } else {
      try {
        tree.createSonToNode(con, (TreeNode) valueToInsert, new TreeNodePK(
            refValue), treeId);
      } catch (Exception exce_create) {
        throw new PdcException("PdcBmImpl.createDaughterValue",
            SilverpeasException.ERROR, "Pdc.CANNOT_CREATE_VALUE", exce_create);
      } finally {
        closeConnection(con);
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
  public String createDaughterValueWithId(
      com.stratelia.silverpeas.pdc.model.Value valueToInsert, String refValue,
      String treeId) throws PdcException {
    // get the Connection object
    Connection con = openConnection(true);

    String daughterId = null;
    List<Value> daughters = getDaughters(con, refValue, treeId);

    if (isValueNameExist(daughters, valueToInsert)) {
      daughterId = "-1";
      closeConnection(con);
    } else {
      try {
        daughterId = tree.createSonToNode(con, (TreeNode) valueToInsert, new TreeNodePK(
            refValue), treeId);
      } catch (Exception exce_create) {
        throw new PdcException("PdcBmImpl.createDaughterValueWithId",
            SilverpeasException.ERROR, "Pdc.CANNOT_CREATE_VALUE", exce_create);
      } finally {
        closeConnection(con);
      }
    }
    return daughterId;
  }

  /**
   * Update the selected value
   * @param value - a Value object
   * @return 1 if the name already exist 0 otherwise
   */
  public int updateValue(com.stratelia.silverpeas.pdc.model.Value value,
      String treeId) throws PdcException {
    // get the Connection object
    Connection con = openConnection(true);

    int status = 0;

    try {
      com.stratelia.silverpeas.pdc.model.Value oldValue = getAxisValue(value
          .getPK().getId(), treeId);
      List<Value> daughters = getDaughters(con, oldValue.getMotherId(), treeId);

      if (isValueNameExist(daughters, value)) {
        status = 1;
      } else {
        TreeNode node = new TreeNode(value.getPK().getId(), treeId, value
            .getName(), value.getDescription(), oldValue.getCreationDate(),
            oldValue.getCreatorId(), oldValue.getPath(), oldValue
                .getLevelNumber(), value.getOrderNumber(), oldValue
                .getFatherId());
        node.setLanguage(value.getLanguage());
        node.setRemoveTranslation(value.isRemoveTranslation());
        node.setTranslationId(value.getTranslationId());
        tree.updateNode(con, node);
      }
    } catch (Exception exce_update) {
      new PdcException("PdcBmImpl.updateValue", SilverpeasException.ERROR,
          "Pdc.CANNOT_UPDATE_VALUE", exce_update);
    } finally {
      closeConnection(con);
    }

    return status;
  }

  /**
   * Delete a value and it's sub tree
   * @param valueId - the id of the select value
   */
  public void deleteValueAndSubtree(Connection con, String valueId,
      String axisId, String treeId) throws PdcException {
    SilverTrace.info("Pdc", "PdcBmImpl.deleteValueAndSubtree",
        "root.MSG_GEN_PARAM_VALUE", "axisId = " + axisId);

    // Connection con = openConnection(false);

    try {
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
      List pathInfo = getFullPath(valueId, treeId);

      List<TreeNode> subtree = tree.getSubTree(con, treeNodePK, treeId);
      tree.deleteSubTree(con, treeNodePK, treeId);

      Value value = new Value();
      // on efface les droits sur les valeurs
      for (int i = 0; i < subtree.size(); i++) {
        value = createValue((TreeNode) subtree.get(i));
        PdcRightsDAO.deleteRights(con, axisId, value.getPK().getId());
      }
      // Warning, we must update the path of the Value(Classify)
      if (oldPath.size() != 0) {
        // Après l'effacement de la valeur et de son arborescence, on recupere
        // les nouveaux chemins
        // ArrayList newPath = getPathes(con,valueId);
        // Les nouveaux chemins sont tous identiques, c'est celui de la mère
        ArrayList<String> newPath = new ArrayList<String>();
        for (int i = 0; i < oldPath.size(); i++) {
          newPath.add(motherPath);
        }
        SilverTrace.info("Pdc", "PdcBmImpl.deleteValueAndSubtree",
            "root.MSG_GEN_PARAM_VALUE", "newPath.size() = " + newPath.size());
        // call the ClassifyBm to create oldValue and newValue
        // and to replace the oldValue by the newValue

        (new PdcSubscriptionUtil()).checkValueOnDelete(
            Integer.parseInt(axisId), axisName, oldPath, newPath, pathInfo);
        pdcClassifyBm.createValuesAndReplace(con, axisId, oldPath, newPath);
      }

      // commitConnection(con);

    } catch (Exception exce_delete) {
      // rollbackConnection(con);
      throw new PdcException("PdcBmImpl.deleteValueAndSubtree",
          SilverpeasException.ERROR, "Pdc.CANNOT_DELETE_VALUE_SUBTREE",
          exce_delete);
    }
    /*
     * finally { closeConnection(con); }
     */
  }

  /**
   * Delete the selected value. If a daughter of the selected value is named like a sister of her
   * mother the delete is not possible.
   * @param valueId - the id of the select value
   * @return null if the delete is possible, the name of her daughter else.
   */
  public String deleteValue(Connection con, String valueId, String axisId,
      String treeId) throws PdcException {
    String possibleDaughterName = null;
    try {
      Value valueToDelete = getAxisValue(valueId, treeId);
      List<Value> daughtersOfMother = getDaughters(con, valueToDelete.getMotherId(),
          treeId); // filles de la mère = soeurs des filles de la valeur à
      // supprimer
      List<Value> daughtersOfValueToDelete = getDaughters(con, valueId, treeId); // filles
      // de
      // la
      // valeur
      // à
      // supprimer

      possibleDaughterName = isValueNameExist(daughtersOfMother, daughtersOfValueToDelete);
      if (possibleDaughterName == null) {

        // Mise à jour de la partie utilisation
        updateBaseValueInInstances(con, valueId, axisId, treeId);

        // pdcUtilizationBm.deleteUsedAxisByValueId(con, valueId, axisId);

        // Avant l'effacement de la valeur, on recupere les vieux chemins
        ArrayList<String> oldPath = getPathes(con, valueId, treeId);

        AxisHeader axisHeader = getAxisHeader(con, axisId);
        String axisName = axisHeader.getName();
        List pathInfo = getFullPath(valueId, treeId);

        tree.deleteNode(con, new TreeNodePK(valueId), treeId);

        // on efface les droits sur la valeur
        PdcRightsDAO.deleteRights(con, axisId, valueId);

        // Warning, we must update the path of the Value(Classify)
        if (oldPath.size() != 0) {
          // Après l'effacement de la valeur, on creait les nouveaux chemins

          ArrayList<String> newPath = getPathes(con, valueId, treeId);
          // lecture de l'arrayList oldPath et on retire la valueId
          String path = ""; // ancien chemin
          String pattern = "/" + valueId; // motif que l'on doit rechercher dans
          // l'ancien chemin pour le supprimer
          int lenOfPattern = pattern.length(); // longueur du motif
          int pattern_idx; // position du pattern rechercher
          for (int i = 0; i < oldPath.size(); i++) {
            path = (String) oldPath.get(i);
            pattern_idx = path.indexOf(pattern); // ne peux etre à -1
            path = path.substring(0, pattern_idx)
                + path.substring(pattern_idx + lenOfPattern); // retire le motif
            newPath.add(path);
          }

          // call the ClassifyBm to create oldValue and newValue
          // and to replace the oldValue by the newValue
          (new PdcSubscriptionUtil()).checkValueOnDelete(Integer
              .parseInt(axisId), axisName, oldPath, newPath, pathInfo);
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
  public List<Value> getFullPath(String valueId, String treeId) throws PdcException {
    Connection con = openConnection(false);

    List<Value> listValues = null;
    try {
      // récupère une collection de Value
       List<TreeNode> listTreeNode = tree.getFullPath(con, new TreeNodePK(valueId), treeId);
      listValues = createValuesList(listTreeNode);
    } catch (Exception exce_delete) {
      throw new PdcException("PdcBmImpl.deleteValue",
          SilverpeasException.ERROR, "Pdc.CANNOT_DELETE_VALUE", exce_delete);
    } finally {
      closeConnection(con);
    }

    return listValues;
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
      axisHeader = (AxisHeader) it.next();
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
      valueToCheck = (Value) it.next();
      if (isValueNameExist(values, valueToCheck)) {
        valueName = valueToCheck.getName();
        break;
      }
    }

    return valueName;
  }

  /**
   * Returns an AxisHeader Object.
   * @param axisId - the id of the selected axe
   * @return an AxisHeader
   */
  /*
   * public AxisHeader getAxisHeader(String axisId) { AxisHeader axisHeader = null; try { axisHeader
   * = (AxisHeader) dao.findByPrimaryKey(new AxisPK(axisId)); } catch (PersistenceException
   * err_select) { SilverTrace.info("PDC", "PdcBmImpl.getAxisHeader",
   * "Pdc.CANNOT_RETRIEVE_HEADER_AXE", err_select); } return axisHeader; }
   */

  public AxisHeader getAxisHeader(String axisId) {
    return getAxisHeader(axisId, true);
  }

  public AxisHeader getAxisHeader(String axisId, boolean setTranslations) {
    AxisHeader axisHeader = (AxisHeader) axisHeaders.get(axisId);
    if (axisHeader == null) {
      try {
        AxisHeaderPersistence axisHeaderPersistence = (AxisHeaderPersistence) dao
            .findByPrimaryKey(new AxisPK(axisId));
        axisHeader = new AxisHeader(axisHeaderPersistence);

        axisHeaders.put(axisHeader.getPK().getId(), axisHeader);
      } catch (PersistenceException err_select) {
        SilverTrace.info("PDC", "PdcBmImpl.getAxisHeader",
            "Pdc.CANNOT_RETRIEVE_HEADER_AXE", err_select);
      }
    }
    if (setTranslations)
      setTranslations(axisHeader);
    return axisHeader;
  }

  private AxisHeaderPersistence getAxisHeaderPersistence(String axisId) {
    try {
      return (AxisHeaderPersistence) dao.findByPrimaryKey(new AxisPK(axisId));
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
      AxisHeaderPersistence axisHeaderPersistence = (AxisHeaderPersistence) dao
          .findByPrimaryKey(connection, new AxisPK(axisId));
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

  public List<Value> getDaughters(String axisId, String valueId) {
    List<Value> daughters = new ArrayList<Value>();
    Connection con = null;
    try {
      con = openConnection(false);
      AxisHeader axisHeader = getAxisHeader(axisId, false);
      int tId = axisHeader.getRootId();
      daughters = getAxisValues(tId);
    } catch (Exception err_list) {
      SilverTrace.info("PDC", "PdcBmImpl.getDaughters",
          "Pdc.CANNOT_RETRIEVE_SUBNODES", err_list);
    } finally {
      closeConnection(con);
    }
    return daughters;
  }

  public List<Value> getSubAxisValues(String axisId, String valueId) {
    List<Value> daughters = new ArrayList<Value>();
    Connection con = null;
    try {
      con = openConnection(false);
      AxisHeader axisHeader = getAxisHeader(axisId, false);
      int tId = axisHeader.getRootId();

      daughters = createValuesList(tree.getSubTree(con,
          new TreeNodePK(valueId), new Integer(tId).toString()));
    } catch (Exception err_list) {
      SilverTrace.info("PDC", "PdcBmImpl.getSubAxis",
          "Pdc.CANNOT_RETRIEVE_SUBNODES", err_list);
    } finally {
      closeConnection(con);
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
    Iterator<TreeNode> it = treeNodes.iterator();

    while (it.hasNext()) {
      values.add(createValue(it.next()));
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
      Value value = new Value(treeNode
          .getPK().getId(), treeNode.getTreeId(), treeNode.getName(), treeNode
          .getDescription(), treeNode.getCreationDate(), treeNode
          .getCreatorId(), treeNode.getPath(), treeNode.getLevelNumber(),
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
  public int addUsedAxis(UsedAxis usedAxis) throws PdcException {
    AxisHeader axisHeader = getAxisHeader(Integer
        .toString(usedAxis.getAxisId()), false); // get the header of the axe to
    // obtain the treeId.
    String treeId = new Integer(axisHeader.getRootId()).toString();
    return pdcUtilizationBm.addUsedAxis(usedAxis, treeId);
  }

  /**
   * Method declaration
   * @param usedAxis
   * @return
   * @throws PdcException
   * @see
   */
  public int updateUsedAxis(UsedAxis usedAxis) throws PdcException {
    AxisHeader axisHeader = getAxisHeader(Integer
        .toString(usedAxis.getAxisId()), false); // get the header of the axe to
    // obtain the treeId.
    String treeId = Integer.valueOf(axisHeader.getRootId()).toString();

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
    List<Integer> objectIdList = pdcClassifyBm.getObjectsByInstance(usedAxis
        .getInstanceId());

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
    boolean isAscendante = false;
    if (pdcClassifyBm.hasAlreadyPositions(objectIdList, usedAxis)) {
      isAscendante = true;
    }
    return isAscendante;
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
    int newBaseValue = new Integer(value.getMotherId()).intValue();

    SilverTrace.info("Pdc", "PdcBmImpl.updateBaseValueInInstances",
        "root.MSG_GEN_PARAM_VALUE", "newBaseValue = " + newBaseValue);

    pdcUtilizationBm.updateOrDeleteBaseValue(con,
        new Integer(baseValueToUpdate).intValue(), new Integer(newBaseValue)
            .intValue(), new Integer(axisId).intValue(), treeId);
  }

  /**
   * Update some base values from the PdcUtilization table
   * @param baseValuesToUpdate - the base values that must be updated
   */
  private void updateBaseValuesInInstances(Connection con,
      String baseValueToUpdate, String axisId, String treeId)
      throws PdcException {

    List descendants = null;

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
    int newBaseValue = new Integer(value.getMotherId()).intValue();

    SilverTrace.info("Pdc", "PdcBmImpl.updateBaseValuesInInstances",
        "root.MSG_GEN_PARAM_VALUE", "newBaseValue = " + newBaseValue);

    TreeNode descendant = null;
    String descendantId = null;
    for (int i = 0; i < descendants.size(); i++) {
      descendant = (TreeNode) descendants.get(i);
      descendantId = descendant.getPK().getId();

      SilverTrace.info("Pdc", "PdcBmImpl.updateBaseValuesInInstances",
          "root.MSG_GEN_PARAM_VALUE", "descendantId = " + descendantId);

      pdcUtilizationBm.updateOrDeleteBaseValue(con, new Integer(descendantId)
          .intValue(), new Integer(newBaseValue).intValue(),
          new Integer(axisId).intValue(), treeId);
    }
  }

  /**
   * Method declaration
   * @param usedAxisId
   * @throws PdcException
   * @see
   */
  public void deleteUsedAxis(String usedAxisId) throws PdcException {
    pdcUtilizationBm.deleteUsedAxis(usedAxisId);
  }

  /**
   * Method declaration
   * @param usedAxisIds
   * @throws PdcException
   * @see
   */
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

  public List<UsedAxis> getUsedAxisToClassify(String instanceId, int silverObjectId)
      throws PdcException {
    List<UsedAxis> usedAxis = getUsedAxisByInstanceId(instanceId);

    // Il faut enrichir chaque axe utilisé par l'axe (pour l'i18n) et ses
    // valeurs
    UsedAxis axis = null;
    int axisRootId = -1;
    for (int i = 0; i < usedAxis.size(); i++) {
      axis = (UsedAxis) usedAxis.get(i);

      if (I18NHelper.isI18N) {
        AxisHeader header = getAxisHeader(Integer.toString(axis.getAxisId()));
        axis._setAxisHeader(header);
      }

      axisRootId = axis._getAxisRootId();
      axis._setAxisValues(getAxisValues(axisRootId));
      if (axis.getVariant() == 0) {
        // Si l'axe est invariant, il faut préciser la valeur obligatoire
        List<ClassifyPosition> positions = getPositions(silverObjectId, instanceId);
        String invariantValue = null;
        if (positions.size() > 0) {
          // Une position existe déjà
          ClassifyPosition position = (ClassifyPosition) positions.get(0);
          invariantValue = position.getValueOnAxis(axis.getAxisId());
          axis._setInvariantValue(invariantValue);
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
  public void copyPositions(int fromObjectId, String fromInstanceId,
      int toObjectId, String toInstanceId) throws PdcException {
    List positions = getPositions(fromObjectId, fromInstanceId);

    List usedAxis = getUsedAxisByInstanceId(toInstanceId);

    ClassifyPosition position = null;
    ClassifyPosition newPosition = null;
    for (int p = 0; p < positions.size(); p++) {
      position = (ClassifyPosition) positions.get(p);

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

    ClassifyValue value = null;
    List values = position.getListClassifyValue();
    for (int v = 0; v < values.size(); v++) {
      value = (ClassifyValue) values.get(v);
      value = checkClassifyValue(value, usedAxis);
      if (value != null)
        newPosition.addValue(value);
    }

    if (newPosition.getValues() == null)
      return null;

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
      if (("/" + value.getValue() + "/").indexOf(baseValuePath) == -1) {
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
      if (uAxis.getAxisId() == axisId)
        return uAxis;
    }
    return null;
  }

  public int addPosition(int silverObjectId, ClassifyPosition position,
      String sComponentId) throws PdcException {
    return addPosition(silverObjectId, position, sComponentId, true);
  }

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

  public int updatePosition(ClassifyPosition position, String instanceId,
      int silverObjectId) throws PdcException {
    return updatePosition(position, instanceId, silverObjectId, true);
  }

  public int updatePosition(ClassifyPosition position, String instanceId,
      int silverObjectId, boolean alertSubscribers) throws PdcException {

    List usedAxisList = getUsedAxisToClassify(instanceId, silverObjectId);
    ArrayList invariantUsedAxis = new ArrayList();
    UsedAxis ua = null;
    for (int i = 0; i < usedAxisList.size(); i++) {
      ua = (UsedAxis) usedAxisList.get(i);
      // on cherche les axes invariants
      if (ua.getVariant() == 0) {
        invariantUsedAxis.add(new Integer(ua.getAxisId()));
      }
    }

    // maintenant, on cherche les valeurs qui sont sur un axe invariant
    List classifyValueList = position.getValues();
    ClassifyValue cv = null;
    ArrayList classifyValues = new ArrayList();
    for (int i = 0; i < classifyValueList.size(); i++) {
      cv = (ClassifyValue) classifyValueList.get(i);
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

  public void deletePosition(int positionId, String sComponentId)
      throws PdcException {
    pdcClassifyBm.deletePosition(positionId, sComponentId);
  }

  public List<ClassifyPosition> getPositions(int silverObjectId, String sComponentId)
      throws PdcException {
    List<Position> positions = pdcClassifyBm.getPositions(silverObjectId, sComponentId);
    ArrayList<ClassifyPosition> classifyPositions = new ArrayList<ClassifyPosition>();

    // transform Position to ClassifyPosition
    ClassifyPosition classifyPosition = null;
    Position position = null;
    for (int i = 0; i < positions.size(); i++) {
      position = (Position) positions
          .get(i);
      List values = position.getValues();

      // transform Value to ClassifyValue
      ClassifyValue classifyValue = null;
      com.stratelia.silverpeas.classifyEngine.Value value = null;
      ArrayList<ClassifyValue> classifyValues = new ArrayList<ClassifyValue>();
      String valuePath = "";
      String valueId = "";
      for (int p = 0; p < values.size(); p++) {
        value = (com.stratelia.silverpeas.classifyEngine.Value) values.get(p);
        classifyValue = new ClassifyValue(value.getAxisId(), value.getValue());

        if (value.getAxisId() != -1) {
          int treeId = Integer.parseInt(getTreeId(Integer.toString(value
              .getAxisId())));
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
  public List<SearchAxis> getPertinentAxis(SearchContext searchContext, String axisType)
      throws PdcException {
    List<AxisHeader> axis = getAxisByType(axisType);
    ArrayList<Integer> axisIds = new ArrayList<Integer>();
    AxisHeader axisHeader = null;
    String axisId = null;
    for (int i = 0; i < axis.size(); i++) {
      axisHeader = (AxisHeader) axis.get(i);
      axisId = axisHeader.getPK().getId();
      axisIds.add(new Integer(axisId));
    }
    List pertinentAxis = pdcClassifyBm.getPertinentAxis(searchContext, axisIds);

    return transformPertinentAxisIntoSearchAxis(pertinentAxis, axis);
  }

  // recherche à l'intérieur d'une instance
  public List<SearchAxis> getPertinentAxisByInstanceId(SearchContext searchContext,
      String axisType, String instanceId) throws PdcException {
    return getPertinentAxisByInstanceId(searchContext, axisType, instanceId,
        new AxisFilter());
  }

  public List<SearchAxis> getPertinentAxisByInstanceId(SearchContext searchContext,
      String axisType, String instanceId, AxisFilter filter)
      throws PdcException {
    List<String> instanceIds = new ArrayList<String>();
    instanceIds.add(instanceId);
    return getPertinentAxisByInstanceIds(searchContext, axisType, instanceIds,
        filter);
  }

  // recherche à l'intérieur d'une liste d'instance
  public List<SearchAxis> getPertinentAxisByInstanceIds(SearchContext searchContext,
      String axisType, List<String> instanceIds) throws PdcException {
    return getPertinentAxisByInstanceIds(searchContext, axisType, instanceIds,
        new AxisFilter());
  }

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
    AxisHeader axisHeader = null;
    String axisId = null;
    for (int i = 0; i < axis.size(); i++) {
      axisHeader = (AxisHeader) axis.get(i);
      if (axisHeader.getAxisType().equals(axisType)) {
        axisId = axisHeader.getPK().getId();
        axisIds.add(new Integer(axisId));
      }
    }

    List pertinentAxis = pdcClassifyBm.getPertinentAxis(searchContext, axisIds,
        pdcClassifyBm.getPositionsJoinStatement(instanceIds));
    SilverTrace.info("Pdc", "PdcBmImpl.getPertinentAxisByInstanceIds",
        "root.MSG_GEN_EXIT_METHOD", pertinentAxis.size() + " pertinent axis !");
    return transformPertinentAxisIntoSearchAxis(pertinentAxis, axis);
  }

  private List<SearchAxis> transformPertinentAxisIntoSearchAxis(List pertinentAxisList,
      List axis) throws PdcException {
    ArrayList searchAxisList = new ArrayList();
    SearchAxis searchAxis = null;
    PertinentAxis pertinentAxis = null;
    AxisHeader axisHeader = null;
    String axisId = null;
    for (int i = 0; i < pertinentAxisList.size(); i++) {
      pertinentAxis = (PertinentAxis) pertinentAxisList.get(i);
      axisId = new Integer(pertinentAxis.getAxisId()).toString();
      searchAxis = new SearchAxis(pertinentAxis.getAxisId(), pertinentAxis
          .getNbObjects());
      for (int j = 0; j < axis.size(); j++) {
        axisHeader = (AxisHeader) axis.get(j);
        if (axisHeader.getPK().getId().equals(axisId)) {
          // searchAxis.setAxisName(axisHeader.getName());
          setTranslations(axisHeader);

          searchAxis.setAxis(axisHeader);
          searchAxis.setAxisRootId(new Integer(getRootId(axisHeader.getPK()
              .getId())).intValue());
          searchAxisList.add(searchAxis);
        }
      }
    }
    return searchAxisList;
  }

  // recherche à l'intérieur d'une instance
  public List<Value> getPertinentDaughterValuesByInstanceId(
      SearchContext searchContext, String axisId, String valueId,
      String instanceId) throws PdcException {
    return getPertinentDaughterValuesByInstanceId(searchContext, axisId,
        valueId, instanceId, new AxisFilter());
  }

  public List<Value> getPertinentDaughterValuesByInstanceId(
      SearchContext searchContext, String axisId, String valueId,
      String instanceId, AxisFilter filter) throws PdcException {
    List instanceIds = (List) new ArrayList();
    instanceIds.add(instanceId);
    return getPertinentDaughterValuesByInstanceIds(searchContext, axisId,
        valueId, instanceIds, filter);
  }

  // recherche à l'intérieur d'une liste d'instance
  public List<Value> getPertinentDaughterValuesByInstanceIds(
      SearchContext searchContext, String axisId, String valueId,
      List<String> instanceIds) throws PdcException {
    return getPertinentDaughterValuesByInstanceIds(searchContext, axisId,
        valueId, instanceIds, new AxisFilter());
  }

  public List<Value> getPertinentDaughterValuesByInstanceIds(
      SearchContext searchContext, String axisId, String valueId,
      List<String> instanceIds, AxisFilter filter) throws PdcException {
    SilverTrace.info("Pdc",
        "PdcBmImpl.getPertinentDaughterValuesByInstanceIds",
        "root.MSG_GEN_ENTER_METHOD", "axisId = " + axisId + ", valueId = "
            + valueId + ", userId = " + searchContext.getUserId());

    // List pertinentValues = pdcClassifyBm.getPertinentValues(searchContext,
    // new Integer(axisId).intValue(),
    // pdcClassifyBm.getPositionsJoinStatement(instanceIds));
    List pertinentValues = new ArrayList();

    List<Value> pertinentDaughters = filterValues(searchContext, axisId, valueId,
        pertinentValues, instanceIds, filter);

    Value value = null;
    for (int d = 0; d < pertinentDaughters.size(); d++) {
      value = (Value) pertinentDaughters
          .get(d);
      SilverTrace
          .debug(
              "pdcPeas",
              "PdcSearchSessionController.getPertinentDaughterValuesByInstanceIds()",
              "root.MSG_GEN_PARAM_VALUE", "valueId = " + value.getPK().getId()
                  + ", valueName = " + value.getName() + ", nbObjects = "
                  + value.getNbObjects());
    }

    return pertinentDaughters;
  }

  public List<Value> getFirstLevelAxisValuesByInstanceId(SearchContext searchContext,
      String axisId, String instanceId) throws PdcException {
    List instanceIds = (List) new ArrayList();
    instanceIds.add(instanceId);
    return getFirstLevelAxisValuesByInstanceIds(searchContext, axisId,
        instanceIds);
  }

  public List<Value> getFirstLevelAxisValuesByInstanceIds(SearchContext searchContext,
      String axisId, List<String> instanceIds) throws PdcException {
    SilverTrace.info("Pdc", "PdcBmImpl.getFirstLevelAxisValuesByInstanceIds",
        "root.MSG_GEN_ENTER_METHOD", "axisId = " + axisId);

    // List pertinentValues = pdcClassifyBm.getPertinentValues(searchContext,
    // new Integer(axisId).intValue(),
    // pdcClassifyBm.getPositionsJoinStatement(instanceIds));
    List pertinentValues = new ArrayList();

    // quelle est la racine de l'axe
    String rootId = getRootId(axisId);

    List<Value> pertinentDaughters = filterValues(searchContext, axisId, rootId,
        pertinentValues, instanceIds);

    Value value = null;
    for (int d = 0; d < pertinentDaughters.size(); d++) {
      value = pertinentDaughters.get(d);
      SilverTrace.debug("pdcPeas",
          "PdcSearchSessionController.getFirstLevelAxisValuesByInstanceIds()",
          "root.MSG_GEN_PARAM_VALUE", "valueId = " + value.getPK().getId()
              + ", valueName = " + value.getName() + ", nbObjects = "
              + value.getNbObjects());
    }

    return pertinentDaughters;

  }

  private String getRootId(String axisId) throws PdcException {
    Connection con = openConnection(false);
    SilverTrace.info("Pdc", "PdcBmImpl.getRootId", "root.MSG_GEN_PARAM_VALUE",
        "axisId = " + axisId);
    String rootId = null;
    try {
      AxisHeader axisHeader = getAxisHeader(axisId, false); // get the header of
      // the axe to obtain
      // the rootId.
      int treeId = axisHeader.getRootId();
      TreeNode root = tree.getRoot(con, new Integer(treeId).toString());
      rootId = root.getPK().getId();
    } catch (Exception e) {
      throw new PdcException("PdcBmImpl.getRootId", SilverpeasException.ERROR,
          "Pdc.CANNOT_GET_VALUE", e);
    } finally {
      closeConnection(con);
    }
    SilverTrace.info("Pdc", "PdcBmImpl.getRootId", "root.MSG_GEN_PARAM_VALUE",
        "rootId = " + rootId);
    return rootId;
  }

  private List<Value> filterValues(SearchContext searchContext, String axisId,
      String motherId, List valuesToFilter, List<String> instanceIds)
      throws PdcException {
    return filterValues(searchContext, axisId, motherId, valuesToFilter,
        instanceIds, new AxisFilter());
  }

  private List<Value> filterValues(SearchContext searchContext, String axisId,
      String motherId, List valuesToFilter, List<String> instanceIds, AxisFilter filter)
      throws PdcException {
    SilverTrace.info("Pdc", "PdcBmImpl.filterValues",
        "root.MSG_GEN_ENTER_METHOD", "axisId = " + axisId + ", motherId = "
            + motherId);
    Connection con = openConnection(true);
    ArrayList<Value> descendants = null;
    ArrayList<String> emptyValues = new ArrayList<String>();
    com.stratelia.silverpeas.pdc.model.Value descendant = null;
    com.stratelia.silverpeas.pdc.model.Value nextDescendant = null;
    boolean isLeaf = false;
    boolean leafFind = false;
    PertinentValue pertinentValue = null;

    // get the header of the axe to obtain the treeId.
    AxisHeader axisHeader = getAxisHeader(axisId, false);
    int treeId = axisHeader.getRootId();

    List objectValuePairs = null;
    List<Integer> countedObjects = null;

    SilverTrace.info("Pdc", "PdcBmImpl.filterValues",
        "root.MSG_GEN_PARAM_VALUE", "apres getHeader()");

    ComponentSecurity componentSecurity = null;

    try {
      // Get all the values for this tree
      descendants = (ArrayList) getAxisValues(treeId, filter);

      SilverTrace.info("Pdc", "PdcBmImpl.filterValues",
          "root.MSG_GEN_PARAM_VALUE", "apres getAxisValues()");

      JoinStatement joinStatement = pdcClassifyBm
          .getPositionsJoinStatement(instanceIds);

      SilverTrace.info("Pdc", "PdcBmImpl.filterValues",
          "root.MSG_GEN_PARAM_VALUE", "apres getPositionsJoinStatement()");

      List pertinentValues = pdcClassifyBm.getPertinentValues(searchContext,
          new Integer(axisId).intValue(), joinStatement);

      SilverTrace.info("Pdc", "PdcBmImpl.filterValues",
          "root.MSG_GEN_PARAM_VALUE", "apres getPertinentValues()");

      // Set the NbObject for all the pertinent values
      String descendantPath = null;
      for (int nI = 0; nI < descendants.size(); nI++) {
        // Get the i descendant
        descendant = (com.stratelia.silverpeas.pdc.model.Value) descendants
            .get(nI);
        descendantPath = descendant.getFullPath();

        // check if it's a leaf or not
        if (nI + 1 < descendants.size()) {
          nextDescendant = (com.stratelia.silverpeas.pdc.model.Value) descendants
              .get(nI + 1);
          if (nextDescendant != null)
            isLeaf = (nextDescendant.getLevelNumber() <= descendant
                .getLevelNumber());
          else
            isLeaf = false;
        } else
          isLeaf = true;

        if (isLeaf) {
          // C'est une feuille, est-ce une feuille pertinente ?
          // le calcul a déjà été fait par getPertinentValues()
          pertinentValue = null;
          leafFind = false;
          for (int pv = 0; pv < pertinentValues.size() && !leafFind; pv++) {
            pertinentValue = (PertinentValue) pertinentValues.get(pv);
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
            emptyPath = (String) emptyValues.get(nJ);
            if (descendantPath.startsWith(emptyPath))
              isEmpty = true;
          }

          // Set the real number of objects or remove the empty values
          if (isEmpty)
            descendants.remove(nI--);
          else {
            /*
             * PertinentAxis pertinentAxis = pdcClassifyBm.getPertinentAxis(searchContext, axisId,
             * descendantPath, joinStatement); SilverTrace.info("Pdc", "PdcBmImpl.filterValues",
             * "root.MSG_GEN_PARAM_VALUE", "apres getPertinentAxis()");
             * if(pertinentAxis.getNbObjects() > 0)
             * descendant.setNbObjects(pertinentAxis.getNbObjects()); else {
             * emptyValues.add(descendantPath); descendants.remove(nI--); }
             */
            if (objectValuePairs == null)
              objectValuePairs = pdcClassifyBm.getObjectValuePairs(
                  searchContext, Integer.parseInt(axisId), joinStatement);

            countedObjects = new ArrayList<Integer>();

            int nbObjects = 0;
            Integer objectId = null;
            String instanceId = null;
            ObjectValuePair ovp = null;
            for (int o = 0; o < objectValuePairs.size(); o++) {
              ovp = (ObjectValuePair) objectValuePairs.get(o);
              objectId = ovp.getObjectId();
              instanceId = ovp.getInstanceId();
              if (ovp.getValuePath().startsWith(descendantPath)
                  && !countedObjects.contains(objectId)) {
                // check if object is available for user
                if (instanceId.startsWith("kmelia")) {
                  if (componentSecurity == null) {
                    componentSecurity = (ComponentSecurity) Class.forName(
                        "com.stratelia.webactiv.kmelia.KmeliaSecurity")
                        .newInstance();
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
      closeConnection(con);

      if (componentSecurity != null)
        componentSecurity.disableCache();
    }
  }

  /**
   * To know if classifying is mandatory on a given component
   * @param componentId - id of the component to test
   * @return true if at least one axis has been selected on component AND at least one axis is
   * mandatory
   * @throws PdcException
   */
  public boolean isClassifyingMandatory(String componentId) throws PdcException {
    List<UsedAxis> axisUsed = getUsedAxisByInstanceId(componentId);
    if (axisUsed == null) {
      return false;
    } else {
      UsedAxis axis = null;
      for (int a = 0; a < axisUsed.size(); a++) {
        axis = (UsedAxis) axisUsed.get(a);
        if (axis.getMandatory() == 1)
          return true;
      }
      return false;
    }
  }

  public void indexAllAxis() throws PdcException {
    Iterator<AxisHeader> axis = getAxis().iterator();
    AxisHeader a = null;
    Connection con = openConnection(false);
    try {
      while (axis.hasNext()) {
        a = (AxisHeader) axis.next();
        int rootId = a.getRootId();
        tree.indexTree(con, rootId);
      }
    } catch (Exception e) {
      throw new PdcException("PdcBmImpl.indexAllAxis()",
          SilverpeasException.ERROR, "Pdc.INDEXING_AXIS_FAILED", e);
    } finally {
      closeConnection(con);
    }
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
   * @param con
   * @see
   */
  private void closeConnection(Connection con) {
    if (con != null) {
      try {
        con.close();
      } catch (Exception e) {
        SilverTrace.error("Pdc", "PdcBmImpl.closeConnection()",
            "root.EX_CONNECTION_CLOSE_FAILED", "", e);
      }
    }
  }

  /**
   * Method declaration
   * @param con
   * @see
   */
  private Connection openConnection(boolean autoCommit) throws PdcException {
    Connection con = null;
    try {
      con = DBUtil.makeConnection(JNDINames.PDC_DATASOURCE);
      if (autoCommit) {
        con.setAutoCommit(autoCommit);
      }
    } catch (Exception e) {
      throw new PdcException("PdcBmImpl.openConnection()",
          SilverpeasException.ERROR, "root.EX_CONNECTION_OPEN_FAILED", e);
    }
    return con;
  }

  /**
   * Method declaration
   * @param con
   * @see
   */
  private void rollbackConnection(Connection con) {
    if (con != null) {
      try {
        con.rollback();
      } catch (Exception e) {
        SilverTrace.error("Pdc", "PdcBmImpl.rollbackConnection()",
            "root.EX_CONNECTION_CLOSE_FAILED", "", e);
      }
    }
  }

  /**
   * Method declaration
   * @param con
   * @see
   */
  private void commitConnection(Connection con) {
    if (con != null) {
      try {
        con.commit();
      } catch (Exception e) {
        SilverTrace.error("Pdc", "PdcBmImpl.commitConnection()",
            "root.EX_CONNECTION_CLOSE_FAILED", "", e);
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
  public String getCallParameters(String sComponentId, String sSilverContentId) {
    return "ComponentId=" + sComponentId + "&SilverObjectId="
        + sSilverContentId;
  }

  /** Remove all the positions of the given content */
  public List removePosition(Connection connection, int nSilverContentId)
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
              (com.stratelia.silverpeas.classifyEngine.Value) alValues
                  .get(nI);
          if (value.getAxisId() != -1 && value.getValue() != null)
            searchContext.addCriteria(new SearchCriteria(value.getAxisId(),
                value.getValue()));
        }
      }

      return searchContext;
    } catch (Exception e) {
      throw new ContainerManagerException(
          "PdcBmImpl.getSilverContentIdPositions", SilverpeasException.ERROR,
          "containerManager.EX_INTERFACE_FIND_FUNCTIONS", e);
    }
  }

  public List<Integer> findSilverContentIdByPosition(
      ContainerPositionInterface containerPosition, List alComponentId,
      String authorId, String afterDate, String beforeDate)
      throws ContainerManagerException {
    return findSilverContentIdByPosition(containerPosition, alComponentId,
        authorId, afterDate, beforeDate, true, true);
  }

  /** Find all the SilverContentId with the given position */
  public List<Integer> findSilverContentIdByPosition(
      ContainerPositionInterface containerPosition, List<String> alComponentId,
      String authorId, String afterDate, String beforeDate,
      boolean recursiveSearch, boolean visibilitySensitive)
      throws ContainerManagerException {
    try {
      // Get the objects
      List<Integer> alSilverContentId = pdcClassifyBm.findSilverContentIdByPosition(
          containerPosition, alComponentId, authorId, afterDate, beforeDate,
          recursiveSearch, visibilitySensitive);

      return alSilverContentId;
    } catch (Exception e) {
      throw new ContainerManagerException(
          "PdcBmImpl.findSilverContentIdByPosition", SilverpeasException.ERROR,
          "containerManager.EX_INTERFACE_FIND_FUNCTIONS", e);
    }
  }

  public List<Integer> findSilverContentIdByPosition(
      ContainerPositionInterface containerPosition, List alComponentId)
      throws ContainerManagerException {
    return findSilverContentIdByPosition(containerPosition, alComponentId,
        true, true);
  }

  public List<Integer> findSilverContentIdByPosition(
      ContainerPositionInterface containerPosition, List<String> alComponentId,
      boolean recursiveSearch, boolean visibilitySensitive)
      throws ContainerManagerException {
    return findSilverContentIdByPosition(containerPosition, alComponentId,
        null, null, null, recursiveSearch, visibilitySensitive);
  }

}
