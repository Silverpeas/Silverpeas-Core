/**
 * Copyright (C) 2000 - 2012 Silverpeas
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
package org.silverpeas.importExport.attachment;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.silverpeas.attachment.AttachmentServiceFactory;
import org.silverpeas.attachment.model.DocumentType;
import org.silverpeas.attachment.model.SimpleAttachment;
import org.silverpeas.attachment.model.SimpleDocument;
import org.silverpeas.attachment.model.SimpleDocumentPK;

import com.silverpeas.form.importExport.FormTemplateImportExport;
import com.silverpeas.form.importExport.XMLModelContentType;
import com.silverpeas.util.FileUtil;
import com.silverpeas.util.ForeignPK;
import com.silverpeas.util.StringUtil;

import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.beans.admin.UserDetail;
import com.stratelia.webactiv.util.FileRepositoryManager;
import com.stratelia.webactiv.util.FileServerUtils;
import com.stratelia.webactiv.util.ResourceLocator;
import com.stratelia.webactiv.util.WAPrimaryKey;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

/**
 * Classe de gestion des attachments dans le moteur d'importExport de silverpeas.
 */
public class AttachmentImportExport {
  private UserDetail user;
  private final ResourceLocator resources = new ResourceLocator(
      "org.silverpeas.importExport.settings.importSettings", "");

