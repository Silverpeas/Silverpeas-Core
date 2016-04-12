/*
 * Copyright (C) 2000 - 2016 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU Affero General Public License as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of the GPL, you may
 * redistribute this Program in connection with Free/Libre Open Source Software ("FLOSS")
 * applications as described in Silverpeas's FLOSS exception. You should have received a copy of the
 * text describing the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License along with this program.
 * If not, see <http://www.gnu.org/licenses/>.
 */
package org.silverpeas.core.importexport.control;

import org.silverpeas.core.importexport.model.ImportExportException;
import org.silverpeas.core.importexport.report.ImportReportManager;
import org.silverpeas.core.importexport.report.UnitReport;
import org.silverpeas.core.node.importexport.NodeTreeType;
import org.silverpeas.core.node.importexport.NodeTreesType;
import org.silverpeas.core.admin.service.OrganizationControllerProvider;
import org.silverpeas.core.util.StringUtil;
import org.silverpeas.core.admin.component.model.ComponentInst;
import org.silverpeas.core.admin.user.model.UserDetail;
import org.silverpeas.core.node.model.NodeDetail;
import java.util.Collection;
import java.util.List;

/**
 * Classe de gestion des importations unitaires de thèmes dans KMelia pour le moteur d'importExport
 * de SilverPeas.
 */
public class NodeTreesTypeManager {

  /**
   * Crée tous les thèmes (ou noeuds) unitairement définis tels que précisés dans le fichier
   * d'import XML. La méthode est récursive pour chaque sous-noeud spécifié. Si un noeud existe déjà
   * avec le même ID, l'algorithme interrompt la création de la branche correspondante (les
   * éventuels sous-noeuds ne seront pas créés) et il passe aux noeuds suivants.
   * <p>
   * Construit parallèlement un objet ComponentReport contenant les informations de création des
   * noeuds et nécéssaire au rapport détaillé.
   * </p>
   *
   * @param userDetail contient les informations sur l'utilisateur du moteur d'importExport
   * @param nodeTreesType objet mappé par castor contenant toutes les informations de création des
   * noeuds
   * @param targetComponentId ID du composant par défaut dans lequel creer les noeuds.
   */
  public void processImport(UserDetail userDetail, NodeTreesType nodeTreesType,
      String targetComponentId, ImportReportManager reportManager) {
    int nbTopicTree = 1;
    int nbTopic = 1;
    GEDImportExport gedIE =
        ImportExportFactory.createGEDImportExport(userDetail, targetComponentId);
    List<NodeTreeType> listNodeTreeType = nodeTreesType.getListNodeTreeType();
    if (!listNodeTreeType.isEmpty()) {
      // On parcours les objets NodeTreeType
      for (NodeTreeType nodeTreeType : listNodeTreeType) {

        // On détermine si on doit utiliser le componentId par défaut
        String componentId;
        if (!StringUtil.isDefined(nodeTreeType.getComponentId())) {
          componentId = targetComponentId;
        } else {
          componentId = nodeTreeType.getComponentId();
        }
        gedIE.setCurrentComponentId(componentId);
        // Création du rapport unitaire
        UnitReport unitReport = new UnitReport("<topicTree> #" + nbTopicTree);
        reportManager.addUnitReport(unitReport, componentId);

        ComponentInst component = OrganizationControllerProvider
            .getOrganisationController().getComponentInst(componentId);
        if (component == null) {
          // le composant n'existe pas
          unitReport.setError(UnitReport.ERROR_NOT_EXISTS_COMPONENT);
          unitReport.setStatus(UnitReport.STATUS_PUBLICATION_NOT_CREATED);
        } else {
          reportManager.setComponentName(componentId, component.getLabel());
          nbTopic = processImportNodeInternal(nodeTreeType.getNodeDetail(), null, gedIE, nbTopic,
              componentId, reportManager);
          nbTopicTree++;
        }
      }
    }
  }

  /**
   * Méthode récursive pour la création des noeuds et de leurs sous-noeuds.
   *
   * @param node noeud à créer (avec ses sous-noeuds éventuels).
   * @param parentNode noeud parent ou <code>null</code> pour désigner le noeud racine.
   * @param gedIE objet implémentant les méthodes utiles pour la gestion des thèmes et des
   * publications.
   * @param nbTopic nombre courant de noeuds déjà créés.
   * @param componentId Identifiant du composant dans lequel on crée les noeuds.
   * @return le nombre de noeuds augmentés par le nombre de créations effectuées avec succès lors de
   * l'appel à cette méthode.
   */
  private int processImportNodeInternal(NodeDetail node, NodeDetail parentNode,
      GEDImportExport gedIE, int nbTopic, String componentId, ImportReportManager reportManager) {

    if (node != null) {
      NodeDetail newNode;
      int parentNodeId;

      try {
        String parentNodeIdStr = (parentNode != null) ? parentNode.getNodePK().getId() : null;
        parentNodeId = (parentNodeIdStr != null) ? Integer.parseInt(parentNodeIdStr) : 0;
      } catch (NumberFormatException ex) {
        parentNodeId = 0;
      }

      // Création du rapport unitaire
      UnitReport unitReport = new UnitReport("<topic> #" + nbTopic);
      reportManager.addUnitReport(unitReport, componentId);

      // On commence par créer le topic dont la description est passée en
      // paramètre
      try {
        newNode = gedIE.createTopicForUnitImport(unitReport, node, parentNodeId);
      } catch (ImportExportException e) {
        return nbTopic;
      }

      // On vérifie qu'on a bien création du noeud (c'est à dire s'il
      // n'existait pas au préalable)
      if (newNode != null) {
        nbTopic++;
      }

      // On vérifie s'il existe des sous-topics dans la description passée
      // en paramètre
      Collection<NodeDetail> children = node.getChildrenDetails();
      if (children == null) {
        return nbTopic;
      }

      // S'il y a des sous-topics, on les crée de façon récursive
      for (NodeDetail childNode : children) {
        nbTopic = processImportNodeInternal(childNode, newNode, gedIE, nbTopic, componentId,
            reportManager);
      }
    }

    return nbTopic;
  }
}
