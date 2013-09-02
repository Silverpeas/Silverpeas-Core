/**
 * Copyright (C) 2000 - 2012 Silverpeas
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

package com.silverpeas.socialnetwork.model;

import java.util.Date;

/**
 * @author bensalem Nabil
 */
public abstract class AbstractSocialInformation implements SocialInformation {

  protected String title;
  protected String description;
  protected String author;
  protected String url;
  protected Date date;
  protected boolean socialInformationWasupdated;

  @Override
  public String getTitle() {
    return title;
  }

  @Override
  public String getDescription() {
    return description;
  }

  @Override
  public String getAuthor() {
    return author;
  }

  @Override
  public String getUrl() {
    return url;
  }

  @Override
  public Date getDate() {
    return date;
  }

  @Override
  public boolean isUpdeted() {
    return socialInformationWasupdated;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    final AbstractSocialInformation other = (AbstractSocialInformation) obj;
    if ((this.title == null) ? (other.title != null) : !this.title.equals(other.title)) {
      return false;
    }
    if ((this.description == null) ? (other.description != null) : !this.description.equals(
        other.description)) {
      return false;
    }
    if ((this.author == null) ? (other.author != null) : !this.author.equals(other.author)) {
      return false;
    }
    if ((this.url == null) ? (other.url != null) : !this.url.equals(other.url)) {
      return false;
    }
    if (this.date != other.date && (this.date == null || !this.date.equals(other.date))) {
      return false;
    }

    return true;
  }

  @Override
  public int hashCode() {
    int hash = 3;
    hash = 71 * hash + (this.title != null ? this.title.hashCode() : 0);
    hash = 71 * hash + (this.description != null ? this.description.hashCode() : 0);
    hash = 71 * hash + (this.author != null ? this.author.hashCode() : 0);
    hash = 71 * hash + (this.url != null ? this.url.hashCode() : 0);
    hash = 71 * hash + (this.date != null ? this.date.hashCode() : 0);
    return hash;
  }
}
