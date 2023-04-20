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
package org.silverpeas.core.contribution.attachment.repository;

import org.apache.commons.io.FileUtils;
import org.silverpeas.core.ResourceReference;
import org.silverpeas.core.annotation.Repository;
import org.silverpeas.core.cache.model.SimpleCache;
import org.silverpeas.core.contribution.attachment.model.DocumentType;
import org.silverpeas.core.contribution.attachment.model.HistorisedDocument;
import org.silverpeas.core.contribution.attachment.model.SimpleAttachment;
import org.silverpeas.core.contribution.attachment.model.SimpleDocument;
import org.silverpeas.core.contribution.attachment.model.SimpleDocumentPK;
import org.silverpeas.core.contribution.attachment.model.SimpleDocumentVersion;
import org.silverpeas.core.contribution.attachment.util.SimpleDocumentList;
import org.silverpeas.core.i18n.I18NHelper;
import org.silverpeas.core.io.media.image.ImageTool;
import org.silverpeas.core.io.media.image.option.OrientationOption;
import org.silverpeas.core.jcr.JCRSession;
import org.silverpeas.core.persistence.jcr.util.NodeIterable;
import org.silverpeas.core.persistence.jcr.util.PropertyIterable;
import org.silverpeas.core.util.DateUtil;
import org.silverpeas.core.util.Pair;
import org.silverpeas.core.util.StringUtil;
import org.silverpeas.core.util.file.FileRepositoryManager;
import org.silverpeas.core.util.file.FileUtil;
import org.silverpeas.core.util.logging.SilverLogger;

import javax.jcr.ItemNotFoundException;
import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.Property;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.Value;
import javax.jcr.query.QueryManager;
import javax.jcr.query.QueryResult;
import javax.jcr.query.qom.ChildNode;
import javax.jcr.query.qom.Comparison;
import javax.jcr.query.qom.Constraint;
import javax.jcr.query.qom.DescendantNode;
import javax.jcr.query.qom.Ordering;
import javax.jcr.query.qom.QueryObjectModel;
import javax.jcr.query.qom.QueryObjectModelConstants;
import javax.jcr.query.qom.QueryObjectModelFactory;
import javax.jcr.query.qom.Selector;
import javax.jcr.version.Version;
import javax.jcr.version.VersionHistory;
import javax.jcr.version.VersionIterator;
import javax.jcr.version.VersionManager;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static java.util.Optional.*;
import static javax.jcr.nodetype.NodeType.MIX_VERSIONABLE;
import static org.silverpeas.core.cache.service.CacheServiceProvider.getThreadCacheService;
import static org.silverpeas.core.contribution.attachment.util.AttachmentSettings.YOUNGEST_TO_OLDEST_MANUAL_REORDER_THRESHOLD;
import static org.silverpeas.core.contribution.attachment.util.AttachmentSettings.listFromYoungestToOldestAdd;
import static org.silverpeas.core.i18n.I18NHelper.DEFAULT_LANGUAGE;
import static org.silverpeas.core.jcr.util.SilverpeasProperty.*;

/**
 * Repository of documents attached to some contributions in Silverpeas. This repository abstracts
 * and wraps the use of the JCR by providing an API to work with {@link SimpleDocument} objects. It
 * is a low-level technical class.
 * @author ehugonnet
 */
@Repository
public class DocumentRepository {

  private static final String SIMPLE_DOCUMENT_ALIAS = "SimpleDocuments";
  final DocumentConverter converter;

  public DocumentRepository() {
    this(new DocumentConverter());
  }

  protected DocumentRepository(final DocumentConverter converter) {
    this.converter = converter;
  }

  private void prepareComponentAttachments(String instanceId, String folder)
      throws RepositoryException {
    try (JCRSession session = JCRSession.openSystemSession()) {
      prepareComponentAttachments(session, instanceId, folder);
      session.save();
    }
  }

  protected Node prepareComponentAttachments(Session session, String instanceId, String folder)
      throws RepositoryException {
    Node targetInstanceNode = converter.getFolder(session.getRootNode(), instanceId);
    return converter.getFolder(targetInstanceNode, folder);
  }

  /**
   * Creates the specified document attached to a contribution in Silverpeas. The contribution has
   * to be referred by the {@link SimpleDocument#getForeignId()} identifier.
   * @param session a session in the JCR
   * @param document the document to create in the JCR
   * @return the unique identifier of the document
   * @throws RepositoryException if the creation in the JCR failed.
   */
  public SimpleDocumentPK createDocument(Session session, SimpleDocument document)
      throws RepositoryException {
    if (0 >= document.getOrder()) {
      getMinMaxOrderIndexes(session, document.getInstanceId(), document.getForeignId(),
          document.getDocumentType()).ifPresent(i -> {
        final Integer minOrderIndex = i.getFirst();
        if (listFromYoungestToOldestAdd() &&
            minOrderIndex > YOUNGEST_TO_OLDEST_MANUAL_REORDER_THRESHOLD) {
          document.setOrder(minOrderIndex - 1);
        } else {
          document.setOrder(i.getSecond() + 1);
        }
      });
    }
    Node docsNode = prepareComponentAttachments(session, document.getInstanceId(),
        document.getFolder());
    Node documentNode = docsNode.addNode(document.computeNodeName(), SLV_SIMPLE_DOCUMENT);
    document.setUpdatedBy(document.getCreatedBy());
    document.setLastUpdateDate(document.getCreationDate());
    converter.fillNode(document, documentNode);
    if (document.isVersioned()) {
      documentNode.addMixin(MIX_VERSIONABLE);
    }
    document.setId(documentNode.getIdentifier());
    document.setOldSilverpeasId(documentNode.getProperty(SLV_PROPERTY_OLD_ID).getLong());
    return document.getPk();
  }

  /**
   * Moves the document to another attached contribution.
   * @param session a session in the JCR
   * @param document an exising document in the JCR to move from its actual contribution to another
   * one.
   * @param destination a reference to another contribution in Silverpeas. This contribution can be
   * within another component instance.
   * @return the new identifier of the moved document.
   * @throws RepositoryException if an error occurs while moving the document in the JCR.
   */
  public SimpleDocumentPK moveDocument(Session session, SimpleDocument document,
      ResourceReference destination) throws RepositoryException {
    SimpleDocument targetDoc = new SimpleDocument();
    SimpleDocumentPK pk = new SimpleDocumentPK(null, destination.getComponentInstanceId());
    pk.setOldSilverpeasId(document.getOldSilverpeasId());
    targetDoc.setPK(pk);
    targetDoc.setDocumentType(document.getDocumentType());
    targetDoc.setNodeName(document.getNodeName());
    prepareComponentAttachments(session, destination.getComponentInstanceId(),
        document.getFolder());
    session.save();
    Node originDocumentNode = session.getNodeByIdentifier(document.getPk().getId());
    if (!originDocumentNode.getPath().equals(targetDoc.getFullJcrPath())) {
      session.getWorkspace().move(originDocumentNode.getPath(), targetDoc.getFullJcrPath());
    }
    VersionManager versionManager = session.getWorkspace().getVersionManager();
    Node targetDocumentNode = session.getNode(targetDoc.getFullJcrPath());
    boolean mustCheckInVersion = true;
    if (converter.isVersionedMaster(targetDocumentNode)) {
      if (!targetDocumentNode.isCheckedOut()) {
        versionManager.checkout(targetDocumentNode.getPath());
      } else {
        mustCheckInVersion = false;
      }
    }
    converter.addStringProperty(targetDocumentNode, SLV_PROPERTY_FOREIGN_KEY,
        destination.getLocalId());
    converter.addStringProperty(targetDocumentNode, SLV_PROPERTY_INSTANCEID,
        destination.getComponentInstanceId());
    if (converter.isVersionedMaster(targetDocumentNode) && targetDocumentNode.isCheckedOut()) {
      session.save();
      if (mustCheckInVersion) {
        versionManager.checkin(targetDocumentNode.getPath());
      }
    }
    pk.setId(targetDocumentNode.getIdentifier());
    return pk;
  }

