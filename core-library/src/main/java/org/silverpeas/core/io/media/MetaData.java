/*
 * Copyright (C) 2000 - 2024 Silverpeas
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
package org.silverpeas.core.io.media;

import org.apache.tika.metadata.*;
import org.silverpeas.core.date.TimeUnit;
import org.silverpeas.core.util.DateUtil;
import org.silverpeas.kernel.util.StringUtil;
import org.silverpeas.core.util.UnitUtil;
import org.silverpeas.core.util.memory.MemoryData;
import org.silverpeas.core.util.time.Duration;

import java.math.BigDecimal;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Metadata embedded in a media, whatever its type or format. It wraps the Tika Metadata to both
 * provide unified access to a metadata property that can have a different name according to type of
 * the format of the media, and to avoid Tika property name or namespace evolution in the time.
 */
public class MetaData {

  private final Metadata tikaMetadata;

  MetaData(Metadata metadata) {
    this.tikaMetadata = metadata;
  }

  /**
   * Gets the value of the specified property name.
   * @param name the name of a metadata property.
   * @return the value of the property.
   */
  public String getValue(String name) {
    return tikaMetadata.get(name);
  }

  /**
   * Gets all name of all the properties in this metadata instance.
   * @return a list of property names.
   */
  @SuppressWarnings("unused")
  public List<String> getAvailablePropertyNames() {
    return Arrays.asList(tikaMetadata.names());
  }

  /**
   * Gets the title as set in the media's metadata.
   * @return the title the media related by this metadata
   */
  public String getTitle() {
    return cleanString(tikaMetadata.get(TikaCoreProperties.TITLE));
  }

  /**
   * Gets the content type set in the media's metadata.
   * @return the content type if such information is available in the metadata, null otherwise.
   */
  public String getContentType() {
    String contentType = tikaMetadata.get(HttpHeaders.CONTENT_TYPE);
    if (StringUtil.isNotDefined(contentType)) {
      contentType = tikaMetadata.get(TikaCoreProperties.FORMAT);
    }
    return contentType;
  }

  /**
   * Gets the subject as set in the media's metadata. Because the subject can be covered by a
   * property that differs from a media format to another one, this property is seeked for each of
   * them. And because the property in which the subject is set can also refer others values than
   * the subject itself, the subject is figured out from the values of such a property.
   * @return the subject of the media related by this metadata.
   */
  public String getSubject() {
    String subject = "";
    // the Dublin Core subject property is made up of both the subject and the keywords
    List<String> subjectAndKeywords =
        new ArrayList<>(
            Arrays.asList(cleanString(tikaMetadata.getValues(TikaCoreProperties.SUBJECT))));
    List<String> keywords = Arrays.asList(getKeywords());
    subjectAndKeywords.removeAll(keywords);
    if (!subjectAndKeywords.isEmpty()) {
      subject = String.join(", ", subjectAndKeywords);
    }

    if (StringUtil.isNotDefined(subject)) {
      // for old Open XML document
      Property ooXmlSubject = Property.composite(Property.externalText(
              OfficeOpenXMLCore.PREFIX + TikaCoreProperties.NAMESPACE_PREFIX_DELIMITER + "subject"),
          new Property[]{DublinCore.SUBJECT,});
      subject = tikaMetadata.get(ooXmlSubject);
    }

    if (StringUtil.isNotDefined(subject)) {
      subject = String.join(". ", keywords);
    }
    return cleanString(subject);
  }

  /**
   * Gets the initial author, id est the creator, of the media as set in the media's metadata. The
   * author can be a person, an organisation or a service.
   * @return the name of the author of the media related by this metadata.
   */
  public String getAuthor() {
    String author = tikaMetadata.get(TikaCoreProperties.CREATOR);
    if (StringUtil.isNotDefined(author)) {
      author = tikaMetadata.get(Office.AUTHOR);
    }
    return cleanString(author);
  }

  /**
   * Gets the last author (the more recent one), id est the last modifier, of the media as set in
   * the media's metadata. The author can be a person, an organisation or a service.
   * @return the name of the more recent author of the media related by this metadata.
   */
  public String getLastAuthor() {
    String[] authors = tikaMetadata.getValues(TikaCoreProperties.CREATOR);
    if (authors.length > 1) {
      return authors[authors.length - 1];
    }
    return cleanString(tikaMetadata.get(TikaCoreProperties.MODIFIER));
  }

  /**
   * Gets any comments set in the media's metadata.
   * @return the comment on the media related by this metadata.
   */
  public String getComments() {
    String comments = tikaMetadata.get(TikaCoreProperties.COMMENTS);
    if (StringUtil.isNotDefined(comments)) {
      comments = tikaMetadata.get(TikaCoreProperties.DESCRIPTION);
    }
    return cleanString(comments);
  }

  /**
   * Gets the keywords as set in the media's metadata. This property is supported only for office
   * documents.
   * @return an array of keywords qualifying the office document related by this metadata.
   */
  public String[] getKeywords() {
    return cleanString(tikaMetadata.getValues(Office.KEYWORDS));
  }

