/*
 * Copyright (C) 2000 - 2013 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection withWriter Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have recieved a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.silverpeas.attachment;

import com.silverpeas.annotation.Service;
import com.silverpeas.form.FormException;
import com.silverpeas.form.RecordSet;
import com.silverpeas.jcrutil.BasicDaoFactory;
import com.silverpeas.publicationTemplate.PublicationTemplate;
import com.silverpeas.publicationTemplate.PublicationTemplateException;
import com.silverpeas.publicationTemplate.PublicationTemplateManager;
import com.silverpeas.util.ForeignPK;
import com.silverpeas.util.StringUtil;
import com.silverpeas.util.i18n.I18NHelper;
import com.stratelia.silverpeas.silverpeasinitialize.CallBackManager;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.util.DateUtil;
import com.stratelia.webactiv.util.ResourceLocator;
import com.stratelia.webactiv.util.WAPrimaryKey;
import com.stratelia.webactiv.util.exception.SilverpeasException;
import com.stratelia.webactiv.util.exception.SilverpeasRuntimeException;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.inject.Inject;
import javax.inject.Named;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.CharEncoding;
import org.silverpeas.attachment.model.DocumentType;
import org.silverpeas.attachment.model.HistorisedDocument;
import org.silverpeas.attachment.model.SimpleDocument;
import org.silverpeas.attachment.model.SimpleDocumentPK;
import org.silverpeas.attachment.model.UnlockContext;
import org.silverpeas.attachment.notification.AttachmentNotificationService;
import org.silverpeas.attachment.repository.DocumentRepository;
import org.silverpeas.attachment.webdav.WebdavRepository;
import org.silverpeas.search.indexEngine.model.FullIndexEntry;
import org.silverpeas.search.indexEngine.model.IndexEngineProxy;
import org.silverpeas.search.indexEngine.model.IndexEntryPK;

/**
 *
 * @author ehugonnet
 */
@Service
public class SimpleDocumentService implements AttachmentService {

  private static final int STEP = 5;
  @Inject
  @Named("webdavRepository")
  private WebdavRepository webdavRepository;
  @Inject
  @Named("documentRepository")
  private DocumentRepository repository;
  private final ResourceLocator resources = new ResourceLocator(
      "org.silverpeas.util.attachment.Attachment", "");

  @Override
  public void createIndex(SimpleDocument document) {
    createIndex(document, null, null);
  }

  @Override
  public void deleteIndex(SimpleDocument document) {
    for (String lang : I18NHelper.getAllSupportedLanguages()) {
      deleteIndex(document, lang);
    }
  }

  @Override
  public void createIndex(SimpleDocument document, Date startOfVisibility, Date endOfVisibility) {
    if (resources.getBoolean("attachment.index.separately", true)) {
      String language = I18NHelper.checkLanguage(document.getLanguage());
      String objectType = "Attachment" + document.getId() + "_" + language;
      FullIndexEntry indexEntry = new FullIndexEntry(document.getInstanceId(), objectType, document.
          getForeignId());
      indexEntry.setLang(language);
      indexEntry.setCreationDate(document.getCreated());
      indexEntry.setCreationUser(document.getCreatedBy());
      if (startOfVisibility != null) {
        indexEntry.setStartDate(DateUtil.date2SQLDate(startOfVisibility));
      }
      if (endOfVisibility != null) {
        indexEntry.setEndDate(DateUtil.date2SQLDate(endOfVisibility));
      }

      indexEntry.setTitle(document.getTitle(), language);
      indexEntry.setPreview(document.getDescription(), language);
      indexEntry.setFilename(document.getFilename());
      indexEntry.addFileContent(document.getAttachmentPath(), CharEncoding.UTF_8, document.
          getContentType(), language);
      if (StringUtil.isDefined(document.getXmlFormId())) {
        updateIndexEntryWithXMLFormContent(document.getPk(), document.getXmlFormId(), indexEntry);
      }
      IndexEngineProxy.addIndexEntry(indexEntry);
    }
  }

  private void updateIndexEntryWithXMLFormContent(SimpleDocumentPK pk, String xmlFormName,
      FullIndexEntry indexEntry) {
    try {
      String objectType = "Attachment";
      PublicationTemplate pub = PublicationTemplateManager.getInstance().
          getPublicationTemplate(indexEntry.getComponent() + ":" + objectType + ":" + xmlFormName);
      RecordSet set = pub.getRecordSet();
      set.indexRecord(pk.getId(), xmlFormName, indexEntry);
    } catch (PublicationTemplateException e) {
      SilverTrace.error("attachment",
          "AttachmentService.updateIndexEntryWithXMLFormContent()", "", e);
    } catch (FormException e) {
      SilverTrace.error("attachment",
          "AttachmentService.updateIndexEntryWithXMLFormContent()", "", e);
    }
  }