  /**
   * Copies the document to another contribution. The operation duplicates the document and attaches
   * it to the other contribution.
   * @param session the session in the JCR.
   * @param document the document to copy.
   * @param destination the reference to the other contribution. The contribution can be in another
   * component instance.
   * @return the identifier of the copied document.
   * @throws RepositoryException if an error occurs while copying the document in the JCR.
   */
  public SimpleDocumentPK copyDocument(Session session, SimpleDocument document,
      ResourceReference destination) throws RepositoryException {
    prepareComponentAttachments(destination.getComponentInstanceId(), document.getFolder());
    SimpleDocumentPK pk = new SimpleDocumentPK(null, destination.getComponentInstanceId());
    SimpleDocument targetDoc;
    if (document.isVersioned() && document.getDocumentType() == DocumentType.attachment) {
      targetDoc = new HistorisedDocument();
    } else {
      targetDoc = new SimpleDocument();
    }
    targetDoc.setNodeName(null);
    targetDoc.setPK(pk);
    targetDoc.setDocumentType(document.getDocumentType());
    targetDoc.setForeignId(destination.getLocalId());
    targetDoc.computeNodeName();
    Node originDocumentNode = session.getNodeByIdentifier(document.getPk().getId());
    session.getWorkspace().copy(originDocumentNode.getPath(), targetDoc.getFullJcrPath());
    Node copy = session.getNode(targetDoc.getFullJcrPath());
    copy.setProperty(SLV_PROPERTY_OLD_ID, targetDoc.getOldSilverpeasId());
    copy.setProperty(SLV_PROPERTY_FOREIGN_KEY, destination.getLocalId());
    copy.setProperty(SLV_PROPERTY_INSTANCEID, destination.getComponentInstanceId());
    // The reservation is not copied.
    targetDoc = converter.fillDocument(copy, null);
    targetDoc.release();
    converter.fillNode(targetDoc, copy);
    return targetDoc.getPk();
  }

  /**
   * Copies the versioned document to another contribution in Silverpeas. The operation duplicates
   * the document and all of its versions, and attaches them to the other contribution.
   * @param session a session in the JCR
   * @param document the document with all of its versions to copy
   * @param destination the reference to the other contribution. It can be in another component
   * instance.
   * @return the identifier of the copied historized document.
   * @throws RepositoryException if an error occurs while copying the document and all of its
   * versions in the repository.
   */
  public SimpleDocumentPK copyDocument(Session session, HistorisedDocument document,
      ResourceReference destination) throws RepositoryException {
    try {
      prepareComponentAttachments(destination.getComponentInstanceId(), document.getFolder());
      List<SimpleDocument> history = new ArrayList<>(document.getHistory());
      Collections.reverse(history);
      history.add(document);

      SimpleDocumentPK pk = new SimpleDocumentPK(null, destination.getComponentInstanceId());
      SimpleDocument targetDoc = new HistorisedDocument(history.remove(0));
      FirstVersionManager.computeIfAbsent(new DocumentVersion(targetDoc));
      targetDoc.setNodeName(null);
      targetDoc.setPK(pk);
      targetDoc.setDocumentType(document.getDocumentType());
      targetDoc.setForeignId(destination.getLocalId());
      targetDoc.setUpdatedBy(null);
      targetDoc.computeNodeName();
      pk = createDocument(session, targetDoc);
      if (I18NHelper.isI18nContentEnabled()) {
        addAttachmentInSupportedLanguages(session, document, targetDoc);
      }
      checkin(session, targetDoc, false);
      VersionManager versionManager = session.getWorkspace().getVersionManager();
      String previousVersion = targetDoc.getVersion();
      for (SimpleDocument doc : history) {
        SimpleDocument docToConsider = searchDocumentWithUpdatedContent(session, doc);
        HistorisedDocument targetHistorizedDoc = new HistorisedDocument(docToConsider);
        targetHistorizedDoc.setPK(pk);
        targetHistorizedDoc.setForeignId(destination.getLocalId());
        targetHistorizedDoc.setNodeName(targetDoc.getNodeName());
        // The reservation is not copied.
        targetHistorizedDoc.release();
        Node masterDocumentNode = session.getNodeByIdentifier(pk.getId());
        if (!previousVersion.equals(docToConsider.getVersion())) {
          // In this case, a functional version is performed, so the common tools are used
          checkout(session, targetDoc, docToConsider.getUpdatedBy());
          converter.fillNode(targetHistorizedDoc, masterDocumentNode);
          checkin(session, targetHistorizedDoc, false);
          previousVersion = targetHistorizedDoc.getVersion();
        } else {
          // In this case, the version is a technical one (setOrder for example) and technical
          // tools are used
          versionManager.checkout(masterDocumentNode.getPath());
          converter.fillNode(targetHistorizedDoc, masterDocumentNode);
          session.save();
          versionManager.checkin(masterDocumentNode.getPath());
        }
      }
      return pk;
    } finally {
      FirstVersionManager.clear();
    }
  }

  private SimpleDocument searchDocumentWithUpdatedContent(final Session session,
      final SimpleDocument doc) throws RepositoryException {
    SimpleDocument updated = doc;
    if (I18NHelper.isI18nContentEnabled()) {
      // One language content is aimed by a version. So the first step here is to search the
      // language content updated.
      Set<String> checkedLanguages = new HashSet<>();
      checkedLanguages.add(doc.getLanguage());
      for (String language : I18NHelper.getAllSupportedLanguages()) {
        if (!checkedLanguages.contains(language)) {
          SimpleDocument temp = findDocumentById(session, doc.getPk(), language);
          if (temp != null && !checkedLanguages.contains(temp.getLanguage()) &&
              temp.getLastUpdateDate().after(doc.getLastUpdateDate())) {
            updated = temp;
          }
          checkedLanguages.add(language);
        }
      }
    }
    return updated;
  }

  private void addAttachmentInSupportedLanguages(final Session session,
      final HistorisedDocument document, final SimpleDocument targetDoc)
      throws RepositoryException {
    // The first version can have several language contents.
    Set<String> checkedLanguages = new HashSet<>();
    checkedLanguages.add(targetDoc.getLanguage());
    for (String language : I18NHelper.getAllSupportedLanguages()) {
      if (!checkedLanguages.contains(language)) {
        HistorisedDocument temp = (HistorisedDocument) findDocumentById(session, document.getPk(),
            language);
        List<SimpleDocumentVersion> versions = temp.getHistory();
        if (!versions.isEmpty()) {
          SimpleDocumentVersion firstVersion = versions.get(versions.size() - 1);
          if (!checkedLanguages.contains(firstVersion.getLanguage())) {
            addContent(session, targetDoc.getPk(), firstVersion.getAttachment());
          }
        }
        checkedLanguages.add(language);
      }
    }
  }

  /**
   * Updates the documents in the JCR. The specified document contains the new data with which the
   * document in the JCR has to be updated. The document in the JCR is referred by the unique
   * identifier of the specified document.
   * @param session a session in the JCR
   * @param document the document to update
   * @param updateLastModifiedData a flag indicating if the modification date and the last modifier
   * has to be explicitly done.
   * @throws RepositoryException if the update in the JCR fails.
   */
  public void updateDocument(Session session, SimpleDocument document,
      final boolean updateLastModifiedData) throws RepositoryException {
    Node documentNode = session.getNodeByIdentifier(document.getPk().getId());
    if (updateLastModifiedData) {
      if (StringUtil.isDefined(document.getEditedBy())) {
        document.setUpdatedBy(document.getEditedBy());
      }
      document.setLastUpdateDate(new Date());
    }
    converter.fillNode(document, documentNode);
  }

