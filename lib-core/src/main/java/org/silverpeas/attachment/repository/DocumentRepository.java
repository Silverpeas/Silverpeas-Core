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

import com.silverpeas.jcrutil.JcrConstants;
import com.silverpeas.util.i18n.I18NHelper;
import java.io.InputStream;
import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.query.QueryManager;
import javax.jcr.query.QueryResult;
import javax.jcr.query.qom.ChildNode;
import javax.jcr.query.qom.Ordering;
import javax.jcr.query.qom.QueryObjectModel;
import javax.jcr.query.qom.QueryObjectModelFactory;
import javax.jcr.query.qom.Selector;
import org.silverpeas.attachment.model.SimpleDocument;
import org.silverpeas.attachment.model.SimpleDocumentPK;

/**
 *
 * @author ehugonnet
 */
public class DocumentRepository {
  SimpleDocumentConverter converter = new SimpleDocumentConverter();
  /**
   * Create file attached to an object who is identified by "PK" AttachmentDetail object contains an
   * attribute who identifie the link by a foreign key.
   *
   * @param document
   * @param content
   * @param callBack
   * @return
   */
  public SimpleDocumentPK createAttachment(Session session, SimpleDocument document,
      InputStream content, boolean callBack) throws RepositoryException {
    SimpleDocument last = findLast(session, document);
    if (last != null) {
      document.setOrder(last.getOrder() + 1);
    }
    Node componentNode = session.getRootNode().getNode(document.getInstanceId());
    Node contextFolderNode = componentNode;
    Node documentNode = contextFolderNode.addNode(document.getNodeName(), JcrConstants.SLV_SIMPLE_DOCUMENT);
    converter.fillNode(document, documentNode);
    session.save();
    document.setId(documentNode.getIdentifier());
    return document.getPk();
  }

  public SimpleDocument findLast(Session session, SimpleDocument document) throws
      RepositoryException {
    QueryManager manager = session.getWorkspace().getQueryManager();
    QueryObjectModelFactory factory = manager.getQOMFactory();
    Selector source = factory.selector(JcrConstants.SLV_SIMPLE_DOCUMENT, "docs");
    ChildNode childNodeConstraint = factory.childNode("docs", session.getRootNode().getPath() + '/'
        + document.getInstanceId() + '/' + document.getNodeName());
    Ordering order = factory.descending(factory.propertyValue("docs",
        JcrConstants.SLV_PROPERTY_ORDER));
    QueryObjectModel query = factory.createQuery(source, childNodeConstraint, new Ordering[]{order},
        null);
    QueryResult result = query.execute();
    NodeIterator iter = result.getNodes();
    if (iter.hasNext()) {
      return converter.convertNode(iter.nextNode(), I18NHelper.defaultLanguage);
    }
    return null;
  }
}
