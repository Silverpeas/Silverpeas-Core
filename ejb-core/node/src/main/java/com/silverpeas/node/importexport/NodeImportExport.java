/**
 * Copyright (C) 2000 - 2012 Silverpeas
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
 * "http://www.silverpeas.org/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.silverpeas.node.importexport;

import com.stratelia.webactiv.util.EJBUtilitaire;
import com.stratelia.webactiv.util.JNDINames;
import com.stratelia.webactiv.util.exception.SilverpeasRuntimeException;
import com.stratelia.webactiv.util.node.control.NodeBm;
import com.stratelia.webactiv.util.node.control.NodeBmHome;
import com.stratelia.webactiv.util.node.model.NodeDetail;
import com.stratelia.webactiv.util.node.model.NodePK;
import com.stratelia.webactiv.util.node.model.NodeRuntimeException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Classe de gestion des node pour le bus d'importExport
 * @author sdevolder
 */
public class NodeImportExport {

  // Variables
  private NodeBm nodeBm;

  // Méthodes
  /**
   * Méthode récupération du chemin de topics menant à un topic donné
   * @param nodePk - le nodePK du topic dont on veut le chemin
   * @return une collection des topic composant le chemin
   */
  public Collection<NodeDetail> getPathOfNode(NodePK nodePk) {
    try {
      return getNodeBm().getPath(nodePk);
    } catch (RemoteException ex) {
      throw new NodeRuntimeException("NodeImportExport.getPathOfNode()",
          SilverpeasRuntimeException.ERROR, "node.GETTING_NODE_PATH_FAILED", ex);
    }
  }

  /**
   * Méthode de récupération de l'arborescence totale des topics d'un liste de composants
   * @param listComponentId - liste des ids des composants dont on veut l'arborescence des topics
   * @return un object NodeTreesType utilisé par le mapping castor du module d ' ImportExport
   */
  public NodeTreesType getTrees(List<String> listComponentId) {

    NodeTreesType nodeTreesType = new NodeTreesType();
    List<NodeTreeType> listNodeTreeType = new ArrayList<NodeTreeType>(listComponentId.size());

    // Parcours de la liste des id des composants dont on veut récupérer
    // l'arborescence de thèmes
    for (String componentId : listComponentId) {
      NodeTreeType nodeTreeType = new NodeTreeType();
      listNodeTreeType.add(nodeTreeType);
      nodeTreeType.setComponentId(componentId);

      NodePK nodePK = new NodePK(NodePK.ROOT_NODE_ID, "useless", componentId);
      try {// Récupérarion du thème racine
        NodeDetail nodeDetail = getNodeBm().getDetail(nodePK);
        nodeTreeType.setNodeDetail(nodeDetail);
        // Récupération de l'arbre des nodes de façon récursive
        nodeDetail.setChildrenDetails(getRecursiveTree(nodePK));
      } catch (RemoteException ex) {
        throw new NodeRuntimeException("NodeImportExport.getNodeBm()",
            SilverpeasRuntimeException.ERROR, "node.GETTING_NODES_BY_FATHER_FAILED", ex);
      }
    }
    nodeTreesType.setListNodeTreeType(listNodeTreeType);
    return nodeTreesType;
  }

  /**
   * Méthode récursive de récupération de l'arbre des fils d'un node
   * @param nodePK - le nodePK du node père dont on cherche les fils
   * @return une Collection des fils du père
   * @throws RemoteException
   */
  private Collection<NodeDetail> getRecursiveTree(NodePK nodePK) throws RemoteException {
    Collection<NodeDetail> listChildrenDetails = getNodeBm().getChildrenDetails(nodePK);
    if (listChildrenDetails != null && !listChildrenDetails.isEmpty()) {
      for (NodeDetail nodeDetail : listChildrenDetails) {
        nodeDetail.setChildrenDetails(getRecursiveTree(nodeDetail.getNodePK()));
      }
    } else {
      listChildrenDetails = null;
    }
    return listChildrenDetails;
  }

  /**
   * @return l'EJB NodeBM
   * @throws ImportExportException
   */
  private synchronized NodeBm getNodeBm() throws NodeRuntimeException {
    if (nodeBm == null) {
      try {
        NodeBmHome kscEjbHome = EJBUtilitaire.getEJBObjectRef(JNDINames.NODEBM_EJBHOME,
            NodeBmHome.class);
        nodeBm = kscEjbHome.create();
      } catch (Exception e) {
        throw new NodeRuntimeException("NodeImportExport.getNodeBm()",
            SilverpeasRuntimeException.ERROR, "root.EX_CANT_GET_REMOTE_OBJECT", e);
      }
    }
    return nodeBm;
  }
}