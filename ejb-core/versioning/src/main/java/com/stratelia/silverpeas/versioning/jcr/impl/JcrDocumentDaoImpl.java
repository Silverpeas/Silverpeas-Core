package com.stratelia.silverpeas.versioning.jcr.impl;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Calendar;

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
import com.stratelia.silverpeas.versioning.jcr.JcrDocumentDao;
import com.stratelia.silverpeas.versioning.model.DocumentVersion;

public class JcrDocumentDaoImpl implements JcrDocumentDao {

  /*
   * (non-Javadoc)
   * 
   * @seecom.stratelia.webactiv.util.document.model.jcr.impl.JcrdocumentDao#
   * createdocumentNode(javax.jcr.Session,
   * com.stratelia.webactiv.util.document.model.DocumentVersion,
   * java.lang.String)
   */
  public void createDocumentNode(Session session, DocumentVersion document)
      throws RepositoryException, IOException {
    Node rootNode = session.getRootNode();
    Node componentFolder = addFolder(rootNode, document.getInstanceId());
    Node contextFolder = componentFolder;
    contextFolder = addFolder(contextFolder, DocumentVersion.CONTEXT);
    if (document.getDocumentPK().getId() != null) {
      contextFolder = addFolder(contextFolder, document.getDocumentPK().getId());
    }
    contextFolder = addFolder(contextFolder, document.getMajorNumber() + "."
        + document.getMinorNumber());
    addFile(contextFolder, document);
  }

  public void updateNodeDocument(Session session, DocumentVersion document)
      throws RepositoryException, IOException {
    Node rootNode = session.getRootNode();
    try {
      Node fileNode = rootNode.getNode(document.getJcrPath());
      setContent(fileNode, document);
    } catch (PathNotFoundException pex) {
      createDocumentNode(session, document);
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @seecom.stratelia.webactiv.util.document.model.jcr.impl.JcrdocumentDao#
   * deletedocumentNode(javax.jcr.Session,
   * com.stratelia.webactiv.util.document.model.DocumentVersion,
   * java.lang.String)
   */
  public void deleteDocumentNode(Session session, DocumentVersion document)
      throws RepositoryException, IOException {
    Node rootNode = session.getRootNode();
    try {
      Node fileNode = rootNode.getNode(document.getJcrPath());
      Node contentNode = fileNode.getNode(JcrConstants.JCR_CONTENT);
      contentNode.remove();
      fileNode.remove();
    } catch (PathNotFoundException pex) {
      // Le noeud n'existe pas
    }
  }

  public void updateDocument(Session session, DocumentVersion document)
      throws RepositoryException, IOException {
    Node rootNode = session.getRootNode();
    Node fileNode = rootNode.getNode(document.getJcrPath());
    String filePath = document.getDocumentPath();
    File updateddocument = new File(filePath);
    File oldFile = new File(filePath + ".old");
    boolean tempSave = updateddocument.renameTo(oldFile);
    FileOutputStream out = null;
    InputStream in = null;
    try {
      out = new FileOutputStream(updateddocument);
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
      fileNode.getNode(JcrConstants.JCR_CONTENT).setProperty(
          JcrConstants.JCR_DATA, new ByteArrayInputStream(new byte[0]));
      fileNode.remove();
      document.setSize(new Long(updateddocument.length()).intValue());
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
   * Indicate if the node for the specified attachment is currently locked (for
   * example by Office in the case of a webdav online edition).
   * 
   * @param session
   *          the JCR session.
   * @param attachment
   *          the attachment.
   * @param language
   *          the language to obtain the file.
   * @return true if the node is locked - false otherwise.
   * @throws RepositoryException
   */
  public boolean isNodeLocked(Session session, DocumentVersion document)
      throws RepositoryException {
    Node rootNode = session.getRootNode();
    try {
      Node fileNode = rootNode.getNode(document.getJcrPath());
      return fileNode.isLocked();
    } catch (PathNotFoundException pex) {
      return false;
    }
  }

  /**
   * Add a folder node into the repository
   * 
   * @param parent
   *          the parent node
   * @param name
   *          the name of the new node
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
   * 
   * @param parent
   *          the folder node containing the file node.
   * @param document
   *          the document for the file.
   * @param language
   *          the language for the file.
   * @return the created node.
   * @throws IOException
   * @throws RepositoryException
   */
  protected Node addFile(Node folder, DocumentVersion document)
      throws RepositoryException, IOException {
    String escapedName = StringUtil.escapeQuote(document.getLogicalName());
    Node fileNode = folder.addNode(escapedName, JcrConstants.NT_FILE);
    if (document.getAuthorId() > 0) {
      fileNode.addMixin(JcrConstants.SLV_OWNABLE_MIXIN);
      fileNode.setProperty(JcrConstants.SLV_PROPERTY_OWNER, ""
          + document.getAuthorId());
    }
    Node contentNode = fileNode.addNode(JcrConstants.JCR_CONTENT,
        JcrConstants.NT_RESOURCE);
    contentNode.setProperty(JcrConstants.JCR_MIMETYPE, document.getMimeType());
    contentNode.setProperty(JcrConstants.JCR_ENCODING, "");
    setContent(fileNode, document);
    return fileNode;

  }

  protected void setContent(Node fileNode, DocumentVersion document)
      throws RepositoryException, IOException {
    File file = new File(document.getDocumentPath());
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

}
