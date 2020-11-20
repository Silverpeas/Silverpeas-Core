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
package org.silverpeas.core.contribution.attachment;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.silverpeas.core.ActionType;
import org.silverpeas.core.ResourceReference;
import org.silverpeas.core.admin.component.ComponentInstanceDeletion;
import org.silverpeas.core.admin.user.model.SilverpeasRole;
import org.silverpeas.core.annotation.Service;
import org.silverpeas.core.cache.VolatileResourceCleaner;
import org.silverpeas.core.contribution.attachment.model.DocumentType;
import org.silverpeas.core.contribution.attachment.model.HistorisedDocument;
import org.silverpeas.core.contribution.attachment.model.SimpleDocument;
import org.silverpeas.core.contribution.attachment.model.SimpleDocumentPK;
import org.silverpeas.core.contribution.attachment.model.UnlockContext;
import org.silverpeas.core.contribution.attachment.notification.AttachmentEventNotifier;
import org.silverpeas.core.contribution.attachment.process.AttachmentSimulationElementLister;
import org.silverpeas.core.contribution.attachment.repository.DocumentRepository;
import org.silverpeas.core.contribution.attachment.util.SimpleDocumentList;
import org.silverpeas.core.contribution.attachment.webdav.WebdavRepository;
import org.silverpeas.core.contribution.content.form.FormException;
import org.silverpeas.core.contribution.content.form.RecordSet;
import org.silverpeas.core.contribution.template.publication.PublicationTemplate;
import org.silverpeas.core.contribution.template.publication.PublicationTemplateException;
import org.silverpeas.core.contribution.template.publication.PublicationTemplateManager;
import org.silverpeas.core.i18n.I18NHelper;
import org.silverpeas.core.index.indexing.model.FullIndexEntry;
import org.silverpeas.core.index.indexing.model.IndexEngineProxy;
import org.silverpeas.core.index.indexing.model.IndexEntryKey;
import org.silverpeas.core.notification.system.ResourceEvent;
import org.silverpeas.core.persistence.jcr.JcrDatastoreManager;
import org.silverpeas.core.persistence.jcr.JcrSession;
import org.silverpeas.core.process.annotation.SimulationActionProcess;
import org.silverpeas.core.util.Charsets;
import org.silverpeas.core.util.ResourceLocator;
import org.silverpeas.core.util.SettingBundle;
import org.silverpeas.core.util.StringUtil;
import org.silverpeas.core.util.annotation.Action;
import org.silverpeas.core.util.annotation.SourceObject;
import org.silverpeas.core.util.annotation.SourcePK;
import org.silverpeas.core.util.annotation.TargetPK;
import org.silverpeas.core.util.file.FileUtil;
import org.silverpeas.core.util.logging.SilverLogger;

import javax.inject.Inject;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.silverpeas.core.contribution.attachment.util.AttachmentSettings.*;
import static org.silverpeas.core.persistence.jcr.JcrRepositoryConnector.openSystemSession;
import static org.silverpeas.core.util.StringUtil.normalize;

/**
 * @author ehugonnet
 */
