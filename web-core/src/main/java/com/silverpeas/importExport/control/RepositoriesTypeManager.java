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
 * Created on 31 janv. 2005
 *
 */
package com.silverpeas.importExport.control;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import com.silverpeas.attachment.importExport.AttachmentImportExport;
import com.silverpeas.importExport.model.ImportExportException;
import com.silverpeas.importExport.model.RepositoriesType;
import com.silverpeas.importExport.model.RepositoryType;
import com.silverpeas.importExport.report.ImportReportManager;
import com.silverpeas.importExport.report.MassiveReport;
import com.silverpeas.importExport.report.UnitReport;
import com.silverpeas.pdc.importExport.PdcImportExport;
import com.silverpeas.versioning.importExport.VersioningImportExport;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.beans.admin.ComponentInst;
import com.stratelia.webactiv.beans.admin.OrganizationController;
import com.stratelia.webactiv.beans.admin.UserDetail;
import com.stratelia.webactiv.util.attachment.ejb.AttachmentPK;
import com.stratelia.webactiv.util.attachment.model.AttachmentDetail;
import com.stratelia.webactiv.util.node.model.NodeDetail;
import com.stratelia.webactiv.util.publication.model.PublicationDetail;
import com.stratelia.webactiv.util.publication.model.PublicationPK;

/**
 * Classe manager des importations massives du moteur d'importExport de
 * silverPeas
 * 
 * @author sdevolder
 */
public class RepositoriesTypeManager {

  /**
   * Méthode métier du moteur d'importExport créant toutes les publications
   * massives définies au niveau du fichier d'import xml passé en paramètre
   * au moteur d'importExport.
   * 
   * @param userDetail
   *          - contient les informations sur l'utilisateur du moteur
   *          d'importExport
   * @param repositoriesType
   *          - objet mappé par castor contenant toutes les informations de
   *          création des publications du path défini
   * @return un objet ComponentReport contenant les informations de création
   *         des publications unitaires et nécéssaire au rapport détaillé
   * @throws ImportExportException
   */
  public void processImport(UserDetail userDetail,
      RepositoriesType repositoriesType, boolean isPOIUsed) {

    List listRep_Type = repositoriesType.getListRepositoryType();
    Iterator itListRep_Type = listRep_Type.iterator();
    OrganizationController orgaController = null;
    AttachmentImportExport attachmentIE = new AttachmentImportExport();
    VersioningImportExport versioningIE = new VersioningImportExport();
    PdcImportExport pdcIE = new PdcImportExport();

    String componentId = null;
    int topicId = -1;
    String sPath = null;
    while (itListRep_Type.hasNext()) {
      RepositoryType rep_Type = (RepositoryType) itListRep_Type.next();

      componentId = rep_Type.getComponentId();
      topicId = rep_Type.getTopicId();
      sPath = rep_Type.getPath();

      // Création du rapport de repository
      MassiveReport massiveReport = new MassiveReport();
      ImportReportManager.addMassiveReport(massiveReport, componentId);
      massiveReport.setRepositoryPath(sPath);

      if (orgaController == null)
        orgaController = new OrganizationController();

      ComponentInst componentInst = orgaController
          .getComponentInst(componentId);
      if (componentInst == null) {
        // le composant n'existe pas
        massiveReport.setError(UnitReport.ERROR_NOT_EXISTS_COMPONENT);
      } else {
        ImportReportManager.setComponentName(componentId, componentInst
            .getLabel());

        File path = new File(sPath);
        if (!path.isDirectory()) {
          // La variable path ne peut contenir qu'un dossier
          massiveReport
              .setError(UnitReport.ERROR_NOT_EXISTS_OR_INACCESSIBLE_DIRECTORY);
        } else {
          boolean isVersioningUsed = ImportExportHelper
              .isVersioningUsed(componentInst);
          boolean isDraftUsed = ImportExportHelper.isDraftUsed(componentInst);

          GEDImportExport gedIE = ImportExportFactory.createGEDImportExport(
              userDetail, componentId);

          Iterator itListcontenuPath = getPathContent(path);
          while (itListcontenuPath.hasNext()) {
            File file = (File) itListcontenuPath.next();
            if (file.isFile()) {
              importFile(file, topicId, massiveReport, gedIE, attachmentIE,
                  versioningIE, pdcIE, isPOIUsed, isVersioningUsed, isDraftUsed);
            } else if (file.isDirectory()) {
              switch (rep_Type.getMassiveTypeInt()) {
                case RepositoryType.NO_RECURSIVE:
                  // on ne fait rien
                  break;
                case RepositoryType.RECURSIVE_NOREPLICATE:
                  // traitement récursif spécifique
                  processImportRecursiveNoReplicate(massiveReport, userDetail,
                      file, gedIE, attachmentIE, versioningIE, pdcIE,
                      componentId, topicId, isPOIUsed, isVersioningUsed,
                      isDraftUsed);
                  break;
                case RepositoryType.RECURSIVE_REPLICATE:
                  try {
                    NodeDetail nodeDetail = gedIE.addSubTopicToTopic(file,
                        topicId, massiveReport);
                    // massiveReport.addOneTopicCreated();
                    // Traitement récursif spécifique
                    processImportRecursiveReplicate(massiveReport, userDetail,
                        file, gedIE, attachmentIE, versioningIE, pdcIE,
                        componentId, Integer.parseInt(nodeDetail.getNodePK()
                            .getId()), isPOIUsed, isVersioningUsed, isDraftUsed);
                  } catch (ImportExportException ex) {
                    massiveReport
                        .setError(UnitReport.ERROR_NOT_EXISTS_OR_INACCESSIBLE_DIRECTORY);
                  }
                  break;
              }
            }
          }
        }
      }
    }
  }

