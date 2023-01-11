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

import org.silverpeas.core.admin.user.model.SilverpeasRole;
import org.silverpeas.core.contribution.attachment.model.DocumentType;
import org.silverpeas.core.contribution.attachment.model.HistorisedDocument;
import org.silverpeas.core.contribution.attachment.model.HistorisedDocumentVersion;
import org.silverpeas.core.contribution.attachment.model.SimpleAttachment;
import org.silverpeas.core.contribution.attachment.model.SimpleDocument;
import org.silverpeas.core.contribution.attachment.model.SimpleDocumentPK;
import org.silverpeas.core.contribution.attachment.model.SimpleDocumentVersion;
import org.silverpeas.core.contribution.attachment.util.SimpleDocumentList;
import org.silverpeas.core.i18n.I18NHelper;
import org.silverpeas.core.persistence.jcr.AbstractJcrConverter;
import org.silverpeas.core.util.CollectionUtil;
import org.silverpeas.core.util.StringUtil;

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
import java.util.Optional;

import static java.util.Optional.ofNullable;
import static javax.jcr.Property.JCR_FROZEN_PRIMARY_TYPE;
import static javax.jcr.Property.JCR_LAST_MODIFIED_BY;
import static javax.jcr.nodetype.NodeType.MIX_SIMPLE_VERSIONABLE;
import static javax.jcr.nodetype.NodeType.MIX_VERSIONABLE;
import static org.silverpeas.core.contribution.attachment.util.AttachmentSettings.defaultValueOfDisplayableAsContentBehavior;
import static org.silverpeas.core.contribution.attachment.util.AttachmentSettings.defaultValueOfEditableSimultaneously;
import static org.silverpeas.core.util.StringUtil.defaultStringIfNotDefined;
import static org.silverpeas.jcr.util.SilverpeasProperty.*;

/**
 * A converter of node representing documents to {@link SimpleDocument} or
 * {@link HistorisedDocument} objects in Silverpeas.
 * @author ehugonnet
 */
class DocumentConverter extends AbstractJcrConverter {

  private final SimpleAttachmentConverter attachmentConverter = new SimpleAttachmentConverter();

  /**
   * Builds from the root version node and from a language the object representation of a versioned
   * document and its history.
   * @param rootVersionNode the root version node (master).
   * @param lang the aimed content language.
   * @return the instance of a versioned document.
   * @throws RepositoryException if an error occurs in the JCR
   */
  HistorisedDocument buildHistorizedDocument(Node rootVersionNode, String lang)
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
      // VersionIterator#getSize() support depends on the JCR implementation: if it isn't supported,
      // VersionIterator#getSize() returns -1. We have to take into account this particularity.
      int capacity = versionsIterator.getSize() == -1 ? 10 : (int) versionsIterator.getSize();
      List<SimpleDocumentVersion> documentHistory = new ArrayList<>(capacity);

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

