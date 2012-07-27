package com.stratelia.webactiv.util.indexEngine.model;

import java.io.Serializable;

/**
 * Reference entry to a binary content in a JCR repository
 * 
 * @author Emmanuel Hugonnet
 * 
 */
public class JcrContent extends FileDescription implements Serializable {

  public JcrContent(String path, String encoding, String mimeType, String lang) {
    super(path, encoding, mimeType, lang);
  }

}
