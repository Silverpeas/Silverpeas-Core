/**
 * Copyright (C) 2000 - 2009 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have recieved a copy of the text describing
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
package com.stratelia.webactiv.publication.socialNetwork;


import com.silverpeas.socialNetwork.model.SocialInformation;
import com.silverpeas.socialNetwork.model.SocialInformationType;
import com.stratelia.silverpeas.peasCore.URLManager;
import com.stratelia.webactiv.util.publication.model.PublicationWithStatus;
import java.sql.Timestamp;
import java.util.Date;

public class SocialInformationPublication implements SocialInformation {

  
  private final String type = SocialInformationType.PUBLICATION.toString();
  private  PublicationWithStatus publication;
  private  String author;
  private  Timestamp date;
  private  String url;
  
  public SocialInformationPublication (PublicationWithStatus publication) {
    this.publication=publication;
    if(publication.isUpdate()){
    this.author = publication.getPublication().getUpdaterId();
    this.date = new java.sql.Timestamp(publication.getPublication().getUpdateDate().getTime());
    }else{
      this.author = publication.getPublication().getCreatorId();
      this.date = new java.sql.Timestamp(publication.getPublication().getCreationDate().getTime());
    }
this.url=URLManager.getURL("kmelia", null, publication.getPublication().getPK().getInstanceId())+publication.getPublication().getURL();
  //  this.url = publication.getPublication().getInstanceId()+"/"+ publication.getPublication().getURL();
  
}
  
  
  @Override
  public String getIcon() {
      if (getSocialInformationWasUpdeted()) {
          return type + "_update.gif";
      } 
          return type + "_new.gif";
  }

  @Override
  public String getType() {
    // TODO Auto-generated method stub
    return type;
  }


  /* (non-Javadoc)
   * @see java.lang.Object#hashCode()
   */
  
  



  @Override
  public String getTitle() {
    return publication.getPublication().getTitle();
  }

  @Override
  public String getDescription() {
    return publication.getPublication().getDescription();
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
  public boolean getSocialInformationWasUpdeted() {
    return publication.isUpdate();
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
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
    if ((this.getTitle() == null) ? (other.getTitle() != null) : !this.getTitle().equals(other.getTitle())) {
      return false;
    }
    if ((this.getDescription() == null) ? (other.getDescription() != null) : !this.getDescription().equals(other.getDescription())) {
      return false;
    }
    
    return true;
  }

  @Override
  public int hashCode() {
    int hash = 7;
    return hash;
  }
  
    @Override
  public int compareTo(SocialInformation o) {
    return getDate().compareTo(o.getDate())*-1;
  }

}