  public AttachmentImportExport(UserDetail user) {
    this.user = user;
  }

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
  public void importAttachment(String pubId, String componentId,
      AttachmentDetail attachmentDetail, InputStream file, boolean indexIt,
      boolean updateLogicalName) {
    if (attachmentDetail.getSize() > 0) {
      addAttachmentToPublication(pubId, componentId, attachmentDetail, file, indexIt);
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
      List<AttachmentDetail> attachments, String userId, boolean indexIt) throws
      FileNotFoundException {
    FormTemplateImportExport xmlIE = null;
    for (AttachmentDetail attDetail : attachments) {
      //TODO check user id
      attDetail.setAuthor(userId);
      attDetail.setInstanceId(componentId);
      XMLModelContentType xmlContent = attDetail.getXMLModelContentType();
      if (xmlContent != null) {
        attDetail.setXmlForm(xmlContent.getName());
      }
      InputStream input = null;
      // Store xml content
      try {
        input = getAttachmentContent(attDetail);
        this.addAttachmentToPublication(pubId, componentId, attDetail, input, indexIt);
        if (xmlContent != null) {
          if (xmlIE == null) {
            xmlIE = new FormTemplateImportExport();
          }
          ForeignPK pk = new ForeignPK(attDetail.getPK().getId(), attDetail.getPK().getInstanceId());
          xmlIE.importXMLModelContentType(pk, "Attachment", xmlContent, attDetail.getAuthor());
        }
      } catch (Exception e) {
        SilverTrace.error("attachment", "AttachmentImportExport.importAttachments()",
            "root.MSG_GEN_PARAM_VALUE", e);
      } finally {
        IOUtils.closeQuietly(input);
      }

      if (attDetail.isRemoveAfterImport()) {
        boolean removed = FileUtils.deleteQuietly(getAttachmentFile(attDetail));
        if (!removed) {
          SilverTrace.error("attachment", "AttachmentImportExport.importAttachments()",
              "root.MSG_GEN_PARAM_VALUE", "Can't remove file " + getAttachmentFile(attDetail));
        }
      }
    }
    return attachments;
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
          "root.MSG_GEN_PARAM_VALUE", "fileToCreate already exists=" + fileToCreate.getAbsolutePath());

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
   *
   * @param pubId - id de la publication dans laquelle creer l'attachment
   * @param componentId - id du composant contenant la publication
   * @param attachment
   * @return AttachmentDetail cree
   */
  private SimpleDocument addAttachmentToPublication(String pubId, String componentId,
      AttachmentDetail attachment, InputStream input, boolean indexIt) {
    SimpleDocumentPK attachmentPk = new SimpleDocumentPK(null, componentId);
    ForeignPK foreignKey = new ForeignPK(pubId, componentId);
    List<SimpleDocument> existingAttachments = AttachmentServiceFactory.getAttachmentService().
        listDocumentsByForeignKeyAndType(foreignKey, DocumentType.attachment,
        attachment.getLanguage());

    String logicalName = attachment.getLogicalName();
    if (!StringUtil.isDefined(logicalName)) {
      logicalName = FileUtil.getFilename(attachment.getPhysicalName());
    }
    String userId = attachment.getAuthor();
    String updateRule = attachment.getImportUpdateRule();
    if (!StringUtil.isDefined(updateRule) || "null".equalsIgnoreCase(updateRule)) {
      updateRule = AttachmentDetail.IMPORT_UPDATE_RULE_ADD;
    }

    SilverTrace.info("attachment", "AttachmentImportExport.addAttachmentToPublication()",
        "root.MSG_GEN_PARAM_VALUE", "updateRule=" + updateRule);

    // Verification s'il existe un attachment de meme nom, si oui, ajout
    // d'un suffixe au nouveau fichier
    logicalName = computeUniqueName(attachment, 0, existingAttachments, logicalName,
        updateRule);
    attachment.setLogicalName(logicalName);

    // On instancie l'objet attachment e creer
    SimpleDocument ad_toCreate = new SimpleDocument(attachmentPk, pubId, -1, false,
        new SimpleAttachment(attachment.getLogicalName(), attachment.getLanguage(), attachment.
        getTitle(), attachment.getInfo(), attachment.getSize(),
        FileUtil.getMimeType(attachment.getPhysicalName()), userId, new Date(), attachment.
        getXmlForm()));
    return AttachmentServiceFactory.getAttachmentService().createAttachment(ad_toCreate, input,
        indexIt);
  }

  private String computeUniqueName(AttachmentDetail attachment, int increment,
      List<SimpleDocument> existingAttachments,
      String logicalName, String updateRule) {
    String uniqueName = logicalName;
    int incrementSuffixe = increment;
    for (SimpleDocument ad_toCreate : existingAttachments) {
      if (ad_toCreate.getFilename().equals(uniqueName)) {
        if ((ad_toCreate.getSize() != attachment.getSize())
            && AttachmentDetail.IMPORT_UPDATE_RULE_ADD.equalsIgnoreCase(updateRule)) {
          uniqueName = attachment.getLogicalName();
          int extPosition = logicalName.lastIndexOf('.');
          if (extPosition != -1) {
            uniqueName = uniqueName.substring(0, extPosition) + '_' + (++incrementSuffixe)
                + uniqueName.substring(extPosition, uniqueName.length());
          } else {
            uniqueName += '_' + (++incrementSuffixe);
          }
          // On reprend la boucle au debut pour verifier que le nom
          // genere n est pas lui meme un autre nom d'attachment de la publication
          return computeUniqueName(attachment, incrementSuffixe, existingAttachments, uniqueName,
              updateRule);
        } else {// on efface l'ancien fichier joint et on stoppe la boucle
          AttachmentServiceFactory.getAttachmentService().deleteAttachment(ad_toCreate);
          return uniqueName;
        }
      }
    }
    return logicalName;
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
    Collection<SimpleDocument> listAttachment = AttachmentServiceFactory.getAttachmentService()
        .listDocumentsByForeignKey(pk, null);
    List<AttachmentDetail> listToReturn = new ArrayList<AttachmentDetail>(listAttachment.size());
    if (!listAttachment.isEmpty()) {
      // Pour chaque attachment trouve, on copie le fichier dans le dossier
      // d'exportation
      for (SimpleDocument attachment : listAttachment) {
        if (attachment.getDocumentType() != DocumentType.attachment) {
          // ce n est pas un fichier joint mais un fichier appartenant surement
          // au wysiwyg si le context
          // est different de images et ce quelque soit le type du fichier
          continue;// on ne copie pas le fichier
        }

        if (extensionFilter == null || FileRepositoryManager.getFileExtension(attachment.
            getFilename()).equalsIgnoreCase(extensionFilter)) {
          try {
            copyAttachment(attachment, exportPath);
            String physicalName = relativeExportPath + File.separator + FileServerUtils.
                replaceAccentChars(attachment.getFilename());
            AttachmentDetail attachDetail =
                new AttachmentDetail(new AttachmentPK(attachment.getId(),
                attachment.getInstanceId()), physicalName, attachment.getFilename(), attachment.
                getDescription(), attachment.getContentType(),
                attachment.getSize(), attachment.getDocumentType().toString(), attachment.
                getCreated(), new ForeignPK(attachment.getForeignId(), attachment.getInstanceId()));
            listToReturn.add(attachDetail);
          } catch (IOException ex) {
            // TODO: gerer ou ne pas gerer telle est la question
            ex.printStackTrace();
          }
        }
      }
    }

    return listToReturn;
  }

  private void copyAttachment(SimpleDocument attDetail, String exportPath) throws IOException {
    String fichierJointExport = exportPath + File.separatorChar + FileServerUtils.
        replaceAccentChars(
        attDetail.getFilename());
    AttachmentServiceFactory.getAttachmentService().getBinaryContent(new File(fichierJointExport),
        attDetail.getPk(), null);
  }

  public InputStream getAttachmentContent(AttachmentDetail attachment) throws FileNotFoundException {
    return new FileInputStream(getAttachmentFile(attachment));
  }

  public File getAttachmentFile(AttachmentDetail attachment) throws FileNotFoundException {
    File file = new File(FileUtil.convertPathToServerOS(attachment.getAttachmentPath()));
    if (file == null || !file.exists() || !file.isFile()) {
      String baseDir = resources.getString("importRepository");
      file = new File(FileUtil.convertPathToServerOS(baseDir + File.separatorChar + attachment.
          getPhysicalName()));
    }
    attachment.setSize(file.length());
    attachment.setType(FileUtil.getMimeType(file.getName()));
    if (!StringUtil.isDefined(attachment.getLogicalName())) {
      attachment.setLogicalName(file.getName());
    }
    attachment.setSize(file.length());
    return file;
  }
}
