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

import org.silverpeas.core.contribution.content.form.XMLField;
import org.silverpeas.core.node.importexport.NodeImportExport;
import org.silverpeas.core.node.importexport.NodePositionType;
import org.silverpeas.core.node.importexport.NodePositionsType;
import org.silverpeas.core.admin.component.model.ComponentInst;
import org.silverpeas.core.admin.component.model.ComponentInstLight;
import org.silverpeas.core.admin.user.model.UserDetail;
import org.silverpeas.core.node.coordinates.model.Coordinate;
import org.silverpeas.core.node.model.NodeDetail;
import org.silverpeas.core.node.model.NodePK;
import org.silverpeas.core.contribution.publication.model.PublicationDetail;
import org.silverpeas.core.contribution.publication.model.PublicationPK;
import org.apache.commons.io.FileUtils;
import org.silverpeas.core.contribution.attachment.AttachmentServiceProvider;
import org.silverpeas.core.contribution.attachment.model.SimpleDocument;
import org.silverpeas.core.contribution.attachment.model.SimpleDocumentPK;
import org.silverpeas.core.contribution.attachment.util.AttachmentSettings;
import org.silverpeas.core.admin.service.OrganizationControllerProvider;
import org.silverpeas.core.importexport.attachment.AttachmentDetail;
import org.silverpeas.core.importexport.attachment.AttachmentImportExport;
import org.silverpeas.core.importexport.attachment.AttachmentsType;
import org.silverpeas.core.importexport.coordinates.CoordinateImportExport;
import org.silverpeas.core.importexport.coordinates.CoordinatePointType;
import org.silverpeas.core.importexport.coordinates.CoordinatesPositionsType;
import org.silverpeas.core.importexport.model.ImportExportException;
import org.silverpeas.core.importexport.model.PublicationType;
import org.silverpeas.core.importexport.model.PublicationsType;
import org.silverpeas.core.importexport.publication.PublicationContentType;
import org.silverpeas.core.importexport.publication.XMLModelContentType;
import org.silverpeas.core.importexport.report.ExportPDFReport;
import org.silverpeas.core.importexport.report.ExportReport;
import org.silverpeas.core.importexport.report.HtmlExportPublicationGenerator;
import org.silverpeas.core.importexport.report.ImportReportManager;
import org.silverpeas.core.importexport.report.UnitReport;
import org.silverpeas.core.importexport.versioning.Document;
import org.silverpeas.core.importexport.versioning.DocumentVersion;
import org.silverpeas.core.importexport.versioning.VersioningImportExport;
import org.silverpeas.core.importexport.wysiwyg.WysiwygContentType;
import org.silverpeas.core.pdc.pdc.importexport.PdcImportExport;
import org.silverpeas.core.pdc.pdc.importexport.PdcPositionsType;
import org.silverpeas.core.pdc.pdc.model.ClassifyPosition;
import org.silverpeas.core.util.Charsets;
import org.silverpeas.core.util.file.FileRepositoryManager;
import org.silverpeas.core.util.file.FileServerUtils;
import org.silverpeas.core.ForeignPK;
import org.silverpeas.core.io.media.MetaData;
import org.silverpeas.core.io.media.MetadataExtractor;
import org.silverpeas.core.util.StringUtil;
import org.silverpeas.core.util.WAAttributeValuePair;
import org.silverpeas.core.util.error.SilverpeasTransverseErrorUtil;
import org.silverpeas.core.util.file.FileFolderManager;
import org.silverpeas.core.util.logging.SilverLogger;
import org.silverpeas.core.contribution.content.wysiwyg.service.WysiwygController;

import javax.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import static java.io.File.separator;

/**
 * Classe manager des importations unitaires du moteur d'importExport de silverPeas
 * @author sdevolder
 */
public class PublicationsTypeManager {

  @Inject
  private CoordinateImportExport coordinateImportExport;
  @Inject
  private NodeImportExport nodeImportExport;
  @Inject
  private PdcImportExport pdcImportExport;

  protected PublicationsTypeManager() {

  }