  private void importFile(File file, int topicId, MassiveReport massiveReport,
      GEDImportExport gedIE, AttachmentImportExport attachmentIE,
      VersioningImportExport versioningIE, PdcImportExport pdcIE,
      boolean isPOIUsed, boolean isVersioningUsed, boolean isDraftUsed) {
    SilverTrace.debug("importExport", "RepositoriesTypeManager.importFile",
        "root.MSG_GEN_ENTER_METHOD", "file = " + file.getName());

    String componentId = gedIE.getCurrentComponentId();
    UserDetail userDetail = gedIE.getCurentUserDetail();

    try {
      // Création du rapport unitaire
      UnitReport unitReport = new UnitReport();
      massiveReport.addUnitReport(unitReport);

      // On récupére les infos nécéssaires à la création de la publication
      PublicationDetail pubDetailToCreate = PublicationImportExport
          .convertFileInfoToPublicationDetail(userDetail, file, isPOIUsed);
      pubDetailToCreate.setPk(new PublicationPK("unknown", "useless",
          componentId));

      SilverTrace.debug("importExport", "RepositoriesTypeManager.importFile",
          "root.MSG_GEN_PARAM_VALUE", "pubDetailToCreate instanciated");

      if ((isDraftUsed && pdcIE.isClassifyingMandatory(componentId))
          || isDraftUsed) {
        pubDetailToCreate.setStatus(PublicationDetail.DRAFT);
        pubDetailToCreate.setStatusMustBeChecked(false);
      }

      SilverTrace.debug("importExport", "RepositoriesTypeManager.importFile",
          "root.MSG_GEN_PARAM_VALUE", "pubDetailToCreate.status = "
              + pubDetailToCreate.getStatus());

      // Création de la publication
      pubDetailToCreate = gedIE.createPublicationForMassiveImport(unitReport,
          userDetail, pubDetailToCreate, topicId);

      SilverTrace.debug("importExport", "RepositoriesTypeManager.importFile",
          "root.MSG_GEN_PARAM_VALUE", "pubDetailToCreate created");

      // Ajout de l'attachment
      AttachmentDetail attDetail = new AttachmentDetail();
      AttachmentPK pk = new AttachmentPK("unknown", "useless", componentId);
      attDetail.setPhysicalName(file.getPath());
      attDetail.setAuthor(userDetail.getId());
      attDetail.setPK(pk);

      SilverTrace.debug("importExport", "RepositoriesTypeManager.importFile",
          "root.MSG_GEN_PARAM_VALUE", "attDetail instanciated");

      if (isVersioningUsed) {
        // Mode versioning
        // copie du fichier sur le serveur et enrichissement du AttachmentDetail
        attachmentIE.copyFile(componentId, attDetail, versioningIE
            .getVersioningPath(componentId));
        if (attDetail.getSize() != 0) {
          List attachments = new ArrayList();
          attachments.add(attDetail);
          versioningIE.importDocuments(pubDetailToCreate.getPK().getId(),
              componentId, attachments, new Integer(userDetail.getId())
                  .intValue(), pubDetailToCreate.isIndexable(), new Integer(
                  topicId).toString());
        }
      } else {
        // Ajout des attachments
        attachmentIE.importAttachment(pubDetailToCreate.getPK().getId(),
            componentId, attDetail, pubDetailToCreate.isIndexable());
      }

      // Traitement des statistiques
      if (attDetail.getSize() > 0) {
        ImportReportManager.addNumberOfFilesProcessed(1);
        ImportReportManager.addImportedFileSize(attDetail.getSize(),
            componentId);
      } else {
        unitReport.setError(UnitReport.ERROR_NOT_EXISTS_OR_INACCESSIBLE_FILE);
        ImportReportManager.addNumberOfFilesNotImported(1);
      }
    } catch (Exception ex) {
      massiveReport.setError(UnitReport.ERROR_ERROR);
      SilverTrace.error("importExport", "RepositoriesTypeManager.importFile()",
          "root.EX_NO_MESSAGE", ex);
    }
  }

