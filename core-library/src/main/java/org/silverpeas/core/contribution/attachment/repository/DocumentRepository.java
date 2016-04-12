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
package org.silverpeas.core.contribution.attachment.repository;

import org.apache.commons.io.FileUtils;
import org.silverpeas.core.contribution.attachment.model.DocumentType;
import org.silverpeas.core.contribution.attachment.model.HistorisedDocument;
import org.silverpeas.core.contribution.attachment.model.SimpleAttachment;
import org.silverpeas.core.contribution.attachment.model.SimpleDocument;
import org.silverpeas.core.contribution.attachment.model.SimpleDocumentPK;
import org.silverpeas.core.contribution.attachment.model.SimpleDocumentVersion;
import org.silverpeas.core.contribution.attachment.util.SimpleDocumentList;
import org.silverpeas.core.persistence.jcr.JcrRepositoryConnector;
import org.silverpeas.core.persistence.jcr.JcrSession;
import org.silverpeas.core.util.DateUtil;
import org.silverpeas.core.util.file.FileRepositoryManager;
import org.silverpeas.core.util.file.FileUtil;
import org.silverpeas.core.util.StringUtil;
import org.silverpeas.core.WAPrimaryKey;
import org.silverpeas.core.i18n.I18NHelper;
import org.silverpeas.core.persistence.jcr.util.NodeIterable;
import org.silverpeas.core.persistence.jcr.util.PropertyIterable;

import javax.jcr.ItemNotFoundException;
import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.Property;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.query.QueryManager;
import javax.jcr.query.QueryResult;
import javax.jcr.query.qom.ChildNode;
import javax.jcr.query.qom.Comparison;
import javax.jcr.query.qom.DescendantNode;
import javax.jcr.query.qom.Ordering;
import javax.jcr.query.qom.QueryObjectModel;
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
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.silverpeas.core.persistence.jcr.util.JcrConstants.*;
import static javax.jcr.nodetype.NodeType.MIX_SIMPLE_VERSIONABLE;

/**
 *
 * @author ehugonnet
 */
public class DocumentRepository {

  private static final String SIMPLE_DOCUMENT_ALIAS = "SimpleDocuments";
  final DocumentConverter converter = new DocumentConverter();

