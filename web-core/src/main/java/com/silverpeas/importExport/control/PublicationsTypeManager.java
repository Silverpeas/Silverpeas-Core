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
package com.silverpeas.importExport.control;

import com.silverpeas.attachment.importExport.AttachmentImportExport;
import com.silverpeas.attachment.importExport.AttachmentsType;
import com.silverpeas.coordinates.importExport.CoordinateImportExport;
import com.silverpeas.coordinates.importExport.CoordinatePointType;
import com.silverpeas.coordinates.importExport.CoordinatesPositionsType;
import com.silverpeas.form.importExport.XMLField;
import com.silverpeas.importExport.model.ImportExportException;
import com.silverpeas.importExport.model.PublicationType;
import com.silverpeas.importExport.model.PublicationsType;
import com.silverpeas.importExport.report.ExportPDFReport;
import com.silverpeas.importExport.report.ExportReport;
import com.silverpeas.importExport.report.HtmlExportPublicationGenerator;
import com.silverpeas.importExport.report.ImportReportManager;
import com.silverpeas.importExport.report.UnitReport;
import com.silverpeas.node.importexport.NodeImportExport;
import com.silverpeas.node.importexport.NodePositionType;
import com.silverpeas.node.importexport.NodePositionsType;
import com.silverpeas.pdc.importExport.PdcImportExport;
import com.silverpeas.pdc.importExport.PdcPositionsType;
import com.silverpeas.publication.importExport.DBModelContentType;
import com.silverpeas.publication.importExport.PublicationContentType;
import com.silverpeas.publication.importExport.XMLModelContentType;
import com.silverpeas.util.ForeignPK;
import com.silverpeas.util.StringUtil;
import com.silverpeas.versioning.importExport.VersioningImportExport;
import com.silverpeas.wysiwyg.importExport.WysiwygContentType;
import com.stratelia.silverpeas.pdc.model.ClassifyPosition;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.silverpeas.versioning.model.Document;
import com.stratelia.silverpeas.versioning.model.DocumentVersion;
import com.stratelia.silverpeas.wysiwyg.control.WysiwygController;
import com.stratelia.webactiv.beans.admin.ComponentInst;
import com.stratelia.webactiv.beans.admin.OrganizationController;
import com.stratelia.webactiv.beans.admin.UserDetail;
import com.stratelia.webactiv.util.FileRepositoryManager;
import com.stratelia.webactiv.util.FileServerUtils;
import com.stratelia.webactiv.util.WAAttributeValuePair;
import com.stratelia.webactiv.util.attachment.control.AttachmentController;
import com.stratelia.webactiv.util.attachment.ejb.AttachmentPK;
import com.stratelia.webactiv.util.attachment.model.AttachmentDetail;
import com.stratelia.webactiv.util.coordinates.model.Coordinate;
import com.stratelia.webactiv.util.fileFolder.FileFolderManager;
import com.stratelia.webactiv.util.node.model.NodeDetail;
import com.stratelia.webactiv.util.node.model.NodePK;
import com.stratelia.webactiv.util.publication.info.model.ModelDetail;
import com.stratelia.webactiv.util.publication.model.PublicationDetail;
import com.stratelia.webactiv.util.publication.model.PublicationPK;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * Classe manager des importations unitaires du moteur d'importExport de silverPeas
 * @author sdevolder
 */
public class PublicationsTypeManager {

  public PublicationsType processExport(ExportReport exportReport, UserDetail userDetail,
      List<WAAttributeValuePair> listItemsToExport, String exportPath) throws ImportExportException,
      IOException {
    return processExport(exportReport, userDetail, listItemsToExport, exportPath, true);
  }