      HistoryDocumentSorter.sortHistory(documentHistory);
      historisedDocument.setHistory(documentHistory);
      historisedDocument.setVersionIndex(versionIndex);
    } catch (PathNotFoundException ex) {
      performBrokenHistory(historisedDocument);
    }
    return historisedDocument;
  }

  protected void performBrokenHistory(final HistorisedDocument historisedDocument) {
    historisedDocument.setHistory(new ArrayList<>(0));
  }

  public SimpleDocument convertNode(Node node, String lang) throws RepositoryException {
    if (isMixinApplied(node, MIX_SIMPLE_VERSIONABLE) && !isMixinApplied(node, MIX_VERSIONABLE)) {
      // convert mixin
      node.addMixin(MIX_VERSIONABLE);
      node.getSession().save();
    }
    if (isVersionedMaster(node)) {
      return buildHistorizedDocument(node, lang);
    }
    Node parentNode = node.getParent();
    if (parentNode instanceof Version) {
      // Getting the parent node, the versioned one
      Node masterNode = getMasterNodeForVersion((Version) parentNode);
      // The historized document is built from the parent node
      HistorisedDocument document = buildHistorizedDocument(masterNode, lang);
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
   * Browses the nodes with the specified iterator and converts each of them into to a document.
   * @param iter th NodeIterator to convert.
   * @param language the language of the wanted document.
   * @return a collection of SimpleDocument.
   * @throws RepositoryException if an error occurs in the JCR
   */
  public SimpleDocumentList<SimpleDocument> convertNodeIterator(NodeIterator iter, String language)
      throws RepositoryException {
    SimpleDocumentList<SimpleDocument> result =
        new SimpleDocumentList<>((int) iter.getSize()).setQueryLanguage(language);
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
      language = I18NHelper.DEFAULT_LANGUAGE;
    }
    SimpleAttachment file = getAttachment(node, language);
    if (file == null) {
      Iterator<String> iter = I18NHelper.getLanguages().iterator();
      while (iter.hasNext() && file == null) {
        file = getAttachment(node, iter.next());
      }
    }

    SimpleDocument doc = new SimpleDocument(pk, getStringProperty(node, SLV_PROPERTY_FOREIGN_KEY),
        getIntProperty(node, SLV_PROPERTY_ORDER),
        getBooleanProperty(node, SLV_PROPERTY_VERSIONED, false),
        getStringProperty(node, SLV_PROPERTY_OWNER), file);
    doc.setReservation(getDateProperty(node, SLV_PROPERTY_RESERVATION_DATE));
    doc.setAlert(getDateProperty(node, SLV_PROPERTY_ALERT_DATE));
    doc.setExpiry(getDateProperty(node, SLV_PROPERTY_EXPIRY_DATE));
    doc.setComment(getStringProperty(node, SLV_PROPERTY_COMMENT));
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
    // Displayable as content
    ofNullable(getBooleanProperty(node, SLV_PROPERTY_DISPLAYABLE_AS_CONTENT, null)).ifPresent(
        doc::setDisplayableAsContent);

    // Editable simultaneously
    ofNullable(getBooleanProperty(node, SLV_PROPERTY_EDITABLE_SIMULTANEOUSLY, null))
        .ifPresent(doc::setEditableSimultaneously);
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
    // Optional viewable mixin
    setDisplayableAsContentOptionalNodeProperty(document, documentNode);
  }

  /**
   * Adding or removing the [slv:forbiddenDownloadForRoles] optional property.
   * @param document the document for which download has to be enabled or disabled
   * @param documentNode the node representation of the document in the JCR
   * @throws RepositoryException if an error occurs in the JCR
   */
  void setForbiddenDownloadForRolesOptionalNodeProperty(SimpleDocument document, Node
      documentNode) throws RepositoryException {

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

  /**
   * Adding or removing the [slv:displayableAsContent] optional property.
   * @param document the document for which the rendering of its content has to be enabled or
   * disabled.
   * @param documentNode the node representation of the document in the JCR
   * @throws RepositoryException if an error occurs in the JCR
   */
  void setDisplayableAsContentOptionalNodeProperty(SimpleDocument document, Node
      documentNode) throws RepositoryException {
    if (document.isDisplayableAsContent() != defaultValueOfDisplayableAsContentBehavior()) {
      // Adding the mixin (no impact when it is already existing)
      documentNode.addMixin(SLV_VIEWABLE_MIXIN);
      documentNode.setProperty(SLV_PROPERTY_DISPLAYABLE_AS_CONTENT,
          document.isDisplayableAsContent());
    } else {
      // Removing the mixin
      if (documentNode.hasProperty(SLV_PROPERTY_DISPLAYABLE_AS_CONTENT)) {
        documentNode.removeMixin(SLV_VIEWABLE_MIXIN);
      }
    }
  }

  /**
   * Adding or removing the [slv:editableSimultaneously] optional property.
   * @param document the document for which the simultaneous edition has to be enabled or disabled.
   * @param documentNode the node representation of the document in the JCR
   * @throws RepositoryException if an error occurs in the JCR
   */
  void setEditableSimultaneouslyOptionalNodeProperty(SimpleDocument document, Node
      documentNode) throws RepositoryException {
    final Optional<Boolean> editableSimultaneously = document.editableSimultaneously();
    if (editableSimultaneously.isPresent()) {
      final boolean enabled = editableSimultaneously.get();
      if (enabled != defaultValueOfEditableSimultaneously()) {
        // Adding the mixin (no impact when it is already existing)
        documentNode.addMixin(SLV_EDITABLE_MIXIN);
        documentNode.setProperty(SLV_PROPERTY_EDITABLE_SIMULTANEOUSLY, enabled);
      } else {
        // Removing the mixin
        if (documentNode.hasProperty(SLV_PROPERTY_EDITABLE_SIMULTANEOUSLY)) {
          documentNode.removeMixin(SLV_EDITABLE_MIXIN);
        }
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
      lang = I18NHelper.DEFAULT_LANGUAGE;
    }
    if (documentNode.hasNode(SimpleDocument.FILE_PREFIX + lang)) {
      Node attachmentNode = documentNode.getNode(SimpleDocument.FILE_PREFIX + lang);
      attachmentNode.remove();
    }
  }

  public boolean isVersionedMaster(Node node) throws RepositoryException {
    return getBooleanProperty(node, SLV_PROPERTY_VERSIONED, false) && !node.hasProperty(
        JCR_FROZEN_PRIMARY_TYPE) && isMixinApplied(node, MIX_VERSIONABLE);
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
      language = I18NHelper.DEFAULT_LANGUAGE;
    }
    final String attachmentNodeName = SimpleDocument.FILE_PREFIX + language;
    if (documentNode.hasNode(attachmentNodeName)) {
      final Node attachmentNode = getAttachmentNode(attachmentNodeName, documentNode);
      final String currentLastModifiedBy = getStringProperty(attachmentNode, JCR_LAST_MODIFIED_BY);
      final String ownerId = getStringProperty(documentNode, SLV_PROPERTY_OWNER);
      final String lastModifiedBy = defaultStringIfNotDefined(ownerId, currentLastModifiedBy);
      addStringProperty(attachmentNode, JCR_LAST_MODIFIED_BY, lastModifiedBy);
    }
    addDateProperty(documentNode, SLV_PROPERTY_EXPIRY_DATE, null);
    addDateProperty(documentNode, SLV_PROPERTY_ALERT_DATE, null);
    addDateProperty(documentNode, SLV_PROPERTY_RESERVATION_DATE, null);
    addStringProperty(documentNode, SLV_PROPERTY_OWNER, null);
  }
}
