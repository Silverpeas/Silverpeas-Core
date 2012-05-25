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
import java.io.InputStream;
import java.util.Iterator;
import javax.jcr.Node;
import javax.jcr.RepositoryException;
import org.silverpeas.attachment.model.SimpleAttachment;
import org.silverpeas.attachment.model.SimpleDocument;
import org.silverpeas.attachment.model.SimpleDocumentPK;
import org.silverpeas.util.jcr.AbstractJcrConverter;

import static com.silverpeas.jcrutil.JcrConstants.*;

/**
 *
 * @author ehugonnet
 */
class SimpleDocumentConverter extends AbstractJcrConverter {

  SimpleAttachmentConverter attachmentConverter = new SimpleAttachmentConverter();

  public SimpleDocument convertNode(Node node, String lang) throws RepositoryException {
    SimpleDocumentPK pk = new SimpleDocumentPK(node.getIdentifier(), getStringProperty(node,
        SLV_PROPERTY_INSTANCEID));
    long oldSilverpeasId = getLongProperty(node, SLV_PROPERTY_OLD_ID);
    pk.setOldSilverpeasId(oldSilverpeasId);
    String language = lang;
    if (language == null) {
      language = I18NHelper.defaultLanguage;
    }

    SimpleAttachment file = getAttachment(node, language);
    if (file == null) {
      Iterator<String> iter = I18NHelper.getLanguages();
      while (iter.hasNext() && file == null) {
        file = getAttachment(node, iter.next());
      }
    }
    return new SimpleDocument(pk, getStringProperty(node, SLV_PROPERTY_FOREIGN_KEY), getIntProperty(
        node, SLV_PROPERTY_ORDER), getBooleanProperty(node, SLV_PROPERTY_VERSIONED),
        getStringProperty(node, SLV_PROPERTY_OWNER), getDateProperty(node,
        SLV_PROPERTY_RESERVATION_DATE), getDateProperty(node, SLV_PROPERTY_ALERT_DATE),
        getDateProperty(node, SLV_PROPERTY_EXPIRY_DATE),
        getStringProperty(node, SLV_PROPERTY_STATUS), file);
  }

  protected SimpleAttachment getAttachment(Node node, String language) throws RepositoryException {
    String attachmentNodeName = SimpleDocument.FILE_PREFIX + language;
    if (node.hasNode(attachmentNodeName)) {
      return attachmentConverter.convertNode(node.getNode(attachmentNodeName));
    }
    return null;
  }

  public void fillNode(SimpleDocument document, Node documentNode) throws RepositoryException {
    setDocumentNodeProperties(document, documentNode);
    Node attachmentNode = getAttachmentNode(document.getFile().getNodeName(), documentNode);
    attachmentConverter.fillNode(document.getFile(), attachmentNode);
  }

  private void setDocumentNodeProperties(SimpleDocument document, Node documentNode) throws
      RepositoryException {
    addStringProperty(documentNode, SLV_PROPERTY_FOREIGN_KEY, document.getForeignId());
    documentNode.setProperty(SLV_PROPERTY_VERSIONED, document.isVersioned());
    documentNode.setProperty(SLV_PROPERTY_ORDER, document.getOrder());
    documentNode.setProperty(SLV_PROPERTY_OLD_ID, document.getOldSilverpeasId());
    addStringProperty(documentNode, SLV_PROPERTY_INSTANCEID, document.getInstanceId());
    addStringProperty(documentNode, SLV_PROPERTY_OWNER, document.getEditedBy());
    addStringProperty(documentNode, SLV_PROPERTY_STATUS, document.getStatus());
    addDateProperty(documentNode, SLV_PROPERTY_ALERT_DATE, document.getAlert());
    addDateProperty(documentNode, SLV_PROPERTY_EXPIRY_DATE, document.getExpiry());
    addDateProperty(documentNode, SLV_PROPERTY_RESERVATION_DATE, document.getReservation());
  }

  private Node getAttachmentNode(String attachmentNodeName, Node documentNode) throws
      RepositoryException {
    Node attachmentNode;
    if (documentNode.hasNode(attachmentNodeName)) {
      attachmentNode = documentNode.getNode(attachmentNodeName);
    } else {
      attachmentNode = documentNode.addNode(attachmentNodeName, SLV_SIMPLE_ATTACHMENT);
    }
    return attachmentNode;
  }

  void fillNode(SimpleDocument document, InputStream content, Node documentNode) throws
      RepositoryException {
    setDocumentNodeProperties(document, documentNode);
    if (content != null) {
      Node attachmentNode = getAttachmentNode(document.getFile().getNodeName(), documentNode);
      attachmentConverter.fillNode(document.getFile(), attachmentNode);
      attachmentConverter.setContent(attachmentNode, content, document.getContentType());
    }
  }

  public void addAttachment(Node documentNode, SimpleAttachment attachment, InputStream content)
      throws RepositoryException {
    if (content != null) {
      Node attachmentNode = getAttachmentNode(attachment.getNodeName(), documentNode);
      attachmentConverter.fillNode(attachment, attachmentNode);
      attachmentConverter.setContent(attachmentNode, content, attachment.getContentType());
    }
  }

  public void removeAttachment(Node documentNode, String language) throws RepositoryException {
    Node attachmentNode = getAttachmentNode(SimpleDocument.FILE_PREFIX + language, documentNode);
    attachmentNode.remove();
  }
}
