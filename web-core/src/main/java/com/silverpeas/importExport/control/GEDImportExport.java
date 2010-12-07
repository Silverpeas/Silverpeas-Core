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

/*
 * Created on 25 janv. 2005
 */
package com.silverpeas.importExport.control;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import com.silverpeas.attachment.importExport.AttachmentImportExport;
import com.silverpeas.form.DataRecord;
import com.silverpeas.form.Field;
import com.silverpeas.form.FieldDisplayer;
import com.silverpeas.form.FieldTemplate;
import com.silverpeas.form.PagesContext;
import com.silverpeas.form.RecordSet;
import com.silverpeas.form.TypeManager;
import com.silverpeas.form.fieldType.FileField;
import com.silverpeas.form.importExport.XMLField;
import com.silverpeas.formTemplate.ejb.FormTemplateBm;
import com.silverpeas.formTemplate.ejb.FormTemplateBmHome;
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
import com.silverpeas.util.StringUtil;
import com.silverpeas.wysiwyg.importExport.WysiwygContentType;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.silverpeas.wysiwyg.WysiwygException;
import com.stratelia.silverpeas.wysiwyg.control.WysiwygController;
import com.stratelia.webactiv.beans.admin.OrganizationController;
import com.stratelia.webactiv.beans.admin.UserDetail;
import com.stratelia.webactiv.util.EJBUtilitaire;
import com.stratelia.webactiv.util.FileRepositoryManager;
import com.stratelia.webactiv.util.FileServerUtils;
import com.stratelia.webactiv.util.JNDINames;
import com.stratelia.webactiv.util.ResourceLocator;
import com.stratelia.webactiv.util.attachment.control.AttachmentController;
import com.stratelia.webactiv.util.attachment.ejb.AttachmentPK;
import com.stratelia.webactiv.util.attachment.model.AttachmentDetail;
import com.stratelia.webactiv.util.exception.UtilException;
import com.stratelia.webactiv.util.fileFolder.FileFolderManager;
import com.stratelia.webactiv.util.indexEngine.model.IndexManager;
import com.stratelia.webactiv.util.node.control.NodeBm;
import com.stratelia.webactiv.util.node.control.NodeBmHome;
import com.stratelia.webactiv.util.node.model.NodeDetail;
import com.stratelia.webactiv.util.node.model.NodePK;
import com.stratelia.webactiv.util.publication.control.PublicationBm;
import com.stratelia.webactiv.util.publication.control.PublicationBmHome;
import com.stratelia.webactiv.util.publication.info.model.InfoDetail;
import com.stratelia.webactiv.util.publication.info.model.InfoImageDetail;
import com.stratelia.webactiv.util.publication.info.model.InfoPK;
import com.stratelia.webactiv.util.publication.info.model.InfoTextDetail;
import com.stratelia.webactiv.util.publication.info.model.ModelDetail;
import com.stratelia.webactiv.util.publication.info.model.ModelPK;
import com.stratelia.webactiv.util.publication.model.CompletePublication;
import com.stratelia.webactiv.util.publication.model.PublicationDetail;
import com.stratelia.webactiv.util.publication.model.PublicationPK;

/**
 * Classe mÃ©tier de crÃ©ation d'entitÃ©s silverpeas utilisÃ©e par le moteur d'importExport.
 * @author sDevolder.
 */
public abstract class GEDImportExport extends ComponentImportExport {

  // Variables
  private static final OrganizationController organizationController = new OrganizationController();
  private PublicationBm publicationBm = null;
  private FormTemplateBm formTemplateBm = null;
  private NodeBm nodeBm = null;
  private AttachmentImportExport attachmentIE = new AttachmentImportExport();

  /**
   * Constructeur public de la classe
   * @param userDetail - informations sur l'utilisateur faisant appel au moteur d'importExport
   * @param targetComponentId - composant silverpeas cible
   * @param topicId - topic cible du composant targetComponentId
   */
  public GEDImportExport(UserDetail curentUserDetail, String currentComponentId) {
    super(curentUserDetail, currentComponentId);
  }

  /**
   * @return l'EJB PublicationBM
   * @throws ImportExportException
   */
  protected PublicationBm getPublicationBm() throws ImportExportException {

    if (publicationBm == null) {
      try {
        PublicationBmHome publicationBmHome = (PublicationBmHome) EJBUtilitaire.getEJBObjectRef(
            JNDINames.PUBLICATIONBM_EJBHOME, PublicationBmHome.class);
        publicationBm = publicationBmHome.create();
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
        FormTemplateBmHome formTemplateBmHome = (FormTemplateBmHome) EJBUtilitaire.getEJBObjectRef(
            JNDINames.FORMTEMPLATEBM_EJBHOME, FormTemplateBmHome.class);
        formTemplateBm = formTemplateBmHome.create();
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
        NodeBmHome kscEjbHome = (NodeBmHome) EJBUtilitaire.getEJBObjectRef(
            JNDINames.NODEBM_EJBHOME, NodeBmHome.class);
        nodeBm = kscEjbHome.create();
      } catch (Exception e) {
        throw new ImportExportException("GEDImportExport.getNodeBm()",
            "root.EX_CANT_GET_REMOTE_OBJECT", e);
      }
    }
    return nodeBm;
  }

