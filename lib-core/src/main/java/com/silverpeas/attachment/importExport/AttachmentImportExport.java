/**
 * Copyright (C) 2000 - 2011 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU Affero General Public License as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of the GPL, you may
 * redistribute this Program in connection with Free/Libre Open Source Software ("FLOSS")
 * applications as described in Silverpeas's FLOSS exception. You should have received a copy of the
 * text describing the FLOSS exception, and it is also available here:
 * "http://repository.silverpeas.com/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License along with this program.
 * If not, see <http://www.gnu.org/licenses/>.
 */
package com.silverpeas.attachment.importExport;

import com.silverpeas.form.AbstractForm;
import com.silverpeas.form.importExport.FormTemplateImportExport;
import com.silverpeas.form.importExport.XMLModelContentType;
import com.silverpeas.util.FileUtil;
import com.silverpeas.util.ForeignPK;
import com.silverpeas.util.StringUtil;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.util.FileRepositoryManager;
import com.stratelia.webactiv.util.FileServerUtils;
import com.stratelia.webactiv.util.WAPrimaryKey;
import com.stratelia.webactiv.util.attachment.control.AttachmentController;
import com.stratelia.webactiv.util.attachment.ejb.AttachmentPK;
import com.stratelia.webactiv.util.attachment.model.AttachmentDetail;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.silverpeas.attachment.AttachmentServiceFactory;
import org.silverpeas.attachment.model.SimpleAttachment;
import org.silverpeas.attachment.model.SimpleDocument;
import org.silverpeas.attachment.model.SimpleDocumentPK;

/**
 * Classe de gestion des attachments dans le moteur d'importExport de silverpeas.
 *
 * @author sdevolder
 */
public class AttachmentImportExport {

  // Methodes
  /**
   * Methode utilisee par l'import massive du moteur d'importExport de silverpeaseffectuant la copie
   * de fichier ainsi que sa liaison avec une publication cible.
   *
   * @param pubId - publication dans laquelle creer l'attachement
   * @param componentId - id du composant contenant la publication (necessaire pour determiner le
   * chemin physique du fichier importe)
   * @param attachmentDetail - objet contenant les details necessaires a la creation du fichier
   * importe et a sa liaison avec la publication
   * @param indexIt
   */
  public void importAttachment(String pubId, String componentId, AttachmentDetail attachmentDetail,
      InputStream file, boolean indexIt) {
    importAttachment(pubId, componentId, attachmentDetail, file, indexIt, true);
  }

  public void importAttachment(String pubId, String componentId,
      AttachmentDetail attachmentDetail, InputStream file, boolean indexIt,
      boolean updateLogicalName) {
    this.copyFile(componentId, attachmentDetail, updateLogicalName);
    if (attachmentDetail.getSize() > 0) {
      this.addAttachmentToPublication(pubId, componentId, attachmentDetail, file, indexIt);
    }
  }

  /* TODO : à reprendre pour feature_82
   * 
   * public AttachmentDetail importWysiwygAttachment(String pubId,
      String componentId, AttachmentDetail attachmentDetail, String context) {
    AttachmentDetail a_detail = null;
    this.copyFileWysiwyg(componentId, attachmentDetail, context);
    if (attachmentDetail.getSize() > 0) {
      a_detail = this.addAttachmentToPublication(pubId, componentId, attachmentDetail, context,
          false);
    }
    return a_detail;
  }*/
  
  @Deprecated
  public AttachmentDetail importWysiwygAttachment(String pubId,
      String componentId, AttachmentDetail attachmentDetail, String context) {
    return null;
  }

  public List<AttachmentDetail> importAttachments(String pubId, String componentId,
      List<AttachmentDetail> attachments, String userId) {
    return importAttachments(pubId, componentId, attachments, userId, false);
  }