  /**
   * Méthode métier du moteur d'importExport créant une exportation pour toutes les publications
   * spécifiées en paramètre. passé en paramètre au moteur d'importExport.
   * @param exportReport the export report logger
   * @param userDetail - contient les informations sur l'utilisateur du moteur d'importExport
   * @param listItemsToExport - liste des WAAttributeValuePair contenant les id des publications à
   * exporter
   * @param exportPath - cible de l'exportation
   * @param useNameForFolders
   * @param bExportPublicationPath
   * @return
   * @throws ImportExportException
   * @throws IOException
   */
  public PublicationsType processExport(ExportReport exportReport, UserDetail userDetail,
      List<WAAttributeValuePair> listItemsToExport, String exportPath, boolean useNameForFolders,
      boolean bExportPublicationPath, NodePK rootPK) throws ImportExportException, IOException {
    AttachmentImportExport attachmentIE = new AttachmentImportExport(userDetail);
    PublicationsType publicationsType = new PublicationsType();
    List<PublicationType> listPubType = new ArrayList<>();
    String wysiwygText = null;

    // Parcours des publications à exporter
    for (WAAttributeValuePair attValue : listItemsToExport) {
      String pubId = attValue.getName();
      String componentId = attValue.getValue();
      ComponentInstLight componentInst = OrganizationControllerProvider.getOrganisationController()
          .getComponentInstLight(componentId);
      GEDImportExport gedIE = ImportExportFactory.createGEDImportExport(userDetail, componentId);
      // Récupération du PublicationType
      PublicationType publicationType = gedIE.getPublicationCompleteById(pubId, componentId);
      PublicationDetail publicationDetail = publicationType.getPublicationDetail();
      listPubType.add(publicationType);

      // Récupération des topics (il y en a au moins un)
      String exportPublicationRelativePath;
      String exportPublicationPath;
      if (gedIE.isKmax()) {
        publicationType.setCoordinatesPositionsType(new CoordinatesPositionsType());
        exportPublicationRelativePath =
            createPathDirectoryForKmaxPublicationExport(exportPath, componentId,
                componentInst.getLabel(), publicationDetail, useNameForFolders);
        exportPublicationPath = exportPath + separator + exportPublicationRelativePath;
      } else {
        fillPublicationType(gedIE, publicationType, rootPK);
        // Création de l'arborescence de dossiers pour la création de l'export de publication
        NodePositionType nodePositionType = publicationType.getNodePositionsType().
            getListNodePositionType().get(0);
        String nodeInstanceId = componentId;
        if (rootPK != null) {
          nodeInstanceId = rootPK.getInstanceId();
          if (!nodeInstanceId.equals(componentId)) {
            // case of aliases
            componentInst = OrganizationControllerProvider.getOrganisationController().
                getComponentInstLight(nodeInstanceId);
          }
        }
        exportPublicationRelativePath =
            createPathDirectoryForPublicationExport(exportPath, nodePositionType.getId(),
                nodeInstanceId, componentInst.getLabel(), publicationDetail, useNameForFolders,
                bExportPublicationPath);
        exportPublicationPath = exportPath + separator + exportPublicationRelativePath;
      }
      // To avoid problems with Winzip
      if (exportPublicationPath.length() > 250) {
        return null;
      }

      // Copie des fichiers de contenu s'il en existe
      PublicationContentType pubContent = publicationType.getPublicationContentType();
      if (pubContent != null) {
        WysiwygContentType wysiwygContent = pubContent.getWysiwygContentType();
        XMLModelContentType xmlModel = pubContent.getXMLModelContentType();
        if (wysiwygContent != null) {
          wysiwygText = exportWysiwygContent(pubId, publicationDetail.getInstanceId(), gedIE,
              exportPublicationRelativePath, exportPublicationPath, wysiwygContent, publicationType.
                  getPublicationDetail().getLanguage());
        } else if (xmlModel != null) {
          exportXmlForm(new PublicationPK(pubId, publicationDetail.getInstanceId()),
              exportPublicationRelativePath, exportPublicationPath, xmlModel);
        }
      }
      exportAttachments(attachmentIE, publicationType, publicationDetail.getPK(),
          exportPublicationRelativePath, exportPublicationPath);
      exportPdc(pdcImportExport, publicationDetail.getPK(), gedIE, publicationType);
      int nbThemes = 1;
      if (!gedIE.isKmax()) {
        nbThemes = getNbThemes(gedIE, publicationType, rootPK);
      }
      if (!writePublicationHtml(exportReport, wysiwygText, pubId, publicationType,
          exportPublicationRelativePath, exportPublicationPath, nbThemes)) {
        return null;
      }
      wysiwygText = null;
    }
    publicationsType.setListPublicationType(listPubType);
    return publicationsType;
  }

  String exportWysiwygContent(String pubId, String componentId, GEDImportExport gedIE,
      String exportPublicationRelativePath, String exportPublicationPath,
      WysiwygContentType wysiwygContent, String language) throws ImportExportException {
    String wysiwygText;
    String wysiwygFileName = wysiwygContent.getPath();
    gedIE.copyWysiwygImageForExport(pubId, componentId, exportPublicationPath);

    try {
      wysiwygText = WysiwygController.load(componentId, pubId, language);
      wysiwygText = HtmlExportPublicationGenerator.replaceImagesPathForExport(wysiwygText);
      if (wysiwygText == null) {
        wysiwygText = ""; // To avoid exception in createFile below
      }
      // Enregistrement du nouveau fichier généré
      FileFolderManager.createFile(exportPublicationPath, wysiwygFileName, wysiwygText);
    } catch (Exception e) {
      throw new ImportExportException("importExport", "importExport.EX_CANT_GET_WYSIWYG", e);
    }

    wysiwygContent.setPath(exportPublicationRelativePath + separator + wysiwygFileName);
    return wysiwygText;
  }

