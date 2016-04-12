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
package org.silverpeas.core.importexport.versioning;

import org.silverpeas.core.importexport.form.FormTemplateImportExport;
import org.silverpeas.core.importexport.form.XMLModelContentType;
import org.silverpeas.core.silvertrace.SilverTrace;
import org.silverpeas.core.admin.user.model.UserDetail;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.silverpeas.core.contribution.attachment.AttachmentServiceProvider;
import org.silverpeas.core.contribution.attachment.model.HistorisedDocument;
import org.silverpeas.core.contribution.attachment.model.SimpleAttachment;
import org.silverpeas.core.contribution.attachment.model.SimpleDocument;
import org.silverpeas.core.contribution.attachment.model.SimpleDocumentPK;
import org.silverpeas.core.contribution.attachment.model.UnlockContext;
import org.silverpeas.core.contribution.attachment.notification.AttachmentEventNotifier;
import org.silverpeas.core.importexport.attachment.AttachmentDetail;
import org.silverpeas.core.importexport.attachment.AttachmentImportExport;
import org.silverpeas.core.notification.system.ResourceEvent;
import org.silverpeas.core.util.file.FileUtil;
import org.silverpeas.core.ForeignPK;
import org.silverpeas.core.util.ResourceLocator;
import org.silverpeas.core.util.SettingBundle;
import org.silverpeas.core.util.StringUtil;
import org.silverpeas.core.i18n.I18NHelper;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author neysseri
 */
public class VersioningImportExport {

  private UserDetail user;
  private final SettingBundle settings =
      ResourceLocator.getSettingBundle("org.silverpeas.importExport.settings.importSettings");

  public VersioningImportExport(UserDetail user) {
    this.user = user;
  }

  public int importDocuments(String objectId, String componentId, List<AttachmentDetail> attachments,
      boolean indexIt) throws RemoteException, IOException {
    return importDocuments(objectId, componentId, attachments, DocumentVersion.TYPE_PUBLIC_VERSION,
        indexIt, null);
  }

  /**
   * @param objectId
   * @param componentId
   * @param attachments
   * @param versionType
   * @param indexIt
   * @param topicId
   * @return
   * @throws RemoteException
   * @throws IOException
   */
  public int importDocuments(String objectId, String componentId, List<AttachmentDetail> attachments,
      int versionType, boolean indexIt, String topicId) throws RemoteException, IOException {

    int nbFilesProcessed = 0;
    AttachmentImportExport attachmentImportExport =
        new AttachmentImportExport(user);
    ForeignPK pubPK = new ForeignPK(objectId, componentId);

    // get existing documents of object
    List<SimpleDocument> documents = AttachmentServiceProvider.getAttachmentService().
        listDocumentsByForeignKey(pubPK, null);
    for (AttachmentDetail attachment : attachments) {
      InputStream content = attachmentImportExport.getAttachmentContent(attachment);
      if (!StringUtil.isDefined(attachment.getAuthor())) {
        attachment.setAuthor(user.getId());
      }
      SimpleDocument document = isDocumentExist(documents, attachment);
      if (document != null) {
        document.edit(attachment.getAuthor());
        AttachmentServiceProvider.getAttachmentService().lock(document.getId(), attachment
            .getAuthor(), null);
        AttachmentServiceProvider.getAttachmentService().updateAttachment(document, content, indexIt,
            true);
        AttachmentServiceProvider.getAttachmentService().unlock(new UnlockContext(document.getId(),
            attachment.getAuthor(), null));
      } else {
        if (attachment.getCreationDate() == null) {
          attachment.setCreationDate(new Date());
        }
        HistorisedDocument version = new HistorisedDocument(new SimpleDocumentPK(null, componentId),
            objectId, -1, attachment.getAuthor(), new SimpleAttachment(attachment.getLogicalName(),
            attachment.getLanguage(), attachment.getTitle(), attachment.getInfo(), attachment.
            getSize(), attachment.getType(), "" + user.getId(), attachment.getCreationDate(),
            attachment.getXmlForm()));
        version.setPublicDocument((versionType == DocumentVersion.TYPE_PUBLIC_VERSION));
        version.setStatus("" + DocumentVersion.STATUS_VALIDATION_NOT_REQ);
        AttachmentServiceProvider.getAttachmentService().createAttachment(version,
            content, indexIt);
      }

      if (attachment.isRemoveAfterImport()) {
        boolean removed = FileUtils.deleteQuietly(attachmentImportExport.getAttachmentFile(
            attachment));
        if (!removed) {
          SilverTrace.error("versioning", "VersioningImportExport.importDocuments()",
              "root.MSG_GEN_PARAM_VALUE", "Can't remove file " + attachmentImportExport
              .getAttachmentFile(attachment));
        }
      }
      nbFilesProcessed++;
    }
    return nbFilesProcessed;
  }

