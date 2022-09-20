/*
 * Copyright (C) 2000 - 2022 Silverpeas
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
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
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
import org.silverpeas.core.contribution.attachment.process.huge.AttachmentHugeProcess;
import org.silverpeas.core.contribution.attachment.process.huge.AttachmentHugeProcessManager;
import org.silverpeas.core.contribution.attachment.process.huge.PreventAttachmentHugeProcess;
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
import org.silverpeas.core.process.annotation.SimulationActionProcess;
import org.silverpeas.core.util.Charsets;
import org.silverpeas.core.util.Pair;
import org.silverpeas.core.util.ResourceLocator;
import org.silverpeas.core.util.SettingBundle;
import org.silverpeas.core.util.StringUtil;
import org.silverpeas.core.util.annotation.Action;
import org.silverpeas.core.util.annotation.SourceObject;
import org.silverpeas.core.util.annotation.SourcePK;
import org.silverpeas.core.util.annotation.TargetPK;
import org.silverpeas.core.util.file.FileUtil;
import org.silverpeas.core.util.logging.SilverLogger;
import org.silverpeas.jcr.JCRSession;

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
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.util.Comparator.comparing;
import static java.util.Optional.ofNullable;
import static org.silverpeas.core.contribution.attachment.SimpleDocumentServiceContext.canUnlockNotifyUpdateFromRequestContext;
import static org.silverpeas.core.contribution.attachment.SimpleDocumentServiceContext.unlockMustNotNotifyUpdateIntoRequestContext;
import static org.silverpeas.core.contribution.attachment.util.AttachmentSettings.*;
import static org.silverpeas.core.util.StringUtil.defaultStringIfNotDefined;
import static org.silverpeas.core.util.StringUtil.normalize;

/**
 * @author ehugonnet
 */
