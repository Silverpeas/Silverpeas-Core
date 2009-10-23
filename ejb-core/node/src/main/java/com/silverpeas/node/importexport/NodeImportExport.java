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
 * FLOSS exception.  You should have recieved a copy of the text describing
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
/*
 * Created on 15 févr. 2005
 */
package com.silverpeas.node.importexport;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import com.stratelia.webactiv.util.EJBUtilitaire;
import com.stratelia.webactiv.util.JNDINames;
import com.stratelia.webactiv.util.exception.SilverpeasRuntimeException;
import com.stratelia.webactiv.util.node.control.NodeBm;
import com.stratelia.webactiv.util.node.control.NodeBmHome;
import com.stratelia.webactiv.util.node.model.NodeDetail;
import com.stratelia.webactiv.util.node.model.NodePK;
import com.stratelia.webactiv.util.node.model.NodeRuntimeException;

/**
 * Classe de gestion des node pour le bus d'importExport
 *
 * @author sdevolder
 */
public class NodeImportExport {

  // Variables
  private NodeBm nodeBm;

  // Méthodes
  /**
   * Méthode récupération du chemin de topics menant à un topic donné
   *
   * @param nodePk
   *          - le nodePK du topic dont on veut le chemin
   * @return une collection des topic composant le chemin
   */
  public Collection getPathOfNode(NodePK nodePk) {
    Collection listNodeDetail = null;
    try {
      listNodeDetail = getNodeBm().getPath(nodePk);
    } catch (RemoteException ex) {
      throw new NodeRuntimeException("NodeImportExport.getPathOfNode()",
          SilverpeasRuntimeException.ERROR, "node.GETTING_NODE_PATH_FAILED", ex);
    }
    return listNodeDetail;
  }

  /**
   * Méthode de récupération de l'arborescence totale des topics d'un liste de
   * composants
   *
   * @param listComponentId
   *          - liste des ids des composants dont on veut l'arborescence des
   *          topics
   * @return un object NodeTreesType utilisé par le mapping castor du module d '
   *         ImportExport
   */
  public NodeTreesType getTrees(List listComponentId) {

    NodeTreesType nodeTreesType = new NodeTreesType();
    ArrayList listNodeTreeType = new ArrayList();

    // Parcours de la liste des id des composants dont on veut récupérer
    // l'arborescence de thèmes
    Iterator itListComponentId = listComponentId.iterator();
    while (itListComponentId.hasNext()) {
      String componentId = (String) itListComponentId.next();
      NodeTreeType nodeTreeType = new NodeTreeType();
      listNodeTreeType.add(nodeTreeType);
      nodeTreeType.setComponentId(componentId);

      NodePK nodePK = new NodePK("0", "useless", componentId);
      try {// Récupérarion du thème racine
        NodeDetail nodeDetail = getNodeBm().getDetail(nodePK);
        nodeTreeType.setNodeDetail(nodeDetail);
        // Récupération de l'arbre des nodes de façon récursive
        nodeDetail.setChildrenDetails(getRecursiveTree(nodePK));
      } catch (RemoteException ex) {
        throw new NodeRuntimeException("NodeImportExport.getNodeBm()",
            SilverpeasRuntimeException.ERROR,
            "node.GETTING_NODES_BY_FATHER_FAILED", ex);
      }
    }
    nodeTreesType.setListNodeTreeType(listNodeTreeType);
    return nodeTreesType;
  }

  /**
   * Méthode récursive de récupération de l'arbre des fils d'un node
   *
   * @param nodePK
   *          - le nodePK du node père dont on cherche les fils
   * @return une Collection des fils du père
   * @throws RemoteException
   */
  private Collection getRecursiveTree(NodePK nodePK) throws RemoteException {
    Collection listChildrenDetails = null;
    // Récupération des nodes fils s'ils y en a
    listChildrenDetails = getNodeBm().getChildrenDetails(nodePK);
    if ((listChildrenDetails != null) && (listChildrenDetails.size() != 0)) {// Il
      // n'y
      // pas
      // d'exception
      // jetée...
      // On parcours les nodes trouvés récursivement pour trouver leurs fils
      Iterator itListChildrenDetails = listChildrenDetails.iterator();
      while (itListChildrenDetails.hasNext()) {
        NodeDetail nodeDetail = (NodeDetail) itListChildrenDetails.next();
        nodeDetail.setChildrenDetails(getRecursiveTree(nodeDetail.getNodePK()));
      }
    } else
      listChildrenDetails = null;// Il n'y pas d'exception jetée... et en plus
    // un arrayList vide est créé

    return listChildrenDetails;
  }

  /**
   * @return l'EJB NodeBM
   * @throws ImportExportException
   */
  private NodeBm getNodeBm() throws NodeRuntimeException {

    if (nodeBm == null) {
      try {
        NodeBmHome kscEjbHome = (NodeBmHome) EJBUtilitaire.getEJBObjectRef(
            JNDINames.NODEBM_EJBHOME, NodeBmHome.class);
        nodeBm = kscEjbHome.create();
      } catch (Exception e) {
        throw new NodeRuntimeException("NodeImportExport.getNodeBm()",
            SilverpeasRuntimeException.ERROR, "root.EX_CANT_GET_REMOTE_OBJECT",
            e);
      }
    }
    return nodeBm;
  }
}