/*
 * Copyright (C) 2000 - 2022 Silverpeas
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
package org.silverpeas.core.importexport.control;

import org.silverpeas.core.ResourceReference;
import org.silverpeas.core.SilverpeasExceptionMessages.LightExceptionMessage;
import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.admin.user.model.UserDetail;
import org.silverpeas.core.contribution.attachment.AttachmentServiceProvider;
import org.silverpeas.core.contribution.attachment.model.DocumentType;
import org.silverpeas.core.contribution.attachment.model.SimpleDocument;
import org.silverpeas.core.contribution.content.form.DataRecord;
import org.silverpeas.core.contribution.content.form.Field;
import org.silverpeas.core.contribution.content.form.FieldDisplayer;
import org.silverpeas.core.contribution.content.form.FieldTemplate;
import org.silverpeas.core.contribution.content.form.FormException;
import org.silverpeas.core.contribution.content.form.PagesContext;
import org.silverpeas.core.contribution.content.form.RecordSet;
import org.silverpeas.core.contribution.content.form.TypeManager;
import org.silverpeas.core.contribution.content.form.XMLField;
import org.silverpeas.core.contribution.content.form.field.FileField;
import org.silverpeas.core.contribution.content.wysiwyg.WysiwygException;
import org.silverpeas.core.contribution.content.wysiwyg.service.WysiwygController;
import org.silverpeas.core.contribution.publication.model.CompletePublication;
import org.silverpeas.core.contribution.publication.model.Location;
import org.silverpeas.core.contribution.publication.model.PublicationDetail;
import org.silverpeas.core.contribution.publication.model.PublicationPK;
import org.silverpeas.core.contribution.publication.service.PublicationService;
import org.silverpeas.core.contribution.template.form.service.FormTemplateService;
import org.silverpeas.core.contribution.template.publication.PublicationTemplate;
import org.silverpeas.core.contribution.template.publication.PublicationTemplateException;
import org.silverpeas.core.contribution.template.publication.PublicationTemplateManager;
import org.silverpeas.core.i18n.I18NHelper;
import org.silverpeas.core.importexport.form.FormTemplateImportExport;
import org.silverpeas.core.importexport.form.XMLModelContentType;
import org.silverpeas.core.importexport.model.ImportExportException;
import org.silverpeas.core.importexport.model.PublicationType;
import org.silverpeas.core.importexport.publication.PublicationContentType;
import org.silverpeas.core.importexport.report.ImportReportManager;
import org.silverpeas.core.importexport.report.MassiveReport;
import org.silverpeas.core.importexport.report.UnitReport;
import org.silverpeas.core.importexport.wysiwyg.WysiwygContentType;
import org.silverpeas.core.index.indexing.model.IndexManager;
import org.silverpeas.core.io.media.image.thumbnail.control.ThumbnailController;
import org.silverpeas.core.io.media.image.thumbnail.model.ThumbnailDetail;
import org.silverpeas.core.node.coordinates.model.Coordinate;
import org.silverpeas.core.node.importexport.NodePositionType;
import org.silverpeas.core.node.model.NodeDetail;
import org.silverpeas.core.node.model.NodePK;
import org.silverpeas.core.node.service.NodeService;
import org.silverpeas.core.util.ResourceLocator;
import org.silverpeas.core.util.ServiceProvider;
import org.silverpeas.core.util.SettingBundle;
import org.silverpeas.core.util.StringUtil;
import org.silverpeas.core.util.file.FileFolderManager;
import org.silverpeas.core.util.file.FileRepositoryManager;
import org.silverpeas.core.util.file.FileUtil;
import org.silverpeas.core.util.logging.SilverLogger;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

import static java.text.MessageFormat.format;

/**
 * Classe metier de creation d'entites silverpeas utilisee par le moteur d'importExport.
 * @author sDevolder.
 */
public abstract class GEDImportExport extends ComponentImportExport {

  private static final String GEDIMPORT_EXPORT_CREATE_PUBLICATION_CONTENT =
      "GEDImportExport.createPublicationContent()";
  private static final String IMPORT_EXPORT_EX_CANT_CREATE_CONTENT =
      "importExport.EX_CANT_CREATE_CONTENT";
  // Variables
  private PublicationService publicationService = null;
  private FormTemplateService formTemplateService = null;
  private NodeService nodeService = NodeService.get();

  /**
   * Constructeur public de la classe
   * @param curentUserDetail - informations sur l'utilisateur faisant appel au moteur
   * d'importExport
   * @param currentComponentId - composant silverpeas cible
   */
  public GEDImportExport(UserDetail curentUserDetail, String currentComponentId) {
    super(curentUserDetail, currentComponentId);
  }

