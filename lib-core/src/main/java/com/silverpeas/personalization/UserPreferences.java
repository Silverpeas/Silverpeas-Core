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
package com.silverpeas.personalization;

import com.silverpeas.personalization.service.PersonalizationService;
import com.silverpeas.util.StringUtil;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "personalization")
public class UserPreferences implements java.io.Serializable {

  private static final long serialVersionUID = 9192830552642027995L;
  @Id
  private String id;
  @Column(name = "languages")
  private String language = null;
  @Column(name = "look")
  private String look = null;
  @Column(name = "personalwspace")
  private String collaborativeWorkSpaceId;
  @Column(name = "thesaurusstatus", columnDefinition = "INTEGER")
  private int thesaurusStatus;
  @Column(name = "draganddropstatus", columnDefinition = "INTEGER")
  private int dragAndDropStatus;
  @Column(name = "webdaveditingstatus", columnDefinition = "INTEGER")
  private int webdavEditionStatus;
  @Column(name = "menuDisplay")
  private String menuDisplay = UserMenuDisplay.DISABLE.name();

  public UserPreferences() {
  }

  public UserPreferences(String userId, String language, String look,
      String collaborativeWorkSpaceId, boolean thesaurusEnabled,
      boolean dragAndDropEnabled, boolean webdavEditionEnabled, UserMenuDisplay display) {
    this(language, look, collaborativeWorkSpaceId, thesaurusEnabled, dragAndDropEnabled,
        webdavEditionEnabled, display);
    this.id = userId;
  }

  public UserPreferences(String language, String look, String collaborativeWorkSpaceId,
      boolean thesaurusEnabled, boolean dragAndDropEnabled, boolean webdavEditionEnabled,
      UserMenuDisplay display) {
    this.language = language;
    this.look = look;
    this.collaborativeWorkSpaceId = collaborativeWorkSpaceId;
    this.thesaurusStatus = thesaurusEnabled ? 1 : 0;
    this.dragAndDropStatus = dragAndDropEnabled ? 1 : 0;
    this.webdavEditionStatus = webdavEditionEnabled ? 1 : 0;
    this.menuDisplay = display.name();
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
    if (!StringUtil.isDefined(look)) {
      this.look = PersonalizationService.DEFAULT_LOOK;
    }
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

  public boolean isThesaurusEnabled() {
    if (1 == thesaurusStatus) {
      return true;
    }
    return false;
  }

  public void enableThesaurus(boolean thesaurusEnabled) {
    this.thesaurusStatus = thesaurusEnabled ? 1 : 0;
  }

  public boolean isDragAndDropEnabled() {
    if (1 == dragAndDropStatus) {
      return true;
    }
    return false;
  }

  public void enableDragAndDrop(boolean dragAndDropEnabled) {
    this.dragAndDropStatus = dragAndDropEnabled ? 1 : 0;
  }

  public boolean isWebdavEditionEnabled() {
    if (1 == webdavEditionStatus) {
      return true;
    }
    return false;
  }

  public void enableWebdavEdition(boolean webdavEditionEnabled) {
    this.webdavEditionStatus = webdavEditionEnabled ? 1 : 0;
  }


  public UserMenuDisplay getDisplay() {
    if (!StringUtil.isDefined(menuDisplay)) {
         this.menuDisplay = UserMenuDisplay.DISABLE.name();
    }
    return UserMenuDisplay.valueOf(menuDisplay);
  }

  public void setDisplay(UserMenuDisplay display) {
    this.menuDisplay = display.name();
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    final UserPreferences other = (UserPreferences) obj;
    if ((this.id == null) ? (other.id != null) : !this.id.equals(other.id)) {
      return false;
    }
    if ((this.language == null) ? (other.language != null) : !this.language.equals(
        other.language)) {
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
    if (this.dragAndDropStatus != other.dragAndDropStatus) {
      return false;
    }
    if (this.webdavEditionStatus != other.webdavEditionStatus) {
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
    return "UserSettings{" + "id=" + id + ", language=" + language + ", look="
        + look + ", collaborativeWorkSpaceId=" + collaborativeWorkSpaceId + ", thesaurusStatus="
        + isThesaurusEnabled() + ", dragDropStatus=" + isDragAndDropEnabled() +
        ", webdavEditingStatus=" + isWebdavEditionEnabled() + '}';
  }
}