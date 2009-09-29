package com.silverpeas.myLinks.model;

import java.io.Serializable;

public class LinkDetail implements Serializable {
  private int linkId;
  private String name;
  private String description;
  private String url;
  private boolean visible;
  private boolean popup;
  private String userId;
  private String instanceId;
  private String objectId;

  public LinkDetail() {

  }

  public LinkDetail(String name, String description, String url,
      boolean visible, boolean popup) {
    this.name = name;
    this.description = description;
    this.url = url;
    this.visible = visible;
    this.popup = popup;

  }

  /*
   * public LinkDetail(String name, String description, String url, boolean
   * visible, boolean popup, String instanceId) { this.name = name;
   * this.description = description; this.url = url; this.visible = visible;
   * this.popup = popup; this.instanceId = instanceId; }
   */

  public String getUserId() {
    return userId;
  }

  public void setUserId(String userId) {
    this.userId = userId;
  }

  public int getLinkId() {
    return linkId;
  }

  public void setLinkId(int linkId) {
    this.linkId = linkId;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getUrl() {
    return url;
  }

  public void setUrl(String url) {
    this.url = url;
  }

  public boolean isVisible() {
    return visible;
  }

  public void setVisible(boolean visible) {
    this.visible = visible;
  }

  public boolean isPopup() {
    return popup;
  }

  public void setPopup(boolean popup) {
    this.popup = popup;
  }

  public String getInstanceId() {
    return instanceId;
  }

  public void setInstanceId(String instanceId) {
    this.instanceId = instanceId;
  }

  public String getObjectId() {
    return objectId;
  }

  public void setObjectId(String objectId) {
    this.objectId = objectId;
  }

}