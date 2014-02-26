/**
 * Copyright (C) 2000 - 2013 Silverpeas
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
package com.silverpeas.importExport.control;

import com.silverpeas.form.DataRecord;
import com.silverpeas.form.Field;
import com.silverpeas.form.FieldDisplayer;
import com.silverpeas.form.FieldTemplate;
import com.silverpeas.form.PagesContext;
import com.silverpeas.form.RecordSet;
import com.silverpeas.form.TypeManager;
import com.silverpeas.form.fieldType.FileField;
import com.silverpeas.form.importExport.FormTemplateImportExport;
import com.silverpeas.form.importExport.XMLField;
import com.silverpeas.formTemplate.ejb.FormTemplateBm;
import com.silverpeas.importExport.model.ImportExportException;
import com.silverpeas.importExport.model.PublicationType;
import com.silverpeas.importExport.report.ImportReportManager;
import com.silverpeas.importExport.report.MassiveReport;
import com.silverpeas.importExport.report.UnitReport;
import com.silverpeas.node.importexport.NodePositionType;
import com.silverpeas.publication.importExport.DBModelContentType;
import com.silverpeas.publication.importExport.PublicationContentType;
import com.silverpeas.publication.importExport.XMLModelContentType;
import com.silverpeas.publicationTemplate.PublicationTemplate;
import com.silverpeas.publicationTemplate.PublicationTemplateManager;
import com.silverpeas.thumbnail.control.ThumbnailController;
import com.silverpeas.thumbnail.model.ThumbnailDetail;
import com.silverpeas.util.FileUtil;
import com.silverpeas.util.ForeignPK;
import com.silverpeas.util.StringUtil;
import com.silverpeas.util.i18n.I18NHelper;
import com.silverpeas.wysiwyg.importExport.WysiwygContentType;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.beans.admin.OrganizationController;
import com.stratelia.webactiv.beans.admin.UserDetail;
import com.stratelia.webactiv.util.EJBUtilitaire;
import com.stratelia.webactiv.util.FileRepositoryManager;
import com.stratelia.webactiv.util.FileServerUtils;
import com.stratelia.webactiv.util.JNDINames;
import com.stratelia.webactiv.util.ResourceLocator;
import com.stratelia.webactiv.util.coordinates.model.Coordinate;
import com.stratelia.webactiv.util.exception.UtilException;
import com.stratelia.webactiv.util.fileFolder.FileFolderManager;
import com.stratelia.webactiv.util.node.control.NodeBm;
import com.stratelia.webactiv.util.node.model.NodeDetail;
import com.stratelia.webactiv.util.node.model.NodePK;
import com.stratelia.webactiv.util.publication.control.PublicationBm;
import com.stratelia.webactiv.util.publication.info.model.InfoDetail;
import com.stratelia.webactiv.util.publication.info.model.InfoImageDetail;
import com.stratelia.webactiv.util.publication.info.model.InfoTextDetail;
import com.stratelia.webactiv.util.publication.info.model.ModelDetail;
import com.stratelia.webactiv.util.publication.info.model.ModelPK;
import com.stratelia.webactiv.util.publication.model.Alias;
import com.stratelia.webactiv.util.publication.model.CompletePublication;
import com.stratelia.webactiv.util.publication.model.PublicationDetail;
import com.stratelia.webactiv.util.publication.model.PublicationPK;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import org.apache.commons.io.IOUtils;
import org.silverpeas.attachment.AttachmentServiceFactory;
import org.silverpeas.attachment.model.SimpleDocument;
import org.silverpeas.importExport.attachment.AttachmentDetail;
import org.silverpeas.importExport.attachment.AttachmentImportExport;
import org.silverpeas.search.indexEngine.model.IndexManager;
import org.silverpeas.wysiwyg.WysiwygException;
import org.silverpeas.wysiwyg.control.WysiwygController;

/**
 * Classe metier de creation d'entites silverpeas utilisee par le moteur d'importExport.
 *
 * @author sDevolder.
 */
public abstract class GEDImportExport extends ComponentImportExport {

  // Variables
  private static final OrganizationController organizationController = new OrganizationController();
  private PublicationBm publicationBm = null;
  private FormTemplateBm formTemplateBm = null;
  private NodeBm nodeBm = null;
  private AttachmentImportExport attachmentIE;

  /**
   * Constructeur public de la classe
   *
   * @param curentUserDetail - informations sur l'utilisateur faisant appel au moteur d'importExport
   * @param currentComponentId - composant silverpeas cible
   */
  public GEDImportExport(UserDetail curentUserDetail, String currentComponentId) {
    super(curentUserDetail, currentComponentId);
    attachmentIE = new AttachmentImportExport();
  }

  /**
   * @return l'EJB PublicationBM
   * @throws ImportExportException
   */
  protected PublicationBm getPublicationBm() throws ImportExportException {
    if (publicationBm == null) {
      try {
        publicationBm = EJBUtilitaire.getEJBObjectRef(JNDINames.PUBLICATIONBM_EJBHOME,
            PublicationBm.class);
      } catch (Exception e) {
        throw new ImportExportException("GEDImportExport.getPublicationBm()",
            "root.EX_CANT_GET_REMOTE_OBJECT", e);
      }
    }
    return publicationBm;
  }

