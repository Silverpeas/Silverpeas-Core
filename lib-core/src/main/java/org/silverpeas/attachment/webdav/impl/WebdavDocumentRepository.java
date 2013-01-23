/**
 * Copyright (C) 2000 - 2012 Silverpeas
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
package org.silverpeas.attachment.webdav.impl;

import com.silverpeas.util.StringUtil;
import com.silverpeas.util.i18n.I18NHelper;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.silverpeas.attachment.model.DocumentType;
import org.silverpeas.attachment.model.SimpleDocument;
import org.silverpeas.attachment.webdav.WebdavRepository;

import javax.inject.Named;
import javax.jcr.Binary;
import javax.jcr.Node;
import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Calendar;

import static com.silverpeas.jcrutil.JcrConstants.*;

@Named("webdavRepository")
public class WebdavDocumentRepository implements WebdavRepository {

  @Override
  public void createAttachmentNode(Session session, SimpleDocument attachment) throws
      RepositoryException, IOException {
    Node rootNode = session.getRootNode();
    Node attachmentFolder = addFolder(rootNode, SimpleDocument.WEBDAV_FOLDER);
    attachmentFolder = addFolder(attachmentFolder, DocumentType.attachment.getFolderName());
    Node componentFolder = addFolder(attachmentFolder, attachment.getInstanceId());
    Node contextFolder = componentFolder;
    if (attachment.getId() != null) {
      contextFolder = addFolder(contextFolder, attachment.getId());
    }
    String lang = attachment.getLanguage();
    if (!StringUtil.isDefined(lang)) {
      lang = I18NHelper.defaultLanguage;
    }
    contextFolder = addFolder(contextFolder, lang);
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
      RepositoryException, IOException {
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

  protected void setContent(Node fileNode, SimpleDocument attachment) throws RepositoryException,
      IOException {

    InputStream in = FileUtils.openInputStream(new File(attachment.getAttachmentPath()));
    try {
      Binary attachmentBinary = fileNode.getSession().getValueFactory().createBinary(in);
      fileNode.getNode(JCR_CONTENT).setProperty(JCR_DATA, attachmentBinary);
    } finally {
      IOUtils.closeQuietly(in);
    }
  }
}