  /**
   * @return Publication service layer
   */
  private PublicationService getPublicationService() {
    if (publicationService == null) {
      publicationService = PublicationService.get();
    }
    return publicationService;
  }

  private FormTemplateService getFormTemplateService() {
    if (formTemplateService == null) {
      formTemplateService = ServiceProvider.getSingleton(FormTemplateService.class);
    }
    return formTemplateService;
  }

  /**
   * @return Node service layer
   */
  protected NodeService getNodeService() {
    return nodeService;
  }

  private List<NodePositionType> processTopics(String userId, List<NodePositionType> topics,
      String componentId) {
    if (isKmax()) {
      return topics;
    }
    return getExistingTopics(userId, topics, componentId);
  }

  /**
   * Methode de creation ou mise a  jour d'une publication utilisee par le manager d'importation de
   * repository du * moteur d'importExport. Cas particulier: si une publication de meme nom existe
   * deja  dans le composant, alors une nouvelle publication ne sera creee que si le premier node a 
   * lier ne contient pas la publication de meme nom.
   * @param pubDetailToCreate - publication a  creer ou a  mettre a  jour.
   * @return l'objet PublicationDetail contenant les informations de la publication creee ou mise a 
   * jour.
   */
  private PublicationDetail processPublicationDetail(UnitReport unitReport, ImportSettings settings,
      PublicationDetail pubDetailToCreate, List<NodePositionType> listOfNodeTypes) {
    // checking topics
    List<NodePositionType> existingTopics = processTopics(settings.getUser()
        .getId(), listOfNodeTypes, pubDetailToCreate.getPK()
        .getInstanceId());

    if (existingTopics.isEmpty() && !isKmax()) {
      // Ids are not corresponding to any topics
      // Classification is not possible
      unitReport.setStatus(UnitReport.STATUS_PUBLICATION_NOT_CREATED);
      unitReport.setError(UnitReport.ERROR_NOT_EXISTS_TOPIC);
      return null;
    } else {
      PublicationDetail pubDetTemp = null;
      boolean pubAlreadyExist = true;
      if (isKmax()) {
        pubAlreadyExist = false;
      }
      String pubId = null;

      // check if publication with same name exists into first topic
      boolean pubIdExists = false;
      if (pubDetailToCreate.getId() != null) {
        pubIdExists = StringUtil.isInteger(pubDetailToCreate.getId());
      }
      if (!pubIdExists) {
        pubAlreadyExist = false;
        if (settings.isPublicationMergeEnabled()) {
          try {
            for (NodePositionType nodeType : existingTopics) {
              pubDetTemp =
                  getPublicationService().getDetailByNameAndNodeId(pubDetailToCreate.getPK(),
                      pubDetailToCreate.getName(), nodeType.getId());

              // Checking that the user has rights to add attachments
              if (pubDetTemp.canBeModifiedBy(getCurrentUserDetail())) {
                pubAlreadyExist = true;
              }
            }
          } catch (Exception e) {
            SilverLogger.getLogger(this)
                .debug("This publication does not exist in this folder");
          }
        }
      } else {
        pubDetTemp = getPublicationService().getDetail(pubDetailToCreate.getPK());
      }
      if (isKmax()) {
        pubDetTemp = getPublicationService().getDetailByName(pubDetailToCreate.getPK(),
            pubDetailToCreate.getName());
        if (pubDetTemp != null) {
          pubAlreadyExist = true;
        }
      }

      if (pubAlreadyExist) {
        try {
          updatePublication(pubDetTemp, pubDetailToCreate, settings.getUser());
          unitReport.setStatus(UnitReport.STATUS_PUBLICATION_UPDATED);
        } catch (Exception e) {
          unitReport.setError(UnitReport.ERROR_ERROR);
          return null;
        }
      } else {
        // la publication n'existe pas
        pubDetTemp = pubDetailToCreate;
      }

      // Processing thumbnail...
      if (pubIdExists && StringUtil.isDefined(pubDetailToCreate.getImage())) {
        processThumbnail(pubDetailToCreate.getImage(), pubDetTemp);
      }

      // Specific Kmax: create Publication with no nodes attached.
      if (isKmax() && !pubAlreadyExist) {
        try {
          pubDetTemp = createPublication(pubDetTemp);
          unitReport.setStatus(UnitReport.STATUS_PUBLICATION_CREATED);
        } catch (Exception e) {
          unitReport.setError(UnitReport.ERROR_ERROR);
        }
      } else {
        // Adding publication into topics
        Iterator<NodePositionType> itListNodeType = existingTopics.iterator();
        if (!pubAlreadyExist) {
          // creating new publication in first topic
          NodePositionType nodeType = itListNodeType.next();
          try {
            NodePK topicPK =
                new NodePK(Integer.toString(nodeType.getId()), pubDetailToCreate.getPK());
            pubId = createPublicationIntoTopic(pubDetTemp, topicPK, settings.getUser());
            pubDetTemp.getPK()
                .setId(pubId);
          } catch (Exception e) {
            unitReport.setError(UnitReport.ERROR_ERROR);
          }
          unitReport.setStatus(UnitReport.STATUS_PUBLICATION_CREATED);
        }
        if (isKmelia()) {
          while (itListNodeType.hasNext()) {
            // Adding publication into other topics
            NodePositionType nodeType = itListNodeType.next();
            try {
              NodePK topicPK =
                  new NodePK(Integer.toString(nodeType.getId()), pubDetailToCreate.getPK());
              PublicationPK pubPK = new PublicationPK(pubId, pubDetailToCreate.getPK());
              if (!pubAlreadyExist || !isPublicationInTopic(pubDetTemp, nodeType)) {
                addPublicationToTopic(pubPK, topicPK);
              }
            } catch (Exception ex) {
              unitReport.setError(UnitReport.ERROR_ERROR);
            }
          }
        }
      }
      return pubDetTemp;
    }
  }

