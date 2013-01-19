package org.silverpeas.util.mail;

import java.io.File;

public class Extractor {
  
  public static MailExtractor getExtractor(File file) throws Exception {
    if (!file.exists() || file.isDirectory()) {
      return null;
    }
    if (file.getName().toLowerCase().endsWith(".eml")){
      return new EMLExtractor(file);
    } else if (file.getName().toLowerCase().endsWith(".msg")) {
      return new MSGExtractor(file);
    }
    return null;
  }

}
