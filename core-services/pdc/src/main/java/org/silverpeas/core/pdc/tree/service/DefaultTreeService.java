/*
 * Copyright (C) 2000 - 2016 Silverpeas
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
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.silverpeas.core.pdc.tree.service;

import org.silverpeas.core.i18n.I18NHelper;
import org.silverpeas.core.index.indexing.model.IndexEntryKey;
import org.silverpeas.core.pdc.tree.model.TreeManagerException;
import org.silverpeas.core.pdc.tree.model.TreeNode;
import org.silverpeas.core.pdc.tree.model.TreeNodeI18N;
import org.silverpeas.core.pdc.tree.model.TreeNodePK;
import org.silverpeas.core.pdc.tree.model.TreeNodePersistence;
import org.silverpeas.core.persistence.jdbc.bean.PersistenceException;
import org.silverpeas.core.persistence.jdbc.bean.SilverpeasBeanDAO;
import org.silverpeas.core.persistence.jdbc.bean.SilverpeasBeanDAOFactory;
import org.silverpeas.core.index.search.model.AxisFilter;
import org.silverpeas.core.index.search.model.AxisFilterNode;

import org.silverpeas.core.util.file.FileServerUtils;
import org.silverpeas.core.exception.SilverpeasException;
import org.silverpeas.core.index.indexing.model.FullIndexEntry;
import org.silverpeas.core.index.indexing.model.IndexEngineProxy;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;

@Singleton
public class DefaultTreeService implements TreeService {

  @Inject
  private TreeI18NDAO treeI18NDAO;

  protected DefaultTreeService() {

  }

  @SuppressWarnings("unchecked")
  @Override
  public TreeNode getRoot(Connection con, String treeId)
      throws TreeManagerException {

    String whereClause = "treeId = " + treeId + " and levelNumber = 0";
    TreeNode root = null;
    try {
      List<TreeNodePersistence> roots =
          (List<TreeNodePersistence>) getDAO().findByWhereClause(new TreeNodePK("useless"),
          whereClause);
      if (roots.size() > 0) {

        TreeNodePersistence rootPers = roots.get(0);
        root = new TreeNode(rootPers);

        setTranslations(con, root);
      }
    } catch (Exception e) {
      throw new TreeManagerException("TreeBmImpl.deleteTree()",
          SilverpeasException.ERROR, "treeManager.DELETING_TREE_FAILED", e);
    }
    return root;
  }

  public String createRoot(Connection con, TreeNode root)
      throws TreeManagerException {

    root.setLevelNumber(0);
    root.setPath("/");
    root.setFatherId("-1");
    root.setOrderNumber(0);
    String treeId = null;
    try {
      treeId = TreeDAO.createRoot(con, root);
      root.setTreeId(treeId);
      root.setPK(new TreeNodePK("0"));

      // createIndex(root);
      createIndex(con, root);
    } catch (Exception e) {
      throw new TreeManagerException("TreeBmImpl.createRoot()",
          SilverpeasException.ERROR, "treeManager.CREATING_ROOT_FAILED", e);
    }

    return treeId;
  }

  public void updateNode(Connection con, TreeNode node)
      throws TreeManagerException {

    String nodeId = node.getPK().getId();
    String treeId = node.getTreeId();
    int order = node.getOrderNumber();
    // recupere les noeuds freres ordonnés qui ont un numéro d'ordre >= à
    // celui
    // du noeud à modifier
    String whereClause = "path = (SELECT path FROM SB_Tree_Tree WHERE treeId = "
        + treeId
        + " and id = "
        + nodeId
        + ") and treeId = "
        + treeId
        + " and orderNumber >= " + order + " ORDER BY orderNumber ASC";

    try {
      @SuppressWarnings("unchecked")
      Collection<TreeNodePersistence> nodesToUpdate = getDAO().findByWhereClause(con, node.getPK(),
          whereClause);
      boolean nodeHasMoved = true;
      Iterator<TreeNodePersistence> it = null;
      if (nodesToUpdate.size() > 0) {
        it = nodesToUpdate.iterator();
        TreeNodePersistence firstNode = null;
        if (it.hasNext()) {
          // Test si le noeud n'a pas changé de place
          firstNode = it.next();
          if (firstNode.getPK().getId().equals(nodeId))
            nodeHasMoved = false;
        }
      } else {
        nodeHasMoved = false;
      }



      TreeNode oldNode = getNode(con, (TreeNodePK) node.getPK(), treeId);
      // gestion des traductions
      if (node.isRemoveTranslation()) {
        if (oldNode.getLanguage() == null) {
          // translation for the first time
          oldNode.setLanguage(I18NHelper.defaultLanguage);
        }
        if (oldNode.getLanguage().equalsIgnoreCase(node.getLanguage())) {
          List<TreeNodeI18N> translations = treeI18NDAO.getTranslations(con,
              node.getTreeId(), node.getPK().getId());

          if (translations != null && translations.size() > 0) {
            TreeNodeI18N translation = translations.get(0);

            node.setLanguage(translation.getLanguage());
            node.setName(translation.getName());
            node.setDescription(translation.getDescription());

            TreeDAO.updateNode(con, node);

            treeI18NDAO.deleteTranslation(con, translation.getId());
          }
        } else {
          treeI18NDAO.deleteTranslation(con, Integer.parseInt(node
              .getTranslationId()));
        }
      } else {
        if (node.getLanguage() != null) {
          if (oldNode.getLanguage() == null) {
            // translation for the first time
            oldNode.setLanguage(I18NHelper.defaultLanguage);
          }
          if (!node.getLanguage().equalsIgnoreCase(oldNode.getLanguage())) {
            TreeNodeI18N newNode = new TreeNodeI18N(Integer.parseInt(node
                .getPK().getId()), node.getLanguage(), node.getName(), node
                .getDescription());
            String translationId = node.getTranslationId();
            if (translationId != null && !translationId.equals("-1")) {
              // update translation
              newNode.setId(Integer.parseInt(node.getTranslationId()));

              treeI18NDAO.updateTranslation(con, newNode);
            } else {
              treeI18NDAO.createTranslation(con, newNode, treeId);
            }

            node.setLanguage(oldNode.getLanguage());
            node.setName(oldNode.getName());
            node.setDescription(oldNode.getDescription());
          }
        }

        TreeDAO.updateNode(con, node);
      }

      // Modifie le noeud
      // TreeDAO.updateNode(con, node);

      // createIndex(node);
      createIndex(con, node);



      // Le noeud a changé de place, on décale les noeuds dont l'ordre est
      // supérieur ou égal
      if (nodeHasMoved) {
        it = nodesToUpdate.iterator();
        TreeNodePersistence nodeToMove = null;
        while (it.hasNext()) {
          nodeToMove = it.next();
          if (!nodeToMove.getPK().getId().equals(nodeId)) {
            // On modifie l'ordre du noeud en l'incrémentant de 1
            nodeToMove.setOrderNumber(nodeToMove.getOrderNumber() + 1);
            TreeNode treeNode = new TreeNode(nodeToMove);
            TreeDAO.updateNode(con, treeNode);
          }
        }
      }
    } catch (Exception e) {
      throw new TreeManagerException("TreeBmImpl.updateNode()",
          SilverpeasException.ERROR, "treeManager.UPDATING_NODE_FAILED", e);
    }

    TreeCache.unvalidateTree(treeId);
  }

  public void updateRoot(Connection con, TreeNode node)
      throws TreeManagerException {

    try {
      String treeId = node.getTreeId();
      TreeNode oldRoot = getRoot(con, treeId);
      // gestion des traductions
      if (node.isRemoveTranslation()) {
        if (oldRoot.getLanguage() == null) {
          // translation for the first time
          oldRoot.setLanguage(I18NHelper.defaultLanguage);
        }
        if (oldRoot.getLanguage().equalsIgnoreCase(node.getLanguage())) {
          List<TreeNodeI18N> translations = treeI18NDAO.getTranslations(con,
              node.getTreeId(), node.getPK().getId());

          if (translations != null && translations.size() > 0) {
            TreeNodeI18N translation = translations.get(0);

            node.setLanguage(translation.getLanguage());
            node.setName(translation.getName());
            node.setDescription(translation.getDescription());

            TreeDAO.updateNode(con, node);

            treeI18NDAO.deleteTranslation(con, translation.getId());
          }
        } else {
          // treeI18NDAO.deleteTranslation(con,
          // Integer.parseInt(node.getTranslationId()));
          treeI18NDAO.deleteTranslation(con, treeId, node.getPK().getId(), node
              .getLanguage());
        }
      } else {
        if (node.getLanguage() != null) {
          if (oldRoot.getLanguage() == null) {
            // translation for the first time
            oldRoot.setLanguage(I18NHelper.defaultLanguage);
          }
          if (!node.getLanguage().equalsIgnoreCase(oldRoot.getLanguage())) {
            TreeNodeI18N newNode = new TreeNodeI18N(Integer.parseInt(node
                .getPK().getId()), node.getLanguage(), node.getName(), node
                .getDescription());
            String translationId = node.getTranslationId();
            if (translationId != null && !translationId.equals("-1")) {
              // update translation
              // newNode.setId(Integer.parseInt(node.getTranslationId()));

              treeI18NDAO.updateTranslation(con, treeId, newNode);
            } else {
              treeI18NDAO.createTranslation(con, newNode, treeId);
            }

            node.setLanguage(oldRoot.getLanguage());
            node.setName(oldRoot.getName());
            node.setDescription(oldRoot.getDescription());
          }
        }

        TreeDAO.updateNode(con, node);
      }

      // Modifie le noeud
      // TreeDAO.updateNode(con, node);
      createIndex(con, node);
    } catch (Exception e) {
      throw new TreeManagerException("TreeBmImpl.updateRoot()",
          SilverpeasException.ERROR, "treeManager.UPDATING_ROOT_FAILED", e);
    }

    TreeCache.unvalidateTree(node.getTreeId());
  }

  public void deleteSubTree(Connection con, TreeNodePK rootPK, String treeId)
      throws TreeManagerException {
    List<TreeNode> subTree = getSubTree(con, rootPK, treeId);
    String rootId = rootPK.getId();
    TreeNode node = getNode(con, rootPK, treeId);

    // Remove all nodes under the rootId
    String whereClause = "treeId = " + treeId + " and (path LIKE '"
        + node.getPath() + rootId + "/%' or id = " + rootId + ")";
    try {
      getDAO().removeWhere(rootPK, whereClause);

      // Remove all index of nodes under the rootId
      for (TreeNode nodeToDelete : subTree) {
        // remove node translations
        treeI18NDAO.deleteNodeTranslations(con, nodeToDelete.getPK().getId(),
            treeId);

        // remove node index
        deleteIndex((TreeNodePK) nodeToDelete.getPK(), nodeToDelete.getTreeId());
      }
    } catch (Exception e) {
      throw new TreeManagerException("TreeBmImpl.deleteSubTree()",
          SilverpeasException.ERROR, "treeManager.DELETING_TREE_FAILED", e);
    }

    try {
      // Delete the node and its index
      TreeDAO.deleteNode(con, rootPK, treeId);
      deleteIndex(rootPK, treeId);
    } catch (Exception e) {
      throw new TreeManagerException("TreeBmImpl.deleteTree()",
          SilverpeasException.ERROR, "treeManager.DELETING_NODE_FAILED", e);
    }
    TreeCache.unvalidateTree(treeId);
  }

  public void deleteTree(Connection con, String treeId)
      throws TreeManagerException {


    List<TreeNode> tree = getTree(con, treeId);

    String whereClause = "treeId = " + treeId;
    try {
      getDAO().removeWhere(new TreeNodePK("useless"), whereClause);

      // remove translations
      treeI18NDAO.deleteTreeTranslations(con, treeId);
    } catch (Exception e) {
      throw new TreeManagerException("TreeBmImpl.deleteTree()",
          SilverpeasException.ERROR, "treeManager.DELETING_TREE_FAILED", e);
    }

    // Remove all index of nodes of the tree
    for (TreeNode nodeToDelete : tree) {
      deleteIndex((TreeNodePK) nodeToDelete.getPK(), treeId);
    }

    TreeCache.unvalidateTree(treeId);
  }

  public List<TreeNode> getTree(Connection con, String treeId)
      throws TreeManagerException {

    return getTree(con, treeId, new AxisFilter());
  }

  public List<TreeNode> getTree(Connection con, String treeId, AxisFilter filter)
      throws TreeManagerException {
    List<TreeNode> sortedList = null;
    if (filter.size() == 0) {
      sortedList = TreeCache.getTree(treeId);
    }

    if (sortedList == null) {
      TreeNode root = null;
      sortedList = new ArrayList<TreeNode>();
      root = getRoot(con, treeId);
      if (root != null) {

        List<TreeNodePersistence> list = getDescendants(con, root, filter);

        // 1 - On parcours la liste list
        // pour chaque élément on le place correctement dans la liste
        // ordonnée
        if (list != null && list.size() > 0) {
          TreeNode node = null;
          int position = -1;
          // On parcours le reste de la liste
          boolean first = true;
          for (TreeNodePersistence nodePers : list) {
            node = new TreeNode(nodePers);
            setTranslations(con, node);
            if (first) {
              // Premier élément de la liste est l'élément racine
              // On l'insére dans la liste en première position
              position = 0;
              first = false;
            } else {
              position = whereInsertNodeToCorrectPlaceInList(sortedList, node);
            }
            sortedList.add(position, node);
          }
        }
      }
      if (filter.size() == 0) {
        TreeCache.cacheTree(treeId, sortedList);
      }
    }
    return sortedList;
  }

  private void setTranslations(Connection con, TreeNode node)
      throws TreeManagerException {
    if (I18NHelper.isI18nContentActivated) {
      // ajout de la traduction par defaut
      TreeNodeI18N translation = new TreeNodeI18N(Integer.parseInt(node.getPK()
          .getId()), node.getLanguage(), node.getName(), node.getDescription());
      node.addTranslation(translation);

      // ajout des autres traductions
      List<TreeNodeI18N> translations = null;
      try {
        translations = treeI18NDAO.getTranslations(con, node.getTreeId(), node
            .getPK().getId());
      } catch (SQLException e) {
        throw new TreeManagerException("TreeBmImpl.setTranslations()",
            SilverpeasException.ERROR,
            "treeManager.GETTING_TRANSLATIONS_FAILED", e);
      }
      for (int t = 0; translations != null && t < translations.size(); t++) {
        TreeNodeI18N tr = translations.get(t);
        node.addTranslation(tr);
      }
    }
  }

  public List<TreeNode> getSubTree(Connection con, TreeNodePK rootPK, String treeId)
      throws TreeManagerException {
    TreeNode root = null;
    root = getNode(con, rootPK, treeId);
    List<TreeNodePersistence> list = getDescendants(con, root);

    // 1 - On parcours la liste list
    // pour chaque élément on le place correctement dans la liste ordonnée
    ArrayList<TreeNode> sortedList = new ArrayList<TreeNode>();
    if (list != null && list.size() > 0) {
      // Premier élément de la liste est l'élément racine
      // root = (TreeNode) list.get(0);

      TreeNodePersistence rootPers = list.get(0);
      root = new TreeNode(rootPers);

      // get Translations from DB
      setTranslations(con, root);

      // On l'insére dans la liste en première position
      sortedList.add(root);

      TreeNode node = null;
      int position = -1;
      // On parcours le reste de la liste
      for (int i = 1; i < list.size(); i++) {
        // node = (TreeNode) list.get(i);
        TreeNodePersistence nodePers = list.get(i);
        node = new TreeNode(nodePers);

        // get Translations from DB
        setTranslations(con, node);

        position = whereInsertNodeToCorrectPlaceInList(sortedList, node);
        sortedList.add(position, node);
      }
    }
    return sortedList;
  }

  private int whereInsertNodeToCorrectPlaceInList(List<TreeNode> sortedList,
      TreeNode nodeToInsert) {
    int order = nodeToInsert.getOrderNumber();
    String fatherId = nodeToInsert.getFatherId();

    int i = 0;
    // recherche le pere
    while (i < sortedList.size()
        && !sortedList.get(i).getPK().getId().equals(fatherId)) {
      i++;
    }
    if (i == sortedList.size()) {
      // On a pas trouvé le père
      return i;
    } else {
      // On a trouvé le père en i
      // On commence la recherche des freres en i + 1
      i = i + 1;
      // On parcours les freres
      while (i < sortedList.size()
          && sortedList.get(i).getFatherId().equals(fatherId)) {
        if (sortedList.get(i).getOrderNumber() >= order) {
          // On place le noeud à insérer ici
          return i;
        }
        i++;
      }
      if (i == sortedList.size()) {
        // on est à la fin de la liste et on a pas trouvé de frere
        return i;
      } else {
        // on a pas trouvé de frere
        return i;
      }
    }
  }

  /**
   * @param con
   * @param root
   * @return a List of TreeNodePersistence
   * @throws TreeManagerException
   */
  private List<TreeNodePersistence> getDescendants(Connection con, TreeNode root)
      throws TreeManagerException {

    return getDescendants(con, root, new AxisFilter());
  }

  /**
   * @param con
   * @param root
   * @return a List of TreeNodePersistence
   * @throws TreeManagerException
   */
  @SuppressWarnings("unchecked")
  private List<TreeNodePersistence> getDescendants(Connection con, TreeNode root, AxisFilter filter)
      throws TreeManagerException {
    String rootId = root.getPK().getId();
    String treeId = root.getTreeId();
    String path = root.getPath();
    // String whereClause =
    // "path LIKE (SELECT path + '%' FROM SB_Tree_Tree WHERE id = "+rootId+") ORDER BY path ASC, orderNumber ASC";
    StringBuilder whereClause = new StringBuilder();
    whereClause.append("treeId = ").append(treeId).append(" and (path LIKE '")
        .append(path).append(rootId).append("/%' or id = ").append(rootId)
        .append(")");

    boolean first_condition = true;
    if (filter.size() > 0) {
      AxisFilterNode condition;
      String property;
      for (int i = 0; i < filter.size(); i++) {
        if (i == 0) {
          condition = filter.getFirstCondition();
        } else {
          condition = filter.getNextCondition();
        }
        property = condition.getPriperty();
        if (AxisFilter.NAME.equals(property)) {
          if (first_condition) {
            whereClause.append(" and (LOWER(name) like LOWER('").append(
                condition.getValue()).append("')");
            first_condition = false;
          } else {
            whereClause.append(" or LOWER(name) like LOWER('").append(
                condition.getValue().toLowerCase()).append("')");
          }
        } else if (AxisFilter.DESCRIPTION.equals(property)) {
          if (first_condition) {
            whereClause.append(" and (LOWER(description) like LOWER('").append(
                condition.getValue().toLowerCase()).append("')");
            first_condition = false;
          } else {
            whereClause.append(" or LOWER(description) like LOWER('").append(
                condition.getValue().toLowerCase()).append("')");
          }
        }
      }
    }

    if (!first_condition) {
      whereClause.append(") ");
    }

    whereClause.append(" ORDER BY path ASC, orderNumber ASC");



    List<TreeNodePersistence> list = null;
    try {
      list = (List<TreeNodePersistence>) getDAO().findByWhereClause(con, root.getPK(),
          whereClause.toString());
    } catch (PersistenceException pe) {
      throw new TreeManagerException("TreeBmImpl.getDescendants()",
          SilverpeasException.ERROR, "treeManager.GETTING_TREE_FAILED", pe);
    }
    return list;
  }

  public TreeNode getNode(Connection con, TreeNodePK nodePK, String treeId)
      throws TreeManagerException {
    TreeNode node = null;
    try {
      String whereClause = "treeId = " + treeId + " and id = " + nodePK.getId();
      @SuppressWarnings("unchecked")
      List<TreeNodePersistence> nodes =
          (List<TreeNodePersistence>) getDAO().findByWhereClause(con, nodePK, whereClause);
      if (nodes.size() > 0) {
        TreeNodePersistence tnp = nodes.get(0);
        node = new TreeNode(tnp);

        // récupération des autres traductions
        setTranslations(con, node);
      }
    } catch (PersistenceException pe) {
      throw new TreeManagerException("TreeBmImpl.getNode()",
          SilverpeasException.ERROR, "treeManager.GETTING_NODE_FAILED", pe);
    }
    return node;
  }

  private String encode(String name) {
    String chaine = "";

    for (int i = 0; i < name.length(); i++) {
      switch (name.charAt(i)) {
        case '\'':
          chaine += "''";
          break;
        default:
          chaine += name.charAt(i);
      }
    }
    return chaine;
  }

  @SuppressWarnings("unchecked")
  public List<TreeNode> getNodesByName(Connection con, String nodeName)
      throws TreeManagerException {

    List<TreeNodePersistence> nodes = null;
    List<TreeNode> result = null;
    try {
      String nameEncode = encode(nodeName);
      String nameNoAccent = FileServerUtils.replaceAccentChars(nameEncode);
      //search brut and case insensitive + search without accent and case insensitive
      String whereClause = "LOWER(name) = LOWER('" + nameEncode + "') OR "+
                            "LOWER(name) = LOWER('" + nameNoAccent + "')";
      nodes =
          (List<TreeNodePersistence>) getDAO().findByWhereClause(con, new TreeNodePK("useless"),
          whereClause);
      result = persistence2TreeNode(con, nodes);
    } catch (PersistenceException pe) {
      throw new TreeManagerException("TreeBmImpl.getNodesByName()",
          SilverpeasException.ERROR,
          "treeManager.GETTING_NODES_BY_NAME_FAILED", pe);
    }
    return result;
  }

  public String insertFatherToNode(Connection con, TreeNode nodeToInsert,
      TreeNodePK refNodePK, String treeId) throws TreeManagerException {
    TreeNode refNode = getNode(con, refNodePK, treeId);

    // Mémoriser le père actuel P1 de la fille
    String refPath = refNode.getPath();



    // Insérer le nouveau père P2 avec la référence sur P1
    nodeToInsert.setTreeId(treeId);
    nodeToInsert.setPath(refNode.getPath());
    nodeToInsert.setLevelNumber(refNode.getLevelNumber());
    nodeToInsert.setFatherId(refNode.getFatherId());
    TreeNodePK newFatherPK = null;
    try {
      newFatherPK = TreeDAO.createNode(con, nodeToInsert);
      nodeToInsert.setPK(newFatherPK);
      // createIndex(nodeToInsert);
      createIndex(con, nodeToInsert);
    } catch (Exception e) {
      throw new TreeManagerException("TreeBmImpl.insertFatherToNode()",
          SilverpeasException.ERROR, "treeManager.INSERTING_FATHER_FAILED", e);
    }

    // Ajouter 1 au niveau du fils également, modifier le père du fils en P2
    // refNode.setLevelNumber(refNode.getLevelNumber()+1);
    refNode.setFatherId(newFatherPK.getId());
    String newPath = refNode.getPath() + newFatherPK.getId() + "/";

    // refNode.setPath(newPath); //Ici ??????
    updateNode(con, refNode);


    // Modifier le chemin de tous les descendants en insérant le nouveau père
    // juste un remplacement de sous chaine
    // Ajouter 1 au niveau des descendants
    List<TreeNodePersistence> list = getDescendants(con, refNode); // Attention ICI
    if (list.size() > 0) {
      String pathToUpdate = "";
      String endOfPath = "";
      for (TreeNodePersistence nodeToUpdate : list) {
        // Modifie le niveau et le chemin de chaque descendant
        nodeToUpdate.setLevelNumber(nodeToUpdate.getLevelNumber() + 1);
        pathToUpdate = nodeToUpdate.getPath();
        endOfPath = pathToUpdate.substring(refPath.length(), pathToUpdate
            .length());
        pathToUpdate = newPath + endOfPath;
        nodeToUpdate.setPath(pathToUpdate);

        updateNode(con, new TreeNode(nodeToUpdate));
      }
    }
    TreeCache.unvalidateTree(treeId);
    return newFatherPK.getId();
  }

  public void moveSubTreeToNewFather(Connection con, TreeNodePK nodeToMovePK,
      TreeNodePK newFatherPK, String treeId, int orderNumber)
      throws TreeManagerException {
    TreeNode movedNode = getNode(con, nodeToMovePK, treeId);
    TreeNode savedNode = movedNode;
    TreeNode newFatherNode = getNode(con, newFatherPK, treeId);
    List<TreeNodePersistence> list = getDescendants(con, savedNode);

    // idée le nouveau niveau = niveauActuel - niveauAncienPere +
    // niveauNouveauPere
    // Mémoriser le niveau du père actuel soit le niveauActuel -1 de la valeur
    // déplacée
    int levelFather = movedNode.getLevelNumber() - 1;
    int levelNewFather = newFatherNode.getLevelNumber();

    // Mémoriser le path du movedMode et on crée la nelle racine
    String oldRacine = movedNode.getPath();
    String newRacine = newFatherNode.getPath() + newFatherPK.getId() + "/";

    // On modifie le movedMode
    movedNode.setPath(newRacine);
    movedNode.setLevelNumber(movedNode.getLevelNumber() - levelFather
        + levelNewFather);
    movedNode.setFatherId(newFatherPK.getId());
    // on le place en début de liste
    movedNode.setOrderNumber(orderNumber);

    updateNode(con, movedNode);

    // Modifier le chemin de tous les descendants en utilisant newRacine,
    // ainsi que le levelnumber

    // Premier élément de la liste est l'élément racine
    // root = (TreeNode) list.get(0);
    // il a déjà été modifié donc on passe à l'index 1
    if (list.size() > 1) {
      TreeNodePersistence nodeToUpdate = null;
      String pathToUpdate = "";

      for (int i = 1; i < list.size(); i++) {
        // Modifie le niveau et le chemin de chaque descendant
        nodeToUpdate = list.get(i);
        nodeToUpdate.setLevelNumber(nodeToUpdate.getLevelNumber() - levelFather
            + levelNewFather);
        pathToUpdate = nodeToUpdate.getPath();
        pathToUpdate = pathToUpdate.replaceFirst(oldRacine, newRacine);
        nodeToUpdate.setPath(pathToUpdate);
        updateNode(con, new TreeNode(nodeToUpdate));
      }
    }
    TreeCache.unvalidateTree(treeId);
  }

  public String createSonToNode(Connection con, TreeNode nodeToInsert,
      TreeNodePK refNode, String treeId) throws TreeManagerException {
    TreeNode father = getNode(con, refNode, treeId);
    nodeToInsert.setLevelNumber(father.getLevelNumber() + 1);
    nodeToInsert.setPath(father.getPath() + father.getPK().getId() + "/");
    nodeToInsert.setFatherId(father.getPK().getId());
    TreeNodePK pk = null;

    int order = nodeToInsert.getOrderNumber();

    if (order == -1) {
      // the order is not specified. We are going to insert the new node
      // following the alphabetical order
      List<TreeNode> brothers = getSonsToNode(con, (TreeNodePK) father
          .getPK(), treeId);
      TreeNode brother = null;
      String brotherName = null;
      boolean placeFind = false;
      String nodeToInsertName = nodeToInsert.getName();
      int i = 0;
      while (placeFind == false && i < brothers.size()) {
        brother = brothers.get(i);
        brotherName = brother.getName();
        if (brotherName.compareTo(nodeToInsertName) >= 0) {
          placeFind = true;
        } else {
          i++;
        }
      }
      order = i;
      nodeToInsert.setOrderNumber(order);
    }

    // recupere les noeuds freres ordonnés qui ont un numéro d'ordre >= à
    // celui
    // du noeud à modifier
    String whereClause = "treeId = " + treeId + " and fatherId = "
        + father.getPK().getId() + " and orderNumber >= " + order
        + " ORDER BY orderNumber ASC";

    try {
      // ATTENTION il faut traiter l'ordre des frères
      @SuppressWarnings("unchecked")
      Collection<TreeNodePersistence> nodesToUpdate = getDAO().findByWhereClause(con,
          father.getPK(), whereClause);

      TreeNode nodeToMove = null;
      for (TreeNodePersistence tnp : nodesToUpdate) {
        // On modifie l'ordre du noeud en ajoutant 1 par rapport au nouveau
        // noeud
        order++;
        tnp.setOrderNumber(order);
        nodeToMove = new TreeNode(tnp);
        TreeDAO.updateNode(con, nodeToMove);
      }
    } catch (Exception e) {
      throw new TreeManagerException("TreeBmImpl.createSonToNode()",
          SilverpeasException.ERROR, "treeManager.UPDATING_NODE_FAILED", e);
    }

    try {
      nodeToInsert.setTreeId(treeId);
      pk = TreeDAO.createNode(con, nodeToInsert);
      nodeToInsert.setPK(pk);
      createIndex(con, nodeToInsert);
    } catch (Exception e) {
      throw new TreeManagerException("TreeBmImpl.createSonToNode()",
          SilverpeasException.ERROR, "treeManager.CREATING_SON_FAILED", e);
    }
    TreeCache.unvalidateTree(treeId);
    return pk.getId();
  }

  @SuppressWarnings("unchecked")
  public List<TreeNode> getSonsToNode(Connection con, TreeNodePK treeNodePK, String treeId)
      throws TreeManagerException {
    String whereClause = "treeId = " + treeId + " and fatherId = "
        + treeNodePK.getId();
    Collection<TreeNodePersistence> sons = null;
    List<TreeNode> result = null;
    try {
      sons = getDAO().findByWhereClause(con, treeNodePK, whereClause);
      result = persistence2TreeNode(con, sons);
    } catch (PersistenceException pe) {
      throw new TreeManagerException("TreeBmImpl.getSonsToNode()",
          SilverpeasException.ERROR, "treeManager.GETTING_SONS_FAILED", pe);
    }
    return result;
  }

  public void deleteNode(Connection con, TreeNodePK nodePK, String treeId)
      throws TreeManagerException {
    String nodeId = nodePK.getId();
    TreeNode nodeToDelete = getNode(con, nodePK, treeId);
    String path = nodeToDelete.getPath();
    String newFather = nodeToDelete.getFatherId();

    try {
      // Change le level de chaque descendants du noeud à supprimer
      TreeDAO.levelUp(con, path + nodeId + "/", treeId);

      // Change le père de chaque fils du noeud à supprimer
      TreeDAO.changeFatherAndPath(con, Integer.parseInt(nodeId),
          Integer.parseInt(newFather), path, treeId);

      // Update du path pour les valeurs descendantes.
      TreeDAO.updatePath(con, nodeId, treeId);
    } catch (SQLException se) {
      throw new TreeManagerException("TreeBmImpl.deleteNode()",
          SilverpeasException.ERROR, "treeManager.DELETING_NODE_FAILED", se);
    }
    try {
      // Supprime le noeud
      TreeDAO.deleteNode(con, nodePK, treeId);

      // Supprime ses traductions
      treeI18NDAO.deleteNodeTranslations(con, treeId, nodeId);

      // Supprime l'index
      deleteIndex(nodePK, treeId);
    } catch (Exception e) {
      throw new TreeManagerException("TreeBmImpl.deleteNode()",
          SilverpeasException.ERROR, "treeManager.DELETING_NODE_FAILED", e);
    }
    TreeCache.unvalidateTree(treeId);
  }

  public SilverpeasBeanDAO getDAO() throws TreeManagerException {
    SilverpeasBeanDAO treeDao = null;
    try {
      treeDao = SilverpeasBeanDAOFactory
          .getDAO("org.silverpeas.core.pdc.tree.model.TreeNodePersistence");
    } catch (PersistenceException pe) {
      throw new TreeManagerException("TreeBmImpl.getDAO()",
          SilverpeasException.ERROR,
          "treeManager.GETTING_SILVERPEASBEANDAO_FAILED", pe);
    }
    return treeDao;
  }

  public List<TreeNode> getFullPath(Connection con, TreeNodePK nodePK, String treeId)
      throws TreeManagerException {
    String path = getPath(con, nodePK, treeId);
    ArrayList<TreeNode> list = new ArrayList<TreeNode>();
    try {
      // récupère la valeur de la colonne path de la table SB_Tree_Tree
      StringTokenizer st = new StringTokenizer(path, "/");
      String whereClause = "treeId = " + treeId + " and (1=0 ";
      while (st.hasMoreTokens()) {
        whereClause += " or id = " + st.nextToken();
      }
      whereClause += " or id = " + nodePK.getId()
          + ") order by levelNumber ASC";

      @SuppressWarnings("unchecked")
      Collection<TreeNodePersistence> tree = getDAO().findByWhereClause(con, nodePK, whereClause);

      list.addAll(persistence2TreeNode(con, tree));
    } catch (Exception e) {
      throw new TreeManagerException("TreeBmImpl.getFullPath()",
          SilverpeasException.ERROR, "treeManager.CREATING_SON_FAILED", e);
    }

    return list;
  }

  private List<TreeNode> persistence2TreeNode(Connection con,
      Collection<TreeNodePersistence> silverpeasBeans)
      throws TreeManagerException {
    List<TreeNode> nodes = new ArrayList<TreeNode>();
    if (silverpeasBeans != null) {
      for (TreeNodePersistence silverpeasBean : silverpeasBeans) {
        TreeNode node = new TreeNode(silverpeasBean);
        nodes.add(node);
        // ajout des traductions :
        setTranslations(con, node);
      }
    }
    return nodes;
  }

  public String getPath(Connection con, TreeNodePK nodePK, String treeId)
      throws TreeManagerException {
    String path = "";
    TreeNode node = null;
    try {
      node = getNode(con, nodePK, treeId);
      path = node.getPath();
    } catch (Exception e) {
      throw new TreeManagerException("TreeBmImpl.getPath()",
          SilverpeasException.ERROR, "treeManager.CREATING_SON_FAILED", e);
    }
    return path;
  }

  public void indexTree(Connection con, int treeId) throws TreeManagerException {
    List<TreeNode> tree = getTree(con, Integer.toString(treeId));
    for (TreeNode node : tree) {
      createIndex(node);
    }
  }

  private void createIndex(Connection con, TreeNode node)
      throws TreeManagerException {
    TreeNode nodeToIndex = getNode(con, (TreeNodePK) node.getPK(), node
        .getTreeId());

    createIndex(nodeToIndex);
  }

  private void createIndex(TreeNode node) {

    FullIndexEntry indexEntry = null;

    if (node != null) {
      // Index the Node
      indexEntry = new FullIndexEntry("pdc", "TreeNode", node.getPK().getId()
          + "_" + node.getTreeId());

      Iterator<String> languages = node.getLanguages();
      while (languages.hasNext()) {
        String language = languages.next();
        TreeNodeI18N translation = (TreeNodeI18N) node.getTranslation(language);

        indexEntry.setTitle(translation.getName(), language);
        indexEntry.setPreview(translation.getDescription(), language);
      }

      indexEntry.setCreationDate(node.getCreationDate());
      indexEntry.setCreationUser(node.getCreatorId());
    }
    IndexEngineProxy.addIndexEntry(indexEntry);
  }

  /**
   * Called on : - removeNode()
   */
  private void deleteIndex(TreeNodePK pk, String treeId) {

    IndexEntryKey indexEntry = new IndexEntryKey("pdc", "TreeNode", pk.getId()
        + "_" + treeId);

    IndexEngineProxy.removeIndexEntry(indexEntry);
  }

}