  /**
   *
   * @param document
   * @param lang
   */
  private void deleteIndex(SimpleDocument document, String lang) {
    SilverTrace.debug("attachment", "DocumentService.deleteIndex", "root.MSG_GEN_ENTER_METHOD",
        document.getId());
    String language = lang;
    if (language == null) {
      language = I18NHelper.defaultLanguage;
    }
    String objectType = "Attachment" + document.getId() + '_' + language;
    IndexEntryPK indexEntry = new IndexEntryPK(document.getInstanceId(), objectType, document.
        getForeignId());
    IndexEngineProxy.removeIndexEntry(indexEntry);
  }

  @Override
  public void unindexAttachmentsOfExternalObject(WAPrimaryKey foreignKey) {
    Session session = null;
    try {
      session = BasicDaoFactory.getSystemSession();
      List<SimpleDocument> docs = repository.listDocumentsByForeignId(session, foreignKey.
          getInstanceId(), foreignKey.getId(), I18NHelper.defaultLanguage);
      for (SimpleDocument doc : docs) {
        deleteIndex(doc, I18NHelper.defaultLanguage);
      }
    } catch (RepositoryException ex) {
      throw new AttachmentException(this.getClass().getName(), SilverpeasException.ERROR, "", ex);
    } finally {
      BasicDaoFactory.logout(session);
    }
  }

  @Override
  public void addXmlForm(SimpleDocumentPK pk, String language, String xmlFormName) {
    Session session = null;
    try {
      session = BasicDaoFactory.getSystemSession();
      SimpleDocument doc = repository.findDocumentById(session, pk, language);
      doc.setXmlFormId(xmlFormName);
      repository.updateDocument(session, doc);
      session.save();
    } catch (RepositoryException ex) {
      throw new AttachmentException(this.getClass().getName(), SilverpeasException.ERROR, "", ex);
    } catch (IOException ex) {
      throw new AttachmentException(this.getClass().getName(), SilverpeasException.ERROR, "", ex);
    } finally {
      BasicDaoFactory.logout(session);
    }
  }

  /**
   * Create file attached to an object who is identified by the foreignId.
   *
   * @param document the document to be created.
   * @param content the binary content of the document.
   * @return the stored document.
   * @throws AttachmentException
   */
  @Override
  public SimpleDocument createAttachment(SimpleDocument document, InputStream content) throws
      AttachmentException {
    return createAttachment(document, content, true);
  }

  /**
   * Create file attached to an object who is identified by the foreignId.
   *
   * @param document the document to be created.
   * @param content the binary content of the document.
   * @param indexIt <code>true</code> if the document is to be indexed,  <code>false</code>
   * otherwhise.
   * @return the stored document.
   */
  @Override
  public SimpleDocument createAttachment(SimpleDocument document, InputStream content,
      boolean indexIt) {
    return createAttachment(document, content, indexIt, true);
  }

  /**
   * Create file attached to an object who is identified by the foreignId.
   *
   * @param document the document to be created.
   * @param content the binary content of the document.
   * @param indexIt <code>true</code> if the document is to be indexed,  <code>false</code>
   * otherwhise.
   * @param invokeCallback <code>true</code> if the callback methods of the components must be
   * called, <code>false</code> for ignoring thoose callbacks.
   * @return the stored document.
   */
  @Override
  public SimpleDocument createAttachment(SimpleDocument document, InputStream content,
      boolean indexIt, boolean invokeCallback) {
    Session session = null;
    try {
      session = BasicDaoFactory.getSystemSession();
      SimpleDocumentPK docPk = repository.createDocument(session, document);
      if (invokeCallback && StringUtil.isDefined(document.getCreatedBy())) {
        CallBackManager callBackManager = CallBackManager.get();
        callBackManager.invoke(CallBackManager.ACTION_ATTACHMENT_ADD, Integer.
            parseInt(document.getCreatedBy()), document.getInstanceId(),
            document.getForeignId());
      }
      session.save();
      SimpleDocument createdDocument = repository.findDocumentById(session, docPk, document.
          getLanguage());
      createdDocument.setPublicDocument(document.isPublic());
      SimpleDocument finalDocument = repository.unlock(session, createdDocument, false);
      repository.storeContent(finalDocument, content, false);
      if (indexIt) {
        createIndex(finalDocument);
      }
      return finalDocument;
    } catch (RepositoryException ex) {
      throw new AttachmentException(this.getClass().getName(), SilverpeasException.ERROR, "", ex);
    } catch (IOException ex) {
      throw new AttachmentException(this.getClass().getName(), SilverpeasException.ERROR, "", ex);
    } finally {
      BasicDaoFactory.logout(session);
    }
  }