  /**
   * MÃ©thode de crÃ©ation ou mise Ã  jour d'une publication utilisÃ©e par le manager d'importation de
   * repository du * moteur d'importExport. Cas particulier: si une publication de mÃªme nom existe
   * dÃ©jÃ  dans le composant, alors une nouvelle publication ne sera crÃ©Ã©e que si le premier node Ã 
   * lier ne contient pas la publication de mÃªme nom.
   * @param pubDetailToCreate - publication Ã  crÃ©er ou Ã  mettre Ã  jour.
   * @return l'objet PublicationDetail contenant les informations de la publication crÃ©Ã©e ou mise Ã 
   * jour.
   * @throws ImportExportException
   */
  private PublicationDetail processPublicationDetail(UnitReport unitReport, UserDetail userDetail,
      PublicationDetail pubDetailToCreate, List listOfNodeTypes) {

    // vÃ©rification des ids de thÃ¨me

    List existingTopics = new ArrayList();
    if (isKmax()) {
      existingTopics = listOfNodeTypes;
    } else {
      existingTopics = getExistingTopics(listOfNodeTypes, pubDetailToCreate.getPK().getInstanceId());
    }

    if (existingTopics.size() == 0 && !isKmax()) {
      // Aucun id ne correspond Ã  un thÃ¨me du composant
      // Le classement de la publication est impossible
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

      // VÃ©rification de l'existance d'une publication de mÃªme nom dans le premier node donnÃ©
      boolean pubIdExists = false;
      if (pubDetailToCreate.getId() != null) {
        try {
          Integer.parseInt(pubDetailToCreate.getId());
          pubIdExists = true;
        } catch (NumberFormatException ex) {
        }
      }
      if (!pubIdExists) {
        try {
          Iterator itListNode_Type = existingTopics.iterator();
          if (itListNode_Type.hasNext()) {
            NodePositionType node_Type = (NodePositionType) itListNode_Type.next();
            pubDet_temp = getPublicationBm().getDetailByNameAndNodeId(pubDetailToCreate.getPK(),
                pubDetailToCreate.getName(), node_Type.getId());
          }
        } catch (Exception pre) {
          pubAlreadyExist = false;
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
          updatePublication(pubDet_temp, pubDetailToCreate, userDetail);
          unitReport.setStatus(UnitReport.STATUS_PUBLICATION_UPDATED);
        } catch (Exception e) {
          unitReport.setError(UnitReport.ERROR_ERROR);
          return null;
        }
      } else {
        // la publication n'existe pas
        pubDet_temp = pubDetailToCreate;
      }

      // Traitement de la vignette
      if (StringUtil.isDefined(pubDetailToCreate.getImage())) {
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
        // Ajout de la publication aux topics
        Iterator itListNode_Type = existingTopics.iterator();
        if (!pubAlreadyExist) {
          NodePositionType node_Type = (NodePositionType) itListNode_Type.next();
          try {
            NodePK topicPK = new NodePK(Integer.toString(node_Type.getId()),
                pubDetailToCreate.getPK());
            pubId = createPublicationIntoTopic(pubDet_temp, topicPK, userDetail);
            pubDet_temp.getPK().setId(pubId);
          } catch (Exception e) {
            unitReport.setError(UnitReport.ERROR_ERROR);
          }
          unitReport.setStatus(UnitReport.STATUS_PUBLICATION_CREATED);
        }
        if (isKmelia()) {
          while (itListNode_Type.hasNext()) {
            // On ajoute la publication crÃ©Ã©e aux autres topics
            NodePositionType node_Type = (NodePositionType) itListNode_Type.next();
            try {
              NodePK topicPK = new NodePK(Integer.toString(node_Type.getId()), pubDetailToCreate.
                  getPK());
              PublicationPK pubPK = new PublicationPK(pubId, pubDetailToCreate.getPK());
              if (pubAlreadyExist) {
                // VÃ©rification dans le cas d une publication dÃ©jÃ  existante de sa prÃ©sence dans
                // le
                // thÃ¨me
                try {
                  getPublicationBm().getDetailByNameAndNodeId(pubDet_temp.getPK(),
                      pubDet_temp.getName(), node_Type.getId());
                } catch (Exception ex) {
                  // La publication n est pas dans le thÃ¨me
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

  public void createPublicationContent(UnitReport unitReport, int pubId,
      PublicationContentType pubContent, String userId) throws ImportExportException {
    createPublicationContent(unitReport, pubId, pubContent, userId, FileServerUtils.
        getApplicationContext());
  }

  /**
   * MÃ©thode de crÃ©ation du contenu d une publication importÃ©e
   * @param unitReport
   * @param pubId - id de la publication pour laquelle on importe un contenu
   * @param pubContent - object de mapping castor contenant les informations d importation du
   * contenu
   * @param userId
   * @throws ImportExportException
   */
  public void createPublicationContent(UnitReport unitReport, int pubId,
      PublicationContentType pubContent, String userId, String webContext) throws
      ImportExportException {

    DBModelContentType dbModelType = pubContent.getDBModelContentType();
    WysiwygContentType wysiwygType = pubContent.getWysiwygContentType();
    XMLModelContentType xmlModel = pubContent.getXMLModelContentType();
    try {
      if (dbModelType != null) {
        // Contenu DBModel
        createDBModelContent(unitReport, dbModelType, Integer.toString(pubId));
      } else if (wysiwygType != null) {
        // Contenu Wysiwyg
        createWysiwygContent(unitReport, pubId, wysiwygType, userId, webContext);
      } else if (xmlModel != null) {
        createXMLModelContent(unitReport, xmlModel, Integer.toString(pubId), userId);
      }
    } catch (ImportExportException ex) {
      throw ex;
    } catch (Exception ex) {
      throw new ImportExportException("GEDImportExport.createPublicationContent()",
          "importExport.EX_CANT_CREATE_CONTENT", "pubId = " + pubId, ex);
    }
  }

  private void createXMLModelContent(UnitReport unitReport, XMLModelContentType xmlModel,
      String pubId, String userId) throws Exception {
    PublicationPK pubPK = new PublicationPK(pubId, getCurrentComponentId());
    PublicationDetail pubDetail = getPublicationBm().getDetail(pubPK);

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
      getPublicationBm().setDetail(pubDetail);
     
      publicationTemplateManager.addDynamicPublicationTemplate(getCurrentComponentId() + ":"
          + xmlFormShortName, xmlFormShortName + ".xml");
    }

    PublicationTemplate pub =
        publicationTemplateManager.getPublicationTemplate(getCurrentComponentId()
            + ":" + xmlModel.getName());

    RecordSet set = pub.getRecordSet();
    // Form form = pub.getUpdateForm();

    DataRecord data = set.getRecord(pubId);
    if (data == null) {
      data = set.getEmptyRecord();
      data.setId(pubId);
    }

    List xmlFields = xmlModel.getFields();
    XMLField xmlField = null;
    Field field = null;
    String xmlFieldName = null;
    String xmlFieldValue = null;
    String fieldValue = null;
    for (int f = 0; f < xmlFields.size(); f++) {
      xmlField = (XMLField) xmlFields.get(f);
      xmlFieldName = xmlField.getName();
      xmlFieldValue = xmlField.getValue();
      try {
        field = data.getField(xmlFieldName);
        if (field != null) {
          FieldTemplate fieldTemplate = pub.getRecordTemplate().getFieldTemplate(xmlFieldName);
          if (fieldTemplate != null) {
            FieldDisplayer fieldDisplayer = TypeManager.getInstance().getDisplayer(
                    field.getTypeName(), fieldTemplate.getDisplayerName());
            if (field.getTypeName().equals(FileField.TYPE) && StringUtil.isDefined(xmlFieldValue)) {
              String context = "Images";
              if (fieldTemplate.getDisplayerName().equals("image")) {
                context = "XMLFormImages";
              }

              String imagePath = xmlFieldValue;
              String imageName = imagePath.substring(imagePath.lastIndexOf(File.separator) + 1,
                  imagePath.length());
              String imageExtension = FileRepositoryManager.getFileExtension(imagePath);
              String imageMimeType = AttachmentController.getMimeType(imagePath);

              String physicalName = new Long(new Date().getTime()).toString() + "." + imageExtension;

              String path = AttachmentController.createPath(getCurrentComponentId(), context);
              FileRepositoryManager.copyFile(imagePath, path + physicalName);
              File file = new File(path + physicalName);
              long size = file.length();

              if (size > 0) {
                AttachmentDetail ad = createAttachmentDetail(pubId, getCurrentComponentId(),
                    physicalName, imageName, imageMimeType, size, context, userId);
                ad = AttachmentController.createAttachment(ad, true);
                fieldValue = ad.getPK().getId();
              } else {
                // le fichier a tout de même été créé sur le serveur avec une taille 0!,
                // il faut le supprimer
                FileFolderManager.deleteFolder(path + physicalName);
              }
            } else {
              fieldValue = xmlFieldValue;
            }

            fieldDisplayer.update(fieldValue, field, fieldTemplate, new PagesContext());
          }
        }
      } catch (Exception e) {
        SilverTrace.warn("importExport", "GEDImportExport.createXMLModelContent",
            "importExport.EX_CANT_IMPORT_XML_FIELD", "xmlField = " + xmlFieldName, e);
      }
    }
    set.save(data);
  }

  private AttachmentDetail createAttachmentDetail(String objectId, String componentId,
      String physicalName, String logicalName, String mimeType, long size, String context,
      String userId) {
    // create AttachmentPK with spaceId and componentId
    AttachmentPK atPK = new AttachmentPK(null, "useless", componentId);

    // create foreignKey with spaceId, componentId and id
    // use AttachmentPK to build the foreign key of customer object.
    AttachmentPK foreignKey = new AttachmentPK("-1", "useless", componentId);
    if (objectId != null) {
      foreignKey.setId(objectId);
    }

    // create AttachmentDetail Object
    AttachmentDetail ad = new AttachmentDetail(atPK, physicalName, logicalName, null, mimeType,
        size, context, new Date(), foreignKey);
    ad.setAuthor(userId);

    return ad;
  }

  private void createDBModelContent(UnitReport unitReport, DBModelContentType dbModelType,
      String pubId) throws ImportExportException, RemoteException {

    PublicationPK pubPK = new PublicationPK(pubId, "useless", getCurrentComponentId());
    InfoDetail infoDetailFromPub = getPublicationBm().getInfoDetail(pubPK);
    ModelPK modelPK = new ModelPK(Integer.toString(dbModelType.getId()), "useless",
        getCurrentComponentId());

    ModelDetail modelDetail = getPublicationBm().getModelDetail(modelPK);

    if (infoDetailFromPub != null && !infoDetailFromPub.getPK().getId().equals("0")) {
      // un contenu DBModel existe dÃ©jÃ 
      if (Integer.parseInt(modelDetail.getId()) == dbModelType.getId()) {
        // on ne met Ã  jour le contenu que si le modÃ¨le d'import est le mÃªme
        // PrÃ©paration des donnÃ©es DBModel Ã  creer
        InfoDetail infoDetail = prepareDbModelContent(unitReport, dbModelType);
        infoDetail.setPK(infoDetailFromPub.getPK());
        // Mise Ã  jour du contenu DBModel
        getPublicationBm().updateInfoDetail(pubPK, infoDetail);
      } else {
        // le modÃ¨le existant n'est pas le mÃªme que l'importÃ©
        unitReport.setError(UnitReport.ERROR_CANT_UPDATE_CONTENT);
      }
    } else if (!WysiwygController.haveGotWysiwyg("useless", getCurrentComponentId(), pubId)) {
      // on ne remplace pas un type de contenu par un autre
      // PrÃ©paration des donnÃ©es DBModel Ã  creer
      if (modelDetail == null) {
        unitReport.setError(UnitReport.ERROR_CANT_CREATE_CONTENT);
      } else {
        InfoDetail infoDetail = prepareDbModelContent(unitReport, dbModelType);
        // CrÃ©ation du contenu DBModel
        getPublicationBm().createInfoDetail(pubPK, modelPK, infoDetail);
      }
    } else {
      unitReport.setError(UnitReport.ERROR_CANT_UPDATE_CONTENT);// la pub existante contient un
      // wysiwyg
    }
  }

  /**
   * MÃ©thode prÃ©parant les dataModels pour la crÃ©ation en base d un contenu DBModel, cette mÃ©thode
   * se charge Ã©galement de la copie des fichiers du contenu sur le serveur
   * @param dbModelType - objet de mapping castor contenant les informations de contenu de type
   * DBModel
   * @return
   */
  private InfoDetail prepareDbModelContent(UnitReport unitReport, DBModelContentType dbModelType) {

    ArrayList listInfoImage = null;
    ArrayList listInfoText = null;
    int imageOrder = 1;
    int textOrder = 0;

    // if infoDetailModel exists...
    List listTextParts = dbModelType.getListTextParts();
    List listImagesParts = dbModelType.getListImageParts();

    // PrÃ©paration des textes dbmodel pour crÃ©ation en base
    if (listTextParts != null) {
      Iterator itListTextParts = listTextParts.iterator();
      while (itListTextParts.hasNext()) {
        String textPart = (String) itListTextParts.next();
        if (listInfoText == null) {
          listInfoText = new ArrayList();
        }
        listInfoText.add(new InfoTextDetail(null, String.valueOf(textOrder++), null, textPart));
      }
    }

    // Preparation du images dbmodel pour cretion en base
    if (listImagesParts != null) {
      Iterator<String> itListImagesParts = listImagesParts.iterator();
      while (itListImagesParts.hasNext()) {
        String imagePath = itListImagesParts.next();
        File f = new File(imagePath);        
        long size = f.length();
        String mimeType = AttachmentController.getMimeType(imagePath);
        if (listInfoImage == null) {
          listInfoImage = new ArrayList<InfoImageDetail>();
        }
        listInfoImage.add(new InfoImageDetail(null, String.valueOf(imageOrder++), null,
            imagePath, imagePath.substring(imagePath.lastIndexOf(File.separator) + 1), "",
            mimeType, size));
      }
      // copie sur le serveur des images
      copyDBmodelImagePartsForImport(unitReport, getCurrentComponentId(), listInfoImage);
    }

    // Creation du contenu en base
    InfoDetail infoDetail = new InfoDetail(
        new InfoPK("unknown", "useless", getCurrentComponentId()), listInfoText, listInfoImage,
        null, null);

    return infoDetail;
  }

  /**
   * Methode de creation d'un contenu de type wysiwyg
   * @param pubId - id de la publication pour laquelle on cree le contenu wysiwyg
   * @param wysiwygType - objet de mapping castor contenant les informations de contenu de type
   * Wysiwyg
   */
  private void createWysiwygContent(UnitReport unitReport, int pubId,
      WysiwygContentType wysiwygType, String userId, String webContext) throws UtilException,
      WysiwygException,
      ImportExportException {

    String wysiwygFileName = WysiwygController.getWysiwygFileName(Integer.toString(pubId));
    String wysiwygPath = AttachmentController.createPath(getCurrentComponentId(), "wysiwyg");

    // RÃ©cupÃ©ration du nouveau contenu wysiwyg
    File f = null;
    String wysiwygText = "";
    try {
      f = new File(wysiwygType.getPath());
      wysiwygText = FileFolderManager.getCode(f.getParent(), f.getName());
    } catch (UtilException ex) {
      unitReport.setError(UnitReport.ERROR_NOT_EXISTS_OR_INACCESSIBLE_FILE_FOR_CONTENT);
      throw new ImportExportException("GEDImportExport.createPublicationContent()",
          "importExport.EX_CANT_CREATE_CONTENT", "file = " + f.getPath(), ex);
    }
    if (wysiwygText == null) {
      unitReport.setError(UnitReport.ERROR_NOT_EXISTS_OR_INACCESSIBLE_FILE_FOR_CONTENT);
      throw new ImportExportException("GEDImportExport.createPublicationContent()",
          "importExport.EX_CANT_CREATE_CONTENT", "file = " + f.getPath());
    }

    // Suppression de tout le contenu wysiwyg s il existe
    if (WysiwygController.haveGotWysiwyg("useless", getCurrentComponentId(), Integer
        .toString(pubId))) {
      // TODO: verifier d abord que la mise a jour est valide?!
      try {
        WysiwygController.deleteWysiwygAttachmentsOnly("useless", getCurrentComponentId(), Integer.
            toString(pubId));
      } catch (WysiwygException ex) {/* TODO: gerer l exception */

      }
      File file = new File(wysiwygPath + File.separator + wysiwygFileName);
      file.delete();
    }

    // CrÃ©ation du fichier de contenu wysiwyg sur les serveur
    String imagesContext = WysiwygController.getImagesFileName(Integer.toString(pubId));
    String newWysiwygText = replaceWysiwygImagesPathForImport(unitReport, pubId, f.getParent(),
        wysiwygText, imagesContext, webContext);
    newWysiwygText = removeWysiwygStringsForImport(newWysiwygText);
    newWysiwygText = replaceWysiwygStringsForImport(newWysiwygText);
    WysiwygController.createFileAndAttachment(newWysiwygText, wysiwygFileName, "useless",
        getCurrentComponentId(), "wysiwyg", Integer.toString(pubId), userId);
  }

  /**
   * MÃ©thode chargÃ©e de copier les fichiers images rÃ©fÃ©rencÃ©s par le contenu wysiwyg sur le serveur
   * et de mettre Ã  jour le contenu wysiwyg avec ces nouveaux liens
   * @param wysiwygText - contenu wysiwyg passÃ© en paramÃ¨tre
   * @param path - chemin cible des images wysiwyg
   * @return - le contenu wysiwyg mis Ã  jour
   */
  private String replaceWysiwygImagesPathForImport(UnitReport unitReport, int pubId,
      String wysiwygImportedPath, String wysiwygText, String imageContext, String webContext)
      throws ImportExportException {

    int finPath = 0;
    int debutPath = 0;
    StringBuilder newWysiwygText = new StringBuilder();

    if (wysiwygText.indexOf("img src=\"", finPath) == -1) {
      newWysiwygText.append(wysiwygText);
    } else {
      // Algorithme d extraction des fichiers images du contenu
      while ((debutPath = wysiwygText.indexOf("img src=\"", finPath)) != -1) {
        debutPath += 9;
        newWysiwygText.append(wysiwygText.substring(finPath, debutPath));
        finPath = wysiwygText.indexOf("\"", debutPath);
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
            attDetail.setPhysicalName(wysiwygImportedPath + File.separator
                + attDetail.getPhysicalName());
          }
          // TODO: chercher autres infos utiles pour la crÃ©ation des attachments ensuite
          try {
            attDetail = attachmentIE.importWysiwygAttachment(Integer.toString(pubId),
                getCurrentComponentId(), attDetail, imageContext);
            ImportReportManager.addNumberOfFilesProcessed(1);
            if (attDetail == null || attDetail.getSize() == 0) {
              unitReport.setError(UnitReport.ERROR_NOT_EXISTS_OR_INACCESSIBLE_FILE_FOR_CONTENT);
              throw new ImportExportException(
                  "GEDImportExport.replaceWysiwygImagesPathForImport()",
                  "importExport.EX_CANT_CREATE_CONTENT", "pic = " + imageSrc);
            }
            // On additionne la taille des fichiers importÃ©s au niveau du rapport
            ImportReportManager.addImportedFileSize(attDetail.getSize(), getCurrentComponentId());
            newWysiwygText.append(webContext + attDetail.getAttachmentURL());
          } catch (Exception e) {
            SilverTrace.error("importExport",
                "GEDImportExport.replaceWysiwygImagesPathForImport()",
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

    String oldString = null;
    String newString = null;

    Enumeration classes = mapping.getKeys();
    while (mapping != null && classes.hasMoreElements()) {
      oldString = (String) classes.nextElement();
      newString = mapping.getString(oldString);

      newWysiwygText = replaceWysiwygStringForImport(oldString, newString, newWysiwygText);
    }
    return newWysiwygText;
  }

  /**
   * MÃ©thode chargÃ©e de remplacer une classe Css par une autre
   * @param wysiwygText - contenu wysiwyg passÃ© en paramÃ¨tre
   * @param oldCssClass - la classe CSS Ã  remplacer
   * @param newCssClass - la nouvelle classe CSS Ã  utiliser
   * @return - le contenu wysiwyg mis Ã  jour
   */
  private String replaceWysiwygStringForImport(String oldString, String newString,
      String wysiwygText) {
    if (!StringUtil.isDefined(wysiwygText)) {
      return "";
    }

    return wysiwygText.replaceAll(oldString, newString);
  }

  private String removeWysiwygStringsForImport(String wysiwygText) {
    ResourceLocator resource = new ResourceLocator("com.silverpeas.importExport.settings.mapping",
        "");
    String dir = resource.getString("mappingDir");
    if (StringUtil.isDefined(dir)) {
      try {
        FileReader fileReader = new FileReader(dir + File.separator + "strings2Remove.txt");
        BufferedReader reader = new BufferedReader(fileReader);

        String ligne = null;
        while ((ligne = reader.readLine()) != null) {
          if ("$$removeAnchors$$".equalsIgnoreCase(ligne)) {
            wysiwygText = removeAnchors(wysiwygText);
          } else {
            wysiwygText = wysiwygText.replaceAll(ligne, "");
          }
        }
      } catch (FileNotFoundException e) {
        SilverTrace.info("importExport", "GEDImportExport", "importExport.FILE_NOT_FOUND", e);
      } catch (IOException e) {
        SilverTrace.info("importExport", "GEDImportExport", "importExport.FILE_NOT_FOUND", e);
      }
    }
    return wysiwygText;
  }

  private static String removeAnchors(String wysiwygText) {
    int fin = 0;
    int debut = 0;
    StringBuffer newWysiwygText = new StringBuffer();

    if (wysiwygText.indexOf("<a name=", fin) == -1) {
      newWysiwygText.append(wysiwygText);
    } else {
      while ((debut = wysiwygText.indexOf("<a name=", fin)) != -1) {
        newWysiwygText.append(wysiwygText.substring(fin, debut));
        debut += 8;
        fin = wysiwygText.indexOf(">", debut);
        debut = wysiwygText.indexOf("</a>", fin);
        newWysiwygText.append(wysiwygText.substring(fin + 1, debut));
        fin = debut + 4;
      }
      newWysiwygText.append(wysiwygText.substring(fin));
    }
    return newWysiwygText.toString();
  }

  /**
   * MÃ©thode copiant les images du contenu DBModel Ã  creer
   * @param componentId - id du composant de la publication dans laquelle on va creer le contenu
   * DBModel
   * @param listInfoImageDetail - liste des InfoImageDetails correspondant aux images du contenu
   * DBModel
   * @return - la liste des InfoImageDetails mise Ã  jour avec les nouveaux fichiers crÃ©Ã©s sur le
   * serveur
   */
  private Collection copyDBmodelImagePartsForImport(UnitReport unitReport, String componentId,
      Collection listInfoImageDetail) {
    // MÃ©thode copiÃ©e du KmeliaRequestRouter ligne 401

    Iterator itListInfoImageDetail = listInfoImageDetail.iterator();
    while (itListInfoImageDetail.hasNext()) {
      InfoImageDetail infoImage = (InfoImageDetail) itListInfoImageDetail.next();
      String from = infoImage.getPhysicalName();// chemin complet mappÃ© depuis l import xml
      String type = infoImage.getPhysicalName().substring(
          infoImage.getPhysicalName().indexOf(".") + 1, infoImage.getPhysicalName().length());
      String newName = new Long(new java.util.Date().getTime()).toString() + "." + type;
      infoImage.setPhysicalName(newName);
      String to = FileRepositoryManager.getAbsolutePath(componentId) + File.separator + "images"
          + File.separator + newName;
      /* TODO: a voir kmelia.getPublicationSettings().getString("imagesSubDirectory")+"\\"+ */
      try {
        FileRepositoryManager.copyFile(from, to);
        File f = new File(to);
        ImportReportManager.addImportedFileSize(f.length(), componentId);
        ImportReportManager.addNumberOfFilesProcessed(1);
      } catch (IOException ex) {
        unitReport.setError(UnitReport.ERROR_NOT_EXISTS_OR_INACCESSIBLE_FILE_FOR_CONTENT);
      }
    }

    return listInfoImageDetail;
  }

  /**
   * MÃ©thode de copie des images DBModel d'un contenu dans le dossier d'exportation d'une
   * publication
   * @param exportPublicationPath - dossier d'exportation de la publication
   * @param listImageParts - liste des images du contenu DBModel
   * @return - la liste des images mise Ã  jour
   */
  public ArrayList copyDBmodelImagePartsForExport(String exportPublicationPath,
      ArrayList listImageParts, String exportPublicationRelativePath) {

    if ((listImageParts != null) && (listImageParts.size() != 0)) {
      for (int i = 0; i < listImageParts.size(); i++) {
        String imagePath = (String) listImageParts.get(i);
        File f = new File(imagePath);
        try {// TODO: a revoir
          FileRepositoryManager.copyFile(imagePath, exportPublicationPath + File.separator
              + f.getName());
          listImageParts.remove(i);
          listImageParts.add(i, exportPublicationRelativePath + File.separator + f.getName());
        } catch (IOException ex) {
          // TODO: gerer l exception!!
        }
      }
    }
    return listImageParts;
  }

  /**
   * MÃ©thode copiant les images contenues dans le dossier d'exportation de la publication. Cette
   * mÃ©thode met Ã  jour le fichier wysiwyg avec les nouveaux chemins d'images avant de le copier
   * dans l'exportation
   * @param pubId - id de la publication Ã  exporter
   * @param componentId - id du composant contenant la publication Ã  exporter
   * @param exportPublicationPath - dossier d'exportation de la publication
   * @return le contenu du fichier wysiwyg
   */
  public void copyWysiwygImageForExport(String pubId, String componentId,
      String exportPublicationPath) {

    // Copie des images du wysiwig;
    String imagesContext = WysiwygController.getImagesFileName(pubId);
    AttachmentPK foreignKey = new AttachmentPK(pubId, "useless", componentId);
    Vector vectAttachment = AttachmentController.searchAttachmentByPKAndContext(foreignKey,
        imagesContext);
    String path = AttachmentController.createPath(componentId, imagesContext);
    Iterator itListAttachment = vectAttachment.iterator();
    while (itListAttachment.hasNext()) {
      AttachmentDetail attDetail = (AttachmentDetail) itListAttachment.next();
      try {
        FileRepositoryManager.copyFile(path + File.separator + attDetail.getPhysicalName(),
            exportPublicationPath + File.separator + attDetail.getLogicalName());
      } catch (IOException ex) {
        // TODO: gerer l exception!!
      }
    }
  }

  private List getExistingTopics(List nodeTypes, String componentId) {
    List topics = new ArrayList();
    NodePositionType node = null;
    for (int n = 0; n < nodeTypes.size(); n++) {
      node = (NodePositionType) nodeTypes.get(n);
      if (isTopicExist(node.getId(), componentId)) {
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
   * MÃ©thode ajoutant un thÃ¨me Ã  un thÃ¨me dÃ©ja existant. Si le thÃ¨me Ã  ajouter existe lui aussi (par
   * exemple avec un mÃªme ID), il n'est pas modifiÃ© et la mÃ©thode ne fait rien et ne lÃ¨ve aucune
   * exception.
   * @param nodeDetail le dÃ©tail du noeud Ã  ajouter.
   * @param topicId l'identifiant du noeud parent, ou 0 pour dÃ©signer le noeud racine.
   * @param unitReport le rapport d'import unitaire.
   * @return un objet clÃ© primaire du nouveau thÃ¨me crÃ©Ã© ou du thÃ¨me dÃ©jÃ  existant (thÃ¨me de mÃªme
   * identifiant non modifiÃ©).
   * @throws ImportExportException en cas d'anomalie lors de la crÃ©ation du noeud.
   */
  protected abstract NodePK addSubTopicToTopic(NodeDetail nodeDetail, int topicId,
      UnitReport unitReport) throws ImportExportException;

  /**
   * MÃ©thode ajoutant un thÃ¨me Ã  un thÃ¨me dÃ©ja existant. Si le thÃ¨me Ã  ajouter existe lui aussi (par
   * exemple avec un mÃªme ID), il n'est pas modifiÃ© et la mÃ©thode ne fait rien et ne lÃ¨ve aucune
   * exception.
   * @param nodeDetail l'objet node correspondant au thÃ¨me Ã  crÃ©er.
   * @param topicId l'ID du thÃ¨me dans lequel crÃ©er le nouveau thÃ¨me.
   * @return un objet clÃ© primaire du nouveau thÃ¨me crÃ©Ã©.
   * @throws ImportExportException en cas d'anomalie lors de la crÃ©ation du noeud.
   */
  protected abstract NodePK addSubTopicToTopic(NodeDetail nodeDetail, int topicId,
      MassiveReport massiveReport) throws ImportExportException;

  /**
   * Ajoute un sous-noeud Ã  un noeud existant Ã  partir d'un rÃ©pertoire du systÃ¨me de fichiers. Le
   * nom de ce rÃ©pertoire reprÃ©sente le noeud Ã  crÃ©er. Utile pour les imports massifs de noeuds et
   * de publications Ã  partir d'une hiÃ©rarchie de dossiers et de fichiers.
   * @param unitReport le rapport d'import unitaire.
   * @param nodeDetail le dÃ©tail du noeud Ã  crÃ©er.
   * @param parentTopicId l'identifiant du noeud parent, ou 0 pour dÃ©signer le noeud racine.
   * @return l'objet qui rÃ©prÃ©sente le dÃ©tail du nouveau noeud crÃ©Ã© ou du noeud existant (en
   * particulier si un noeud de mÃªme ID existe dÃ©jÃ ).
   * @throws ImportExportException en cas d'anomalie lors de la crÃ©ation du noeud.
   */
  public NodeDetail createTopicForUnitImport(UnitReport unitReport, NodeDetail nodeDetail,
      int parentTopicId) throws ImportExportException {

    unitReport.setItemName(nodeDetail.getName());
    NodePK nodePk = addSubTopicToTopic(nodeDetail, parentTopicId, unitReport);

    try {
      return getNodeBm().getDetail(nodePk);

    } catch (RemoteException ex) {
      unitReport.setError(UnitReport.ERROR_NOT_EXISTS_TOPIC);
      SilverTrace.error("importExport", "GEDImportExport.createTopicForUnitImport()",
          "root.EX_NO_MESSAGE", ex);
      throw new ImportExportException("GEDImportExport.createTopicForUnitImport",
          "importExport.EX_NODE_CREATE", ex);
    }
  }

  /**
   * MÃ©thode de crÃ©ation d'une publication dans le cas d'une importation unitaire avec mÃ©ta-donnÃ©es
   * dÃ©finies dans le fichier xml d'importation.
   * @param unitReport
   * @param userDetail
   * @param fileForPubliMetaData
   * @param pubDet_map
   * @param targetComponentId
   * @param listAtt_Detail
   * @param listNode_Type
   * @throws ImportExportException
   * @return le publicationDetail de la publication crÃ©Ã©e
   */
  public PublicationDetail createPublicationForUnitImport(UnitReport unitReport,
      UserDetail userDetail, PublicationDetail pubDetail, List listNode_Type) {

    unitReport.setItemName(pubDetail.getName());
    // On crÃ©e la publication
    pubDetail = processPublicationDetail(unitReport, userDetail, pubDetail, listNode_Type);

    return pubDetail;
  }

  /**
   * MÃ©thode de crÃ©ation d'une publication dans le cas d'une importation massive
   * @param unitReport
   * @param userDetail
   * @param fileForPubliMetaData
   * @param targetComponentId
   * @param nodeId
   * @throws ImportExportException
   * @return le publicationDetail de la publication crÃ©Ã©e
   */
  public PublicationDetail createPublicationForMassiveImport(UnitReport unitReport,
      UserDetail userDetail, PublicationDetail pubDetail, int nodeId) throws ImportExportException {

    unitReport.setItemName(pubDetail.getName());

    NodePositionType nodePosType = new NodePositionType();
    nodePosType.setId(nodeId);
    ArrayList listNode_Type = new ArrayList();
    listNode_Type.add(nodePosType);

    // On crÃ©e la publication
    pubDetail = processPublicationDetail(unitReport, userDetail, pubDetail, listNode_Type);
    return pubDetail;
  }

  /**
   * Ajoute un sous-noeud Ã  un noeud existant Ã  partir d'un rÃ©pertoire du systÃ¨me de fichiers. Le
   * nom de ce rÃ©pertoire reprÃ©sente le noeud Ã  crÃ©er. Utile pour les imports massifs de noeuds et
   * de publications Ã  partir d'une hiÃ©rarchie de dossiers et de fichiers.
   * @param directory le rÃ©pertoire dont le nom reprÃ©sente le nouveau noeud.
   * @param topicId l'identifiant du noeud parent.
   * @param massiveReport le rapprt d'import.
   * @return un objet qui rÃ©prÃ©sente le nouveau noeud crÃ©Ã©.
   * @throws ImportExportException en cas d'anomalie lors de la crÃ©ation du noeud.
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
   * MÃ©thode rÃ©cupÃ©rant le silverObjectId d'un objet d'id id
   * @param id - id de la publication
   * @return le silverObjectId de l'objet d'id id
   * @throws ImportExportException
   */
  public abstract int getSilverObjectId(String id) throws Exception;

  /**
   * MÃ©thode de rÃ¨cupÃ©ration de la publication complÃ¨te utilisÃ©e pour l'exportation
   * @param pubId
   * @param componentId
   * @return
   * @throws ImportExportException
   */
  public PublicationType getPublicationCompleteById(String pubId, String componentId)
      throws ImportExportException {
    PublicationType publicationType = new PublicationType();
    PublicationDetail publicationDetail = null;
    try {
      CompletePublication pubComplete = getCompletePublication(new PublicationPK(pubId,
          getCurrentComponentId()));

      // RÃ©cupÃ©ration de l'objet PublicationDetail
      publicationDetail = pubComplete.getPublicationDetail();

      InfoDetail infoDetail = pubComplete.getInfoDetail();
      PublicationContentType pubContent = null;
      if (infoDetail != null
          &&
          (infoDetail.getInfoImageList().size() != 0 || infoDetail.getInfoTextList().size() != 0)) {
        // la publication a un contenu de type DBModel
        pubContent = new PublicationContentType();
        DBModelContentType dbModel = new DBModelContentType();
        pubContent.setDBModelContentType(dbModel);

        // RÃ©cupÃ©ration des textes
        Collection listInfoText = infoDetail.getInfoTextList();
        if ((listInfoText != null) && (listInfoText.size() != 0)) {
          ArrayList listTextParts = new ArrayList();
          Iterator itListInfoText = listInfoText.iterator();
          while (itListInfoText.hasNext()) {
            InfoTextDetail infoText = (InfoTextDetail) itListInfoText.next();
            listTextParts.add(infoText.getContent());
          }
          dbModel.setListTextParts(listTextParts);
        }

        // RÃ©cupÃ©ration des images
        Collection listInfoImage = infoDetail.getInfoImageList();
        if ((listInfoImage != null) && (listInfoImage.size() != 0)) {
          ArrayList listImageParts = new ArrayList();
          Iterator itListInfoImage = listInfoImage.iterator();
          while (itListInfoImage.hasNext()) {
            InfoImageDetail imageDetail = (InfoImageDetail) itListInfoImage.next();
            String path = FileRepositoryManager.getAbsolutePath(componentId) + File.separator
                + "images";
            listImageParts.add(path + File.separator + imageDetail.getPhysicalName());
          }
          dbModel.setListImageParts(listImageParts);
        }

        // RÃ©cupÃ©ration du model
        ModelDetail modelDetail = pubComplete.getModelDetail();
        dbModel.setId(Integer.parseInt(modelDetail.getId()));
      } else if (!isInteger(publicationDetail.getInfoId())) {
        // la publication a un contenu de type XMLTemplate (formTemplate)
        pubContent = new PublicationContentType();
        List xmlFields = getFormTemplateBm().getXMLFieldsForExport(
            publicationDetail.getPK().getInstanceId() + ":" + publicationDetail.getInfoId(), pubId);
        SilverTrace.info("importExport", "GEDImportExport.getPublicationCompleteById()",
            "root.MSG_GEN_PARAM_VALUE", "# of xmlField = " + xmlFields.size());
        XMLModelContentType xmlModel = new XMLModelContentType(publicationDetail.getInfoId());
        xmlModel.setFields((ArrayList) xmlFields);
        pubContent.setXMLModelContentType(xmlModel);
      } else if (WysiwygController.haveGotWysiwyg("useless", componentId, pubId)) {
        pubContent = new PublicationContentType();
        WysiwygContentType wysiwygContentType = new WysiwygContentType();
        String wysiwygFileName = WysiwygController.getWysiwygFileName(pubId);
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

  private static boolean isInteger(String id) {
    try {
      Integer.parseInt(id);
      return true;
    } catch (NumberFormatException e) {
      return false;
    }
  }

  /**
   * MÃ©thode renvoant la liste des topics de la publication sous forme de NodePK
   * @param pubId - id de la publication dont on veut les topics
   * @return - liste des nodesPk de la publication
   * @throws ImportExportException
   */
  public List getAllTopicsOfPublication(String pubId, String componentId)
      throws ImportExportException {

    Collection listNodePk = null;
    PublicationPK pubPK = new PublicationPK(pubId, "Useless", componentId);

    try {
      listNodePk = getPublicationBm().getAllFatherPK(pubPK);
    } catch (RemoteException ex) {
      throw new ImportExportException("", "", ex);// TODO: completer!!
    }
    return new ArrayList(listNodePk);

  }

  public ModelDetail getModelDetail(int idModelDetail) throws ImportExportException {

    ModelDetail modelDetail = null;
    try {
      modelDetail = getPublicationBm().getModelDetail(new ModelPK(Integer.toString(idModelDetail)));
    } catch (RemoteException ex) {
      throw new ImportExportException("", "", ex);// TODO: completer!!
    }
    return modelDetail;
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
   * @param pubDetail
   * @return pubDetail
   */
  protected abstract PublicationDetail createPublication(PublicationDetail pubDetail)
      throws Exception;

  public Collection getPublicationCoordinates(String pubId, String componentId)
      throws ImportExportException {
    try {
      return getPublicationBm().getCoordinates(pubId, componentId);
    } catch (Exception e) {
      throw new ImportExportException("GEDImportExport.getPublicationCoordinates(String)",
          "importExport.EX_GET_SILVERPEASOBJECTID", "pubId = " + pubId, e);
    }
  }

  private void processThumbnail(String filePath, PublicationDetail pubDetail) {
    // PrÃ©paration des paramÃ¨tres du fichier Ã  creer
    String logicalName = filePath.substring(filePath.lastIndexOf(File.separator) + 1);
    String type = FileRepositoryManager.getFileExtension(logicalName);
    String mimeType = AttachmentController.getMimeType(logicalName);
    String physicalName = new Long(new Date().getTime()).toString() + "." + type;

    if (type.equalsIgnoreCase("gif") || type.equalsIgnoreCase("jpg")
        || type.equalsIgnoreCase("jpeg") || type.equalsIgnoreCase("png")) {
      String dest = FileRepositoryManager.getAbsolutePath(pubDetail.getPK().getInstanceId())
          + "images" + File.separator + physicalName;

      try {
        FileRepositoryManager.copyFile(filePath, dest);
      } catch (Exception e) {
        SilverTrace.error("importExport", "GEDImportExport.processThumbnail()",
            "root.MSG_GEN_PARAM_VALUE", "filePath = " + filePath, e);
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