  boolean writePublicationHtml(ExportReport exportReport, String wysiwygText, String pubId,
      PublicationType publicationType, String exportPublicationRelativePath,
      String exportPublicationPath, int nbThemes) {
    String htmlNameIndex = "index.html";
    HtmlExportPublicationGenerator s =
        new HtmlExportPublicationGenerator(publicationType, wysiwygText,
            exportPublicationRelativePath + separator + htmlNameIndex, nbThemes);
    exportReport.addHtmlIndex(pubId, s);
    File fileHTML = new File(exportPublicationPath + separator + htmlNameIndex);
    try {
      fileHTML.createNewFile();
      FileUtils.write(fileHTML, s.toHtml(), Charsets.UTF_8);
    } catch (IOException ex) {
      return false;
    }
    return true;
  }

  void exportPdc(PdcImportExport pdcImpExp, PublicationPK pk, GEDImportExport gedIE,
      PublicationType publicationType) {
    // Récupération du classement pdc
    try {
      List<ClassifyPosition> listClassifyPostion =
          pdcImpExp.getPositions(gedIE.getSilverObjectId(pk.getId()), pk.getInstanceId());
      if (listClassifyPostion != null && !listClassifyPostion.isEmpty()) {
        publicationType.setPdcPositionsType(new PdcPositionsType());
        publicationType.getPdcPositionsType().setListClassifyPosition(listClassifyPostion);
      }
    } catch (Exception ex) {
      // Do not block export in case of error
      SilverLogger.getLogger(this).warn("Cannot get PdC positions: {0}", ex.getMessage());
    }
  }

  void exportAttachments(AttachmentImportExport attachmentIE, PublicationType publicationType,
      PublicationPK publicationPK, String exportRelativePath, String exportPath)
      throws ImportExportException {
    // Récupération des attachments et copie des fichiers
    List<AttachmentDetail> attachments =
        attachmentIE.getAttachments(publicationPK, exportPath, exportRelativePath, null);
    if (attachments != null && !attachments.isEmpty() && publicationType != null) {
      publicationType.setAttachmentsType(new AttachmentsType());
      publicationType.getAttachmentsType().setListAttachmentDetail(attachments);
    }
  }

  void exportXmlForm(PublicationPK publicationPk, String exportPublicationRelativePath,
      String exportPublicationPath, XMLModelContentType xmlModel) {
    List<XMLField> xmlFields = xmlModel.getFields();
    for (XMLField xmlField : xmlFields) {
      String value = xmlField.getValue();
      if (StringUtil.isDefined(value)) {
        if (value.startsWith("xmlWysiwygField")) {
          String wysiwygFile = value.substring(value.indexOf('_') + 1);
          try {
            String fromPath = FileRepositoryManager.getAbsolutePath(publicationPk.getInstanceId()) +
                "xmlWysiwyg" + separator + wysiwygFile;
            FileRepositoryManager
                .copyFile(fromPath, exportPublicationPath + separator + wysiwygFile);
          } catch (Exception e) {
            SilverLogger.getLogger(this).warn("Cannot write WYSIWYG content: {0}", e.getMessage());
          }

        } else if (value.startsWith("image")) {
          String imageId = value.substring(value.indexOf('_') + 1, value.length());
          SimpleDocument attachment = null;
          try {
            attachment = AttachmentServiceProvider.getAttachmentService()
                .searchDocumentById(new SimpleDocumentPK(imageId, publicationPk.getInstanceId()),
                    null);
          } catch (RuntimeException e1) {
            SilverLogger.getLogger(this).warn("Cannot get image: {0}", e1.getMessage());
          }

          if (attachment != null) {
            String fromPath = attachment.getAttachmentPath();

            try {
              FileRepositoryManager
                  .copyFile(fromPath, exportPublicationPath + separator + attachment.getFilename());
            } catch (Exception e) {
              SilverLogger.getLogger(this).warn("Cannot write file: {0}", e.getMessage());
            }
            xmlField.setValue(exportPublicationRelativePath + separator + attachment.getFilename());
          }
        } else if (value.startsWith("file")) {
          String fileId = value.substring(value.indexOf('_') + 1, value.length());

          SimpleDocument attachment = null;
          try {
            attachment = AttachmentServiceProvider.getAttachmentService()
                .searchDocumentById(new SimpleDocumentPK(fileId, publicationPk.getInstanceId()),
                    null);
          } catch (RuntimeException e1) {
            SilverLogger.getLogger(this).warn("Cannot get attachment: {0}", e1.getMessage());
          }
          if (attachment != null) {
            xmlField.setValue(exportPublicationRelativePath + separator + attachment.getFilename());
          }
        }
      }
    }
  }

