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
package org.silverpeas.core.socialnetwork.model;

import org.silverpeas.core.ResourceReference;
import org.silverpeas.core.date.DateTime;

import java.util.Date;
import java.util.Objects;

/**
 * @author bensalem Nabil
 */
public abstract class AbstractSocialInformation implements SocialInformation {

  protected final ResourceReference resourceReference;
  protected String title;
  protected String description;
  protected String author;
  protected String url;
  protected Date date;
  protected boolean socialInformationWasupdated;
  protected String type;
  protected String icon;

  protected AbstractSocialInformation(final ResourceReference resourceReference) {
    this.resourceReference = resourceReference;
  }

  @Override
  public ResourceReference getResourceReference() {
    return resourceReference;
  }

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
  public boolean isUpdated() {
    return socialInformationWasupdated;
  }

  public void setUpdated(boolean updated) {
    socialInformationWasupdated = updated;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public void setAuthor(String author) {
    this.author = author;
  }

  public void setUrl(String url) {
    this.url = url;
  }

  public void setDate(Date date) {
    this.date = date;
  }

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  @Override
  public String getIcon() {
    if (icon != null) {
      return icon;
    }
    if (isUpdated()) {
      return type + "_update.gif";
    }
    return type + "_new.gif";
  }

  public void setIcon(String icon) {
    this.icon = icon;
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
    if (!Objects.equals(this.title, other.title)) {
      return false;
    }
    if (!Objects.equals(this.description, other.description)) {
      return false;
    }
    if (!Objects.equals(this.author, other.author)) {
      return false;
    }
    if (!Objects.equals(this.url, other.url)) {
      return false;
    }
    if (!Objects.equals(this.date, other.date)) {
      return false;
    }
    return Objects.equals(this.resourceReference, other.resourceReference);
  }

  @Override
  public int hashCode() {
    int hash = 3;
    hash = 71 * hash + (this.title != null ? this.title.hashCode() : 0);
    hash = 71 * hash + (this.description != null ? this.description.hashCode() : 0);
    hash = 71 * hash + (this.author != null ? this.author.hashCode() : 0);
    hash = 71 * hash + (this.url != null ? this.url.hashCode() : 0);
    hash = 71 * hash + (this.date != null ? this.date.hashCode() : 0);
    hash = 71 * hash + (this.resourceReference != null ? this.resourceReference.hashCode() : 0);
    return hash;
  }

  /**
   *Indicates whether some other SocialInformation date is before the date of this one.
   * @param socialInfo the reference object with which to compare.
   * @return int
   */
  @Override
  public int compareTo(SocialInformation socialInfo) {
    DateTime myDate = new DateTime(getDate());
    DateTime otherDate = new DateTime(socialInfo.getDate());

    // First sorting on date (and not the time)
    int result = otherDate.getBeginOfDay().compareTo(myDate.getBeginOfDay());
    if (result == 0) {
      result = compareByUrl(socialInfo);
    }
    if (result == 0) {
      // Then put update before the creation
      if (isUpdated() && !socialInfo.isUpdated()) {
        result = -1;
      } else if (!isUpdated() && socialInfo.isUpdated()) {
        result = 1;
      } else {
        result = socialInfo.getResourceReference().asString().compareTo(getResourceReference().asString());
      }
    }
    return result;
  }

  protected int compareByUrl(final SocialInformation socialInfo) {
    int result = getUrl().compareTo(socialInfo.getUrl());
    if (result == 0) {
      // Then put resource comments before the resource itself
      boolean myIsComment = getType().contains("COMMENT");
      boolean otherIsComment = socialInfo.getType().contains("COMMENT");
      if ((!myIsComment && otherIsComment) || (myIsComment && !otherIsComment)) {
        result = myIsComment ? -1 : 1;
      }
    }
    return result;
  }
}