  /**
   * Méthode métier du moteur d'importExport créant une exportation pour toutes les publications
   * spécifiées en paramètre. passé en paramètre au moteur d'importExport.
   * @param exportReport 
   * @param userDetail - contient les informations sur l'utilisateur du moteur d'importExport
   * @param listItemsToExport - liste des WAAttributeValuePair contenant les id des publications à
   * exporter
   * @param exportPath - cible de l'exportation
   * @param useNameForFolders 
   * @return
   * @throws ImportExportException
   * @throws IOException  
   */
  public PublicationsType processExport(ExportReport exportReport, UserDetail userDetail,
      List<WAAttributeValuePair> listItemsToExport, String exportPath, boolean useNameForFolders)
      throws ImportExportException, IOException {
    AttachmentImportExport attachmentIE = new AttachmentImportExport();
    VersioningImportExport versioningIE = new VersioningImportExport();
    PublicationsType publicationsType = new PublicationsType();
    List<PublicationType> listPubType = new ArrayList<PublicationType>();
    PdcImportExport pdc_impExp = new PdcImportExport();
    OrganizationController orgaController = new OrganizationController();
    String wysiwygText = null;

    SilverTrace.debug("importExport", "PublicationTypeManager.processExport",
        "root.MSG_GEN_PARAM_VALUE", "useNameForFolders = " + useNameForFolders);

    // Parcours des publications à exporter
    for (WAAttributeValuePair attValue : listItemsToExport) {
      SilverTrace.debug("importExport", "PublicationTypeManager.processExport",
          "root.MSG_GEN_PARAM_VALUE", "objectId = " + attValue.getName() + ", instanceId = "
          + attValue.getValue());

      String pubId = attValue.getName();
      String componentId = attValue.getValue();
      ComponentInst componentInst = orgaController.getComponentInst(componentId);
      GEDImportExport gedIE = ImportExportFactory.createGEDImportExport(userDetail, componentId);
      // Récupération du PublicationType
      PublicationType publicationType = gedIE.getPublicationCompleteById(pubId, componentId);
      SilverTrace.debug("importExport", "PublicationTypeManager.processExport",
          "root.MSG_GEN_PARAM_VALUE", "publicationType retrieved");
      PublicationDetail publicationDetail = publicationType.getPublicationDetail();
      listPubType.add(publicationType);

      // Récupération des topics (il y en a au moins un)
      String exportPublicationRelativePath = null;
      String exportPublicationPath = null;
      if (gedIE.isKmax()) {
        publicationType.setCoordinatesPositionsType(new CoordinatesPositionsType());
        exportPublicationRelativePath = createPathDirectoryForKmaxPublicationExport(exportPath,
            componentId, componentInst.getLabel(), publicationDetail, useNameForFolders);
        exportPublicationPath = exportPath + File.separator + exportPublicationRelativePath;
      } else {
        publicationType.setNodePositionsType(new NodePositionsType());
        List<NodePositionType> listNodePos = new ArrayList<NodePositionType>();
        List<NodePK> listNodePK = gedIE.getAllTopicsOfPublication(pubId, componentId);
        for (NodePK nodePK : listNodePK) {
          NodePositionType nodePos = new NodePositionType();
          nodePos.setId(Integer.parseInt(nodePK.getId()));
          listNodePos.add(nodePos);
        }
        publicationType.getNodePositionsType().setListNodePositionType(listNodePos);
        SilverTrace.debug("importExport", "PublicationTypeManager.processExport",
            "root.MSG_GEN_PARAM_VALUE", "nodePositions added");

        // Création de l'arborescence de dossiers pour la création de l'export de publication
        NodePositionType nodePositionType = publicationType.getNodePositionsType().
            getListNodePositionType().get(0);
        exportPublicationRelativePath = createPathDirectoryForPublicationExport(
            exportPath, nodePositionType.getId(), componentId, componentInst.getLabel(),
            publicationDetail, useNameForFolders);
        exportPublicationPath = exportPath + File.separator + exportPublicationRelativePath;
      }

      // Copie des fichiers de contenu s'il en existe
      PublicationContentType pubContent = publicationType.getPublicationContentType();
      ModelDetail modelDetail = null;
      if (pubContent != null) {
        DBModelContentType dbModelContent = pubContent.getDBModelContentType();
        WysiwygContentType wysiwygContent = pubContent.getWysiwygContentType();
        XMLModelContentType xmlModel = pubContent.getXMLModelContentType();
        if (dbModelContent != null) {
          List<String> listImageParts = dbModelContent.getListImageParts();
          if (listImageParts != null && !listImageParts.isEmpty()) {
            listImageParts = gedIE.copyDBmodelImagePartsForExport(exportPublicationPath,
                listImageParts, exportPublicationRelativePath);
            dbModelContent.setListImageParts(listImageParts);
          }
          modelDetail = gedIE.getModelDetail(dbModelContent.getId());
        } else if (wysiwygContent != null) {
          wysiwygText = exportWysiwygContent(pubId, componentId, gedIE,
              exportPublicationRelativePath, exportPublicationPath, wysiwygContent);
        } else if (xmlModel != null) {
          exportXmlForm(componentId, exportPublicationRelativePath, exportPublicationPath,
              xmlModel);
        }
      }
      exportAttachments(attachmentIE, versioningIE, componentInst, publicationType,
          publicationDetail, exportPublicationRelativePath, exportPublicationPath);
      exportPdc(pdc_impExp, pubId, gedIE, publicationType);
      if (!writePublicationHtml(exportReport, wysiwygText, pubId, publicationType,
          exportPublicationRelativePath, exportPublicationPath, modelDetail)) {
        return null;
      }
      wysiwygText = null;
    }
    publicationsType.setListPublicationType(listPubType);
    return publicationsType;
  }