  private String createDirectoryPathForExport(String exportPath, NodePK rootPK, NodePK pk,
      boolean useNameForFolders) throws IOException {

    StringBuilder pathToCreate = new StringBuilder(exportPath);

    List<NodeDetail> listNodes = new ArrayList<>(nodeImportExport.getPathOfNode(pk));
    Collections.reverse(listNodes);
    boolean rootFound = false;
    for (NodeDetail nodeDetail : listNodes) {
      if (nodeDetail.getNodePK().equals(rootPK)) {
        rootFound = true;
      }
      if (rootFound) {
        String nodeNameForm = nodeDetail.getNodePK().getId();
        if (useNameForFolders) {
          nodeNameForm = DirectoryUtils.formatToDirectoryNamingCompliant(nodeDetail.getName());
        }
        pathToCreate.append(separator).append(nodeNameForm);
      }
    }

    // ZIP API manage only ASCII characters. So directories are created in ASCII too.
    String pathToCreateAscii = createASCIIPath(pathToCreate.toString());

    return pathToCreateAscii;
  }

  private String createASCIIPath(String path) throws IOException {
    String pathToCreateAscii = FileServerUtils.replaceAccentChars(path);
    File dir = new File(pathToCreateAscii);
    if (!dir.exists()) {
      boolean creationOK = dir.mkdirs();
      if (!creationOK) {
        throw new IOException();
      }
    }
    return pathToCreateAscii;
  }

  /**
   * Méthode créant l'arboresence des répertoires pour une publication exportée
   * @param exportPath - dossier dans lequel creer notre arborescence de dossiers
   * @param topicId - id du topic dont on veut la branche
   * @param componentId - id du composant de la publication
   * @param componentLabel - label du composant
   * @param pub - la publication
   * @return le chemin relatif créé
   */
  private String createPathDirectoryForPublicationExport(String exportPath, int topicId,
      String componentId, String componentLabel, PublicationDetail pub, boolean useNameForFolders,
      boolean exportPublicationPath) throws IOException {
    String pubNameForm = pub.getPK().getId();
    if (useNameForFolders) {
      pubNameForm = DirectoryUtils.formatToDirectoryNamingCompliant(pub.getName());
    }

    StringBuilder relativeExportPath = new StringBuilder();
    StringBuilder pathToCreate = new StringBuilder(exportPath);
    if (exportPublicationPath) {
      String componentLabelForm = componentId;
      if (useNameForFolders || isKmax(componentId)) {
        componentLabelForm = DirectoryUtils.formatToDirectoryNamingCompliant(componentLabel);
      }

      relativeExportPath.append(componentLabelForm);
      pathToCreate.append(File.separatorChar).append(componentLabelForm);
      List<NodeDetail> listNodes = new ArrayList<>(nodeImportExport.getPathOfNode(new NodePK(String.
          valueOf(topicId), "useless", componentId)));
      Collections.reverse(listNodes);
      for (NodeDetail nodeDetail : listNodes) {
        String nodeNameForm = nodeDetail.getNodePK().getId();
        if (useNameForFolders) {
          nodeNameForm = DirectoryUtils.formatToDirectoryNamingCompliant(nodeDetail.getName());
        }
        pathToCreate.append(separator).append(nodeNameForm);
        relativeExportPath.append(separator).append(nodeNameForm);
      }
    }
    relativeExportPath.append(separator).append(pubNameForm);
    pathToCreate.append(separator).append(pubNameForm);

    // ZIP API manage only ASCII characters. So directories are created in ASCII too.
    String relativeExportPathAscii = FileServerUtils.replaceAccentChars(relativeExportPath.
        toString());
    createASCIIPath(pathToCreate.toString());

    return relativeExportPathAscii;
  }

  public void processExportOfFilesOnly(ExportReport exportReport, UserDetail userDetail,
      List<WAAttributeValuePair> listItemsToExport, String exportPath, NodePK nodeRootPK)
      throws ImportExportException, IOException {
    AttachmentImportExport attachmentIE = new AttachmentImportExport(userDetail);
    GEDImportExport gedIE = null;
    if (listItemsToExport != null && !listItemsToExport.isEmpty()) {
      String componentId = listItemsToExport.get(0).getValue();
      gedIE = ImportExportFactory.createGEDImportExport(userDetail, componentId);
    }

    // Parcours des publications à exporter
    for (WAAttributeValuePair attValue : listItemsToExport) {
      String pubId = attValue.getName();
      String componentId = attValue.getValue();
      PublicationPK pk = new PublicationPK(pubId, componentId);

      if (nodeRootPK == null || !StringUtil.isDefined(nodeRootPK.getId())) {
        // exporting all attachments in same directory
        exportAttachments(attachmentIE, null, pk, "", exportPath);
      } else {
        // exporting attachments in directories according to place of publications
        List<NodePK> folderPKs = gedIE.getAllTopicsOfPublication(pk);
        // add place of aliases
        folderPKs.addAll(gedIE.getAliases(pk));
        NodePK rightFolderPK = null;
        for (NodePK folderPK : folderPKs) {
          if (folderPK.getInstanceId().equals(nodeRootPK.getInstanceId())) {
            List<NodeDetail> listNodes = new ArrayList<>(nodeImportExport.getPathOfNode(folderPK));
            Collections.reverse(listNodes);
            for (NodeDetail nodeDetail : listNodes) {
              if (nodeDetail.getNodePK().equals(nodeRootPK)) {
                rightFolderPK = folderPK;
                break;
              }
            }
            if (rightFolderPK != null) {
              String attachmentsExportPath =
                  createDirectoryPathForExport(exportPath, nodeRootPK, rightFolderPK, true);
              exportAttachments(attachmentIE, null, pk, "", attachmentsExportPath);
              break;
            }
          }
        }
      }
    }
  }