  /**
   * Delete a given attachment.
   *
   * @param document the document to deleted.
   */
  @Override
  public void deleteAttachment(SimpleDocument document) {
    deleteAttachment(document, true);
  }

  /**
   * Delete a given attachment.
   *
   * @param document the attachmentDetail object to deleted.
   * @param invokeCallback   <code>true</code> if the callback methods of the components must be
   * called, <code>false</code> for ignoring those callbacks.
   */
  @Override
  public void deleteAttachment(SimpleDocument document, boolean invokeCallback) {
    Session session = null;
    try {
      session = BasicDaoFactory.getSystemSession();
      deleteAttachment(session, document, invokeCallback);
      session.save();
    } catch (RepositoryException ex) {
      throw new AttachmentException(this.getClass().getName(), SilverpeasException.ERROR, "", ex);
    } finally {
      BasicDaoFactory.logout(session);
    }
  }

  private void deleteAttachment(Session session, SimpleDocument document, boolean invokeCallback)
      throws RepositoryException {
    repository.fillNodeName(session, document);
    repository.deleteDocument(session, document.getPk());
    for (String lang : I18NHelper.getAllSupportedLanguages()) {
      deleteIndex(document, lang);
    }
    if (document.isOpenOfficeCompatible() && !document.isReadOnly()) {
      webdavRepository.deleteAttachmentNode(session, document);
    }
    if (invokeCallback) {
      AttachmentNotificationService notificationService = AttachmentNotificationService
          .getService();
      notificationService.notifyOnDeletionOf(document);
    }
  }

  @Override
  public SimpleDocument searchDocumentById(SimpleDocumentPK primaryKey, String lang) {
    Session session = null;
    try {
      session = BasicDaoFactory.getSystemSession();
      if (StringUtil.isDefined(primaryKey.getId()) && !StringUtil.isLong(primaryKey.getId())) {
        return repository.findDocumentById(session, primaryKey, lang);
      }
      SimpleDocument doc = repository.findDocumentByOldSilverpeasId(session, primaryKey
          .getComponentName(), primaryKey.getOldSilverpeasId(), false, lang);
      if (doc == null) {
        doc = repository.findDocumentByOldSilverpeasId(session, primaryKey.getComponentName(),
            primaryKey.getOldSilverpeasId(), true, lang);
      }
      return doc;
    } catch (RepositoryException ex) {
      throw new AttachmentException(this.getClass().getName(), SilverpeasException.ERROR, "", ex);
    } finally {
      BasicDaoFactory.logout(session);
    }
  }

  @Override
  public List<SimpleDocument> listAllDocumentsByForeignKey(WAPrimaryKey foreignKey, String lang) {
    Session session = null;
    try {
      session = BasicDaoFactory.getSystemSession();
      return repository.listAllDocumentsByForeignId(session, foreignKey.getInstanceId(), foreignKey.
          getId(), lang);
    } catch (RepositoryException ex) {
      throw new AttachmentException(this.getClass().getName(), SilverpeasException.ERROR, "", ex);
    } finally {
      BasicDaoFactory.logout(session);
    }
  }

  @Override
  public List<SimpleDocument> listDocumentsByForeignKey(WAPrimaryKey foreignKey, String lang) {
    Session session = null;
    try {
      session = BasicDaoFactory.getSystemSession();
      return repository.listDocumentsByForeignId(session, foreignKey.getInstanceId(), foreignKey.
          getId(), lang);
    } catch (RepositoryException ex) {
      throw new AttachmentException(this.getClass().getName(), SilverpeasException.ERROR, "", ex);
    } finally {
      BasicDaoFactory.logout(session);
    }
  }

  @Override
  public void updateAttachment(SimpleDocument document, boolean indexIt, boolean invokeCallback) {
    Session session = null;
    try {
      session = BasicDaoFactory.getSystemSession();
      SimpleDocument oldAttachment = repository.findDocumentById(session, document.getPk(),
          document.getLanguage());
      repository.fillNodeName(session, document);
      repository.updateDocument(session, document);
      if (!oldAttachment.isVersioned()) {
        if (document.isOpenOfficeCompatible() && document.isReadOnly()) {
          // le fichier est renommé
          if (!oldAttachment.getFilename().equals(document.getFilename())) {
            webdavRepository.deleteAttachmentNode(session, oldAttachment);
            webdavRepository.createAttachmentNode(session, document);
          } else {
            webdavRepository.updateAttachment(session, document);
          }
        }
      }
      String userId = document.getUpdatedBy();
      if ((userId != null) && (userId.length() > 0) && invokeCallback) {
        CallBackManager callBackManager = CallBackManager.get();
        callBackManager.invoke(CallBackManager.ACTION_ATTACHMENT_UPDATE, Integer.parseInt(userId),
            document.getInstanceId(), document.getForeignId());
      }
      if (indexIt) {
        createIndex(document);
      }
      session.save();
    } catch (RepositoryException ex) {
      throw new AttachmentException(this.getClass().getName(), SilverpeasException.ERROR, "", ex);
    } catch (IOException ex) {
      throw new AttachmentException(this.getClass().getName(), SilverpeasException.ERROR, "", ex);
    } finally {
      BasicDaoFactory.logout(session);
    }
  }