  private boolean isPublicationInTopic(final PublicationDetail pubDetail,
      final NodePositionType nodeType) {
    try {
      return pubDetail != null &&
          getPublicationService().getDetailByNameAndNodeId(pubDetail.getPK(), pubDetail.getName(),
              nodeType.getId()) != null;
    } catch (Exception ex) {
      return false;
    }
  }

  private boolean isKmelia() {
    return getCurrentComponentId().startsWith("kmelia");
  }

  public boolean isKmax() {
    return getCurrentComponentId().startsWith("kmax");
  }

  /**
   * Method which creates publication content from imported publication
   * @param unitReport
   * @param pubId - publication identifier of imported content
   * @param pubContent - content to import
   * @param userId the user identifier
   * @param language
   * @throws ImportExportException
   */
  public void createPublicationContent(ImportReportManager reportManager, UnitReport unitReport,
      int pubId, PublicationContentType pubContent, String userId, String language)
      throws ImportExportException {
    WysiwygContentType wysiwygType = pubContent.getWysiwygContentType();
    XMLModelContentType xmlModel = pubContent.getXMLModelContentType();
    try {
      if (wysiwygType != null) { // Contenu Wysiwyg
        createWysiwygContent(unitReport, pubId, wysiwygType, userId, language);
      } else if (xmlModel != null) {
        createXMLModelContent(xmlModel, java.lang.Integer.toString(pubId), userId);
      }
    } catch (ImportExportException ex) {
      throw ex;
    } catch (Exception ex) {
      throw new ImportExportException(GEDIMPORT_EXPORT_CREATE_PUBLICATION_CONTENT,
          IMPORT_EXPORT_EX_CANT_CREATE_CONTENT, "pubId = " + pubId, ex);
    }
  }

  private void createXMLModelContent(XMLModelContentType xmlModel, String pubId, String userId)
      throws PublicationTemplateException, FormException {
    PublicationPK pubPK = new PublicationPK(pubId, getCurrentComponentId());
    PublicationDetail pubDetail = getPublicationService().getDetail(pubPK);

    // Is it the creation of the content or an update ?
    String infoId = pubDetail.getInfoId();
    PublicationTemplateManager publicationTemplateManager =
        PublicationTemplateManager.getInstance();
    if (infoId == null || "0".equals(infoId)) {
      String xmlFormShortName = xmlModel.getName();

      // The publication have no content
      // We have to register xmlForm to publication
      pubDetail.setInfoId(xmlFormShortName);
      pubDetail.setIndexOperation(IndexManager.NONE);
      getPublicationService().setDetail(pubDetail);
      publicationTemplateManager.addDynamicPublicationTemplate(
          getCurrentComponentId() + ':' + xmlFormShortName, xmlFormShortName + ".xml");
    }

    PublicationTemplate pub = publicationTemplateManager.getPublicationTemplate(
        getCurrentComponentId() + ':' + xmlModel.getName());

    RecordSet set = pub.getRecordSet();
    DataRecord data = set.getRecord(pubId);
    if (data == null) {
      data = set.getEmptyRecord();
      data.setId(pubId);
    }

    PagesContext formContext = new PagesContext();
    formContext.setComponentId(getCurrentComponentId());
    formContext.setObjectId(pubId);

    for (XMLField xmlField : xmlModel.getFields()) {
      String xmlFieldName = xmlField.getName();
      String xmlFieldValue = xmlField.getValue();
      String fieldValue;
      try {
        Field field = data.getField(xmlFieldName);
        if (field != null) {
          FieldTemplate fieldTemplate = pub.getRecordTemplate()
              .getFieldTemplate(xmlFieldName);
          if (fieldTemplate != null) {
            FieldDisplayer<Field> fieldDisplayer = TypeManager.getInstance()
                .getDisplayer(field.getTypeName(), fieldTemplate.getDisplayerName());
            if (field.getTypeName()
                .equals(FileField.TYPE) && StringUtil.isDefined(xmlFieldValue)) {
              fieldValue =
                  new FormTemplateImportExport().manageFileField(new ResourceReference(pubPK),
                      userId, xmlFieldValue);
            } else {
              fieldValue = xmlFieldValue;
            }
            //noinspection unchecked
            fieldDisplayer.update(fieldValue, field, fieldTemplate, formContext);
          }
        }
      } catch (Exception e) {
        SilverLogger.getLogger(this)
            .warn("Can't import XML field '" + xmlFieldName + "'", e);
      }
    }
    set.save(data);
  }

