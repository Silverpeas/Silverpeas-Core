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
package org.silverpeas.core.io.media;

import org.apache.tika.metadata.HttpHeaders;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.metadata.OfficeOpenXMLCore;
import org.apache.tika.metadata.Property;
import org.apache.tika.metadata.TikaCoreProperties;
import org.apache.tika.metadata.XMPDM;
import org.silverpeas.core.util.DateUtil;
import org.silverpeas.core.util.StringUtil;
import org.silverpeas.core.util.UnitUtil;
import org.silverpeas.core.util.memory.MemoryData;
import org.silverpeas.core.util.time.TimeData;
import org.silverpeas.core.util.time.TimeUnit;

import java.io.File;
import java.math.BigDecimal;
import java.text.ParseException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class MetaData {

  private final File source;
  private final Metadata metadata;

  MetaData(final File source, Metadata metadata) {
    this.source = source;
    this.metadata = metadata;
  }

  public String getValue(String name) {
    return metadata.get(name);
  }

  public List<String> getAvailablePropertyNames() {
    return Arrays.asList(metadata.names());
  }

  /**
   * Return Title of an Office document
   *
   * @return String
   */
  public String getTitle() {
    return cleanString(metadata.get(TikaCoreProperties.TITLE));
  }

  /**
   * Return Subject of an Office document
   *
   * @return String
   */
  public String getSubject() {
    String subject = metadata.get(metadata.get(Metadata.SUBJECT));
    if (!StringUtil.isDefined(subject)) {
      subject = metadata.get(OfficeOpenXMLCore.SUBJECT);
    }
    if (!StringUtil.isDefined(subject)) {
      subject = metadata.get(TikaCoreProperties.KEYWORDS);
    }
    return cleanString(subject);
  }

  /**
   * Return Author of an Office document
   *
   * @return String
   */
  public String getAuthor() {
    return cleanString(metadata.get(TikaCoreProperties.CREATOR));
  }

  /**
   * Return Comments of an Office document
   *
   * @return String
   */
  public String getComments() {
    String comments = metadata.get(TikaCoreProperties.COMMENTS);
    if (!StringUtil.isDefined(comments)) {
      comments = metadata.get(TikaCoreProperties.DESCRIPTION);
    }
    return cleanString(comments);
  }

  /**
   * Return Security of an Office document
   *
   * @return String
   */
  public int getSecurity() {
    return metadata.getInt(Property.internalInteger(Metadata.SECURITY));
  }

  /**
   * Return Keywords of an Office document
   *
   * @return String
   */
  public String[] getKeywords() {
    return cleanString(metadata.getValues(TikaCoreProperties.KEYWORDS));
  }

  /**
   * Return SILVERID of an Office document
   *
   * @return String
   */
  public String getSilverId() {
    return cleanString(metadata.get("SILVERID"));
  }

  /**
   * Return SILVERNAME of an Office document
   *
   * @return String
   */
  public String getSilverName() {
    return metadata.get("SILVERNAME");
  }

  public Date getLastSaveDateTime() {
    Date date = parseDate(TikaCoreProperties.MODIFIED);
    if (date == null) {
      return parseDate(Metadata.LAST_MODIFIED);
    }
    return date;
  }

  /**
   * Return CreateDateTime of an Office document
   */
  public Date getCreationDate() {
    Date result = getDate(TikaCoreProperties.CREATED);
    if (result == null) {
      result = metadata.getDate(TikaCoreProperties.CREATED);
    }
    if (result == null) {
      result = metadata.getDate(Metadata.DATE);
    }
    return result;
  }

  /**
   * Return the definition of the file.
   */
  public Definition getDefinition() {
    Definition definition = Definition.fromZero();
    Integer result = getInteger(Metadata.IMAGE_WIDTH);
    if (result == null) {
      result = getInteger(FLV.WIDTH);
    }
    if (result != null) {
      definition.widthOf(result);
    }
    result = getInteger(Metadata.IMAGE_LENGTH);
    if (result == null) {
      result = getInteger(FLV.HEIGHT);
    }
    if (result != null) {
      definition.heightOf(result);
    }
    return definition;
  }

  /**
   * Return the duration of the file.
   */
  public BigDecimal getFramerate() {
    BigDecimal result = getBigDecimal(XMPDM.VIDEO_FRAME_RATE);
    if (result == null) {
      result = getBigDecimal(FLV.FRAMERATE);
    }
    return result;
  }

  /**
   * Return the duration of the file.
   */
  public TimeData getDuration() {
    TimeData result = null;
    BigDecimal duration = getBigDecimal(XMPDM.DURATION);
    if (duration != null) {
      result = UnitUtil.getTimeData(duration);
    }
    if (result == null) {
      duration = getBigDecimal(FLV.DURATION);
      if (duration != null) {
        result = UnitUtil.getTimeData(duration, TimeUnit.SEC);
      }
    }
    return result;
  }

  /**
   * Return the memory data.
   */
  public MemoryData getMemoryData() {
    Long result = getLong(HttpHeaders.CONTENT_LENGTH);
    return result != null ? UnitUtil.getMemData(result) : null;
  }

  protected Date getDate(Property property) {
    Date result = metadata.getDate(property);
    if (result == null) {
      return parseDate(property);
    }
    return result;
  }

  protected Integer getInteger(Property property) {
    Integer result = metadata.getInt(property);
    if (result == null) {
      BigDecimal value = getBigDecimal(property);
      if (value != null) {
        result = value.intValue();
      }
    }
    return result;
  }

  protected Long getLong(Property property) {
    String result = cleanString(metadata.get(property));
    try {
      return Long.valueOf(result);
    } catch (Exception e) {
      return null;
    }
  }

  protected Long getLong(String propertyName) {
    String result = cleanString(metadata.get(propertyName));
    try {
      return Long.valueOf(result);
    } catch (Exception e) {
      return null;
    }
  }

  protected BigDecimal getBigDecimal(Property property) {
    String result = cleanString(metadata.get(property));
    try {
      return new BigDecimal(result);
    } catch (Exception e) {
      return null;
    }
  }

  protected Date parseDate(Property property) {
    String date = metadata.get(property);
    if (date != null) {
      try {
        return DateUtil.parse(date, "yyyy-MM-dd'T'HH:mm:ss'Z'");
      } catch (ParseException ex) {
        return null;
      }
    }
    return null;
  }

  private String[] cleanString(String[] values) {
    String[] result = new String[values.length];
    for (int i = 0; i < values.length; i++) {
      result[i] = cleanString(values[i]);
    }
    if (result.length > 1) {
      Set<String> uniqueResult = new LinkedHashSet<>();
      Collections.addAll(uniqueResult, result);
      return uniqueResult.toArray(new String[uniqueResult.size()]);
    }
    return result;
  }

  private String cleanString(String value) {
    if (StringUtil.isDefined(value)) {
      return value.replace("\u0000", "").replace("ï¿½ï¿½", "").trim();
    }
    return value;
  }

  private interface FLV {

    Property WIDTH = Property.internalText("width");

    Property HEIGHT = Property.internalText("height");

    Property DURATION = Property.internalText("duration");

    Property FRAMERATE = Property.internalText("framerate");
  }
}