  String exportWysiwygContent(String pubId, String componentId, GEDImportExport gedIE,
      String exportPublicationRelativePath, String exportPublicationPath,
      WysiwygContentType wysiwygContent) throws ImportExportException {
    String wysiwygText;
    String wysiwygFileName = wysiwygContent.getPath();
    gedIE.copyWysiwygImageForExport(pubId, componentId, exportPublicationPath);

    try {
      wysiwygText = WysiwygController.loadFileAndAttachment("useless", componentId, pubId);
      wysiwygText = HtmlExportPublicationGenerator.replaceImagesPathForExport(wysiwygText);
      if (wysiwygText == null) {
        wysiwygText = ""; // To avoid exception in createFile below
      }
      // Enregistrement du nouveau fichier généré
      FileFolderManager.createFile(exportPublicationPath, wysiwygFileName, wysiwygText);
    } catch (Exception e) {
      throw new ImportExportException("importExport",
          "importExport.EX_CANT_GET_WYSIWYG", e);
    }

    wysiwygContent.setPath(exportPublicationRelativePath + File.separator
        + wysiwygFileName);
    return wysiwygText;
  }

  boolean writePublicationHtml(ExportReport exportReport, String wysiwygText, String pubId,
      PublicationType publicationType, String exportPublicationRelativePath,
      String exportPublicationPath, ModelDetail modelDetail) {
    String htmlNameIndex = "index.html";
    HtmlExportPublicationGenerator s = new HtmlExportPublicationGenerator(publicationType,
        modelDetail, wysiwygText, exportPublicationRelativePath + File.separator + htmlNameIndex);
    exportReport.addHtmlIndex(pubId, s);
    File fileHTML = new File(exportPublicationPath + File.separator + htmlNameIndex);
    SilverTrace.debug("importExport", "PublicationTypeManager.processExport",
        "root.MSG_GEN_PARAM_VALUE", "pubId = " + pubId);
    Writer fileWriter = null;
    try {
      fileHTML.createNewFile();
      fileWriter = new OutputStreamWriter(new FileOutputStream(fileHTML), "UTF-8");
      fileWriter.write(s.toHtml());
    } catch (IOException ex) {
      return false;
    } finally {
      try {
        fileWriter.close();
      } catch (Exception ex) {
        SilverTrace.debug("importExport", "PublicationTypeManager.processExport",
            "root.MSG_GEN_PARAM_VALUE", "Exception = " + ex);
      }
    }
    return true;
  }

  void exportPdc(PdcImportExport pdc_impExp, String pubId, GEDImportExport gedIE,
      PublicationType publicationType) throws ImportExportException {
    // Récupération du classement pdc
    try {
      List<ClassifyPosition> listClassifyPostion = pdc_impExp.getPositions(gedIE.getSilverObjectId(
          pubId), publicationType.getComponentId());
      if (listClassifyPostion != null && !listClassifyPostion.isEmpty()) {
        publicationType.setPdcPositionsType(new PdcPositionsType());
        publicationType.getPdcPositionsType().setListClassifyPosition(listClassifyPostion);
      }
    } catch (Exception ex) {
      // En cas d"objet non trouvé: pas d'exception gérée par le système
      throw new ImportExportException("importExport", "importExport.EX_CANT_GET_PDC_POSITION", ex);
    }
  }

  void exportAttachments(AttachmentImportExport attachmentIE, VersioningImportExport versioningIE,
      ComponentInst componentInst, PublicationType publicationType,
      PublicationDetail publicationDetail, String exportPublicationRelativePath,
      String exportPublicationPath) throws ImportExportException {
    // Récupération des attachments et copie des fichiers
    try {
      List<AttachmentDetail> attachments;
      if (ImportExportHelper.isVersioningUsed(componentInst)) {
        attachments = versioningIE.exportDocuments(publicationDetail.getPK(),
            exportPublicationPath, exportPublicationRelativePath, null);
      } else {
        attachments = attachmentIE.getAttachments(publicationDetail.getPK(),
            exportPublicationPath, exportPublicationRelativePath, null);
      }
      if (attachments != null && attachments.size() > 0) {
        publicationType.setAttachmentsType(new AttachmentsType());
        publicationType.getAttachmentsType().setListAttachmentDetail(attachments);
      }
    } catch (Exception ex) {
      // En cas d"objet non trouvé: pas d'exception gérée par le système
      throw new ImportExportException("importExport",
          "importExport.EX_CANT_GET_ATTACHMENTS", ex);
    }
  }

