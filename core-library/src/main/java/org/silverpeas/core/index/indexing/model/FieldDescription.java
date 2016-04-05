/*
 * Copyright (C) 2000 - 2016 Silverpeas
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
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.silverpeas.core.index.indexing.model;

import java.io.Serializable;
import java.util.Date;

import org.silverpeas.core.index.indexing.DateFormatter;
import org.silverpeas.core.i18n.I18NHelper;

/**
 * A FieldDescription pack all the needed information to parse and index a generic field (xml field,
 * iptc field) We need :
 * <UL>
 * <LI>the name of the field</LI>
 * <LI>its content</LI>
 * <LI>its language</LI>
 * <LI>to know if its value must be stored in the index (then value could be exploited directly by search engine as facet for example)</LI>
 * </UL>
 */
public class FieldDescription implements Serializable {

  private static final long serialVersionUID = -475049855423827178L;

  public FieldDescription(String fieldName, String content, String lang) {
    this.content = content;
    this.lang = I18NHelper.checkLanguage(lang);
    this.fieldName = fieldName;
    this.stored = false;
  }

  public FieldDescription(String fieldName, String content, String lang, boolean stored) {
    this.content = content;
    this.lang = I18NHelper.checkLanguage(lang);
    this.fieldName = fieldName;
    this.stored = stored;
  }

  public FieldDescription(String fieldName, Date begin, Date end, String lang) {
    String content = "";
    if (begin != null && end != null)
      content = "[" + DateFormatter.date2IndexFormat(begin) + " TO "
          + DateFormatter.date2IndexFormat(end) + "]";
    else if (begin != null && end == null)
      content = "[" + DateFormatter.date2IndexFormat(begin) + " TO "
          + DateFormatter.nullEndDate + "]";
    else if (begin == null && end != null)
      content = "[" + DateFormatter.nullBeginDate + " TO "
          + DateFormatter.date2IndexFormat(end) + "]";

    this.content = content;
    this.lang = I18NHelper.checkLanguage(lang);
    this.fieldName = fieldName;
    this.stored = false;
  }

  /**
   * Return the fieldName
   */
  public String getFieldName() {
    return fieldName;
  }

  /**
   * Return the content itself
   */
  public String getContent() {
    return content;
  }

  /**
   * Return the content language
   */
  public String getLang() {
    return lang;
  }

  public boolean isStored() {
    return stored;
  }

  /**
   * All the attributes are private and final.
   */
  private final String content;
  private final String lang;
  private final String fieldName;
  private final boolean stored;
}
