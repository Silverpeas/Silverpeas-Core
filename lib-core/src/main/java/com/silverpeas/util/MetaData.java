/*
 * Copyright (C) 2000 - 2011 Silverpeas
 * 
* This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU Affero General Public License as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 * 
* As a special exception to the terms and conditions of version 3.0 of the GPL, you may
 * redistribute this Program in connection with Free/Libre Open Source Software ("FLOSS")
 * applications as described in Silverpeas's FLOSS exception. You should have recieved a copy of
 * the text describing the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.com/legal/licensing"
 * 
* This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See
 * the GNU Affero General Public License for more details.
 * 
* You should have received a copy of the GNU Affero General Public License along with this
 * program. If not, see <http://www.gnu.org/licenses/>.
 * 
*/
package com.silverpeas.util;

import com.stratelia.webactiv.util.DateUtil;
import java.text.ParseException;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.metadata.Property;

public class MetaData {

  private final Metadata metadata;

  MetaData(Metadata metadata) {
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
   * @return String
   */
  public String getTitle() {
    return metadata.get(Metadata.TITLE);
  }

  /**
   * Return Subject of an Office document
   * @return String
   */
  public String getSubject() {
    return metadata.get(Metadata.SUBJECT);
  }

  /**
   * Return Author of an Office document
   * @return String
   */
  public String getAuthor() {
    String author = metadata.get(Metadata.AUTHOR);
    if (author == null) {
      author = metadata.get(Metadata.CREATOR);
    }
    return author;
  }

  /**
   * Return Comments of an Office document
   * @return String
   */
  public String getComments() {
    String comments = metadata.get(Metadata.COMMENTS);
    if (!StringUtil.isDefined(comments)) {
      comments = metadata.get(Metadata.DESCRIPTION);
    }
    return comments;
  }

  /**
   * Return Security of an Office document
   * @return String
   */
  public int getSecurity() {
    return metadata.getInt(Property.internalInteger(Metadata.SECURITY));
  }

  /**
   * Return Keywords of an Office document
   * @return String
   */
  public String getKeywords() {
    return metadata.get(Metadata.KEYWORDS);
  }

  /**
   * Return SILVERID of an Office document
   * @return String
   */
  public String getSilverId() {
    return metadata.get("SILVERID");
  }

  /**
   * Return SILVERNAME of an Office document
   * @return String
   */
  public String getSilverName() {
    return metadata.get("SILVERNAME");
  }

  /**
   * Return LastSaveDateTime of an Office document
   * @return String
   */
  public Date getLastSaveDateTime() {
    String date = metadata.get(Metadata.LAST_SAVED);
    if (date != null) {
      try {
        return DateUtil.parse(date, "yyyy-MM-dd'T'HH:mm:ss'Z'");
      } catch (ParseException ex) {
        return null;
      }
    }
    return null;
  }

  /**
   * Return CreateDateTime of an Office document
   */
  public Date getCreationDate() {
    Date result = metadata.getDate(Metadata.CREATION_DATE);
    if (result == null) {
      String date = metadata.get(Metadata.CREATION_DATE);
      if (date != null) {
        try {
          return DateUtil.parse(date, "yyyy-MM-dd'T'HH:mm:ss'Z'");
        } catch (ParseException ex) {
          return null;
        }
      }
    }
    return result;
  }
}