  /**
   * Methode de creation d'un contenu de type wysiwyg
   * @param pubId - id de la publication pour laquelle on cree le contenu wysiwyg
   * @param wysiwygType - content to import
   */
  private void createWysiwygContent(UnitReport unitReport, int pubId,
      WysiwygContentType wysiwygType, String userId, String lang) throws ImportExportException {
    // Retrieve new wysiwyg content
    File wysiwygFile = null;
    String wysiwygText = null;
    try {
      wysiwygFile = new File(FileUtil.convertPathToServerOS(wysiwygType.getPath()));
      if (!wysiwygFile.exists() && !wysiwygFile.isFile()) {
        String baseDir = resources.getString("importRepository", "");
        wysiwygFile = new File(
            FileUtil.convertPathToServerOS(baseDir + File.separatorChar + wysiwygType.getPath()));
      }
      Optional<String> content =
          FileFolderManager.getFileContent(wysiwygFile.getParent(), wysiwygFile.getName());
      if (content.isPresent()) {
        wysiwygText = content.get();
      }
    } catch (org.silverpeas.core.util.UtilException ex) {
      unitReport.setError(UnitReport.ERROR_NOT_EXISTS_OR_INACCESSIBLE_FILE_FOR_CONTENT);
      if (wysiwygFile != null) {
        throw new ImportExportException(GEDIMPORT_EXPORT_CREATE_PUBLICATION_CONTENT,
            IMPORT_EXPORT_EX_CANT_CREATE_CONTENT, "file = " + wysiwygFile.getPath(), ex);
      } else {
        throw new ImportExportException(GEDIMPORT_EXPORT_CREATE_PUBLICATION_CONTENT,
            IMPORT_EXPORT_EX_CANT_CREATE_CONTENT, "file = null", ex);
      }
    }
    if (wysiwygText == null) {
      unitReport.setError(UnitReport.ERROR_NOT_EXISTS_OR_INACCESSIBLE_FILE_FOR_CONTENT);
      throw new ImportExportException(GEDIMPORT_EXPORT_CREATE_PUBLICATION_CONTENT,
          IMPORT_EXPORT_EX_CANT_CREATE_CONTENT, "file = " + wysiwygFile.getPath());
    }
    // Suppression de tout le contenu wysiwyg s il existe
    if (WysiwygController.haveGotWysiwyg(getCurrentComponentId(), String.valueOf(pubId), lang)) {
      try {
        WysiwygController.deleteWysiwygAttachmentsOnly(getCurrentComponentId(),
            String.valueOf(pubId));
      } catch (WysiwygException ignored) {
        SilverLogger.getLogger(this)
            .warn(ignored);
      }
    }
    // Creation du fichier de contenu wysiwyg sur les serveur
    String newWysiwygText = replaceWysiwygImagesPathForImport(wysiwygText);
    newWysiwygText = removeWysiwygStringsForImport(newWysiwygText);
    newWysiwygText = replaceWysiwygStringsForImport(newWysiwygText);
    WysiwygController.createFileAndAttachment(newWysiwygText,
        new ResourceReference(String.valueOf(pubId), getCurrentComponentId()),
        WysiwygController.WYSIWYG_CONTEXT, userId, lang);
  }