  protected FormTemplateBm getFormTemplateBm() throws ImportExportException {
    if (formTemplateBm == null) {
      try {
        formTemplateBm = EJBUtilitaire.getEJBObjectRef(JNDINames.FORMTEMPLATEBM_EJBHOME,
            FormTemplateBm.class);
      } catch (Exception e) {
        throw new ImportExportException("GEDImportExport.getPublicationBm()",
            "root.EX_CANT_GET_REMOTE_OBJECT", e);
      }
    }
    return formTemplateBm;
  }

  /**
   * @return l'EJB NodeBM
   * @throws ImportExportException
   */
  protected NodeBm getNodeBm() throws ImportExportException {
    if (nodeBm == null) {
      try {
        nodeBm = EJBUtilitaire.getEJBObjectRef(JNDINames.NODEBM_EJBHOME, NodeBm.class);
      } catch (Exception e) {
        throw new ImportExportException("GEDImportExport.getNodeBm()",
            "root.EX_CANT_GET_REMOTE_OBJECT", e);
      }
    }
    return nodeBm;
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
   *
   * @param pubDetailToCreate - publication a  creer ou a  mettre a  jour.
   * @return l'objet PublicationDetail contenant les informations de la publication creee ou mise a 
   * jour.
   * @throws ImportExportException
   */
  private PublicationDetail processPublicationDetail(UnitReport unitReport, ImportSettings settings,
      PublicationDetail pubDetailToCreate, List<NodePositionType> listOfNodeTypes) {
    // checking topics
    List<NodePositionType> existingTopics = processTopics(settings.getUser().getId(),
        listOfNodeTypes,
        pubDetailToCreate.getPK().getInstanceId());

    if (existingTopics.isEmpty() && !isKmax()) {
      // Ids are not corresponding to any topics
      // Classification is not possible
      unitReport.setStatus(UnitReport.STATUS_PUBLICATION_NOT_CREATED);
      unitReport.setError(UnitReport.ERROR_NOT_EXISTS_TOPIC);
      return null;
    } else {
      PublicationDetail pubDet_temp = null;
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
        if (!settings.isPublicationMergeEnabled()) {
          pubAlreadyExist = false;
        } else {
          try {
            Iterator<NodePositionType> itListNode_Type = existingTopics.iterator();
            if (itListNode_Type.hasNext()) {
              NodePositionType node_Type = itListNode_Type.next();
              pubDet_temp = getPublicationBm().getDetailByNameAndNodeId(pubDetailToCreate.getPK(),
                  pubDetailToCreate.getName(), node_Type.getId());
            }
          } catch (Exception pre) {
            pubAlreadyExist = false;
          }
        }
      } else {
        try {
          pubDet_temp = getPublicationBm().getDetail(pubDetailToCreate.getPK());
        } catch (Exception ex) {
          unitReport.setError(UnitReport.ERROR_NOT_EXISTS_PUBLICATION_FOR_ID);
          return null;
        }
      }
      if (isKmax()) {
        try {
          pubDet_temp = getPublicationBm().getDetailByName(pubDetailToCreate.getPK(),
              pubDetailToCreate.getName());
          if (pubDet_temp != null) {
            pubAlreadyExist = true;
          }
        } catch (Exception ex) {
        }
      }

      if (pubAlreadyExist) {
        try {
          updatePublication(pubDet_temp, pubDetailToCreate, settings.getUser());
          unitReport.setStatus(UnitReport.STATUS_PUBLICATION_UPDATED);
        } catch (Exception e) {
          unitReport.setError(UnitReport.ERROR_ERROR);
          return null;
        }
      } else {
        // la publication n'existe pas
        pubDet_temp = pubDetailToCreate;
      }

      // Processing thumbnail...
      if (pubIdExists && StringUtil.isDefined(pubDetailToCreate.getImage())) {
        processThumbnail(pubDetailToCreate.getImage(), pubDet_temp);
      }

      // Specific Kmax: create Publication with no nodes attached.
      if (isKmax() && !pubAlreadyExist) {
        try {
          pubDet_temp = createPublication(pubDet_temp);
          unitReport.setStatus(UnitReport.STATUS_PUBLICATION_CREATED);
        } catch (Exception e) {
          unitReport.setError(UnitReport.ERROR_ERROR);
        }
      } else {
        // Adding publication into topics
        Iterator<NodePositionType> itListNode_Type = existingTopics.iterator();
        if (!pubAlreadyExist) {
          // creating new publication in first topic
          NodePositionType node_Type = itListNode_Type.next();
          try {
            NodePK topicPK = new NodePK(Integer.toString(node_Type.getId()), pubDetailToCreate.
                getPK());
            pubId = createPublicationIntoTopic(pubDet_temp, topicPK, settings.getUser());
            pubDet_temp.getPK().setId(pubId);
          } catch (Exception e) {
            unitReport.setError(UnitReport.ERROR_ERROR);
          }
          unitReport.setStatus(UnitReport.STATUS_PUBLICATION_CREATED);
        }
        if (isKmelia()) {
          while (itListNode_Type.hasNext()) {
            // Adding publication into other topics
            NodePositionType node_Type = itListNode_Type.next();
            try {
              NodePK topicPK = new NodePK(Integer.toString(node_Type.getId()), pubDetailToCreate.
                  getPK());
              PublicationPK pubPK = new PublicationPK(pubId, pubDetailToCreate.getPK());
              if (pubAlreadyExist) {
                // check if existing publication is already in this topic
                try {
                  getPublicationBm().getDetailByNameAndNodeId(pubDet_temp.getPK(), pubDet_temp.
                      getName(), node_Type.getId());
                } catch (Exception ex) {
                  // this publication is not in this topic. Adding it...
                  addPublicationToTopic(pubPK, topicPK);
                }
              } else {
                addPublicationToTopic(pubPK, topicPK);
              }
            } catch (Exception ex) {
              unitReport.setError(UnitReport.ERROR_ERROR);
            }
          }
        }
      }
      return pubDet_temp;
    }
  }

