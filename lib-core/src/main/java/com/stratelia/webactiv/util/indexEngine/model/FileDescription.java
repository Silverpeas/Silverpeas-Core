package com.stratelia.webactiv.util.indexEngine.model;

import java.io.File;
import java.io.Serializable;

import com.silverpeas.util.i18n.I18NHelper;

/**
 * A FileDescription pack all the needed information to parse and index a file.
 * 
 * We need :
 * <UL>
 * <LI>the path to the file</LI>
 * <LI>the encoding of the file</LI>
 * <LI>the format of the file</LI>
 * <LI>the language of the file</LI>
 * </UL>
 * 
 * The java.io.InputStreamReader javadoc page provides a link to a list of
 * java-known encodings.
 */
public final class FileDescription implements Serializable {
  /**
   * Set the new FileDescription with the given path, encoding, format and
   * language.
   * 
   * The path should not be null;
   */
  public FileDescription(String path, String encoding, String format,
      String lang) {
    this.path = path.replace('\\', File.separatorChar);
    this.encoding = encoding;
    this.format = format;

    this.lang = I18NHelper.checkLanguage(lang);
  }

  /**
   * Return the file path
   */
  public String getPath() {
    return path;
  }

  /**
   * Return the file encoding
   */
  public String getEncoding() {
    return encoding;
  }

  /**
   * Return the file format
   */
  public String getFormat() {
    return format;
  }

  /**
   * Return the file language
   */
  public String getLang() {
    return lang;
  }

  /**
   * All the attributes are private and final.
   */
  private final String path;
  private final String encoding;
  private final String format;
  private final String lang;
}