  /**
   * Methode chargee de copier les fichiers images references par le contenu wysiwyg sur le serveur
   * et de mettre a  jour le contenu wysiwyg avec ces nouveaux liens
   * @param wysiwygText - contenu wysiwyg passe en parametre
   * @return - le contenu wysiwyg mis a  jour
   */
  private String replaceWysiwygImagesPathForImport(String wysiwygText) {
    int finPath = 0;
    int debutPath;
    StringBuilder newWysiwygText = new StringBuilder();

    if (wysiwygText.indexOf("img src=", finPath) == -1) {
      newWysiwygText.append(wysiwygText);
    } else {
      // we loop for each images in the WYSIWYG content
      while ((debutPath = wysiwygText.indexOf("img src=", finPath)) != -1) {
        debutPath += 9;
        newWysiwygText.append(wysiwygText, finPath, debutPath);
        int finPath1 = wysiwygText.indexOf('"', debutPath);
        int finPath2 = wysiwygText.indexOf("'", debutPath);
        finPath = Math.min(finPath1, finPath2);
        String imageSrc = wysiwygText.substring(debutPath, finPath);
        if (imageSrc.matches("(?i)^https?://.+")) {
          newWysiwygText.append(imageSrc);
        } else {
          SilverLogger.getLogger(this)
              .warn("Local images not taken in charge. Image {0} not loaded", imageSrc);
        }
      }
      newWysiwygText.append(wysiwygText.substring(finPath));
    }
    return newWysiwygText.toString();
  }

  private String replaceWysiwygStringsForImport(String wysiwygText) {
    SettingBundle mapping =
        ResourceLocator.getSettingBundle("org.silverpeas.importExport.settings.stringsMapping");
    String newWysiwygText = wysiwygText;

    for (String oldString : mapping.keySet()) {
      String newString = mapping.getString(oldString);
      newWysiwygText = replaceWysiwygStringForImport(oldString, newString, newWysiwygText);
    }
    return newWysiwygText;
  }

  /**
   * Replace an old CSS class with a new one (parameters)
   * @param oldCssClass - old CSS class to replace
   * @param newCssClass - new CSS class to use
   * @param wysiwygText - wysiwyg content to replace
   * @return new wysiwyg content up to date
   */
  private String replaceWysiwygStringForImport(String oldCssClass, String newCssClass,
      String wysiwygText) {
    if (!StringUtil.isDefined(wysiwygText)) {
      return "";
    }
    return wysiwygText.replaceAll(oldCssClass, newCssClass);
  }

  private String removeWysiwygStringsForImport(String wysiwygText) {
    String currentWysiwygText = wysiwygText;
    SettingBundle resource =
        ResourceLocator.getSettingBundle("org.silverpeas.importExport.settings.mapping");
    String dir = resource.getString("mappingDir");
    if (StringUtil.isDefined(dir)) {
      try (final BufferedReader reader = new BufferedReader(
          new FileReader(dir + File.separator + "strings2Remove.txt"))) {
        String ligne;
        while ((ligne = reader.readLine()) != null) {
          if ("$$removeAnchors$$".equalsIgnoreCase(ligne)) {
            currentWysiwygText = removeAnchors(currentWysiwygText);
          } else {
            currentWysiwygText = currentWysiwygText.replaceAll(ligne, "");
          }
        }
      } catch (IOException e) {
        SilverLogger.getLogger(this)
            .warn(e);
      }
    }
    return currentWysiwygText;
  }

  private static String removeAnchors(String wysiwygText) {
    int fin = 0;
    int debut;
    StringBuilder newWysiwygText = new StringBuilder();

    if (wysiwygText.indexOf("<a name=", fin) == -1) {
      newWysiwygText.append(wysiwygText);
    } else {
      while ((debut = wysiwygText.indexOf("<a name=", fin)) != -1) {
        newWysiwygText.append(wysiwygText.substring(fin, debut));
        debut += 8;
        fin = wysiwygText.indexOf('>', debut);
        debut = wysiwygText.indexOf("</a>", fin);
        newWysiwygText.append(wysiwygText.substring(fin + 1, debut));
        fin = debut + 4;
      }
      newWysiwygText.append(wysiwygText.substring(fin));
    }
    return newWysiwygText.toString();
  }

  /**
   * Methode copiant les images contenues dans le dossier d'exportation de la publication. Cette
   * methode met a  jour le fichier wysiwyg avec les nouveaux chemins d'images avant de le copier
   * dans l'exportation
   * @param pubId - id de la publication a  exporter
   * @param componentId - id du composant contenant la publication a  exporter
   * @param exportPublicationPath - dossier d'exportation de la publication
   * @return le contenu du fichier wysiwyg
   */
  public void copyWysiwygImageForExport(String pubId, String componentId,
      String exportPublicationPath) {
    ResourceReference foreignKey = new ResourceReference(pubId, componentId);
    Collection<SimpleDocument> documents = AttachmentServiceProvider.getAttachmentService()
        .listAllDocumentsByForeignKey(foreignKey, null);
    documents.stream()
        .filter(a -> DocumentType.image == a.getDocumentType() ||
            DocumentType.video == a.getDocumentType())
        .filter(a -> a.isDownloadAllowedForRolesFrom(getCurrentUserDetail()))
        .forEach(a -> {
          try {
            FileRepositoryManager.copyFile(a.getAttachmentPath(),
                exportPublicationPath + File.separator + a.getFilename());
          } catch (IOException ex) {
            SilverLogger.getLogger(this)
                .error(ex);
          }
        });
  }

