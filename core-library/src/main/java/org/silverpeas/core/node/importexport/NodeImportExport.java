/*
 * Copyright (C) 2000 - 2017 Silverpeas
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
package org.silverpeas.core.node.importexport;

import org.silverpeas.core.node.model.NodeDetail;
import org.silverpeas.core.node.model.NodePK;
import org.silverpeas.core.node.service.NodeService;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Classe de gestion des node pour le bus d'importExport
 *
 * @author sdevolder
 */
public class NodeImportExport {

  @Inject
  private NodeService nodeService;

  protected NodeImportExport() {
  }

  // Méthodes
  /**
   * Méthode récupération du chemin de topics menant à un topic donné
   *
   * @param nodePk - le nodePK du topic dont on veut le chemin
   * @return une collection des topic composant le chemin
   */
  public Collection<NodeDetail> getPathOfNode(NodePK nodePk) {
    return getNodeService().getPath(nodePk);
  }

  /**
   * Méthode de récupération de l'arborescence totale des topics d'un liste de composants
   * @param listComponentId - liste des ids des composants dont on veut l'arborescence des topics
   * @return un object NodeTreesType utilisé par le mapping JAXB du module d ' ImportExport
   */
  public List<NodeTreeType> getTrees(List<String> listComponentId) {
    List<NodeTreeType> listNodeTreeType = new ArrayList<>(listComponentId.size());

    // Parcours de la liste des id des composants dont on veut récupérer
    // l'arborescence de thèmes
    for (String componentId : listComponentId) {
      NodeTreeType nodeTreeType = new NodeTreeType();
      listNodeTreeType.add(nodeTreeType);
      nodeTreeType.setComponentId(componentId);

      NodePK nodePK = new NodePK(NodePK.ROOT_NODE_ID, "useless", componentId);
      // Get root folder
      NodeDetail nodeDetail = getNodeService().getDetail(nodePK);
      nodeTreeType.setNodeDetail(nodeDetail);
      // Récupération de l'arbre des nodes de façon récursive
      nodeDetail.setChildrenDetails(getRecursiveTree(nodePK));
    }
    return listNodeTreeType;
  }

  /**
   * Méthode récursive de récupération de l'arbre des fils d'un node
   *
   * @param nodePK - le nodePK du node père dont on cherche les fils
   * @return une Collection des fils du père
   */
  private Collection<NodeDetail> getRecursiveTree(NodePK nodePK) {
    Collection<NodeDetail> listChildrenDetails = getNodeService().getChildrenDetails(nodePK);
    if (listChildrenDetails != null && !listChildrenDetails.isEmpty()) {
      for (NodeDetail nodeDetail : listChildrenDetails) {
        nodeDetail.setChildrenDetails(getRecursiveTree(nodeDetail.getNodePK()));
      }
    } else {
      listChildrenDetails = null;
    }
    return listChildrenDetails;
  }

  private NodeService getNodeService() {
    return nodeService;
  }
}