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
package com.stratelia.webactiv.personalization.model;

import java.util.Vector;

public class PersonalizeDetail implements java.io.Serializable {

  private static final long serialVersionUID = 9192830552642027995L;
  private Vector<String> languages = null;
  private String look = null;
  private String collaborativeWorkSpaceId;
  private boolean thesaurusStatus;
  private boolean dragDropStatus;
  private boolean onlineEditingStatus;
  private boolean webdavEditingStatus;

  public PersonalizeDetail(Vector<String> languages, String look,
      String collaborativeWorkSpaceId, boolean thesaurusStatus,
      boolean dragDropStatus, boolean onlineEditingStatus,
      boolean webdavEditingStatus) {
    this.languages = languages;
    this.look = look;
    this.collaborativeWorkSpaceId = collaborativeWorkSpaceId;
    this.thesaurusStatus = thesaurusStatus;
    this.dragDropStatus = dragDropStatus;
    this.onlineEditingStatus = onlineEditingStatus;
    this.webdavEditingStatus = webdavEditingStatus;
  }

  public void setLanguages(Vector<String> languages) {
    this.languages = languages;
  }

  public Vector<String> getLanguages() {
    return this.languages;
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
}