  private List<NodePositionType> getExistingTopics(String userId, List<NodePositionType> nodeTypes,
      String componentId) {
    List<NodePositionType> topics = new ArrayList<>();
    for (NodePositionType node : nodeTypes) {
      if (node.getId() >= 0) {
        // defined node must exists
        if (isTopicExist(node.getId(), componentId)) {
          topics.add(node);
        }
      } else if (StringUtil.isDefined(node.getExplicitPath()) && node.getId() == -1) {
        // explicit mode is used. Topics must be created on-the-fly if needed.
        String[] path = node.getExplicitPath()
            .substring(1)
            .split("/");
        NodePK nodePK = new NodePK("unknown", componentId);
        String parentId = NodePK.ROOT_NODE_ID;
        for (String name : path) {
          NodeDetail existingNode =
              getNodeService().getDetailByNameAndFatherId(nodePK, name, Integer.parseInt(parentId));
          if (existingNode != null) {
            // topic exists
            parentId = existingNode.getNodePK()
                .getId();
          } else {
            // topic does not exists, creating it
            NodeDetail newNode = new NodeDetail();
            newNode.setName(name);
            newNode.setNodePK(new NodePK("unknown", componentId));
            newNode.setFatherPK(new NodePK(parentId, componentId));
            newNode.setCreatorId(userId);
            NodePK newNodePK;
            try {
              newNodePK = getNodeService().createNode(newNode);
            } catch (Exception e) {
              SilverLogger.getLogger(this)
                  .error(e);
              return new ArrayList<>();
            }
            parentId = newNodePK.getId();
          }
        }
        node.setId(Integer.parseInt(parentId));
        topics.add(node);
      }

    }
    return topics;
  }

  private boolean isTopicExist(int nodeId, String componentId) {
    try {
      getNodeService().getHeader(new NodePK(Integer.toString(nodeId), "useless", componentId));
    } catch (Exception e) {
      return false;
    }
    return true;
  }

  /**
   * Methode ajoutant un theme a  un theme deja existant. Si le theme a  ajouter existe lui aussi
   * (par exemple avec un meme ID), il n'est pas modifie et la methode ne fait rien et ne leve
   * aucune exception.
   * @param nodeDetail le detail du noeud a  ajouter.
   * @param topicId l'identifiant du noeud parent, ou 0 pour designer le noeud racine.
   * @param unitReport le rapport d'import unitaire.
   * @return un objet cle primaire du nouveau theme cree ou du theme deja  existant (theme de meme
   * identifiant non modifie).
   * @throws ImportExportException en cas d'anomalie lors de la creation du noeud.
   */
  protected abstract NodePK addSubTopicToTopic(NodeDetail nodeDetail, int topicId,
      UnitReport unitReport) throws ImportExportException;

  /**
   * Methode ajoutant un theme a  un theme deja existant. Si le theme a  ajouter existe lui aussi
   * (par exemple avec un meme ID), il n'est pas modifie et la methode ne fait rien et ne leve
   * aucune exception.
   * @param nodeDetail l'objet node correspondant au theme a  creer.
   * @param topicId l'ID du theme dans lequel creer le nouveau theme.
   * @param massiveReport
   * @return un objet cle primaire du nouveau theme cree.
   * @throws ImportExportException en cas d'anomalie lors de la creation du noeud.
   */
  protected abstract NodePK addSubTopicToTopic(NodeDetail nodeDetail, int topicId,
      MassiveReport massiveReport) throws ImportExportException;

  /**
   * Ajoute un sous-noeud a  un noeud existant a  partir d'un repertoire du systeme de fichiers. Le
   * nom de ce repertoire represente le noeud a  creer. Utile pour les imports massifs de noeuds et
   * de publications a  partir d'une hierarchie de dossiers et de fichiers.
   * @param unitReport le rapport d'import unitaire.
   * @param nodeDetail le detail du noeud a  creer.
   * @param parentTopicId l'identifiant du noeud parent, ou 0 pour designer le noeud racine.
   * @return l'objet qui represente le detail du nouveau noeud cree ou du noeud existant (en
   * particulier si un noeud de meme ID existe deja ).
   * @throws ImportExportException en cas d'anomalie lors de la creation du noeud.
   */
  public NodeDetail createTopicForUnitImport(UnitReport unitReport, NodeDetail nodeDetail,
      int parentTopicId) throws ImportExportException {
    unitReport.setItemName(nodeDetail.getName());
    NodePK nodePk = addSubTopicToTopic(nodeDetail, parentTopicId, unitReport);
    try {
      return getNodeService().getDetail(nodePk);
    } catch (Exception ex) {
      unitReport.setError(UnitReport.ERROR_NOT_EXISTS_TOPIC);
      SilverLogger.getLogger(this)
          .error(ex);
      throw new ImportExportException("GEDImportExport.createTopicForUnitImport",
          "importExport.EX_NODE_CREATE", ex);
    }
  }

