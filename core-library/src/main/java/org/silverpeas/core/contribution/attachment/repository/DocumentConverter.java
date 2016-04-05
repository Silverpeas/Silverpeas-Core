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

import org.silverpeas.core.util.CollectionUtil;
import org.silverpeas.core.util.StringUtil;
import org.silverpeas.core.i18n.I18NHelper;
import org.silverpeas.core.admin.user.model.SilverpeasRole;
import org.apache.jackrabbit.core.state.NoSuchItemStateException;
import org.silverpeas.core.contribution.attachment.model.DocumentType;
import org.silverpeas.core.contribution.attachment.model.HistorisedDocument;
import org.silverpeas.core.contribution.attachment.model.HistorisedDocumentVersion;
import org.silverpeas.core.contribution.attachment.model.SimpleAttachment;
import org.silverpeas.core.contribution.attachment.model.SimpleDocument;
import org.silverpeas.core.contribution.attachment.model.SimpleDocumentPK;
import org.silverpeas.core.contribution.attachment.util.SimpleDocumentList;
import org.silverpeas.core.contribution.attachment.model.SimpleDocumentVersion;
import org.silverpeas.core.persistence.jcr.AbstractJcrConverter;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import javax.jcr.version.Version;
import javax.jcr.version.VersionHistory;
import javax.jcr.version.VersionIterator;
import javax.jcr.version.VersionManager;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static org.silverpeas.core.persistence.jcr.util.JcrConstants.*;
import static javax.jcr.Property.JCR_FROZEN_PRIMARY_TYPE;
import static javax.jcr.Property.JCR_LAST_MODIFIED_BY;
import static javax.jcr.nodetype.NodeType.MIX_SIMPLE_VERSIONABLE;

/**
 *
 * @author ehugonnet
 */
class DocumentConverter extends AbstractJcrConverter {

  private SimpleAttachmentConverter attachmentConverter = new SimpleAttachmentConverter();

  /**
   * Builds from the root version node and from a language the object representation of a
   * versioned document and its history.
   * @param rootVersionNode the root version node (master).
   * @param lang the aimed content language.
   * @return the instance of a versioned document.
   * @throws RepositoryException
   */
  HistorisedDocument buildHistorisedDocument(Node rootVersionNode, String lang)
  throws RepositoryException {

    VersionManager versionManager = rootVersionNode.getSession().getWorkspace().getVersionManager();
    HistorisedDocument historisedDocument =
        new HistorisedDocument(fillDocument(rootVersionNode, lang));

    try {

      String path = rootVersionNode.getPath();
      VersionHistory history = versionManager.getVersionHistory(path);
      Version root = history.getRootVersion();
      String rootId = "";
      if (root != null) {
        rootId = root.getIdentifier();
      }
      Version base = versionManager.getBaseVersion(path);
      String baseId = "";
      if (base != null) {
        baseId = base.getIdentifier();
      }
      VersionIterator versionsIterator = history.getAllVersions();
      List<SimpleDocumentVersion> documentHistory =
          new ArrayList<SimpleDocumentVersion>((int) versionsIterator.
              getSize());

      int versionIndex = 0;
      SimpleDocumentVersion previousVersion = null;
      while (versionsIterator.hasNext()) {
        Version version = versionsIterator.nextVersion();
        if (!version.getIdentifier().equals(rootId) && !version.getIdentifier().equals(baseId)) {
          SimpleDocumentVersion versionDocument =
              new SimpleDocumentVersion(fillDocument(version.getFrozenNode(), lang),
                  historisedDocument);
          versionDocument.setNodeName(rootVersionNode.getName());
          versionDocument.setVersionIndex(versionIndex++);
          versionDocument.setPreviousVersion(previousVersion);
          documentHistory.add(versionDocument);
          previousVersion = versionDocument;
        }
      }

      HistoryDocumentSorter.sortHistory((List) documentHistory);
      historisedDocument.setHistory(documentHistory);
      historisedDocument.setVersionIndex(versionIndex);
    } catch (RepositoryException ex) {
      if (ex.getCause() instanceof NoSuchItemStateException) {
        historisedDocument.setHistory(new ArrayList<SimpleDocumentVersion>(0));
      } else {
        throw ex;
      }
    }
    return historisedDocument;
  }