  public List<AttachmentDetail> importAttachments(String pubId, String componentId,
      List<AttachmentDetail> attachments, String userId, boolean indexIt) {
    List<AttachmentDetail> copiedAttachments = copyFiles(componentId, attachments);
    FormTemplateImportExport xmlIE = null;
    for (AttachmentDetail attDetail : copiedAttachments) {
      attDetail.setAuthor(userId);
      XMLModelContentType xmlContent = attDetail.getXMLModelContentType();
      if (xmlContent != null) {
        attDetail.setXmlForm(xmlContent.getName());
      }
      InputStream input = null;
      // Store xml content
      try {
        input = new FileInputStream(attDetail.getAttachmentPath(null));
        this.addAttachmentToPublication(pubId, componentId, attDetail, input, indexIt);
        if (xmlContent != null) {
          if (xmlIE == null) {
            xmlIE = new FormTemplateImportExport();
          }
          ForeignPK pk = new ForeignPK(attDetail.getPK().getId(), attDetail.getPK().getInstanceId());
          xmlIE.importXMLModelContentType(pk, "Attachment", xmlContent,
              attDetail.getAuthor());
        }
      } catch (Exception e) {
        SilverTrace.error("attachment",
            "AttachmentImportExport.importAttachments()",
            "root.MSG_GEN_PARAM_VALUE", e);
      } finally {
        IOUtils.closeQuietly(input);
      }

      if (attDetail.isRemoveAfterImport()) {
        boolean removed = FileUtils.deleteQuietly(new File(attDetail.getOriginalPath()));
        if (!removed) {
          SilverTrace.error("attachment",
              "AttachmentImportExport.importAttachments()",
              "root.MSG_GEN_PARAM_VALUE", "Can't remove file " + attDetail.getOriginalPath());
        }
      }
    }
    return copiedAttachments;
  }

  private AttachmentDetail copyFile(String componentId, AttachmentDetail a_Detail,
      boolean updateLogicalName) {
    String path = getPath(componentId);
    return copyFile(componentId, a_Detail, path, updateLogicalName);
  }

  private AttachmentDetail copyFileWysiwyg(String componentId, AttachmentDetail a_Detail,
      String context) {
    String path = getPathWysiwyg(componentId, context);
    a_Detail.setContext(context);
    return copyFile(componentId, a_Detail, path);
  }

  public List<AttachmentDetail> copyFiles(String componentId, List<AttachmentDetail> attachments) {
    return copyFiles(componentId, attachments, getPath(componentId));
  }

  public List<AttachmentDetail> copyFiles(String componentId, List<AttachmentDetail> attachments,
      String path) {
    List<AttachmentDetail> copiedAttachments = new ArrayList<AttachmentDetail>();
    for (AttachmentDetail attDetail : attachments) {
      this.copyFile(componentId, attDetail, path);
      if (attDetail.getSize() != 0) {
        copiedAttachments.add(attDetail);
      }
    }
    return copiedAttachments;
  }

  /**
   * Methode de copie de fichier utilisee par la methode
   * importAttachement(String,String,AttachmentDetail)
   *
   * @param componentId - id du composant contenant la publication e laquelle est destine
   * l'attachement
   * @param a_Detail - objet contenant les informations sur le fichier e copier
   * @param path - chemin oe doit etre copie le fichier
   * @return renvoie l'objet des informations sur le fichier e copier complete par les nouvelles
   * donnees issues de la copie
   */
  public AttachmentDetail copyFile(String componentId, AttachmentDetail a_Detail, String path) {
    return copyFile(componentId, a_Detail, path, true);
  }

