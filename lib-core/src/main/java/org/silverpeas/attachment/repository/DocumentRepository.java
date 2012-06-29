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
package org.silverpeas.attachment.repository;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import com.silverpeas.jcrutil.BasicDaoFactory;
import com.silverpeas.util.ArrayUtil;
import com.silverpeas.util.StringUtil;
import com.silverpeas.util.i18n.I18NHelper;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.util.DateUtil;
import com.stratelia.webactiv.util.WAPrimaryKey;
import javax.inject.Named;
import javax.jcr.ItemNotFoundException;
import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.query.QueryManager;
import javax.jcr.query.QueryResult;
import javax.jcr.query.qom.ChildNode;
import javax.jcr.query.qom.Comparison;
import javax.jcr.query.qom.Ordering;
import javax.jcr.query.qom.QueryObjectModel;
import javax.jcr.query.qom.QueryObjectModelFactory;
import javax.jcr.query.qom.Selector;
import org.silverpeas.attachment.model.SimpleAttachment;
import org.silverpeas.attachment.model.SimpleDocument;
import org.silverpeas.attachment.model.SimpleDocumentPK;

import static com.silverpeas.jcrutil.JcrConstants.*;

/**
 *
 * @author ehugonnet
 */
@Named("documentRepository")
public class DocumentRepository {

  SimpleDocumentConverter converter = new SimpleDocumentConverter();

  public void prepareComponentAttachments(String instanceId) throws
      RepositoryException {
    Session session = BasicDaoFactory.getSystemSession();
    try {
      prepareComponentAttachments(session, instanceId);
      session.save();
    } finally {
      BasicDaoFactory.logout(session);
    }
  }

  protected Node prepareComponentAttachments(Session session, String instanceId) throws
      RepositoryException {
    Node targetInstanceNode = converter.getFolder(session.getRootNode(), instanceId);
    return converter.getFolder(targetInstanceNode, "attachments");
  }

  /**
   * Create file attached to an object who is identified by "PK" SimpleDocument object contains an
   * attribute who identifie the link by a foreign key.
   *
   * @param document
   * @param content
   * @param callBack
   * @return
   */
  public SimpleDocumentPK createDocument(Session session, SimpleDocument document,
      InputStream content) throws RepositoryException {
    SimpleDocument last = findLast(session, document.getInstanceId(), document.getForeignId());
    if (last != null && document.getOrder() <= 0) {
      document.setOrder(last.getOrder() + 1);
    }
    Node docsNode = prepareComponentAttachments(session, document.getInstanceId());
    Node documentNode = docsNode.addNode(document.getNodeName(), SLV_SIMPLE_DOCUMENT);
    converter.fillNode(document, content, documentNode);
    document.setId(documentNode.getIdentifier());
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
    prepareComponentAttachments(session, destination.getInstanceId());
    Node originDocumentNode = session.getNodeByIdentifier(document.getPk().getId());
    session.move(originDocumentNode.getPath(), targetDoc.getFullJcrPath());
    Node targetDocumentNode = session.getNode(targetDoc.getFullJcrPath());
    converter.addStringProperty(targetDocumentNode, SLV_PROPERTY_FOREIGN_KEY, destination.getId());
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
    prepareComponentAttachments(destination.getInstanceId());
    SimpleDocumentPK pk = new SimpleDocumentPK(null, destination.getInstanceId());
    SimpleDocument targetDoc = findDocumentById(session, document.getPk(), document.getLanguage());
    targetDoc.setPK(pk);
    targetDoc.setForeignId(destination.getId());
    session.getWorkspace().copy(document.getFullJcrPath(), targetDoc.getFullJcrPath());
    Node copy = session.getNode(targetDoc.getFullJcrPath());
    copy.setProperty(SLV_PROPERTY_OLD_ID, targetDoc.getOldSilverpeasId());
    copy.setProperty(SLV_PROPERTY_FOREIGN_KEY, destination.getId());
    pk.setId(copy.getIdentifier());
    return pk;
  }

