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
package org.silverpeas.core.importexport.versioning;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.silverpeas.core.ResourceReference;
import org.silverpeas.core.admin.user.model.UserDetail;
import org.silverpeas.core.contribution.attachment.AttachmentServiceProvider;
import org.silverpeas.core.contribution.attachment.model.HistorisedDocument;
import org.silverpeas.core.contribution.attachment.model.SimpleAttachment;
import org.silverpeas.core.contribution.attachment.model.SimpleDocument;
import org.silverpeas.core.contribution.attachment.model.SimpleDocumentPK;
import org.silverpeas.core.contribution.attachment.model.UnlockContext;
import org.silverpeas.core.contribution.attachment.notification.AttachmentEventNotifier;
import org.silverpeas.core.i18n.I18NHelper;
import org.silverpeas.core.importexport.attachment.AttachmentDetail;
import org.silverpeas.core.importexport.attachment.AttachmentImportExport;
import org.silverpeas.core.importexport.form.FormTemplateImportExport;
import org.silverpeas.core.importexport.form.XMLModelContentType;
import org.silverpeas.core.notification.system.ResourceEvent;
import org.silverpeas.core.util.ResourceLocator;
import org.silverpeas.core.util.SettingBundle;
import org.silverpeas.core.util.StringUtil;
import org.silverpeas.core.util.file.FileUtil;
import org.silverpeas.core.util.logging.SilverLogger;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author neysseri
 */
public class VersioningImport {

  private UserDetail user;
  private final SettingBundle settings =
      ResourceLocator.getSettingBundle("org.silverpeas.importExport.settings.importSettings");

  public VersioningImport(UserDetail user) {
    this.user = user;
  }