  @Override
  public void updateAttachment(SimpleDocument document, InputStream in, boolean indexIt,
      boolean invokeCallback) {
    Session session = null;
    try {
      session = BasicDaoFactory.getSystemSession();
      String owner = document.getEditedBy();
      if (!StringUtil.isDefined(owner)) {
        owner = document.getUpdatedBy();
      }
      boolean checkinRequired = repository.lock(session, document, owner);
      repository.updateDocument(session, document);
      repository.addContent(session, document.getPk(), document.getFile());
      repository.fillNodeName(session, document);
      SimpleDocument finalDocument = document;
      if (checkinRequired) {
        finalDocument = repository.unlock(session, document, false);
      }
      repository.storeContent(finalDocument, in, true);
      if (document.isOpenOfficeCompatible() && finalDocument.isReadOnly()) {
        webdavRepository.updateNodeAttachment(session, finalDocument);
      }
      repository.duplicateContent(document, finalDocument);
      String userId = finalDocument.getUpdatedBy();
      if (StringUtil.isDefined(userId) && invokeCallback && finalDocument.isPublic()) {
        CallBackManager callBackManager = CallBackManager.get();
        callBackManager.invoke(CallBackManager.ACTION_ATTACHMENT_UPDATE, Integer.parseInt(userId),
            finalDocument.getInstanceId(), finalDocument.getForeignId());
      }
      if (indexIt) {
        createIndex(finalDocument);
      }
      session.save();
    } catch (RepositoryException ex) {
      throw new AttachmentException(this.getClass().getName(), SilverpeasException.ERROR, "", ex);
    } catch (IOException ex) {
      throw new AttachmentException(this.getClass().getName(), SilverpeasException.ERROR, "", ex);
    } finally {
      BasicDaoFactory.logout(session);
    }
  }

  @Override
  public void removeContent(SimpleDocument document, String lang, boolean invokeCallback) {
    Session session = null;
    try {
      session = BasicDaoFactory.getSystemSession();
      boolean requireLock = repository.lock(session, document, document.getEditedBy());
      repository.removeContent(session, document.getPk(), lang);
      if (document.isOpenOfficeCompatible() && document.isReadOnly()) {
        webdavRepository.deleteAttachmentNode(session, document);
      }
      String userId = document.getCreatedBy();
      if ((userId != null) && (userId.length() > 0) && invokeCallback) {
        CallBackManager callBackManager = CallBackManager.get();
        callBackManager.invoke(CallBackManager.ACTION_ATTACHMENT_UPDATE, Integer.parseInt(userId),
            document.getInstanceId(), document.getForeignId());
      }
      deleteIndex(document, document.getLanguage());
      session.save();
      SimpleDocument finalDocument = document;
      if (requireLock) {
        finalDocument = repository.unlock(session, document, false);
        repository.duplicateContent(document, finalDocument);
      }
      finalDocument.setLanguage(lang);
      FileUtils.deleteQuietly(new File(finalDocument.getAttachmentPath()));
    } catch (RepositoryException ex) {
      throw new AttachmentException(this.getClass().getName(), SilverpeasException.ERROR, "", ex);
    } catch (IOException ex) {
      throw new AttachmentException(this.getClass().getName(), SilverpeasException.ERROR, "", ex);
    } finally {
      BasicDaoFactory.logout(session);
    }
  }

  /**
   * Clone the attachment.
   *
   * @param original
   * @param foreignCloneId
   * @return
   */
  @Override
  public SimpleDocumentPK cloneDocument(SimpleDocument original, String foreignCloneId) {
    Session session = null;
    InputStream in = null;
    try {
      in = new FileInputStream(original.getAttachmentPath());
      session = BasicDaoFactory.getSystemSession();
      SimpleDocumentPK clonePk = repository.copyDocument(session, original, new ForeignPK(
          foreignCloneId, original.getInstanceId()));
      SimpleDocument clone = repository.findDocumentById(session, clonePk, null);
      repository.copyMultilangContent(original, clone);
      repository.setClone(session, original, clone);
      session.save();
      return clonePk;
    } catch (RepositoryException ex) {
      throw new AttachmentException(this.getClass().getName(), SilverpeasException.ERROR, "", ex);
    } catch (IOException ex) {
      throw new AttachmentException(this.getClass().getName(), SilverpeasException.ERROR, "", ex);
    } finally {
      IOUtils.closeQuietly(in);
      BasicDaoFactory.logout(session);
    }
  }