  public AttachmentDetail copyFile(String componentId, AttachmentDetail a_Detail, String path,
      boolean updateLogicalName) {

    String fileToUpload = a_Detail.getPhysicalName();

    // Get parameters of file to create
    String logicalName = fileToUpload.substring(fileToUpload.lastIndexOf(File.separator) + 1);
    String type = logicalName.substring(logicalName.lastIndexOf('.') + 1, logicalName.length());
    String mimeType = FileUtil.getMimeType(logicalName);
    String physicalName = System.currentTimeMillis() + "." + type;
    File fileToCreate = new File(path + physicalName);
    while (fileToCreate.exists()) {
      SilverTrace.info("attachment", "AttachmentImportExport.copyFile()",
          "root.MSG_GEN_PARAM_VALUE",
          "fileToCreate already exists=" + fileToCreate.getAbsolutePath());

      // To prevent overwriting
      physicalName = String.valueOf(System.currentTimeMillis()) + '.' + type;
      fileToCreate = new File(path + physicalName);
    }
    SilverTrace.info("attachment", "AttachmentImportExport.copyFile()",
        "root.MSG_GEN_PARAM_VALUE", "fileName=" + logicalName);

    long size = 0;
    try {
      FileUtils.copyFile(new File(fileToUpload), fileToCreate);
      size = fileToCreate.length();
    } catch (IOException e) {
      SilverTrace.error("attachment", "AttachmentImportExport.copyFile()",
          "attachment.EX_FILE_COPY_ERROR", e);
    }

    // Complements sur les attachmentDetail
    a_Detail.setSize(size);
    a_Detail.setType(mimeType);
    a_Detail.setPhysicalName(physicalName);
    if (updateLogicalName) {
      a_Detail.setLogicalName(logicalName);
    }
    a_Detail.setOriginalPath(fileToUpload);

    AttachmentPK pk = new AttachmentPK("unknown", "useless", componentId);
    a_Detail.setPK(pk);
    return a_Detail;
  }

  /**
   * Methode utilisee par la methode importAttachement(String,String,AttachmentDetail) pour creer un
   * attachement sur la publication creee dans la methode citee.
   *
   * @param pubId - id de la publication dans laquelle creer l'attachment
   * @param componentId - id du composant contenant la publication
   * @param a_Detail - obejt contenant les informations necessaire e la creation de l'attachment
   * @return AttachmentDetail cree
   */
  private SimpleDocument addAttachmentToPublication(String pubId, String componentId,
      AttachmentDetail a_Detail, InputStream input, boolean indexIt) {

    int incrementSuffixe = 0;
    SimpleDocumentPK attachmentPk = new SimpleDocumentPK(null, componentId);
    AttachmentPK foreignKey = new AttachmentPK(pubId, componentId);
    List<SimpleDocument> attachments = AttachmentServiceFactory.getAttachmentService().
        searchAttachmentsByExternalObject(foreignKey, null);
    int i = 0;

    String logicalName = a_Detail.getLogicalName();
    String userId = a_Detail.getAuthor();
    String updateRule = a_Detail.getImportUpdateRule();
    if (!StringUtil.isDefined(updateRule) || "null".equalsIgnoreCase(updateRule)) {
      updateRule = AttachmentDetail.IMPORT_UPDATE_RULE_ADD;
    }

    SilverTrace.info("attachment", "AttachmentImportExport.addAttachmentToPublication()",
        "root.MSG_GEN_PARAM_VALUE", "updateRule=" + updateRule);

    // Verification s'il existe un attachment de meme nom, si oui, ajout
    // d'un suffixe au nouveau fichier
    SimpleDocument ad_toCreate;
    while (i < attachments.size()) {
      ad_toCreate = attachments.get(i);
      if (ad_toCreate.getFilename().equals(logicalName)) {
        if ((ad_toCreate.getSize() != a_Detail.getSize())
            && AttachmentDetail.IMPORT_UPDATE_RULE_ADD.equalsIgnoreCase(updateRule)) {
          logicalName = a_Detail.getLogicalName();
          int extPosition = logicalName.lastIndexOf('.');
          if (extPosition != -1) {
            logicalName = logicalName.substring(0, extPosition) + "_" + (++incrementSuffixe)
                + logicalName.substring(extPosition, logicalName.length());
          } else {
            logicalName += "_" + (++incrementSuffixe);
          }
          // On reprend la boucle au debut pour verifier que le nom
          // genere n est pas lui meme un autre nom d'attachment de la publication
          i = 0;
        } else {// on efface l'ancien fichier joint et on stoppe la boucle
          AttachmentServiceFactory.getAttachmentService().deleteAttachment(ad_toCreate);
          break;
        }
      } else {
        i++;
      }
    }
    a_Detail.setLogicalName(logicalName);

    // On instancie l'objet attachment e creer
    ad_toCreate = new SimpleDocument(attachmentPk, pubId, -1, false, new SimpleAttachment(a_Detail.
        getLogicalName(),
        null, a_Detail.getTitle(), a_Detail.getInfo(), a_Detail.getSize(), a_Detail.getType(),
        userId, new Date(), a_Detail.getXmlForm()));
    AttachmentServiceFactory.getAttachmentService().createAttachment(ad_toCreate, input, indexIt);

    return ad_toCreate;
  }

