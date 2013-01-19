package org.silverpeas.util.mail;

import java.util.List;

public interface MailExtractor {
  
  public Mail getMail() throws Exception;
  
  public List<MailAttachment> getAttachments() throws Exception;
  
}
