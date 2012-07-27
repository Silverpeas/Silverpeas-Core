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
 * "http://www.silverpeas.org/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.stratelia.webactiv.publication.social;

import com.silverpeas.socialnetwork.model.SocialInformation;
import com.silverpeas.socialnetwork.model.SocialInformationType;
import com.stratelia.silverpeas.peasCore.URLManager;
import com.stratelia.webactiv.util.publication.model.PublicationWithStatus;
import java.util.Date;

public class SocialInformationPublication implements SocialInformation {

  private final String type = SocialInformationType.PUBLICATION.toString();
  private PublicationWithStatus publication;
  private String author;
  private Date date;
  private String url;

  /**
   * Constructor with one param
   * @param publication
   */
  public SocialInformationPublication(PublicationWithStatus publication) {
    this.publication = publication;
    if (publication.isUpdate()) {
      this.author = publication.getPublication().getUpdaterId();
      this.date = publication.getPublication().getUpdateDate();
    } else {
      this.author = publication.getPublication().getCreatorId();
      this.date = publication.getPublication().getCreationDate();
    }
    this.url =
        URLManager.getURL("kmelia", null,
        publication.getPublication().getPK().getInstanceId()) +
        publication.getPublication().getURL();

  }

  /**
   * return the icon of this SocialInformation
   * @return String
   */
  @Override
  public String getIcon() {
    if (isUpdeted()) {
      return type + "_update.gif";
    }
    return type + "_new.gif";
  }

  /**
   * return the type of this SocialInformation
   * @return String
   */
  @Override
  public String getType() {
    // TODO Auto-generated method stub
    return type;
  }

  /**
   * return the Title of this SocialInformation
   * @return String
   */
  @Override
  public String getTitle() {
    return publication.getPublication().getTitle();
  }

  /**
   * return the Description of this SocialInformation
   * @return String
   */
  @Override
  public String getDescription() {
    return publication.getPublication().getDescription();
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
   * return the Url of this SocialInfo
   * @return String
   */
  @Override
  public String getUrl() {
    return url;
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
   * return if this socialInfo was updtated or not
   * @return boolean
   */
  @Override
  public boolean isUpdeted() {
    return publication.isUpdate();
  }

  /**
   * compare to SocialInformationPublication if are iquals or not
   * @param obj
   * @return boolean
   */
  @Override
  public boolean equals(Object obj) {
    if (obj == null || getClass() != obj.getClass()) {
      return false;
    }
    final SocialInformationPublication other = (SocialInformationPublication) obj;

    if ((this.type == null) ? (other.type != null) : !this.type.equals(other.type)) {
      return false;
    }
    if ((this.author == null) ? (other.author != null) : !this.author.equals(other.author)) {
      return false;
    }
    if (this.date != other.date && (this.date == null || !this.date.equals(other.date))) {
      return false;
    }
    if ((this.url == null) ? (other.url != null) : !this.url.equals(other.url)) {
      return false;
    }
    if ((this.getTitle() == null) ? (other.getTitle() != null) : !this.getTitle().equals(other.
        getTitle())) {
      return false;
    }
    if ((this.getDescription() == null) ? (other.getDescription() != null) : !this.getDescription()
        .
        equals(other.getDescription())) {
      return false;
    }
    return true;
  }

  @Override
  public int hashCode() {
    int hash = 7;
    hash = 43 * hash + (this.type != null ? this.type.hashCode() : 0);
    hash = 43 * hash + (this.author != null ? this.author.hashCode() : 0);
    hash = 43 * hash + (this.date != null ? this.date.hashCode() : 0);
    hash = 43 * hash + (this.url != null ? this.url.hashCode() : 0);
    return hash;
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