  /**
   * Saves the optional slv:forbiddenDownloadForRoles document property (a mixin). This saving works
   * with versionable documents without changing the major and minor version. This property is
   * transverse between all versions.
   * @param session a session in the JCR
   * @param document the document for which download has to be forbidden for some user roles.
   * @throws RepositoryException if an error occurs while saving the mixin.
   */
  public void saveForbiddenDownloadForRoles(Session session, SimpleDocument document)
      throws RepositoryException {
    acceptOnMasterVersionWithoutChangingFunctionalVersion(session, document,
        d -> converter.setForbiddenDownloadForRolesOptionalNodeProperty(document, d));
  }

  /**
   * Saves the optional slv:displayableAsContent simple document property (a mixin). This saving
   * works with versionable documents without changing the major and minor version. This property is
   * transverse between all versions.
   * @param session a session in the JCR
   * @param document the document for which the content can be automatically displayed.
   * @throws RepositoryException if an error occurs while saving the mixin.
   */
  public void saveDisplayableAsContent(Session session, SimpleDocument document)
      throws RepositoryException {
    acceptOnMasterVersionWithoutChangingFunctionalVersion(session, document,
        d -> converter.setDisplayableAsContentOptionalNodeProperty(document, d));
  }

  /**
   * Saves the optional slv:editableSimultaneously simple document property (a mixin). This saving
   * works with versionable documents without changing the major and minor version. This property is
   * transverse between all versions.
   * @param session a session in the JCR
   * @param document the document for which the simultaneous edition can be done.
   * @throws RepositoryException if an error occurs while saving the mixin.
   */
  public void saveEditableSimultaneously(Session session, SimpleDocument document)
      throws RepositoryException {
    acceptOnMasterVersionWithoutChangingFunctionalVersion(session, document,
        d -> converter.setEditableSimultaneouslyOptionalNodeProperty(document, d));
  }

  /**
   * Adds the document's clone id to the document even if it is locked. This saving works with
   * versionable documents without changing the major and minor version. This property is transverse
   * between all versions.
   * @param session the JCR session.
   * @param original the original document to be cloned.
   * @param clone the cone of the original document.
   * @throws RepositoryException if an error occurs while setting the clone identifier to the
   * document in the JCR.
   */
  public void setClone(Session session, SimpleDocument original, SimpleDocument clone)
      throws RepositoryException {
    acceptWithoutChangingFunctionalVersion(session, clone,
        d -> d.setProperty(SLV_PROPERTY_CLONE, original.getId()));
  }

  /**
   * Update the document order. This is a unique operation since the order property is not
   * versionable. This saving works with versionable documents without changing the major and minor
   * version. This property is transverse between all versions.
   */
  public void setOrder(Session session, SimpleDocument document) throws RepositoryException {
    acceptWithoutChangingFunctionalVersion(session, document,
        d -> d.setProperty(SLV_PROPERTY_ORDER, document.getOrder()));
  }

  /**
   * Centralization of a simple update. This saving works with versionable documents without
   * changing the major and minor version. This property is transverse between all versions.
   * @param session the current session.
   * @param document the aimed document which will be updated on master version.
   * @param consumer the consumer of a checked documentNode.
   * @throws RepositoryException on JCR error.
   */
  private void acceptOnMasterVersionWithoutChangingFunctionalVersion(Session session,
      SimpleDocument document, RepositoryConsumer<Node> consumer) throws RepositoryException {
    acceptWithoutChangingFunctionalVersion(session, document.getVersionMaster(), consumer);
  }

  /**
   * Centralization of the mechanism to update a property
   * @param session the current session.
   * @param document the aimed document without taking care of the version.
   * @param consumer the consumer of a checked documentNode.
   * @throws RepositoryException on JCR error.
   */
  private void acceptWithoutChangingFunctionalVersion(Session session, SimpleDocument document,
      RepositoryConsumer<Node> consumer) throws RepositoryException {
    Node documentNode = session.getNodeByIdentifier(document.getPk().getId());
    boolean checkedIn = !documentNode.isCheckedOut();
    if (checkedIn) {
      session.getWorkspace().getVersionManager().checkout(documentNode.getPath());
    }
    consumer.accept(documentNode);
    if (checkedIn) {
      session.save();
      session.getWorkspace().getVersionManager().checkin(documentNode.getPath());
    }
  }

  /**
   * Deletes the document in the JCR referred by the specified identifier.
   * @param session a session in the JCR.
   * @param documentPk the unique identifier of the document to delete.
   * @throws RepositoryException if the deletion of the document in the JCR fails.
   */
  public void deleteDocument(Session session, SimpleDocumentPK documentPk)
      throws RepositoryException {
    try {
      final Node documentNode = session.getNodeByIdentifier(documentPk.getId());
      final String documentNodeName = documentNode.getName();
      deleteDocumentNode(documentNode);
      deleteContent(documentPk.getInstanceId(), documentNodeName);
    } catch (ItemNotFoundException e) {
      SilverLogger.getLogger(this).silent(e);
    }
  }

  /**
   * Change the management of versions of the document if it is locked. If the document is currently
   * with version management, then the whole versions history is removed and the document becomes a
   * simple document with no more version history. If the document isn't versioned then a new public
   * version is created and the document becomes a document with a version history management.
   * @param session a session in the JCR
   * @param documentPk the id of the document.
   * @param comment the comment of the user about the first version in the case the versioning is
   * enabled.
   * @throws RepositoryException if the version management switch fails
   * @throws IOException if an error occurs while accessing the filesystem to create or remove
   * versions of the related document
   */
  public SimpleDocumentPK changeVersionState(Session session, SimpleDocumentPK documentPk,
      String comment) throws RepositoryException, IOException {
    try {
      Node documentNode = session.getNodeByIdentifier(documentPk.getId());
      boolean versionedNode =
          documentNode.getParent() instanceof Version || converter.isVersionedMaster(documentNode);
      Node parent = documentNode.getParent();
      if (parent instanceof Version) {
        Version selectedVersion = (Version) parent;
        VersionManager versionManager = documentNode.getSession()
            .getWorkspace()
            .getVersionManager();
        versionManager.restore(selectedVersion, true);
        documentNode = session.getNodeByIdentifier(
            selectedVersion.getContainingHistory().getVersionableIdentifier());
      }
      if (!documentNode.isCheckedOut()) {
        checkoutNode(documentNode, null);
      }
      if (StringUtil.isDefined(comment)) {
        documentNode.setProperty(SLV_PROPERTY_COMMENT, comment);
      }

      final SimpleDocument origin = converter.fillDocument(documentNode, DEFAULT_LANGUAGE);
      if (versionedNode) {
        VersionHistory history = session.getWorkspace()
            .getVersionManager()
            .getVersionHistory(documentNode.getPath());

        documentNode.removeMixin(MIX_VERSIONABLE);
        documentNode.setProperty(SLV_PROPERTY_VERSIONED, false);
        documentNode.setProperty(SLV_PROPERTY_MAJOR, 0);
        documentNode.setProperty(SLV_PROPERTY_MINOR, 0);

        session.save();

        removeHistory(history);

        final SimpleDocument target = converter.fillDocument(documentNode, DEFAULT_LANGUAGE);
        moveMultilangContent(origin, target);
        File currentDocumentDir = new File(
            target.getDirectoryPath(DEFAULT_LANGUAGE)).getParentFile();
        final Optional<File[]> files = ofNullable(currentDocumentDir.getParentFile().listFiles());
        final File[] safeContents = files.orElseGet(() -> {
          SilverLogger.getLogger(this)
              .warn(
                  "During version state removing, attempting to delete {0} which does not exist " +
                      "whereas JCR is having a reference on it",
                  currentDocumentDir.getParentFile().toString());
          return new File[0];
        });
        for (File versionDirectory : safeContents) {
          if (!versionDirectory.equals(currentDocumentDir)) {
            FileUtils.deleteDirectory(versionDirectory);
          }
        }
      } else {
        documentNode.setProperty(SLV_PROPERTY_VERSIONED, true);
        documentNode.setProperty(SLV_PROPERTY_MAJOR, 1);
        documentNode.setProperty(SLV_PROPERTY_MINOR, 0);
        documentNode.addMixin(MIX_VERSIONABLE);
        final SimpleDocument target = converter.fillDocument(documentNode, DEFAULT_LANGUAGE);
        VersionManager versionManager = documentNode.getSession()
            .getWorkspace()
            .getVersionManager();
        session.save();
        versionManager.checkin(documentNode.getPath());
        moveMultilangContent(origin, target);
      }
      return new SimpleDocumentPK(documentNode.getIdentifier(), documentPk);
    } catch (ItemNotFoundException e) {
      return documentPk;
    }
  }

