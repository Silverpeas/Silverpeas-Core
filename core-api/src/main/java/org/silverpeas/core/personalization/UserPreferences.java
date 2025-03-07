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
package org.silverpeas.core.personalization;

import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.persistence.datasource.model.identifier.ExternalStringIdentifier;
import org.silverpeas.core.persistence.datasource.model.jpa.BasicJpaEntity;
import org.silverpeas.core.personalization.service.PersonalizationService;
import org.silverpeas.kernel.util.StringUtil;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import java.io.Serializable;
import java.time.ZoneId;
import java.util.Objects;

@Entity
@Table(name = "personalization")
@NamedQuery(name = "UserPreferences.findByDefaultSpace",
    query = "select p from UserPreferences p WHERE p.collaborativeWorkSpaceId = :space")
public class UserPreferences
    extends BasicJpaEntity<UserPreferences, ExternalStringIdentifier>
    implements Serializable {
  private static final long serialVersionUID = 9192830552642027995L;

  @Column(name = "languages")
  private String language = null;
  @Column(name = "zoneId")
  private String zoneId = null;
  @Column(name = "look")
  private String look = null;
  @Column(name = "personalwspace")
  private String collaborativeWorkSpaceId;
  @Column(name = "thesaurusstatus")
  private int thesaurusStatus = 0;
  @Column(name = "draganddropstatus")
  private int dragAndDropStatus = 0;
  @Column(name = "webdaveditingstatus")
  private int webdavEditionStatus = 0;
  @Column(name = "menuDisplay")
  private String menuDisplay = UserMenuDisplay.DEFAULT.name();

  public UserPreferences() {
  }

  public UserPreferences(String userId, String language, final ZoneId zoneId) {
    setId(userId);
    this.language = language;
    this.zoneId = zoneId.toString();
  }

  public User getUser() {
    return User.getById(getId());
  }

  public void setLanguage(String language) {
    this.language = language;
  }

  public String getLanguage() {
    return this.language;
  }

  public ZoneId getZoneId() {
    return ZoneId.of(zoneId);
  }

  public void setZoneId(final ZoneId zoneId) {
    this.zoneId = zoneId.toString();
  }

  public String getLook() {
    return this.look;
  }

  public void setLook(String look) {
    if (!StringUtil.isDefined(look)) {
      this.look = PersonalizationService.DEFAULT_LOOK;
    } else {
      this.look = look;
    }
  }

  public String getPersonalWorkSpaceId() {
    return collaborativeWorkSpaceId;
  }

  public void setPersonalWorkSpaceId(String collaborativeWorkSpaceId) {
    this.collaborativeWorkSpaceId = collaborativeWorkSpaceId;
  }

  public boolean isThesaurusEnabled() {
    return 1 == thesaurusStatus;
  }

  public void enableThesaurus(boolean thesaurusEnabled) {
    this.thesaurusStatus = thesaurusEnabled ? 1 : 0;
  }

  public boolean isDragAndDropEnabled() {
    return 1 == dragAndDropStatus;
  }

  public void enableDragAndDrop(boolean dragAndDropEnabled) {
    this.dragAndDropStatus = dragAndDropEnabled ? 1 : 0;
  }

  public boolean isWebdavEditionEnabled() {
    return 1 == webdavEditionStatus;
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
    if ((getId() == null) ? (other.getId() != null) : !getId().equals(other.getId())) {
      return false;
    }
    if (!Objects.equals(this.language, other.language)) {
      return false;
    }
    if (!Objects.equals(this.zoneId, other.zoneId)) {
      return false;
    }
    if (!Objects.equals(this.look, other.look)) {
      return false;
    }
    if (!Objects.equals(this.collaborativeWorkSpaceId, other.collaborativeWorkSpaceId)) {
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
    return this.getDisplay() == other.getDisplay();
  }

  @Override
  public int hashCode() {
    int result = getId() != null ? getId().hashCode() : 0;
    result = 31 * result + (language != null ? language.hashCode() : 0);
    result = 31 * result + (zoneId != null ? zoneId.hashCode() : 0);
    result = 31 * result + (look != null ? look.hashCode() : 0);
    result =
        31 * result + (collaborativeWorkSpaceId != null ? collaborativeWorkSpaceId.hashCode() : 0);
    result = 31 * result + thesaurusStatus;
    result = 31 * result + dragAndDropStatus;
    result = 31 * result + webdavEditionStatus;
    result = 31 * result + (menuDisplay != null ? menuDisplay.hashCode() : 0);
    return result;
  }

  @Override
  public String toString() {
    return "UserSettings{" + "id=" + getId() + ", language=" + language + ", look=" + look +
        ", collaborativeWorkSpaceId=" + collaborativeWorkSpaceId + ", thesaurusStatus=" +
        isThesaurusEnabled() + ", dragDropStatus=" + isDragAndDropEnabled() +
        ", webdavEditingStatus=" + isWebdavEditionEnabled() + ", display=" + getDisplay() + '}';
  }
}