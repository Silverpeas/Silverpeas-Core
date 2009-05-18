package com.stratelia.silverpeas.versioning.jcr;

import javax.jcr.RepositoryException;

import com.stratelia.silverpeas.versioning.model.DocumentVersion;

public interface JcrDocumentService {

  public void createDocument(DocumentVersion document);

  public void getUpdatedDocument(DocumentVersion document);

  public void deleteDocument(DocumentVersion document);

  public void updateDocument(DocumentVersion document);

  /**
   * Indicate if the node for the specified attachment is currently locked (for
   * example by Office in the case of a webdav online edition).
   * 
   * @param attachment
   *          the attachment.
   * @param language
   *          the language to obtain the file.
   * @return true if the node is locked - false otherwise.
   * @throws RepositoryException
   */
  public boolean isNodeLocked(DocumentVersion document);

}