/*
 * Copyright (C) 2000 - 2012 Silverpeas
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
 * "http://www.silverpeas.org/legal/licensing"
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

import java.io.*;
import java.util.Date;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;
import javax.jcr.Binary;
import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.CharEncoding;

import org.silverpeas.attachment.model.SimpleDocument;
import org.silverpeas.attachment.model.SimpleDocumentPK;
import org.silverpeas.attachment.model.UnlockContext;
import org.silverpeas.attachment.repository.DocumentRepository;
import org.silverpeas.attachment.webdav.WebdavRepository;

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
import com.stratelia.webactiv.util.indexEngine.model.FullIndexEntry;
import com.stratelia.webactiv.util.indexEngine.model.IndexEngineProxy;
import com.stratelia.webactiv.util.indexEngine.model.IndexEntryPK;

import static javax.jcr.Property.JCR_CONTENT;
import static javax.jcr.Property.JCR_DATA;

/**
 *
 * @author ehugonnet
 */
@Named("simpleDocumentService")
public class SimpleDocumentService implements AttachmentService {

  @Inject
  @Named("webdavRepository")
  private WebdavRepository webdavRepository;
  @Inject
  @Named("documentRepository")
  private DocumentRepository repository;
  private final ResourceLocator resources = new ResourceLocator(
      "com.stratelia.webactiv.util.attachment.Attachment", "");

  @Override
  public void createIndex(SimpleDocument document) {
    createIndex(document, null, null);
  }

  @Override
  public void createIndex(SimpleDocument document, Date startOfVisibility, Date endOfVisibility) {
    String language = document.getLanguage();
    if (!StringUtil.isDefined(language)) {
      language = I18NHelper.defaultLanguage;
    }
    String objectType = "Attachment" + document.getId();
    if (I18NHelper.isI18N) {
      objectType += "_" + language;
    }
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
    String title = document.getTitle();
    if (StringUtil.isDefined(title)) {
      indexEntry.setKeywords(title, language);
    }
    indexEntry.addFileContent(document.getAttachmentPath(), CharEncoding.UTF_8, document.
        getContentType(), language);

    if (StringUtil.isDefined(document.getXmlFormId())) {
      updateIndexEntryWithXMLFormContent(document.getPk(), document.getXmlFormId(), indexEntry);
    }
    IndexEngineProxy.addIndexEntry(indexEntry);
  }

