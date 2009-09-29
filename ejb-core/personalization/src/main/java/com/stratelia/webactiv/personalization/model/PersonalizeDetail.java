package com.stratelia.webactiv.personalization.model;

import java.util.Vector;

public class PersonalizeDetail implements java.io.Serializable {

  private Vector languages = null;
  private String look = null;
  private String collaborativeWorkSpaceId;
  private boolean thesaurusStatus;
  private boolean dragDropStatus;
  private boolean onlineEditingStatus;
  private boolean webdavEditingStatus;

  public PersonalizeDetail(Vector languages, String look,
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

  public void setLanguages(Vector languages) {
    this.languages = languages;
  }

  public Vector getLanguages() {
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