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

public class PersonalizeDetail implements java.io.Serializable {

  private static final long serialVersionUID = 9192830552642027995L;
  private String language = null;
  private String look = null;
  private String collaborativeWorkSpaceId;
  private boolean thesaurusStatus;
  private boolean dragDropStatus;
  private boolean onlineEditingStatus;
  private boolean webdavEditingStatus;

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
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    PersonalizeDetail that = (PersonalizeDetail) o;

    if (dragDropStatus != that.dragDropStatus) {
      return false;
    }
    if (onlineEditingStatus != that.onlineEditingStatus) {
      return false;
    }
    if (thesaurusStatus != that.thesaurusStatus) {
      return false;
    }
    if (webdavEditingStatus != that.webdavEditingStatus) {
      return false;
    }
    if (collaborativeWorkSpaceId != null ? !collaborativeWorkSpaceId.equals(
        that.collaborativeWorkSpaceId) : that.collaborativeWorkSpaceId != null) {
      return false;
    }
    if (language != null ? !language.equals(that.language) : that.language != null) {
      return false;
    }
    if (look != null ? !look.equals(that.look) : that.look != null) {
      return false;
    }

    return true;
  }

  @Override
  public int hashCode() {
    int result = language != null ? language.hashCode() : 0;
    result = 31 * result + (look != null ? look.hashCode() : 0);
    result = 31 * result + (collaborativeWorkSpaceId != null ? collaborativeWorkSpaceId.hashCode() : 0);
    result = 31 * result + (thesaurusStatus ? 1 : 0);
    result = 31 * result + (dragDropStatus ? 1 : 0);
    result = 31 * result + (onlineEditingStatus ? 1 : 0);
    result = 31 * result + (webdavEditingStatus ? 1 : 0);
    return result;
  }

  @Override
  public String toString() {
    return "PersonalizeDetail{" +
        "language=" + language +
        ", look='" + look + '\'' +
        ", collaborativeWorkSpaceId='" + collaborativeWorkSpaceId + '\'' +
        ", thesaurusStatus=" + thesaurusStatus +
        ", dragDropStatus=" + dragDropStatus +
        ", onlineEditingStatus=" + onlineEditingStatus +
        ", webdavEditingStatus=" + webdavEditingStatus +
        '}';
  }
}