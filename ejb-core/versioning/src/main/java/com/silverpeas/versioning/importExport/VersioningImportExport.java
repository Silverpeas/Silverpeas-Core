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
 * Created on 11 févr. 2005
 *
 */
package com.silverpeas.versioning.importExport;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Vector;

import com.silverpeas.form.importExport.FormTemplateImportExport;
import com.silverpeas.form.importExport.XMLModelContentType;
import com.silverpeas.util.ForeignPK;
import com.silverpeas.util.StringUtil;
import com.silverpeas.versioning.VersioningIndexer;
import com.stratelia.silverpeas.silverpeasinitialize.CallBackManager;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.silverpeas.versioning.ejb.VersioningBm;
import com.stratelia.silverpeas.versioning.ejb.VersioningBmHome;
import com.stratelia.silverpeas.versioning.ejb.VersioningRuntimeException;
import com.stratelia.silverpeas.versioning.model.Document;
import com.stratelia.silverpeas.versioning.model.DocumentPK;
import com.stratelia.silverpeas.versioning.model.DocumentVersion;
import com.stratelia.silverpeas.versioning.model.DocumentVersionPK;
import com.stratelia.silverpeas.versioning.model.Worker;
import com.stratelia.silverpeas.versioning.util.VersioningUtil;
import com.stratelia.webactiv.util.EJBUtilitaire;
import com.stratelia.webactiv.util.FileRepositoryManager;
import com.stratelia.webactiv.util.FileServerUtils;
import com.stratelia.webactiv.util.JNDINames;
import com.stratelia.webactiv.util.WAPrimaryKey;
import com.stratelia.webactiv.util.attachment.control.AttachmentController;
import com.stratelia.webactiv.util.attachment.ejb.AttachmentException;
import com.stratelia.webactiv.util.attachment.ejb.AttachmentPK;
import com.stratelia.webactiv.util.attachment.model.AttachmentDetail;
import com.stratelia.webactiv.util.exception.SilverpeasException;

/**
 * @author neysseri
 */
public class VersioningImportExport {

  private VersioningBm versioningBm = null;
  private VersioningIndexer indexer = new VersioningIndexer();
  private static int BUFFER_SIZE = 1024;

  public int importDocuments(String objectId, String componentId,
      List attachments, int userId, boolean indexIt, String topicId)
      throws RemoteException {
    return importDocuments(objectId, componentId, attachments, userId,
        DocumentVersion.TYPE_PUBLIC_VERSION, indexIt, topicId);
  }

  public int importDocuments(String objectId, String componentId,
      List attachments, int userId, boolean indexIt) throws RemoteException {
    return importDocuments(objectId, componentId, attachments, userId,
        DocumentVersion.TYPE_PUBLIC_VERSION, indexIt, null);
  }

  public int importDocuments(String objectId, String componentId,
      List attachments, int userId, int versionType, boolean indexIt)
      throws RemoteException {
    return importDocuments(objectId, componentId, attachments, userId,
        versionType, indexIt, null);
  }

