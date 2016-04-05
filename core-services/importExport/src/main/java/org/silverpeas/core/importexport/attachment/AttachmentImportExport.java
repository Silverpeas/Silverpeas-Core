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
package org.silverpeas.core.importexport.attachment;

import org.silverpeas.core.importexport.form.FormTemplateImportExport;
import org.silverpeas.core.importexport.form.XMLModelContentType;
import org.silverpeas.core.silvertrace.SilverTrace;
import org.silverpeas.core.admin.user.model.UserDetail;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.silverpeas.core.contribution.attachment.AttachmentServiceProvider;
import org.silverpeas.core.contribution.attachment.model.DocumentType;
import org.silverpeas.core.contribution.attachment.model.SimpleAttachment;
import org.silverpeas.core.contribution.attachment.model.SimpleDocument;
import org.silverpeas.core.contribution.attachment.model.SimpleDocumentPK;
import org.silverpeas.core.util.file.FileRepositoryManager;
import org.silverpeas.core.util.file.FileServerUtils;
import org.silverpeas.core.util.file.FileUtil;
import org.silverpeas.core.ForeignPK;
import org.silverpeas.core.util.ResourceLocator;
import org.silverpeas.core.util.SettingBundle;
import org.silverpeas.core.util.StringUtil;
import org.silverpeas.core.WAPrimaryKey;
import org.silverpeas.core.util.error.SilverpeasTransverseErrorUtil;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

/**
 * A non-versioned attachment importer/exporter. Import and export are performed by a user and then
 * they are done according to its privileges.
 */
public class AttachmentImportExport {

  private UserDetail user;
  private final SettingBundle settings =
      ResourceLocator.getSettingBundle("org.silverpeas.importExport.settings.importSettings");

  public AttachmentImportExport(final UserDetail user) {
    this.user = user;
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
      List<AttachmentDetail> attachments, boolean indexIt) throws
      FileNotFoundException {
    FormTemplateImportExport xmlIE = null;
    for (AttachmentDetail attDetail : attachments) {
      //TODO check user id
      attDetail.setAuthor(this.user.getId());
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
        SilverpeasTransverseErrorUtil.throwTransverseErrorIfAny(e, attDetail.getLanguage());
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
    List<SimpleDocument> existingAttachments = AttachmentServiceProvider.getAttachmentService().
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



    // Verification s'il existe un attachment de meme nom, si oui, ajout
    // d'un suffixe au nouveau fichier
    logicalName = computeUniqueName(attachment, 0, existingAttachments, logicalName,
        updateRule);
    attachment.setLogicalName(logicalName);

    Date creationDate = attachment.getCreationDate();
    if (creationDate == null) {
      creationDate = new Date();
    }
    SimpleDocument ad_toCreate = new SimpleDocument(attachmentPk, pubId, -1, false,
        new SimpleAttachment(attachment.getLogicalName(), attachment.getLanguage(), attachment.
        getTitle(), attachment.getInfo(), attachment.getSize(),
        FileUtil.getMimeType(attachment.getPhysicalName()), userId, creationDate, attachment.
        getXmlForm()));
    return AttachmentServiceProvider.getAttachmentService().createAttachment(ad_toCreate, input,
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
          AttachmentServiceProvider.getAttachmentService().deleteAttachment(ad_toCreate);
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
    Collection<SimpleDocument> listAttachment = AttachmentServiceProvider.getAttachmentService()
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

        if (!attachment.isDownloadAllowedForRolesFrom(user)) {
          // The user is not allowed to download this document. No error is thrown but the
          // document is not exported.
          continue;
        }

        if (extensionFilter == null || FileRepositoryManager.getFileExtension(attachment.
            getFilename()).equalsIgnoreCase(extensionFilter)) {
          copyAttachment(attachment, exportPath);
          String physicalName = relativeExportPath + File.separator + FileServerUtils.
              replaceAccentChars(attachment.getFilename());
          AttachmentDetail attachDetail = new AttachmentDetail(new AttachmentPK(attachment.getId(),
              attachment.getInstanceId()), physicalName, attachment.getFilename(), attachment.
              getDescription(), attachment.getContentType(),
              attachment.getSize(), attachment.getDocumentType().toString(), attachment.
              getCreated(), new ForeignPK(attachment.getForeignId(), attachment.getInstanceId()));
          listToReturn.add(attachDetail);
        }
      }
    }

    return listToReturn;
  }

  private void copyAttachment(SimpleDocument attDetail, String exportPath) {
    String fichierJointExport = exportPath + File.separatorChar + FileServerUtils.
        replaceAccentChars(attDetail.getFilename());
    AttachmentServiceProvider.getAttachmentService().getBinaryContent(new File(fichierJointExport),
        attDetail.getPk(), null);
  }

  public InputStream getAttachmentContent(AttachmentDetail attachment) throws FileNotFoundException {
    return new FileInputStream(getAttachmentFile(attachment));
  }

  public File getAttachmentFile(AttachmentDetail attachment) throws FileNotFoundException {
    File file = new File(FileUtil.convertPathToServerOS(attachment.getAttachmentPath()));
    if (file == null || !file.exists() || !file.isFile()) {
      String baseDir = settings.getString("importRepository", "");
      file = new File(FileUtil.convertPathToServerOS(baseDir + File.separatorChar + attachment.
          getPhysicalName()));
    }
    attachment.setSize(file.length());
    attachment.setType(FileUtil.getMimeType(file.getName()));
    if (!StringUtil.isDefined(attachment.getLogicalName())) {
      attachment.setLogicalName(file.getName());
    }
    return file;
  }
}