  private void updateIndexEntryWithXMLFormContent(SimpleDocumentPK pk, String xmlFormName,
      FullIndexEntry indexEntry) {
    SilverTrace.info("attachment", "AttachmentController.updateIndexEntryWithXMLFormContent()",
        "root.MSG_GEN_ENTER_METHOD", "indexEntry = " + indexEntry.toString());
    try {
      String objectType = "Attachment";
      PublicationTemplate pub = PublicationTemplateManager.getInstance().
          getPublicationTemplate(indexEntry.getComponent() + ":" + objectType + ":" + xmlFormName);
      RecordSet set = pub.getRecordSet();
      set.indexRecord(pk.getId(), xmlFormName, indexEntry);
    } catch (PublicationTemplateException e) {
      SilverTrace.error("attachment",
          "AttachmentController.updateIndexEntryWithXMLFormContent()", "", e);
    } catch (FormException e) {
      SilverTrace.error("attachment",
          "AttachmentController.updateIndexEntryWithXMLFormContent()", "", e);
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
  public void updateIndexEntryWithAttachments(FullIndexEntry indexEntry) {
    if (resources.getBoolean("attachment.index.incorporated", true)) {
      if (!indexEntry.getObjectType().startsWith("Attachment")) {
        WAPrimaryKey pk = new ForeignPK(indexEntry.getObjectId(), indexEntry.getComponent());
        List<SimpleDocument> attachments = searchAttachmentsByExternalObject(pk, indexEntry.
            getLang());
        for (SimpleDocument attachment : attachments) {
          indexEntry.addFileContent(attachment.getAttachmentPath(), CharEncoding.UTF_8,
              attachment.getContentType(), indexEntry.getLang());
        }
      }
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
      SimpleDocumentPK docPk = repository.createDocument(session, document, content);
      SimpleDocument currentDocument = repository.findDocumentById(session, docPk, document.
          getLanguage());
      storeContent(session, currentDocument);
      if (invokeCallback && StringUtil.isDefined(currentDocument.getCreatedBy())) {
        CallBackManager callBackManager = CallBackManager.get();
        callBackManager.invoke(CallBackManager.ACTION_ATTACHMENT_ADD, Integer.
            parseInt(currentDocument.getCreatedBy()), currentDocument.getInstanceId(),
            currentDocument.getForeignId());
      }
      session.save();
      repository.unlock(session, document, false);
      if (indexIt) {
        createIndex(currentDocument);
      }
      return currentDocument;
    } catch (RepositoryException ex) {
      throw new AttachmentException(this.getClass().getName(), SilverpeasException.ERROR, "", ex);
    } catch (IOException ex) {
      throw new AttachmentException(this.getClass().getName(), SilverpeasException.ERROR, "", ex);
    } finally {
      BasicDaoFactory.logout(session);
    }
  }

  private void storeContent(Session session, SimpleDocument document) throws RepositoryException,
      IOException {
    File file = new File(document.getAttachmentPath());
    Node docNode = session.getNodeByIdentifier(document.getId());
    String fileNodeName = SimpleDocument.FILE_PREFIX + document.getLanguage();
    if (docNode.hasNode(fileNodeName)) {
      Node fileNode = docNode.getNode(fileNodeName);
      if (fileNode.hasNode(JCR_CONTENT)) {
        Node contentNode = fileNode.getNode(JCR_CONTENT);
        Binary binary = contentNode.getProperty(JCR_DATA).getBinary();
        FileUtils.copyInputStreamToFile(binary.getStream(), file);
        binary.dispose();
      }
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
      repository.deleteDocument(session, document.getPk());
      FileUtils.deleteQuietly(new File(document.getAttachmentPath()));
      for (String lang : I18NHelper.getAllSupportedLanguages()) {
        deleteIndex(document, lang);
      }
      if (document.isOpenOfficeCompatible() && !document.isReadOnly()) {
        webdavRepository.deleteAttachmentNode(session, document);
      }
      if (invokeCallback) {
        int authorId = -1;
        if (StringUtil.isDefined(document.getCreatedBy())) {
          authorId = Integer.parseInt(document.getCreatedBy());
        }
        CallBackManager callBackManager = CallBackManager.get();
        callBackManager.invoke(CallBackManager.ACTION_ATTACHMENT_REMOVE, authorId, document.
            getInstanceId(), document);
      }
      session.save();
    } catch (RepositoryException ex) {
      throw new AttachmentException(this.getClass().getName(), SilverpeasException.ERROR, "", ex);
    } finally {
      BasicDaoFactory.logout(session);
    }
  }

  @Override
  public SimpleDocument searchAttachmentById(SimpleDocumentPK primaryKey, String lang) {
    Session session = null;
    try {
      session = BasicDaoFactory.getSystemSession();
      if (StringUtil.isDefined(primaryKey.getId())) {
        return repository.findDocumentById(session, primaryKey, lang);
      }
      return repository.findDocumentByOldSilverpeasId(session, primaryKey.getComponentName(),
          primaryKey.getOldSilverpeasId(), false, lang);
    } catch (RepositoryException ex) {
      throw new AttachmentException(this.getClass().getName(), SilverpeasException.ERROR, "", ex);
    } finally {
      BasicDaoFactory.logout(session);
    }
  }

  @Override
  public List<SimpleDocument> searchAttachmentsByExternalObject(WAPrimaryKey foreignKey, String lang) {
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
      repository.updateDocument(session, document);
      if (document.isOpenOfficeCompatible() && document.isReadOnly()) {
        // le fichier est renommé
        if (!oldAttachment.getFilename().equals(document.getFilename())) {
          webdavRepository.deleteAttachmentNode(session, oldAttachment);
          webdavRepository.createAttachmentNode(session, document);
        } else {
          webdavRepository.updateNodeAttachment(session, document);
        }
      }
      String userId = document.getCreatedBy();
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
    } finally {
      BasicDaoFactory.logout(session);
    }
  }

  @Override
  public void addContent(SimpleDocument document, InputStream in, boolean indexIt,
      boolean invokeCallback) {
    Session session = null;
    try {
      session = BasicDaoFactory.getSystemSession();
      repository.addContent(session, document.getPk(), document.getFile(), in);
      storeContent(session, document);
      if (document.isOpenOfficeCompatible() && document.isReadOnly()) {
        webdavRepository.updateNodeAttachment(session, document);
      }
      String userId = document.getCreatedBy();
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
  public void removeContent(SimpleDocument document, String lang, boolean invokeCallback) {
    Session session = null;
    try {
      session = BasicDaoFactory.getSystemSession();
      boolean requireLock = repository.lock(session, document);
      repository.removeContent(session, document.getPk(), lang);
      FileUtils.deleteQuietly(new File(document.getDirectoryPath(lang)));
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
      if(requireLock) {
        repository.unlock(session, document, false);
      }
    } catch (RepositoryException ex) {
      throw new AttachmentException(this.getClass().getName(), SilverpeasException.ERROR, "", ex);
    } finally {
      BasicDaoFactory.logout(session);
    }
  }

  /**
   * Clone the attchments
   *
   * @param original
   * @param foreignCloneId
   * @return
   */
  @Override
  public SimpleDocumentPK cloneDocument(SimpleDocument original, String foreignCloneId) {
    Session session = null;
    try {
      session = BasicDaoFactory.getSystemSession();
      SimpleDocumentPK clonePk = repository.copyDocument(session, original, new ForeignPK(
          foreignCloneId, original.getInstanceId()));
      SimpleDocument clone = repository.findDocumentById(session, clonePk, null);
      original.setCloneId(clonePk.getId());
      repository.updateDocument(session, original);
      storeContent(session, clone);
      session.save();
      return clonePk;
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
      int i = 5;
      for (SimpleDocumentPK pk : pks) {
        SimpleDocument doc = repository.findDocumentById(session, pk, null);
        doc.setOrder(i);
        repository.updateDocument(session, doc);
        i = i + 5;
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
      int i = 5;
      for (SimpleDocument doc : documents) {
        doc.setOrder(i);
        repository.updateDocument(session, doc);
        i = i + 5;
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
  public void addContent(SimpleDocument document, File content, boolean indexIt,
      boolean invokeCallback) {
    InputStream in = null;
    try {
      in = new BufferedInputStream(new FileInputStream(content));
      addContent(document, in, indexIt, invokeCallback);
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
      SilverTrace.debug("attachment", "AttachmentController.checkinOfficeFile()",
          "root.MSG_GEN_ENTER_METHOD", "attachmentId = " + context.getAttachmentId());

      SimpleDocument document = repository.findDocumentById(session, new SimpleDocumentPK(
          context.getAttachmentId()), context.getLang());
      if (document.isOpenOfficeCompatible() && !context.isForce() && webdavRepository.isNodeLocked(
          session,
          document)) {
        SilverTrace.warn("attachment", "AttachmentController.checkinOfficeFile()",
            "attachment.NODE_LOCKED");
        return false;
      }
      if (!context.isForce() && document.isReadOnly() && !document.getEditedBy().equals(context.
          getUserId())) {
        SilverTrace.warn("attachment", "AttachmentController.checkinOfficeFile()",
            "attachment.INCORRECT_USER");
        return false;
      }

      boolean invokeCallback = false;
      if (context.isWebdav() || context.isUpload()) {
        String workerId = document.getEditedBy();
        document.setUpdated(new Date());
        document.setUpdatedBy(workerId);
        invokeCallback = true;
      }
      if (document.isOpenOfficeCompatible() && !context.isUpload() && context.isWebdav()) {
        webdavRepository.updateAttachment(session, document);
        storeContent(session, document);
      } else if (document.isOpenOfficeCompatible() && (context.isUpload() || !context.isWebdav())) {
        webdavRepository.deleteAttachmentNode(session, document);
      }
      // Remove workerId from this attachment
      document.release();
      session.save();
      updateAttachment(document, false, invokeCallback);
      repository.unlock(session, document, context.isForce());
    } catch (Exception e) {
      SilverTrace.error("attachment", "AttachmentController.checkinOfficeFile()",
          "attachment.CHECKIN_FAILED", e);
      throw new AttachmentException("AttachmentController.checkinOfficeFile()",
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
      repository.lock(session, document);
      document.edit(userId);
      if (document.isOpenOfficeCompatible()) {
        webdavRepository.createAttachmentNode(session, document);
      }
      updateAttachment(session, document, false, false);
      session.save();
      return true;
    } catch (RepositoryException ex) {
      throw new AttachmentException(this.getClass().getName(), SilverpeasException.ERROR, "", ex);
    } finally {
      BasicDaoFactory.logout(session);
    }
  }

  private void updateAttachment(Session session, SimpleDocument document, boolean indexIt,
      boolean invokeCallback) throws RepositoryException {
    SimpleDocument oldAttachment = repository.findDocumentById(session, document.getPk(),
        document.getLanguage());
    repository.updateDocument(session, document);
    if (document.isOpenOfficeCompatible() && document.isReadOnly()) {
      // le fichier est renommé
      if (!oldAttachment.getFilename().equals(document.getFilename())) {
        webdavRepository.deleteAttachmentNode(session, oldAttachment);
        webdavRepository.createAttachmentNode(session, document);
      } else {
        webdavRepository.updateNodeAttachment(session, document);
      }
    }
    String userId = document.getCreatedBy();
    if ((userId != null) && (userId.length() > 0) && invokeCallback) {
      CallBackManager callBackManager = CallBackManager.get();
      callBackManager.invoke(CallBackManager.ACTION_ATTACHMENT_UPDATE, Integer.parseInt(userId),
          document.getInstanceId(), document.getForeignId());
    }
    if (indexIt) {
      createIndex(document);
    }
  }

  @Override
  public void changeVersionState(SimpleDocumentPK pk) {
    Session session = null;
    try {
      session = BasicDaoFactory.getSystemSession();
      repository.changeVersionState(session, pk);
      session.save();
    } catch (RepositoryException ex) {
      throw new AttachmentException(this.getClass().getName(), SilverpeasException.ERROR, "", ex);
    } finally {
      BasicDaoFactory.logout(session);
    }
  }
}
