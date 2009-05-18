package com.stratelia.webactiv.util.attachment.model.jcr;

import java.io.IOException;

import javax.jcr.RepositoryException;
import javax.jcr.Session;

import com.stratelia.webactiv.util.attachment.model.AttachmentDetail;

public interface JcrAttachmentDao {

  /**
   * Create a new node for the specified attachment so that the file may be
   * accessed through webdav.
   * 
   * @param session
   *          the JCR session.
   * @param attachment
   *          the attachment.
   * @param language
   *          the language to obtain the file.
   */
  public void createAttachmentNode(Session session,
      AttachmentDetail attachment, String language) throws RepositoryException,
      IOException;

  /**
   * Delete the node associated to the specified attachment.
   * 
   * @param session
   *          the JCR session.
   * @param attachment
   *          the attachment.
   * @param language
   *          the language to obtain the file.
   */
  public void deleteAttachmentNode(Session session,
      AttachmentDetail attachment, String language) throws RepositoryException,
      IOException;

  /**
   * Update the AttachmentDetail using the node
   * 
   * @param session
   *          the JCR session.
   * @param attachment
   * @param language
   * @throws RepositoryException
   * @throws IOException
   */
  public void updateAttachment(Session session, AttachmentDetail attachment,
      String language) throws RepositoryException, IOException;

  /**
   * Update the node using the AttachmentDetail
   * 
   * @param session
   *          the JCR session.
   * @param attachment
   * @param language
   * @throws RepositoryException
   * @throws IOException
   */
  public void updateNodeAttachment(Session session,
      AttachmentDetail attachment, String language) throws RepositoryException,
      IOException;

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
   */
  public boolean isNodeLocked(Session session, AttachmentDetail attachment,
      String language) throws RepositoryException;

}