  private void deleteDocumentNode(Node documentNode) throws RepositoryException {
    if (null != documentNode) {
      Session session = documentNode.getSession();

      VersionHistory history = converter.isVersionedMaster(documentNode) ?
          documentNode.getSession()
              .getWorkspace()
              .getVersionManager()
              .getVersionHistory(documentNode.getPath()) :
          null;

      documentNode.remove();
      session.save();

      if (history != null) {
        removeHistory(history);
      }
    }
  }

  /**
   * Finds the document referred by the specified identifier and written in the given language. The
   * document can be proposed in different languages, in this case the language parameter indicates
   * the version of the document to find.
   * @param session the session in the JCR.
   * @param documentPk the unique identifier of the document;
   * @param lang the language in which the document to find is written.
   * @return the document  or null if no such document exists.
   * @throws RepositoryException if an error occurs while looking for the document.
   */
  public SimpleDocument findDocumentById(Session session, SimpleDocumentPK documentPk, String lang)
      throws RepositoryException {
    SimpleDocument document = null;
    try {
      Node documentNode = session.getNodeByIdentifier(documentPk.getId());
      document = converter.convertNode(documentNode, lang);
    } catch (ItemNotFoundException e) {
      SilverLogger.getLogger(this).silent(e);
    }
    return document;
  }

  /**
   * Finds the document referred by its old unique identifier and in the given language. In old days
   * of the management of the documents in Silverpeas, the documents were stored in a database, and
   * they were uniquely identified by an identifier crafted by this database. After their transition
   * to the JCR, a new schema of unique identifier has been applied to them and a need to maintain a
   * map between old identifier and new one for older documents was required.
   * @param session the session in the JCR.
   * @param instanceId the unique instance of the component in which the document is managed.
   * @param oldSilverpeasId the old unique identifier of the document.
   * @param versioned a boolean indicating if the document is or not versioned.
   * @param lang the language in which the document is written.
   * @return the document or null if no such document exists.
   * @throws RepositoryException if an error occurs while looking for the document.
   */
  public SimpleDocument findDocumentByOldSilverpeasId(Session session, String instanceId,
      long oldSilverpeasId, boolean versioned, String lang) throws RepositoryException {
    QueryManager manager = session.getWorkspace().getQueryManager();
    QueryObjectModelFactory factory = manager.getQOMFactory();
    Selector source = factory.selector(SLV_SIMPLE_DOCUMENT, SIMPLE_DOCUMENT_ALIAS);
    DescendantNode descendantNodeConstraint = factory.descendantNode(SIMPLE_DOCUMENT_ALIAS,
        session.getRootNode().getPath() + instanceId);
    Comparison oldSilverpeasIdComparison = getComparison(factory, SLV_PROPERTY_OLD_ID,
        QueryObjectModelConstants.JCR_OPERATOR_EQUAL_TO,
        session.getValueFactory().createValue(oldSilverpeasId));
    Comparison versionedComparison = getComparison(factory, SLV_PROPERTY_VERSIONED,
        QueryObjectModelConstants.JCR_OPERATOR_EQUAL_TO,
        session.getValueFactory().createValue(versioned));

    QueryObjectModel query = factory.createQuery(source, factory.and(descendantNodeConstraint,
        factory.and(oldSilverpeasIdComparison, versionedComparison)), null, null);
    QueryResult result = query.execute();
    NodeIterator iter = result.getNodes();
    if (iter.hasNext()) {
      return converter.convertNode(iter.nextNode(), lang);
    }
    return null;
  }

  /**
   * The last document in an instance with the specified foreignId.
   * @param session the current JCR session.
   * @param instanceId the component id containing the documents.
   * @param foreignId the id of the container owning the documents.
   * @param documentType the type of document.
   * @return the last document in an instance with the specified foreignId.
   * @throws RepositoryException if an error occurs while accessing the JCR
   */
  @SuppressWarnings("SameParameterValue")
  SimpleDocument findLast(Session session, String instanceId, String foreignId,
      final DocumentType documentType) throws RepositoryException {
    final NodeIterator iter = selectDocumentsByForeignIdAndType(session, instanceId, foreignId,
        documentType);
    while (iter.hasNext()) {
      final Node node = iter.nextNode();
      if (!iter.hasNext()) {
        return converter.convertNode(node, DEFAULT_LANGUAGE);
      }
    }
    return null;
  }

  /**
   * Get the minimum and maximum order indexes of attachments linked to a resource for a given
   * type.
   * @param session the current JCR session.
   * @param instanceId the component id containing the documents.
   * @param foreignId the id of the container owning the documents.
   * @param documentType the type of document.
   * @return optional min and max order indexes of documents for the specified foreignId and type.
   * Optional is empty when it exists no document for the foreign key and the type.
   * @throws RepositoryException in case of JCR repository problem.
   */
  Optional<Pair<Integer, Integer>> getMinMaxOrderIndexes(Session session, String instanceId,
      String foreignId, final DocumentType documentType) throws RepositoryException {
    final NodeIterator iter = selectDocumentsByForeignIdAndType(session, instanceId, foreignId,
        documentType);
    int min = Integer.MAX_VALUE;
    int max = Integer.MIN_VALUE;
    while (iter.hasNext()) {
      final Node node = iter.nextNode();
      if (node.hasProperty(SLV_PROPERTY_ORDER)) {
        final int currentOrder = (int) node.getProperty(SLV_PROPERTY_ORDER).getLong();
        min = Math.min(min, currentOrder);
        max = Math.max(max, currentOrder);
      }
    }
    return min == Integer.MAX_VALUE ? empty() : of(Pair.of(min, max));
  }

  /**
   * Search all the documents of type attachment in an instance with the specified foreignId.
   * @param session the current JCR session.
   * @param instanceId the component id containing the documents.
   * @param foreignId the id of the container owning the documents.
   * @param language the language in which the documents are required.
   * @return an ordered list of the documents.
   * @throws RepositoryException if an error occurs
   */
  public SimpleDocumentList<SimpleDocument> listDocumentsByForeignId(Session session,
      String instanceId, String foreignId, String language) throws RepositoryException {
    NodeIterator iter = selectDocumentsByForeignIdAndType(session, instanceId, foreignId,
        DocumentType.attachment);
    return converter.convertNodeIterator(iter, language);
  }

  /**
   * Search all the documents of any type in an instance with the specified foreignId.
   * @param session the current JCR session.
   * @param instanceId the component id containing the documents.
   * @param foreignId the id of the container owning the documents.
   * @param language the language in which the documents are required.
   * @return an ordered list of the documents.
   * @throws RepositoryException if an error occurs
   */
  public SimpleDocumentList<SimpleDocument> listAllDocumentsByForeignId(Session session,
      String instanceId, String foreignId, String language) throws RepositoryException {
    NodeIterator iter = selectDocumentsByForeignId(session, instanceId, foreignId);
    return converter.convertNodeIterator(iter, language);
  }

  /**
   * Search all the documents in an instance with the specified foreignId.
   * @param session the current JCR session.
   * @param instanceId the component id containing the documents.
   * @param foreignId the id of the container owning the documents.
   * @param type the type of required documents.
   * @param language the language in which the documents are required.
   * @return an ordered list of the documents.
   * @throws RepositoryException if an error occurs
   */
  public SimpleDocumentList<SimpleDocument> listDocumentsByForeignIdAndType(Session session,
      String instanceId, String foreignId, DocumentType type, String language)
      throws RepositoryException {
    NodeIterator iter = selectDocumentsByForeignIdAndType(session, instanceId, foreignId, type);
    return converter.convertNodeIterator(iter, language);
  }

