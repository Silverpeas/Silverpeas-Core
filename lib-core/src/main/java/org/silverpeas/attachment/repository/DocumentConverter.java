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

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.version.Version;
import javax.jcr.version.VersionHistory;
import javax.jcr.version.VersionIterator;

import org.apache.jackrabbit.core.state.NoSuchItemStateException;

import org.silverpeas.attachment.model.HistorisedDocument;
import org.silverpeas.attachment.model.SimpleAttachment;
import org.silverpeas.attachment.model.SimpleDocument;
import org.silverpeas.attachment.model.SimpleDocumentPK;
import org.silverpeas.util.jcr.AbstractJcrConverter;

import com.silverpeas.util.i18n.I18NHelper;

import static com.silverpeas.jcrutil.JcrConstants.*;

/**
 *
 * @author ehugonnet
 */
class DocumentConverter extends AbstractJcrConverter {

  SimpleAttachmentConverter attachmentConverter = new SimpleAttachmentConverter();

  /**
   * Convert the document history in a list of SimpleDocument.
   *
   * @param session
   * @param path
   * @param lang
   * @return
   * @throws RepositoryException
   */
  List<SimpleDocument> convertDocumentHistory(Node node, String lang) throws
      RepositoryException {
    try {
      VersionHistory history = node.getSession().getWorkspace().getVersionManager().
          getVersionHistory(node.getPath());
      Version root = history.getRootVersion();
      String rootId = "";
      if (root != null) {
        rootId = root.getIdentifier();
      }
      VersionIterator versionsIterator = history.getAllVersions();
      List<SimpleDocument> documentHistory = new ArrayList<SimpleDocument>((int) versionsIterator.
          getSize());
      while (versionsIterator.hasNext()) {
        Version version = versionsIterator.nextVersion();
        if (!version.getIdentifier().equals(rootId)) {
          documentHistory.add(fillDocument(version.getFrozenNode(), lang));
        }
      }
      return documentHistory;
    } catch (RepositoryException ex) {
      if (ex.getCause() instanceof NoSuchItemStateException) {
        return new ArrayList<SimpleDocument>(0);
      }
      throw ex;
    }
  }

  public SimpleDocument convertNode(Node node, String lang) throws RepositoryException {
    if (isVersioned(node)) {
      HistorisedDocument document = new HistorisedDocument(fillDocument(node, lang));
      document.setHistory(convertDocumentHistory(node, lang));
      return document;
    }
    return fillDocument(node, lang);
  }

  SimpleDocument fillDocument(Node node, String lang) throws RepositoryException {
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
    SimpleDocument doc = new SimpleDocument(pk, getStringProperty(node, SLV_PROPERTY_FOREIGN_KEY),
        getIntProperty(node, SLV_PROPERTY_ORDER), getBooleanProperty(node, SLV_PROPERTY_VERSIONED),
        getStringProperty(node, SLV_PROPERTY_OWNER), getDateProperty(node,
        SLV_PROPERTY_RESERVATION_DATE), getDateProperty(node, SLV_PROPERTY_ALERT_DATE),
        getDateProperty(node, SLV_PROPERTY_EXPIRY_DATE),
        getStringProperty(node, SLV_PROPERTY_STATUS), file);
    doc.setCloneId(getStringProperty(node, SLV_PROPERTY_CLONE));
    doc.setMajorVersion(getIntProperty(node, SLV_PROPERTY_MAJOR));
    doc.setMinorVersion(getIntProperty(node, SLV_PROPERTY_MINOR));
    return doc;
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
    documentNode.setProperty(SLV_PROPERTY_MAJOR, document.getMajorVersion());
    documentNode.setProperty(SLV_PROPERTY_MINOR, document.getMinorVersion());
    addStringProperty(documentNode, SLV_PROPERTY_INSTANCEID, document.getInstanceId());
    addStringProperty(documentNode, SLV_PROPERTY_OWNER, document.getEditedBy());
    addStringProperty(documentNode, SLV_PROPERTY_STATUS, document.getStatus());
    addDateProperty(documentNode, SLV_PROPERTY_ALERT_DATE, document.getAlert());
    addDateProperty(documentNode, SLV_PROPERTY_EXPIRY_DATE, document.getExpiry());
    addDateProperty(documentNode, SLV_PROPERTY_RESERVATION_DATE, document.getReservation());
    addStringProperty(documentNode, SLV_PROPERTY_CLONE, document.getCloneId());
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
    String attachmentNodeName = document.getFile().getNodeName();
    if (content != null) {
      Node attachmentNode = getAttachmentNode(attachmentNodeName, documentNode);
      attachmentConverter.fillNode(document.getFile(), attachmentNode);
      attachmentConverter.setContent(attachmentNode, content, document.getContentType());
    } else if (documentNode.hasNode(attachmentNodeName)) {
      Node attachmentNode = documentNode.getNode(attachmentNodeName);
      attachmentConverter.fillNode(document.getFile(), attachmentNode);
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
    String lang = language;
    if (lang == null) {
      lang = I18NHelper.defaultLanguage;
    }
    if (documentNode.hasNode(SimpleDocument.FILE_PREFIX + lang)) {
      Node attachmentNode = documentNode.getNode(SimpleDocument.FILE_PREFIX + lang);
      attachmentNode.remove();
    }
  }

  public boolean isVersioned(Node node) throws RepositoryException {
    return getBooleanProperty(node, SLV_PROPERTY_VERSIONED) && !node.hasProperty(
        "jcr:frozenPrimaryType");
  }

  public String updateVersion(Node node, boolean isPublic) throws RepositoryException {
    if (isVersioned(node) && node.isCheckedOut()) {
      if (isPublic) {
        int majorVersion = getIntProperty(node, SLV_PROPERTY_MAJOR) + 1;
        node.setProperty(SLV_PROPERTY_MAJOR, majorVersion);
        node.setProperty(SLV_PROPERTY_MINOR, 0);
      } else {
        int minorVersion = getIntProperty(node, SLV_PROPERTY_MINOR) + 1;
        node.setProperty(SLV_PROPERTY_MINOR, minorVersion);
      }
    }
    return "Version " + getIntProperty(node, SLV_PROPERTY_MAJOR) + "." + getIntProperty(node,
        SLV_PROPERTY_MINOR);
  }
}