  /**
   * Clone the attachment.
   *
   * @param original
   * @param targetPk
   * @return
   */
  @Override
  public SimpleDocumentPK copyDocument(SimpleDocument original, ForeignPK targetPk) {
    Session session = null;
    try {
      session = BasicDaoFactory.getSystemSession();
      SimpleDocumentPK copyPk;
      if (original instanceof HistorisedDocument) {
        copyPk = repository.copyDocument(session, (HistorisedDocument) original, targetPk);
      } else {
        copyPk = repository.copyDocument(session, original, targetPk);
      }
      session.save();
      SimpleDocument copy = repository.findDocumentById(session, copyPk, null);
      if (original.isVersioned()) {
        repository.copyFullContent(original, copy);
      } else {
        repository.copyMultilangContent(original, copy);
      }

      return copyPk;
    } catch (RepositoryException ex) {
      throw new AttachmentException(this.getClass().getName(), SilverpeasException.ERROR, "", ex);
    } catch (IOException ex) {
      throw new AttachmentException(this.getClass().getName(), SilverpeasException.ERROR, "", ex);
    } finally {
      BasicDaoFactory.logout(session);
    }
  }

  /**
   * Reorder the attachments according to the order in the list.
   *
   * @param pks
   * @throws AttachmentException
   */
  @Override
  public void reorderAttachments(List<SimpleDocumentPK> pks) throws AttachmentException {
    Session session = null;
    try {
      session = BasicDaoFactory.getSystemSession();
      int i = STEP;
      for (SimpleDocumentPK pk : pks) {
        SimpleDocument doc = repository.findDocumentById(session, pk, null);
        doc.setOrder(i);
        repository.setOrder(session, doc);
        i += STEP;
      }
      session.save();
    } catch (RepositoryException ex) {
      throw new AttachmentException(this.getClass().getName(), SilverpeasException.ERROR, "", ex);
    } finally {
      BasicDaoFactory.logout(session);
    }
  }

  /**
   * Reorder the attachments according to the order in the list.
   *
   *
   * @param documents
   * @throws AttachmentException
   */
  @Override
  public void reorderDocuments(List<SimpleDocument> documents) throws AttachmentException {
    Session session = null;
    try {
      session = BasicDaoFactory.getSystemSession();
      int i = STEP;
      for (SimpleDocument doc : documents) {
        doc.setOrder(i);
        repository.setOrder(session, doc);
        i += STEP;
      }
      session.save();
    } catch (RepositoryException ex) {
      throw new AttachmentException(this.getClass().getName(), SilverpeasException.ERROR, "", ex);
    } finally {
      BasicDaoFactory.logout(session);
    }
  }

  @Override
  public void getBinaryContent(OutputStream output, SimpleDocumentPK pk, String lang) {
    Session session = null;
    InputStream in = null;
    try {
      session = BasicDaoFactory.getSystemSession();
      in = repository.getContent(session, pk, lang);
      IOUtils.copy(in, output);
    } catch (IOException ex) {
      throw new AttachmentException(this.getClass().getName(), SilverpeasException.ERROR, "", ex);
    } catch (RepositoryException ex) {
      throw new AttachmentException(this.getClass().getName(), SilverpeasException.ERROR, "", ex);
    } finally {
      IOUtils.closeQuietly(in);
      BasicDaoFactory.logout(session);
    }
  }

  @Override
  public List<SimpleDocument> listDocumentsRequiringWarning(Date alertDate, String language) {
    Session session = null;
    try {
      session = BasicDaoFactory.getSystemSession();
      return repository.listDocumentsRequiringWarning(session, alertDate, language);
    } catch (RepositoryException ex) {
      throw new AttachmentException(this.getClass().getName(), SilverpeasException.ERROR, "", ex);
    } finally {
      BasicDaoFactory.logout(session);
    }
  }

  @Override
  public List<SimpleDocument> listExpiringDocuments(Date expiryDate, String language) {
    Session session = null;
    try {
      session = BasicDaoFactory.getSystemSession();
      return repository.listExpiringDocuments(session, expiryDate, language);
    } catch (RepositoryException ex) {
      throw new AttachmentException(this.getClass().getName(), SilverpeasException.ERROR, "", ex);
    } finally {
      BasicDaoFactory.logout(session);
    }
  }

  @Override
  public List<SimpleDocument> listDocumentsToUnlock(Date expiryDate, String language) {
    Session session = null;
    try {
      session = BasicDaoFactory.getSystemSession();
      return repository.listDocumentsToUnlock(session, expiryDate, language);
    } catch (RepositoryException ex) {
      throw new AttachmentException(this.getClass().getName(), SilverpeasException.ERROR, "", ex);
    } finally {
      BasicDaoFactory.logout(session);
    }
  }

