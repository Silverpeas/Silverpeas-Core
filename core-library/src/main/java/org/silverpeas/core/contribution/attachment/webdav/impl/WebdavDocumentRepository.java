/*
 * Copyright (C) 2000 - 2016 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU Affero General Public License as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of the GPL, you may
 * redistribute this Program in connection with Free/Libre Open Source Software ("FLOSS")
 * applications as described in Silverpeas's FLOSS exception. You should have received a copy of the
 * text describing the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License along with this program.
 * If not, see <http://www.gnu.org/licenses/>.
 */
package org.silverpeas.core.contribution.attachment.webdav.impl;

import org.silverpeas.core.util.StringUtil;
import org.silverpeas.core.i18n.I18NHelper;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.silverpeas.core.contribution.attachment.model.DocumentType;
import org.silverpeas.core.contribution.attachment.model.SimpleDocument;
import org.silverpeas.core.contribution.attachment.webdav.WebdavRepository;

import javax.jcr.Binary;
import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Calendar;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.silverpeas.core.persistence.jcr.util.JcrConstants.*;

public class WebdavDocumentRepository implements WebdavRepository {

  @Override
  public void createAttachmentNode(Session session, SimpleDocument attachment) throws
      RepositoryException, IOException {
    Node rootNode = session.getRootNode();
    Node attachmentFolder = addFolder(rootNode, SimpleDocument.WEBDAV_FOLDER);
    attachmentFolder = addFolder(attachmentFolder, DocumentType.attachment.getFolderName());
    Node contextFolder = addFolder(attachmentFolder, attachment.getInstanceId());
    if (attachment.getId() != null) {
      contextFolder = addFolder(contextFolder, attachment.getId());
    }
    String lang = attachment.getLanguage();
    if (!StringUtil.isDefined(lang)) {
      lang = I18NHelper.defaultLanguage;
    }
    contextFolder = addExclusiveFolder(contextFolder, lang);
    addFile(contextFolder, attachment);
  }

  @Override
  public void updateNodeAttachment(Session session, SimpleDocument attachment) throws
      RepositoryException, IOException {
    Node rootNode = session.getRootNode();
    try {
      Node fileNode = rootNode.getNode(attachment.getWebdavJcrPath());
      setContent(fileNode, attachment);
    } catch (PathNotFoundException pex) {
      createAttachmentNode(session, attachment);
    }
  }

  @Override
  public void moveNodeAttachment(final Session session, final SimpleDocument attachment,
      final String targetComponentInstanceId) throws RepositoryException, IOException {
    if (attachment.getPk() != null && StringUtil.isDefined(targetComponentInstanceId) &&
        !attachment.getInstanceId().equals(targetComponentInstanceId)) {
      Node nodeToMove = getDocumentIdentifierNode(session, attachment);
      if (nodeToMove != null) {
        Node nodeToPurge = nodeToMove.getParent();
        Node rootNode = session.getRootNode();
        Node webdavNode = addFolder(rootNode, SimpleDocument.WEBDAV_FOLDER);
        Node documentTypeNode = addFolder(webdavNode, DocumentType.attachment.getFolderName());
        Node destinationNode = addFolder(documentTypeNode, targetComponentInstanceId);
        session.save();
        session.getWorkspace()
            .move(nodeToMove.getPath(), destinationNode.getPath() + "/" + attachment.getId());
        purgeWebdavFromNode(nodeToPurge);
      }
    }
  }

  /**
   * Gets the webdav document identifier node from the specified attachment.
   * @param session the JCR session.
   * @param attachment the attachment from which the webdav document identifier JCR node is
   * searched.
   * @return the node if found, null otherwise.
   */
  protected Node getDocumentIdentifierNode(Session session, SimpleDocument attachment)
      throws RepositoryException {
    return getDocumentNode(session, attachment, null);
  }

  /**
   * Gets the webdav document content language node from the specified attachment.
   * @param session the JCR session.
   * @param attachment the attachment from which the webdav document content language JCR node is
   * searched.
   * @param language the aimed content language.
   * @return the node if found, null otherwise.
   */
  protected Node getDocumentContentLanguageNode(Session session, SimpleDocument attachment,
      String language) throws RepositoryException {
    Node documentContentLanguageNode = null;
    if (StringUtil.isDefined(language)) {
      documentContentLanguageNode = getDocumentNode(session, attachment, "/" + language);
    }
    return documentContentLanguageNode;
  }

