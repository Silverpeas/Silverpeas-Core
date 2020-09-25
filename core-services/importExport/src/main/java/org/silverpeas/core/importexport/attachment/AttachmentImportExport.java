/*
 * Copyright (C) 2000 - 2020 Silverpeas
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
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.silverpeas.core.importexport.attachment;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.silverpeas.core.ResourceReference;
import org.silverpeas.core.admin.user.model.UserDetail;
import org.silverpeas.core.contribution.attachment.model.DocumentType;
import org.silverpeas.core.contribution.attachment.model.SimpleAttachment;
import org.silverpeas.core.contribution.attachment.model.SimpleDocument;
import org.silverpeas.core.contribution.attachment.model.SimpleDocumentPK;
import org.silverpeas.core.importexport.form.FormTemplateImportExport;
import org.silverpeas.core.importexport.form.XMLModelContentType;
import org.silverpeas.core.util.ResourceLocator;
import org.silverpeas.core.util.SettingBundle;
import org.silverpeas.core.util.StringUtil;
import org.silverpeas.core.util.error.SilverpeasTransverseErrorUtil;
import org.silverpeas.core.util.file.FileRepositoryManager;
import org.silverpeas.core.util.file.FileServerUtils;
import org.silverpeas.core.util.file.FileUtil;
import org.silverpeas.core.util.logging.SilverLogger;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import static org.silverpeas.core.contribution.attachment.AttachmentServiceProvider.getAttachmentService;
import static org.silverpeas.core.util.StringUtil.isDefined;
import static org.silverpeas.core.util.StringUtil.normalize;

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
      List<AttachmentDetail> attachments, boolean indexIt) throws FileNotFoundException {
    FormTemplateImportExport xmlIE = null;
    for (AttachmentDetail attDetail : attachments) {
      //TODO check user id
      attDetail.setAuthor(this.user.getId());
      attDetail.setInstanceId(componentId);
      XMLModelContentType xmlContent = attDetail.getXMLModelContentType();
      if (xmlContent != null) {
        attDetail.setXmlForm(xmlContent.getName());
      }
      // Store xml content
      try (final InputStream input = getAttachmentContent(attDetail)) {
        this.addAttachmentToPublication(pubId, componentId, attDetail, input, indexIt);
        if (xmlContent != null) {
          if (xmlIE == null) {
            xmlIE = new FormTemplateImportExport();
          }
          ResourceReference pk = new ResourceReference(attDetail.getPK().getId(), attDetail.getPK().getInstanceId());
          xmlIE.importXMLModelContentType(pk, "Attachment", xmlContent, attDetail.getAuthor());
        }
      } catch (Exception e) {
        SilverLogger.getLogger(this).error(e);
        SilverpeasTransverseErrorUtil.throwTransverseErrorIfAny(e, null);
      }

      if (attDetail.isRemoveAfterImport()) {
        boolean removed = FileUtils.deleteQuietly(getAttachmentFile(attDetail));
        if (!removed) {
          SilverLogger.getLogger(this)
              .error("Can''t remove file {0}", getAttachmentFile(attDetail).toString());
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
    ResourceReference foreignKey = new ResourceReference(pubId, componentId);
    List<SimpleDocument> existingAttachments = getAttachmentService().
        listDocumentsByForeignKeyAndType(foreignKey, DocumentType.attachment, null);

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
    final SimpleDocument documentToCreate = new SimpleDocument(attachmentPk, pubId, -1, false,
        new SimpleAttachment(attachment.getLogicalName(), null, attachment.
        getTitle(), attachment.getDescription(), attachment.getSize(),
        FileUtil.getMimeType(attachment.getPhysicalName()), userId, creationDate, attachment.
        getXmlForm()));
    return getAttachmentService()
        .createAttachment(documentToCreate, input, indexIt);
  }

  private String computeUniqueName(AttachmentDetail attachment, int increment,
      List<SimpleDocument> existingAttachments, String logicalName, String updateRule) {
    final String normalizedUniqueName = normalize(logicalName);
    final int incrementSuffix = increment + 1;
    for (SimpleDocument existingDocument : existingAttachments) {
      if (existingDocument.getFilename().equals(normalizedUniqueName)) {
        if (existingDocument.getSize() != attachment.getSize() &&
            AttachmentDetail.IMPORT_UPDATE_RULE_ADD.equalsIgnoreCase(updateRule)) {
          final String baseName = FilenameUtils.getBaseName(attachment.getLogicalName());
          final String extension = FilenameUtils.getExtension(attachment.getLogicalName());
          final String incrementedLogicalName =
              baseName + '_' + incrementSuffix + (isDefined(extension) ? "." + extension : "");
          // Continuing to verify if duplication are not already existing
          return computeUniqueName(attachment, incrementSuffix, existingAttachments,
              incrementedLogicalName, updateRule);
        } else {
          // Deleting old file and stopping the loop
          getAttachmentService().deleteAttachment(existingDocument);
          return normalizedUniqueName;
        }
      }
    }
    return normalizedUniqueName;
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
  public List<AttachmentDetail> getAttachments(ResourceReference pk, String exportPath,
      String relativeExportPath, String extensionFilter) {

    // Recuperation des attachments
    Collection<SimpleDocument> listAttachment = getAttachmentService()
        .listDocumentsByForeignKey(pk, null);
    List<AttachmentDetail> listToReturn = new ArrayList<>(listAttachment.size());
    if (!listAttachment.isEmpty()) {
      // Pour chaque attachment trouve, on copie le fichier dans le dossier
      // d'exportation
      for (SimpleDocument attachment : listAttachment) {
        if (attachment.getDocumentType() != DocumentType.attachment
            || !attachment.isDownloadAllowedForRolesFrom(user)) {

          //It is not a document of type 'attachment' but maybe a wysiwyg file if document type
          //is not 'images'

          //or

          //The user is not allowed to download this document. No error is thrown but the
          //document is not exported.

          //File is not copied
          continue;
        }

        if (extensionFilter == null || FileRepositoryManager.getFileExtension(attachment.
            getFilename()).equalsIgnoreCase(extensionFilter)) {
          try {
            copyAttachment(attachment, exportPath);
            String physicalName = relativeExportPath + File.separator + FileServerUtils.
                replaceAccentChars(attachment.getFilename());
            AttachmentDetail attachDetail = new AttachmentDetail(attachment, physicalName);
            listToReturn.add(attachDetail);
          } catch (Exception e) {
            SilverLogger.getLogger(this).error(
                "Can't export document #" + attachment.getId() + " of publication #" +
                    attachment.getForeignId());
          }
        }
      }
    }

    return listToReturn;
  }

  private void copyAttachment(SimpleDocument attDetail, String exportPath) {
    String fichierJointExport = exportPath + File.separatorChar + FileServerUtils.
        replaceAccentChars(attDetail.getFilename());
    getAttachmentService().getBinaryContent(new File(fichierJointExport),
        attDetail.getPk(), null);
  }

  public InputStream getAttachmentContent(AttachmentDetail attachment) throws FileNotFoundException {
    return new FileInputStream(getAttachmentFile(attachment));
  }

  public File getAttachmentFile(AttachmentDetail attachment) {
    File file = new File(FileUtil.convertPathToServerOS(attachment.getAttachmentPath()));
    if (!file.exists() || !file.isFile()) {
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