  @Override
  public void updateAttachment(SimpleDocument document, File content, boolean indexIt,
      boolean invokeCallback) {
    InputStream in = null;
    try {
      in = new BufferedInputStream(new FileInputStream(content));
      updateAttachment(document, in, indexIt, invokeCallback);
    } catch (FileNotFoundException ex) {
      throw new AttachmentException(this.getClass().getName(), SilverpeasException.ERROR, "", ex);
    } finally {
      IOUtils.closeQuietly(in);
    }
  }

  @Override
  public void getBinaryContent(File file, SimpleDocumentPK pk, String lang) {
    OutputStream out = null;
    try {
      out = new BufferedOutputStream(new FileOutputStream(file));
      getBinaryContent(out, pk, lang);
    } catch (FileNotFoundException ex) {
      throw new AttachmentException(this.getClass().getName(), SilverpeasException.ERROR, "", ex);
    } finally {
      IOUtils.closeQuietly(out);
    }
  }

  @Override
  public SimpleDocument createAttachment(SimpleDocument document, File content) throws
      AttachmentException {
    return createAttachment(document, content, true);
  }

  @Override
  public SimpleDocument createAttachment(SimpleDocument document, File content, boolean indexIt) {
    return createAttachment(document, content, indexIt, true);
  }

  @Override
  public SimpleDocument createAttachment(SimpleDocument document, File content, boolean indexIt,
      boolean invokeCallback) {
    InputStream in = null;
    try {
      in = new BufferedInputStream(new FileInputStream(content));
      return createAttachment(document, in, indexIt, invokeCallback);
    } catch (FileNotFoundException ex) {
      throw new AttachmentException(this.getClass().getName(), SilverpeasException.ERROR, "", ex);
    } finally {
      IOUtils.closeQuietly(in);
    }
  }

  /**
   * Release a locked file.
   *
   * @param context : the unlock parameters.
   * @return false if the file is locked - true if the unlock succeeded.
   * @throws AttachmentException
   */
  @Override
  public boolean unlock(UnlockContext context) {
    Session session = null;
    try {
      session = BasicDaoFactory.getSystemSession();
      SimpleDocument document = repository.findDocumentById(session, new SimpleDocumentPK(
          context.getAttachmentId()), context.getLang());
      if (document.isOpenOfficeCompatible() && !context.isForce() && webdavRepository.isNodeLocked(
          session, document)) {
        return false;
      }
      if (!context.isForce() && document.isReadOnly() && !document.getEditedBy().equals(context.
          getUserId())) {
        return false;
      }

      boolean invokeCallback = false;
      if (context.isWebdav() || context.isUpload()) {
        String workerId = document.getEditedBy();
        document.setUpdated(new Date());
        document.setUpdatedBy(workerId);
        invokeCallback = true;
      }
      document.setPublicDocument(context.isPublicVersion());
      document.setComment(context.getComment());
      SimpleDocument finalDocument = repository.unlock(session, document, context.isForce());
      if (document.isOpenOfficeCompatible() && !context.isUpload() && context.isWebdav()) {
        webdavRepository.updateAttachment(session, finalDocument);
      } else if (finalDocument.isOpenOfficeCompatible() && (context.isUpload() || !context.
          isWebdav())) {
        webdavRepository.deleteAttachmentNode(session, finalDocument);
      } else {
        File file = new File(finalDocument.getAttachmentPath());
        if (!file.exists() && !context.isForce()) {
          repository.duplicateContent(document, finalDocument);
        }
      }
      session.save();
      if (document.isPublic()) {
        String userId = context.getUserId();
        if (StringUtil.isDefined(userId) && invokeCallback) {
          CallBackManager callBackManager = CallBackManager.get();
          callBackManager.invoke(CallBackManager.ACTION_ATTACHMENT_UPDATE, Integer.parseInt(userId),
              finalDocument.getInstanceId(), finalDocument.getForeignId());
        }
      }
    } catch (IOException e) {
      throw new AttachmentException("AttachmentService.unlock()",
          SilverpeasRuntimeException.ERROR, "attachment.CHECKIN_FAILED", e);
    } catch (RepositoryException e) {
      throw new AttachmentException("AttachmentService.unlock()",
          SilverpeasRuntimeException.ERROR, "attachment.CHECKIN_FAILED", e);
    } finally {
      BasicDaoFactory.logout(session);
    }
    return true;
  }

