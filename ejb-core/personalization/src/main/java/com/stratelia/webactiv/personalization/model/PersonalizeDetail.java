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
package com.stratelia.webactiv.personalization.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "personalization")
public class PersonalizeDetail implements java.io.Serializable {

  private static final long serialVersionUID = 9192830552642027995L;
  @Id
  private String id;
  @Column(name = "languages")
  private String language = null;
  private String look = null;
  @Column(name = "personalwspace")
  private String collaborativeWorkSpaceId;
  @Column(name = "thesaurusstatus", columnDefinition = "INTEGER")
  private boolean thesaurusStatus;
  @Column(name = "draganddropstatus", columnDefinition = "INTEGER")
  private boolean dragDropStatus;
  @Column(name = "onlineeditingstatus", columnDefinition = "INTEGER")
  private boolean onlineEditingStatus;
  @Column(name = "webdaveditingstatus", columnDefinition = "INTEGER")
  private boolean webdavEditingStatus;

  public PersonalizeDetail() {
  }

  public PersonalizeDetail(String userId, String language, String look,
      String collaborativeWorkSpaceId, boolean thesaurusStatus,
      boolean dragDropStatus, boolean onlineEditingStatus,
      boolean webdavEditingStatus) {
    this(language, look, collaborativeWorkSpaceId, thesaurusStatus, dragDropStatus,
        onlineEditingStatus, webdavEditingStatus);
    this.id = userId;
  }

  public PersonalizeDetail(String language, String look,
      String collaborativeWorkSpaceId, boolean thesaurusStatus,
      boolean dragDropStatus, boolean onlineEditingStatus,
      boolean webdavEditingStatus) {
    this.language = language;
    this.look = look;
    this.collaborativeWorkSpaceId = collaborativeWorkSpaceId;
    this.thesaurusStatus = thesaurusStatus;
    this.dragDropStatus = dragDropStatus;
    this.onlineEditingStatus = onlineEditingStatus;
    this.webdavEditingStatus = webdavEditingStatus;
  }

  public void setLanguage(String language) {
    this.language = language;
  }

  public String getLanguage() {
    return this.language;
  }

  public String getLook() {
    return this.look;
  }

  public void setLook(String look) {
    this.look = look;
  }

  public String getCollaborativeWorkSpaceId() {
    return collaborativeWorkSpaceId;
  }

  public void setCollaborativeWorkSpaceId(String collaborativeWorkSpaceId) {
    this.collaborativeWorkSpaceId = collaborativeWorkSpaceId;
  }

  public String getPersonalWorkSpaceId() {
    return collaborativeWorkSpaceId;
  }

  public void setPersonalWorkSpaceId(String collaborativeWorkSpaceId) {
    this.collaborativeWorkSpaceId = collaborativeWorkSpaceId;
  }

  public boolean getThesaurusStatus() {
    return thesaurusStatus;
  }

  public void setThesaurusStatus(boolean thesaurusStatus) {
    this.thesaurusStatus = thesaurusStatus;
  }

  public boolean getDragAndDropStatus() {
    return dragDropStatus;
  }

  public void setDragAndDropStatus(boolean dragDropStatus) {
    this.dragDropStatus = dragDropStatus;
  }

  public boolean getOnlineEditingStatus() {
    return onlineEditingStatus;
  }

  public void setOnlineEditingStatus(boolean onlineEditingStatus) {
    this.onlineEditingStatus = onlineEditingStatus;
  }

  public boolean isWebdavEditingStatus() {
    return webdavEditingStatus;
  }

  public void setWebdavEditingStatus(boolean webdavEditingStatus) {
    this.webdavEditingStatus = webdavEditingStatus;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    final PersonalizeDetail other = (PersonalizeDetail) obj;
    if ((this.id == null) ? (other.id != null) : !this.id.equals(other.id)) {
      return false;
    }
    if ((this.language == null) ? (other.language != null) : !this.language.equals(other.language)) {
      return false;
    }
    if ((this.look == null) ? (other.look != null) : !this.look.equals(other.look)) {
      return false;
    }
    if ((this.collaborativeWorkSpaceId == null) ? (other.collaborativeWorkSpaceId != null) : !this.collaborativeWorkSpaceId.
        equals(other.collaborativeWorkSpaceId)) {
      return false;
    }
    if (this.thesaurusStatus != other.thesaurusStatus) {
      return false;
    }
    if (this.dragDropStatus != other.dragDropStatus) {
      return false;
    }
    if (this.onlineEditingStatus != other.onlineEditingStatus) {
      return false;
    }
    if (this.webdavEditingStatus != other.webdavEditingStatus) {
      return false;
    }
    return true;
  }

  @Override
  public int hashCode() {
    int hash = 3;
    return hash;
  }

  @Override
  public String toString() {
    return "PersonalizeDetail{" + "id=" + id + ", language=" + language + ", look=" 
        + look + ", collaborativeWorkSpaceId=" + collaborativeWorkSpaceId + ", thesaurusStatus=" 
        + thesaurusStatus + ", dragDropStatus=" + dragDropStatus + ", onlineEditingStatus=" 
        + onlineEditingStatus + ", webdavEditingStatus=" + webdavEditingStatus + '}';
  }
}