  /**
   * Gets the Silverpeas identifier of the media related by this metadata.
   * @return the silverpeas identifier.
   */
  public String getSilverId() {
    return cleanString(tikaMetadata.get("SILVERID"));
  }

  /**
   * Gets the name of the media in Silverpeas. The name of the media itself can differ from the name
   * set in Silverpeas by a user for the media related by this metadata.
   * @return the name of the media related by this metadata as set by the user in Silverpeas.
   */
  public String getSilverName() {
    return tikaMetadata.get("SILVERNAME");
  }

  /**
   * Gets the date at which the media related by this metadata has been lastly saved.
   * @return the date of the last modification of the media related by this metadata.
   */
  public Date getLastSaveDateTime() {
    Date date = parseDate(TikaCoreProperties.MODIFIED);
    if (date == null) {
      return parseDate(Office.SAVE_DATE);
    }
    return date;
  }

  /**
   * Gets the date at which the media related by this metadata has been created.
   * @return the date of the creation of the media related by this metadata.
   */
  public Date getCreationDate() {
    Date result = getDate(TikaCoreProperties.CREATED);
    if (result == null) {
      result = tikaMetadata.getDate(TikaCoreProperties.CREATED);
    }
    if (result == null) {
      result = tikaMetadata.getDate(Office.CREATION_DATE);
    }
    return result;
  }

  /**
   * Gets the definition of the media related by this metadata. Only images and videos are supported
   * by this metadata property.
   * @return the image or video definition (id est the width x height of the picture or frame)
   */
  public Definition getDefinition() {
    Definition definition = Definition.fromZero();
    Integer result = getInteger(TIFF.IMAGE_WIDTH);
    if (result == null) {
      result = getInteger(FLV.WIDTH);
    }
    if (result == null) {
      result = getInteger(IPTC.MAX_AVAIL_WIDTH);
    }
    if (result != null) {
      definition.widthOf(result);
    }
    result = getInteger(TIFF.IMAGE_LENGTH);
    if (result == null) {
      result = getInteger(FLV.HEIGHT);
    }
    if (result == null) {
      result = getInteger(IPTC.MAX_AVAIL_HEIGHT);
    }
    if (result != null) {
      definition.heightOf(result);
    }
    return definition;
  }

  /**
   * Gets the frame rate of the media related by this metadata. Only videos are supported by this
   * metadata property.
   * @return the frame rate of the video (id est the number of frames per second)
   */
  public BigDecimal getFramerate() {
    BigDecimal result = getBigDecimal(XMPDM.VIDEO_FRAME_RATE);
    if (result == null) {
      result = getBigDecimal(FLV.FRAMERATE);
    }
    return result;
  }

  /**
   * Gets the duration of the media related by this metadata. Only videos are supported by this
   * metadata property.
   * @return the duration of the video.
   */
  public Duration getDuration() {
    Duration result = null;
    BigDecimal duration = getBigDecimal(XMPDM.DURATION);
    if (duration != null) {
      result = UnitUtil.getDuration(duration);
    }
    if (result == null) {
      duration = getBigDecimal(FLV.DURATION);
      if (duration != null) {
        result = UnitUtil.getDuration(duration, TimeUnit.SECOND);
      }
    }
    return result;
  }

  /**
   * Gets the memory foootprint of the media related by this metadata.
   * @return data of the memory taken by the media (id est the size of the storage space)
   */
  public MemoryData getMemoryData() {
    Long result = getLong(HttpHeaders.CONTENT_LENGTH);
    return result != null ? UnitUtil.getMemData(result) : null;
  }

  private Date getDate(@SuppressWarnings("SameParameterValue") Property property) {
    Date result = tikaMetadata.getDate(property);
    if (result == null) {
      return parseDate(property);
    }
    return result;
  }

  private Integer getInteger(Property property) {
    Integer result = tikaMetadata.getInt(property);
    if (result == null) {
      BigDecimal value = getBigDecimal(property);
      if (value != null) {
        result = value.intValue();
      }
    }
    return result;
  }

  private Long getLong(@SuppressWarnings("SameParameterValue") String propertyName) {
    String result = cleanString(tikaMetadata.get(propertyName));
    try {
      return Long.valueOf(result);
    } catch (Exception e) {
      return null;
    }
  }

  private BigDecimal getBigDecimal(Property property) {
    String result = cleanString(tikaMetadata.get(property));
    try {
      return new BigDecimal(result);
    } catch (Exception e) {
      return null;
    }
  }

  private Date parseDate(Property property) {
    String date = tikaMetadata.get(property);
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
      return uniqueResult.toArray(new String[0]);
    }
    return result;
  }

  private String cleanString(String value) {
    if (StringUtil.isDefined(value)) {
      return value.replace("\u0000", "").replace("ï¿½ï¿½", "").trim();
    }
    return value;
  }

  private static class FLV {

    public static final Property WIDTH = Property.internalText("width");

    public static final Property HEIGHT = Property.internalText("height");

    public static final Property DURATION = Property.internalText("duration");

    public static final Property FRAMERATE = Property.internalText("framerate");
  }
}