  private SimpleDocument isDocumentExist(List<SimpleDocument> documents, AttachmentDetail attachment) {
    String documentName = attachment.getTitle();
    if (!StringUtil.isDefined(documentName)) {
      documentName = attachment.getLogicalName();
    }
    for (SimpleDocument document : documents) {
      if (documentName.equalsIgnoreCase(document.getFilename())) {
        return document;
      }
    }
    return null;
  }

  public List<SimpleDocument> importDocuments(ForeignPK objectPK, List<Document> documents,
      int userId, boolean indexIt) throws RemoteException, FileNotFoundException {

    boolean launchCallback = false;
    int userIdCallback = -1;

    List<SimpleDocument> importedDocs = new ArrayList<SimpleDocument>(documents.size());

    // get existing documents of object
    List<SimpleDocument> existingDocuments = AttachmentServiceProvider.getAttachmentService().
        listDocumentsByForeignKey(objectPK, null);
    FormTemplateImportExport xmlIE = null;
    for (Document document : documents) {
      SimpleDocument existingDocument = null;
      if (document.getPk() != null && StringUtil.isDefined(document.getPk().getId())
          && !"-1".equals(document.getPk().getId())) {
        existingDocument = AttachmentServiceProvider.getAttachmentService().searchDocumentById(
            new SimpleDocumentPK("", document.getPk()), null);
      }
      if (existingDocument == null) {
        existingDocument = isDocumentExist(existingDocuments, document.getName());
        if (existingDocument != null) {
          document.setPk(new DocumentPK((int) existingDocument.getPk().getOldSilverpeasId(),
              objectPK.getInstanceId()));
        }
      }

      ResourceEvent.Type type = ResourceEvent.Type.CREATION;
      if (existingDocument != null && existingDocument.isVersioned()) {
        type = ResourceEvent.Type.UPDATE;
        List<DocumentVersion> versions = document.getVersionsType().getListVersions();
        for (DocumentVersion version : versions) {
          version.setInstanceId(objectPK.getInstanceId());
          existingDocument = addVersion(version, existingDocument, userId, indexIt);
          XMLModelContentType xmlContent = version.getXMLModelContentType();
          // Store xml content
          try {
            if (xmlContent != null) {
              if (xmlIE == null) {
                xmlIE = new FormTemplateImportExport();
              }
              ForeignPK pk = new ForeignPK(version.getPk().getId(), version.getPk().
                  getInstanceId());
              xmlIE.importXMLModelContentType(pk, "Versioning", xmlContent,
                  Integer.toString(version.getAuthorId()));
            }
          } catch (Exception e) {
            SilverTrace.error("versioning", "VersioningImportExport.importDocuments()",
                "root.MSG_GEN_PARAM_VALUE", e);
          }
        }
      } else {
        // Il n'y a pas de document portant le même nom
        // On crée un nouveau document
        List<DocumentVersion> versions = document.getVersionsType().getListVersions();
        existingDocument = null;
        for (DocumentVersion version : versions) {
          if (existingDocument == null) {
            if (version.getCreationDate() == null) {
              version.setCreationDate(new Date());
            }
            if (version.getAuthorId() == -1) {
              version.setAuthorId(userId);
            }
            // Création du nouveau document

            XMLModelContentType xmlContent = version.getXMLModelContentType();
            String xmlFormId = null;
            if (xmlContent != null) {
              xmlFormId = xmlContent.getName();
            }
            existingDocument = new HistorisedDocument(new SimpleDocumentPK(null, objectPK.
                getInstanceId()), objectPK.getId(), -1, new SimpleAttachment(version.
                getLogicalName(), I18NHelper.defaultLanguage,
                document.getName(), document.getDescription(), version.getSize(), version.
                getMimeType(), version.getAuthorId() + "", version.getCreationDate(), xmlFormId));

            existingDocument.setStatus("" + DocumentVersion.STATUS_VALIDATION_NOT_REQ);
            boolean isPublic = version.getType() == DocumentVersion.TYPE_PUBLIC_VERSION;
            if (isPublic) {
              launchCallback = true;
              userIdCallback = version.getAuthorId();
            }
            existingDocument.setPublicDocument(isPublic);
            InputStream content = getVersionContent(version);
            existingDocument.setContentType(version.getMimeType());
            existingDocument.setSize(version.getSize());
            existingDocument.setFilename(version.getLogicalName());
            existingDocument = AttachmentServiceProvider.getAttachmentService()
                .createAttachment(existingDocument, content, indexIt);
            IOUtils.closeQuietly(content);
          } else {
            existingDocument = addVersion(version, existingDocument, userId, indexIt);
          }
          importedDocs.add(existingDocument);
          // Store xml content
          try {
            XMLModelContentType xmlContent = version.getXMLModelContentType();
            if (xmlContent != null) {
              if (xmlIE == null) {
                xmlIE = new FormTemplateImportExport();
              }
              ForeignPK pk = new ForeignPK(version.getPk().getId(), version.getPk().getInstanceId());
              xmlIE.importXMLModelContentType(pk, "Versioning", xmlContent, Integer.
                  toString(version.getAuthorId()));
            }
          } catch (Exception e) {
            SilverTrace.error("versioning", "VersioningImportExport.importDocuments()",
                "root.MSG_GEN_PARAM_VALUE", e);
          }
        }
      }
      if (launchCallback && existingDocument != null) {
        AttachmentEventNotifier notifier = AttachmentEventNotifier.getNotifier();
        notifier.notifyEventOn(type, existingDocument);
      }
    }
    return importedDocs;
  }