  /**
   * Gets the webdav document node from the specified attachment.
   * It can retrieve all sub nodes from the one which contains the attachment JCR identfier.
   * @param session the JCR session.
   * @param attachment the attachment from which the webdav document identifier JCR node is
   * searched.
   * @param suffixPathPattern the relative path from the attachment JCR identifier node.
   * @return the node if found, null otherwise.
   */
  private Node getDocumentNode(Session session, SimpleDocument attachment, String suffixPathPattern)
      throws RepositoryException {
    if (StringUtil.isDefined(attachment.getId())) {
      Pattern pattern = Pattern.compile(
          ".*/" + attachment.getId() + StringUtil.defaultStringIfNotDefined(suffixPathPattern, ""));
      Matcher matcher = pattern.matcher(attachment.getWebdavJcrPath());
      if (!matcher.find()) {
        // This case should normally never happen.
        return null;
      }
      String webdavPath = matcher.group();
      try {
        Node rootNode = session.getRootNode();
        return rootNode.getNode(webdavPath);
      } catch (PathNotFoundException pex) {
        // Node does not exist.
      }
    }
    return null;
  }

  @Override
  public void deleteAttachmentNode(Session session, SimpleDocument attachment) throws
      RepositoryException {
    Node rootNode = session.getRootNode();
    try {
      /**
       Two cases here because of the analysis of {@link SimpleDocument#getWebdavJcrPath()}...
       */
      Node fileNode = getDocumentIdentifierNode(session, attachment);
      if (fileNode == null) {
        fileNode = rootNode.getNode(attachment.getWebdavJcrPath());
      }
      Node parentNode = fileNode.getParent();
      fileNode.remove();
      purgeWebdavFromNode(parentNode);
    } catch (PathNotFoundException pex) {
      // Since the node doesn't exist, deleting it has no effect.
    }
  }

  @Override
  public void deleteAttachmentContentNode(final Session session, final SimpleDocument attachment,
      String language) throws RepositoryException {
    Node fileNode = getDocumentContentLanguageNode(session, attachment, language);
    if (fileNode != null) {
      Node parentNode = fileNode.getParent();
      fileNode.remove();
      purgeWebdavFromNode(parentNode);
    }
  }

  /**
   * Purges all empty nodes from specified node to webdav node.
   * @param parentNodeOfDeletedOne a deleted node.
   */
  private void purgeWebdavFromNode(Node parentNodeOfDeletedOne) throws RepositoryException {
    //noinspection UnnecessaryLocalVariable
    Node currentNode = parentNodeOfDeletedOne;
    if (currentNode != null) {
      while (!currentNode.hasNodes() &&
          !SimpleDocument.WEBDAV_FOLDER.equals(currentNode.getName())) {
        Node nodeToRemove = currentNode;
        currentNode = currentNode.getParent();
        nodeToRemove.remove();
      }
      if (!currentNode.hasNodes()) {
        currentNode.remove();
      }
    }
  }

  @Override
  public boolean isNodeLocked(Session session, SimpleDocument attachment) throws RepositoryException {
    Node rootNode = session.getRootNode();
    try {
      Node fileNode = rootNode.getNode(attachment.getWebdavJcrPath());
      return fileNode.isLocked();
    } catch (PathNotFoundException pex) {
      return false;
    }
  }

  @Override
  public void updateAttachmentBinaryContent(Session session, SimpleDocument attachment)
      throws RepositoryException, IOException {
    Node rootNode = session.getRootNode();
    Node webdavFileNode = rootNode.getNode(attachment.getWebdavJcrPath());
    Binary webdavBinary = webdavFileNode.getNode(JCR_CONTENT).getProperty(JCR_DATA).getBinary();
    InputStream in = webdavBinary.getStream();
    OutputStream out = null;
    try {
      out = FileUtils.openOutputStream(new File(attachment.getAttachmentPath()));
      IOUtils.copy(in, out);
    } finally {
      IOUtils.closeQuietly(out);
      IOUtils.closeQuietly(in);
      webdavBinary.dispose();
    }
  }

  /**
   * Add a folder node into the repository
   *
   * @param parent the parent node
   * @param name the name of the new node
   * @return the created node.
   * @throws RepositoryException
   */
  protected Node addFolder(Node parent, String name) throws RepositoryException {
    try {
      return parent.getNode(name);
    } catch (PathNotFoundException pnfex) {
      return parent.addNode(name, NT_FOLDER);
    }
  }