  void exportXmlForm(String componentId, String exportPublicationRelativePath,
      String exportPublicationPath, XMLModelContentType xmlModel) {
    List<XMLField> xmlFields = xmlModel.getFields();
    String value = null;
    for (XMLField xmlField : xmlFields) {
      value = xmlField.getValue();
      if (StringUtil.isDefined(value)) {
        if (value.startsWith("xmlWysiwygField")) {
          String wysiwygFile = value.substring(value.indexOf('_') + 1);
          try {
            String fromPath = FileRepositoryManager.getAbsolutePath(componentId)
                + "xmlWysiwyg" + File.separator + wysiwygFile;
            FileRepositoryManager.copyFile(fromPath,
                exportPublicationPath + File.separator + wysiwygFile);
          } catch (Exception e) {
            SilverTrace.warn("importExport", "PublicationTypeManager.processExport",
                "root.EX_CANT_WRITE_FILE_XMLWYSIWYG", e);
          }

        } else if (value.startsWith("image")) {
          String imageId = value.substring(value.indexOf('_') + 1, value.length());
          AttachmentDetail attachment = null;
          try {
            attachment = AttachmentController.searchAttachmentByPK(new AttachmentPK(imageId,
                componentId));
          } catch (RuntimeException e1) {
            SilverTrace.warn("importExport", "PublicationTypeManager.processExport",
                "root.EX_CANT_WRITE_FILE", e1);
          }

          if (attachment != null) {
            String[] context = FileRepositoryManager.getAttachmentContext("XMLFormImages");
            String fromPath = FileRepositoryManager.getAbsolutePath(componentId, context)
                + attachment.getPhysicalName();

            try {
              FileRepositoryManager.copyFile(fromPath, exportPublicationPath + File.separator
                  + attachment.getLogicalName());
            } catch (Exception e) {
              SilverTrace.warn("importExport", "PublicationTypeManager.processExport",
                  "root.EX_CANT_WRITE_FILE", e);
            }
            xmlField.setValue(exportPublicationRelativePath + File.separator + attachment.
                getLogicalName());
          }
        } else if (value.startsWith("file")) {
          String fileId = value.substring(value.indexOf('_') + 1, value.length());

          AttachmentDetail attachment = null;
          try {
            attachment = AttachmentController.searchAttachmentByPK(new AttachmentPK(fileId,
                componentId));
          } catch (RuntimeException e1) {
            SilverTrace.warn("importExport", "PublicationTypeManager.processExport",
                "root.EX_CANT_WRITE_FILE", e1);
          }
          if (attachment != null) {
            xmlField.setValue(exportPublicationRelativePath + File.separator + attachment.
                getLogicalName());
          }
        }
      }
    }
  }