  /**
   * @param objectId
   * @param componentId
   * @param attachments
   * @param userId
   * @return
   * @throws RemoteException
   */
  public int importDocuments(String objectId, String componentId,
      List attachments, int userId, int versionType, boolean indexIt,
      String topicId) throws RemoteException {
    SilverTrace.info("versioning", "VersioningImportExport.importDocuments()",
        "root.GEN_PARAM_VALUE", componentId);
    int nbFilesProcessed = 0;

    ForeignPK pubPK = new ForeignPK(objectId, componentId);

    // get existing documents of object
    List<Document> documents = getVersioningBm().getDocuments(pubPK);

    AttachmentDetail attachment = null;
    DocumentVersion version = null;
    Document document = null;
    for (int a = 0; a < attachments.size(); a++) {
      attachment = (AttachmentDetail) attachments.get(a);
      version = new DocumentVersion(attachment);
      version.setAuthorId(userId);
      version.setInstanceId(componentId);
      document = isDocumentExist(documents, attachment);
      if (document != null) {
        // Un document portant le même nom existe déjà
        // On ajoute une nouvelle version au document
        DocumentVersion lastVersion = getVersioningBm().getLastDocumentVersion(document.getPk());
        if (lastVersion != null) {
          version.setMimeType(attachment.getType());
          if (attachment.getType() == null) {
            version.setMimeType("dummy");
          }
          version.setMajorNumber(lastVersion.getMajorNumber());
          version.setMinorNumber(lastVersion.getMinorNumber());
          version.setType(versionType);
          version.setStatus(DocumentVersion.STATUS_VALIDATION_NOT_REQ);
          version.setCreationDate(new Date());

          if (!StringUtil.isDefined(attachment.getInfo())) {
            version.setComments(attachment.getInfo());
          }
          version = getVersioningBm().addDocumentVersion(document, version);
        }
      } else {
        // Il n'y a pas de document portant le même nom
        // On crée un nouveau document
        DocumentPK docPK = new DocumentPK(-1, "useless", componentId);
        document = new Document(docPK, pubPK, attachment.getLogicalName(), "",
            Document.STATUS_CHECKOUTED, -1, new Date(), null, componentId, new ArrayList<Worker>(),
            new ArrayList(), 0, 0);
        if (StringUtil.isDefined(attachment.getTitle())) {
          document.setName(attachment.getTitle());
        }
        if (StringUtil.isDefined(attachment.getInfo())) {
          document.setDescription(attachment.getInfo());
        }
        if (versionType == DocumentVersion.TYPE_PUBLIC_VERSION) {
          version.setMajorNumber(1);
          version.setMinorNumber(0);
        } else {
          version.setMajorNumber(0);
          version.setMinorNumber(1);
        }
        version.setType(versionType);
        version.setStatus(DocumentVersion.STATUS_VALIDATION_NOT_REQ);
        version.setCreationDate(new Date());
        version.setMimeType(attachment.getType());
        if (attachment.getType() == null) {
          version.setMimeType("dummy");
        }
        docPK = getVersioningBm().createDocument(document, version);
        document.setPk(docPK);

        VersioningUtil versioningUtil = new VersioningUtil(componentId, document,
            String.valueOf(userId), topicId);
        versioningUtil.setFileRights(document);
        getVersioningBm().updateWorkList(document);
        getVersioningBm().updateDocument(document);
      }

      nbFilesProcessed++;
      indexer.createIndex(document, version);
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
  public Vector exportDocuments(WAPrimaryKey pk, String exportPath,
      String relativeExportPath, String extensionFilter) throws RemoteException {
    Vector<AttachmentDetail> attachments = new Vector<AttachmentDetail>();
    String componentId = pk.getInstanceId();
    ForeignPK pubPK = new ForeignPK(pk.getId(), componentId);
    // get existing documents of object
    List<Document> documents = getVersioningBm().getDocuments(pubPK);

    // retrieve last public versions of each document
    for (Document document : documents) {
      DocumentVersion version = getVersioningBm().getLastPublicDocumentVersion(document.getPk());
      if (version != null) {
        AttachmentDetail attachment = getAttachmentDetail(document, version);
        if (extensionFilter == null
            || attachment.getExtension().equalsIgnoreCase(extensionFilter)) {
          attachments.add(copyAttachment(attachment, exportPath,
              relativeExportPath, componentId));
          attachments.add(attachment);
        }
      }
    }

    if (attachments.isEmpty()) {
      return null;
    } else {
      return attachments;
    }
  }

  private AttachmentDetail copyAttachment(AttachmentDetail attachment,
      String exportPath, String relativeExportPath, String componentId) {
    AttachmentDetail attachmentCopy = attachment;
    String fichierJoint = getVersioningPath(componentId) + File.separator
        + attachmentCopy.getPhysicalName();
    String fichierJointExport = exportPath
        + File.separator
        + FileServerUtils.replaceAccentChars(attachmentCopy.getLogicalName());
    try {
      FileRepositoryManager.copyFile(fichierJoint, fichierJointExport);
    } catch (IOException e) {
      SilverTrace.warn("versioning", "VersioningImportExport.copyAttachment",
          "root.EX_FILE_NOT_FOUND", e);
    }
    // Le nom physique correspond maintenant au fichier copié
    attachmentCopy.setPhysicalName(relativeExportPath
        + File.separator
        + FileServerUtils.replaceAccentChars(attachmentCopy.getLogicalName()));
    attachmentCopy.setLogicalName(FileServerUtils.replaceAccentChars(attachmentCopy.getLogicalName()));
    return attachmentCopy;
  }

  public String getVersioningPath(String componentId) {
    return indexer.createPath("useless", componentId);
  }

  private VersioningBm getVersioningBm() {
    if (versioningBm == null) {
      try {
        VersioningBmHome versioningBmHome = (VersioningBmHome) EJBUtilitaire.getEJBObjectRef(JNDINames.VERSIONING_EJBHOME,
            VersioningBmHome.class);
        versioningBm = versioningBmHome.create();
      } catch (Exception e) {
        throw new VersioningRuntimeException(
            "VersioningImportExport.getVersioningBm()",
            SilverpeasException.ERROR, "root.EX_CANT_GET_REMOTE_OBJECT", e);
      }
    }
    return versioningBm;
  }

  private Document isDocumentExist(List<Document> documents, AttachmentDetail attachment) {
    String documentName = attachment.getTitle();
    if (! StringUtil.isDefined(documentName)) {
      documentName = attachment.getLogicalName();
    }
    for (Document document : documents) {
      if (documentName.equalsIgnoreCase(document.getName())) {
        return document;
      }
    }
    return null;
  }

  private AttachmentDetail getAttachmentDetail(Document document,
      DocumentVersion version) {
    AttachmentPK pk = new AttachmentPK("useless", "useless", version.getPk().getInstanceId());
    AttachmentDetail attachment = new AttachmentDetail(pk, version.getPhysicalName(), version.getLogicalName(), version.getComments(),
        version.getMimeType(), version.getSize(), "Versioning", version.getCreationDate(), document.getForeignKey());
    attachment.setTitle(document.getName());

    String info = document.getDescription();
    String versionComments = version.getComments();
    if (!StringUtil.isDefined(info)) {
      if (!StringUtil.isDefined(versionComments)) {
        info += " " + versionComments;
      }
    } else {
      if (!StringUtil.isDefined(versionComments)) {
        info = versionComments;
      }
    }
    attachment.setInfo(info);

    return attachment;
  }

  public int importDocuments(ForeignPK objectPK, List<Document> documents, int userId,
      boolean indexIt) throws RemoteException {
    SilverTrace.info("versioning", "VersioningImportExport.importDocuments()",
        "root.GEN_PARAM_VALUE", objectPK.toString());
    int nbFilesProcessed = 0;
    boolean launchCallback = false;
    int userIdCallback = -1;

    // get existing documents of object
    List<Document> existingDocuments = getVersioningBm().getDocuments(objectPK);


    // DocumentVersion version = null;
    XMLModelContentType xmlContent = null;
    FormTemplateImportExport xmlIE = null;
    for (Document document : documents) {
      Document existingDocument = null;
      if (document.getPk() != null && StringUtil.isDefined(document.getPk().getId())
          && !"-1".equals(document.getPk().getId())) {
        existingDocument = getVersioningBm().getDocument(document.getPk());
      }
      if (existingDocument == null) {
        existingDocument = isDocumentExist(existingDocuments, document.getName());
        if(existingDocument != null) {
          document.setPk(existingDocument.getPk());
        }
      }

      if (existingDocument != null) {
        // Un document portant le même nom existe déjà
        // On ajoute les nouvelles versions au document
        List<DocumentVersion> versions = getVersioningBm().getDocumentVersions(existingDocument.getPk());
        if (versions != null && !versions.isEmpty()) {
          DocumentVersion lastVersion = versions.get(0);
          int majorNumber = lastVersion.getMajorNumber();
          int minorNumber = lastVersion.getMinorNumber();
          versions = document.getVersionsType().getListVersions();
          for (DocumentVersion version : versions) {
            version.setMajorNumber(majorNumber);
            version.setMinorNumber(minorNumber);
            version.setStatus(DocumentVersion.STATUS_VALIDATION_NOT_REQ);
            if (version.getCreationDate() == null) {
              version.setCreationDate(new Date());
            }
            if (version.getAuthorId() == -1) {
              version.setAuthorId(userId);
            }
            xmlContent = version.getXMLModelContentType();
            if (xmlContent != null) {
              version.setXmlForm(xmlContent.getName());
            }

            getVersioningBm().addDocumentVersion(document, version);

            if (version.getType() == DocumentVersion.TYPE_PUBLIC_VERSION) {
              launchCallback = true;
              userIdCallback = version.getAuthorId();
            }

            // Store xml content
            try {
              if (xmlContent != null) {
                if (xmlIE == null) {
                  xmlIE = new FormTemplateImportExport();
                }

                ForeignPK pk = new ForeignPK(version.getPk().getId(), version.getPk().getInstanceId());
                xmlIE.importXMLModelContentType(pk, "Versioning", xmlContent,
                    Integer.toString(version.getAuthorId()));
              }
            } catch (Exception e) {
              SilverTrace.error("versioning",
                  "VersioningImportExport.importDocuments()",
                  "root.MSG_GEN_PARAM_VALUE", e);
            }

            majorNumber = version.getMajorNumber();
            minorNumber = version.getMinorNumber();
          }
        }
      } else {
        // Il n'y a pas de document portant le même nom
        // On crée un nouveau document
        List<DocumentVersion> versions = document.getVersionsType().getListVersions();
        int majorNumber = 0;
        int minorNumber = 0;
        for (int v = 0; v < versions.size(); v++) {
          DocumentVersion version = versions.get(v);
          if (v == 0) {
            // Création du nouveau document
            DocumentPK docPK = new DocumentPK(-1, "useless", objectPK.getInstanceId());
            document = new Document(docPK, objectPK, document.getName(),
                document.getDescription(),Document.STATUS_CHECKOUTED, -1, new Date(), null,
                objectPK.getInstanceId(), new ArrayList<Worker>(), new ArrayList(), 0, 0);

            // et on y ajoute la première version
            if (version.getType() == DocumentVersion.TYPE_PUBLIC_VERSION) {
              majorNumber = 1;
              minorNumber = 0;
            } else {
              majorNumber = 0;
              minorNumber = 1;
            }
            version.setMajorNumber(majorNumber);
            version.setMinorNumber(minorNumber);

            version.setStatus(DocumentVersion.STATUS_VALIDATION_NOT_REQ);
            if (version.getCreationDate() == null) {
              version.setCreationDate(new Date());
            }
            if (version.getAuthorId() == -1) {
              version.setAuthorId(userId);
            }

            xmlContent = version.getXMLModelContentType();
            if (xmlContent != null) {
              version.setXmlForm(xmlContent.getName());
            }

            docPK = getVersioningBm().createDocument(document, version);
            document.setPk(docPK);

            if (version.getType() == DocumentVersion.TYPE_PUBLIC_VERSION) {
              launchCallback = true;
              userIdCallback = version.getAuthorId();
            }
            VersioningUtil versioningUtil = new VersioningUtil(objectPK.getInstanceId(), document,
                String.valueOf(userId), "0"); // TODO
            versioningUtil.setFileRights(document);
            getVersioningBm().updateWorkList(document);
            getVersioningBm().updateDocument(document);
          } else {
            // The document exists. Just add successive versions.
            version.setMajorNumber(majorNumber);
            version.setMinorNumber(minorNumber);
            version.setStatus(DocumentVersion.STATUS_VALIDATION_NOT_REQ);
            if (version.getCreationDate() == null) {
              version.setCreationDate(new Date());
            }
            if (version.getAuthorId() == -1) {
              version.setAuthorId(userId);
            }

            xmlContent = version.getXMLModelContentType();
            if (xmlContent != null) {
              version.setXmlForm(xmlContent.getName());
            }
            getVersioningBm().addDocumentVersion(document, version);
            if (version.getType() == DocumentVersion.TYPE_PUBLIC_VERSION) {
              launchCallback = true;
              userIdCallback = version.getAuthorId();
            }

            majorNumber = version.getMajorNumber();
            minorNumber = version.getMinorNumber();
          }

          // Store xml content
          try {
            xmlContent = version.getXMLModelContentType();
            if (xmlContent != null) {
              if (xmlIE == null) {
                xmlIE = new FormTemplateImportExport();
              }
              ForeignPK pk = new ForeignPK(version.getPk().getId(), version.getPk().getInstanceId());
              xmlIE.importXMLModelContentType(pk, "Versioning", xmlContent,
                  Integer.toString(version.getAuthorId()));
            }
          } catch (Exception e) {
            SilverTrace.error("versioning",
                "VersioningImportExport.importDocuments()",
                "root.MSG_GEN_PARAM_VALUE", e);
          }

          nbFilesProcessed++;
          if (indexIt) {
            indexer.createIndex(document, version);
          }
        }
      }
      if (launchCallback) {
        CallBackManager callBackManager = CallBackManager.get();
        callBackManager.invoke(CallBackManager.ACTION_VERSIONING_UPDATE,
            userIdCallback, document.getForeignKey().getInstanceId(), document.getForeignKey().getId());
      }
    }
    return nbFilesProcessed;
  }

  private Document isDocumentExist(List<Document> documents, String name) {
    for (Document document : documents) {
      if (document.getName().equalsIgnoreCase(name)) {
        return document;
      }
    }
    return null;
  }

  public List<DocumentVersion> copyFiles(String componentId, List<Document> documents, String path) {
    List<DocumentVersion> copiedAttachments = new ArrayList<DocumentVersion>();
    for (Document document : documents) {
      List<DocumentVersion> versions = document.getVersionsType().getListVersions();
      for( DocumentVersion version: versions) {
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
    String mimeType = AttachmentController.getMimeType(logicalName);
    String physicalName = new Long(new Date().getTime()).toString() + "." + type;

    File fileToCreate = new File(path + physicalName);
    while (fileToCreate.exists()) {
      SilverTrace.info("versioning", "VersioningImportExport.copyFile()",
          "root.MSG_GEN_PARAM_VALUE", "fileToCreate already exists="
          + fileToCreate.getAbsolutePath());

      // To prevent overwriting
      physicalName = new Long(new Date().getTime()).toString() + "." + type;
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

  private long copyFileToDisk(String from, File to) throws AttachmentException {
    FileInputStream fl_in = null;
    FileOutputStream fl_out = null;

    long size = 0;
    try {
      fl_in = new FileInputStream(from);
    } catch (FileNotFoundException ex) {
      throw new AttachmentException("AttachmentsType.copyFileToDisk()",
          SilverpeasException.ERROR, "attachment.EX_FILE_TO_UPLOAD_NOTFOUND",
          ex);
    }
    try {
      fl_out = new FileOutputStream(to);

      byte[] data = new byte[BUFFER_SIZE];
      int bytes_readed = fl_in.read(data);
      while (bytes_readed > 0) {
        size += bytes_readed;
        fl_out.write(data, 0, bytes_readed);
        bytes_readed = fl_in.read(data);
      }
      fl_in.close();
      fl_out.close();
    } catch (Exception ex) {
      throw new AttachmentException("AttachmentsType.copyFileToDisk()",
          SilverpeasException.ERROR, "attachment.EX_FILE_COPY_ERROR", ex);
    }
    return size;
  }
}