  public List<AttachmentDetail> processPDFExport(ExportPDFReport exportReport,
      UserDetail userDetail, List<WAAttributeValuePair> listItemsToExport, String exportPath,
      boolean useNameForFolders, NodePK rootPK) throws ImportExportException, IOException {
    AttachmentImportExport attachmentIE = new AttachmentImportExport(userDetail);
    List<AttachmentDetail> result = new ArrayList<>();

    // Parcours des publications à exporter
    for (WAAttributeValuePair attValue : listItemsToExport) {
      String pubId = attValue.getName();
      String componentId = attValue.getValue();
      GEDImportExport gedIE = ImportExportFactory.createGEDImportExport(userDetail, componentId);

      // Récupération du PublicationType
      PublicationType publicationType = gedIE.getPublicationCompleteById(pubId, componentId);
      PublicationDetail publicationDetail = publicationType.getPublicationDetail();
      fillPublicationType(gedIE, publicationType, rootPK);

      List<AttachmentDetail> attachments = attachmentIE
          .getAttachments(publicationDetail.getPK(), exportPath, null, "pdf");

      if (attachments != null && !attachments.isEmpty()) {
        result.addAll(attachments);
      }
    }

    return result;
  }

  /**
   * Méthode créant l'arborescence des répertoires pour une publication exportée
   * @param exportPath - dossier dans lequel creer notre arborescence de dossiers
   * @param componentId - id du composant de la publication
   * @param componentLabel - label du composant
   * @param pub - la publication
   * @return le chemin relatif créé
   */
  private String createPathDirectoryForKmaxPublicationExport(String exportPath, String componentId,
      String componentLabel, PublicationDetail pub, boolean useNameForFolders) throws IOException {
    String pubNameForm;
    if (useNameForFolders) {
      pubNameForm = DirectoryUtils.formatToDirectoryNamingCompliant(pub.getName());
    } else {
      pubNameForm = pub.getPK().getId();
    }

    String componentLabelForm;
    if (useNameForFolders || isKmax(componentId)) {
      componentLabelForm = DirectoryUtils.formatToDirectoryNamingCompliant(componentLabel);
    } else {
      componentLabelForm = componentId;
    }

    // ZIP API manage only ASCII characters. So directories are created in ASCII too.
    String relativeExportPathAscii = FileServerUtils.replaceAccentChars(
        componentLabelForm + separator + pubNameForm);
    createASCIIPath(exportPath + File.separatorChar + componentLabelForm + separator + pubNameForm);

    return relativeExportPathAscii;
  }