  public int importDocuments(String objectId, String componentId, List<AttachmentDetail> attachments,
      boolean indexIt) throws IOException {

    int versionType = DocumentVersion.TYPE_PUBLIC_VERSION;
    int nbFilesProcessed = 0;
    AttachmentImportExport attachmentImportExport = new AttachmentImportExport(user);
    ResourceReference pubPK = new ResourceReference(objectId, componentId);

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
            null, attachment.getTitle(), attachment.getDescription(), attachment.
            getSize(), attachment.getType(), "" + user.getId(), attachment.getCreationDate(),
            attachment.getXmlForm()));
        version.setPublicDocument(versionType == DocumentVersion.TYPE_PUBLIC_VERSION);
        version.setStatus("" + DocumentVersion.STATUS_VALIDATION_NOT_REQ);
        AttachmentServiceProvider.getAttachmentService().createAttachment(version,
            content, indexIt);
      }

      if (attachment.isRemoveAfterImport()) {
        boolean removed = FileUtils.deleteQuietly(attachmentImportExport.getAttachmentFile(
            attachment));
        if (!removed) {
          SilverLogger.getLogger(this).error("Can't remove file {0}", attachmentImportExport
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

  public List<SimpleDocument> importDocuments(ResourceReference objectPK, List<Document> documents,
      int userId, boolean indexIt) throws FileNotFoundException {
    List<SimpleDocument> importedDocs = new ArrayList<>(documents.size());

    // get existing documents of object
    List<SimpleDocument> existingDocuments = AttachmentServiceProvider.getAttachmentService().
        listDocumentsByForeignKey(objectPK, null);
    FormTemplateImportExport xmlIE = new FormTemplateImportExport();
    for (Document document : documents) {
      importDocument(objectPK, userId, indexIt, importedDocs, existingDocuments,
              xmlIE, document);
    }
    return importedDocs;
  }

  private void importDocument(final ResourceReference objectPK, final int userId, final boolean indexIt,
      final List<SimpleDocument> importedDocs,
      final List<SimpleDocument> existingDocuments, final FormTemplateImportExport xmlIE,
      final Document document) throws FileNotFoundException {
    boolean launchCallback = false;

    SimpleDocument existingDocument = getExistingDocument(objectPK, existingDocuments, document);

    ResourceEvent.Type type = ResourceEvent.Type.CREATION;
    if (existingDocument != null && existingDocument.isVersioned()) {
      type = ResourceEvent.Type.UPDATE;
      existingDocument = addVersionForExistingDocument(objectPK, userId, indexIt, xmlIE, document,
          existingDocument);
    } else {
      // no document yet existing with the same name, so create it
      List<DocumentVersion> versions = document.getVersionsType();
      existingDocument = null;
      for (DocumentVersion version : versions) {
        if (existingDocument == null) {
          if (version.getCreationDate() == null) {
            version.setCreationDate(new Date());
          }
          if (version.getAuthorId() == -1) {
            version.setAuthorId(userId);
          }
          boolean isPublic = version.getType() == DocumentVersion.TYPE_PUBLIC_VERSION;
          if (isPublic) {
            launchCallback = true;
          }
          existingDocument = createSimpleDocument(objectPK, indexIt, document, version, isPublic);
        } else {
          existingDocument = addVersion(version, existingDocument, userId, indexIt);
        }
        importedDocs.add(existingDocument);
        // Store xml content
        importXMLContent(version, xmlIE);
      }
    }
    if (launchCallback && existingDocument != null) {
      AttachmentEventNotifier notifier = AttachmentEventNotifier.getNotifier();
      notifier.notifyEventOn(type, existingDocument);
    }
  }

  private SimpleDocument addVersionForExistingDocument(final ResourceReference objectPK, final int userId,
      final boolean indexIt, final FormTemplateImportExport xmlIE, final Document document,
      SimpleDocument existingDocument) throws FileNotFoundException {
    List<DocumentVersion> versions = document.getVersionsType();
    SimpleDocument lastDocumentVersion = existingDocument;
    for (DocumentVersion version : versions) {
      version.setInstanceId(objectPK.getInstanceId());
      lastDocumentVersion = addVersion(version, existingDocument, userId, indexIt);
      importXMLContent(version, xmlIE);
    }
    return lastDocumentVersion;
  }

  private SimpleDocument createSimpleDocument(final ResourceReference objectPK, final boolean indexIt,
      final Document document, final DocumentVersion version, final boolean isPublic)
      throws FileNotFoundException {
    XMLModelContentType xmlContent = version.getXMLModelContentType();
    String xmlFormId = null;
    if (xmlContent != null) {
      xmlFormId = xmlContent.getName();
    }
    SimpleDocument existingDocument = new HistorisedDocument(new SimpleDocumentPK(null, objectPK.
        getInstanceId()), objectPK.getId(), -1, new SimpleAttachment(version.
        getLogicalName(), I18NHelper.defaultLanguage,
        document.getName(), document.getDescription(), version.getSize(), version.
        getMimeType(), version.getAuthorId() + "", version.getCreationDate(), xmlFormId));

    existingDocument.setStatus("" + DocumentVersion.STATUS_VALIDATION_NOT_REQ);
    existingDocument.setPublicDocument(isPublic);
    InputStream content = getVersionContent(version);
    existingDocument.setContentType(version.getMimeType());
    existingDocument.setSize(version.getSize());
    existingDocument.setFilename(version.getLogicalName());
    existingDocument.setComment(version.getComments());
    existingDocument = AttachmentServiceProvider.getAttachmentService()
        .createAttachment(existingDocument, content, indexIt);
    IOUtils.closeQuietly(content);
    return existingDocument;
  }

  private SimpleDocument getExistingDocument(final ResourceReference objectPK,
      final List<SimpleDocument> existingDocuments, final Document document) {
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
    return existingDocument;
  }

  private void importXMLContent(DocumentVersion version, FormTemplateImportExport xmlIE) {
    XMLModelContentType xmlContent = version.getXMLModelContentType();
    try {
      if (xmlContent != null) {
        ResourceReference pk = new ResourceReference(version.getPk().getId(), version.getPk().
            getInstanceId());
        xmlIE.importXMLModelContentType(pk, "Versioning", xmlContent,
            Integer.toString(version.getAuthorId()));
      }
    } catch (Exception e) {
      SilverLogger.getLogger(this).error(e);
    }
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

  private SimpleDocument addVersion(DocumentVersion version, SimpleDocument existingDocument,
      int userId, boolean indexIt) throws FileNotFoundException {
    InputStream content = getVersionContent(version);
    boolean isPublic = version.getType() == DocumentVersion.TYPE_PUBLIC_VERSION;
    boolean launchCallback = isPublic;
    existingDocument.setPublicDocument(isPublic);
    existingDocument.setStatus("" + DocumentVersion.STATUS_VALIDATION_NOT_REQ);
    existingDocument.setUpdated(new Date());
    existingDocument.setUpdatedBy("" + userId);
    existingDocument.setContentType(version.getMimeType());
    existingDocument.setSize(version.getSize());
    existingDocument.setFilename(version.getLogicalName());
    existingDocument.setComment(version.getComments());
    XMLModelContentType xmlContent = version.getXMLModelContentType();
    if (xmlContent != null) {
      existingDocument.setXmlFormId(xmlContent.getName());
    }
    AttachmentServiceProvider.getAttachmentService().
        lock(existingDocument.getId(), "" + userId, existingDocument.getLanguage());
    AttachmentServiceProvider.getAttachmentService().updateAttachment(existingDocument,
        content, indexIt, launchCallback);
    AttachmentServiceProvider.getAttachmentService().
        unlock(new UnlockContext(existingDocument.getId(), "" + userId, existingDocument.
        getLanguage()));

    IOUtils.closeQuietly(content);

    return AttachmentServiceProvider.getAttachmentService().searchDocumentById(existingDocument.
        getPk(), existingDocument.getLanguage());
  }

  private InputStream getVersionContent(DocumentVersion version) throws FileNotFoundException {
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