@Service
public class SimpleDocumentService
    implements AttachmentService, ComponentInstanceDeletion, VolatileResourceCleaner {

  private static final String ATTACHMENT_TYPE = "Attachment";
  private static final String COMMENT_TYPE = "Comment";
  @Inject
  private WebdavRepository webdavRepository;
  @Inject
  private DocumentRepository repository;
  @Inject
  private AttachmentEventNotifier notificationService;

  private final SettingBundle settings =
      ResourceLocator.getSettingBundle("org.silverpeas.util.attachment.Attachment");

  @Override
  public void deleteAllAttachments(final String componentInstanceId) {
    try (JcrSession session = openSystemSession()) {
      final String componentInstanceNodePath = '/' + componentInstanceId;
      if (session.nodeExists(componentInstanceNodePath)) {
        List<SimpleDocument> documentsToDelete =
            repository.listAllDocumentsByComponentId(session, componentInstanceId, null);
        for (SimpleDocument documentToDelete : documentsToDelete) {
          deleteAttachment(session, documentToDelete, true);
        }
        session.getNode(componentInstanceNodePath).remove();
        session.save();
      } else {
        SilverLogger.getLogger(this)
            .warn("Non existing node in JCR matching the component instance {0}",
                componentInstanceId);
      }
    } catch (RepositoryException ex) {
      throw new AttachmentException(ex);
    }
  }

  @Override
  public void cleanVolatileResources(final String volatileResourceId,
      final String componentInstanceIdentifier) {
    deleteAllAttachments(volatileResourceId, componentInstanceIdentifier);
  }

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
    if (settings.getBoolean("attachment.index.separately", true)) {
      String language = I18NHelper.checkLanguage(document.getLanguage());
      String objectType = ATTACHMENT_TYPE + document.getId() + "_" + language;
      FullIndexEntry indexEntry = new FullIndexEntry(document.getInstanceId(), objectType, document.
          getForeignId());
      indexEntry.setLang(language);
      indexEntry.setCreationDate(document.getCreated());
      indexEntry.setCreationUser(document.getCreatedBy());
      if (startOfVisibility != null) {
        indexEntry.setStartDate(startOfVisibility);
      }
      if (endOfVisibility != null) {
        indexEntry.setEndDate(endOfVisibility);
      }

      indexEntry.setTitle(document.getTitle(), language);
      indexEntry.setPreview(document.getDescription(), language);
      indexEntry.setFilename(document.getFilename());
      indexEntry.addFileContent(document.getAttachmentPath(), Charsets.UTF_8.name(), document.
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
      String objectType = ATTACHMENT_TYPE;
      PublicationTemplate pub = PublicationTemplateManager.getInstance().
          getPublicationTemplate(indexEntry.getComponent() + ":" + objectType + ":" + xmlFormName);
      RecordSet set = pub.getRecordSet();
      set.indexRecord(pk.getId(), xmlFormName, indexEntry);
    } catch (PublicationTemplateException | FormException e) {
      SilverLogger.getLogger(this).error(e);
    }
  }

  /**
   * @param document
   * @param lang
   */
  private void deleteIndex(SimpleDocument document, String lang) {
    String language = lang;
    if (language == null) {
      language = I18NHelper.defaultLanguage;
    }
    String objectType = ATTACHMENT_TYPE + document.getId() + '_' + language;
    IndexEntryKey indexEntry = new IndexEntryKey(document.getInstanceId(), objectType, document.
        getForeignId());
    IndexEngineProxy.removeIndexEntry(indexEntry);
  }

  @Override
  public void unindexAttachmentsOfExternalObject(ResourceReference foreignKey) {
    try (JcrSession session = openSystemSession()) {
      List<SimpleDocument> docs = repository.listDocumentsByForeignId(session, foreignKey.
          getInstanceId(), foreignKey.getId(), I18NHelper.defaultLanguage);
      for (SimpleDocument doc : docs) {
        deleteIndex(doc, I18NHelper.defaultLanguage);
      }
    } catch (RepositoryException ex) {
      throw new AttachmentException(ex);
    }
  }

  @Override
  public void addXmlForm(SimpleDocumentPK pk, String language, String xmlFormName) {
    try (JcrSession session = openSystemSession()) {
      SimpleDocument doc = repository.findDocumentById(session, pk, language);
      doc.setXmlFormId(xmlFormName);
      repository.updateDocument(session, doc, true);
      session.save();
    } catch (RepositoryException ex) {
      throw new AttachmentException(ex);
    }
  }

  /**
   * Create file attached to an object who is identified by the foreignId.
   * @param document the document to be created.
   * @param content the binary content of the document.
   * @return the stored document.
   * @throws AttachmentException
   */
  @SimulationActionProcess(elementLister = AttachmentSimulationElementLister.class)
  @Action(ActionType.CREATE)
  @Override
  public SimpleDocument createAttachment(@SourceObject @TargetPK SimpleDocument document,
      InputStream content) {
    return createAttachment(document, content, true);
  }

  /**
   * Create file attached to an object who is identified by the foreignId.
   * @param document the document to be created.
   * @param content the binary content of the document.
   * @param indexIt <code>true</code> if the document is to be indexed,  <code>false</code>
   * otherwhise.
   * @return the stored document.
   */
  @SimulationActionProcess(elementLister = AttachmentSimulationElementLister.class)
  @Action(ActionType.CREATE)
  @Override
  public SimpleDocument createAttachment(@SourceObject @TargetPK SimpleDocument document,
      InputStream content, boolean indexIt) {
    return createAttachment(document, content, indexIt, true);
  }

  /**
   * Create file attached to an object who is identified by the foreignId.
   * @param document the document to be created.
   * @param content the binary content of the document.
   * @param indexIt <code>true</code> if the document is to be indexed,  <code>false</code>
   * otherwhise.
   * @param notify <code>true</code> to notify about the creation of an attachment,
   * <code>false</code> otherwise.
   * @return the stored document.
   */
  @SimulationActionProcess(elementLister = AttachmentSimulationElementLister.class)
  @Action(ActionType.CREATE)
  @Override
  public SimpleDocument createAttachment(@SourceObject @TargetPK SimpleDocument document,
      InputStream content, boolean indexIt, boolean notify) {
    normalizeFileName(document);
    try (JcrSession session = openSystemSession()) {
      SimpleDocumentPK docPk = repository.createDocument(session, document);
      session.save();
      SimpleDocument createdDocument = repository.findDocumentById(session, docPk, document.
          getLanguage());
      createdDocument.setPublicDocument(document.isPublic());
      SimpleDocument finalDocument = repository.unlock(session, createdDocument, false);
      repository.storeContent(finalDocument, content, false);
      if (reallyNotifying(document, notify) &&
          StringUtil.isDefined(document.getCreatedBy())) {
        notificationService.notifyEventOn(ResourceEvent.Type.CREATION, document);
      }
      if (indexIt) {
        createIndex(finalDocument);
      }
      return finalDocument;
    } catch (RepositoryException | IOException ex) {
      throw new AttachmentException(ex);
    }
  }

  /**
   * Delete a given attachment.
   * @param document the document to deleted.
   */
  @Override
  public void deleteAttachment(SimpleDocument document) {
    deleteAttachment(document, true);
  }

  @Override
  public void deleteAllAttachments(final String resourceId, final String componentInstanceId) {
    List<SimpleDocument> documentsToDelete =
        listAllDocumentsByForeignKey(new ResourceReference(resourceId, componentInstanceId), null);
    for (SimpleDocument documentToDelete : documentsToDelete) {
      deleteAttachment(documentToDelete);
    }
  }

  /**
   * Delete a given attachment.
   * @param document the attachmentDetail object to deleted.
   * @param notify <code>true</code> to notify about the deletion of an attachment,
   * <code>false</code> otherwise.</code>
   */
  @Override
  public void deleteAttachment(SimpleDocument document, boolean notify) {
    try (JcrSession session = openSystemSession()) {
      deleteAttachment(session, document, notify);
      session.save();
    } catch (RepositoryException ex) {
      throw new AttachmentException(ex);
    }
  }

  private void deleteAttachment(Session session, SimpleDocument document, boolean notify)
      throws RepositoryException {
    repository.fillNodeName(session, document);
    repository.deleteDocument(session, document.getPk());
    for (String lang : I18NHelper.getAllSupportedLanguages()) {
      deleteIndex(document, lang);
    }
    if (document.isOpenOfficeCompatible()) {
      webdavRepository.deleteAttachmentNode(session, document);
    }
    if (reallyNotifying(document, notify)) {
      notificationService.notifyEventOn(ResourceEvent.Type.DELETION, document);
    }
  }

  @Override
  public SimpleDocument searchDocumentById(SimpleDocumentPK primaryKey, String lang) {
    try (JcrSession session = openSystemSession()) {
      if (StringUtil.isDefined(primaryKey.getId()) && !StringUtil.isLong(primaryKey.getId())) {
        return repository.findDocumentById(session, primaryKey, lang);
      }
      SimpleDocument doc = repository
          .findDocumentByOldSilverpeasId(session, primaryKey.getComponentName(),
              primaryKey.getOldSilverpeasId(), false, lang);
      if (doc == null) {
        doc = repository.findDocumentByOldSilverpeasId(session, primaryKey.getComponentName(),
            primaryKey.getOldSilverpeasId(), true, lang);
      }
      return doc;
    } catch (RepositoryException ex) {
      throw new AttachmentException(ex);
    }
  }

  @Override
  public SimpleDocumentList<SimpleDocument> listAllDocumentsByForeignKey(ResourceReference foreignKey,
      String lang) {
    try (JcrSession session = openSystemSession()) {
      return repository.listAllDocumentsByForeignId(session, foreignKey.getInstanceId(), foreignKey.
          getId(), lang);
    } catch (RepositoryException ex) {
      throw new AttachmentException(ex);
    }
  }

  @Override
  public SimpleDocumentList<SimpleDocument> listDocumentsByForeignKey(ResourceReference foreignKey,
      String lang) {
    try (JcrSession session = openSystemSession()) {
      final SimpleDocumentList<SimpleDocument> documents = repository
          .listDocumentsByForeignId(session, foreignKey.getInstanceId(), foreignKey.
              getId(), lang);
      documents.sortYoungestToOldestAddIfEnabled();
      return documents;
    } catch (RepositoryException ex) {
      throw new AttachmentException(ex);
    }
  }

  @SimulationActionProcess(elementLister = AttachmentSimulationElementLister.class)
  @Action(ActionType.UPDATE)
  @Override
  public void updateAttachment(@SourceObject @TargetPK SimpleDocument document, boolean indexIt,
      boolean notify) {
    normalizeFileName(document);
    try (JcrSession session = openSystemSession()) {
      SimpleDocument oldAttachment =
          repository.findDocumentById(session, document.getPk(), document.getLanguage());
      repository.fillNodeName(session, document);
      repository.updateDocument(session, document, true);
      if (!oldAttachment.isVersioned() && document.isOpenOfficeCompatible() &&
          document.isReadOnly()) {
        // le fichier est renommé
        if (!oldAttachment.getFilename().equals(document.getFilename())) {
          webdavRepository.deleteAttachmentNode(session, oldAttachment);
          webdavRepository.createAttachmentNode(session, document);
        } else {
          webdavRepository.updateAttachmentBinaryContent(session, document);
        }
      }
      session.save();

      String userId = document.getUpdatedBy();
      if (StringUtil.isDefined(userId) && reallyNotifying(document, notify)) {
        notificationService.notifyEventOn(ResourceEvent.Type.UPDATE, oldAttachment, document);
      }
      if (indexIt) {
        createIndex(document);
      }
    } catch (RepositoryException | IOException ex) {
      throw new AttachmentException(ex);
    }
  }

  @SimulationActionProcess(elementLister = AttachmentSimulationElementLister.class)
  @Action(ActionType.UPDATE)
  @Override
  public void updateAttachment(@SourceObject @TargetPK SimpleDocument document, InputStream in,
      boolean indexIt, boolean notify) {
    normalizeFileName(document);
    try (JcrSession session = openSystemSession()) {
      String owner = document.getEditedBy();
      if (!StringUtil.isDefined(owner)) {
        owner = document.getUpdatedBy();
      }
      boolean checkinRequired = repository.lock(session, document, owner);
      SimpleDocument docBeforeUpdate =
          repository.findDocumentById(session, document.getPk(), document.getLanguage());
      repository.updateDocument(session, document, true);
      repository.addContent(session, document.getPk(), document.getAttachment());
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

      session.save();

      String userId = finalDocument.getUpdatedBy();
      if (StringUtil.isDefined(userId) && reallyNotifying(finalDocument, notify) &&
          finalDocument.isPublic()) {
        notificationService.notifyEventOn(ResourceEvent.Type.UPDATE, docBeforeUpdate, document);
      }
      if (indexIt) {
        createIndex(finalDocument);
      }
    } catch (RepositoryException | IOException ex) {
      throw new AttachmentException(ex);
    }
  }

  @Override
  public void removeContent(SimpleDocument document, String lang, boolean notify) {
    try (JcrSession session = openSystemSession()) {
      boolean requireLock = repository.lock(session, document, document.getEditedBy());
      boolean existsOtherContents = repository.removeContent(session, document.getPk(), lang);
      if (document.isOpenOfficeCompatible() && document.isReadOnly()) {
        webdavRepository.deleteAttachmentContentNode(session, document, lang);
      }
      deleteIndex(document, document.getLanguage());

      session.save();

      String userId = document.getCreatedBy();
      if (StringUtil.isDefined(userId) && reallyNotifying(document, notify)) {
        if (existsOtherContents) {
          notificationService.notifyEventOn(ResourceEvent.Type.UPDATE, document, document);
        } else {
          notificationService.notifyEventOn(ResourceEvent.Type.DELETION, document);
        }
      }
      SimpleDocument finalDocument = document;
      if (requireLock) {
        finalDocument = repository.unlockFromContentDeletion(session, document);
        if (existsOtherContents) {
          repository.duplicateContent(document, finalDocument);
        }
      }
      finalDocument.setLanguage(lang);
      final File fileToDelete;
      if (!existsOtherContents) {
        fileToDelete =
            new File(finalDocument.getDirectoryPath(null)).getParentFile().getParentFile();
      } else {
        fileToDelete = new File(finalDocument.getAttachmentPath()).getParentFile();
      }
      FileUtils.deleteQuietly(fileToDelete);
      FileUtil.deleteEmptyDir(fileToDelete.getParentFile());
    } catch (RepositoryException | IOException ex) {
      throw new AttachmentException(ex);
    }
  }

  /**
   * Clone the attachment.
   * @param original
   * @param foreignCloneId
   * @return
   */
  @Override
  public SimpleDocumentPK cloneDocument(SimpleDocument original, String foreignCloneId) {
    try (JcrSession session = openSystemSession();
         InputStream in = new FileInputStream(original.getAttachmentPath())) {
      SimpleDocumentPK clonePk = repository
          .copyDocument(session, original, new ResourceReference(foreignCloneId, original.getInstanceId()));
      SimpleDocument clone = repository.findDocumentById(session, clonePk, null);
      repository.copyMultilangContent(original, clone);
      repository.setClone(session, original, clone);
      session.save();
      return clonePk;
    } catch (RepositoryException | IOException ex) {
      throw new AttachmentException(ex);
    }
  }

  /**
   * Clone the attachment.
   * @param original
   * @param targetPk
   * @return
   */
  @SimulationActionProcess(elementLister = AttachmentSimulationElementLister.class)
  @Action(ActionType.COPY)
  @Override
  public SimpleDocumentPK copyDocument(@SourceObject SimpleDocument original,
      @TargetPK ResourceReference targetPk) {
    try (JcrSession session = openSystemSession()) {
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
    } catch (RepositoryException | IOException ex) {
      throw new AttachmentException(ex);
    }
  }

  @SimulationActionProcess(elementLister = AttachmentSimulationElementLister.class)
  @Action(ActionType.COPY)
  @Override
  public List<SimpleDocumentPK> copyAllDocuments(@SourcePK ResourceReference resourceSourcePk,
      @TargetPK ResourceReference targetDestinationPk) {
    List<SimpleDocumentPK> copiedDocumentKeys = new ArrayList<>();
    List<SimpleDocument> documentsToCopy = listAllDocumentsByForeignKey(resourceSourcePk, null);
    for (SimpleDocument documentToCopy : documentsToCopy) {
      copiedDocumentKeys.add(copyDocument(documentToCopy, new ResourceReference(targetDestinationPk)));
    }
    return copiedDocumentKeys;
  }

  /**
   * Reorder the attachments according to the order in the list.
   * @param pks
   * @throws AttachmentException
   */
  @Override
  public void reorderAttachments(List<SimpleDocumentPK> pks) {
    try (JcrSession session = openSystemSession()) {
      final List<SimpleDocument> list = new ArrayList<>();
      for (SimpleDocumentPK pk : pks) {
        list.add(repository.findDocumentById(session, pk, null));
      }
      reorderDocuments(session, list);
      session.save();
    } catch (RepositoryException ex) {
      throw new AttachmentException(ex);
    }
  }

  /**
   * Reorder the attachments according to the order in the list.
   * @param documents
   * @throws AttachmentException
   */
  @Override
  public void reorderDocuments(List<SimpleDocument> documents) {
    try (JcrSession session = openSystemSession()) {
      reorderDocuments(session, documents);
      session.save();
    } catch (RepositoryException ex) {
      throw new AttachmentException(ex);
    }
  }

  private void reorderDocuments(final JcrSession session, final List<SimpleDocument> documents)
      throws RepositoryException {
    int i;
    if (listFromYoungestToOldestAdd()) {
      boolean isYoungestToOldestSorted = true;
      for (int y = 1; isYoungestToOldestSorted && y < documents.size(); y++) {
        final SimpleDocument previous = documents.get(y - 1);
        final SimpleDocument current = documents.get(y);
        isYoungestToOldestSorted = previous.getOldSilverpeasId() > current.getOldSilverpeasId();
      }
      if (isYoungestToOldestSorted) {
        i = DEFAULT_REORDER_START;
        Collections.reverse(documents);
      } else {
        i = YOUNGEST_TO_OLDEST_MANUAL_REORDER_START;
      }
    } else {
      i = DEFAULT_REORDER_START;
    }
    for (SimpleDocument doc : documents) {
      doc.setOrder(i);
      repository.setOrder(session, doc);
      i += 1;
    }
  }

  @Override
  public void getBinaryContent(OutputStream output, SimpleDocumentPK pk, String lang) {
    getBinaryContent(output, pk, lang, 0, -1);
  }

  @Override
  public void getBinaryContent(final OutputStream output, final SimpleDocumentPK pk,
      final String lang, final long contentOffset, final long contentLength) {
    try (JcrSession session = openSystemSession();
         InputStream in = repository.getContent(session, pk, lang)) {
      IOUtils.copyLarge(in, output, contentOffset, contentLength);
    } catch (IOException | RepositoryException ex) {
      throw new AttachmentException(ex);
    }
  }

  @Override
  public List<SimpleDocument> listDocumentsRequiringWarning(Date alertDate, String language) {
    try (JcrSession session = openSystemSession()) {
      return repository.listDocumentsRequiringWarning(session, alertDate, language);
    } catch (RepositoryException ex) {
      throw new AttachmentException(ex);
    }
  }

  @Override
  public List<SimpleDocument> listExpiringDocuments(Date expiryDate, String language) {
    try (JcrSession session = openSystemSession()) {
      return repository.listExpiringDocuments(session, expiryDate, language);
    } catch (RepositoryException ex) {
      throw new AttachmentException(ex);
    }
  }

  @Override
  public List<SimpleDocument> listDocumentsToUnlock(Date expiryDate, String language) {
    try (JcrSession session = openSystemSession()) {
      return repository.listDocumentsToUnlock(session, expiryDate, language);
    } catch (RepositoryException ex) {
      throw new AttachmentException(ex);
    }
  }

  @SimulationActionProcess(elementLister = AttachmentSimulationElementLister.class)
  @Action(ActionType.UPDATE)
  @Override
  public void updateAttachment(@SourceObject @TargetPK SimpleDocument document, File content,
      boolean indexIt, boolean notify) {
    try (InputStream in = new BufferedInputStream(new FileInputStream(content))) {
      updateAttachment(document, in, indexIt, notify);
    } catch (IOException ex) {
      throw new AttachmentException(ex);
    }
  }

  @Override
  public void getBinaryContent(File file, SimpleDocumentPK pk, String lang) {
    try (OutputStream out = new BufferedOutputStream(new FileOutputStream(file))) {
      getBinaryContent(out, pk, lang);
    } catch (IOException ex) {
      throw new AttachmentException(ex);
    }
  }

  @SimulationActionProcess(elementLister = AttachmentSimulationElementLister.class)
  @Action(ActionType.CREATE)
  @Override
  public SimpleDocument createAttachment(@SourceObject @TargetPK SimpleDocument document,
      File content) {
    return createAttachment(document, content, true);
  }

  @SimulationActionProcess(elementLister = AttachmentSimulationElementLister.class)
  @Action(ActionType.CREATE)
  @Override
  public SimpleDocument createAttachment(@SourceObject @TargetPK SimpleDocument document,
      File content, boolean indexIt) {
    return createAttachment(document, content, indexIt, true);
  }

  @SimulationActionProcess(elementLister = AttachmentSimulationElementLister.class)
  @Action(ActionType.CREATE)
  @Override
  public SimpleDocument createAttachment(@SourceObject @TargetPK SimpleDocument document,
      File content, boolean indexIt, boolean notify) {
    try (InputStream in = new BufferedInputStream(new FileInputStream(content))) {
      return createAttachment(document, in, indexIt, notify);
    } catch (IOException ex) {
      throw new AttachmentException(ex);
    }
  }

  /**
   * Release a locked file.
   * @param context : the unlock parameters.
   * @return false if the file is locked - true if the unlock succeeded.
   * @throws AttachmentException
   */
  @Override
  public boolean unlock(UnlockContext context) {
    try (JcrSession session = openSystemSession()) {
      boolean restorePreviousVersion = context.isForce();
      String contentLanguage = I18NHelper.checkLanguage(context.getLang());
      SimpleDocument document = repository
          .findDocumentById(session, new SimpleDocumentPK(context.getAttachmentId()),
              contentLanguage);
      SimpleDocument docBeforeUpdate =
          repository.findDocumentById(session, document.getPk(), contentLanguage);
      contentLanguage = document.getLanguage();
      boolean updateOfficeContentFromWebDav =
          document.isOpenOfficeCompatible() && !context.isUpload() && context.isWebdav();
      if (updateOfficeContentFromWebDav && !contentLanguage.equals(StringUtil
          .defaultStringIfNotDefined(document.getWebdavContentEditionLanguage(),
              contentLanguage))) {
        // Verifying if the content language handled in WEBDAV repository is the same as the
        // content language took from the context.
        // The language handled into WEVDAV is different, SimpleDocument must be reloaded with
        // the right content language.
        contentLanguage = document.getWebdavContentEditionLanguage();
        document =
            repository.findDocumentById(session, new SimpleDocumentPK(context.getAttachmentId()),
                contentLanguage);
      }

      if (!canBeUnlocked(context, session, document)) {
        return false;
      }

      boolean notify =
          prepareDocumentForUnlocking(context, document, updateOfficeContentFromWebDav);
      unlockDocumentInRepo(session, context, document, restorePreviousVersion,
          updateOfficeContentFromWebDav);
      if (document.isPublic()) {
        String userId = context.getUserId();
        if (StringUtil.isDefined(userId) && reallyNotifying(document, notify)) {
          notificationService.notifyEventOn(ResourceEvent.Type.UPDATE, docBeforeUpdate, document);
        }
      }
      notificationService.notifyEventOn(ResourceEvent.Type.UNLOCK, docBeforeUpdate, document);
    } catch (IOException | RepositoryException e) {
      throw new AttachmentException("Check-in failed", e);
    }
    return true;
  }

  private void unlockDocumentInRepo(final JcrSession session, final UnlockContext context,
      final SimpleDocument document, final boolean restorePreviousVersion,
      final boolean updateOfficeContentFromWebDav) throws RepositoryException, IOException {
    SimpleDocument finalDocument = repository.unlock(session, document, restorePreviousVersion);
    if (updateOfficeContentFromWebDav) {
      webdavRepository.updateAttachmentBinaryContent(session, finalDocument);
      webdavRepository.deleteAttachmentNode(session, finalDocument);
      repository.duplicateContent(document, finalDocument);
      JcrDatastoreManager.get().notifyDataSave();
    } else if (finalDocument.isOpenOfficeCompatible() && (context.isUpload() || !context.isWebdav())) {
      webdavRepository.deleteAttachmentNode(session, finalDocument);
      JcrDatastoreManager.get().notifyDataSave();
    } else {
      File file = new File(finalDocument.getAttachmentPath());
      if (!file.exists() && !context.isForce()) {
        repository.duplicateContent(document, finalDocument);
      }
    }
    session.save();
  }

  private boolean prepareDocumentForUnlocking(final UnlockContext context,
      final SimpleDocument document, final boolean updateOfficeContentFromWebDav) {
    boolean notify = false;
    if (context.isWebdav() || context.isUpload()) {
      String workerId = document.getEditedBy();
      document.setUpdated(new Date());
      document.setUpdatedBy(workerId);
      notify = true;
    }
    document.setPublicDocument(context.isPublicVersion());
    document.setComment(context.getComment());
    if (updateOfficeContentFromWebDav) {
      document.setSize(document.getWebdavContentEditionSize());
    }
    return notify;
  }

  private boolean canBeUnlocked(final UnlockContext context, final JcrSession session,
      final SimpleDocument document) throws RepositoryException {
    if (document.isOpenOfficeCompatible() && !context.isForce() &&
        webdavRepository.isNodeLocked(session, document)) {
      return false;
    }
    return context.isForce() || !document.isReadOnly() || document.getEditedBy().equals(context.
        getUserId());
  }

  /**
   * Lock a file so it can be edited by an user.
   * @param attachmentId
   * @param userId
   * @param language
   * @return false if the attachment is already checkout - true if the attachment was successfully
   * checked out.
   */
  @Override
  public boolean lock(String attachmentId, String userId, String language) {
    try (JcrSession session = openSystemSession()) {
      SimpleDocumentPK pk = new SimpleDocumentPK(attachmentId);
      SimpleDocument document = repository.findDocumentById(session, pk, language);
      if (document.isReadOnly()) {
        return document.getEditedBy().equals(userId);
      }
      repository.lock(session, document, document.getEditedBy());
      document.edit(userId);
      if (document.isOpenOfficeCompatible()) {
        webdavRepository.createAttachmentNode(session, document);
      }

      SimpleDocument oldAttachment =
          repository.findDocumentById(session, document.getPk(), document.getLanguage());
      repository.updateDocument(session, document, false);
      if (document.isOpenOfficeCompatible() && document.isReadOnly()) {
        if (!oldAttachment.getFilename().equals(document.getFilename())) {
          webdavRepository.deleteAttachmentNode(session, oldAttachment);
          webdavRepository.createAttachmentNode(session, document);
        } else {
          webdavRepository.updateNodeAttachment(session, document);
        }
      }

      session.save();
      return true;
    } catch (RepositoryException | IOException ex) {
      throw new AttachmentException(ex);
    }
  }

  @Override
  public SimpleDocumentPK changeVersionState(SimpleDocumentPK pk, String comment) {
    try (JcrSession session = openSystemSession()) {
      SimpleDocumentPK updatedPk = repository.changeVersionState(session, pk, comment);
      session.save();
      return updatedPk;
    } catch (RepositoryException | IOException ex) {
      throw new AttachmentException(ex);
    }
  }

  @Override
  public SimpleDocument findExistingDocument(SimpleDocumentPK pk, String fileName,
      ResourceReference foreign, String lang) {
    List<SimpleDocument> existingDocuments = listDocumentsByForeignKey(foreign, lang);
    SimpleDocument document = searchDocumentById(pk, lang);
    if (document == null) {
      for (SimpleDocument doc : existingDocuments) {
        if (doc.getFilename().equalsIgnoreCase(fileName)) {
          return doc;
        }
      }
    }
    return document;
  }

  @Override
  public SimpleDocumentList<SimpleDocument> listDocumentsByForeignKeyAndType(
      ResourceReference foreignKey, DocumentType type, String lang) {
    try (JcrSession session = openSystemSession()) {
      final SimpleDocumentList<SimpleDocument> documents = repository
          .listDocumentsByForeignIdAndType(session, foreignKey.getInstanceId(), foreignKey.
              getId(), type, lang);
      documents.sortYoungestToOldestAddIfEnabled();
      return documents;
    } catch (RepositoryException ex) {
      throw new AttachmentException(ex);
    }
  }

  @Override
  public List<SimpleDocument> listDocumentsLockedByUser(String usedId, String language) {
    try (JcrSession session = openSystemSession()) {
      final List<SimpleDocument> documents = repository
          .listDocumentsLockedByUser(session, usedId, language);
      documents.sort((o1, o2) -> o2.getUpdated().compareTo(o1.getUpdated()));
      return documents;
    } catch (RepositoryException ex) {
      throw new AttachmentException(ex);
    }
  }

  @SimulationActionProcess(elementLister = AttachmentSimulationElementLister.class)
  @Action(ActionType.MOVE)
  @Override
  public SimpleDocumentPK moveDocument(@SourceObject SimpleDocument document,
      @TargetPK ResourceReference destination) {
    try (JcrSession session = openSystemSession()) {
      SimpleDocumentPK pk = repository.moveDocument(session, document, destination);
      SimpleDocument moveDoc = repository.findDocumentById(session, pk, null);
      repository.moveFullContent(document, moveDoc);
      if (moveDoc.isOpenOfficeCompatible()) {
        webdavRepository.moveNodeAttachment(session, document, destination.getInstanceId());
      }
      session.save();
      return pk;
    } catch (RepositoryException | IOException ex) {
      throw new AttachmentException(ex);
    }
  }

  @SimulationActionProcess(elementLister = AttachmentSimulationElementLister.class)
  @Action(ActionType.MOVE)
  @Override
  public List<SimpleDocumentPK> moveAllDocuments(@SourcePK ResourceReference resourceSourcePk,
      @TargetPK ResourceReference targetDestinationPk) {
    List<SimpleDocumentPK> movedDocumentKeys = new ArrayList<>();
    List<SimpleDocument> documentsToMove = listAllDocumentsByForeignKey(resourceSourcePk, null);
    for (SimpleDocument documentToMove : documentsToMove) {
      movedDocumentKeys.add(moveDocument(documentToMove, new ResourceReference(targetDestinationPk)));
    }
    return movedDocumentKeys;
  }

  @Override
  public void updateIndexEntryWithDocuments(FullIndexEntry indexEntry) {
    if (!indexEntry.getObjectType().startsWith(ATTACHMENT_TYPE) &&
        !indexEntry.getObjectType().startsWith(COMMENT_TYPE)) {
      final ResourceReference pk =
          new ResourceReference(indexEntry.getObjectId(), indexEntry.getComponent());
      final String lang = indexEntry.getLang();
      final List<SimpleDocument> documents = listDocumentsByForeignKey(pk, lang);
      final boolean indexFileContent = settings.getBoolean("attachment.index.incorporated", true);
      for (SimpleDocument currentDocument : documents) {
        final SimpleDocument lastPublicVersion = currentDocument.getLastPublicVersion();
        if (lastPublicVersion != null) {
          indexEntry.addTextContent(lastPublicVersion.getTitle(), lang);
          indexEntry.addTextContent(lastPublicVersion.getDescription(), lang);
          indexEntry.addTextContent(lastPublicVersion.getFilename(), lang);
          if (indexFileContent) {
            indexEntry.addFileContent(lastPublicVersion.getAttachmentPath(), Charsets.UTF_8.name(),
                lastPublicVersion.getContentType(), lang);
          }
        }
      }
    }
  }

  @Override
  public void indexAllDocuments(ResourceReference fk, Date startOfVisibilityPeriod,
      Date endOfVisibilityPeriod) {
    List<SimpleDocument> documents = listAllDocumentsByForeignKey(fk, null);
    for (SimpleDocument currentDocument : documents) {
      createIndex(currentDocument, startOfVisibilityPeriod, endOfVisibilityPeriod);
    }
  }

  @Override
  public Map<String, String> mergeDocuments(ResourceReference originalForeignKey, ResourceReference cloneForeignKey,
      DocumentType type) {
    try (JcrSession session = openSystemSession()) {
      // On part des fichiers d'origine
      List<SimpleDocument> attachments =
          listDocumentsByForeignKeyAndType(originalForeignKey, type, null);
      Map<String, SimpleDocument> clones = listDocumentsOfClone(cloneForeignKey, type, null);
      Map<String, String> ids = new HashMap<>(clones.size());
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
    } catch (RepositoryException | IOException ex) {
      throw new AttachmentException(ex);
    }

  }

  private Map<String, SimpleDocument> listDocumentsOfClone(ResourceReference resourceReference, DocumentType type,
      String lang) {
    List<SimpleDocument> documents = listDocumentsByForeignKeyAndType(resourceReference, type, lang);
    Map<String, SimpleDocument> result = new HashMap<>(documents.size());
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
    try (JcrSession session = openSystemSession()) {
      // On part des fichiers d'origine
      List<SimpleDocument> attachments = repository
          .listDocumentsByComponentIdAndType(session, componentId, DocumentType.attachment,
              I18NHelper.defaultLanguage);
      for (SimpleDocument attachment : attachments) {
        if (attachment.isVersioned() != toVersionning) {
          repository.changeVersionState(session, attachment.getPk(), "");
        }
      }
      session.save();
    } catch (RepositoryException | IOException ex) {
      throw new AttachmentException(ex);
    }
  }

  @Override
  public void switchAllowingDownloadForReaders(final SimpleDocumentPK pk, final boolean allowing) {
    SimpleDocument document = searchDocumentById(pk, null);
    final boolean documentUpdateRequired;
    if (allowing) {
      documentUpdateRequired =
          document.addRolesForWhichDownloadIsAllowed(SilverpeasRole.READER_ROLES);
    } else {
      documentUpdateRequired =
          document.addRolesForWhichDownloadIsForbidden(SilverpeasRole.READER_ROLES);
    }

    // Updating JCR if required
    if (documentUpdateRequired) {
      try (JcrSession session = openSystemSession()) {
        repository.saveForbiddenDownloadForRoles(session, document);
        session.save();
      } catch (RepositoryException ex) {
        throw new AttachmentException(ex);
      }
    }
  }

  @Override
  public void switchEnableDisplayAsContent(final SimpleDocumentPK pk, final boolean enable) {
    SimpleDocument document = searchDocumentById(pk, null);
    final boolean documentUpdateRequired = enable != document.isDisplayableAsContent();

    // Updating JCR if required
    if (documentUpdateRequired) {
      document.setDisplayableAsContent(enable);
      try (JcrSession session = openSystemSession()) {
        repository.saveDisplayableAsContent(session, document);
        session.save();
      } catch (RepositoryException ex) {
        throw new AttachmentException(ex);
      }
    }
  }

  @Override
  public void switchEnableEditSimultaneously(final SimpleDocumentPK pk, final boolean enable) {
    SimpleDocument document = searchDocumentById(pk, null);
    document.editableSimultaneously().ifPresent(e -> {
      final boolean documentUpdateRequired = enable != e;
      // Updating JCR if required
      if (documentUpdateRequired) {
        document.setEditableSimultaneously(enable);
        try (JcrSession session = openSystemSession()) {
          repository.saveEditableSimultaneously(session, document);
          session.save();
        } catch (RepositoryException ex) {
          throw new AttachmentException(ex);
        }
      }
    });
  }

  /**
   * Check if notification must be really performed
   */
  private boolean reallyNotifying(SimpleDocument document, boolean requestedNotify) {
    if (document.getDocumentType() == DocumentType.image) {
      // adding an image (to an existing WYSIWYG) must never trigger the notification.
      // This must be done by WYSIWYG service itself (if needed).
      return false;
    }
    return requestedNotify;
  }

  /**
   * Deletes the resources belonging to the specified component instance. This method is invoked
   * by Silverpeas when a component instance is being deleted.
   * @param componentInstanceId the unique identifier of a component instance.
   */
  @Override
  public void delete(final String componentInstanceId) {
    deleteAllAttachments(componentInstanceId);
  }

  /**
   * Normalizes the fileName returned by {@link SimpleDocument#getFilename()} and sets the
   * normalized result by using {@link SimpleDocument#setFilename(String)} method of given document.
   * @param document the document which the filename MUST be normalized.
   */
  private void normalizeFileName(final SimpleDocument document) {
    final String normalizedFileName = normalize(document.getFilename());
    document.setFilename(normalizedFileName);
  }
}