  /**
   * Adds a folder node into the repository and removes all others so that it exists only the
   * folder named like specified.
   * If the folder already exists, the existing is kept and no folder is created,
   * but potential other existing folders are removed.
   *
   * @param parent the parent node
   * @param name the name of the new node
   * @return the created or already existing node.
   * @throws RepositoryException
   */
  protected Node addExclusiveFolder(Node parent, String name) throws RepositoryException {
    try {
      NodeIterator nodeIt = parent.getNodes();
      while (nodeIt.hasNext()) {
        Node currentNode = nodeIt.nextNode();
        if (!currentNode.getName().equals(name)) {
          currentNode.remove();
        }
      }
      return parent.getNode(name);
    } catch (PathNotFoundException pnfex) {
      return parent.addNode(name, NT_FOLDER);
    }
  }

  /**
   * Add a file node into the repository
   *
   * @param folder the folder node containing the file node.
   * @param attachment the attachment for the file.
   * @return the created node.
   * @throws RepositoryException
   * @throws IOException
   */
  protected Node addFile(Node folder, SimpleDocument attachment) throws RepositoryException,
      IOException {
    NodeIterator fileNodeIt = folder.getNodes();
    while (fileNodeIt.hasNext()) {
      fileNodeIt.nextNode().remove();
    }
    String escapedName = StringUtil.escapeQuote(attachment.getFilename());
    Node fileNode = folder.addNode(escapedName, NT_FILE);
    if (attachment.getEditedBy() != null) {
      fileNode.addMixin(SLV_OWNABLE_MIXIN);
      fileNode.setProperty(SLV_PROPERTY_OWNER, attachment.getEditedBy());
    }
    Node contentNode = fileNode.addNode(JCR_CONTENT, NT_RESOURCE);
    contentNode.setProperty(JCR_MIMETYPE, attachment.getContentType());
    contentNode.setProperty(JCR_ENCODING, "");
    contentNode.setProperty(JCR_LAST_MODIFIED, Calendar.getInstance());
    setContent(fileNode, attachment);
    return fileNode;
  }

  private void setContent(Node fileNode, SimpleDocument attachment)
      throws RepositoryException, IOException {
    InputStream in = FileUtils.openInputStream(new File(attachment.getAttachmentPath()));
    try {
      Binary attachmentBinary = fileNode.getSession().getValueFactory().createBinary(in);
      fileNode.getNode(JCR_CONTENT).setProperty(JCR_DATA, attachmentBinary);
    } finally {
      IOUtils.closeQuietly(in);
    }
  }

  @Override
  public String getContentEditionLanguage(final Session session, final SimpleDocument attachment)
      throws RepositoryException {
    String contentEditionLanguage = null;
    if (StringUtil.isDefined(attachment.getId())) {
      Pattern pattern = Pattern.compile("(?i).*/" + attachment.getId() + "/");
      Matcher matcher = pattern.matcher(attachment.getWebdavJcrPath());
      if (matcher.find()) {
        Node rootNode = session.getRootNode();
        try {
          Node webdavNode = rootNode.getNode(matcher.group());
          NodeIterator webdavNodeIt = webdavNode.getNodes();
          Date creationDate = null;
          while (webdavNodeIt.hasNext()) {
            Node currentLanguageNode = webdavNodeIt.nextNode();
            // Normaly, it must exist one filename node.
            Node currentFileNode = currentLanguageNode.getNodes().nextNode();
            if (creationDate == null ||
                creationDate.before(currentFileNode.getProperty(JCR_CREATED).getDate().getTime())) {
              creationDate = currentFileNode.getProperty(JCR_CREATED).getDate().getTime();
              contentEditionLanguage = currentLanguageNode.getName();
            }
          }
        } catch (PathNotFoundException pex) {
          // Node does not exist.
        }
      }
    }
    return contentEditionLanguage;
  }

  @Override
  public long getContentEditionSize(final Session session, final SimpleDocument attachment)
      throws RepositoryException {
    String currentLanguage = attachment.getLanguage();
    String webDavJcrPath = attachment.getWebdavJcrPath().replaceFirst("/[^/]+$", "");
    String languageInWebDav = getContentEditionLanguage(session, attachment);
    if (languageInWebDav != null) {
      if (!currentLanguage.equals(languageInWebDav)) {
        String before = attachment.getId() + "/" + currentLanguage;
        String after = attachment.getId() + "/" + languageInWebDav;
        webDavJcrPath = webDavJcrPath.replace(before, after);
      }
      try {
        Node rootNode = session.getRootNode();
        Node webdavFileNode = rootNode.getNode(webDavJcrPath).getNodes().nextNode();
        Binary webdavBinary = webdavFileNode.getNode(JCR_CONTENT).getProperty(JCR_DATA).getBinary();
        return webdavBinary.getSize();
      } catch (PathNotFoundException pex) {
        // Node does not exist.
      }
    }
    return -1;
  }
}
