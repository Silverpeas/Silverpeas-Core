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

import com.silverpeas.util.i18n.I18NHelper;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
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
public class DocumentRepository {

  SimpleDocumentConverter converter = new SimpleDocumentConverter();

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
    Node componentNode = session.getRootNode().getNode(document.getInstanceId());
    Node contextFolderNode = componentNode;
    Node documentNode = contextFolderNode.addNode(document.getNodeName(), SLV_SIMPLE_DOCUMENT);
    converter.fillNode(document, content, documentNode);
    session.save();
    document.setId(documentNode.getIdentifier());
    return document.getPk();
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
    session.save();
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
      if (componentNode != null) {
        return converter.convertNode(componentNode, lang);
      }
    } catch (ItemNotFoundException infex) {
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
    if (iter.hasNext()) {
      return converter.convertNode(iter.nextNode(), I18NHelper.defaultLanguage);
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
        + instanceId);
    Comparison foreignIdComparison = factory.comparison(factory.propertyValue(alias,
        SLV_PROPERTY_FOREIGN_KEY), QueryObjectModelFactory.JCR_OPERATOR_EQUAL_TO, factory.
        literal(session.getValueFactory().createValue(foreignId)));
    Ordering order = factory.descending(factory.propertyValue(alias, SLV_PROPERTY_ORDER));
    QueryObjectModel query = factory.createQuery(source, factory.and(childNodeConstraint,
        foreignIdComparison), new Ordering[]{order}, null);
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
        + instanceId);
    Comparison foreignIdComparison = factory.comparison(factory.propertyValue(alias,
        SLV_PROPERTY_OWNER), QueryObjectModelFactory.JCR_OPERATOR_EQUAL_TO, factory.literal(session.
        getValueFactory().createValue(owner)));
    Ordering order = factory.descending(factory.propertyValue(alias, SLV_PROPERTY_ORDER));
    QueryObjectModel query = factory.createQuery(source, factory.and(childNodeConstraint,
        foreignIdComparison), new Ordering[]{order}, null);
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
    try {
      Node componentNode = session.getNodeByIdentifier(documentPk.getId());
      if (componentNode != null) {
        converter.addAttachment(componentNode, attachment, content);
      }
    } catch (ItemNotFoundException infex) {
    }
  }

  /**
   * Remove the content for he specified language.
   *
   * @param session the current JCR session.
   * @param documentPk the document which content is to be removed.
   * @param language the language of the content which is to be removed.
   * @throws RepositoryException
   */
  public void removeContent(Session session, SimpleDocumentPK documentPk,
      String language) throws RepositoryException {
    try {
      Node componentNode = session.getNodeByIdentifier(documentPk.getId());
      if (componentNode != null) {
        converter.removeAttachment(componentNode, language);
      }
    } catch (ItemNotFoundException infex) {
    }
  }
}