  /**
   * Search all the documents in an instance with the specified type.
   * @param session the current JCR session.
   * @param instanceId the component id containing the documents.
   * @param type the type of required documents.
   * @param language the language in which the documents are required.
   * @return an ordered list of the documents.
   * @throws RepositoryException if an error occurs
   */
  public List<SimpleDocument> listDocumentsByComponentIdAndType(Session session, String instanceId,
      DocumentType type, String language) throws RepositoryException {
    NodeIterator iter = selectDocumentsByComponentIdAndType(session, instanceId, type);
    return converter.convertNodeIterator(iter, language);
  }

  /**
   * Search all the documents related to the component instance identified by the specified
   * identifier.
   * @param session the current JCR session.
   * @param instanceId the component id containing the documents.
   * @param language the language in which the documents are required.
   * @return an ordered list of the documents.
   * @throws RepositoryException if an error occurs
   */
  public List<SimpleDocument> listAllDocumentsByComponentId(Session session, String instanceId,
      String language) throws RepositoryException {
    NodeIterator iter = selectAllDocumentsByComponentId(session, instanceId);
    return converter.convertNodeIterator(iter, language);
  }

  /**
   * Search all the documents in an instance with the specified owner.
   * @param session the current JCR session.
   * @param instanceId the component id containing the documents.
   * @param owner the id of the user owning the document.
   * @param language the language in which the documents are required.
   * @return an ordered list of the documents.
   * @throws RepositoryException if an error occurs
   */
  public List<SimpleDocument> listComponentDocumentsByOwner(Session session, String instanceId,
      String owner, String language) throws RepositoryException {
    NodeIterator iter = selectDocumentsByOwnerIdAndComponentId(session, instanceId, owner);
    return converter.convertNodeIterator(iter, language);
  }

  public List<SimpleDocument> listDocumentsLockedByUser(Session session, String usedId,
      String language) throws RepositoryException {
    NodeIterator iter = selectAllDocumentsByOwnerId(session, usedId);
    return converter.convertNodeIterator(iter, language);
  }

  /**
   * Search all the documents of the specified type in an instance with the specified foreignId.
   * @param session the current JCR session.
   * @param instanceId the component id containing the documents.
   * @param foreignId the id of the container owning the documents.
   * @return an ordered list of the documents.
   * @throws RepositoryException if an error occurs
   */
  NodeIterator selectDocumentsByForeignIdAndType(Session session, String instanceId,
      String foreignId, DocumentType type) throws RepositoryException {
    QueryManager manager = session.getWorkspace().getQueryManager();
    QueryObjectModelFactory factory = manager.getQOMFactory();
    Selector source = factory.selector(SLV_SIMPLE_DOCUMENT, SIMPLE_DOCUMENT_ALIAS);
    ChildNode childNodeConstraint = factory.childNode(SIMPLE_DOCUMENT_ALIAS,
        session.getRootNode().getPath() + instanceId + '/' + type.getFolderName());
    return getNodeIteratorByProperty(factory, source, childNodeConstraint, SLV_PROPERTY_FOREIGN_KEY,
        session.getValueFactory().createValue(foreignId));
  }

  private static Comparison getComparison(final QueryObjectModelFactory factory,
      final String slvProperty, final String jcrOperatorEqualTo, final Value session)
      throws RepositoryException {
    return factory.comparison(factory.propertyValue(SIMPLE_DOCUMENT_ALIAS, slvProperty),
        jcrOperatorEqualTo, factory.literal(session));
  }

  /**
   * Search all the documents of any type in an instance with the specified foreignId.
   * @param session the current JCR session.
   * @param instanceId the component id containing the documents.
   * @param foreignId the id of the container owning the documents.
   * @return an ordered list of the documents.
   * @throws RepositoryException if an error occurs
   */
  NodeIterator selectDocumentsByForeignId(Session session, String instanceId, String foreignId)
      throws RepositoryException {
    QueryManager manager = session.getWorkspace().getQueryManager();
    QueryObjectModelFactory factory = manager.getQOMFactory();
    Selector source = factory.selector(SLV_SIMPLE_DOCUMENT, SIMPLE_DOCUMENT_ALIAS);
    DescendantNode descendantNodeConstraint = factory.descendantNode(SIMPLE_DOCUMENT_ALIAS,
        session.getRootNode().getPath() + instanceId + '/');
    return getNodeIteratorByProperty(factory, source, descendantNodeConstraint,
        SLV_PROPERTY_FOREIGN_KEY, session.getValueFactory().createValue(foreignId));
  }

  /**
   * Search all the documents of the specified type in an instance with the specified foreignId.
   * @param session the current JCR session.
   * @param instanceId the component id containing the documents.
   * @param type the type of document.
   * @return an ordered list of the documents.
   * @throws RepositoryException if an error occurs with the JCR
   */
  NodeIterator selectDocumentsByComponentIdAndType(Session session, String instanceId,
      DocumentType type) throws RepositoryException {
    QueryManager manager = session.getWorkspace().getQueryManager();
    QueryObjectModelFactory factory = manager.getQOMFactory();
    Selector source = factory.selector(SLV_SIMPLE_DOCUMENT, SIMPLE_DOCUMENT_ALIAS);
    ChildNode childNodeConstraint = factory.childNode(SIMPLE_DOCUMENT_ALIAS,
        session.getRootNode().getPath() + instanceId + '/' + type.getFolderName());
    Ordering order = factory.ascending(
        factory.propertyValue(SIMPLE_DOCUMENT_ALIAS, SLV_PROPERTY_ORDER));
    QueryObjectModel query = factory.createQuery(source, childNodeConstraint, new Ordering[]{order},
        null);
    QueryResult result = query.execute();
    return result.getNodes();
  }

  /**
   * Search all the documents related to the component instance identified by the specified
   * identifier.
   * @param session the current JCR session.
   * @param instanceId the component id containing the documents.
   * @return an ordered list of the documents.
   * @throws RepositoryException if an error occurs with the JCR
   */
  NodeIterator selectAllDocumentsByComponentId(Session session, String instanceId)
      throws RepositoryException {
    QueryManager manager = session.getWorkspace().getQueryManager();
    QueryObjectModelFactory factory = manager.getQOMFactory();
    Selector source = factory.selector(SLV_SIMPLE_DOCUMENT, SIMPLE_DOCUMENT_ALIAS);
    DescendantNode childNodeConstraint = factory.descendantNode(SIMPLE_DOCUMENT_ALIAS,
        session.getRootNode().getPath() + instanceId);
    Ordering order = factory.ascending(
        factory.propertyValue(SIMPLE_DOCUMENT_ALIAS, SLV_PROPERTY_ORDER));
    QueryObjectModel query = factory.createQuery(source, childNodeConstraint, new Ordering[]{order},
        null);
    QueryResult result = query.execute();
    return result.getNodes();
  }

  /**
   * Search all the documents in an instance which are expiring at the specified date.
   * @param session the current JCR session.
   * @param expiryDate the date when the document reservation should expire.
   * @param language the language in which the documents are required.
   * @return an ordered list of the documents.
   * @throws RepositoryException if an error occurs with the JCR
   */
  public List<SimpleDocument> listExpiringDocuments(Session session, Date expiryDate,
      String language) throws RepositoryException {
    NodeIterator iter = selectExpiringDocuments(session, DateUtil.getBeginOfDay(expiryDate));
    return converter.convertNodeIterator(iter, language);
  }

  /**
   * Search all the documents in an instance which are locked at the alert date.
   * @param session the current JCR session.
   * @param alertDate the date when the document reservation should send an alert.
   * @param language the language in which the documents are required.
   * @return an ordered list of the documents.
   * @throws RepositoryException if an error occurs with the JCR
   */
  public List<SimpleDocument> listDocumentsRequiringWarning(Session session, Date alertDate,
      String language) throws RepositoryException {
    NodeIterator iter = selectWarningDocuments(session, DateUtil.getBeginOfDay(alertDate));
    return converter.convertNodeIterator(iter, language);
  }