  /**
   * Méthode récursive appelée dans le cas de l'importation massive
   * récursive sans création de nouveau topic: toutes les publications crées
   * le seront dans le thème passé en paramètre.
   * 
   * @param massiveReport
   *          - référence sur l'objet de rapport détaillé du cas import
   *          massif permettant de le compléter quelque soit le niveau de
   *          récursivité.
   * @param userDetail
   *          - contient les informations sur l'utilisateur du moteur
   *          d'importExport.
   * @param path
   *          - dossier correspondant au niveau de récursivité auquel on se
   *          trouve.
   * @param componentId
   *          - id du composant dans le lequel l'import massif est effectué.
   * @param topicId
   *          - id du thème dans lequel seront crées les éléments, l'id
   *          passé est toujours le même dans le cas présent
   * @throws ImportExportException
   */
  public void processImportRecursiveNoReplicate(MassiveReport massiveReport,
      UserDetail userDetail, File path, GEDImportExport gedIE,
      AttachmentImportExport attachmentIE, VersioningImportExport versioningIE,
      PdcImportExport pdcIE, String componentId, int topicId,
      boolean isPOIUsed, boolean isVersioningUsed, boolean isDraftUsed) {
    Iterator itListcontenuPath = getPathContent(path);
    while (itListcontenuPath.hasNext()) {
      File file = (File) itListcontenuPath.next();
      if (file.isFile()) {
        importFile(file, topicId, massiveReport, gedIE, attachmentIE,
            versioningIE, pdcIE, isPOIUsed, isVersioningUsed, isDraftUsed);
      } else if (file.isDirectory()) {
        // traitement récursif spécifique
        processImportRecursiveNoReplicate(massiveReport, userDetail, file,
            gedIE, attachmentIE, versioningIE, pdcIE, componentId, topicId,
            isPOIUsed, isVersioningUsed, isDraftUsed);
      }
    }
  }

  /**
   * Méthode récursive appelée dans le cas de l'importation massive
   * récursive avec création de nouveau topic: chaque sous dossier entrainera
   * la création d'un topic de même nom.
   * 
   * @param massiveReport
   *          - référence sur l'objet de rapport détaillé du cas import
   *          massif permettant de le compléter quelque soit le niveau de
   *          récursivité.
   * @param userDetail
   *          - contient les informations sur l'utilisateur du moteur
   *          d'importExport.
   * @param path
   *          - dossier correspondant au niveau de récursivité auquel on se
   *          trouve.
   * @param componentId
   *          - id du composant dans le lequel l'import massif est effectué.
   * @param topicId
   *          - id du thème dans lequel seront crées les éléments du niveau
   *          de récursivité auquel on se trouve.
   * @throws ImportExportException
   */
  public void processImportRecursiveReplicate(MassiveReport massiveReport,
      UserDetail userDetail, File path, GEDImportExport gedIE,
      AttachmentImportExport attachmentIE, VersioningImportExport versioningIE,
      PdcImportExport pdcIE, String componentId, int topicId,
      boolean isPOIUsed, boolean isVersioningUsed, boolean isDraftUsed)
      throws ImportExportException {
    Iterator itListcontenuPath = getPathContent(path);
    while (itListcontenuPath.hasNext()) {
      File file = (File) itListcontenuPath.next();
      if (file.isFile()) {
        importFile(file, topicId, massiveReport, gedIE, attachmentIE,
            versioningIE, pdcIE, isPOIUsed, isVersioningUsed, isDraftUsed);
      } else if (file.isDirectory()) {
        NodeDetail nodeDetail = gedIE.addSubTopicToTopic(file, topicId,
            massiveReport);
        // massiveReport.addOneTopicCreated();
        // Traitement récursif spécifique
        processImportRecursiveReplicate(massiveReport, userDetail, file, gedIE,
            attachmentIE, versioningIE, pdcIE, componentId, Integer
                .parseInt(nodeDetail.getNodePK().getId()), isPOIUsed,
            isVersioningUsed, isDraftUsed);
      }
    }
  }

  private Iterator getPathContent(File path) {
    SilverTrace.debug("importExport", "RepositoriesTypeManager.getPathContent",
        "root.MSG_GEN_ENTER_METHOD", "path = " + path.getPath());
    // Récupération du contenu du dossier
    String[] listContenuStringPath = path.list();
    List listcontenuPath = convertListStringToListFile(listContenuStringPath,
        path.getPath());

    // Tri alphabétique du contenu
    Arrays.sort(listContenuStringPath);

    return listcontenuPath.iterator();
  }

  /**
   * Transforme la table des chaines de caractères de nom de fichier en une
   * liste de fichiers pour le chemin passé en paramètre
   * 
   * @param listFileName
   *          - table des nom de fichier sous forme de chaine de caractères.
   * @param path
   *          - chemin des fichiers contenu dans les chaines de caractères.
   * @return renvoie une liste d'objets File pour les noms de fichiers passés
   *         en paramètres
   */
  private List convertListStringToListFile(String[] listFileName, String path) {

    List listFile = new ArrayList();

    if (listFileName == null)
      return null;

    for (int i = 0; i < listFileName.length; i++) {
      listFile.add(new File(path + File.separator + listFileName[i]));
    }
    return listFile;
  }
}