  /**
   * Lock a file so it can be edited by an user.
   *
   * @param attachmentId
   * @param userId
   * @param language
   * @return false if the attachment is already checkout - true if the attachment was successfully
   * checked out.
   */
  @Override
  public boolean lock(String attachmentId, String userId, String language) {
    Session session = null;
    try {
      SimpleDocumentPK pk = new SimpleDocumentPK(attachmentId);
      session = BasicDaoFactory.getSystemSession();
      SimpleDocument document = repository.findDocumentById(session, pk, language);
      if (document.isReadOnly()) {
        return document.getEditedBy().equals(userId);
      }
      repository.lock(session, document, document.getEditedBy());
      document.edit(userId);
      if (document.isOpenOfficeCompatible()) {
        webdavRepository.createAttachmentNode(session, document);
      }
      updateAttachment(session, document, false, false);
      session.save();
      return true;
    } catch (RepositoryException ex) {
      throw new AttachmentException(this.getClass().getName(), SilverpeasException.ERROR, "", ex);
    } catch (IOException ex) {
      throw new AttachmentException(this.getClass().getName(), SilverpeasException.ERROR, "", ex);
    } finally {
      BasicDaoFactory.logout(session);
    }
  }

  private void updateAttachment(Session session, SimpleDocument document, boolean indexIt,
      boolean invokeCallback) throws RepositoryException, IOException {
    SimpleDocument oldAttachment = repository.findDocumentById(session, document.getPk(),
        document.getLanguage());
    repository.updateDocument(session, document);
    if (document.isOpenOfficeCompatible() && document.isReadOnly()) {
      if (!oldAttachment.getFilename().equals(document.getFilename())) {
        webdavRepository.deleteAttachmentNode(session, oldAttachment);
        webdavRepository.createAttachmentNode(session, document);
      } else {
        webdavRepository.updateNodeAttachment(session, document);
      }
    }
    String userId = document.getCreatedBy();
    if (StringUtil.isDefined(userId) && invokeCallback) {
      CallBackManager callBackManager = CallBackManager.get();
      callBackManager.invoke(CallBackManager.ACTION_ATTACHMENT_UPDATE, Integer.parseInt(userId),
          document.getInstanceId(), document.getForeignId());
    }
    if (indexIt) {
      createIndex(document);
    }
  }

  @Override
  public SimpleDocumentPK changeVersionState(SimpleDocumentPK pk, String comment) {
    Session session = null;
    try {
      session = BasicDaoFactory.getSystemSession();
      SimpleDocumentPK updatedPk = repository.changeVersionState(session, pk, comment);
      session.save();
      return updatedPk;
    } catch (RepositoryException ex) {
      throw new AttachmentException(this.getClass().getName(), SilverpeasException.ERROR, "", ex);
    } catch (IOException ex) {
      throw new AttachmentException(this.getClass().getName(), SilverpeasException.ERROR, "", ex);
    } finally {
      BasicDaoFactory.logout(session);
    }
  }

  @Override
  public SimpleDocument findExistingDocument(SimpleDocumentPK pk, String fileName, ForeignPK foreign,
      String lang) {
    List<SimpleDocument> exisitingsDocuments = listDocumentsByForeignKey(foreign, lang);
    SimpleDocument document = searchDocumentById(pk, lang);
    if (document == null) {
      for (SimpleDocument doc : exisitingsDocuments) {
        if (doc.getFilename().equalsIgnoreCase(fileName)) {
          return doc;
        }
      }
    }
    return document;
  }

  @Override
  public List<SimpleDocument> listDocumentsByForeignKeyAndType(WAPrimaryKey foreignKey,
      DocumentType type, String lang) {
    Session session = null;
    try {
      session = BasicDaoFactory.getSystemSession();
      return repository.listDocumentsByForeignIdAndType(session, foreignKey.getInstanceId(),
          foreignKey.
          getId(), type, lang);
    } catch (RepositoryException ex) {
      throw new AttachmentException(this.getClass().getName(), SilverpeasException.ERROR, "", ex);
    } finally {
      BasicDaoFactory.logout(session);
    }
  }

  @Override
  public List<SimpleDocument> listDocumentsLockedByUser(String usedId, String language) {
    Session session = null;
    try {
      session = BasicDaoFactory.getSystemSession();
      return repository.listDocumentsLockedByUser(session, usedId, language);
    } catch (RepositoryException ex) {
      throw new AttachmentException(this.getClass().getName(), SilverpeasException.ERROR, "", ex);
    } finally {
      BasicDaoFactory.logout(session);
    }
  }

  @Override
  public SimpleDocumentPK moveDocument(SimpleDocument document, ForeignPK destination) {
    Session session = null;
    try {
      session = BasicDaoFactory.getSystemSession();
      SimpleDocumentPK pk = repository.moveDocument(session, document, destination);
      SimpleDocument moveDoc = repository.findDocumentById(session, pk, null);
      repository.moveMultilangContent(document, moveDoc);
      session.save();
      return pk;
    } catch (RepositoryException ex) {
      throw new AttachmentException(this.getClass().getName(), SilverpeasException.ERROR, "", ex);
    } catch (IOException ex) {
      throw new AttachmentException(this.getClass().getName(), SilverpeasException.ERROR, "", ex);
    } finally {
      BasicDaoFactory.logout(session);
    }
  }