  /**
   * Méthode créant l'arboresecne des répertoires pour une publication exportée
   * @param exportPath - dossier dans lequel creer notre arborescence de dossiers
   * @param topicId - id du topic dont on veut la branche
   * @param componentId - id du composant de la publication
   * @param componentLabel - label du composant
   * @param pubName - nom de la publication
   * @return le chemin relatif créé
   */
  private String createPathDirectoryForPublicationExport(String exportPath, int topicId,
      String componentId, String componentLabel, PublicationDetail pub, boolean useNameForFolders)
      throws IOException {
    String pubNameForm;
    if (useNameForFolders) {
      pubNameForm = DirectoryUtils.formatToDirectoryNamingCompliant(pub.getName());
    } else {
      pubNameForm = pub.getPK().getId();
    }

    String componentLabelForm = "";
    if (useNameForFolders || isKmax(componentId)) {
      componentLabelForm = DirectoryUtils.formatToDirectoryNamingCompliant(componentLabel);
    } else {
      componentLabelForm = componentId;
    }

    NodeImportExport nodeIE = new NodeImportExport();
    StringBuilder relativeExportPath = new StringBuilder(componentLabelForm);
    StringBuilder pathToCreate = new StringBuilder(exportPath).append(File.separatorChar).append(
        componentLabelForm);
    List<NodeDetail> listNodes = new ArrayList<NodeDetail>(nodeIE.getPathOfNode(new NodePK(String.
        valueOf(topicId), "useless", componentId)));
    Collections.reverse(listNodes);
    for (NodeDetail nodeDetail : listNodes) {
      String nodeNameForm;
      if (useNameForFolders) {
        nodeNameForm = DirectoryUtils.formatToDirectoryNamingCompliant(nodeDetail.getName());
      } else {
        nodeNameForm = nodeDetail.getNodePK().getId();
      }
      pathToCreate.append(File.separator).append(nodeNameForm);
      relativeExportPath.append(File.separator).append(nodeNameForm);
    }
    relativeExportPath.append(File.separator).append(pubNameForm);
    pathToCreate.append(File.separator).append(pubNameForm);

    // L'api zip ne prends que les caractères ascii, aussi pour être
    // cohérent, on crée nos dossiers comme tel
    String relativeExportPathAscii =
        FileServerUtils.replaceAccentChars(relativeExportPath.toString());
    SilverTrace.debug("importExport",
        "PublicationTypeManager.createPathDirectoryForPublicationExport",
        "root.MSG_GEN_PARAM_VALUE", "relativeExportPathAscii = "
        + relativeExportPathAscii);
    // String relativeExportPathAscii = relativeExportPath.toString();
    String pathToCreateAscii = FileServerUtils.replaceAccentChars(pathToCreate.toString());
    SilverTrace.debug("importExport",
        "PublicationTypeManager.createPathDirectoryForPublicationExport",
        "root.MSG_GEN_PARAM_VALUE", "pathToCreateAscii = " + pathToCreateAscii);
    // String pathToCreateAscii = pathToCreate.toString();

    File dir = new File(pathToCreateAscii.toString());
    if (!dir.exists()) {
      boolean creationOK = dir.mkdirs();
      if (!creationOK) {
        throw new IOException();
      }
    }

    return relativeExportPathAscii.toString();
  }

  public List<AttachmentDetail> processPDFExport(ExportPDFReport exportReport, UserDetail userDetail,
      List<WAAttributeValuePair> listItemsToExport, String exportPath,
      boolean useNameForFolders) throws ImportExportException, IOException {
    AttachmentImportExport attachmentIE = new AttachmentImportExport();
    VersioningImportExport versioningIE = new VersioningImportExport();
    OrganizationController orgaController = new OrganizationController();
    List<AttachmentDetail> result = new ArrayList<AttachmentDetail>();

    // Parcours des publications à exporter
    for (WAAttributeValuePair attValue : listItemsToExport) {
      String pubId = attValue.getName();
      String componentId = attValue.getValue();
      ComponentInst componentInst = orgaController.getComponentInst(componentId);
      GEDImportExport gedIE = ImportExportFactory.createGEDImportExport(userDetail, componentId);

      // Récupération du PublicationType
      PublicationType publicationType = gedIE.getPublicationCompleteById(pubId, componentId);
      PublicationDetail publicationDetail = publicationType.getPublicationDetail();

      publicationType.setNodePositionsType(new NodePositionsType());
      List<NodePositionType> listNodePos = new ArrayList<NodePositionType>();
      List<NodePK> listNodePK = gedIE.getAllTopicsOfPublication(pubId, componentId);
      for (NodePK nodePK : listNodePK) {
        NodePositionType nodePos = new NodePositionType();
        nodePos.setId(Integer.parseInt(nodePK.getId()));
        listNodePos.add(nodePos);
      }
      publicationType.getNodePositionsType().setListNodePositionType(listNodePos);
      String exportPublicationPath = exportPath;

      try {
        List<AttachmentDetail> attachments;
        if (ImportExportHelper.isVersioningUsed(componentInst)) {
          attachments = versioningIE.exportDocuments(publicationDetail.getPK(),
              exportPublicationPath, null, "pdf");

        } else {
          attachments = attachmentIE.getAttachments(publicationDetail.getPK(),
              exportPublicationPath, null, "pdf");
        }

        if (attachments != null && !attachments.isEmpty()) {
          result.addAll(attachments);
        }

      } catch (Exception ex) {
        // En cas d'objet non trouvé: pas d'exception gérée par le système
        throw new ImportExportException("importExport",
            "importExport.EX_CANT_GET_ATTACHMENTS", ex);
      }

    }

    return result;
  }

