package com.stratelia.webactiv.util.attachment.control;

import com.silverpeas.jcrutil.BasicDaoFactory;
import com.stratelia.webactiv.util.attachment.model.jcr.JcrAttachmentService;

public class RepositoryHelper {
  public static final String JRC_ATTACHMENT_SERVICE = "jcrAttachmentManager";

  public static final String ATTACHMENT_SERVICE = "attachmentBm";

  public static JcrAttachmentService getJcrAttachmentService() {
    return (JcrAttachmentService) BasicDaoFactory
        .getBean(JRC_ATTACHMENT_SERVICE);
  }

  public static AttachmentBm getAttachmentService() {
    return (AttachmentBm) BasicDaoFactory.getBean(ATTACHMENT_SERVICE);
  }
}