  private boolean isKmelia() {
    return getCurrentComponentId().startsWith("kmelia");
  }

  public boolean isKmax() {
    return getCurrentComponentId().startsWith("kmax");
  }

  public void createPublicationContent(ImportReportManager reportManager, UnitReport unitReport,
      int pubId, PublicationContentType pubContent, String userId, String language) throws
      ImportExportException {
    createPublicationContent(reportManager, unitReport, pubId, pubContent, userId, FileServerUtils.
        getApplicationContext(), language);
  }

  /**
   * Methode de creation du contenu d une publication importee
   *
   * @param unitReport
   * @param pubId - id de la publication pour laquelle on importe un contenu
   * @param pubContent - object de mapping castor contenant les informations d importation du
   * contenu
   * @param userId
   * @param webContext
   * @param lang
   * @throws ImportExportException
   */
  public void createPublicationContent(ImportReportManager reportManager, UnitReport unitReport,
      int pubId, PublicationContentType pubContent, String userId, String webContext,
      String language) throws ImportExportException {
    WysiwygContentType wysiwygType = pubContent.getWysiwygContentType();
    XMLModelContentType xmlModel = pubContent.getXMLModelContentType();
    try {
      if (wysiwygType != null) { // Contenu Wysiwyg
        createWysiwygContent(reportManager, unitReport, pubId, wysiwygType, userId, language);
      } else if (xmlModel != null) {
        createXMLModelContent(xmlModel, java.lang.Integer.toString(pubId), userId);
      }
    } catch (ImportExportException ex) {
      throw ex;
    } catch (Exception ex) {
      throw new ImportExportException("GEDImportExport.createPublicationContent()",
          "importExport.EX_CANT_CREATE_CONTENT", "pubId = " + pubId, ex);
    }
  }