  private SimpleDocument isDocumentExist(List<SimpleDocument> documents, String name) {
    if (name != null) {
      for (SimpleDocument document : documents) {
        if (name.equalsIgnoreCase(document.getFilename()) || name.equalsIgnoreCase(document.
            getTitle())) {
          return document;
        }
      }
    }
    return null;
  }

  protected SimpleDocument addVersion(DocumentVersion version, SimpleDocument existingDocument,
      int userId, boolean indexIt) throws FileNotFoundException {
    boolean isPublic = (version.getType() == DocumentVersion.TYPE_PUBLIC_VERSION);
    boolean launchCallback = (version.getType() == DocumentVersion.TYPE_PUBLIC_VERSION);
    existingDocument.setPublicDocument(isPublic);
    existingDocument.setStatus("" + DocumentVersion.STATUS_VALIDATION_NOT_REQ);
    existingDocument.setUpdated(new Date());
    existingDocument.setUpdatedBy("" + userId);
    XMLModelContentType xmlContent = version.getXMLModelContentType();
    if (xmlContent != null) {
      existingDocument.setXmlFormId(xmlContent.getName());
    }
    AttachmentServiceProvider.getAttachmentService().
        lock(existingDocument.getId(), "" + userId, existingDocument.getLanguage());
    AttachmentServiceProvider.getAttachmentService().updateAttachment(existingDocument,
        getVersionContent(version), indexIt, launchCallback);
    AttachmentServiceProvider.getAttachmentService().
        unlock(new UnlockContext(existingDocument.getId(), "" + userId, existingDocument.
        getLanguage()));
    return AttachmentServiceProvider.getAttachmentService().searchDocumentById(existingDocument.
        getPk(), existingDocument.getLanguage());
  }

  InputStream getVersionContent(DocumentVersion version) throws FileNotFoundException {
    File file = new File(FileUtil.convertPathToServerOS(version.getDocumentPath()));
    if (file == null || !file.exists() || !file.isFile()) {
      String baseDir = settings.getString("importRepository", "");
      file = new File(FileUtil.convertPathToServerOS(baseDir + File.separatorChar + version.
          getPhysicalName()));
    }
    version.setMimeType(FileUtil.getMimeType(file.getName()));
    if (!StringUtil.isDefined(version.getLogicalName())) {
      version.setLogicalName(file.getName());
    }
    version.setSize(file.length());
    return new FileInputStream(file);
  }
}