  /**
   * Methode de creation d'une publication dans le cas d'une importation unitaire avec meta-donnees
   * definies dans le fichier xml d'importation.
   * @param unitReport
   * @param settings
   * @param pubDetail
   * @param listNodeType
   * @return
   */
  public PublicationDetail createPublicationForUnitImport(UnitReport unitReport,
      ImportSettings settings, PublicationDetail pubDetail, List<NodePositionType> listNodeType) {
    unitReport.setItemName(pubDetail.getName());
    return processPublicationDetail(unitReport, settings, pubDetail, listNodeType);
  }

  /**
   * Methode de creation d'une publication dans le cas d'une importation massive
   * @param unitReport
   * @param pubDetail
   * @param settings
   * @return
   * @throws ImportExportException
   */
  public PublicationDetail createPublicationForMassiveImport(UnitReport unitReport,
      PublicationDetail pubDetail, ImportSettings settings) {
    unitReport.setItemName(pubDetail.getName());
    NodePositionType nodePosType = new NodePositionType();
    nodePosType.setId(Integer.valueOf(settings.getFolderId()));
    List<NodePositionType> listNodeType = new ArrayList<>(1);
    listNodeType.add(nodePosType);
    return processPublicationDetail(unitReport, settings, pubDetail, listNodeType);
  }

  /**
   * Ajoute un sous-noeud a  un noeud existant a  partir d'un repertoire du systeme de fichiers. Le
   * nom de ce repertoire represente le noeud a  creer. Utile pour les imports massifs de noeuds et
   * de publications a  partir d'une hierarchie de dossiers et de fichiers.
   * @param directory le repertoire dont le nom represente le nouveau noeud.
   * @param topicId l'identifiant du noeud parent.
   * @param massiveReport le rapprt d'import.
   * @return un objet qui represente le nouveau noeud cree.
   * @throws ImportExportException en cas d'anomalie lors de la creation du noeud.
   */
  public NodeDetail addSubTopicToTopic(File directory, int topicId, MassiveReport massiveReport)
      throws ImportExportException {
    try {
      String directoryName = directory.getName();
      NodeDetail nodeDetail = new NodeDetail("unknow", directoryName, directoryName, 0, "useless");
      nodeDetail.setNodePK(addSubTopicToTopic(nodeDetail, topicId, massiveReport));
      return nodeDetail;
    } catch (Exception ex) {
      throw new ImportExportException("GEDImportExport.addSubTopicToTopic",
          "importExport.EX_NODE_CREATE", ex);
    }
  }

  /**
   * Methode recuperant le silverObjectId d'un objet d'id id
   * @param id - id de la publication
   * @return le silverObjectId de l'objet d'id id
   */
  public abstract int getSilverObjectId(String id);

  /**
   * Methode de recuperation de la publication complete utilisee pour l'exportation
   * @param pubId the publication identifier
   * @param componentId the component instance identifier
   * @return
   */
  public PublicationType getPublicationCompleteById(String pubId, String componentId) {
    PublicationType publicationType = new PublicationType();
    CompletePublication pubComplete =
        getCompletePublication(new PublicationPK(pubId, getCurrentComponentId()));

    // Recuperation de l'objet PublicationDetail
    PublicationDetail publicationDetail = pubComplete.getPublicationDetail();

    PublicationContentType pubContent = null;
    try {
      if (!StringUtil.isInteger(publicationDetail.getInfoId())) {
        // la publication a un contenu de type XMLTemplate (formTemplate)
        pubContent = new PublicationContentType();
        List<XMLField> xmlFields = getFormTemplateService().getXMLFieldsForExport(
            publicationDetail.getPK()
                .getInstanceId() + ":" + publicationDetail.getInfoId(), pubId);

        XMLModelContentType xmlModel = new XMLModelContentType(publicationDetail.getInfoId());
        xmlModel.setFields(xmlFields);
        pubContent.setXMLModelContentType(xmlModel);
      } else if (WysiwygController.haveGotWysiwyg(publicationDetail.getPK()
          .getInstanceId(), pubId, I18NHelper.checkLanguage(publicationDetail.getLanguage()))) {
        pubContent = new PublicationContentType();
        WysiwygContentType wysiwygContentType = new WysiwygContentType();
        String wysiwygFileName = WysiwygController.getWysiwygFileName(pubId,
            I18NHelper.checkLanguage(publicationDetail.getLanguage()));
        wysiwygContentType.setPath(wysiwygFileName);
        pubContent.setWysiwygContentType(wysiwygContentType);
      }
    } catch (Exception e) {
      SilverLogger.getLogger(this)
          .error(new LightExceptionMessage(this, e).singleLineWith(
              format("Cannot export content of publication #{0} on instanceId #{1} ({2})", pubId,
                  publicationDetail.getPK()
                      .getInstanceId(), e.getMessage())));
    }
    publicationType.setPublicationContentType(pubContent);
    publicationType.setPublicationDetail(publicationDetail);
    publicationType.setId(Integer.parseInt(pubId));
    publicationType.setComponentId(componentId);

    // Recherche du nom et du prenom du createur de la pub pour le marschalling
    User creator = publicationDetail.getCreator();
    if (creator != null) {
      String nomPrenomCreator = creator.getDisplayedName();
      publicationDetail.setCreatorName(nomPrenomCreator);
    }
    return publicationType;
  }

