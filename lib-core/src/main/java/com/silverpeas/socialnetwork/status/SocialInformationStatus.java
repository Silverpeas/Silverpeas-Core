/**
 * Copyright (C) 2000 - 2011 Silverpeas
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
package com.silverpeas.socialnetwork.status;

import java.sql.Timestamp;
import java.util.Date;

import com.silverpeas.socialnetwork.model.SocialInformation;
import com.silverpeas.socialnetwork.model.SocialInformationType;

public class SocialInformationStatus implements SocialInformation {

  private final SocialInformationType type = SocialInformationType.STATUS;
  private String title;
  private String description;
  private String author;
  private Timestamp date;
  private String url;
  private String icon;

  public SocialInformationStatus(Status status) {

    this.description = status.getDescription();
    this.author = Integer.toString(status.getUserId());
    this.title = author;
    this.date = new java.sql.Timestamp(status.getCreationDate().getTime());
    this.url = "#";
    this.icon = type.toString() + ".gif";
  }

  /**
   * return the Author of this SocialInfo
   * @return String
   */
  @Override
  public String getAuthor() {
    return author;
  }

  /**
   * return the Date of this SocialInfo
   * @return
   */
  @Override
  public Date getDate() {
    return date;
  }

  /**
   * return the Description of this SocialInformation
   * @return String
   */
  @Override
  public String getDescription() {
    return description;
  }

  @Override
  public String getIcon() {
    return icon;
  }

  /**
   * return if this socialInfo was updtated or not
   * @return boolean
   */
  @Override
  public boolean isUpdeted() {
    return false;
  }

  /**
   * return the Title of this SocialInformation
   * @return String
   */
  @Override
  public String getTitle() {
    return title;
  }

  /**
   * return the Url of this SocialInfo
   * @return String
   */
  @Override
  public String getUrl() {
    return url;
  }

  @Override
  public String getType() {
    return type.toString();
  }

  /*
   * (non-Javadoc)
   * @see java.lang.Object#hashCode()
   */
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((author == null) ? 0 : author.hashCode());
    result = prime * result + ((date == null) ? 0 : date.hashCode());
    result = prime * result + ((description == null) ? 0 : description.hashCode());
    result = prime * result + ((icon == null) ? 0 : icon.hashCode());
    result = prime * result + ((title == null) ? 0 : title.hashCode());
    result = prime * result + ((type == null) ? 0 : type.hashCode());
    result = prime * result + ((url == null) ? 0 : url.hashCode());
    return result;
  }

  /*
   * (non-Javadoc)
   * @see java.lang.Object#equals(java.lang.Object)
   */
  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (!(obj instanceof SocialInformationStatus)) {
      return false;
    }
    SocialInformationStatus other = (SocialInformationStatus) obj;
    if (author == null) {
      if (other.author != null) {
        return false;
      }
    } else if (!author.equals(other.author)) {
      return false;
    }

    if (date == null) {
      if (other.date != null) {
        return false;
      }
    } else if (!date.equals(other.date)) {
      return false;
    }
    if (description == null) {
      if (other.description != null) {
        return false;
      }
    } else if (!description.equals(other.description)) {
      return false;
    }
    if (icon == null) {
      if (other.icon != null) {
        return false;
      }
    } else if (!icon.equals(other.icon)) {
      return false;
    }
    if (title == null) {
      if (other.title != null) {
        return false;
      }
    } else if (!title.equals(other.title)) {
      return false;
    }
    if (type == null) {
      if (other.type != null) {
        return false;
      }
    } else if (type != other.type) {
      return false;
    }
    if (url == null) {
      if (other.url != null) {
        return false;
      }
    } else if (!url.equals(other.url)) {
      return false;
    }
    return true;
  }

  /**
   *Indicates whether some other SocialInformation date is befor the date of this one.
   *@param obj the reference object with which to compare.
   * @return int
   */
  @Override
  public int compareTo(SocialInformation o) {
    return getDate().compareTo(o.getDate()) * -1;
  }
}