  /**
   * Search all the documents in an instance expiring at the specified date.
   * @param session the current JCR session.
   * @param expiryDate the date when the document reservation should expire.
   * @return an ordered list of the documents.
   * @throws RepositoryException if an error occurs with the JCR
   */
  NodeIterator selectExpiringDocuments(Session session, Date expiryDate)
      throws RepositoryException {
    return getNodeIteratorByDateProperty(session, SLV_PROPERTY_EXPIRY_DATE, expiryDate);
  }

  private NodeIterator getNodeIteratorByDateProperty(final Session session,
      final String slvPropertyDate, final Date date) throws RepositoryException {
    QueryManager manager = session.getWorkspace().getQueryManager();
    QueryObjectModelFactory factory = manager.getQOMFactory();
    Calendar expiry = Calendar.getInstance();
    expiry.setTime(DateUtil.getBeginOfDay(date));
    Selector source = factory.selector(SLV_SIMPLE_DOCUMENT, SIMPLE_DOCUMENT_ALIAS);
    return getNodeIteratorByProperty(factory, source, slvPropertyDate,
        session.getValueFactory().createValue(expiry));
  }

  private NodeIterator getNodeIteratorByProperty(final QueryObjectModelFactory factory,
      final Selector source, final Constraint anotherConstraint, final String slvProperty,
      final Value value) throws RepositoryException {
    Comparison comparison = getComparison(factory, slvProperty,
        QueryObjectModelConstants.JCR_OPERATOR_EQUAL_TO, value);
    Ordering order = factory.ascending(
        factory.propertyValue(SIMPLE_DOCUMENT_ALIAS, SLV_PROPERTY_ORDER));
    Constraint constraint =
        anotherConstraint == null ? comparison : factory.and(anotherConstraint, comparison);
    QueryObjectModel query = factory.createQuery(source, constraint, new Ordering[]{order}, null);
    QueryResult result = query.execute();
    return result.getNodes();
  }

  private NodeIterator getNodeIteratorByProperty(final QueryObjectModelFactory factory,
      final Selector source, final String slvProperty, final Value value)
      throws RepositoryException {
    return getNodeIteratorByProperty(factory, source, null, slvProperty, value);
  }

  /**
   * Search all the documents in an instance requiring to be unlocked at the specified date.
   * @param session the current JCR session.
   * @param expiryDate the date when the document reservation should expire.
   * @param language the language in which the documents are required.
   * @return an ordered list of the documents.
   * @throws RepositoryException if an error occurs with the JCR
   */
  public List<SimpleDocument> listDocumentsToUnlock(Session session, Date expiryDate,
      String language) throws RepositoryException {
    NodeIterator iter = selectDocumentsRequiringUnlocking(session, expiryDate);
    return converter.convertNodeIterator(iter, language);
  }

  /**
   * Search all the documents in an instance requiring to be unlocked at the specified date.
   * @param session the current JCR session.
   * @param expiryDate the date when the document reservation should expire.
   * @return an ordered list of the documents.
   * @throws RepositoryException if an error occurs with the JCR
   */
  NodeIterator selectDocumentsRequiringUnlocking(Session session, Date expiryDate)
      throws RepositoryException {
    QueryManager manager = session.getWorkspace().getQueryManager();
    QueryObjectModelFactory factory = manager.getQOMFactory();
    Calendar expiry = Calendar.getInstance();
    expiry.setTime(DateUtil.getBeginOfDay(expiryDate));
    Selector source = factory.selector(SLV_SIMPLE_DOCUMENT, SIMPLE_DOCUMENT_ALIAS);
    Comparison foreignIdComparison = getComparison(factory, SLV_PROPERTY_EXPIRY_DATE,
        QueryObjectModelConstants.JCR_OPERATOR_LESS_THAN,
        session.getValueFactory().createValue(expiry));
    Ordering order = factory.ascending(
        factory.propertyValue(SIMPLE_DOCUMENT_ALIAS, SLV_PROPERTY_ORDER));
    QueryObjectModel query = factory.createQuery(source, foreignIdComparison, new Ordering[]{order},
        null);
    QueryResult result = query.execute();
    return result.getNodes();
  }

  /**
   * Search all the documents in an instance in a warning state at the specified date.
   * @param session the current JCR session.
   * @param alertDate the date when a warning is required.
   * @return an ordered list of the documents.
   * @throws RepositoryException if an error occurs with the JCR
   */
  NodeIterator selectWarningDocuments(Session session, Date alertDate) throws RepositoryException {
    return getNodeIteratorByDateProperty(session, SLV_PROPERTY_ALERT_DATE, alertDate);
  }

  /**
   * Search all the documents in an instance with the specified owner.
   * @param session the current JCR session.
   * @param instanceId the component id containing the documents.
   * @param owner the id of the user owning the documents.
   * @return an ordered list of the documents.
   * @throws RepositoryException if an error occurs with the JCR
   */
  NodeIterator selectDocumentsByOwnerIdAndComponentId(Session session, String instanceId,
      String owner) throws RepositoryException {
    QueryManager manager = session.getWorkspace().getQueryManager();
    QueryObjectModelFactory factory = manager.getQOMFactory();
    Selector source = factory.selector(SLV_SIMPLE_DOCUMENT, SIMPLE_DOCUMENT_ALIAS);
    ChildNode childNodeConstraint = factory.childNode(SIMPLE_DOCUMENT_ALIAS,
        session.getRootNode().getPath() + instanceId + '/' +
            DocumentType.attachment.getFolderName());
    return getNodeIteratorByProperty(factory, source, childNodeConstraint, SLV_PROPERTY_OWNER,
        session.getValueFactory().createValue(owner));
  }

  /**
   * Search all the documents with the specified owner.
   * @param session the current JCR session.
   * @param owner the id of the user owning the documents.
   * @return an ordered list of the documents.
   * @throws RepositoryException if an error occurs with the JCR
   */
  NodeIterator selectAllDocumentsByOwnerId(Session session, String owner)
      throws RepositoryException {
    QueryManager manager = session.getWorkspace().getQueryManager();
    QueryObjectModelFactory factory = manager.getQOMFactory();
    Selector source = factory.selector(SLV_SIMPLE_DOCUMENT, SIMPLE_DOCUMENT_ALIAS);
    return getNodeIteratorByProperty(factory, source, SLV_PROPERTY_OWNER,
        session.getValueFactory().createValue(owner));
  }

  /**
   * Add the content.
   * @param session the current JCR session.
   * @param documentPk the document which content is to be added.
   * @param attachment the attachment metadata.
   * @throws RepositoryException if an error occurs in the JCR
   */
  public void addContent(Session session, SimpleDocumentPK documentPk, SimpleAttachment attachment)
      throws RepositoryException {
    Node documentNode = session.getNodeByIdentifier(documentPk.getId());
    if (converter.isVersionedMaster(documentNode) && !documentNode.isCheckedOut()) {
      String owner = attachment.getUpdatedBy();
      if (!StringUtil.isDefined(owner)) {
        owner = attachment.getCreatedBy();
      }
      checkoutNode(documentNode, owner);
    }
    converter.addAttachment(documentNode, attachment);
  }

  /**
   * Get the content.
   * @param session the current JCR session.
   * @param pk the document which content is to be added.
   * @param lang the content language.
   * @return the attachment binary content.
   * @throws RepositoryException if an error occurs in the JCR
   * @throws IOException if the access to the filesystem fails
   */
  public InputStream getContent(Session session, SimpleDocumentPK pk, String lang)
      throws RepositoryException, IOException {
    Node docNode = session.getNodeByIdentifier(pk.getId());
    String language = lang;
    if (!StringUtil.isDefined(language)) {
      language = DEFAULT_LANGUAGE;
    }
    SimpleDocument document = converter.fillDocument(docNode, language);
    return new BufferedInputStream(
        FileUtils.openInputStream(new File(document.getAttachmentPath())));
  }