  /**
   * Méthode métier du moteur d'importExport créant toutes les publications unitaires définies au
   * niveau du fichier d'import xml passé en paramètre au moteur d'importExport.
   */
  public void processImport(PublicationsType publicationsType, ImportSettings settings,
      ImportReportManager reportManager) {
    GEDImportExport gedIE =
        ImportExportFactory.createGEDImportExport(settings.getUser(), settings.getComponentId());
    AttachmentImportExport attachmentIE = new AttachmentImportExport(gedIE.getCurrentUserDetail());
    VersioningImportExport versioningIE = new VersioningImportExport(settings.getUser());

    List<PublicationType> listPub_Type = publicationsType.getListPublicationType();
    List<Integer> nodesKmax = new ArrayList<>();
    List<NodePositionType> nodes = new ArrayList<>();
    UserDetail userDetail = settings.getUser();

    // On parcours les objets PublicationType
    for (PublicationType pubType : listPub_Type) {
      String componentId;
      // On détermine si on doit utiliser le componentId par défaut
      if (pubType.getComponentId() == null || pubType.getComponentId().length() == 0) {
        componentId = settings.getComponentId();
      } else {
        componentId = pubType.getComponentId();
      }
      gedIE.setCurrentComponentId(componentId);

      // Création du rapport unitaire
      UnitReport unitReport = new UnitReport();
      reportManager.addUnitReport(unitReport, componentId);
      ComponentInst componentInst =
          OrganizationControllerProvider.getOrganisationController().getComponentInst(componentId);
      if (componentInst == null) {
        // le composant n'existe pas
        unitReport.setError(UnitReport.ERROR_NOT_EXISTS_COMPONENT);
        unitReport.setStatus(UnitReport.STATUS_PUBLICATION_NOT_CREATED);
      } else {
        reportManager.setComponentName(componentId, componentInst.getLabel());

        PublicationDetail pubDetailToCreate = pubType.getPublicationDetail();
        List<AttachmentDetail> attachments = null;
        List<Document> documents = null;
        if (pubType.getAttachmentsType() != null) {
          attachments = pubType.getAttachmentsType().getListAttachmentDetail();
        }
        if (pubType.getDocumentsType() != null) {
          documents = pubType.getDocumentsType().getListDocuments();
        }

        if (pubDetailToCreate == null) {
          // Le mapping ne contient pas l'élément publicationHeader
          if (pubType.getId() == -1) {
            // Ce n'est pas une publication à mettre à jour par id
            if (attachments != null && !attachments.isEmpty()) {
              AttachmentDetail attachment = attachments.get(0);
              File file = new File(attachment.getPhysicalName());
              pubDetailToCreate =
                  PublicationImportExport.convertFileInfoToPublicationDetail(file, settings);
            } else {/* TODO: jeter exception ou trouver une autre solution de nommage */
              pubDetailToCreate = new PublicationDetail("unknown", "pub temp", "description",
                  new Date(),
                  new Date(), null, userDetail.getId(), "1", null, null, null);
            }
          } else {
            // C'est une publication à mettre à jour par id
            pubDetailToCreate = new PublicationDetail();
          }
        }
        if (pubDetailToCreate != null) {
          pubDetailToCreate.setPk(new PublicationPK(null, "useless", componentId));
          // Le mapping contient l'élément publicationHeader
          if (pubType.getId() != -1) {
            pubDetailToCreate.getPK().setId(java.lang.Integer.toString(pubType.getId()));
          }
          // Vérifie les données nécessaires à la création de la publication
          checkPublication(pubDetailToCreate, userDetail);

          // Specific classify for kmax
          if (isKmax(componentId)) {
            // kmax : Get coordinates with value (name of the position)
            List<Coordinate> coordinates = pubType.getCoordinatesPositionsType().
                getCoordinatesPositions();
            boolean createCoordinateAllowed =
                pubType.getCoordinatesPositionsType().getCreateEnable();
            if (coordinates != null) {
              for (Coordinate coordinate : coordinates) {
                if (coordinate != null) {
                  Collection<CoordinatePointType> listCoordinatePointsType = coordinate.
                      getCoordinatePoints();
                  if (listCoordinatePointsType != null) {
                    StringBuilder coordinatePointsPath = new StringBuilder("");
                    boolean first = true;
                    for (CoordinatePointType coordinatePointType : listCoordinatePointsType) {
                      if (StringUtil.isDefined(coordinatePointType.getValue())) {
                        // Get NodeDetail by his name
                        NodeDetail nodeDetail = coordinateImportExport
                            .getNodeDetailByName(coordinatePointType.getValue(),
                                coordinatePointType.getAxisId(), componentId);
                        if (nodeDetail == null && createCoordinateAllowed) {
                          NodeDetail position =
                              new NodeDetail("toDefine", coordinatePointType.getValue(), "", null,
                                  userDetail.getId(), null, NodePK.ROOT_NODE_ID,
                                  String.valueOf(coordinatePointType.getAxisId()), null);
                          nodeDetail = coordinateImportExport.addPosition(position,
                              String.valueOf(coordinatePointType.getAxisId()), componentId);
                        }
                        if (nodeDetail != null) {
                          if (first) {
                            coordinatePointsPath.append(nodeDetail.getPath()).append(nodeDetail.
                                getId());
                            first = false;
                          } else {
                            coordinatePointsPath.append(",").append(nodeDetail.getPath()).
                                append(nodeDetail.getId());
                          }
                        }
                      }
                    }
                    // Add coordinate (set of coordinatePoints)
                    int coordinateId =
                        coordinateImportExport.addPositions(componentId, coordinatePointsPath.
                            toString());
                    if (coordinateId == 0) {
                      unitReport.setError(UnitReport.ERROR_INCORRECT_CLASSIFICATION_ON_COMPONENT);
                    } else {
                      nodesKmax.add(coordinateId);
                    }
                  }
                }
              }
            }
          } else // récupère les thèmes dans lesquels la publication doit être créée
          {
            nodes = pubType.getNodePositionsType().getListNodePositionType();
          }

          // Création ou modification de la publication
          PublicationDetail pubDetail =
              gedIE.createPublicationForUnitImport(unitReport, settings, pubDetailToCreate, nodes);
          try {
            if (pubDetail != null) {
              unitReport.setLabel(pubDetail.getPK().getId());

              if (isKmax(componentId)) {
                PublicationImportExport.addNodesToPublication(pubDetail.getPK(), nodesKmax);
              }
              // traitement du contenu de la publi
              if (pubType.getPublicationContentType() != null) {
                try {
                  gedIE.createPublicationContent(reportManager, unitReport,
                      Integer.parseInt(pubDetail.getId()), pubType.getPublicationContentType(),
                      userDetail.getId(), pubDetail.getLanguage());
                } catch (ImportExportException ex) {
                  if (unitReport.getError() == UnitReport.ERROR_NO_ERROR) {
                    unitReport.setError(UnitReport.ERROR_CANT_CREATE_CONTENT);
                  }
                }
              }
              // process the publication's attachments
              long maximumFileSize = FileRepositoryManager.getUploadMaximumFileSize();

              if (attachments != null) {

                //New list of attachments whose size does not exceed the limit
                List<AttachmentDetail> attachmentsSizeOk = new ArrayList<AttachmentDetail>();
                for (AttachmentDetail attdetail : attachments) {
                  long fileSize = attdetail.getSize();
                  if (fileSize > maximumFileSize) {
                    unitReport.setError(UnitReport.ERROR_FILE_SIZE_EXCEEDS_LIMIT);
                  } else {
                    MetaData metaData = null;
                    if (AttachmentSettings.isUseFileMetadataForAttachmentDataEnabled() &&
                        settings.isPoiUsed()) {
                      // extract title, subject and keywords
                      metaData = MetadataExtractor.get()
                          .extractMetadata(attdetail.getAttachmentPath());
                      if (!StringUtil.isDefined(attdetail.getTitle()) &&
                          StringUtil.isDefined(metaData.getTitle())) {
                        attdetail.setTitle(metaData.getTitle());
                      }
                      if (!StringUtil.isDefined(attdetail.getInfo()) &&
                          StringUtil.isDefined(metaData.getSubject())) {
                        attdetail.setInfo(metaData.getSubject());
                      }
                    }
                    if (AttachmentSettings.isUseFileMetadataForAttachmentDataEnabled() &&
                        settings.useFileDates()) {
                      // extract creation date
                      if (metaData == null) {
                        metaData = MetadataExtractor.get()
                            .extractMetadata(attdetail.getAttachmentPath());
                      }
                      if (metaData.getCreationDate() != null) {
                        attdetail.setCreationDate(metaData.getCreationDate());
                      }
                    }
                    attachmentsSizeOk.add(attdetail);
                  }
                }

                List<AttachmentDetail> copiedAttachments;
                if (ImportExportHelper.isVersioningUsed(componentInst)) {
                  copiedAttachments = attachmentsSizeOk;
                  versioningIE.importDocuments(pubDetail.getId(), componentId, attachmentsSizeOk,
                      pubDetail.isIndexable());
                } else {
                  // Ajout des attachments
                  copiedAttachments = attachmentIE
                      .importAttachments(pubDetail.getId(), componentId, attachmentsSizeOk,
                          pubDetail.isIndexable());
                  if (copiedAttachments.size() != attachmentsSizeOk.size()) {
                    unitReport.setError(UnitReport.ERROR_NOT_EXISTS_OR_INACCESSIBLE_FILE);
                  }
                }
                reportManager.
                    addNumberOfFilesProcessed(copiedAttachments.size());
                reportManager
                    .addNumberOfFilesNotImported(attachments.size() - copiedAttachments.size());

                // On additionne la taille des fichiers importés au niveau du rapport
                for (AttachmentDetail attdetail : copiedAttachments) {
                  reportManager.addImportedFileSize(attdetail.getSize(), componentId);
                }
              }
              if (documents != null && ImportExportHelper.isVersioningUsed(componentInst)) {
                // Get number of versions
                int nbFiles = 0;

                //New list of versions whose size does not exceed the limit
                List<Document> documentsSizeOk = new ArrayList<>();
                for (Document documentDetail : documents) {
                  nbFiles += documentDetail.getVersionsType().getListVersions().size();
                  List<DocumentVersion> documentVersionsSizeOk = new ArrayList<>();

                  List<DocumentVersion> documentVersions = documentDetail.getVersionsType().
                      getListVersions();
                  for (DocumentVersion documentVersionDetail : documentVersions) {
                    long fileSize = documentVersionDetail.getSize();
                    if (fileSize > maximumFileSize) {
                      unitReport.setError(UnitReport.ERROR_FILE_SIZE_EXCEEDS_LIMIT);
                    } else {
                      documentVersionsSizeOk.add(documentVersionDetail);
                    }
                  }
                  documentDetail.getVersionsType().setListVersions(documentVersionsSizeOk);
                  documentsSizeOk.add(documentDetail);
                }

                // Copy files on disk, set info on each version
                List<SimpleDocument> copiedFiles = versioningIE.
                    importDocuments(new ForeignPK(pubDetail.getId(), componentId), documentsSizeOk,
                        Integer.parseInt(userDetail.getId()),
                        ImportExportHelper.isIndexable(pubDetail));
                reportManager.addNumberOfFilesProcessed(copiedFiles.size());
                reportManager.addNumberOfFilesNotImported(nbFiles - copiedFiles.
                    size());

                // Create documents and versions in DB
                // On additionne la taille des fichiers importés au niveau du rapport
                for (SimpleDocument version : copiedFiles) {
                  reportManager.addImportedFileSize(version.getSize(), componentId);
                }
              }

              // traitement du classement PDC
              try {
                int silverObjectId = gedIE.getSilverObjectId(pubDetail.getId());

                // Ajout au plan de classement s'il y en a
                if (pubType.getPdcPositionsType() != null) {
                  boolean pdcOK = pdcImportExport
                      .addPositions(silverObjectId, componentId, pubType.getPdcPositionsType());
                  if (!pdcOK) {
                    unitReport.setError(UnitReport.ERROR_INCORRECT_CLASSIFICATION_ON_COMPONENT);
                  }
                }

                List<ClassifyPosition> positions =
                    pdcImportExport.getPositions(silverObjectId, componentId);
                if (positions == null) {
                  // La publication n'est pas classée sur le PDC
                  // Si le mode brouillon est activé et que le classement est obligatoire, la
                  // publication passe en mode "Draft"
                  if (pdcImportExport.isClassifyingMandatory(componentId) &&
                      ImportExportHelper.isDraftUsed(componentInst)) {
                    gedIE.publicationNotClassifiedOnPDC(pubDetail.getId());
                  }
                }
              } catch (Exception e) {
                unitReport.setError(UnitReport.ERROR_INCORRECT_CLASSIFICATION_ON_COMPONENT);
              }
            }
          } catch (Exception ex) {
            SilverLogger.getLogger(this).error(ex.getMessage(), ex);
            unitReport.setError(UnitReport.ERROR_ERROR);
            SilverpeasTransverseErrorUtil
                .throwTransverseErrorIfAny(ex, userDetail.getUserPreferences().getLanguage());
          }
        }
      }
    }
  }