  /**
   * Méthode créant l'arborescence des répertoires pour une publication exportée
   * @param exportPath - dossier dans lequel creer notre arborescence de dossiers
   * @param positionPath - ensemble des noeuds sur laquelle la publication est classée
   * @param componentId - id du composant de la publication
   * @param componentLabel - label du composant
   * @param pub - la publication
   * @return le chemin relatif créé
   */
  private String createPathDirectoryForKmaxPublicationExport(String exportPath,
      String componentId, String componentLabel, PublicationDetail pub,
      boolean useNameForFolders) throws IOException {
    String pubNameForm = "";
    if (useNameForFolders) {
      pubNameForm = DirectoryUtils.formatToDirectoryNamingCompliant(pub.getName());
    } else {
      pubNameForm = pub.getPK().getId();
    }

    String componentLabelForm = "";
    if (useNameForFolders || isKmax(componentId)) {
      componentLabelForm = DirectoryUtils.formatToDirectoryNamingCompliant(componentLabel);
    } else {
      componentLabelForm = componentId;
    }

    StringBuilder relativeExportPath = new StringBuilder(componentLabelForm);
    StringBuilder pathToCreate = new StringBuilder(exportPath).append(File.separatorChar).append(
        componentLabelForm);

    relativeExportPath.append(File.separator).append(pubNameForm);
    pathToCreate.append(File.separator).append(pubNameForm);

    // L'api zip ne prends que les caractères ascii, aussi pour être
    // cohérent, on crée nos dossiers comme tel
    String relativeExportPathAscii =
        FileServerUtils.replaceAccentChars(relativeExportPath.toString());
    SilverTrace.debug("importExport",
        "PublicationTypeManager.createPathDirectoryForKmaxPublicationExport",
        "root.MSG_GEN_PARAM_VALUE", "relativeExportPathAscii = "
        + relativeExportPathAscii);
    String pathToCreateAscii = FileServerUtils.replaceAccentChars(pathToCreate.toString());
    SilverTrace.debug("importExport",
        "PublicationTypeManager.createPathDirectoryForKmaxPublicationExport",
        "root.MSG_GEN_PARAM_VALUE", "pathToCreateAscii = " + pathToCreateAscii);

    File dir = new File(pathToCreateAscii.toString());
    if (!dir.exists()) {
      boolean creationOK = dir.mkdirs();
      if (!creationOK) {
        throw new IOException();
      }
    }
    return relativeExportPathAscii.toString();
  }

  public void processImport(UserDetail userDetail,
      PublicationsType publicationsType, String targetComponentId)
      throws ImportExportException {
    processImport(userDetail, publicationsType, targetComponentId, true);
  }