  public SimpleDocument convertNode(Node node, String lang) throws RepositoryException {
    if (isVersionedMaster(node)) {
      return buildHistorisedDocument(node, lang);
    }
    Node parentNode = node.getParent();
    if (parentNode instanceof Version) {
      // Getting the parent node, the versionned one
      Node masterNode = getMasterNodeForVersion((Version) parentNode);
      // The historised document is built from the parent node
      HistorisedDocument document = buildHistorisedDocument(masterNode, lang);
      // Returning the version
      SimpleDocumentVersion version = document.getVersionIdentifiedBy(node.getIdentifier());
      if (version != null) {
        return new HistorisedDocumentVersion(version);
      }
      throw new PathNotFoundException(
          "Version identified by " + node.getIdentifier() + " has not been found.");
    }
    return fillDocument(node, lang);
  }

  public Node getMasterNodeForVersion(Version version) throws RepositoryException {
    String uuid = version.getContainingHistory().getVersionableIdentifier();
    return version.getSession().getNodeByIdentifier(uuid);
  }

  /**
   * Convert a NodeIteraor into a collection of SimpleDocument.
   *
   * @param iter th NodeIterator to convert.
   * @param language the language of the wanted document.
   * @return a collection of SimpleDocument.
   * @throws RepositoryException
   */
  public SimpleDocumentList<SimpleDocument> convertNodeIterator(NodeIterator iter, String language)
      throws RepositoryException {
    SimpleDocumentList<SimpleDocument> result =
        new SimpleDocumentList<SimpleDocument>((int) iter.getSize()).setQueryLanguage(language);
    while (iter.hasNext()) {
      result.add(convertNode(iter.nextNode(), language));
    }
    return result;
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
        getStringProperty(node, SLV_PROPERTY_COMMENT), file);
    doc.setRepositoryPath(node.getPath());
    doc.setCloneId(getStringProperty(node, SLV_PROPERTY_CLONE));
    doc.setMajorVersion(getIntProperty(node, SLV_PROPERTY_MAJOR));
    doc.setMinorVersion(getIntProperty(node, SLV_PROPERTY_MINOR));
    doc.setStatus(getStringProperty(node, SLV_PROPERTY_STATUS));
    doc.setDocumentType(DocumentType.fromFolderName(node.getParent().getName()));
    String nodeName = node.getName();
    if ("jcr:frozenNode".equals(nodeName)) {
      nodeName = doc.computeNodeName();
      doc.setNodeName(nodeName);
      if (!node.getSession().nodeExists(doc.getFullJcrPath())) {
        nodeName = SimpleDocument.VERSION_PREFIX + doc.getOldSilverpeasId();
      }
    }
    doc.setNodeName(nodeName);
    doc.setPublicDocument(!doc.isVersioned() || doc.getMinorVersion() == 0);
    // Forbidden download for roles
    String forbiddenDownloadForRoles =
        getStringProperty(node, SLV_PROPERTY_FORBIDDEN_DOWNLOAD_FOR_ROLES);
    if (StringUtil.isDefined(forbiddenDownloadForRoles)) {
      doc.addRolesForWhichDownloadIsForbidden(SilverpeasRole.listFrom(forbiddenDownloadForRoles));
    }
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
    fillNode(document, documentNode, false);
  }

  public void fillNode(SimpleDocument document, Node documentNode, boolean skipAttachmentContent)
      throws RepositoryException {
    setDocumentNodeProperties(document, documentNode);
    if (!skipAttachmentContent) {
      Node attachmentNode = getAttachmentNode(document.getAttachment().getNodeName(), documentNode);
      attachmentConverter.fillNode(document.getAttachment(), attachmentNode);
    }
  }

  private void setDocumentNodeProperties(SimpleDocument document, Node documentNode) throws
      RepositoryException {
    addStringProperty(documentNode, SLV_PROPERTY_FOREIGN_KEY, document.getForeignId());
    documentNode.setProperty(SLV_PROPERTY_VERSIONED, document.isVersioned());
    documentNode.setProperty(SLV_PROPERTY_ORDER, document.getOrder());
    documentNode.setProperty(SLV_PROPERTY_OLD_ID, document.getOldSilverpeasId());
    addStringProperty(documentNode, SLV_PROPERTY_INSTANCEID, document.getInstanceId());
    addStringProperty(documentNode, SLV_PROPERTY_OWNER, document.getEditedBy());
    addStringProperty(documentNode, SLV_PROPERTY_COMMENT, document.getComment());
    addStringProperty(documentNode, SLV_PROPERTY_STATUS, document.getStatus());
    addDateProperty(documentNode, SLV_PROPERTY_ALERT_DATE, document.getAlert());
    addDateProperty(documentNode, SLV_PROPERTY_EXPIRY_DATE, document.getExpiry());
    addDateProperty(documentNode, SLV_PROPERTY_RESERVATION_DATE, document.getReservation());
    addStringProperty(documentNode, SLV_PROPERTY_CLONE, document.getCloneId());
    // Optional downloadable mixin
    setForbiddenDownloadForRolesOptionalNodeProperty(document, documentNode);
  }

  /**
   * Adding or removing the [slv:forbiddenDownloadForRoles] optional property.
   * @param document
   * @param documentNode
   * @throws RepositoryException
   */
  protected void setForbiddenDownloadForRolesOptionalNodeProperty(SimpleDocument document,
      Node documentNode) throws RepositoryException {

    if (CollectionUtil.isNotEmpty(document.getForbiddenDownloadForRoles())) {
      // Adding the mixin (no impact when it is already existing)
      documentNode.addMixin(SLV_DOWNLOADABLE_MIXIN);
      addStringProperty(documentNode, SLV_PROPERTY_FORBIDDEN_DOWNLOAD_FOR_ROLES,
          SilverpeasRole.asString(document.getForbiddenDownloadForRoles()));
    } else {
      // Removing the mixin
      if (documentNode.hasProperty(SLV_PROPERTY_FORBIDDEN_DOWNLOAD_FOR_ROLES)) {
        documentNode.removeMixin(SLV_DOWNLOADABLE_MIXIN);
      }
    }
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

  public void addAttachment(Node documentNode, SimpleAttachment attachment)
      throws RepositoryException {
    Node attachmentNode = getAttachmentNode(attachment.getNodeName(), documentNode);
    attachmentConverter.fillNode(attachment, attachmentNode);
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

  public boolean isVersionedMaster(Node node) throws RepositoryException {
    return getBooleanProperty(node, SLV_PROPERTY_VERSIONED) && !node.hasProperty(
        JCR_FROZEN_PRIMARY_TYPE) && isMixinApplied(node, MIX_SIMPLE_VERSIONABLE);
  }

  public boolean isForm(Node node) throws RepositoryException {
    return node.getPath().contains('/' + DocumentType.form.getFolderName() + '/');
  }

  public String updateVersion(Node node, String lang, boolean isPublic) throws RepositoryException {
    int majorVersion = getIntProperty(node, SLV_PROPERTY_MAJOR);
    int minorVersion = getIntProperty(node, SLV_PROPERTY_MINOR);
    if (isVersionedMaster(node) && node.isCheckedOut()) {
      releaseDocumentNode(node, lang);
      if (isPublic) {
        majorVersion = majorVersion + 1;
        minorVersion = 0;
        node.setProperty(SLV_PROPERTY_MAJOR, majorVersion);
        node.setProperty(SLV_PROPERTY_MINOR, 0);
      } else {
        minorVersion = minorVersion + 1;
        node.setProperty(SLV_PROPERTY_MINOR, minorVersion);
        if (!node.hasProperty(SLV_PROPERTY_MAJOR)) {
          node.setProperty(SLV_PROPERTY_MAJOR, 0);
        }
      }
    }
    return "Version " + majorVersion + "." + minorVersion;
  }

  public void releaseDocumentNode(Node documentNode, String lang) throws RepositoryException {
    String language = lang;
    if (!StringUtil.isDefined(language)) {
      language = I18NHelper.defaultLanguage;
    }
    String attachmentNodeName = SimpleDocument.FILE_PREFIX + language;
    if (documentNode.hasNode(attachmentNodeName)) {
      Node attachmentNode = getAttachmentNode(attachmentNodeName, documentNode);
      addStringProperty(attachmentNode, JCR_LAST_MODIFIED_BY, getStringProperty(documentNode,
          SLV_PROPERTY_OWNER));
    }
    addDateProperty(documentNode, SLV_PROPERTY_EXPIRY_DATE, null);
    addDateProperty(documentNode, SLV_PROPERTY_ALERT_DATE, null);
    addDateProperty(documentNode, SLV_PROPERTY_RESERVATION_DATE, null);
    addStringProperty(documentNode, SLV_PROPERTY_OWNER, null);
  }
}