  /**
   * Remove the content for the specified language. If no other content exists, then the document
   * node is deleted.
   * @param session the current JCR session.
   * @param documentPk the document which content is to be removed.
   * @param language the language of the content which is to be removed.
   * @return false if the document has no child node after the content remove, true otherwise.
   * @throws RepositoryException if an error occurs in the JCR
   */
  public boolean removeContent(Session session, SimpleDocumentPK documentPk, String language)
      throws RepositoryException {
    Node documentNode = session.getNodeByIdentifier(documentPk.getId());
    if (converter.isVersionedMaster(documentNode) && !documentNode.isCheckedOut()) {
      checkoutNode(documentNode, null);
    }
    converter.removeAttachment(documentNode, language);
    documentNode = session.getNodeByIdentifier(documentPk.getId());
    boolean existsOtherContents = documentNode.hasNodes();
    if (!existsOtherContents) {
      deleteDocumentNode(documentNode);
    }
    return existsOtherContents;
  }

  /**
   * Checkout the specified document if it is versioned to create a new work in progress version.
   * In the JCR the versioned documents are protected for direct modifications. The checking out
   * mechanism is a way to create a working copy of the document on which the modification will
   * be done. Once the modifications done, don't forget to invoke one of the checkin methods to
   * create a new version of the document from its modified working copy.
   * <p>
   * Any non-versioned documents are by default modifiable and hence doesn't required to be
   * explicitly checked out.
   * </p>
   * @param session the session in the JCR
   * @param document the document to lock.
   * @param owner the user asking to checkout the node.
   * @return true if node has be checked out, false otherwise.
   * @throws RepositoryException if an error occurs in the JCR
   */
  public boolean checkout(Session session, SimpleDocument document, String owner)
      throws RepositoryException {
    if (document.isVersioned()) {
      Node documentNode = session.getNodeByIdentifier(document.getId());
      if (!documentNode.isCheckedOut()) {
        checkoutNode(documentNode, owner);
      }
      return true;
    }
    return false;
  }

  /**
   * Checkin the specified document if it is versioned to create a new version or to restore a
   * previous one. By using this method, the metadata of the content are always updated.
   * @param session the current JCR open session to perform actions.
   * @param document the document data from which all needed identifiers are retrieved.
   * @param restore true to restore the previous version if any.
   * @return document updated.
   * @throws RepositoryException if an error occurs in the JCR
   */
  public SimpleDocument checkin(Session session, SimpleDocument document, boolean restore)
      throws RepositoryException {
    return checkin(session, document, restore, false);
  }

  /**
   * Checkin a document if it is versioned from a context into which a language content has just
   * been deleted. This method does not update the metadata of the content in order to obtain an
   * efficient content deletion.
   * @param session the current JCR open session to perform actions.
   * @param document the document data from which all needed identifiers are retrieved.
   * @return the document updated.
   * @throws RepositoryException if an error occurs in the JCR
   */
  public SimpleDocument checkinFromContentDeletion(Session session, SimpleDocument document)
      throws RepositoryException {
    return checkin(session, document, false, true);
  }

  /**
   * Checkin the specified document if it is versioned to create a new version or to restore a
   * previous one.
   * @param session the current JCR open session to perform actions.
   * @param document the document data from which all needed identifiers are retrieved.
   * @param restore true to restore the previous version if any.
   * @param skipContentMetadataUpdate false to update the metadata of the content
   * {@link SimpleDocument#getAttachment()}.
   * @return the document updated.
   * @throws RepositoryException if an error occurs in the JCR
   */
  private SimpleDocument checkin(Session session, SimpleDocument document, boolean restore,
      boolean skipContentMetadataUpdate) throws RepositoryException {
    Node documentNode;
    try {
      documentNode = session.getNodeByIdentifier(document.getId());
    } catch (ItemNotFoundException ex) {
      //Node may have been deleted after removing all its content.
      return document;
    }
    if (document.isVersioned() && documentNode.isCheckedOut()) {
      if (restore) {
        VersionIterator iter = session.getWorkspace()
            .getVersionManager()
            .getVersionHistory(document.getFullJcrPath())
            .getAllVersions();
        Version lastVersion = null;
        while (iter.hasNext()) {
          lastVersion = iter.nextVersion();
        }
        if (null != lastVersion) {
          session.getWorkspace().getVersionManager().restore(lastVersion, true);
          return converter.convertNode(lastVersion.getFrozenNode(), document.getLanguage());
        }
      }
      converter.fillNode(document, documentNode, (restore || skipContentMetadataUpdate));
      return checkinNode(documentNode, document.getLanguage(), document.isPublic());
    }
    if (!document.isVersioned()) {
      converter.fillNode(document, documentNode, (restore || skipContentMetadataUpdate));
      converter.releaseDocumentNode(documentNode, document.getLanguage());
      return converter.convertNode(documentNode, document.getLanguage());
    }
    document.release();
    return document;
  }

  /**
   * Check out the document.
   * @param node the node to check out.
   * @param owner the user  behind the operation
   * @throws RepositoryException if an error occurs in the JCR
   */
  void checkoutNode(Node node, String owner) throws RepositoryException {
    node.getSession().getWorkspace().getVersionManager().checkout(node.getPath());
    converter.addStringProperty(node, SLV_PROPERTY_OWNER, owner);
  }

  /**
   * Check in the document.
   * @param documentNode the node to check in.
   * @param isMajor true if the new version is a major one - false otherwise.
   * @return the document for this new version.
   * @throws RepositoryException on technical JCR error.
   */
  SimpleDocument checkinNode(Node documentNode, String lang, boolean isMajor)
      throws RepositoryException {
    final DocumentVersion firstVersion = FirstVersionManager.remove();
    if (!documentNode.hasProperty(SLV_PROPERTY_MAJOR) && firstVersion != null) {
      isMajor = firstVersion.isMajor();
      int major = firstVersion.getMajor();
      int minor = firstVersion.getMinor();
      documentNode.setProperty(SLV_PROPERTY_MAJOR, isMajor ? major - 1 : major);
      documentNode.setProperty(SLV_PROPERTY_MINOR, isMajor ? minor : minor - 1);
    }
    VersionManager versionManager = documentNode.getSession().getWorkspace().getVersionManager();
    String versionLabel = converter.updateVersion(documentNode, lang, isMajor);
    documentNode.getSession().save();
    Version lastVersion = versionManager.checkin(documentNode.getPath());
    lastVersion.getContainingHistory().addVersionLabel(lastVersion.getName(), versionLabel, false);
    return converter.convertNode(documentNode, lang);
  }

  void removeHistory(VersionHistory history) throws RepositoryException {
    Version root = history.getRootVersion();
    VersionIterator versions = history.getAllVersions();
    while (versions.hasNext()) {
      Version version = versions.nextVersion();
      if (!version.isSame(root)) {
        history.removeVersion(version.getName());
      }
    }
  }

  public void fillNodeName(Session session, SimpleDocument document) throws RepositoryException {
    Node documentNode = session.getNodeByIdentifier(document.getId());
    if (!StringUtil.isDefined(document.getNodeName())) {
      document.setNodeName(documentNode.getName());
    }
  }

  public long storeContent(SimpleDocument document, InputStream in, boolean update)
      throws IOException {
    File file = new File(document.getAttachmentPath());
    if (update) {
      File parentFile = file.getParentFile();
      String[] children = parentFile.list();
      if (parentFile.isDirectory() && children != null && children.length > 0) {
        FileUtils.deleteQuietly(parentFile);
        FileUtils.forceMkdir(parentFile);
      }
    }
    FileUtils.copyInputStreamToFile(in, file);
    if (document.isContentImage()) {
      ImageTool.get().convert(file, null, OrientationOption.auto().modifyImageOnlyIfNecessary());
    }
    return file.length();
  }

  public long storeContent(SimpleDocument document, InputStream in) throws IOException {
    return storeContent(document, in, false);
  }