  /**
   * Methode de recuperation des attachements et de copie des fichiers dans le dossier d'exportation
   *
   * @param pk - PrimaryKey de l'obijet dont on veut les attachments?
   * @param exportPath - Repertoire dans lequel copier les fichiers
   * @param relativeExportPath chemin relatif du fichier copie
   * @param extensionFilter
   * @return une liste des attachmentDetail trouves
   */
  public List<AttachmentDetail> getAttachments(WAPrimaryKey pk, String exportPath,
      String relativeExportPath, String extensionFilter) {

    // Recuperation des attachments
    Collection<AttachmentDetail> listAttachment = AttachmentController.searchAttachmentByCustomerPK(
        pk);
    List<AttachmentDetail> listToReturn = new ArrayList<AttachmentDetail>();
    if (listAttachment != null && listAttachment.isEmpty()) {
      listAttachment = null;
    }
    if (listAttachment != null) {
      // Pour chaque attachment trouve, on copie le fichier dans le dossier
      // d'exportation
      for (AttachmentDetail attDetail : listAttachment) {
        if (!attDetail.getContext().equals(AbstractForm.CONTEXT_FORM_FILE)) {
          // ce n est pas un fichier joint mais un fichier appartenant surement
          // au wysiwyg si le context
          // est different de images et ce quelque soit le type du fichier
          continue;// on ne copie pas le fichier
        }

        if (extensionFilter == null) {
          try {
            copyAttachment(attDetail, pk, exportPath);

            // Le nom physique correspond maintenant au fichier copie
            attDetail.setPhysicalName(relativeExportPath + File.separator
                + FileServerUtils.replaceAccentChars(attDetail.getLogicalName()));

          } catch (IOException ex) {
            // TODO: gerer ou ne pas gerer telle est la question
            ex.printStackTrace();
          }

          listToReturn.add(attDetail);

        } else if (attDetail.getExtension().equalsIgnoreCase(extensionFilter)) {
          try {
            copyAttachment(attDetail, pk, exportPath);
            // Le nom physique correspond maintenant au fichier copi
            attDetail.setLogicalName(FileServerUtils.replaceAccentChars(attDetail.getLogicalName()));

          } catch (Exception ex) {
            // TODO: gerer ou ne pas gerer telle est la question
            ex.printStackTrace();
          }

          listToReturn.add(attDetail);
        }
      }
    }

    return listToReturn;
  }

  private void copyAttachment(AttachmentDetail attDetail, WAPrimaryKey pk, String exportPath) throws
      FileNotFoundException, IOException {
    String fichierJoint = AttachmentController.createPath(pk.getInstanceId(),
        attDetail.getContext()) + File.separator + attDetail.getPhysicalName();

    String fichierJointExport = exportPath + File.separator + FileServerUtils.replaceAccentChars(
        attDetail.getLogicalName());
    FileRepositoryManager.copyFile(fichierJoint, fichierJointExport);
  }

  /**
   * Methode recuperant le chemin d'acces au dossier de stockage des fichiers importes dans un
   * composant.
   *
   * @param componentId - id du composant dont on veut recuperer le chemin de stockage de ses
   * fichiers importes
   * @return le chemin recherche
   */
  private String getPath(String componentId) {
    String path = AttachmentController.createPath(componentId, AbstractForm.CONTEXT_FORM_FILE);
    SilverTrace.info("attachment", "AttachmentImportExport.getPath()", "root.MSG_GEN_PARAM_VALUE",
        "path=" + path);
    return path;
  }

  private String getPathWysiwyg(String componentId, String context) {
    String path = AttachmentController.createPath(componentId, context);
    SilverTrace.info("attachment", "AttachmentImportExport.getPath()", "root.MSG_GEN_PARAM_VALUE",
        "path=" + path);
    return path;
  }
}