  /**
   * Create file attached to an object who is identified by "PK" SimpleDocument object contains an
   * attribute who identifie the link by a foreign key.
   *
   * @param document
   * @param content
   * @param callBack
   * @return
   */
  public void updateDocument(Session session, SimpleDocument document) throws
      RepositoryException {
    Node documentNode = session.getNodeByIdentifier(document.getPk().getId());
    converter.fillNode(document, null, documentNode);
  }

  /**
   * Delete a file attached to an object who is identified by "PK" SimpleDocument object contains an
   * attribute who identifie the link by a foreign key.
   *
   * @param session
   * @param document
   * @throws RepositoryException
   */
  public void deleteDocument(Session session, SimpleDocumentPK documentPk) throws
      RepositoryException {
    try {
      Node componentNode = session.getNodeByIdentifier(documentPk.getId());
      if (componentNode != null) {
        componentNode.remove();
      }
    } catch (ItemNotFoundException infex) {
      SilverTrace.info("attachment", "DocumentRepository.deleteDocument()", "", infex);
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
    try {
      Node componentNode = session.getNodeByIdentifier(documentPk.getId());
      return converter.convertNode(componentNode, lang);
    } catch (ItemNotFoundException infex) {
      SilverTrace.info("attachment", "DocumentRepository.findDocumentById()", "", infex);
    }
    return null;
  }

  /**
   *
   * @param session
   * @param documentPk
   * @param lang
   * @return
   * @throws RepositoryException
   */
  public SimpleDocument findDocumentByOldSilverpeasId(Session session, String instanceId,
      long oldSilverpeasId, boolean versioned, String lang) throws RepositoryException {
    QueryManager manager = session.getWorkspace().getQueryManager();
    QueryObjectModelFactory factory = manager.getQOMFactory();
    final String alias = "SimpleDocuments";
    Selector source = factory.selector(SLV_SIMPLE_DOCUMENT, alias);
    ChildNode childNodeConstraint = factory.childNode(alias, session.getRootNode().getPath()
        + instanceId + "/attachments");
    Comparison oldSilverpeasIdComparison = factory.comparison(factory.propertyValue(alias,
        SLV_PROPERTY_OLD_ID), QueryObjectModelFactory.JCR_OPERATOR_EQUAL_TO, factory.
        literal(session.getValueFactory().createValue(oldSilverpeasId)));
    Comparison versionedComparison = factory.comparison(factory.propertyValue(alias,
        SLV_PROPERTY_VERSIONED), QueryObjectModelFactory.JCR_OPERATOR_EQUAL_TO, factory.
        literal(session.getValueFactory().createValue(versioned)));

    QueryObjectModel query = factory.createQuery(source, factory.and(childNodeConstraint,
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
    NodeIterator iter = selectDocumentsByForeignId(session, instanceId, foreignId);
    while (iter.hasNext()) {
      Node node = iter.nextNode();
      if (!iter.hasNext()) {
        return converter.convertNode(node, I18NHelper.defaultLanguage);
      }
    }
    return null;
  }

  /**
   * Search all the documents in an instance with the specified foreignId.
   *
   * @param session the current JCR session.
   * @param instanceId the component id containing the documents.
   * @param foreignId the id of the container owning the documents.
   * @param language the language in which the documents are required.
   * @return an ordered list of the documents.
   * @throws RepositoryException
   * @throws RepositoryException
   */
  public List<SimpleDocument> listDocumentsByForeignId(Session session, String instanceId,
      String foreignId, String language) throws RepositoryException {
    List<SimpleDocument> result = new ArrayList<SimpleDocument>();
    NodeIterator iter = selectDocumentsByForeignId(session, instanceId, foreignId);
    while (iter.hasNext()) {
      result.add(converter.convertNode(iter.nextNode(), language));
    }
    return result;
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
   * @throws RepositoryException
   */
  public List<SimpleDocument> listDocumentsByOwner(Session session, String instanceId,
      String owner, String language) throws RepositoryException {
    List<SimpleDocument> result = new ArrayList<SimpleDocument>();
    NodeIterator iter = selectDocumentsByOwnerId(session, instanceId, owner);
    while (iter.hasNext()) {
      result.add(converter.convertNode(iter.nextNode(), language));
    }
    return result;
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
  NodeIterator selectDocumentsByForeignId(Session session, String instanceId, String foreignId)
      throws RepositoryException {
    QueryManager manager = session.getWorkspace().getQueryManager();
    QueryObjectModelFactory factory = manager.getQOMFactory();
    final String alias = "SimpleDocuments";
    Selector source = factory.selector(SLV_SIMPLE_DOCUMENT, alias);
    ChildNode childNodeConstraint = factory.childNode(alias, session.getRootNode().getPath()
        + instanceId + "/attachments");
    Comparison foreignIdComparison = factory.comparison(factory.propertyValue(alias,
        SLV_PROPERTY_FOREIGN_KEY), QueryObjectModelFactory.JCR_OPERATOR_EQUAL_TO, factory.
        literal(session.getValueFactory().createValue(foreignId)));
    Ordering order = factory.ascending(factory.propertyValue(alias, SLV_PROPERTY_ORDER));
    QueryObjectModel query = factory.createQuery(source, factory.and(childNodeConstraint,
        foreignIdComparison), new Ordering[]{order}, null);
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
   * @throws RepositoryException.
   */
  public List<SimpleDocument> listExpiringDocuments(Session session, Date expiryDate,
      String language) throws RepositoryException {
    List<SimpleDocument> result = new ArrayList<SimpleDocument>();
    NodeIterator iter = selectExpiringDocuments(session, DateUtil.getBeginOfDay(
        expiryDate));
    while (iter.hasNext()) {
      result.add(converter.convertNode(iter.nextNode(), language));
    }
    return result;
  }

  /**
   * Search all the documents in an instance which are locked at the alert date.
   *
   * @param session the current JCR session.
   * @param alertDate the date when the document reservation should send an alert.
   * @param language the language in which the documents are required.
   * @return an ordered list of the documents.
   * @throws RepositoryException
   * @throws RepositoryException
   */
  public List<SimpleDocument> listDocumentsRequiringWarning(Session session, Date alertDate,
      String language) throws RepositoryException {
    List<SimpleDocument> result = new ArrayList<SimpleDocument>();
    NodeIterator iter = selectWarningDocuments(session, DateUtil.getBeginOfDay(
        alertDate));
    while (iter.hasNext()) {
      result.add(converter.convertNode(iter.nextNode(), language));
    }
    return result;
  }

  /**
   * Search all the documents in an instance expirying at the specified date.
   *
   * @param session the current JCR session.
   * @param expiryDate the date when the document reservation should expire.
   * @return an ordered list of the documents.
   * @throws RepositoryException
   */
  NodeIterator selectExpiringDocuments(Session session, Date expiryDate) throws RepositoryException {
    QueryManager manager = session.getWorkspace().getQueryManager();
    QueryObjectModelFactory factory = manager.getQOMFactory();
    final String alias = "SimpleDocuments";
    Calendar expiry = Calendar.getInstance();
    expiry.setTime(DateUtil.getBeginOfDay(expiryDate));
    Selector source = factory.selector(SLV_SIMPLE_DOCUMENT, alias);
    Comparison foreignIdComparison = factory.comparison(factory.propertyValue(alias,
        SLV_PROPERTY_EXPIRY_DATE), QueryObjectModelFactory.JCR_OPERATOR_EQUAL_TO, factory.
        literal(session.getValueFactory().createValue(expiry)));
    Ordering order = factory.ascending(factory.propertyValue(alias, SLV_PROPERTY_ORDER));
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
   * @throws RepositoryException.
   */
  public List<SimpleDocument> listDocumentsToUnlock(Session session, Date expiryDate,
      String language) throws RepositoryException {
    List<SimpleDocument> result = new ArrayList<SimpleDocument>();
    NodeIterator iter = selectDocumentsRequiringUnlocking(session, expiryDate);
    while (iter.hasNext()) {
      result.add(converter.convertNode(iter.nextNode(), language));
    }
    return result;
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
    final String alias = "SimpleDocuments";
    Calendar expiry = Calendar.getInstance();
    expiry.setTime(DateUtil.getBeginOfDay(expiryDate));
    Selector source = factory.selector(SLV_SIMPLE_DOCUMENT, alias);
    Comparison foreignIdComparison = factory.comparison(factory.propertyValue(alias,
        SLV_PROPERTY_EXPIRY_DATE), QueryObjectModelFactory.JCR_OPERATOR_LESS_THAN, factory.
        literal(session.getValueFactory().createValue(expiry)));
    Ordering order = factory.ascending(factory.propertyValue(alias, SLV_PROPERTY_ORDER));
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
    final String alias = "SimpleDocuments";
    Calendar alert = Calendar.getInstance();
    alert.setTime(DateUtil.getBeginOfDay(alertDate));
    Selector source = factory.selector(SLV_SIMPLE_DOCUMENT, alias);
    Comparison foreignIdComparison = factory.comparison(factory.propertyValue(alias,
        SLV_PROPERTY_ALERT_DATE), QueryObjectModelFactory.JCR_OPERATOR_EQUAL_TO, factory.
        literal(session.getValueFactory().createValue(alert)));
    Ordering order = factory.ascending(factory.propertyValue(alias, SLV_PROPERTY_ORDER));
    QueryObjectModel query = factory.createQuery(source, foreignIdComparison, new Ordering[]{order},
        null);
    QueryResult result = query.execute();
    return result.getNodes();
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
  NodeIterator selectDocumentsByOwnerId(Session session, String instanceId,
      String owner) throws RepositoryException {
    QueryManager manager = session.getWorkspace().getQueryManager();
    QueryObjectModelFactory factory = manager.getQOMFactory();
    final String alias = "SimpleDocuments";
    Selector source = factory.selector(SLV_SIMPLE_DOCUMENT, alias);
    ChildNode childNodeConstraint = factory.childNode(alias, session.getRootNode().getPath()
        + instanceId + "/attachments");
    Comparison ownerComparison = factory.comparison(factory.propertyValue(alias,
        SLV_PROPERTY_OWNER), QueryObjectModelFactory.JCR_OPERATOR_EQUAL_TO, factory.literal(session.
        getValueFactory().createValue(owner)));
    Ordering order = factory.ascending(factory.propertyValue(alias, SLV_PROPERTY_ORDER));
    QueryObjectModel query = factory.createQuery(source, factory.and(childNodeConstraint,
        ownerComparison), new Ordering[]{order}, null);
    QueryResult result = query.execute();
    return result.getNodes();
  }

  /**
   * Add the content.
   *
   * @param session the current JCR session.
   * @param documentPk the document which content is to be added.
   * @param attachment the attachment metadata.
   * @param content the attachment binary content.
   * @throws RepositoryException
   */
  public void addContent(Session session, SimpleDocumentPK documentPk, SimpleAttachment attachment,
      InputStream content) throws RepositoryException {
    Node componentNode = session.getNodeByIdentifier(documentPk.getId());
    converter.addAttachment(componentNode, attachment, content);
  }

  /**
   * Get the content.
   *
   * @param session the current JCR session.
   * @param documentPk the document which content is to be added.
   * @param attachment the attachment metadata.
   * @param content the attachment binary content.
   * @throws RepositoryException
   */
  public InputStream getContent(Session session, SimpleDocumentPK pk, String lang) throws
      RepositoryException, IOException {
    Node docNode = session.getNodeByIdentifier(pk.getId());
    String language = lang;
    if (!StringUtil.isDefined(language)) {
      language = I18NHelper.defaultLanguage;
    }
    String fileNodeName = SimpleDocument.FILE_PREFIX + language;
    if (docNode.hasNode(fileNodeName)) {
      Node fileNode = docNode.getNode(fileNodeName);
      return new BinaryInputStream(fileNode.getPath());
    }
    return new ByteArrayInputStream(ArrayUtil.EMPTY_BYTE_ARRAY);
  }

  /**
   * Remove the content for he specified language.
   *
   * @param session the current JCR session.
   * @param documentPk the document which content is to be removed.
   * @param language the language of the content which is to be removed.
   * @throws RepositoryException
   */
  public void removeContent(Session session, SimpleDocumentPK documentPk, String language) throws
      RepositoryException {
    Node componentNode = session.getNodeByIdentifier(documentPk.getId());
    converter.removeAttachment(componentNode, language);
    componentNode = session.getNodeByIdentifier(documentPk.getId());
    if (!componentNode.hasNodes()) {
      componentNode.remove();
    }
  }
}