  /**
   * Méthode métier du moteur d'importExport créant toutes les publications unitaires définies au
   * niveau du fichier d'import xml passé en paramètre au moteur d'importExport.
   * @param userDetail - contient les informations sur l'utilisateur du moteur d'importExport
   * @param publicationsType - objet mappé par castor contenant toutes les informations de création
   * des publications de type unitaire
   * @param targetComponentId - id du composant dans lequel creer les publications unitaires
   * @param isPOIUsed  
   */
  public void processImport(UserDetail userDetail, PublicationsType publicationsType,
      String targetComponentId, boolean isPOIUsed) {
    GEDImportExport gedIE = ImportExportFactory.createGEDImportExport(userDetail, targetComponentId);
    AttachmentImportExport attachmentIE = new AttachmentImportExport();
    PdcImportExport pdcIE = new PdcImportExport();
    VersioningImportExport versioningIE = new VersioningImportExport();
    CoordinateImportExport coordinateIE = new CoordinateImportExport();

    List<PublicationType> listPub_Type = publicationsType.getListPublicationType();
    List<Integer> nodesKmax = new ArrayList<Integer>();
    List<NodePositionType> nodes = new ArrayList<NodePositionType>();
    OrganizationController orgaController = new OrganizationController();
    int nbItem = 1;

    // On parcours les objets PublicationType
    for (PublicationType pubType : listPub_Type) {
      String componentId;
      // On détermine si on doit utiliser le componentId par défaut
      if (pubType.getComponentId() == null || pubType.getComponentId().length() == 0) {
        componentId = targetComponentId;
      } else {
        componentId = pubType.getComponentId();
      }
      gedIE.setCurrentComponentId(componentId);

      // Création du rapport unitaire
      UnitReport unitReport = new UnitReport("<publication> #" + nbItem);
      ImportReportManager.addUnitReport(unitReport, componentId);
      ComponentInst componentInst = orgaController.getComponentInst(componentId);
      if (componentInst == null) {
        // le composant n'existe pas
        unitReport.setError(UnitReport.ERROR_NOT_EXISTS_COMPONENT);
        unitReport.setStatus(UnitReport.STATUS_PUBLICATION_NOT_CREATED);
      } else {
        ImportReportManager.setComponentName(componentId, componentInst.getLabel());

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
              pubDetailToCreate = PublicationImportExport.convertFileInfoToPublicationDetail(
                  userDetail, file, isPOIUsed);
            } else {/* TODO: jeter exception ou trouver une autre solution de nommage */
              pubDetailToCreate = new PublicationDetail("unknown", "pub temp", "description",
                  new Date(), new Date(), null, userDetail.getId(), "5", null, null, null);
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
            boolean createCoordinateAllowed = pubType.getCoordinatesPositionsType().getCreateEnable();
            if (coordinates != null) {
              for (Coordinate coordinate : coordinates) {
                if (coordinate != null) {
                  Collection<CoordinatePointType> listCoordinatePointsType = coordinate.
                      getCoordinatePoints();
                  if (listCoordinatePointsType != null) {
                    StringBuffer coordinatePointsPath = new StringBuffer("");
                    boolean first = true;
                    for (CoordinatePointType coordinatePointType : listCoordinatePointsType) {
                      if (StringUtil.isDefined(coordinatePointType.getValue())) {
                        // Get NodeDetail by his name
                        NodeDetail nodeDetail = coordinateIE.getNodeDetailByName(
                            coordinatePointType.getValue(), coordinatePointType.getAxisId(),
                            componentId);
                        SilverTrace.debug("importExport", "PublicationsTypeManager.processImport",
                            "root.MSG_GEN_PARAM_VALUE", "nodeDetail avant= " + nodeDetail);
                        if (nodeDetail == null && createCoordinateAllowed) {
                          NodeDetail position = new NodeDetail("toDefine",
                              coordinatePointType.getValue(), "", null, userDetail.getId(), null,
                              NodePK.ROOT_NODE_ID, String.valueOf(coordinatePointType.getAxisId()), null);
                          nodeDetail = coordinateIE.addPosition(position, String.valueOf(
                              coordinatePointType.getAxisId()), componentId);
                          SilverTrace.debug("importExport", "PublicationsTypeManager.processImport",
                              "root.MSG_GEN_PARAM_VALUE", "nodeDetail apres création= " + nodeDetail);
                        }
                        if (nodeDetail != null) {
                          if (first) {
                            coordinatePointsPath.append(nodeDetail.getPath()).append(
                                nodeDetail.getId());
                            first = false;
                          } else {
                            coordinatePointsPath.append(",").
                                append(nodeDetail.getPath()).append(nodeDetail.getId());
                          }
                        }
                      }
                    }
                    SilverTrace.debug("importExport", "PublicationsTypeManager.processImport",
                        "root.MSG_GEN_PARAM_VALUE", "coordinatePointsPath = " + coordinatePointsPath);
                    // Add coordinate (set of coordinatePoints)
                    int coordinateId = coordinateIE.addPositions(componentId, coordinatePointsPath.
                        toString());
                    if (coordinateId == 0) {
                      unitReport.setError(UnitReport.ERROR_INCORRECT_CLASSIFICATION_ON_COMPONENT);
                    } else {
                      nodesKmax.add(Integer.valueOf(coordinateId));
                    }
                  }
                }
              }
            }
            SilverTrace.debug("importExport", "PublicationsTypeManager.processImport",
                "root.MSG_GEN_PARAM_VALUE", "TOTAL List coordinatesId = " + nodesKmax);
          } else // récupère les thèmes dans lesquels la publication doit être créée
          {
            nodes = pubType.getNodePositionsType().getListNodePositionType();
          }

          // Création ou modification de la publication
          PublicationDetail pubDetail = gedIE.createPublicationForUnitImport(unitReport, userDetail,
              pubDetailToCreate, nodes);

          try {
            if (pubDetail != null) {
              if (isKmax(componentId)) {
                PublicationImportExport.addNodesToPublication(pubDetail.getPK(), nodesKmax);
              }
              // traitement du contenu de la publi
              if (pubType.getPublicationContentType() != null) {
                try {
                  gedIE.createPublicationContent(unitReport, Integer.parseInt(pubDetail.getId()),
                      pubType.getPublicationContentType(), userDetail.getId());
                } catch (ImportExportException ex) {
                  if (unitReport.getError() == UnitReport.ERROR_NO_ERROR) {
                    unitReport.setError(UnitReport.ERROR_CANT_CREATE_CONTENT);
                  }
                }
              }

              // traitement des fichiers joints à la publi
              if (attachments != null) {
                List<AttachmentDetail> copiedAttachments;
                if (ImportExportHelper.isVersioningUsed(componentInst)) {
                  // Mode versioning
                  // copie des fichiers sur le serveur et enrichissement des AttachmentDetail
                  copiedAttachments = attachmentIE.copyFiles(componentId, attachments, versioningIE.
                      getVersioningPath(componentId));
                  if (copiedAttachments.size() != attachments.size()) {
                    unitReport.setError(UnitReport.ERROR_NOT_EXISTS_OR_INACCESSIBLE_FILE);
                  }
                  versioningIE.importDocuments(pubDetail.getId(), componentId, copiedAttachments,
                      Integer.parseInt(userDetail.getId()), pubDetail.isIndexable());
                } else {
                  // Ajout des attachments
                  copiedAttachments = attachmentIE.importAttachments(pubDetail.getId(), componentId,
                      attachments, userDetail.getId(), pubDetail.isIndexable());
                  if (copiedAttachments.size() != attachments.size()) {
                    unitReport.setError(UnitReport.ERROR_NOT_EXISTS_OR_INACCESSIBLE_FILE);
                  }
                }
                ImportReportManager.addNumberOfFilesProcessed(copiedAttachments.size());
                ImportReportManager.addNumberOfFilesNotImported(attachments.size()
                    - copiedAttachments.size());

                // On additionne la taille des fichiers importés au niveau du rapport
                for (AttachmentDetail attdetail : copiedAttachments) {
                  ImportReportManager.addImportedFileSize(attdetail.getSize(), componentId);
                }
              }
              if (documents != null && ImportExportHelper.isVersioningUsed(componentInst)) {
                // Get number of versions
                int nbFiles = 0;
                for (Document document : documents) {
                  nbFiles += document.getVersionsType().getListVersions().size();
                }

                // Copy files on disk, set info on each version
                List<DocumentVersion> copiedFiles = versioningIE.copyFiles(componentId, documents, versioningIE.
                    getVersioningPath(componentId));
                ImportReportManager.addNumberOfFilesProcessed(copiedFiles.size());
                ImportReportManager.addNumberOfFilesNotImported(nbFiles - copiedFiles.size());

                // Create documents and versions in DB
                versioningIE.importDocuments(new ForeignPK(pubDetail.getId(), componentId),
                    documents, Integer.parseInt(userDetail.getId()), ImportExportHelper.isIndexable(
                    pubDetail));

                // On additionne la taille des fichiers importés au niveau du rapport
                for (DocumentVersion version : copiedFiles) {
                  ImportReportManager.addImportedFileSize(version.getSize(), componentId);
                }
              }

              // traitement du classement PDC
              try {
                int silverObjectId = gedIE.getSilverObjectId(pubDetail.getId());

                // Ajout au plan de classement s'il y en a
                if (pubType.getPdcPositionsType() != null) {
                  boolean pdcOK =
                      pdcIE.addPositions(silverObjectId, componentId, pubType.getPdcPositionsType());
                  if (!pdcOK) {
                    unitReport.setError(UnitReport.ERROR_INCORRECT_CLASSIFICATION_ON_COMPONENT);
                  }
                }

                List<ClassifyPosition> positions = pdcIE.getPositions(silverObjectId, componentId);
                if (positions == null) {
                  // La publication n'est pas classée sur le PDC
                  // Si le mode brouillon est activé et que le classement est obligatoire, la
                  // publication passe en mode "Draft"
                  if (pdcIE.isClassifyingMandatory(componentId) && ImportExportHelper.isDraftUsed(
                      componentInst)) {
                    gedIE.publicationNotClassifiedOnPDC(pubDetail.getId());
                  }
                }
              } catch (Exception e) {
                unitReport.setError(UnitReport.ERROR_INCORRECT_CLASSIFICATION_ON_COMPONENT);
              }
            }
          } catch (Exception ex) {
            unitReport.setError(UnitReport.ERROR_ERROR);
            SilverTrace.error("importExport", "PublicationsTypeManager.processImport()",
                "root.EX_NO_MESSAGE", ex);
          }
          attachments = null;
        }
      }
      nbItem++;
    }
  }

  private void checkPublication(PublicationDetail publication,
      UserDetail userDetail) {
    if (publication.getCreationDate() == null) {
      publication.setCreationDate(new Date());
    }
    if (publication.getCreatorId() == null) {
      publication.setCreatorId(userDetail.getId());
    }
    if (publication.getUpdaterId() == null) {
      publication.setUpdaterId(userDetail.getId());
    }
    if (publication.getImportance() == 0) {
      publication.setImportance(5);
    }
  }

  private boolean isKmax(String currentComponentId) {
    return currentComponentId.startsWith("kmax");
  }
}