  public void duplicateContent(SimpleDocument origin, SimpleDocument document) throws IOException {
    String originDir = origin.getDirectoryPath(null);
    String targetDir = document.getDirectoryPath(null);
    targetDir = targetDir.replace('/', File.separatorChar);
    File target = new File(targetDir).getParentFile();
    File source = new File(originDir).getParentFile();
    File[] children = source.listFiles();
    if (!source.exists() || !source.isDirectory() || children == null) {
      return;
    }
    if (!target.exists()) {
      Files.createDirectories(target.toPath());
    }
    for (File langDir : children) {
      File targetLangDir = new File(target, langDir.getName());
      if (!targetLangDir.exists()) {
        FileUtils.copyDirectory(langDir, targetLangDir);
      }
    }
  }

  public void deleteContent(String instanceId, final String nodeName) {
    String directory = FileRepositoryManager.getAbsolutePath(instanceId) + nodeName;
    directory = directory.replace('/', File.separatorChar);
    File documentDirectory = new File(directory);
    if (documentDirectory.exists() && documentDirectory.isDirectory()) {
      FileUtils.deleteQuietly(documentDirectory);
    }
  }

  public void copyMultilangContent(SimpleDocument origin, SimpleDocument copy) throws IOException {
    String originDir = origin.getDirectoryPath(null);
    String targetDir = copy.getDirectoryPath(null);
    targetDir = targetDir.replace('/', File.separatorChar);
    File target = new File(targetDir).getParentFile();
    File source = new File(originDir).getParentFile();
    if (!source.exists() || !source.isDirectory() || source.listFiles() == null) {
      return;
    }
    if (target.exists()) {
      FileUtils.cleanDirectory(target);
    }
    FileUtils.copyDirectory(source, target);
  }

  public void copyFullContent(SimpleDocument origin, SimpleDocument copy) throws IOException {
    String originDir = origin.getDirectoryPath(null);
    String targetDir = copy.getDirectoryPath(null);
    targetDir = targetDir.replace('/', File.separatorChar);
    File target = new File(targetDir).getParentFile().getParentFile();
    File source = new File(originDir).getParentFile().getParentFile();
    if (!source.exists() || !source.isDirectory() || source.listFiles() == null) {
      return;
    }
    if (target.exists()) {
      FileUtils.cleanDirectory(target);
    }
    FileUtils.copyDirectory(source, target);
  }

  public void moveMultilangContent(SimpleDocument origin, SimpleDocument copy) throws IOException {
    String originDir = origin.getDirectoryPath(null);
    File source = new File(originDir).getParentFile();
    String targetDir = copy.getDirectoryPath(null);
    targetDir = targetDir.replace('/', File.separatorChar);
    File target = new File(targetDir).getParentFile();
    if (!source.exists() || !source.isDirectory() || source.listFiles() == null) {
      return;
    }
    if (!target.getParentFile().getName().equals(source.getParentFile().getName())) {
      source = source.getParentFile();
      target = target.getParentFile();
    }
    if (!source.equals(target)) {
      FileUtils.moveDirectory(source, target);
      FileUtil.deleteEmptyDir(source.getParentFile());
    }
  }

  public void moveFullContent(SimpleDocument origin, SimpleDocument copy) throws IOException {
    String originDir = origin.getDirectoryPath(null);
    File source = new File(originDir).getParentFile().getParentFile();
    String targetDir = copy.getDirectoryPath(null);
    targetDir = targetDir.replace('/', File.separatorChar);
    File target = new File(targetDir).getParentFile().getParentFile();
    if (!source.exists() || !source.isDirectory() || source.listFiles() == null) {
      return;
    }
    if (!source.equals(target)) {
      FileUtils.moveDirectory(source, target);
      FileUtil.deleteEmptyDir(source.getParentFile());
    }
  }

  public void mergeAttachment(Session session, SimpleDocument attachment, SimpleDocument clone)
      throws RepositoryException {
    Node originalNode = session.getNodeByIdentifier(attachment.getId());
    Set<String> existingAttachments = new HashSet<>(I18NHelper.getNumberOfLanguages());
    for (Node child : new NodeIterable(originalNode.getNodes())) {
      existingAttachments.add(child.getName());
    }
    Node cloneNode = session.getNodeByIdentifier(clone.getId());
    for (Node child : new NodeIterable(cloneNode.getNodes())) {
      String childNodeName = child.getName();
      if (existingAttachments.contains(childNodeName) && originalNode.hasNode(childNodeName)) {
        copyNode(session, child, originalNode.getNode(childNodeName));
        existingAttachments.remove(childNodeName);
      } else {
        session.move(child.getPath(), originalNode.getPath() + '/' + childNodeName);
      }
    }
    for (String deletedNode : existingAttachments) {
      if (originalNode.hasNode(deletedNode)) {
        originalNode.getNode(deletedNode).remove();
      }
    }
    converter.addStringProperty(originalNode, SLV_PROPERTY_CLONE, null);
  }

  private void copyNode(Session session, Node source, Node target) throws RepositoryException {
    for (Node child : new NodeIterable(target.getNodes())) {
      if (!child.getDefinition().isProtected()) {
        child.remove();
      }
    }
    for (Node child : new NodeIterable(source.getNodes())) {
      session.move(child.getPath(), target.getPath() + '/' + child.getName());
    }
    for (Property property : new PropertyIterable(target.getProperties())) {
      if (!property.getDefinition().isProtected()) {
        property.remove();
      }
    }
    for (Property property : new PropertyIterable(source.getProperties())) {
      if (!property.getDefinition().isProtected()) {
        target.setProperty(property.getName(), property.getValue());
      }
    }
  }

  /**
   * Represents a version of a document.
   */
  public static class DocumentVersion {
    private final int major;
    private final int minor;

    public DocumentVersion(final int major, final int minor) {
      this.major = major;
      this.minor = minor;
    }

    public DocumentVersion(final SimpleDocument document) {
      this(document.getMajorVersion(), document.getMinorVersion());
    }

    boolean isMajor() {
      return minor == 0;
    }

    int getMajor() {
      return major;
    }

    int getMinor() {
      return minor;
    }
  }

  /**
   * In charge of registering the first version to apply for {@link HistorisedDocument}.
   * <p>
   * When a first version will be registered (document copy as an example), using this manager
   * permits to apply the right first version.
   * </p>
   * <p>
   * BE CAREFUL: the repository of this manager is the thread memory, so it MUST be used into a try
   * finally statement:
   * <pre>
   *   try {
   *     ...
   *     FirstVersionManager.set(new Version(...));
   *     ...
   *   } finally {
   *     FirstVersionManager.clear();
   *   }
   * </pre> or
   * <pre>
   *   try {
   *     ...
   *     FirstVersionManager.computeIfAbsent(new Version(...));
   *     ...
   *   } finally {
   *     FirstVersionManager.clear();
   *   }
   * </pre>
   * </p>
   */
  public static class FirstVersionManager {

    private static final String CACHE_KEY = DocumentVersion.class.getSimpleName() + "@theFirst";

    private FirstVersionManager() {
      throw new IllegalAccessError("Utility class");
    }

    /**
     * Gets first version and computes it if it does not yet exist.
     * @param version a version to register if none yet registered.
     * @return the already registered version if any, the given version otherwise.
     */
    public static DocumentVersion computeIfAbsent(final DocumentVersion version) {
      return getThreadCache().computeIfAbsent(CACHE_KEY, DocumentVersion.class, () -> version);
    }

    /**
     * Registers the given version, even if another one is already registered.
     * <p>The previous one registered if any is lost.</p>
     * @param version a version to register.
     */
    public static void set(final DocumentVersion version) {
      getThreadCache().put(CACHE_KEY, version);
    }

    /**
     * Clears the registered version as a first one.
     */
    public static void clear() {
      remove();
    }

    static DocumentVersion remove() {
      return getThreadCache().remove(CACHE_KEY, DocumentVersion.class);
    }

    private static SimpleCache getThreadCache() {
      return getThreadCacheService().getCache();
    }
  }

  @FunctionalInterface
  private interface RepositoryConsumer<T> {
    void accept(T t) throws RepositoryException;
  }
}