@Service
public class SimpleDocumentService
    implements AttachmentService, ComponentInstanceDeletion, VolatileResourceCleaner {

  private static final String ATTACHMENT_TYPE = "Attachment";
  private static final String COMMENT_TYPE = "Comment";
  private static final String NODE_TYPE = "Node";

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
    try (JCRSession session = JCRSession.openSystemSession()) {
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
      indexEntry.setCreationDate(document.getCreationDate());
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
      PublicationTemplate pub = PublicationTemplateManager.getInstance().
          getPublicationTemplate(
              indexEntry.getComponent() + ":" + ATTACHMENT_TYPE + ":" + xmlFormName);
      RecordSet set = pub.getRecordSet();
      set.indexRecord(pk.getId(), xmlFormName, indexEntry);
    } catch (PublicationTemplateException | FormException e) {
      SilverLogger.getLogger(this).error(e);
    }
  }

  private void deleteIndex(SimpleDocument document, String lang) {
    String language = lang;
    if (language == null) {
      language = I18NHelper.DEFAULT_LANGUAGE;
    }
    String objectType = ATTACHMENT_TYPE + document.getId() + '_' + language;
    IndexEntryKey indexEntry = new IndexEntryKey(document.getInstanceId(), objectType, document.
        getForeignId());
    IndexEngineProxy.removeIndexEntry(indexEntry);
  }

  @Override
  public void unindexAttachmentsOfExternalObject(ResourceReference foreignKey) {
    try (JCRSession session = JCRSession.openSystemSession()) {
      List<SimpleDocument> docs = repository.listDocumentsByForeignId(session, foreignKey.
          getInstanceId(), foreignKey.getId(), I18NHelper.DEFAULT_LANGUAGE);
      for (SimpleDocument doc : docs) {
        deleteIndex(doc, I18NHelper.DEFAULT_LANGUAGE);
      }
    } catch (RepositoryException ex) {
      throw new AttachmentException(ex);
    }
  }

  @PreventAttachmentHugeProcess
  @Override
  public void addXmlForm(@SourcePK SimpleDocumentPK pk, String language, String xmlFormName) {
    try (JCRSession session = JCRSession.openSystemSession()) {
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
   * @throws AttachmentException if an error occurs in the process
   */
  @PreventAttachmentHugeProcess
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
   * otherwise.
   * @return the stored document.
   */
  @PreventAttachmentHugeProcess
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
   * otherwise.
   * @param notify <code>true</code> to notify about the creation of an attachment,
   * <code>false</code> otherwise.
   * @return the stored document.
   */
  @PreventAttachmentHugeProcess
  @SimulationActionProcess(elementLister = AttachmentSimulationElementLister.class)
  @Action(ActionType.CREATE)
  @Override
  public SimpleDocument createAttachment(@SourceObject @TargetPK SimpleDocument document,
      InputStream content, boolean indexIt, boolean notify) {
    normalizeFileName(document);
    try (JCRSession session = JCRSession.openSystemSession()) {
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
   * @param document the document to delete.
   */
  @PreventAttachmentHugeProcess
  @Override
  public void deleteAttachment(@SourceObject SimpleDocument document) {
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
   * @param document the attachmentDetail object to delete.
   * @param notify <code>true</code> to notify about the deletion of an attachment,
   * <code>false</code> otherwise.</code>
   */
  @PreventAttachmentHugeProcess
  @Override
  public void deleteAttachment(@SourceObject SimpleDocument document, boolean notify) {
    try (JCRSession session = JCRSession.openSystemSession()) {
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
    try (JCRSession session = JCRSession.openSystemSession()) {
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
  public SimpleDocumentList<SimpleDocument> listAllDocumentsByForeignKey(
      ResourceReference foreignKey,
      String lang) {
    try (JCRSession session = JCRSession.openSystemSession()) {
      return repository.listAllDocumentsByForeignId(session, foreignKey.getInstanceId(), foreignKey.
          getId(), lang);
    } catch (RepositoryException ex) {
      throw new AttachmentException(ex);
    }
  }

  @Override
  public SimpleDocumentList<SimpleDocument> listDocumentsByForeignKey(ResourceReference foreignKey,
      String lang) {
    try (JCRSession session = JCRSession.openSystemSession()) {
      final SimpleDocumentList<SimpleDocument> documents = repository
          .listDocumentsByForeignId(session, foreignKey.getInstanceId(), foreignKey.
              getId(), lang);
      documents.sortYoungestToOldestAddIfEnabled();
      return documents;
    } catch (RepositoryException ex) {
      throw new AttachmentException(ex);
    }
  }

  @PreventAttachmentHugeProcess
  @SimulationActionProcess(elementLister = AttachmentSimulationElementLister.class)
  @Action(ActionType.UPDATE)
  @Override
  public void updateAttachment(@SourceObject @TargetPK SimpleDocument document, boolean indexIt,
      boolean notify) {
    normalizeFileName(document);
    try (JCRSession session = JCRSession.openSystemSession()) {
      SimpleDocument oldAttachment =
          repository.findDocumentById(session, document.getPk(), document.getLanguage());
      repository.fillNodeName(session, document);
      repository.updateDocument(session, document, true);
      if (!oldAttachment.isVersioned() && document.isOpenOfficeCompatible() &&
          document.isEdited()) {
        // the file is renaming
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
        unlockMustNotNotifyUpdateIntoRequestContext(document);
      }
      if (indexIt) {
        createIndex(document);
      }
    } catch (RepositoryException | IOException ex) {
      throw new AttachmentException(ex);
    }
  }

  @PreventAttachmentHugeProcess
  @SimulationActionProcess(elementLister = AttachmentSimulationElementLister.class)
  @Action(ActionType.UPDATE)
  @Override
  public void updateAttachment(@SourceObject @TargetPK SimpleDocument document, InputStream in,
      boolean indexIt, boolean notify) {
    normalizeFileName(document);
    try (JCRSession session = JCRSession.openSystemSession()) {
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
      if (document.isOpenOfficeCompatible() && finalDocument.isEdited()) {
        webdavRepository.updateNodeAttachment(session, finalDocument);
      }
      repository.duplicateContent(document, finalDocument);

      session.save();

      String userId = finalDocument.getUpdatedBy();
      if (StringUtil.isDefined(userId) && reallyNotifying(finalDocument, notify) &&
          finalDocument.isPublic()) {
        notificationService.notifyEventOn(ResourceEvent.Type.UPDATE, docBeforeUpdate, document);
        unlockMustNotNotifyUpdateIntoRequestContext(document);
      }
      if (indexIt) {
        createIndex(finalDocument);
      }
    } catch (RepositoryException | IOException ex) {
      throw new AttachmentException(ex);
    }
  }

  @PreventAttachmentHugeProcess
  @Override
  public void removeContent(@SourceObject SimpleDocument document, String lang, boolean notify) {
    try (JCRSession session = JCRSession.openSystemSession()) {
      boolean requireLock = repository.lock(session, document, document.getEditedBy());
      boolean existsOtherContents = repository.removeContent(session, document.getPk(), lang);
      if (document.isOpenOfficeCompatible() && document.isEdited()) {
        webdavRepository.deleteAttachmentContentNode(session, document, lang);
      }
      deleteIndex(document, document.getLanguage());

      session.save();

      String userId = document.getCreatedBy();
      if (StringUtil.isDefined(userId) && reallyNotifying(document, notify)) {
        if (existsOtherContents) {
          notificationService.notifyEventOn(ResourceEvent.Type.UPDATE, document, document);
          unlockMustNotNotifyUpdateIntoRequestContext(document);
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
   * Clones the document and attaches it to the resource referred by the specified identifier. The
   * resource has to be in the same component instance that the document to clone.
   * @param original the document to clone.
   * @param foreignCloneId the unique identifier of the resource to which the clone has to be
   * attached.
   * @return the unique identifier of the clone.
   */
  @PreventAttachmentHugeProcess
  @Override
  public SimpleDocumentPK cloneDocument(@SourceObject SimpleDocument original, String foreignCloneId) {
    try (JCRSession session = JCRSession.openSystemSession()) {
      SimpleDocumentPK clonePk = repository
          .copyDocument(session, original,
              new ResourceReference(foreignCloneId, original.getInstanceId()));
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
   * Copy the document and attaches it to the specified resource.
   * @param original the document to copy.
   * @param targetPk the resource to which the document has to be attached.
   * @return the identifier of the copy.
   */
  @PreventAttachmentHugeProcess
  @SimulationActionProcess(elementLister = AttachmentSimulationElementLister.class)
  @Action(ActionType.COPY)
  @Override
  public SimpleDocumentPK copyDocument(@SourceObject SimpleDocument original,
      @TargetPK ResourceReference targetPk) {
    try (JCRSession session = JCRSession.openSystemSession()) {
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

  @PreventAttachmentHugeProcess
  @SimulationActionProcess(elementLister = AttachmentSimulationElementLister.class)
  @Action(ActionType.COPY)
  @Override
  public List<Pair<SimpleDocumentPK, SimpleDocumentPK>> copyAllDocuments(
      @SourcePK ResourceReference resourceSourcePk,
      @TargetPK ResourceReference targetDestinationPk) {
    return listAllDocumentsByForeignKey(resourceSourcePk, null)
        .stream()
        .map(s -> {
          final SimpleDocumentPK copyPK =
              copyDocument(s, new ResourceReference(targetDestinationPk));
          return Pair.of(s.getPk(), copyPK);
        }).collect(Collectors.toList());
  }

  /**
   * Reorder the attachments according to the order in the list.
   * @param pks the list of document identifiers.
   * @throws AttachmentException if an error occurs in the process
   */
  @Override
  public void reorderAttachments(List<SimpleDocumentPK> pks) {
    try (JCRSession session = JCRSession.openSystemSession()) {
      final List<SimpleDocument> list = new ArrayList<>();
      for (SimpleDocumentPK pk : pks) {
        list.add(repository.findDocumentById(session, pk, null));
      }
      final AttachmentHugeProcessManager attachmentHugeProcessManager = AttachmentHugeProcessManager.get();
      list.stream()
          .map(SimpleDocument::getInstanceId)
          .distinct()
          .forEach(attachmentHugeProcessManager::checkNoOneIsRunningOnInstance);
      reorderDocuments(session, list);
      session.save();
    } catch (RepositoryException ex) {
      throw new AttachmentException(ex);
    }
  }

  /**
   * Reorder the attachments according to the order in the list.
   * @param documents a list of documents.
   * @throws AttachmentException if an error occurs in the process
   */
  @Override
  public void reorderDocuments(List<SimpleDocument> documents) {
    final AttachmentHugeProcessManager attachmentHugeProcessManager = AttachmentHugeProcessManager.get();
    documents.stream()
        .map(SimpleDocument::getInstanceId)
        .distinct()
        .forEach(attachmentHugeProcessManager::checkNoOneIsRunningOnInstance);
    try (JCRSession session = JCRSession.openSystemSession()) {
      reorderDocuments(session, documents);
      session.save();
    } catch (RepositoryException ex) {
      throw new AttachmentException(ex);
    }
  }

  private void reorderDocuments(final JCRSession session, final List<SimpleDocument> documents)
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
    try (JCRSession session = JCRSession.openSystemSession();
         InputStream in = repository.getContent(session, pk, lang)) {
      IOUtils.copyLarge(in, output, contentOffset, contentLength);
    } catch (IOException | RepositoryException ex) {
      throw new AttachmentException(ex);
    }
  }

  @Override
  public List<SimpleDocument> listDocumentsRequiringWarning(Date alertDate, String language) {
    try (JCRSession session = JCRSession.openSystemSession()) {
      return repository.listDocumentsRequiringWarning(session, alertDate, language);
    } catch (RepositoryException ex) {
      throw new AttachmentException(ex);
    }
  }

  @Override
  public List<SimpleDocument> listExpiringDocuments(Date expiryDate, String language) {
    try (JCRSession session = JCRSession.openSystemSession()) {
      return repository.listExpiringDocuments(session, expiryDate, language);
    } catch (RepositoryException ex) {
      throw new AttachmentException(ex);
    }
  }

  @Override
  public List<SimpleDocument> listDocumentsToUnlock(Date expiryDate, String language) {
    try (JCRSession session = JCRSession.openSystemSession()) {
      return repository.listDocumentsToUnlock(session, expiryDate, language);
    } catch (RepositoryException ex) {
      throw new AttachmentException(ex);
    }
  }

  @PreventAttachmentHugeProcess
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

  @PreventAttachmentHugeProcess
  @SimulationActionProcess(elementLister = AttachmentSimulationElementLister.class)
  @Action(ActionType.CREATE)
  @Override
  public SimpleDocument createAttachment(@SourceObject @TargetPK SimpleDocument document,
      File content) {
    return createAttachment(document, content, true);
  }

  @PreventAttachmentHugeProcess
  @SimulationActionProcess(elementLister = AttachmentSimulationElementLister.class)
  @Action(ActionType.CREATE)
  @Override
  public SimpleDocument createAttachment(@SourceObject @TargetPK SimpleDocument document,
      File content, boolean indexIt) {
    return createAttachment(document, content, indexIt, true);
  }

  @PreventAttachmentHugeProcess
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
   * @param context the unlock parameters.
   * @return false if the file is locked, true if the unlocking succeeded.
   * @throws AttachmentException if an error occurs in the process
   */
  @Override
  public boolean unlock(UnlockContext context) {
    try (JCRSession session = JCRSession.openSystemSession()) {
      boolean restorePreviousVersion = context.isForce();
      String contentLanguage = I18NHelper.checkLanguage(context.getLang());
      SimpleDocument document = repository
          .findDocumentById(session, new SimpleDocumentPK(context.getAttachmentId()),
              contentLanguage);
      AttachmentHugeProcessManager.get().checkNoOneIsRunningOnInstance(document.getInstanceId());
      SimpleDocument docBeforeUpdate = new SimpleDocument(document);
      contentLanguage = document.getLanguage();
      boolean updateOfficeContentFromWebDav =
          document.isOpenOfficeCompatible() && !context.isUpload() && context.isWebdav();
      if (updateOfficeContentFromWebDav && !contentLanguage.equals(StringUtil
          .defaultStringIfNotDefined(document.getWebdavContentEditionLanguage(),
              contentLanguage))) {
        // Verifying if the content language handled in WEBDAV repository is the same as the
        // content language took from the context.
        // The language handled into WebDAV is different, SimpleDocument must be reloaded with
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

  private void unlockDocumentInRepo(final JCRSession session, final UnlockContext context,
      final SimpleDocument document, final boolean restorePreviousVersion,
      final boolean updateOfficeContentFromWebDav) throws RepositoryException, IOException {
    SimpleDocument finalDocument = repository.unlock(session, document, restorePreviousVersion);
    if (updateOfficeContentFromWebDav) {
      webdavRepository.updateAttachmentBinaryContent(session, finalDocument);
      webdavRepository.deleteAttachmentNode(session, finalDocument);
      repository.duplicateContent(document, finalDocument);
    } else if (finalDocument.isOpenOfficeCompatible() &&
        (context.isUpload() || !context.isWebdav())) {
      webdavRepository.deleteAttachmentNode(session, finalDocument);
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
      document.setLastUpdateDate(new Date());
      document.setUpdatedBy(defaultStringIfNotDefined(document.getEditedBy(), context.getUserId()));
      notify = canUnlockNotifyUpdateFromRequestContext(document);
    }
    document.setPublicDocument(context.isPublicVersion());
    document.setComment(context.getComment());
    if (updateOfficeContentFromWebDav) {
      document.setSize(document.getWebdavContentEditionSize());
    }
    return notify;
  }

  private boolean canBeUnlocked(final UnlockContext context, final JCRSession session,
      final SimpleDocument document) throws RepositoryException {
    if (document.isOpenOfficeCompatible() && !context.isForce() &&
        webdavRepository.isNodeLocked(session, document)) {
      return false;
    }
    return context.isForce() || !document.isEdited() || document.getEditedBy().equals(context.
        getUserId());
  }

  /**
   * Locks a file, so it can be edited by the user locking it.
   * @param attachmentId the unique identifier of the attached file.
   * @param userId the unique identifier of the user locking the file.
   * @param language the language in which the attached file is written.
   * @return false if the attachment is already checkout, true if the attachment was successfully
   * checked out.
   */
  @Override
  public boolean lock(String attachmentId, String userId, String language) {
    try (JCRSession session = JCRSession.openSystemSession()) {
      SimpleDocumentPK pk = new SimpleDocumentPK(attachmentId);
      SimpleDocument document = repository.findDocumentById(session, pk, language);
      AttachmentHugeProcessManager.get().checkNoOneIsRunningOnInstance(document.getInstanceId());
      if (document.isEdited()) {
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
      if (document.isOpenOfficeCompatible() && document.isEdited()) {
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

  @PreventAttachmentHugeProcess
  @Override
  public SimpleDocumentPK changeVersionState(@SourcePK SimpleDocumentPK pk, String comment) {
    try (JCRSession session = JCRSession.openSystemSession()) {
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
    final Function<SimpleDocument, String> getLanguageForCompare = d -> {
      final String language = d.getAttachment().getLanguage();
      if (language.equals(lang)) {
        return "0";
      } else if (I18NHelper.DEFAULT_LANGUAGE.equals(language)) {
        return "1";
      }
      return language;
    };
    return ofNullable(searchDocumentById(pk, lang))
        .orElseGet(() ->
            listDocumentsByForeignKey(foreign, lang)
                .stream()
                .sorted(comparing(getLanguageForCompare))
                .filter(d -> d.getFilename().equalsIgnoreCase(fileName))
                .findFirst()
                .orElse(null));
  }

  @Override
  public SimpleDocumentList<SimpleDocument> listDocumentsByForeignKeyAndType(
      ResourceReference foreignKey, DocumentType type, String lang) {
    try (JCRSession session = JCRSession.openSystemSession()) {
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
    try (JCRSession session = JCRSession.openSystemSession()) {
      final List<SimpleDocument> documents = repository
          .listDocumentsLockedByUser(session, usedId, language);
      documents.sort((o1, o2) -> o2.getLastUpdateDate().compareTo(o1.getLastUpdateDate()));
      return documents;
    } catch (RepositoryException ex) {
      throw new AttachmentException(ex);
    }
  }

  @PreventAttachmentHugeProcess
  @SimulationActionProcess(elementLister = AttachmentSimulationElementLister.class)
  @Action(ActionType.MOVE)
  @Override
  public SimpleDocumentPK moveDocument(@SourceObject SimpleDocument document,
      @TargetPK ResourceReference destination) {
    try (JCRSession session = JCRSession.openSystemSession()) {
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

  @PreventAttachmentHugeProcess
  @SimulationActionProcess(elementLister = AttachmentSimulationElementLister.class)
  @Action(ActionType.MOVE)
  @Override
  public List<SimpleDocumentPK> moveAllDocuments(@SourcePK ResourceReference resourceSourcePk,
      @TargetPK ResourceReference targetDestinationPk) {
    List<SimpleDocumentPK> movedDocumentKeys = new ArrayList<>();
    List<SimpleDocument> documentsToMove = listAllDocumentsByForeignKey(resourceSourcePk, null);
    for (SimpleDocument documentToMove : documentsToMove) {
      movedDocumentKeys.add(
          moveDocument(documentToMove, new ResourceReference(targetDestinationPk)));
    }
    return movedDocumentKeys;
  }

  @Override
  public void updateIndexEntryWithDocuments(FullIndexEntry indexEntry) {
    if (!indexEntry.getObjectType().startsWith(ATTACHMENT_TYPE) &&
        !indexEntry.getObjectType().startsWith(COMMENT_TYPE) &&
        !indexEntry.getObjectType().startsWith(NODE_TYPE)) {
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

  @PreventAttachmentHugeProcess
  @Override
  public Map<String, String> mergeDocuments(@SourcePK ResourceReference originalForeignKey,
      @TargetPK ResourceReference cloneForeignKey, DocumentType type) {
    try (JCRSession session = JCRSession.openSystemSession()) {
      // starts from the original files
      List<SimpleDocument> attachments =
          listDocumentsByForeignKeyAndType(originalForeignKey, type, null);
      Map<String, SimpleDocument> clones = listDocumentsOfClone(cloneForeignKey, type);
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

  private Map<String, SimpleDocument> listDocumentsOfClone(ResourceReference resourceReference,
      DocumentType type) {
    List<SimpleDocument> documents =
        listDocumentsByForeignKeyAndType(resourceReference, type, null);
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

  @AttachmentHugeProcess
  @Override
  public void switchComponentBehaviour(@SourceObject String componentId, boolean toVersioning) {
    try (JCRSession session = JCRSession.openSystemSession()) {
      // starts from the original files
      List<SimpleDocument> attachments = repository
          .listDocumentsByComponentIdAndType(session, componentId, DocumentType.attachment,
              I18NHelper.DEFAULT_LANGUAGE);
      for (SimpleDocument attachment : attachments) {
        if (attachment.isVersioned() != toVersioning) {
          repository.changeVersionState(session, attachment.getPk(), "");
        }
      }
      session.save();
    } catch (RepositoryException | IOException ex) {
      throw new AttachmentException(ex);
    }
  }

  @PreventAttachmentHugeProcess
  @Override
  public void switchAllowingDownloadForReaders(@SourcePK final SimpleDocumentPK pk, final boolean allowing) {
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
      try (JCRSession session = JCRSession.openSystemSession()) {
        repository.saveForbiddenDownloadForRoles(session, document);
        session.save();
      } catch (RepositoryException ex) {
        throw new AttachmentException(ex);
      }
    }
  }

  @PreventAttachmentHugeProcess
  @Override
  public void switchEnableDisplayAsContent(@SourcePK final SimpleDocumentPK pk, final boolean enable) {
    SimpleDocument document = searchDocumentById(pk, null);
    final boolean documentUpdateRequired = enable != document.isDisplayableAsContent();

    // Updating JCR if required
    if (documentUpdateRequired) {
      document.setDisplayableAsContent(enable);
      try (JCRSession session = JCRSession.openSystemSession()) {
        repository.saveDisplayableAsContent(session, document);
        session.save();
      } catch (RepositoryException ex) {
        throw new AttachmentException(ex);
      }
    }
  }

  @PreventAttachmentHugeProcess
  @Override
  public void switchEnableEditSimultaneously(@SourcePK final SimpleDocumentPK pk, final boolean enable) {
    SimpleDocument document = searchDocumentById(pk, null);
    document.editableSimultaneously().ifPresent(e -> {
      final boolean documentUpdateRequired = enable != e;
      // Updating JCR if required
      if (documentUpdateRequired) {
        document.setEditableSimultaneously(enable);
        try (JCRSession session = JCRSession.openSystemSession()) {
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
   * Deletes the resources belonging to the specified component instance. This method is invoked by
   * Silverpeas when a component instance is being deleted.
   * @param componentInstanceId the unique identifier of a component instance.
   */
  @Override
  public void delete(final String componentInstanceId) {
    deleteAllAttachments(componentInstanceId);
  }

  /**
   * Normalizes the fileName returned by {@link SimpleDocument#getFilename()} and sets the
   * normalized result by using {@link SimpleDocument#setFilename(String)} method of given
   * document.
   * @param document the document which the filename MUST be normalized.
   */
  private void normalizeFileName(final SimpleDocument document) {
    final String normalizedFileName = normalize(document.getFilename());
    document.setFilename(normalizedFileName);
  }
}
