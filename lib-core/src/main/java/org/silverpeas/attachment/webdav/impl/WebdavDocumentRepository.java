/**
 * Copyright (C) 2000 - 2011 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU Affero General Public License as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of the GPL, you may
 * redistribute this Program in connection with Free/Libre Open Source Software ("FLOSS")
 * applications as described in Silverpeas's FLOSS exception. You should have received a copy of the
 * text describing the FLOSS exception, and it is also available here:
 * "http://repository.silverpeas.com/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License along with this program.
 * If not, see <http://www.gnu.org/licenses/>.
 */
package org.silverpeas.attachment.webdav.impl;

import java.util.Calendar;

import javax.inject.Named;
import javax.jcr.Binary;
import javax.jcr.Node;
import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.silverpeas.attachment.model.SimpleDocument;
import org.silverpeas.attachment.webdav.WebdavRepository;

import com.silverpeas.jcrutil.JcrConstants;
import com.silverpeas.util.StringUtil;

import static com.silverpeas.jcrutil.JcrConstants.*;
import static javax.jcr.Property.*;

@Named("webdavRepository")
public class WebdavDocumentRepository implements WebdavRepository {

  @Override
  public void createAttachmentNode(Session session, SimpleDocument attachment) throws
      RepositoryException {
    Node rootNode = session.getRootNode();
    Node attachmentFolder = addFolder(rootNode, SimpleDocument.WEBDAV_FOLDER);
    attachmentFolder = addFolder(attachmentFolder, SimpleDocument.ATTACHMENTS_FOLDER);
    Node componentFolder = addFolder(attachmentFolder, attachment.getInstanceId());
    Node contextFolder = componentFolder;
    if (attachment.getId() != null) {
      contextFolder = addFolder(contextFolder, attachment.getId());
    }
    addFile(contextFolder, attachment);
  }

  @Override
  public void updateNodeAttachment(Session session, SimpleDocument attachment) throws
      RepositoryException {
    Node rootNode = session.getRootNode();
    try {
      Node fileNode = rootNode.getNode(attachment.getWebdavJcrPath());
      setContent(fileNode, attachment);
    } catch (PathNotFoundException pex) {
      createAttachmentNode(session, attachment);
    }
  }

  @Override
  public void deleteAttachmentNode(Session session, SimpleDocument attachment) throws
      RepositoryException {
    Node rootNode = session.getRootNode();
    try {
      Node fileNode = rootNode.getNode(attachment.getWebdavJcrPath());
      fileNode.remove();
    } catch (PathNotFoundException pex) {
      // Since the node doesn't exist, deleting it has no effect.
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
  public void updateAttachment(Session session, SimpleDocument attachment) throws
      RepositoryException {
    Node rootNode = session.getRootNode();
    Node webdavFileNode = rootNode.getNode(attachment.getWebdavJcrPath());
    Node attachmentNode = session.getNode(attachment.getFullJcrContentPath());
    Binary webdavBinary = webdavFileNode.getNode(JCR_CONTENT).getProperty(JCR_DATA).getBinary();
    attachmentNode.getNode(JCR_CONTENT).setProperty(JCR_DATA, webdavBinary);
    webdavBinary.dispose();
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
      return parent.addNode(name, JcrConstants.NT_FOLDER);
    }
  }

  /**
   * Add a file node into the repository
   *
   * @param folder the folder node containing the file node.
   * @param attachment the attachment for the file.
   * @return the created node.
   * @throws RepositoryException
   */
  protected Node addFile(Node folder, SimpleDocument attachment) throws RepositoryException {
    String escapedName = StringUtil.escapeQuote(attachment.getFilename());
    if (folder.hasNode(escapedName)) {
      folder.getNode(escapedName).remove();
    }
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

  protected void setContent(Node fileNode, SimpleDocument attachment) throws RepositoryException {
    Node attachmentNode = fileNode.getSession().getNode(attachment.getFullJcrContentPath());
    Binary attachmentBinary = attachmentNode.getNode(JCR_CONTENT).getProperty(JCR_DATA).getBinary();
    fileNode.getNode(JCR_CONTENT).setProperty(JCR_DATA, attachmentBinary);
    attachmentBinary.dispose();
  }
}