  public void prepareComponentAttachments(String instanceId, String folder) throws
      RepositoryException {
    try (JcrSession session = JcrRepositoryConnector.openSystemSession()) {
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
   * Create file attached to an object who is identified by "PK" SimpleDocument object contains an
   * attribute who identifie the link by a foreign key.
   *
   * @param session
   * @param document
   * @return
   * @throws RepositoryException
   */
  public SimpleDocumentPK createDocument(Session session, SimpleDocument document) throws
      RepositoryException {
    SimpleDocument last = findLast(session, document.getInstanceId(), document.getForeignId());
    if ((null != last) && (0 >= document.getOrder())) {
      document.setOrder(last.getOrder() + 1);
    }
    Node docsNode = prepareComponentAttachments(session, document.getInstanceId(), document.
        getFolder());
    Node documentNode = docsNode.addNode(document.computeNodeName(), SLV_SIMPLE_DOCUMENT);
    document.setUpdatedBy(document.getCreatedBy());
    document.setUpdated(document.getCreated());
    converter.fillNode(document, documentNode);
    if (document.isVersioned()) {
      documentNode.addMixin(MIX_SIMPLE_VERSIONABLE);
    }
    document.setId(documentNode.getIdentifier());
    document.setOldSilverpeasId(documentNode.getProperty(SLV_PROPERTY_OLD_ID).getLong());
    return document.getPk();
  }

  /**
   * Move the document to another attached object.
   *
   * @param session
   * @param document
   * @param destination
   * @return
   * @throws RepositoryException
   */
  public SimpleDocumentPK moveDocument(Session session, SimpleDocument document,
      WAPrimaryKey destination) throws RepositoryException {
    SimpleDocument targetDoc = new SimpleDocument();
    SimpleDocumentPK pk = new SimpleDocumentPK(null, destination.getInstanceId());
    pk.setOldSilverpeasId(document.getOldSilverpeasId());
    targetDoc.setPK(pk);
    targetDoc.setDocumentType(document.getDocumentType());
    targetDoc.setNodeName(document.getNodeName());
    prepareComponentAttachments(session, destination.getInstanceId(), document.getFolder());
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
    converter.addStringProperty(targetDocumentNode, SLV_PROPERTY_FOREIGN_KEY, destination.getId());
    converter.addStringProperty(targetDocumentNode, SLV_PROPERTY_INSTANCEID, destination.
        getInstanceId());
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
   * Copy the document to another attached object.
   *
   * @param session
   * @param document
   * @param destination the foreingId holding reference to the copy.
   * @return
   * @throws RepositoryException
   */
  public SimpleDocumentPK copyDocument(Session session, SimpleDocument document,
      WAPrimaryKey destination) throws RepositoryException {
    prepareComponentAttachments(destination.getInstanceId(), document.getFolder());
    SimpleDocumentPK pk = new SimpleDocumentPK(null, destination.getInstanceId());
    SimpleDocument targetDoc;
    if (document.isVersioned() && document.getDocumentType() == DocumentType.attachment) {
      targetDoc = new HistorisedDocument();
    } else {
      targetDoc = new SimpleDocument();
    }
    targetDoc.setNodeName(null);
    targetDoc.setPK(pk);
    targetDoc.setDocumentType(document.getDocumentType());
    targetDoc.setForeignId(destination.getId());
    targetDoc.computeNodeName();
    Node originDocumentNode = session.getNodeByIdentifier(document.getPk().getId());
    session.getWorkspace().copy(originDocumentNode.getPath(), targetDoc.getFullJcrPath());
    Node copy = session.getNode(targetDoc.getFullJcrPath());
    copy.setProperty(SLV_PROPERTY_OLD_ID, targetDoc.getOldSilverpeasId());
    copy.setProperty(SLV_PROPERTY_FOREIGN_KEY, destination.getId());
    copy.setProperty(SLV_PROPERTY_INSTANCEID, destination.getInstanceId());
    // The reservation is not copied.
    targetDoc = converter.fillDocument(copy, null);
    targetDoc.release();
    converter.fillNode(targetDoc, copy);
    return targetDoc.getPk();
  }

  /**
   * Copy the document to another attached object.
   *
   * @param session
   * @param document
   * @param destination the foreingId holding reference to the copy.
   * @return
   * @throws RepositoryException
   */
  public SimpleDocumentPK copyDocument(Session session, HistorisedDocument document,
      WAPrimaryKey destination) throws RepositoryException, IOException {
    prepareComponentAttachments(destination.getInstanceId(), document.getFolder());
    SimpleDocumentPK pk = new SimpleDocumentPK(null, destination.getInstanceId());
    List<SimpleDocument> history = new ArrayList<SimpleDocument>(document.getHistory());
    Collections.reverse(history);
    history.add(document);
    SimpleDocument targetDoc = new HistorisedDocument(history.remove(0));
    targetDoc.setNodeName(null);
    targetDoc.setPK(pk);
    targetDoc.setDocumentType(document.getDocumentType());
    targetDoc.setForeignId(destination.getId());
    targetDoc.setUpdatedBy(null);
    targetDoc.computeNodeName();
    pk = createDocument(session, targetDoc);
    if (I18NHelper.isI18nContentEnabled()) {
      // The first version can have several language contents.
      Set<String> checkedLanguages = new HashSet<String>();
      checkedLanguages.add(targetDoc.getLanguage());
      for (String language : I18NHelper.getAllSupportedLanguages()) {
        if (!checkedLanguages.contains(language)) {
          HistorisedDocument temp =
              (HistorisedDocument) findDocumentById(session, document.getPk(), language);
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
    unlock(session, targetDoc, false);
    VersionManager versionManager = session.getWorkspace().getVersionManager();
    String currentVersion = targetDoc.getVersion();
    for (SimpleDocument doc : history) {
      if (I18NHelper.isI18nContentEnabled()) {
        // One language content is aimed by a version. So the first step here is to search the
        // language content updated.
        Set<String> checkedLanguages = new HashSet<String>();
        checkedLanguages.add(doc.getLanguage());
        for (String language : I18NHelper.getAllSupportedLanguages()) {
          if (!checkedLanguages.contains(language)) {
            SimpleDocument temp = findDocumentById(session, doc.getPk(), language);
            if (temp != null && !checkedLanguages.contains(temp.getLanguage()) &&
                temp.getUpdated().after(doc.getUpdated())) {
              doc = temp;
            }
            checkedLanguages.add(language);
          }
        }
      }
      HistorisedDocument targetHistorisedDoc = new HistorisedDocument(doc);
      targetHistorisedDoc.setPK(pk);
      targetHistorisedDoc.setForeignId(destination.getId());
      targetHistorisedDoc.setNodeName(targetDoc.getNodeName());
      // The reservation is not copied.
      targetHistorisedDoc.release();
      Node masterDocumentNode = session.getNodeByIdentifier(pk.getId());
      if (!currentVersion.equals(doc.getVersion())) {
        // In this case, a functional version is performed, so the common tools are used
        lock(session, targetDoc, doc.getUpdatedBy());
        converter.fillNode(targetHistorisedDoc, masterDocumentNode);
        unlock(session, targetHistorisedDoc, false);
        currentVersion = targetHistorisedDoc.getVersion();
      } else {
        // In this case, the version is a technical one (setOrder for example) and technical
        // tools are used
        versionManager.checkout(masterDocumentNode.getPath());
        converter.fillNode(targetHistorisedDoc, masterDocumentNode);
        session.save();
        versionManager.checkin(masterDocumentNode.getPath());
      }
    }
    return pk;
  }

  /**
   * Create file attached to an object who is identified by "PK" SimpleDocument object contains an
   * attribute who identifie the link by a foreign key.
   *
   * @param session
   * @param document
   * @param updateLastModifiedData
   * @throws RepositoryException
   * @throws IOException
   */
  public void updateDocument(Session session, SimpleDocument document,
      final boolean updateLastModifiedData) throws
      RepositoryException, IOException {
    Node documentNode = session.getNodeByIdentifier(document.getPk().getId());
    if (updateLastModifiedData) {
      if (StringUtil.isDefined(document.getEditedBy())) {
        document.setUpdatedBy(document.getEditedBy());
      }
      document.setUpdated(new Date());
    }
    converter.fillNode(document, documentNode);
  }

  /**
   * Save the optional slv:forbiddenDownloadForRoles simple document property (MIXIN).
   * This saving works with versionable documents without changing the major and minor version.
   * This property is transverse between all versions.
   *
   * @param session
   * @param document
   * @throws RepositoryException
   */
  public void saveForbiddenDownloadForRoles(Session session, SimpleDocument document)
      throws RepositoryException {
    Node documentNode = session.getNodeByIdentifier(document.getVersionMaster().getPk().getId());
    boolean checkedin = !documentNode.isCheckedOut();
    if (checkedin) {
      session.getWorkspace().getVersionManager().checkout(documentNode.getPath());
    }

    // Optional downloadable mixin
    converter.setForbiddenDownloadForRolesOptionalNodeProperty(document, documentNode);

    if (checkedin) {
      session.save();
      session.getWorkspace().getVersionManager().checkin(documentNode.getPath());
    }
  }

  /**
   * Add the document's clone id to the document even if it is locked.
   *
   * @param session the JCR session.
   * @param original the original document to be cloned.
   * @param clone the cone of the original document.
   * @throws RepositoryException
   */
  public void setClone(Session session, SimpleDocument original, SimpleDocument clone) throws
      RepositoryException {
    Node documentNode = session.getNodeByIdentifier(clone.getId());
    boolean checkedin = !documentNode.isCheckedOut();
    if (checkedin) {
      session.getWorkspace().getVersionManager().checkout(documentNode.getPath());
    }
    documentNode.setProperty(SLV_PROPERTY_CLONE, original.getId());
    if (checkedin) {
      session.save();
      session.getWorkspace().getVersionManager().checkin(documentNode.getPath());
    }
  }

  /**
   * Update the document order. This is a unique operation since the order propery is not
   * versionable.
   *
   * @param session
   * @param document
   * @throws RepositoryException
   */
  public void setOrder(Session session, SimpleDocument document) throws
      RepositoryException {
    Node documentNode = session.getNodeByIdentifier(document.getPk().getId());
    boolean checkedin = !documentNode.isCheckedOut();
    if (checkedin) {
      session.getWorkspace().getVersionManager().checkout(documentNode.getPath());
    }
    documentNode.setProperty(SLV_PROPERTY_ORDER, document.getOrder());
    if (checkedin) {
      session.save();
      session.getWorkspace().getVersionManager().checkin(documentNode.getPath());
    }
  }

  /**
   * Delete a file attached to an object who is identified by "PK" SimpleDocument object contains an
   * attribute who identifie the link by a foreign key.
   *
   * @param session
   * @param documentPk
   * @throws RepositoryException
   */
  public void deleteDocument(Session session, SimpleDocumentPK documentPk) throws
      RepositoryException {
    try {
      Node documentNode = session.getNodeByIdentifier(documentPk.getId());
      deleteContent(documentNode, documentPk.getInstanceId());
      deleteDocumentNode(documentNode);
    } catch (ItemNotFoundException infex) {

    }
  }

  /**
   * Change the management of versions of the document if the document is checkouted. If the
   * document is currently with version management, then all history is removed and the document
   * becomes a simple document with no more version management. If the document has no version
   * management then a new public version is created and the document becomes a document with a
   * version history management.
   *
   * @param session
   * @param documentPk the id of the document.
   * @param comment
   * @throws RepositoryException
   * @throws IOException
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
        VersionManager versionManager = documentNode.getSession().getWorkspace().getVersionManager();
        versionManager.restore(selectedVersion, true);
        documentNode = session.getNodeByIdentifier(selectedVersion.getContainingHistory()
            .getVersionableIdentifier());
      }
      if (!documentNode.isCheckedOut()) {
        checkoutNode(documentNode, null);
      }
      if (StringUtil.isDefined(comment)) {
        documentNode.setProperty(SLV_PROPERTY_COMMENT, comment);
      }
      SimpleDocument origin = converter.fillDocument(documentNode, I18NHelper.defaultLanguage);
      if (versionedNode) {
        removeHistory(documentNode);
        documentNode.removeMixin(MIX_SIMPLE_VERSIONABLE);
        documentNode.setProperty(SLV_PROPERTY_VERSIONED, false);
        documentNode.setProperty(SLV_PROPERTY_MAJOR, 0);
        documentNode.setProperty(SLV_PROPERTY_MINOR, 0);
        SimpleDocument target = converter.fillDocument(documentNode, I18NHelper.defaultLanguage);
        moveMultilangContent(origin, target);
        File currentDocumentDir = new File(target.getDirectoryPath(I18NHelper.defaultLanguage))
            .getParentFile();
        File[] contents = currentDocumentDir.getParentFile().listFiles();
        for (File versionDirectory : contents) {
          if (!versionDirectory.equals(currentDocumentDir)) {
            FileUtils.deleteDirectory(versionDirectory);
          }
        }
      } else {
        documentNode.setProperty(SLV_PROPERTY_VERSIONED, true);
        documentNode.setProperty(SLV_PROPERTY_MAJOR, 1);
        documentNode.setProperty(SLV_PROPERTY_MINOR, 0);
        documentNode.addMixin(MIX_SIMPLE_VERSIONABLE);
        SimpleDocument target = converter.fillDocument(documentNode, I18NHelper.defaultLanguage);
        VersionManager versionManager = documentNode.getSession().getWorkspace().getVersionManager();
        documentNode.getSession().save();
        moveMultilangContent(origin, target);
        versionManager.checkin(documentNode.getPath());
      }
      return new SimpleDocumentPK(documentNode.getIdentifier(), documentPk);
    } catch (ItemNotFoundException infex) {

      return documentPk;
    }
  }

  private void deleteDocumentNode(Node documentNode) throws RepositoryException {
    if (null != documentNode) {
      if (converter.isVersionedMaster(documentNode)) {
        removeHistory(documentNode);
      }
      documentNode.remove();
    }
  }

  /**
   *
   * @param session
   * @param documentPk
   * @param lang
   * @return
   * @throws RepositoryException
   */
  public SimpleDocument findDocumentById(Session session, SimpleDocumentPK documentPk, String lang)
      throws RepositoryException {
    SimpleDocument document = null;
    try {
      Node documentNode = session.getNodeByIdentifier(documentPk.getId());
      document = converter.convertNode(documentNode, lang);
    } catch (ItemNotFoundException infex) {

    }
    return document;
  }

  /**
   *
   * @param session
   * @param instanceId
   * @param oldSilverpeasId
   * @param versioned
   * @param lang
   * @return
   * @throws RepositoryException
   */
  public SimpleDocument findDocumentByOldSilverpeasId(Session session, String instanceId,
      long oldSilverpeasId, boolean versioned, String lang) throws RepositoryException {
    QueryManager manager = session.getWorkspace().getQueryManager();
    QueryObjectModelFactory factory = manager.getQOMFactory();
    Selector source = factory.selector(SLV_SIMPLE_DOCUMENT, SIMPLE_DOCUMENT_ALIAS);
    DescendantNode descendantdNodeConstraint = factory.descendantNode(SIMPLE_DOCUMENT_ALIAS,
        session.getRootNode().getPath() + instanceId);
    Comparison oldSilverpeasIdComparison = factory.comparison(factory.propertyValue(
        SIMPLE_DOCUMENT_ALIAS, SLV_PROPERTY_OLD_ID), QueryObjectModelFactory.JCR_OPERATOR_EQUAL_TO,
        factory.literal(session.getValueFactory().createValue(oldSilverpeasId)));
    Comparison versionedComparison = factory.comparison(factory.propertyValue(SIMPLE_DOCUMENT_ALIAS,
        SLV_PROPERTY_VERSIONED), QueryObjectModelFactory.JCR_OPERATOR_EQUAL_TO, factory.
        literal(session.getValueFactory().createValue(versioned)));

    QueryObjectModel query = factory.createQuery(source, factory.and(descendantdNodeConstraint,
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
   *
   * @param session the current JCR session.
   * @param instanceId the component id containing the documents.
   * @param foreignId the id of the container owning the documents.
   * @return the last document in an instance with the specified foreignId.
   * @throws RepositoryException
   */
  public SimpleDocument findLast(Session session, String instanceId, String foreignId) throws
      RepositoryException {
    NodeIterator iter = selectDocumentsByForeignIdAndType(session, instanceId, foreignId,
        DocumentType.attachment);
    while (iter.hasNext()) {
      Node node = iter.nextNode();
      if (!iter.hasNext()) {
        return converter.convertNode(node, I18NHelper.defaultLanguage);
      }
    }
    return null;
  }

  /**
   * Search all the documents of type attachment in an instance with the specified foreignId.
   *
   * @param session the current JCR session.
   * @param instanceId the component id containing the documents.
   * @param foreignId the id of the container owning the documents.
   * @param language the language in which the documents are required.
   * @return an ordered list of the documents.
   * @throws RepositoryException
   */
  public SimpleDocumentList<SimpleDocument> listDocumentsByForeignId(Session session,
      String instanceId, String foreignId, String language) throws RepositoryException {
    NodeIterator iter = selectDocumentsByForeignIdAndType(session, instanceId, foreignId,
        DocumentType.attachment);
    return converter.convertNodeIterator(iter, language);
  }

  /**
   * Search all the documents of any type in an instance with the specified foreignId.
   *
   * @param session the current JCR session.
   * @param instanceId the component id containing the documents.
   * @param foreignId the id of the container owning the documents.
   * @param language the language in which the documents are required.
   * @return an ordered list of the documents.
   * @throws RepositoryException
   */
  public SimpleDocumentList<SimpleDocument> listAllDocumentsByForeignId(Session session,
      String instanceId, String foreignId, String language) throws RepositoryException {
    NodeIterator iter = selectDocumentsByForeignId(session, instanceId, foreignId);
    return converter.convertNodeIterator(iter, language);
  }

  /**
   * Search all the documents in an instance with the specified foreignId.
   *
   * @param session the current JCR session.
   * @param instanceId the component id containing the documents.
   * @param foreignId the id of the container owning the documents.
   * @param type thetype of required documents.
   * @param language the language in which the documents are required.
   * @return an ordered list of the documents.
   * @throws RepositoryException
   */
  public SimpleDocumentList<SimpleDocument> listDocumentsByForeignIdAndType(Session session,
      String instanceId, String foreignId, DocumentType type, String language)
      throws RepositoryException {
    NodeIterator iter = selectDocumentsByForeignIdAndType(session, instanceId, foreignId, type);
    return converter.convertNodeIterator(iter, language);
  }

  /**
   * Search all the documents in an instance with the specified type.
   *
   * @param session the current JCR session.
   * @param instanceId the component id containing the documents.
   * @param type thetype of required documents.
   * @param language the language in which the documents are required.
   * @return an ordered list of the documents.
   * @throws RepositoryException
   */
  public List<SimpleDocument> listDocumentsByComponentIdAndType(Session session, String instanceId,
      DocumentType type, String language) throws RepositoryException {
    NodeIterator iter = selectDocumentsByComponentIdAndType(session, instanceId, type);
    return converter.convertNodeIterator(iter, language);
  }

  /**
   * Search all the documents related to the component instance identified by the specified
   * identifier.
   *
   * @param session the current JCR session.
   * @param instanceId the component id containing the documents.
   * @param language the language in which the documents are required.
   * @return an ordered list of the documents.
   * @throws RepositoryException
   */
  public List<SimpleDocument> listAllDocumentsByComponentId(Session session, String instanceId,
      String language) throws RepositoryException {
    NodeIterator iter = selectAllDocumentsByComponentId(session, instanceId);
    return converter.convertNodeIterator(iter, language);
  }

  /**
   * Search all the documents in an instance with the specified owner.
   *
   * @param session the current JCR session.
   * @param instanceId the component id containing the documents.
   * @param owner the id of the user owning the document.
   * @param language the language in which the documents are required.
   * @return an ordered list of the documents.
   * @throws RepositoryException
   */
  public List<SimpleDocument> listComponentDocumentsByOwner(Session session, String instanceId,
      String owner, String language) throws RepositoryException {
    NodeIterator iter = selectDocumentsByOwnerIdAndComponentId(session, instanceId, owner);
    return converter.convertNodeIterator(iter, language);
  }

  public List<SimpleDocument> listDocumentsLockedByUser(Session session, String usedId,
      String language) throws
      RepositoryException {
    NodeIterator iter = selectAllDocumentsByOwnerId(session, usedId);
    return converter.convertNodeIterator(iter, language);
  }

  /**
   * Search all the documents in an instance with the specified foreignId.
   *
   * @param session the current JCR session.
   * @param instanceId the component id containing the documents.
   * @param foreignId the id of the container owning the documents.
   * @return an ordered list of the documents.
   * @throws RepositoryException
   */
  NodeIterator selectAllDocumentsByForeignId(Session session, String instanceId, String foreignId)
      throws RepositoryException {
    QueryManager manager = session.getWorkspace().getQueryManager();
    QueryObjectModelFactory factory = manager.getQOMFactory();
    Selector source = factory.selector(SLV_SIMPLE_DOCUMENT, SIMPLE_DOCUMENT_ALIAS);
    DescendantNode descendantdNodeConstraint = factory.descendantNode(SIMPLE_DOCUMENT_ALIAS,
        session.getRootNode().getPath() + instanceId);
    Comparison foreignIdComparison = factory.comparison(factory.propertyValue(SIMPLE_DOCUMENT_ALIAS,
        SLV_PROPERTY_FOREIGN_KEY), QueryObjectModelFactory.JCR_OPERATOR_EQUAL_TO, factory.
        literal(session.getValueFactory().createValue(foreignId)));
    Ordering order = factory.ascending(factory.propertyValue(SIMPLE_DOCUMENT_ALIAS,
        SLV_PROPERTY_ORDER));
    QueryObjectModel query = factory.createQuery(source, factory.and(descendantdNodeConstraint,
        foreignIdComparison), new Ordering[]{order}, null);
    QueryResult result = query.execute();
    return result.getNodes();
  }

  /**
   * Search all the documents of the specified type in an instance with the specified foreignId.
   *
   * @param session the current JCR session.
   * @param instanceId the component id containing the documents.
   * @param foreignId the id of the container owning the documents.
   * @return an ordered list of the documents.
   * @throws RepositoryException
   */
  NodeIterator selectDocumentsByForeignIdAndType(Session session, String instanceId,
      String foreignId, DocumentType type) throws RepositoryException {
    QueryManager manager = session.getWorkspace().getQueryManager();
    QueryObjectModelFactory factory = manager.getQOMFactory();
    Selector source = factory.selector(SLV_SIMPLE_DOCUMENT, SIMPLE_DOCUMENT_ALIAS);
    ChildNode childNodeConstraint = factory.childNode(SIMPLE_DOCUMENT_ALIAS, session.getRootNode().
        getPath() + instanceId + '/' + type.getFolderName());
    Comparison foreignIdComparison = factory.comparison(factory.propertyValue(SIMPLE_DOCUMENT_ALIAS,
        SLV_PROPERTY_FOREIGN_KEY), QueryObjectModelFactory.JCR_OPERATOR_EQUAL_TO, factory.
        literal(session.getValueFactory().createValue(foreignId)));
    Ordering order = factory.ascending(factory.propertyValue(SIMPLE_DOCUMENT_ALIAS,
        SLV_PROPERTY_ORDER));
    QueryObjectModel query = factory.createQuery(source, factory.and(childNodeConstraint,
        foreignIdComparison), new Ordering[]{order}, null);
    QueryResult result = query.execute();
    return result.getNodes();
  }

  /**
   * Search all the documents of tany type in an instance with the specified foreignId.
   *
   * @param session the current JCR session.
   * @param instanceId the component id containing the documents.
   * @param foreignId the id of the container owning the documents.
   * @return an ordered list of the documents.
   * @throws RepositoryException
   */
  NodeIterator selectDocumentsByForeignId(Session session, String instanceId, String foreignId)
      throws RepositoryException {
    QueryManager manager = session.getWorkspace().getQueryManager();
    QueryObjectModelFactory factory = manager.getQOMFactory();
    Selector source = factory.selector(SLV_SIMPLE_DOCUMENT, SIMPLE_DOCUMENT_ALIAS);
    DescendantNode descendantNodeConstraint = factory.descendantNode(SIMPLE_DOCUMENT_ALIAS, session.
        getRootNode().getPath() + instanceId + '/');
    Comparison foreignIdComparison = factory
        .comparison(factory.propertyValue(SIMPLE_DOCUMENT_ALIAS, SLV_PROPERTY_FOREIGN_KEY),
            QueryObjectModelFactory.JCR_OPERATOR_EQUAL_TO, factory.
                literal(session.getValueFactory().createValue(foreignId))
        );
    Ordering order = factory.ascending(factory.propertyValue(SIMPLE_DOCUMENT_ALIAS,
        SLV_PROPERTY_ORDER));
    QueryObjectModel query = factory.createQuery(source, factory.and(descendantNodeConstraint,
        foreignIdComparison), new Ordering[]{order}, null);
    QueryResult result = query.execute();
    return result.getNodes();
  }

  /**
   * Search all the documents of the specified type in an instance with the specified foreignId.
   *
   * @param session the current JCR session.
   * @param instanceId the component id containing the documents.
   * @param type the type of document.
   * @return an ordered list of the documents.
   * @throws RepositoryException
   */
  NodeIterator selectDocumentsByComponentIdAndType(Session session, String instanceId,
      DocumentType type) throws RepositoryException {
    QueryManager manager = session.getWorkspace().getQueryManager();
    QueryObjectModelFactory factory = manager.getQOMFactory();
    Selector source = factory.selector(SLV_SIMPLE_DOCUMENT, SIMPLE_DOCUMENT_ALIAS);
    ChildNode childNodeConstraint = factory.childNode(SIMPLE_DOCUMENT_ALIAS, session.getRootNode().
        getPath() + instanceId + '/' + type.getFolderName());
    Ordering order = factory.ascending(factory.propertyValue(SIMPLE_DOCUMENT_ALIAS,
        SLV_PROPERTY_ORDER));
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
   * @throws RepositoryException
   */
  NodeIterator selectAllDocumentsByComponentId(Session session, String instanceId)
      throws RepositoryException {
    QueryManager manager = session.getWorkspace().getQueryManager();
    QueryObjectModelFactory factory = manager.getQOMFactory();
    Selector source = factory.selector(SLV_SIMPLE_DOCUMENT, SIMPLE_DOCUMENT_ALIAS);
    DescendantNode childNodeConstraint =
        factory.descendantNode(SIMPLE_DOCUMENT_ALIAS, session.getRootNode().
            getPath() + instanceId);
    Ordering order =
        factory.ascending(factory.propertyValue(SIMPLE_DOCUMENT_ALIAS, SLV_PROPERTY_ORDER));
    QueryObjectModel query =
        factory.createQuery(source, childNodeConstraint, new Ordering[]{order}, null);
    QueryResult result = query.execute();
    return result.getNodes();
  }

  /**
   * Search all the documents in an instance which are expiring at the specified date.
   *
   * @param session the current JCR session.
   * @param expiryDate the date when the document reservation should expire.
   * @param language the language in which the documents are required.
   * @return an ordered list of the documents.
   * @throws RepositoryException
   */
  public List<SimpleDocument> listExpiringDocuments(Session session, Date expiryDate,
      String language) throws RepositoryException {
    NodeIterator iter = selectExpiringDocuments(session, DateUtil.getBeginOfDay(expiryDate));
    return converter.convertNodeIterator(iter, language);
  }

  /**
   * Search all the documents in an instance which are locked at the alert date.
   *
   * @param session the current JCR session.
   * @param alertDate the date when the document reservation should send an alert.
   * @param language the language in which the documents are required.
   * @return an ordered list of the documents.
   * @throws RepositoryException
   */
  public List<SimpleDocument> listDocumentsRequiringWarning(Session session, Date alertDate,
      String language) throws RepositoryException {
    NodeIterator iter = selectWarningDocuments(session, DateUtil.getBeginOfDay(alertDate));
    return converter.convertNodeIterator(iter, language);
  }

  /**
   * Search all the documents in an instance expiring at the specified date.
   *
   * @param session the current JCR session.
   * @param expiryDate the date when the document reservation should expire.
   * @return an ordered list of the documents.
   * @throws RepositoryException
   */
  NodeIterator selectExpiringDocuments(Session session, Date expiryDate) throws RepositoryException {
    QueryManager manager = session.getWorkspace().getQueryManager();
    QueryObjectModelFactory factory = manager.getQOMFactory();
    Calendar expiry = Calendar.getInstance();
    expiry.setTime(DateUtil.getBeginOfDay(expiryDate));
    Selector source = factory.selector(SLV_SIMPLE_DOCUMENT, SIMPLE_DOCUMENT_ALIAS);
    Comparison foreignIdComparison = factory.comparison(factory.propertyValue(SIMPLE_DOCUMENT_ALIAS,
        SLV_PROPERTY_EXPIRY_DATE), QueryObjectModelFactory.JCR_OPERATOR_EQUAL_TO, factory.
        literal(session.getValueFactory().createValue(expiry)));
    Ordering order = factory.ascending(factory.propertyValue(SIMPLE_DOCUMENT_ALIAS,
        SLV_PROPERTY_ORDER));
    QueryObjectModel query = factory.createQuery(source, foreignIdComparison, new Ordering[]{order},
        null);
    QueryResult result = query.execute();
    return result.getNodes();
  }

  /**
   * Search all the documents in an instance requiring to be unlocked at the specified date.
   *
   * @param session the current JCR session.
   * @param expiryDate the date when the document reservation should expire.
   * @param language the language in which the documents are required.
   * @return an ordered list of the documents.
   * @throws RepositoryException
   */
  public List<SimpleDocument> listDocumentsToUnlock(Session session, Date expiryDate,
      String language) throws RepositoryException {
    NodeIterator iter = selectDocumentsRequiringUnlocking(session, expiryDate);
    return converter.convertNodeIterator(iter, language);
  }

  /**
   * Search all the documents in an instance requiring to be unlocked at the specified date.
   *
   * @param session the current JCR session.
   * @param expiryDate the date when the document reservation should expire.
   * @return an ordered list of the documents.
   * @throws RepositoryException
   */
  NodeIterator selectDocumentsRequiringUnlocking(Session session, Date expiryDate) throws
      RepositoryException {
    QueryManager manager = session.getWorkspace().getQueryManager();
    QueryObjectModelFactory factory = manager.getQOMFactory();
    Calendar expiry = Calendar.getInstance();
    expiry.setTime(DateUtil.getBeginOfDay(expiryDate));
    Selector source = factory.selector(SLV_SIMPLE_DOCUMENT, SIMPLE_DOCUMENT_ALIAS);
    Comparison foreignIdComparison = factory.comparison(factory.propertyValue(SIMPLE_DOCUMENT_ALIAS,
        SLV_PROPERTY_EXPIRY_DATE), QueryObjectModelFactory.JCR_OPERATOR_LESS_THAN, factory.
        literal(session.getValueFactory().createValue(expiry)));
    Ordering order = factory.ascending(factory.propertyValue(SIMPLE_DOCUMENT_ALIAS,
        SLV_PROPERTY_ORDER));
    QueryObjectModel query = factory.createQuery(source, foreignIdComparison, new Ordering[]{order},
        null);
    QueryResult result = query.execute();
    return result.getNodes();
  }

  /**
   * Search all the documents in an instance in a warning state at the specified date.
   *
   * @param session the current JCR session.
   * @param alertDate the date when a warning is required.
   * @return an ordered list of the documents.
   * @throws RepositoryException
   */
  NodeIterator selectWarningDocuments(Session session, Date alertDate) throws RepositoryException {
    QueryManager manager = session.getWorkspace().getQueryManager();
    QueryObjectModelFactory factory = manager.getQOMFactory();
    Calendar alert = Calendar.getInstance();
    alert.setTime(DateUtil.getBeginOfDay(alertDate));
    Selector source = factory.selector(SLV_SIMPLE_DOCUMENT, SIMPLE_DOCUMENT_ALIAS);
    Comparison foreignIdComparison = factory.comparison(factory.propertyValue(SIMPLE_DOCUMENT_ALIAS,
        SLV_PROPERTY_ALERT_DATE), QueryObjectModelFactory.JCR_OPERATOR_EQUAL_TO, factory.
        literal(session.getValueFactory().createValue(alert)));
    Ordering order = factory.ascending(factory.propertyValue(SIMPLE_DOCUMENT_ALIAS,
        SLV_PROPERTY_ORDER));
    QueryObjectModel query = factory.createQuery(source, foreignIdComparison, new Ordering[]{order},
        null);
    QueryResult result = query.execute();
    return result.getNodes();
  }

  /**
   * Search all the documents in an instance with the specified owner.
   *
   * @param session the current JCR session.
   * @param instanceId the component id containing the documents.
   * @param owner the id of the user owning the documents.
   * @return an ordered list of the documents.
   * @throws RepositoryException
   */
  NodeIterator selectDocumentsByOwnerIdAndComponentId(Session session, String instanceId,
      String owner) throws RepositoryException {
    QueryManager manager = session.getWorkspace().getQueryManager();
    QueryObjectModelFactory factory = manager.getQOMFactory();
    Selector source = factory.selector(SLV_SIMPLE_DOCUMENT, SIMPLE_DOCUMENT_ALIAS);
    ChildNode childNodeConstraint = factory.childNode(SIMPLE_DOCUMENT_ALIAS, session.getRootNode().
        getPath() + instanceId + '/' + DocumentType.attachment.getFolderName());
    Comparison ownerComparison = factory.comparison(factory.propertyValue(SIMPLE_DOCUMENT_ALIAS,
        SLV_PROPERTY_OWNER), QueryObjectModelFactory.JCR_OPERATOR_EQUAL_TO, factory.literal(session.
        getValueFactory().createValue(owner)));
    Ordering order = factory.ascending(factory.propertyValue(SIMPLE_DOCUMENT_ALIAS,
        SLV_PROPERTY_ORDER));
    QueryObjectModel query = factory.createQuery(source, factory.and(childNodeConstraint,
        ownerComparison), new Ordering[]{order}, null);
    QueryResult result = query.execute();
    return result.getNodes();
  }

  /**
   * Search all the documents with the specified owner.
   *
   * @param session the current JCR session.
   * @param owner the id of the user owning the documents.
   * @return an ordered list of the documents.
   * @throws RepositoryException
   */
  NodeIterator selectAllDocumentsByOwnerId(Session session, String owner) throws
      RepositoryException {
    QueryManager manager = session.getWorkspace().getQueryManager();
    QueryObjectModelFactory factory = manager.getQOMFactory();
    Selector source = factory.selector(SLV_SIMPLE_DOCUMENT, SIMPLE_DOCUMENT_ALIAS);
    Comparison ownerComparison = factory.comparison(factory.propertyValue(SIMPLE_DOCUMENT_ALIAS,
        SLV_PROPERTY_OWNER), QueryObjectModelFactory.JCR_OPERATOR_EQUAL_TO, factory.literal(session.
        getValueFactory().createValue(owner)));
    Ordering order = factory.ascending(factory.propertyValue(SIMPLE_DOCUMENT_ALIAS,
        SLV_PROPERTY_ORDER));
    QueryObjectModel query = factory.createQuery(source, ownerComparison, new Ordering[]{order},
        null);
    QueryResult result = query.execute();
    return result.getNodes();
  }

  /**
   * Add the content.
   *
   * @param session the current JCR session.
   * @param documentPk the document which content is to be added.
   * @param attachment the attachment metadata.
   * @throws RepositoryException
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
   *
   * @param session the current JCR session.
   * @param pk the document which content is to be added.
   * @param lang the content language.
   * @return the attachment binary content.
   * @throws RepositoryException
   * @throws IOException
   */
  public InputStream getContent(Session session, SimpleDocumentPK pk, String lang) throws
      RepositoryException, IOException {
    Node docNode = session.getNodeByIdentifier(pk.getId());
    String language = lang;
    if (!StringUtil.isDefined(language)) {
      language = I18NHelper.defaultLanguage;
    }
    SimpleDocument document = converter.fillDocument(docNode, language);
    return new BufferedInputStream(
        FileUtils.openInputStream(new File(document.getAttachmentPath())));
  }

  /**
   * Remove the content for the specified language.
   * If no other content exists, then the document node is deleted.
   *
   * @param session the current JCR session.
   * @param documentPk the document which content is to be removed.
   * @param language the language of the content which is to be removed.
   * @return false if the document has no child node after the content remove, true otherwise.
   * @throws RepositoryException
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
   * Lock a document if it is versionned to create a new work in progress version.
   *
   * @param session
   * @param document
   * @param owner the user locking the node.
   * @return true if node has be checked out - false otherwise.
   * @throws RepositoryException
   */
  public boolean lock(Session session, SimpleDocument document, String owner) throws
      RepositoryException {
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
   * Unlock a document if it is versionned to create a new version or to restore a previous one.
   * By using this method, the metadata of the content are always updated.
   *
   * @param session the current JCR open session to perform actions.
   * @param document the document data from which all needed identifiers are retrieved.
   * @param restore true to restore the previous version if any.
   * @return the result of {@link #unlock(Session, SimpleDocument, boolean, boolean)} execution.
   * @throws RepositoryException
   */
  public SimpleDocument unlock(Session session, SimpleDocument document, boolean restore)
      throws RepositoryException {
    return unlock(session, document, restore, false);
  }

  /**
   * Unlock a document if it is versionned from a context into which a language content has just
   * been deleted. This method does not update the metadata of the content in order to obtain an
   * efficient content deletion.
   *
   * @param session the current JCR open session to perform actions.
   * @param document the document data from which all needed identifiers are retrieved.
   * @return the result of {@link #unlock(Session, SimpleDocument, boolean, boolean)} execution.
   * @throws RepositoryException
   */
  public SimpleDocument unlockFromContentDeletion(Session session, SimpleDocument document)
      throws RepositoryException {
    return unlock(session, document, false, true);
  }

  /**
   * Unlock a document if it is versionned to create a new version or to restore a previous one.
   *
   * @param session the current JCR open session to perform actions.
   * @param document the document data from which all needed identifiers are retrieved.
   * @param restore true to restore the previous version if any.
   * @param skipContentMetadataUpdate false to update the metadata of the content {@link
   * SimpleDocument#getAttachment()}.
   * @return the document updated.
   * @throws RepositoryException
   */
  private SimpleDocument unlock(Session session, SimpleDocument document, boolean restore,
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
        VersionIterator iter = session.getWorkspace().getVersionManager().
            getVersionHistory(document.getFullJcrPath()).getAllVersions();
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
   * Check the document out.
   *
   * @param node the node to checkout.
   * @param owner the user checkouting the node.
   * @throws RepositoryException
   */
  void checkoutNode(Node node, String owner) throws RepositoryException {
    node.getSession().getWorkspace().getVersionManager().checkout(node.getPath());
    converter.addStringProperty(node, SLV_PROPERTY_OWNER, owner);
  }

  /**
   * Check the document in.
   *
   * @param documentNode the node to checkin.
   * @param isMajor true if the new version is a major one - false otherwise.
   * @return the document for this new version.
   * @throws RepositoryException
   */
  SimpleDocument checkinNode(Node documentNode, String lang, boolean isMajor) throws
      RepositoryException {
    VersionManager versionManager = documentNode.getSession().getWorkspace().getVersionManager();
    String versionLabel = converter.updateVersion(documentNode, lang, isMajor);
    documentNode.getSession().save();
    Version lastVersion = versionManager.checkin(documentNode.getPath());
    lastVersion.getContainingHistory().addVersionLabel(lastVersion.getName(), versionLabel, false);
    SimpleDocument doc = converter.convertNode(documentNode, lang);
    return doc;
  }

  /**
   * Add the version feature to an existing document. If the document has already the version
   * feature, nothing is done.
   *
   * @param session
   * @param documentPk
   * @throws RepositoryException
   */
  public void setVersionnable(Session session, SimpleDocumentPK documentPk) throws
      RepositoryException {
    Node documentNode = session.getNodeByIdentifier(documentPk.getId());
    if (!converter.isVersionedMaster(documentNode)) {
      documentNode.addMixin(MIX_SIMPLE_VERSIONABLE);
      documentNode.setProperty(SLV_PROPERTY_VERSIONED, true);
    }
  }

  /**
   * Remove the version feature to an existing document. If the document doesn't have already the
   * version feature, nothing is done.
   *
   * @param session
   * @param documentPk
   * @throws RepositoryException
   */
  public void removeVersionnable(Session session, SimpleDocumentPK documentPk) throws
      RepositoryException {
    Node documentNode = session.getNodeByIdentifier(documentPk.getId());
    if (converter.isVersionedMaster(documentNode)) {
      removeHistory(documentNode);
      VersionHistory history = documentNode.getSession().getWorkspace().getVersionManager().
          getVersionHistory(documentNode.getPath());
      history.remove();

      documentNode.removeMixin(MIX_SIMPLE_VERSIONABLE);
    }
    documentNode.setProperty(SLV_PROPERTY_VERSIONED, false);
  }

  void removeHistory(Node documentNode) throws RepositoryException {
    VersionHistory history = documentNode.getSession().getWorkspace().getVersionManager().
        getVersionHistory(documentNode.getPath());
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

  public long storeContent(SimpleDocument document, InputStream in, boolean update) throws
      RepositoryException, IOException {
    File file = new File(document.getAttachmentPath());
    if (update) {
      File parentFile = file.getParentFile();
      if (parentFile.isDirectory() && parentFile.list().length > 0) {
        FileUtils.deleteQuietly(parentFile);
        FileUtils.forceMkdir(parentFile);
      }
    }
    FileUtils.copyInputStreamToFile(in, file);
    return file.length();
  }

  public long storeContent(SimpleDocument document, InputStream in) throws
      RepositoryException, IOException {
    return storeContent(document, in, false);
  }

  public void duplicateContent(SimpleDocument origin, SimpleDocument document)
      throws IOException, RepositoryException {
    String originDir = origin.getDirectoryPath(null);
    String targetDir = document.getDirectoryPath(null);
    targetDir = targetDir.replace('/', File.separatorChar);
    File target = new File(targetDir).getParentFile();
    File source = new File(originDir).getParentFile();
    if (!source.exists() || !source.isDirectory() || source.listFiles() == null) {
      return;
    }
    if (!target.exists()) {
      target.mkdir();
    }
    for (File langDir : source.listFiles()) {
      File targetLangDir = new File(target, langDir.getName());
      if (!targetLangDir.exists()) {
        FileUtils.copyDirectory(langDir, targetLangDir);
      }
    }
  }

  public void deleteContent(Node documentNode, String instanceId) throws RepositoryException {
    String directory = FileRepositoryManager.getAbsolutePath(instanceId) + documentNode.getName();
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
    Set<String> existingAttachements = new HashSet<String>(I18NHelper.getNumberOfLanguages());
    for (Node child : new NodeIterable(originalNode.getNodes())) {
      existingAttachements.add(child.getName());
    }
    Node cloneNode = session.getNodeByIdentifier(clone.getId());
    for (Node child : new NodeIterable(cloneNode.getNodes())) {
      String childNodeName = child.getName();
      if (existingAttachements.contains(childNodeName) && originalNode.hasNode(childNodeName)) {
        copyNode(session, child, originalNode.getNode(childNodeName));
        existingAttachements.remove(childNodeName);
      } else {
        session.move(child.getPath(), originalNode.getPath() + '/' + childNodeName);
      }
    }
    for (String deletedNode : existingAttachements) {
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
}