  /**
   * Methode renvoyant la liste des topics de la publication sous forme de NodePK
   * @param pubPK - pk de la publication dont on veut les topics
   * @return - liste des nodesPk de la publication
   */
  public List<Location> getAllTopicsOfPublicationInSameComponentInstance(PublicationPK pubPK) {
    final List<Location> result = new ArrayList<>();
    getPublicationService().getMainLocation(pubPK)
        .ifPresent(result::add);
    return result;
  }

  public List<Location> getAliases(PublicationPK pubPK) {
    return getPublicationService().getAllAliases(pubPK);
  }

  public List<NodePK> getTopicTree(NodePK pk) {
    List<NodePK> listNodePk = new ArrayList<>();
    Collection<NodeDetail> path = getNodeService().getPath(pk);
    for (NodeDetail detail : path) {
      listNodePk.add(detail.getNodePK());
    }
    return listNodePk;

  }

  public abstract void publicationNotClassifiedOnPDC(String pubId);

  /**
   * Specific Kmax: Create publication with no nodeFather
   * @param pubDetail
   * @return pubDetail
   */
  protected abstract PublicationDetail createPublication(PublicationDetail pubDetail);

  public Collection<Coordinate> getPublicationCoordinates(String pubId, String componentId)
      throws ImportExportException {
    try {
      return getPublicationService().getCoordinates(pubId, componentId);
    } catch (Exception e) {
      throw new ImportExportException("GEDImportExport.getPublicationCoordinates(String)",
          "importExport.EX_GET_SILVERPEASOBJECTID", "pubId = " + pubId, e);
    }
  }

  private void processThumbnail(String filePath, PublicationDetail pubDetail) {
    // Preparation des parametres du fichier a creer
    String logicalName = filePath.substring(filePath.lastIndexOf(File.separator) + 1);
    String type = FileRepositoryManager.getFileExtension(logicalName);
    String mimeType = FileUtil.getMimeType(logicalName);
    String physicalName = Long.toString(System.currentTimeMillis()) + "." + type;
    if (FileUtil.isImage(logicalName)) {
      String dest = FileRepositoryManager.getAbsolutePath(pubDetail.getPK()
          .getInstanceId()) + "images" + File.separator + physicalName;
      try {
        FileRepositoryManager.copyFile(filePath, dest);
      } catch (Exception e) {
        SilverLogger.getLogger(this)
            .error(e);
      }
      ThumbnailDetail thumbnailDetail = new ThumbnailDetail(pubDetail.getPK()
          .getComponentName(), Integer.valueOf(pubDetail.getPK()
          .getId()), ThumbnailDetail.THUMBNAIL_OBJECTTYPE_PUBLICATION_VIGNETTE);
      thumbnailDetail.setOriginalFileName(physicalName);
      thumbnailDetail.setOriginalFileName(mimeType);
      ThumbnailController.createThumbnail(thumbnailDetail, 50, 50);
    }
  }

  protected abstract void updatePublication(PublicationDetail pubDetTemp,
      PublicationDetail pubDetailToCreate, UserDetail userDetail);

  protected abstract String createPublicationIntoTopic(PublicationDetail pubDetTemp, NodePK topicPK,
      UserDetail userDetail);

  protected abstract void addPublicationToTopic(PublicationPK pubPK, NodePK topicPK);

  protected abstract CompletePublication getCompletePublication(PublicationPK pk);
}
