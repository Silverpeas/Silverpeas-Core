package com.stratelia.silverpeas.versioning.jcr;

import java.io.IOException;

import javax.jcr.RepositoryException;
import javax.jcr.Session;

import com.stratelia.silverpeas.versioning.model.DocumentVersion;

public interface JcrDocumentDao {

  public void createDocumentNode(Session session, DocumentVersion document)
      throws RepositoryException, IOException;

  public void deleteDocumentNode(Session session, DocumentVersion document)
      throws RepositoryException, IOException;

  /**
   * Update the DocumentVersion using the node
   * 
   * @param session
   * @param document
   * @throws RepositoryException
   * @throws IOException
   */
  public void updateDocument(Session session, DocumentVersion document)
      throws RepositoryException, IOException;

  /**
   * Update the node using the DocumentVersion
   * 
   * @param session
   * @param document
   * @throws RepositoryException
   * @throws IOException
   */
  public void updateNodeDocument(Session session, DocumentVersion document)
      throws RepositoryException, IOException;

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
      throws RepositoryException;

}