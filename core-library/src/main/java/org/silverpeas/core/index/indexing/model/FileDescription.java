/*
 * Copyright (C) 2000 - 2016 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU Affero General Public License as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of the GPL, you may
 * redistribute this Program in connection with Free/Libre Open Source Software ("FLOSS")
 * applications as described in Silverpeas's FLOSS exception. You should have received a copy of the
 * text describing the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License along with this program.
 * If not, see <http://www.gnu.org/licenses/>.
 */
package org.silverpeas.core.index.indexing.model;

import org.silverpeas.core.i18n.I18NHelper;
import java.io.File;
import java.io.Serializable;

/**
 * A FileDescription pack all the needed information to parse and index a file. We need : <UL>
 * <LI>the path to the file</LI> <LI>the encoding of the file</LI> <LI>the format of the file</LI>
 * <LI>the language of the file</LI> </UL> The java.io.InputStreamReader javadoc page provides a
 * link to a list of java-known encodings.
 */
public class FileDescription implements Serializable {

  private static final long serialVersionUID = 6095740867318623417L;

  /**
   * Set the new FileDescription with the given path, encoding, format and language. The path must
   * not be null.
   *
   * @param path
   * @param encoding
   * @param format
   * @param lang
   */
  public FileDescription(String path, String encoding, String format, String lang) {
    this.path = path.replace('\\', File.separatorChar);
    this.encoding = encoding;
    this.format = format;
    this.lang = I18NHelper.checkLanguage(lang);
  }

  /**
   * Return the file path
   * @return
   */
  public String getPath() {
    return path;
  }

  /**
   * Return the file encoding
   * @return
   */
  public String getEncoding() {
    return encoding;
  }

  /**
   * Return the file format
   * @return
   */
  public String getFormat() {
    return format;
  }

  /**
   * Return the file language
   * @return
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

  @Override
  public boolean equals(final Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    final FileDescription that = (FileDescription) o;

    if (format != null ? !format.equals(that.format) : that.format != null) {
      return false;
    }
    if (lang != null ? !lang.equals(that.lang) : that.lang != null) {
      return false;
    }
    if (!path.equals(that.path)) {
      return false;
    }

    return true;
  }

  @Override
  public int hashCode() {
    int result = path.hashCode();
    result = 31 * result + (format != null ? format.hashCode() : 0);
    result = 31 * result + (lang != null ? lang.hashCode() : 0);
    return result;
  }
}
