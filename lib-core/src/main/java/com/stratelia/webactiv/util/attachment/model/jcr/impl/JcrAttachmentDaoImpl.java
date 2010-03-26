/**
 * Copyright (C) 2000 - 2009 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have recieved a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "http://repository.silverpeas.com/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.stratelia.webactiv.util.attachment.model.jcr.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Calendar;
import java.util.Date;

import javax.jcr.ItemExistsException;
import javax.jcr.Node;
import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.lock.LockException;
import javax.jcr.nodetype.ConstraintViolationException;
import javax.jcr.nodetype.NoSuchNodeTypeException;
import javax.jcr.version.VersionException;

import com.silverpeas.jcrutil.JcrConstants;

import com.silverpeas.util.StringUtil;
import com.stratelia.webactiv.util.FileRepositoryManager;
import com.stratelia.webactiv.util.attachment.model.AttachmentDetail;
import com.stratelia.webactiv.util.attachment.model.jcr.JcrAttachmentDao;

public class JcrAttachmentDaoImpl implements JcrAttachmentDao {

  /*
   * (non-Javadoc)
   * @see com.stratelia.webactiv.util.attachment.model.jcr.impl.JcrAttachmentDao#
   * createAttachmentNode(javax.jcr.Session,
   * com.stratelia.webactiv.util.attachment.model.AttachmentDetail, java.lang.String)
   */
  public void createAttachmentNode(Session session,
      AttachmentDetail attachment, String language) throws RepositoryException,
      IOException {
    Node rootNode = session.getRootNode();
    Node attachmentFolder = addFolder(rootNode,
        AttachmentDetail.ATTACHMENTS_FOLDER);
    Node componentFolder = addFolder(attachmentFolder, attachment
        .getInstanceId());
    Node contextFolder = componentFolder;
    if (attachment.getContext() != null && !"".equals(attachment.getContext())) {
      String[] contexts = FileRepositoryManager.getAttachmentContext(attachment
          .getContext());
      for (int i = 0; i < contexts.length; i++) {
        contextFolder = addFolder(contextFolder, contexts[i]);
      }
    }
    if (attachment.getPK().getId() != null) {
      contextFolder = addFolder(contextFolder, attachment.getPK().getId());
    }
    if (language != null && !"".equals(language.trim())) {
      contextFolder = addFolder(contextFolder, language);
    }
    addFile(contextFolder, attachment, language);
  }

  public void updateNodeAttachment(Session session,
      AttachmentDetail attachment, String language) throws RepositoryException,
      IOException {
    Node rootNode = session.getRootNode();
    try {
      Node fileNode = rootNode.getNode(attachment.getJcrPath(language));
      setContent(fileNode, attachment, language);
    } catch (PathNotFoundException pex) {
      createAttachmentNode(session, attachment, language);
    }
  }

  /*
   * (non-Javadoc)
   * @see com.stratelia.webactiv.util.attachment.model.jcr.impl.JcrAttachmentDao#
   * deleteAttachmentNode(javax.jcr.Session,
   * com.stratelia.webactiv.util.attachment.model.AttachmentDetail, java.lang.String)
   */
  public void deleteAttachmentNode(Session session,
      AttachmentDetail attachment, String language) throws RepositoryException,
      IOException {
    Node rootNode = session.getRootNode();
    try {
      Node fileNode = rootNode.getNode(attachment.getJcrPath(language));
      fileNode.remove();
    } catch (PathNotFoundException pex) {
      // Le noeud n'existe pas
    }
  }

  /**
   * Indicate if the node for the specified attachment is currently locked (for example by Office in
   * the case of a webdav online edition).
   * @param session the JCR session.
   * @param attachment the attachment.
   * @param language the language to obtain the file.
   * @return true if the node is locked - false otherwise.
   * @throws RepositoryException
   */
  public boolean isNodeLocked(Session session, AttachmentDetail attachment,
      String language) throws RepositoryException {
    Node rootNode = session.getRootNode();
    try {
      Node fileNode = rootNode.getNode(attachment.getJcrPath(language));
      return fileNode.isLocked();
    } catch (PathNotFoundException pex) {
      return false;
    }
  }

  public void updateAttachment(Session session, AttachmentDetail attachment,
      String language) throws RepositoryException, IOException {
    Node rootNode = session.getRootNode();
    Node fileNode = rootNode.getNode(attachment.getJcrPath(language));
    String filePath = attachment.getAttachmentPath(language);
    File updatedAttachment = new File(filePath);
    File oldFile = new File(filePath + ".old");
    boolean tempSave = updatedAttachment.renameTo(oldFile);
    FileOutputStream out = null;
    InputStream in = null;
    try {
      out = new FileOutputStream(updatedAttachment);
      in = fileNode.getNode(JcrConstants.JCR_CONTENT).getProperty(
          JcrConstants.JCR_DATA).getStream();
      byte[] buffer = new byte[8];
      int c = 0;
      while ((c = in.read(buffer)) != -1) {
        out.write(buffer, 0, c);
      }
      in.close();
      in = null;
      out.flush();
      out.close();
      fileNode.remove();
      attachment.setSize(updatedAttachment.length());
      attachment.setCreationDate(new Date());
      if (tempSave) {
        oldFile.delete();
      }
    } finally {
      if (out != null) {
        out.close();
      }
      if (in != null) {
        in.close();
      }
    }

  }

  /**
   * Add a folder node into the repository
   * @param parent the parent node
   * @param name the name of the new node
   * @return the created node.
   * @throws ItemExistsException
   * @throws PathNotFoundException
   * @throws NoSuchNodeTypeException
   * @throws LockException
   * @throws VersionException
   * @throws ConstraintViolationException
   * @throws RepositoryException
   */
  protected Node addFolder(Node parent, String name)
      throws ItemExistsException, PathNotFoundException,
      NoSuchNodeTypeException, LockException, VersionException,
      ConstraintViolationException, RepositoryException {
    try {
      return parent.getNode(name);
    } catch (PathNotFoundException pnfex) {
      return parent.addNode(name, JcrConstants.NT_FOLDER);
    }
  }

  /**
   * Add a file node into the repository
   * @param parent the folder node containing the file node.
   * @param attachment the attachment for the file.
   * @param language the language for the file.
   * @return the created node.
   * @throws IOException
   * @throws RepositoryException
   */
  protected Node addFile(Node folder, AttachmentDetail attachment,
      String language) throws RepositoryException, IOException {
    String escapedName = StringUtil.escapeQuote(attachment
        .getLogicalName(getLanguage(language)));
    if (folder.hasNode(escapedName)) {
      folder.getNode(escapedName).remove();
    }
    Node fileNode = folder.addNode(escapedName, JcrConstants.NT_FILE);
    if (attachment.getWorkerId() != null) {
      fileNode.addMixin(JcrConstants.SLV_OWNABLE_MIXIN);
      fileNode.setProperty(JcrConstants.SLV_PROPERTY_OWNER, attachment
          .getWorkerId());
    }
    Node contentNode = fileNode.addNode(JcrConstants.JCR_CONTENT,
        JcrConstants.NT_RESOURCE);
    contentNode.setProperty(JcrConstants.JCR_MIMETYPE, attachment
        .getType(language));
    contentNode.setProperty(JcrConstants.JCR_ENCODING, "");
    contentNode.setProperty(JcrConstants.JCR_LASTMODIFIED, Calendar
        .getInstance());
    setContent(fileNode, attachment, language);
    return fileNode;

  }

  protected void setContent(Node fileNode, AttachmentDetail attachment,
      String language) throws RepositoryException, IOException {
    File file = new File(attachment.getAttachmentPath(language));
    FileInputStream in = null;
    try {
      in = new FileInputStream(file);
      Node contentNode = fileNode.getNode(JcrConstants.JCR_CONTENT);
      contentNode.setProperty(JcrConstants.JCR_DATA, in);
      Calendar lastModified = Calendar.getInstance();
      lastModified.setTimeInMillis(file.lastModified());
      contentNode.setProperty(JcrConstants.JCR_LASTMODIFIED, lastModified);
    } finally {
      if (in != null) {
        in.close();
      }
    }
  }

  protected String getLanguage(String language) {
    if (language != null
        && ("fr".equalsIgnoreCase(language) || "".equals(language.trim()))) {
      return null;
    }
    return language;
  }

}
