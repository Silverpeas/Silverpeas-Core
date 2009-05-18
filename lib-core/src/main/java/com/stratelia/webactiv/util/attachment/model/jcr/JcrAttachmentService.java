package com.stratelia.webactiv.util.attachment.model.jcr;

import com.stratelia.webactiv.util.attachment.model.AttachmentDetail;

public interface JcrAttachmentService {

  /**
   * Create a new node for the specified attachment so that the file may be accessed through webdav.
   * @param attachment the attachment.
   * @param language the language to obtain the file.
   */
  public void createAttachment(AttachmentDetail attachment, String language);

  /**
   * Update the attachment content with the data from the node.
   * @param attachment the attachment.
   * @param language the language to obtain the file.
   */
  public void getUpdatedDocument(AttachmentDetail attachment, String language);

  /**
   * Delete the node associated to the specified attachment.
   * @param attachment the attachment.
   * @param language the language to obtain the file.
   */
  public void deleteAttachment(AttachmentDetail attachment, String language);

  /**
   * Update the node content with the attachment data.
   * @param attachment the attachment.
   * @param language the language to obtain the file.
   */
  public void updateNodeAttachment(AttachmentDetail attachment, String language);
  
  /**
   * Indicate if the node for the specified attachment is currently locked 
   * (for example by Office in the case of a webdav online edition).
   * @param attachment the attachment.
   * @param language the language to obtain the file.
   * @return true if the node is locked - false otherwise.
   */
  public boolean isNodeLocked(AttachmentDetail attachment, String language);

}