/**
 * Copyright (C) 2000 - 2009 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have received a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "http://repository.silverpeas.com/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.stratelia.webactiv.util.indexEngine.model;

import java.io.File;
import java.io.Serializable;

import com.silverpeas.util.i18n.I18NHelper;

/**
 * A FileDescription pack all the needed information to parse and index a file. We need :
 * <UL>
 * <LI>the path to the file</LI>
 * <LI>the encoding of the file</LI>
 * <LI>the format of the file</LI>
 * <LI>the language of the file</LI>
 * </UL>
 * The java.io.InputStreamReader javadoc page provides a link to a list of java-known encodings.
 */
public final class FileDescription implements Serializable {

  private static final long serialVersionUID = 6095740867318623417L;

  /**
   * Set the new FileDescription with the given path, encoding, format and language. The path should
   * not be null;
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
