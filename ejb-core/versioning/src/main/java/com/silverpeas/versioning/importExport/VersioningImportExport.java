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
package com.silverpeas.versioning.importExport;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import com.silverpeas.form.importExport.FormTemplateImportExport;
import com.silverpeas.form.importExport.XMLModelContentType;
import com.silverpeas.util.FileUtil;
import com.silverpeas.util.ForeignPK;
import com.silverpeas.util.StringUtil;
import com.silverpeas.util.i18n.I18NHelper;
import com.silverpeas.versioning.VersioningIndexer;
import com.stratelia.silverpeas.silverpeasinitialize.CallBackManager;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.silverpeas.versioning.model.Document;
import com.stratelia.silverpeas.versioning.model.DocumentVersion;
import com.stratelia.silverpeas.versioning.model.DocumentVersionPK;
import com.stratelia.webactiv.util.FileRepositoryManager;
import com.stratelia.webactiv.util.FileServerUtils;
import com.stratelia.webactiv.util.WAPrimaryKey;
import com.stratelia.webactiv.util.attachment.ejb.AttachmentPK;
import com.stratelia.webactiv.util.attachment.model.AttachmentDetail;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.silverpeas.attachment.AttachmentServiceFactory;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.silverpeas.attachment.model.HistorisedDocument;
import org.silverpeas.attachment.model.SimpleAttachment;
import org.silverpeas.attachment.model.SimpleDocument;
import org.silverpeas.attachment.model.SimpleDocumentPK;
import org.silverpeas.attachment.model.UnlockContext;

import com.stratelia.webactiv.util.ResourceLocator;

/**
 * @author neysseri
 */
public class VersioningImportExport {
  
  private final ResourceLocator resources = new ResourceLocator(
      "org.silverpeas.importExport.settings.importSettings", "");
  
  public int importDocuments(String objectId, String componentId, List<AttachmentDetail> attachments,
      int userId, boolean indexIt) throws RemoteException {
    return importDocuments(objectId, componentId, attachments, userId,
        DocumentVersion.TYPE_PUBLIC_VERSION, indexIt, null);
  }
  
  public int importDocuments(String objectId, String componentId,
      List<AttachmentDetail> attachments, int userId, int versionType, boolean indexIt)
      throws RemoteException {
    return importDocuments(objectId, componentId, attachments, userId, versionType, indexIt, null);
  }

  /**
   * @param objectId
   * @param componentId
   * @param attachments
   * @param userId
   * @param versionType
   * @param indexIt
   * @param topicId
   * @return
   * @throws RemoteException
   */
  public int importDocuments(String objectId, String componentId, List<AttachmentDetail> attachments,
      int userId, int versionType, boolean indexIt, String topicId) throws RemoteException {
    SilverTrace.info("versioning", "VersioningImportExport.importDocuments()",
        "root.GEN_PARAM_VALUE", componentId);
    int nbFilesProcessed = 0;
    
    ForeignPK pubPK = new ForeignPK(objectId, componentId);

    // get existing documents of object
    List<SimpleDocument> documents = AttachmentServiceFactory.getAttachmentService().
        listDocumentsByForeignKey(pubPK, null);
    for (AttachmentDetail attachment : attachments) {
      SimpleDocument document = isDocumentExist(documents, attachment);
      if (document != null) {
        // Un document portant le même nom existe déjà. On ajoute une nouvelle version au document
        AttachmentServiceFactory.getAttachmentService().updateAttachment(document,
            new File(attachment.getPhysicalName()), indexIt, indexIt);
      } else {
        HistorisedDocument version = new HistorisedDocument(new SimpleDocumentPK(null,
            componentId), objectId, -1, new SimpleAttachment(attachment.getLogicalName(),
            attachment.getLanguage(), attachment.getTitle(), attachment.getInfo(), attachment.
            getSize(), attachment.getType(), "" + userId, attachment.getCreationDate(), attachment.
            getXmlForm()));
        version.setPublicDocument((versionType == DocumentVersion.TYPE_PUBLIC_VERSION));
        version.setStatus("" + DocumentVersion.STATUS_VALIDATION_NOT_REQ);
        AttachmentServiceFactory.getAttachmentService().createAttachment(document,
            new File(attachment.getPhysicalName()), indexIt);
      }
      
      if (attachment.isRemoveAfterImport()) {
        boolean removed = FileUtils.deleteQuietly(new File(attachment.getOriginalPath()));
        if (!removed) {
          SilverTrace.error("versioning",
              "VersioningImportExport.importDocuments()",
              "root.MSG_GEN_PARAM_VALUE", "Can't remove file " + attachment.getOriginalPath());
        }
      }
      nbFilesProcessed++;
    }
    return nbFilesProcessed;
  }

