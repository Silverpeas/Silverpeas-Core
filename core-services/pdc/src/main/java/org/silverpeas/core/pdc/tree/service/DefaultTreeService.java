/*
 * Copyright (C) 2000 - 2024 Silverpeas
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
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package org.silverpeas.core.pdc.tree.service;

import org.silverpeas.core.annotation.Service;
import org.silverpeas.core.i18n.I18NHelper;
import org.silverpeas.core.index.indexing.model.FullIndexEntry;
import org.silverpeas.core.index.indexing.model.IndexEngineProxy;
import org.silverpeas.core.index.indexing.model.IndexEntryKey;
import org.silverpeas.core.pdc.tree.model.*;
import org.silverpeas.core.persistence.jdbc.bean.BeanCriteria;
import org.silverpeas.core.persistence.jdbc.bean.PersistenceException;
import org.silverpeas.core.persistence.jdbc.bean.SilverpeasBeanDAO;
import org.silverpeas.core.persistence.jdbc.bean.SilverpeasBeanDAOFactory;
import org.silverpeas.core.util.DateUtil;
import org.silverpeas.core.util.file.FileServerUtils;
import org.silverpeas.kernel.SilverpeasRuntimeException;
import org.silverpeas.kernel.logging.SilverLogger;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.*;

import static org.silverpeas.core.persistence.jdbc.bean.BeanCriteria.OPERATOR.*;

@Service
@Singleton
@SuppressWarnings("deprecation")
public class DefaultTreeService implements TreeService {

  private static final String TREE_ID = "treeId";
  private static final String ORDER_NUMBER = "orderNumber";
  @Inject
  private TreeI18NDAO treeI18NDAO;

  protected DefaultTreeService() {

  }

  @Override
  public TreeNode getRoot(Connection con, String treeId)
      throws TreeManagerException {

    TreeNode root = null;
    try {
      List<TreeNodePersistence> roots =
          (List<TreeNodePersistence>) getDAO().findBy(
              BeanCriteria.addCriterion(TREE_ID, Integer.parseInt(treeId))
              .and("levelNumber", 0));
      if (!roots.isEmpty()) {

        TreeNodePersistence rootPers = roots.get(0);
        root = new TreeNode(rootPers);

        setTranslations(con, root);
      }
    } catch (Exception e) {
      throw new TreeManagerException(e);
    }
    return root;
  }

  public String createRoot(Connection con, TreeNode root)
      throws TreeManagerException {

    root.setLevelNumber(0);
    root.setPath("/");
    root.setFatherId("-1");
    root.setOrderNumber(0);
    String treeId;
    try {
      treeId = TreeDAO.createRoot(con, root);
      root.setTreeId(treeId);
      root.setPK(new TreeNodePK("0"));

      createIndex(con, root);
    } catch (Exception e) {
      throw new TreeManagerException(e);
    }

    return treeId;
  }

  public void updateNode(Connection con, TreeNode node)
      throws TreeManagerException {
    final String nodeId = node.getPK().getId();
    final String treeId = node.getTreeId();
    final int order = node.getOrderNumber();
    // recupere les noeuds freres ordonnés qui ont un numéro d'ordre >= à
    // celui
    // du noeud à modifier
    try {
      int treeIdAsInt = Integer.parseInt(treeId);
      BeanCriteria criteria = BeanCriteria.addCriterion(TREE_ID, treeIdAsInt)
          .and(ORDER_NUMBER, GREATER_OR_EQUAL, order)
          .andSubQuery("path", EQUALS, "path FROM SB_Tree_Tree",
              BeanCriteria.addCriterion(TREE_ID, treeIdAsInt).and("id", Integer.parseInt(nodeId)));
      criteria.setAscOrderBy(ORDER_NUMBER);
      Collection<TreeNodePersistence> nodesToUpdate = getDAO().findBy(con,
          criteria);
      boolean nodeHasMoved = true;
      final Iterator<TreeNodePersistence> it = nodesToUpdate.iterator();
      if (it.hasNext()) {
        final TreeNodePersistence firstNode = it.next();
        if (firstNode.getPK().getId().equals(nodeId)) {
          nodeHasMoved = false;
        }
      } else {
        nodeHasMoved = false;
      }

      TreeNode oldNode = getNode(con, node.getPK(), treeId);
      if (node.isRemoveTranslation()) {
        applyTranslationDeletion(con, treeId, oldNode, node, true);
      } else {
        applyTranslationModification(con, treeId, oldNode, node, true);
      }

      createIndex(con, node);

      // Le noeud a changé de place, on décale les noeuds dont l'ordre est
      // supérieur ou égal
      if (nodeHasMoved) {
        shiftNodes(con, nodeId, nodesToUpdate);
      }
    } catch (Exception e) {
      throw new TreeManagerException(e);
    }

    TreeCache.unvalidateTree(treeId);
  }

  private void shiftNodes(final Connection con, final String nodeId,
      final Collection<TreeNodePersistence> nodesToUpdate) {
    for (final TreeNodePersistence node: nodesToUpdate) {
      if (!node.getPK().getId().equals(nodeId)) {
        node.setOrderNumber(node.getOrderNumber() + 1);
        TreeNode treeNode = new TreeNode(node);
        try {
          TreeDAO.updateNode(con, treeNode);
        } catch (SQLException e) {
          throw new SilverpeasRuntimeException(e);
        }
      }
    }
  }

  public void updateRoot(Connection con, TreeNode node) throws TreeManagerException {
    try {
      final String treeId = node.getTreeId();
      final TreeNode oldRoot = getRoot(con, treeId);
      // gestion des traductions
      if (node.isRemoveTranslation()) {
        applyTranslationDeletion(con, treeId, oldRoot, node, false);
      } else {
        applyTranslationModification(con, treeId, oldRoot, node, false);
      }

      // Modifie le noeud
      createIndex(con, node);
    } catch (Exception e) {
      throw new TreeManagerException(e);
    }

    TreeCache.unvalidateTree(node.getTreeId());
  }

  private void setDefaultLanguage(final TreeNode oldRoot) {
    if (oldRoot.getLanguage() == null) {
      // translation for the first time
      oldRoot.setLanguage(I18NHelper.DEFAULT_LANGUAGE);
    }
  }

  private void applyTranslationModification(final Connection con, final String treeId,
      final TreeNode oldNode, final TreeNode node, final boolean withIdSetting) throws SQLException {
    if (node.getLanguage() != null) {
      setDefaultLanguage(oldNode);
      if (!node.getLanguage().equalsIgnoreCase(oldNode.getLanguage())) {
        TreeNodeI18N newNode = new TreeNodeI18N(node.getPK().getId(), node.getLanguage(),
            node.getName(), node.getDescription());
        String translationId = node.getTranslationId();
        if (translationId != null && !translationId.equals("-1")) {
          // update translation
          if (withIdSetting) {
            newNode.setId(node.getTranslationId());
            treeI18NDAO.updateTranslation(con, newNode);
          } else {
            treeI18NDAO.updateTranslation(con, treeId, newNode);
          }
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

  private void applyTranslationDeletion(final Connection con, final String treeId,
      final TreeNode oldNode, final TreeNode node, boolean byId) throws SQLException {
    setDefaultLanguage(oldNode);
    if (oldNode.getLanguage().equalsIgnoreCase(node.getLanguage())) {
      List<TreeNodeI18N> translations = treeI18NDAO.getTranslations(con,
          node.getTreeId(), node.getPK().getId());

      if (translations != null && !translations.isEmpty()) {
        TreeNodeI18N translation = translations.get(0);

        node.setLanguage(translation.getLanguage());
        node.setName(translation.getName());
        node.setDescription(translation.getDescription());

        TreeDAO.updateNode(con, node);

        treeI18NDAO.deleteTranslation(con, translation.getId());
      }
    } else {
      if (byId) {
        treeI18NDAO.deleteTranslation(con, node.getTranslationId());
      } else {
        treeI18NDAO.deleteTranslation(con, treeId, node.getPK().getId(), node.getLanguage());
      }
    }
  }

  public void deleteSubTree(Connection con, TreeNodePK rootPK, String treeId)
      throws TreeManagerException {
    List<TreeNode> subTree = getSubTree(con, rootPK, treeId);
    String rootId = rootPK.getId();
    TreeNode node = getNode(con, rootPK, treeId);

    // Remove all nodes under the rootId
    BeanCriteria criteria = BeanCriteria.addCriterion(TREE_ID, Integer.parseInt(treeId))
        .and(BeanCriteria
            .addCriterion("path", LIKE, node.getPath() + rootId + "/%")
              .or("id", Integer.parseInt(rootId)));
    try {
      getDAO().removeBy(criteria);

      // Remove all index of nodes under the rootId
      for (TreeNode nodeToDelete : subTree) {
        // remove node translations
        treeI18NDAO.deleteNodeTranslations(con, nodeToDelete.getPK().getId(),
            treeId);

        // remove node index
        deleteIndex(nodeToDelete.getPK(), nodeToDelete.getTreeId());
      }
    } catch (Exception e) {
      throw new TreeManagerException(e);
    }

    try {
      // Delete the node and its index
      TreeDAO.deleteNode(con, rootPK, treeId);
      deleteIndex(rootPK, treeId);
    } catch (Exception e) {
      throw new TreeManagerException(e);
    }
    TreeCache.unvalidateTree(treeId);
  }

  public void deleteTree(Connection con, String treeId)
      throws TreeManagerException {


    List<TreeNode> tree = getTree(con, treeId);

    try {
      BeanCriteria criteria = BeanCriteria.addCriterion(TREE_ID, Integer.parseInt(treeId));
      getDAO().removeBy(criteria);

      // remove translations
      treeI18NDAO.deleteTreeTranslations(con, treeId);
    } catch (Exception e) {
      throw new TreeManagerException(e);
    }

    // Remove all index of nodes of the tree
    for (TreeNode nodeToDelete : tree) {
      deleteIndex(nodeToDelete.getPK(), treeId);
    }

    TreeCache.unvalidateTree(treeId);
  }

  public List<TreeNode> getTree(Connection con, String treeId) throws TreeManagerException {
    List<TreeNode> sortedList = TreeCache.getTree(treeId);
    if (sortedList == null) {
      sortedList = new ArrayList<>();
      final TreeNode root = getRoot(con, treeId);
      if (root != null) {
        List<TreeNodePersistence> list = getDescendants(con, root);
        for (int i = 0; i < list.size(); i++) {
          final TreeNodePersistence nodePers = list.get(i);
          final TreeNode node = new TreeNode(nodePers);
          setTranslations(con, node);
          final int position;
          if (i == 0) {
            position = 0;
          } else {
            position = whereInsertNodeToCorrectPlaceInList(sortedList, node);
          }
          sortedList.add(position, node);
        }
      }
      TreeCache.cacheTree(treeId, sortedList);
    }
    return sortedList;
  }

  private void setTranslations(Connection con, TreeNode node)
      throws TreeManagerException {
      // ajout de la traduction par defaut
    TreeNodeI18N translation = new TreeNodeI18N(node.getPK().getId(), node.getLanguage(),
        node.getName(), node.getDescription());
    node.addTranslation(translation);
    if (I18NHelper.isI18nContentActivated) {
      // ajout des autres traductions
      List<TreeNodeI18N> translations;
      try {
        translations = treeI18NDAO.getTranslations(con, node.getTreeId(), node
            .getPK().getId());
      } catch (SQLException e) {
        throw new TreeManagerException(e);
      }
      for (int t = 0; translations != null && t < translations.size(); t++) {
        TreeNodeI18N tr = translations.get(t);
        node.addTranslation(tr);
      }
    }
  }

  public List<TreeNode> getSubTree(Connection con, TreeNodePK rootPK, String treeId)
      throws TreeManagerException {
    TreeNode root = getNode(con, rootPK, treeId);
    List<TreeNodePersistence> list = getDescendants(con, root);

    // 1 - On parcours la liste list
    // pour chaque élément on le place correctement dans la liste ordonnée
    ArrayList<TreeNode> sortedList = new ArrayList<>();
    if (list != null && !list.isEmpty()) {
      // Premier élément de la liste est l'élément racine
      TreeNodePersistence rootPers = list.get(0);
      root = new TreeNode(rootPers);

      // get Translations from DB
      setTranslations(con, root);

      // On l'insére dans la liste en première position
      sortedList.add(root);

      TreeNode node;
      int position;
      // On parcours le reste de la liste
      for (int i = 1; i < list.size(); i++) {
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
    if (i != sortedList.size()) {
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
    }
    return i;
  }

  private List<TreeNodePersistence> getDescendants(Connection con, TreeNode root)
      throws TreeManagerException {
    int rootId = Integer.parseInt(root.getPK().getId());
    int treeId = Integer.parseInt(root.getTreeId());
    String path = root.getPath();
    BeanCriteria criteria = BeanCriteria.addCriterion(TREE_ID, treeId)
        .and(BeanCriteria.addCriterion("path", LIKE, path + rootId + "/%")
            .or("id", rootId));
    criteria.setAscOrderBy("path, orderNumber");
    try {
      return (List<TreeNodePersistence>) getDAO().findBy(con, criteria);
    } catch (PersistenceException pe) {
      throw new TreeManagerException(pe);
    }
  }

  public TreeNode getNode(Connection con, TreeNodePK nodePK, String treeId)
      throws TreeManagerException {
    TreeNode node = null;
    try {
      BeanCriteria criteria = BeanCriteria.addCriterion(TREE_ID, Integer.parseInt(treeId))
          .and("id", Integer.parseInt(nodePK.getId()));
      List<TreeNodePersistence> nodes =
          (List<TreeNodePersistence>) getDAO().findBy(con, criteria);
      if (!nodes.isEmpty()) {
        TreeNodePersistence tnp = nodes.get(0);
        node = new TreeNode(tnp);

        // récupération des autres traductions
        setTranslations(con, node);
      }
    } catch (PersistenceException pe) {
      throw new TreeManagerException(pe);
    }
    return node;
  }

  private String encode(String name) {
    StringBuilder str = new StringBuilder();

    for (int i = 0; i < name.length(); i++) {
      if (name.charAt(i) == '\'') {
        str.append("''");
      } else {
        str.append(name.charAt(i));
      }
    }
    return str.toString();
  }

  public List<TreeNode> getNodesByName(Connection con, String nodeName)
      throws TreeManagerException {

    List<TreeNodePersistence> nodes;
    List<TreeNode> result;
    try {
      String nameEncode = encode(nodeName);
      String nameNoAccent = FileServerUtils.replaceAccentChars(nameEncode);
      //search brut and case insensitive + search without accent and case insensitive
      BeanCriteria criteria = BeanCriteria.emptyCriteria()
          .andWithFunction("LOWER(name)", nameEncode, "LOWER")
          .orWithFunction("LOWER(name)", nameNoAccent, "LOWER");
      nodes = (List<TreeNodePersistence>) getDAO().findBy(con, criteria);
      result = persistence2TreeNode(con, nodes);
    } catch (PersistenceException pe) {
      throw new TreeManagerException(pe);
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
    TreeNodePK newFatherPK;
    try {
      newFatherPK = TreeDAO.createNode(con, nodeToInsert);
      nodeToInsert.setPK(newFatherPK);
      createIndex(con, nodeToInsert);
    } catch (Exception e) {
      throw new TreeManagerException(e);
    }

    // Ajouter 1 au niveau du fils également, modifier le père du fils en P2
    refNode.setFatherId(newFatherPK.getId());
    String newPath = refNode.getPath() + newFatherPK.getId() + '/';

    // refNode.setPath(newPath); //Ici ??????
    updateNode(con, refNode);


    // Modifier le chemin de tous les descendants en insérant le nouveau père
    // juste un remplacement de sous chaine
    // Ajouter 1 au niveau des descendants
    List<TreeNodePersistence> list = getDescendants(con, refNode); // Attention ICI
    if (!list.isEmpty()) {
      String pathToUpdate;
      String endOfPath;
      for (TreeNodePersistence nodeToUpdate : list) {
        // Modifie le niveau et le chemin de chaque descendant
        nodeToUpdate.setLevelNumber(nodeToUpdate.getLevelNumber() + 1);
        pathToUpdate = nodeToUpdate.getPath();
        endOfPath = pathToUpdate.substring(refPath.length());
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
    TreeNode newFatherNode = getNode(con, newFatherPK, treeId);
    List<TreeNodePersistence> list = getDescendants(con, movedNode);

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
    // il a déjà été modifié donc on passe à l'index 1
    if (list.size() > 1) {
      TreeNodePersistence nodeToUpdate;
      String pathToUpdate;

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
    TreeNodePK pk;

    int order = nodeToInsert.getOrderNumber();

    if (order == -1) {
      // the order is not specified. We are going to insert the new node
      // following the alphabetical order
      List<TreeNode> brothers = getSonsToNode(con, father.getPK(), treeId);
      TreeNode brother;
      String brotherName;
      boolean placeFind = false;
      String nodeToInsertName = nodeToInsert.getName();
      int i = 0;
      while (!placeFind && i < brothers.size()) {
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
    BeanCriteria criteria = BeanCriteria.addCriterion(TREE_ID, Integer.parseInt(treeId))
        .and("fatherId", Integer.parseInt(father.getPK().getId()))
        .and(ORDER_NUMBER, GREATER_OR_EQUAL, order);
    criteria.setAscOrderBy(ORDER_NUMBER);
    try {
      // ATTENTION il faut traiter l'ordre des frères
      Collection<TreeNodePersistence> nodesToUpdate = getDAO().findBy(con,
          criteria);

      TreeNode nodeToMove;
      for (TreeNodePersistence tnp : nodesToUpdate) {
        // On modifie l'ordre du noeud en ajoutant 1 par rapport au nouveau
        // noeud
        order++;
        tnp.setOrderNumber(order);
        nodeToMove = new TreeNode(tnp);
        TreeDAO.updateNode(con, nodeToMove);
      }
    } catch (Exception e) {
      throw new TreeManagerException(e);
    }

    try {
      nodeToInsert.setTreeId(treeId);
      pk = TreeDAO.createNode(con, nodeToInsert);
      nodeToInsert.setPK(pk);
      createIndex(con, nodeToInsert);
    } catch (Exception e) {
      throw new TreeManagerException(e);
    }
    TreeCache.unvalidateTree(treeId);
    return pk.getId();
  }

  public List<TreeNode> getSonsToNode(Connection con, TreeNodePK treeNodePK, String treeId)
      throws TreeManagerException {
    Collection<TreeNodePersistence> sons;
    List<TreeNode> result;
    try {
      BeanCriteria criteria = BeanCriteria.addCriterion(TREE_ID, Integer.parseInt(treeId))
          .and("fatherId", Integer.parseInt(treeNodePK.getId()));
      sons = getDAO().findBy(con, criteria);
      result = persistence2TreeNode(con, sons);
    } catch (PersistenceException pe) {
      throw new TreeManagerException(pe);
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
      throw new TreeManagerException(se);
    }
    try {
      // Supprime le noeud
      TreeDAO.deleteNode(con, nodePK, treeId);

      // Supprime ses traductions
      treeI18NDAO.deleteNodeTranslations(con, treeId, nodeId);

      // Supprime l'index
      deleteIndex(nodePK, treeId);
    } catch (Exception e) {
      throw new TreeManagerException(e);
    }
    TreeCache.unvalidateTree(treeId);
  }

  public SilverpeasBeanDAO<TreeNodePersistence> getDAO() throws TreeManagerException {
    SilverpeasBeanDAO<TreeNodePersistence> treeDao;
    try {
      treeDao = SilverpeasBeanDAOFactory.getDAO(TreeNodePersistence.class);
    } catch (PersistenceException pe) {
      throw new TreeManagerException(pe);
    }
    return treeDao;
  }

  public List<TreeNode> getFullPath(Connection con, TreeNodePK nodePK, String treeId)
      throws TreeManagerException {
    String path = getPath(con, nodePK, treeId);
    ArrayList<TreeNode> list = new ArrayList<>();
    try {
      // récupère la valeur de la colonne path de la table SB_Tree_Tree
      Set<Integer> ids = new HashSet<>();
      StringTokenizer tokenizer = new StringTokenizer(path, "/");
      while (tokenizer.hasMoreTokens()) {
        ids.add(Integer.parseInt(tokenizer.nextToken()));
      }
      ids.add(Integer.parseInt(nodePK.getId()));
      BeanCriteria criteria = BeanCriteria.addCriterion(TREE_ID, Integer.parseInt(treeId))
          .and("id", ids);
      criteria.setAscOrderBy("levelNumber");
      Collection<TreeNodePersistence> tree = getDAO().findBy(con, criteria);

      list.addAll(persistence2TreeNode(con, tree));
    } catch (Exception e) {
      throw new TreeManagerException(e);
    }

    return list;
  }

  private List<TreeNode> persistence2TreeNode(Connection con,
      Collection<TreeNodePersistence> silverpeasBeans)
      throws TreeManagerException {
    List<TreeNode> nodes = new ArrayList<>();
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
    String path;
    try {
      TreeNode node = getNode(con, nodePK, treeId);
      path = node.getPath();
    } catch (Exception e) {
      throw new TreeManagerException(e);
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
    TreeNode nodeToIndex = getNode(con, node.getPK(), node.getTreeId());

    createIndex(nodeToIndex);
  }

  private void createIndex(TreeNode node) {
    if (node != null) {
      // Index the Node
      final FullIndexEntry indexEntry = new FullIndexEntry(new IndexEntryKey("pdc", "TreeNode",
          node.getPK().getId() + "_" + node.getTreeId()));

      Collection<String> languages = node.getLanguages();
      languages.forEach(l -> {
        TreeNodeI18N translation = node.getTranslations().get(l);

        indexEntry.setTitle(translation.getName(), l);
        indexEntry.setPreview(translation.getDescription(), l);
      });
      try {
        indexEntry.setCreationDate(DateUtil.parse(node.getCreationDate()));
      } catch (ParseException e) {
        SilverLogger.getLogger(this).warn(e);
      }
      indexEntry.setCreationUser(node.getCreatorId());
      IndexEngineProxy.addIndexEntry(indexEntry);
    }
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