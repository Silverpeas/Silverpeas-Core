/*
 * Copyright (C) 2000 - 2022 Silverpeas
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
 * "https://www.silverpeas.org/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package org.silverpeas.core.index.indexing.model;

import org.silverpeas.core.i18n.I18NHelper;
import org.silverpeas.core.util.DateUtil;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.Date;
import java.util.Objects;

import static org.silverpeas.core.util.StringUtil.isNotDefined;

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

  /**
   * All the attributes are private and final.
   */
  private final String content;
  private final String lang;
  private final String fieldName;
  private final boolean stored;
  private final boolean basedOnDates;
  private final LocalDate startDate;
  private final LocalDate endDate;

  public FieldDescription(String fieldName, String content, String lang) {
    this.content = content;
    this.lang = I18NHelper.checkLanguage(lang);
    this.fieldName = fieldName;
    this.stored = false;
    this.basedOnDates = false;
    this.startDate = null;
    this.endDate = null;
  }

  public FieldDescription(String fieldName, String content, String lang, boolean stored) {
    this.content = content;
    this.lang = I18NHelper.checkLanguage(lang);
    this.fieldName = fieldName;
    this.stored = stored;
    this.basedOnDates = false;
    this.startDate = null;
    this.endDate = null;
  }

  public FieldDescription(String fieldName, Date date, String lang, boolean stored) {
    this.content = DateUtil.formatAsLuceneDate(DateUtil.toLocalDate(date));
    this.lang = I18NHelper.checkLanguage(lang);
    this.fieldName = fieldName;
    this.stored = stored;
    this.basedOnDates = false;
    this.startDate = null;
    this.endDate = null;
  }

  public FieldDescription(String fieldName, Date begin, Date end, String lang) {
    this.content = "";
    this.lang = I18NHelper.checkLanguage(lang);
    this.fieldName = fieldName;
    this.stored = false;
    this.basedOnDates = true;
    this.startDate = DateUtil.toLocalDate(begin);
    this.endDate = DateUtil.toLocalDate(end);
  }

  public boolean isBasedOnDate() {
    return basedOnDates;
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

  public LocalDate getStartDate() {
    return startDate;
  }

  public LocalDate getEndDate() {
    return endDate;
  }

  public boolean isEmpty() {
    return isNotDefined(content);
  }

  @Override
  public boolean equals(final Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    final FieldDescription that = (FieldDescription) o;
    return stored == that.stored && basedOnDates == that.basedOnDates &&
        Objects.equals(content, that.content) && Objects.equals(lang, that.lang) &&
        Objects.equals(fieldName, that.fieldName) && Objects.equals(startDate, that.startDate) &&
        Objects.equals(endDate, that.endDate);
  }

  @Override
  public int hashCode() {
    return Objects.hash(content, lang, fieldName, stored, basedOnDates, startDate, endDate);
  }
}