  @Override
  public void updateIndexEntryWithDocuments(FullIndexEntry indexEntry) {
    if (resources.getBoolean("attachment.index.incorporated", true)) {
      if (!indexEntry.getObjectType().startsWith("Attachment")) {
        ForeignPK pk = new ForeignPK(indexEntry.getObjectId(), indexEntry.getComponent());
        List<SimpleDocument> documents = listDocumentsByForeignKey(pk, indexEntry.getLang());
        for (SimpleDocument currentDocument : documents) {
          SimpleDocument version = currentDocument.getLastPublicVersion();
          if (version != null) {
            indexEntry.addFileContent(version.getAttachmentPath(), CharEncoding.UTF_8, version.
                getContentType(), indexEntry.getLang());
          }
        }
      }
    }
  }

  @Override
  public void indexAllDocuments(WAPrimaryKey fk, Date startOfVisibilityPeriod,
      Date endOfVisibilityPeriod) {
    List<SimpleDocument> documents = listAllDocumentsByForeignKey(fk, null);
    for (SimpleDocument currentDocument : documents) {
      createIndex(currentDocument, startOfVisibilityPeriod, endOfVisibilityPeriod);
    }
  }

  @Override
  public Map<String, String> mergeDocuments(ForeignPK originalForeignKey, ForeignPK cloneForeignKey,
      DocumentType type) {
    Session session = null;
    try {
      session = BasicDaoFactory.getSystemSession();
      // On part des fichiers d'origine
      List<SimpleDocument> attachments = listDocumentsByForeignKeyAndType(originalForeignKey, type,
          null);
      Map<String, SimpleDocument> clones = listDocumentsOfClone(cloneForeignKey, type, null);
      Map<String, String> ids = new HashMap<String, String>(clones.size());
      // looking for updates and deletions
      for (SimpleDocument attachment : attachments) {
        if (clones.containsKey(attachment.getId())) {
          SimpleDocument clone = clones.get(attachment.getId());
          // the file already exists
          // elements of clone must be merged on original
          repository.mergeAttachment(session, attachment, clone);
          repository.copyMultilangContent(clone, attachment);
          repository.deleteDocument(session, clone.getPk());
          ids.put(clone.getId(), attachment.getId());
          // remove it from clones list
          clones.remove(attachment.getId());
        } else {
          // the file have been removed
          deleteAttachment(session, attachment, true);
        }
      }

      if (!clones.isEmpty()) {
        for (SimpleDocument clone : clones.values()) {
          clone.setCloneId(null);
          clone.setForeignId(originalForeignKey.getId());
          updateAttachment(clone, false, false);
        }
      }
      session.save();
      return ids;
    } catch (RepositoryException ex) {
      throw new AttachmentException(this.getClass().getName(), SilverpeasException.ERROR, "", ex);
    } catch (IOException ex) {
      throw new AttachmentException(this.getClass().getName(), SilverpeasException.ERROR, "", ex);
    } finally {
      BasicDaoFactory.logout(session);
    }

  }

  private Map<String, SimpleDocument> listDocumentsOfClone(ForeignPK foreignPk, DocumentType type,
      String lang) {
    List<SimpleDocument> documents = listDocumentsByForeignKeyAndType(foreignPk, type, lang);
    Map<String, SimpleDocument> result = new HashMap<String, SimpleDocument>(documents.size());
    for (SimpleDocument doc : documents) {
      if (StringUtil.isDefined(doc.getCloneId())) {
        result.put(doc.getCloneId(), doc);
      } else {
        result.put(doc.getId(), doc);
      }
    }
    return result;
  }

  @Override
  public void switchComponentBehaviour(String componentId, boolean toVersionning) {
    Session session = null;
    try {
      session = BasicDaoFactory.getSystemSession();
      // On part des fichiers d'origine
      List<SimpleDocument> attachments = repository.listDocumentsByComponentdAndType(session,
          componentId, DocumentType.attachment, I18NHelper.defaultLanguage);
      for (SimpleDocument attachment : attachments) {
        if (attachment.isVersioned() != toVersionning) {
          repository.changeVersionState(session, attachment.getPk(), "");
        }
      }
      session.save();
    } catch (RepositoryException ex) {
      throw new AttachmentException(this.getClass().getName(), SilverpeasException.ERROR, "", ex);
    } catch (IOException ex) {
      throw new AttachmentException(this.getClass().getName(), SilverpeasException.ERROR, "", ex);
    } finally {
      BasicDaoFactory.logout(session);
    }

  }
}