  private void checkPublication(PublicationDetail publication, UserDetail userDetail) {
    if (publication.getCreationDate() == null) {
      publication.setCreationDate(new Date());
    }
    publication
        .setCreatorId(ImportExportHelper.checkUserId(publication.getCreatorId(), userDetail));
    publication
        .setUpdaterId(ImportExportHelper.checkUserId(publication.getUpdaterId(), userDetail));
    if (publication.getImportance() == 0) {
      publication.setImportance(5);
    }
  }

  private boolean isKmax(String currentComponentId) {
    return currentComponentId.startsWith("kmax");
  }

  public void fillPublicationType(GEDImportExport gedIE, PublicationType publicationType,
      NodePK rootPK) throws ImportExportException {
    PublicationPK pk = new PublicationPK(String.valueOf(publicationType.getId()), publicationType.
        getComponentId());
    publicationType.setNodePositionsType(new NodePositionsType());
    List<NodePositionType> listNodePos = new ArrayList<>();
    List<NodePK> listNodePK = gedIE.getAllTopicsOfPublication(pk);
    if (rootPK != null && !rootPK.getInstanceId().equals(pk.getInstanceId())) {
      // it's an alias, process only aliases
      listNodePK = gedIE.getAliases(pk);
    }
    for (NodePK nodePK : listNodePK) {
      if (rootPK == null || nodePK.getInstanceId().equals(rootPK.getInstanceId())) {
        NodePositionType nodePos = new NodePositionType();
        nodePos.setId(Integer.parseInt(nodePK.getId()));
        listNodePos.add(nodePos);
      }
    }
    if (listNodePos.isEmpty()) {
      NodePositionType nodePos = new NodePositionType();
      nodePos.setId(Integer.parseInt(NodePK.UNCLASSED_NODE_ID));
      listNodePos.add(nodePos);
    }
    publicationType.getNodePositionsType().setListNodePositionType(listNodePos);
  }

  public int getNbThemes(GEDImportExport gedIE, PublicationType publicationType, NodePK rootPK)
      throws ImportExportException {
    int nbThemes = 1;
    List<NodePositionType> positions = publicationType.getNodePositionsType().
        getListNodePositionType();
    if (positions != null && !positions.isEmpty()) {
      String instanceId = publicationType.getComponentId();
      if (rootPK != null) {
        instanceId = rootPK.getInstanceId();
      }
      NodePK pk = new NodePK(String.valueOf(positions.get(0).getId()), instanceId);
      nbThemes = gedIE.getTopicTree(pk).size();
    }
    return nbThemes;
  }
}
