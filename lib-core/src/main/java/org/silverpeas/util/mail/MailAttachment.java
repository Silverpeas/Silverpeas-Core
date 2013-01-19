package org.silverpeas.util.mail;

import java.io.InputStream;

public class MailAttachment {
  
  private String name;
  private InputStream file;
  
  public MailAttachment(String name) {
    setName(name);
  }
  
  public String getName() {
    return name;
  }
  public void setName(String name) {
    this.name = name;
  }
  public InputStream getFile() {
    return file;
  }
  public void setFile(InputStream file) {
    this.file = file;
  }

}