  /**
   * @param pk
   * @param exportPath
   * @param relativeExportPath
   * @param extensionFilter : permet de filtrer un type de document en particulier, par son
   * extension.
   * @return
   * @throws RemoteException
   */
  public List<AttachmentDetail> exportDocuments(WAPrimaryKey pk, String exportPath,
      String relativeExportPath, String extensionFilter) throws RemoteException {
    List<AttachmentDetail> attachments = new ArrayList<AttachmentDetail>();
    String componentId = pk.getInstanceId();
    ForeignPK pubPK = new ForeignPK(pk.getId(), componentId);
    // get existing documents of object
    List<SimpleDocument> documents = AttachmentServiceFactory.getAttachmentService().
        listDocumentsByForeignKey(pubPK, null);

    // retrieve last public versions of each document
    for (SimpleDocument document : documents) {
      SimpleDocument lastVersion = document.getLastPublicVersion();
      if (extensionFilter == null || FilenameUtils.isExtension(lastVersion.getFilename(),
          extensionFilter)) {
        attachments.add(copyAttachment(document, exportPath, relativeExportPath));
      }
    }
    
    if (attachments.isEmpty()) {
      return null;
    }
    return attachments;
  }
  
  private AttachmentDetail copyAttachment(SimpleDocument document, String exportPath,
      String relativeExportPath) {
    AttachmentDetail attachmentCopy = getAttachmentDetail(document);
    String fichierJointExport = exportPath + File.separator
        + FileServerUtils.replaceAccentChars(attachmentCopy.getLogicalName());
    AttachmentServiceFactory.getAttachmentService().getBinaryContent(new File(fichierJointExport),
        document.getPk(), document.getLanguage());

    // Le nom physique correspond maintenant au fichier copié
    attachmentCopy.setPhysicalName(relativeExportPath + File.separator
        + FileServerUtils.replaceAccentChars(attachmentCopy.getLogicalName()));
    attachmentCopy.setLogicalName(FileServerUtils.
        replaceAccentChars(attachmentCopy.getLogicalName()));
    return attachmentCopy;
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
  
  private AttachmentDetail getAttachmentDetail(SimpleDocument version) {
    AttachmentPK pk = new AttachmentPK("useless", "useless", version.getPk().getInstanceId());
    AttachmentDetail attachment = new AttachmentDetail(pk, version.getAttachmentPath(), version.
        getFilename(), version.getDescription(), version.getContentType(), version.getSize(),
        "Versioning", version.getCreated(), new ForeignPK(version.getForeignId(), version.
        getInstanceId()));
    attachment.setTitle(version.getTitle());
    return attachment;
  }
  
  public List<SimpleDocument> importDocuments(ForeignPK objectPK, List<Document> documents,
      int userId, boolean indexIt) throws RemoteException, FileNotFoundException {
    SilverTrace.info("versioning", "VersioningImportExport.importDocuments()",
        "root.GEN_PARAM_VALUE", objectPK.toString());
    boolean launchCallback = false;
    int userIdCallback = -1;
    
    List<SimpleDocument> importedDocs = new ArrayList<SimpleDocument>(documents.size());

    // get existing documents of object
    List<SimpleDocument> existingDocuments = AttachmentServiceFactory.getAttachmentService().
        listDocumentsByForeignKey(objectPK, null);
    FormTemplateImportExport xmlIE = null;
    for (Document document : documents) {
      SimpleDocument existingDocument = null;
      if (document.getPk() != null && StringUtil.isDefined(document.getPk().getId())
          && !"-1".equals(document.getPk().getId())) {
        existingDocument = AttachmentServiceFactory.getAttachmentService().searchDocumentById(
            new SimpleDocumentPK("", document.getPk()), null);
      }
      if (existingDocument == null) {
        existingDocument = isDocumentExist(existingDocuments, document.getName());
        if (existingDocument != null) {
          document.getPk().setId("" + existingDocument.getPk().getOldSilverpeasId());
        }
      }
      
      if (existingDocument != null && existingDocument.isVersioned()) {
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
        SimpleDocument simpleDocument = null;
        for (DocumentVersion version : versions) {
          if (simpleDocument == null) {
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
            simpleDocument = new HistorisedDocument(new SimpleDocumentPK(null, objectPK.
                getInstanceId()), objectPK.getId(), -1, new SimpleAttachment(version.
                getLogicalName(), I18NHelper.defaultLanguage,
                document.getName(), document.getDescription(), version.getSize(), version.
                getMimeType(), version.getAuthorId() + "", version.getCreationDate(), xmlFormId));
            
            simpleDocument.setStatus("" + DocumentVersion.STATUS_VALIDATION_NOT_REQ);
            boolean isPublic = version.getType() == DocumentVersion.TYPE_PUBLIC_VERSION;
            if (isPublic) {
              launchCallback = true;
              userIdCallback = version.getAuthorId();
            }
            simpleDocument.setPublicDocument(isPublic);
            InputStream content = getVersionContent(version);
            simpleDocument.setContentType(version.getMimeType());
            simpleDocument.setSize(version.getSize());
            simpleDocument.setFilename(version.getLogicalName());
            simpleDocument = AttachmentServiceFactory.getAttachmentService().createAttachment(
                simpleDocument, content, indexIt);
            IOUtils.closeQuietly(content);
          } else {
            simpleDocument = addVersion(version, simpleDocument, userId, indexIt);
          }
          importedDocs.add(simpleDocument);
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
      if (launchCallback) {
        CallBackManager callBackManager = CallBackManager.get();
        callBackManager.invoke(CallBackManager.ACTION_VERSIONING_UPDATE, userIdCallback, objectPK.
            getInstanceId(), objectPK.getId());
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
  
  public List<DocumentVersion> copyFiles(String componentId, List<Document> documents, String path) {
    List<DocumentVersion> copiedAttachments = new ArrayList<DocumentVersion>();
    for (Document document : documents) {
      List<DocumentVersion> versions = document.getVersionsType().getListVersions();
      for (DocumentVersion version : versions) {
        copyFile(componentId, version, path);
        if (version.getSize() != 0) {
          copiedAttachments.add(version);
        }
      }
    }
    return copiedAttachments;
  }
  
  private void copyFile(String componentId, DocumentVersion version, String path) {
    String fileToUpload = version.getPhysicalName();
    // Préparation des paramètres du fichier à creer
    String logicalName = fileToUpload.substring(fileToUpload.lastIndexOf(File.separator) + 1);
    String type = FileRepositoryManager.getFileExtension(logicalName);
    String mimeType = FileUtil.getMimeType(logicalName);
    String physicalName = Long.toString(System.currentTimeMillis()) + "." + type;
    
    File fileToCreate = new File(path + physicalName);
    while (fileToCreate.exists()) {
      SilverTrace.info("versioning", "VersioningImportExport.copyFile()",
          "root.MSG_GEN_PARAM_VALUE", "fileToCreate already exists="
          + fileToCreate.getAbsolutePath());

      // To prevent overwriting
      physicalName = Long.toString(System.currentTimeMillis()) + "." + type;
      fileToCreate = new File(path + physicalName);
    }
    SilverTrace.info("versioning", "VersioningImportExport.copyFile()",
        "root.MSG_GEN_PARAM_VALUE", "fileName=" + logicalName);
    
    long size = 0;
    try {
      // Copie du fichier dans silverpeas
      size = copyFileToDisk(fileToUpload, fileToCreate);
    } catch (Exception e) {
      SilverTrace.error("versioning", "VersioningImportExport.copyFile()",
          "attachment.EX_FILE_COPY_ERROR", e);
    }

    // Compléments sur l'objet DocumentVersion
    version.setSize((int) size);
    version.setMimeType(mimeType);
    version.setPhysicalName(physicalName);
    version.setLogicalName(logicalName);
    version.setInstanceId(componentId);
    
    DocumentVersionPK pk = new DocumentVersionPK(-1, "useless", componentId);
    version.setPk(pk);
  }
  
  private long copyFileToDisk(String from, File to) throws IOException {
    OutputStream out = new BufferedOutputStream(new FileOutputStream(to));
    try {
      return FileUtils.copyFile(new File(from), out);
    } finally {
      IOUtils.closeQuietly(out);
    }
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
    AttachmentServiceFactory.getAttachmentService().
        lock(existingDocument.getId(), "" + userId, existingDocument.getLanguage());
    AttachmentServiceFactory.getAttachmentService().updateAttachment(existingDocument,
        getVersionContent(version), indexIt, launchCallback);
    AttachmentServiceFactory.getAttachmentService().
        unlock(new UnlockContext(existingDocument.getId(), "" + userId, existingDocument.
        getLanguage()));
    return AttachmentServiceFactory.getAttachmentService().searchDocumentById(existingDocument.
        getPk(), existingDocument.getLanguage());
  }
  
  InputStream getVersionContent(DocumentVersion version) throws FileNotFoundException {
    File file = new File(FileUtil.convertPathToServerOS(version.getDocumentPath()));
    if (file == null || !file.exists() || !file.isFile()) {
      String baseDir = resources.getString("importRepository");
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