  private void createXMLModelContent(XMLModelContentType xmlModel,
      String pubId, String userId) throws Exception {
    PublicationPK pubPK = new PublicationPK(pubId, getCurrentComponentId());
    PublicationDetail pubDetail = getPublicationBm().getDetail(pubPK);

    // Is it the creation of the content or an update ?
    String infoId = pubDetail.getInfoId();
    PublicationTemplateManager publicationTemplateManager = PublicationTemplateManager.getInstance();
    if (infoId == null || "0".equals(infoId)) {
      String xmlFormShortName = xmlModel.getName();

      // The publication have no content
      // We have to register xmlForm to publication
      pubDetail.setInfoId(xmlFormShortName);
      pubDetail.setIndexOperation(IndexManager.NONE);
      getPublicationBm().setDetail(pubDetail);
      publicationTemplateManager
          .addDynamicPublicationTemplate(getCurrentComponentId() + ':' + xmlFormShortName,
              xmlFormShortName + ".xml");
    }

    PublicationTemplate pub = publicationTemplateManager
        .getPublicationTemplate(getCurrentComponentId() + ':' + xmlModel.getName());

    RecordSet set = pub.getRecordSet();
    // Form form = pub.getUpdateForm();

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
          FieldTemplate fieldTemplate = pub.getRecordTemplate().getFieldTemplate(xmlFieldName);
          if (fieldTemplate != null) {
            FieldDisplayer fieldDisplayer = TypeManager.getInstance().getDisplayer(field.
                getTypeName(), fieldTemplate.getDisplayerName());
            if (field.getTypeName().equals(FileField.TYPE) && StringUtil.isDefined(xmlFieldValue)) {
              fieldValue = new FormTemplateImportExport()
                  .manageFileField(new ForeignPK(pubPK), userId, xmlFieldValue, fieldTemplate);
            } else {
              fieldValue = xmlFieldValue;
            }
            fieldDisplayer.update(fieldValue, field, fieldTemplate, formContext);
          }
        }
      } catch (Exception e) {
        SilverTrace.warn("importExport", "GEDImportExport.createXMLModelContent",
            "importExport.EX_CANT_IMPORT_XML_FIELD", "xmlField = " + xmlFieldName, e);
      }
    }
    set.save(data);
  }

  /**
   * Methode de creation d'un contenu de type wysiwyg
   *
   * @param pubId - id de la publication pour laquelle on cree le contenu wysiwyg
   * @param wysiwygType - objet de mapping castor contenant les informations de contenu de type
   * Wysiwyg
   */
  private void createWysiwygContent(ImportReportManager reportManager, UnitReport unitReport,
      int pubId, WysiwygContentType wysiwygType, String userId, String lang)
      throws UtilException, WysiwygException, ImportExportException {
    // Recuperation du nouveau contenu wysiwyg
    File wysiwygFile = null;
    String wysiwygText = "";
    try {
      wysiwygFile = new File(FileUtil.convertPathToServerOS(wysiwygType.getPath()));
      if (!wysiwygFile.exists() && !wysiwygFile.isFile()) {
        String baseDir = resources.getString("importRepository");
        wysiwygFile = new File(
            FileUtil.convertPathToServerOS(baseDir + File.separatorChar + wysiwygType.getPath()));
      }
      wysiwygText = FileFolderManager.getCode(wysiwygFile.getParent(), wysiwygFile.getName());
    } catch (UtilException ex) {
      unitReport.setError(UnitReport.ERROR_NOT_EXISTS_OR_INACCESSIBLE_FILE_FOR_CONTENT);
      if (wysiwygFile != null) {
        throw new ImportExportException("GEDImportExport.createPublicationContent()",
            "importExport.EX_CANT_CREATE_CONTENT", "file = " + wysiwygFile.getPath(), ex);
      } else {
        throw new ImportExportException("GEDImportExport.createPublicationContent()",
            "importExport.EX_CANT_CREATE_CONTENT", "file = null", ex);
      }
    }
    if (wysiwygText == null) {
      unitReport.setError(UnitReport.ERROR_NOT_EXISTS_OR_INACCESSIBLE_FILE_FOR_CONTENT);
      if (wysiwygFile != null) {
        throw new ImportExportException("GEDImportExport.createPublicationContent()",
            "importExport.EX_CANT_CREATE_CONTENT", "file = " + wysiwygFile.getPath());
      }
    }
    // Suppression de tout le contenu wysiwyg s il existe
    if (WysiwygController.haveGotWysiwyg(getCurrentComponentId(), String.valueOf(pubId), lang)) {
      // TODO: verifier d abord que la mise a jour est valide?!
      try {
        WysiwygController.deleteWysiwygAttachmentsOnly("useless", getCurrentComponentId(),
            String.valueOf(pubId));
      } catch (WysiwygException ex) {
      }
    }
    // Creation du fichier de contenu wysiwyg sur les serveur
    String imagesContext = WysiwygController.getImagesFileName(String.valueOf(pubId));
    String newWysiwygText = replaceWysiwygImagesPathForImport(reportManager, unitReport, pubId,
        wysiwygFile.getParent(), wysiwygText, imagesContext);
    newWysiwygText = removeWysiwygStringsForImport(newWysiwygText);
    newWysiwygText = replaceWysiwygStringsForImport(newWysiwygText);
    WysiwygController.createFileAndAttachment(newWysiwygText, new ForeignPK(String.valueOf(pubId),
        getCurrentComponentId()), WysiwygController.WYSIWYG_CONTEXT, userId, lang);
  }

  /**
   * Methode chargee de copier les fichiers images references par le contenu wysiwyg sur le serveur
   * et de mettre a  jour le contenu wysiwyg avec ces nouveaux liens
   *
   * @param wysiwygText - contenu wysiwyg passe en parametre
   * @return - le contenu wysiwyg mis a  jour
   */
  private String replaceWysiwygImagesPathForImport(ImportReportManager reportManager,
      UnitReport unitReport, int pubId, String wysiwygImportedPath, String wysiwygText,
      String imageContext) throws ImportExportException {
    int finPath = 0;
    int debutPath;
    StringBuilder newWysiwygText = new StringBuilder();

    if (wysiwygText.indexOf("img src=\"", finPath) == -1) {
      newWysiwygText.append(wysiwygText);
    } else {
      // Algorithme d extraction des fichiers images du contenu
      while ((debutPath = wysiwygText.indexOf("img src=\"", finPath)) != -1) {
        debutPath += 9;
        newWysiwygText.append(wysiwygText.substring(finPath, debutPath));
        finPath = wysiwygText.indexOf('"', debutPath);
        String imageSrc = wysiwygText.substring(debutPath, finPath);
        if (imageSrc.indexOf("http://") != 0) {
          AttachmentDetail attDetail = new AttachmentDetail();
          attDetail.setPhysicalName(imageSrc);
          File f = new File(imageSrc);
          if (!f.isAbsolute()) {
            // si le wysiwyg est issu d une export, les liens image ne comporte que leur nom(donc
            // pour l import, l utilisateur doit placer
            // les images wysiwyg dans le meme dossier que le wysiwyg OU remplacer leur chemin par
            // l absolu
            attDetail.setPhysicalName(wysiwygImportedPath + File.separator + attDetail.
                getPhysicalName());
          }
          // TODO: chercher autres infos utiles pour la creation des attachments ensuite
          try {
            attDetail = attachmentIE
                .importWysiwygAttachment(String.valueOf(pubId), getCurrentComponentId(), attDetail,
                    imageContext);
            reportManager.addNumberOfFilesProcessed(1);
            if (attDetail == null || attDetail.getSize() == 0) {
              unitReport.setError(UnitReport.ERROR_NOT_EXISTS_OR_INACCESSIBLE_FILE_FOR_CONTENT);
              throw new ImportExportException("GEDImportExport.replaceWysiwygImagesPathForImport()",
                  "importExport.EX_CANT_CREATE_CONTENT", "pic = " + imageSrc);
            }
            // On additionne la taille des fichiers importes au niveau du rapport
            reportManager.addImportedFileSize(attDetail.getSize(), getCurrentComponentId());
            //TODO FEATURE 82 newWysiwygText.append(webContext).append(attDetail.getAttachmentURL
            // ());
          } catch (Exception e) {
            SilverTrace.error("importExport", "GEDImportExport.replaceWysiwygImagesPathForImport()",
                "importExport.CANNOT_FIND_FILE", e);
            newWysiwygText.append(imageSrc);
          }
        } else {
          newWysiwygText.append(imageSrc);
        }
      }
      newWysiwygText.append(wysiwygText.substring(finPath));
    }
    return newWysiwygText.toString();
  }

  private String replaceWysiwygStringsForImport(String wysiwygText) {
    ResourceLocator mapping = new ResourceLocator(
        "com.silverpeas.importExport.settings.stringsMapping", "");
    String newWysiwygText = wysiwygText;

    Enumeration<String> classes = mapping.getKeys();
    while (mapping != null && classes.hasMoreElements()) {
      String oldString = classes.nextElement();
      String newString = mapping.getString(oldString);
      newWysiwygText = replaceWysiwygStringForImport(oldString, newString, newWysiwygText);
    }
    return newWysiwygText;
  }

  /**
   * Methode chargee de remplacer une classe Css par une autre
   *
   * @param wysiwygText - contenu wysiwyg passe en parametre
   * @param oldCssClass - la classe CSS a  remplacer
   * @param newCssClass - la nouvelle classe CSS a  utiliser
   * @return - le contenu wysiwyg mis a  jour
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
    ResourceLocator resource = new ResourceLocator("com.silverpeas.importExport.settings.mapping",
        "");
    String dir = resource.getString("mappingDir");
    if (StringUtil.isDefined(dir)) {
      BufferedReader reader = null;
      try {
        reader = new BufferedReader(new FileReader(dir + File.separator + "strings2Remove.txt"));
        String ligne;
        while ((ligne = reader.readLine()) != null) {
          if ("$$removeAnchors$$".equalsIgnoreCase(ligne)) {
            currentWysiwygText = removeAnchors(currentWysiwygText);
          } else {
            currentWysiwygText = currentWysiwygText.replaceAll(ligne, "");
          }
        }
      } catch (FileNotFoundException e) {
        SilverTrace.info("importExport", "GEDImportExport", "importExport.FILE_NOT_FOUND", e);
      } catch (IOException e) {
        SilverTrace.info("importExport", "GEDImportExport", "importExport.FILE_NOT_FOUND", e);
      } finally {
        IOUtils.closeQuietly(reader);
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
   * Methode de copie des images DBModel d'un contenu dans le dossier d'exportation d'une
   * publication
   *
   * @param exportPublicationPath - dossier d'exportation de la publication
   * @param listImageParts - liste des images du contenu DBModel
   * @param exportPublicationRelativePath
   * @return - la liste des images mise a jour
   */
  public List<String> copyDBmodelImagePartsForExport(String exportPublicationPath,
      List<String> listImageParts, String exportPublicationRelativePath) {
    List<String> result = new ArrayList<String>();
    if ((listImageParts != null) && !listImageParts.isEmpty()) {
      Iterator<String> iter = listImageParts.iterator();
      while (iter.hasNext()) {
        String imagePath = iter.next();
        File f = new File(imagePath);
        try {// TODO: a revoir
          FileRepositoryManager.copyFile(imagePath, exportPublicationPath + File.separator + f.
              getName());
          iter.remove();
          result.add(exportPublicationRelativePath + File.separator + f.getName());
        } catch (IOException ex) {
          // TODO: gerer l exception!!
        }
      }
    }
    return result;
  }

  /**
   * Methode copiant les images contenues dans le dossier d'exportation de la publication. Cette
   * methode met a  jour le fichier wysiwyg avec les nouveaux chemins d'images avant de le copier
   * dans l'exportation
   *
   * @param pubId - id de la publication a  exporter
   * @param componentId - id du composant contenant la publication a  exporter
   * @param exportPublicationPath - dossier d'exportation de la publication
   * @return le contenu du fichier wysiwyg
   */
  public void copyWysiwygImageForExport(String pubId, String componentId,
      String exportPublicationPath) {
    ForeignPK foreignKey = new ForeignPK(pubId, componentId);
    Collection<SimpleDocument> documents = AttachmentServiceFactory.getAttachmentService().
        listDocumentsByForeignKey(foreignKey, null);
    for (SimpleDocument attDetail : documents) {
      try {
        FileRepositoryManager.copyFile(attDetail.getAttachmentPath(),
            exportPublicationPath + File.separator + attDetail.getFilename());
      } catch (IOException ex) {
        // TODO: gerer l exception!!
      }
    }
  }

  private List<NodePositionType> getExistingTopics(String userId, List<NodePositionType> nodeTypes,
      String componentId) {
    List<NodePositionType> topics = new ArrayList<NodePositionType>();
    for (NodePositionType node : nodeTypes) {
      if (node.getId() >= 0) {
        // defined node must exists
        if (isTopicExist(node.getId(), componentId)) {
          topics.add(node);
        }
      } else if (StringUtil.isDefined(node.getExplicitPath()) && node.getId() == -1) {
        // explicit mode is used. Topics must be created on-the-fly if needed.
        String[] path = node.getExplicitPath().substring(1).split("/");
        NodePK nodePK = new NodePK("unknown", componentId);
        String parentId = NodePK.ROOT_NODE_ID;
        for (String name : path) {
          NodeDetail existingNode = null;
          try {
            existingNode = getNodeBm().getDetailByNameAndFatherId(nodePK, name, Integer.parseInt(
                parentId));
          } catch (Exception e) {
            SilverTrace.info("importExport", "GEDImportExport.getExistingTopics",
                "root.MSG_GEN_PARAM_VALUE", "node named '" + name + "' in path '" + node.
                getExplicitPath() + "' does not exist");
          }
          if (existingNode != null) {
            // topic exists
            parentId = existingNode.getNodePK().getId();
          } else {
            // topic does not exists, creating it
            NodeDetail newNode = new NodeDetail();
            newNode.setName(name);
            newNode.setNodePK(new NodePK("unknown", componentId));
            newNode.setFatherPK(new NodePK(parentId, componentId));
            newNode.setCreatorId(userId);
            NodePK newNodePK;
            try {
              newNodePK = getNodeBm().createNode(newNode);
            } catch (Exception e) {
              SilverTrace.error("importExport", "GEDImportExport.getExistingTopics",
                  "root.MSG_GEN_PARAM_VALUE",
                  "Can't create node named '" + name + "' in path '" + node.getExplicitPath() + "'",
                  e);
              return new ArrayList<NodePositionType>();
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
      getNodeBm().getHeader(new NodePK(Integer.toString(nodeId), "useless", componentId));
    } catch (Exception e) {
      return false;
    }
    return true;
  }

  /**
   * Methode ajoutant un theme a  un theme deja existant. Si le theme a  ajouter existe lui aussi
   * (par exemple avec un meme ID), il n'est pas modifie et la methode ne fait rien et ne leve
   * aucune exception.
   *
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
   *
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
   *
   * @param unitReport le rapport d'import unitaire.
   * @param nodeDetail le detail du noeud a  creer.
   * @param parentTopicId l'identifiant du noeud parent, ou 0 pour designer le noeud racine.
   * @return l'objet qui represente le detail du nouveau noeud cree ou du noeud existant (en
   * particulier si un noeud de meme ID existe deja ).
   * @throws ImportExportException en cas d'anomalie lors de la creation du noeud.
   */
  public NodeDetail createTopicForUnitImport(
      UnitReport unitReport, NodeDetail nodeDetail, int parentTopicId)
      throws ImportExportException {
    unitReport.setItemName(nodeDetail.getName());
    NodePK nodePk = addSubTopicToTopic(nodeDetail, parentTopicId, unitReport);
    try {
      return getNodeBm().getDetail(nodePk);
    } catch (Exception ex) {
      unitReport.setError(UnitReport.ERROR_NOT_EXISTS_TOPIC);
      SilverTrace.error("importExport", "GEDImportExport.createTopicForUnitImport()",
          "root.EX_NO_MESSAGE", ex);
      throw new ImportExportException("GEDImportExport.createTopicForUnitImport",
          "importExport.EX_NODE_CREATE", ex);
    }
  }

  /**
   * Methode de creation d'une publication dans le cas d'une importation unitaire avec meta-donnees
   * definies dans le fichier xml d'importation.
   *
   * @param unitReport
   * @param userDetail
   * @param pubDetail
   * @param listNode_Type
   * @return
   */
  public PublicationDetail createPublicationForUnitImport(UnitReport unitReport,
      ImportSettings settings, PublicationDetail pubDetail, List<NodePositionType> listNode_Type) {
    unitReport.setItemName(pubDetail.getName());
    // On cree la publication
    return processPublicationDetail(unitReport, settings, pubDetail, listNode_Type);
  }

  /**
   * Methode de creation d'une publication dans le cas d'une importation massive
   *
   * @param unitReport
   * @param userDetail
   * @param pubDetail
   * @param nodeId
   * @return
   * @throws ImportExportException
   */
  public PublicationDetail createPublicationForMassiveImport(UnitReport unitReport,
      PublicationDetail pubDetail, ImportSettings settings) throws ImportExportException {
    unitReport.setItemName(pubDetail.getName());
    NodePositionType nodePosType = new NodePositionType();
    nodePosType.setId(Integer.valueOf(settings.getFolderId()));
    List<NodePositionType> listNode_Type = new ArrayList<NodePositionType>(1);
    listNode_Type.add(nodePosType);
    return processPublicationDetail(unitReport, settings, pubDetail, listNode_Type);
  }

  /**
   * Ajoute un sous-noeud a  un noeud existant a  partir d'un repertoire du systeme de fichiers. Le
   * nom de ce repertoire represente le noeud a  creer. Utile pour les imports massifs de noeuds et
   * de publications a  partir d'une hierarchie de dossiers et de fichiers.
   *
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
      NodeDetail nodeDetail = new NodeDetail("unknow", directoryName, directoryName, null, null,
          null, "0", "useless");
      nodeDetail.setNodePK(addSubTopicToTopic(nodeDetail, topicId, massiveReport));
      return nodeDetail;
    } catch (Exception ex) {
      throw new ImportExportException("GEDImportExport.addSubTopicToTopic",
          "importExport.EX_NODE_CREATE", ex);
    }
  }

  /**
   * Methode recuperant le silverObjectId d'un objet d'id id
   *
   * @param id - id de la publication
   * @return le silverObjectId de l'objet d'id id
   * @throws Exception
   */
  public abstract int getSilverObjectId(String id) throws Exception;

  /**
   * Methode de recuperation de la publication complete utilisee pour l'exportation
   *
   * @param pubId
   * @param componentId
   * @return
   * @throws ImportExportException
   */
  public PublicationType getPublicationCompleteById(String pubId, String componentId)
      throws ImportExportException {
    PublicationType publicationType = new PublicationType();
    try {
      CompletePublication pubComplete = getCompletePublication(new PublicationPK(pubId,
          getCurrentComponentId()));

      // Recuperation de l'objet PublicationDetail
      PublicationDetail publicationDetail = pubComplete.getPublicationDetail();

      InfoDetail infoDetail = pubComplete.getInfoDetail();
      PublicationContentType pubContent = null;
      if (infoDetail != null && !(infoDetail.getInfoImageList().isEmpty() && infoDetail.
          getInfoTextList().isEmpty())) {
        // la publication a un contenu de type DBModel
        pubContent = new PublicationContentType();
        DBModelContentType dbModel = new DBModelContentType();
        pubContent.setDBModelContentType(dbModel);

        // Recuperation des textes
        Collection<InfoTextDetail> listInfoText = infoDetail.getInfoTextList();
        if (listInfoText != null && !listInfoText.isEmpty()) {
          ArrayList<String> listTextParts = new ArrayList<String>();
          for (InfoTextDetail infoText : listInfoText) {
            listTextParts.add(infoText.getContent());
          }
          dbModel.setListTextParts(listTextParts);
        }

        // Recuperation des images
        Collection<InfoImageDetail> listInfoImage = infoDetail.getInfoImageList();
        if (listInfoImage != null && !listInfoImage.isEmpty()) {
          ArrayList<String> listImageParts = new ArrayList<String>();
          for (InfoImageDetail imageDetail : listInfoImage) {
            String path = FileRepositoryManager.getAbsolutePath(componentId) + File.separator
                + "images";
            listImageParts.add(path + File.separator + imageDetail.getPhysicalName());
          }
          dbModel.setListImageParts(listImageParts);
        }

        // Recuperation du model
        ModelDetail modelDetail = pubComplete.getModelDetail();
        dbModel.setId(Integer.parseInt(modelDetail.getId()));
      } else if (!StringUtil.isInteger(publicationDetail.getInfoId())) {
        // la publication a un contenu de type XMLTemplate (formTemplate)
        pubContent = new PublicationContentType();
        List<XMLField> xmlFields = getFormTemplateBm().getXMLFieldsForExport(publicationDetail.
            getPK().getInstanceId() + ":" + publicationDetail.getInfoId(), pubId);
        SilverTrace.info("importExport", "GEDImportExport.getPublicationCompleteById()",
            "root.MSG_GEN_PARAM_VALUE", "# of xmlField = " + xmlFields.size());
        XMLModelContentType xmlModel = new XMLModelContentType(publicationDetail.getInfoId());
        xmlModel.setFields(xmlFields);
        pubContent.setXMLModelContentType(xmlModel);
      } else if (WysiwygController.haveGotWysiwyg(publicationDetail.getPK().getInstanceId(), pubId,
          I18NHelper.checkLanguage(publicationDetail.getLanguage()))) {
        pubContent = new PublicationContentType();
        WysiwygContentType wysiwygContentType = new WysiwygContentType();
        String wysiwygFileName = WysiwygController.getWysiwygFileName(pubId, I18NHelper
            .checkLanguage(publicationDetail.getLanguage()));
        wysiwygContentType.setPath(wysiwygFileName);
        pubContent.setWysiwygContentType(wysiwygContentType);
      }
      publicationType.setPublicationContentType(pubContent);
      publicationType.setPublicationDetail(publicationDetail);
      publicationType.setId(Integer.parseInt(pubId));
      publicationType.setComponentId(componentId);

      // Recherche du nom et du prenom du createur de la pub pour le marschalling
      UserDetail userDetail = organizationController.getUserDetail(publicationDetail.getCreatorId());
      if (userDetail != null) {
        String nomPrenomCreator = userDetail.getDisplayedName().trim();
        publicationDetail.setCreatorName(nomPrenomCreator);
      }
    } catch (Exception ex) {
      throw new ImportExportException("importExport", "", "", ex);
    }
    return publicationType;
  }

  /**
   * Methode renvoyant la liste des topics de la publication sous forme de NodePK
   *
   * @param pubId - id de la publication dont on veut les topics
   * @param componentId
   * @return - liste des nodesPk de la publication
   * @throws ImportExportException
   */
  public List<NodePK> getAllTopicsOfPublication(PublicationPK pubPK)
      throws ImportExportException {
    Collection<NodePK> listNodePk = getPublicationBm().getAllFatherPK(pubPK);
    return new ArrayList<NodePK>(listNodePk);
  }

  public List<NodePK> getAliases(PublicationPK pubPK) throws ImportExportException {
    List<NodePK> pks = new ArrayList<NodePK>();
    Collection<Alias> aliases = getPublicationBm().getAlias(pubPK);
    for (Alias alias : aliases) {
      if (!alias.getInstanceId().equals(pubPK.getInstanceId())) {
        pks.add(new NodePK(alias.getId(), alias.getInstanceId()));
      }
    }
    return pks;
  }

  public List<NodePK> getTopicTree(NodePK pk) throws ImportExportException {
    List<NodePK> listNodePk = new ArrayList<NodePK>();
    Collection<NodeDetail> path = getNodeBm().getPath(pk);
    for (NodeDetail detail : path) {
      listNodePk.add(detail.getNodePK());
    }
    return listNodePk;

  }

  public ModelDetail getModelDetail(int idModelDetail) throws ImportExportException {
    return getPublicationBm().getModelDetail(new ModelPK(String.valueOf(idModelDetail)));
  }

  /**
   * @param string
   */
  @Override
  public void setCurrentComponentId(String string) {
    super.setCurrentComponentId(string);
  }

  public abstract void publicationNotClassifiedOnPDC(String pubId) throws Exception;

  /**
   * Specific Kmax: Create publication with no nodeFather
   *
   * @param pubDetail
   * @return pubDetail
   */
  protected abstract PublicationDetail createPublication(PublicationDetail pubDetail)
      throws Exception;

  public Collection<Coordinate> getPublicationCoordinates(String pubId, String componentId)
      throws ImportExportException {
    try {
      return getPublicationBm().getCoordinates(pubId, componentId);
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
      String dest = FileRepositoryManager.getAbsolutePath(pubDetail.getPK().getInstanceId())
          + "images"
          + File.separator + physicalName;
      try {
        FileRepositoryManager.copyFile(filePath, dest);
      } catch (Exception e) {
        SilverTrace
            .error("importExport", "GEDImportExport.processThumbnail()", "root.MSG_GEN_PARAM_VALUE",
                "filePath = " + filePath, e);
      }
      ThumbnailDetail thumbnailDetail = new ThumbnailDetail(pubDetail.getPK().getComponentName(),
          Integer.valueOf(pubDetail.getPK().getId()),
          ThumbnailDetail.THUMBNAIL_OBJECTTYPE_PUBLICATION_VIGNETTE);
      thumbnailDetail.setOriginalFileName(physicalName);
      thumbnailDetail.setOriginalFileName(mimeType);
      ThumbnailController.createThumbnail(thumbnailDetail, 50, 50);

    }

  }

  protected abstract void updatePublication(PublicationDetail pubDet_temp,
      PublicationDetail pubDetailToCreate, UserDetail userDetail) throws Exception;

  protected abstract String createPublicationIntoTopic(PublicationDetail pubDet_temp,
      NodePK topicPK, UserDetail userDetail) throws Exception;

  protected abstract void addPublicationToTopic(PublicationPK pubPK, NodePK topicPK)
      throws Exception;

  protected abstract CompletePublication getCompletePublication(PublicationPK pk) throws Exception;
}
